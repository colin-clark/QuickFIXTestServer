package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.Column;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.KSMetaData;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryFilter;
import com.cep.messaging.impls.gossip.keyspace.filter.QueryPath;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.partitioning.token.DecoratedKey;
import com.cep.messaging.impls.gossip.service.MigrationManager;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.UUIDGen;
import com.cep.messaging.util.exception.ConfigurationException;

/**
 * A migration represents a single metadata mutation (cf dropped, added, etc.).  Migrations can be applied locally, or
 * serialized and sent to another machine where it can be applied there. Each migration has a version represented by
 * a TimeUUID that can be used to look up both the Migration itself (see getLocalMigrations) as well as a serialization
 * of the Keyspace definition that was modified.
 * 
 * There are three parts to a migration (think of it as a schema update):
 * 1. data is written to the schema cf.
 * 2. the migration is serialized to the migrations cf.
 * 3. updated models are applied to the DarkStar instance.
 * 
 * Since steps 1, 2 and 3 are not committed atomically, care should be taken to ensure that a node/cluster is reasonably
 * quiescent with regard to the keyspace or columnfamily whose schema is being modified.
 * 
 * Each class that extends Migration is required to implement a no arg constructor, which will be used to inflate the
 * object from it's serialized form.
 */

@SuppressWarnings({"rawtypes"})
public abstract class Migration
{
    protected static final Logger logger = LoggerFactory.getLogger(Migration.class);
    
    public static final String NAME_VALIDATOR_REGEX = "\\w+";
    public static final String MIGRATIONS_CF = "Migrations";
    public static final String SCHEMA_CF = "Schema";
    public static final ByteBuffer MIGRATIONS_KEY = ByteBufferUtil.bytes("Migrations Key");
    public static final ByteBuffer LAST_MIGRATION_KEY = ByteBufferUtil.bytes("Last Migration");
    
    protected RowMutation rm;
    protected UUID newVersion;
    protected UUID lastVersion;

    // the migration in column form, used when announcing to others
    private IColumn column;

    /** Subclasses must have a matching constructor */
    protected Migration() { }

    Migration(UUID newVersion, UUID lastVersion)
    {
        this();
        this.newVersion = newVersion;
        this.lastVersion = lastVersion;
    }
    
    /** apply changes */
	public final void apply() throws IOException, ConfigurationException
    {
        // ensure migration is serial. don't apply unless the previous version matches.
        if (!NodeDescriptor.getDefsVersion().equals(lastVersion))
            throw new ConfigurationException("Previous version mismatch. cannot apply.");
        if (newVersion.timestamp() <= lastVersion.timestamp())
            throw new ConfigurationException("New version timestamp is not newer than the current version timestamp.");
        // write to schema
        assert rm != null;
        if (!StorageService.instance.isClientMode())
        {
            rm.apply();

            long now = System.currentTimeMillis();
            ByteBuffer buf = serialize();
            RowMutation migration = new RowMutation(Table.SYSTEM_TABLE, MIGRATIONS_KEY);
            ColumnFamily cf = ColumnFamily.create(Table.SYSTEM_TABLE, MIGRATIONS_CF);
            column = new Column(ByteBuffer.wrap(UUIDGen.decompose(newVersion)), buf, now);
            cf.addColumn(column);
            migration.add(cf);
            migration.apply();
            
            // note that we're storing this in the system table, which is not replicated
            logger.info("Applying migration {} {}", newVersion.toString(), toString());
            migration = new RowMutation(Table.SYSTEM_TABLE, LAST_MIGRATION_KEY);
            migration.add(new QueryPath(SCHEMA_CF, null, LAST_MIGRATION_KEY), ByteBuffer.wrap(UUIDGen.decompose(newVersion)), now);
            migration.apply();

            // if we fail here, there will be schema changes in the CL that will get replayed *AFTER* the schema is loaded.
            // DarkStarMessagingDaemon checks for this condition (the stored version will be greater than the loaded version)
            // and calls MigrationManager.applyMigrations(loaded version, stored version).
        
            // flush changes out of memtables so we don't need to rely on the commit log.
            ColumnFamilyStore[] schemaStores = new ColumnFamilyStore[] {
                Table.open(Table.SYSTEM_TABLE).getColumnFamilyStore(Migration.MIGRATIONS_CF),
                Table.open(Table.SYSTEM_TABLE).getColumnFamilyStore(Migration.SCHEMA_CF)
            };
            List<Future> flushes = new ArrayList<Future>();
            for (ColumnFamilyStore cfs : schemaStores)
                flushes.add(cfs.forceFlush());
            for (Future f : flushes)
            {
                if (f == null)
                    // applying the migration triggered a flush independently
                    continue;
                try
                {
                    f.get();
                }
                catch (ExecutionException e)
                {
                    throw new IOException(e);
                }
                catch (InterruptedException e)
                {
                    throw new IOException(e);
                }
            }
        }
        
        applyModels(); 
    }

    /** send this migration immediately to existing nodes in the cluster.  apply() must be called first. */
    public final void announce()
    {
        assert !StorageService.instance.isClientMode();
        assert column != null;
        MigrationManager.announce(column);
    }

    public final void passiveAnnounce()
    {
        MigrationManager.passiveAnnounce(newVersion);
    }

    public static UUID getLastMigrationId()
    {
        DecoratedKey dkey = StorageService.getPartitioner().decorateKey(LAST_MIGRATION_KEY);
        Table defs = Table.open(Table.SYSTEM_TABLE);
        ColumnFamilyStore cfStore = defs.getColumnFamilyStore(SCHEMA_CF);
        QueryFilter filter = QueryFilter.getNamesFilter(dkey, new QueryPath(SCHEMA_CF), LAST_MIGRATION_KEY);
        ColumnFamily cf = cfStore.getColumnFamily(filter);
        if (cf == null || cf.getColumnNames().size() == 0)
            return null;
        else
            return UUIDGen.getUUID(cf.getColumn(LAST_MIGRATION_KEY).value());
    }
    
    /** keep in mind that applyLive might happen on another machine */
    abstract void applyModels() throws IOException;
    
    public UUID getVersion()
    {
        return newVersion;
    }

    /**
     * Definitions are serialized as a row with a UUID key, with a magical column named DEFINITION_SCHEMA_COLUMN_NAME
     * (containing the Avro Schema) and a column per keyspace. Each keyspace column contains a avro.KsDef object
     * encoded with the Avro schema.
     */
    static RowMutation makeDefinitionMutation(KSMetaData add, KSMetaData remove, UUID versionId) throws IOException
    {
        return null;
    }
        
    public ByteBuffer serialize() throws IOException
    {
    	return null;
    }

    public static Migration deserialize(ByteBuffer bytes, int version) throws IOException
    {
    	return null;
    }
    
    /** load serialized migrations. */
    public static Collection<IColumn> getLocalMigrations(UUID start, UUID end)
    {
        DecoratedKey dkey = StorageService.getPartitioner().decorateKey(MIGRATIONS_KEY);
        Table defs = Table.open(Table.SYSTEM_TABLE);
        ColumnFamilyStore cfStore = defs.getColumnFamilyStore(Migration.MIGRATIONS_CF);
        QueryFilter filter = QueryFilter.getSliceFilter(dkey,
                                                        new QueryPath(MIGRATIONS_CF),
                                                        ByteBuffer.wrap(UUIDGen.decompose(start)),
                                                        ByteBuffer.wrap(UUIDGen.decompose(end)),
                                                        false,
                                                        100);
        ColumnFamily cf = cfStore.getColumnFamily(filter);
        return cf.getSortedColumns();
    }
    
    public static ByteBuffer toUTF8Bytes(UUID version)
    {
        return ByteBufferUtil.bytes(version.toString());
    }
    
    public static boolean isLegalName(String s)
    {
        return s.matches(Migration.NAME_VALIDATOR_REGEX);
    }
}

package com.cep.messaging.impls.gossip.thrift;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.commands.Migration;
import com.cep.messaging.impls.gossip.keyspace.marshal.MarshalException;
import com.cep.messaging.impls.gossip.keyspace.ColumnDefinition;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyType;
import com.cep.messaging.impls.gossip.keyspace.IColumn;
import com.cep.messaging.impls.gossip.keyspace.KSMetaData;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.marshal.AbstractType;
import com.cep.messaging.impls.gossip.keyspace.marshal.AsciiType;
import com.cep.messaging.impls.gossip.keyspace.marshal.TypeParser;
import com.cep.messaging.impls.gossip.net.IEndpointSnitch;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.partitioning.IPartitioner;
import com.cep.messaging.impls.gossip.partitioning.RandomPartitioner;
import com.cep.messaging.impls.gossip.partitioning.token.Token;
import com.cep.messaging.impls.gossip.partitioning.token.TokenMetadata;
import com.cep.messaging.impls.gossip.replication.AbstractReplicationStrategy;
import com.cep.messaging.impls.gossip.replication.NetworkTopologyStrategy;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.util.exception.ConfigurationException;
import com.cep.messaging.util.exception.KeyspaceNotDefinedException;

/**
 * This has a lot of building blocks for DarkStarMessagingServer to call to make sure it has valid input
 * -- ensuring column names conform to the declared comparator, for instance.
 *
 * The methods here mostly try to do just one part of the validation so they can be combined
 * for different needs -- supercolumns vs regular, range slices vs named, batch vs single-column.
 * (ValidateColumnPath is the main exception in that it includes table and CF validation.)
 */
public class ThriftValidation
{
    public static void validateKey(CFMetaData metadata, ByteBuffer key) throws InvalidRequestException
    {
        if (key == null || key.remaining() == 0)
        {
            throw new InvalidRequestException("Key may not be empty");
        }

        // check that key can be handled by GossipUtilities.writeShortByteArray
        if (key.remaining() > GossipUtilities.MAX_UNSIGNED_SHORT)
        {
            throw new InvalidRequestException("Key length of " + key.remaining() +
                                              " is longer than maximum of " + GossipUtilities.MAX_UNSIGNED_SHORT);
        }

        try
        {
            metadata.getKeyValidator().validate(key);
        }
        catch (MarshalException e)
        {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static void validateTable(String tablename) throws KeyspaceNotDefinedException
    {
        if (!NodeDescriptor.getTables().contains(tablename))
        {
            throw new KeyspaceNotDefinedException("Keyspace " + tablename + " does not exist");
        }
    }

    // Don't check that the table exists, validateTable or validateColumnFamily must be called beforehand.
    public static void validateConsistencyLevel(String table, ConsistencyLevel cl) throws InvalidRequestException
    {
        switch (cl)
        {
            case LOCAL_QUORUM:
            case EACH_QUORUM:
                AbstractReplicationStrategy strategy = Table.open(table).getReplicationStrategy();
                if (!(strategy instanceof NetworkTopologyStrategy))
                    throw new InvalidRequestException("consistency level " + cl + " not compatible with replication strategy (" + strategy.getClass().getName() + ")");
        }
    }

    public static CFMetaData validateColumnFamily(String tablename, String cfName, boolean isCommutativeOp) throws InvalidRequestException
    {
        CFMetaData metadata = validateColumnFamily(tablename, cfName);

        if (isCommutativeOp)
        {
            if (!metadata.getDefaultValidator().isCommutative())
                throw new InvalidRequestException("invalid operation for non commutative columnfamily " + cfName);
        }
        else
        {
            if (metadata.getDefaultValidator().isCommutative())
                throw new InvalidRequestException("invalid operation for commutative columnfamily " + cfName);
        }
        return metadata;
    }

    // To be used when the operation should be authorized whether this is a counter CF or not
    public static CFMetaData validateColumnFamily(String tablename, String cfName) throws InvalidRequestException
    {
        validateTable(tablename);
        if (cfName.isEmpty())
            throw new InvalidRequestException("non-empty columnfamily is required");

        CFMetaData metadata = NodeDescriptor.getCFMetaData(tablename, cfName);
        if (metadata == null)
            throw new InvalidRequestException("unconfigured columnfamily " + cfName);

        return metadata;
    }

    /**
     * validates all parts of the path to the column, including the column name
     */
    public static void validateColumnPath(CFMetaData metadata, ColumnPath column_path) throws InvalidRequestException
    {
        if (metadata.cfType == ColumnFamilyType.Standard)
        {
            if (column_path.super_column != null)
            {
                throw new InvalidRequestException("supercolumn parameter is invalid for standard CF " + metadata.cfName);
            }
            if (column_path.column == null)
            {
                throw new InvalidRequestException("column parameter is not optional for standard CF " + metadata.cfName);
            }
        }
        else
        {
            if (column_path.super_column == null)
                throw new InvalidRequestException("supercolumn parameter is not optional for super CF " + metadata.cfName);
        }
        if (column_path.column != null)
        {
            validateColumnNames(metadata, column_path.super_column, Arrays.asList(column_path.column));
        }
        if (column_path.super_column != null)
        {
            validateColumnNames(metadata, (ByteBuffer)null, Arrays.asList(column_path.super_column));
        }
    }

    public static void validateColumnParent(CFMetaData metadata, ColumnParent column_parent) throws InvalidRequestException
    {
        if (metadata.cfType == ColumnFamilyType.Standard)
        {
            if (column_parent.super_column != null)
            {
                throw new InvalidRequestException("columnfamily alone is required for standard CF " + metadata.cfName);
            }
        }

        if (column_parent.super_column != null)
        {
            validateColumnNames(metadata, (ByteBuffer)null, Arrays.asList(column_parent.super_column));
        }
    }

    // column_path_or_parent is a ColumnPath for remove, where the "column" is optional even for a standard CF
    static void validateColumnPathOrParent(CFMetaData metadata, ColumnPath column_path_or_parent) throws InvalidRequestException
    {
        if (metadata.cfType == ColumnFamilyType.Standard)
        {
            if (column_path_or_parent.super_column != null)
            {
                throw new InvalidRequestException("supercolumn may not be specified for standard CF " + metadata.cfName);
            }
        }
        if (metadata.cfType == ColumnFamilyType.Super)
        {
            if (column_path_or_parent.super_column == null && column_path_or_parent.column != null)
            {
                throw new InvalidRequestException("A column cannot be specified without specifying a super column for removal on super CF " + metadata.cfName);
            }
        }
        if (column_path_or_parent.column != null)
        {
            validateColumnNames(metadata, column_path_or_parent.super_column, Arrays.asList(column_path_or_parent.column));
        }
        if (column_path_or_parent.super_column != null)
        {
            validateColumnNames(metadata, (ByteBuffer)null, Arrays.asList(column_path_or_parent.super_column));
        }
    }

    /**
     * Validates the column names but not the parent path or data
     */
    @SuppressWarnings("rawtypes")
	private static void validateColumnNames(CFMetaData metadata, ByteBuffer superColumnName, Iterable<ByteBuffer> column_names)
            throws InvalidRequestException
    {
        if (superColumnName != null)
        {
            if (superColumnName.remaining() > IColumn.MAX_NAME_LENGTH)
                throw new InvalidRequestException("supercolumn name length must not be greater than " + IColumn.MAX_NAME_LENGTH);
            if (superColumnName.remaining() == 0)
                throw new InvalidRequestException("supercolumn name must not be empty");
            if (metadata.cfType == ColumnFamilyType.Standard)
                throw new InvalidRequestException("supercolumn specified to ColumnFamily " + metadata.cfName + " containing normal columns");
        }
        AbstractType comparator = metadata.getComparatorFor(superColumnName);
        for (ByteBuffer name : column_names)
        {
            if (name.remaining() > IColumn.MAX_NAME_LENGTH)
                throw new InvalidRequestException("column name length must not be greater than " + IColumn.MAX_NAME_LENGTH);
            if (name.remaining() == 0)
                throw new InvalidRequestException("column name must not be empty");
            try
            {
                comparator.validate(name);
            }
            catch (MarshalException e)
            {
                throw new InvalidRequestException(e.getMessage());
            }
        }
    }

    public static void validateColumnNames(CFMetaData metadata, ColumnParent column_parent, Iterable<ByteBuffer> column_names) throws InvalidRequestException
    {
        validateColumnNames(metadata, column_parent.super_column, column_names);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void validateRange(CFMetaData metadata, ColumnParent column_parent, SliceRange range) throws InvalidRequestException
    {
        AbstractType comparator = metadata.getComparatorFor(column_parent.super_column);
        try
        {
            comparator.validate(range.start);
            comparator.validate(range.finish);
        }
        catch (MarshalException e)
        {
            throw new InvalidRequestException(e.getMessage());
        }

        if (range.count < 0)
            throw new InvalidRequestException("get_slice requires non-negative count");

        Comparator<ByteBuffer> orderedComparator = range.isReversed() ? comparator.reverseComparator : comparator;
        if (range.start.remaining() > 0
            && range.finish.remaining() > 0
            && orderedComparator.compare(range.start, range.finish) > 0)
        {
            throw new InvalidRequestException("range finish must come after start in the order of traversal");
        }
    }

    public static void validateColumnOrSuperColumn(CFMetaData metadata, ColumnOrSuperColumn cosc)
            throws InvalidRequestException
    {
        boolean isCommutative = metadata.getDefaultValidator().isCommutative();

        int nulls = 0;
        if (cosc.column == null) nulls++;
        if (cosc.super_column == null) nulls++;
        if (cosc.counter_column == null) nulls++;
        if (cosc.counter_super_column == null) nulls++;

        if (nulls != 3)
            throw new InvalidRequestException("ColumnOrSuperColumn must have one (and only one) of column, super_column, counter and counter_super_column");

        if (cosc.column != null)
        {
            if (isCommutative)
                throw new InvalidRequestException("invalid operation for commutative columnfamily " + metadata.cfName);

            validateTtl(cosc.column);
            validateColumnPath(metadata, new ColumnPath(metadata.cfName).setSuper_column((ByteBuffer)null).setColumn(cosc.column.name));
            validateColumnData(metadata, cosc.column);
        }

        if (cosc.super_column != null)
        {
            if (isCommutative)
                throw new InvalidRequestException("invalid operation for commutative columnfamily " + metadata.cfName);

            for (Column c : cosc.super_column.columns)
            {
                validateColumnPath(metadata, new ColumnPath(metadata.cfName).setSuper_column(cosc.super_column.name).setColumn(c.name));
                validateColumnData(metadata, c);
            }
        }

        if (cosc.counter_column != null)
        {
            if (!isCommutative)
                throw new InvalidRequestException("invalid operation for non commutative columnfamily " + metadata.cfName);

            validateColumnPath(metadata, new ColumnPath(metadata.cfName).setSuper_column((ByteBuffer)null).setColumn(cosc.counter_column.name));
        }

        if (cosc.counter_super_column != null)
        {
            if (!isCommutative)
                throw new InvalidRequestException("invalid operation for non commutative columnfamily " + metadata.cfName);

            for (CounterColumn c : cosc.counter_super_column.columns)
                validateColumnPath(metadata, new ColumnPath(metadata.cfName).setSuper_column(cosc.counter_super_column.name).setColumn(c.name));
        }
    }

    private static void validateTtl(Column column) throws InvalidRequestException
    {
        if (column.isSetTtl() && column.ttl <= 0)
        {
            throw new InvalidRequestException("ttl must be positive");
        }
        // if it's not set, then it should be zero -- here we are just checking to make sure Thrift doesn't change that contract with us.
        assert column.isSetTtl() || column.ttl == 0;
    }

    public static void validateMutation(CFMetaData metadata, Mutation mut)
            throws InvalidRequestException
    {
        ColumnOrSuperColumn cosc = mut.column_or_supercolumn;
        Deletion del = mut.deletion;

        int nulls = 0;
        if (cosc == null) nulls++;
        if (del == null) nulls++;

        if (nulls != 1)
        {
            throw new InvalidRequestException("mutation must have one and only one of column_or_supercolumn or deletion");
        }

        if (cosc != null)
        {
            validateColumnOrSuperColumn(metadata, cosc);
        }
        else
        {
            validateDeletion(metadata, del);
        }
    }

    public static void validateDeletion(CFMetaData metadata, Deletion del) throws InvalidRequestException
    {

        if (del.super_column != null)
            validateColumnNames(metadata, (ByteBuffer)null, Arrays.asList(del.super_column));

        if (del.predicate != null)
        {
            validateSlicePredicate(metadata, del.super_column, del.predicate);
            if (del.predicate.slice_range != null)
                throw new InvalidRequestException("Deletion does not yet support SliceRange predicates.");
        }

        if (metadata.cfType == ColumnFamilyType.Standard && del.super_column != null)
        {
            String msg = String.format("Deletion of super columns is not possible on a standard ColumnFamily (KeySpace=%s ColumnFamily=%s Deletion=%s)", metadata.ksName, metadata.cfName, del);
            throw new InvalidRequestException(msg);
        }

        if (metadata.getDefaultValidator().isCommutative())
        {
            // forcing server timestamp even if a timestamp was set for coherence with other counter operation
            del.timestamp = System.currentTimeMillis();
        }
        else if (!del.isSetTimestamp())
        {
            throw new InvalidRequestException("Deletion timestamp is not optional for non commutative column family " + metadata.cfName);
        }
    }

    public static void validateSlicePredicate(CFMetaData metadata, ByteBuffer scName, SlicePredicate predicate) throws InvalidRequestException
    {
        if (predicate.column_names == null && predicate.slice_range == null)
            throw new InvalidRequestException("A SlicePredicate must be given a list of Columns, a SliceRange, or both");

        if (predicate.slice_range != null)
            validateRange(metadata, new ColumnParent(metadata.cfName).setSuper_column(scName), predicate.slice_range);

        if (predicate.column_names != null)
            validateColumnNames(metadata, scName, predicate.column_names);
    }

    /**
     * Validates the data part of the column (everything in the Column object but the name)
     */
    @SuppressWarnings("rawtypes")
	public static void validateColumnData(CFMetaData metadata, Column column) throws InvalidRequestException
    {
        validateTtl(column);
        if (!column.isSetValue())
            throw new InvalidRequestException("Column value is required");
        if (!column.isSetTimestamp())
            throw new InvalidRequestException("Column timestamp is required");
        try
        {
            AbstractType validator = metadata.getValueValidator(column.name);
            if (validator != null)
                validator.validate(column.value);
        }
        catch (MarshalException me)
        {
            throw new InvalidRequestException(String.format("[%s][%s][%s] = [%s] failed validation (%s)",
                                                            metadata.ksName,
                                                            metadata.cfName,
                                                            ByteBufferUtil.bytesToHex(column.name),
                                                            ByteBufferUtil.bytesToHex(column.value),
                                                            me.getMessage()));
        }
    }

    public static void validatePredicate(CFMetaData metadata, ColumnParent column_parent, SlicePredicate predicate)
            throws InvalidRequestException
    {
        if (predicate.column_names == null && predicate.slice_range == null)
            throw new InvalidRequestException("predicate column_names and slice_range may not both be null");
        if (predicate.column_names != null && predicate.slice_range != null)
            throw new InvalidRequestException("predicate column_names and slice_range may not both be present");

        if (predicate.getSlice_range() != null)
            validateRange(metadata, column_parent, predicate.slice_range);
        else
            validateColumnNames(metadata, column_parent, predicate.column_names);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static void validateKeyRange(KeyRange range) throws InvalidRequestException
    {
        if ((range.start_key == null) != (range.end_key == null))
        {
            throw new InvalidRequestException("start key and end key must either both be non-null, or both be null");
        }
        if ((range.start_token == null) != (range.end_token == null))
        {
            throw new InvalidRequestException("start token and end token must either both be non-null, or both be null");
        }
        if ((range.start_key == null) == (range.start_token == null))
        {
            throw new InvalidRequestException("exactly one of {start key, end key} or {start token, end token} must be specified");
        }

        if (range.start_key != null)
        {
            IPartitioner p = StorageService.getPartitioner();
            Token startToken = p.getToken(range.start_key);
            Token endToken = p.getToken(range.end_key);
            if (startToken.compareTo(endToken) > 0 && !endToken.equals(p.getMinimumToken()))
            {
                if (p instanceof RandomPartitioner)
                    throw new InvalidRequestException("start key's md5 sorts after end key's md5.  this is not allowed; you probably should not specify end key at all, under RandomPartitioner");
                else
                    throw new InvalidRequestException("start key must sort before (or equal to) finish key in your partitioner!");
            }
        }

        if (range.count <= 0)
        {
            throw new InvalidRequestException("maxRows must be positive");
        }
    }

    @SuppressWarnings("rawtypes")
	public static void validateIndexClauses(CFMetaData metadata, IndexClause index_clause)
    throws InvalidRequestException
    {
        if (index_clause.expressions.isEmpty())
            throw new InvalidRequestException("index clause list may not be empty");
        Set<ByteBuffer> indexedColumns = Table.open(metadata.ksName).getColumnFamilyStore(metadata.cfName).getIndexedColumns();
        AbstractType nameValidator =  ColumnFamily.getComparatorFor(metadata.ksName, metadata.cfName, null);

        boolean isIndexed = false;
        for (IndexExpression expression : index_clause.expressions)
        {
            try
            {
                nameValidator.validate(expression.column_name);
            }
            catch (MarshalException me)
            {
                throw new InvalidRequestException(String.format("[%s]=[%s] failed name validation (%s)",
                                                                ByteBufferUtil.bytesToHex(expression.column_name),
                                                                ByteBufferUtil.bytesToHex(expression.value),
                                                                me.getMessage()));
            }

            AbstractType valueValidator = NodeDescriptor.getValueValidator(metadata.ksName, metadata.cfName, expression.column_name);
            try
            {
                valueValidator.validate(expression.value);
            }
            catch (MarshalException me)
            {
                throw new InvalidRequestException(String.format("[%s]=[%s] failed value validation (%s)",
                                                                ByteBufferUtil.bytesToHex(expression.column_name),
                                                                ByteBufferUtil.bytesToHex(expression.value),
                                                                me.getMessage()));
            }

            isIndexed |= expression.op.equals(IndexOperator.EQ) && indexedColumns.contains(expression.column_name);
        }

        if (!isIndexed)
            throw new InvalidRequestException("No indexed columns present in index clause with operator EQ");
    }

    @SuppressWarnings("rawtypes")
	public static void validateCfDef(CfDef cf_def, CFMetaData old) throws InvalidRequestException
    {
        try
        {
            if (cf_def.key_alias != null)
            {
                if (!cf_def.key_alias.hasRemaining())
                    throw new InvalidRequestException("key_alias may not be empty");
                try
                {
                    // it's hard to use a key in a select statement if we can't type it.
                    // for now let's keep it simple and require ascii.
                    AsciiType.instance.validate(cf_def.key_alias);
                }
                catch (MarshalException e)
                {
                    throw new InvalidRequestException("Key aliases must be ascii");
                }
            }

            if (cf_def.key_alias != null)
            {
                if (!cf_def.key_alias.hasRemaining())
                    throw new InvalidRequestException("key_alias may not be empty");
                try
                {
                    // it's hard to use a key in a select statement if we can't type it.
                    // for now let's keep it simple and require ascii.
                    AsciiType.instance.validate(cf_def.key_alias);
                }
                catch (MarshalException e)
                {
                    throw new InvalidRequestException("Key aliases must be ascii");
                }
            }

            ColumnFamilyType cfType = ColumnFamilyType.create(cf_def.column_type);
            if (cfType == null)
                throw new InvalidRequestException("invalid column type " + cf_def.column_type);

            TypeParser.parse(cf_def.comparator_type);
            TypeParser.parse(cf_def.subcomparator_type);
            TypeParser.parse(cf_def.default_validation_class);
            if (cfType != ColumnFamilyType.Super && cf_def.subcomparator_type != null)
                throw new InvalidRequestException("subcomparator_type is invalid for standard columns");

            if (cf_def.column_metadata == null)
                return;

            AbstractType comparator = cfType == ColumnFamilyType.Standard
                                    ? TypeParser.parse(cf_def.comparator_type)
                                    : TypeParser.parse(cf_def.subcomparator_type);

            // initialize a set of names NOT in the CF under consideration
            Set<String> indexNames = new HashSet<String>();
            for (ColumnFamilyStore cfs : ColumnFamilyStore.all())
            {
                if (!cfs.getColumnFamilyName().equals(cf_def.name))
                    for (ColumnDefinition cd : cfs.metadata.getColumn_metadata().values())
                        indexNames.add(cd.getIndexName());
            }

            for (ColumnDef c : cf_def.column_metadata)
            {
                TypeParser.parse(c.validation_class);

                try
                {
                    comparator.validate(c.name);
                }
                catch (MarshalException e)
                {
                    throw new InvalidRequestException(String.format("Column name %s is not valid for comparator %s",
                                                                    ByteBufferUtil.bytesToHex(c.name), cf_def.comparator_type));
                }

                if (c.index_type == null)
                {
                    if (c.index_name != null)
                        throw new ConfigurationException("index_name cannot be set without index_type");
                }
                else
                {
                    if (cfType == ColumnFamilyType.Super)
                        throw new InvalidRequestException("Secondary indexes are not supported on supercolumns");
                    assert c.index_name != null; // should have a default set by now if none was provided
                    if (!Migration.isLegalName(c.index_name))
                        throw new InvalidRequestException("Illegal index name " + c.index_name);
                    // check index names against this CF _and_ globally
                    if (indexNames.contains(c.index_name))
                        throw new InvalidRequestException("Duplicate index name " + c.index_name);
                    indexNames.add(c.index_name);

                    ColumnDefinition oldCd = old == null ? null : old.getColumnDefinition(c.name);
                    if (oldCd != null && oldCd.getIndexType() != null)
                    {
                        assert oldCd.getIndexName() != null;
                        if (!oldCd.getIndexName().equals(c.index_name))
                            throw new InvalidRequestException("Cannot modify index name");
                    }
                }
            }
            validateMinMaxCompactionThresholds(cf_def);
            validateMemtableSettings(cf_def);
        }
        catch (ConfigurationException e)
        {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public static void validateCommutativeForWrite(CFMetaData metadata, ConsistencyLevel consistency) throws InvalidRequestException
    {
        if (!metadata.getReplicateOnWrite() && consistency != ConsistencyLevel.ONE)
        {
            throw new InvalidRequestException("cannot achieve CL > CL.ONE without replicate_on_write on columnfamily " + metadata.cfName);
        }
    }

    public static void validateKsDef(KsDef ks_def) throws ConfigurationException
    {
        // Attempt to instantiate the ARS, which will throw a ConfigException if
        //  the strategy_options aren't fully formed or if the ARS Classname is invalid.
        Map<String, String> options = KSMetaData.forwardsCompatibleOptions(ks_def);
        TokenMetadata tmd = StorageService.instance.getTokenMetadata();
        IEndpointSnitch eps = NodeDescriptor.getEndpointSnitch();
        Class<? extends AbstractReplicationStrategy> cls = AbstractReplicationStrategy.getClass(ks_def.strategy_class);
        AbstractReplicationStrategy.createReplicationStrategy(ks_def.name, cls, tmd, eps, options);
    }

    public static void validateMinMaxCompactionThresholds(CfDef cf_def) throws ConfigurationException
    {
        if (cf_def.isSetMin_compaction_threshold() && cf_def.isSetMax_compaction_threshold())
        {
            if ((cf_def.min_compaction_threshold > cf_def.max_compaction_threshold)
                && cf_def.max_compaction_threshold != 0)
            {
                throw new ConfigurationException("min_compaction_threshold cannot be greater than max_compaction_threshold");
            }
        }
        else if (cf_def.isSetMin_compaction_threshold())
        {
            if (cf_def.min_compaction_threshold > CFMetaData.DEFAULT_MAX_COMPACTION_THRESHOLD)
            {
                throw new ConfigurationException(String.format("min_compaction_threshold cannot be greather than max_compaction_threshold (default %d)",
                                                               CFMetaData.DEFAULT_MAX_COMPACTION_THRESHOLD));
            }
        }
        else if (cf_def.isSetMax_compaction_threshold())
        {
            if (cf_def.max_compaction_threshold < CFMetaData.DEFAULT_MIN_COMPACTION_THRESHOLD && cf_def.max_compaction_threshold != 0)
            {
                throw new ConfigurationException("max_compaction_threshold cannot be less than min_compaction_threshold");
            }
        }
        else
        {
            //Defaults are valid.
        }
    }

    public static void validateMemtableSettings(CfDef cf_def) throws ConfigurationException
    {
        if (cf_def.isSetMemtable_throughput_in_mb())
            NodeDescriptor.validateMemtableThroughput(cf_def.memtable_throughput_in_mb);
        if (cf_def.isSetMemtable_operations_in_millions())
            NodeDescriptor.validateMemtableOperations(cf_def.memtable_operations_in_millions);
    }
}

package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.KSMetaData;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.keyspace.compaction.CompactionManager;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.UUIDGen;
import com.cep.messaging.util.exception.ConfigurationException;

public class DropColumnFamily extends Migration
{
    private String tableName;
    private String cfName;
    
    /** Required no-arg constructor */
    protected DropColumnFamily() { /* pass */ }
    
    public DropColumnFamily(String tableName, String cfName) throws ConfigurationException, IOException
    {
        super(UUIDGen.makeType1UUIDFromHost(GossipUtilities.getLocalAddress()), NodeDescriptor.getDefsVersion());
        this.tableName = tableName;
        this.cfName = cfName;
        
        KSMetaData ksm = NodeDescriptor.getTableDefinition(tableName);
        if (ksm == null)
            throw new ConfigurationException("No such keyspace: " + tableName);
        else if (!ksm.cfMetaData().containsKey(cfName))
            throw new ConfigurationException("CF is not defined in that keyspace.");
        
        KSMetaData newKsm = makeNewKeyspaceDefinition(ksm);
        rm = Migration.makeDefinitionMutation(newKsm, null, newVersion);
    }

    private KSMetaData makeNewKeyspaceDefinition(KSMetaData ksm)
    {
        // clone ksm but do not include the new def
        CFMetaData cfm = ksm.cfMetaData().get(cfName);
        List<CFMetaData> newCfs = new ArrayList<CFMetaData>(ksm.cfMetaData().values());
        newCfs.remove(cfm);
        assert newCfs.size() == ksm.cfMetaData().size() - 1;
        return new KSMetaData(ksm.name, ksm.strategyClass, ksm.strategyOptions, newCfs.toArray(new CFMetaData[newCfs.size()]));
    }

    public void applyModels() throws IOException
    {
        ColumnFamilyStore cfs = Table.open(tableName).getColumnFamilyStore(cfName);

        // reinitialize the table.
        KSMetaData existing = NodeDescriptor.getTableDefinition(tableName);
        CFMetaData cfm = existing.cfMetaData().get(cfName);
        KSMetaData ksm = makeNewKeyspaceDefinition(existing);
        CFMetaData.purge(cfm);
        NodeDescriptor.setTableDefinition(ksm, newVersion);

        if (!StorageService.instance.isClientMode())
        {
            cfs.snapshot(Table.getTimestampedSnapshotName(null));

            CompactionManager.instance.getCompactionLock().lock();
            cfs.flushLock.lock();
            try
            {
                Table.open(ksm.name).dropCf(cfm.cfId);
            }
            finally
            {
                cfs.flushLock.unlock();
                CompactionManager.instance.getCompactionLock().unlock();
            }
        }
    }
    
    @Override
    public String toString()
    {
        return String.format("Drop column family: %s.%s", tableName, cfName);
    }
}

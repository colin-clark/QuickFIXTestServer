package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.IOException;

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

public class DropKeyspace extends Migration
{
    private String name;
    
    /** Required no-arg constructor */
    protected DropKeyspace() { /* pass */ }
    
    public DropKeyspace(String name) throws ConfigurationException, IOException
    {
        super(UUIDGen.makeType1UUIDFromHost(GossipUtilities.getLocalAddress()), NodeDescriptor.getDefsVersion());
        this.name = name;
        KSMetaData ksm = NodeDescriptor.getTableDefinition(name);
        if (ksm == null)
            throw new ConfigurationException("Keyspace does not exist.");
        rm = makeDefinitionMutation(null, ksm, newVersion);
    }

    public void applyModels() throws IOException
    {
        String snapshotName = Table.getTimestampedSnapshotName(null);
        CompactionManager.instance.getCompactionLock().lock();
        try
        {
            KSMetaData ksm = NodeDescriptor.getTableDefinition(name);

            // remove all cfs from the table instance.
            for (CFMetaData cfm : ksm.cfMetaData().values())
            {
                ColumnFamilyStore cfs = Table.open(ksm.name).getColumnFamilyStore(cfm.cfName);
                CFMetaData.purge(cfm);
                if (!StorageService.instance.isClientMode())
                {
                    cfs.snapshot(snapshotName);
                    cfs.flushLock.lock();
                    try
                    {
                        Table.open(ksm.name).dropCf(cfm.cfId);
                    }
                    finally
                    {
                        cfs.flushLock.unlock();
                    }
                }
            }
                            
            // remove the table from the static instances.
            Table table = Table.clear(ksm.name);
            assert table != null;
            // reset defs.
            NodeDescriptor.clearTableDefinition(ksm, newVersion);
        }
        finally
        {
            CompactionManager.instance.getCompactionLock().unlock();
        }
    }
    
    @Override
    public String toString()
    {
        return "Drop keyspace: " + name;
    }
}

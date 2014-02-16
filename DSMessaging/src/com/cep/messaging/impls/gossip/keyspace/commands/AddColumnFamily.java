package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.ColumnDefinition;
import com.cep.messaging.impls.gossip.keyspace.KSMetaData;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.UUIDGen;
import com.cep.messaging.util.exception.ConfigurationException;

public class AddColumnFamily extends Migration
{
    private CFMetaData cfm;
    
    /** Required no-arg constructor */
    protected AddColumnFamily() { /* pass */ }
    
    public AddColumnFamily(CFMetaData cfm) throws ConfigurationException, IOException
    {
        super(UUIDGen.makeType1UUIDFromHost(GossipUtilities.getLocalAddress()), NodeDescriptor.getDefsVersion());
        this.cfm = cfm;
        KSMetaData ksm = NodeDescriptor.getTableDefinition(cfm.ksName);
        
        if (ksm == null)
            throw new ConfigurationException("No such keyspace: " + cfm.ksName);
        else if (ksm.cfMetaData().containsKey(cfm.cfName))
            throw new ConfigurationException(String.format("%s already exists in keyspace %s",
                                                           cfm.cfName,
                                                           cfm.ksName));
        else if (!Migration.isLegalName(cfm.cfName))
            throw new ConfigurationException("Invalid column family name: " + cfm.cfName);
        for (Map.Entry<ByteBuffer, ColumnDefinition> entry : cfm.getColumn_metadata().entrySet())
        {
            String indexName = entry.getValue().getIndexName();
            if (indexName != null && !Migration.isLegalName(indexName))
                throw new ConfigurationException("Invalid index name: " + indexName);
        }

        // clone ksm but include the new cf def.
        KSMetaData newKsm = makeNewKeyspaceDefinition(ksm);
        
        rm = Migration.makeDefinitionMutation(newKsm, null, newVersion);
    }
    
    private KSMetaData makeNewKeyspaceDefinition(KSMetaData ksm)
    {
        List<CFMetaData> newCfs = new ArrayList<CFMetaData>(ksm.cfMetaData().values());
        newCfs.add(cfm);
        return new KSMetaData(ksm.name, ksm.strategyClass, ksm.strategyOptions, newCfs.toArray(new CFMetaData[newCfs.size()]));
    }
    
    public void applyModels() throws IOException
    {
        // reinitialize the table.
        KSMetaData ksm = NodeDescriptor.getTableDefinition(cfm.ksName);
        ksm = makeNewKeyspaceDefinition(ksm);
        try
        {
            CFMetaData.map(cfm);
        }
        catch (ConfigurationException ex)
        {
            throw new IOException(ex);
        }
        Table.open(cfm.ksName); // make sure it's init-ed w/ the old definitions first, since we're going to call initCf on the new one manually
        NodeDescriptor.setTableDefinition(ksm, newVersion);
        // these definitions could have come from somewhere else.
        CFMetaData.fixMaxId();
        if (!StorageService.instance.isClientMode())
            Table.open(ksm.name).initCf(cfm.cfId, cfm.cfName);
    }

    @Override
    public String toString()
    {
        return "Add column family: " + cfm.toString();
    }
}

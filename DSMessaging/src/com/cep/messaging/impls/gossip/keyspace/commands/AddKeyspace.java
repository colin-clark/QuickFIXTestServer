package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.IOException;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.KSMetaData;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.UUIDGen;
import com.cep.messaging.util.exception.ConfigurationException;

public class AddKeyspace extends Migration
{
    private KSMetaData ksm;
    
    /** Required no-arg constructor */
    protected AddKeyspace() { /* pass */ }
    
    public AddKeyspace(KSMetaData ksm) throws ConfigurationException, IOException
    {
        super(UUIDGen.makeType1UUIDFromHost(GossipUtilities.getLocalAddress()), NodeDescriptor.getDefsVersion());
        
        if (NodeDescriptor.getTableDefinition(ksm.name) != null)
            throw new ConfigurationException("Keyspace already exists.");
        if (!Migration.isLegalName(ksm.name))
            throw new ConfigurationException("Invalid keyspace name: " + ksm.name);
        for (CFMetaData cfm : ksm.cfMetaData().values())
            if (!Migration.isLegalName(cfm.cfName))
                throw new ConfigurationException("Invalid column family name: " + cfm.cfName);
        
        this.ksm = ksm;
        rm = makeDefinitionMutation(ksm, null, newVersion);
    }

    public void applyModels() throws IOException
    {
        for (CFMetaData cfm : ksm.cfMetaData().values())
        {
            try
            {
                CFMetaData.map(cfm);
            }
            catch (ConfigurationException ex)
            {
                // throw RTE since this indicates a table,cf maps to an existing ID. It shouldn't if this is really a
                // new keyspace.
                throw new RuntimeException(ex);
            }
        }
        NodeDescriptor.setTableDefinition(ksm, newVersion);
        // these definitions could have come from somewhere else.
        CFMetaData.fixMaxId();
        if (!StorageService.instance.isClientMode())
        {
            Table.open(ksm.name);
        }
    }
    
    @Override
    public String toString()
    {
        return "Add keyspace: " + ksm.toString();
    }
}

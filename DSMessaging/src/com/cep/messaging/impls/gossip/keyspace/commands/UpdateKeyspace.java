package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.IOException;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.KSMetaData;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.UUIDGen;
import com.cep.messaging.util.exception.ConfigurationException;

public class UpdateKeyspace extends Migration
{
    private KSMetaData newKsm;
    private KSMetaData oldKsm;
    
    /** Required no-arg constructor */
    protected UpdateKeyspace() { }
    
    /** create migration based on thrift parameters */
    public UpdateKeyspace(KSMetaData ksm) throws ConfigurationException, IOException
    {
        super(UUIDGen.makeType1UUIDFromHost(GossipUtilities.getLocalAddress()), NodeDescriptor.getDefsVersion());
        
        assert ksm != null;
        assert ksm.cfMetaData() != null;
        if (ksm.cfMetaData().size() > 0)
            throw new ConfigurationException("Updated keyspace must not contain any column families");
    
        // create the new ksm by merging the one passed in with the cf defs from the exisitng ksm.
        oldKsm = NodeDescriptor.getKSMetaData(ksm.name);
        if (oldKsm == null)
            throw new ConfigurationException(ksm.name + " cannot be updated because it doesn't exist.");
        this.newKsm = new KSMetaData(ksm.name, ksm.strategyClass, ksm.strategyOptions, oldKsm.cfMetaData().values().toArray(new CFMetaData[]{}));
        rm = makeDefinitionMutation(newKsm, oldKsm, newVersion);
    }
    
    void applyModels() throws IOException
    {
        NodeDescriptor.clearTableDefinition(oldKsm, newVersion);
        NodeDescriptor.setTableDefinition(newKsm, newVersion);


        Table table = Table.open(newKsm.name);
        try
        {
            table.createReplicationStrategy(newKsm);
        }
        catch (ConfigurationException e)
        {
            throw new IOException(e);
        }

        logger.info("Keyspace updated. Please perform any manual operations.");
    }

    @Override
    public String toString()
    {
        return String.format("Update keyspace %s to %s", oldKsm.toString(), newKsm.toString());
    }
}

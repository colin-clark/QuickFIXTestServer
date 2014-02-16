package com.cep.messaging.impls.gossip.keyspace.commands;

import java.io.IOException;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.CFMetaData;
import com.cep.messaging.impls.gossip.keyspace.ColumnFamilyStore;
import com.cep.messaging.impls.gossip.keyspace.KSMetaData;
import com.cep.messaging.impls.gossip.keyspace.Table;
import com.cep.messaging.impls.gossip.node.StorageService;
import com.cep.messaging.impls.gossip.thrift.CfDef;
import com.cep.messaging.impls.gossip.thrift.ColumnDef;
import com.cep.messaging.impls.gossip.util.GossipUtilities;
import com.cep.messaging.impls.gossip.util.UUIDGen;
import com.cep.messaging.util.exception.ConfigurationException;

/** todo: doesn't work with secondary indices yet. */
public class UpdateColumnFamily extends Migration
{
    // does not point to a CFM stored in NodeDescriptor.
    private CFMetaData metadata;
    
    protected UpdateColumnFamily() { }
    
    /** assumes validation has already happened. That is, replacing oldCfm with newCfm is neither illegal or totally whackass. */
    public UpdateColumnFamily(CfDef cf_def) throws ConfigurationException, IOException
    {
        super(UUIDGen.makeType1UUIDFromHost(GossipUtilities.getLocalAddress()), NodeDescriptor.getDefsVersion());
        
        KSMetaData ksm = NodeDescriptor.getTableDefinition(cf_def.keyspace.toString());
        if (ksm == null)
            throw new ConfigurationException("No such keyspace: " + cf_def.keyspace.toString());
        if (cf_def.column_metadata != null)
        {
            for (ColumnDef entry : cf_def.column_metadata)
            {
                if (entry.index_name != null && !Migration.isLegalName(entry.index_name.toString()))
                    throw new ConfigurationException("Invalid index name: " + entry.index_name);
            }
        }

        CFMetaData oldCfm = NodeDescriptor.getCFMetaData(CFMetaData.getId(cf_def.keyspace.toString(), cf_def.name.toString()));
        
        // create a copy of the old CF meta data. Apply new settings on top of it.
        this.metadata = oldCfm; //CFMetaData.inflate(oldCfm.deflate());
        this.metadata.apply(cf_def);
    }

    void applyModels() throws IOException
    {
        logger.debug("Updating " + NodeDescriptor.getCFMetaData(metadata.cfId) + " to " + metadata);
        // apply the meta update.
        /*try 
        {
            NodeDescriptor.getCFMetaData(metadata.cfId).apply(CFMetaData.convertToAvro(metadata));
        } 
        catch (ConfigurationException ex) 
        {
            throw new IOException(ex);
        }*/
        NodeDescriptor.setTableDefinition(null, newVersion);

        if (!StorageService.instance.isClientMode())
        {
            Table table = Table.open(metadata.ksName);
            ColumnFamilyStore oldCfs = table.getColumnFamilyStore(metadata.cfName);
            oldCfs.reload();
        }
    }

    @Override
    public String toString()
    {
        return String.format("Update column family to %s", metadata.toString());
    }
}

package com.cep.messaging.impls.gossip.transport.handlers;

import java.io.IOError;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.keyspace.Column;
import com.cep.messaging.impls.gossip.keyspace.commands.Migration;
import com.cep.messaging.impls.gossip.service.MigrationManager;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.util.Stage;
import com.cep.messaging.impls.gossip.util.StageManager;
import com.cep.messaging.impls.gossip.util.UUIDGen;
import com.cep.messaging.util.WrappedRunnable;
import com.cep.messaging.util.exception.ConfigurationException;

public class DefinitionsUpdateVerbHandler implements IVerbHandler
{
    private static final Logger logger = LoggerFactory.getLogger(DefinitionsUpdateVerbHandler.class);

    /** someone sent me their data definitions */
    public void doVerb(final Message message, String id)
    {
        try
        {
            // these are the serialized row mutations that I must apply.
            // check versions at every step along the way to make sure migrations are not applied out of order.
            Collection<Column> cols = MigrationManager.makeColumns(message);
            for (Column col : cols)
            {
                final UUID version = UUIDGen.getUUID(col.name());
                if (version.timestamp() > NodeDescriptor.getDefsVersion().timestamp())
                {
                    final Migration m = Migration.deserialize(col.value(), message.getVersion());
                    assert m.getVersion().equals(version);
                    StageManager.getStage(Stage.MIGRATION).submit(new WrappedRunnable()
                    {
                        protected void runMayThrow() throws Exception
                        {
                            // check to make sure the current version is before this one.
                            if (NodeDescriptor.getDefsVersion().timestamp() == version.timestamp())
                                logger.debug("Not appling (equal) " + version.toString());
                            else if (NodeDescriptor.getDefsVersion().timestamp() > version.timestamp())
                                logger.debug("Not applying (before)" + version.toString());
                            else
                            {
                                logger.debug("Applying {} from {}", m.getClass().getSimpleName(), message.getFrom());
                                try
                                {
                                    m.apply();
                                    // update gossip, but don't contact nodes directly
                                    m.passiveAnnounce();
                                }
                                catch (ConfigurationException ex)
                                {
                                    // Trying to apply the same migration twice. This happens as a result of gossip.
                                    logger.debug("Migration not applied " + ex.getMessage());
                                }
                            }
                        }
                    });
                }
            }
        }
        catch (IOException ex)
        {
            throw new IOError(ex);
        }
    }
}

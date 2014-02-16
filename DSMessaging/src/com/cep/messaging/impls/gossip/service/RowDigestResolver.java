package com.cep.messaging.impls.gossip.service;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import com.cep.messaging.impls.gossip.keyspace.ColumnFamily;
import com.cep.messaging.impls.gossip.keyspace.Row;
import com.cep.messaging.impls.gossip.transport.messages.Message;
import com.cep.messaging.impls.gossip.transport.messages.ReadResponse;

public class RowDigestResolver extends AbstractRowResolver
{
    public RowDigestResolver(String table, ByteBuffer key)
    {
        super(key, table);
    }
    
    public Row getData() throws IOException
    {
        for (Map.Entry<Message, ReadResponse> entry : replies.entrySet())
        {
            ReadResponse result = entry.getValue();
            if (!result.isDigestQuery())
                return result.row();
        }

        throw new AssertionError("getData should not be invoked when no data is present");
    }

    /*
     * This method handles two different scenarios:
     *
     * 1a)we're handling the initial read, of data from the closest replica + digests
     *    from the rest.  In this case we check the digests against each other,
     *    throw an exception if there is a mismatch, otherwise return the data row.
     *
     * 1b)we're checking additional digests that arrived after the minimum to handle
     *    the requested ConsistencyLevel, i.e. asynchronouse read repair check
     */
    public Row resolve() throws DigestMismatchException, IOException
    {
        if (logger.isDebugEnabled())
            logger.debug("resolving " + replies.size() + " responses");

        long startTime = System.currentTimeMillis();
		ColumnFamily data = null;

        // case 1: validate digests against each other; throw immediately on mismatch.
        // also, collects data results into versions/endpoints lists.
        //
        // results are cleared as we process them, to avoid unnecessary duplication of work
        // when resolve() is called a second time for read repair on responses that were not
        // necessary to satisfy ConsistencyLevel.
        ByteBuffer digest = null;
        for (Map.Entry<Message, ReadResponse> entry : replies.entrySet())
        {
            ReadResponse response = entry.getValue();
            if (response.isDigestQuery())
            {
                if (digest == null)
                {
                    digest = response.digest();
                }
                else
                {
                    ByteBuffer digest2 = response.digest();
                    if (!digest.equals(digest2))
                        throw new DigestMismatchException(key, digest, digest2);
                }
            }
            else
            {
                data = response.row().cf;
            }
        }

		// If there was a digest query compare it with all the data digests
		// If there is a mismatch then throw an exception so that read repair can happen.
        //
        // It's important to note that we do not compare the digests of multiple data responses --
        // if we are in that situation we know there was a previous mismatch and now we're doing a repair,
        // so our job is now case 2: figure out what the most recent version is and update everyone to that version.
        if (digest != null)
        {
            ByteBuffer digest2 = ColumnFamily.digest(data);
            if (!digest.equals(digest2))
                throw new DigestMismatchException(key, digest, digest2);
            if (logger.isDebugEnabled())
                logger.debug("digests verified");
        }

        if (logger.isDebugEnabled())
            logger.debug("resolve: " + (System.currentTimeMillis() - startTime) + " ms.");
		return new Row(key, data);
	}

    public boolean isDataPresent()
	{
        for (ReadResponse result : replies.values())
        {
            if (!result.isDigestQuery())
                return true;
        }
        return false;
    }
}

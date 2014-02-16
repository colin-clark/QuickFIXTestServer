package com.cep.messaging.impls.gossip.transport.messages;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.cep.messaging.impls.gossip.serialization.ICompactSerializer;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;
import com.cep.messaging.impls.gossip.util.GossipUtilities;


/*
 * This message is sent back the row mutation verb handler 
 * and basically specifies if the write succeeded or not for a particular 
 * key in a table
 */
public class WriteResponse 
{
    private static WriteResponseSerializer serializer_ = new WriteResponseSerializer();

    public static WriteResponseSerializer serializer()
    {
        return serializer_;
    }

    public static Message makeWriteResponseMessage(Message original, WriteResponse writeResponseMessage) throws IOException
    {
    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( bos );
        WriteResponse.serializer().serialize(writeResponseMessage, dos, original.getVersion());
        return original.getReply(GossipUtilities.getLocalAddress(), bos.toByteArray(), original.getVersion());
    }

	private final String table_;
	private final ByteBuffer key_;
	private final boolean status_;

	public WriteResponse(String table, ByteBuffer key, boolean bVal) {
		table_ = table;
		key_ = key;
		status_ = bVal;
	}

	public String table()
	{
		return table_;
	}

	public ByteBuffer key()
	{
		return key_;
	}

	public boolean isSuccess()
	{
		return status_;
	}

    public static class WriteResponseSerializer implements ICompactSerializer<WriteResponse>
    {
        public void serialize(WriteResponse wm, DataOutputStream dos, int version) throws IOException
        {
            dos.writeUTF(wm.table());
            ByteBufferUtil.writeWithShortLength(wm.key(), dos);
            dos.writeBoolean(wm.isSuccess());
        }

        public WriteResponse deserialize(DataInputStream dis, int version) throws IOException
        {
            String table = dis.readUTF();
            ByteBuffer key = ByteBufferUtil.readWithShortLength(dis);
            boolean status = dis.readBoolean();
            return new WriteResponse(table, key, status);
        }
    }
}

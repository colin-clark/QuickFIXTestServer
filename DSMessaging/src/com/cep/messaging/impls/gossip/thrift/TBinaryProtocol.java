package com.cep.messaging.impls.gossip.thrift;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;

public class TBinaryProtocol extends org.apache.thrift.protocol.TBinaryProtocol
{

    public TBinaryProtocol(TTransport trans)
    {
        this(trans, false, true);
    }

    public TBinaryProtocol(TTransport trans, boolean strictRead, boolean strictWrite)
    {
        super(trans);
        strictRead_ = strictRead;
        strictWrite_ = strictWrite;
    }

    public static class Factory extends org.apache.thrift.protocol.TBinaryProtocol.Factory
    {
        /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Factory()
        {
            super(false, true);
        }

        public Factory(boolean strictRead, boolean strictWrite)
        {
            super(strictRead, strictWrite, 0);
        }

        public Factory(boolean strictRead, boolean strictWrite, int readLength)
        {
            super(strictRead, strictWrite, readLength);
        }

        public TProtocol getProtocol(TTransport trans)
        {
            TBinaryProtocol protocol = new TBinaryProtocol(trans, strictRead_, strictWrite_);

            if (readLength_ != 0)
            {
                protocol.setReadLength(readLength_);
            }

            return protocol;
        }
    }

    @Override
    public void writeBinary(ByteBuffer buffer) throws TException
    {
        writeI32(buffer.remaining());

        if (buffer.hasArray())
        {
            trans_.write(buffer.array(), buffer.position() + buffer.arrayOffset(), buffer.remaining());
        }
        else
        {
            byte[] bytes = new byte[buffer.remaining()];

            int j = 0;
            for (int i = buffer.position(); i < buffer.limit(); i++)
            {
                bytes[j++] = buffer.get(i);
            }

            trans_.write(bytes);
        }
    }
}
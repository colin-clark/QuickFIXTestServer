package com.cep.messaging.impls.gossip.serialization;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.cep.messaging.impls.gossip.keyspace.commands.ReadCommand;

public class ReadCommandSerializer implements ICompactSerializer<ReadCommand>
{
    private static final Map<Byte, ReadCommandSerializer> CMD_SERIALIZER_MAP = new HashMap<Byte, ReadCommandSerializer>(); 
    static 
    {
        CMD_SERIALIZER_MAP.put(ReadCommand.CMD_TYPE_GET_SLICE_BY_NAMES, new SliceByNamesReadCommandSerializer());
        CMD_SERIALIZER_MAP.put(ReadCommand.CMD_TYPE_GET_SLICE, new SliceFromReadCommandSerializer());
    }


    public void serialize(ReadCommand rm, DataOutputStream dos, int version) throws IOException
    {
        dos.writeByte(rm.commandType);
        ReadCommandSerializer ser = CMD_SERIALIZER_MAP.get(rm.commandType);
        ser.serialize(rm, dos, version);
    }

    public ReadCommand deserialize(DataInputStream dis, int version) throws IOException
    {
        byte msgType = dis.readByte();
        return CMD_SERIALIZER_MAP.get(msgType).deserialize(dis, version);
    }
        
}

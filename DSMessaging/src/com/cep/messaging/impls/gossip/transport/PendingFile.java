package com.cep.messaging.impls.gossip.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.cep.messaging.impls.gossip.keyspace.sstable.Descriptor;
import com.cep.messaging.impls.gossip.keyspace.sstable.SSTable;
import com.cep.messaging.impls.gossip.serialization.ICompactSerializer;
import com.cep.messaging.util.Pair;

/**
 * Represents portions of a file to be streamed between nodes.
 */
public class PendingFile
{
    private static PendingFileSerializer serializer_ = new PendingFileSerializer();

    public static PendingFileSerializer serializer()
    {
        return serializer_;
    }

    // NB: this reference prevents garbage collection of the sstable on the source node
    @SuppressWarnings("unused")
	private final SSTable sstable;

    public final Descriptor desc;
    public final String component;
    public final List<Pair<Long,Long>> sections;
    public final OperationType type;
    public final long size;
    public long progress;

    public PendingFile(Descriptor desc, PendingFile pf)
    {
        this(null, desc, pf.component, pf.sections, pf.type);
    }

    public PendingFile(SSTable sstable, Descriptor desc, String component, List<Pair<Long,Long>> sections, OperationType type)
    {
        this.sstable = sstable;
        this.desc = desc;
        this.component = component;
        this.sections = sections;
        this.type = type;

        long tempSize = 0;
        for(Pair<Long,Long> section : sections)
        {
            tempSize += section.right - section.left;
        }
        size = tempSize;
    }

    public String getFilename()
    {
        return desc.filenameFor(component);
    }
    
    public boolean equals(Object o)
    {
        if ( !(o instanceof PendingFile) )
            return false;

        PendingFile rhs = (PendingFile)o;
        return getFilename().equals(rhs.getFilename());
    }

    public int hashCode()
    {
        return getFilename().hashCode();
    }

    public String toString()
    {
        return getFilename() + " sections=" + sections.size() + " progress=" + progress + "/" + size + " - " + progress*100/size + "%";
    }

    public static class PendingFileSerializer implements ICompactSerializer<PendingFile>
    {
        public void serialize(PendingFile sc, DataOutputStream dos, int version) throws IOException
        {
            if (sc == null)
            {
                dos.writeUTF("");
                return;
            }

            dos.writeUTF(sc.desc.filenameFor(sc.component));
            dos.writeUTF(sc.component);
            dos.writeInt(sc.sections.size());
            for (Pair<Long,Long> section : sc.sections)
            {
                dos.writeLong(section.left); dos.writeLong(section.right);
            }
            if (version > MessagingService.VERSION_07)
                dos.writeUTF(sc.type.name());
        }

        public PendingFile deserialize(DataInputStream dis, int version) throws IOException
        {
            String filename = dis.readUTF();
            if (filename.isEmpty())
                return null;
            
            Descriptor desc = Descriptor.fromFilename(filename);
            String component = dis.readUTF();
            int count = dis.readInt();
            List<Pair<Long,Long>> sections = new ArrayList<Pair<Long,Long>>(count);
            for (int i = 0; i < count; i++)
                sections.add(new Pair<Long,Long>(Long.valueOf(dis.readLong()), Long.valueOf(dis.readLong())));
            // this controls the way indexes are rebuilt when streaming in.  
            OperationType type = OperationType.RESTORE_REPLICA_COUNT;
            if (version > MessagingService.VERSION_07)
                type = OperationType.valueOf(dis.readUTF());
            return new PendingFile(null, desc, component, sections, type);
        }
    }
}

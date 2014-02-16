package com.cep.messaging.impls.gossip.keyspace;

import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.util.ByteBufferUtil;

public class DefsTable
{
    // column name for the schema storing serialized keyspace definitions
    // NB: must be an invalid keyspace name
    public static final ByteBuffer DEFINITION_SCHEMA_COLUMN_NAME = ByteBufferUtil.bytes("Avro/Schema");

    /** gets all the files that belong to a given column family. */
    public static Set<File> getFiles(String table, final String cf)
    {
        Set<File> found = new HashSet<File>();
        for (String path : NodeDescriptor.getAllDataFileLocationsForTable(table))
        {
            File[] dbFiles = new File(path).listFiles(new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return pathname.getName().startsWith(cf + "-") && pathname.getName().endsWith(".db") && pathname.exists();
                }
            });
            found.addAll(Arrays.asList(dbFiles));
        }
        return found;
    }
}

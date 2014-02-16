package com.cep.messaging.util;

import java.io.Closeable;
import java.io.DataInput;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileUtils
{
    private static Logger logger_ = LoggerFactory.getLogger(FileUtils.class);
    private static final DecimalFormat df_ = new DecimalFormat("#.##");
    private static final double kb_ = 1024d;
    private static final double mb_ = 1024*1024d;
    private static final double gb_ = 1024*1024*1024d;
    private static final double tb_ = 1024*1024*1024*1024d;

    public static void deleteWithConfirm(String file) throws IOException
    {
        deleteWithConfirm(new File(file));
    }

    public static void deleteWithConfirm(File file) throws IOException
    {
        assert file.exists() : "attempted to delete non-existing file " + file.getName();
        if (logger_.isDebugEnabled())
            logger_.debug("Deleting " + file.getName());
        if (!file.delete())
        {
            throw new IOException("Failed to delete " + file.getAbsolutePath());
        }
    }
    
    public static void renameWithConfirm(File from, File to) throws IOException
    {
        assert from.exists();
        if (logger_.isDebugEnabled())
            logger_.debug((String.format("Renaming %s to %s", from.getPath(), to.getPath())));
        if (!from.renameTo(to))
            throw new IOException(String.format("Failed to rename %s to %s", from.getPath(), to.getPath()));
    }

    public static void truncate(String path, long size) throws IOException
    {
        RandomAccessFile file;
        try
        {
            file = new RandomAccessFile(path, "rw");
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        try
        {
            file.getChannel().truncate(size);
        }
        finally
        {
            file.close();
        }
    }

    public static void closeQuietly(Closeable c)
    {
        try
        {
            if (c != null)
                c.close();
        }
        catch (Exception e)
        {
            logger_.warn("Failed closing stream", e);
        }
    }

    public static void close(Iterable<? extends Closeable> cs) throws IOException
    {
        IOException e = null;
        for (Closeable c : cs)
        {
            try
            {
                if (c != null)
                    c.close();
            }
            catch (IOException ex)
            {
                e = ex;
                logger_.warn("Failed closing stream " + c, ex);
            }
        }
        if (e != null)
            throw e;
    }

    public static class FileComparator implements Comparator<File>
    {
        public int compare(File f, File f2)
        {
            return (int)(f.lastModified() - f2.lastModified());
        }
    }

    public static void createDirectory(String directory) throws IOException
    {
        File file = new File(directory);
        if (!file.exists())
        {
            if (!file.mkdirs())
            {
                throw new IOException("unable to mkdirs " + directory);
            }
        }
    }

    public static boolean delete(String file)
    {
        File f = new File(file);
        return f.delete();
    }

    public static boolean delete(List<String> files)
    {
        boolean bVal = true;
        for ( int i = 0; i < files.size(); ++i )
        {
            String file = files.get(i);
            bVal = delete(file);
            if (bVal)
            {
            	if (logger_.isDebugEnabled())
            	  logger_.debug("Deleted file {}", file);
                files.remove(i);
            }
        }
        return bVal;
    }

    public static void delete(File[] files)
    {
        for ( File file : files )
        {
            file.delete();
        }
    }

    public static String stringifyFileSize(double value)
    {
        double d;
        if ( value >= tb_ )
        {
            d = value / tb_;
            String val = df_.format(d);
            return val + " TB";
        }
        else if ( value >= gb_ )
        {
            d = value / gb_;
            String val = df_.format(d);
            return val + " GB";
        }
        else if ( value >= mb_ )
        {
            d = value / mb_;
            String val = df_.format(d);
            return val + " MB";
        }
        else if ( value >= kb_ )
        {
            d = value / kb_;
            String val = df_.format(d);
            return val + " KB";
        }
        else
        {       
            String val = df_.format(value);
            return val + " bytes";
        }        
    }
    
    /**
     * Deletes all files and subdirectories under "dir".
     * @param dir Directory to be deleted
     * @throws IOException if any part of the tree cannot be deleted
     */
    public static void deleteRecursive(File dir) throws IOException
    {
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (String child : children)
                deleteRecursive(new File(dir, child));
        }

        // The directory is now empty so now it can be smoked
        deleteWithConfirm(dir);
    }

    public static void skipBytesFully(DataInput in, int bytes) throws IOException
    {
        int n = 0;
        while (n < bytes)
        {
            int skipped = in.skipBytes(bytes - n);
            if (skipped == 0)
                throw new EOFException("EOF after " + n + " bytes out of " + bytes);
            n += skipped;
        }
    }

    public static void skipBytesFully(DataInput in, long bytes) throws IOException
    {
        long n = 0;
        while (n < bytes)
        {
            int m = (int) Math.min(Integer.MAX_VALUE, bytes - n);
            int skipped = in.skipBytes(m);
            if (skipped == 0)
                throw new EOFException("EOF after " + n + " bytes out of " + bytes);
            n += skipped;
        }
    }
}

package com.cep.messaging.impls.gossip.auth;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;

import com.cep.messaging.impls.gossip.util.Resources;
import com.cep.messaging.util.exception.ConfigurationException;

public class SimpleAuthority implements IAuthority
{
    public final static String ACCESS_FILENAME_PROPERTY = "access.properties";
    // magical property for WRITE permissions to the keyspaces list
    public final static String KEYSPACES_WRITE_PROPERTY = "<modify-keyspaces>";
    private Properties accessProperties = null;

    public EnumSet<Permission> authorize(AuthenticatedUser user, List<Object> resource)
    {
        if (resource.size() < 2 || !Resources.ROOT.equals(resource.get(0)) || !Resources.KEYSPACES.equals(resource.get(1)))
            return Permission.NONE;
        
        String keyspace, columnFamily = null;
        EnumSet<Permission> authorized = Permission.NONE;
        
        // /darkstar/keyspaces
        if (resource.size() == 2)
        {
            keyspace = KEYSPACES_WRITE_PROPERTY;
            authorized = EnumSet.of(Permission.READ);
        }
        // /darkstar/keyspaces/<keyspace name>
        else if (resource.size() == 3)
        {
            keyspace = (String)resource.get(2);
        }
        // /darkstar/keyspaces/<keyspace name>/<cf name>
        else if (resource.size() == 4)
        {
            keyspace = (String)resource.get(2);
            columnFamily = (String)resource.get(3);
        }
        else
        {
            // We don't currently descend any lower in the hierarchy.
            throw new UnsupportedOperationException();
        }
        
        String accessFilename = System.getProperty(ACCESS_FILENAME_PROPERTY);
        try
        {
            // TODO: auto-reload when the file has been updated
            if (accessProperties == null)   // Don't hit the disk on every invocation
            {
                FileInputStream in = new FileInputStream(accessFilename);
                accessProperties = new Properties();
                accessProperties.load(in);
                in.close();
            }
            
            // Special case access to the keyspace list
            if (keyspace == KEYSPACES_WRITE_PROPERTY)
            {
                String kspAdmins = accessProperties.getProperty(KEYSPACES_WRITE_PROPERTY);
                for (String admin : kspAdmins.split(","))
                    if (admin.equals(user.username))
                        return Permission.ALL;
            }
            
            boolean canRead = false, canWrite = false;
            String readers = null, writers = null;
            
            if (columnFamily == null)
            {    
                readers = accessProperties.getProperty(keyspace + ".<ro>");
                writers = accessProperties.getProperty(keyspace + ".<rw>");
            }
            else
            {
                readers = accessProperties.getProperty(keyspace + "." + columnFamily + ".<ro>");
                writers = accessProperties.getProperty(keyspace + "." + columnFamily + ".<rw>");
            }
            
            if (readers != null)
            {
                for (String reader : readers.split(","))
                {
                    if (reader.equals(user.username))
                    {
                        canRead = true;
                        break;
                    }
                }
            }
            
            if (writers != null)
            {
                for (String writer : writers.split(","))
                {
                    if (writer.equals(user.username))
                    {
                        canWrite = true;
                        break;
                    }
                }
            }
            
            if (canWrite)
                authorized = Permission.ALL;
            else if (canRead)
                authorized = EnumSet.of(Permission.READ);
                
        }
        catch (IOException e)
        {
            throw new RuntimeException(String.format("Authorization table file '%s' could not be opened: %s",
                                                     accessFilename,
                                                     e.getMessage()));
        }

        return authorized;
    }

    public void validateConfiguration() throws ConfigurationException
    {
        String afilename = System.getProperty(ACCESS_FILENAME_PROPERTY);
        if (afilename == null)
        {
            throw new ConfigurationException(String.format("When using %s, '%s' property must be defined.",
                                                           this.getClass().getCanonicalName(),
                                                           ACCESS_FILENAME_PROPERTY));	
        }
    }
}

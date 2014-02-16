package com.cep.messaging.impls.gossip.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cep.messaging.impls.gossip.auth.AuthenticatedUser;
import com.cep.messaging.impls.gossip.auth.Permission;
import com.cep.messaging.impls.gossip.configuration.NodeDescriptor;
import com.cep.messaging.impls.gossip.thrift.AuthenticationException;
import com.cep.messaging.impls.gossip.thrift.InvalidRequestException;
import com.cep.messaging.impls.gossip.util.Resources;

/**
 * A container for per-client, thread-local state that Avro/Thrift threads must hold.
 * TODO: Kill thrift exceptions
 */
public class ClientState
{
    private static Logger logger = LoggerFactory.getLogger(ClientState.class);

    // Current user for the session
    private AuthenticatedUser user;
    private String keyspace;
    // Reusable array for authorization
    private final List<Object> resource = new ArrayList<Object>();

    /**
     * Construct a new, empty ClientState: can be reused after logout() or reset().
     */
    public ClientState()
    {
        reset();
    }
    
    public String getKeyspace() throws InvalidRequestException
    {
        if (keyspace == null)
            throw new InvalidRequestException("no keyspace has been specified");
        return keyspace;
    }

    public void setKeyspace(String ks)
    {
        keyspace = ks;
    }

    public String getSchedulingValue()
    {
        return (NodeDescriptor.getRequestSchedulerId().equals(keyspace) ? keyspace : "default");
    }

    /**
     * Attempts to login this client with the given credentials map.
     */
    public void login(Map<? extends CharSequence,? extends CharSequence> credentials) throws AuthenticationException
    {
        AuthenticatedUser user = NodeDescriptor.getAuthenticator().authenticate(credentials);
        if (logger.isDebugEnabled())
            logger.debug("logged in: {}", user);
        this.user = user;
    }

    public void logout()
    {
        if (logger.isDebugEnabled())
            logger.debug("logged out: {}", user);
        reset();
    }

    private void resourceClear()
    {
        resource.clear();
        resource.add(Resources.ROOT);
        resource.add(Resources.KEYSPACES);
    }

    public void reset()
    {
        user = NodeDescriptor.getAuthenticator().defaultUser();
        keyspace = null;
        resourceClear();
    }

    /**
     * Confirms that the client thread has the given Permission for the Keyspace list.
     */
    public void hasKeyspaceListAccess(Permission perm) throws InvalidRequestException
    {
        validateLogin();
        
        resourceClear();
        Set<Permission> perms = NodeDescriptor.getAuthority().authorize(user, resource);

        hasAccess(user, perms, perm, resource);
    }
    
    /**
     * Confirms that the client thread has the given Permission for the ColumnFamily list of
     * the current keyspace.
     */
    public void hasColumnFamilyListAccess(Permission perm) throws InvalidRequestException
    {
        validateLogin();
        validateKeyspace();

        // hardcode disallowing messing with system keyspace
        if (keyspace.equalsIgnoreCase("system"))
            throw new InvalidRequestException("system keyspace is not user-modifiable");

        resourceClear();
        resource.add(keyspace);
        Set<Permission> perms = NodeDescriptor.getAuthority().authorize(user, resource);
        
        hasAccess(user, perms, perm, resource);
    }
    
    /**
     * Confirms that the client thread has the given Permission in the context of the given
     * ColumnFamily and the current keyspace.
     */
    public void hasColumnFamilyAccess(String columnFamily, Permission perm) throws InvalidRequestException
    {
        validateLogin();
        validateKeyspace();
        
        resourceClear();
        resource.add(keyspace);
        resource.add(columnFamily);
        Set<Permission> perms = NodeDescriptor.getAuthority().authorize(user, resource);
        
        hasAccess(user, perms, perm, resource);
    }

    private void validateLogin() throws InvalidRequestException
    {
        if (user == null)
            throw new InvalidRequestException("You have not logged in");
    }
    
    private void validateKeyspace() throws InvalidRequestException
    {
        if (keyspace == null)
            throw new InvalidRequestException("You have not set a keyspace for this session");
    }

    private static void hasAccess(AuthenticatedUser user, Set<Permission> perms, Permission perm, List<Object> resource) throws InvalidRequestException
    {
        if (perms.contains(perm))
            return;
        throw new InvalidRequestException(String.format("%s does not have permission %s for %s",
                                                        user,
                                                        perm,
                                                        Resources.toString(resource)));
    }
}

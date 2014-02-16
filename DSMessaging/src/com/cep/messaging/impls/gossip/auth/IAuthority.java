package com.cep.messaging.impls.gossip.auth;

import java.util.EnumSet;
import java.util.List;

import com.cep.messaging.util.exception.ConfigurationException;

public interface IAuthority
{
    /**
     * @param user An authenticated user from a previous call to IAuthenticator.authenticate.
     * @param resource A List of Objects containing Strings and byte[]s: represents a resource in the hierarchy
     * described in the Javadocs.  
     * @return An AccessLevel representing the permissions for the user and resource: should never return null.
     */
    public EnumSet<Permission> authorize(AuthenticatedUser user, List<Object> resource);

    public void validateConfiguration() throws ConfigurationException;
}

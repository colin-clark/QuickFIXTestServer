package com.cep.messaging.impls.gossip.auth;

import java.util.Map;

import com.cep.messaging.impls.gossip.thrift.AuthenticationException;
import com.cep.messaging.util.exception.ConfigurationException;

public class AllowAllAuthenticator implements IAuthenticator
{
    private final static AuthenticatedUser USER = new AuthenticatedUser("allow_all");

    public AuthenticatedUser defaultUser()
    {
        return USER;
    }

    public AuthenticatedUser authenticate(Map<? extends CharSequence,? extends CharSequence> credentials) throws AuthenticationException
    {
        return USER;
    }
    
    public void validateConfiguration() throws ConfigurationException
    {
        // pass
    }
}

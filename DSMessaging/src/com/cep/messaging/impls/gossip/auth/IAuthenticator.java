package com.cep.messaging.impls.gossip.auth;

import java.util.Map;

import com.cep.messaging.impls.gossip.thrift.AuthenticationException;
import com.cep.messaging.util.exception.ConfigurationException;

public interface IAuthenticator
{
    /**
     * @return The user that a connection is initialized with, or 'null' if a user must call login().
     */
    public AuthenticatedUser defaultUser();

    /**
     * @param credentials An implementation specific collection of identifying information.
     * @return A successfully authenticated user: should throw AuthenticationException rather than ever returning null.
     */
    public AuthenticatedUser authenticate(Map<? extends CharSequence,? extends CharSequence> credentials) throws AuthenticationException;

    public void validateConfiguration() throws ConfigurationException;
}

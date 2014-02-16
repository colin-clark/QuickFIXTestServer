package com.cep.messaging.impls.gossip.auth;

import java.util.EnumSet;
import java.util.List;

import com.cep.messaging.util.exception.ConfigurationException;

public class AllowAllAuthority implements IAuthority
{
    public EnumSet<Permission> authorize(AuthenticatedUser user, List<Object> resource)
    {
        return Permission.ALL;
    }

    public void validateConfiguration() throws ConfigurationException
    {
        // pass
    }
}

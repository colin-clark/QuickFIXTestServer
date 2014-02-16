package com.cep.messaging.impls.gossip.auth;

import java.util.EnumSet;

/**
 * An enum encapsulating the set of possible permissions that an authenticated user can have for a resource.
 *
 * IAuthority implementations may encode permissions using ordinals, so the Enum order must never change.
 */
public enum Permission
{
    READ,
    WRITE;

    public static final EnumSet<Permission> ALL = EnumSet.allOf(Permission.class);
    public static final EnumSet<Permission> NONE = EnumSet.noneOf(Permission.class);
}

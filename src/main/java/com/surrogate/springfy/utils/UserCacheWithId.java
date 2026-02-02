package com.surrogate.springfy.utils;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserCacheWithId {

    /**
     * Obtains a {@link UserDetails} from the cache.
     *
     * @param username the {@link User#getUsername()} used to place the user in the cache
     * @return the populated <code>UserDetails</code> or <code>null</code> if the user
     * could not be found or if the cache entry has expired
     */
    UserDetailsWithId getUserFromCache(String username);

    /**
     * Places a {@link UserDetails} in the cache. The <code>username</code> is the key
     * used to subsequently retrieve the <code>UserDetails</code>.
     *
     * @param user the fully populated <code>UserDetails</code> to place in the cache
     */
    void putUserInCache(UserDetailsWithId user);

    /**
     * Removes the specified user from the cache. The <code>username</code> is the key
     * used to remove the user. If the user is not found, the method should simply return
     * (not thrown an exception).
     * <p>
     * Some cache implementations may not support eviction from the cache, in which case
     * they should provide appropriate behaviour to alter the user in either its
     * documentation, via an exception, or through a log message.
     *
     * @param username to be evicted from the cache
     */
    void removeUserFromCache(String username);

}
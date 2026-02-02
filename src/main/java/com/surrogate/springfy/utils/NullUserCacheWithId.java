package com.surrogate.springfy.utils;


public class NullUserCacheWithId implements UserCacheWithId {
    @Override
    public UserDetailsWithId getUserFromCache(String username) {
        return null;
    }

    @Override
    public void putUserInCache(UserDetailsWithId user) {
    }

    @Override
    public void removeUserFromCache(String username) {
    }

}

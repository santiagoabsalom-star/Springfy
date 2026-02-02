package com.surrogate.springfy.utils;

public interface UserDetailsCheckerWithId {

    /**
     * Examines the User
     *
     * @param toCheck the UserDetails instance whose status should be checked.
     */
    void check(UserDetailsWithId toCheck);

}
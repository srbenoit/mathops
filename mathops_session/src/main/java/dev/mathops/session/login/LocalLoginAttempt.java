package dev.mathops.session.login;

import dev.mathops.db.old.rawrecord.RawStudent;

/**
 * A container for the data involved in a login attempt using the local login processor. This data is stored in a
 * separate class to allow the login processor to avoid storing member variables that would prevent it from being
 * thread-safe.
 */
final class LocalLoginAttempt {

    /** The session ID of the request. */
    final String sessionId;

    /** The username. */
    final String username;

    /** The password. */
    final String password;

    /** The student object. */
    RawStudent student;

    /**
     * Constructs a new {@code LocalLoginAttempt}.
     *
     * @param theSessionId the session ID of the request
     * @param theUsername  the username
     * @param thePassword  the password
     */
    LocalLoginAttempt(final String theSessionId, final String theUsername, final String thePassword) {

        this.sessionId = theSessionId;
        this.username = theUsername;
        this.password = thePassword;
    }
}

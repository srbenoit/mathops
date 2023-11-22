package dev.mathops.session.login;

import dev.mathops.session.LiveSessionInfo;

/**
 * Carries the results of a login attempt.
 */
public final class LoginResult {

    /** An error message if unsuccessful. */
    public final String error;

    /** The session information if successful. */
    public final LiveSessionInfo session;

    /**
     * Constructs a new {@code LoginResult} for a successful operation.
     *
     * @param theSession the session information
     */
    LoginResult(final LiveSessionInfo theSession) {

        this.session = theSession;
        this.error = null;
    }

    /**
     * Constructs a new {@code LoginResult} for an unsuccessful operation.
     *
     * @param theError the error message
     */
    LoginResult(final String theError) {

        this.session = null;
        this.error = theError;
    }
}

package dev.mathops.session;

/**
 * The result of a session operation, which is either a new, immutable, copy of the session configuration, or an error
 * message.
 */
public final class SessionResult {

    /** An error message if unsuccessful. */
    public final String error;

    /** The session information if successful. */
    public final ImmutableSessionInfo session;

    /**
     * Constructs a new {@code SessionResult} for a successful operation.
     *
     * @param theSession the session information
     */
    SessionResult(final ImmutableSessionInfo theSession) {

        this.session = theSession;
        this.error = null;
    }

    /**
     * Constructs a new {@code SessionResult} for an unsuccessful operation.
     *
     * @param theError the error message
     */
    /* default */ SessionResult(final String theError) {

        this.session = null;
        this.error = theError;
    }
}

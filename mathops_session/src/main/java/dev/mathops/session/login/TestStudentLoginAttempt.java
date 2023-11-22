package dev.mathops.session.login;

import dev.mathops.db.cfg.DbProfile;

/**
 * A container for the data involved in a login attempt using the local login processor. This data is stored in a
 * separate class to allow the login processor to avoid storing member variables that would prevent it from being
 * thread-safe.
 */
final class TestStudentLoginAttempt {

    /** The context to use for database access. */
    private final DbProfile dbProfile;

    /** The session ID of the request. */
    private final String sessionId;

    /** The username. */
    private final String studentId;

    /**
     * Constructs a new {@code TestStudentLoginAttempt}.
     *
     * @param theDbProfile the context to use for database access
     * @param theSessionId the session ID of the request
     * @param theStudentId the student ID
     */
    TestStudentLoginAttempt(final DbProfile theDbProfile, final String theSessionId,
                            final String theStudentId) {

        this.dbProfile = theDbProfile;
        this.sessionId = theSessionId;
        this.studentId = theStudentId;
    }
}

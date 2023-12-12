package dev.mathops.session.login;

import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawrecord.RawStudent;

/**
 * A container for the data involved in a login attempt using the CSU eID login processor. This data is stored in a
 * separate class to allow the login processor to avoid storing member variables that would prevent it from being
 * thread-safe.
 */
final class ShibbolethLoginAttempt {

    /** The session ID of the request. */
    final String sessionId;

    /** The CSU ID. */
    final String csuId;

    /** The role. */
    ERole role;

    /** The student object. */
    RawStudent student;

    /**
     * Constructs a new {@code ShibbolethLoginAttempt}.
     *
     * @param theSessionId the session ID of the request
     * @param theCsuID     the CSU ID
     */
    ShibbolethLoginAttempt(final String theSessionId, final String theCsuID) {

        this.sessionId = theSessionId;
        this.csuId = theCsuID;
        this.student = null;
        this.role = ERole.STUDENT;
    }
}

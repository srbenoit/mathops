package dev.mathops.session.login;

import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.LiveSessionInfo;

import java.sql.SQLException;
import java.util.Map;

/**
 * A login processor that can be used to create a session for a user directly. Used by LTI tools where the student's
 * identity is verified some other way. This processor can ONLY establish sessions with the STUDENT role.
 */
public final class AutoLoginProcessor implements ILoginProcessor {

    /** The type name for this processor. */
    private static final String TYPE = "Auto";

    /** The name of the CSU ID field. */
    public static final String CSUID = "csuId";

    /**
     * Construct a new {@code AutoLoginProcessor}.
     */
    public AutoLoginProcessor() {

        // No action
    }

    /**
     * Gets a type name (safe for use as an HTTP request parameter) for the login authentication method.
     *
     * @return the type name
     */
    @Override
    public String getType() {

        return TYPE;
    }

    /**
     * Gets a display name for the login authentication method (the user may choose from several available methods).
     *
     * @return the display name
     */
    @Override
    public String getDisplayName() {

        return "Auto";
    }

    /**
     * Gets a list of the fields that the login form should display.
     *
     * @return the array of fields
     */
    @Override
    public LoginFormField[] getLoginFormFields() {

        return new LoginFormField[]{new LoginFormField(ELoginFieldType.TEXT),};
    }

    /**
     * Attempts to authenticate a user based on responses to the login fields and create a login session.
     *
     * @param cache          the data cache
     * @param secSessionId   the ID of the new session
     * @param fieldValues    the values provided by the user in the login field
     * @param doLiveRegCheck true to include a live registration check in the process
     * @return the login information, which will contain the session and student information (if login was successful)
     *         or an error message (if login did not succeed)
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public LoginResult login(final Cache cache, final String secSessionId, final Map<String, String> fieldValues,
                             final boolean doLiveRegCheck) throws SQLException {

        final LoginResult reply;

        final String csuId = fieldValues.get(CSUID);

        if (csuId == null) {
            reply = new LoginResult("No CSU ID provided");
        } else {
            final AutoLoginAttempt attempt = new AutoLoginAttempt(secSessionId, csuId);

            if (lookUpStudent(cache, attempt)) {
                reply = establishSession(attempt);
            } else {
                reply = new LoginResult("Unable to access student information");
            }
        }

        return reply;
    }

    /**
     * Uses the stored CSU ID to look up a student, or failing that, the Aries ID.
     *
     * @param cache   the data cache
     * @param attempt the login attempt
     * @return {@code true} if the lookup succeeded, {@code false} if it failed
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean lookUpStudent(final Cache cache, final AutoLoginAttempt attempt) throws SQLException {

        final String csuId = attempt.csuId;
        RawStudent student = null;

        if (csuId != null) {
            student = RawStudentLogic.query(cache, csuId, true);
        }

        attempt.student = student;

        return student != null;
    }

    /**
     * Create a new record in the sessions table that will be used for authorization checks on each secured web page.
     *
     * @param attempt the login attempt
     * @return the constructed {@code LoginSessionInfo}
     */
    private LoginResult establishSession(final AutoLoginAttempt attempt) {

        final String studentId = attempt.student.stuId;
        final String first = attempt.student.firstName;
        final String last = attempt.student.lastName;
        final String screen = attempt.student.getScreenName();

        final LiveSessionInfo live = new LiveSessionInfo(attempt.sessionId, TYPE, ERole.STUDENT);
        live.setUserInfo(studentId, first, last, screen);

        return new LoginResult(live);
    }
}

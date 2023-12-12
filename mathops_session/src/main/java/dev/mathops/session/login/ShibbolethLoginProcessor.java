package dev.mathops.session.login;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.CsuLiveRegChecker;
import dev.mathops.session.LiveSessionInfo;
import dev.mathops.session.SessionManager;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Retrieves Shibboleth attributes from an HttpServletRequest and uses them to establish a user session.
 */
public final class ShibbolethLoginProcessor implements ILoginProcessor {

    /** The type name for this processor. */
    private static final String TYPE = "Shib";

    /** The name of the CSU ID field. */
    public static final String CSUID = "csuId";

    /** The special student type that gets mapped to the administrative user ID. */
    private static final String MAP_TO_ADMIN_TYPE = "STEVE";

    /**
     * Construct a new {@code ShibbolethLoginProcessor}.
     */
    public ShibbolethLoginProcessor() {

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

        return "Shibboleth";
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
     * @throws SQLException if there was an error accessing the database
     */
    @Override
    public LoginResult login(final Cache cache, final String secSessionId,
                             final Map<String, String> fieldValues, final boolean doLiveRegCheck) throws SQLException {

        final LoginResult reply;

        final String csuId = fieldValues.get(CSUID);

        if (csuId == null) {
            reply = new LoginResult("No CSU ID provided");
        } else {
            final ShibbolethLoginAttempt attempt = new ShibbolethLoginAttempt(secSessionId, csuId);

            if (lookUpStudent(cache, attempt, doLiveRegCheck)) {
                reply = establishSession(attempt);
            } else {
                reply = new LoginResult("Unable to access student information");
            }
        }

        return reply;
    }

    /**
     * Uses the stored CSU ID to look up a student.
     *
     * @param cache          the data cache
     * @param attempt        the login attempt
     * @param doLiveRegCheck true to do a live registration check as part of the process
     * @return {@code true} if the lookup succeeded, {@code false} if it failed
     * @throws SQLException if there was an error accessing the database
     */
    private static boolean lookUpStudent(final Cache cache, final ShibbolethLoginAttempt attempt,
                                         final boolean doLiveRegCheck) throws SQLException {

        String csuId = attempt.csuId;
        RawStudent student = null;

        if (csuId != null) {
            csuId = filterCsuId(cache, attempt, csuId);

            if (attempt.role == ERole.BOOKSTORE) {
                // Make an artificial student record, so we don't have to add bookstore staff
                // members to the student table
                student = RawStudentLogic.makeFakeStudent(csuId, CoreConstants.EMPTY, //
                        "Bookstore Staff");
            } else {
                if (doLiveRegCheck) {
                    CsuLiveRegChecker.checkLiveReg(cache, csuId);
                }
                student = RawStudentLogic.query(cache, csuId, doLiveRegCheck);
            }
        }

        attempt.student = student;

        return student != null;
    }

    /**
     * Given a particular CSU ID, applies filters that may allow one user ID to be mapped to another.
     *
     * @param cache   the data cache
     * @param attempt the login attempt
     * @param csuId   the CSU ID returned by the login process
     * @return the actual CSU ID to use to create the session
     * @throws SQLException if there is an error accessing the database
     */
    private static String filterCsuId(final Cache cache, final ShibbolethLoginAttempt attempt,
                                      final String csuId) throws SQLException {

        final SessionManager mgr = SessionManager.getInstance();
        if (mgr.isSysadmin(csuId)) {
            Log.info("Assigning user ", csuId, " sysadmin role");
            attempt.role = ERole.SYSADMIN;
        } else {
            attempt.role = ERole.STUDENT;

            // The SpecialStudent table can flag students to log in as a particular ID
            final List<RawSpecialStus> specs = RawSpecialStusLogic.queryByStudent(cache, csuId);

            for (final RawSpecialStus spec : specs) {
                final String type = spec.stuType;
                Log.info("Student ", csuId, " is member of catgory '", type, "'");

                if (MAP_TO_ADMIN_TYPE.equals(type)) {
                    attempt.role = ERole.ADMINISTRATOR;
                    break;
                } else if ("ADVISER".equals(type)) {
                    attempt.role = ERole.ADVISER;
                } else if ("PROCTOR".equals(type)) {
                    attempt.role = ERole.PROCTOR;
                } else if ("BOOKSTO".equals(type)) {
                    attempt.role = ERole.BOOKSTORE;
                }
            }
        }

        return csuId;
    }

    /**
     * Create a new record in the sessions table that will be used for authorization checks on each secured web page.
     *
     * @param attempt the login attempt
     * @return the constructed {@code LoginSessionInfo}
     */
    private static LoginResult establishSession(final ShibbolethLoginAttempt attempt) {

        final String studentId = attempt.student.stuId;
        final String first = attempt.student.firstName;
        final String last = attempt.student.lastName;
        final String screen = attempt.student.getScreenName();

        final LiveSessionInfo live = new LiveSessionInfo(attempt.sessionId, TYPE, attempt.role);
        live.setUserInfo(studentId, first, last, screen);

        return new LoginResult(live);
    }
}

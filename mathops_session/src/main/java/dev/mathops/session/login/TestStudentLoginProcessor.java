package dev.mathops.session.login;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.LiveSessionInfo;

import java.sql.SQLException;
import java.util.Map;

/**
 * A dummy login processor for test students.
 */
public final class TestStudentLoginProcessor implements ILoginProcessor {

    /** The type name for this processor. */
    public static final String TYPE = "TestStudent";

    /** The name of the student ID field. */
    public static final String STU_ID = "stu_id";

    /** Error message. */
    private static final String ERR_MSG = "Invalid Test Student Id";

    /**
     * Construct a new {@code TestStudentLoginProcessor}.
     */
    public TestStudentLoginProcessor() {

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

        return "Test Student";
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

        final String studentId = fieldValues.get(STU_ID);
        final LoginResult result;

        if (studentId == null) {
            result = new LoginResult("No student ID provided");
        } else if (studentId.length() == 9 && studentId.startsWith("99")) {
            final RawStudent stu = RawStudentLogic.query(cache, studentId, false);

            if (stu == null) {
                Log.warning("No test student with student ID '", studentId, "'");
                result = new LoginResult(ERR_MSG);
            } else {
                final String first = stu.firstName;
                final String last = stu.lastName;
                final String screen = stu.getScreenName();

                final LiveSessionInfo live = new LiveSessionInfo(secSessionId, TYPE, ERole.STUDENT);
                live.setUserInfo(studentId, first, last, screen);

                result = new LoginResult(live);
            }
        } else {
            result = new LoginResult("Invalid test student ID provided");
        }

        return result;
    }
}

package dev.mathops.session.login;

import dev.mathops.db.logic.Cache;

import java.sql.SQLException;
import java.util.Map;

/**
 * An interface for a general login processor.
 */
public interface ILoginProcessor extends IAuthenticationMethod {

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
    LoginResult login(Cache cache, String secSessionId, Map<String, String> fieldValues,
                      boolean doLiveRegCheck) throws SQLException;
}

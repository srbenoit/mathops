package dev.mathops.web.site;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.CsuLiveRegChecker;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.SessionResult;

import jakarta.servlet.ServletRequest;

import java.sql.SQLException;

/**
 * A definition of a user information bar, to be loaded from a database. The information bar shows the currently
 * logged-in user, a button to log out, and the current date. If the user is logged in under a role with administrative
 * features, those are also displayed here.
 */
public enum UserInfoBar {
    ;

    /** Milliseconds per day. */
    private static final long MILLIS_PER_DAY = 86400000L;

    /**
     * Processes any submissions by the role controls (call on POST).
     *
     * @param cache   the data cache
     * @param req     the HTTP request
     * @param session the user session info
     * @throws SQLException if there was an error accessing the database
     */
    public static void processRoleControls(final Cache cache, final ServletRequest req,
                                           final ImmutableSessionInfo session) throws SQLException {

        final ISessionManager sessMgr = SessionManager.getInstance();

        final String actAsStu = req.getParameter("act-as-stu-id");
        final String becomeStu = req.getParameter("become-stu-id");
        final String dateAdjust = req.getParameter("date-adjust");

        if (AbstractSite.isParamInvalid(actAsStu) || AbstractSite.isParamInvalid(becomeStu)
            || AbstractSite.isParamInvalid(dateAdjust)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  act-as-stu-id='", actAsStu, "'");
            Log.warning("  become-stu-id='", becomeStu, "'");
            Log.warning("  date-adjust='", dateAdjust, "'");
        } else {
            ImmutableSessionInfo sess = session;

            String actAs2 = actAsStu == null ? null : actAsStu.trim().replace(CoreConstants.DASH, CoreConstants.EMPTY);
            String become2 = becomeStu == null ? null
                    : becomeStu.trim().replace(CoreConstants.DASH, CoreConstants.EMPTY);

            if (actAs2 != null && !actAs2.isEmpty() && Character.isLetter(actAs2.charAt(0))) {
                actAs2 = idFromStudentName(cache, actAs2);
            }
            if (become2 != null && !become2.isEmpty() && Character.isLetter(become2.charAt(0))) {
                become2 = idFromStudentName(cache, become2);
            }

            if (dateAdjust != null) {
                try {
                    final int days = Long.valueOf(dateAdjust).intValue();
                    sess = adjustDate(sessMgr, days, sess);
                } catch (final NumberFormatException ex) {
                    Log.warning("Failed to parse date adjustment '", dateAdjust, "'", ex);
                }
            }

            if (become2 == null) {
                if (actAs2 != null) {
                    actAsStudent(cache, sessMgr, actAs2, sess);
                }
            } else {
                becomeStudent(cache, sessMgr, become2, sess);
            }
        }
    }

    /**
     * Attempts to resolve a student name into an ID.
     *
     * @param cache the data cache
     * @param name  the name
     * @return the ID if successful, null if not
     * @throws SQLException if there was an error accessing the database
     */
    private static String idFromStudentName(final Cache cache, final String name)
            throws SQLException {

        final int space = name.indexOf(' ');

        RawStudent stu;

        if (space == -1) {
            // Assume last name
            stu = RawStudentLogic.queryByLastName(cache, name);
        } else {
            final String part1 = name.substring(0, space);
            final String part2 = name.substring(space + 1);

            stu = RawStudentLogic.queryByName(cache, part1, part2);
            if (stu == null) {
                stu = RawStudentLogic.queryByName(cache, part2, part1);
            }
        }

        return stu == null ? null : stu.stuId;
    }

    /**
     * Processes a request to act as a new student.
     *
     * @param cache   the data cache
     * @param sessMgr the session manager
     * @param newStu  the new student ID
     * @param session the current session information
     * @throws SQLException if there was an error accessing the database
     */
    private static void actAsStudent(final Cache cache, final ISessionManager sessMgr, final String newStu,
                                     final ImmutableSessionInfo session) throws SQLException {

        if (session.role.canActAs(ERole.STUDENT)) {
            final SessionResult res = sessMgr.setEffectiveUserId(cache, session.loginSessionId, newStu);

            CsuLiveRegChecker.checkLiveReg(cache, newStu);

            if (res != null && res.session != null) {
                Log.info("Acting as student ID ", newStu);
            }
        }

    }

    /**
     * Processes a request to become a new student.
     *
     * @param cache   the data cache
     * @param sessMgr the session manager
     * @param newStu  the new student ID
     * @param session the current session information
     * @throws SQLException if there was an error accessing the database
     */
    private static void becomeStudent(final Cache cache, final ISessionManager sessMgr, final String newStu,
                                      final ImmutableSessionInfo session) throws SQLException {

        Log.info("Request to become ", newStu, " from ", session.role);

        if (session.role.canActAs(ERole.STUDENT)) {
            ERole newRole = ERole.STUDENT;
            if (RawSpecialStusLogic.isSpecialType(cache, newStu, session.getNow().toLocalDate(),
                    RawSpecialStus.ADVISER)) {
                newRole = ERole.ADVISER;
            }

            Log.info("Setting user ID to ", newStu, " and role to ", newRole);
            final SessionResult res = sessMgr.setUserId(cache, session.loginSessionId, newStu, newRole);

            CsuLiveRegChecker.checkLiveReg(cache, newStu);
            // StudentCache.get(primary, secondary).liveQueryStudent(newStu);

            // TODO: If role is INSTRUCTOR, limit students we alloy to those in that instructor's
            // courses, and if DEVELOPER, allow access only to test students

            if (res != null && res.session != null) {
                Log.info("Becoming user ID ", newStu);
            }
        }
    }

    /**
     * Adjusts the date in a session by some number of days (positive or negative).
     *
     * @param sessMgr the session manager
     * @param days    the number of days by which to adjust the session date
     * @param session the current session information
     * @return the new session information (updated if the change was permitted; or the original session information if
     *         not)
     */
    private static ImmutableSessionInfo adjustDate(final ISessionManager sessMgr, final int days,
                                                   final ImmutableSessionInfo session) {

        ImmutableSessionInfo sess = session;

        if (session.role.canActAs(ERole.STUDENT)) {

            final long curOffset = session.timeOffset;
            final long newOffset = curOffset + (long) days * MILLIS_PER_DAY;

            final SessionResult res = sessMgr.setTimeOffset(session.loginSessionId, newOffset);

            if (res != null && res.session != null) {
                sess = res.session;
                Log.info("Updated time offset");
            }
        }

        return sess;
    }
}

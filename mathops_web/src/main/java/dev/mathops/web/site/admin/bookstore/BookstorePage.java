package dev.mathops.web.site.admin.bookstore;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawlogic.RawWhichDbLogic;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rawrecord.RawWhichDb;
import dev.mathops.session.CsuLiveRegChecker;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.session.SessionResult;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminPage;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A base class for pages in the welcome site.
 */
enum BookstorePage {
    ;

    /** Milliseconds per day. */
    private static final long MILLIS_PER_DAY = 86400000L;

    /** A request parameter. */
    private static final String ACT_AS_STU_ID = "actAsStuId";

    /** A request parameter. */
    private static final String BECOME_STU_ID = "becomeStuId";

    /** A request parameter. */
    private static final String ADJUST_DATE = "adjustDate";

    /** A request parameter. */
    private static final String TARGET = "target";

    /** A request parameter. */
    private static final String WEEK_SUB = "weeksub";

    /** A request parameter. */
    private static final String WEEK_ADD = "weekadd";

    /** A request parameter. */
    private static final String DAY_SUB = "daysub";

    /** A request parameter. */
    private static final String DAY_ADD = "dayadd";

    /** A request parameter. */
    private static final String RESET = "reset";

    /**
     * Creates an {@code HtmlBuilder} and starts a system administration page, emitting the page start and the top level
     * header.
     *
     * @param data    the web view data
     * @param site    the owning site
     * @param session the login session
     * @return the created {@code HtmlBuilder}
     * @throws SQLException if there is an error accessing the database
     */
    static HtmlBuilder startBookstorePage(final WebViewData data, final AdminSite site,
                                          final ImmutableSessionInfo session) throws SQLException {

        final Cache cache = data.getCache();
        final RawWhichDb whichDb = RawWhichDbLogic.query(cache);

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String title = site.getTitle();
        Page.startOrdinaryPage(htm, title, null, false, null, "home.html", Page.NO_BARS, null, false, true);
        AdminPage.emitPageHeader(htm, session, whichDb, false);

        return htm;
    }

    /**
     * Processes any submissions by the role controls (call on POST).
     *
     * @param data    the web view data
     * @param site    the site
     * @param req     the HTTP request
     * @param resp    the HTTP response
     * @param session the user session info
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void processRoleControls(final WebViewData data, final AdminSite site, final ServletRequest req,
                                    final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final ISessionManager sessMgr = SessionManager.getInstance();

        final String actAsStu = req.getParameter(ACT_AS_STU_ID);
        final String becomeStu = req.getParameter(BECOME_STU_ID);
        final String adjustDate = req.getParameter(ADJUST_DATE);
        final String target = req.getParameter(TARGET);

        if (target == null) {
            final String msg = Res.get(Res.NO_ROLE_TARGET);
            PageError.doGet(data, site, req, resp, session, msg);
        } else if (AbstractSite.isParamInvalid(actAsStu) || AbstractSite.isParamInvalid(becomeStu)
                || AbstractSite.isParamInvalid(adjustDate) || AbstractSite.isParamInvalid(target)) {
            final String msg1 = Res.get(Res.POSSIBLE_ATTACK);
            final String msg2 = Res.fmt(Res.ATTACK_PARAM, ACT_AS_STU_ID, actAsStu);
            final String msg3 = Res.fmt(Res.ATTACK_PARAM, BECOME_STU_ID, becomeStu);
            final String msg4 = Res.fmt(Res.ATTACK_PARAM, ADJUST_DATE, adjustDate);
            final String msg5 = Res.fmt(Res.ATTACK_PARAM, TARGET, target);
            Log.warning(msg1);
            Log.warning(msg2);
            Log.warning(msg3);
            Log.warning(msg4);
            Log.warning(msg5);
        } else {
            final int dateAdjust;
            if (WEEK_SUB.equals(adjustDate)) {
                dateAdjust = -7;
            } else if (WEEK_ADD.equals(adjustDate)) {
                dateAdjust = 7;
            } else if (DAY_SUB.equals(adjustDate)) {
                dateAdjust = -1;
            } else if (DAY_ADD.equals(adjustDate)) {
                dateAdjust = 1;
            } else if (RESET.equals(adjustDate)) {
                dateAdjust = (int) (-session.timeOffset / MILLIS_PER_DAY);
            } else {
                dateAdjust = 0;
            }

            String actAs2 = actAsStu == null ? null
                    : actAsStu.trim().replace(CoreConstants.DASH, CoreConstants.EMPTY);
            String become2 = becomeStu == null ? null
                    : becomeStu.trim().replace(CoreConstants.DASH, CoreConstants.EMPTY);

            if (actAs2 != null && !actAs2.isEmpty()) {
                final char firstChar = actAs2.charAt(0);

                if (Character.isLetter(firstChar)) {
                    final Cache cache = data.getCache();
                    actAs2 = idFromStudentName(cache, actAs2);
                }
            }
            if (become2 != null && !become2.isEmpty()) {
                final char firstChar = become2.charAt(0);

                if (Character.isLetter(firstChar)) {
                    final Cache cache = data.getCache();
                    become2 = idFromStudentName(cache, become2);
                }
            }

            ImmutableSessionInfo sess = session;

            if (dateAdjust != 0) {
                sess = adjustDate(sessMgr, dateAdjust, sess);
            }

            if (become2 == null) {
                if (actAs2 != null) {
                    actAsStudent(data, sessMgr, actAs2, sess);
                }
            } else {
                becomeStudent(data, sessMgr, become2, sess);
            }

            resp.sendRedirect(target);
        }
    }

    /**
     * Attempts to resolve a student name into a student ID.
     *
     * @param cache the data cache
     * @param name  the name
     * @return the ID if successful, null if not
     * @throws SQLException if there was an error accessing the database
     */
    private static String idFromStudentName(final Cache cache, final String name) throws SQLException {

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
     * @param data    the web view data
     * @param sessMgr the session manager
     * @param newStu  the new student ID
     * @param session the current session information
     * @return the new session information (updated if the change was permitted; or the original session information if
     *         not)
     * @throws SQLException of there was an error accessing the database
     */
    private static ImmutableSessionInfo actAsStudent(final WebViewData data, final ISessionManager sessMgr,
                                                     final String newStu, final ImmutableSessionInfo session)
            throws SQLException {

        ImmutableSessionInfo sess = session;

        if (session.getEffectiveRole().canActAs(ERole.STUDENT)) {
            final SessionResult res = sessMgr.setEffectiveUserId(data, session.loginSessionId, newStu);

            final Cache cache = data.getCache();
            CsuLiveRegChecker.checkLiveReg(cache, newStu);
            // StudentCache.get(primary, secondary).liveQueryStudent(newStu);

            if (res != null && res.session != null) {
                sess = res.session;
                final String msg = Res.fmt(Res.ACTING_AS, newStu);
                Log.info(msg);
            }
        }

        return sess;
    }

    /**
     * Processes a request to become a new student.
     *
     * @param data    the web view data
     * @param sessMgr the session manager
     * @param newStu  the new student ID
     * @param session the current session information
     * @return the new session information (updated if the change was permitted; or the original session information if
     *         not)
     * @throws SQLException of there was an error accessing the database
     */
    private static ImmutableSessionInfo becomeStudent(final WebViewData data, final ISessionManager sessMgr,
                                                      final String newStu, final ImmutableSessionInfo session)
            throws SQLException {

        ImmutableSessionInfo sess = session;

        if (session.getEffectiveRole().canActAs(ERole.STUDENT)) {
            final ERole newRole = ERole.STUDENT;

            final SessionResult res = sessMgr.setUserId(data, session.loginSessionId, newStu, newRole);

            final Cache cache = data.getCache();
            CsuLiveRegChecker.checkLiveReg(cache, newStu);

            if (res != null && res.session != null) {
                sess = res.session;
                final String msg = Res.fmt(Res.BECOMING, newStu);
                Log.info(msg);
            }
        }

        return sess;
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

        if (session.getEffectiveRole().canActAs(ERole.STUDENT)) {

            final long curOffset = session.timeOffset;
            final long newOffset = curOffset + (long) days * MILLIS_PER_DAY;

            final SessionResult res = sessMgr.setTimeOffset(session.loginSessionId, newOffset);

            if (res != null && res.session != null) {
                sess = res.session;
            }
        }

        return sess;
    }

    /**
     * Emits the form used to enter an e-text key.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param key   the initial value to load in for the key
     * @param error an optional error message to display
     */
    static void emitKeyForm(final HtmlBuilder htm, final String key, final String error) {

        htm.sDiv("indent22");
        htm.div("gap2");
        htm.addln(" <form action='check_etext_key.html' method='post'>");
        htm.sDiv("center");
        htm.addln(Res.get(Res.CHECK_STATUS_PROMPT)).br();
        htm.add("<input style='margin:1em; border:2px solid #050;' type='text' name='key'");
        if (key != null) {
            htm.add(" value='", key, "'");
        }
        htm.addln('>').br();
        final String lbl = Res.get(Res.CHECK_BTN_LBL);
        htm.addln("<input class='btn' type='submit' value='", lbl, "'/>");
        htm.eDiv();
        htm.addln("</form>");
        htm.sP("center");
        if (error == null) {
            htm.addln("&nbsp;");
        } else {
            htm.addln("<strong><span class='red'>", error, "</span></strong>");
        }
        htm.eP();
        htm.hr("orange");
        htm.eDiv();
    }
}

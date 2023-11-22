package dev.mathops.web.site.placement.main;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.WebSiteProfile;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.EProctoringType;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;

import javax.servlet.ServletConfig;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * The math placement website.
 */
public final class MathPlacementSite extends AbstractPageSite {

    /** Proctor passwords (one for ProctorU, one for UTC, one for Honorlock). */
    private static final String[] CORRECT = {"AfumwaviLiz7", "UpqoujziAki6", "EvjiosgoAwoq", "mathplacement4u"};

    /** The secondary footer. */
    private final WelcomeFooter footer;

    /**
     * Constructs a new {@code MathPlacementSite}.
     *
     * @param theSiteProfile the context under which this site is accessed
     * @param theSessions    the singleton user session repository
     */
    public MathPlacementSite(final WebSiteProfile theSiteProfile, final ISessionManager theSessions) {

        super(theSiteProfile, theSessions);

        this.footer = new WelcomeFooter();
    }

    /**
     * Gets the welcome footer.
     *
     * @return the footer
     */
    public WelcomeFooter getFooter() {

        return this.footer;
    }

    /**
     * Generates the site title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return "Math Placement";
    }

    /**
     * Initializes the site - called when the servlet is initialized.
     *
     * @param config the servlet context in which the servlet is being initialized
     */
    @Override
    public void init(final ServletConfig config) {

        // No action
    }

    /**
     * Indicates whether this site should do live queries to update student registration data.
     *
     * @return true to do live registration queries; false to skip
     */
    @Override
    public boolean doLiveRegQueries() {

        return false;
    }

    /**
     * Processes a GET request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache    the data cache
     * @param subpath  the portion of the path beyond that which was used to select this site
     * @param type the site type
     * @param req      the request
     * @param resp     the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public void doGet(final Cache cache, final String subpath, final ESiteType type,
                      final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        // Log.info("GET ", subpath);

        if ("basestyle.css".equals(subpath)) {
            sendReply(req, resp, "text/css",
                    FileLoader.loadFileAsBytes(Page.class, "basestyle.css", true));
        } else if ("style.css".equals(subpath)) {
            sendReply(req, resp, "text/css",
                    FileLoader.loadFileAsBytes(getClass(), "style.css", true));
        } else if ("favicon.ico".equals(subpath)) {
            serveImage(subpath, req, resp);
        } else if (CoreConstants.EMPTY.equals(subpath) || "welcome.html".equals(subpath)) {
            resp.sendRedirect("placement.html");
        } else if (subpath.startsWith("ramready.svc")) {
            RamReadyService.process(cache, this, subpath, req, resp);
//        } else if (subpath.startsWith("ramready.svc")) {
//            PageRamReady.doGet(cache, this, subpath, req, resp);
        } else {
            ImmutableSessionInfo session = validateSession(req, resp, null);

            if (session != null) {
                final String userId = session.getEffectiveUserId();
                if ("GUEST".equals(userId) || "AACTUTOR".equals(userId)) {
                    SessionManager.getInstance().logout(session.loginSessionId);
                    session = null;
                }
            }

            if ("placement.html".equals(subpath)) {
                PagePlacement.doGet(cache, this, req, resp, session);
            } else if ("secure/shibboleth.html".equals(subpath)) {
                doShibbolethLogin(cache, req, resp, session);
            } else if ("login_test_user_99.html".equals(subpath)) {
                PageToolLoginTestUser.doGet(cache, this, req, resp);
            } else if (session == null) {
                // Legacy URLs and mistypes
                PagePlacement.doGet(cache, this, req, resp, null);
            } else if ("rolecontrol.html".equals(subpath)) {
                processRoleControls(cache, req, resp, session);
            } else if ("secure_landing.html".equals(subpath)) {
                PageSecureLanding.doGet(cache, this, req, resp, session);
            } else if ("plan_start.html".equals(subpath)) {
                PagePlanStart.doGet(cache, this, req, resp, session);
            } else if ("plan_majors1.html".equals(subpath)) {
                PagePlanMajors1.doGet(cache, this, req, resp, session);
            } else if ("plan_majors2.html".equals(subpath)) {
                PagePlanMajors2.doGet(cache, this, req, resp, session);
            } else if ("plan_record.html".equals(subpath)) {
                PagePlanRecord.doGet(cache, this, req, resp, session);
            } else if ("plan_view.html".equals(subpath)) {
                PagePlanView.doGet(cache, this, req, resp, session);
            } else if ("plan_next.html".equals(subpath)) {
                PagePlanNext.doGet(cache, this, req, resp, session);
            } else if ("missing.html".equals(subpath)) {
                PageMissing.doGet(cache, this, req, resp, session);
            } else if ("review.html".equals(subpath)) {
                PageReview.doGet(this, req, resp, session);
            } else if ("review_outline.html".equals(subpath)) {
                PageReviewOutline.doGet(cache, this, req, resp, session);
            } else if ("review_lesson.html".equals(subpath)) {
                PageReviewLesson.doGet(cache, this, req, resp, session);
            } else if ("review_homework.html".equals(subpath)) {
                PageReviewHomework.doGet(cache, this, req, resp, session);
            } else if ("tool.html".equals(subpath)) {
                PageToolHome.doGet(cache, this, req, resp, session);
            } else if ("tool_fees.html".equals(subpath)) {
                PageToolFees.doGet(this, req, resp, session);
            } else if ("tool_instructions_tc.html".equals(subpath)) {
                PageToolInstructionsTc.doGet(cache, this, req, resp, session);
            } else if ("tool_instructions_pu.html".equals(subpath)) {
                PageToolInstructionsPu.doGet(cache, this, req, resp, session);
            } else if ("tool_schedule_pu.html".equals(subpath)) {
                PageToolSchedulePu.doGet(this, req, resp, session);
            } else if ("tool_start_pu.html".equals(subpath)) {
                PageToolStartPu.doGet(this, req, resp, session);
            } else if ("tool_proctor_login.html".equals(subpath)) {
                PageToolProctorLogin.doGet(cache, this, req, resp, session);
            } else if ("tool_process_proctor_login_pu.html".equals(subpath)) {
                doProcessProctorLoginPu(cache, req, resp, session);
            } else if ("tool_update_placement_exam.html".equals(subpath)) {
                PageToolHtmlPlacementExam.doGet(cache, this, req, resp, session);
            } else if ("tool_instructions_elm.html".equals(subpath)) {
                PageToolInstructionsElm.doGet(this, req, resp, session);
            } else if ("tool_instructions_re.html".equals(subpath)) {
                PageToolInstructionsRe.doGet(cache, this, req, resp, session);
            } else if ("tool_start_re.html".equals(subpath)) {
                PageToolStartRe.doGet(this, req, resp, session);
            } else if ("tool_taking_exam_re.html".equals(subpath)) {
                PageToolHtmlPlacementExam.startPlacementTool(cache, this, req, resp, session,
                        EProctoringType.NONE);
            } else {
                Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    /**
     * Processes a POST request. Before this method is called, the request will have been verified to be secure and have
     * a session ID.
     *
     * @param cache    the data cache
     * @param subpath  the portion of the path beyond that which was used to select this site
     * @param type the site type
     * @param req      the request
     * @param resp     the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    @Override
    public void doPost(final Cache cache, final String subpath, final ESiteType type,
                       final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        // Log.info("POST ", subpath);

        ImmutableSessionInfo session = validateSession(req, resp, null);

        if (session != null) {
            final String userId = session.getEffectiveUserId();
            if ("GUEST".equals(userId) || "AACTUTOR".equals(userId)) {
                SessionManager.getInstance().logout(session.loginSessionId);
                session = null;
            }
        }

        if (session == null) {
            Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else if ("rolecontrol.html".equals(subpath)) {
            processRoleControls(cache, req, resp, session);
        } else if ("plan_start.html".equals(subpath)) {
            PagePlanStart.doPost(cache, this, req, resp, session);
        } else if ("plan_majors1.html".equals(subpath)) {
            PagePlanMajors1.doPost(cache, this, this.siteProfile, req, resp, session);
        } else if ("plan_majors2.html".equals(subpath)) {
            PagePlanMajors2.doPost(cache, this, this.siteProfile, req, resp, session);
        } else if ("plan_view.html".equals(subpath)) {
            PagePlanView.doPost(cache, this, req, resp, session);
        } else if ("plan_next.html".equals(subpath)) {
            PagePlanNext.doPost(cache, this, req, resp, session);
        } else if ("review_homework.html".equals(subpath)) {
            PageReviewHomework.doPost(cache, this, req, resp, session);
        } else if ("tool_process_proctor_login_pu.html".equals(subpath)) {
            doProcessProctorLoginPu(cache, req, resp, session);
        } else if ("tool_update_placement_exam.html".equals(subpath)) {
            PageToolHtmlPlacementExam.doPost(cache, this, req, resp, session);
        } else if ("login_test_user_by_id.html".equals(subpath)) {
            PageToolLoginTestUser.doPost(cache, this, req, resp);
        } else {
            Log.warning(Res.fmt(Res.UNRECOGNIZED_PATH, subpath));
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Shows the top bar with "Logged in as [Name]" and with a button to return to the home page.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the login session
     */
    static void emitLoggedInAs1(final HtmlBuilder htm,
                                final ImmutableSessionInfo session) {

        htm.sDiv("inset");
        htm.sDiv("shaded", "style='padding-left:12px;'");
        htm.sDiv("right", "style='padding-left:10px;'")
                .add("<a class='smallbtn' style='margin:0;' href='placement.html'>",
                        Res.get(Res.BACK_TO_HOME), "</a>")
                .eDiv();

        htm.sDiv(null, "style='margin-top:5px;'")
                .add(Res.fmt(Res.LOGGED_IN_AS, session.getEffectiveScreenName())) //
                .eDiv();
        htm.div("clear");

        htm.eDiv(); // shaded
        htm.eDiv(); // inset

        htm.div("vgap");
    }

    /**
     * Shows the top bar with "Logged in as [Name]" and with a button to return to the secure page.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the login session
     */
    static void emitLoggedInAs2(final HtmlBuilder htm,
                                final ImmutableSessionInfo session) {

        htm.sDiv("inset");
        htm.sDiv("shaded", "style='padding-left:12px;'");
        htm.sDiv("right", "style='padding-left:10px;'")
                .add("<a class='smallbtn' style='margin:0;' href='secure_landing.html'>",
                        Res.get(Res.BACK_TO_SECURE), "</a>")
                .eDiv();

        htm.sDiv(null, "style='margin-top:5px;'")
                .add(Res.fmt(Res.LOGGED_IN_AS, session.getEffectiveScreenName())) //
                .eDiv();
        htm.div("clear");

        htm.eDiv(); // shaded
        htm.eDiv(); // inset

        htm.div("vgap");
    }

    /**
     * Shows the top bar with "Logged in as [Name]" and with a button to return to the secure page.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the login session
     */
    static void emitLoggedInAs3(final HtmlBuilder htm, final ImmutableSessionInfo session) {

        htm.sDiv("inset");
        htm.sDiv("shaded", "style='padding-left:12px;'");
        htm.sDiv("right", "style='padding-left:10px;'")
                .add("<a class='smallbtn' style='margin:0;' href='tool.html'>",
                        Res.get(Res.BACK_TO_TOOL), "</a>")
                .eDiv();

        htm.sDiv(null, "style='margin-top:5px;'")
                .add(Res.fmt(Res.LOGGED_IN_AS, session.getEffectiveScreenName())) //
                .eDiv();
        htm.div("clear");

        htm.eDiv(); // shaded
        htm.eDiv(); // inset

        htm.div("vgap");
    }

    /**
     * Scans the request for Shibboleth attributes and uses them (if found) to establish a session, and then redirects
     * to either the secure page (if valid) or the login page (if not valid).
     *
     * @param cache   the data cache
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws SQLException if there was an error accessing the database
     */
    private void doShibbolethLogin(final Cache cache, final HttpServletRequest req,
                                   final HttpServletResponse resp, final ImmutableSessionInfo session) throws SQLException {

        ImmutableSessionInfo sess = session;

        if (sess == null) {
            sess = processShibbolethLogin(cache, req);
        }

        final String target = sess == null ? "placement.html"
                : "secure_landing.html";

        if (sess != null) {
            // Install the session ID cookie in the response
            final Cookie cook = new Cookie(SessionManager.SESSION_ID_COOKIE, sess.loginSessionId);
            cook.setDomain(req.getServerName());
            cook.setPath(CoreConstants.SLASH);
            cook.setMaxAge(-1);
            cook.setSecure(true);
            resp.addCookie(cook);
        }

        final String path = this.siteProfile.path;
        final String redirect = path + (path.endsWith(Contexts.ROOT_PATH) //
                ? target : CoreConstants.SLASH + target);

        resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        resp.setHeader("Location", redirect);
    }

    /**
     * Tests whether the password entered by the ProctorU proctor is correct.
     *
     * @param cache   the data cache
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private void doProcessProctorLoginPu(final Cache cache, final ServletRequest req,
                                         final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String password = req.getParameter("drowssap");

        boolean match = false;
        for (final String test : CORRECT) {
            if (test.equals(password)) {
                match = true;
                break;
            }
        }

        if (match) {
            PageToolHtmlPlacementExam.startPlacementTool(cache, this, req, resp, session,
                    EProctoringType.PROCTORU);
        } else {
            resp.sendRedirect("tool_proctor_login.html?error=Invalid%20password.");
        }
    }
}

package dev.mathops.web.site.placement.main;

import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.EProctoringType;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.placementexam.PlacementExamSession;
import dev.mathops.web.site.html.placementexam.PlacementExamSessionStore;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Presents a placement exam.
 */
enum PageToolHtmlPlacementExam {
    ;

    /**
     * Starts a review exam and presents the Math Placement Tool instructions.
     *
     * @param cache      the data cache
     * @param site       the owning site
     * @param req        the request
     * @param resp       the response
     * @param session    the user's login session information
     * @param proctoring type of proctoring used
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void startPlacementTool(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                                   final HttpServletResponse resp, final ImmutableSessionInfo session,
                                   final EProctoringType proctoring)
            throws IOException, SQLException {

        final String studentId = session.getEffectiveUserId();

        // FIXME: Begin block that should be synchronized on the placement store's cache (from
        // here to "End block" comment below)

        final PlacementExamSessionStore store = PlacementExamSessionStore.getInstance();
        PlacementExamSession pes = store.getPlacementExamSessionForStudent(studentId);

        final String examId = switch (proctoring) {
            case DEPT_TESTING_CENTER -> "MPTTC";
            case STAFF_PROCTORING, RAMWORK -> "MPTRW";
            case UNIV_TESTING_CENTER, SDC_TESTING_CENTER -> "MPTUT";
            case PROCTORU -> "MPTPU";
            case HONORLOCK -> "MPTHL";
            case RESPONDUS -> "MPTRM";
            default -> "MPTUN";
        };

        if (pes == null) {
            pes = new PlacementExamSession(cache, site.siteProfile, session.loginSessionId, studentId,
                    proctoring != EProctoringType.NONE, examId, "tool.html");
            store.setPlacementExamSession(pes);
        }
        // End block

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startEmptyPage(htm, site.getTitle(), false);
        htm.sDiv("exam");

        htm.addln("<form id='placement_exam_form' action='tool_update_placement_exam.html' method='POST'>");
        htm.addln(" <input type='hidden' id='placement_exam_act' name='action'>");
        pes.generateHtml(cache, session.getNow(), req, htm);
        htm.addln("</form>");

        htm.eDiv(); // exam
        MPPage.emitScripts(htm);

        Page.endEmptyPage(htm, false);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Handles a GET request.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String studentId = session.getEffectiveUserId();

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startEmptyPage(htm, site.getTitle(), false);
        htm.sDiv("exam");

        final PlacementExamSessionStore store = PlacementExamSessionStore.getInstance();
        final PlacementExamSession res = store.getPlacementExamSessionForStudent(studentId);
        if (res == null) {
            htm.sDiv("indent33");
            htm.sP().add("Exam not found.").eP();
            htm.addln("<form action='tool.html' method='GET'>");
            htm.addln(" <input type='submit' value='Close'>");
            htm.addln("</form>");
            htm.eDiv();
        } else {
            htm.addln("<form id='placement_exam_form' action='tool_update_placement_exam.html' method='POST'>");
            htm.addln(" <input type='hidden' id='placement_exam_act' name='action'>");
            res.generateHtml(cache, session.getNow(), req, htm);
            htm.addln("</form>");
        }

        htm.eDiv(); // exam

        MPPage.emitScripts(htm);

        Page.endEmptyPage(htm, false);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Handles a POST request.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                       final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String studentId = session.getEffectiveUserId();

        String redirect = null;

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startEmptyPage(htm, site.getTitle(), false);
        htm.sDiv("exam");

        final PlacementExamSessionStore store = PlacementExamSessionStore.getInstance();
        final PlacementExamSession pes = store.getPlacementExamSessionForStudent(studentId);

        if (pes == null) {
            htm.sDiv("indent33");
            htm.sP().add("Exam not found.").eP();
            htm.addln("<form action='tool.html' method='GET'>");
            htm.addln(" <input type='submit' value='Close'>");
            htm.addln("</form>");
            htm.eDiv();
        } else {
            htm.addln("<form id='placement_exam_form' action='tool_update_placement_exam.html' method='POST'>");
            htm.addln(" <input type='hidden' id='placement_exam_act' name='action'>");
            redirect = pes.processPost(cache, session, req, htm);
            htm.addln("</form>");
        }

        if (redirect == null) {
            htm.eDiv(); // exam
            MPPage.emitScripts(htm);

            Page.endEmptyPage(htm, false);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        } else {
            resp.sendRedirect(redirect);
        }
    }
}

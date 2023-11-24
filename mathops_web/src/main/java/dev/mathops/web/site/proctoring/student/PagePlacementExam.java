package dev.mathops.web.site.proctoring.student;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.placementexam.PlacementExamSession;
import dev.mathops.web.site.html.placementexam.PlacementExamSessionStore;
import dev.mathops.web.websocket.proctor.MPSEndpoint;
import dev.mathops.web.websocket.proctor.MPSSession;
import dev.mathops.web.websocket.proctor.MPSSessionManager;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * A page that will display in an IFrame to present a placement exam within a proctoring session.
 */
enum PagePlacementExam {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final ProctoringSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startPage(htm, Res.get(Res.SITE_TITLE), false, false);
        htm.addln("<body style='background:white;color:black;height:calc(100% - 50px);'>");

        final String studentId = session.getEffectiveUserId();
        final MPSSession ps = MPSSessionManager.getInstance().getSessionForStudent(studentId);

        if (ps != null) {
            ps.timeout = System.currentTimeMillis() + MPSEndpoint.SESSION_TIMEOUT_MS;

            final LocalDateTime newTimeout =
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(ps.timeout), ZoneId.systemDefault());
            Log.info("Updating timeout on session ", ps, " to ",
                    TemporalUtils.FMT_MDY_HMS.format(newTimeout));

            if ("MPTRW".equals(ps.examId)) {
                emitDesmosLink(htm);

                final PlacementExamSessionStore store = PlacementExamSessionStore.getInstance();
                PlacementExamSession pes = store.getPlacementExamSessionForStudent(studentId);

                if (pes == null) {
                    Log.info("Starting proctored placement exam session for student ", studentId, " exam MPTRW");

                    pes = new PlacementExamSession(cache, site.siteProfile, session.loginSessionId, studentId, true,
                            "MPTRW", "placement_done.html");
                    store.setPlacementExamSession(pes);
                }

                htm.addln("<form id='placement_exam_form' action='placement.html' method='POST'>");
                htm.addln(" <input type='hidden' id='placement_exam_act' name='action'>");
                pes.generateHtml(cache, session.getNow(), req, htm);
                htm.addln("</form>");
            } else {
                htm.sP().addln("Unrecognized unit exam ID").eP();
            }
        }

        htm.addln("</body>");
        Page.endPage(htm);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final Cache cache, final ServletRequest req, final HttpServletResponse resp,
                       final ImmutableSessionInfo session) throws IOException, SQLException {

        final String studentId = session.getEffectiveUserId();
        final MPSSession ps = MPSSessionManager.getInstance().getSessionForStudent(studentId);

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startPage(htm, Res.get(Res.SITE_TITLE), false, false);
        htm.addln("<body style='background:white;color:black;height:calc(100% - 50px);'>");

        if (ps != null) {
            ps.timeout = System.currentTimeMillis() + MPSEndpoint.SESSION_TIMEOUT_MS;

            final LocalDateTime newTimeout =
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(ps.timeout), ZoneId.systemDefault());
            Log.info("Updating timeout on session ", ps, " to ",
                    TemporalUtils.FMT_MDY_HMS.format(newTimeout));

            if ("MPTRW".equals(ps.examId)) {

                final PlacementExamSessionStore store = PlacementExamSessionStore.getInstance();
                final PlacementExamSession pes = store.getPlacementExamSessionForStudent(studentId);

                String redirect = null;

                if (pes == null) {
                    htm.sP().add("Logged in as: ", session.getEffectiveScreenName()).eP();
                    htm.sP().add("No active placment tool session found for student ",
                            session.getEffectiveUserId(), CoreConstants.DOT).eP();
                } else {
                    emitDesmosLink(htm);

                    htm.sDiv("inset");

                    htm.addln("<form id='placement_exam_form' action='placement.html' ",
                            "method='POST'>");
                    htm.addln(" <input type='hidden' id='placement_exam_act' name='action'>");
                    redirect = pes.processPost(cache, session, req, htm);
                    htm.addln("</form>");

                    htm.eDiv(); // inset
                }

                if (redirect == null) {
                    Page.endEmptyPage(htm, true);

                    AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML,
                            htm.toString().getBytes(StandardCharsets.UTF_8));
                } else {
                    resp.sendRedirect(redirect);
                }
            } else {
                htm.sP().addln("Unrecognized exam ID").eP();
            }
        }

        htm.addln("</body>");
        Page.endPage(htm);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits the message and link to Desmos.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitDesmosLink(final HtmlBuilder htm) {

        htm.sDiv("inset")
                .add("You are welcome to use scratch paper, and you can use either your own ",
                        "personal graphing calculator, or the <strong>Desmos calculator</strong> ",
                        "while completing the Math Placement Tool. &nbsp; ",
                        "<a href='https://www.desmos.com/calculator' target='_blank' rel='noopener'>",
                        "Open Desmos...</a>")
                .eDiv();
        htm.div("vgap0");
        htm.hr();
    }

    /**
     * Generates the page.
     *
     * @param type the site type
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void showDone(final ESiteType type, final ServletRequest req,
                         final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startPage(htm, Res.get(Res.SITE_TITLE), false, false);
        htm.addln("<body style='background:white;color:black;height:100%;'>");

        htm.div("vgap");

        htm.sDiv("inset2");
        htm.sH(3).add("The Math Placement Tool has been completed").eH(3);

        htm.sP().add("You can check your results at the Math Placement web site.").eP();

        htm.sP("indent").add("<button class='btn' id='view_placement_results'>Math Placement web site</button>").sP();

        htm.eDiv(); // inset2

        htm.addln("<script>");
        htm.addln("document.getElementById('view_placement_results').onclick = function(ev) {");
        if (type == ESiteType.PROD) {
            htm.addln("  window.top.location.href = \"https://placement.math.colostate.edu/index.html\";");
        } else {
            htm.addln("  window.top.location.href = \"https://placementdev.math.colostate.edu/index.html\";");
        }
        htm.addln("}");
        htm.addln("let event = new CustomEvent('examEnded')");
        htm.addln("window.parent.document.dispatchEvent(event)");
        htm.addln("</script>");

        htm.addln("</body>");

        Page.endPage(htm);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}

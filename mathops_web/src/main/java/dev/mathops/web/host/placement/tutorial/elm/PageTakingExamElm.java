package dev.mathops.web.host.placement.tutorial.elm;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.unitexam.UnitExamSession;
import dev.mathops.web.site.html.unitexam.UnitExamSessionStore;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates the content of the home page.
 */
enum PageTakingExamElm {
    ;

    /**
     * Generates the home page.
     *
     * @param cache   the data
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final ElmTutorialSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Entry Level Mathematics Tutorial",
                "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

        doPage(cache, site, htm, session);

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Displays the instructions for what to do after the exam, then launches the exam.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the user's login session information
     * @throws SQLException if there is an error accessing the database
     */
    private static void doPage(final Cache cache, final ElmTutorialSite site, final HtmlBuilder htm,
                               final ImmutableSessionInfo session) throws SQLException {

        htm.sH(2).add("ELM Exam Administered by ProctorU").eH(2);

        // FIXME: The following duplicates code in HtmlUnitExamPage - refactor to re-use

        final UnitExamSessionStore store = UnitExamSessionStore.getInstance();
        UnitExamSession us = store.getUnitExamSession(session.loginSessionId, "MT4UE");

        if (us == null) {
            Log.info("Starting unit exam for session ", session.loginSessionId,
                    " user ", session.getEffectiveUserId(), " exam MT4UE");

            us = new UnitExamSession(cache, site.site, session.loginSessionId,
                    session.getEffectiveUserId(), RawRecordConstants.M100T, "MT4UE", "tutorial.html");
            store.setUnitExamSession(us);
        } else {
            Log.info("Found existing unit exam for session ", session.loginSessionId, " exam MT4UE");
        }

        htm.addln("<form id='unit_exam_form' action='update_unit_exam.html' method='POST'>");
        htm.addln(" <input type='hidden' name='exam' value='MT4UE'>");
        htm.addln(" <input type='hidden' name='course' value='M 100T'>");
        htm.addln(" <input type='hidden' id='unit_exam_act' name='action'>");
        us.generateHtml(cache, session, htm);
        htm.addln("</form>");
    }
}

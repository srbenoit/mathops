package dev.mathops.web.host.placement.tutorial.elm;

import dev.mathops.db.Cache;
import dev.mathops.db.logic.DateRange;
import dev.mathops.db.logic.DateRangeGroups;
import dev.mathops.db.old.logic.ELMTutorialStatus;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates the content of the home page.
 */
enum PageInstructionsElmPU {
    ;

    /**
     * Generates the home page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param status  the student status with respect to the ELM Tutorial
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final ElmTutorialSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final ELMTutorialStatus status) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Entry Level Mathematics Tutorial",
                "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

        htm.sDiv("menupanel");
        TutorialMenu.buildMenu(cache, session, status, htm);
        htm.sDiv("panel");

        doPage(htm, status);

        htm.eDiv(); // (end "panel" div)
        htm.eDiv(); // (end "menupanel" div)

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Creates the home page HTML.
     *
     * @param htm    the {@code HtmlBuilder} to which to append the HTML
     * @param status the student status with respect to the ELM Tutorial
     */
    private static void doPage(final HtmlBuilder htm, final ELMTutorialStatus status) {

        htm.sH(2).add("ELM Exam Administered by ProctorU").eH(2);

        htm.sDiv("indent11");

        htm.addln("<ul class='boxlist'>");

        htm.addln(" <li class='boxlist'>");
        htm.addln("  Schedule your <b>ELM Exam</b> with ProctorU after completing the ",
                "<b>ELM Tutorial</b> and passing the <b>Unit 4 Review Exam</b>.");
        htm.addln(" </li>");
        htm.div("vgap");

        final DateRangeGroups examDates = status.onlineProctoredExamAvailability;

        if (examDates.hasCurrentOrFuture()) {
            htm.addln("<li class='boxlist'>");
            htm.addln("<strong style='background-color:#FF9;'>The exam is available on the ",
                    "following days</strong> (except from 6am - 8am daily):");
            htm.sDiv(null, "style='margin-top:0.5em;line-height:1.5em;'");

            final DateRange cur = examDates.current;

            if (cur != null) {
                htm.addln("&nbsp; &nbsp; <strong>", cur, "</strong>").br();
            }
            for (final DateRange r : examDates.future) {
                htm.addln("&nbsp; &nbsp; ", r).br();
            }
            htm.addln("These dates are based on the <strong>U.S. Mountain Time ", "Zone</strong>.").br();

            htm.addln("The exam is currently ");
            if (cur == null) {
                htm.addln("<strong style='background-color:#FF9;'>not available</strong>.");
            } else {
                htm.addln("<strong style='background-color:#FF9;'>available</strong>.");
            }
            htm.eDiv().add("</li>");
            htm.div("vgap");
        }

        htm.addln("<li class='boxlist'>");
        htm.addln("<strong>ProctorU will charge a separate fee of $6.50 for their proctoring service (fee subject ",
                "to change).</strong> This fee will be collected by ProctorU at the time the exam is taken.");
        htm.addln("</li>");
        htm.div("vgap");

        htm.addln("<li class='boxlist'>");
        htm.addln("The time limit for the exam is 60 minutes. The exam will automatically be ",
                "submitted for grading when the time limit expires.");
        htm.addln("</li>");
        htm.div("vgap");

        htm.addln("<li class='boxlist'>");
        htm.addln("You may use a personal graphing calculator (eg. TI-83/84) on the exam, but ",
                "are <strong>not</strong> permitted <strong>any</strong> reference materials or outside assistance.");
        htm.addln("</li>");
        htm.div("vgap");

        htm.addln("<li class='boxlist'>");
        htm.addln("The exam consists of 20 questions. There is no penalty for guessing. To pass ",
                "the <span class='green'>ELM Exam</span>, you must answer at least 14 questions correctly.");
        htm.addln("</li>");
        htm.div("vgap");

        htm.addln("<li class='boxlist'>");
        htm.addln("Report any problems via email to ",
                "<a class='ulink2' href='mailto:precalc_math@colostate.edu'>precalc_math@colostate.edu</a>.");
        htm.addln("</li>");
        htm.div("vgap");

        htm.addln("</ul>");

        htm.sDiv("center");

        htm.addln(" <form style='display:inline;margin:20pt' method='get' action='schedule_elm_pu.html'>");
        htm.addln("  <input type='submit' class='btn' value='Tell me how to schedule this exam with ProctorU.'/>");
        htm.addln(" </form>");

        htm.addln(" &nbsp;");

        htm.addln(" <form style='display:inline;margin:20pt' method='get' action='start_elm_pu.html'>");
        htm.addln("  <div style='display:inline'>");
        htm.add("  <input type='submit' class='btn'");

        final boolean notEligible = !status.eligibleForElmExam;
        if (notEligible) {
            htm.add(" disabled='disabled'");
        }
        htm.add(" value='I am ready to take my scheduled exam now...'/>");
        if (notEligible) {
            htm.br().br().add("<span class='red'><strong>ELM Tutorial Unit 4 Review Exam ",
                    "not yet passed</strong></span>");
        }
        htm.eDiv();
        htm.addln(" </form>");

        htm.div("vgap2");

        htm.addln(" <a class='btn' href='tutorial.html'>Return to the tutorial outline</a>").br();

        htm.eDiv(); // center

        htm.eDiv(); // indent11
    }
}

package dev.mathops.web.site.placement.main;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractPageSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates the content of a page with instructions for taking the exam at the testing center.
 */
enum PageToolInstructionsTc {
    ;

    /**
     * Generates the page of information on a challenge attempt in the testing center.
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

        final HtmlBuilder htm = MPPage.startPage3(site, session);

        final String stuId = session.getEffectiveUserId();
        final RawStudent stu = RawStudentLogic.query(cache, stuId, false);

        htm.sDiv("inset2");
        htm.sDiv("shaded2left");

        htm.sP();
        htm.addln("The Math Placement Tool may be completed in the Precalculus Center's testing ",
                "area, Weber 138, during the Precalculus Center hours of operation.");
        htm.eP();

        htm.div("vgap");

        htm.sDiv("center");
        htm.sTable("plan-table", "style='display:inline-table;'");
        htm.sTr();
        htm.sTh().add("Format:").eTh();
        htm.sTd().add("50 items, multiple-choice / multiple selection").eTd();
        htm.eTr();
        htm.sTr();
        htm.sTh().add("Time limit:").eTh();
        if (stu != null && stu.timelimitFactor != null) {
            final float factor = stu.timelimitFactor.floatValue();

            if (factor > 1.49f && factor < 1.51f) {
                htm.sTd().add("2 hours, 20 minutes<br/>",
                        "(Adjusted to 3 hours, 30 minutes by accommodation)").eTd();
            } else if (factor > 1.99f && factor < 2.01f) {
                htm.sTd().add("2 hours, 20 minutes<br/>",
                        "(Adjusted to 4 hours, 40 minutes by accommodation)").eTd();
            } else if (factor > 2.49f && factor < 2.51f) {
                htm.sTd().add("2 hours, 20 minutes<br/>",
                        "(Adjusted to 5 hours, 50 minutes by accommodation)").eTd();
            } else if (factor > 2.99f && factor < 3.01f) {
                htm.sTd().add("2 hours, 20 minutes<br/>", "(Adjusted to 7 hours by accommodation)")
                        .eTd();
            } else {
                final int minutes = Math.round(140.0f * factor);
                if (minutes <= 140) {
                    htm.sTd().add("2 hours, 20 minutes").eTd();
                } else {
                    htm.sTd().add("2 hours, 20 minutes<br/>", "(Adjusted to ", Integer.toString(minutes),
                            " minutes by accommodation)").eTd();
                }
            }
        }
        htm.eTr();
        htm.sTr();
        htm.sTh().add("Calculator:").eTh();
        htm.sTd().add("Must use provided on-screen TI-84 calculator").eTd();
        htm.eTr();
        htm.sTr();
        htm.sTh().add("References:").eTh();
        htm.sTd().add("Not allowed").eTd();
        htm.eTr();
        htm.sTr();
        htm.sTh("highlight").add("Fee:").eTh();
        htm.sTd("highlight").add("<a class='ulink' href='tool_fees.html'>",
                "Tell me about the Math Placement fee</a>").eTd();
        htm.eTr();
        htm.eTable();
        htm.eDiv(); // center

        htm.div("vgap2");

        htm.addln("<ul>");

        htm.addln("<li>",
                "To place out of a course, you must demonstrate proficiency in all of its ",
                "prerequisite courses during the same attempt of the Math Placement Tool.  That means ",
                "you should not skip the algebra portion if you want to place out of trigonometry or ",
                "logarithmic &amp; exponential functions.",
                "</li>");
        htm.div("vgap");

        htm.addln("<li>",
                "Results are available immediately after completing the Math Placement Tool.",
                "</li>");
        htm.addln("</ul>");

        htm.div("vgap");

        AbstractPageSite.hours(cache, htm, true, false);

        htm.div("vgap");

        htm.sDiv("advice");
        htm.sDiv("left");
        htm.addln("<img width='90pt' height='90pt' src='/images/dialog-warning-2.png'/>");
        htm.eDiv(); // left

        htm.sDiv(null, "style='margin-left:80pt;'");
        htm.addln("You may only complete the Math Placement Tool twice. You should review and study before you begin.");
        htm.eDiv();

        htm.sDiv(null, "style='margin:8pt 20pt 0 100pt;'");
        htm.addln("<a class='ulink' href='review.html'>Use our interactive review materials.</a>");
        htm.eDiv();

        htm.sDiv(null, "style='margin-left:80pt;margin-top:8pt;'");
        htm.addln("Depending on your major, your results could directly affect your ability to ",
                "register for important classes in your degree program.");
        htm.eDiv();
        htm.div("clear");

        htm.eDiv(); // advice

        htm.div("vgap");

        htm.eDiv(); // shaded2left
        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }
}

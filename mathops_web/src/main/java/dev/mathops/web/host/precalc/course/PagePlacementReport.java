package dev.mathops.web.host.precalc.course;

import dev.mathops.db.Cache;
import dev.mathops.db.logic.placement.PlacementLogic;
import dev.mathops.db.logic.placement.PlacementStatus;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.host.placement.placement.PlacementReport;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates the content of the home page.
 */
enum PagePlacementReport {
    ;

    /**
     * Generates the home page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final CourseSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final CourseSiteLogic logic) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, null, null,
                Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

        htm.sDiv("menupanelu");
        CourseMenu.buildMenu(cache, site, session, logic, htm);
        htm.sDiv("panelu");

        final PlacementStatus pstatus = new PlacementLogic(cache, session.getEffectiveUserId(),
                logic.data.studentData.getStudent().aplnTerm, session.getNow()).status;

        PlacementReport.doPlacementReport(cache, pstatus, session, null, false, htm);

        htm.eDiv(); // panelu
        htm.eDiv(); // menupanelu

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Appends a message describing the number of times the student has completed the Math Placement Tool in both
     * proctored and unproctored settings, and how many tries are available in each.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param status the student's placement status
     */
    private static void appendYouHaveTaken(final HtmlBuilder htm, final PlacementStatus status) {

        htm.sP("indent11", "style='padding-left:32px;'");
        htm.add("<img src='/images/orange2.png' style='margin:-2px 0 0 -32px; padding-right:10px;'/>");

        if (status.attemptsUsed == 0) {
            htm.addln(" You have not yet completed the Math Placement Tool.");
        } else {
            htm.add(" You have completed the Math Placement Tool ");
            appendNumTimes(htm, status.attemptsUsed);
            htm.add(". ");

            if (status.attemptsRemaining == 0) {
                htm.addln(" You have no attempts remaining.");
            } else if (status.attemptsRemaining == 1) {
                htm.addln(" You have 1 attempt remaining.");
            } else {
                htm.addln(" You have " + status.attemptsRemaining + " attempts remaining.");
            }
        }

        htm.eP();
    }

    /**
     * Appends a string telling the user how many times they have attempted the exam or how many attempts they may use.
     * Based on the number of times passed in, one of the following is appended to the {@code HtmlBuilder}:
     *
     * <pre>
     *   0 times
     *   1 time
     *   N times (where N is a number larger than 1)
     * </pre>
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param numTimes the number of times
     */
    private static void appendNumTimes(final HtmlBuilder htm, final int numTimes) {

        if (numTimes == 1) {
            htm.add("one time");
        } else {
            htm.add(Integer.toString(numTimes), " times");
        }
    }
}

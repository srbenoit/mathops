package dev.mathops.web.site.scheduling;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Generates the home page for the scheduling system.
 */
enum PageHome {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final SchedulingSite site, final ServletRequest req,
                         final HttpServletResponse resp, final ImmutableSessionInfo session) throws IOException,
            SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, "Center Scheduling System", null, false, Page.ADMIN_BAR, null, false, true);

        htm.sDiv(null, "style='padding-left:16px; padding-right:16px;'");

        htm.sH(1).add("Center Scheduling System").eH(1);

        final String effectiveName = session.getEffectiveScreenName();
        htm.sDiv().add("Logged in as ", effectiveName).eDiv().hr();

        final String effectiveId = session.getEffectiveUserId();
        final ZonedDateTime sessionNow = session.getNow();
        final LocalDate today = sessionNow.toLocalDate();
        final boolean isAdmin = RawSpecialStusLogic.isSpecialType(cache, effectiveId, today, RawSpecialStus.STEVE);
        final boolean isMgr = RawSpecialStusLogic.isSpecialType(cache, effectiveId, today, RawSpecialStus.MANAGER);
        final boolean isStaff = RawSpecialStusLogic.isSpecialType(cache, effectiveId, today, RawSpecialStus.STAFF);
        final boolean isEmployee = RawSpecialStusLogic.isSpecialType(cache, effectiveId, today, RawSpecialStus.EMPLOY);

        // Present an interface with all features for which the user is authorized.
        if (isAdmin) {
            htm.sP().add("Administrative Functions:").eP();

            htm.addln("<ul>");
            htm.addln("<li>Create a workplace</li>");
            htm.addln("<li>Define job categories for which employees may or may not be trained</li>");
            htm.addln(
                    "<li>Create semesters, start and end dates, weekdays and hours of operation, holidays/breaks</li>");
            htm.addln("<li>Define weekly time slots (hours)</li>");
            htm.addln("<li>Define the number of employees desired in each position during each hour</li>");
            htm.addln("<li>Define the pay rate for each position</li>");
            htm.addln("<li>Define the maximum number of consecutive hours a worker can be assigned to a job</li>");
            htm.addln("<li>Enter a set of employees to be scheduled (to allow them to log in)</li>");
            htm.addln("<li>Define training sessions, professional development opportunities for employees</li>");
            htm.addln("</ul>");

        }
        if (isMgr || isAdmin) {
            htm.sP().add("Supervisor Functions:").eP();

            htm.addln("<ul>");
            htm.addln("<li>Construct or optimize schedule –suggest changes based on employee preferences</li>");
            htm.addln("<li>Add new employees</li>");
            htm.addln("<li>Terminate employees, release shift assignments.</li>");
            htm.addln("<li>Manually change regular shift assignments.</li>");
            htm.addln("<li>Post shifts</li>");
            htm.addln("<li>Assign shifts</li>");
            htm.addln("<li>Document employee attendance, issues, interventions, kudos</li>");
            htm.addln("<li>Document training session attendance</li>");
            htm.addln("<li>Generate report of upcoming week(s) with unfilled shifts</li>");
            htm.addln("<li>Generate report of employee performance</li>");
            htm.addln("</ul>");
        }
        if (isStaff) {
            htm.sP().add("Staff Functions:").eP();

            htm.addln("<ul>");
            htm.addln("<li>See the schedule</li>");
            htm.addln("<li>Document employee attendance, issues, interventions, kudos</li>");
            htm.addln("<li>Document reassignments in real time based on shortages</li>");
            htm.addln("</ul>");
        }
        if (isEmployee) {
            htm.sP().add("Employee Functions:").eP();

            htm.addln("<ul>");
            htm.addln("<li>Fill out or update availability and preferences for a semester</li>");
            htm.addln("<li>Look at assigned shifts</li>");
            htm.addln("<li>Notify supervisor of upcoming absence, illness, circumstance that prevents attendance</li>");
            htm.addln("<li>Post shifts</li>");
            htm.addln("<li>Look at available posted shifts</li>");
            htm.addln("<li>Take a posted shift</li>");
            htm.addln("<li>RSVP for training session</li>");
            htm.addln("</ul>");
        }

        htm.eDiv();

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}

package dev.mathops.web.site.reporting;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rec.ReportPermsRec;
import dev.mathops.db.old.reclogic.ReportPermsLogic;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates the home page.
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
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final ReportingSite site, final ServletRequest req,
                         final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);

        final ERole role = session.getEffectiveRole();
        final boolean isAdmin = role.canActAs(ERole.ADMINISTRATOR);

        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, isAdmin ? Page.ADMIN_BAR : Page.NO_BARS,
                null, false, true);
        htm.sH(2).add(Res.get(Res.HOME_HEADING)).eH(2);

        if (isAdmin) {
            htm.sP().add("<a href='reports_admin.html'>Manage Reports...</a>").eP();
        }
        htm.hr();

        final String myStuId = session.getEffectiveUserId();
        final String screenName = session.getEffectiveScreenName();
        htm.sP().add("Logged in as <strong>", screenName, "</strong>").eP();
        htm.div("vgap");

        final List<ReportPermsRec> allPerms = ReportPermsLogic.get(cache).queryAll(cache);
        final Set<EDefinedReport> reportIds = new HashSet<>(5);
        for (final ReportPermsRec rec : allPerms) {
            if (rec.stuId.equals(myStuId)) {
                final EDefinedReport defined = EDefinedReport.forId(rec.rptId);
                if (defined != null) {
                    reportIds.add(defined);
                }
            }
        }


        if (isAdmin || !reportIds.isEmpty()) {
            // Show list of reports user is authorized for

            if (reportIds.contains(EDefinedReport.MPT_BY_CATEGORY)
                    || reportIds.contains(EDefinedReport.MPT_BY_IDS)) {
                htm.sH(3).add("Math Placement Reports").eH(3);
                htm.addln("<ul>");
                if (reportIds.contains(EDefinedReport.MPT_BY_CATEGORY)) {
                    htm.addln("  <li><a href='placement_by_category.html'>",
                            "Math Placement progress by special category (Athletes, Engineering students, etc.)",
                            "</a></li>");
                }
                if (reportIds.contains(EDefinedReport.MPT_BY_IDS)) {
                    htm.addln("  <li><a href='placement_by_students.html'>",
                            "Math Placement progress for specified students",
                            "</a></li>");
                }
                htm.addln("</ul>");
            }

            if (reportIds.contains(EDefinedReport.PROGRESS_BY_SECTION)
                    || reportIds.contains(EDefinedReport.PROGRESS_BY_IDS)) {
                htm.sH(3).add("Precalculus Course Reports").eH(3);
                htm.addln("<ul>");
                if (reportIds.contains(EDefinedReport.PROGRESS_BY_SECTION)) {
                    htm.addln("  <li><a href='precalc_by_course.html'>",
                            "Precalculus Course progress by course and/or section",
                            "</a></li>");
                }
                if (reportIds.contains(EDefinedReport.PROGRESS_BY_IDS)) {
                    htm.addln("  <li><a href='precalc_by_students.html'>",
                            "Precalculus Course progress for specified students",
                            "</a></li>");
                }
                htm.addln("</ul>");
            }

        } else  {
            htm.sP().addln("This site provides reports for authorized personnel on the status of students or ",
                    "applicants with respect to Math Placement and progress through Precalculus courses.").eP();
            htm.sP().addln("You are not currently authorized to access any reports through this site.  To ",
                    "request access, please send an email (from your official CSU email account) to ",
                    "<a href='mailto:precalc_math@colostate.edu'>precalc_math@colostate.edu</a>",
                    "with your CSU ID number, the type of reports you would like to access, and a justification ",
                    "for your request.").eP();
        }

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}

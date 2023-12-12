package dev.mathops.web.site.admin.genadmin.reports;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdminTopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DecimalFormat;

/**
 * A page to access reports.
 */
public enum PageReports {
    ;

    /**
     * Generates the page with available reports.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.MONITOR_SYSTEM, htm);

        htm.addln("<h1>Reports</h1>");

        // Placement
        htm.sDiv("block4", "id='first'").add("<a href='report_placement.html'>");
        htm.sDiv("histogram4");
        PagePlacementReport.appendHistogram(cache, htm);
        htm.eDiv();
        htm.sDiv("center").add("Placement Activity").eDiv();
        htm.add("</a>").eDiv();

        htm.sDiv("block4").add("<a href='report_elm.html'>");
        htm.sDiv("histogram4");
        PageElmReport.appendHistogram(cache, htm);
        htm.eDiv();
        htm.sDiv("center").add("ELM Tutorial Activity").eDiv();
        htm.add("</a>").eDiv();

        htm.sDiv("block4").add("<a href='report_precalc.html'>");
        htm.sDiv("histogram4");
        PagePrecalcReport.appendHistogram(cache, htm);
        htm.eDiv();
        htm.sDiv("center").add("Precalc Tutorial Activity").eDiv();
        htm.add("</a>").eDiv();

        htm.sDiv("block4", "id='last'").add("<a href='report_mathplan.html'>");
        htm.sDiv("histogram4");
        PageMathPlanReport.appendHistogram(cache, htm);
        htm.eDiv();
        htm.sDiv("center").add("Math Plan Activity").eDiv();
        htm.add("</a>").eDiv();

        htm.div("vgap");

        htm.sDiv("block4", "id='first'").add("<a href='report_course_exams.html'>");
        htm.sDiv("histogram4");
        PageCourseExamsReport.appendHistogram(cache, htm);
        htm.eDiv();
        htm.sDiv("center").add("Course Exam Activity").eDiv();
        htm.add("</a>").eDiv();

        htm.sDiv("block4").add("<a href='report_course_homework.html'>");
        htm.sDiv("histogram4");
        PageCourseHomeworkReport.appendHistogram(cache, htm);
        htm.eDiv();
        htm.sDiv("center").add("Homework Activity").eDiv();
        htm.add("</a>").eDiv();

        final DecimalFormat fmt = new DecimalFormat("#,##0.#");
        final long total = Runtime.getRuntime().totalMemory();
        final long free = Runtime.getRuntime().freeMemory();
        final long used = total - free;
        final double totalMb = (double) total / 1048576.0;
        final double freeMb = (double) free / 1048576.0;
        final double usedMb = (double) used / 1048576.0;

        htm.sP().add("<strong>Memory Usage</strong>: using ", fmt.format(usedMb), " Mb out of ",
                fmt.format(totalMb), " Mb (", fmt.format(freeMb), " Mb free)").eP();

        countOpenFiles(htm);

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Attempts to count the number of open files.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void countOpenFiles(final HtmlBuilder htm) {

        try {
            final File me = new File("/proc/self");
            if (me.exists()) {
                final int pid = Integer.parseInt(me.getCanonicalFile().getName());
                htm.sP().add("My process ID: ", Integer.toString(pid)).eP();

                final File fds = new File(me, "fd");
                if (fds.exists()) {
                    htm.sP().add("Open Files: ", Integer.toString(fds.listFiles().length)).eP();
                } else {
                    htm.sP().add("Unable to determine number of open files").eP();
                }
            } else {
                htm.sP().add("Unable to determine my process ID").eP();
            }
        } catch (final IOException ex) {
            Log.warning("Failed to execute 'ps' to find my process ID", ex);
        }
    }
}

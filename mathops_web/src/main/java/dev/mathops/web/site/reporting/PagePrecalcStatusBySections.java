package dev.mathops.web.site.reporting;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.dbjobs.report.HtmlCsvCourseProgressReport;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 * Generates the page to select and generate Precalculus Course progress reports by course and section.
 */
enum PagePrecalcStatusBySections {
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

        final String csv = req.getParameter("csv");

        if (csv == null || csv.isEmpty()) {
            emitHtmlPage(cache, site, req, resp, session);
        } else {
            emitCsvData(cache, req, resp);
        }
    }

    /**
     * Generates the HTML page with the report.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitHtmlPage(final Cache cache, final ReportingSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String title = Res.get(Res.SITE_TITLE);
        Page.startOrdinaryPage(htm, title, session, false, Page.NO_BARS, null, false, true);

        final String heading = Res.get(Res.HOME_HEADING);
        htm.sH(2).add(heading).eH(2);
        htm.sDiv().add("<a href='home.html'>Home</a>").eDiv();
        htm.hr();

        htm.sH(3).add("Precalculus Course progress for specified courses and sections").eH(3);

        htm.add("<form action='precalc_by_course.html' method='POST'>");

        final SystemData systemData = cache.getSystemData();
        final TermRec activeTerm = systemData.getActiveTerm();
        final TermKey activeKey = activeTerm == null ? null : activeTerm.term;

        final List<RawCsection> courseSections = systemData.getCourseSections(activeKey);
        courseSections.removeIf(row -> row.instrnType == null || "OT".equals(row.instrnType));
        courseSections.sort(null);

        if (courseSections.isEmpty()) {
            htm.sP().add("No courses or sections found in database for the active term").eP();
        } else {
            htm.sP().add("Select all course sections to include in report:").eP();
            final Collection<String> courseIds = new TreeSet<>();
            for (final RawCsection courseSect : courseSections) {
                courseIds.add(courseSect.course);
            }

            for (final String courseId : courseIds) {
                htm.sP("indent").add("<strong>", courseId, "</strong>: ");

                for (final RawCsection courseSect : courseSections) {
                    if (courseSect.course.equals(courseId)) {
                        final String id = courseId + "_" + courseSect.sect;
                        htm.add("<input type='checkbox' id='", id, "' name='", id, "' value='", courseSect.sect, "'/>");
                        htm.add("<label for='", id, "'> ", courseSect.sect, "</label>");
                    }
                }
                htm.eP();
            }
        }

        htm.sP().addln("<input type='submit' name='generate' value='View Report'/> ",
                "<input type='submit' name='csv' value='Download Report Data (CSV)'/>").eP();
        htm.addln("</form>");
        htm.hr();

        // Run the report if selections were provided
        final String generate = req.getParameter("generate");
        if (generate != null && !generate.isBlank()) {

            final List<RawCsection> included = new ArrayList<>(10);
            for (final RawCsection row : courseSections) {
                final String id = row.course + "_" + row.sect;
                final String value = req.getParameter(id);
                if (row.sect.equals(value)) {
                    included.add(row);
                }
            }

            if (!included.isEmpty()) {
                final Collection<String> report = new ArrayList<>(10);
                final Collection<String> csv = new ArrayList<>(10);
                final HtmlCsvCourseProgressReport job = new HtmlCsvCourseProgressReport(null, included,
                        "Status for a provided list of courses and sections");
                job.generate(report, csv);

                for (final String rep : report) {
                    htm.addln(rep);
                }
            }
        }

        Page.endOrdinaryPage(cache, site, htm, true);
        final String str = htm.toString();
        final byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
    }
    /**
     * Generates a CSV file with report data.
     *
     * @param req     the request
     * @param resp    the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitCsvData(final Cache cache, final ServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        final SystemData systemData = cache.getSystemData();
        final TermRec activeTerm = systemData.getActiveTerm();
        final TermKey activeKey = activeTerm == null ? null : activeTerm.term;

        final List<RawCsection> courseSections = systemData.getCourseSections(activeKey);
        courseSections.removeIf(row -> row.instrnType == null || "OT".equals(row.instrnType));
        courseSections.sort(null);

        final List<RawCsection> included = new ArrayList<>(10);
        for (final RawCsection row : courseSections) {
            final String id = row.course + "_" + row.sect;
            final String value = req.getParameter(id);
            if (row.sect.equals(value)) {
                included.add(row);
            }
        }

        // Run the report if student IDs were provided
        if (!included.isEmpty()) {

            final Collection<String> report = new ArrayList<>(10);
            final Collection<String> csv = new ArrayList<>(10);
                final HtmlCsvCourseProgressReport job = new HtmlCsvCourseProgressReport(null, included,
                        "Status for a provided list of courses and sections");
            job.generate(report, csv);

            final HtmlBuilder csvData = new HtmlBuilder(2000);
            for (final String row : csv) {
                csvData.addln(row);
            }

            resp.setHeader("Content-Disposition", "attachment;filename=placement_data.csv;");
            final String str = csvData.toString();
            final byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_CSV, bytes);
        }
    }
}

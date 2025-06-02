package dev.mathops.web.host.nibbler.reporting;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.dbjobs.report.ESortOrder;
import dev.mathops.dbjobs.report.HtmlCsvPlacementReport;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Generates the page to select and generate Math Placement reports by student category.  This also has a form to
 * request a special category with pre-loaded student IDs.
 */
enum PagePlacementByCategory {
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
            emitCsvData(cache, site, req, resp, session);
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

        final String category = req.getParameter("category");
        final String sortorder = req.getParameter("sortorder");

        if (AbstractSite.isParamInvalid(category)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  category='", category, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            String specialType = null;
            final List<RawSpecialStus> foundSpecials;
            if (category != null) {
                foundSpecials = RawSpecialStusLogic.queryByType(cache, category);
                if (!foundSpecials.isEmpty()) {
                    specialType = category;
                }
            }

            final Set<String> availableTypes = getAvailableTypes(cache);

            final HtmlBuilder htm = new HtmlBuilder(2000);
            Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

            htm.sH(2).add(Res.get(Res.HOME_HEADING)).eH(2);
            htm.sDiv().add("<a href='home.html'>Home</a>").eDiv();
            htm.hr();

            htm.sH(3).add("Math Placement progress by special category").eH(3);

            if (availableTypes.isEmpty()) {
                htm.sP().add("There are currently no special student categories defined.").eP();
            } else {
                htm.add("<form action='placement_by_category.html' method='POST'>");

                htm.addln("<label for='sortorder'>Sort Order:</label>");
                htm.addln("<select name='sortorder' id='sortorder'>");
                htm.add("  <option value='lastname'");
                if ("lastname".equals(sortorder)) {
                    htm.add(" selected");
                }
                htm.addln(">Alphabetical, by last name</option>");

                htm.add("  <option value='csiud'");
                if ("csiud".equals(sortorder)) {
                    htm.add(" selected");
                }
                htm.addln(">By CSU ID number</option>");
                htm.addln("</select>");

                htm.sP().add("Select the student category: ");
                htm.addln("<select name='category' id='category'>");
                for (final String type : availableTypes) {
                    if (type.equals(specialType)) {
                        htm.add(" <option selected='selected'>");
                    } else {
                        htm.add(" <option>");
                    }
                    htm.addln(type, "</option>");
                }
                htm.addln("</select> <input type='submit' name='generate' value='View Report'/> ",
                        "<input type='submit' name='csv' value='Download Report Data (CSV)'/>").eP();
                htm.addln("</form>");
            }

            htm.sP().add("To request a new student category, or to request permission to access other existing ",
                    "categories, please send an email to ",
                    "<a href='mailto:precalc_math@colostate.edu'>precalc_math@colostate.edu</a>",
                    " with a description of the category and a list of CSU IDs of students that should be ",
                    "added to the category.").eP();

            htm.sP().add("To update the list of students who are considered part of a category, please send an ",
                    "email to <a href='mailto:precalc_math@colostate.edu'>precalc_math@colostate.edu</a> " +
                    "with the category label and the updated list of CSU IDs.").eP();
            htm.hr();

            if (specialType != null) {
                final ESortOrder sort = ESortOrder.forId(sortorder);
                final ESortOrder actualSort = sort == null ? ESortOrder.LAST_NAME : sort;
                htm.sP().addln("Math Placement Status for students in [", specialType, "] category");

                final Collection<String> report = new ArrayList<>(10);
                final Collection<String> csv = new ArrayList<>(10);
                final HtmlCsvPlacementReport job = new HtmlCsvPlacementReport(specialType, actualSort);
                job.generate(report, csv);

                for (final String rep : report) {
                    htm.addln(rep);
                }
            }

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Generates a CSV file with report data.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitCsvData(final Cache cache, final ReportingSite site, final ServletRequest req,
                                    final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String category = req.getParameter("category");
        if (AbstractSite.isParamInvalid(category)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  category='", category, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            String specialType = null;
            final List<RawSpecialStus> foundSpecials;
            if (category != null) {
                foundSpecials = RawSpecialStusLogic.queryByType(cache, category);
                if (!foundSpecials.isEmpty()) {
                    specialType = category;
                }
            }

            if (specialType == null) {
                emitHtmlPage(cache, site, req, resp, session);
            } else {
                final String sortorder = req.getParameter("sortorder");
                final ESortOrder sort = ESortOrder.forId(sortorder);
                final ESortOrder actualSort = sort == null ? ESortOrder.LAST_NAME : sort;

                final Collection<String> report = new ArrayList<>(10);
                final Collection<String> csv = new ArrayList<>(10);
                final HtmlCsvPlacementReport job2 = new HtmlCsvPlacementReport(specialType, actualSort);
                job2.generate(report, csv);

                final HtmlBuilder csvData = new HtmlBuilder(2000);
                for (final String row : csv) {
                    csvData.addln(row);
                }

                resp.setHeader("Content-Disposition", "attachment;filename=placement_data.csv;");
                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_CSV, csvData);
            }
        }
    }

    /**
     * Gathers the list of available student categories for which the report can be run.
     *
     * @param cache the cache
     * @return the set of available types (never {@code null}, but could be empty)
     * @throws SQLException if there is an error accessing the database
     */
    private static Set<String> getAvailableTypes(final Cache cache) throws SQLException {

        final List<RawSpecialStus> allSpecials = RawSpecialStusLogic.queryAll(cache);
        final Set<String> availableTypes = new TreeSet<>();

        for (final RawSpecialStus rec : allSpecials) {
            final String test = rec.stuType;
            if ("ADMIN".equals(test) || "STEVE".equals(test) || "TUTOR".equals(test) || "RAMWORK".equals(test)
                || "PROCTOR".equals(test) || "MPT3".equals(test) || "PCT117".equals(test)
                || "PCT118".equals(test) || "PCT124".equals(test) || "PCT125".equals(test)
                || "PCT126".equals(test)) {
                continue;
            }
            availableTypes.add(test);
        }

        return availableTypes;
    }
}

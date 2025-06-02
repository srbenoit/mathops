package dev.mathops.web.host.testing.adminsys.genadmin.dbadmin;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.dbjobs.batch.daily.ImportBannerStudentRegistrations;
import dev.mathops.dbjobs.batch.daily.ImportOdsApplicants;
import dev.mathops.dbjobs.batch.daily.ImportOdsNewStus;
import dev.mathops.dbjobs.batch.daily.ImportOdsPastCourses;
import dev.mathops.dbjobs.batch.daily.ImportOdsTransferCredit;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdmSubtopic;
import dev.mathops.web.host.testing.adminsys.genadmin.GenAdminPage;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A page that executes a named batch and presents the results.
 */
public enum PageDbAdminBatchRun {
    ;

    /**
     * Executes the batch and presents the results.
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

        final String id = req.getParameter("id");

        if (AbstractSite.isParamInvalid(id)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  id='", id, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);
            htm.sH(2, "gray").add("Database Administration").eH(2);
            htm.hr("orange");

            PageDbAdmin.emitNavMenu(htm, EAdmSubtopic.DB_BATCH);
            doPageContent(htm, id);

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Appends page content to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to write
     * @param id  the batch job ID
     */
    private static void doPageContent(final HtmlBuilder htm, final String id) {

        htm.div("vgap0").hr().div("vgap0");

        boolean valid = true;

        switch (id) {
            case "import_ods_applicants" -> htm.sH(2).add("Import ODS Applicants").eH(2);
            case "import_ods_transfer" -> htm.sH(2).add("Import ODS Transfer Credit").eH(2);
            case "import_ods_past" -> htm.sH(2).add("Import ODS Past Courses").eH(2);
            case "import_banner_registrations" -> htm.sH(2).add("Import Banner Registrations").eH(2);
            case "import_ods_new_students" -> htm.sH(2).add("Import ODS New Students").eH(2);
            case null, default -> {
                htm.sH(2).add("Batch Jobs").eH(2);
                htm.sP().add("Unrecognized batch ID: ", id).eP();
                valid = false;
            }
        }

        if (valid) {
            htm.addln("<form action='dbadm_batch_run.html' method='POST'>");
            htm.addln("<input type='hidden' name='id' value='", id, "'/>");
            htm.addln("<input type='submit' value='Execute'/>");
            htm.addln("</form>");
        }
    }

    /**
     * Executes the batch and presents the results.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doPost(final Cache cache, final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String id = req.getParameter("id");

        if (AbstractSite.isParamInvalid(id)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  id='", id, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);
            htm.sH(2, "gray").add("Database Administration").eH(2);
            htm.hr("orange");

            PageDbAdmin.emitNavMenu(htm, EAdmSubtopic.DB_BATCH);
            htm.div("vgap0").hr().div("vgap0");

            switch (id) {
                case "import_ods_applicants" -> {
                    htm.sH(2).add("Import ODS Applicants").eH(2);
                    htm.add(new ImportOdsApplicants().execute());
                }
                case "import_ods_transfer" -> {
                    htm.sH(2).add("Import ODS Transfer Credit").eH(2);
                    htm.add(new ImportOdsTransferCredit().execute());
                }
                case "import_ods_past" -> {
                    htm.sH(2).add("Import ODS past Courses").eH(2);
                    htm.add(new ImportOdsPastCourses().execute());
                }
                case "import_banner_registrations" -> {
                    htm.sH(2).add("Import Banner Registrations").eH(2);
                    htm.add(new ImportBannerStudentRegistrations().execute());
                }
                case "import_ods_new_students" -> {
                    htm.sH(2).add("Import ODS New Students").eH(2);
                    htm.add(new ImportOdsNewStus().execute());
                }
                case null, default -> {
                    htm.sH(2).add("Batch Jobs").eH(2);
                    htm.sP().add("Unrecognized batch ID: ", id).eP();
                }
            }

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
        }
    }
}

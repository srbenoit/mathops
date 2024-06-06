package dev.mathops.web.site.admin.genadmin.dbadmin;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.dbjobs.batch.daily.ImportBannerStudentRegistrations;
import dev.mathops.dbjobs.batch.daily.ImportOdsApplicants;
import dev.mathops.dbjobs.batch.daily.ImportOdsNewStus;
import dev.mathops.dbjobs.batch.daily.ImportOdsPastCourses;
import dev.mathops.dbjobs.batch.daily.ImportOdsTransferCredit;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdmSubtopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * A page that executes a named batch and presents the results.
 */
public enum PageDbAdminBatchRun {
    ;

    /**
     * Executes the batch and presents the results.
     *
     * @param data    the web view data
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final WebViewData data, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String id = req.getParameter("id");

        if (AbstractSite.isParamInvalid(id)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  id='", id, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = GenAdminPage.startGenAdminPage(data, site, session, true);
            htm.sH(2, "gray").add("Database Administration").eH(2);
            htm.hr("orange");

            PageDbAdmin.emitNavMenu(htm, EAdmSubtopic.DB_BATCH);
            doPageContent(htm, id);

            final SystemData systemData = data.getSystemData();
            Page.endOrdinaryPage(systemData, site, htm, true);

            final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
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

        if ("import_ods_applicants".equals(id)) {
            htm.sH(2).add("Import ODS Applicants").eH(2);
        } else if ("import_ods_transfer".equals(id)) {
            htm.sH(2).add("Import ODS Transfer Credit").eH(2);
        } else if ("import_ods_past".equals(id)) {
            htm.sH(2).add("Import ODS Past Courses").eH(2);
        } else if ("import_banner_registrations".equals(id)) {
            htm.sH(2).add("Import Banner Registrations").eH(2);
        } else if ("import_ods_new_students".equals(id)) {
            htm.sH(2).add("Import ODS New Students").eH(2);
        } else {
            htm.sH(2).add("Batch Jobs").eH(2);
            htm.sP().add("Unrecognized batch ID: ", id).eP();
            valid = false;
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
     * @param data    the web view data
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doPost(final WebViewData data, final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String id = req.getParameter("id");

        if (AbstractSite.isParamInvalid(id)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  id='", id, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = GenAdminPage.startGenAdminPage(data, site, session, true);
            htm.sH(2, "gray").add("Database Administration").eH(2);
            htm.hr("orange");

            PageDbAdmin.emitNavMenu(htm, EAdmSubtopic.DB_BATCH);
            htm.div("vgap0").hr().div("vgap0");

            if ("import_ods_applicants".equals(id)) {
                htm.sH(2).add("Import ODS Applicants").eH(2);
                htm.add(new ImportOdsApplicants().execute());
            } else if ("import_ods_transfer".equals(id)) {
                htm.sH(2).add("Import ODS Transfer Credit").eH(2);
                htm.add(new ImportOdsTransferCredit().execute());
            } else if ("import_ods_past".equals(id)) {
                htm.sH(2).add("Import ODS past Courses").eH(2);
                htm.add(new ImportOdsPastCourses().execute());
            } else if ("import_banner_registrations".equals(id)) {
                htm.sH(2).add("Import Banner Registrations").eH(2);
                htm.add(new ImportBannerStudentRegistrations().execute());
            } else if ("import_ods_new_students".equals(id)) {
                htm.sH(2).add("Import ODS New Students").eH(2);
                htm.add(new ImportOdsNewStus().execute());
            } else {
                htm.sH(2).add("Batch Jobs").eH(2);
                htm.sP().add("Unrecognized batch ID: ", id).eP();
            }

            final SystemData systemData = data.getSystemData();
            Page.endOrdinaryPage(systemData, site, htm, true);

            final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
        }
    }
}

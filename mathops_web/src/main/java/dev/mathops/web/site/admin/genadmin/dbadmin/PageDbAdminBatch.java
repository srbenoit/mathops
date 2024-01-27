package dev.mathops.web.site.admin.genadmin.dbadmin;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
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
 * The "Batch" sub-page of the Database Administration page.
 */
public enum PageDbAdminBatch {
    ;

    /**
     * Generates the database administration "Batch Jobs" page.
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

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);
        htm.sH(2, "gray").add("Database Administration").eH(2);
        htm.hr("orange");

        PageDbAdmin.emitNavMenu(htm, EAdmSubtopic.DB_BATCH);
        doPageContent(htm);

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Appends page content to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to write
     */
    private static void doPageContent(final HtmlBuilder htm) {

        htm.div("vgap0").hr().div("vgap0");

        htm.sH(2).add("Batch Jobs").eH(2);

        htm.sP().add("<a href='dbadm_batch_run.html?id=import_ods_applicants'>",
                "Import ODS Applicants</a>").eP();

        htm.sP().add("<a href='dbadm_batch_run.html?id=import_ods_transfer'>",
                "Import ODS Transfer Credit</a>").eP();

        htm.sP().add("<a href='dbadm_batch_run.html?id=import_ods_past'>",
                "Import ODS Past Courses</a>").eP();

        htm.sP().add("<a href='dbadm_batch_run.html?id=import_banner_registrations'>",
                "Import Banner Registrations</a>").eP();

        htm.sP().add("<a href='dbadm_batch_run.html?id=import_ods_new_students'>",
                "Import ODS New Students</a>").eP();
    }
}

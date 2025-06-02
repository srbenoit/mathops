package dev.mathops.web.host.testing.adminsys.genadmin.dbadmin;

import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdmSubtopic;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdminTopic;
import dev.mathops.web.host.testing.adminsys.genadmin.GenAdminPage;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
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

        GenAdminPage.emitNavBlock(EAdminTopic.DB_ADMIN, htm);
        htm.sH(1).add("Database Administration").eH(1);

        PageDbAdmin.emitNavMenu(htm, EAdmSubtopic.DB_BATCH);
        doPageContent(htm);

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
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

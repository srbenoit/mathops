package dev.mathops.web.site.admin.genadmin.dbadmin;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.WebViewData;
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
 * The "Queries" sub-page of the Database Administration page.
 */
public enum PageDbAdminQueries {
    ;

    /**
     * Generates the database administration "Queries" page.
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

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(data, site, session, true);
        htm.sH(2, "gray").add("Database Administration").eH(2);
        htm.hr("orange");

        PageDbAdmin.emitNavMenu(htm, EAdmSubtopic.DB_QUERIES);
        doPageContent(htm);

        final SystemData systemData = data.getSystemData();
        Page.endOrdinaryPage(systemData, site, htm, true);

        final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
    }

    /**
     * Appends page content to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to write
     */
    private static void doPageContent(final HtmlBuilder htm) {

        htm.div("vgap0").hr().div("vgap0");

        htm.sH(2).add("Queries").eH(2);
    }
}

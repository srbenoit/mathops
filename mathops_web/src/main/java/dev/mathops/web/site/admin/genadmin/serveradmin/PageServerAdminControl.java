package dev.mathops.web.site.admin.genadmin.serveradmin;

import dev.mathops.assessment.InstructionalCache;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdmSubtopic;
import dev.mathops.web.site.admin.genadmin.EAdminTopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * The "Sessions" sub-page of the Server Administration page.
 */
public enum PageServerAdminControl {
    ;

    /**
     * Generates the server administration page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String action = req.getParameter("action");

        if (AbstractSite.isParamInvalid(action)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  id='", action, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

            GenAdminPage.emitNavBlock(EAdminTopic.SERVER_ADMIN, htm);

            PageServerAdmin.emitNavMenu(htm, EAdmSubtopic.SRV_CONTROL);

            if ("rescanitems".equals(action)) {
                InstructionalCache.getInstance().rescan();
                doPageContent(htm, action, "Item Cache rescan requested.");
            } else {
                doPageContent(htm, action, null);
            }

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Appends page content to an {@code HtmlBuilder}.
     *
     * @param htm     the {@code HtmlBuilder} to which to write
     * @param action  the action
     * @param message a message to display
     */
    private static void doPageContent(final HtmlBuilder htm, final String action,
                                      final String message) {

        htm.div("vgap0").hr().div("vgap0");

        htm.sH(2).add("Server Control").eH(2);

        htm.sP("indent");
        htm.addln(" This page provides controls that affect the server directly.");
        htm.eP();

        htm.addln("<hr style='margin-bottom:15px;'/>");

        htm.sDiv("indent");
        htm.addln("<dl>");
        htm.addln("<dt><a class='link' href='srvadm_control.html?action=rescanitems'>",
                "Rescan assessment item cache</a></dt>");
        htm.addln("<dd>Use this when an item or assessment XML file has been edited and the ",
                "changes should be reflected immediately.</dd>");
        if ("rescanitems".equals(action) && message != null) {
            htm.addln("<dd class='red'>", message, "</dd>");
        }
        htm.eDiv();
    }

    /**
     * Handles a POST request to the sessions page.
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

        doGet(cache, site, req, resp, session);
    }
}

package dev.mathops.web.site.admin.genadmin;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates a page that displays an error message.
 */
public enum PageError {
    ;

    /**
     * Generates the page.
     *
     * @param data    the web view data
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param message the error message
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final WebViewData data, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session, final String message)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(data, site, session, true);

        htm.sDiv("error").br();
        htm.addln("<strong>An error has occurred:</strong>").br().br();
        htm.addln(message);
        htm.eDiv();

        final SystemData systemData = data.getSystemData();
        Page.endOrdinaryPage(systemData, site, htm, true);

        final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, bytes);
    }
}

package dev.mathops.web.site;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.session.ImmutableSessionInfo;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates the content of the home page.
 */
enum PageError {
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
    static void doGet(final WebViewData data, final AbstractPageSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session, final String message)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String title = site.getTitle();
        Page.startOrdinaryPage(htm, title, session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR,
                null, false, true);

        htm.sDiv("error").br();
        htm.addln("<strong>An error has occurred:</strong>").br().br();
        htm.addln(message);
        htm.eDiv();

        Page.endOrdinaryPage(data, site, htm, true);

        final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, bytes);
    }
}

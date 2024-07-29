package dev.mathops.web.site.proctoring.student;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates a page with an error message.
 */
enum PageError {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @param message the error message
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final ProctoringSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session, final String message)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.ADMIN_BAR, null, false, true);

        htm.sH(1).add(Res.get(Res.HOME_HEADING)).eH(1);

        htm.sP("error").add(message).eP();

        htm.addln("<form action='home.html' method='GET'>");
        htm.addln("<input class='btn' type='submit' value='",
                "Return to main page'/>");
        htm.addln("</form>");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}

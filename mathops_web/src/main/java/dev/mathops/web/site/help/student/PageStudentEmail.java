package dev.mathops.web.site.help.student;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.help.HelpSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * The lobby, where students who have selected a context (course, unit, etc.) can wait for a tutor to accept their help
 * request and create a session, at which time they are redirected to the help page.
 *
 * <p>
 * This page gives students the option of posting a question to the forums, or to cancel their request.
 */
public enum PageStudentEmail {
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
    public static void doGet(final Cache cache, final HelpSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        // final HelpContext context = new HelpContext(req);

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS, null, false, true);

        htm.sH(1).add(Res.get(Res.LIVE_HELP_HEADING)).eH(1);
        htm.sH(2).add("E-mail a Course Assistant").eH(2);

        htm.div("vgap");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}

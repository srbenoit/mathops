package dev.mathops.web.site.help.student;

import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.help.HelpSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * A forum, where students who have selected a context (course, unit, etc.) can wait for a tutor to accept their help
 * request and create a session, at which time they are redirected to the help page.
 *
 * <p>
 * This page gives students the option of posting a question to the forums, or to cancel their request.
 */
public enum PageStudentForum {
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

        // emitForum(htm, site, context, req, resp, session);

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

//    /**
//     * Generates the page.
//     *
//     * @param htm the {@code HtmlBuilder} to which to append
//     * @param site the owning site
//     * @param context the help context
//     * @param req the request
//     * @param resp the response
//     * @param session the session
//     */
//     private static void emitForum(final HtmlBuilder htm, final HelpSite site,
//     final HelpContext context, final HttpServletRequest req, final HttpServletResponse resp,
//     final ImmutableSessionInfo session) {
//
//     htm.sH(2).add("Course Forum").eH(2);
//
//     htm.div("vgap");
//     }
}

package dev.mathops.web.site.admin.genadmin;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
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
 * The administrative system login page.
 */
enum PageHome {
    ;

    /**
     * Generates the page that prompts the user to log in.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, false);

        emitGenadminNavBlock(htm);
        GenAdminPage.emitNavBlock(null, htm);

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits the navigation block for a user with the SYSADMIN role.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitGenadminNavBlock(final HtmlBuilder htm) {

        htm.addln("<script>");
        htm.addln(" function pick(target) {");
        htm.addln("  window.location.assign(target);");
        htm.addln(" }");
        htm.addln("</script>");

        htm.addln("<nav>");

        navButton(htm, "first", "Office Staff View", "../office/home.html");

        htm.addln("</nav>");
        htm.hr("orange");
    }

    /**
     * Starts a navigation button.
     *
     * @param htm        the {@code HtmlBuilder} to which to append
     * @param id         the button ID ("first" or "last" to adjust margins)
     * @param buttonName the button name
     * @param url        the URL to which the button redirects
     */
    private static void navButton(final HtmlBuilder htm, final String id, final String buttonName, final String url) {

        htm.add("<button");
        htm.add(" class='nav4'");
        if (id != null) {
            htm.add(" id='", id, "'");
        }
        htm.add(" onclick='pick(\"", url, "\");'>", buttonName, "</button>");
    }
}

package dev.mathops.web.site.admin;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * The home page.
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
     * @param session the user session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), null, false, null, "home.html", Page.NO_BARS, null, false, true);
        AdminPage.emitPageHeader(htm, session, false);

        final ERole role = session.role;
        if (role == ERole.SYSADMIN) {
            emitSysadminNavBlock(htm);

            htm.div("vgap");

            Page.endOrdinaryPage(cache, site, htm, true);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        } else if (role == ERole.ADMINISTRATOR) {
            resp.sendRedirect("genadmin/home.html");
        } else if (role.canActAs(ERole.DIRECTOR)) {
            resp.sendRedirect("director/home.html");
        } else if (role.canActAs(ERole.OFFICE_STAFF)) {
            resp.sendRedirect("office/home.html");
        } else if (role.canActAs(ERole.RESOURCE_DESK)) {
            resp.sendRedirect("resource/home.html");
        } else if (role.canActAs(ERole.CHECKIN_CHECKOUT)) {
            resp.sendRedirect("testing/home.html");
        } else if (role.canActAs(ERole.PROCTOR)) {
            resp.sendRedirect("proctor/home.html");
        } else if (role.canActAs(ERole.BOOKSTORE)) {
            resp.sendRedirect("bookstore/home.html");
        } else {
            Log.warning("GET: invalid role: ", role.name());
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * Emits the navigation block for a user with the SYSADMIN role.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitSysadminNavBlock(final HtmlBuilder htm) {

        htm.addln("<script>");
        htm.addln(" function pick(target) {");
        htm.addln("  window.location.assign(target);");
        htm.addln(" }");
        htm.addln("</script>");

        htm.addln("<nav>");

        navButton(htm, "first", Res.get(Res.SYSADM_BTN_LBL), "sysadmin/home.html");
        navButton(htm, null, Res.get(Res.GENADM_BTN_LBL), "genadmin/home.html");
        navButton(htm, null, Res.get(Res.DIRECTOR_BTN_LBL), "director/home.html");
        navButton(htm, "last", Res.get(Res.OFFICE_BTN_LBL), "office/home.html");

        navButton(htm, "first", Res.get(Res.RESOURCE_BTN_LBL), "resource/home.html");
        navButton(htm, null, Res.get(Res.TESTING_BTN_LBL), "testing/home.html");
        navButton(htm, null, Res.get(Res.PROCTOR_BTN_LBL), "proctor/home.html");
        navButton(htm, "last", Res.get(Res.BOOKSTORE_BTN_LBL), "bookstore/home.html");

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
    private static void navButton(final HtmlBuilder htm, final String id, final String buttonName,
                                  final String url) {

        htm.add("<button");
        htm.add(" class='nav4'");
        if (id != null) {
            htm.add(" id='", id, "'");
        }
        htm.add(" onclick='pick(\"", url, "\");'>", buttonName, "</button>");
    }
}

package dev.mathops.web.host.precalc.canvas;

import dev.mathops.db.Cache;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * A page that displays a prompt to log in.  This page is called if the user either accesses "login.html", or accesses
 * any other page without a valid Shibboleth session.  If "login.html" is accessed, any existing session is cleared,
 * allowing another user to log in.
 */
enum PageLogin {
    ;

    /**
     * Generates the welcome page that users see when they access the site with either the '/' or '/index.html' paths
     * but have not logged in.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final CanvasSite site, final ServletRequest req,
                      final HttpServletResponse resp) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();
        Page.startOrdinaryPage(htm, siteTitle, null, true, Page.NO_BARS, null, false, true);

        final String selectedCourse = req.getParameter(CanvasSite.COURSE_PARAM);

        htm.sH(2).add(siteTitle).eH(2);

        htm.sP().add("TODO: Program image and introductory text.").eP();

        htm.sP("center");
        if (selectedCourse == null) {
            htm.add("<a href='secure/shibboleth.html' class='btn'/>");
        } else {
            final String encoded = URLEncoder.encode(selectedCourse, StandardCharsets.UTF_8);
            htm.add("<a href='secure/shibboleth.html?course=", encoded, "' class='btn'/>").eP();
        }
        htm.add("CSU NetID Login</a>").eP();

        htm.sP().add("TODO: Links to public information.").eP();

        htm.sP("center").add("<a href='https://canvas.colostate.edu/' class='smallbtn'/>Return to Canvas</a>").eP();

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }
}

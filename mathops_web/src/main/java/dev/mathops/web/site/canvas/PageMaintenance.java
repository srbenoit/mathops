package dev.mathops.web.site.canvas;

import dev.mathops.db.Cache;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A page that displays a maintenance message.
 */
enum PageMaintenance {
    ;

    /**
     * Generates the page with contact information.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param message the maintenance message
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final CanvasSite site, final ServletRequest req,
                      final HttpServletResponse resp, final String message) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();

        Page.startOrdinaryPage(htm, siteTitle, null, true, Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

        htm.div("vgap2");

        htm.sDiv("center");

        htm.sP().add("TODO: Program image, construction sign").eP();

        htm.sH(4, "center").add("SYSTEM UNDERGOING MAINTENANCE").eH(4);
        htm.sP().add("<strong>", message, "</strong>").eP();

        htm.sP().add("TODO: Links to public information, link back to Canvas.").eP();

        htm.eDiv(); // center
        htm.eDiv(); // vgap2

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }
}

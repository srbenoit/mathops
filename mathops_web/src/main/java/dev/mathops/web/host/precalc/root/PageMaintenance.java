package dev.mathops.web.host.precalc.root;

import dev.mathops.db.Cache;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
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
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @param message the maintenance message
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final PrecalcRootSite site, final ESiteType type,
                      final ServletRequest req, final HttpServletResponse resp, final String message)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), null, true, Page.NO_BARS, null, false, true);
        htm.sDiv("menupanel");
        PrecalcRootMenu.buildMenu(site, type, htm);
        htm.sDiv("panel");

        htm.div("vgap2");
        htm.sDiv("center");
        htm.sDiv("hours");
        htm.sH(5, "center red").add("SYSTEM UNDERGOING MAINTENANCE").eH(5);
        htm.sP().add("<strong>", message, "</strong>").eP();
        htm.eDiv();
        htm.eDiv();

        htm.div("vgap2");

        Page.emitFile(htm, "precalc_root_index_content.txt");

        htm.eDiv(); // panel
        htm.eDiv(); // menupanel

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }
}

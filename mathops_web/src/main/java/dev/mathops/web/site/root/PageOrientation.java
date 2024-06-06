package dev.mathops.web.site.root;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.Cache;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates a page with orientation videos.
 */
enum PageOrientation {
    ;

    /**
     * Generates a page with information on orientation.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param type  the site type
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final PrecalcRootSite site, final ESiteType type, final ServletRequest req,
                      final HttpServletResponse resp) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), null, false, null, null, Page.NO_BARS, null, false, true);

        htm.sDiv("menupanel");
        PrecalcRootMenu.buildMenu(site, type, htm);
        htm.sDiv("panel");

        Page.emitFile(htm, "precalc_root_orientation_content.txt");

        htm.eDiv(); // panel
        htm.eDiv(); // menupanel

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}

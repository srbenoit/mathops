package dev.mathops.web.site.root;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * A page that displays contact information.
 */
enum PageContact {
    ;

    /**
     * Generates the welcome page that users see when they access the site with either the '/' or '/index.html' paths.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param type  the site type
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final PrecalcRootSite site, final ESiteType type,
                      final ServletRequest req, final HttpServletResponse resp) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), null, true, Page.NO_BARS, null, false, true);
        htm.sDiv("menupanel");
        PrecalcRootMenu.buildMenu(site, type, htm);
        htm.sDiv("panel");

        Page.emitFile(htm, "precalc_root_contact_content.txt");

        htm.eDiv(); // panel
        htm.eDiv(); // menupanel

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}

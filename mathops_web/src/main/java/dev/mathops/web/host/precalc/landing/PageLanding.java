package dev.mathops.web.host.precalc.landing;

import dev.mathops.db.Cache;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rec.TermRec;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates a landing page that provides a link to login and some basic instructions.
 */
enum PageLanding {
    ;

    /**
     * Generates the page.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param type  the site type
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final LandingSite site, final ESiteType type,
                         final ServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, "Precalculus Center", null, false, Page.ADMIN_BAR, null, false, true);

        final TermRec active = cache.getSystemData().getActiveTerm();

        if (type == ESiteType.DEV) {
            if (active != null && active.term.name == ETermName.SUMMER) {
                Page.emitFile(htm, "landing_dev_summer.txt");
            } else {
                Page.emitFile(htm, "landing_dev.txt");
            }
        } else if (active != null && active.term.name == ETermName.SUMMER) {
            Page.emitFile(htm, "landing_prod_summer.txt");
        } else {
            Page.emitFile(htm, "landing_prod.txt");
        }

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}

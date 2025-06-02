package dev.mathops.web.host.nibbler.cfm;

import dev.mathops.db.Cache;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates a page with information about the Center.
 */
enum PageInformation {
    ;

    /**
     * Generates the page.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final CfmSite site, final ServletRequest req,
                         final HttpServletResponse resp) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, "The Center for Foundational Mathematics - Information", null, true, 0, null, false,
                false);

        PageUtilities.emitSubpageTitle(htm, "Information", "info.svg");

        htm.sDiv("inset");

        htm.sP().add("... TODO ...").eP();

        htm.eDiv(); // inset

        PageUtilities.emitNavigationBar(htm);

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}

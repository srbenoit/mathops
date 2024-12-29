package dev.mathops.web.site.cfm;

import dev.mathops.db.Cache;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates a page with a simulation for the Spur campus academic program.
 */
enum PageCfmIndex {
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
    static void showPage(final Cache cache, final CfmSite site, final ESiteType type, final ServletRequest req,
                         final HttpServletResponse resp) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, "The Center for Foundational Mathematics", null, true, 0, null, false, false);

        htm.sDiv("center");
        htm.sDiv(null, "style='display:inline-block; width:420px;'");

        htm.sP("center").add("<img src='/www/images/cfm/logo_transparent_256.png' alt=''/>").eP();

        htm.sH(1);
        htm.sSpan(null, "style='font-family:prox-regular;'");
        htm.sP("center", "style='font-size:31pt; margin:1px;'").addln("THE CENTER FOR").eP();
        htm.sP("center", "style='font-size:33pt; margin:1px;'").addln("FOUNDATIONAL").eP();
        htm.sP("center", "style='font-size:37pt; margin:1px;'").addln("MATHEMATICS").eP();
        htm.eSpan();
        htm.eH(1);

        htm.eDiv(); // fixed width
        htm.eDiv(); // centered

        PageUtilities.emitNavigationBar(htm);

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}

package dev.mathops.web.site.cfm;

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
 * Generates a page with analytics.
 */
enum PageAnalytics {
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
        Page.startOrdinaryPage(htm, "The Center for Foundational Mathematics - Analytics", null, true, 0, null, false,
                false);

        htm.sDiv("left")
                .add("<img style='padding-left:20px; padding-right:12px; height:66px;' ",
                        "src='/www/images/cfm/logo_transparent_256.png' alt=''/>")
                .eDiv();

        htm.sH(1);
        htm.sSpan(null, "style='font-family:prox-regular;'");
        htm.sP(null, "style='font-size:11pt; margin:1px;'").addln("THE CENTER FOR").eP();
        htm.sP(null, "style='font-size:12pt; margin:1px;'").addln("FOUNDATIONAL").eP();
        htm.sP(null, "style='font-size:13pt; margin:1px;'").addln("MATHEMATICS").eP();
        htm.eSpan();
        htm.eH(1);

        htm.div("clear");
        htm.div("vgap");

        htm.sDiv("inset");
        htm.sH(2).add("<img style='height:24px;' src='/www/images/cfm/analytics.svg'/> Analytics").eH(2);

        htm.eDiv(); // inset

        htm.addln("<table style='margin:30px auto; width:90%; max-width:800px; font-size:14pt;'><tr>");
        htm.addln("  <td style='text-align:center;'><a href='contact.html'>");
        htm.addln("    <img style='width:40px;' src='/www/images/cfm/contact.svg'/>").br().add("Contact Us");
        htm.addln("  </td>");
        htm.addln("  <td style='text-align:center;'><a href='people.html'>");
        htm.addln("    <img style='width:40px;' src='/www/images/cfm/people.svg'/>").br().add("People");
        htm.addln("  </td>");
        htm.addln("  <td style='text-align:center;'><a href='analytics.html'>");
        htm.addln("    <img style='width:40px;' src='/www/images/cfm/analytics.svg'/>").br().add("Analytics");
        htm.addln("  </td>");
        htm.addln("  <td style='text-align:center;'><a href='strategy.html'>");
        htm.addln("    <img style='width:40px;' src='/www/images/cfm/strategy.svg'/>").br().add("Strategy");
        htm.addln("  </td>");

        htm.addln("</tr></table>");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}

package dev.mathops.web.site.admin;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * The administrative system login page.
 */
enum PageLogin {
    ;

    /**
     * Generates the page that prompts the user to log in.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doLoginPage(final Cache cache, final AdminSite site, final ServletRequest req,
                            final HttpServletResponse resp) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();
        Page.startOrdinaryPage(htm, siteTitle, null, false, null, "home.html", Page.NO_BARS, null, false, true);

        final String headerTitle = Res.get(Res.SITE_TITLE);
        htm.sH(1).add(headerTitle).eH(1);
        htm.div("vgap");

        htm.sDiv("loginpane", "style='max-width:380pt;'");
        htm.sP("center", "style='margin:auto; max-width:320pt; padding:.6em 0;'");
        final String loginPrompt = Res.get(Res.LOGIN_PROMPT);
        htm.addln(loginPrompt);
        htm.eP();

        htm.sDiv("center");
        final String loginBtnLbl = Res.get(Res.LOGIN_BTN_LBL);
        htm.addln(" <a href='secure/shibboleth.html' class='btn'>", loginBtnLbl, "</a>");
        htm.eDiv();

        htm.eDiv();

        Page.endOrdinaryPage(cache, site, htm, true);

        final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
    }
}

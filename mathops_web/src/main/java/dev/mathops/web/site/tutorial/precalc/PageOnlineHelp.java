package dev.mathops.web.site.tutorial.precalc;

import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates a page with instructions for accessing online help.
 */
enum PageOnlineHelp {
    ;

    /**
     * Generates a page with information on orientation.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doOnlineHelpPage(final Cache cache, final PrecalcTutorialSite site,
                                 final ServletRequest req, final HttpServletResponse resp,
                                 final ImmutableSessionInfo session, final PrecalcTutorialSiteLogic logic)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Precalculus Tutorial",
                "/precalc-tutorial/home.html", Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

        htm.sDiv("menupanel");
        TutorialMenu.buildMenu(session, logic, htm);
        htm.sDiv("panel");

        Page.emitFile(htm, "pct_getting_help.txt");

        htm.eDiv(); // (end "panel" div)
        htm.eDiv(); // (end "menupanel" div)

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }
}

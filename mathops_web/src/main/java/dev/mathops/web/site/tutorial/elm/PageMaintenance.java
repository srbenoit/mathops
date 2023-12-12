package dev.mathops.web.site.tutorial.elm;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    static void doGet(final Cache cache, final ElmTutorialSite site, final ServletRequest req,
                      final HttpServletResponse resp, final String message) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), null, false, "Entry Level Mathematics Tutorial",
                "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

        htm.div("vgap2");
        htm.sDiv("indent11 errorbox");
        htm.addln("<h4 class='center red'>SYSTEM UNDERGOING MAINTENANCE</h4>");

        htm.sP().add("<strong>", message, "</strong>").eP();
        htm.eDiv();

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}

package dev.mathops.web.host.placement.tutorial.precalc;

import dev.mathops.db.Cache;
import dev.mathops.db.logic.placement.PlacementLogic;
import dev.mathops.db.logic.placement.PlacementStatus;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.host.placement.placement.PlacementReport;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates the content of the home page.
 */
enum PagePlacementReport {
    ;

    /**
     * Generates the home page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final PrecalcTutorialSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session,
                             final PrecalcTutorialSiteLogic logic) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Precalculus Tutorial",
                "/precalc-tutorial/home.html", Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

        htm.sDiv("menupanel");
        TutorialMenu.buildMenu(session, logic, htm);
        htm.sDiv("panel");

        final PlacementStatus pstatus = new PlacementLogic(cache, session.getEffectiveUserId(),
                logic.getStudent().aplnTerm, session.getNow()).status;

        PlacementReport.doPlacementReport(cache, pstatus, session, null, true, htm);

        htm.eDiv(); // (end "panel" div)
        htm.eDiv(); // (end "menupanel" div)

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }
}

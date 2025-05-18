package dev.mathops.web.site.scheduling;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * Generates the home page for the scheduling system.
 */
enum PageHome {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final SchedulingSite site, final ServletRequest req,
                         final HttpServletResponse resp, final ImmutableSessionInfo session) throws IOException,
            SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, "Center Scheduling System", null, false, Page.ADMIN_BAR, null, false, true);

        htm.sDiv(null, "style='padding-left:16px; padding-right:16px;'");

        htm.sH(1).add("Center Scheduling System").eH(1);

        final String effectiveId = session.getEffectiveUserId();
        final String effectiveName = session.getEffectiveScreenName();
        final ZonedDateTime sessionNow = session.getNow();
        final LocalDate today = sessionNow.toLocalDate();

        htm.sDiv().add("Logged in as ", effectiveName).eDiv().hr();

        final boolean isAdmin = RawSpecialStusLogic.isSpecialType(cache, effectiveId, today, "STEVE");
        final boolean isMgr = RawSpecialStusLogic.isSpecialType(cache, effectiveId, today, "MANAGER");
        final boolean isStaff = RawSpecialStusLogic.isSpecialType(cache, effectiveId, today, "STAFF");
        final boolean isEmployee = RawSpecialStusLogic.isSpecialType(cache, effectiveId, today, "EMPLOY");

        // Present an interface with all features for which the user is authorized.

        htm.eDiv();

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}

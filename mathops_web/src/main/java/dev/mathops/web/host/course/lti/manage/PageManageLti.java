package dev.mathops.web.host.course.lti.manage;

import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.host.course.lti.LtiSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A page with the "collaboration" placement.
 */
public enum PageManageLti {
    ;

    /**
     * Handles a GET request to the "Manage LTI Tool" page.
     *
     * @param req  the request
     * @param resp the response
     */
    public static void doGet(final Cache cache, final LtiSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), null, false, Page.ADMIN_BAR, null, false, true);

        htm.sDiv(null, "style='padding-left:16px; padding-right:16px;'");
        htm.sH(1).add(LtiSite.TOOL_NAME).eH(1);
        htm.sH(2).add("LTI Tool Management").eH(2);
        htm.hr().div("vgap");

        htm.sP().addln("<a href='manage_lti_regs.html'>Manage LTI Tool Registrations</a>");






        htm.eDiv();

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}

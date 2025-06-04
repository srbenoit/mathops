package dev.mathops.web.host.course.lti.manage;

import dev.mathops.db.Cache;
import dev.mathops.db.rec.main.LtiRegistrationRec;
import dev.mathops.db.reclogic.main.LtiRegistrationLogic;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.host.course.lti.LtiSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * A page to manage the set of LTI registrations.  This allows one to delete old registrations and to view the set of
 * all registrations.
 */
public enum PageManageLtiRegs {
    ;

    /**
     * Handles a GET request to the "Manage LTI Tool Registrations" page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @param error   an error message to show; null if none
     */
    public static void doGet(final Cache cache, final LtiSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session,
                             final String error)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String title = Res.get(Res.SITE_TITLE);
        Page.startOrdinaryPage(htm, title, null, false, Page.ADMIN_BAR, null, false, true);

        htm.sDiv(null, "style='padding-left:16px; padding-right:16px;'");
        htm.sH(1).add(LtiSite.TOOL_NAME).eH(1);
        htm.sH(2).add("<a class='ulink' href='manage_lti.html'>LTI Tool Management</a>").eH(2);
        htm.hr().div("vgap");
        htm.sH(3).add("LTI Tool Registrations").eH(3);

        try {
            final List<LtiRegistrationRec> regs = LtiRegistrationLogic.INSTANCE.queryAll(cache);
            regs.sort(null);

            htm.sP().addln("<table class='data'>");
            htm.addln("<tr><th>Issuer</th><th>Client ID</th><th>Actions</th></tr>");
            for (final LtiRegistrationRec reg : regs) {
                htm.add("<tr><td>", reg.issuer, "</td><td>", reg.clientId, "</td><td>");
                htm.add("<form style='display:inline-block' action='manage_lti_regs.html' method='GET'>");
                htm.add("<input type='hidden' name='act' value='view'/>");
                htm.add("<input type='hidden' name='iss' value='", reg.issuer, "'/>");
                htm.add("<input type='hidden' name='cid' value='", reg.clientId, "'/>");
                htm.add("<input type='submit' value='View'/>");
                htm.add("</form> &nbsp;");
                htm.add("<form style='display:inline-block' action='manage_lti_regs.html' method='POST'>");
                htm.add("<input type='hidden' name='act' value='del'/>");
                htm.add("<input type='hidden' name='iss' value='", reg.issuer, "'/>");
                htm.add("<input type='hidden' name='cid' value='", reg.clientId, "'/>");
                htm.add("<input type='submit' value='Delete'/>");
                htm.add("</form>");
                htm.addln("</td></tr>");
            }
            htm.addln("</table>").eP();

            if (error != null) {
                htm.div("vgap");
                htm.sP("error").addln(error).eP();
            }

            final String act = req.getParameter("act");
            final String iss = req.getParameter("iss");
            final String cid = req.getParameter("cid");

            if ("view".equals(act)) {
                LtiRegistrationRec found = null;
                for (final LtiRegistrationRec reg : regs) {
                    if (reg.issuer.equals(iss) && reg.clientId.equals(cid)) {
                        found = reg;
                        break;
                    }
                }

                if (found != null) {
                    htm.div("vgap");
                    htm.sP().addln("<table class='data'>");
                    htm.addln("<tr><th class='row'>Issuer</th><td>", found.issuer, "</td></tr>");
                    htm.addln("<tr><th class='row'>Issuer Port</th><td>", found.issuerPort, "</td></tr>");
                    htm.addln("<tr><th class='row'>Client ID</th><td>", found.clientId, "</td></tr>");
                    htm.addln("<tr><th class='row'>Redirect URI</th><td>", found.redirectUri, "</td></tr>");
                    htm.addln("<tr><th class='row'>Authorization Endpoint</th><td>", found.authEndpoint, "</td></tr>");
                    htm.addln("<tr><th class='row'>Token Endpoint</th><td>", found.tokenEndpoint, "</td></tr>");
                    htm.addln("<tr><th class='row'>Registration Endpoint</th><td>", found.regEndpoint, "</td></tr>");
                    htm.addln("<tr><th class='row'>JWKS URI</th><td>", found.jwksUri, "</td></tr>");
                    htm.addln("</table>").eP();
                }
            }

        } catch (final SQLException ex) {
            htm.sP().addln("ERROR: ", ex.getMessage()).eP();
        }
        htm.eDiv();

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Handles a POST request to the "Manage LTI Tool" page.
     *
     * @param req  the request
     * @param resp the response
     */
    public static void doPost(final Cache cache, final LtiSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String act = req.getParameter("act");

        if ("del".equals(act)) {
            final String iss = req.getParameter("iss");
            final String cid = req.getParameter("cid");
            final String confirm = req.getParameter("confirm");

            if (iss == null || cid == null) {
                doGet(cache, site, req, resp, session, "Delete action invoked without required issuer and client ID.");
            } else {
                final LtiRegistrationRec reg = LtiRegistrationLogic.INSTANCE.query(cache, cid, iss);
                if (reg == null) {
                    doGet(cache, site, req, resp, session, "Unable to find registration record to delete");
                } else if ("yes".equals(confirm)) {
                    LtiRegistrationLogic.INSTANCE.delete(cache, reg);
                    doGet(cache, site, req, resp, session, null);
                } else {
                    final HtmlBuilder htm = new HtmlBuilder(2000);
                    final String title = Res.get(Res.SITE_TITLE);
                    Page.startOrdinaryPage(htm, title, null, false, Page.ADMIN_BAR, null, false, true);

                    htm.sDiv(null, "style='padding-left:16px; padding-right:16px;'");
                    htm.sH(1).add(LtiSite.TOOL_NAME).eH(1);
                    htm.sH(2).add("<a class='ulink' href='manage_lti.html'>LTI Tool Management</a>").eH(2);
                    htm.hr().div("vgap");
                    htm.sH(3).add("LTI Tool Registrations").eH(3);

                    htm.hr().div("vgap");
                    htm.sP("error").add("DELETE the following registration?").eP();

                    htm.sP().addln("<table class='data'>");
                    htm.addln("<tr><th class='row'>Issuer</th><td>", reg.issuer, "</td></tr>");
                    htm.addln("<tr><th class='row'>Issuer Port</th><td>", reg.issuerPort, "</td></tr>");
                    htm.addln("<tr><th class='row'>Client ID</th><td>", reg.clientId, "</td></tr>");
                    htm.addln("<tr><th class='row'>Redirect URI</th><td>", reg.redirectUri, "</td></tr>");
                    htm.addln("<tr><th class='row'>Authorization Endpoint</th><td>", reg.authEndpoint, "</td></tr>");
                    htm.addln("<tr><th class='row'>Token Endpoint</th><td>", reg.tokenEndpoint, "</td></tr>");
                    htm.addln("<tr><th class='row'>Registration Endpoint</th><td>", reg.regEndpoint, "</td></tr>");
                    htm.addln("<tr><th class='row'>JWKS URI</th><td>", reg.jwksUri, "</td></tr>");
                    htm.addln("</table>").eP();
                    htm.div("vgap");

                    htm.sDiv("indent");
                    htm.add("<form style='display:inline-block' action='manage_lti_regs.html' method='POST'>");
                    htm.add("<input type='hidden' name='act' value='del'/>");
                    htm.add("<input type='hidden' name='iss' value='", reg.issuer, "'/>");
                    htm.add("<input type='hidden' name='cid' value='", reg.clientId, "'/>");
                    htm.add("<input type='hidden' name='confirm' value='yes'/>");
                    htm.add("<input type='submit' value='Yes, delete this record'/>");
                    htm.add("</form> &nbsp;");
                    htm.add("<form style='display:inline-block' action='manage_lti_regs.html' method='GET'>");
                    htm.add("<input type='submit' value='Cancel'/>");
                    htm.add("</form>");
                    htm.eDiv();

                    htm.eDiv();

                    Page.endOrdinaryPage(cache, site, htm, true);
                    AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);

                }
            }
        } else {
            doGet(cache, site, req, resp, session, "POST request with invalid action.");
        }
    }
}

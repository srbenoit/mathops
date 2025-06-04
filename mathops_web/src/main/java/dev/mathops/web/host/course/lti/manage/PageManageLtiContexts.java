package dev.mathops.web.host.course.lti.manage;

import dev.mathops.db.Cache;
import dev.mathops.db.rec.main.LtiRegistrationRec;
import dev.mathops.db.rec.term.LtiContextCourseSectionRec;
import dev.mathops.db.rec.term.LtiContextRec;
import dev.mathops.db.reclogic.main.LtiRegistrationLogic;
import dev.mathops.db.reclogic.term.LtiContextCourseSectionLogic;
import dev.mathops.db.reclogic.term.LtiContextLogic;
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
 * A page to manage the set of LTI contexts.  This allows one to delete old registrations and to view the set of all
 * context and their associated course sections.
 */
public enum PageManageLtiContexts {
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
        Page.startOrdinaryPage(htm, LtiSite.TOOL_NAME, null, false, Page.ADMIN_BAR, null, false, true);

        htm.sDiv(null, "style='padding-left:16px; padding-right:16px;'");
        htm.sH(1).add(LtiSite.TOOL_NAME).eH(1);
        htm.sH(2).add("<a class='ulink' href='manage_lti.html'>LTI Tool Management</a>").eH(2);
        htm.hr().div("vgap");
        htm.sH(3).add("LTI Tool Contexts").eH(3);

        try {
            final List<LtiContextRec> contexts = LtiContextLogic.INSTANCE.queryAll(cache);
            final List<LtiContextCourseSectionRec> sections = LtiContextCourseSectionLogic.INSTANCE.queryAll(cache);
            contexts.sort(null);

            htm.sP().addln("<table class='data'>");
            htm.addln("<tr><th>Issuer</th><th>Client ID</th><th>Deployment ID</th><th>Context ID</th>",
                    "<th>Course Sections</th><th>Actions</th></tr>");
            for (final LtiContextRec ctx : contexts) {
                htm.add("<tr><td>", ctx.issuer, "</td><td>", ctx.clientId, "</td><td>", ctx.deploymentId, "</td><td>",
                        ctx.contextId, "</td><td>");
                boolean brk = false;
                for (final LtiContextCourseSectionRec sect : sections) {
                    if (sect.issuer.equals(ctx.issuer)
                        && sect.clientId.equals(ctx.clientId)
                        && sect.deploymentId.equals(ctx.deploymentId)
                        && sect.contextId.equals(ctx.contextId)) {
                        if (brk) {
                            htm.br();
                        }
                        htm.add(sect.courseId, "/", sect.sectionNbr);
                        brk = true;
                    }
                }
                htm.add("</td><td>");

                htm.add("<form style='display:inline-block' action='manage_lti_regs.html' method='GET'>");
                htm.add("<input type='hidden' name='act' value='view'/>");
                htm.add("<input type='hidden' name='iss' value='", ctx.issuer, "'/>");
                htm.add("<input type='hidden' name='cid' value='", ctx.clientId, "'/>");
                htm.add("<input type='hidden' name='did' value='", ctx.deploymentId, "'/>");
                htm.add("<input type='hidden' name='ctx' value='", ctx.contextId, "'/>");
                htm.add("<input type='submit' value='View'/>");
                htm.add("</form> &nbsp;");
                htm.add("<form style='display:inline-block' action='manage_lti_regs.html' method='POST'>");
                htm.add("<input type='hidden' name='act' value='del'/>");
                htm.add("<input type='hidden' name='iss' value='", ctx.issuer, "'/>");
                htm.add("<input type='hidden' name='cid' value='", ctx.clientId, "'/>");
                htm.add("<input type='hidden' name='did' value='", ctx.deploymentId, "'/>");
                htm.add("<input type='hidden' name='ctx' value='", ctx.contextId, "'/>");
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
            final String did = req.getParameter("did");
            final String ctx = req.getParameter("ctx");

            if ("view".equals(act)) {
                LtiContextRec found = null;
                for (final LtiContextRec reg : contexts) {
                    if (reg.issuer.equals(iss) && reg.clientId.equals(cid)) {
                        found = reg;
                        break;
                    }
                }

                if (found != null) {
                    htm.div("vgap");
                    htm.sP().addln("<table class='data'>");
                    htm.addln("<tr><th class='row'>Issuer</th><td>", found.issuer, "</td></tr>");
                    htm.addln("<tr><th class='row'>Client ID</th><td>", found.clientId, "</td></tr>");
                    htm.addln("<tr><th class='row'>Deployment ID</th><td>", found.deploymentId, "</td></tr>");
                    htm.addln("<tr><th class='row'>Context ID</th><td>", found.contextId, "</td></tr>");
                    htm.addln("<tr><th class='row'>LMS Course ID</th><td>", found.lmsCourseId, "</td></tr>");
                    htm.addln("<tr><th class='row'>LMS Course Title</th><td>", found.lmsCourseTitle, "</td></tr>");
                    for (final LtiContextCourseSectionRec sect : sections) {
                        if (sect.issuer.equals(found.issuer)
                            && sect.clientId.equals(found.clientId)
                            && sect.deploymentId.equals(found.deploymentId)
                            && sect.contextId.equals(found.contextId)) {
                            htm.addln("<tr><th class='row'>Linked To </th><td>", sect.courseId, "/", sect.sectionNbr,
                                    "</td></tr>");
                        }
                    }
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
            final String did = req.getParameter("did");
            final String ctx = req.getParameter("ctx");
            final String confirm = req.getParameter("confirm");

            if (iss == null || cid == null || did == null || ctx == null) {
                doGet(cache, site, req, resp, session,
                        "Delete action invoked without required issuer, client ID, deployment ID, and context ID.");
            } else {
                final LtiContextRec context = LtiContextLogic.INSTANCE.query(cache, cid, iss, did, ctx);
                if (context == null) {
                    doGet(cache, site, req, resp, session, "Unable to find context record to delete");
                } else {
                    final List<LtiContextCourseSectionRec> sections = LtiContextCourseSectionLogic.INSTANCE.queryAll(
                            cache);
                    if ("yes".equals(confirm)) {
                        for (final LtiContextCourseSectionRec sect : sections) {
                            if (sect.issuer.equals(context.issuer)
                                && sect.clientId.equals(context.clientId)
                                && sect.deploymentId.equals(context.deploymentId)
                                && sect.contextId.equals(context.contextId)) {
                                LtiContextCourseSectionLogic.INSTANCE.delete(cache, sect);
                            }
                        }
                        LtiContextLogic.INSTANCE.delete(cache, context);
                        doGet(cache, site, req, resp, session, null);
                    } else {
                        final HtmlBuilder htm = new HtmlBuilder(2000);
                        Page.startOrdinaryPage(htm, LtiSite.TOOL_NAME, null, false, Page.ADMIN_BAR, null, false, true);

                        htm.sDiv(null, "style='padding-left:16px; padding-right:16px;'");
                        htm.sH(1).add(LtiSite.TOOL_NAME).eH(1);
                        htm.sH(2).add("<a class='ulink' href='manage_lti.html'>LTI Tool Management</a>").eH(2);
                        htm.hr().div("vgap");
                        htm.sH(3).add("LTI Tool Contexts").eH(3);

                        htm.hr().div("vgap");
                        htm.sP("error").add(
                                "DELETE the following context (and all associated course section links)?").eP();

                        htm.sP().addln("<table class='data'>");
                        htm.addln("<tr><th class='row'>Issuer</th><td>", context.issuer, "</td></tr>");
                        htm.addln("<tr><th class='row'>Client ID</th><td>", context.clientId, "</td></tr>");
                        htm.addln("<tr><th class='row'>Deployment ID</th><td>", context.deploymentId, "</td></tr>");
                        htm.addln("<tr><th class='row'>Context ID</th><td>", context.contextId, "</td></tr>");
                        htm.addln("<tr><th class='row'>LMS Course ID</th><td>", context.lmsCourseId, "</td></tr>");
                        htm.addln("<tr><th class='row'>LMS Course Title</th><td>", context.lmsCourseTitle,
                                "</td></tr>");
                        for (final LtiContextCourseSectionRec sect : sections) {
                            if (sect.issuer.equals(context.issuer)
                                && sect.clientId.equals(context.clientId)
                                && sect.deploymentId.equals(context.deploymentId)
                                && sect.contextId.equals(context.contextId)) {
                                htm.addln("<tr><th class='row'>Linked To </th><td>", sect.courseId, "/",
                                        sect.sectionNbr, "</td></tr>");
                            }
                        }
                        htm.addln("</table>").eP();
                        htm.div("vgap");

                        htm.sDiv("indent");
                        htm.add("<form style='display:inline-block' action='manage_lti_regs.html' method='POST'>");
                        htm.add("<input type='hidden' name='act' value='del'/>");
                        htm.add("<input type='hidden' name='iss' value='", context.issuer, "'/>");
                        htm.add("<input type='hidden' name='cid' value='", context.clientId, "'/>");
                        htm.add("<input type='hidden' name='did' value='", context.deploymentId, "'/>");
                        htm.add("<input type='hidden' name='ctx' value='", context.contextId, "'/>");
                        htm.add("<input type='hidden' name='confirm' value='yes'/>");
                        htm.add("<input type='submit' value='Yes, delete this context'/>");
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
            }
        } else {
            doGet(cache, site, req, resp, session, "POST request with invalid action.");
        }
    }
}

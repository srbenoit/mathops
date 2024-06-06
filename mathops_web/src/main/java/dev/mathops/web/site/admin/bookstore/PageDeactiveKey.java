package dev.mathops.web.site.admin.bookstore;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.db.old.rawlogic.RawEtextKeyLogic;
import dev.mathops.db.old.rawlogic.RawStetextLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawEtextKey;
import dev.mathops.db.old.rawrecord.RawStetext;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * The bookstore site home page.
 */
enum PageDeactiveKey {
    ;

    /**
     * Generates the page to confirm deletion of an active key.
     *
     * @param data    the web view data
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void deactivateKey(final WebViewData data, final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String key = req.getParameter("key");

        if (AbstractSite.isParamInvalid(key)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  key='", key, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = BookstorePage.startBookstorePage(data, site, session);

            if (key == null) {
                BookstorePage.emitKeyForm(htm, null, null);
            } else {
                final Cache cache = data.getCache();
                final RawEtextKey keyModel = RawEtextKeyLogic.query(cache, key);

                if (keyModel == null) {
                    final String msg = Res.get(Res.KEY_NOT_FOUND);
                    BookstorePage.emitKeyForm(htm, key, msg);
                } else {
                    BookstorePage.emitKeyForm(htm, key, null);

                    htm.div("gap2");
                    htm.sDiv("center");
                    htm.addln("<strong>");

                    if (keyModel.activeDt == null) {
                        final String msg = Res.get(Res.KEY_NOT_ACTIVE);
                        htm.addln(msg);
                    } else {
                        final RawStetext stetext = RawStetextLogic.getOwnerOfKey(cache, key);

                        final String activeDtStr = TemporalUtils.FMT_WMDY_AT_HM_A.format(keyModel.activeDt);

                        if (stetext == null) {
                            final String msg = Res.fmt(Res.KEY_ACTIVE_NO_USER, activeDtStr);
                            htm.addln(msg);
                        } else {
                            final RawStudent stu = RawStudentLogic.query(cache, stetext.stuId, true);

                            if (stu == null) {
                                final String msg = Res.fmt(Res.KEY_ACTIVE_NO_STU, activeDtStr, stetext.stuId);
                                htm.addln(msg);
                            } else {
                                final String msg = Res.fmt(Res.KEY_ACTIVE_STU, activeDtStr, stu.firstName, stu.lastName,
                                        stetext.stuId);
                                htm.addln(msg);
                            }

                            emitDeactivateConfirmForm(htm, key);
                        }
                    }

                    htm.addln("</strong>");
                    htm.eDiv();
                }
            }

            Page.endOrdinaryPage(data, site, htm, true);

            final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
        }
    }

    /**
     * Emits the form used to deactivate an e-text key.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     * @param key the initial value to load in for the key
     */
    private static void emitDeactivateConfirmForm(final HtmlBuilder htm, final String key) {

        htm.sDiv("indent22");
        htm.hr();
        htm.div("gap2");
        htm.addln("<form action='deactivate_etext_key_yes.html' method='post'>");
        htm.sDiv("center");
        final String msg = Res.get(Res.DEACTIVATE_CONFIRM);
        htm.addln(" <strong class='red'>", msg, "<strong>");
        htm.div("gap2");
        htm.addln(" <input type='hidden' name='key' value='", key, "'/>");
        final String lbl = Res.get(Res.DEACTIVATE_BTN_LBL);
        htm.addln(" <input type='submit' value='", lbl, "'/>");
        htm.eDiv();
        htm.addln("</form>");
        htm.div("gap2").hr().eDiv();
    }

    /**
     * Generates the page to confirm deletion of an active key.
     *
     * @param data    the web view data
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void deactivateKeyYes(final WebViewData data, final AdminSite site, final ServletRequest req,
                                 final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String key = req.getParameter("key");

        if (AbstractSite.isParamInvalid(key)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  key='", key, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(400);
            final String title = Res.get(Res.SITE_TITLE);

            Page.startOrdinaryPage(htm, title, session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR,
                    null, false, true);

            htm.div("gap");
            htm.sDiv("indent11");

            htm.sP("center");
            final String dept = Res.get(Res.DEPARTMENT_TITLE);
            htm.addln(" <strong>", dept, "</strong>").br();
            htm.addln(" <strong><span class='green'>", title, "</span></strong>");
            htm.eP();

            htm.sP().add("&nbsp;").eP();
            htm.eDiv();

            if (key == null) {
                BookstorePage.emitKeyForm(htm, null, null);
            } else {
                final Cache cache = data.getCache();
                final RawEtextKey keyModel = RawEtextKeyLogic.query(cache, key);

                if (keyModel == null) {
                    final String msg = Res.get(Res.KEY_NOT_FOUND);
                    BookstorePage.emitKeyForm(htm, key, msg);
                } else {
                    BookstorePage.emitKeyForm(htm, key, null);

                    htm.div("gap2");

                    htm.sDiv("center");
                    htm.addln("<strong><span class='green'>");

                    if (keyModel.activeDt == null) {
                        final String msg = Res.get(Res.KEY_NOT_ACTIVE);
                        htm.addln(msg);
                    } else {
                        final RawStetext stetext = RawStetextLogic.getOwnerOfKey(cache, key);

                        if (RawEtextKeyLogic.updateActiveDt(cache, keyModel.etextKey, null)) {

                            if (RawStetextLogic.INSTANCE.delete(cache, stetext)) {
                                final String msg = Res.get(Res.KEY_DEACTIVATED);
                                htm.addln(msg);
                            }
                        } else {
                            final String msg = Res.get(Res.DEACTIVATION_ERROR);
                            htm.addln("<span class='red'>", msg, "</span>");
                        }
                    }

                    htm.addln("</span></strong>");
                    htm.eDiv();
                }
            }

            Page.endOrdinaryPage(data, site, htm, true);

            final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
        }
    }
}

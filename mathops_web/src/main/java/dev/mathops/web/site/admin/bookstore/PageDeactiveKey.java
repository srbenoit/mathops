package dev.mathops.web.site.admin.bookstore;

import dev.mathops.core.TemporalUtils;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rawlogic.RawEtextKeyLogic;
import dev.mathops.db.rawlogic.RawStetextLogic;
import dev.mathops.db.rawlogic.RawStudentLogic;
import dev.mathops.db.rawrecord.RawEtextKey;
import dev.mathops.db.rawrecord.RawStetext;
import dev.mathops.db.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
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
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void deactivateKey(final Cache cache, final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String key = req.getParameter("key");

        if (AbstractSite.isParamInvalid(key)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  key='", key, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = BookstorePage.startBookstorePage(site, session);

            if (key == null) {
                BookstorePage.emitKeyForm(htm, null, null);
            } else {
                final RawEtextKey keyModel = RawEtextKeyLogic.query(cache, key);

                if (keyModel == null) {
                    BookstorePage.emitKeyForm(htm, key, Res.get(Res.KEY_NOT_FOUND));
                } else {
                    BookstorePage.emitKeyForm(htm, key, null);

                    htm.div("gap2");
                    htm.sDiv("center");
                    htm.addln("<strong>");

                    if (keyModel.activeDt == null) {
                        htm.addln(Res.get(Res.KEY_NOT_ACTIVE));
                    } else {
                        final RawStetext stetext = RawStetextLogic.getOwnerOfKey(cache, key);

                        if (stetext == null) {
                            htm.addln(Res.fmt(Res.KEY_ACTIVE_NO_USER,
                                    TemporalUtils.FMT_WMDY_AT_HM_A.format(keyModel.activeDt)));
                        } else {
                            final RawStudent stu = RawStudentLogic.query(cache, stetext.stuId, true);

                            if (stu == null) {
                                htm.addln(Res.fmt(Res.KEY_ACTIVE_NO_STU,
                                        TemporalUtils.FMT_WMDY_AT_HM_A.format(keyModel.activeDt), stetext.stuId));
                            } else {
                                htm.addln(Res.fmt(Res.KEY_ACTIVE_STU,
                                        TemporalUtils.FMT_WMDY_AT_HM_A.format(keyModel.activeDt),
                                        stu.firstName, stu.lastName, stetext.stuId));
                            }

                            emitDeactiveConfirmForm(htm, key);
                        }
                    }

                    htm.addln("</strong>");
                    htm.eDiv();
                }
            }

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Emits the form used to deactivate an e-text key.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     * @param key the initial value to load in for the key
     */
    private static void emitDeactiveConfirmForm(final HtmlBuilder htm, final String key) {

        htm.sDiv("indent22");
        htm.hr();
        htm.div("gap2");
        htm.addln("<form action='deactivate_etext_key_yes.html' method='post'>");
        htm.sDiv("center");
        htm.addln(" <strong class='red'>", Res.get(Res.DEACTIVATE_CONFIRM), "<strong>");
        htm.div("gap2");
        htm.addln(" <input type='hidden' name='key' value='", key, "'/>");
        htm.addln(" <input type='submit' value='", Res.get(Res.DEACTIVATE_BTN_LBL), "'/>");
        htm.eDiv();
        htm.addln("</form>");
        htm.div("gap2").hr().eDiv();
    }

    /**
     * Generates the page to confirm deletion of an active key.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void deactivateKeyYes(final Cache cache, final AdminSite site,
                                 final ServletRequest req, final HttpServletResponse resp,
                                 final ImmutableSessionInfo session) throws IOException, SQLException {

        final String key = req.getParameter("key");

        if (AbstractSite.isParamInvalid(key)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  key='", key, "'");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(400);
            Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR,
                    null, false, true);

            htm.div("gap");
            htm.sDiv("indent11");

            htm.sP("center");
            htm.addln(" <strong>", Res.get(Res.DEPARTMENT_TITLE), "</strong>").br();
            htm.addln(" <strong><span class='green'>", Res.get(Res.SITE_TITLE), "</span></strong>");
            htm.eP();

            htm.sP().add("&nbsp;").eP();
            htm.eDiv();

            if (key == null) {
                BookstorePage.emitKeyForm(htm, null, null);
            } else {
                final RawEtextKey keyModel = RawEtextKeyLogic.query(cache, key);

                if (keyModel == null) {
                    BookstorePage.emitKeyForm(htm, key, Res.get(Res.KEY_NOT_FOUND));
                } else {
                    BookstorePage.emitKeyForm(htm, key, null);

                    htm.div("gap2");

                    htm.sDiv("center");
                    htm.addln("<strong><span class='green'>");

                    if (keyModel.activeDt == null) {
                        htm.addln(Res.get(Res.KEY_NOT_ACTIVE));
                    } else {
                        final RawStetext stetext = RawStetextLogic.getOwnerOfKey(cache, key);

                        if (RawEtextKeyLogic.updateActiveDt(cache, keyModel.etextKey, null)) {

                            if (RawStetextLogic.INSTANCE.delete(cache, stetext)) {
                                htm.addln(Res.get(Res.KEY_DEACTIVATED));
                            }
                        } else {
                            htm.addln("<span class='red'>", Res.get(Res.DEACTIVATION_ERROR), "</span>");
                        }
                    }

                    htm.addln("</span></strong>");
                    htm.eDiv();
                }
            }

            Page.endOrdinaryPage(cache, site, htm, true);

            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}

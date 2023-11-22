package dev.mathops.web.site.help.tutor;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.help.HelpAdminBar;
import dev.mathops.web.site.help.HelpSite;
import dev.mathops.web.skin.IServletSkin;
import dev.mathops.web.skin.SkinnedPage;
import dev.mathops.web.websocket.help.HelpManager;
import dev.mathops.web.websocket.help.livehelp.LiveHelpSession;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Generates the home page.
 */
public enum PageTutorLiveHelp {
    ;

    /**
     * Processes a GET in un-maximized form.
     *
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException if there is an error writing the response
     */
    public static void doGet(final HelpSite site, final ServletRequest req, final ServletResponse resp,
                             final ImmutableSessionInfo session) throws IOException {

        final LiveHelpSession lhsess = HelpManager.getInstance().acceptRequest(session);

        final HtmlBuilder htm = new HtmlBuilder(2000);
        SkinnedPage.startOrdinaryPage(site, htm, false, Res.get(Res.SITE_TITLE), "livehelpmax.html",
                HelpAdminBar.INSTANCE, session);

        if (lhsess == null) {
            htm.sP("center").add("You have no active Live Help session.").eP();
            htm.sP("center").add("<form action='home.html' method='get'>")
                    .add("  <input action='home.html' type='submit' ",
                            "value='Return to Dashboard...'/>")
                    .add("</form>").eP();
        } else {
            final IServletSkin skin = site.skin;
            final int delta = skin.getTopBarHeight(false, false) + skin.getBottomBarHeight(false)
                    + HelpAdminBar.INSTANCE.getAdminBarHeight(session) + 32; // 32 from .layer padding

            emitContents(htm, delta, lhsess);
        }

        SkinnedPage.endOrdinaryPage(site, htm);
        SkinnedPage.sendReply(req, resp, SkinnedPage.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Processes a GET in maximized form.
     *
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException if there is an error writing the response
     */
    public static void doMaxGet(final HelpSite site, final ServletRequest req, final ServletResponse resp,
                                final ImmutableSessionInfo session) throws IOException {

        final LiveHelpSession lhsess = HelpManager.getInstance().acceptRequest(session);

        final HtmlBuilder htm = new HtmlBuilder(2000);
        SkinnedPage.startOrdinaryMaxPage(site, htm, false, Res.get(Res.SITE_TITLE), "livehelp.html",
                HelpAdminBar.INSTANCE, session);

        if (lhsess == null) {
            htm.sP("center").add("You have no active Live Help session.").eP();
            htm.sP("center").add("<form action='homemax.html' method='get'>")
                    .add("  <input type='submit' action='homemax.html' ",
                            "value='Return to Dashboard...'/>")
                    .add("</form>").eP();
        } else {
            final IServletSkin skin = site.skin;
            final int delta = skin.getTopBarHeight(false, true) + skin.getBottomBarHeight(true)
                    + HelpAdminBar.INSTANCE.getAdminBarHeight(session) + 8;

            emitContents(htm, delta, lhsess);
        }

        SkinnedPage.endOrdinaryMaxPage(site, htm);
        SkinnedPage.sendReply(req, resp, SkinnedPage.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits the page contents.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param delta  the delta to the height
     * @param lhsess the help session
     */
    private static void emitContents(final HtmlBuilder htm, final int delta, final LiveHelpSession lhsess) {

        htm.add("<div style='width:calc(100% - 8px); ",
                "height:calc(100vh - " + delta + "px); ",
                "min-width:800px; padding:4px; display:grid; grid-gap: 4px 4px;",
                "grid-template-columns:25% 50% 25%; grid-template-rows:40% 20% 40%;",
                "grid-template-areas: ",
                "\"media scontext sinfo\" ",
                "\"media scontext notes\" ",
                "\"media tcontext notes\";'>");

        PanelMediaChat.emitPanel(htm, true);
        PanelStudentInfo.emitPanel(htm, lhsess);
        PanelStudentContext.emitPanel(htm);

        PanelHistory.emitPanel(htm);
        PanelTutorContext.emitPanel(htm);

        htm.eDiv();
    }
}
// $NON-NLS-1$

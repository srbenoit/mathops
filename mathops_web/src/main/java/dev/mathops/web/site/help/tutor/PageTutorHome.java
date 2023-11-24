package dev.mathops.web.site.help.tutor;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.help.HelpAdminBar;
import dev.mathops.web.site.help.HelpSite;
import dev.mathops.web.skin.IServletSkin;
import dev.mathops.web.skin.SkinnedPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Generates the home page. This page represents the "idle" state of a learning assistant, and presents options for
 * helping students. It also shows the current time and hours of operation.
 *
 * <ul>
 * <li>The queue of live help requests, from which the assistant can claim a request (which
 * navigates to the "PreLiveHelp" page)
 * <li>A summary of all public forums with the number of unanswered requests in each (picking a
 * forum navigates to the "Forum" page)
 * <li>A summary of all private help conversations with messages that have not been answered, by
 * context (picking a context navigates to the "Conversations" page).
 * </ul>
 */
public enum PageTutorHome {
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

        final HtmlBuilder htm = new HtmlBuilder(2000);
        SkinnedPage.startOrdinaryPage(site, htm, false, Res.get(Res.SITE_TITLE), "homemax.html",
                HelpAdminBar.INSTANCE, session);

        final IServletSkin skin = site.skin;
        final int delta = skin.getTopBarHeight(false, false) + skin.getBottomBarHeight(false)
                + HelpAdminBar.INSTANCE.getAdminBarHeight(session) + 32; // 32 from .layer padding

        htm.sDiv("threecolumns", "style='height:calc(100vh - " + delta + "px);'");

        PanelQueue.emitPanel(req, session, htm, false);
        PanelForums.emitPanel(req, session, htm);
        PanelConversations.emitPanel(req, session, htm);

        htm.eDiv();

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

        final HtmlBuilder htm = new HtmlBuilder(2000);
        SkinnedPage.startOrdinaryMaxPage(site, htm, false, Res.get(Res.SITE_TITLE), "home.html",
                HelpAdminBar.INSTANCE, session);

        final IServletSkin skin = site.skin;
        final int delta = skin.getTopBarHeight(false, true) + skin.getBottomBarHeight(true)
                + HelpAdminBar.INSTANCE.getAdminBarHeight(session);

        htm.sDiv("threecolumns", "style='height:calc(100vh - " + delta + "px);'");

        PanelQueue.emitPanel(req, session, htm, true);
        PanelForums.emitPanel(req, session, htm);
        PanelConversations.emitPanel(req, session, htm);

        htm.eDiv();

        SkinnedPage.endOrdinaryMaxPage(site, htm);
        SkinnedPage.sendReply(req, resp, SkinnedPage.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}

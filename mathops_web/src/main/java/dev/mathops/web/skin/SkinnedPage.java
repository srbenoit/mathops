package dev.mathops.web.skin;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.ISkinnedAdminBar;
import dev.mathops.web.site.help.HelpSite;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A base class for web pages that use a skin.
 */
public enum SkinnedPage {
    ;

    /** The MIME type text/html. */
    public static final String MIME_TEXT_HTML = "text/html";

    /**
     * Sends a response with a particular content type and content.
     *
     * @param req         the request
     * @param resp        the response
     * @param contentType the content type
     * @param reply       the reply content
     * @throws IOException if there was an exception writing the response
     */
    public static void sendReply(final ServletRequest req, final ServletResponse resp,
                                 final String contentType, final byte[] reply) throws IOException {

        resp.setContentType(contentType);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(reply.length);
        resp.setLocale(req.getLocale());

        try (final OutputStream out = resp.getOutputStream()) {
            out.write(reply);
        } catch (final IOException ex) {
            if (!"ClientAbortException".equals(ex.getClass().getSimpleName())) {
                throw ex;
            }
        }
    }

    /**
     * Appends the start of the HTML page (the DOCTYPE declaration, opening of the &lt;html&gt; element, &lt;head&gt;
     * block, opening of &lt;body&gt;, and two surrounding &lt;div&gt;s) to an {@code HtmlBuilder}.
     *
     * @param site           the owning site
     * @param htm            the {@code HtmlBuilder} to which to append
     * @param title          the title (to be followed by " | Department of Mathematics")
     * @param allowIndex     {@code true} to allow search engines to index the page
     * @param includeScripts {@code true} to include scripts to resize header/footer
     */
    private static void startPage(final HelpSite site, final HtmlBuilder htm,
                                  final String title, final boolean allowIndex, final boolean includeScripts) {

        htm.addln("<!DOCTYPE html>");
        htm.addln("<html>");

        htm.addln("<head>");
        if (!allowIndex) {
            htm.addln(" <meta name=\"robots\" content=\"noindex\">");
        }

        htm.addln("<script async src='https://www.googletagmanager.com/gtag/js?",
                        "id=G-JTNEG80W4C'></script>")
                .addln("<script>")
                .addln("window.dataLayer = window.dataLayer || [];")
                .addln("function gtag(){dataLayer.push(arguments);}")
                .addln("gtag('js', new Date());")
                .addln("gtag('config', 'G-JTNEG80W4C');")
                .addln("</script>")

                .addln(" <meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>")
                .addln(" <meta http-equiv='Content-Type' ",
                        "content='text/html;charset=utf-8'/>")
                .addln(" <meta name='viewport' ",
                        "content='width=device-width, initial-scale=1'>")

                .addln(" <link rel='stylesheet' href='basestyle.css' type='text/css'>")
                .addln(" <link rel='stylesheet' href='style.css' type='text/css'>")

                .addln(" <style>");

        site.skin.emitStylesheet(htm);
        htm.addln(" </style>")
                .addln(" <link rel='icon' type='image/x-icon' ",
                        "href='/www/images/favicon.ico'>")
                .addln(" <title>", title, "</title>");

        if (includeScripts) {
            site.skin.emitScripts(htm);
        }

        htm.addln("</head>");
    }

    /**
     * Appends the end of the HTML page (the closure of the two surrounding &lt;div&gt;s and the &lt;body&gt; and
     * &lt;html&gt; elements) to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void endPage(final HtmlBuilder htm) {

        htm.addln("</html>");
    }

    /**
     * Writes the start of an ordinary page, including the opening of the "maincontent" div.
     *
     * @param site        the owning site
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param allowIndex  {@code true} to allow search engines to index the page
     * @param title       the page title
     * @param maximizeUrl to allow maximizing, the URL to which the maximize button should link; null to prevent
     *                    maximizing
     * @param adminBar    an optional admin bar
     * @param session     the login session
     */
    public static void startOrdinaryPage(final HelpSite site, final HtmlBuilder htm,
                                            final boolean allowIndex, final String title, final String maximizeUrl,
                                            final ISkinnedAdminBar adminBar, final ImmutableSessionInfo session) {

        startOrdinaryPage(site, htm, allowIndex, title, null, null, maximizeUrl, adminBar, session);
    }

    /**
     * Writes the start of an ordinary page, including the opening of the "maincontent" div.
     *
     * @param site         the owning site
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param allowIndex   {@code true} to allow search engines to index the page
     * @param title        the page title
     * @param subtitle     an optional subtitle
     * @param subtitleLink the URL to which the subtitle should link
     * @param maximizeUrl  to allow maximizing the URL to which the maximize button should link; null to prevent
     *                     maximizing
     * @param adminBar     an optional admin bar
     * @param session      the login session
     */
    private static void startOrdinaryPage(final HelpSite site, final HtmlBuilder htm,
                                          final boolean allowIndex, final String title, final String subtitle,
                                          final String subtitleLink, final String maximizeUrl,
                                          final ISkinnedAdminBar adminBar,
                                          final ImmutableSessionInfo session) {

        startPage(site, htm, title, allowIndex, true);

        htm.addln("<body onload='resized();' onresize='resized();'>");
        htm.addln(" <a class='sr-only' href='#maincontent'>",
                Res.get(Res.SKIP_TO_MAIN), "</a>");

        site.skin.emitTopBar(htm, subtitle, subtitleLink, maximizeUrl, false);

        if (subtitle == null) {
            if (adminBar != null) {
                adminBar.emitAdminBar(site, htm, session);
                htm.sDiv("page-wrapper-admin");
            } else {
                htm.sDiv("page-wrapper");
            }
        } else {
            if (adminBar != null) {
                adminBar.emitAdminBar(site, htm, session);
                htm.sDiv("page-wrapper-admin2");
            } else {
                htm.sDiv("page-wrapper2");
            }
        }

        htm.sDiv("layer", "id='maincontent'");

    }

    /**
     * Writes the start of an ordinary maximized page, including the opening of the "maincontent" div.
     *
     * @param site        the owning site
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param allowIndex  {@code true} to allow search engines to index the page
     * @param title       the page title
     * @param minimizeUrl to allow minimizing, the URL to which the minimize button should link; null to prevent
     *                    minimizing
     * @param adminBar    an optional admin bar
     * @param session     the login session
     */
    public static void startOrdinaryMaxPage(final HelpSite site, final HtmlBuilder htm,
                                               final boolean allowIndex, final String title, final String minimizeUrl,
                                               final ISkinnedAdminBar adminBar, final ImmutableSessionInfo session) {

        startOrdinaryMaxPage(site, htm, allowIndex, title, null, minimizeUrl, adminBar, session);
    }

    /**
     * Writes the start of an ordinary maximized page, including the opening of the "maincontent" div.
     *
     * @param site        the owning site
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param allowIndex  {@code true} to allow search engines to index the page
     * @param title       the page title
     * @param subtitle    an optional subtitle
     * @param minimizeUrl to allow minimizing, the URL to which the minimize button should link; null to prevent
     *                    minimizing
     * @param adminBar    an optional admin bar
     * @param session     the login session
     */
    private static void startOrdinaryMaxPage(final HelpSite site, final HtmlBuilder htm,
                                             final boolean allowIndex, final String title, final String subtitle,
                                             final String minimizeUrl, final ISkinnedAdminBar adminBar,
                                             final ImmutableSessionInfo session) {

        startPage(site, htm, title, allowIndex, false);

        htm.addln("<body>");
        htm.addln(" <a class='sr-only' href='#maincontent'>", Res.get(Res.SKIP_TO_MAIN), "</a>");

        site.skin.emitTopBar(htm, subtitle, null, minimizeUrl, true);

        if (adminBar != null) {
            adminBar.emitAdminBar(site, htm, session);
            htm.sDiv("page-wrapper-max-admin");
        } else {
            htm.sDiv("page-wrapper-max");
        }
    }

    /**
     * Writes the end of an ordinary page, including the closing of the "maincontent" div.
     *
     * @param site the owning site
     * @param htm  the {@code HtmlBuilder} to which to append
     */
    public static void endOrdinaryPage(final HelpSite site, final HtmlBuilder htm) {

        htm.eDiv();
        htm.eDiv();
        site.skin.emitBottomBar(htm, false);
        htm.addln("</body>");
        endPage(htm);
    }

    /**
     * Writes the end of an ordinary maximized page, including the closing of the "maincontent" div.
     *
     * @param site the owning site
     * @param htm  the {@code HtmlBuilder} to which to append
     */
    public static void endOrdinaryMaxPage(final HelpSite site, final HtmlBuilder htm) {

        htm.eDiv();
        htm.eDiv();
        site.skin.emitBottomBar(htm, true);
        htm.addln("</body>");
        endPage(htm);
    }
}

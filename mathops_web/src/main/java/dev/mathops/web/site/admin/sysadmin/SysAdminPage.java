package dev.mathops.web.site.admin.sysadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawWhichDb;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.front.BuildDateTime;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminPage;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * A base class for pages in the administrative system site.
 */
public enum SysAdminPage {
    ;

    /**
     * Creates an {@code HtmlBuilder} and starts a system administration page, emitting the page start and the top level
     * header.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param session the login session
     * @return the created {@code HtmlBuilder}
     * @throws SQLException if there is an error accessing the database
     */
    public static HtmlBuilder startSysAdminPage(final Cache cache, final AdminSite site,
                                                final ImmutableSessionInfo session) throws SQLException {

        final RawWhichDb whichDb = cache.getSystemData().getWhichDb();

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();
        Page.startOrdinaryPage(htm, siteTitle, null, false, null, "home.html", Page.NO_BARS, null, false, true);
        AdminPage.emitPageHeader(htm, session, whichDb, false);

        return htm;
    }

    /**
     * Ends a system administration page.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to write
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void endSysAdminPage(final Cache cache, final HtmlBuilder htm, final AdminSite site,
                                       final ServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        Page.endOrdinaryPage(cache, site, htm, true);
        final String htmStr = htm.toString();
        final byte[] bytes = htmStr.getBytes(StandardCharsets.UTF_8);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
    }

    /**
     * Generates the page that prompts the user to log in.
     *
     * @param selected the currently selected topic; {@code null} if none
     * @param htm      the {@code HtmlBuilder} to which to append
     */
    public static void emitNavBlock(final ESysadminTopic selected, final HtmlBuilder htm) {

        htm.addln("<nav>");
        htm.addln("<script>");
        htm.addln(" function pick(target) {");
        htm.addln("  window.location.assign(target);");
        htm.addln(" }");
        htm.addln("</script>");

        navButton(htm, selected, "first", ESysadminTopic.DB_SERVERS);
        navButton(htm, selected, null, ESysadminTopic.WEB_SERVERS);
        navButton(htm, selected, null, ESysadminTopic.MEDIA_SERVERS);
        navButton(htm, selected, "last", ESysadminTopic.TURN_SERVERS);

        htm.addln("</nav>");
        htm.hr("orange").div("vgap");

        // Show some basic information on the build and runtime environment

        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss 'on' EEEE, MMM d, yyyy", Locale.US);

        final String now = LocalDateTime.now().format(fmt);
        final String buildDateTime = BuildDateTime.get().value;
        final String javaVersion = System.getProperty("java.version");
        final String javaVendor = System.getProperty("java.vendor");
        final String osName = System.getProperty("os.name");
        final String osVersion = System.getProperty("os.version");

        // Attempt to glean HTTPD version
        String httpdVersion = null;
        try {
            final String[] command = {"/opt/httpd/bin/httpd", "-v"};
            final Process p = Runtime.getRuntime().exec(command);
            try (final InputStream in = p.getInputStream()) {
                final byte[] data = FileLoader.readStreamAsBytes(in);
                final String s = new String(data, StandardCharsets.UTF_8).replace(CoreConstants.CRLF, "\n");
                final String[] lines = s.split("\n");

                for (final String line : lines) {
                    if (line.startsWith("Server version: ")) {
                        httpdVersion = line.substring(16);
                        break;
                    }
                }
            }
        } catch (final IOException ex) {
            Log.warning(ex);
        }
        if (httpdVersion == null) {
            httpdVersion = "(unable to determine)";
        }

        // Attempt to glean Tomcat version
        String tomcatVersion = null;
        try {
            final String[] command = {"/opt/tomcat/bin/version.sh"};
            final Process p = Runtime.getRuntime().exec(command);
            try (final InputStream in = p.getInputStream()) {
                final byte[] data = FileLoader.readStreamAsBytes(in);
                final String s = new String(data, StandardCharsets.UTF_8).replace(CoreConstants.CRLF, "\n");
                final String[] lines = s.split("\n");

                for (final String line : lines) {
                    if (line.startsWith("Server version: ")) {
                        tomcatVersion = line.substring(16);
                        break;
                    }
                }
            }
        } catch (final IOException ex) {
            Log.warning(ex);
        }
        if (tomcatVersion == null) {
            tomcatVersion = "(unable to determine)";
        }

        htm.sTable();
        htm.sTr().sTh().add("Current date &nbsp;").eTh().sTd().add(now).eTd().eTr();
        htm.sTr().sTh().add("Build date &nbsp;").eTh().sTd().add(buildDateTime).eTd().eTr();
        htm.sTr().sTh().add("Java Version &nbsp;").eTh().sTd().add(javaVersion, " (", javaVendor, ")").eTd().eTr();
        htm.sTr().sTh().add("HTTPD Version &nbsp;").eTh().sTd().add(httpdVersion).eTd().eTr();
        htm.sTr().sTh().add("Tomcat Version &nbsp;").eTh().sTd().add(tomcatVersion).eTd().eTr();
        htm.sTr().sTh().add("O/S &nbsp;").eTh().sTd().add(osName, CoreConstants.SPC, osVersion).eTd().eTr();
        htm.eTable();
    }

    /**
     * Starts a navigation button.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param selected the selected topic
     * @param id       the button ID ("first" or "last" to adjust margins)
     * @param topic    the topic
     */
    private static void navButton(final HtmlBuilder htm, final ESysadminTopic selected, final String id,
                                  final ESysadminTopic topic) {

        htm.add("<button");
        if (selected == topic) {
            htm.add(" class='nav4 selected'");
        } else {
            htm.add(" class='nav4'");
        }
        if (id != null) {
            htm.add(" id='", id, "'");
        }
        final String url = topic.getUrl();
        final String label = topic.getLabel();
        htm.add(" onclick='pick(\"", url, "\");'>", label, "</button>");
    }

    /**
     * Sends a page to the client indicating the logged-in user is not authorized to access system administration.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void sendNotAuthorizedPage(final Cache cache, final AdminSite site, final ServletRequest req,
                                             final HttpServletResponse resp) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);

        final String siteTitle = site.getTitle();
        Page.startOrdinaryPage(htm, siteTitle, null, false, Page.NO_BARS, null, false, true);

        htm.sH(1).add("System Administration").eH(1);
        htm.div("vgap");
        htm.sP().addln("You are not authorized to perform system administration.").eP();

        Page.endOrdinaryPage(cache, site, htm, true);

        final String htmStr = htm.toString();
        final byte[] bytes = htmStr.getBytes(StandardCharsets.UTF_8);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
    }
}

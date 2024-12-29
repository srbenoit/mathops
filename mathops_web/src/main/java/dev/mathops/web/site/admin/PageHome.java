package dev.mathops.web.site.admin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawrecord.RawWhichDb;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.front.BuildDateTime;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * The home page.
 */
enum PageHome {
    ;

    /**
     * Generates the page that prompts the user to log in.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final AdminSite site, final ServletRequest req, final HttpServletResponse resp,
                      final ImmutableSessionInfo session) throws IOException, SQLException {

        final RawWhichDb whichDb = cache.getSystemData().getWhichDb();

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();
        Page.startOrdinaryPage(htm, siteTitle, null, false, null, "home.html", Page.NO_BARS, null, false, true);
        AdminPage.emitPageHeader(htm, session, whichDb, false);

        final ERole role = session.role;
        if (role == ERole.SYSADMIN) {
            emitSysadminNavBlock(htm);

            htm.div("vgap");

            Page.endOrdinaryPage(cache, site, htm, true);
            final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
            AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
        } else if (role == ERole.ADMINISTRATOR) {
            resp.sendRedirect("genadmin/home.html");
        } else if (role.canActAs(ERole.DIRECTOR)) {
            resp.sendRedirect("director/home.html");
        } else if (role.canActAs(ERole.OFFICE_STAFF)) {
            resp.sendRedirect("office/home.html");
        } else if (role.canActAs(ERole.RESOURCE_DESK)) {
            resp.sendRedirect("resource/home.html");
        } else if (role.canActAs(ERole.CHECKIN_CHECKOUT)) {
            resp.sendRedirect("testing/home.html");
        } else if (role.canActAs(ERole.PROCTOR)) {
            resp.sendRedirect("proctor/home.html");
        } else if (role.canActAs(ERole.BOOKSTORE)) {
            resp.sendRedirect("bookstore/home.html");
        } else {
            final String roleName = role.name();
            Log.warning("GET: invalid role: ", roleName);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    /**
     * Emits the navigation block for a user with the SYSADMIN role.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitSysadminNavBlock(final HtmlBuilder htm) {

        htm.addln("<script>");
        htm.addln(" function pick(target) {");
        htm.addln("  window.location.assign(target);");
        htm.addln(" }");
        htm.addln("</script>");

        htm.addln("<nav>");

        final String generalLbl = Res.get(Res.GENADM_BTN_LBL);
        navButton(htm, "first", generalLbl, "genadmin/home.html");

        final String officeLbl = Res.get(Res.OFFICE_BTN_LBL);
        navButton(htm, null, officeLbl, "office/home.html");

        final String testingLbl = Res.get(Res.TESTING_BTN_LBL);
        navButton(htm, null, testingLbl, "testing/home.html");

        final String proctorLbl = Res.get(Res.PROCTOR_BTN_LBL);
        navButton(htm, "last", proctorLbl, "proctor/home.html");

        htm.addln("</nav>");
        htm.hr("orange").div("vgap");

        // Show some basic information on the build and runtime environment

        final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss 'on' EEEE, MMM d, yyyy", Locale.US);

        final String now = LocalDateTime.now().format(fmt);
        final String buildDateTime = BuildDateTime.getValue();
        final String javaVersion = System.getProperty("java.version");
        final String javaVendor = System.getProperty("java.vendor");
        final String osName = System.getProperty("os.name");
        final String osVersion = System.getProperty("os.version");

        // Attempt to determine operating system release

        String osRelease = null;
        final File releaseFile = new File("/etc/os-release");
        final String[] relLines = FileLoader.loadFileAsLines(releaseFile, false);
        if (relLines != null) {
            for (final String line : relLines) {
                final int index = line.indexOf("PRETTY_NAME=");
                if (index >= 0) {
                    final String sub = line.substring(index + 12).trim();
                    if (sub.startsWith("\"") && sub.endsWith("\"")) {
                        final int len = sub.length();
                        osRelease = sub.substring(1, len - 1);
                        break;
                    }
                }
            }
        }

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

        // Attempt to glean OpenSSL version
        String openSslVersion = null;
        final File sslInclude = new File("/opt/httpd/include/openssl/opensslv.h");
        final String[] incLines = FileLoader.loadFileAsLines(sslInclude, false);
        if (incLines != null) {
            for (final String line : incLines) {
                final int index = line.indexOf("define OPENSSL_VERSION_TEXT");
                if (index >= 0) {
                    final String sub = line.substring(index + 27).trim();
                    if (sub.startsWith("\"") && sub.endsWith("\"")) {
                        final int len = sub.length();
                        openSslVersion = sub.substring(1, len - 1);
                        break;
                    }
                }
            }
        }
        if (openSslVersion == null) {
            openSslVersion = "(unable to determine)";
        }

        // Attempt to glean Tomcat version
        String tomcatVersion = null;
        try {
            final String[] command = {"/opt/tomcat/bin/version.sh"};
            final Process p = Runtime.getRuntime().exec(command);
            try (final InputStream in = p.getInputStream()) {
                final byte[] data = FileLoader.readStreamAsBytes(in);
                final String s = new String(data, StandardCharsets.UTF_8).replace(CoreConstants.CRLF, "\n");
                final String[] versionLines = s.split("\n");

                for (final String line : versionLines) {
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
        htm.sTr().sTh().add("OpenSSL Version &nbsp;").eTh().sTd().add(openSslVersion).eTd().eTr();
        htm.sTr().sTh().add("Tomcat Version &nbsp;").eTh().sTd().add(tomcatVersion).eTd().eTr();
        if (osRelease == null) {
            htm.sTr().sTh().add("OS &nbsp;").eTh().sTd().add(osName, CoreConstants.SPC, osVersion).eTd().eTr();
        } else {
            htm.sTr().sTh().add("OS &nbsp;").eTh().sTd().add(osRelease, " (", osName, CoreConstants.SPC, osVersion,
                    ")").eTd().eTr();
        }
        htm.eTable();
    }

    /**
     * Starts a navigation button.
     *
     * @param htm        the {@code HtmlBuilder} to which to append
     * @param id         the button ID ("first" or "last" to adjust margins)
     * @param buttonName the button name
     * @param url        the URL to which the button redirects
     */
    private static void navButton(final HtmlBuilder htm, final String id, final String buttonName,
                                  final String url) {

        htm.add("<button");
        htm.add(" class='nav4'");
        if (id != null) {
            htm.add(" id='", id, "'");
        }
        htm.add(" onclick='pick(\"", url, "\");'>", buttonName, "</button>");
    }
}

package dev.mathops.web.host.testing.adminsys.genadmin.siteadmin;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdmSubtopic;
import dev.mathops.web.host.testing.adminsys.genadmin.EAdminTopic;
import dev.mathops.web.host.testing.adminsys.genadmin.GenAdminPage;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * The Website Administration page.
 */
public enum PageSiteAdmin {
    ;

    /**
     * Generates the website administration page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.SITE_ADMIN, htm);
        htm.sH(1).add("Web Site Administration").eH(1);

        emitNavMenu(htm, null);
        htm.hr().div("vgap");

        final File stage = new File("/opt/tomcat/stage");
        final File webapps = new File("/opt/tomcat/webapps");

        htm.addln("<form method='POST' action='site_admin.html'>");
        htm.sH(2).addln("WAR files available to deploy").eH(2);
        htm.sDiv("indent");
        final File[] stageList = stage.listFiles();
        if (stageList != null) {
            for (final File item : stageList) {
                final String name = item.getName();

                if (item.isDirectory()) {
                    final File[] subList = item.listFiles();
                    if (subList != null) {
                        for (final File subItem : subList) {
                            if (subItem.isDirectory()) {
                                continue;
                            }
                            final String subname = subItem.getName();
                            if (subname.endsWith(".war")) {
                                final long size = subItem.length();
                                final String sizeStr = Long.toString(size);
                                htm.sP().addln("<code>", name, "/", subname, "</code> (", sizeStr,
                                        " bytes) <input type='submit' name='", name, "~",
                                        subname, "' value='Deploy'/>").eP();
                            }
                        }
                    }
                } else if ("ROOT.war".equals(name)) {
                    final long size = item.length();
                    final String sizeStr = Long.toString(size);
                    htm.sP().addln("<code>", name, " (", sizeStr, "</code> bytes) <button type='submit' name='", name,
                            "' value='Deploy'/>").eP();
                }
            }
        }
        htm.eDiv();
        htm.div("vgap");

        htm.sH(2).addln("Active WAR files:").eH(2);
        htm.sDiv("indent");
        final File[] currentList = webapps.listFiles();
        if (currentList != null) {
            for (final File curItem : currentList) {
                final String name = curItem.getName();

                if (curItem.isDirectory()) {
                    final File[] subList = curItem.listFiles();
                    if (subList != null) {
                        for (final File subItem : subList) {
                            if (subItem.isDirectory()) {
                                continue;
                            }
                            final String subname = subItem.getName();
                            if (subname.endsWith(".war")) {
                                final long size = subItem.length();
                                final String sizeStr = Long.toString(size);
                                htm.sP().addln("<code>", name, "/", subname, "</code> (", sizeStr, " bytes)").eP();
                            }
                        }
                    }
                } else if ("ROOT.war".equals(name)) {
                    final long size = curItem.length();
                    final String sizeStr = Long.toString(size);
                    htm.sP().addln("<code>", name, "</code> (", sizeStr, " bytes)").eP();
                }
            }
        }
        htm.eDiv();

        htm.addln("</form>");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Generates the website administration page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doPost(final Cache cache, final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        String path = null;

        final Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            final String name = paramNames.nextElement();
            final String value = req.getParameter(name);
            if ("Deploy".equals(value)) {
                path = name;
                break;
            }
        }

        if (path != null) {
            final String actual = path.replace("~", "/");
            final File stage = new File("/opt/tomcat/stage");
            final File webapps = new File("/opt/tomcat/webapps");

            final File source = new File(stage, actual);
            File target = null;

            final File initialTarget = new File(webapps, actual);
            final String name = initialTarget.getName();
            if ("ROOT.war".equals(name)) {
                target = initialTarget;
            } else if (name.endsWith("_ROOT.war")) {
                final String parent = initialTarget.getParent();
                target = new File(parent, "ROOT.war");
            }

            if (target != null) {
                Log.info("Copying [", source.getAbsolutePath(), "] to [", target.getAbsolutePath(), "]");

                if (target.exists()) {
                    final File parent = target.getParentFile();
                    final File bak1 = new File(parent, "ROOT.war.bak1");
                    final File bak2 = new File(parent, "ROOT.war.bak2");
                    if (bak2.exists()) {
                        bak2.delete();
                    }
                    if (bak1.exists()) {
                        bak1.renameTo(bak2);
                    }
                    target.renameTo(bak1);
                }

                copyFile(source, target);
            }
        }

        doGet(cache, site, req, resp, session);
    }

    /**
     * Copies a file to a target directory. If there is already a file in the target directory with the same name, that
     * file is overwritten!
     *
     * @param source the source file
     * @param target the target file
     */
    private static void copyFile(final File source, final File target) {

        final byte[] bytes = FileLoader.loadFileAsBytes(source, true);
        if (bytes != null) {
            try (final FileOutputStream out = new FileOutputStream(target)) {
                out.write(bytes);
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Emits the server administration navigation sub-menu with an optional selected item and query string to append to
     * each button link.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param selected the subtopic to show as selected
     */
    private static void emitNavMenu(final HtmlBuilder htm, final EAdmSubtopic selected) {

        htm.addln("<nav>");

        // TODO:
        // navButtonSmall(htm, selected, EAdmSubtopic.SRV_SESSIONS, null);
        // navButtonSmall(htm, selected, EAdmSubtopic.SRV_DIAGNOSTICS, null);

        htm.addln("</nav>");
    }
}

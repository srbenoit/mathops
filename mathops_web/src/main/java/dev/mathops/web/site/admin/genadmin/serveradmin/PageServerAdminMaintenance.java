package dev.mathops.web.site.admin.genadmin.serveradmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.xml.XmlEscaper;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdmSubtopic;
import dev.mathops.web.site.admin.genadmin.EAdminTopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

/**
 * Pages to provide maintenance mode management.
 *
 * <p>
 * Maintenance mode settings are stored in a 'maintenence.properties' file in the config directory. It will have the
 * following entries for every host/path combination in the context set where the path begins with '/':
 *
 * <pre>
 * host~default_msg=Default maintenance message for the host.
 * &lt;host&gt;&lt;path&gt;~maintenance=[true|false]
 * &lt;host&gt;&lt;path&gt;~msg=Optional override maintenance message for path.
 * </pre>
 */
public enum PageServerAdminMaintenance {
    ;

    /**
     * Generates a page that shows the global maintenance message and a list of websites. Maintenance mode can be
     * turned on or off on each site, and an optional override maintenance message can be provided per site.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the responses
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.SERVER_ADMIN, htm);

        PageServerAdmin.emitNavMenu(htm, EAdmSubtopic.SRV_MAINTENANCE);
        doPageContent(htm);

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Appends page content to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to write
     */
    private static void doPageContent(final HtmlBuilder htm) {

        htm.div("vgap0").hr().div("vgap0");

        htm.sH(2).add("Manage Maintenance Mode").eH(2);

        htm.sP("indent");
        htm.addln(" This page allows you to place some or all web sites into maintenance mode.");
        htm.addln(" When in maintenance mode, a maintenance message is displayed and users are");
        htm.addln(" prevented from logging in or accessing any site information. A web site in");
        htm.addln(" maintenance mode should not attempt any database access.");
        htm.eP();

        htm.addln("<hr style='margin-bottom:5px;'/>");
        doMaintenanceModeList(htm);
    }

    /**
     * Stores the maintenance file.
     *
     * @param props the maintenance properties
     */
    private static void storeMaintenanceFile(final Properties props) {

        final ContextMap map = ContextMap.getDefaultInstance();

        final File cfg = PathList.getInstance().get(EPath.CFG_PATH);
        final File file = new File(cfg, AbstractSite.MAINT_FILE);

        if (file.exists()) {
            if (!file.renameTo(new File(cfg, AbstractSite.MAINT_FILE + ".old"))) {
                Log.warning("Failed to rename ", file.getAbsolutePath());
            }
        }

        final HtmlBuilder builder = new HtmlBuilder(10000);

        builder.addln("################################################################################");
        builder.addln("# Configuration of maintenance mode and maintenance messages for all hostnames #");
        builder.addln("################################################################################");
        builder.addln();

        final String[] hosts = map.getWebHosts();
        Arrays.sort(hosts);

        for (final String host : hosts) {
            builder.addln('#');
            builder.addln("# Host: ", host);
            builder.addln('#');
            builder.addln();

            builder.addln("# ...default maintenance message for all pages on host");
            builder.add(host, "_default_msg=");
            final String defMsg = props.getProperty(host + "_default_msg");
            builder.addln(defMsg == null ? CoreConstants.EMPTY : defMsg);
            builder.addln();

            final String[] hostPaths = map.getWebSites(host);
            if (hostPaths != null) {
                Arrays.sort(hostPaths);

                for (final String hPath : hostPaths) {
                    if (hPath.isEmpty() || hPath.charAt(0) != '/') {
                        continue;
                    }

                    final String key = host + hPath.replace(CoreConstants.SLASH, "~");

                    builder.addln("# ...settings for '", hPath,
                            "' path (maintenance flag and optional override message)");

                    builder.add(key, "_maintenance=");
                    final String maint = props.getProperty(key + "_maintenance");
                    builder.addln(maint == null ? "false" : maint);

                    builder.add(key, "_msg=");
                    final String msg = props.getProperty(key + "_msg");
                    builder.addln(msg == null ? CoreConstants.EMPTY : msg);
                    builder.addln();
                }
            }
        }

        try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(builder.toString());
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Builds the maintenance mode management portion of the website management page.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void doMaintenanceModeList(final HtmlBuilder htm) {

        final ContextMap map = ContextMap.getDefaultInstance();

        // Maintenance Mode
        htm.sDiv("indent1");
        htm.addln("<form action='maint_mode_update.html' method='post'>");

        final Properties props = AbstractSite.loadMaintenanceFile();

        final String[] hosts = map.getWebHosts();
        Arrays.sort(hosts);

        for (final String host : hosts) {
            htm.sH(4).add("Hostname: <code class='green'>", host, "</code>").eH(4);

            // Display the global maintenance HTML for this host
            htm.sP().add("Maintenance text for all paths within the hostname that do not specify path-specific text:")
                    .br();
            htm.addln(" <textarea id='", host, ".gen' name='", host, ".gen' rows='6' cols='80'>");
            final String defMsg = props.getProperty(host + "_default_msg");
            if (defMsg != null) {
                htm.addln(XmlEscaper.escape(defMsg));
            }
            htm.addln(" </textarea>");

            // Display the paths within the hostname, and each path's maintenance status
            final String[] hostPaths = map.getWebSites(host);
            if (hostPaths != null) {
                Arrays.sort(hostPaths);

                htm.addln(" <table>");
                htm.addln("  <tr>");
                htm.addln("   <th colspan='2'>Maintenance?</th>");
                htm.addln("   <th colspan='2'>Path-Specific Maintenance Message</th>");
                htm.addln("  </tr>");

                for (final String hPath : hostPaths) {
                    if (hPath.isEmpty() || hPath.charAt(0) != '/') {
                        continue;
                    }

                    final String key = host + hPath.replace(CoreConstants.SLASH, "~");

                    htm.addln("  <tr>");
                    htm.addln("   <td style='vertical-align:top;width:16pt;'>");
                    htm.add("    <input type='checkbox' name='chk_", key, "' id='chk_", key, "'");
                    final String maint = props.getProperty(key + "_maintenance");
                    final boolean inMaint = "true".equalsIgnoreCase(maint);
                    if (inMaint) {
                        htm.add(" checked='checked'");
                    }
                    htm.addln(">");
                    htm.addln("   </td>");

                    htm.addln("   <td style='vertical-align:top;width:100pt;'><code>", hPath, "</code></td>");

                    htm.addln("   <td style='vertical-align:top;'>");
                    htm.add("    <textarea name='msg_", key, "' id='msg_",
                            key, "' rows='3' cols='56'>");
                    final String msg = props.getProperty(key + "_msg");
                    if (msg != null) {
                        htm.add(XmlEscaper.escape(msg));
                    }
                    htm.addln("</textarea>");
                    htm.addln("   </td>");
                    htm.addln("  </tr>");
                }

                htm.addln(" </table></p>");
                htm.addln("<hr style='margin-bottom:5px;'/>");
            }
        }

        htm.addln("  <button name='submit' type='submit' val ue='submit'>Update</button>");
        htm.addln("  <button name='reset' type='reset'>Reset</button>");
        htm.addln(" </form>");
        htm.eDiv();
    }

    /**
     * Handles update of maintenance mode settings, then redirects to maintenance mode page.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the responses
     */
    public static void doMaintenanceModeUpdate(final ServletRequest req,
                                               final HttpServletResponse resp) throws IOException {

        final ContextMap map = ContextMap.getDefaultInstance();

        final Properties props = AbstractSite.loadMaintenanceFile();

        final String[] hosts = map.getWebHosts();

        // Update the properties object based on field values
        boolean changed = false;
        for (final String host : hosts) {

            String genPrm = req.getParameter(host + ".gen");
            if (genPrm != null) {
                genPrm = genPrm.trim();
                if (genPrm.isEmpty()) {
                    genPrm = null;
                }
            }
            final String genProp = props.getProperty(host + "_default_msg");
            if (!Objects.equals(genPrm, genProp)) {
                if (genPrm == null) {
                    props.remove(host + "_default_msg");
                } else {
                    props.setProperty(host + "_default_msg", genPrm);
                }
                changed = true;
            }

            final String[] hostPaths = map.getWebSites(host);
            if (hostPaths != null) {
                for (final String hPath : hostPaths) {
                    if (hPath.isEmpty() || hPath.charAt(0) != '/') {
                        continue;
                    }

                    final String key = host + hPath.replace(CoreConstants.SLASH, "~");

                    final String chkPrm = req.getParameter("chk_" + key) == null ? "false" : "true";
                    final String chkProp = props.getProperty(key + "_maintenance");
                    if (!Objects.equals(chkPrm, chkProp)) {
                        props.setProperty(key + "_maintenance", chkPrm);
                        changed = true;
                    }

                    final String msgPrm = req.getParameter("msg_" + key);
                    final String msgProp = props.getProperty(key + "_msg");
                    if (!Objects.equals(msgPrm, msgProp)) {
                        props.setProperty(key + "_msg", msgPrm);
                        changed = true;
                    }
                }
            }
        }

        if (changed) {
            storeMaintenanceFile(props);
        }

        resp.sendRedirect("srvadm_maintenance.html");
    }
}

package dev.mathops.web.site.admin.sysadmin.db;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.EPath;
import dev.mathops.commons.PathList;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.parser.xml.XmlEscaper;
import dev.mathops.db.Cache;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.enums.ERole;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.sysadmin.ESysadminTopic;
import dev.mathops.web.site.admin.sysadmin.SysAdminPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * Top-level page for database server management.
 */
public enum PageDb {
    ;

    /** The list of servers read from the XML configuration file. */
    private static DataDbServersList serversList;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        if (session.role == ERole.SYSADMIN) {
            final HtmlBuilder htm = SysAdminPage.startSysAdminPage(cache, site, session);

            SysAdminPage.emitNavBlock(ESysadminTopic.DB_SERVERS, htm);
            emitPageContent(htm);
            emitXmlFile(htm);

            SysAdminPage.endSysAdminPage(cache, htm, site, req, resp);
        } else {
            SysAdminPage.sendNotAuthorizedPage(cache, site, req, resp);
        }
    }

    /**
     * Emits the content of the page.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void emitPageContent(final HtmlBuilder htm) {

        htm.sH(2).add("Database Servers").eH(2);
        emitServersNav(htm, null, true);
    }

    /**
     * Emits a navigation block with all configured servers, and a button to create a new server.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param selected the selected server; {@code null} if none
     * @param allowAdd true to include the "add" button
     */
    static void emitServersNav(final HtmlBuilder htm, final DataDbServer selected, final boolean allowAdd) {

        htm.addln("<nav>");

        synchronized (CoreConstants.INSTANCE_SYNCH) {
            if (serversList == null) {
                serversList = DataDbServersList.load();
            }

            // List all defined database servers
            if (serversList != null) {
                final int size = serversList.size();
                for (int i = 0; i < size; ++i) {
                    final DataDbServer server = serversList.getServer(i);
                    final String name = server.name;
                    final String hostname = server.hostname;

                    if (selected == null || !selected.hostname.equals(hostname)) {
                        htm.add("<button title='", hostname, "' class='nav6'");
                    } else {
                        htm.add("<button title='", hostname, "' class='nav6 selected'");
                    }

                    htm.add(" onclick='pick(\"db_srv.html?hostname=", hostname, "\");'>", name, "</button>");
                }
            }
        }

        if (allowAdd) {
            // Button to create new server
            htm.add("<button id='add' class='nav6'");
            htm.add(" onclick='pick(\"db_srv_add.html\");'>Add Server</button>");
        }

        htm.addln("</nav>");
    }

    /**
     * Emits the contents of the "db_servers.xml" file for debugging.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    static void emitXmlFile(final HtmlBuilder htm) {

        final File file = new File(PathList.getInstance().get(EPath.CFG_PATH), "db_servers.xml");
        final String xml = FileLoader.loadFileAsString(file, false);

        htm.div("vgap").hr();
        if (xml == null) {
            htm.sP().add("Unable to read ", file.getAbsolutePath()).eP();
        } else {
            htm.sP().add("Contents of ", file.getAbsolutePath(), CoreConstants.COLON).eP();
            htm.addln("<pre style='font-size:80%'>").addln(XmlEscaper.escape(xml)).addln("</pre>");
        }
    }

    /**
     * Retrieves a database server object based on its hostname.
     *
     * @param hostname the hostname
     * @return the database object; {@code null} if none was found
     */
    static DataDbServer getServer(final String hostname) {

        DataDbServer result = null;

        synchronized (CoreConstants.INSTANCE_SYNCH) {
            if (serversList == null) {
                serversList = DataDbServersList.load();
            }
            if (serversList != null) {
                final int size = serversList.size();
                for (int i = 0; i < size; ++i) {
                    final DataDbServer test = serversList.getServer(i);
                    if (test.hostname.equals(hostname)) {
                        result = test;
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Adds a server to the server list.
     *
     * @param server the server to add
     */
    static void addServer(final DataDbServer server) {

        synchronized (CoreConstants.INSTANCE_SYNCH) {
            if (serversList == null) {
                serversList = DataDbServersList.load();
            }
            if (serversList == null) {
                serversList = new DataDbServersList();
            }
            serversList.addServer(server);
            serversList.save();
        }
    }

    /**
     * Attempts to find a database server with a specified hostname. If found, the server is deleted.
     *
     * @param hostname the hostname
     */
    static void deleteServer(final String hostname) {

        synchronized (CoreConstants.INSTANCE_SYNCH) {
            if (serversList == null) {
                serversList = DataDbServersList.load();
            }
            if (serversList != null) {
                final int size = serversList.size();
                for (int i = 0; i < size; ++i) {
                    final DataDbServer test = serversList.getServer(i);
                    if (test.hostname.equals(hostname)) {
                        serversList.removeServer(i);
                        serversList.save();
                        break;
                    }
                }
            }
        }
    }

    /**
     * Adds an installed database product to a database server.
     *
     * @param server  the server
     * @param product the product to add
     */
    static void addProductServer(final DataDbServer server, final DataDbInstalledProduct product) {

        synchronized (CoreConstants.INSTANCE_SYNCH) {
            boolean missing = true;
            for (final DataDbInstalledProduct test : server.products) {
                if (test.product == product.product && test.version.equals(product.version)) {
                    missing = false;
                    break;
                }
            }

            if (missing) {
                server.products.add(product);
                serversList.save();
            }
        }
    }

    /**
     * Attempts to find an installed product record within a database server with a specified product type and version.
     * If found, the product record is deleted.
     *
     * @param server  the server
     * @param product the product to delete
     * @param version the version to delete
     */
    static void deleteProduct(final DataDbServer server, final EDbProduct product,
                              final String version) {

        synchronized (CoreConstants.INSTANCE_SYNCH) {
            final Iterator<DataDbInstalledProduct> iter = server.products.iterator();
            while (iter.hasNext()) {
                final DataDbInstalledProduct prod = iter.next();
                if (prod.product == product && prod.version.equals(version)) {
                    iter.remove();
                    serversList.save();
                    break;
                }
            }
        }
    }
}

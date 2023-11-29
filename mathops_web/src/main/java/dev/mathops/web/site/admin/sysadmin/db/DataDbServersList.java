package dev.mathops.web.site.admin.sysadmin.db;

import dev.mathops.core.EPath;
import dev.mathops.core.PathList;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.IElement;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.parser.xml.NonemptyElement;
import dev.mathops.core.parser.xml.XmlContent;
import dev.mathops.web.file.WebFileLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * A list of database servers, as stored in "db_servers.xml" in the configuration directory.
 */
final class DataDbServersList {

    /** The list of servers. */
    private final List<DataDbServer> servers;

    /**
     * Constructs a new {@code DataDbServersList}.
     */
    DataDbServersList() {

        this.servers = new ArrayList<>(4);
    }

    /**
     * Gets the size of the list.
     *
     * @return the size
     */
    int size() {

        return this.servers.size();
    }

    /**
     * Gets the information on a single server.
     *
     * @param index the index
     * @return the server
     */
    public DataDbServer getServer(final int index) {

        return this.servers.get(index);
    }

    /**
     * Removes a server.
     *
     * @param index the index of the server to remove
     */
    void removeServer(final int index) {

        this.servers.remove(index);
    }

    /**
     * Adds a server.
     *
     * @param server the server to add
     */
    void addServer(final DataDbServer server) {

        // Primary key is hostname - prevent duplicates
        boolean scanning = true;
        for (final DataDbServer test : this.servers) {
            if (test.hostname.equals(server.hostname)) {
                scanning = false;
                break;
            }
        }

        if (scanning) {
            this.servers.add(server);
        }
    }

    /**
     * Writes the server list to the XML file.
     */
    void save() {

        final HtmlBuilder htm = new HtmlBuilder(100);
        htm.addln("<db-servers>");
        for (final DataDbServer server : this.servers) {
            server.appendXml(htm);
        }
        htm.addln("</db-servers>");

        final File file = new File(PathList.getInstance().get(EPath.CFG_PATH), "db_servers.xml");
        try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(htm.toString());
        } catch (final IOException ex) {
            Log.warning("Failed to write 'db_servers.xml'", ex);
        }
    }

    /**
     * Reads the server list from its XML file.
     *
     * @return the loaded file; {@code null} if unable to load
     */
    static DataDbServersList load() {

        DataDbServersList list = null;

        final File file = new File(PathList.getInstance().get(EPath.CFG_PATH), "db_servers.xml");
        final String xml = WebFileLoader.loadFileAsString(file, false);
        if (xml != null) {
            try {
                final XmlContent content = new XmlContent(xml, true, false);
                final IElement top = content.getToplevel();

                if ("db-servers".equals(top.getTagName())) {
                    if (top instanceof final NonemptyElement ntop) {
                        list = new DataDbServersList();

                        for (final INode node : ntop.getChildrenAsList()) {
                            if (node instanceof final IElement elem) {
                                if ("db-server".equals(elem.getTagName())) {
                                    final DataDbServer server = DataDbServer.parse(elem);

                                    if (server != null) {
                                        list.servers.add(server);
                                    }
                                } else {
                                    Log.warning("Found unexpected '", elem.getTagName(),
                                            "' child of 'db-servers' - ignoring.");
                                }
                            }
                        }
                    }
                } else {
                    Log.warning("Top-level element in ", file.getAbsolutePath(), " was '", top.getTagName(),
                            "' when 'db-servers' was expected");
                }
            } catch (final ParsingException ex) {
                Log.warning("Failed to parse ", file.getAbsolutePath(), ex);
            }
        }

        return list;
    }
}

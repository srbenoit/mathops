package dev.mathops.app.database.dba;

import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Server;
import jwabbit.FileLoader;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A "ribbon" that shows the set of all defined servers and databases and allows the user to select (using checkboxes)
 * which databases are "active" for administration.
 */
public final class DatabaseRibbon extends JPanel {

    /** A map from server to a map from database to its checkbox. */
    private final Map<Server, Map<Database, JCheckBox>> checkboxes;

    /**
     * Constructs a new {@code DatabaseRibbon}.
     *
     * @param config   the database configuration
     * @param listener the listener to notify when the set of selected databases changes
     */
    DatabaseRibbon(final DatabaseConfig config, final ActionListener listener) {

        super(new StackedBorderLayout());

        final Color background = getBackground();
        final boolean isLight = InterfaceUtils.isLight(background);
        final Color accent = InterfaceUtils.createAccentColor(background, isLight);

        final Border outline = BorderFactory.createLineBorder(accent);
        setBorder(outline);

        final Class<? extends DatabaseRibbon> cls = getClass();
        final BufferedImage cassandraIcon = FileLoader.loadFileAsImage(cls, "cassandra.png", true);
        final BufferedImage informixIcon = FileLoader.loadFileAsImage(cls, "informix.png", true);
        final BufferedImage mysqlIcon = FileLoader.loadFileAsImage(cls, "mysql.png", true);
        final BufferedImage oracleIcon = FileLoader.loadFileAsImage(cls, "oracle.png", true);
        final BufferedImage postgresqlIcon = FileLoader.loadFileAsImage(cls, "postgresql.png", true);

        final Border leftLine = BorderFactory.createMatteBorder(0, 1, 0, 0, accent);
        final Border rightLine = BorderFactory.createMatteBorder(0, 0, 0, 1, accent);
        final Border topLine = BorderFactory.createMatteBorder(1, 0, 0, 0, accent);

        final List<Server> servers = config.getServers();
        final int numServers = servers.size();
        this.checkboxes = new HashMap<>(numServers);

        for (final Server server : servers) {
            final JPanel serverPane = new JPanel(new StackedBorderLayout());
            serverPane.setBorder(rightLine);
            add(serverPane, StackedBorderLayout.WEST);

            final JLabel serverLabel = new JLabel("  Server  ");
            serverLabel.setHorizontalAlignment(SwingConstants.CENTER);
            serverPane.add(serverLabel, StackedBorderLayout.NORTH);

            final JLabel hostPort = new JLabel("  " + server.host + ":" + server.port + "  ");
            hostPort.setHorizontalAlignment(SwingConstants.CENTER);
            serverPane.add(hostPort, StackedBorderLayout.NORTH);

            ImageIcon icon = null;
            switch (server.type) {
                case INFORMIX -> {
                    if (informixIcon != null) {
                        icon = new ImageIcon(informixIcon);
                    }
                }
                case ORACLE -> {
                    if (oracleIcon != null) {
                        icon = new ImageIcon(oracleIcon);
                    }
                }
                case POSTGRESQL -> {
                    if (postgresqlIcon != null) {
                        icon = new ImageIcon(postgresqlIcon);
                    }
                }
                case MYSQL -> {
                    if (mysqlIcon != null) {
                        icon = new ImageIcon(mysqlIcon);
                    }
                }
                case CASSANDRA -> {
                    if (cassandraIcon != null) {
                        icon = new ImageIcon(cassandraIcon);
                    }
                }
            }

            if (icon != null) {
                final JLabel lbl = new JLabel(icon);
                serverPane.add(lbl, StackedBorderLayout.NORTH);
            }

            final JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            south.setBorder(topLine);
            serverPane.add(south, StackedBorderLayout.SOUTH);

            final List<Database> databases = server.getDatabases();
            final int numDatabases = databases.size();
            final Map<Database, JCheckBox> checkboxMap = new HashMap<>(numDatabases);
            this.checkboxes.put(server, checkboxMap);

            boolean bar = false;
            for (final Database database : databases) {

                final JPanel dbPane = new JPanel(new StackedBorderLayout());
                south.add(dbPane);
                if (bar) {
                    dbPane.setBorder(leftLine);
                }

                final JLabel dbLabel = new JLabel("  Database  ");
                dbLabel.setHorizontalAlignment(SwingConstants.CENTER);
                dbPane.add(dbLabel, StackedBorderLayout.NORTH);

                final String txt = database.instance == null ? database.id :
                        (database.id + " (" + database.instance + ")");

                final JLabel idLabel = new JLabel("  " + txt + "  ");
                idLabel.setHorizontalAlignment(SwingConstants.CENTER);
                dbPane.add(idLabel, StackedBorderLayout.NORTH);

                final JPanel flow = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
                dbPane.add(flow, StackedBorderLayout.NORTH);

                final JCheckBox check = new JCheckBox();
                check.addActionListener(listener);
                flow.add(check);

                checkboxMap.put(database, check);
                bar = true;
            }
        }
    }

    /**
     * Gets the list of selected databases.
     *
     * @param databases a list to populate with the selected databases
     */
    void getSelectedDatabases(final Collection<? super Database> databases) {

        databases.clear();

        for (final Map<Database, JCheckBox> map : this.checkboxes.values()) {
            for (final Map.Entry<Database, JCheckBox> entry : map.entrySet()) {
                final JCheckBox checkbox = entry.getValue();
                if (checkbox.isSelected()) {
                    final Database db = entry.getKey();
                    databases.add(db);
                }
            }
        }
    }
}

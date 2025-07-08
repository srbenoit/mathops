package dev.mathops.app.database.dba;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbUse;
import dev.mathops.db.cfg.Data;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Server;
import jwabbit.FileLoader;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A single tile in the database ribbon.  A tile represents a single server.
 */
final class DatabaseRibbonTile extends JPanel implements ActionListener {

    /** Key for the card with the connect button. */
    private static final String CONNECT_CARD = "CONNECT";

    /** Key for the card with uses and selector check-box. */
    private static final String SELECT_CARD = "SELECT";

    /** The server this tile represents. */
    private final Server server;

    /** A map from database to its connection. */
    private final Map<Database, DbConnection> connections;

    /** A map from database to a label showing the connected user. */
    private final Map<Database, JLabel> usernames;

    /** A map from database to its checkbox. */
    private final Map<Database, Map<EDbUse, JCheckBox>> checkboxes;

    /**
     * Constructs a new {@code DatabaseRibbonTile}
     *
     * @param theServer      the server this tile represents
     * @param listener       the listener to notify when the set of selected databases changes
     * @param theConnections a map from database to its connection
     * @param accent         the accent color to use for dividers
     */
    DatabaseRibbonTile(final Server theServer, final ActionListener listener,
                       final Map<Database, DbConnection> theConnections, final Color accent) {

        super(new StackedBorderLayout());

        this.server = theServer;
        this.connections = theConnections;

        final Class<? extends DatabaseRibbonTile> cls = getClass();
        final BufferedImage cassandraIcon = FileLoader.loadFileAsImage(cls, "cassandra.png", true);
        final BufferedImage informixIcon = FileLoader.loadFileAsImage(cls, "informix.png", true);
        final BufferedImage mysqlIcon = FileLoader.loadFileAsImage(cls, "mysql.png", true);
        final BufferedImage oracleIcon = FileLoader.loadFileAsImage(cls, "oracle.png", true);
        final BufferedImage postgresqlIcon = FileLoader.loadFileAsImage(cls, "postgresql.png", true);

        final Border leftLine = BorderFactory.createMatteBorder(0, 1, 0, 0, accent);
        final Border rightLine = BorderFactory.createMatteBorder(0, 0, 0, 1, accent);
        final Border topLine = BorderFactory.createMatteBorder(1, 0, 0, 0, accent);

        setBorder(rightLine);

        final JLabel serverLabel = new JLabel("  Server  ");
        serverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(serverLabel, StackedBorderLayout.NORTH);

        final String trimmedHost = theServer.host.replace(".colostate.edu", "");
        final JLabel hostPort = new JLabel("  " + trimmedHost + ":" + theServer.port + "  ");
        hostPort.setHorizontalAlignment(SwingConstants.CENTER);
        add(hostPort, StackedBorderLayout.NORTH);

        BufferedImage iconImg = null;
        switch (theServer.type) {
            case INFORMIX -> iconImg = informixIcon;
            case ORACLE -> iconImg = oracleIcon;
            case POSTGRESQL -> iconImg = postgresqlIcon;
            case MYSQL -> iconImg = mysqlIcon;
            case CASSANDRA -> iconImg = cassandraIcon;
        }

        if (iconImg != null) {
            final ImageIcon icon = new ImageIcon(iconImg);
            final JLabel lbl = new JLabel(icon);
            add(lbl, StackedBorderLayout.NORTH);
        }

        final JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        south.setBorder(topLine);
        add(south, StackedBorderLayout.SOUTH);

        final List<Database> databases = server.getDatabases();
        final int numDatabases = databases.size();
        this.usernames = new HashMap<>(numDatabases);
        this.checkboxes = new HashMap<>(numDatabases);

        boolean bar = false;
        for (final Database database : databases) {

            // One sub-panel for each database
            final JPanel dbPane = new JPanel(new StackedBorderLayout());
            if (bar) {
                dbPane.setBorder(leftLine);
            }
            south.add(dbPane);
            bar = true;

            final JLabel dbLabel = new JLabel("  Database  ");
            dbLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dbPane.add(dbLabel, StackedBorderLayout.NORTH);

            final String txt = database.instance == null ? database.id :
                    (database.id + " (" + database.instance + ")");

            final JLabel idLabel = new JLabel("  " + txt + "  ");
            idLabel.setHorizontalAlignment(SwingConstants.CENTER);
            dbPane.add(idLabel, StackedBorderLayout.NORTH);

            final CardLayout cardLayout = new CardLayout();
            final JPanel dbCards = new JPanel(cardLayout);
            dbPane.add(dbCards, StackedBorderLayout.SOUTH);

            // Card when database is "not yet connected"

            final JButton connect = new JButton("Connect...");
            connect.setActionCommand(database.id);
            connect.addActionListener(this);
            final JPanel buttonFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
            buttonFlow.add(connect);
            dbCards.add(buttonFlow, CONNECT_CARD);

            // Card when database is connected
            final JPanel connectedCard = new JPanel(new StackedBorderLayout());
            dbCards.add(connectedCard, SELECT_CARD);

            final JPanel connectedAsFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 0));
            final JLabel connectedAs = new JLabel("Connected as:");
            connectedAsFlow.add(connectedAs);
            final JLabel userLbl = new JLabel(CoreConstants.SPC);
            connectedAsFlow.add(userLbl);
            this.usernames.put(database, userLbl);
            connectedCard.add(connectedAsFlow, StackedBorderLayout.NORTH);

            final JPanel uses = new JPanel(new StackedBorderLayout());
            uses.setBorder(topLine);
            connectedCard.add(uses, StackedBorderLayout.CENTER);

            final Map<EDbUse, JCheckBox> useCheckboxes = new EnumMap<>(EDbUse.class);
            this.checkboxes.put(database, useCheckboxes);

            for (final EDbUse use : EDbUse.values()) {
                boolean hasUse = false;
                for (final Data data : database.getData()) {
                    if (data.use == use) {
                        hasUse = true;
                        break;
                    }
                }

                if (hasUse) {
                    final JPanel usePane = new JPanel(new StackedBorderLayout());
                    final String useName = use.name();
                    final JLabel useLbl = new JLabel(useName);
                    usePane.add(useLbl, StackedBorderLayout.NORTH);

                    final JCheckBox check = new JCheckBox();
                    check.addActionListener(listener);
                    useCheckboxes.put(use, check);
                    usePane.add(check, StackedBorderLayout.SOUTH);
                }
            }
        }
    }

    /**
     * Gets the list of selected database uses.
     *
     * @param databaseUses a list to populate with the selected database uses
     */
    void getSelectedDatabaseUses(final Collection<? super DatabaseUse> databaseUses) {

        for (final Map.Entry<Database, Map<EDbUse, JCheckBox>>  databaseEntry : this.checkboxes.entrySet()) {
            final Database database = databaseEntry.getKey();
            final Map<EDbUse, JCheckBox> useMap = databaseEntry.getValue();

            for (final Map.Entry<EDbUse, JCheckBox> entry : useMap.entrySet()) {
                final JCheckBox checkbox = entry.getValue();
                if (checkbox.isSelected()) {
                    final EDbUse use = entry.getKey();
                    final DatabaseUse rec = new DatabaseUse(database, use);
                    databaseUses.add(rec);
                }
            }
        }
    }

    /**
     * Called when the "Connect" button is activated.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        Database toConnect = null;

        final String dbId = e.getActionCommand();
        for (final Database db : this.server.getDatabases()) {
            if (db.id.equals(dbId)) {
                toConnect = db;
                break;
            }
        }

        if (toConnect != null) {
            Log.info("Connecting to ", dbId);
            for (final Login login : toConnect.getLogins()) {

            }
        }
    }
}

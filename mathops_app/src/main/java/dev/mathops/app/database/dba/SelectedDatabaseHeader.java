package dev.mathops.app.database.dba;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbUse;
import dev.mathops.db.cfg.Data;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.Login;
import jwabbit.FileLoader;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A pane to represent a selected database, login, and use in a multi-database window for a specific schema and table.
 */
final class SelectedDatabaseHeader extends JPanel implements ActionListener {

    /** An action command. */
    private static final String STATS_CMD = "STATS";

    /** The database, login, and use. */
    private final DatabaseUse databaseUse;

    /** The schema and table. */
    private final SchemaTable schemaTable;

    /** The qualified table name. */
    private final String qualifiedTableName;

    /** Display for the row count. */
    private final JLabel rowCount;

    /**
     * Constructs a new {@code DatabaseIcon}
     *
     * @param theDatabaseUse the database, login, and use this tile represents
     * @param theSchemaTable the schema and table being examined
     */
    SelectedDatabaseHeader(final DatabaseUse theDatabaseUse, final SchemaTable theSchemaTable) {

        super(new StackedBorderLayout());

        this.databaseUse = theDatabaseUse;
        this.schemaTable = theSchemaTable;

        final Database database = theDatabaseUse.database();
        final Login login = theDatabaseUse.login();
        final EDbUse use = theDatabaseUse.use();

        // Find the "Data" object that represents the selected schema and use
        Data data = null;
        for (final Data test : database.getData()) {
            if (test.schema == theSchemaTable.schema() && test.use == theDatabaseUse.use()) {
                data = test;
                break;
            }
        }

        String qualifiedName = CoreConstants.EMPTY;

        if (data == null) {
            final String[] msg = {"Unable to locate Data object that provides this database table"};
            JOptionPane.showMessageDialog(this, msg, "Select Database", JOptionPane.ERROR_MESSAGE);
        } else {
            final DbConnection conn = login.checkOutConnection();
            try {
                final Connection jdbc = conn.getConnection();
                final DatabaseMetaData meta = jdbc.getMetaData();
                final String whichTable = theSchemaTable.table();
                final ResultSet rs = meta.getTables(null, data.prefix, whichTable, null);
                if (rs.next()) {
                    final String schema = rs.getString("TABLE_SCHEM");
                    final String table = rs.getString("TABLE_NAME");
                    qualifiedName = schema == null || schema.isBlank() ? table : (schema + "." + table);
                } else {
                    final String[] msg = {"Unable to locate the associated table in database"};
                    JOptionPane.showMessageDialog(this, msg, "Select Database", JOptionPane.ERROR_MESSAGE);
                }
            } catch (final SQLException ex) {
                final String[] msg = {"Unable to access database table", ex.getLocalizedMessage()};
                JOptionPane.showMessageDialog(this, msg, "Select Database\"", JOptionPane.ERROR_MESSAGE);
            } finally {
                login.checkInConnection(conn);
            }
        }
        this.qualifiedTableName = qualifiedName;

        final Color background = getBackground();
        final boolean isLight = InterfaceUtils.isLight(background);
        final Color accent = InterfaceUtils.createAccentColor(background, isLight);

        final Border outline = BorderFactory.createLineBorder(accent);

        final Border pad = BorderFactory.createEmptyBorder(4, 4, 4, 4);
        final Border paddedBox = BorderFactory.createCompoundBorder(outline, pad);
        setBorder(paddedBox);

        final Color stampBg = isLight ? background.brighter() : background.darker();

        final JPanel stamp = new JPanel(new StackedBorderLayout());
        stamp.setBackground(stampBg);
        stamp.setBorder(outline);
        add(stamp, StackedBorderLayout.WEST);

        final Class<? extends SelectedDatabaseHeader> cls = getClass();
        final BufferedImage cassandraIcon = FileLoader.loadFileAsImage(cls, "cassandra.png", true);
        final BufferedImage informixIcon = FileLoader.loadFileAsImage(cls, "informix.png", true);
        final BufferedImage mysqlIcon = FileLoader.loadFileAsImage(cls, "mysql.png", true);
        final BufferedImage oracleIcon = FileLoader.loadFileAsImage(cls, "oracle.png", true);
        final BufferedImage postgresqlIcon = FileLoader.loadFileAsImage(cls, "postgresql.png", true);

        final Border topLine = BorderFactory.createMatteBorder(1, 0, 0, 0, accent);

        // Show the server

        final JLabel serverLabel = new JLabel("  Server  ");
        serverLabel.setHorizontalAlignment(SwingConstants.CENTER);
        stamp.add(serverLabel, StackedBorderLayout.NORTH);

        final String trimmedHost = database.server.host.replace(".colostate.edu", "");
        final JLabel hostPort = new JLabel("  " + trimmedHost + ":" + database.server.port + "  ");
        hostPort.setHorizontalAlignment(SwingConstants.CENTER);
        stamp.add(hostPort, StackedBorderLayout.NORTH);

        BufferedImage iconImg = null;
        switch (database.server.type) {
            case INFORMIX -> iconImg = informixIcon;
            case ORACLE -> iconImg = oracleIcon;
            case POSTGRESQL -> iconImg = postgresqlIcon;
            case MYSQL -> iconImg = mysqlIcon;
            case CASSANDRA -> iconImg = cassandraIcon;
        }

        if (iconImg != null) {
            final ImageIcon icon = new ImageIcon(iconImg);
            final JLabel lbl = new JLabel(icon);
            stamp.add(lbl, StackedBorderLayout.NORTH);
        }

        // Show the database and login

        final JPanel dbPane = new JPanel(new StackedBorderLayout());
        dbPane.setOpaque(false);
        dbPane.setBorder(topLine);
        stamp.add(dbPane, StackedBorderLayout.NORTH);

        final JLabel dbLabel = new JLabel("  Database  ");
        dbLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dbPane.add(dbLabel, StackedBorderLayout.NORTH);

        final String txt = database.instance == null ? database.id : (database.id + " (" + database.instance + ")");

        final JLabel idLabel = new JLabel("  " + txt + "  ");
        idLabel.setHorizontalAlignment(SwingConstants.CENTER);
        dbPane.add(idLabel, StackedBorderLayout.NORTH);

        final JPanel connectedAsFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 0));
        connectedAsFlow.setOpaque(false);
        final JLabel connectedAs = new JLabel("Connected as: " + login.user);
        connectedAsFlow.add(connectedAs);
        dbPane.add(connectedAsFlow, StackedBorderLayout.NORTH);

        // Show the use

        final JPanel useFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));
        useFlow.setOpaque(false);
        useFlow.setBorder(topLine);
        final String useName = use.name();
        final JLabel useLbl = new JLabel(useName);
        useFlow.add(useLbl);
        stamp.add(useFlow, StackedBorderLayout.NORTH);

        final JPanel information = new JPanel(new StackedBorderLayout());
        add(information, StackedBorderLayout.WEST);

        final JButton statsButton = new JButton("Count Rows...");
        statsButton.setActionCommand(STATS_CMD);
        statsButton.addActionListener(this);
        if (this.qualifiedTableName.isEmpty()) {
            statsButton.setEnabled(false);
        }
        final JPanel buttonFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 0));
        buttonFlow.add(statsButton);
        information.add(buttonFlow, StackedBorderLayout.NORTH);

        final JLabel spacer = new JLabel(CoreConstants.SPC);
        information.add(spacer, StackedBorderLayout.NORTH);

        final JPanel countFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 0));
        final JLabel lbl = new JLabel("Row Count:");
        this.rowCount = new JLabel("        ");
        countFlow.add(lbl);
        countFlow.add(this.rowCount);
        information.add(countFlow, StackedBorderLayout.NORTH);
    }

    /**
     * Called when the "statistics" method is invoked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (STATS_CMD.equals(cmd) && !this.qualifiedTableName.isEmpty()) {
            this.rowCount.setText(CoreConstants.SPC);
            final Login login = this.databaseUse.login();

            final DbConnection conn = login.checkOutConnection();
            try {
                countRows(conn);
            } catch (final SQLException ex) {
                Log.warning(ex);
                final String[] msg = {"There was an error executing the query:", ex.getLocalizedMessage()};
                JOptionPane.showMessageDialog(this, msg, "Query", JOptionPane.ERROR_MESSAGE);
            } finally {
                login.checkInConnection(conn);
            }
        }
    }

    /**
     * Performs a query and populates the results table.
     *
     * @param conn the database connection
     */
    private void countRows(final DbConnection conn) throws SQLException {

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + this.qualifiedTableName)) {

            if (rs.next()) {
                final int count = rs.getInt(1);
                final String countStr = Integer.toString(count);
                this.rowCount.setText(countStr);
            }
        }
    }
}

package dev.mathops.app.database.dbimport;

import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.cfg.DbConfig;
import dev.mathops.db.old.cfg.EDbUse;
import dev.mathops.db.old.cfg.LoginConfig;
import dev.mathops.db.old.cfg.ServerConfig;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A frame that lets the user select a PostgreSQL server, database, and login from those configured in "db-config.xml".
 */
final class PgDatabasePicker extends JFrame implements ActionListener {

    /** A zero-length server config array used to convert a list to an array. */
    private static final ServerConfig[] ZERO_LEN_SERVER_CONFIG_ARRAY = new ServerConfig[0];

    /** An action command. */
    private static final String SERVER_CMD = "SERVER";

    /** An action command. */
    private static final String DB_CMD = "DB";

    /** An action command. */
    private static final String LOGIN_CMD = "LOGIN";

    /** An action command. */
    private static final String OK_CMD = "OK";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** An action command. */
    private static final String[] PROD_DEV_TEST = {"Production", "Development", "Test"};

    /** The list of servers. */
    private final List<ServerConfig> servers;

    /** The {@code DbImport} process to invoke when the user makes a selection. */
    private final DbImport callback;

    /** The server picker. */
    private final JComboBox<ServerConfig> serverPicker;

    /** The data model for the database picker. */
    private final DefaultComboBoxModel<String> databaseModel;

    /** The database picker. */
    private final JComboBox<String> databasePicker;

    /** The data model for the login picker. */
    private final DefaultComboBoxModel<String> loginModel;

    /** The login picker. */
    private final JComboBox<String> loginPicker;

    /** The PROD/DEV/TEST picker. */
    private final JComboBox<String> schemaPicker;

    /** A checkbox to allow all existing tables to be dropped. */
    private final JCheckBox deleteExisting;

    /** The "OK" button. */
    private final JButton okButton;

    /**
     * Constructs a new {@code PgDatabasePicker}.  This should be called on the AWT event thread.
     *
     * @param theServers  the list of servers
     * @param theCallback the {@code DbImport} process to invoke when the user makes a selection
     */
    PgDatabasePicker(final List<ServerConfig> theServers, final DbImport theCallback) {

        super("Select PostgreSQL Database");

        this.servers = theServers;
        this.callback = theCallback;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        final JPanel content = new JPanel(new StackedBorderLayout());
        final Border emptyBorder = BorderFactory.createEmptyBorder(15, 15, 0, 15);
        content.setBorder(emptyBorder);
        setContentPane(content);

        final JLabel title = new JLabel("Choose a PostgreSQL database into which to import:   ");
        final Border padding = BorderFactory.createEmptyBorder(0, 0, 6, 0);
        title.setBorder(padding);
        final Font font = title.getFont();
        final int size = font.getSize();
        final Font larger = font.deriveFont((float) size * 1.3f);
        title.setFont(larger);
        content.add(title, StackedBorderLayout.NORTH);

        final JLabel[] labels = {new JLabel("Server:"), new JLabel("Database:"), new JLabel("Login:"),
                new JLabel("Schema:")};
        UIUtilities.makeLabelsSameSizeRightAligned(labels);

        final ServerConfig[] serverArray = theServers.toArray(ZERO_LEN_SERVER_CONFIG_ARRAY);
        this.serverPicker = new JComboBox<>(serverArray);
        this.serverPicker.setActionCommand(SERVER_CMD);

        this.databaseModel = new DefaultComboBoxModel<>();
        this.databasePicker = new JComboBox<>(this.databaseModel);
        this.databasePicker.setActionCommand(DB_CMD);

        this.loginModel = new DefaultComboBoxModel<>();
        this.loginPicker = new JComboBox<>(this.loginModel);
        this.loginPicker.setActionCommand(LOGIN_CMD);

        final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        row1.add(labels[0]);
        row1.add(this.serverPicker);
        content.add(row1, StackedBorderLayout.NORTH);

        final JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        row2.add(labels[1]);
        row2.add(this.databasePicker);
        content.add(row2, StackedBorderLayout.NORTH);

        final JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        row3.add(labels[2]);
        row3.add(this.loginPicker);
        content.add(row3, StackedBorderLayout.NORTH);

        this.schemaPicker = new JComboBox<>(PROD_DEV_TEST);
        this.schemaPicker.setSelectedIndex(1);

        final JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        row4.add(labels[3]);
        row4.add(this.schemaPicker);
        content.add(row4, StackedBorderLayout.NORTH);

        this.deleteExisting = new JCheckBox("Drop existing tables");
        final JPanel row5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        row5.add(this.deleteExisting);
        content.add(row5, StackedBorderLayout.NORTH);

        this.okButton = new JButton("Ok");
        this.okButton.setActionCommand(OK_CMD);
        final JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand(CANCEL_CMD);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttons.add(this.okButton);
        buttons.add(cancelButton);
        content.add(buttons, StackedBorderLayout.NORTH);

        this.serverPicker.addActionListener(this);
        this.databasePicker.addActionListener(this);
        this.loginPicker.addActionListener(this);
        this.okButton.addActionListener(this);
        cancelButton.addActionListener(this);

        if (this.servers.size() == 1) {
            final ServerConfig first = this.servers.getFirst();
            pickServer(first);
        }
    }

    /**
     * Called when an action invoked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (OK_CMD.equals(cmd)) {
            setVisible(false);
            dispose();

            final int serverIndex = this.serverPicker.getSelectedIndex();
            final int dbIndex = this.databasePicker.getSelectedIndex();
            final int loginIndex = this.loginPicker.getSelectedIndex();
            final boolean dropTables = this.deleteExisting.isSelected();

            if (serverIndex >= 0 && dbIndex >= 0 && loginIndex >= 0) {
                final ServerConfig server = this.servers.get(serverIndex);
                final DbConfig db = server.getDatabases().get(dbIndex);
                final LoginConfig login = db.getLogins().get(loginIndex);

                final int whichSchema = this.schemaPicker.getSelectedIndex();
                final EDbUse use = whichSchema == 0 ? EDbUse.PROD : (whichSchema == 1 ? EDbUse.DEV : EDbUse.TEST);

                if (dropTables) {
                    final String[] message = {"This will drop all existing tables in the database.",
                            "Do you want to proceed?"};
                    final int option = JOptionPane.showConfirmDialog(this, message, "Database Import",
                            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (option == JOptionPane.YES_OPTION) {
                        this.callback.databaseSelected(login, use, true);
                    }
                } else {
                    this.callback.databaseSelected(login, use, false);
                }
                setVisible(false);
                dispose();
            } else {
                enableButtons();
            }
        } else if (CANCEL_CMD.equals(cmd)) {
            setVisible(false);
            dispose();
        } else {
            if (SERVER_CMD.equals(cmd)) {
                this.databaseModel.removeAllElements();

                final int serverIndex = this.serverPicker.getSelectedIndex();
                if (serverIndex >= 0) {
                    final ServerConfig server = this.servers.get(serverIndex);
                    pickServer(server);
                }
            } else if (DB_CMD.equals(cmd)) {
                this.loginModel.removeAllElements();

                final int serverIndex = this.serverPicker.getSelectedIndex();
                if (serverIndex >= 0) {
                    final ServerConfig picked = this.servers.get(serverIndex);
                    final int dbIndex = this.databasePicker.getSelectedIndex();
                    if (dbIndex >= 0) {
                        final DbConfig db = picked.getDatabases().get(dbIndex);
                        pickDatabase(db);
                    }
                }
            }

            enableButtons();
        }
    }

    /**
     * Picks a server and populates the database picker.
     *
     * @param server the selected server
     */
    private void pickServer(final ServerConfig server) {
        final List<DbConfig> databases = server.getDatabases();

        final int size = databases.size();
        final Collection<String> ids = new ArrayList<>(size);
        for (final DbConfig database : databases) {
            ids.add(database.id);
        }
        this.databaseModel.addAll(ids);

        if (databases.size() == 1) {
            this.databasePicker.setSelectedIndex(0);
        }
    }

    /**
     * Picks a database and populates the login picker.
     *
     * @param database the selected server
     */
    private void pickDatabase(final DbConfig database) {
        final List<LoginConfig> logins = database.getLogins();

        final int size = logins.size();
        final Collection<String> ids = new ArrayList<>(size);
        for (final LoginConfig login : logins) {
            ids.add(login.id);
        }
        this.loginModel.addAll(ids);

        if (logins.size() == 1) {
            this.loginPicker.setSelectedIndex(0);
        }
    }

    /**
     * Enables (or disables) the "accept" button based on whether a server, database, and login have been selected.
     */
    private void enableButtons() {

        final int serverIndex = this.serverPicker.getSelectedIndex();
        final int dbIndex = this.databasePicker.getSelectedIndex();
        final int loginIndex = this.loginPicker.getSelectedIndex();

        this.okButton.setEnabled(serverIndex >= 0 && dbIndex >= 0 && loginIndex >= 0);
    }
}

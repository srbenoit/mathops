package dev.mathops.app.db.ui;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.ui.layout.StackedBorderLayout;
import dev.mathops.db.EDbInstallationType;
import dev.mathops.db.config.DatabaseConfig;
import dev.mathops.db.config.ESchemaType;
import dev.mathops.db.config.LoginConfig;
import dev.mathops.db.config.ServerConfig;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * A panel that presents the list of configured database servers and their configured logins, and allows the user
 * to edit these objects.
 *
 * <p>
 * Selecting a server that has a DBA profile lets the user open/activate a server administration frame for that server.
 * This panel allows all server logins to be verified.
 */
final class ServerConfigPanel extends JPanel implements ActionListener {

    /** A button action command. */
    private static final String DELETE_CMD = "DELETE";

    /** A button action command. */
    private static final String UPDATE_CMD = "UPDATE";

    /** A button action command. */
    private static final String ADD_DATA_LOGIN_CMD = "ADD_DATA_LOGIN";

    /** A button action command. */
    private static final String ADD_DBA_LOGIN_CMD = "ADD_DBA_LOGIN";

    /** The panel that owns this panel */
    private final ConfiguredServersPanel owner;

    /** The database configuration that contains the server configuration. */
    private final DatabaseConfig dbConfig;

    /** The server configuration this panel displays and edits. */
    private final ServerConfig server;

    /** The combo box to set the database type. */
    private final JComboBox<EDbInstallationType> typeCombo;

    /** The combo box to set the schema type. */
    private final JComboBox<ESchemaType> schemaCombo;

    /** The hostname field. */
    private final JTextField host;

    /** The port field. */
    private final JTextField port;

    /** The database ID field. */
    private final JTextField id;

    /** The DBA login username field. */
    private final JTextField dbaUser;

    /** The data login ID fields. */
    private final JTextField[] dataLoginIds;

    /** The data login username fields. */
    private final JTextField[] dataLoginUsers;

    /** The data login password fields. */
    private final JPasswordField[] dataLoginPasswords;

    /**
     * Constructs a new {@code ConfiguredServersPanel}.
     *
     * @param theOwner the owning {@code ConfiguredServersPanel}
     * @param theDbConfig the database configuration that owns the server configuration
     * @param theServer the server configuration this panel displays and edits
     */
    ServerConfigPanel(final ConfiguredServersPanel theOwner, final DatabaseConfig theDbConfig,
                      final ServerConfig theServer) {

        super(new StackedBorderLayout());

        if (theOwner == null) {
            throw new IllegalArgumentException("Owning ConfiguredServersPanel may not be null");
        }
        if (theDbConfig == null) {
            throw new IllegalArgumentException("Database configuration may not be null");
        }
        if (theServer == null) {
            throw new IllegalArgumentException("Server configuration may not be null");
        }

        this.owner = theOwner;
        this.dbConfig = theDbConfig;
        this.server = theServer;

        final Color bg = getBackground();
        final int bgr = bg.getRed();
        final int bgg = bg.getGreen();
        final int bgb = bg.getBlue();
        final int shade = bgr + bgg + bgb; // In [0 .. 768]

        final Color accent = shade < 384 ? bg.brighter() : bg.darker();
        final Border lineBorder = BorderFactory.createLineBorder(accent, 1);
        final Border titledBorder = BorderFactory.createTitledBorder(lineBorder, "Database Server");

        final Border padding = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        final Border compound = BorderFactory.createCompoundBorder(titledBorder, padding);
        setBorder(compound);

        final Color bg2 = shade < 384 ? bg.darker() : bg.brighter();
        final int bg2r = bg2.getRed();
        final int bg2g = bg2.getGreen();
        final int bg2b = bg2.getBlue();


        // Make the background change more subtle...
        final Color newBg = new Color((bgr + bg2r) >> 1, (bgg + bg2g) >> 1, (bgb + bg2b) >> 1);
        setBackground(newBg);

        final JPanel middle = new JPanel(new StackedBorderLayout());
        middle.setBackground(newBg);
        add(middle, StackedBorderLayout.CENTER);

        final JLabel[] lbls = new JLabel[4];
        lbls[0] = new JLabel("Type:");
        lbls[1] = new JLabel("      Schema:");
        lbls[2] = new JLabel("      Host/Port:");
        lbls[3] = new JLabel("      Database:");

        final Color lblColor = lbls[0].getForeground();
        final int lblR = lblColor.getRed();
        final int lblG = lblColor.getGreen();
        final Color newLblColor =  new Color(lblR, lblG, 128);
        for (final JLabel lbl : lbls) {
            lbl.setForeground(newLblColor);
        }

        final EDbInstallationType[] types = EDbInstallationType.values();
        this.typeCombo = new JComboBox<EDbInstallationType>(types);
        this.typeCombo.setSelectedItem(this.server.type);

        final ESchemaType[] schemas = ESchemaType.values();
        this.schemaCombo = new JComboBox<ESchemaType>(schemas);
        this.schemaCombo.setSelectedItem(this.server.schema);

        this.host = new JTextField(16);
        this.host.setText(this.server.host);
        this.port = new JTextField(3);
        this.port.setText(Integer.toString(this.server.port));
        this.id = new JTextField(19);
        this.id.setText(this.server.id);

        final JPanel row0 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        row0.setBackground(newBg);
        row0.add(lbls[0]);
        row0.add(this.typeCombo);
        row0.add(lbls[1]);
        row0.add(this.schemaCombo);
        row0.add(lbls[2]);
        row0.add(this.host);
        final JLabel colon = new JLabel(":");
        colon.setForeground(newLblColor);
        row0.add(colon);
        row0.add(this.port);
        row0.add(lbls[3]);
        row0.add(this.id);
        middle.add(row0, StackedBorderLayout.NORTH);

        final JPanel buttons = new JPanel(new StackedBorderLayout(0, 7));
        buttons.setBackground(newBg);
        buttons.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
        add(buttons, StackedBorderLayout.EAST);

        final JButton delete = new JButton("Delete");
        final Color btnColor = delete.getBackground();
        final int btnR = btnColor.getRed();
        final int btnB = btnColor.getBlue();
        final Color newBtnColor =  new Color(btnR, 128, btnB);
        delete.setBackground(newBtnColor);
        delete.setActionCommand(DELETE_CMD);
        delete.addActionListener(this);
        buttons.add(delete, StackedBorderLayout.SOUTH);

        final JButton update = new JButton("Update");
        update.setBackground(newBtnColor);
        update.setActionCommand(UPDATE_CMD);
        update.addActionListener(this);
        buttons.add(update, StackedBorderLayout.SOUTH);

        this.dbaUser = new JTextField(10);

        final JPanel dbaRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        dbaRow.setBackground(newBg);
        dbaRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, accent));

        if (server.dbaLogin == null) {
            final JButton addDba = new JButton("Configure the DBA login...");
            addDba.setBackground(newBtnColor);
            addDba.setActionCommand(ADD_DBA_LOGIN_CMD);
            addDba.addActionListener(this);
            dbaRow.add(addDba);
        } else {
            final JLabel dbaHeader = new JLabel("DBA login with username:");
            dbaHeader.setForeground(newLblColor);
            dbaRow.add(dbaHeader);
            dbaRow.add(this.dbaUser);
        }
        middle.add(dbaRow, StackedBorderLayout.NORTH);

        final List<LoginConfig> logins = this.server.getLogins();
        final int numLogins = logins.size();
        this.dataLoginIds = new JTextField[numLogins];
        this.dataLoginUsers = new JTextField[numLogins];
        this.dataLoginPasswords = new JPasswordField[numLogins];

        for (int i = 0; i < numLogins; ++i) {
            final LoginConfig cfg = logins.get(i);

            this.dataLoginIds[i] = new JTextField(7);
            this.dataLoginIds[i].setText(cfg.id);

            this.dataLoginUsers[i] = new JTextField(10);
            this.dataLoginUsers[i].setText(cfg.user);

            this.dataLoginPasswords[i] = new JPasswordField(7);
            this.dataLoginPasswords[i].setText(cfg.password);

            final JPanel loginRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
            loginRow.setBackground(newBg);
            loginRow.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, accent));

            final JLabel dataHeader = new JLabel("Data login:");
            dataHeader.setForeground(newLblColor);
            loginRow.add(dataHeader);
            loginRow.add(this.dataLoginIds[i]);
            final JLabel withUser = new JLabel("   with username:");
            withUser.setForeground(newLblColor);
            loginRow.add(withUser);
            loginRow.add(this.dataLoginUsers[i]);
            final JLabel andPwd = new JLabel("   and password:");
            andPwd.setForeground(newLblColor);
            loginRow.add(andPwd);
            loginRow.add(this.dataLoginPasswords[i]);
            middle.add(loginRow, StackedBorderLayout.NORTH);

            final JPanel addLoginRow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
            addLoginRow.setBackground(newBg);

            final JButton addLogin = new JButton("Add a Data login...");
            addLogin.setBackground(newBtnColor);
            addLogin.setActionCommand(ADD_DATA_LOGIN_CMD);
            addLogin.addActionListener(this);
            addLoginRow.add(addLogin);
            middle.add(addLoginRow, StackedBorderLayout.NORTH);
        }

//  <server type='PostgreSQL' schema='primary' host='localhost' port='5432' id='math'>
//    <dbalogin user='postgres'/>
//    <login id='PGMATH' user='math' password='foo'/>
//  </server>


    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("ServerConfigPanel{dbConfig=", this.dbConfig, ", server=", this.server, "}");
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (DELETE_CMD.equals(cmd)) {
            this.owner.deleteServer(this, this.server);
        }
    }
}

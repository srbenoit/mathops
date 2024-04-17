package dev.mathops.db.oldadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.DbConfig;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.EDbUse;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.cfg.LoginConfig;
import dev.mathops.db.old.cfg.SchemaConfig;
import dev.mathops.db.old.cfg.ServerConfig;
import dev.mathops.db.old.rawlogic.RawWhichDbLogic;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

/**
 * A login dialog shown when the program starts to log the user in.
 */
public final class LoginDialog extends JFrame implements ActionListener {

    /** A radio button to select the PROD database. */
    private final JRadioButton prod;

    /** The username field. */
    private final JTextField username;

    /** The password field. */
    private final JPasswordField password;

    /** An error message. */
    private final JLabel error;

    /** The "connect" button". */
    private final JButton connect;

    /**
     * Constructs a new {@code LoginDialog}.
     *
     * @param theUsername the username to use (null to prompt for user to enter a username)
     * @param thePassword the password to use (null to prompt for user to enter a password)
     */
    LoginDialog(final String theUsername, final String thePassword) {

        super("MATH ADMIN: LOGIN");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.prod = new JRadioButton("PROD");
        final JRadioButton dev = new JRadioButton("DEV");
        this.username =new JTextField(15);
        if (theUsername != null) {
            this.username.setText(theUsername);
        }
        this.password = new JPasswordField(15);
        if (thePassword != null) {
            this.password.setText(thePassword);
        }
        this.connect = new JButton("Connect");
        getRootPane().setDefaultButton(this.connect);

        final JPanel content = new JPanel(new StackedBorderLayout());
        final Border padding = BorderFactory.createEmptyBorder(20, 20, 20, 20);
        content.setBorder(padding);
        setContentPane(content);

        final ButtonGroup group = new ButtonGroup();
        group.add(this.prod);
        group.add(dev);
        this.prod.setSelected(true);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 3));
        flow1.add(this.prod);
        flow1.add(dev);
        content.add(flow1, StackedBorderLayout.NORTH);

        final JLabel[] labels = new JLabel[2];
        labels[0] = new JLabel("Username");
        labels[1] = new JLabel("Password");
        UIUtilities.makeLabelsSameSizeRightAligned(labels);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 3));
        flow2.add(labels[0]);
        flow2.add(this.username);
        content.add(flow2, StackedBorderLayout.NORTH);

        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 3));
        flow3.add(labels[1]);
        flow3.add(this.password);
        content.add(flow3, StackedBorderLayout.NORTH);

        final JPanel flow4 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 3));
        this.error = new JLabel(CoreConstants.SPC);
        this.error.setForeground(new Color(180, 0, 0));
        flow4.add(this.error);
        content.add(flow4, StackedBorderLayout.NORTH);

        final JPanel flow5 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 6));
        flow5.add(this.connect);
        content.add(flow5, StackedBorderLayout.NORTH);
    }

    /**
     * Displays the dialog.
     */
    void display() {

        this.connect.addActionListener(this);

        UIUtilities.packAndCenter(this);
        setVisible(true);
    }

    /**
     * Called when the "Connect" button is pressed.
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final boolean isProd = this.prod.isSelected();

        // Create the server configuration
        final int port = isProd ? 1900 : 1901;
        final String serverName = isProd ? "imp_prodtcp" : "imp_devtcp";
        final ServerConfig server = new ServerConfig(EDbProduct.INFORMIX, "baer.math.colostate.edu", port, serverName);

        // Create the "Primary" schema object
        final SchemaConfig schema = new SchemaConfig("CSU PRI", "dev.mathops.db.old.schema.csuprimary.Builder",
                ESchemaUse.PRIMARY);

        // Create the database configurations
        final DbConfig mathDb;
        if (isProd) {
            mathDb = new DbConfig(server, "math", EDbUse.PROD);
            mathDb.addSchema(schema);
            server.addDatabase(mathDb);
        } else {
            mathDb = new DbConfig(server, "math", EDbUse.DEV);
            mathDb.addSchema(schema);
            server.addDatabase(mathDb);
        }

        // Create a login configuration with the supplied credentials
        final String usernameStr = this.username.getText();
        final char[] passwordChars = this.password.getPassword();
        final String passwordStr = new String(passwordChars);
        final String loginName = isProd ? "IFX-PROD-PRI" : "IFX-DEV-PRI";
        final LoginConfig login = new LoginConfig(loginName, mathDb, usernameStr, passwordStr);

        final Map<ESchemaUse, DbContext> useMap = new EnumMap<>(ESchemaUse.class);

        final DbContext ctx = new DbContext(schema, login);
        useMap.put(ESchemaUse.PRIMARY, ctx);

        final DbProfile profile;
        if (this.prod.isSelected()) {
            profile = new DbProfile("PROD", useMap);
        } else {
            profile = new DbProfile("DEV", useMap);
        }

        try {
            Log.info("Connecting as '", usernameStr, "' with password '", passwordStr);

            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(profile, conn);

            try {
                RawWhichDbLogic.query(cache);
                setVisible(false);

                final MainWindow mainWindow = new MainWindow(cache);
                mainWindow.display();

                dispose();
            } catch (final SQLException ex) {
                this.error.setText("Unable to connect.");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }
}

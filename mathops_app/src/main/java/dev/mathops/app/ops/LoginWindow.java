package dev.mathops.app.ops;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.EDbUse;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.Data;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.DatabaseConfigXml;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.cfg.Facet;
import dev.mathops.db.cfg.Server;
import dev.mathops.text.parser.ParsingException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * A window where the user can choose a server host, and a database cluster (selections are persisted via the
 * preferences API), and then enters a username and password to connect to the server. Once a connection is created, the
 * databases in that cluster become available, and the main application window opens.
 */
final class LoginWindow implements Runnable, ActionListener {

    /** An action command. */
    private static final String LOGIN_CMD = "LOGIN";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** An action command. */
    private static final String ACCESS_TOKEN_CMD = "ACCESS_TOKEN";

    /** An empty array used to create other arrays. */
    private static final Database[] EMPTY_DB_CONFIG_ARR = new Database[0];

    /** The database configuration. */
    private final DatabaseConfig dbConfig;

    /** The initial password. */
    private final String initialPassword;

    /** The initial username. */
    private final String initialUsername;

    /** The Canvas access token. */
    private String accessToken;

    /** The frame. */
    private JFrame frame;

    /** A combo box from which to choose database. */
    private JComboBox<Database> dbCombo;

    /** The username. */
    private JTextField username;

    /** The password. */
    private JPasswordField password;

    /** An error message. */
    private JLabel error;

    /**
     * Constructs a new {@code LoginWindow}
     *
     * @param theInitialUsername the username to pre-populate (from command-line)
     * @param theInitialPassword the password to pre-populate (from command-line)
     * @param theAccessToken     the Canvas access token
     */
    LoginWindow(final String theInitialUsername, final String theInitialPassword, final String theAccessToken) {

        this.initialUsername = theInitialUsername;
        this.initialPassword = theInitialPassword;
        this.accessToken = theAccessToken;

        final String path = System.getProperty("user.dir");
        final File dir = new File(path);
        final File cfgFile = new File(dir, DatabaseConfigXml.FILENAME);

        DatabaseConfig theMap;
        if (cfgFile.exists()) {
            Log.info("Found '", DatabaseConfigXml.FILENAME, " ' in ", dir.getAbsolutePath());
            try {
                theMap = DatabaseConfigXml.load(dir);
            } catch (final IOException | ParsingException ex) {
                theMap = DatabaseConfig.getDefault();
                Log.warning(ex);
            }
        } else {
            theMap = DatabaseConfig.getDefault();
        }

        this.dbConfig = theMap;
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    @Override
    public void run() {

        final List<Database> toChooseFrom = new ArrayList<>(10);
        Database initial = null;

        final List<Server> servers = this.dbConfig.getServers();
        for (final Server server : servers) {
            if (server.type == EDbProduct.INFORMIX) {
                for (final Database db : server.getDatabases()) {

                    boolean hasProd = false;
                    boolean hasDev = false;
                    for (final Data data : db.getData()) {
                        if (data.schema == ESchema.LEGACY) {
                            if (data.use == EDbUse.PROD) {
                                hasProd = true;
                            } else if (data.use == EDbUse.DEV) {
                                hasDev = true;
                            }
                        }
                    }

                    if (hasProd) {
                        toChooseFrom.add(db);
                        // Default the database selection
                        initial = db;
                    } else if (hasDev) {
                        toChooseFrom.add(db);
                    }
                }
            }
        }

        Collections.sort(toChooseFrom);

        this.frame = new JFrame(Res.get(Res.TITLE));
        this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        this.frame.setContentPane(content);

        // NORTH: Header
        final JPanel north = new JPanel(new BorderLayout());
        north.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        final JLabel header = new JLabel(Res.get(Res.TITLE));
        final Font bigHeaderFont = header.getFont().deriveFont(18.0f);
        header.setFont(bigHeaderFont);
        header.setHorizontalAlignment(SwingConstants.CENTER);
        north.add(header, BorderLayout.CENTER);
        content.add(north, BorderLayout.PAGE_START);

        // CENTER: Fields
        final JPanel center = new JPanel();
        center.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        final LayoutManager box = new BoxLayout(center, BoxLayout.PAGE_AXIS);
        center.setLayout(box);

        final JLabel dbPickLbl = new JLabel(Res.get(Res.LOGIN_DB_FIELD_LBL));
        dbPickLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        final Font boldTextFont = header.getFont().deriveFont(Font.BOLD, 12.0f);
        dbPickLbl.setFont(boldTextFont);

        final JLabel usernameLbl =
                new JLabel(Res.get(Res.LOGIN_USER_FIELD_LBL));
        usernameLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        usernameLbl.setFont(boldTextFont);

        final JLabel passwordLbl =
                new JLabel(Res.get(Res.LOGIN_PWD_FIELD_LBL));
        passwordLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        passwordLbl.setFont(boldTextFont);

        final Dimension hostPickSize = dbPickLbl.getPreferredSize();
        final Dimension usernameSize = usernameLbl.getPreferredSize();
        final Dimension passwordSize = passwordLbl.getPreferredSize();
        final Dimension lblSize = new Dimension(
                Math.max(hostPickSize.width, Math.max(usernameSize.width, passwordSize.width)),
                hostPickSize.height);

        dbPickLbl.setPreferredSize(lblSize);
        usernameLbl.setPreferredSize(lblSize);
        passwordLbl.setPreferredSize(lblSize);

        final JPanel hostPick = new JPanel(new BorderLayout(10, 10));
        hostPick.add(dbPickLbl, BorderLayout.LINE_START);
        this.dbCombo = new JComboBox<>(toChooseFrom.toArray(EMPTY_DB_CONFIG_ARR));
        if (initial != null) {
            this.dbCombo.setSelectedItem(initial);
        }
        hostPick.add(this.dbCombo);
        center.add(hostPick);

        center.add(Box.createRigidArea(new Dimension(0, 24)));

        final JPanel usernamePanel = new JPanel(new BorderLayout(10, 10));
        usernamePanel.add(usernameLbl, BorderLayout.LINE_START);
        this.username = new JTextField(14);
        usernamePanel.add(this.username);
        usernamePanel.add(Box.createRigidArea(new Dimension(30, 1)), BorderLayout.LINE_END);
        center.add(usernamePanel);

        center.add(Box.createRigidArea(new Dimension(0, 6)));

        final JPanel passwordPanel = new JPanel(new BorderLayout(10, 10));
        passwordPanel.add(passwordLbl, BorderLayout.LINE_START);
        this.password = new JPasswordField(14);
        passwordPanel.add(this.password);
        passwordPanel.add(Box.createRigidArea(new Dimension(30, 1)), BorderLayout.LINE_END);
        center.add(passwordPanel);

        center.add(Box.createRigidArea(new Dimension(0, 12)));

        final JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        this.error = new JLabel(CoreConstants.SPC);
        this.error.setFont(boldTextFont);
        errorPanel.add(this.error);
        center.add(errorPanel);

        content.add(center, BorderLayout.CENTER);

        // SOUTH: Buttons
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));

        final JButton loginBtn = new JButton(Res.get(Res.LOGIN_LOGIN_BTN));
        final Font buttonFont = loginBtn.getFont().deriveFont(13.0f);
        loginBtn.setFont(buttonFont);
        loginBtn.setActionCommand(LOGIN_CMD);
        loginBtn.addActionListener(this);

        final JButton cancelBtn = new JButton(Res.get(Res.LOGIN_CANCEL_BTN));
        cancelBtn.setFont(buttonFont);
        cancelBtn.setActionCommand(CANCEL_CMD);
        cancelBtn.addActionListener(this);

        final JButton accessTokenBtn = new JButton(Res.get(Res.LOGIN_ACCESS_TOKEN_BTN));
        accessTokenBtn.setFont(buttonFont);
        accessTokenBtn.setActionCommand(ACCESS_TOKEN_CMD);
        accessTokenBtn.addActionListener(this);

        buttons.add(loginBtn);
        buttons.add(cancelBtn);
        buttons.add(accessTokenBtn);

        content.add(buttons, BorderLayout.PAGE_END);

        this.frame.getRootPane().setDefaultButton(loginBtn);

        this.frame.pack();

        final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice selected = env.getDefaultScreenDevice();

        final Rectangle bounds = selected.getDefaultConfiguration().getBounds();

        final Dimension size = this.frame.getSize();
        this.frame.setLocation(bounds.x + (bounds.width - size.width) / 2,
                bounds.y + (bounds.height - size.height) / 2);
        this.frame.setVisible(true);

        if (this.initialUsername != null) {
            this.username.setText(this.initialUsername);
        }
        if (this.initialPassword != null) {
            this.password.setText(this.initialPassword);
        }

        if (this.initialUsername == null) {
            this.username.requestFocus();
        } else if (this.initialPassword == null) {
            this.password.requestFocus();
        }
    }

    /**
     * Called on the AWT event dispatch thread when a button is pressed.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (LOGIN_CMD.equals(cmd)) {
            handleLogin();
        } else if (CANCEL_CMD.equals(cmd)) {
            this.frame.setVisible(false);
            this.frame.dispose();
        } else if (ACCESS_TOKEN_CMD.equals(cmd)) {
            handleAccessToken();
        }
    }

    /**
     * Handles the "Login" action.
     */
    private void handleLogin() {

        String err = null;
        this.error.setText(CoreConstants.SPC);

        int good = 0;

        final char[] p = this.password.getPassword();
        if (p == null || p.length == 0) {
            err = Res.get(Res.LOGIN_NO_PWD_ERR);
        } else {
            ++good;
        }

        final String u = this.username.getText();

        if (u == null || u.isEmpty()) {
            err = Res.get(Res.LOGIN_NO_USER_ERR);
        } else {
            ++good;
        }

        Data selectedData = null;

        final Database db = (Database) this.dbCombo.getSelectedItem();
        if (db == null) {
            err = Res.get(Res.LOGIN_NO_DB_ERR);
        } else {
            for (final Data dat : db.getData()) {
                if (dat.schema == ESchema.LEGACY) {
                    selectedData = dat;
                    ++good;
                    break;
                }
            }

            if (selectedData == null) {
                err = Res.get(Res.LOGIN_NO_SCHEMA_ERR);
            }
        }

        if (good == 3 && p != null) {
            final Profile batchProfile = this.dbConfig.getCodeProfile(Contexts.BATCH_PATH);

            final Facet odsFacet = batchProfile.getFacet(ESchema.ODS);
            final Facet liveFacet = batchProfile.getFacet(ESchema.LIVE);
            final Facet extrenFacet = batchProfile.getFacet(ESchema.EXTERN);
            final Facet analyticsFacet = batchProfile.getFacet(ESchema.ANALYTICS);
            final Facet legacyFacet = batchProfile.getFacet(ESchema.LEGACY);

            final Profile profile = new Profile("Operations");
            profile.addFacet(odsFacet);
            profile.addFacet(liveFacet);
            profile.addFacet(extrenFacet);
            profile.addFacet(analyticsFacet);

            final Login login = new Login(db, "ADMIN_LOGIN", u, new String(p));
            final Facet newFacet = new Facet(legacyFacet.data, login);
            profile.addFacet(newFacet);

            final Cache cache = new Cache(profile);
            final DbConnection conn = login.checkOutConnection();

            try {
                // The following throws exception if login credentials are invalid
                conn.getConnection();

                new MainWindow(u, newFacet, cache, liveFacet, accessToken).run();
                this.frame.setVisible(false);
                this.frame.dispose();
            } catch (final SQLException ex2) {
                Log.warning(ex2.getMessage());
                this.error.setText(Res.get(Res.LOGIN_BAD_LOGIN_ERR));
            } finally {
                login.checkInConnection(conn);
            }
        } else if (err != null) {
            this.error.setText(err);
        }
    }

    /**
     * Handles the "Access Token" action.
     */
    private void handleAccessToken() {

        final String[] message = {"This program integrates with the Canvas LMS.",
                "To do this, it requires an 'access token'.",
                " ",
                "Log in to your Canvas LMS, and click the [Account] icon on the left side.",
                "Under 'Approved Integrations' you will need a 'User-Generated' access token.",
                " ",
                "If such a token already exists, open it and [Regenerate Token].",
                "If not, create a new User-Defined token.",
                " ",
                "Paste the generated token below."};
        final String enteredToken = JOptionPane.showInputDialog(null, message, "Canvas LMS Integration",
                javax.swing.JOptionPane.QUESTION_MESSAGE);

        boolean badToken = enteredToken == null || enteredToken.isBlank();
        if (!badToken) {
            final Preferences prefs = Preferences.userNodeForPackage(Operations.class);
            prefs.put(Operations.ACCESS_TOKEN_KEY, enteredToken);
            this.accessToken = enteredToken;
        }
    }
}

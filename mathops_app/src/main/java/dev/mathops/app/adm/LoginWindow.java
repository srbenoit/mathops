package dev.mathops.app.adm;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbConfig;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.EDbInstallationType;
import dev.mathops.db.old.cfg.EDbUse;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.cfg.LoginConfig;
import dev.mathops.db.old.cfg.SchemaConfig;
import dev.mathops.db.old.cfg.ServerConfig;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
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
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * A window where the user can choose a server host, and a database cluster (selections are persisted via the
 * preferences API), and then enters a username and password to connect to the server. Once a connection is created, the
 * databases in that cluster become available, and the main application window opens.
 */
/* default */ class LoginWindow implements Runnable, ActionListener {

    /** An action command. */
    private static final String LOGIN_CMD = "LOGIN";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** An action command. */
    private static final String SETTINGS_CMD = "SETTINGS";

    /** A font test string. */
    private static final String TEST_STRING = "ABCDEFGHI abcdefghi /\\--|";

    /** Color to indicate an error. */
    private static final Color ERROR_COLOR = new Color(255, 240, 240);

    /** The database context map. */
    private final ContextMap map;

    /** The initial username. */
    private final String initialUsername;

    /** The initial password. */
    private final String initialPassword;

    /** The frame. */
    private JFrame frame;

    /** A combo box from which to choose database. */
    private JComboBox<EDbUse> schemaCombo;

    /** The username. */
    private JTextField username;

    /** The password. */
    private JPasswordField password;

    /** An error message. */
    private JLabel error;

    /** Font antialiasing selections. */
    private final JRadioButton[] radios;

    /** The content pane. */
    private final JPanel content;

    /** The settings button. */
    private JButton settingsBtn;

    /** A panel to set text antialiasing. */
    private final JPanel pickAntialias;

    /**
     * Constructs a new {@code LoginWindow}
     *
     * @param theInitialUsername the username to pre-populate (from command-line)
     * @param theInitialPassword the password to pre-populate (from command-line)
     */
    /* default */ LoginWindow(final String theInitialUsername, final String theInitialPassword) {

        this.initialUsername = theInitialUsername;
        this.initialPassword = theInitialPassword;
        this.radios = new JRadioButton[6];
        this.content = new JPanel(new BorderLayout());
        this.pickAntialias = new JPanel(new StackedBorderLayout(0, 0));

        final String path = System.getProperty("user.dir");
        final File dir = new File(path);
        final File cfgFile = new File(dir, "db-config.xml");

        ContextMap theMap;
        if (cfgFile.exists()) {
            try {
                theMap = ContextMap.load(dir);
            } catch (final ParsingException ex) {
                theMap = ContextMap.getDefaultInstance();
                Log.warning(ex);
            }
        } else {
            theMap = ContextMap.getDefaultInstance();
        }

        this.map = theMap;
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    @Override
    public void run() {

        int pref = -1;
        final Preferences prefs = Preferences.userNodeForPackage(getClass());
        if (prefs != null) {
            pref = prefs.getInt("antialias", -1);
        }

        final List<EDbUse> toChooseFrom = new ArrayList<>(10);
        toChooseFrom.add(EDbUse.PROD);
        toChooseFrom.add(EDbUse.DEV);

        if (this.map == null) {
            JOptionPane.showMessageDialog(null, "Failed to load database configuration");
        } else {
            this.frame = new JFrame(Res.get(Res.TITLE));
            this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            this.content.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            this.content.setBackground(Skin.OFF_WHITE_GRAY);
            this.frame.setContentPane(this.content);

            // NORTH: Header
            final JPanel north = new JPanel(new BorderLayout());
            north.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            north.setBackground(Skin.OFF_WHITE_GRAY);

            final JLabel header = new JLabel(Res.get(Res.TITLE));
            header.setFont(Skin.BIG_HEADER_18_FONT);
            header.setForeground(Skin.LABEL_COLOR);
            header.setHorizontalAlignment(SwingConstants.CENTER);
            north.add(header, BorderLayout.CENTER);
            this.content.add(north, BorderLayout.PAGE_START);

            // CENTER: Fields
            final JPanel center = new JPanel();
            center.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
            center.setBackground(Skin.OFF_WHITE_GRAY);
            final LayoutManager box = new BoxLayout(center, BoxLayout.PAGE_AXIS);
            center.setLayout(box);

            final JLabel schemaPickLbl =
                    new JLabel(Res.get(Res.LOGIN_SCHEMA_FIELD_LBL));
            schemaPickLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            schemaPickLbl.setFont(Skin.BOLD_12_FONT);

            final JLabel usernameLbl =
                    new JLabel(Res.get(Res.LOGIN_USER_FIELD_LBL));
            usernameLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            usernameLbl.setFont(Skin.BOLD_12_FONT);

            final JLabel passwordLbl =
                    new JLabel(Res.get(Res.LOGIN_PWD_FIELD_LBL));
            passwordLbl.setHorizontalAlignment(SwingConstants.RIGHT);
            passwordLbl.setFont(Skin.BOLD_12_FONT);

            final Dimension schemaPickSize = schemaPickLbl.getPreferredSize();
            final Dimension usernameSize = usernameLbl.getPreferredSize();
            final Dimension passwordSize = passwordLbl.getPreferredSize();
            final Dimension lblSize = new Dimension(
                    Math.max(schemaPickSize.width, Math.max(usernameSize.width, passwordSize.width)),
                    schemaPickSize.height);

            schemaPickLbl.setPreferredSize(lblSize);
            usernameLbl.setPreferredSize(lblSize);
            passwordLbl.setPreferredSize(lblSize);

            final JPanel schemaPick = new JPanel(new BorderLayout(10, 10));
            schemaPick.setBackground(Skin.OFF_WHITE_GRAY);
            schemaPick.add(schemaPickLbl, BorderLayout.LINE_START);
            this.schemaCombo = new JComboBox<>(toChooseFrom.toArray(new EDbUse[0]));
            this.schemaCombo.setBackground(Color.WHITE);
            this.schemaCombo.setSelectedItem(EDbUse.PROD);
            schemaPick.add(this.schemaCombo);
            center.add(schemaPick);

            center.add(Box.createRigidArea(new Dimension(0, 24)));

            final JPanel usernamePanel = new JPanel(new BorderLayout(10, 10));
            usernamePanel.setBackground(Skin.OFF_WHITE_GRAY);
            usernamePanel.add(usernameLbl, BorderLayout.LINE_START);
            this.username = new JTextField(14);
            this.username.setBackground(Color.WHITE);
            usernamePanel.add(this.username);
            usernamePanel.add(Box.createRigidArea(new Dimension(30, 1)), BorderLayout.LINE_END);
            center.add(usernamePanel);

            center.add(Box.createRigidArea(new Dimension(0, 6)));

            final JPanel passwordPanel = new JPanel(new BorderLayout(10, 10));
            passwordPanel.setBackground(Skin.OFF_WHITE_GRAY);
            passwordPanel.add(passwordLbl, BorderLayout.LINE_START);
            this.password = new JPasswordField(14);
            this.password.setBackground(Color.WHITE);
            passwordPanel.add(this.password);
            passwordPanel.add(Box.createRigidArea(new Dimension(30, 1)), BorderLayout.LINE_END);
            center.add(passwordPanel);

            center.add(Box.createRigidArea(new Dimension(0, 12)));

            final JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            errorPanel.setBackground(Skin.OFF_WHITE_GRAY);
            this.error = new JLabel(CoreConstants.SPC);
            this.error.setFont(Skin.BOLD_12_FONT);
            this.error.setForeground(Skin.ERROR_COLOR);
            errorPanel.add(this.error);
            center.add(errorPanel);

            this.content.add(center, BorderLayout.CENTER);

            // SOUTH: Buttons
            final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
            buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
            buttons.setBackground(Skin.OFF_WHITE_GRAY);

            final JButton loginBtn = new JButton(Res.get(Res.LOGIN_LOGIN_BTN));
            loginBtn.setFont(Skin.BUTTON_13_FONT);
            loginBtn.setActionCommand(LOGIN_CMD);
            loginBtn.addActionListener(this);

            final JButton cancelBtn = new JButton(Res.get(Res.LOGIN_CANCEL_BTN));
            cancelBtn.setFont(Skin.BUTTON_13_FONT);
            cancelBtn.setActionCommand(CANCEL_CMD);
            cancelBtn.addActionListener(this);

            this.settingsBtn = new JButton(Res.get(Res.LOGIN_SETTINGS_BTN));
            this.settingsBtn.setFont(Skin.BUTTON_13_FONT);
            this.settingsBtn.setActionCommand(SETTINGS_CMD);
            this.settingsBtn.addActionListener(this);

            buttons.add(loginBtn);
            buttons.add(cancelBtn);
            buttons.add(this.settingsBtn);
            this.content.add(buttons, BorderLayout.PAGE_END);

            //

            this.pickAntialias.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5),
                            BorderFactory.createEtchedBorder()),
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            this.pickAntialias.setBackground(Skin.OFF_WHITE_GRAY);

            final JLabel antialiasHeader = new JLabel("Select the item below with the clearest text: ");
            this.pickAntialias.add(antialiasHeader, BorderLayout.NORTH);

            final Font tiny = new Font(Font.DIALOG, Font.PLAIN, 8);

            final JLabel lbl1 = new JLabel("No anti-aliasing");
            final JLabel lbl1b = new JLabel(TEST_STRING);
            lbl1b.setFont(tiny);

            final JLabel lbl2 = new JLabel("Basic anti-aliasing");
            final JLabel lbl2b = new JLabel(TEST_STRING);
            lbl2b.setFont(tiny);

            final JLabel lbl3 = new JLabel("HBGR anti-aliasing");
            final JLabel lbl3b = new JLabel(TEST_STRING);
            lbl3b.setFont(tiny);

            final JLabel lbl4 = new JLabel("HRGB anti-aliasing");
            final JLabel lbl4b = new JLabel(TEST_STRING);
            lbl4b.setFont(tiny);

            final JLabel lbl5 = new JLabel("VBGR anti-aliasing");
            final JLabel lbl5b = new JLabel(TEST_STRING);
            lbl5b.setFont(tiny);

            final JLabel lbl6 = new JLabel("VRGB anti-aliasing");
            final JLabel lbl6b = new JLabel(TEST_STRING);
            lbl6b.setFont(tiny);

            final ButtonGroup group = new ButtonGroup();

            final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
            flow1.setBackground(Skin.OFF_WHITE_GRAY);
            this.radios[0] = new JRadioButton();
            group.add(this.radios[0]);
            flow1.add(this.radios[0]);
            flow1.add(lbl1);
            flow1.add(lbl1b);

            final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
            flow2.setBackground(Skin.OFF_WHITE_GRAY);
            this.radios[1] = new JRadioButton();
            group.add(this.radios[1]);
            flow2.add(this.radios[1]);
            flow2.add(lbl2);
            flow2.add(lbl2b);

            final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
            flow3.setBackground(Skin.OFF_WHITE_GRAY);
            this.radios[2] = new JRadioButton();
            group.add(this.radios[2]);
            flow3.add(this.radios[2]);
            flow3.add(lbl3);
            flow3.add(lbl3b);

            final JPanel flow4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
            flow4.setBackground(Skin.OFF_WHITE_GRAY);
            this.radios[3] = new JRadioButton();
            group.add(this.radios[3]);
            flow4.add(this.radios[3]);
            flow4.add(lbl4);
            flow4.add(lbl4b);

            final JPanel flow5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
            flow5.setBackground(Skin.OFF_WHITE_GRAY);
            this.radios[4] = new JRadioButton();
            group.add(this.radios[4]);
            flow5.add(this.radios[4]);
            flow5.add(lbl5);
            flow5.add(lbl5b);

            final JPanel flow6 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
            flow6.setBackground(Skin.OFF_WHITE_GRAY);
            this.radios[5] = new JRadioButton();
            group.add(this.radios[5]);
            flow6.add(this.radios[5]);
            flow6.add(lbl6);
            flow6.add(lbl6b);

            this.pickAntialias.add(flow1, BorderLayout.PAGE_START);
            this.pickAntialias.add(flow2, BorderLayout.PAGE_START);
            this.pickAntialias.add(flow3, BorderLayout.PAGE_START);
            this.pickAntialias.add(flow4, BorderLayout.PAGE_START);
            this.pickAntialias.add(flow5, BorderLayout.PAGE_START);
            this.pickAntialias.add(flow6, BorderLayout.PAGE_START);

            if (pref >= 0 && pref <= 5) {
                this.radios[pref].setSelected(true);
            } else if (pref == -1) {
                this.radios[2].setSelected(true);
            }

            // content.add(this.pickAntialias, BorderLayout.EAST);

            //

            this.frame.getRootPane().setDefaultButton(loginBtn);
            this.frame.pack();

            final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            final GraphicsDevice selected = env.getDefaultScreenDevice();

            // final GraphicsDevice[] devs = env.getScreenDevices();
            // if (devs.length > 1) {
            // for (GraphicsDevice test : devs) {
            // if (test != selected) {
            // selected = test;
            // break;
            // }
            // }
            // }

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
    }

    /**
     * Called on the AWT event dispatch thread when a button is pressed.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();
        String err = null;

        if (LOGIN_CMD.equals(cmd)) {

            this.error.setText(CoreConstants.SPC);

            int good = 0;

            final char[] p = this.password.getPassword();
            if (p == null || p.length == 0) {
                this.password.setBackground(new Color(255, 240, 240));
                err = Res.get(Res.LOGIN_NO_PWD_ERR);
            } else {
                this.password.setBackground(Color.WHITE);
                ++good;
            }

            final String u = this.username.getText();

            if (u == null || u.isEmpty()) {
                this.username.setBackground(new Color(255, 240, 240));
                err = Res.get(Res.LOGIN_NO_USER_ERR);
            } else {
                this.username.setBackground(Color.WHITE);
                ++good;
            }

            DbConfig ifxDb = null;
            DbConfig pgDb = null;

            SchemaConfig ifxSchema = null;
            SchemaConfig pgSchema = null;

            final EDbUse dbUse = (EDbUse) this.schemaCombo.getSelectedItem();

            final ServerConfig[] servers = this.map.getServers();

            if (dbUse == null) {
                this.schemaCombo.setBackground(ERROR_COLOR);
                err = Res.get(Res.LOGIN_NO_DB_ERR);
            } else {
                for (final ServerConfig cfg : servers) {
                    for (final DbConfig db : cfg.getDatabases()) {
                        if (db.id.startsWith("term")) {
                            continue;
                        }
                        if (db.use == dbUse) {
                            if (db.server.type == EDbInstallationType.INFORMIX) {
                                ifxDb = db;
                            } else if (db.server.type == EDbInstallationType.POSTGRESQL) {
                                pgDb = db;
                            }
                        }
                    }
                }

                if (ifxDb == null || pgDb == null) {
                    this.schemaCombo.setBackground(ERROR_COLOR);
                    err = Res.get(Res.LOGIN_NO_DB_ERR);
                } else {
                    for (final SchemaConfig sch : ifxDb.getSchemata()) {
                        if (sch.use == ESchemaUse.PRIMARY) {
                            ifxSchema = sch;
                        }
                    }
                    for (final SchemaConfig sch : pgDb.getSchemata()) {
                        if (sch.use == ESchemaUse.PRIMARY) {
                            pgSchema = sch;
                        }
                    }

                    if (ifxSchema == null || pgSchema == null) {
                        this.schemaCombo.setBackground(ERROR_COLOR);
                        err = Res.get(Res.LOGIN_NO_SCHEMA_ERR);
                    } else {
                        this.schemaCombo.setBackground(Color.WHITE);
                        ++good;
                    }
                }
            }

            if (good == 3) {
                final String pwd = new String(p);
                final LoginConfig ifxLogin = new LoginConfig("ADM_I", ifxDb, u, pwd);
                final LoginConfig pgLogin = new LoginConfig("ADM_P", pgDb, u, pwd);
                final DbContext ifxCtx = new DbContext(ifxSchema, ifxLogin);
                final DbContext pgCtx = new DbContext(pgSchema, pgLogin);

                final DbProfile batchProfile = this.map.getCodeProfile(Contexts.BATCH_PATH);
                final DbContext odsContext = batchProfile.getDbContext(ESchemaUse.ODS);
                final DbContext liveContext = batchProfile.getDbContext(ESchemaUse.LIVE);

                final Map<ESchemaUse, DbContext> ifxContexts = new HashMap<>(10);
                ifxContexts.put(ESchemaUse.PRIMARY, ifxCtx);
                ifxContexts.put(ESchemaUse.ODS, odsContext);
                ifxContexts.put(ESchemaUse.LIVE, liveContext);

                final Map<ESchemaUse, DbContext> pgContexts = new HashMap<>(10);
                pgContexts.put(ESchemaUse.PRIMARY, pgCtx);
                pgContexts.put(ESchemaUse.ODS, odsContext);
                pgContexts.put(ESchemaUse.LIVE, liveContext);

                final DbProfile ifxProfile = new DbProfile("AdminIfx", ifxContexts);
                final DbProfile pgProfile = new DbProfile("AdminPg", pgContexts);

                try {
                    final DbConnection ifxConn = ifxCtx.checkOutConnection();

                    try {
                        final Cache ifxCache = new Cache(ifxProfile, ifxConn);

                        final DbConnection pgConn = pgCtx.checkOutConnection();
                        try {
                            // final Cache pgCache = new Cache(pgProfile, ifxConn);

                            // The following throws exception if login credentials are invalid
                            ifxConn.getConnection();
                            // pgConn.getConnection();

                            Object renderingHint = null;
                            int pref = -1;
                            if (this.radios[0].isSelected()) {
                                renderingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_OFF;
                                pref = 0;
                            } else if (this.radios[1].isSelected()) {
                                renderingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
                                pref = 1;
                            } else if (this.radios[2].isSelected()) {
                                renderingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR;
                                pref = 2;
                            } else if (this.radios[3].isSelected()) {
                                renderingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB;
                                pref = 3;
                            } else if (this.radios[4].isSelected()) {
                                renderingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VBGR;
                                pref = 4;
                            } else if (this.radios[5].isSelected()) {
                                renderingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB;
                                pref = 5;
                            }

                            final Preferences prefs = Preferences.userNodeForPackage(getClass());
                            if (prefs != null) {
                                prefs.putInt("antialias", pref);
                            }

                            // FIXME:
                            // new MainWindow(u, ifxCtx, pgCtx, ifxCache, pgCache, liveContext,
                            // renderingHint).run();
                            new AdminMainWindow(u, ifxCtx, pgCtx, ifxCache, null, liveContext,
                                    renderingHint).run();
                            this.frame.setVisible(false);
                            this.frame.dispose();
                        } finally {
                            pgCtx.checkInConnection(pgConn);
                        }
                    } catch (final SQLException ex2) {
                        Log.warning(ex2);
                        this.error.setText(Res.get(Res.LOGIN_BAD_LOGIN_ERR));
                    } finally {
                        ifxCtx.checkInConnection(ifxConn);
                    }
                } catch (final SQLException ex) {
                    Log.warning(ex.getMessage());
                    this.error.setText(Res.get(Res.LOGIN_CANT_CREATE_SCHEMA_ERR));
                }
            } else if (err != null) {
                this.error.setText(err);
            }
        } else if (CANCEL_CMD.equals(cmd)) {
            this.frame.setVisible(false);
            this.frame.dispose();
        } else if (SETTINGS_CMD.equals(cmd)) {
            this.settingsBtn.setEnabled(false);
            this.content.add(this.pickAntialias, BorderLayout.EAST);
            this.frame.pack();
        }
    }
}

package dev.mathops.app.adm;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.EDbProduct;
import dev.mathops.db.cfg.EDbUse;
import dev.mathops.db.schema.ESchema;
import dev.mathops.db.cfg.Data;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.cfg.Facet;
import dev.mathops.db.cfg.Server;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.sql.SQLException;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * A window where the user can choose a server host, and a database cluster (selections are persisted via the
 * preferences API), and then enters a username and password to connect to the server. Once a connection is created, the
 * databases in that cluster become available, and the main application window opens.
 */
final class LoginWindow extends JFrame implements ActionListener {

    /** An action command. */
    private static final String LOGIN_CMD = "LOGIN";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** An action command. */
    private static final String SETTINGS_CMD = "SETTINGS";

    /** A font test string. */
    private static final String TEST_STRING = "ABCDEFGHI abcdefghi /\\--|";

    /** An action command. */
    private static final String ANTIALIAS = "antialias";

    /** Color to indicate an error. */
    private static final Color ERROR_COLOR = new Color(255, 240, 240);

    /** The width of text fields. */
    private static final int FIELD_WIDTH = 14;

    /** The database configuration. */
    private final DatabaseConfig dbConfig;

    /** A combo box from which to choose between PROD and DEV. */
    private final JComboBox<EDbUse> useCombo;

    /** The username. */
    private final JTextField username;

    /** The password. */
    private final JPasswordField password;

    /** An error message. */
    private final JLabel error;

    /** Font antialiasing selections. */
    private final JRadioButton[] radios;

    /** The content pane. */
    private final JPanel content;

    /** The settings button. */
    private final JButton settingsBtn;

    /** A panel to set text antialiasing. */
    private final JPanel pickAntialias;

    /**
     * Constructs a new {@code LoginWindow}
     *
     * @param theDbConfig        the database configuration
     * @param theInitialUsername the username to pre-populate (from command-line)
     * @param theInitialPassword the password to pre-populate (from command-line)
     */
    LoginWindow(final DatabaseConfig theDbConfig, final String theInitialUsername, final String theInitialPassword) {

        super();

        this.dbConfig = theDbConfig;

        final String title = Res.get(Res.TITLE);
        setTitle(title);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.radios = new JRadioButton[6];
        this.content = new JPanel(new BorderLayout());
        this.pickAntialias = new JPanel(new StackedBorderLayout(0, 0));

        int pref = -1;
        final Class<? extends LoginWindow> cls = getClass();
        final Preferences prefs = Preferences.userNodeForPackage(cls);
        if (prefs != null) {
            pref = prefs.getInt(ANTIALIAS, -1);
        }

        final Border contentPadding = BorderFactory.createEmptyBorder(10, 20, 10, 20);
        this.content.setBorder(contentPadding);
        this.content.setBackground(Skin.OFF_WHITE_GRAY);
        setContentPane(this.content);

        // NORTH: Header
        final JPanel north = new JPanel(new BorderLayout());
        final MatteBorder underline = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);
        final Border northBorder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        final CompoundBorder paddedOutline = BorderFactory.createCompoundBorder(underline, northBorder);
        north.setBorder(paddedOutline);
        north.setBackground(Skin.OFF_WHITE_GRAY);

        final JLabel header = new JLabel(title);
        header.setFont(Skin.BIG_HEADER_18_FONT);
        header.setForeground(Skin.LABEL_COLOR);
        header.setHorizontalAlignment(SwingConstants.CENTER);
        north.add(header, BorderLayout.CENTER);
        this.content.add(north, BorderLayout.PAGE_START);

        // CENTER: Fields
        final JPanel center = new JPanel();
        final Border centerPadding = BorderFactory.createEmptyBorder(20, 10, 20, 10);
        center.setBorder(centerPadding);
        center.setBackground(Skin.OFF_WHITE_GRAY);
        final LayoutManager box = new BoxLayout(center, BoxLayout.PAGE_AXIS);
        center.setLayout(box);

        final String schemaLabelTxt = Res.get(Res.LOGIN_SCHEMA_FIELD_LBL);
        final JLabel schemaPickLbl = new JLabel(schemaLabelTxt);
        schemaPickLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        schemaPickLbl.setFont(Skin.BOLD_12_FONT);

        final String userLabelTxt = Res.get(Res.LOGIN_USER_FIELD_LBL);
        final JLabel usernameLbl = new JLabel(userLabelTxt);
        usernameLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        usernameLbl.setFont(Skin.BOLD_12_FONT);

        final String passwordLabelTxt = Res.get(Res.LOGIN_PWD_FIELD_LBL);
        final JLabel passwordLbl = new JLabel(passwordLabelTxt);
        passwordLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        passwordLbl.setFont(Skin.BOLD_12_FONT);

        final Dimension schemaPickSize = schemaPickLbl.getPreferredSize();
        final Dimension usernameSize = usernameLbl.getPreferredSize();
        final Dimension passwordSize = passwordLbl.getPreferredSize();
        final int max1 = Math.max(usernameSize.width, passwordSize.width);
        final int max2 = Math.max(schemaPickSize.width, max1);
        final Dimension lblSize = new Dimension(max2, schemaPickSize.height);

        schemaPickLbl.setPreferredSize(lblSize);
        usernameLbl.setPreferredSize(lblSize);
        passwordLbl.setPreferredSize(lblSize);

        final JPanel schemaPick = new JPanel(new BorderLayout(10, 10));
        schemaPick.setBackground(Skin.OFF_WHITE_GRAY);
        schemaPick.add(schemaPickLbl, BorderLayout.LINE_START);

        final EDbUse[] useArray = {EDbUse.PROD, EDbUse.DEV};
        this.useCombo = new JComboBox<>(useArray);
        this.useCombo.setBackground(Color.WHITE);
        this.useCombo.setSelectedItem(EDbUse.PROD);
        schemaPick.add(this.useCombo);
        center.add(schemaPick);

        final Component spacer24 = Box.createRigidArea(new Dimension(0, 24));
        center.add(spacer24);

        final JPanel usernamePanel = new JPanel(new BorderLayout(10, 10));
        usernamePanel.setBackground(Skin.OFF_WHITE_GRAY);
        usernamePanel.add(usernameLbl, BorderLayout.LINE_START);
        this.username = new JTextField(FIELD_WIDTH);
        this.username.setBackground(Color.WHITE);
        usernamePanel.add(this.username);
        final Component spacer30a = Box.createRigidArea(new Dimension(30, 1));
        usernamePanel.add(spacer30a, BorderLayout.LINE_END);
        center.add(usernamePanel);

        final Component spacer6 = Box.createRigidArea(new Dimension(0, 6));
        center.add(spacer6);

        final JPanel passwordPanel = new JPanel(new BorderLayout(10, 10));
        passwordPanel.setBackground(Skin.OFF_WHITE_GRAY);
        passwordPanel.add(passwordLbl, BorderLayout.LINE_START);
        this.password = new JPasswordField(FIELD_WIDTH);
        this.password.setBackground(Color.WHITE);
        passwordPanel.add(this.password);
        final Component spacer30b = Box.createRigidArea(new Dimension(30, 1));
        passwordPanel.add(spacer30b, BorderLayout.LINE_END);
        center.add(passwordPanel);

        final Component spacer12 = Box.createRigidArea(new Dimension(0, 12));
        center.add(spacer12);

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
        final MatteBorder overline = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY);
        buttons.setBorder(overline);
        buttons.setBackground(Skin.OFF_WHITE_GRAY);

        final String loginButtonTxt = Res.get(Res.LOGIN_LOGIN_BTN);
        final JButton loginBtn = new JButton(loginButtonTxt);
        loginBtn.setFont(Skin.BUTTON_13_FONT);
        loginBtn.setActionCommand(LOGIN_CMD);
        loginBtn.addActionListener(this);

        final String cancelButtonTxt = Res.get(Res.LOGIN_CANCEL_BTN);
        final JButton cancelBtn = new JButton(cancelButtonTxt);
        cancelBtn.setFont(Skin.BUTTON_13_FONT);
        cancelBtn.setActionCommand(CANCEL_CMD);
        cancelBtn.addActionListener(this);

        final String settingsButtonTxt = Res.get(Res.LOGIN_SETTINGS_BTN);
        this.settingsBtn = new JButton(settingsButtonTxt);
        this.settingsBtn.setFont(Skin.BUTTON_13_FONT);
        this.settingsBtn.setActionCommand(SETTINGS_CMD);
        this.settingsBtn.addActionListener(this);

        buttons.add(loginBtn);
        buttons.add(cancelBtn);
        buttons.add(this.settingsBtn);
        this.content.add(buttons, BorderLayout.PAGE_END);

        final Border etchedOutline = BorderFactory.createEtchedBorder();
        final Border antialiasPad = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        final CompoundBorder antialiasInner = BorderFactory.createCompoundBorder(antialiasPad, etchedOutline);
        final CompoundBorder antialiasOuter = BorderFactory.createCompoundBorder(antialiasInner, antialiasPad);
        this.pickAntialias.setBorder(antialiasOuter);
        this.pickAntialias.setBackground(Skin.OFF_WHITE_GRAY);

        final JLabel antialiasHeader = new JLabel("Select the item below with the clearest text: ");
        this.pickAntialias.add(antialiasHeader, StackedBorderLayout.NORTH);

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

        this.pickAntialias.add(flow1, StackedBorderLayout.NORTH);
        this.pickAntialias.add(flow2, StackedBorderLayout.NORTH);
        this.pickAntialias.add(flow3, StackedBorderLayout.NORTH);
        this.pickAntialias.add(flow4, StackedBorderLayout.NORTH);
        this.pickAntialias.add(flow5, StackedBorderLayout.NORTH);
        this.pickAntialias.add(flow6, StackedBorderLayout.NORTH);

        if (pref >= 0 && pref <= 5) {
            this.radios[pref].setSelected(true);
        } else if (pref == -1) {
            this.radios[2].setSelected(true);
        }

        getRootPane().setDefaultButton(loginBtn);
        pack();

        final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice selected = env.getDefaultScreenDevice();

        final Rectangle bounds = selected.getDefaultConfiguration().getBounds();

        final Dimension size = getSize();
        setLocation(bounds.x + (bounds.width - size.width) / 2, bounds.y + (bounds.height - size.height) / 2);

        if (theInitialUsername != null) {
            this.username.setText(theInitialUsername);
        }

        if (theInitialPassword != null) {
            this.password.setText(theInitialPassword);
        }

        if (theInitialUsername == null) {
            this.username.requestFocus();
        } else if (theInitialPassword == null) {
            this.password.requestFocus();
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
                this.password.setBackground(ERROR_COLOR);
                err = Res.get(Res.LOGIN_NO_PWD_ERR);
            } else {
                this.password.setBackground(Color.WHITE);
                ++good;
            }

            final String usr = this.username.getText();

            if (usr == null || usr.isEmpty()) {
                this.username.setBackground(ERROR_COLOR);
                err = Res.get(Res.LOGIN_NO_USER_ERR);
            } else {
                this.username.setBackground(Color.WHITE);
                ++good;
            }

            final EDbUse dbUse = (EDbUse) this.useCombo.getSelectedItem();

            if (dbUse == null) {
                this.useCombo.setBackground(ERROR_COLOR);
                err = Res.get(Res.LOGIN_NO_DB_ERR);
            } else if (dbUse == EDbUse.PROD) {
                ++good;
            } else if (dbUse == EDbUse.DEV) {
                ++good;
            } else {
                this.useCombo.setBackground(ERROR_COLOR);
                err = Res.get(Res.LOGIN_NO_DB_ERR);
            }

            if (good == 3) {
                final String pwd = new String(p);

                // Rather than pick a pre-made profile (that already has a password), we need to create a new
                // Login for the Informix database (PROD or DEV), then create a new profile that uses that Login.

                final Profile profile = new Profile("ADMIN_PROFILE");

                // Find login/data combinations for non-LEGACY schemas and add those to the profile
                boolean foundLive = false;
                boolean foundOds = false;

                Data systemData = null;
                Data mainData = null;
                Data externData = null;
                Data analyticData = null;
                Data termData = null;
                Data legacyData = null;

                for (final Server server : this.dbConfig.getServers()) {
                    for (final Database database : server.getDatabases()) {
                        for (final Data data : database.getData()) {
                            if (data.schema == ESchema.LIVE && data.use == EDbUse.LIVE) {
                                final List<Login> logins = database.getLogins();
                                final Login login = logins.getFirst();
                                final Facet facet = new Facet(data, login);
                                profile.addFacet(facet);
                                foundLive = true;
                            } else if (data.schema == ESchema.ODS && data.use == EDbUse.ODS) {
                                final List<Login> logins = database.getLogins();
                                final Login login = logins.getFirst();
                                final Facet facet = new Facet(data, login);
                                profile.addFacet(facet);
                                foundOds = true;
                            } else if (data.schema == ESchema.SYSTEM && data.use == dbUse
                                       && server.type == EDbProduct.POSTGRESQL) {
                                final List<Login> logins = database.getLogins();
                                final Login login = logins.getFirst();
                                final Facet facet = new Facet(data, login);
                                profile.addFacet(facet);
                                systemData = data;
                            } else if (data.schema == ESchema.MAIN && data.use == dbUse
                                       && server.type == EDbProduct.POSTGRESQL) {
                                final List<Login> logins = database.getLogins();
                                final Login login = logins.getFirst();
                                final Facet facet = new Facet(data, login);
                                profile.addFacet(facet);
                                mainData = data;
                            } else if (data.schema == ESchema.EXTERN && data.use == dbUse) {
                                final List<Login> logins = database.getLogins();
                                final Login login = logins.getFirst();
                                final Facet facet = new Facet(data, login);
                                profile.addFacet(facet);
                                externData = data;
                            } else if (data.schema == ESchema.ANALYTICS && data.use == dbUse
                                       && server.type == EDbProduct.POSTGRESQL) {
                                final List<Login> logins = database.getLogins();
                                final Login login = logins.getFirst();
                                final Facet facet = new Facet(data, login);
                                profile.addFacet(facet);
                                analyticData = data;
                            } else if (data.schema == ESchema.TERM && data.use == dbUse
                                       && server.type == EDbProduct.POSTGRESQL) {
                                final List<Login> logins = database.getLogins();
                                final Login login = logins.getFirst();
                                final Facet facet = new Facet(data, login);
                                profile.addFacet(facet);
                                termData = data;
                            } else if (data.schema == ESchema.LEGACY && data.use == dbUse
                                       && server.type == EDbProduct.INFORMIX) {
                                legacyData = data;
                            }
                        }
                    }
                }

                if (foundLive && foundOds && systemData != null && mainData != null && externData != null
                    && analyticData != null && termData != null && legacyData != null) {

                    // Create synthetic facet for Informix with the entered login credentials
//
                    final Login mainLogin = profile.getLogin(ESchema.MAIN);

                    final Login legacyLogin = new Login(legacyData.database, "LEGACY_LOGIN_ID", usr, pwd);
                    final Facet legacyFacet = new Facet(legacyData, legacyLogin);
                    profile.addFacet(legacyFacet);

                    try {
                        // Attempt to make a connection to see if login credentials are invalid
                        final DbConnection legacyConn = legacyLogin.checkOutConnection();
                        legacyLogin.checkInConnection(legacyConn);

                        final DbConnection mainConn = mainLogin.checkOutConnection();
                        mainLogin.checkInConnection(mainConn);

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

                        final Class<? extends LoginWindow> cls = getClass();
                        final Preferences prefs = Preferences.userNodeForPackage(cls);
                        if (prefs != null) {
                            prefs.putInt(ANTIALIAS, pref);
                        }

                        new MainWindow(usr, profile, renderingHint).setVisible(true);

                        setVisible(false);
                        dispose();
                    } catch (final SQLException ex2) {
                        Log.warning(ex2);
                        final String msg = Res.get(Res.LOGIN_BAD_LOGIN_ERR);
                        this.error.setText(msg);
                    }
                } else {
                    err = Res.get(Res.LOGIN_NO_SCHEMA_ERR);
                    this.error.setText(err);
                }
            } else if (err != null) {
                this.error.setText(err);
            }
        } else if (CANCEL_CMD.equals(cmd)) {
            setVisible(false);
            dispose();
        } else if (SETTINGS_CMD.equals(cmd)) {
            this.settingsBtn.setEnabled(false);
            this.content.add(this.pickAntialias, BorderLayout.LINE_END);
            pack();
        }
    }
}

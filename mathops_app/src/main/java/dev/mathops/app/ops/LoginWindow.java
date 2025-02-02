package dev.mathops.app.ops;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbConfig;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.EDbProduct;
import dev.mathops.db.EDbUse;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.cfg.LoginConfig;
import dev.mathops.db.old.cfg.SchemaConfig;
import dev.mathops.db.old.cfg.ServerConfig;
import dev.mathops.text.parser.ParsingException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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

    /** An empty array used to create other arrays. */
    private static final DbConfig[] EMPTY_DB_CONFIG_ARR = new DbConfig[0];

    /** The database context map. */
    private final ContextMap map;

    /** The initial username. */
    private final String initialUsername;

    /** The initial password. */
    private final String initialPassword;

    /** The frame. */
    private JFrame frame;

    /** A combo box from which to choose database. */
    private JComboBox<DbConfig> dbCombo;

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
     */
    LoginWindow(final String theInitialUsername, final String theInitialPassword) {

        this.initialUsername = theInitialUsername;
        this.initialPassword = theInitialPassword;

        final String path = System.getProperty("user.dir");
        final File dir = new File(path);
        final File cfgFile = new File(dir, "db-config.xml");

        ContextMap theMap;
        if (cfgFile.exists()) {
            Log.info("Found 'db-config.xml' in ", dir.getAbsolutePath());
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

        final List<DbConfig> toChooseFrom = new ArrayList<>(10);
        DbConfig initial = null;

        final ServerConfig[] servers = this.map.getServers();
        for (final ServerConfig cfg : servers) {
            for (final DbConfig db : cfg.getDatabases()) {
                if (db.id.startsWith("term")) {
                    continue;
                }

                final EDbUse use = db.use;

                if (db.server.type == EDbProduct.INFORMIX) {
                    if (use == EDbUse.DEV) {
                        toChooseFrom.add(db);
                    } else if (use == EDbUse.PROD) {
                        toChooseFrom.add(db);
                        // Default the database selection
                        initial = db;
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

        buttons.add(loginBtn);
        buttons.add(cancelBtn);
        content.add(buttons, BorderLayout.PAGE_END);

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

            SchemaConfig selectedSchema = null;

            final DbConfig db = (DbConfig) this.dbCombo.getSelectedItem();
            if (db == null) {
                err = Res.get(Res.LOGIN_NO_DB_ERR);
            } else {
                for (final SchemaConfig sch : db.getSchemata()) {
                    if (sch.use == ESchemaUse.PRIMARY) {
                        selectedSchema = sch;
                    }
                }

                if (selectedSchema == null) {
                    err = Res.get(Res.LOGIN_NO_SCHEMA_ERR);
                }
                ++good;
            }

            if (good == 3 && p != null) {
                final LoginConfig login = new LoginConfig("ADMIN_APP", db, u, new String(p));
                final DbContext ctx = new DbContext(selectedSchema, login);

                final DbProfile batchProfile = this.map.getCodeProfile(Contexts.BATCH_PATH);
                final DbContext odsContext = batchProfile.getDbContext(ESchemaUse.ODS);
                final DbContext liveContext = batchProfile.getDbContext(ESchemaUse.LIVE);

                final Map<ESchemaUse, DbContext> dbContexts = new EnumMap<>(ESchemaUse.class);
                dbContexts.put(ESchemaUse.PRIMARY, ctx);
                dbContexts.put(ESchemaUse.ODS, odsContext);
                dbContexts.put(ESchemaUse.LIVE, liveContext);

                final DbProfile profile = new DbProfile("Operations", dbContexts);

                try {
                    final DbConnection conn = ctx.checkOutConnection();
                    final Cache cache = new Cache(profile, conn);

                    try {
                        // The following throws exception if login credentials are invalid
                        conn.getConnection();

                        new MainWindow(u, ctx, cache, liveContext).run();
                        this.frame.setVisible(false);
                        this.frame.dispose();
                    } catch (final SQLException ex2) {
                        Log.warning(ex2.getMessage());
                        this.error.setText(Res.get(Res.LOGIN_BAD_LOGIN_ERR));
                        ctx.checkInConnection(conn);
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
        }
    }
}

package dev.mathops.app.database.dba;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbConfig;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.EDbUse;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.cfg.LoginConfig;
import dev.mathops.db.old.cfg.ServerConfig;
import dev.mathops.text.builder.HtmlBuilder;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * A frame that presents all the servers, databases, and logins configured in "db-config.xml" and allows the user to
 * select which to administer.
 */
final class DatabasePicker extends JFrame implements ActionListener {

    /** An action command. */
    private static final String OK_CMD = "OK";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** User preferences. */
    private final Preferences prefs;

    /** The context map. */
    private final ContextMap map;

    /** A map from checkbox ID to checkbox. */
    private final Map<String, JCheckBox> checkboxes;

    /** A map from radio button ID to radio button. */
    private final Map<String, JRadioButton> radioButtons;

    /** A map from radio button ID to the associated database profile. */
    private final Map<String, DbProfile> profiles;

    /** The "OK" button. */
    private final JButton okButton;

    /** The "Cancel" button. */
    private final JButton cancelButton;

    /**
     * Constructs a new {@code DatabasePicker}.  This should be called on the AWT event thread.
     *
     * @param theMap the context map
     */
    DatabasePicker(final ContextMap theMap) {

        super("Select Databases to Administer");

        final Class<? extends DatabasePicker> cls = getClass();
        this.prefs = Preferences.userNodeForPackage(cls);

        this.map = theMap;
        this.checkboxes = new HashMap<>(5);
        this.radioButtons = new HashMap<>(10);
        this.profiles = new HashMap<>(10);

        final DbProfile[] contextProfiles = theMap.getProfiles();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        final JPanel content = new JPanel(new StackedBorderLayout(8, 8));
        final Border emptyBorder = BorderFactory.createEmptyBorder(15, 15, 0, 15);
        content.setBorder(emptyBorder);
        setContentPane(content);

        final Color bg = content.getBackground();
        final boolean isLight = InterfaceUtils.isLight(bg);
        final Color highlightColor = InterfaceUtils.createHighlightColor(isLight);
        final Color accentColor = InterfaceUtils.createAccentColor(bg, isLight);

        final JLabel title = new JLabel("Select one or more databases to administer:            ");
        InterfaceUtils.resizeFont(title, 1.3);
        content.add(title, StackedBorderLayout.NORTH);

        final HtmlBuilder builder = new HtmlBuilder(100);

        final Border highlightLineBorder = BorderFactory.createLineBorder(highlightColor);
        final Border pad4Border = BorderFactory.createEmptyBorder(4, 4, 4, 4);
        final Border serverPadding = BorderFactory.createCompoundBorder(highlightLineBorder, pad4Border);

        final ServerConfig[] servers = theMap.getServers();
        for (final ServerConfig server : servers) {
            final JPanel serverBlock = new JPanel(new StackedBorderLayout(4, 4));
            serverBlock.setBackground(accentColor);
            serverBlock.setBorder(serverPadding);

            builder.reset();
            builder.add(server.type.name, " server '", server.name, "' on ", server.host, ":");
            builder.add(server.port);
            final String serverTitleStr = builder.toString();
            final JLabel serverTitle = new JLabel(serverTitleStr);
            serverBlock.add(serverTitle, StackedBorderLayout.NORTH);

            boolean addBlock = false;

            final List<DbConfig> databases = server.getDatabases();
            for (final DbConfig database : databases) {

                final EDbUse use = database.use;

                if (use == EDbUse.LIVE || use == EDbUse.ODS) {
                    continue;
                }

                final List<LoginConfig> dbLogins = database.getLogins();
                if (dbLogins.isEmpty()) {
                    continue;
                }

                builder.reset();
                builder.add("DB:", server.name, ".", database.id);
                final String checkboxId = builder.toString();

                final JPanel databaseFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
                databaseFlow.setBackground(accentColor);
                final JLabel databaseIndent = new JLabel("    ");
                databaseFlow.add(databaseIndent);

                final JCheckBox check = new JCheckBox();
                this.checkboxes.put(checkboxId, check);
                check.setActionCommand(checkboxId);
                databaseFlow.add(check);

                final boolean databaseChecked = this.prefs.getBoolean(checkboxId, false);
                check.setSelected(databaseChecked);

                builder.reset();
                builder.add(database.use.name(), " database '", database.id, "'");

                final String databaseTitleStr = builder.toString();
                final JLabel databaseTitle = new JLabel(databaseTitleStr);
                databaseFlow.add(databaseTitle);

                serverBlock.add(databaseFlow, StackedBorderLayout.NORTH);

                final ButtonGroup group = new ButtonGroup();
                for (final LoginConfig login : dbLogins) {

                    DbProfile profile = null;
                    for (final DbProfile testProfile : contextProfiles) {
                        final DbContext testContext = testProfile.getDbContext(ESchemaUse.PRIMARY);
                        if (testContext != null) {
                            final LoginConfig testLogin = testContext.getLoginConfig();
                            if (testLogin != null && testLogin.id.equals(login.id)) {
                                profile = testProfile;
                                break;
                            }
                        }
                    }

                    if (profile == null) {
                        Log.warning("Could not find a database profile with login ", login.id);
                        continue;
                    }

                    final JPanel loginFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
                    loginFlow.setBackground(accentColor);

                    final JLabel indent = new JLabel("        ");
                    loginFlow.add(indent);

                    builder.reset();
                    builder.add("L:", server.name, ".", database.id, ".", login.id);
                    final String radioId = builder.toString();

                    final JRadioButton radio = new JRadioButton();
                    this.radioButtons.put(radioId, radio);
                    this.profiles.put(radioId, profile);
                    radio.setActionCommand(radioId);
                    loginFlow.add(radio);
                    group.add(radio);

                    final boolean radioChecked = this.prefs.getBoolean(radioId, false);
                    radio.setSelected(radioChecked);

                    builder.reset();
                    builder.add("Login as '", login.user, "'");

                    final String loginTitleStr = builder.toString();
                    final JLabel loginTitle = new JLabel(loginTitleStr);
                    loginFlow.add(loginTitle);

                    serverBlock.add(loginFlow, StackedBorderLayout.NORTH);
                    addBlock = true;
                }
            }

            if (addBlock) {
                content.add(serverBlock, StackedBorderLayout.NORTH);
            }
        }

        this.okButton = new JButton("Ok");
        this.okButton.setEnabled(false);

        getRootPane().setDefaultButton(this.okButton);

        this.cancelButton = new JButton("Cancel");

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        buttons.add(this.okButton);
        buttons.add(this.cancelButton);
        content.add(buttons, StackedBorderLayout.NORTH);
        enableButtons();
    }

    /**
     * Initializes the picker.  This is separated from the constructor because it leaks references to this object which
     * is not completely constructed during the constructor.
     */
    void init() {

        for (final JCheckBox check : this.checkboxes.values()) {
            check.addActionListener(this);
        }

        this.okButton.setActionCommand(OK_CMD);
        this.cancelButton.setActionCommand(CANCEL_CMD);

        this.okButton.addActionListener(this);
        this.cancelButton.addActionListener(this);
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
            try {
                final String[] children = this.prefs.childrenNames();

                for (final String childName : children) {
                    if (childName.startsWith("DB:") || childName.startsWith("L:")) {
                        this.prefs.remove(childName);
                    }
                }

                for (final Map.Entry<String, JCheckBox> entry : this.checkboxes.entrySet()) {
                    final JCheckBox checkbox = entry.getValue();
                    if (checkbox.isSelected()) {
                        final String key = entry.getKey();
                        this.prefs.putBoolean(key, true);
                    }
                }
                for (final Map.Entry<String, JRadioButton> entry : this.radioButtons.entrySet()) {
                    final JRadioButton radio = entry.getValue();
                    if (radio.isSelected()) {
                        final String key = entry.getKey();
                        this.prefs.putBoolean(key, true);
                    }
                }

                this.prefs.flush();
            } catch (final BackingStoreException ex) {
                Log.warning("Failed to store preferences", ex);
            }

            setVisible(false);
            dispose();

            final List<DbProfile> selectedProfiles = new ArrayList<>(10);
            for (final Map.Entry<String, JRadioButton> entry : this.radioButtons.entrySet()) {
                final JRadioButton radio = entry.getValue();
                if (radio.isSelected()) {
                    final String key = entry.getKey();
                    final DbProfile profile = this.profiles.get(key);
                    if (profile != null) {
                        selectedProfiles.add(profile);
                    }
                }
            }

            final MainWindow main = new MainWindow(selectedProfiles);
            main.init();
            UIUtilities.packAndCenter(main);
            main.setVisible(true);

        } else if (CANCEL_CMD.equals(cmd)) {
            setVisible(false);
            dispose();
        } else {
            enableButtons();
        }
    }

    /**
     * Enables (or disables) the "accept" button based on whether a server, database, and login have been selected.
     */
    private void enableButtons() {

        boolean checked = false;

        for (final JCheckBox check : this.checkboxes.values()) {
            if (check.isSelected()) {
                checked = true;
                break;
            }
        }

        this.okButton.setEnabled(checked);
    }
}

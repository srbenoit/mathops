package dev.mathops.app.db.ui;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.parser.xml.XmlEscaper;
import dev.mathops.core.ui.layout.StackedBorderLayout;
import dev.mathops.db.config.DataProfile;
import dev.mathops.db.config.DatabaseConfig;
import dev.mathops.db.config.ESchemaType;
import dev.mathops.db.config.LoginConfig;
import dev.mathops.db.config.ServerConfig;
import dev.mathops.db.config.WebSiteContext;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * The main window.
 */
public final class MainWindow extends JFrame implements ActionListener {

    /** The initial width of the window's client area. */
    private static final int WIN_WIDTH = 1200;

    /** The initial height of the window's client area. */
    private static final int WIN_HEIGHT = 800;

    /** An action commands. */
    private static final String LOAD_ACTIVE_CMD = "LOAD_ACTIVE";

    /** An action commands. */
    private static final String SAVE_ACTIVE_CMD = "SAVE_ACTIVE";

    /** An action commands. */
    private static final String LOAD_NAMED_CMD = "LOAD_NAMED";

    /** An action commands. */
    private static final String SAVE_NAMED_CMD = "SAVE_NAMED";

    /** The servers panel. */
    private final ConfiguredServersPanel serversPanel;

    /** The profiles panel. */
    private final ConfiguredProfilesPanel profilesPanel;

    /** The contexts panel. */
    private final ConfiguredContextsPanel contextsPanel;

    /** The "Load Active Configuration" button. */
    private final JButton loadActiveButton;

    /** The "Save as Active Configuration" button. */
    private final JButton saveActiveButton;

    /** The "Load Named Configuration..." button. */
    private final JButton loadNamedButton;

    /** The "Save As Named Configuration..." button. */
    private final JButton saveNamedButton;

    /** The loaded database configuration. */
    private DatabaseConfig dbConfig;

    /**
     * Constructs a new {@code MainWindow}.
     */
    public MainWindow() {

        super(Res.get(Res.MAIN_WINDOW_TITLE));

        this.dbConfig = DatabaseConfig.getDefaultInstance();

        final JPanel content = new JPanel(new StackedBorderLayout());
        setContentPane(content);
        content.setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));

        final JTabbedPane tabs = new JTabbedPane();
        content.add(tabs, StackedBorderLayout.CENTER);

        // Present the list of configured database servers, DBA logins, and data logins
        this.serversPanel = new ConfiguredServersPanel();
        final String serverTabTitle = Res.get(Res.SERVER_TAB_TITLE);
        tabs.addTab(serverTabTitle, this.serversPanel);

        // Present the list of data profiles, which select logins for each needed schema
        this.profilesPanel = new ConfiguredProfilesPanel();
        final String profileTabTitle = Res.get(Res.PROFILE_TAB_TITLE);
        tabs.addTab(profileTabTitle, this.profilesPanel);

        // Then it has a list of web and code contexts, each selecting a profile.
        this.contextsPanel = new ConfiguredContextsPanel();
        final String contextTabTitle = Res.get(Res.CONTEXT_TAB_TITLE);
        tabs.addTab(contextTabTitle, this.contextsPanel);

        this.serversPanel.setConfig(this.dbConfig);
        this.profilesPanel.setConfig(this.dbConfig);
        this.contextsPanel.setConfig(this.dbConfig);

        // A "Save" button to write the [db_config.xml] file, a "Save As..." button to write a [db_config_(name).xml]
        // file to store a named config, and a "Load..." button to load a named config.

        final String loadActieLbl = Res.get(Res.LOAD_ACTIVE_BTN);
        this.loadActiveButton = new JButton(loadActieLbl);

        final String saveActiveLbl = Res.get(Res.SAVE_ACTIVE_BTN);
        this.saveActiveButton = new JButton(saveActiveLbl);

        final String loadNamedLbl = Res.get(Res.LOAD_NAMED_BTN);
        this.loadNamedButton = new JButton(loadNamedLbl);

        final String saveNamedLbl = Res.get(Res.SAVE_NAMED_BTN);
        this.saveNamedButton = new JButton(saveNamedLbl);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING, 20, 12));
        buttons.add(this.loadActiveButton);
        buttons.add(this.saveActiveButton);
        buttons.add(this.loadNamedButton);
        buttons.add(this.saveNamedButton);
        final MatteBorder border = BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY);
        buttons.setBorder(border);
        content.add(buttons, StackedBorderLayout.SOUTH);

        pack();
    }

    /**
     * Initializes the main window.  Call this after the object has been constructed.
     */
    public void init() {

        this.loadActiveButton.setActionCommand(LOAD_ACTIVE_CMD);
        this.loadActiveButton.addActionListener(this);

        this.saveActiveButton.setActionCommand(SAVE_ACTIVE_CMD);
        this.saveActiveButton.addActionListener(this);

        this.loadNamedButton.setActionCommand(LOAD_NAMED_CMD);
        this.loadNamedButton.addActionListener(this);

        this.saveNamedButton.setActionCommand(SAVE_NAMED_CMD);
        this.saveNamedButton.addActionListener(this);
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (LOAD_ACTIVE_CMD.equals(cmd)) {
            final DatabaseConfig newDbConfig = DatabaseConfig.getDefaultInstance();

            this.dbConfig = newDbConfig;
            this.serversPanel.setConfig(newDbConfig);
            this.profilesPanel.setConfig(newDbConfig);
            this.contextsPanel.setConfig(newDbConfig);
        } else if (SAVE_ACTIVE_CMD.equals(cmd)) {
            saveActiveConfig();
        } else if (LOAD_NAMED_CMD.equals(cmd)) {
            // TODO:
        } else if (SAVE_NAMED_CMD.equals(cmd)) {
            // TODO:
        }
    }

    /**
     * Saves the current configuration as the "active" configuration.
     */
    private void saveActiveConfig() {

        final HtmlBuilder newContent = new HtmlBuilder(1000);

        newContent.addln("<!-- Database configuration -->");
        newContent.addln();
        newContent.addln("<!--");
        newContent.addln("  - A \"database context\" configures one database connection (with server information and");
        newContent.addln("  - login credentials) for each of three separate schemas: \"Primary\", \"Live\", and");
        newContent.addln("  - \"Store\". The \"Primary\" schema has all local storage, and is the main schema for");
        newContent.addln("  - system operation. The \"Live\" schema is a University system that provides student and");
        newContent.addln("  - registration information, updated in real-time.  The \"Store\" schema is a data store");
        newContent.addln("  - of University data that is updated regularly but does not provide real-time data.  The");
        newContent.addln("  - \"Store\" database may be more efficient than the \"Live\" database, and there may be");
        newContent.addln("  - incentive to reduce unnecessary load on the \"Live\" system by accessing the \"Store\"");
        newContent.addln("  - system where it provides sufficient currency.  If a University does not have such a");
        newContent.addln("  - data store, the \"Store\" schema can be implemented on the \"Live\" system.");
        newContent.addln("  -");
        newContent.addln("  - This configuration information is stored in this XML file, to be read on startup of");
        newContent.addln("  - web servers or applications.  Obviously, this data cannot be stored in a database for");
        newContent.addln("  - which this file itself provides configuration.  It may be desirable to allow for");
        newContent.addln("  - web-based or remote administration of this file's content, but this operation involves");
        newContent.addln("  - risk that misconfiguration causes interruptions in service.  As such, tools that");
        newContent.addln("  - support remote configuration SHOULD have mechanisms for testing and verifying the");
        newContent.addln("  - configuration data in this file by attempting to connect to all configured databases");
        newContent.addln("  - and verifying internal consistency of configuration data before writing the file, and");
        newContent.addln("  - should probably archive backups of prior versions when changes are made.");
        newContent.addln("  -->");
        newContent.addln();
        newContent.addln("<database-config>");
        newContent.addln();
        newContent.addln("<!--");
        newContent.addln("  - Database server software installations.  Each has a host, a TCP port on which to");
        newContent.addln("  - connect, a server type, which can be used to select a driver within client software,");
        newContent.addln("  - and a database ID.");
        newContent.addln("  -");
        newContent.addln("  - Within each server installation, there may be at most one DBA login, used for");
        newContent.addln("  - administration, and any number of data logins, each providing a level of data access");
        newContent.addln("  - or manipulation permissions.");
        newContent.addln("  -->");

        final ServerConfig[] servers = this.dbConfig.getServers();
        for (final ServerConfig server : servers) {
            final String escType = XmlEscaper.escape(server.type.name);
            final String escSchema = XmlEscaper.escape(server.schema.name);
            final String escHost = XmlEscaper.escape(server.host);
            newContent.add("<server type='", escType, "' schema='", escSchema, "' host='", escHost, "' port='");
            newContent.add(server.port);
            newContent.add("'");
            if (server.id != null) {
                final String escId = XmlEscaper.escape(server.id);
                newContent.add(" id='", escId, "'");
            }
            newContent.addln(">");

            if (server.dbaLogin != null) {
                final String escUser = XmlEscaper.escape(server.dbaLogin.user);
                newContent.addln("  <dbalogin user='", escUser, "'/>");
            }

            for (final LoginConfig login : server.getLogins()) {
                final String escId = XmlEscaper.escape(login.id);
                final String escUser = XmlEscaper.escape(login.user);
                final String escPassword = XmlEscaper.escape(login.password);
                newContent.addln("  <login id='", escId, "' user='", escUser, "' password='", escPassword, "'/>");
            }

            newContent.addln("</server>");
            newContent.addln();
        }

        newContent.addln("<!--");
        newContent.addln("- Data profiles. Each named profile selects one login for each schema, where the login");
        newContent.addln("- implicitly selects the database server.");
        newContent.addln("-->");

        for (final String id : this.dbConfig.getDataProfileIDs()) {
            final DataProfile profile = this.dbConfig.getDataProfile(id);

            final String escId = XmlEscaper.escape(profile.id);

            newContent.addln("<data-profile id='", escId, "'>");

            for (final ESchemaType schemaType : ESchemaType.values()) {
                final LoginConfig schemaLogin = profile.getLogin(schemaType);
                final String escSchema = XmlEscaper.escape(schemaType.name);
                final String escLogin = XmlEscaper.escape(schemaLogin.id);
                newContent.addln("  <schema-login schema='", escSchema, "' login='", escLogin, "'/>");
            }
            newContent.addln("</data-profile>");
            newContent.addln();
        }

        newContent.addln("<!--");
        newContent.addln("- Access contexts.  Client software like applications, batch jobs, report generators, and");
        newContent.addln("- web sites define an access context, which selects a profile.  Access contexts can be");
        newContent.addln("- updated dynamically to allow web sites or applications to point to different databases");
        newContent.addln("- without having to change the core configurations of servers and data profiles.");
        newContent.addln("-");
        newContent.addln("- There are two types of access context: \"web\" and \"code\".  A web context allows");
        newContent.addln("- multiple website paths on a single host to be defined, each with a distinct profile.  A");
        newContent.addln("- code context simply associates a data profile with the context name that an application");
        newContent.addln("- uses.");
        newContent.addln("-->");

        for (final String host : this.dbConfig.getWebHosts()) {
            final String[] paths = this.dbConfig.getWebSites(host);
            if (paths != null) {
                final String escHost = XmlEscaper.escape(host);
                newContent.addln("<web host='", escHost, "'>");
                for (final String path : paths) {
                    final WebSiteContext profile = this.dbConfig.getWebSiteContext(host, path);
                    if (profile != null) {
                        final String escPath = XmlEscaper.escape(path);
                        final String escProfile = XmlEscaper.escape(profile.dataProfile.id);
                        newContent.addln("  <site path='", escPath, "' profile='", escProfile, "'/>");
                    }

                }
                newContent.addln("</web>");
                newContent.addln();
            }
        }

        for (final String code : this.dbConfig.getCodeContexts()) {
            final DataProfile codeProfile = this.dbConfig.getCodeContext(code);
            if (codeProfile != null) {
                final String escCpde = XmlEscaper.escape(code);
                final String escProfile = XmlEscaper.escape(codeProfile.id);
                newContent.addln("<code context='", escCpde, "'    profile='", escProfile, "'/>");
            }
        }

        newContent.addln("</database-config>");

        // TODO: Write the actual file
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("MainWindow{dbConfig=", this.dbConfig, "}");
    }
}

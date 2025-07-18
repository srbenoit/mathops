package dev.mathops.app.adm;

import dev.mathops.app.adm.instructor.TopPanelInstructor;
import dev.mathops.app.adm.management.TopPanelManagement;
import dev.mathops.app.adm.office.TopPanelOffice;
import dev.mathops.app.adm.resource.TopPanelResource;
import dev.mathops.app.adm.testing.TopPanelTesting;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.Data;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.cfg.Facet;
import dev.mathops.text.builder.HtmlBuilder;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.SQLException;

/**
 * The main window for the administration application.
 */
public final class MainWindow extends JFrame implements WindowListener, ChangeListener {

    /** The preferred size for panes. */
    public static final Dimension PREF_SIZE = new Dimension(1200, 800);

    /** The Informix data cache. */
    private final Cache cache;

    /** The server site URL to use when constructing a ScramClientStub. */
    private final String serverSiteUrl;

    /** The fixed data. */
    private final UserData fixed;

    /** The tabbed pane. */
    private JTabbedPane tabs = null;

    /** The office pane. */
    private TopPanelOffice officePane = null;

    /** The instructor pane. */
    private TopPanelInstructor instructorPane = null;

    /** The resource pane. */
    private TopPanelResource resourcePane = null;

    /** The testing pane. */
    private TopPanelTesting testingPane = null;

    /** The management pane. */
    private TopPanelManagement managementPane = null;

    /** The text render hint to use. */
    private final Object renderingHint;

    /**
     * Constructs a new {@code MainWindow}
     *
     * @param theUsername the username
     * @param theProfile  the database profile
     * @throws SQLException if there is an error accessing the database
     */
    MainWindow(final String theUsername, final Profile theProfile, final Object theRenderingHint) throws SQLException {

        // Called on the AWT event thread.

        super();

        this.cache = new Cache(theProfile);

        this.fixed = new UserData(this.cache, theUsername);
        this.renderingHint = theRenderingHint;

        if ("math".equals(theUsername)
            || "pattison".equals(theUsername)
            || "bromley".equals(theUsername)
            || "demoulpi".equals(theUsername)
            || "fadir".equals(theUsername)
            || "spdir".equals(theUsername)
            || "smdir".equals(theUsername)) {
            this.serverSiteUrl = "https://testing.math.colostate.edu/websvc/";
        } else {
            Log.warning("User not authorized to control testing stations remotely.");
            this.serverSiteUrl = null;
        }

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(this);

        final JPanel content = new JPanel(new BorderLayout());
        final Border padding = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        content.setBorder(padding);
        content.setBackground(Skin.WHITE);
        setContentPane(content);

        final Facet legacyFacet = theProfile.getFacet(ESchema.LEGACY);
        final Login legacyLogin = legacyFacet.login;
        final Data legacyData = legacyFacet.data;

        final String ifxDbName = legacyLogin.database.id;
        final String ifxServer = legacyLogin.database.server.host;
        final String username = this.fixed.username;

        final HtmlBuilder windowTitle = new HtmlBuilder(100);
        windowTitle.add("Connected to Informix [", ifxDbName, "] on [", ifxServer, "] (", legacyData.use, ")  as  [",
                username, "]");

        final String windowTitleStr = windowTitle.toString();
        setTitle(windowTitleStr);

        this.tabs = new JTabbedPane();
        this.tabs.setFont(Skin.TAB_15_FONT);
        this.tabs.setForeground(Skin.LABEL_COLOR);
        this.tabs.addChangeListener(this);
        this.tabs.setBackground(Skin.WHITE);
        content.add(this.tabs, BorderLayout.CENTER);

        if (this.fixed.getClearanceLevel("STU_MENU") != null) {
            this.officePane = new TopPanelOffice(this.cache, this.fixed);
            final String tabTitle = Res.get(Res.OFFICE_TAB);
            this.tabs.addTab(tabTitle, this.officePane);
        }

        if (this.fixed.getClearanceLevel("INS_MENU") != null) {
            this.instructorPane = new TopPanelInstructor(this.cache, this.fixed);
            final String tabTitle = Res.get(Res.INSTRUCTOR_TAB);
            this.tabs.addTab(tabTitle, this.instructorPane);
        }

        if (this.fixed.getClearanceLevel("RES_MENU") != null) {
            this.resourcePane = new TopPanelResource(this.cache, this.fixed);
            final String tabTitle = Res.get(Res.RESOURCE_TAB);
            this.tabs.addTab(tabTitle, this.resourcePane);
        }

        if (this.fixed.getClearanceLevel("TST_MENU") != null) {
            this.testingPane = new TopPanelTesting(this.cache, this.serverSiteUrl, this.fixed, this);
            final String tabTitle = Res.get(Res.TESTING_TAB);
            this.tabs.addTab(tabTitle, this.testingPane);
        }

        if (this.fixed.getClearanceLevel("MGT_MENU") != null) {
            this.managementPane = new TopPanelManagement(this.cache, this.renderingHint);
            final String tabTitle = Res.get(Res.MGT_TAB);
            this.tabs.addTab(tabTitle, this.managementPane);
        }

        if (this.tabs.getTabCount() == 0) {
            // No tabs to provide a preferred size
            this.tabs.setPreferredSize(PREF_SIZE);
        }

        pack();

        final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice selected = env.getDefaultScreenDevice();

        final Rectangle bounds = selected.getDefaultConfiguration().getBounds();

        final Dimension size = getSize();
        setLocation(bounds.x + (bounds.width - size.width) / 2, bounds.y + (bounds.height - size.height) / 2);

        if ("video".equals(this.fixed.username) && this.resourcePane != null) {
            this.resourcePane.focus();
        } else if (this.officePane != null) {
            this.officePane.focus();
        }
    }

    /**
     * Called when a tab is selected.
     *
     * @param e the change event
     */
    @Override
    public void stateChanged(final ChangeEvent e) {

        final Component comp = this.tabs.getSelectedComponent();

        if (comp == this.officePane) {
            this.officePane.focus();
        } else if (comp == this.resourcePane) {
            this.resourcePane.focus();
        } else if (comp == this.testingPane) {
            this.testingPane.focus();
        } else if (comp == this.managementPane) {
            this.managementPane.focus();
        }
    }

    /**
     * Called when the window is opened.
     *
     * @param e the window event
     */
    @Override
    public void windowOpened(final WindowEvent e) {

        // No action
    }

    /**
     * Called when the window is closing.
     *
     * @param e the window event
     */
    @Override
    public void windowClosing(final WindowEvent e) {

        final String title = Res.get(Res.TITLE);
        if (JOptionPane.showConfirmDialog(this, "Close the application?", title, JOptionPane.YES_NO_OPTION)
            == JOptionPane.YES_OPTION) {

            if (this.officePane != null) {
                this.officePane.clearDisplay();
            }
            if (this.resourcePane != null) {
                this.resourcePane.clearDisplay();
            }
            if (this.testingPane != null) {
                this.testingPane.clearDisplay();
            }
            if (this.managementPane != null) {
                this.managementPane.clearDisplay();
            }

            setVisible(false);
            dispose();
        }
    }

    /**
     * Called when the window is closed.
     *
     * @param e the window event
     */
    @Override
    public void windowClosed(final WindowEvent e) {

        // No action
    }

    /**
     * Called when the window is iconified.
     *
     * @param e the window event
     */
    @Override
    public void windowIconified(final WindowEvent e) {

        // No action
    }

    /**
     * Called when the window is deiconified.
     *
     * @param e the window event
     */
    @Override
    public void windowDeiconified(final WindowEvent e) {

        // No action
    }

    /**
     * Called when the window is activated.
     *
     * @param e the window event
     */
    @Override
    public void windowActivated(final WindowEvent e) {

        // No action
    }

    /**
     * Called when the window is deactivated.
     *
     * @param e the window event
     */
    @Override
    public void windowDeactivated(final WindowEvent e) {

        // No action
    }
}

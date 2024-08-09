package dev.mathops.app.adm;

import dev.mathops.app.adm.forms.TopPanelForms;
import dev.mathops.app.adm.management.TopPanelManagement;
import dev.mathops.app.adm.resource.TopPanelResource;
import dev.mathops.app.adm.office.TopPanelOffice;
import dev.mathops.app.adm.testing.TopPanelTesting;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.DbContext;

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

    /** The live database context. */
    private final DbContext liveContext;

    /** The Informix data cache. */
    private final Cache ifxCache;

    /** The server site URL to use when constructing a ScramClientStub. */
    private final String serverSiteUrl;

    /** The fixed data. */
    private final UserData fixed;

    /** The tabbed pane. */
    private JTabbedPane tabs = null;

    /** The admin pane. */
    private TopPanelOffice studentPane = null;

    /** The resource pane. */
    private TopPanelResource resourcePane = null;

    /** The testing pane. */
    private TopPanelTesting testingPane = null;

    /** The management pane. */
    private TopPanelManagement managementPane = null;

    /** The form pane. */
    private TopPanelForms formsPane = null;

    /** The text render hint to use. */
    private final Object renderingHint;

    /**
     * Constructs a new {@code MainWindow}
     *
     * @param theUsername    the username
     * @param theIfxContext  the Informix database context
     * @param theIfxCache    the Informix data cache
     * @param theLiveContext the live data database context
     * @throws SQLException if there is an error accessing the database
     */
    MainWindow(final String theUsername, final DbContext theIfxContext, final Cache theIfxCache,
               final DbContext theLiveContext, final Object theRenderingHint) throws SQLException {

        // Called on the AWT event thread.

        super();

        this.ifxCache = theIfxCache;
        this.liveContext = theLiveContext;

        this.fixed = new UserData(this.ifxCache, theUsername);
        this.renderingHint = theRenderingHint;

        if ("math".equals(theUsername)
                || "pattison".equals(theUsername)
                || "bromley".equals(theUsername)
                || "orchard".equals(theUsername)
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

        final String ifxDbName = theIfxContext.loginConfig.db.id;
        final String ifxServer = theIfxContext.loginConfig.db.server.name;
        final String username = this.fixed.username;

        final HtmlBuilder windowTitle = new HtmlBuilder(100);
        windowTitle.add("Connected to Informix [", ifxDbName, "] on [", ifxServer, "] (",
                theIfxContext.loginConfig.db.use, ")  as  [", username, "]");

        final String windowTitleStr = windowTitle.toString();
        setTitle(windowTitleStr);

        this.tabs = new JTabbedPane();
        this.tabs.setFont(Skin.TAB_15_FONT);
        this.tabs.setForeground(Skin.LABEL_COLOR);
        this.tabs.addChangeListener(this);
        this.tabs.setBackground(Skin.WHITE);
        content.add(this.tabs, BorderLayout.CENTER);
        if (this.fixed.getClearanceLevel("STU_MENU") != null) {
            this.studentPane = new TopPanelOffice(this.ifxCache, this.liveContext, this.fixed);
            final String tabTitle = Res.get(Res.OFFICE_TAB);
            this.tabs.addTab(tabTitle, this.studentPane);
        }

        if (this.fixed.getClearanceLevel("RES_MENU") != null) {
            this.resourcePane = new TopPanelResource(this.ifxCache, this.fixed);
            final String tabTitle = Res.get(Res.RESOURCE_TAB);
            this.tabs.addTab(tabTitle, this.resourcePane);
        }

        if (this.fixed.getClearanceLevel("TST_MENU") != null) {
            this.testingPane = new TopPanelTesting(this.ifxCache, this.serverSiteUrl, this.fixed, this);
            final String tabTitle = Res.get(Res.TESTING_TAB);
            this.tabs.addTab(tabTitle, this.testingPane);
        }

        if (this.fixed.getClearanceLevel("MGT_MENU") != null) {
            this.managementPane = new TopPanelManagement(this.ifxCache, this.renderingHint);
            final String tabTitle = Res.get(Res.MGT_TAB);
            this.tabs.addTab(tabTitle, this.managementPane);
        }

        if (this.fixed.getClearanceLevel("FRM_MENU") != null) {
            this.formsPane = new TopPanelForms(this.ifxCache);
            final String tabTitle = Res.get(Res.FORMS_TAB);
            this.tabs.addTab(tabTitle, this.formsPane);
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
        } else if (this.studentPane != null) {
            this.studentPane.focus();
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

        if (comp == this.studentPane) {
            this.studentPane.focus();
        } else if (comp == this.resourcePane) {
            this.resourcePane.focus();
        } else if (comp == this.testingPane) {
            this.testingPane.focus();
        } else if (comp == this.formsPane) {
            this.formsPane.focus();
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

            this.studentPane.clearDisplay();
            this.resourcePane.clearDisplay();
            this.testingPane.clearDisplay();
            this.managementPane.clearDisplay();
            this.formsPane.clearDisplay();

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
    public void windowDeactivated(WindowEvent e) {

        // No action
    }
}

package dev.mathops.app.adm;

import dev.mathops.app.adm.forms.TopPanelForms;
import dev.mathops.app.adm.management.TopPanelManagement;
import dev.mathops.app.adm.resource.TopPanelResource;
import dev.mathops.app.adm.student.TopPanelStudent;
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
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

/**
 * The main window for the administration application.
 */
public final class AdmMainWindow extends WindowAdapter implements Runnable, ChangeListener {

    /** The preferred size for panes. */
    public static final Dimension PREF_SIZE = new Dimension(1200, 800);

    /** The Informix database context. */
    private final DbContext ifxContext;

    /** The live database context. */
    private final DbContext liveContext;

    /** The Informix data cache. */
    private final Cache ifxCache;

    /** The server site URL to use when constructing a ScramClientStub. */
    private final String serverSiteUrl;

    /** The fixed data. */
    private final FixedData fixed;

    /** The frame. */
    private JFrame frame;

    /** The tabbed pane. */
    private JTabbedPane tabs;

    /** The admin pane. */
    private TopPanelStudent studentPane;

    /** The resource pane. */
    private TopPanelResource resourcePane;

    /** The testing pane. */
    private TopPanelTesting testingPane;

    /** The management pane. */
    private TopPanelManagement managementPane;

    /** The form pane. */
    private TopPanelForms formsPane;

    /** The text render hint to use. */
    private final Object renderingHint;

    /**
     * Constructs a new {@code MainWindow}
     *
     * @param theUsername      the username
     * @param theIfxContext    the Informix database context
     * @param theIfxCache      the Informix data cache
     * @param theLiveContext   the live data database context
     * @throws SQLException if there is an error accessing the database
     */
    AdmMainWindow(final String theUsername, final DbContext theIfxContext, final Cache theIfxCache,
                  final DbContext theLiveContext, final Object theRenderingHint) throws SQLException {

        super();

        this.ifxContext = theIfxContext;
        this.ifxCache = theIfxCache;
        this.liveContext = theLiveContext;

        this.fixed = new FixedData(this.ifxCache, theUsername);
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
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    @Override
    public void run() {

        final String title = Res.get(Res.TITLE);
        this.frame = new JFrame(title);
        this.frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.frame.addWindowListener(this);

        final JPanel content = new JPanel(new BorderLayout());
        final Border padding = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        content.setBorder(padding);
        content.setBackground(Skin.WHITE);
        this.frame.setContentPane(content);

        final String ifxDbName = this.ifxContext.loginConfig.db.id;
        final String ifxServer = this.ifxContext.loginConfig.db.server.name;
        final String username = this.fixed.username;

        final HtmlBuilder windowTitle = new HtmlBuilder(100);
        windowTitle.add("Connected to Informix [", ifxDbName, "] on [", ifxServer,
                "] (", this.ifxContext.loginConfig.db.use, ")  as  [", username, "]");

        final String windowTitleStr = windowTitle.toString();
        this.frame.setTitle(windowTitleStr);

        this.tabs = new JTabbedPane();
        this.tabs.setFont(Skin.TAB_15_FONT);
        this.tabs.setForeground(Skin.LABEL_COLOR);
        this.tabs.addChangeListener(this);
        this.tabs.setBackground(Skin.WHITE);
        content.add(this.tabs, BorderLayout.CENTER);
        if (this.fixed.getClearanceLevel("STU_MENU") != null) {
            this.studentPane = new TopPanelStudent(this.ifxCache, this.liveContext, this.fixed);
            final String tabTitle = Res.get(Res.STUDENT_TAB);
            this.tabs.addTab(tabTitle, this.studentPane);
        }

        if (this.fixed.getClearanceLevel("RES_MENU") != null) {
            this.resourcePane = new TopPanelResource(this.ifxCache, this.fixed);
            final String tabTitle = Res.get(Res.RESOURCES_TAB);
            this.tabs.addTab(tabTitle, this.resourcePane);
        }

        if (this.fixed.getClearanceLevel("TST_MENU") != null) {
            this.testingPane = new TopPanelTesting(this.ifxCache, this.serverSiteUrl, this.fixed, this.frame);
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

        this.frame.pack();

        final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice selected = env.getDefaultScreenDevice();

        final Rectangle bounds = selected.getDefaultConfiguration().getBounds();

        final Dimension size = this.frame.getSize();
        this.frame.setLocation(bounds.x + (bounds.width - size.width) / 2,
                bounds.y + (bounds.height - size.height) / 2);
        this.frame.setVisible(true);

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
     * Called when the window is closing.
     *
     * @param e the window event
     */
    @Override
    public void windowClosing(final WindowEvent e) {

        final String title = Res.get(Res.TITLE);
        if (JOptionPane.showConfirmDialog(this.frame, "Close the application?", title, JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION) {

            this.studentPane.clearDisplay();
            this.resourcePane.clearDisplay();
            this.testingPane.clearDisplay();
            this.managementPane.clearDisplay();
            this.formsPane.clearDisplay();


            this.frame.setVisible(false);
            this.frame.dispose();
        }
    }
}

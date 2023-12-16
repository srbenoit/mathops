package dev.mathops.app.db.ui;

import dev.mathops.app.AppFileLoader;
import dev.mathops.app.db.ui.analytics.AnalyticsPanel;
import dev.mathops.app.db.ui.configuration.MainConfigurationPanel;
import dev.mathops.app.db.ui.data.DataPanel;
import dev.mathops.app.db.ui.dba.DbaPanel;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.ui.layout.StackedBorderLayout;
import dev.mathops.db.config.DatabaseConfig;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import java.awt.image.BufferedImage;

/**
 * The main window.
 */
public final class MainWindow extends JFrame {

    /** The data panel. */
    private final DataPanel dataPanel;

    /** The analytics panel. */
    private final AnalyticsPanel analyticsPanel;

    /** The DBA panel. */
    private final DbaPanel dbaPanel;

    /** The configuration panel. */
    private final MainConfigurationPanel configurationPanel;

    /** The currently active database configuration. */
    private DatabaseConfig dbConfig;

    /**
     * Constructs a new {@code MainWindow}.
     */
    public MainWindow() {

        // Called on the AWT event thread

        super(Res.get(Res.MAIN_WINDOW_TITLE));

        final BufferedImage img = AppFileLoader.loadFileAsImage(MainWindow.class, "development-database.png", true);
        if (img != null) {
            setIconImage(img);
        }

        final JPanel content = new JPanel(new StackedBorderLayout());
        final Border bevelBorder = BorderFactory.createLoweredSoftBevelBorder();
        final Border pad = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        final Border border = BorderFactory.createCompoundBorder(pad, bevelBorder);
        content.setBorder(border);

        setContentPane(content);

        final JTabbedPane tabs = new JTabbedPane();
        content.add(tabs, StackedBorderLayout.CENTER);

        this.dataPanel = new DataPanel();
        final String dataTabTitle = Res.get(Res.DATA_TAB_TITLE);
        tabs.addTab(dataTabTitle, this.dataPanel);

        this.analyticsPanel = new AnalyticsPanel();
        final String analyticsTabTitle = Res.get(Res.ANALYTICS_TAB_TITLE);
        tabs.addTab(analyticsTabTitle, this.analyticsPanel);

        this.dbaPanel = new DbaPanel();
        final String dbaTabTitle = Res.get(Res.DBA_TAB_TITLE);
        tabs.addTab(dbaTabTitle, this.dbaPanel);

        this.configurationPanel = new MainConfigurationPanel();
        final String configurationTabTitle = Res.get(Res.CONFIGURATION_TAB_TITLE);
        tabs.addTab(configurationTabTitle, this.configurationPanel);

        pack();
    }

    /**
     * Gets the active database configuration.
     *
     * @return the active configuration
     */
    public DatabaseConfig getDbConfig() {

        return this.dbConfig;
    }

    /**
     * Initializes the main window and all contained panels.  Call this after the object has been constructed.
     * This is separated from construction to prevent constructors from leaking "this" references during construction.
     */
    public void init() {

        // Called on the application thread

        this.dbConfig = DatabaseConfig.getDefaultInstance();

        this.dataPanel.updateConfig(this);
        this.analyticsPanel.updateConfig(this);
        this.dbaPanel.updateConfig(this);
        this.configurationPanel.updateConfig(this);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("MainWindow{}");
    }
}

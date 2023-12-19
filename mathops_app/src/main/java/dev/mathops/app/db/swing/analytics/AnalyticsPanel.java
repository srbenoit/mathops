package dev.mathops.app.db.swing.analytics;

import dev.mathops.app.db.swing.MainWindow;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.ui.layout.StackedBorderLayout;
import dev.mathops.db.config.DatabaseConfig;

import javax.swing.JPanel;

/**
 * A panel that organizes analytics reports.
 */
public final class AnalyticsPanel extends JPanel {

    /** The active database configuration. */
    private DatabaseConfig dbConfig;

    /**
     * Constructs a new {@code AnalyticsPanel}.
     */
    public AnalyticsPanel() {

        super(new StackedBorderLayout());
    }

    /**
     * Updates configuration, including the active database configuration.  This does not affect existing analytics
     * displays, but will be used for any subsequent database access.
     *
     * @param theMainWindow the owning {@code MainWindow}
     */
    public void updateConfig(final MainWindow theMainWindow) {

        this.dbConfig = theMainWindow.getDbConfig();
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("AnalyticsPanel{}");
    }
}

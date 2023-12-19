package dev.mathops.app.db.swing.dba;

import dev.mathops.app.db.swing.MainWindow;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.ui.layout.StackedBorderLayout;
import dev.mathops.db.config.DatabaseConfig;

import javax.swing.JPanel;

/**
 * A panel that provides access to database administration functions like database construction and validation,
 * backup and restore, etc.
 */
public final class DbaPanel extends JPanel {

    /** The active database configuration. */
    private DatabaseConfig dbConfig;

    /**
     * Constructs a new {@code DbaPanel}.
     */
    public DbaPanel() {

        super(new StackedBorderLayout());

        // TODO:
    }

    /**
     * Updates configuration, including the active database configuration.  Any DBA functions that are "open" should
     * check that the new configuration is consistent with the action they are performing, and should abort any that are not.
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

        return SimpleBuilder.concat("DbaPanel{}");
    }
}

package dev.mathops.app.db.ui;

import dev.mathops.core.ui.layout.StackedBorderLayout;
import dev.mathops.db.config.DatabaseConfig;

import javax.swing.JPanel;

/**
 * A panel that presents the list of configured data profiles, and allows the user to edit these objects.
 *
 * <p>
 * Selecting a data profile lets the user open/activate a data management frame for that profile, which includes
 * backup and restore functions.
 */
class ConfiguredProfilesPanel extends JPanel {

    /** The database configuration this panel displays and edits. */
    private DatabaseConfig dbConfig;

    /**
     * Constructs a new {@code ConfiguredProfilesPanel}.
     */
    ConfiguredProfilesPanel() {

        super(new StackedBorderLayout());
    }

    /**
     * Updates the database config object this panel is displaying, and updates the UI to match the new object.
     *
     * @param theDbConfig the database configuration this panel displays and edits
     */
    public void setConfig(final DatabaseConfig theDbConfig) {

        this.dbConfig = theDbConfig;
    }
}

package dev.mathops.app.db.ui;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.ui.layout.StackedBorderLayout;
import dev.mathops.db.config.DatabaseConfig;
import dev.mathops.db.config.ServerConfig;

import javax.net.ServerSocketFactory;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A panel that presents the list of configured database servers and their configured logins, and allows the user
 * to edit these objects.
 *
 * <p>
 * Selecting a server that has a DBA profile lets the user open/activate a server administration frame for that server.
 * This panel allows all server logins to be verified.
 */
final class ConfiguredServersPanel extends JPanel implements ActionListener {

    /** The database configuration this panel displays and edits. */
    private DatabaseConfig dbConfig;

    /**
     * Constructs a new {@code ConfiguredServersPanel}.
     */
    ConfiguredServersPanel() {

        super(new StackedBorderLayout(5, 5));

        final Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        setBorder(padding);
    }

    /**
     * Updates the database config object this panel is displaying, and updates the UI to match the new object.
     *
     * @param theDbConfig the database configuration this panel displays and edits
     */
    public void setConfig(final DatabaseConfig theDbConfig) {

        if (theDbConfig != this.dbConfig) {
            this.dbConfig = theDbConfig;
            buildUI();
        }
    }

    /**
     * Rebuilds the UI when the panel is created or a new configuration is loaded.
     */
    private void buildUI() {

        removeAll();

        final ServerConfig[] servers = this.dbConfig.getServers();

        for (final ServerConfig server : servers) {
            final ServerConfigPanel inner = new ServerConfigPanel(this, this.dbConfig, server);
            add(inner, StackedBorderLayout.NORTH);
        }
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("ConfiguredServersPanel{dbConfig=", this.dbConfig, "}");
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

    }

    /**
     * Deletes a server panel.
     *
     * @param panel the panel representing the server to delete
     * @param server the server to delete
     */
    void deleteServer(final ServerConfigPanel panel, final ServerConfig server) {

    }
}

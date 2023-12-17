package dev.mathops.app.db.ui.configuration;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.ui.layout.StackedBorderLayout;

import javax.swing.JPanel;
import java.awt.Color;

/**
 * A pane that manages database servers.
 *
 * <p>
 * The "View" portion of this pane displays a list of all defined database servers (reflecting those in the Model).
 * Each server may be "expanded" to open up a set of controls that both (A) acts as the View for the attributes of
 * the server, and (B) acts as a controller to change those attributes.  Included in these controls is a button to
 * delete the server.  Also included is display of the set of logins configured for the server, with a
 *
 */
public final class DatabaseServersPane extends JPanel {

    /**
     * Constructs a new {@code DatabaseServersPane}.
     */
    public DatabaseServersPane() {

        super(new StackedBorderLayout());

        setBackground(Color.RED);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("DatabaseServersPane{}");
    }
}

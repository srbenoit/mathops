package dev.mathops.app.db.swing.configuration;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.ui.layout.StackedBorderLayout;

import javax.swing.JPanel;
import java.awt.Color;

/**
 * A pane that manages database logins.
 *
 * <p>
 * The "View" portion of this pane displays a list of all defined database logins (reflecting those in the Model).
 * Each login may be "expanded" to open up a set of controls that both (A) acts as the View for the attributes of
 * the login, and (B) acts as a controller to change those attributes.  Included in these controls is a button to
 * delete the login.
 */
public final class DatabaseLoginsPane extends JPanel {

    /**
     * Constructs a new {@code DatabaseLoginsPane}.
     */
    public DatabaseLoginsPane() {

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

        return SimpleBuilder.concat("DatabaseLoginsPane{}");
    }
}

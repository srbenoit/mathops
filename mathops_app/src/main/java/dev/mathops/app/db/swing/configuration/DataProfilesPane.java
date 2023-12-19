package dev.mathops.app.db.swing.configuration;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.ui.layout.StackedBorderLayout;

import javax.swing.JPanel;
import java.awt.Color;

/**
 * A pane that manages data profiles.
 */
public final class DataProfilesPane extends JPanel {

    /**
     * Constructs a new {@code DataProfilesPane}.
     */
    public DataProfilesPane() {

        super(new StackedBorderLayout());

        setBackground(Color.GREEN);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("DataProfilesPane{}");
    }
}

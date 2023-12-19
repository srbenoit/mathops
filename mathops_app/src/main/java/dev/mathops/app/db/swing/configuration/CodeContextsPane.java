package dev.mathops.app.db.swing.configuration;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.ui.layout.StackedBorderLayout;

import javax.swing.JPanel;
import java.awt.Color;

/**
 * A pane that manages code contexts.
 */
public final class CodeContextsPane extends JPanel {

    /**
     * Constructs a new {@code CodeContextsPane}.
     */
    public CodeContextsPane() {

        super(new StackedBorderLayout());

        setBackground(Color.BLUE);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("CodeContextsPane{}");
    }
}

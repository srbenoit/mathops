package dev.mathops.app.db.swing.configuration;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.ui.layout.StackedBorderLayout;

import javax.swing.JPanel;
import java.awt.Color;

/**
 * A pane that manages web contexts.
 */
public final class WebContextsPane extends JPanel {

    /**
     * Constructs a new {@code WebContextsPane}.
     */
    public WebContextsPane() {

        super(new StackedBorderLayout());

        setBackground(Color.MAGENTA);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("WebContextsPane{}");
    }
}

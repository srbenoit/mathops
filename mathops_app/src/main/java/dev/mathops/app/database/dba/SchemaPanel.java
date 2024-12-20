package dev.mathops.app.database.dba;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;

/**
 * The panel that should be displayed when a schema is selected in the tree view.
 */
final class SchemaPanel extends PanelBase {

    /** A label to show the name of the selected schema. */
    private final JLabel schemaNameLabel;

    /**
     * Constructs a new {@code SchemaPanel}.
     */
    SchemaPanel() {

        super();

        final JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        this.schemaNameLabel = makeHeadingLabel("Schema: ", top);
        add(top, StackedBorderLayout.NORTH);
    }

    /**
     * Sets the schema to display.
     *
     * @param schemaName the schema name
     */
    void setSchema(final String schemaName) {

        this.schemaNameLabel.setText(schemaName);
    }
}

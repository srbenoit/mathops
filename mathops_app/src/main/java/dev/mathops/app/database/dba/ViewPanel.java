package dev.mathops.app.database.dba;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;

/**
 * The panel that should be displayed when a view is selected in the tree view.
 */
final class ViewPanel extends PanelBase {

    /** A label to show the name of the selected schema. */
    private final JLabel schemaNameLabel;

    /** A label to show the name of the selected view. */
    private final JLabel viewNameLabel;

    /**
     * Constructs a new {@code ViewPanel}.
     */
    ViewPanel() {

        super();

        final JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        this.schemaNameLabel = makeHeadingLabel("Schema: ", top);
        this.viewNameLabel = makeHeadingLabel("        View: ", top);
        add(top, StackedBorderLayout.NORTH);
    }

    /**
     * Sets the view to display.
     *
     * @param schemaName the schema name
     * @param viewName   the view name
     */
    void setView(final String schemaName, final String viewName) {

        this.schemaNameLabel.setText(schemaName);
        this.viewNameLabel.setText(viewName);
    }
}

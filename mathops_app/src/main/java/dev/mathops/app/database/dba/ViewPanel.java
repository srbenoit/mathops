package dev.mathops.app.database.dba;

import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.util.List;

/**
 * The panel that should be displayed when a view is selected in the tree view.
 */
final class ViewPanel extends PanelBase {

    /** The caches for each login configuration provided. */
    private final List<Cache> caches;

    /** A label to show the name of the selected schema. */
    private final JLabel schemaNameLabel;

    /** A label to show the name of the selected view. */
    private final JLabel viewNameLabel;

    /**
     * Constructs a new {@code ViewPanel}.
     *
     * @param theCaches the caches for each login configuration provided
     */
    ViewPanel(final List<Cache> theCaches) {

        super();

        this.caches = theCaches;

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

        // TODO: Go to each cache and get some view-related information, present in tabs or as a table.
    }
}

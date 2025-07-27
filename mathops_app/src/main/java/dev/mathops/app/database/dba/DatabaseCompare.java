package dev.mathops.app.database.dba;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.FlowLayout;

/**
 * A pane that lets the user compare all selected databases.  This checks that the structures are compatible, and scans
 * data from each database, reporting the number of records in each that are not in the others.  It provides a way to
 * copy missing data from one database to another, or to overwrite one database with the contents of another.
 */
final class DatabaseCompare extends JPanel {

    /**
     * Constructs a new {@code DatabaseCompare}
     */
    DatabaseCompare() {

        super(new StackedBorderLayout());

        final Color background = getBackground();
        final boolean isLight = InterfaceUtils.isLight(background);
        final Color accent = InterfaceUtils.createAccentColor(background, isLight);
        final Color newBg = isLight ? background.brighter() : background.darker();

        final Border outline = BorderFactory.createLineBorder(accent);
        final Border pad = BorderFactory.createEmptyBorder(4, 4, 4, 4);
        final Border paddedBox = BorderFactory.createCompoundBorder(outline, pad);
        setBorder(paddedBox);

        setBackground(newBg);

        final JPanel buttonFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
        buttonFlow.setOpaque(false);
        final JButton compareData = new JButton("Compare Data");
        buttonFlow.add(compareData);
        add(buttonFlow, StackedBorderLayout.NORTH);
    }

    /**
     * Updates the schema and table this panel shows and the databases holding the data.
     *
     * @param schemaTable  the schema and table; null if none is selected
     * @param databaseUses the selected database uses
     */
    void update(final SchemaTable schemaTable, final Iterable<DatabaseUse> databaseUses) {

    }
}

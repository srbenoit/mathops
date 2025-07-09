package dev.mathops.app.database.dba;

import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * A panel that presents a selected record.
 */
final class PaneActiveRecord extends JPanel {

    /** The fields to display record values. */
    private final List<JTextField> activeTableFields;

    /** The normal background color for a text field. */
    private final Color normalFieldBg;

    /** The "error" background color for a text field. */
    private final Color errorFieldBg;

    /** The panel with query fields. */
    private final JPanel recordFields;

    /**
     * Constructs a new {@code PaneActiveRecord}.
     */
    PaneActiveRecord() {

        super(new StackedBorderLayout());

        this.activeTableFields = new ArrayList<>(40);

        final JTextField field = new JTextField();
        this.normalFieldBg = field.getBackground();
        final boolean isLight = InterfaceUtils.isLight(this.normalFieldBg);
        final int normalRed = this.normalFieldBg.getRed();
        final int normalGreen = this.normalFieldBg.getGreen();
        final int normalBlue = this.normalFieldBg.getBlue();
        this.errorFieldBg = isLight ? new Color(255, normalGreen - 10, normalBlue - 10)
                : new Color(normalRed + 50, normalGreen, normalBlue);

        final Border padding = BorderFactory.createEmptyBorder(5, 8, 5, 8);

        setPreferredSize(new Dimension(300, 300));
        setBorder(padding);

        final JLabel queryHeader = new JLabel("Active Record:");
        add(queryHeader, StackedBorderLayout.NORTH);

        this.recordFields = new JPanel(new StackedBorderLayout());
        final JScrollPane queryScroll = new JScrollPane(this.recordFields);

        final JScrollBar verticalBar = queryScroll.getVerticalScrollBar();
        verticalBar.setUnitIncrement(10);
        verticalBar.setBlockIncrement(10);

        queryScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(queryScroll, StackedBorderLayout.CENTER);
    }

    /**
     * Clears tha panel.
     */
    void clear() {

        this.recordFields.removeAll();
        this.activeTableFields.clear();
    }

    /**
     * Updates the panel to reflect a given set of columns.
     *
     * @param columns the list of columns
     * @param values  the object values
     */
    void update(final List<Column> columns, final Object[] values) {

        clear();

        final int numColumns = columns.size();
        if (numColumns > 0) {
            final JLabel[] fieldNames = new JLabel[numColumns];
            for (int i = 0; i < numColumns; ++i) {
                final Column col = columns.get(i);
                fieldNames[i] = new JLabel(col.name() + ":");
            }
            UIUtilities.makeLabelsSameSizeRightAligned(fieldNames);

            for (int i = 0; i < numColumns; ++i) {
                final JPanel columnFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 2));
                columnFlow.add(fieldNames[i]);

                final JTextField field = new JTextField(10);
                if (values[i] != null) {
                    field.setText(values[i].toString());
                }
                this.activeTableFields.add(field);
                columnFlow.add(field);

                this.recordFields.add(columnFlow, StackedBorderLayout.NORTH);
            }

            invalidate();
            revalidate();
            repaint();
        }
    }
}

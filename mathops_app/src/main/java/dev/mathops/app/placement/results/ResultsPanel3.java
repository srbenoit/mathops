package dev.mathops.app.placement.results;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.io.Serial;
import java.util.SortedSet;

/**
 * The third panel in the placement results wizard.
 */
class ResultsPanel3 extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2320120932228590844L;

    /** The list of courses the student placed out of. */
    private final SortedSet<String> placed;

    /**
     * Construct a new {@code ResultsPanel3}.
     *
     * @param thePlaced the list of courses the student placed out of
     */
    ResultsPanel3(final SortedSet<String> thePlaced) {

        super();

        this.placed = thePlaced;

        setLayout(new BorderLayout());

        final JPanel content = getContentPanel();
        content.setBorder(new EmptyBorder(10, 10, 10, 10));

        add(getTitlePanel(), BorderLayout.NORTH);
        final JPanel inner = new JPanel(new BorderLayout());
        inner.add(content, BorderLayout.NORTH);
        add(inner, BorderLayout.CENTER);
    }

    /**
     * Build the content panel.
     *
     * @return the constructed panel
     */
    private static JPanel getTitlePanel() {

        final JPanel title = new JPanel();
        title.setLayout(new BorderLayout());
        title.setBackground(Color.gray);

        final JLabel label = new JLabel("Placement Earned");
        label.setBackground(Color.gray);
        label.setFont(new Font("Dialog", Font.BOLD, 16));
        label.setBorder(new EmptyBorder(10, 10, 10, 10));
        label.setOpaque(true);
        title.add(label, BorderLayout.CENTER);
        title.add(new JSeparator(), BorderLayout.SOUTH);

        return title;
    }

    /**
     * Build the content panel.
     *
     * @return the constructed panel
     */
    private JPanel getContentPanel() {

        final JPanel content = new JPanel();
        content.setLayout(new BorderLayout());

        final JPanel inner = new JPanel();
        inner.setLayout(new java.awt.GridLayout(0, 1));

        inner.add(new JLabel("Your results indicate that you have placed out of the courses shown below:"));
        inner.add(new JLabel());

        for (final String s : this.placed) {
            inner.add(new JLabel(Results.courseNameLookup(s)));
        }

        content.add(inner, BorderLayout.NORTH);

        return content;
    }
}

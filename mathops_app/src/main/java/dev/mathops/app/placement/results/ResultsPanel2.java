package dev.mathops.app.placement.results;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.io.Serial;
import java.util.SortedSet;

/**
 * The second panel in the placement results wizard.
 */
class ResultsPanel2 extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8825015076454451679L;

    /** The list of courses for which credit was earned. */
    private final SortedSet<String> credit;

    /**
     * Construct a new {@code ResultsPanel2}.
     *
     * @param theCredit the list of courses for which credit was earned
     */
    ResultsPanel2(final SortedSet<String> theCredit) {

        super();

        this.credit = theCredit;

        setLayout(new BorderLayout());

        final JPanel contentPanel = getContentPanel();
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        add(getTitlePanel(), BorderLayout.NORTH);
        final JPanel secondaryPanel = new JPanel(new BorderLayout());
        secondaryPanel.add(contentPanel, BorderLayout.NORTH);
        add(secondaryPanel, BorderLayout.CENTER);
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

        final JLabel label = new JLabel("Placement Credit Earned");
        label.setBackground(Color.gray);
        label.setFont(new Font("Dialog", Font.BOLD, 16));
        label.setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
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

        inner.add(new JLabel("Your results indicate that you have earned credit for the courses shown below:"));
        inner.add(new JLabel());

        for (final String s : this.credit) {
            inner.add(new JLabel(Results.courseNameLookup(s)));
        }

        content.add(inner, BorderLayout.NORTH);

        return content;
    }
}

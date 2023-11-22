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
 * The second panel in the placement results wizard.
 */
class ResultsPanel4 extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1689099979826536852L;

    /** The list of courses the student is cleared to take. */
    private final SortedSet<String> cleared;

    /**
     * Construct a new {@code ResultsPanel4}.
     *
     * @param theCleared the list of courses the student is cleared to take
     */
    ResultsPanel4(final SortedSet<String> theCleared) {

        super();

        this.cleared = theCleared;

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

        final JLabel label = new JLabel("Available Courses");
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

        inner.add(new JLabel("Your results indicate that your are currently eligible to take any of the"));
        inner.add(new JLabel("math courses shown below:"));
        inner.add(new JLabel());

        for (final String course : this.cleared) {
            if ((!"Calculus".equals(course)) && (!"Recommend-Retake".equals(course))) {
                inner.add(new JLabel(Results.courseNameLookup(course)));
            }
        }

        if (this.cleared.contains("Calculus")) {
            inner.add(new JLabel());
            inner.add(new JLabel("You may also be eligible to register for a Calculus course."));
            inner.add(new JLabel());
            inner.add(new JLabel("Speak with your adviser to determine the most appropriate course(s) to"));
            inner.add(new JLabel("select."));
        }

        if (this.cleared.contains("Recommend-Retake")) {
            inner.add(new JLabel());
            inner.add(new JLabel("Based on your results, we recommend that you take the placement exam again"));
            inner.add(new JLabel("in the Math Department testing center when you arrive on campus.  You may"));
            inner.add(new JLabel("be able to earn credit for some math courses by taking the exam again."));
        }

        content.add(inner, BorderLayout.NORTH);

        return content;
    }
}

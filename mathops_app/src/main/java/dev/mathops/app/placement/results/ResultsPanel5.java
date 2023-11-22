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

/**
 * The second panel in the placement results wizard.
 */
class ResultsPanel5 extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4479361871503486626L;

    /**
     * Construct a new {@code ResultsPanel5}.
     */
    ResultsPanel5() {

        super();

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

        final JLabel label = new JLabel("No Placement Achieved");
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
    private static JPanel getContentPanel() {

        final JPanel content = new JPanel();
        content.setLayout(new BorderLayout());

        final JPanel inner = new JPanel();
        inner.setLayout(new java.awt.GridLayout(0, 1));

        inner.add(new JLabel(
                "Your results do not indicate that your are currently eligible for any of the"));
        inner.add(new JLabel("math courses at Colorado State University."));
        inner.add(new JLabel());
        inner.add(
                new JLabel("You must take and pass the Entry Level Mathematics (ELM) Exam prior to"));
        inner.add(
                new JLabel("registering for math courses.  This exam must be taken on-campus in the"));
        inner.add(new JLabel("Precalculus Center (Weber 138)."));

        content.add(inner, BorderLayout.NORTH);

        return content;
    }
}

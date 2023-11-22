package dev.mathops.app.placement.survey;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.Serial;

/**
 * The first panel in the test wizard.
 */
class SurveyPanel1 extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1798718167045872368L;

    /**
     * Construct a new {@code SurveyPanel1}.
     *
     * @param examTitle the exam title
     */
    SurveyPanel1(final String examTitle) {

        super(new BorderLayout());

        final JPanel contentPanel = getContentPanel(examTitle);
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        final JLabel iconLabel = new JLabel(//
                new ImageIcon(Survey.class.getResource("sidebar.jpg")));

        add(iconLabel, BorderLayout.WEST);

        final JPanel secondaryPanel = new JPanel(new BorderLayout());
        secondaryPanel.add(contentPanel, BorderLayout.NORTH);
        add(secondaryPanel, BorderLayout.CENTER);
    }

    /**
     * Builds the content panel.
     *
     * @param examTitle the exam title
     * @return the constructed panel
     */
    private static JPanel getContentPanel(final String examTitle) {

        final JPanel contentPanel1 = new JPanel(new BorderLayout());

        final JLabel label = new JLabel("Welcome to " + examTitle);
        label.setFont(new Font("Dialog", Font.BOLD, 16));
        contentPanel1.add(label, BorderLayout.NORTH);

        final JPanel jPanel1 = new JPanel(new GridLayout(0, 1));

        jPanel1.add(new JLabel());
        jPanel1
                .add(new JLabel("Before you begin the exam, we ask that you take a moment to provide"));
        jPanel1.add(new JLabel("some background information."));
        jPanel1.add(new JLabel());

        jPanel1.add(new JLabel("Press the 'Next' button to continue...."));

        contentPanel1.add(jPanel1, BorderLayout.CENTER);

        return contentPanel1;
    }
}

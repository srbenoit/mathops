package dev.mathops.app.placement.survey;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * The sixth panel in the test wizard.
 */
class SurveyPanel6 extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4194575593638399628L;

    /** Checkboxes. */
    private JCheckBox[] checks;

    /**
     * Construct a new {@code SurveyPanel6}.
     */
    SurveyPanel6() {

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
     * See whether the box is selected or not.
     *
     * @param index the index of the checkbox to test
     * @return {@code true} if selected; {@code false} otherwise
     */
    final boolean isCheckBoxSelected(final int index) {

        return this.checks[index].isSelected();
    }

    /**
     * Set the selected radio button for question 1 based on its action command.
     *
     * @param command the action command of the radio button to select
     */
    public void setCheckBoxSelected(final String command) {

        for (final JCheckBox check : this.checks) {
            if (command.equals(check.getActionCommand())) {
                check.setSelected(true);
                break;
            }
        }
    }

    /**
     * Register a listener for check box changes.
     *
     * @param listener the listener to register
     */
    final void addActionListener(final ActionListener listener) {

        for (int i = 0; i < 7; i++) {
            this.checks[i].addActionListener(listener);
        }
    }

    /**
     * Get the action command associated with a particular checkbox.
     *
     * @param index the index of the checkbox
     * @return the checkbox action command
     */
    final String getCheckBoxCommand(final int index) {

        return this.checks[index].getActionCommand();
    }

    /**
     * Build the content panel.
     *
     * @return the constructed panel
     */
    private static JPanel getTitlePanel() {

        final JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.gray);

        final JLabel label = new JLabel("College Mathematics Courses");
        label.setBackground(Color.gray);
        label.setFont(new Font("Dialog", Font.BOLD, 16));
        label.setBorder(new EmptyBorder(10, 10, 10, 10));
        titlePanel.add(label, BorderLayout.CENTER);

        titlePanel.add(new JSeparator(), BorderLayout.SOUTH);

        return titlePanel;
    }

    /**
     * Build the content panel.
     *
     * @return the constructed panel
     */
    private JPanel getContentPanel() {

        final String[] titles = {"College Math for Liberal Arts", "College Elementry Algebra",
                "College Intermediate Algebra", "College Algebra",
                "College Pre-calculus (includes Trigonometry)", "College Calculus",
                "Other College Course"};
        final String[] actions = {"3", "8",
                "11", "12", "14",
                "16", "2"};

        final JPanel contentPanel = new JPanel(new BorderLayout());

        this.checks = new JCheckBox[7];

        final JPanel innerPanel = new JPanel(new java.awt.GridLayout(2, 1));

        innerPanel
                .add(new JLabel("What math classes have you taken (or are currently taking) at a"));
        innerPanel.add(new JLabel("COLLEGE or UNIVERSITY? (Mark all that apply):"));
        contentPanel.add(innerPanel, BorderLayout.NORTH);

        final JPanel innerPanel2 = new JPanel(new java.awt.GridLayout(0, 1));

        for (int i = 0; i < 7; i++) {
            this.checks[i] = new JCheckBox(titles[i]);
            this.checks[i].setActionCommand(actions[i]);
            this.checks[i].setBorder(new EmptyBorder(4, 40, 4, 0));
            innerPanel2.add(this.checks[i]);
        }

        contentPanel.add(innerPanel2, BorderLayout.CENTER);

        return contentPanel;
    }
}

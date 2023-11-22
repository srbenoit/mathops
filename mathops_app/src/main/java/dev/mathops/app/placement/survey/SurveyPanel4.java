package dev.mathops.app.placement.survey;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * The fourth panel in the test wizard.
 */
class SurveyPanel4 extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -1496119043215065977L;

    /** Checkboxes for the various freshman courses. */
    private JCheckBox[] q1Checks;

    /** Checkboxes for the various sophomore courses. */
    private JCheckBox[] q2Checks;

    /**
     * Construct a new {@code SurveyPanel4}.
     */
    SurveyPanel4() {

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
     * See whether a box for question 1 is selected or not.
     *
     * @param index the index of the checkbox to test
     * @return {@code true} if selected; {@code false} otherwise
     */
    final boolean isQ1CheckBoxSelected(final int index) {

        return this.q1Checks[index].isSelected();
    }

    /**
     * See whether a box for question 2 is selected or not.
     *
     * @param index the index of the checkbox to test
     * @return {@code true} if selected; {@code false} otherwise
     */
    final boolean isQ2CheckBoxSelected(final int index) {

        return this.q2Checks[index].isSelected();
    }

    /**
     * Set the selected radio button for question 1 based on its action command.
     *
     * @param command the action command of the radio button to select
     */
    public void setQ1CheckBoxSelected(final String command) {

        for (final JCheckBox q1Check : this.q1Checks) {
            if (command.equals(q1Check.getActionCommand())) {
                q1Check.setSelected(true);
                break;
            }
        }
    }

    /**
     * Set the selected radio button for question 1 based on its action command.
     *
     * @param command the action command of the radio button to select
     */
    public void setQ2CheckBoxSelected(final String command) {

        for (final JCheckBox q2Check : this.q2Checks) {
            if (command.equals(q2Check.getActionCommand())) {
                q2Check.setSelected(true);
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

        for (int i = 0; i < 9; ++i) {
            this.q1Checks[i].addActionListener(listener);
            this.q2Checks[i].addActionListener(listener);
        }
    }

    /**
     * Get the action command associated with a particular checkbox in question 1.
     *
     * @param index the index of the checkbox
     * @return the checkbox action command
     */
    final String getQ1CheckBoxCommand(final int index) {

        return this.q1Checks[index].getActionCommand();
    }

    /**
     * Get the action command associated with a particular checkbox in question 2.
     *
     * @param index the index of the checkbox
     * @return the checkbox action command.
     */
    final String getQ2CheckBoxCommand(final int index) {

        return this.q2Checks[index].getActionCommand();
    }

    /**
     * Build the content panel.
     *
     * @return the constructed panel
     */
    private static JPanel getTitlePanel() {

        final JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.gray);

        final JLabel label = new JLabel("High School Mathematics Courses");
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

        final JPanel contentPanel = new JPanel();
        final String[] titles = {"Algebra I", "Integrated Math I", "Geometry", "Integrated Math II", "Algebra II",
                "Integrated Math III", "Pre-calculus (Includes Trigonometry)", "Calculus", "Other"};
        final String[] actions = {"4", "5", "6", "7", "9", "10", "13", "15", "1"};

        contentPanel.setLayout(new GridLayout(2, 1));

        this.q1Checks = new JCheckBox[9];
        this.q2Checks = new JCheckBox[9];

        JPanel group = new JPanel(new BorderLayout());
        contentPanel.add(group);

        JPanel innerPanel = new JPanel(new GridLayout(2, 1));

        innerPanel.add(new JLabel("What math classes did you take during your FRESHMAN/FIRST year in high school?"));
        innerPanel.add(new JLabel("(Mark all that apply):"));
        group.add(innerPanel, BorderLayout.NORTH);

        innerPanel = new JPanel(new GridLayout(0, 2));

        for (int i = 0; i < 9; ++i) {
            this.q1Checks[i] = new JCheckBox(titles[i]);
            this.q1Checks[i].setActionCommand(actions[i]);
            this.q1Checks[i].setBorder(new EmptyBorder(4, 40, 4, 0));
            innerPanel.add(this.q1Checks[i]);
        }

        group.add(innerPanel, BorderLayout.CENTER);

        group = new JPanel(new BorderLayout());
        contentPanel.add(group);

        innerPanel = new JPanel(new GridLayout(2, 1));

        innerPanel.add(new JLabel("What math classes did you take during your SOPHOMORE/SECOND year in high school?"));
        innerPanel.add(new JLabel("(Mark all that apply):"));
        group.add(innerPanel, BorderLayout.NORTH);

        innerPanel = new JPanel(new GridLayout(0, 2));

        for (int i = 0; i < 9; ++i) {
            this.q2Checks[i] = new JCheckBox(titles[i]);
            this.q2Checks[i].setActionCommand(actions[i]);
            this.q2Checks[i].setBorder(new EmptyBorder(4, 40, 4, 0));
            innerPanel.add(this.q2Checks[i]);
        }

        group.add(innerPanel, BorderLayout.CENTER);

        return contentPanel;
    }

    /**
     * Enable or disable checkboxes to ensure at most two are selected in each section.
     */
    final void updateCheckboxes() {

        // Count the number selected in section 1
        int count = 0;
        for (final JCheckBox q1Check : this.q1Checks) {
            if (q1Check.isSelected()) {
                count++;
            }
        }

        // If 2 or more selected, disable the rest; otherwise, enable all
        if (count >= 2) {
            for (final JCheckBox q1Check : this.q1Checks) {
                q1Check.setEnabled(q1Check.isSelected());
            }
        } else {
            for (final JCheckBox q1Check : this.q1Checks) {
                q1Check.setEnabled(true);
            }
        }

        // Count the number selected in section 2
        count = 0;

        for (final JCheckBox q2Check : this.q2Checks) {
            if (q2Check.isSelected()) {
                count++;
            }
        }

        // If 2 or more selected, disable the rest; otherwise, enable all
        if (count >= 2) {
            for (final JCheckBox q2Check : this.q2Checks) {
                q2Check.setEnabled(q2Check.isSelected());
            }
        } else {
            for (final JCheckBox q2Check : this.q2Checks) {
                q2Check.setEnabled(true);
            }
        }
    }
}

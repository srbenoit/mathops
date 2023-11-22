package dev.mathops.app.placement.survey;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * The second panel in the test wizard.
 */
class SurveyPanel2 extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2014716738302620967L;

    /** List of radio buttons for "time spent preparing" question. */
    private JRadioButton[] q1Radios;

    /** Button group for radio buttons. */
    private ButtonGroup q1Buttons;

    /** List of checkboxes for "resources used to prepare" question. */
    private JCheckBox[] q2Checks;

    /** The label for the "resources used to prepare" question. */
    private JLabel q2Label;

    /**
     * Construct a new {@code SurveyPanel2}.
     */
    SurveyPanel2() {

        super(new BorderLayout());

        final JPanel contentPanel = getContentPanel();
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        add(getTitlePanel(), BorderLayout.NORTH);
        final JPanel secondaryPanel = new JPanel(new BorderLayout());
        secondaryPanel.add(contentPanel, BorderLayout.NORTH);
        add(secondaryPanel, BorderLayout.CENTER);
    }

    /**
     * See whether the box is selected or not. Boxes must be enabled to be counted as selected.
     *
     * @param index the index of the checkbox to test
     * @return {@code true} if selected; {@code false} otherwise
     */
    final boolean isCheckBoxSelected(final int index) {

        return this.q2Checks[index].isSelected() && this.q2Checks[index].isEnabled();
    }

    /**
     * Select a check box.
     *
     * @param index the index of the checkbox to test
     */
    public void setCheckBoxSelected(final int index) {

        this.q2Checks[index].setSelected(true);
    }

    /**
     * The the action command of the selected radio button.
     *
     * @return the selected radio button command
     */
    public String getRadioButtonSelected() {

        return (this.q1Buttons.getSelection() == null) ? null
                : this.q1Buttons.getSelection().getActionCommand();
    }

    /**
     * Register a listener for check box changes.
     *
     * @param listener the listener to register
     */
    final void addActionListener(final ActionListener listener) {

        for (int i = 0; i < 5; ++i) {
            this.q1Radios[i].addActionListener(listener);
        }
    }

    /**
     * Set the selected radio button based on its action command.
     *
     * @param command the action command of the radio button to select
     */
    public void setRadioButtonSelected(final String command) {

        for (final JRadioButton q1Radio : this.q1Radios) {
            if (command.equals(q1Radio.getActionCommand())) {
                q1Radio.setSelected(true);
                break;
            }
        }

        setCheckboxEnabledState();
    }

    /**
     * Build the content panel.
     *
     * @return the constructed panel
     */
    private static JPanel getTitlePanel() {

        final JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.gray);

        final JLabel label = new JLabel("Pre-Exam Preparation");
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
     * @return The constructed panel.
     */
    private JPanel getContentPanel() {

        final String[] q1Labels =
                {"None at all", "Less than 2 hours", "2-5 hours", "5-10 hours", "More than 10 hours"};
        final String[] q1Cmds = {"1", "2", "3",
                "4", "5"};
        final String[] q2Labels = {"The Study Guide on the Math Department Web Site", "Tutors",
                "Textbook(s)", "Materials from past math courses"};
        final String[] q2Cmds = {"1", "2", "3",
                "4"};

        final JPanel contentPanel = new JPanel(new BorderLayout());

        this.q1Buttons = new ButtonGroup();
        this.q1Radios = new JRadioButton[5];
        this.q2Checks = new JCheckBox[4];

        final JPanel innerPanel = new JPanel(new GridLayout(0, 1));

        innerPanel.add(new JLabel("How much time did you spend preparing for this exam?"));

        for (int i = 0; i < 5; ++i) {
            this.q1Radios[i] = new JRadioButton(q1Labels[i]);
            this.q1Radios[i].setActionCommand(q1Cmds[i]);
            this.q1Radios[i].setBorder(new EmptyBorder(3, 40, 3, 0));
            this.q1Buttons.add(this.q1Radios[i]);
            innerPanel.add(this.q1Radios[i]);
        }

        innerPanel.add(new JLabel());

        this.q2Label = new JLabel("What resources did you use to prepare? (Mark all that apply.)");
        this.q2Label.setEnabled(false);
        innerPanel.add(this.q2Label);
        contentPanel.add(innerPanel, BorderLayout.CENTER);

        for (int i = 0; i < 4; ++i) {
            this.q2Checks[i] = new JCheckBox(q2Labels[i]);
            this.q2Checks[i].setActionCommand(q2Cmds[i]);
            this.q2Checks[i].setBorder(new EmptyBorder(4, 40, 4, 0));
            this.q2Checks[i].setEnabled(false);
            innerPanel.add(this.q2Checks[i]);
        }

        return contentPanel;
    }

    /**
     * Set the enabled state of the "resources used" checkboxes based on the current state of the radio button. If no
     * radio selection has been made, or if the selection was "None", the checkboxes are disabled (and all cleared to
     * unchecked). Otherwise, the checkboxes are enabled.
     */
    final void setCheckboxEnabledState() {

        boolean hit = false;

        // See if any of the radio buttons other than "None" are selected
        for (int i = 1; i < 5; ++i) {
            if (this.q1Radios[i].isSelected()) {
                hit = true;
                break;
            }
        }

        // Enable or disable the checkboxes accordingly
        for (int i = 0; i < 4; ++i) {
            this.q2Checks[i].setEnabled(hit);
        }

        this.q2Label.setEnabled(hit);
    }
}

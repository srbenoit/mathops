package dev.mathops.app.placement.survey;

import javax.swing.ButtonGroup;
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
 * The third panel in the test wizard.
 */
class SurveyPanel3 extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4112258698161144627L;

    /** The list of radio buttons for the "time since last math" question. */
    private JRadioButton[] q1Radios;

    /** The button group for the "time since last math" question. */
    private ButtonGroup q1Buttons;

    /** The list of radio buttons for the "typical grade" question. */
    private JRadioButton[] q2Radios;

    /** The button group for the "typical grade" question. */
    private ButtonGroup q2Buttons;

    /**
     * Construct a new {@code SurveyPanel3}.
     */
    SurveyPanel3() {

        super(new BorderLayout());

        final JPanel contentPanel = getContentPanel();
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        add(getTitlePanel(), BorderLayout.NORTH);
        final JPanel secondaryPanel = new JPanel(new BorderLayout());
        secondaryPanel.add(contentPanel, BorderLayout.NORTH);
        add(secondaryPanel, BorderLayout.CENTER);
    }

    /**
     * The the action command of the selected radio button for question 1.
     *
     * @return the selected radio button command
     */
    public String getQ1RadioButtonSelected() {

        return (this.q1Buttons.getSelection() == null) ? null
                : this.q1Buttons.getSelection().getActionCommand();
    }

    /**
     * The the action command of the selected radio button for question 2.
     *
     * @return the selected radio button command
     */
    public String getQ2RadioButtonSelected() {

        return (this.q2Buttons.getSelection() == null) ? null
                : this.q2Buttons.getSelection().getActionCommand();
    }

    /**
     * Set the selected radio button for question 1 based on its action command.
     *
     * @param command the action command of the radio button to select
     */
    public void setQ1RadioButtonSelected(final String command) {

        for (final JRadioButton q1Radio : this.q1Radios) {
            if (command.equals(q1Radio.getActionCommand())) {
                q1Radio.setSelected(true);
                break;
            }
        }
    }

    /**
     * Set the selected radio button for question 2 based on its action command.
     *
     * @param command the action command of the radio button to select
     */
    public void setQ2RadioButtonSelected(final String command) {

        for (final JRadioButton q2Radio : this.q2Radios) {
            if (command.equals(q2Radio.getActionCommand())) {
                q2Radio.setSelected(true);
                break;
            }
        }
    }

    /**
     * Registers a listener for check box changes.
     *
     * @param listener the listener to register
     */
    final void addActionListener(final ActionListener listener) {

        for (int i = 0; i < 6; ++i) {
            this.q1Radios[i].addActionListener(listener);
            this.q2Radios[i].addActionListener(listener);
        }
    }

    /**
     * Build the content panel.
     *
     * @return the constructed panel
     */
    private static JPanel getTitlePanel() {

        final JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.gray);

        final JLabel label = new JLabel("Mathematics Background");
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

        final String[] q1labels = {"Currently enrolled", "Less than 3 months", "3-9 months",
                "9 months-2 years", "2-5 years", "More than 5 years"};
        final String[] q1cmds = {"1", "2", "3",
                "4", "5", "6"};
        final String[] q2labels = {"A", "A or B",
                "B", "B or C",
                "C", "C- or lower"};
        final String[] q2cmds = {"1", "2", "3",
                "4", "5", "6"};

        final JPanel contentPanel = new JPanel(new BorderLayout());

        this.q1Buttons = new ButtonGroup();
        this.q1Radios = new JRadioButton[6];
        this.q2Buttons = new ButtonGroup();
        this.q2Radios = new JRadioButton[6];

        final JPanel innerPanel = new JPanel(new GridLayout(0, 1));

        innerPanel
                .add(new JLabel("How long has it been since you completed your last math class?"));

        for (int inx = 0; inx < 6; inx++) {
            this.q1Radios[inx] = new JRadioButton(q1labels[inx]);
            this.q1Radios[inx].setBorder(new EmptyBorder(1, 40, 1, 0));
            this.q1Radios[inx].setActionCommand(q1cmds[inx]);
            this.q1Buttons.add(this.q1Radios[inx]);
            innerPanel.add(this.q1Radios[inx]);
        }

        innerPanel.add(new JLabel());

        innerPanel.add(new JLabel(
                "What final grade have you typically earned in the math classes you have taken?"));

        for (int inx = 0; inx < 6; inx++) {
            this.q2Radios[inx] = new JRadioButton(q2labels[inx]);
            this.q2Radios[inx].setBorder(new EmptyBorder(1, 40, 1, 0));
            this.q2Radios[inx].setActionCommand(q2cmds[inx]);
            this.q2Buttons.add(this.q2Radios[inx]);
            innerPanel.add(this.q2Radios[inx]);
        }

        contentPanel.add(innerPanel, BorderLayout.CENTER);

        return contentPanel;
    }
}

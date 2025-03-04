package dev.mathops.app.assessment.examprinter;

import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.border.Border;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A dialog to ask the user what they want to do...
 */
public final class WhatToDoDialog extends JFrame implements ActionListener {

    /** An action command. */
    private static final String PRINT_CMD = "PRINT";

    /** An action command. */
    private static final String LATEX_CMD = "LATEX";

    /** The owning application to notify when the user makes a choice. */
    private final ExamPrinterApp owner;

    /** A checkbox to select "large font". */
    private final JCheckBox largeFont;

    /** A checkbox to select "include solutions". */
    private final JCheckBox includeSolutions;

    /** A spinner to select the number to generate. */
    private final JSpinner numberToGenerate;

    /**
     * Constructs a new {@code WhatToDoDialog}.
     *
     * @param theOwner the owning application to notify when the user makes a choice.
     */
    WhatToDoDialog(final ExamPrinterApp theOwner) {

        super("Exam Printer");

        this.owner = theOwner;

        final JPanel content = new JPanel(new StackedBorderLayout(10, 10));
        final Border contentPadding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
        content.setBorder(contentPadding);
        setContentPane(content);

        final String labelStr = Res.get(Res.WHAT_TO_DO);
        final JLabel lbl = new JLabel(labelStr);
        content.add(lbl, StackedBorderLayout.NORTH);

        final JPanel buttonFlow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));

        final String printLabel = Res.get(Res.SEND_TO_PRINTER);
        final JButton printButton = new JButton(printLabel);
        printButton.setActionCommand(PRINT_CMD);
        buttonFlow.add(printButton);

        final String latexLabel = Res.get(Res.GENERATE_LATEX);
        final JButton latexButton = new JButton(latexLabel);
        latexButton.setActionCommand(LATEX_CMD);
        buttonFlow.add(latexButton);

        content.add(buttonFlow, StackedBorderLayout.NORTH);

        this.largeFont = new JCheckBox("Use large font");
        content.add(this.largeFont, StackedBorderLayout.NORTH);

        this.includeSolutions = new JCheckBox("Include solutions");
        content.add(this.includeSolutions, StackedBorderLayout.NORTH);

        this.numberToGenerate = new JSpinner();
        this.numberToGenerate.setValue(Integer.valueOf(1));

        final JPanel flow = new JPanel(new FlowLayout());
        final JLabel numToGenerateLbl = new JLabel("Versions to generate: ");
        flow.add(numToGenerateLbl);
        flow.add(this.numberToGenerate);
        content.add(flow, StackedBorderLayout.NORTH);

        printButton.addActionListener(this);
        latexButton.addActionListener(this);

        UIUtilities.packAndCenter(this);
    }

    /**
     * Handles action events.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        final boolean large = this.largeFont.isSelected();
        final boolean solutions = this.includeSolutions.isSelected();
        final Object spinnerValue = this.numberToGenerate.getValue();
        int toGen = 1;
        if (spinnerValue instanceof final Integer spinnerInt) {
            toGen = spinnerInt.intValue();
        }

        if (PRINT_CMD.equals(cmd)) {
            this.owner.printExam(large, solutions, toGen);
            setVisible(false);
            dispose();
        } else if (LATEX_CMD.equals(cmd)) {
            this.owner.latexExam(large, solutions, toGen);
            setVisible(false);
            dispose();
        }
    }
}

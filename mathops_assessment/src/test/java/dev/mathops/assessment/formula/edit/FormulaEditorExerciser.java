package dev.mathops.assessment.formula.edit;

import dev.mathops.assessment.EType;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * A harness to exercise the formula editor classes.
 */
public final class FormulaEditorExerciser implements Runnable, ActionListener {

    /** A checkbox to indicate Boolean values are allowed. */
    private JCheckBox booleanType;

    /** A checkbox to indicate integer values are allowed. */
    private JCheckBox integerType;

    /** A checkbox to indicate real values are allowed. */
    private JCheckBox realType;

    /** A checkbox to indicate integer vector values are allowed. */
    private JCheckBox integerVectorType;

    /** A checkbox to indicate real vector values are allowed. */
    private JCheckBox realVectorType;

    /** A checkbox to indicate String values are allowed. */
    private JCheckBox stringType;

    /** A checkbox to indicate Span values are allowed. */
    private JCheckBox spanType;

    /** Button to create a formula editor. */
    private JButton createButton;

    /** The center panel to which to add a formula editor pane. */
    private JPanel center;

    /** The formula editor panel. */
    private FormulaEditorPanel editorPane;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private FormulaEditorExerciser() {

        // No action
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    public void run() {

        final JFrame frame = new JFrame("Formula Editor Exerciser");
        final JPanel content = new JPanel(new StackedBorderLayout(5, 5));
        content.setPreferredSize(new Dimension(500, 300));
        content.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        frame.setContentPane(content);

        final JLabel lbl = new JLabel("Select the types of value the formula can produce:");
        content.add(lbl, StackedBorderLayout.NORTH);

        // NORTH: a set of checkboxes to control the type of values a new formula will be allowed to generate
        final JPanel north = new JPanel(new GridLayout(2, 4));
        this.booleanType = new JCheckBox("Boolean");
        north.add(this.booleanType);
        this.integerType = new JCheckBox("Integer");
        north.add(this.integerType);
        this.realType = new JCheckBox("Real");
        north.add(this.realType);
        this.integerVectorType = new JCheckBox("Integer Vector");
        north.add(this.integerVectorType);
        this.realVectorType = new JCheckBox("Real Vector");
        north.add(this.realVectorType);
        this.stringType = new JCheckBox("String");
        north.add(this.stringType);
        this.spanType = new JCheckBox("Span");
        north.add(this.spanType);
        content.add(north, StackedBorderLayout.NORTH);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        this.createButton = new JButton("Create Formula Editor Panel");
        this.createButton.addActionListener(this);
        buttons.add(this.createButton);
        content.add(buttons, StackedBorderLayout.NORTH);

        this.center = new JPanel(new BorderLayout());
        final Color bg = content.getBackground();
        final Color darker = bg.darker();
        this.center.setBackground(darker);

        this.center.setBorder(BorderFactory.createEtchedBorder());
        content.add(this.center, StackedBorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final List<EType> types = new ArrayList<>(7);

        if (this.booleanType.isSelected()) {
            types.add(EType.BOOLEAN);
        }
        if (this.integerType.isSelected()) {
            types.add(EType.INTEGER);
        }
        if (this.realType.isSelected()) {
            types.add(EType.REAL);
        }
        if (this.integerVectorType.isSelected()) {
            types.add(EType.INTEGER_VECTOR);
        }
        if (this.realVectorType.isSelected()) {
            types.add(EType.REAL_VECTOR);
        }
        if (this.stringType.isSelected()) {
            types.add(EType.STRING);
        }
        if (this.spanType.isSelected()) {
            types.add(EType.SPAN);
        }

        if (types.isEmpty()) {
            JOptionPane.showMessageDialog(this.center, "At least one valid type must be selected.",
                    "Unable to create formula editor panel", JOptionPane.ERROR_MESSAGE);
        } else {
            this.booleanType.setEnabled(false);
            this.integerType.setEnabled(false);
            this.realType.setEnabled(false);
            this.integerVectorType.setEnabled(false);
            this.realVectorType.setEnabled(false);
            this.stringType.setEnabled(false);
            this.spanType.setEnabled(false);
            this.createButton.setEnabled(false);

            final Insets insets = new Insets(4, 4, 4, 4);
            this.editorPane = new FormulaEditorPanel(16, insets, types.toArray(new EType[0]));

            this.center.add(this.editorPane, BorderLayout.CENTER);
            this.center.revalidate();
            this.center.repaint();
        }
    }

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        SwingUtilities.invokeLater(new FormulaEditorExerciser());
    }
}

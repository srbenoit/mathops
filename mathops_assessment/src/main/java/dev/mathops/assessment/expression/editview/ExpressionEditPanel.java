package dev.mathops.assessment.expression.editview;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import java.awt.FlowLayout;

/**
 * A panel that supports text-based entry of expressions, with an associated palette to enter constants or constructions
 * that are inconvenient to enter through the keyboard.
 */
final class ExpressionEditPanel extends JPanel {

    /**
     * Constructs a new {@code ExpressionEditPanel}.  This should be called on the AWT event dispatch thread.
     */
    ExpressionEditPanel() {

        super(new StackedBorderLayout());

        // The top portion is the editor window in which the user can type.
        final JTextArea area = new JTextArea(2, 20);
        final Border etched = BorderFactory.createEtchedBorder();
        area.setBorder(etched);
        add(area, StackedBorderLayout.NORTH);

        // The bottom portion is a set of rows of palette buttons
        final JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        final JButton trueBtn = new JButton("TRUE");
        row1.add(trueBtn);
        final JButton falseBtn = new JButton("FALSE");
        row1.add(falseBtn);
        final JButton engrBtn = new JButton("E");
        row1.add(engrBtn);
        final JButton exponentBtn = new JButton("power");
        row1.add(exponentBtn);
        final JButton fractionBtn = new JButton("fraction");
        row1.add(fractionBtn);
        final JButton radicalBtn = new JButton("radical");
        row1.add(radicalBtn);
        final JButton ifThenElseBtn = new JButton("if-then-else");
        row1.add(ifThenElseBtn);
        final JButton caseBtn = new JButton("case");
        row1.add(caseBtn);

        add(row1, StackedBorderLayout.NORTH);
    }
}

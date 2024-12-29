package dev.mathops.assessment.expression;

import dev.mathops.assessment.expression.editmodel.EOperatorSymbol;
import dev.mathops.assessment.expression.editmodel.ESymbolicConstant;
import dev.mathops.assessment.expression.editmodel.Expr;
import dev.mathops.assessment.expression.editmodel.ExprLeafDecimalPoint;
import dev.mathops.assessment.expression.editmodel.ExprLeafDigit;
import dev.mathops.assessment.expression.editmodel.ExprLeafEngineeringE;
import dev.mathops.assessment.expression.editmodel.ExprLeafOperator;
import dev.mathops.assessment.expression.editmodel.ExprLeafSymbolicConstant;
import dev.mathops.assessment.expression.editview.ExpressionViewPanel;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * A class that presents an editor and allows the user to edit an expression.
 */
public class ExpressionEditor extends KeyAdapter implements Runnable, ActionListener {

    /** An action command. */
    public static final String CMD_ENGINEERING_E = "ENGR_E";

    /** An action command. */
    public static final String CMD_CONST_PI = "CONST_PI";

    /** An action command. */
    public static final String CMD_CONST_E = "CONST_E";

    /** An action command. */
    public static final String CMD_CONST_I = "CONST_I";

    /** The model. */
    private final Expr model;

    /** The expression view panel. */
    private final ExpressionViewPanel view;

    /**
     * Constructs a new {@code ExpressionEditor}.
     */
    private ExpressionEditor() {

        this.model = new Expr();
        this.view = new ExpressionViewPanel(this.model,new Dimension(400, 100), 34.0f, 9.0f);
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    public void run() {

        final JFrame frame = new JFrame("Expression Editor");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new StackedBorderLayout(1,1));
        content.setBackground(Color.GRAY);
        content.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
        frame.setContentPane(content);

        content.add(this.view, StackedBorderLayout.NORTH);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING));
        content.add(buttons, StackedBorderLayout.SOUTH);

        final JButton engineeringEButton = new JButton("E");
        engineeringEButton.setActionCommand(CMD_ENGINEERING_E);
        engineeringEButton.addActionListener(this);
        buttons.add(engineeringEButton);

        final JButton constPiBtn = new JButton(ESymbolicConstant.PI.str);
        constPiBtn.setActionCommand(CMD_CONST_PI);
        constPiBtn.addActionListener(this);
        buttons.add(constPiBtn);

        final JButton constEBtn = new JButton(ESymbolicConstant.E.str);
        constEBtn.setActionCommand(CMD_CONST_E);
        constEBtn.addActionListener(this);
        buttons.add(constEBtn);

        final JButton constIBtn = new JButton(ESymbolicConstant.I.str);
        constIBtn.setActionCommand(CMD_CONST_I);
        constIBtn.addActionListener(this);
        buttons.add(constIBtn);

        this.view.setFocusable(true);
        this.view.addKeyListener(this);

        UIUtilities.packAndCenter(frame);
        frame.setVisible(true);

        this.view.requestFocus();
    }

    /**
     * Main method to run the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        SwingUtilities.invokeLater(new ExpressionEditor());
    }

    /**
     * Called when a key is typed.
     * @param e the event to be processed
     */
    @Override
    public void keyTyped(final KeyEvent e) {

        final int character = e.getKeyChar();
        Log.info("Key typed: " + character);

        boolean repaint = false;

        if (character == '+') {
            insertOperator(EOperatorSymbol.PLUS);
            repaint = true;
        } else if (character == '-') {
            insertOperator(EOperatorSymbol.MINUS);
            repaint = true;
        } else if (character == '*') {
            insertOperator(EOperatorSymbol.TIMES);
            repaint = true;
        } else if (character == '/') {
            insertOperator(EOperatorSymbol.DIVIDED_BY);
            repaint = true;
        } else if (character == '%') {
            insertOperator(EOperatorSymbol.REMAINDER);
            repaint = true;
        } else if (character == '|') {
            insertOperator(EOperatorSymbol.OR);
            repaint = true;
        } else if (character == '&') {
            insertOperator(EOperatorSymbol.AND);
            repaint = true;
        } else if (character == '!') {
            insertOperator(EOperatorSymbol.NOT);
            repaint = true;
        } else if (character == '=') {
            insertOperator(EOperatorSymbol.EQUALS);
            repaint = true;
        } else if (character == '<') {
            insertOperator(EOperatorSymbol.LESS_THAN);
            repaint = true;
        } else if (character == '>') {
            insertOperator(EOperatorSymbol.GREATER_THAN);
            repaint = true;
        } else if (character == '.') {
            insertDecimal();
            repaint = true;
        } else if (character == '0') {
            insertDigit(0);
            repaint = true;
        } else if (character == '1') {
            insertDigit(1);
            repaint = true;
        } else if (character == '2') {
            insertDigit(2);
            repaint = true;
        } else if (character == '3') {
            insertDigit(3);
            repaint = true;
        } else if (character == '4') {
            insertDigit(4);
            repaint = true;
        } else if (character == '5') {
            insertDigit(5);
            repaint = true;
        } else if (character == '6') {
            insertDigit(6);
            repaint = true;
        } else if (character == '7') {
            insertDigit(7);
            repaint = true;
        } else if (character == '8') {
            insertDigit(8);
            repaint = true;
        } else if (character == '9') {
            insertDigit(9);
            repaint = true;
        }

        if (repaint) {
            this.view.updateExpression();
        }
    }

    /**
     * Called when a key is typed.
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(final KeyEvent e) {

        final int code = e.getKeyCode();
        Log.info("Key pressed: " + code);

        boolean repaint = false;

        if (code == KeyEvent.VK_LEFT) {
            // Left arrow
            Log.info("Left arrow");
            repaint = true;
        } else if (code == KeyEvent.VK_RIGHT) {
            // Right arrow
            Log.info("Right arrow");
            repaint = true;
        }

        if (repaint) {
            this.view.updateExpression();
        }
    }

    /**
     * Called when an action is invoked.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        boolean repaint = false;

        if (CMD_ENGINEERING_E.equals(cmd)) {
            insertEngineeringE();
            repaint = true;
        } else if (CMD_CONST_PI.equals(cmd)) {
            insertSymbolicConstant(ESymbolicConstant.PI);
            repaint = true;
        } else if (CMD_CONST_E.equals(cmd)) {
            insertSymbolicConstant(ESymbolicConstant.E);
            repaint = true;
        } else if (CMD_CONST_I.equals(cmd)) {
            insertSymbolicConstant(ESymbolicConstant.I);
            repaint = true;
        }

        this.view.requestFocus();

        if (repaint) {
            this.view.updateExpression();
        }
    }

    /**
     * Inserts an operator.
     *
     * @param operator the operator
     */
    private void insertOperator(final EOperatorSymbol operator) {

        final int size = this.model.size();
        this.model.insert(size, new ExprLeafOperator(operator));
    }

    /**
     * Inserts a decimal point.
     */
    private void insertDecimal() {

        final int size = this.model.size();
        this.model.insert(size, new ExprLeafDecimalPoint());
    }

    /**
     * Inserts a decimal digit.
     *
     * @param digit the digit (from 0 to 9, inclusive)
     */
    private void insertDigit(final int digit) {

        final int size = this.model.size();
        this.model.insert(size, new ExprLeafDigit(digit));
    }

    /**
     * Inserts an Engineering notation "E" to indicate multiplication by a power of ten.
     */
    private void insertEngineeringE() {

        final int size = this.model.size();
        this.model.insert(size, new ExprLeafEngineeringE());
    }

    /**
     * Inserts a symbolic constant.
     *
     * @param value the value to insert
     */
    private void insertSymbolicConstant(final ESymbolicConstant value) {

        final int size = this.model.size();
        this.model.insert(size, new ExprLeafSymbolicConstant(value));
    }
}

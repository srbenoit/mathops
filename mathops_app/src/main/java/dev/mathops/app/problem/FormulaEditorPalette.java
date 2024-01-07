package dev.mathops.app.problem;

import dev.mathops.assessment.formula.edit.FormulaEditorPanel;
import dev.mathops.core.ui.layout.StackedBorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A palette that supports formula editing.
 */
public final class FormulaEditorPalette extends JPanel implements ActionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -3573047086825932527L;

    /** An action command. */
    private static final String PI_CMD = "PI";

    /** An action command. */
    private static final String E_CMD = "E";

    /** An action command. */
    private static final String TRUE_CMD = "TRUE";

    /** An action command. */
    private static final String FALSE_CMD = "FALSE";

    /** An action command. */
    private static final String ABS_CMD = "ABS";

    /** An action command. */
    private static final String COS_CMD = "COS";

    /** An action command. */
    private static final String SIN_CMD = "SIN";

    /** An action command. */
    private static final String TAN_CMD = "TAN";

    /** An action command. */
    private static final String ACOS_CMD = "ACOS";

    /** An action command. */
    private static final String ASIN_CMD = "ASIN";

    /** An action command. */
    private static final String ATAN_CMD = "ATAN";

    /** An action command. */
    private static final String EXP_CMD = "EXP";

    /** An action command. */
    private static final String LOG_CMD = "LOG";

    /** An action command. */
    private static final String CEIL_CMD = "CEIL";

    /** An action command. */
    private static final String FLOOR_CMD = "FLOOR";

    /** An action command. */
    private static final String ROUND_CMD = "ROUND";

    /** An action command. */
    private static final String SQRT_CMD = "SQRT";

    /** An action command. */
    private static final String CBRT_CMD = "CBRT";

    /** An action command. */
    private static final String TO_DEG_CMD = "TO_DEG";

    /** An action command. */
    private static final String TO_RAD_CMD = "TO_RAD";

    /** An action command. */
    private static final String NOT_CMD = "NOT";

    /** An action command. */
    private static final String GCD_CMD = "GCD";

    /** An action command. */
    private static final String SRAD2_CMD = "SRAD2";

    /** An action command. */
    private static final String SRAD3_CMD = "SRAD3";

    /** An action command. */
    private static final String TEST_CMD = "TEST";

    /** The owning panel. */
    private final FormulaEditorPanel owner;

    /**
     * Constructs a new {@code FormulaEditorPalette}.
     *
     * @param theOwner the owning panel
     */
    FormulaEditorPalette(final FormulaEditorPanel theOwner) {

        super(new StackedBorderLayout());

        this.owner = theOwner;

        final JPanel line1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        add(line1, StackedBorderLayout.NORTH);

        final JPanel line2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        add(line2, StackedBorderLayout.NORTH);

        final JPanel line3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        add(line3, StackedBorderLayout.NORTH);

        final JPanel line4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        add(line4, StackedBorderLayout.NORTH);

        final JButton piButton = new JButton("\u03C0");
        piButton.setActionCommand(PI_CMD);
        piButton.addActionListener(this);
        line1.add(piButton);

        final JButton eButton = new JButton("e");
        eButton.setActionCommand(E_CMD);
        eButton.addActionListener(this);
        line1.add(eButton);

        final JButton trueButton = new JButton("TRUE");
        trueButton.setActionCommand(TRUE_CMD);
        trueButton.addActionListener(this);
        line1.add(trueButton);

        final JButton falseButton = new JButton("FALSE");
        falseButton.setActionCommand(FALSE_CMD);
        falseButton.addActionListener(this);
        line1.add(falseButton);

        final Dimension falsePrefSize = falseButton.getPreferredSize();
        piButton.setPreferredSize(falsePrefSize);
        eButton.setPreferredSize(falsePrefSize);

        final JButton absButton = new JButton("abs()");
        absButton.setActionCommand(ABS_CMD);
        absButton.addActionListener(this);
        line1.add(absButton);

        final JButton cosButton = new JButton("cos()");
        cosButton.setActionCommand(COS_CMD);
        cosButton.addActionListener(this);
        line1.add(cosButton);

        final JButton sinButton = new JButton("sin()");
        sinButton.setActionCommand(SIN_CMD);
        sinButton.addActionListener(this);
        line1.add(sinButton);

        final JButton tanButton = new JButton("tan()");
        tanButton.setActionCommand(TAN_CMD);
        tanButton.addActionListener(this);
        line1.add(tanButton);

        //

        final JButton acosButton = new JButton("acos()");
        acosButton.setActionCommand(ACOS_CMD);
        acosButton.addActionListener(this);
        line2.add(acosButton);

        final JButton asinButton = new JButton("asin()");
        asinButton.setActionCommand(ASIN_CMD);
        asinButton.addActionListener(this);
        line2.add(asinButton);

        final JButton atanButton = new JButton("atan()");
        atanButton.setActionCommand(ATAN_CMD);
        atanButton.addActionListener(this);
        line2.add(atanButton);

        final JButton expButton = new JButton("exp()");
        expButton.setActionCommand(EXP_CMD);
        expButton.addActionListener(this);
        line2.add(expButton);

        final JButton logButton = new JButton("log()");
        logButton.setActionCommand(LOG_CMD);
        logButton.addActionListener(this);
        line2.add(logButton);

        final JButton ceilButton = new JButton("ceil()");
        ceilButton.setActionCommand(CEIL_CMD);
        ceilButton.addActionListener(this);
        line2.add(ceilButton);

        final JButton floorButton = new JButton("floor()");
        floorButton.setActionCommand(FLOOR_CMD);
        floorButton.addActionListener(this);
        line2.add(floorButton);

        final JButton roundButton = new JButton("round()");
        roundButton.setActionCommand(ROUND_CMD);
        roundButton.addActionListener(this);
        line2.add(roundButton);

        //

        final JButton sqrtButton = new JButton("sqrt()");
        sqrtButton.setActionCommand(SQRT_CMD);
        sqrtButton.addActionListener(this);
        line3.add(sqrtButton);

        final JButton cbrtButton = new JButton("cbrt()");
        cbrtButton.setActionCommand(CBRT_CMD);
        cbrtButton.addActionListener(this);
        line3.add(cbrtButton);

        final JButton toDegButton = new JButton("toDeg()");
        toDegButton.setActionCommand(TO_DEG_CMD);
        toDegButton.addActionListener(this);
        line3.add(toDegButton);

        final JButton toRadButton = new JButton("toRad()");
        toRadButton.setActionCommand(TO_RAD_CMD);
        toRadButton.addActionListener(this);
        line3.add(toRadButton);

        final JButton notButton = new JButton("not()");
        notButton.setActionCommand(NOT_CMD);
        notButton.addActionListener(this);
        line3.add(notButton);

        final JButton gcdButton = new JButton("gcd()");
        gcdButton.setActionCommand(GCD_CMD);
        gcdButton.addActionListener(this);
        line3.add(gcdButton);

        final JButton srad2Button = new JButton("srad2()");
        srad2Button.setActionCommand(SRAD2_CMD);
        srad2Button.addActionListener(this);
        line3.add(srad2Button);

        final JButton srad3Button = new JButton("srad3()");
        srad3Button.setActionCommand(SRAD3_CMD);
        srad3Button.addActionListener(this);
        line3.add(srad3Button);

        //

        final JButton testButton = new JButton("IF - THEN - ELSE");
        testButton.setActionCommand(TEST_CMD);
        testButton.addActionListener(this);
        line4.add(testButton);
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (PI_CMD.equals(cmd)) {
            this.owner.typeChar('\u03C0');
        } else if (E_CMD.equals(cmd)) {
            this.owner.typeChar('\u0435');
        } else if (TRUE_CMD.equals(cmd)) {
            this.owner.typeChar('\u22a4');
        } else if (FALSE_CMD.equals(cmd)) {
            this.owner.typeChar('\u22a5');
        } else if (ABS_CMD.equals(cmd)) {
            this.owner.typeChar('\u2720');
        } else if (COS_CMD.equals(cmd)) {
            this.owner.typeChar('\u2721');
        } else if (SIN_CMD.equals(cmd)) {
            this.owner.typeChar('\u2722');
        } else if (TAN_CMD.equals(cmd)) {
            this.owner.typeChar('\u2723');
        } else if (ACOS_CMD.equals(cmd)) {
            this.owner.typeChar('\u2724');
        } else if (ASIN_CMD.equals(cmd)) {
            this.owner.typeChar('\u2725');
        } else if (ATAN_CMD.equals(cmd)) {
            this.owner.typeChar('\u2726');
        } else if (EXP_CMD.equals(cmd)) {
            this.owner.typeChar('\u2727');
        } else if (LOG_CMD.equals(cmd)) {
            this.owner.typeChar('\u2728');
        } else if (CEIL_CMD.equals(cmd)) {
            this.owner.typeChar('\u2729');
        } else if (FLOOR_CMD.equals(cmd)) {
            this.owner.typeChar('\u272A');
        } else if (ROUND_CMD.equals(cmd)) {
            this.owner.typeChar('\u272B');
        } else if (SQRT_CMD.equals(cmd)) {
            this.owner.typeChar('\u272C');
        } else if (CBRT_CMD.equals(cmd)) {
            this.owner.typeChar('\u272D');
        } else if (TO_DEG_CMD.equals(cmd)) {
            this.owner.typeChar('\u272E');
        } else if (TO_RAD_CMD.equals(cmd)) {
            this.owner.typeChar('\u272F');
        } else if (NOT_CMD.equals(cmd)) {
            this.owner.typeChar('\u2730');
        } else if (GCD_CMD.equals(cmd)) {
            this.owner.typeChar('\u2731');
        } else if (SRAD2_CMD.equals(cmd)) {
            this.owner.typeChar('\u2732');
        } else if (SRAD3_CMD.equals(cmd)) {
            this.owner.typeChar('\u2733');
        } else if (TEST_CMD.equals(cmd)) {
            this.owner.typeChar('<');
        }

        this.owner.requestFocus();
    }
}

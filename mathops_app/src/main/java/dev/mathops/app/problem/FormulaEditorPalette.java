package dev.mathops.app.problem;

import dev.mathops.assessment.formula.edit.FormulaEditorPanel;
import dev.mathops.core.ui.layout.StackedBorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
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

    /** A button to enter the "Pi" symbol. */
    private final JButton piButton;

    /** A button to enter the "E" symbol. */
    private final JButton eButton;

    /** A button to enter the "TRUE" symbol. */
    private final JButton trueButton;

    /** A button to enter the "FALSE" symbol. */
    private final JButton falseButton;

    /** A button to enter the "abs()" function. */
    private final JButton absButton;

    /** A button to enter the "cos()" function. */
    private final JButton cosButton;

    /** A button to enter the "sin()" function. */
    private final JButton sinButton;

    /** A button to enter the "tan()" function. */
    private final JButton tanButton;

    /** A button to enter the "acos()" function. */
    private final JButton acosButton;

    /** A button to enter the "asin()" function. */
    private final JButton asinButton;

    /** A button to enter the "atan()" function. */
    private final JButton atanButton;

    /** A button to enter the "exp()" function. */
    private final JButton expButton;

    /** A button to enter the "log()" function. */
    private final JButton logButton;

    /** A button to enter the "ceil()" function. */
    private final JButton ceilButton;

    /** A button to enter the "floor()" function. */
    private final JButton floorButton;

    /** A button to enter the "round()" function. */
    private final JButton roundButton;

    /** A button to enter the "sqrt()" function. */
    private final JButton sqrtButton;

    /** A button to enter the "cbrt()" function. */
    private final JButton cbrtButton;

    /** A button to enter the "toDeg()" function. */
    private final JButton toDegButton;

    /** A button to enter the "toRad()" function. */
    private final JButton toRadButton;

    /** A button to enter the "not()" function. */
    private final JButton notButton;

    /** A button to enter the "gcd()" function. */
    private final JButton gcdButton;

    /** A button to enter the "srad2()" function. */
    private final JButton srad2Button;

    /** A button to enter the "srad3()" function. */
    private final JButton srad3Button;

    /** A button to enter the "test(?:)" construction. */
    private final JButton testButton;

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

        this.piButton = new JButton("\u03C0");
        this.piButton.setActionCommand(PI_CMD);
        this.piButton.addActionListener(this);
        line1.add(this.piButton);

        this.eButton = new JButton("e");
        this.eButton.setActionCommand(E_CMD);
        this.eButton.addActionListener(this);
        line1.add(this.eButton);

        this.trueButton = new JButton("TRUE");
        this.trueButton.setActionCommand(TRUE_CMD);
        this.trueButton.addActionListener(this);
        line1.add(this.trueButton);

        this.falseButton = new JButton("FALSE");
        this.falseButton.setActionCommand(FALSE_CMD);
        this.falseButton.addActionListener(this);
        line1.add(this.falseButton);

        this.piButton.setPreferredSize(this.falseButton.getPreferredSize());
        this.eButton.setPreferredSize(this.falseButton.getPreferredSize());

        this.absButton = new JButton("abs()");
        this.absButton.setActionCommand(ABS_CMD);
        this.absButton.addActionListener(this);
        line1.add(this.absButton);

        this.cosButton = new JButton("cos()");
        this.cosButton.setActionCommand(COS_CMD);
        this.cosButton.addActionListener(this);
        line1.add(this.cosButton);

        this.sinButton = new JButton("sin()");
        this.sinButton.setActionCommand(SIN_CMD);
        this.sinButton.addActionListener(this);
        line1.add(this.sinButton);

        this.tanButton = new JButton("tan()");
        this.tanButton.setActionCommand(TAN_CMD);
        this.tanButton.addActionListener(this);
        line1.add(this.tanButton);

        //

        this.acosButton = new JButton("acos()");
        this.acosButton.setActionCommand(ACOS_CMD);
        this.acosButton.addActionListener(this);
        line2.add(this.acosButton);

        this.asinButton = new JButton("asin()");
        this.asinButton.setActionCommand(ASIN_CMD);
        this.asinButton.addActionListener(this);
        line2.add(this.asinButton);

        this.atanButton = new JButton("atan()");
        this.atanButton.setActionCommand(ATAN_CMD);
        this.atanButton.addActionListener(this);
        line2.add(this.atanButton);

        this.expButton = new JButton("exp()");
        this.expButton.setActionCommand(EXP_CMD);
        this.expButton.addActionListener(this);
        line2.add(this.expButton);

        this.logButton = new JButton("log()");
        this.logButton.setActionCommand(LOG_CMD);
        this.logButton.addActionListener(this);
        line2.add(this.logButton);

        this.ceilButton = new JButton("ceil()");
        this.ceilButton.setActionCommand(CEIL_CMD);
        this.ceilButton.addActionListener(this);
        line2.add(this.ceilButton);

        this.floorButton = new JButton("floor()");
        this.floorButton.setActionCommand(FLOOR_CMD);
        this.floorButton.addActionListener(this);
        line2.add(this.floorButton);

        this.roundButton = new JButton("round()");
        this.roundButton.setActionCommand(ROUND_CMD);
        this.roundButton.addActionListener(this);
        line2.add(this.roundButton);

        //

        this.sqrtButton = new JButton("sqrt()");
        this.sqrtButton.setActionCommand(SQRT_CMD);
        this.sqrtButton.addActionListener(this);
        line3.add(this.sqrtButton);

        this.cbrtButton = new JButton("cbrt()");
        this.cbrtButton.setActionCommand(CBRT_CMD);
        this.cbrtButton.addActionListener(this);
        line3.add(this.cbrtButton);

        this.toDegButton = new JButton("toDeg()");
        this.toDegButton.setActionCommand(TO_DEG_CMD);
        this.toDegButton.addActionListener(this);
        line3.add(this.toDegButton);

        this.toRadButton = new JButton("toRad()");
        this.toRadButton.setActionCommand(TO_RAD_CMD);
        this.toRadButton.addActionListener(this);
        line3.add(this.toRadButton);

        this.notButton = new JButton("not()");
        this.notButton.setActionCommand(NOT_CMD);
        this.notButton.addActionListener(this);
        line3.add(this.notButton);

        this.gcdButton = new JButton("gcd()");
        this.gcdButton.setActionCommand(GCD_CMD);
        this.gcdButton.addActionListener(this);
        line3.add(this.gcdButton);

        this.srad2Button = new JButton("srad2()");
        this.srad2Button.setActionCommand(SRAD2_CMD);
        this.srad2Button.addActionListener(this);
        line3.add(this.srad2Button);

        this.srad3Button = new JButton("srad3()");
        this.srad3Button.setActionCommand(SRAD3_CMD);
        this.srad3Button.addActionListener(this);
        line3.add(this.srad3Button);

        //

        this.testButton = new JButton("IF - THEN - ELSE");
        this.testButton.setActionCommand(TEST_CMD);
        this.testButton.addActionListener(this);
        line4.add(this.testButton);
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

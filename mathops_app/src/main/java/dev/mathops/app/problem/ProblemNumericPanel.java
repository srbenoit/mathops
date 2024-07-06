package dev.mathops.app.problem;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.problem.template.ProblemNumericTemplate;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import jwabbit.gui.CalculatorPanel;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.regex.Pattern;

/**
 * A panel that contains a numeric answer problem.
 */
public final class ProblemNumericPanel extends AbstractProblemPanelBase
        implements DocumentListener, ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 5183718366808725415L;

    /** A compiled patern. */
    private static final Pattern COMPILED = Pattern.compile(CoreConstants.COMMA);

    /** The numeric answer problem being presented. */
    private final ProblemNumericTemplate problem;

    /** The base size for the entry label. */
    private static final float entryBaseFontSize = 18.0f;

    /** The label for the entry box. */
    private JLabel entryLbl;

    /** The base size for the answer. */
    private static final float answerBaseFontSize = 18.0f;

    /** The text field in which the student enters the answer. */
    private JTextField answer;

    /** The label to display the correct answer, if so configured. */
    private JLabel correct;

    /**
     * Construct a new {@code ProblemNumericPanel}.
     *
     * @param theProblem    the problem to render in the panel
     * @param theCalculator the panel showing the calculator
     * @param showAnswer    true to display the answers, false otherwise
     * @param showSolution  true to display the solution, false otherwise
     */
    public ProblemNumericPanel(final ProblemNumericTemplate theProblem, final CalculatorPanel theCalculator,
                               final boolean showAnswer, final boolean showSolution) {

        super(theCalculator, showAnswer, showSolution);

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        // setCalculatorProfile(theProblem.calculator);

        setLayout(new NumericProblemLayout(theProblem, 20, showAnswer, showSolution,
                theProblem.evalContext));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        this.problem = theProblem;

        buildUI();
    }

    /**
     * Enable or disable the panel.
     *
     * @param enabled true to enable; false to disable
     */
    @Override
    public void setEnabled(final boolean enabled) {

        super.setEnabled(enabled);
        this.answer.setEnabled(enabled);
    }

    /**
     * Construct the panel user interface.
     */
    private void buildUI() {

        double dvar;
        String txt = null;
        DecimalFormat df = new DecimalFormat("#.######");
        final JButton btn;

        // Create the entry field label
        Font fnt = new Font("Dialog", Font.PLAIN, (int) entryBaseFontSize);
        this.entryLbl = new JLabel("Enter your answer here:  ");
        this.entryLbl.setFont(fnt);
        add(this.entryLbl, NumericProblemLayout.ENTRY_LABEL);

        // Create the answer field
        fnt = new Font("Monospaced", Font.PLAIN, (int) answerBaseFontSize);
        this.answer = new JTextField(15);
        this.answer.setFont(fnt);
        final Object[] ans = this.problem.getAnswer();

        if (ans != null && ans.length > 0) {

            if (ans[0] instanceof String) {
                this.answer.setText((String) ans[0]);
            } else if (ans[0] instanceof Long) {
                this.answer.setText(((Long) ans[0]).toString());
            } else if (ans[0] instanceof Double) {
                this.answer.setText(df.format(((Double) ans[0]).doubleValue()));
            }
        }

        // If answers shown, disable answer entry.
        this.answer.setEnabled(!this.showAnswers);

        add(this.answer, NumericProblemLayout.ENTRY_FIELD);

        this.answer.getDocument().addDocumentListener(this);

        if (this.showAnswers) {
            dvar = 1.0;
            final Object var = this.problem.acceptNumber == null ? null
                    : this.problem.acceptNumber.getVarianceValue(this.problem.evalContext);

            if (var instanceof Long) {
                dvar = ((Long) var).doubleValue();
            } else if (var instanceof Double) {
                dvar = ((Double) var).doubleValue();
            }

            if (dvar >= 0.9) {
                df = new DecimalFormat("#");
            } else if (dvar >= 0.09) {
                df = new DecimalFormat("#.#");
            } else if (dvar >= 0.009) {
                df = new DecimalFormat("#.##");
            } else if (dvar >= 0.0009) {
                df = new DecimalFormat("#.###");
            } else if (dvar >= 0.00009) {
                df = new DecimalFormat("#.####");
            } else if (dvar >= 0.000009) {
                df = new DecimalFormat("#.#####");
            } else if (dvar >= 0.0000009) {
                df = new DecimalFormat("#.######");
            } else {
                df = new DecimalFormat("#.#######");
            }

            final Object cor = this.problem.acceptNumber == null ? null
                    : this.problem.acceptNumber.getCorrectAnswerValue(this.problem.evalContext);

            switch (cor) {
                case Long l -> txt = cor.toString();
                case Double v -> txt = df.format(v.doubleValue());
                case null -> Log.warning("Null correct answer");
                default -> Log.warning("Unsupported correct answer type: " + cor.getClass().getName());
            }

            // Special case handling
            if ("-0".equals(txt)) {
                txt = "0";
            }

            this.correct = new JLabel(txt == null ? "No correct answer provided."
                    : "The correct answer is " + txt.replace('-', '\u2013'));
            this.correct.setFont(this.problem.question.getFont());
            this.correct.setForeground(Color.BLUE);
            add(this.correct, NumericProblemLayout.CORRECT_ANSWER);
        }

        if (this.calculator != null && this.calculator.isCalculatorAvailable()) {
            btn = new JButton("Copy answer from calculator");
            btn.setActionCommand("CopyAnswer");
            btn.addActionListener(this);
            add(btn, NumericProblemLayout.COPY_FROM_CALCULATOR);
        }
    }

    /**
     * Implementation of the {@code DocumentListener} interface.
     *
     * @param e the document change event
     */
    @Override
    public void changedUpdate(final DocumentEvent e) {

        processChange();
    }

    /**
     * Implementation of the {@code DocumentListener} interface.
     *
     * @param e the document change event
     */
    @Override
    public void insertUpdate(final DocumentEvent e) {

        processChange();
    }

    /**
     * Implementation of the {@code DocumentListener} interface.
     *
     * @param e the document change event
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {

        processChange();
    }

    /**
     * Handler for all changes to the text area content.
     */
    private void processChange() {

        final String txt = this.answer.getText();

        String cleaned;
        if (txt == null) {
            cleaned = null;
        } else {
            cleaned = COMPILED.matcher(txt.trim()).replaceAll(CoreConstants.EMPTY);

            if (!cleaned.isEmpty() && cleaned.charAt(0) == '(' && cleaned.charAt(cleaned.length() - 1) == ')') {
                cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
            }
            if (!cleaned.isEmpty() && cleaned.charAt(0) == '$') {
                cleaned = cleaned.substring(1);
            }
            if (!cleaned.isEmpty() && cleaned.charAt(cleaned.length() - 1) == '%') {
                cleaned = cleaned.substring(0, cleaned.length() - 1);
            }
        }

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        final Serializable[] answers = new Serializable[1];

        // Try to convert to integer first
        if (cleaned != null && !cleaned.isEmpty()) {

            if (!CoreConstants.DASH.equals(cleaned) && !CoreConstants.DOT.equals(cleaned)) {

                try {
                    answers[0] = Long.valueOf(cleaned);
                } catch (final NumberFormatException e) {

                    // Try to convert to a double next
                    try {
                        final int pos = cleaned.indexOf('/');

                        if (pos == -1) {
                            answers[0] = Double.valueOf(cleaned);
                        } else {
                            final double denom = Double.valueOf(cleaned.substring(pos + 1)).doubleValue();
                            if (denom == 0.0) {
                                throw new NumberFormatException("Invalid fraction");
                            }

                            final double numer = Double.valueOf(cleaned.substring(0, pos)).doubleValue();
                            answers[0] = Double.valueOf(numer / denom);
                        }

                        if (this.problem.acceptNumber != null && this.problem.acceptNumber.forceInteger) {
                            final double value = ((Double) answers[0]).doubleValue();
                            answers[0] = Long.valueOf(Math.round(value));
                        }
                    } catch (final NumberFormatException e2) {

                        // Bad format - make the field red.
                        this.answer.setBackground(new Color(255, 100, 100));

                        return;
                    }
                }
            }
        }

        if (answers[0] != null) {
            this.problem.recordAnswer(answers);
            this.recordAnswer(answers);
        } else {
            this.problem.clearAnswer();
        }

        this.answer.setBackground(Color.WHITE);
    }

    /**
     * Set the visibility of the entry field. This is intended to be used as part of the image export feature.
     *
     * @param visible true to make the choices/answer entry box visible, false to hide them
     */
    @Override
    public void setEntryVisibility(final boolean visible) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.entryLbl != null) {
            this.entryLbl.setVisible(visible);
        }

        if (this.answer != null) {
            this.answer.setVisible(visible);
        }

        revalidate();
        repaint();
    }

    /**
     * Set the visibility of the answers. This is intended to be used as part of the image export feature.
     *
     * @param visible true to make the choices/answer entry box visible, false to hide them
     */
    @Override
    public void setAnswerVisibility(final boolean visible) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.correct != null) {
            this.correct.setVisible(visible);
        }

        this.showSolutions = visible;
        this.answer.setEnabled(!(this.showSolutions || this.showAnswers));

        revalidate();
        repaint();
    }

    /**
     * Set the visibility of the solution. This is intended to be used as part of the image export feature.
     *
     * @param visible true to make the solution visible, false to hide it
     */
    @Override
    public void setSolutionVisibility(final boolean visible) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.showSolutions = visible;
        this.answer.setEnabled(!(this.showSolutions || this.showAnswers));

        revalidate();
        repaint();
    }

    /**
     * Draw the panel.
     *
     * @param g the {@code Graphics} to which to draw
     */
    @Override
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.problem.question.paintComponent(g, ELayoutMode.TEXT);

        if (this.showSolutions && this.problem.solution != null) {
            this.problem.solution.paintComponent(g, ELayoutMode.TEXT);
        }
    }

    /**
     * Handler for action events sent by the button that copies the answer from the calculator to the entry field.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        if ("CopyAnswer".equals(e.getActionCommand())) {
            if (this.calculator == null) {
                return;
            }

            // Old method: wait in the AWT event thread for results of copy...
            // final String ans = this.calculator.getAns();
            // if (ans != null) {
            // this.answer.setText(ans);
            // }

            // New method: enqueue the request, have the calculator thread install the answer in
            // the target field using SwingUtilities.invokeLater
            this.calculator.copyAns(this.answer);
        }
    }

    /**
     * Sets the relative size.
     *
     * @param relSize the size, from -3 to +5.
     */
    @Override
    public void setRelativeSize(final int relSize) {

        if (this.problem.question != null) {
            this.problem.question.setRelativeSize(relSize);
            this.problem.question.doLayout(this.problem.evalContext, ELayoutMode.TEXT);
        }

        if (this.problem.solution != null) {
            this.problem.solution.setRelativeSize(relSize);
            this.problem.solution.doLayout(this.problem.evalContext, ELayoutMode.TEXT);
        }

        final float fontFactor = (float) StrictMath.pow(2.5, (double) relSize / 4.0);

        if (this.answer != null) {
            this.answer.setFont(this.answer.getFont().deriveFont(answerBaseFontSize * fontFactor));
        }

        if (this.entryLbl != null) {
            this.entryLbl.setFont(this.entryLbl.getFont().deriveFont(entryBaseFontSize * fontFactor));
        }

        if (this.correct != null) {
            final float correctBaseFontSize = AbstractDocObjectTemplate.DEFAULT_BASE_FONT_SIZE;
            this.correct
                    .setFont(this.correct.getFont().deriveFont(correctBaseFontSize * fontFactor));
        }

        revalidate();
    }
}

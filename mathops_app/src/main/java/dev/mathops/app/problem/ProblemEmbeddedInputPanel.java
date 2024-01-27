package dev.mathops.app.problem;

import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.template.AbstractDocInput;
import dev.mathops.assessment.document.template.DocColumnPanel;
import dev.mathops.assessment.document.template.InputChangeListener;
import dev.mathops.assessment.problem.template.ProblemEmbeddedInputTemplate;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import jwabbit.gui.CalculatorPanel;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.io.Serial;

/**
 * A panel that contains an embedded-input problem.
 */
public final class ProblemEmbeddedInputPanel extends AbstractProblemPanelBase implements InputChangeListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -9047911427563122774L;

    /** Flag indicating inputs should always be enabled. */
    private final boolean isAlwaysEnabled;

    /** The embedded input problem being presented. */
    private final ProblemEmbeddedInputTemplate problem;

    /** The panel containing the question. */
    private DocColumnPanel question;

    /** The panel containing the solution, if any. */
    private DocColumnPanel solution;

    /** The panel to display the correctness of the answer. */
    private DocColumnPanel correctAnswer;

    /** The base font size for the "correct" label. */
    private float correctBaseFontSize;

    /** The label to display the correct answer, if so configured. */
    private JLabel correct;

    /**
     * Construct a new {@code ProblemEmbeddedInputPanel}.
     *
     * @param theProblem    the problem to render in the panel
     * @param theCalculator the panel showing the calculator
     * @param showAnswer    true to display the answers, false otherwise
     * @param showSolution  true to display the solution, false otherwise
     * @param alwaysEnable  true to enable inputs even when solutions shown
     */
    public ProblemEmbeddedInputPanel(final ProblemEmbeddedInputTemplate theProblem, final CalculatorPanel theCalculator,
                                     final boolean showAnswer, final boolean showSolution, final boolean alwaysEnable) {

        super(theCalculator, showAnswer, showSolution);

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.isAlwaysEnabled = alwaysEnable;

        // setCalculatorProfile(theProblem.getCalculator());

        final LayoutManager layout = new EmbeddedInputProblemLayout(theProblem, 20, showAnswer, showSolution);
        setLayout(layout);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        this.problem = theProblem;

        buildUI(theProblem.evalContext);

        theProblem.question.addInputChangeListener(this);
    }

    /**
     * Enable or disable the panel.
     *
     * @param enabled true to enable; false to disable
     */
    @Override
    public void setEnabled(final boolean enabled) {

        super.setEnabled(enabled);

        this.question.setEnabled(enabled);
        this.question.requestFocusInWindow();
    }

    /**
     * Set the background color for the panel.
     *
     * @param bg the background color
     */
    @Override
    public void setBackground(final Color bg) {

        super.setBackground(bg);

        if (this.question != null) {
            this.question.setBackground(bg);
        }

        if (this.solution != null) {
            this.solution.setBackground(bg);
        }
    }

    /**
     * Construct the panel user interface.
     *
     * @param context the evaluation context
     */
    private void buildUI(final EvalContext context) {

        this.question = new DocColumnPanel(this.problem.question, context);
        add(this.question, EmbeddedInputProblemLayout.QUESTION);

        // If answers shown, disable answer entry.
        this.question.setEnabled(this.isAlwaysEnabled || !(this.showAnswers || this.showSolutions));

        this.correct = new JLabel(CoreConstants.EMPTY);
        this.correct.setFont(this.problem.question.getFont());
        this.correctBaseFontSize = (float) this.correct.getFont().getSize();
        this.correct.setForeground(Color.BLUE);
        this.correct.setVisible(this.showAnswers || this.showSolutions);
        updateCorrectnessLabel();
        add(this.correct, EmbeddedInputProblemLayout.CORRECTNESS);

        if (this.problem.solution != null) {
            this.solution = new DocColumnPanel(this.problem.solution, context);
            this.solution.setVisible(this.showSolutions);
            add(this.solution, EmbeddedInputProblemLayout.SOLUTION);
        }

        if (this.problem.correctAnswer != null) {
            this.correctAnswer = new DocColumnPanel(this.problem.correctAnswer, context);
            this.correctAnswer.setFont(this.problem.correctAnswer.getFont());
            this.correctAnswer.setVisible(this.showAnswers);
            this.correctAnswer.setForeground(Color.BLUE);
            add(this.correctAnswer, EmbeddedInputProblemLayout.CORRECT_ANSWER);
        }

        this.question.requestFocusInWindow();
    }

    /**
     * Set the visibility of the entry blocks for students. This is intended to be used as part of the image export
     * feature.
     *
     * @param visible true to make the choices/answer entry box visible, false to hide them
     */
    @Override
    public void setEntryVisibility(final boolean visible) {

        // No action
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

        if (this.correctAnswer != null) {
            this.correctAnswer.setVisible(visible);
        }

        this.showAnswers = visible;

        revalidate();
        repaint();
    }

    /**
     * Set the visibility of the solutions. This is intended to be used as part of the image export feature.
     *
     * @param visible true to make the choices/answer entry box visible, false to hide them
     */
    @Override
    public void setSolutionVisibility(final boolean visible) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.solution != null) {
            this.solution.setVisible(visible);
        }

        this.showSolutions = visible;

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

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        super.paintComponent(g);

        this.question.requestFocusInWindow();
    }

    /**
     * Indication that an input's value has changed.
     *
     * @param source the input whose value has changed
     */
    @Override
    public void inputChanged(final AbstractDocInput source) {

        final Object[] values = this.question.column.getInputValues();

        this.problem.recordAnswer(values); // Stores in student response,

        // updates completion time.

        // NOTE: this will fire answer listeners.
        recordAnswer(values);
    }

//    /**
//     * Get the {@code DocColumnPanel} showing the question.
//     *
//     * @return the question panel
//     */
//    public DocColumnPanel getQuestionPanel() {
//
//        return this.question;
//    }

    /**
     * Update the label used to display correctness based on the current answer.
     */
    public void updateCorrectnessLabel() {

        Object[] ans = null;
        final boolean isCorrect;
        final String msg;

        if (this.problem != null) {
            ans = this.problem.getAnswer();
        }

        if (ans == null) {
            msg = "No answer was entered              ";
        } else {
            isCorrect = this.problem.isCorrect(ans);
            msg = isCorrect ? "Your answer is correct.          " : "Your answer is not correct.    ";
        }

        if (this.correct != null) {
            this.correct.setText(msg);
            revalidate();
            repaint();
        }
    }

    /**
     * Sets the relative size.
     *
     * @param relSize the size, from -3 to +5.
     */
    @Override
    public void setRelativeSize(final int relSize) {

        if (this.question != null) {
            this.question.column.setRelativeSize(relSize);
            this.question.column.doLayout(this.problem.evalContext, ELayoutMode.TEXT);
        }
        if (this.solution != null) {
            this.solution.column.setRelativeSize(relSize);
            this.solution.column.doLayout(this.problem.evalContext, ELayoutMode.TEXT);
        }
        if (this.correctAnswer != null) {
            this.correctAnswer.column.setRelativeSize(relSize);
            this.correctAnswer.column.doLayout(this.problem.evalContext, ELayoutMode.TEXT);
        }

        final float fontFactor = (float) StrictMath.pow(2.5, (double) relSize / 4.0);
        this.correct.setFont(this.correct.getFont().deriveFont(this.correctBaseFontSize * fontFactor));
        revalidate();
        repaint();
    }
}

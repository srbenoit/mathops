package dev.mathops.app.assessment.problemauthor;

import dev.mathops.app.exam.CurrentProblemPanel;
import dev.mathops.app.problem.AbstractProblemPanelBase;
import dev.mathops.app.problem.AnswerListener;
import dev.mathops.app.problem.ProblemEmbeddedInputPanel;
import dev.mathops.assessment.document.template.AbstractDocInput;
import dev.mathops.assessment.document.template.InputChangeListener;
import dev.mathops.assessment.exam.EExamSessionState;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSession;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.log.LogEntry;
import dev.mathops.commons.log.LogWriter;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.Color;
import java.awt.Dimension;
import java.io.Serial;

/**
 * A pane that presents the generated problem.
 */
public final class GeneratedPane extends JPanel implements AnswerListener, InputChangeListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -293293998978914279L;

    /** The owning file pane. */
    private final FilePane owner;

    /** The current problem panel. */
    private final CurrentProblemPanel currentProblemPane;

    /** The current pane. */
    private AbstractProblemPanelBase currentPane;

    /** The problem this pane will render. */
    private AbstractProblemTemplate problem;

    /** The variables panel. */
    private final VariableValuesPanel variables;

    /** The split pane (problem on left, variables on right). */
    private final JSplitPane split;

    /**
     * Constructs a new {@code GeneratedPane}.
     *
     * @param theOwner the owning file pane
     * @param bg       the background color
     */
    GeneratedPane(final FilePane theOwner, final Color bg) {

        super(new StackedBorderLayout());
        setBackground(bg);

        this.owner = theOwner;

        final ExamObj exam = new ExamObj();
        final ExamSection examSection = new ExamSection();
        final ExamProblem examProblem = new ExamProblem(exam);
        exam.addSection(examSection);
        examSection.addProblem(examProblem);
        examSection.setProblemOrder(new int[]{0});
        final ExamSession examSession = new ExamSession(EExamSessionState.INTERACTING, exam);

        this.currentProblemPane = new CurrentProblemPanel(examSession, null, true);

        this.variables = new VariableValuesPanel(bg);

        this.split =
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.currentProblemPane, this.variables);

        add(this.split, StackedBorderLayout.CENTER);
    }

    /**
     * Gets the generated problem.
     *
     * @return the problem
     */
    public AbstractProblemTemplate getProblem() {

        return this.problem;
    }

    /**
     * Realizes a problem and displays both the results (in the "Realized Problem" pane) and any errors (in the
     * console).
     *
     * @param theProblem the problem
     * @param answers    true to show answers
     * @param solutions  true to show solutions (ignored if 'answers' is false)
     */
    public void realize(final AbstractProblemTemplate theProblem, final boolean answers,
                        final boolean solutions) {

        final Dimension mySize = getSize();
        final int problemWidth = (mySize.width << 2) / 5;
        this.currentProblemPane.setSize(problemWidth, mySize.height);
        this.owner.clearConsole();

        this.split.setDividerLocation(problemWidth + 5);

        if (this.problem != null) {
            this.problem.question.removeInputChangeListener(this);
        }

        this.problem = theProblem;
        theProblem.question.removeInputChangeListener(this);

        this.variables.setEvalContext(this.problem.evalContext);
        this.variables.clearVariableValues();

        if (this.currentPane != null) {
            this.currentPane.removeAnswerListener(this);
            remove(this.currentPane);
        }

        final EvalContext ctx = theProblem.evalContext;

        final LogWriter writer = Log.getWriter();
        writer.clearList();
        writer.startList(1000);

        if (theProblem.realize(ctx)) {
            final ExamObj exam = this.currentProblemPane.getExamSession().getExam();

            final ExamProblem examProblem = exam.getSection(0).getProblem(0);
            examProblem.clearProblems();
            examProblem.addProblem(theProblem);
            examProblem.setSelectedProblem(theProblem);

            this.currentProblemPane.setCurrentProblem(0, 0, this, false, false);
            this.currentProblemPane.setShowAnswersAndSolutions(answers, solutions);

            final JPanel visible = this.currentProblemPane.getVisiblePanel();

            if (visible instanceof final AbstractProblemPanelBase problemPanelBase) {
                this.currentPane = problemPanelBase;
            }

            this.variables.updateVariableValues();
        }

        final int numErrors = writer.getNumInList();
        if (numErrors > 0) {
            for (int i = 0; i < numErrors; ++i) {
                final LogEntry entry = writer.getListMessage(i);
                final String message = entry.getMessage();
                this.owner.logToConsole(message);
            }
        }
        writer.stopList();
        writer.clearList();

        theProblem.question.addInputChangeListener(this);

        revalidate();
        repaint();
    }

    /**
     * Called when the user enters something and the answer should be recorded.
     *
     * @param answer the new answer
     */
    @Override
    public void recordAnswer(final Object[] answer) {

        this.problem.recordAnswer(answer);

        if (this.currentPane instanceof final ProblemEmbeddedInputPanel emb) {
            emb.updateCorrectnessLabel();
        }
    }

    /**
     * Called to clear the answer
     */
    @Override
    public void clearAnswer() {

        this.problem.clearAnswer();
    }

    /**
     * Makes the display larger.
     */
    public void larger() {

        if (this.currentProblemPane != null) {
            this.currentProblemPane.larger();
        }
    }

    /**
     * Makes the display smaller.
     */
    public void smaller() {

        if (this.currentProblemPane != null) {
            this.currentProblemPane.smaller();
        }
    }

    /**
     * Called when an input changes.
     *
     * @param source the input that has changed
     */
    @Override
    public void inputChanged(final AbstractDocInput source) {

        this.variables.updateVariableValues();
    }
}

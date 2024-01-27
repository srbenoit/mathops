package dev.mathops.app.exam;

import dev.mathops.app.problem.AbstractProblemPanelBase;
import dev.mathops.app.problem.AnswerListener;
import dev.mathops.app.problem.ProblemAutoCorrectPanel;
import dev.mathops.app.problem.ProblemEmbeddedInputPanel;
import dev.mathops.app.problem.ProblemMultipleChoicePanel;
import dev.mathops.app.problem.ProblemMultipleSelectionPanel;
import dev.mathops.app.problem.ProblemNumericPanel;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSession;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemAutoCorrectTemplate;
import dev.mathops.assessment.problem.template.ProblemEmbeddedInputTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleChoiceTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleSelectionTemplate;
import dev.mathops.assessment.problem.template.ProblemNumericTemplate;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ColorNames;
import jwabbit.gui.CalculatorPanel;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.Serial;
import java.util.Objects;

/**
 * A panel in which to show the current question.
 */
public final class CurrentProblemPanel extends JPanel implements ComponentListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2415782742093867342L;

    /** The size adjustment. */
    private transient int sizeAdjustment;

    /** The exam session. */
    private final ExamSession examSession;

    /** The panel containing the current problem. */
    private JPanel visiblePanel;

    /** The scroll pane containing the panel. */
    private JScrollPane scroll;

    /** The panel showing the calculator. */
    private final CalculatorPanel calculator;

    /** Flag to support display of answers to problems. */
    private boolean showAnswers;

    /** Flag to support display of solutions to problems. */
    private boolean showSolutions;

    /** Flag to enable inputs regardless of answer/solution display. */
    private final boolean alwaysEnable;

    /** The index of the current section. */
    private int sectionIndex;

    /** The index of the current problem. */
    private int problemIndex;

    /** The current problem. */
    private AbstractProblemTemplate currentProblem;

    /** The answer listener. */
    private AnswerListener listener;

    /**
     * Constructs a new {@code CurrentProblemPanel}.
     *
     * @param theExamSession  the exam session
     * @param theCalculator   the panel showing the calculator
     * @param isAlwaysEnabled true to enable inputs regardless of visibility of solutions/answers
     */
    public CurrentProblemPanel(final ExamSession theExamSession, final CalculatorPanel theCalculator,
                               final boolean isAlwaysEnabled) {

        super(new BorderLayout());

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.showAnswers = theExamSession.getState().showAnswers;
        this.showSolutions = theExamSession.getState().showSolutions;
        this.alwaysEnable = isAlwaysEnabled;

        this.examSession = theExamSession;
        this.calculator = theCalculator;

        setBackground(ColorNames.getColor("white"));

        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2),
                BorderFactory.createLoweredBevelBorder()));
    }

    /**
     * Gets the exam session the panel is based on.
     *
     * @return the panel's exam
     */
    public ExamSession getExamSession() {

        return this.examSession;
    }

    /**
     * Gets the currently visible panel.
     *
     * @return the panel
     */
    public JPanel getVisiblePanel() {

        return this.visiblePanel;
    }

    /**
     * Sets the flag that controls whether answers will be shown on generated problems.
     *
     * @param shouldShowAnswers   {@code true} to show answers; {@code false} to hide answers
     * @param shouldShowSolutions {@code true} to show solutions; {@code false} to hide solutions
     */
    public void setShowAnswersAndSolutions(final boolean shouldShowAnswers, final boolean shouldShowSolutions) {

        this.showAnswers = shouldShowAnswers;
        this.showSolutions = shouldShowSolutions;

        if (this.listener != null) {
            setCurrentProblem(this.sectionIndex, this.problemIndex, this.listener, this.showAnswers,
                    this.showSolutions);
        }
    }

    /**
     * Displays the exam instructions.
     */
    public void showInstructions() {

        if (this.examSession == null) {
            Log.warning("Exam null when trying to show instructions", null);
        } else {
            final ShowInstructions setter = new ShowInstructions(this, this.visiblePanel, this.scroll);

            if (SwingUtilities.isEventDispatchThread()) {
                setter.run();
            } else {
                try {
                    SwingUtilities.invokeAndWait(setter);
                } catch (final Exception ex) {
                    Log.warning(ex);
                }
            }

            this.visiblePanel = setter.getPanel();
            this.scroll = setter.getScrollPane();
        }
    }

    /**
     * Sets the question that is to be displayed in the panel.
     *
     * @param theSectionIndex the index of the section
     * @param theProblemIndex the index of the problem
     * @param theListener     the {@code AnswerListener} that should receive notifications when a student's answer
     *                        changes
     * @param answers         true to show answers
     * @param solutions       true to show solutions
     */
    public void setCurrentProblem(final int theSectionIndex, final int theProblemIndex,
                                  final AnswerListener theListener, final boolean answers, final boolean solutions) {

        if (this.examSession == null) {
            Log.warning("Set current problem when no exam", null);
            return;
        }

        this.sectionIndex = theSectionIndex;
        this.problemIndex = theProblemIndex;
        this.listener = theListener;

        final ExamSection examSection = this.examSession.getExam().getSection(theSectionIndex);

        ExamProblem examProblem = null;

        if (examSection != null) {
            examProblem = examSection.getPresentedProblem(theProblemIndex);
        }

        if (examProblem == null) {
            this.currentProblem = null;
        } else {
            this.currentProblem = examProblem.getSelectedProblem();
        }

        if (this.currentProblem == null) {
            Log.warning("No selected problem");
            return;
        }

        final SetCurrentProblem setter = new SetCurrentProblem(this, this.examSession,
                this.currentProblem, this.visiblePanel, this.scroll, theListener, this.calculator,
                answers, solutions, this.alwaysEnable);

        if (SwingUtilities.isEventDispatchThread()) {
            setter.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(setter);
            } catch (final Exception ex) {
                Log.warning(ex);
            }
        }

        this.visiblePanel = setter.getPanel();
        this.scroll = setter.getScrollPane();
        updateFonts();
    }

    /**
     * Enables or disables this panel and all contained subpanels.
     *
     * @param enabled {@code true} to enable; {@code false} to disable
     */
    @Override
    public void setEnabled(final boolean enabled) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        super.setEnabled(enabled);

        if (this.visiblePanel != null) {
            this.visiblePanel.setEnabled(enabled);
        }
    }

    /**
     * Implementation of the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentHidden(final ComponentEvent e) { /* Empty */

    }

    /**
     * Implementation of the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentMoved(final ComponentEvent e) { /* Empty */

    }

    /**
     * Implementation of the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentResized(final ComponentEvent e) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.scroll != null) {
            final JViewport view = this.scroll.getViewport();

            if (this.visiblePanel != null) {

                // Set the view's width to the pane's width less that of the bar
                final int width =
                        this.scroll.getWidth() - this.scroll.getVerticalScrollBar().getWidth();
                view.setSize(new Dimension(width, view.getHeight()));

                // Re-layout the panel
                this.visiblePanel.setSize(view.getSize());
                this.visiblePanel.doLayout();
            }
        }
    }

    /**
     * Implementation of the ComponentListener interface.
     *
     * @param e the component event
     */
    @Override
    public void componentShown(final ComponentEvent e) {

        // No action
    }

    /**
     * Gets the current size adjustment.
     *
     * @return the size adjustment
     */
    private int getSizeAdjustment() {

        return this.sizeAdjustment;
    }

    /**
     * Make the window render larger, up to some limit.
     */
    public void larger() {

        if (this.sizeAdjustment < 5) {
            ++this.sizeAdjustment;
            updateFonts();
        }
    }

    /**
     * Make the window render smaller, down to some limit.
     */
    public void smaller() {

        if (this.sizeAdjustment > -3) {
            --this.sizeAdjustment;
            updateFonts();
        }
    }

    /**
     * Updates the color of the active panel to match that of the exam.
     */
    void updateColor() {

        if (this.visiblePanel != null && this.examSession != null) {
            this.visiblePanel.setBackground(this.examSession.getExam().backgroundColor);
        }
    }

    /**
     * Updates fonts of the existing panel using the current font scale.
     */
    private void updateFonts() {

        if (this.scroll != null && this.currentProblem != null) {
            final JViewport view = this.scroll.getViewport();

            if (this.visiblePanel != null) {

                if (this.visiblePanel instanceof final InstructionsPanel instr) {
                    instr.setRelativeSize(this.sizeAdjustment, this.currentProblem.evalContext);
                } else if (this.visiblePanel instanceof final AbstractProblemPanelBase prob) {
                    prob.setRelativeSize(this.sizeAdjustment);
                }

                // Set the view's width to the pane's width less that of the bar
                final int width =
                        this.scroll.getWidth() - this.scroll.getVerticalScrollBar().getWidth();
                view.setSize(new Dimension(width, view.getHeight()));

                // Re-layout the panel
                this.visiblePanel.setSize(view.getSize());
                this.visiblePanel.doLayout();
            }
        }
    }

    /**
     * Class to change the currently displayed problem from the AWT thread.
     */
    private final class SetCurrentProblem implements Runnable {

        /** The owning current problem panel. */
        private final CurrentProblemPanel owningPanel;

        /** The exam session. */
        private final ExamSession session;

        /** The selected problem. */
        private final AbstractProblemTemplate problem;

        /** The problem panel. */
        private JPanel panel;

        /** The scroll pane that will contain the problem panel. */
        private JScrollPane scrollPane;

        /** The listener to receive answer change notifications. */
        private final AnswerListener answerListener;

        /** The panel showing the calculator. */
        private final CalculatorPanel calc;

        /** True to show answers. */
        private final boolean isShowAnswers;

        /** True to show solutions. */
        private final boolean isShowSolutions;

        /** True to enable inputs regardless of solution/answer visibility. */
        private final boolean isAlwaysEnabled;

        /**
         * Constructs a new {@code SetCurrentProblem}.
         *
         * @param theOwner          the owning current problem panel
         * @param theExamSession    the exam session
         * @param theProblem        the selected problem
         * @param thePanel          the current problem panel
         * @param theScroll         the scroll pane that will contain the problem panel
         * @param theAnswerListener the listener to receive answer change notifications
         * @param theCalc           the panels showing the calculator
         * @param answers           true to show answers
         * @param solutions         true to show solutions
         * @param alwaysEnabled     true to always enable the panel
         */
        SetCurrentProblem(final CurrentProblemPanel theOwner, final ExamSession theExamSession,
                          final AbstractProblemTemplate theProblem, final JPanel thePanel,
                          final JScrollPane theScroll, final AnswerListener theAnswerListener,
                          final CalculatorPanel theCalc, final boolean answers, final boolean solutions,
                          final boolean alwaysEnabled) {

            this.owningPanel = theOwner;
            this.session = theExamSession;
            this.problem = theProblem;
            this.panel = thePanel;
            this.scrollPane = theScroll;
            this.answerListener = theAnswerListener;
            this.calc = theCalc;
            this.isShowAnswers = answers;
            this.isShowSolutions = solutions;
            this.isAlwaysEnabled = alwaysEnabled;
        }

        /**
         * Gets the problem panel generated by this class.
         *
         * @return the installed problem panel
         */
        JPanel getPanel() {

            return this.panel;
        }

        /**
         * Gets the scroll panel generated by this class.
         *
         * @return the installed scroll pane
         */
        JScrollPane getScrollPane() {

            return this.scrollPane;
        }

        /**
         * Performs the changes in the AWT thread.
         */
        @Override
        public void run() {

            if (!SwingUtilities.isEventDispatchThread()) {
                Log.warning(Res.get(Res.NOT_AWT_THREAD));
            }

            // Remove any prior problem from view
            if (this.scrollPane != null) {
                remove(this.scrollPane);
                this.scrollPane = null;
            } else if (this.panel != null) {
                remove(this.panel);
                this.panel = null;
            }

            // Compute the size for the problem panel.
            final Dimension sizeForProblemPanel = this.owningPanel.getSize();
            final Insets insets = this.owningPanel.getInsets();
            sizeForProblemPanel.width -= insets.left + insets.right + 3;
            sizeForProblemPanel.height -= insets.top + insets.bottom + 3;

            // Set layout management based on problem type (be sure to test for subclasses before
            // superclasses)

            if (this.problem instanceof final ProblemMultipleSelectionTemplate multsel) {
                this.panel = new ProblemMultipleSelectionPanel(multsel, this.calc,
                        this.isShowAnswers, this.isShowSolutions);
                ((AbstractProblemPanelBase) this.panel)
                        .setRelativeSize(this.owningPanel.getSizeAdjustment());
            } else if (this.problem instanceof final ProblemMultipleChoiceTemplate multchoice) {
                this.panel = new ProblemMultipleChoicePanel(multchoice, this.calc,
                        this.isShowAnswers, this.isShowSolutions);
                ((AbstractProblemPanelBase) this.panel)
                        .setRelativeSize(this.owningPanel.getSizeAdjustment());
            } else if (this.problem instanceof final ProblemNumericTemplate numeric) {
                this.panel = new ProblemNumericPanel(numeric, this.calc, this.isShowAnswers,
                        this.isShowSolutions);
                ((AbstractProblemPanelBase) this.panel)
                        .setRelativeSize(this.owningPanel.getSizeAdjustment());
            } else if (this.problem instanceof final ProblemEmbeddedInputTemplate embedded) {
                this.panel = new ProblemEmbeddedInputPanel(embedded, this.calc, this.isShowAnswers,
                        this.isShowSolutions, this.isAlwaysEnabled);
                ((AbstractProblemPanelBase) this.panel)
                        .setRelativeSize(this.owningPanel.getSizeAdjustment());
            } else if (this.problem instanceof final ProblemAutoCorrectTemplate autoCorrect) {
                this.panel = new ProblemAutoCorrectPanel(autoCorrect, this.calc, this.isShowAnswers,
                        this.isShowSolutions);
                ((AbstractProblemPanelBase) this.panel)
                        .setRelativeSize(this.owningPanel.getSizeAdjustment());
            } else {
                this.panel = new JPanel();
                this.panel.setLayout(new BorderLayout());

                final JTextArea area = new JTextArea();
                area.append("Unable to construct problem.");
                this.panel.add(area, BorderLayout.CENTER);
            }

            this.panel.setBackground(Objects.requireNonNullElseGet(this.session.getExam().backgroundColor,
                    () -> new Color(245, 245, 245)));

            // Lay out the panel to generate a preferred size
            this.panel.setSize(sizeForProblemPanel);
            this.panel.getLayout().layoutContainer(this.panel);

            int height = sizeForProblemPanel.height;

            if (this.panel.getPreferredSize().height > height) {
                height = this.panel.getPreferredSize().height;
            }

            this.panel.setSize(sizeForProblemPanel.width, height);

            // Add the panel to the scroll pane
            this.scrollPane = new JScrollPane(this.panel);
            this.scrollPane.setWheelScrollingEnabled(true);
            this.scrollPane.addComponentListener(this.owningPanel);
            this.scrollPane.getVerticalScrollBar().setUnitIncrement(36);
            this.scrollPane
                    .setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            add(this.scrollPane, BorderLayout.CENTER);

            if (this.panel instanceof AbstractProblemPanelBase) {
                ((AbstractProblemPanelBase) this.panel).addAnswerListener(this.answerListener);
                this.panel.requestFocusInWindow();
            }

            revalidate();
            repaint();
        }
    }

    /**
     * Presents the exam instructions in the panel.
     */
    private final class ShowInstructions implements Runnable {

        /** The owning current problem panel. */
        private final CurrentProblemPanel owningPanel;

        /** The problem panel. */
        private JPanel panel;

        /** The scroll pane that will contain the instructions panel. */
        private JScrollPane scrollPane;

        /**
         * Constructs a new {@code ShowInstructions}.
         *
         * @param theOwner  the owning current problem panel
         * @param thePanel  the instructions panel
         * @param theScroll the scroll pane that will contain the problem panel
         */
        ShowInstructions(final CurrentProblemPanel theOwner, final JPanel thePanel,
                         final JScrollPane theScroll) {

            this.owningPanel = theOwner;
            this.panel = thePanel;
            this.scrollPane = theScroll;
        }

        /**
         * Gets the instructions panel generated by this class.
         *
         * @return the installed problem panel
         */
        JPanel getPanel() {

            return this.panel;
        }

        /**
         * Gets the scroll panel generated by this class.
         *
         * @return the installed scroll pane
         */
        JScrollPane getScrollPane() {

            return this.scrollPane;
        }

        /**
         * Performs the changes in the AWT thread.
         */
        @Override
        public void run() {

            if (!SwingUtilities.isEventDispatchThread()) {
                Log.warning(Res.get(Res.NOT_AWT_THREAD));
            }

            // Remove any prior problem from view
            if (this.scrollPane != null) {
                remove(this.scrollPane);
                this.scrollPane = null;
            } else if (this.panel != null) {
                remove(this.panel);
                this.panel = null;
            }

            // Compute the size for the instructions panel.
            final Dimension sizeForInstructions = this.owningPanel.getSize();
            final Insets insets = this.owningPanel.getInsets();
            sizeForInstructions.width -= insets.left + insets.right;
            sizeForInstructions.height -= insets.top + insets.bottom;

            this.panel = new InstructionsPanel(this.owningPanel.getExamSession().getExam());

            // Lay out the panel to generate a preferred size
            this.panel.setSize(sizeForInstructions);
            this.panel.getLayout().layoutContainer(this.panel);

            int height = sizeForInstructions.height;

            if (this.panel.getPreferredSize().height > height) {
                height = this.panel.getPreferredSize().height;
            }

            this.panel.setSize(sizeForInstructions.width, height);

            add(this.panel, BorderLayout.CENTER);

            revalidate();
            repaint();
        }
    }
}

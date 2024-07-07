package dev.mathops.app.exam;

import dev.mathops.app.problem.AnswerListener;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.exam.ExamObj;
import dev.mathops.assessment.exam.ExamProblem;
import dev.mathops.assessment.exam.ExamSection;
import dev.mathops.assessment.exam.ExamSession;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemAcceptNumberTemplate;
import dev.mathops.assessment.problem.template.ProblemChoiceTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleChoiceTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleSelectionTemplate;
import dev.mathops.assessment.problem.template.ProblemNumericTemplate;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.ColorNames;
import dev.mathops.font.BundledFontManager;
import jwabbit.CalcBasicAction;
import jwabbit.ECalcAction;
import jwabbit.Launcher;
import jwabbit.gui.CalculatorPanel;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * The panel that runs the exam.
 */
public class ExamPanel extends JPanel implements ExamPanelInt, AnswerListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7773717464552213208L;

    /** The name of the logged-in user. */
    private final String username;

    /** The set of listeners registered to receive action events. */
    private List<ActionListener> listeners;

    /** The owning application. */
    private final ExamContainerInt owner;

    /** The exam session. */
    private final ExamSession examSession;

    /** The skin settings. */
    private final Properties skin;

    /** The minimum move-on score for the exam. */
    private final Integer minMoveOn;

    /** The minimum mastery score for the exam. */
    private final Integer minMastery;

    /** The top bar with the exam name, username, time, and time remaining. */
    private TopBarPanel top;

    /** The bottom bar with buttons to submit exam, access proctor controls. */
    private BottomBarPanel bottom;

    /** The side panel that lists the sections & questions on the exam. */
    private ProblemListPanel problems;

    /** A panel to present the current problem and gathers student response. */
    private CurrentProblemPanel currentProblem;

    /** Flag indicating answers should be pre-populated. */
    private final boolean populateAnswers;

    /** True if this is a practice exam; false for a graded exam. */
    private final boolean isPractice;

    /** A panel to present the calculator. */
    private CalculatorPanel calculator;

    /**
     * Constructs a new {@code ExamPanel}.
     *
     * @param theOwner           the {@code ExamContainerInt} that owns this panel
     * @param theSkin            the skin that controls the panel's look and behavior
     * @param theUsername        the name of the logged-in user
     * @param theExamSession     the exam session
     * @param thePopulateAnswers {@code true} if answers should be pre-populated
     * @param practice           {@code true} if this is a practice exam; {@code false} for a graded exam
     * @param theMinMoveOn       the minimum move-on score for the exam
     * @param theMinMastery      the minimum mastery score for the exam
     */
    public ExamPanel(final ExamContainerInt theOwner, final Properties theSkin,
                     final String theUsername, final ExamSession theExamSession,
                     final boolean thePopulateAnswers, final boolean practice, final Integer theMinMoveOn,
                     final Integer theMinMastery) {

        super(new BorderLayout());

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.owner = theOwner;
        this.skin = theSkin;
        this.username = theUsername;
        this.examSession = theExamSession;
        this.populateAnswers = thePopulateAnswers;
        this.isPractice = practice;
        this.minMoveOn = theMinMoveOn;
        this.minMastery = theMinMastery;
    }

    /**
     * Gets the exam session.
     *
     * @return the exam session
     */
    public final ExamSession getExamSession() {

        return this.examSession;
    }

    /**
     * Gets the name of the user taking the exam.
     *
     * @return the username
     */
    public final String getUsername() {

        return this.username;
    }

    /**
     * Constructs the exam delivery user interface.
     */
    public final void buildUI() {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        // Set default formatting for DocObject objects
        AbstractDocObjectTemplate.setDefaultFontName("SERIF");
        AbstractDocObjectTemplate.setDefaultFontSize(24);

        setBackground(Color.white);

        final JPanel center = new JPanel(new BorderLayout());
        add(center, BorderLayout.CENTER);

        // Set up the top bar
        final boolean zeroreq = this.minMoveOn != null && this.minMastery != null
                && this.minMoveOn.intValue() == 0 && this.minMastery.intValue() == 0;
        this.top = new TopBarPanel(this.examSession, this.username, this.isPractice, zeroreq, this.skin);
        center.add(this.top, BorderLayout.PAGE_START);

        // Set up the bottom bar
        this.bottom = new BottomBarPanel(this.examSession, this.isPractice, this.skin);
        this.bottom.addActionListener(this.owner);
        center.add(this.bottom, BorderLayout.PAGE_END);

        final Rectangle bounds = getBounds();
        final BundledFontManager bfm = BundledFontManager.getInstance();


        // Set up the calculator panel
        int calculatorWidth = 0;
        int calculatorHeight = 0;
        if ("true".equalsIgnoreCase(this.skin.getProperty("show-calculator"))) {
            // NOTE: 725 below is exactly half the native height of the calculator's skin - bigger than this looks
            // too big on the testing station...

            calculatorHeight = Math.min(725, this.owner.getFrame().getSize().height
                    - this.top.getPreferredSize().height - this.bottom.getPreferredSize().height);
            calculatorWidth = calculatorHeight * 700 / 1450;
        }

        // Set up the problem list
        if ("true".equalsIgnoreCase(this.skin.getProperty("show-problem-list"))) {

            this.problems = new ProblemListPanel(this, this.examSession, bfm, bounds.width - calculatorWidth);
            final JScrollPane scroll = new JScrollPane(this.problems, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.setWheelScrollingEnabled(true);
            scroll.getVerticalScrollBar().setUnitIncrement(36);
            center.add(scroll, BorderLayout.LINE_START);
        }

        // Set up the calculator panel
        if ("true".equalsIgnoreCase(this.skin.getProperty("show-calculator"))) {
            this.calculator = CalculatorPanel.getInstance();
            if (this.calculator != null) {
                this.calculator.setPreferredSize(new Dimension(calculatorWidth, calculatorHeight));
                this.calculator.setSize(calculatorWidth, calculatorHeight);

                decorateCalcPanel();

                add(this.calculator, BorderLayout.LINE_END);
                this.calculator.showCalculator(true);
            }
        }

        // Set up the current problem panel
        this.currentProblem = new CurrentProblemPanel(this.examSession, this.calculator, false);
        center.add(this.currentProblem, BorderLayout.CENTER);
        updateTop(0);

        // Start the clock
        if ("true".equalsIgnoreCase(this.skin.getProperty("run-timer"))) {
            new Thread(this.top).start();
            this.top.addTimerListener(this.owner);
        }
    }

    /**
     * Sets the background and border on the calculator panel.
     */
    private void decorateCalcPanel() {

        final String bgProp = this.skin.getProperty("top-bar-background-color");

        if (bgProp != null && ColorNames.isColorNameValid(bgProp)) {
            this.calculator.setBackground(ColorNames.getColor(bgProp));
        }

        final String bColor = this.skin.getProperty("top-bar-border-color");
        final String bSize = this.skin.getProperty("top-bar-border-size");
        final String bInset = this.skin.getProperty("top-bar-border-inset");

        if (bColor != null && ColorNames.isColorNameValid(bColor)) {

            final Color color = ColorNames.getColor(bColor);
            try {
                final int border = bSize == null ? 0 : Integer.parseInt(bSize);
                final int inset = bInset == null ? 0 : Integer.parseInt(bInset);

                if (inset > 0) {
                    if (border > 0) {
                        this.calculator.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createEmptyBorder(inset, inset, inset, inset),
                                BorderFactory.createLineBorder(color, border)));
                    } else {
                        this.calculator.setBorder(BorderFactory.createEmptyBorder(inset, inset, inset, inset));
                    }
                } else if (border > 0) {
                    this.calculator.setBorder(BorderFactory.createLineBorder(color, border));
                }

            } catch (final NumberFormatException ex) {
                // No action
            }
        }
    }

    /**
     * Selects a problem to present in the current problem panel.
     *
     * @param sectionIndex the index of the section
     * @param problemIndex the index of the problem
     */
    @Override
    public final void pickProblem(final int sectionIndex, final int problemIndex) {

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        this.owner.pickProblem(sectionIndex, problemIndex);

        // Identify the problem
        final ExamSection examSection = this.examSession.getExam().getSection(sectionIndex);
        ExamProblem examProblem = null;

        if (examSection != null) {
            examProblem = examSection.getProblem(problemIndex);
        } else {
            Log.warning("Exam does not have a section " + sectionIndex, null);
        }

        AbstractProblemTemplate problem = null;

        if (examProblem != null) {
            problem = examProblem.getSelectedProblem();
        }

        if (problem == null) {
            Log.warning("Exam does not have a problem " + problemIndex, null);
            return;
        }

        if (this.populateAnswers) {

            // Fill in the correct answer.
            switch (problem) {
                case final ProblemMultipleSelectionTemplate multisel -> {

                    // Fill in the correct answers
                    final List<ProblemChoiceTemplate> choices = multisel.getChoices();
                    final int numChoices = choices.size();
                    final List<Long> right = new ArrayList<>(numChoices);

                    for (int i = 0; i < numChoices; i++) {
                        final ProblemChoiceTemplate choice = choices.get(i);
                        final Object obj = choice.correct.evaluate(problem.evalContext);

                        if (Boolean.TRUE.equals(obj)) {
                            right.add(Long.valueOf((long) i + 1L));
                        }
                    }

                    if (!right.isEmpty()) {
                        final Serializable[] answers = new Serializable[right.size()];

                        final int rightLen = right.size();
                        for (int i = 0; i < rightLen; i++) {
                            answers[i] = right.get(i);
                        }

                        problem.recordAnswer(answers);
                    }
                }
                case final ProblemMultipleChoiceTemplate multChoice -> {

                    // Fill in the correct answers
                    final Serializable[] answers = new Long[1];

                    final List<ProblemChoiceTemplate> choices = multChoice.getChoices();
                    final int numChoices = choices.size();

                    for (int i = 0; i < numChoices; i++) {
                        final ProblemChoiceTemplate choice = choices.get(i);
                        final Object obj = choice.correct.evaluate(problem.evalContext);

                        if (Boolean.TRUE.equals(obj)) {
                            answers[0] = Long.valueOf((long) i + 1L);

                            break;
                        }
                    }

                    if (answers[0] != null) {
                        problem.recordAnswer(answers);
                    }
                }
                case final ProblemNumericTemplate numeric -> {

                    // Fill in the correct answer
                    final Serializable[] answers = new Serializable[1];
                    final ProblemAcceptNumberTemplate accept = numeric.acceptNumber;
                    answers[0] = accept.getCorrectAnswerValue(problem.evalContext);

                    if (answers[0] != null) {
                        problem.recordAnswer(answers);
                    }
                }
                default -> {
                }
            }
        }

        // See if this section permits a calculator
        final Iterator<String> resources =
                this.examSession.getExam().getSection(sectionIndex).getResources().iterator();

        final boolean calc;
        while (resources.hasNext()) {
            if ("calculator".equalsIgnoreCase(resources.next())) {
                // Section allows calculator - check this problem
            }
        }

        // FIXME: For now, allow calc on all problems
        calc = true;

        if (this.calculator != null) {
            this.calculator.showCalculator(calc);
            if (this.calculator.getCalcUI() != null) {
                final int slot = this.calculator.getCalcUI().getSlot();
                if (slot >= 0) {
                    Launcher.getCalcThread(slot).enqueueAction(new CalcBasicAction(ECalcAction.SET_SPEED_200));
                    Launcher.getCalcThread(slot).enqueueAction(new CalcBasicAction(ECalcAction.TURN_ON));
                }
            }
        }

        this.top.setSelectedSectionIndex(sectionIndex);
        updateTop(sectionIndex);

        final boolean answers = this.examSession.getState().showAnswers;
        final boolean solutions = this.examSession.getState().showSolutions;

        this.currentProblem.setCurrentProblem(sectionIndex, problemIndex, this, answers, solutions);

        if (this.problems != null) {
            this.problems.setCurrentProblem(sectionIndex, problemIndex);
        }

        this.owner.doCacheExamState();

        this.currentProblem.revalidate();
    }

    /**
     * Shows the instructions in the current problem panel.
     */
    public final void showInstructions() {

        this.currentProblem.showInstructions();
    }

    /**
     * Records a student's answer.
     *
     * @param answer a list of answer objects, whose type depends on the type of problem for which the answer is being
     *               submitted
     */
    @Override
    public final void recordAnswer(final Object[] answer) {

        if (this.problems != null) {
            this.problems.refresh();
        }

        if (this.owner != null) {
            this.owner.doCacheExamState();
        }
    }

    /**
     * Clears a student's answer.
     */
    @Override
    public final void clearAnswer() {

        if (this.problems != null) {
            this.problems.refresh();
        }

        if (this.owner != null) {
            this.owner.doCacheExamState();
        }
    }

    /**
     * Adds an action listener that will be notified whenever the panel fires an action. Each subclass will specify a
     * set of actions it will fire. These can be used to detect when the user accepts or cancels a dialog, makes a
     * selection, and so on. Typically, a client program that uses a subclass will create the object and register as a
     * listener before showing the object, then enter a "wait". In the action performed method the waiting thread is
     * interrupted, and processing continues.
     *
     * @param listener the action listener to register
     */
    public final void addActionListener(final ActionListener listener) {

        synchronized (this) {

            if (this.listeners == null) {
                this.listeners = new ArrayList<>(1);
            }

            this.listeners.add(listener);
        }
    }

    /**
     * Asks the top panel to update itself.
     *
     * @param selected the index of the currently selected section
     */
    public final void updateTop(final int selected) {

        boolean paint = false;

        final ExamObj exam = this.examSession.getExam();

        if (exam.getNumSections() == 1) {
            final String prop = this.skin.getProperty("top-bar-show-sections-if-one");

            if ("true".equalsIgnoreCase(prop)) {
                final ExamSection sect = exam.getSection(0);

                String name = this.isPractice ? "Practice Assignment" : "Assignment";

                if (sect.mastered) {

                    if (sect.score != null && sect.score.intValue() == 0) {
                        name = name + " has been completed.";
                    } else {
                        name = name + " has been mastered with a score of " + sect.score;
                    }
                } else if (sect.passed) {

                    if (sect.score != null && sect.score.intValue() == 0) {
                        name = name + " has been completed.";
                    } else {
                        name = name + " has been completed with a score of " + sect.score;
                    }
                } else {
                    name = CoreConstants.EMPTY;
                }

                if (sect.enabled) {
                    this.top.enableSection(0);
                }

                this.top.setSectionTitle(0, name);
                this.top.setSelectedSectionIndex(selected);
                paint = true;
            }
        } else {
            final String prop = this.skin.getProperty("top-bar-show-sections");

            if ("true".equalsIgnoreCase(prop)) {

                final HtmlBuilder builder = new HtmlBuilder(100);

                final int numSect = exam.getNumSections();
                for (int i = 0; i < numSect; i++) {
                    final ExamSection sect = exam.getSection(i);

                    builder.add(sect.sectionName);

                    if (!this.isPractice) {

                        if (sect.mastered) {

                            if (sect.score != null && sect.score.intValue() == 0) {
                                builder.add(" (may move on)");
                            } else {
                                builder.add(" (completed)");
                            }
                        } else if (sect.passed) {
                            builder.add(" (may move on)");
                        }
                    }

                    if (sect.enabled) {
                        this.top.enableSection(i);
                    }

                    this.top.setSectionTitle(i, builder.toString());
                    this.top.setSelectedSectionIndex(selected);
                    paint = true;
                    builder.reset();
                }
            }
        }

        if (paint) {
            this.top.repaint();
        }
    }

    /**
     * Enables or disables this panel and all contained subpanels.
     *
     * @param enabled {@code true} to enable; {@code false} to disable
     */
    @Override
    public final void setEnabled(final boolean enabled) {

        super.setEnabled(enabled);

        if (!SwingUtilities.isEventDispatchThread()) {
            Log.warning(Res.get(Res.NOT_AWT_THREAD));
        }

        if (this.top != null) {
            this.top.setEnabled(enabled);
        }

        if (this.bottom != null) {
            this.bottom.setEnabled(enabled);
        }

        if (this.problems != null) {
            this.problems.setEnabled(enabled);
        }

        if (this.currentProblem != null) {
            this.currentProblem.setEnabled(enabled);
        }
    }

    /**
     * Make the window render larger, up to some limit.
     */
    public final void larger() {

        this.top.larger();
        this.bottom.larger();
        if (this.problems != null) {
            this.problems.larger();
        }
        if (this.currentProblem != null) {
            this.currentProblem.larger();
        }
    }

    /**
     * Make the window render smaller, down to some limit.
     */
    public final void smaller() {

        this.top.smaller();
        this.bottom.smaller();
        if (this.problems != null) {
            this.problems.smaller();
        }
        if (this.currentProblem != null) {
            this.currentProblem.smaller();
        }
    }

    /**
     * Updates the color of the active panel to match that of the exam.
     */
    public final void updateColor() {

        if (this.currentProblem != null) {
            this.currentProblem.updateColor();
        }
    }
}

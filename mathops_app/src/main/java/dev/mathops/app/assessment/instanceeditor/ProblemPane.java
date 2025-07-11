package dev.mathops.app.assessment.instanceeditor;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.formula.ConstRealValue;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.edit.FormulaEditorPanel;
import dev.mathops.assessment.problem.ECalculatorType;
import dev.mathops.assessment.problem.EProblemType;
import dev.mathops.assessment.problem.template.AbstractProblemMultipleChoiceTemplate;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemChoiceTemplate;
import dev.mathops.assessment.problem.template.ProblemEmbeddedInputTemplate;
import dev.mathops.assessment.problem.template.ProblemMultipleSelectionTemplate;
import dev.mathops.assessment.problem.template.ProblemNumericTemplate;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.text.builder.HtmlBuilder;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.Iterator;
import java.util.List;

/**
 * A pane that presents the selected problem for editing.
 */
final class ProblemPane extends JPanel implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4143194143961200882L;

    /** The font size for formula editors. */
    private static final int FORMULA_FONT_SIZE = 14;

    /** An action command. */
    private static final String PROBLEM_TYPE_CMD = "PROBLEM_TYPE";

    /** An action command. */
    private static final String FORCE_INTEGER_CMD = "FORCE_INTEGER";

    /** An action command. */
    private static final String ADD_CHOICE_CMD = "ADD_CHOICE";

    /** The working copy that reflects edits made in the panel. */
    private final AbstractProblemTemplate workingProblem;

    /** The identifier reference. */
    private final JComboBox<EProblemType> problemType;

    /** The "force integer" flag for an "accept-number" configuration in a numeric problem. */
    private final JCheckBox forceInteger;

    /** The "variance" formula for an "accept-number" configuration in a numeric problem. */
    private final FormulaEditorPanel variance;

    /** Flag indicating the "Min Correct Choices / Max Correct Choices" are showing. */
    private boolean minMaxCorrectShowing;

    /** The container for the "Min Correct Choices / Max Correct Choices". */
    private final JPanel minMaxCorrectContainer;

    /** The box for "Min Correct Choices / Max Correct Choices". */
    private final JPanel minMaxCorrectBox;

    /** Flag indicating the "Correctness" formula is showing. */
    private boolean correctnessShowing;

    /** The container for the "Correctness" formula. */
    private final JPanel correctnessContainer;

    /** The box for "Correctness" formula. */
    private final JPanel correctnessBox;

    /** Flag indicating the "Accept Number" parameters are showing. */
    private boolean acceptNumberShowing;

    /** The container for the "Accept Number" parameters. */
    private final JPanel acceptNumberContainer;

    /** The box for "Accept Number" parameters. */
    private final JPanel acceptNumberBox;

    /** Flag indicating the "Choices" list is showing. */
    private boolean choicesShowing;

    /** The container for the "Choices" list. */
    private final JPanel choicesContainer;

    /** The box for "Choices" list. */
    private final JPanel choicesBox;

    /** The inner panel in the "Choices" list that holds the actual choices. */
    private final JPanel choicesInner;

    /** Flag indicating the "Answer" box is showing. */
    private boolean answerShowing;

    /** The container for the "Answer" box. */
    private final JPanel answerContainer;

    /** The box for "Answer" box. */
    private final JPanel answerBox;

    /**
     * Constructs a new {@code ProblemPane}.
     *
     * @param theProblem the problem this pane will render
     * @param bg         the background color
     */
    ProblemPane(final AbstractProblemTemplate theProblem, final Color bg) {

        super(new StackedBorderLayout());
        setBackground(bg);

        this.workingProblem = theProblem.deepCopy();

        setFocusCycleRoot(true);

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        final Insets formulaInsets = new Insets(6, 8, 6, 8);

        // Line 1: Problem ID (<ref-base>) and type
        final JTextField problemId = new JTextField(25);
        problemId.setEditable(false);
        if (this.workingProblem.id != null) {
            problemId.setText(this.workingProblem.id);
        }

        final JPanel flow1 = makeFlow(bg);
        flow1.add(new JLabel("Problem ID:"));
        flow1.add(problemId);
        add(flow1, StackedBorderLayout.NORTH);

        final JPanel flow2 = makeFlow(bg);
        flow2.add(new JLabel("Problem Type:"));
        final EProblemType[] types = {EProblemType.NUMERIC, EProblemType.MULTIPLE_CHOICE,
                EProblemType.MULTIPLE_SELECTION, EProblemType.EMBEDDED_INPUT};
        this.problemType = new JComboBox<>(types);
        this.problemType.setSelectedItem(this.workingProblem.getType());
        this.problemType.setActionCommand(PROBLEM_TYPE_CMD);
        this.problemType.addActionListener(this);
        flow2.add(this.problemType);
        add(flow2, StackedBorderLayout.NORTH);

        final JPanel flow3 = makeFlow(bg);
        flow3.add(new JLabel("Calculator allowed:"));
        final JComboBox<ECalculatorType> calculatorType = new JComboBox<>(ECalculatorType.values());
        calculatorType.setSelectedItem(this.workingProblem.calculator);
        flow3.add(calculatorType);
        add(flow3, StackedBorderLayout.NORTH);

        Formula minCorrectFormula = null;
        Formula maxCorrectFormula = null;
        if (this.workingProblem instanceof final ProblemMultipleSelectionTemplate multsel) {
            minCorrectFormula = multsel.minCorrect;
            maxCorrectFormula = multsel.maxCorrect;
        }

        Formula correctnessFormula = null;
        if (this.workingProblem instanceof final ProblemEmbeddedInputTemplate embedded) {
            correctnessFormula = embedded.correctness;
        }

        this.minMaxCorrectBox = new JPanel(new StackedBorderLayout());
        this.minMaxCorrectBox.setBackground(bg);
        this.minMaxCorrectBox.add(new JLabel(CoreConstants.SPC), StackedBorderLayout.NORTH);
        final JPanel minMaxCorrectInner = new JPanel(new StackedBorderLayout());
        minMaxCorrectInner.setBorder(BorderFactory.createTitledBorder("Multiple Selection Settings"));
        minMaxCorrectInner.setBackground(bg);
        this.minMaxCorrectBox.add(minMaxCorrectInner, StackedBorderLayout.NORTH);

        final JPanel flow4 = makeFlow(bg);
        flow4.add(new JLabel("Min correct choices to display:"));
        final FormulaEditorPanel minCorrect = new FormulaEditorPanel(FORMULA_FONT_SIZE, formulaInsets,
                minCorrectFormula, EType.INTEGER);
        minCorrect.setEnabled(this.workingProblem instanceof ProblemMultipleSelectionTemplate);
        flow4.add(minCorrect);
        minMaxCorrectInner.add(flow4, StackedBorderLayout.NORTH);

        final JPanel flow5 = makeFlow(bg);
        flow5.add(new JLabel("Max correct choices to display:"));
        final FormulaEditorPanel maxCorrect = new FormulaEditorPanel(FORMULA_FONT_SIZE, formulaInsets,
                maxCorrectFormula, EType.INTEGER);
        maxCorrect.setEnabled(this.workingProblem instanceof ProblemMultipleSelectionTemplate);
        flow5.add(maxCorrect);
        minMaxCorrectInner.add(flow5, StackedBorderLayout.NORTH);

        this.minMaxCorrectContainer = new JPanel(new StackedBorderLayout());
        this.minMaxCorrectContainer.setBackground(bg);
        add(this.minMaxCorrectContainer, StackedBorderLayout.NORTH);
        this.minMaxCorrectShowing = false;

        this.correctnessBox = new JPanel(new StackedBorderLayout());
        this.correctnessBox.setBackground(bg);
        this.correctnessBox.add(new JLabel(CoreConstants.SPC), StackedBorderLayout.NORTH);

        final JPanel flow6 = makeFlow(bg);
        flow6.add(new JLabel("Correctness formula:"));
        final FormulaEditorPanel correctness = new FormulaEditorPanel(FORMULA_FONT_SIZE, formulaInsets,
                correctnessFormula, EType.BOOLEAN);
        correctness.setEnabled(this.workingProblem instanceof ProblemEmbeddedInputTemplate);
        flow6.add(correctness);
        this.correctnessBox.add(flow6, StackedBorderLayout.NORTH);

        this.correctnessContainer = new JPanel(new BorderLayout());
        this.correctnessContainer.setBackground(bg);
        add(this.correctnessContainer, StackedBorderLayout.NORTH);
        this.correctnessShowing = false;

        //

        Number varianceConstant = null;
        Formula varianceFormula = null;
        Formula correctAnswerFormula = null;
        if (this.workingProblem instanceof final ProblemNumericTemplate numeric) {
            varianceConstant = numeric.acceptNumber.varianceConstant;
            varianceFormula = numeric.acceptNumber.varianceFormula;
            correctAnswerFormula = numeric.acceptNumber.correctAnswer;
        }

        this.acceptNumberBox = new JPanel(new StackedBorderLayout());
        this.acceptNumberBox.setBackground(bg);
        this.acceptNumberBox.add(new JLabel(CoreConstants.SPC), StackedBorderLayout.NORTH);
        final JPanel acceptNumberInner = new JPanel(new StackedBorderLayout());
        acceptNumberInner.setBorder(BorderFactory.createTitledBorder("Numeric Entry"));
        acceptNumberInner.setBackground(bg);
        this.acceptNumberBox.add(acceptNumberInner, StackedBorderLayout.NORTH);

        final JPanel flow7 = makeFlow(bg);
        this.forceInteger = new JCheckBox("Force integer");
        this.forceInteger.setActionCommand(FORCE_INTEGER_CMD);
        this.forceInteger.addActionListener(this);
        flow7.add(this.forceInteger);
        acceptNumberInner.add(flow7, StackedBorderLayout.NORTH);

        final JPanel flow8 = makeFlow(bg);
        flow8.add(new JLabel("Allowed variance:"));
        final Formula initialVar = varianceConstant == null ? varianceFormula
                : new Formula(new ConstRealValue(varianceConstant.doubleValue()));

        this.variance = new FormulaEditorPanel(FORMULA_FONT_SIZE, formulaInsets, initialVar,
                EType.INTEGER, EType.REAL);
        flow8.add(this.variance);
        acceptNumberInner.add(flow8, StackedBorderLayout.NORTH);

        final JPanel flow9 = makeFlow(bg);
        flow9.add(new JLabel("Correct answer:"));
        final FormulaEditorPanel correctAnswer = new FormulaEditorPanel(FORMULA_FONT_SIZE, formulaInsets,
                correctAnswerFormula, EType.INTEGER, EType.REAL);
        flow9.add(correctAnswer);
        acceptNumberInner.add(flow9, StackedBorderLayout.NORTH);

        this.acceptNumberContainer = new JPanel(new StackedBorderLayout());
        this.acceptNumberContainer.setBackground(bg);
        add(this.acceptNumberContainer, StackedBorderLayout.NORTH);
        this.acceptNumberShowing = false;

        // Variables

        add(new JLabel(CoreConstants.SPC), StackedBorderLayout.NORTH);
        final VariablesPane vars = new VariablesPane(this.workingProblem, bg);
        add(vars, StackedBorderLayout.NORTH);

        //

        this.choicesBox = new JPanel(new StackedBorderLayout());
        this.choicesBox.setBackground(bg);
        this.choicesBox.add(new JLabel(CoreConstants.SPC), StackedBorderLayout.NORTH);
        this.choicesInner = new JPanel(new StackedBorderLayout());
        this.choicesInner.setBorder(BorderFactory.createTitledBorder("Choices"));
        this.choicesInner.setBackground(bg);
        this.choicesBox.add(this.choicesInner, StackedBorderLayout.NORTH);

        if ((this.workingProblem instanceof final AbstractProblemMultipleChoiceTemplate choiceBase)) {
            for (final ProblemChoiceTemplate choice : choiceBase.getChoices()) {
                final ChoicePane choicePane = new ChoicePane(this, this.workingProblem, choice);
                this.choicesInner.add(choicePane, StackedBorderLayout.NORTH);
            }
        }

        final JPanel choiceButtons = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        choiceButtons.setBackground(bg);
        final JButton addChoiceButton = new JButton("Create new choice");
        addChoiceButton.setActionCommand(ADD_CHOICE_CMD);
        addChoiceButton.addActionListener(this);
        choiceButtons.add(addChoiceButton);
        this.choicesInner.add(choiceButtons, StackedBorderLayout.SOUTH);

        // TODO: button to add a new choice, button to delete existing choice

        this.choicesContainer = new JPanel(new StackedBorderLayout());
        this.choicesContainer.setBackground(bg);
        add(this.choicesContainer, StackedBorderLayout.NORTH);
        this.choicesShowing = false;

        //

        add(new JLabel(CoreConstants.SPC), StackedBorderLayout.NORTH);

        final JPanel questionBox = new JPanel(new StackedBorderLayout());
        questionBox.setBackground(bg);
        questionBox.setBorder(BorderFactory.createTitledBorder("Question"));
        final DocColumnPane question = new DocColumnPane();
        if (this.workingProblem.question != null) {
            final HtmlBuilder xml = new HtmlBuilder(100);
            for (final AbstractDocObjectTemplate child : this.workingProblem.question.getChildren()) {
                child.toXml(xml, 0);
            }
            question.setText(xml.toString());
        }
        questionBox.add(question, StackedBorderLayout.CENTER);
        add(questionBox, StackedBorderLayout.NORTH);

        //

        this.answerBox = new JPanel(new StackedBorderLayout());
        this.answerBox.setBackground(bg);
        final JPanel answerInner = new JPanel(new StackedBorderLayout());
        answerInner.setBorder(BorderFactory.createTitledBorder("Representative Correct Answer"));
        answerInner.setBackground(bg);
        this.answerBox.add(answerInner, StackedBorderLayout.NORTH);

        final DocColumnPane answer = new DocColumnPane();
        if ((this.workingProblem instanceof final ProblemEmbeddedInputTemplate embedded)
            && (embedded.correctAnswer != null)) {

            final HtmlBuilder xml = new HtmlBuilder(100);
            for (final AbstractDocObjectTemplate child : embedded.correctAnswer.getChildren()) {
                child.toXml(xml, 0);
            }
            answer.setText(xml.toString());
        }
        answerInner.add(answer, StackedBorderLayout.CENTER);

        this.answerContainer = new JPanel(new StackedBorderLayout());
        this.answerContainer.setBackground(bg);
        add(this.answerContainer, StackedBorderLayout.NORTH);
        this.answerShowing = false;

        //

        final JPanel solutionBox = new JPanel(new StackedBorderLayout());
        solutionBox.setBackground(bg);
        solutionBox.setBorder(BorderFactory.createTitledBorder("Solution"));
        final DocColumnPane solution = new DocColumnPane();
        if (this.workingProblem.solution != null) {
            final HtmlBuilder xml = new HtmlBuilder(100);
            for (final AbstractDocObjectTemplate child : this.workingProblem.solution.getChildren()) {
                child.toXml(xml, 0);
            }
            solution.setText(xml.toString());
        }
        solutionBox.add(solution, StackedBorderLayout.CENTER);
        add(solutionBox, StackedBorderLayout.NORTH);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        buttons.setBackground(bg);
        add(buttons, StackedBorderLayout.NORTH);

        updateBasedOnProblemType();
    }

    /**
     * Creates a flow pane.
     *
     * @param bg the background color
     * @return the pane
     */
    private static JPanel makeFlow(final Color bg) {

        final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 2));
        flow.setBackground(bg);

        return flow;
    }

    /**
     * Gets the working problem.
     *
     * @return the working problem
     */
    AbstractProblemTemplate getWorkingProblem() {

        return this.workingProblem;
    }

    /**
     * Updates the visibility of sections of control based on the problem type.
     */
    private void updateBasedOnProblemType() {

        final Object selectedType = this.problemType.getSelectedItem();

        final boolean isNumeric = selectedType == EProblemType.NUMERIC;
        final boolean isMultipleChoice = selectedType == EProblemType.MULTIPLE_CHOICE;
        final boolean isMultipleSelection = selectedType == EProblemType.MULTIPLE_SELECTION;
        final boolean isEmbedded = selectedType == EProblemType.EMBEDDED_INPUT;

        if (isNumeric) {
            if (!this.acceptNumberShowing) {
                this.acceptNumberContainer.add(this.acceptNumberBox, StackedBorderLayout.NORTH);
                this.acceptNumberShowing = true;
            }
        } else if (this.acceptNumberShowing) {
            this.acceptNumberContainer.remove(this.acceptNumberBox);
            this.acceptNumberShowing = false;
        }

        if (isMultipleSelection) {
            if (!this.minMaxCorrectShowing) {
                this.minMaxCorrectContainer.add(this.minMaxCorrectBox, StackedBorderLayout.NORTH);
                this.minMaxCorrectShowing = true;
            }
        } else if (this.minMaxCorrectShowing) {
            this.minMaxCorrectContainer.remove(this.minMaxCorrectBox);
            this.minMaxCorrectShowing = false;
        }

        if (isEmbedded) {
            if (!this.correctnessShowing) {
                this.correctnessContainer.add(this.correctnessBox, StackedBorderLayout.NORTH);
                this.correctnessShowing = true;
            }
        } else if (this.correctnessShowing) {
            this.correctnessContainer.remove(this.correctnessBox);
            this.correctnessShowing = false;
        }

        if (isEmbedded) {
            if (!this.answerShowing) {
                this.answerContainer.add(this.answerBox, StackedBorderLayout.NORTH);
                this.answerShowing = true;
            }
        } else if (this.answerShowing) {
            this.answerContainer.remove(this.answerBox);
            this.answerShowing = false;
        }

        if (isMultipleChoice || isMultipleSelection) {
            if (!this.choicesShowing) {
                this.choicesContainer.add(this.choicesBox, StackedBorderLayout.NORTH);
                this.choicesShowing = true;
            }
        } else if (this.choicesShowing) {
            this.choicesContainer.remove(this.choicesBox);
            this.choicesShowing = false;
        }

        revalidate();
        repaint();
    }

    /**
     * Deletes a choice from this problem.
     *
     * @param choice the choice to delete
     */
    void deleteChoice(final ProblemChoiceTemplate choice) {

        if (this.workingProblem instanceof final AbstractProblemMultipleChoiceTemplate base) {

            final List<ProblemChoiceTemplate> choices = base.getChoices();
            final Iterator<ProblemChoiceTemplate> iter = choices.iterator();
            while (iter.hasNext()) {
                final ProblemChoiceTemplate item = iter.next();

                if (item == choice) {
                    iter.remove();
                    break;
                }
            }

            for (final Component comp : this.choicesInner.getComponents()) {
                if (comp instanceof final ChoicePane choicePane && choicePane.choice == choice) {
                    this.choicesInner.remove(comp);
                    revalidate();
                    repaint();
                    break;
                }
            }
        }
    }

    /**
     * Called when a button is clicked.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (PROBLEM_TYPE_CMD.equals(cmd)) {
            updateBasedOnProblemType();
        } else if (FORCE_INTEGER_CMD.equals(cmd)) {
            this.variance.setEnabled(!this.forceInteger.isSelected());
        } else if (ADD_CHOICE_CMD.equals(cmd)) {
            final ProblemChoiceTemplate newChoice = new ProblemChoiceTemplate();
            final ChoicePane choicePane = new ChoicePane(this, this.workingProblem, newChoice);
            this.choicesInner.add(choicePane, StackedBorderLayout.NORTH);
            revalidate();
            repaint();
        }
    }
}

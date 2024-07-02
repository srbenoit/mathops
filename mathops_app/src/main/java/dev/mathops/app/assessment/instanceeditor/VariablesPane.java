package dev.mathops.app.assessment.instanceeditor;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EVariableType;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableBoolean;
import dev.mathops.assessment.variable.VariableDerived;
import dev.mathops.assessment.variable.VariableInteger;
import dev.mathops.assessment.variable.VariableRandomBoolean;
import dev.mathops.assessment.variable.VariableRandomChoice;
import dev.mathops.assessment.variable.VariableRandomInteger;
import dev.mathops.assessment.variable.VariableRandomPermutation;
import dev.mathops.assessment.variable.VariableRandomReal;
import dev.mathops.assessment.variable.VariableRandomSimpleAngle;
import dev.mathops.assessment.variable.VariableReal;
import dev.mathops.assessment.variable.VariableSpan;
import dev.mathops.assessment.variable.edit.VariableEditorPanel;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A pane that presents all variables in selected problem for editing.
 */
final class VariablesPane extends JPanel implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 2273481746352249087L;

//    /** The font size for formula editors. */
//    public static final int FORMULA_FONT_SIZE = 14;

    /** The possible variable types. */
    private static final String[] TYPES = {VariableEditorPanel.BOOLEAN, VariableEditorPanel.INTEGER,
            VariableEditorPanel.REAL, VariableEditorPanel.SPAN, VariableEditorPanel.RANDOM_BOOLEAN,
            VariableEditorPanel.RANDOM_INTEGER, VariableEditorPanel.RANDOM_REAL,
            VariableEditorPanel.RANDOM_PERMUTATION, VariableEditorPanel.RANDOM_CHOICE_INTEGER,
            VariableEditorPanel.RANDOM_CHOICE_REAL, VariableEditorPanel.RANDOM_CHOICE_SPAN,
            VariableEditorPanel.DERIVED_BOOLEAN, VariableEditorPanel.DERIVED_INTEGER,
            VariableEditorPanel.DERIVED_REAL, VariableEditorPanel.DERIVED_SPAN};

    /** An action command. */
    private static final String ADD_VAR_CMD = "ADD_VAR";

    /** Combo box to choose type for a new variable. */
    private final JComboBox<String> variableTypeSelector;

    /** The working copy that reflects edits made in the panel. */
    private final AbstractProblemTemplate problem;

    /**
     * Constructs a new {@code VariablesPane}.
     *
     * @param theProblem the problem this pane will render
     * @param bg         the background color
     */
    VariablesPane(final AbstractProblemTemplate theProblem, final Color bg) {

        super(new StackedBorderLayout());
        setBackground(bg);
        setBorder(BorderFactory.createTitledBorder("Variables"));

        this.problem = theProblem;

        final EvalContext evalContext = theProblem.evalContext;
        final List<String> names = new ArrayList<>(evalContext.getVariableNames());
        Collections.sort(names);

        for (final String name : names) {
            final AbstractVariable var = evalContext.getVariable(name);
            if (var != null) {
                final EVariableType type = var.getVariableType();
                if (type == EVariableType.INPUT_INTEGER || type == EVariableType.INPUT_REAL
                        || type == EVariableType.INPUT_STRING) {
                    continue;
                }

                final VariableEditorPanel pane = new VariableEditorPanel(evalContext, this, var);
                add(pane, StackedBorderLayout.NORTH);
            }
        }

        final JPanel varButtons = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 6));
        varButtons.setBackground(bg);

        this.variableTypeSelector = new JComboBox<>(TYPES);
        varButtons.add(this.variableTypeSelector);

        final JButton addVarButton = new JButton("Create new variable");
        addVarButton.setActionCommand(ADD_VAR_CMD);
        addVarButton.addActionListener(this);
        varButtons.add(addVarButton);
        add(varButtons, StackedBorderLayout.SOUTH);
    }

    /**
     * Called when a button is clicked.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (ADD_VAR_CMD.equals(cmd)) {
            final Object selection = this.variableTypeSelector.getSelectedItem();
            if (selection instanceof final String selectedType) {

                // Choose a unique name
                final EvalContext context = this.problem.evalContext;

                String varName = "var";
                for (int i = 1; i < 10000; ++i) {
                    final String name = "var" + i;
                    if (context.getVariable(name) == null) {
                        varName = name;
                        break;
                    }
                }

                AbstractVariable var = null;

                switch (selectedType) {
                    case VariableEditorPanel.BOOLEAN:
                        var = new VariableBoolean(varName);
                        break;
                    case VariableEditorPanel.INTEGER:
                        var = new VariableInteger(varName);
                        break;
                    case VariableEditorPanel.REAL:
                        var = new VariableReal(varName);
                        break;
                    case VariableEditorPanel.SPAN:
                        var = new VariableSpan(varName);
                        break;
                    case VariableEditorPanel.RANDOM_BOOLEAN:
                        var = new VariableRandomBoolean(varName);
                        break;
                    case VariableEditorPanel.RANDOM_INTEGER:
                        var = new VariableRandomInteger(varName);
                        break;
                    case VariableEditorPanel.RANDOM_REAL:
                        var = new VariableRandomReal(varName);
                        break;
                    case VariableEditorPanel.RANDOM_PERMUTATION:
                        var = new VariableRandomPermutation(varName);
                        break;
                    case VariableEditorPanel.RANDOM_CHOICE_INTEGER:
                        var = new VariableRandomChoice(varName, EType.INTEGER);
                        break;
                    case VariableEditorPanel.RANDOM_CHOICE_REAL:
                        var = new VariableRandomChoice(varName, EType.REAL);
                        break;
                    case VariableEditorPanel.RANDOM_CHOICE_SPAN:
                        var = new VariableRandomChoice(varName, EType.SPAN);
                        break;
                    case VariableEditorPanel.RANDOM_SIMPLE_ANGLE:
                        var = new VariableRandomSimpleAngle(varName);
                        break;
                    case VariableEditorPanel.DERIVED_BOOLEAN:
                        var = new VariableDerived(varName, EType.BOOLEAN);
                        break;
                    case VariableEditorPanel.DERIVED_INTEGER:
                        var = new VariableDerived(varName, EType.INTEGER);
                        break;
                    case VariableEditorPanel.DERIVED_REAL:
                        var = new VariableDerived(varName, EType.REAL);
                        break;
                    case VariableEditorPanel.DERIVED_SPAN:
                        var = new VariableDerived(varName, EType.SPAN);
                        break;
                    default:
                        Log.warning("Unsupported variable type: ", selectedType);
                        break;
                }

                if (var != null) {
                    this.problem.evalContext.addVariable(var);
                    final VariableEditorPanel panel =
                            new VariableEditorPanel(this.problem.evalContext, this, var);
                    add(panel, StackedBorderLayout.NORTH);
                    revalidate();
                    repaint();
                }
            }
        }
    }
}

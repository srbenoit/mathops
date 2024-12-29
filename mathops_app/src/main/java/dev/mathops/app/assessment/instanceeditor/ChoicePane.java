package dev.mathops.app.assessment.instanceeditor;

import dev.mathops.assessment.EType;
import dev.mathops.assessment.document.template.AbstractDocObjectTemplate;
import dev.mathops.assessment.formula.edit.FormulaEditorPanel;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.assessment.problem.template.ProblemChoiceTemplate;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.text.builder.HtmlBuilder;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * A pane that presents a single choice from a multiple-choice or multiple-selection problem.
 */
public final class ChoicePane extends JPanel implements ActionListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = 2431570462226282595L;

    /** An action command. */
    private static final String DELETE_CHOICE_CMD = "DELETE_CHOICE";

    /** The choice this panel will display. */
    final ProblemChoiceTemplate choice;

    /** The owning problem pane. */
    private final ProblemPane owner;

    /**
     * Constructs a new {@code ChoicePane}.
     *
     * @param theOwner   the owning problem pane
     * @param theProblem the problem (needed to access the evaluation context)
     * @param theChoice  the choice
     */
    ChoicePane(final ProblemPane theOwner, final AbstractProblemTemplate theProblem,
               final ProblemChoiceTemplate theChoice) {

        super(new StackedBorderLayout(0, 4));
        setBorder(BorderFactory.createEtchedBorder());

        this.choice = theChoice;
        this.owner = theOwner;

        final Insets formulaInsets = new Insets(6, 8, 6, 8);
        final int fontSize = getFont().getSize();

        final JPanel topFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
        topFlow.add(new JLabel("Choice ID:"));
        final JTextField choiceIdField = new JTextField(4);
        choiceIdField.setText(Integer.toString(theChoice.choiceId));
        topFlow.add(choiceIdField);

        topFlow.add(new JLabel("   Fixed position:"));
        final JTextField positionField = new JTextField(4);
        if (theChoice.pos != 0) {
            positionField.setText(Integer.toString(theChoice.pos));
        }
        topFlow.add(positionField);

        topFlow.add(new JLabel("   Correctness Formula:"));
        final FormulaEditorPanel correctnessFormula = new FormulaEditorPanel(fontSize, formulaInsets, theChoice.correct,
                EType.BOOLEAN);
        topFlow.add(correctnessFormula);

        topFlow.add(new JLabel("    "));
        final JButton deleteChoice = new JButton("Delete Choice");
        deleteChoice.setActionCommand(DELETE_CHOICE_CMD);
        deleteChoice.addActionListener(this);
        topFlow.add(deleteChoice);

        add(topFlow, StackedBorderLayout.NORTH);

        final DocColumnPane content = new DocColumnPane();
        content.setBackground(getBackground());
        if (theChoice.doc != null) {
            final HtmlBuilder xml = new HtmlBuilder(100);
            for (final AbstractDocObjectTemplate child : theChoice.doc.getChildren()) {
                child.toXml(xml, 0);
            }
            content.setText(xml.toString());
        }
        add(content, StackedBorderLayout.NORTH);
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (DELETE_CHOICE_CMD.equals(cmd)) {
            this.owner.deleteChoice(this.choice);
        }
    }
}

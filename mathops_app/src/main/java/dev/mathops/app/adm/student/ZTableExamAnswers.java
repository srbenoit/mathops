package dev.mathops.app.adm.student;

import dev.mathops.app.adm.AbstractZTable;
import dev.mathops.app.adm.IZTableCommandListener;
import dev.mathops.core.CoreConstants;
import dev.mathops.db.old.rawrecord.RawStqa;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A table to present all answers associated with a student exam.
 */
/* default */ class ZTableExamAnswers extends AbstractZTable<RawStqa> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -4478080873035508485L;

    /** True if there should be a button to update answers. */
    private final boolean updateAllowed;

    /** A list of checkboxes that show correctness of each answer. */
    private List<JCheckBox> correctness;

    /** A list of update buttons. */
    private List<JButton> updateButtons;

    /**
     * Constructs a new {@code ZTableExamAnswer}.
     *
     * @param theListener       the listener that will be notified when a button is pressed in a row
     * @param allowChangeAnswer true to include the update button to change an answer
     */
    /* default */ ZTableExamAnswers(final IZTableCommandListener<RawStqa> theListener,
                                    final boolean allowChangeAnswer) {

        super(theListener);

        this.updateAllowed = allowChangeAnswer;
    }

    /**
     * Adds the column headings to the table.
     */
    @Override
    protected void addColumnHeadings() {

        addHeaderCell("Question", 0, 0, 6);
        addHeaderCell("Answer Index", 1, 0, 6);
        addHeaderCell("Objective", 2, 0, 6);
        addHeaderCell("Answer", 3, 0, 6);
        addHeaderCell("Correct", 4, 0, 6);
        addHeaderCell("Subtest", 5, 0, 6);
        if (this.updateAllowed) {
            addHeaderCell(CoreConstants.EMPTY, 6, 0, 6);
        }
    }

    /**
     * Sets table data from a list of records. This method should call {@code storeCurrentData} if any records will
     * include buttons.
     *
     * @param data the data
     */
    @Override
    public void setData(final List<RawStqa> data) {

        clear();

        if (data == null || data.isEmpty()) {
            this.correctness = null;
            this.updateButtons = null;
        } else {
            storeCurrentData(data);

            this.correctness = new ArrayList<>(data.size());
            if (this.updateAllowed) {
                this.updateButtons = new ArrayList<>(data.size());
            } else {
                this.updateButtons = null;
            }

            int yy = 1;

            for (final RawStqa row : data) {

                addCell(row.questionNbr, 0, yy);
                addCell(row.answerNbr, 1, yy);
                addCell(row.objective, 2, yy);
                addCell(row.stuAnswer, 3, yy);

                final JCheckBox check = makeCheckBoxCell(CoreConstants.EMPTY, 4, yy, //
                        "COR", "Y".equals(row.ansCorrect));
                this.correctness.add(check);

                addCell(row.subtest, 5, yy);

                if (this.updateAllowed) {
                    final JButton btn = addButtonCell("Update", 6, yy, "UPDATE");
                    btn.setEnabled(false);
                    this.updateButtons.add(btn);
                }

                ++yy;
            }

            final int numCols = this.updateAllowed ? 6 : 7;
            addLastRow(yy, numCols);
            addLastCol(numCols, yy + 1);
        }
    }

    /**
     * Given the row number of a "correct" checkbox that was just changed, this method first scans for all other rows
     * that have the same question/answer index, and sets those checkboxes to the same state (since a question can apply
     * to multiple subtests, hence can have multiple rows).
     *
     * <p>
     * Then, it compares each correctness checkbox setting to the correctness value in current data, and enables the
     * "Update" button for any that are different.
     *
     * @param rowIndex the index of the row in which the correctness checkbox was just updated
     */
    public void setUpdateButtonStates(final int rowIndex) {

        final RawStqa row = getRow(rowIndex);
        final int numRows = getNumRows();

        final boolean checked = this.correctness.get(rowIndex).isSelected();
        final Integer q = row.questionNbr;
        final Integer a = row.answerNbr;

        // Make any other checkboxes for the same item (but different subtest) agree

        for (int i = 0; i < numRows; ++i) {
            if (i == rowIndex) {
                continue;
            }
            final RawStqa test = getRow(i);

            if (Objects.equals(q, test.questionNbr)
                    && Objects.equals(a, test.answerNbr)) {
                final JCheckBox matched = this.correctness.get(i);
                if (matched.isSelected() != checked) {
                    // Update the matching checkbox without firing an event (which would call this
                    // method again)
                    final ActionListener temp = matched.getActionListeners()[0];
                    matched.removeActionListener(temp);
                    matched.setSelected(checked);
                    matched.addActionListener(temp);
                }
            }
        }

        // Set the state of all buttons based on checkbox/data differences

        for (int i = 0; i < numRows; ++i) {
            final RawStqa data = getRow(i);
            final boolean dataState = "Y".equals(data.ansCorrect);
            final boolean boxState = this.correctness.get(i).isSelected();
            this.updateButtons.get(i).setEnabled(dataState != boxState);
        }
    }
}

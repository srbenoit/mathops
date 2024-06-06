package dev.mathops.app.adm.student;

import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.IZTableCommandListener;
import dev.mathops.app.adm.Skin;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.old.rawrecord.RawSemesterCalendar;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStqa;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.Serial;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A card within the list of all exams on the student's record (all "STEXAM" rows, not "STMPE" rows).
 */
/* default */ final class ExamListCard extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7690311966795710851L;

    /** The fixed data. */
    private final FixedData fixed;

    /** The exam table. */
    private final ZTableExamList examsTable;

    /** The scroll pane for the exam table. */
    private final JScrollPane examsScroll;

    /**
     * Constructs a new {@code ExamListCard}.
     *
     * @param theFixed         the fixed data
     * @param theListener      the listener to notify when the "Add" button is pressed.
     */
    /* default */ ExamListCard(final FixedData theFixed,
                               final IZTableCommandListener<ExamListRow> theListener) {

        super(new BorderLayout(10, 10));
        setBackground(Skin.WHITE);

        this.fixed = theFixed;
        this.examsTable = new ZTableExamList(theListener);
        this.examsScroll = new JScrollPane(this.examsTable);
        this.examsScroll.getVerticalScrollBar().setUnitIncrement(20);
        this.examsScroll.getVerticalScrollBar().setBlockIncrement(200);
        this.examsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(this.examsScroll, BorderLayout.WEST);

        final int prefWidth = this.examsTable.getPreferredSize().width
                + this.examsScroll.getVerticalScrollBar().getPreferredSize().width + 10;

        this.examsScroll.setPreferredSize(new Dimension(prefWidth, Integer.MAX_VALUE));
    }

    /**
     * Clears the display.
     */
    /* default */ void clear() {

        this.examsTable.clear();
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    /* default */ void populateDisplay(final StudentData data) {

        try {
            final List<RawStexam> studentExams = data.getStudentExams();

            if (studentExams.isEmpty()) {
                final List<ExamListRow> rows = new ArrayList<>(0);
                this.examsTable.setData(rows);
            } else {
                final List<RawSemesterCalendar> weeks = this.fixed.termWeeks;
                final int count = studentExams.size();
                final List<ExamListRow> rows = new ArrayList<>(count);
                final List<RawStqa> answers = new ArrayList<>(20);

                for (final RawStexam stexam : studentExams) {

                    int week = 0;
                    for (int i = weeks.size() - 1; i >= 0; --i) {
                        final RawSemesterCalendar test = weeks.get(i);
                        if (!stexam.examDt.isBefore(test.startDt)) {
                            week = test.weekNbr.intValue();
                            break;
                        }
                    }
                    final List<RawStqa> studentExamAnswers = data.getStudentExamAnswers();

                    for (final RawStqa ansrec : studentExamAnswers) {
                        if (Objects.equals(ansrec.serialNbr, stexam.serialNbr)) {
                            answers.add(ansrec);
                        }
                    }

                    Collections.sort(answers);
                    rows.add(new ExamListRow(week, stexam, answers));
                    answers.clear();
                }

                this.examsTable.setData(rows);

                final int prefWidth = this.examsTable.getPreferredSize().width
                        + this.examsScroll.getVerticalScrollBar().getPreferredSize().width + 10;

                this.examsScroll.setPreferredSize(new Dimension(prefWidth, Integer.MAX_VALUE));
            }
        } catch (final SQLException ex) {
            final List<ExamListRow> rows = new ArrayList<>(0);
            this.examsTable.setData(rows);
        }

        invalidate();
        revalidate();
        repaint();
    }
}

package dev.mathops.app.adm.student;

import dev.mathops.app.adm.IZTableCommandListener;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
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
/* default */ class ExamListCard extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7690311966795710851L;

    /** The list of all term weeks. */
    private final List<RawSemesterCalendar> termWeeks;

    /** The exam table. */
    private final ZTableExamList examsTable;

    /** The scroll pane for the exam table. */
    private final JScrollPane examsScroll;

    /**
     * Constructs a new {@code ExamListCard}.
     *
     * @param cache       the cache
     * @param theListener the listener to notify when the "Add" button is pressed.
     */
    ExamListCard(final Cache cache, final IZTableCommandListener<ExamListRow> theListener) {

        super(new BorderLayout(10, 10));
        setBackground(Skin.WHITE);

        this.examsTable = new ZTableExamList(theListener);
        this.examsScroll = new JScrollPane(this.examsTable);
        this.examsScroll.getVerticalScrollBar().setUnitIncrement(20);
        this.examsScroll.getVerticalScrollBar().setBlockIncrement(200);
        this.examsScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(this.examsScroll, BorderLayout.WEST);

        final int prefWidth = this.examsTable.getPreferredSize().width
                + this.examsScroll.getVerticalScrollBar().getPreferredSize().width + 10;

        this.examsScroll.setPreferredSize(new Dimension(prefWidth, Integer.MAX_VALUE));

        List<RawSemesterCalendar> allTermWeeks;
        try {
            allTermWeeks = cache.getSystemData().getSemesterCalendars();
        } catch (final SQLException ex) {
            Log.warning(ex);
            allTermWeeks = new ArrayList<>(0);
        }

        this.termWeeks = allTermWeeks;
    }

    /**
     * Clears the display.
     */
    void clear() {

        this.examsTable.clear();
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param data the student data
     */
    void populateDisplay(final StudentData data) {

        if (data.studentExams.isEmpty()) {
            final List<ExamListRow> rows = new ArrayList<>(0);
            this.examsTable.setData(rows);
        } else {
            final List<ExamListRow> rows = new ArrayList<>(data.studentExams.size());
            final List<RawStqa> answers = new ArrayList<>(20);

            for (final RawStexam stexam : data.studentExams) {

                int week = 0;
                for (int i = this.termWeeks.size() - 1; i >= 0; --i) {
                    final RawSemesterCalendar test = this.termWeeks.get(i);
                    if (!stexam.examDt.isBefore(test.startDt)) {
                        week = test.weekNbr.intValue();
                        break;
                    }
                }

                for (final RawStqa ansrec : data.studentExamAnswers) {
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

        invalidate();
        revalidate();
        repaint();
    }
}

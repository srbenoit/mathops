package dev.mathops.app.adm.office.registration;

import dev.mathops.app.adm.AdmPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.StudentData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawSemesterCalendar;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStterm;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.Serial;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * A panel that displays student activity.
 */
public final class CourseActivityPanel extends AdmPanelBase {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 117974520221014212L;

    /** The activity table. */
    private final JTableActivity activityTable;

    /** The scroll pane for the activity table. */
    private final JScrollPane activityScroll;

    /** A display for the student's pace. */
    private final JTextField paceDisplay;

    /** A display for the student's pace track. */
    private final JTextField paceTrackDisplay;

    /** An error message. */
    private final JLabel error;

    /**
     * Constructs a new {@code StudentActivityPanel}.
     *
     */
    public CourseActivityPanel() {

        super();

        setBackground(Skin.LIGHTEST);

        // Top - student's pace and pace track
        final JPanel top = makeOffWhitePanel(new FlowLayout(FlowLayout.LEADING, 5, 5));
        top.setBackground(Skin.LIGHTEST);
        add(top, StackedBorderLayout.NORTH);

        top.add(makeLabel("Pace:"));

        this.paceDisplay = makeTextField(2);
        top.add(this.paceDisplay);

        final JLabel trackHeader = makeLabel("Pace Track:");
        trackHeader.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));
        top.add(trackHeader);

        this.paceTrackDisplay = makeTextField(2);
        top.add(this.paceTrackDisplay);

        // Left side: Activity by week
        final JPanel left = makeOffWhitePanel(new BorderLayout(5, 5));
        left.setBackground(Skin.LIGHTEST);
        add(left, StackedBorderLayout.WEST);

        left.add(makeHeader("Activity by Week", true), BorderLayout.PAGE_START);

        this.activityTable = new JTableActivity();
        this.activityTable.setFillsViewportHeight(true);
        this.activityScroll = new JScrollPane(this.activityTable);
        left.add(this.activityScroll, BorderLayout.LINE_START);

        final int prefWidth = this.activityTable.getMinimumSize().width;
        this.activityScroll.setPreferredSize(new Dimension(prefWidth, Integer.MAX_VALUE));

        this.error = makeError();
        add(this.error, StackedBorderLayout.SOUTH);
    }

    /**
     * Sets the selected student.
     *
     * @param cache the cache
     * @param data  the selected student data
     */
    public void setSelectedStudent(final Cache cache, final StudentData data) {

        this.error.setText(CoreConstants.SPC);
        clearDisplay();

        if (data != null) {
            try {
                populateDisplay(cache, data);
            } catch (final SQLException ex) {
                Log.warning(ex);
            }

            this.activityScroll.setPreferredSize(this.activityTable.getPreferredScrollSize(this.activityScroll, 3));
        }
    }

    /**
     * Clears all displayed fields.
     */
    public void clearDisplay() {

        this.activityTable.clear();
    }

    /**
     * Populates all displayed fields for a selected student.
     *
     * @param cache the cache
     * @param data  the student data
     * @throws SQLException if there is an error accessing the database
     */
    private void populateDisplay(final Cache cache, final StudentData data) throws SQLException {

        final TermKey active = cache.getSystemData().getActiveTerm().term;

        if (active != null) {
            // Build a list of activities, including course exams, homeworks, placement attempts,
            // challenge attempts,

            final RawStterm stterm = data.studentTerm;

            if (stterm == null) {
                this.paceDisplay.setText("-");
                this.paceTrackDisplay.setText("-");
            } else {
                if (stterm.pace == null) {
                    this.paceDisplay.setText("?");
                } else {
                    this.paceDisplay.setText(stterm.pace.toString());
                }
                this.paceTrackDisplay.setText(stterm.paceTrack);
            }

            final List<RawSemesterCalendar> weeks = cache.getSystemData().getSemesterCalendars();

            final List<ActivityRow> rows = new ArrayList<>(10);

            final Collection<Integer> hitWeeks = new HashSet<>(10);
            final int todayWeek = determineWeek(LocalDate.now(), weeks);

            // Accumulate exams
            final List<RawStexam> exams = data.studentExams;
            for (final RawStexam exam : exams) {
                final int week = determineWeek(exam.examDt, weeks);
                final String activty =
                        RawRecordConstants.M100U.equals(exam.course) ? "User's Exam" : exam.examType;
                final LocalTime start = exam.getStartDateTime().toLocalTime();
                final LocalTime finish = exam.getFinishDateTime().toLocalTime();

                rows.add(new ActivityRow(week, exam.course, exam.unit, activty, exam.version,
                        exam.examDt, start, finish,
                        exam.examScore == null ? CoreConstants.EMPTY : exam.examScore.toString(),
                        exam.passed, "Y".equals(exam.isFirstPassed)));
                hitWeeks.add(Integer.valueOf(week));
            }

            // Accumulate homeworks
            final List<RawSthomework> homeworks = data.studentHomeworks;
            for (

                    final RawSthomework homework : homeworks) {

                final LocalDateTime fin = homework.getFinishDateTime();
                final LocalDateTime start = homework.getStartDateTime();

                final LocalDate date = fin.toLocalDate();
                final int week = determineWeek(date, weeks);
                final String course = homework.course;
                final Integer unit = homework.unit;
                final Integer obj = homework.objective;
                final Integer score = homework.hwScore;
                final String passed = homework.passed;
                final LocalTime sTime = start.toLocalTime();
                final LocalTime eTime = fin.toLocalTime();

                final String activity = obj == null ? "HW" : "HW " + unit + CoreConstants.DOT + obj;

                rows.add(new ActivityRow(week, course, unit, activity, homework.version,
                        homework.hwDt, sTime, eTime,
                        score == null ? CoreConstants.EMPTY : score.toString(), passed, false));
                hitWeeks.add(Integer.valueOf(week));
            }

            // Accumulate placement activities
            final List<RawStmpe> placements = data.studentPlacementAttempts;

            for (final RawStmpe placement : placements) {

                final int week = determineWeek(placement.examDt, weeks);
                final String passed = placement.placed;

                final LocalTime start = placement.getStartDateTime().toLocalTime();
                final LocalTime end = placement.getFinishDateTime().toLocalTime();

                final HtmlBuilder score = new HtmlBuilder(20);
                score.add(placement.stsA).add('/') //
                        .add(placement.sts117).add('/') //
                        .add(placement.sts118).add('/') //
                        .add(placement.sts124).add('/') //
                        .add(placement.sts125).add('/') //
                        .add(placement.sts126);

                rows.add(new ActivityRow(week, RawRecordConstants.M100P, null, //
                        "Placement", placement.version, placement.examDt, start, end,
                        score.toString(), passed, false));
                hitWeeks.add(Integer.valueOf(week));
            }

            // Accumulate challenge activities
            final List<RawStchallenge> challenges = data.studentChallengeAttempts;

            for (final RawStchallenge challenge : challenges) {

                final LocalDate date = challenge.examDt;

                final int week = determineWeek(date, weeks);
                final String course = challenge.course;
                final String passed = challenge.passed;
                final Integer score = challenge.score;

                final Integer startTime = challenge.startTime;
                final Integer endTime = challenge.finishTime;

                final LocalTime start = startTime == null ? null
                        : LocalTime.of(startTime.intValue() / 60, startTime.intValue() % 60);
                final LocalTime end = endTime == null ? null
                        : LocalTime.of(endTime.intValue() / 60, endTime.intValue() % 60);

                rows.add(new ActivityRow(week, course, null, "Challenge",
                        challenge.version, challenge.examDt, start, end,
                        score == null ? CoreConstants.EMPTY : score.toString(), passed, false));
                hitWeeks.add(Integer.valueOf(week));
            }

            // Ensure at least one row for each week
            for (int w = 1; w <= todayWeek; ++w) {
                if (!hitWeeks.contains(Integer.valueOf(w))) {
                    rows.add(new ActivityRow(w, null, null, "(none)", null, null,
                            null, null, null, null, false));
                }
            }

            Collections.sort(rows);

            this.activityTable.addData(rows, 2);

        } else {
            Log.warning("No active term!");
        }
    }

    /**
     * Determines the week number.
     *
     * @param date  the date
     * @param weeks the list of term weeks
     * @return 0 if the date is before all term week records,the week number of the matching record, or one larger than
     *         the largest week number if the date is beyond all term weeks
     */
    private static int determineWeek(final ChronoLocalDate date, final List<RawSemesterCalendar> weeks) {

        int result;

        if (date.isBefore(weeks.getFirst().startDt)) {
            result = 0;
        } else {
            final RawSemesterCalendar last = weeks.getLast();
            result = last.weekNbr.intValue() + 1;

            for (final RawSemesterCalendar test : weeks) {
                if (!date.isAfter(test.endDt)) {
                    result = test.weekNbr.intValue();
                    break;
                }
            }
        }

        return result;
    }
}

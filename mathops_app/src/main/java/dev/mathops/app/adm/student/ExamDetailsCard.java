package dev.mathops.app.adm.student;

import dev.mathops.app.adm.IZTableCommandListener;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.logic.CourseLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawMpscorequeueLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawStqaLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawStqa;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermRec;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * A card within the "Exams" tab of the admin app that shows details of a single exam, and supports changing the
 * "correct" status of items (automatically updating the score, passed field, etc. as needed).
 */
class ExamDetailsCard extends JPanel implements ActionListener, IZTableCommandListener<RawStqa> {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2074279291337741592L;

    /** Date formatter. */
    private static final DateTimeFormatter FMT_WMD = DateTimeFormatter.ofPattern("MM/dd/yyyy", Locale.US);

    /** Time formatter. */
    private static final DateTimeFormatter FMT_HM = DateTimeFormatter.ofPattern("hh':'mm a", Locale.US);

    /** An action command. */
    private static final String CLOSE_CMD = "CLOSE";

    /** The owning exams panel. */
    private final StudentExamsPanel owner;

    /** The data cache. */
    private final Cache cache;

    /** The database context used to access live data. */
    private final DbContext liveContext;

    /** The exam date field. */
    private final JTextField examDate;

    /** The course field. */
    private final JTextField course;

    /** The unit field. */
    private final JTextField unit;

    /** The exam type field. */
    private final JTextField examType;

    /** The exam version field. */
    private final JTextField version;

    /** The serial number field. */
    private final JTextField serial;

    /** The start time field. */
    private final JTextField startTime;

    /** The end time field. */
    private final JTextField endTime;

    /** The duration field. */
    private final JTextField duration;

    /** The score field. */
    private final JTextField score;

    /** The passed field. */
    private final JTextField passed;

    /** The answers table. */
    private final ZTableExamAnswers answersTable;

    /** The scroll pane for the exam answers table. */
    private final JScrollPane answersScroll;

    /**
     * Constructs a new {@code ExamDetailsCard}.
     *
     * @param theOwner          the owning discipline panel
     * @param theCache          the data cache
     * @param theLiveContext    the database context used to access live data
     * @param allowChangeAnswer true to allow the user to change exam answers
     */
    ExamDetailsCard(final StudentExamsPanel theOwner, final Cache theCache, final DbContext theLiveContext,
                    final boolean allowChangeAnswer) {

        super(new BorderLayout(10, 10));
        setBackground(Skin.WHITE);

        this.owner = theOwner;
        this.liveContext = theLiveContext;
        this.cache = theCache;

        final JPanel west = new JPanel(new BorderLayout());
        west.setBackground(Skin.WHITE);

        add(west, BorderLayout.WEST);

        final JPanel form = new JPanel(new BorderLayout());
        form.setBackground(Skin.LIGHT);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        west.add(form, BorderLayout.NORTH);

        final JLabel[] lbls = new JLabel[10];
        lbls[0] = new JLabel("Exam Date:");
        lbls[1] = new JLabel("Course:");
        lbls[2] = new JLabel("Exam Type:");
        lbls[3] = new JLabel("Version:");
        lbls[4] = new JLabel("Serial #:");
        lbls[5] = new JLabel("Start Time:");
        lbls[6] = new JLabel("Finish Time:");
        lbls[7] = new JLabel("Duration:");
        lbls[8] = new JLabel("Score:");
        lbls[9] = new JLabel("Passed:");

        int maxw = 0;
        int maxh = 0;
        for (final JLabel lbl : lbls) {
            lbl.setForeground(Skin.LABEL_COLOR);
            lbl.setFont(Skin.BOLD_12_FONT);
            lbl.setHorizontalAlignment(SwingConstants.RIGHT);
            final Dimension dim = lbl.getPreferredSize();
            maxw = Math.max(maxw, dim.width);
            maxh = Math.max(maxh, dim.height);
        }
        final Dimension dim = new Dimension(maxw, maxh);
        for (final JLabel lbl : lbls) {
            lbl.setPreferredSize(dim);
        }

        this.examDate = new JTextField(12);
        this.examDate.setFont(Skin.BODY_12_FONT);
        this.course = new JTextField(5);
        this.course.setFont(Skin.BODY_12_FONT);
        this.unit = new JTextField(2);
        this.unit.setFont(Skin.BODY_12_FONT);
        this.version = new JTextField(10);
        this.version.setFont(Skin.BODY_12_FONT);
        this.examType = new JTextField(20);
        this.examType.setFont(Skin.BODY_12_FONT);
        this.serial = new JTextField(12);
        this.serial.setFont(Skin.BODY_12_FONT);
        this.startTime = new JTextField(10);
        this.startTime.setFont(Skin.BODY_12_FONT);
        this.endTime = new JTextField(10);
        this.endTime.setFont(Skin.BODY_12_FONT);
        this.duration = new JTextField(10);
        this.duration.setFont(Skin.BODY_12_FONT);
        this.score = new JTextField(3);
        this.score.setFont(Skin.BODY_12_FONT);
        this.passed = new JTextField(10);
        this.passed.setFont(Skin.BODY_12_FONT);

        final JPanel inner1 = new JPanel(new BorderLayout());
        inner1.setBackground(Skin.LIGHT);
        form.add(inner1, BorderLayout.CENTER);

        final JPanel inner2 = new JPanel(new BorderLayout());
        inner2.setBackground(Skin.LIGHT);
        inner1.add(inner2, BorderLayout.CENTER);

        final JPanel inner3 = new JPanel(new BorderLayout());
        inner3.setBackground(Skin.LIGHT);
        inner2.add(inner3, BorderLayout.CENTER);

        final JPanel inner4 = new JPanel(new BorderLayout());
        inner4.setBackground(Skin.LIGHT);
        inner3.add(inner4, BorderLayout.CENTER);

        // 01 0N Date
        // 02 1N Course/Unit
        // 03 2N Type
        // 04 3N Version
        // 05 4N Serial
        // 06 4S Start
        // 07 3S End
        // 08 2S Duration
        // 09 1S Score
        // 10 0S Passed

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow1.setBackground(Skin.LIGHT);
        flow1.add(lbls[0]);
        flow1.add(this.examDate);
        form.add(flow1, BorderLayout.NORTH);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow2.setBackground(Skin.LIGHT);
        flow2.add(lbls[1]);
        flow2.add(this.course);
        final JLabel unitLbl = new JLabel(", Unit ");
        flow2.add(unitLbl);
        flow2.add(this.unit);
        inner1.add(flow2, BorderLayout.NORTH);

        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow3.setBackground(Skin.LIGHT);
        flow3.add(lbls[2]);
        flow3.add(this.examType);
        inner2.add(flow3, BorderLayout.NORTH);

        final JPanel flow4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow4.setBackground(Skin.LIGHT);
        flow4.add(lbls[3]);
        flow4.add(this.version);
        inner3.add(flow4, BorderLayout.NORTH);

        final JPanel flow5 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow5.setBackground(Skin.LIGHT);
        flow5.add(lbls[4]);
        flow5.add(this.serial);
        inner4.add(flow5, BorderLayout.NORTH);

        final JPanel flow6 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow6.setBackground(Skin.LIGHT);
        flow6.add(lbls[5]);
        flow6.add(this.startTime);
        inner4.add(flow6, BorderLayout.SOUTH);

        final JPanel flow7 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow7.setBackground(Skin.LIGHT);
        flow7.add(lbls[6]);
        flow7.add(this.endTime);
        inner3.add(flow7, BorderLayout.SOUTH);

        final JPanel flow8 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow8.setBackground(Skin.LIGHT);
        flow8.add(lbls[7]);
        flow8.add(this.duration);
        inner2.add(flow8, BorderLayout.SOUTH);

        final JPanel flow9 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow9.setBackground(Skin.LIGHT);
        flow9.add(lbls[8]);
        flow9.add(this.score);
        inner1.add(flow9, BorderLayout.SOUTH);

        final JPanel flow10 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        flow10.setBackground(Skin.LIGHT);
        flow10.add(lbls[9]);
        flow10.add(this.passed);
        form.add(flow10, BorderLayout.SOUTH);

        // Center: answers, with ability to update correctness of each

        final JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Skin.WHITE);
        add(center, BorderLayout.CENTER);

        this.answersTable = new ZTableExamAnswers(this, allowChangeAnswer);
        this.answersScroll = new JScrollPane(this.answersTable);
        this.answersScroll.getVerticalScrollBar().setUnitIncrement(20);
        this.answersScroll.getVerticalScrollBar().setBlockIncrement(200);
        this.answersScroll
                .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        center.add(this.answersScroll, BorderLayout.WEST);

        final int prefWidth = this.answersTable.getPreferredSize().width
                + this.answersScroll.getVerticalScrollBar().getPreferredSize().width + 10;

        this.answersScroll.setPreferredSize(new Dimension(prefWidth, Integer.MAX_VALUE));

        // Buttons

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        buttons.setBackground(Skin.WHITE);

        final JButton cancelButton = new JButton("Close");
        cancelButton.setActionCommand(CLOSE_CMD);
        cancelButton.addActionListener(this);
        buttons.add(cancelButton);

        add(buttons, BorderLayout.SOUTH);
    }

    /**
     * Resets the form.
     */
    void reset() {

        this.examDate.setText(CoreConstants.EMPTY);
        this.course.setText(CoreConstants.EMPTY);
        this.unit.setText(CoreConstants.EMPTY);
        this.examType.setText(CoreConstants.EMPTY);
        this.version.setText(CoreConstants.EMPTY);
        this.serial.setText(CoreConstants.EMPTY);
        this.startTime.setText(CoreConstants.EMPTY);
        this.endTime.setText(CoreConstants.EMPTY);
        this.duration.setText(CoreConstants.EMPTY);
        this.score.setText(CoreConstants.EMPTY);
        this.passed.setText(CoreConstants.EMPTY);
        this.answersTable.clear();
    }

    /**
     * Sets the current record.
     *
     * @param record the current record
     */
    public void setCurrent(final ExamListRow record) {

        final RawStexam stexam = record.examRecord;

        this.examDate.setText(FMT_WMD.format(stexam.examDt));

        this.course.setText(valueToString(stexam.course));
        this.unit.setText(valueToString(stexam.unit));

        // NOTE: The "stexam" table uses "Q" for user's exams
        final String type;
        switch (stexam.examType) {
            case RawStexam.QUALIFYING_EXAM -> type = "User's Exam";
            case RawStexam.REVIEW_EXAM -> {
                if (Integer.valueOf(0).equals(record.examRecord.unit)) {
                    type = "Skills Rev.";
                } else {
                    type = "Unit Rev.";
                }
            }
            case RawStexam.UNIT_EXAM -> type = "Unit Exam";
            case RawStexam.FINAL_EXAM -> type = "Final Exam";
            case null, default -> type = valueToString(stexam.examType);
        }

        this.examType.setText(type);
        this.version.setText(valueToString(stexam.version));
        this.serial.setText(valueToString(stexam.serialNbr));

        final LocalDateTime sta = stexam.getStartDateTime();
        final LocalDateTime fin = stexam.getFinishDateTime();

        this.startTime.setText(sta == null ? CoreConstants.EMPTY : FMT_HM.format(sta.toLocalTime()));

        this.endTime.setText(FMT_HM.format(fin.toLocalTime()));

        String durstr = null;
        if (sta != null) {
            final long sec = Duration.between(sta, fin).getSeconds();
            if ((sec % 60L) == 0L) {
                durstr = (sec / 60L) + " min";
            } else {
                durstr = String.format("%d:%02d", Long.valueOf(sec / 60L),
                        Long.valueOf(sec % 60L));
            }
        }
        this.duration.setText(valueToString(durstr));
        this.score.setText(valueToString(stexam.examScore));

        final String passedStr;
        if ("Y".equals(stexam.passed)) {
            if ("Y".equals(stexam.isFirstPassed)) {
                passedStr = "Yes (First)";
            } else {
                passedStr = "Yes";
            }
        } else if ("N".equals(stexam.passed)) {
            passedStr = "No";
        } else {
            passedStr = stexam.passed;
        }

        this.passed.setText(valueToString(passedStr));

        this.answersTable.setData(record.answers);

        final int prefWidth = this.answersTable.getPreferredSize().width
                + this.answersScroll.getVerticalScrollBar().getPreferredSize().width + 10;

        this.answersScroll.setPreferredSize(new Dimension(prefWidth, Integer.MAX_VALUE));
    }

    /**
     * Called when the "Submit" or "Reset" button is pressed.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (CLOSE_CMD.equals(cmd)) {
            reset();
            this.owner.closeDetails();
        }
    }

    /**
     * Called when the update button is pressed in an exam answer row.
     *
     * @param rowIndex the row index
     * @param rowData  the student exam answer record being updated
     * @param cmd      the action command
     */
    @Override
    public void commandOnRow(final int rowIndex, final RawStqa rowData,
                             final String cmd) {

        if ("COR".equals(cmd)) {
            this.answersTable.setUpdateButtonStates(rowIndex);
        } else if ("UPDATE".equals(cmd)) {
            Log.info("Updating question " + rowData.questionNbr);

            final String newCorrect = "N".equals(rowData.ansCorrect) ? "Y" : "N";

            try {
                RawStqaLogic.updateAnsCorrect(this.cache, rowData, newCorrect);
                updateExamPassed(rowData.stuId, rowData.serialNbr);
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Recalculates the score for an exam and checks to see if the "passed" field needs to be changed.
     *
     * @param stuId     the student ID
     * @param serialNbr the exam serial number
     */
    private void updateExamPassed(final String stuId, final Long serialNbr) {

        try {
            final List<RawStexam> exams = RawStexamLogic.queryByStudent(this.cache, stuId, true);

            RawStexam match = null;
            for (final RawStexam exam : exams) {
                if (exam.serialNbr.equals(serialNbr)) {
                    match = exam;
                    break;
                }
            }

            if (match == null) {
                Log.warning("Exam not found!");
            } else {
                final List<RawStqa> answers = RawStqaLogic.queryBySerial(this.cache, serialNbr);
                int rawScore = 0;
                for (final RawStqa row : answers) {
                    if ("Y".equals(row.ansCorrect)) {
                        ++rawScore;
                    }
                }

                RawStcourse reg = null;
                final TermRec active = this.cache.getSystemData().getActiveTerm();
                if (active != null) {
                    final List<RawStcourse> regs =
                            RawStcourseLogic.getActiveForStudent(this.cache, stuId, active.term);
                    for (final RawStcourse test : regs) {
                        if (test.course.equals(match.course)) {
                            reg = test;
                            break;
                        }
                    }
                }

                if (match.masteryScore == null && reg != null) {
                    final int mastery = determineMasteryScore(match, reg);
                    match.masteryScore = Integer.valueOf(mastery);

                    RawStexamLogic.updateMasteryScore(this.cache, match, match.masteryScore);
                }

                Log.info("Mastery score is ", match.masteryScore);

                final String thePassed;
                boolean passedChanged = false;
                if (rawScore >= match.masteryScore.intValue()) {
                    thePassed = "Y";
                } else {
                    thePassed = "N";
                }

                final Integer theScore = Integer.valueOf(rawScore);
                if (!theScore.equals(match.examScore)) {
                    Log.info("Updating exam score from ", match.examScore, " to ", theScore,
                            ", passed to ", thePassed);
                    RawStexamLogic.updateScoreAndPassed(this.cache, match, theScore, thePassed);
                }
                match.examScore = theScore;
                if (!thePassed.equals(match.passed)) {
                    match.passed = thePassed;
                    passedChanged = true;
                }

                if (passedChanged) {
                    // Depending on exam type, determine consequences of change
                    final String crs = match.course;

                    if (RawRecordConstants.M117.equals(crs) //
                            || RawRecordConstants.M118.equals(crs)
                            || RawRecordConstants.M124.equals(crs)
                            || RawRecordConstants.M125.equals(crs)
                            || RawRecordConstants.M126.equals(crs)) {

                        if (reg == null) {
                            final String[] message = new String[3];
                            message[0] = "student exam score in " + crs + " was changed,";
                            message[1] = "but student is not registered in " + crs + ".";
                            message[2] = "Skipping checks for 'completed' state update.";

                            JOptionPane.showMessageDialog(this, message);
                        } else {
                            courseExamConsequences(reg);
                        }
                    } else if (RawRecordConstants.M1170.equals(crs)
                            || RawRecordConstants.M1180.equals(crs)
                            || RawRecordConstants.M1240.equals(crs)
                            || RawRecordConstants.M1250.equals(crs)
                            || RawRecordConstants.M1260.equals(crs)
                            || RawRecordConstants.M100T.equals(crs)) {

                        tutorialExamConsequences(match);
                    } else if (RawRecordConstants.M100U.equals(crs)) {

                        usersExamConsequences(this.cache, match);
                    }
                }
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Determines the mastery score for an exam.
     *
     * @param exam the student exam record
     * @param reg  the student registration
     * @return the mastery score
     */
    private int determineMasteryScore(final RawStexam exam, final RawStcourse reg) {

        final String crs = exam.course;
        final String type = exam.examType;

        int mastery;

        final SystemData systemData = this.cache.getSystemData();

        // Identify the course section whose mastery scores to use
        try {
            final TermRec active = systemData.getActiveTerm();
            final List<RawCsection> sections = systemData.getCourseSections(active.term);

            sections.removeIf(sect -> !sect.course.equals(exam.course));

            RawCsection matchedSect = null;
            final int numSectRemain = sections.size();
            if (numSectRemain == 1 || reg == null) {
                matchedSect = sections.getFirst();
            } else {
                for (final RawCsection test : sections) {
                    if (test.sect.equals(reg.sect)) {
                        matchedSect = test;
                        break;
                    }
                }
                if (matchedSect == null && numSectRemain > 0) {
                    matchedSect = sections.getFirst();
                }
            }

            if (matchedSect == null) {
                Log.warning("Unable to determine section number to lookup mastery score.");
                mastery = guessMasteryScore(exam);
            } else {
                final RawCusection cusect = systemData.getCourseUnitSection(crs, matchedSect.sect, exam.unit,
                        active.term);

                if (cusect == null) {
                    Log.warning("Unable to lookup cusection record.");
                    mastery = guessMasteryScore(exam);
                } else if ((RawStexam.REVIEW_EXAM.equals(type) && cusect.reMasteryScore != null)
                        || (RawStexam.QUALIFYING_EXAM.equals(type) && cusect.reMasteryScore != null)) {
                    mastery = cusect.reMasteryScore.intValue();
                } else if ((RawStexam.UNIT_EXAM.equals(type) && cusect.ueMasteryScore != null)
                        || (RawStexam.FINAL_EXAM.equals(type) && cusect.ueMasteryScore != null)) {
                    mastery = cusect.ueMasteryScore.intValue();
                } else {
                    Log.warning("No score data in cusection.");
                    mastery = guessMasteryScore(exam);
                }
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            mastery = guessMasteryScore(exam);
        }

        return mastery;
    }

    /**
     * Guesses the mastery score for an exam.
     *
     * @param exam the student exam record
     * @return the mastery score
     */
    private static int guessMasteryScore(final RawStexam exam) {

        final int mastery;

        final String course = exam.course;
        final int unit = exam.unit.intValue();

        // Guess
        mastery = switch (course) {
            case RawRecordConstants.M117, RawRecordConstants.M118, RawRecordConstants.M124, RawRecordConstants.M125,
                 RawRecordConstants.M126 -> unit < 5 ? 8 : 16;
            case RawRecordConstants.M1170, RawRecordConstants.M1180, RawRecordConstants.M1240, RawRecordConstants.M1250,
                 RawRecordConstants.M1260, RawRecordConstants.M100T -> unit == 1 ? 7 : unit == 2 ? 11 : 14;
            case RawRecordConstants.M100U -> 16;
            case null, default -> 0;
        };

        return mastery;
    }

    /**
     * Determines consequences of a changed exam record in a course. The consequence here could be a change to the
     * "Completed" status if the course.
     *
     * @param reg the student's registration
     */
    private void courseExamConsequences(final RawStcourse reg) {

        String error;
        try {
            error = CourseLogic.checkForComplete(this.cache, reg);
        } catch (final SQLException ex) {
            error = ex.getMessage();
        }

        if (error != null) {
            final String[] message = new String[2];
            message[0] = "Failed to check course completion status:";
            message[1] = error;

            JOptionPane.showMessageDialog(this, message);
        }
    }

    /**
     * Determines consequences of a changed exam record in a tutorial. The consequence here could be a change to the
     * student's placement outcomes.
     *
     * @param stexam the student exam record
     */
    private void tutorialExamConsequences(final RawStexam stexam) {

        // Only consequence comes if the passed status on a proctored exam (type U) changes.

        if (RawStexam.UNIT_EXAM.equals(stexam.examType)) {
            try {
                final String crs = stexam.course;

                final List<RawStexam> exams =
                        RawStexamLogic.queryByStudentCourse(this.cache, stexam.stuId, crs, false);

                boolean isPassed = false;
                for (final RawStexam test : exams) {
                    if (RawStexam.UNIT_EXAM.equals(test.examType) && stexam.unit.equals(test.unit)
                            && "Y".equals(test.passed)) {
                        isPassed = true;
                        break;
                    }
                }

                if (isPassed) {
                    // There should be a corresponding placement result
                    if (RawRecordConstants.M100T.equals(crs)) {
                        ensurePlacement(stexam, RawRecordConstants.M100C);
                    } else if (RawRecordConstants.M1170.equals(crs)) {
                        ensurePlacement(stexam, RawRecordConstants.M117);
                    } else if (RawRecordConstants.M1180.equals(crs)) {
                        ensurePlacement(stexam, RawRecordConstants.M118);
                    } else if (RawRecordConstants.M1240.equals(crs)) {
                        ensurePlacement(stexam, RawRecordConstants.M124);
                    } else if (RawRecordConstants.M1250.equals(crs)) {
                        ensurePlacement(stexam, RawRecordConstants.M125);
                    } else if (RawRecordConstants.M1260.equals(crs)) {
                        ensurePlacement(stexam, RawRecordConstants.M126);
                    }
                } else // There should NOT be a corresponding placement result, but we don't
                    // automatically take such a result away - instead, we alert the user that
                    if (RawRecordConstants.M100T.equals(crs)) {
                        alertIfPlacement(stexam.stuId, RawRecordConstants.M100C);
                    } else if (RawRecordConstants.M1170.equals(crs)) {
                        alertIfPlacement(stexam.stuId, RawRecordConstants.M117);
                    } else if (RawRecordConstants.M1180.equals(crs)) {
                        alertIfPlacement(stexam.stuId, RawRecordConstants.M118);
                    } else if (RawRecordConstants.M1240.equals(crs)) {
                        alertIfPlacement(stexam.stuId, RawRecordConstants.M124);
                    } else if (RawRecordConstants.M1250.equals(crs)) {
                        alertIfPlacement(stexam.stuId, RawRecordConstants.M125);
                    } else if (RawRecordConstants.M1260.equals(crs)) {
                        alertIfPlacement(stexam.stuId, RawRecordConstants.M126);
                    }

            } catch (final SQLException ex) {
                Log.warning("Failed to query all user's exams.", ex);
            }
        }
    }

    /**
     * Ensures that there is a placement outcome in a specific course.
     *
     * @param stexam    the exam record
     * @param theCourse the course in which there should be a "placed out" result
     * @throws SQLException if there is an error accessing the database
     */
    private void ensurePlacement(final RawStexam stexam, final String theCourse)
            throws SQLException {

        final String stuId = stexam.stuId;
        final List<RawMpeCredit> allCredit = RawMpeCreditLogic.queryByStudent(this.cache, stuId);

        boolean found = false;
        for (final RawMpeCredit test : allCredit) {
            if (test.course.equals(theCourse) && ("P".equals(test.examPlaced)
                    || "C".equals(test.examPlaced))) {
                found = true;
                break;
            }
        }

        if (!found) {
            Log.info("*** Recording placement in '", theCourse,
                    "' for ", stuId);

            final RawMpeCredit newRow = new RawMpeCredit(stuId, theCourse, "P",
                    stexam.examDt, null, stexam.serialNbr, stexam.version, stexam.examSource);
            RawMpeCreditLogic.INSTANCE.apply(this.cache, newRow);

            // Send results to BANNER, or store in queue table
            final RawStudent stu = RawStudentLogic.query(this.cache, stuId, false);

            if (stu == null) {
                RawMpscorequeueLogic.logActivity(//
                        "Unable to upload placement result for student "
                                + stuId + ": student record not found");
            } else {
                final DbConnection liveConn = this.liveContext.checkOutConnection();
                try {
                    if (RawRecordConstants.M100C.equals(theCourse)) {
                        RawMpscorequeueLogic.INSTANCE.postELMTutorialResult(this.cache, liveConn,
                                stu.pidm, stexam.getFinishDateTime());
                    } else {
                        RawMpscorequeueLogic.INSTANCE.postPrecalcTutorialResult(this.cache,
                                liveConn, stu.pidm, theCourse, stexam.getFinishDateTime());
                    }
                } finally {
                    this.liveContext.checkInConnection(liveConn);
                }
            }
        }
    }

    /**
     * Checks for a placement result in a specific course, and alerts the user if so (since the result may have
     * changed).
     *
     * @param stuId     the student ID
     * @param theCourse the course in which there should be a "placed out" result
     * @throws SQLException if there is an error accessing the database
     */
    private void alertIfPlacement(final String stuId, final String theCourse) throws SQLException {

        final List<RawMpeCredit> allCredit = RawMpeCreditLogic.queryByStudent(this.cache, stuId);

        boolean found = false;
        for (final RawMpeCredit test : allCredit) {
            if (test.course.equals(theCourse) && ("P".equals(test.examPlaced)
                    || "C".equals(test.examPlaced))) {
                found = true;
                break;
            }
        }

        if (found) {
            final String[] message = new String[3];
            message[0] = "Student has placed out of '" + theCourse + "' in STMPE table.";
            message[1] = "This result may no longer be correct.";
            message[2] = "Please verify and update as needed.";

            JOptionPane.showMessageDialog(this, message);
        }
    }

    /**
     * Determines consequences of a changed User's exam record. The consequence here could be a change to the student's
     * "licensed" status.
     *
     * @param cache  the data cache
     * @param stexam the student exam record
     */
    private static void usersExamConsequences(final Cache cache, final RawStexam stexam) {

        try {
            final List<RawStexam> usersExams =
                    RawStexamLogic.queryByStudentCourse(cache, stexam.stuId, stexam.course, false);

            String licensed = "N";
            for (final RawStexam test : usersExams) {
                if ("Y".equals(test.passed)) {
                    licensed = "Y";
                    break;
                }
            }

            final RawStudent stu = RawStudentLogic.query(cache, stexam.stuId, false);
            if (stu != null && !licensed.equals(stu.licensed)) {
                Log.info("Updating 'licensed' status of student record to ", licensed);
                RawStudentLogic.updateLicensed(cache, stexam.stuId, licensed);
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query all user's exams.", ex);
        }
    }

    /**
     * Given an object value, returns the empty string if the object is null, or the string representation of the
     * object.
     *
     * @param value the value
     * @return the string representation
     */
    private static String valueToString(final Object value) {

        return value == null ? CoreConstants.EMPTY : value.toString();
    }
}

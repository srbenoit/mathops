package dev.mathops.db.oldadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.CourseLogic;
import dev.mathops.db.old.logic.CourseStatus;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rec.RecBase;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;

import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The Course screen.
 */
final class ScreenCourse extends AbstractStudentScreen {

    /** A single instance. */
    private static final Comparator<RawStcourse> COURSE_SORT = new StCourseSort();

    /** A single instance. */
    private static final Comparator<RawSthomework> HW_SORT = new SthwSort();

    /** The character to select "Pick". */
    private static final char PICK_CHAR = 'p';

    /** The character to select "Course". */
    private static final char HISTORY_CHAR = 'h';

    /** The character to select "Schedule". */
    private static final char CURRENT_CHAR = 'c';

    /** The character to select "Discipline". */
    private static final char HOMEWORK_CHAR = 'w';

    /** The character to select "Lock". */
    private static final char LOCK_CHAR = 'k';

    /** The character to select "Quit". */
    private static final char QUIT_CHAR = 'q';

    /** The display. */
    private ScreenCourseDisplay display = ScreenCourseDisplay.NONE;

    /** The set of course history rows to show. */
    private final List<String> history;

    /** The index of the current "first" row in the history display. */
    private int firstHistoryIndex;

    /** The zero-based line where the cursor currently is in the history list. */
    private int historyCursorLine;

    /** The set of current courses to show. */
    private final List<String> current;

    /** A list of detail strings for each course. */
    private final List<List<String>> currentDetail;

    /** The index of the selected current course. */
    private int currentCourseIndex;

    /** The set of homework rows to show. */
    private final List<String> homework;

    /** The index of the current "first" homework row in the display. */
    private int firstHomeworkIndex;

    /** The zero-based line where the cursor currently is in the homework list. */
    private int homeworkCursorLine;

    /** Flag indicating there is an active "Press any key to continue" prompt. */
    private boolean clearPressAnyKey;

    /**
     * Constructs a new {@code ScreenCourse}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenCourse(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow, 6);

        this.history = new ArrayList<>(20);
        this.current = new ArrayList<>(6);
        this.currentDetail = new ArrayList<>(6);
        this.homework = new ArrayList<>(20);

        this.clearPressAnyKey = false;
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        final Console console = getConsole();

        console.clear();
        console.print("COURSE OPTIONS:   History  Current  homeWork_rpt  Pick  locK  QUIT", 0, 0);

        switch (getSelection()) {
            case 0:
                console.reverse(17, 0, 9);
                console.print("View registration history in PACe courses", 0, 1);
                break;
            case 1:
                console.reverse(26, 0, 9);
                console.print("View current PACe course registrations", 0, 1);
                break;
            case 2:
                console.reverse(35, 0, 14);
                console.print("View homework record for student in selected course/section", 0, 1);
                break;
            case 3:
                console.reverse(49, 0, 6);
                console.print("Select a different student", 0, 1);
                break;
            case 4:
                console.reverse(55, 0, 6);
                console.print("Lock the terminal to restrict unauthorized use", 0, 1);
                break;
            case 5:
                console.reverse(61, 0, 6);
                console.print("Return to MAIN ADMIN menu", 0, 1);
                break;
        }

        if (isLocked()) {
            drawLocked();
        } else if (isPicking()) {
            drawPickBox();
        } else if (getStudent() != null) {
            drawStudentNameId();

            switch (this.display) {
                case HISTORY -> drawHistory();
                case CURRENT -> drawCurrent();
                case HOMEWORK -> drawHomework();
            }
        }

        drawErrors();

        console.commit();
    }

    /**
     * Gathers course history.
     */
    private void gatherHistory() {

        this.history.clear();

        final RawStudent stu = getStudent();

        final Cache cache = getCache();
        final SystemData systemData = cache.getSystemData();

        try {
            final TermRec active = systemData.getActiveTerm();
            final List<RawStcourse> stcFull = RawStcourseLogic.queryByStudent(cache, stu.stuId, true, true);

            // Sort full list by term (descending) then by course
            stcFull.sort(COURSE_SORT);

            final StringBuilder builder = new StringBuilder(40);

            for (final RawStcourse stc : stcFull) {
                String pacing = null;
                final RawCsection csect = systemData.getCourseSection(stc.course, stc.sect, stc.termKey);
                if (csect != null) {
                    pacing = csect.pacingStructure;
                }

                final String pacingName;
                if ("OT".equals(csect.instrnType)) {
                    pacingName = "Chall. Exam ";
                } else if ("M".equals(pacing)) {
                    pacingName = "Stu Manage ";
                } else if ("O".equals(pacing)) {
                    pacingName = "CSU Online ";
                } else if ("K".equals(pacing)) {
                    pacingName = "KEY Acad.  ";
                } else if ("I".equals(pacing)) {
                    pacingName = "Instr Led  ";
                } else if ("S".equals(pacing)) {
                    pacingName = "Standards  ";
                } else {
                    pacingName = "Unknown    ";
                }

                final TermKey dispTerm;
                if (active.term.equals(stc.termKey)) {
                    if ("Y".equals(stc.iInProgress)) {
                        dispTerm = stc.iTermKey;
                    } else {
                        dispTerm = stc.termKey;
                    }
                } else {
                    dispTerm = stc.termKey;
                }

                builder.append(stc.course);
                builder.append(" ".repeat(Math.max(0, 10 - stc.course.length())));
                builder.append(stc.sect);
                builder.append(" ".repeat(Math.max(0, 10 - stc.sect.length())));
                builder.append(dispTerm.name.termName);
                builder.append("  ");
                builder.append(dispTerm.year);
                builder.append("   ");

                if ("Y".equals(stc.iInProgress)) {
                    if ("Y".equals(stc.iCounted)) {
                        builder.append("I, count ");
                    } else {
                        builder.append("I        ");
                    }
                } else if ("D".equals(stc.openStatus)) {
                    builder.append("(drop)   ");
                } else if (stc.courseGrade == null) {
                    builder.append("         ");
                } else {
                    builder.append("  ");
                    builder.append(stc.courseGrade);
                    builder.append(" ".repeat(Math.max(0, 7 - stc.courseGrade.length())));
                }
                builder.append(pacingName);

                final String regString = builder.toString();
                this.history.add(regString);

                builder.setLength(0);
            }

            this.firstHistoryIndex = 0;
            this.historyCursorLine = 0;
        } catch (final SQLException ex) {
            final String msg = ex.getMessage();
            setError("Unable to query course history", msg);
        }
    }

    /**
     * Gathers current courses.
     */
    private void gatherCurrent() {

        this.current.clear();
        this.currentDetail.clear();

        final RawStudent stu = getStudent();

        final DecimalFormat fmt = new DecimalFormat("0.#");
        final LocalDate today = LocalDate.now();

        final Cache cache = getCache();
        final SystemData systemData = cache.getSystemData();
        try {
            final TermRec active = systemData.getActiveTerm();
            final List<RawStcourse> stcFull = RawStcourseLogic.queryByStudent(cache, stu.stuId, active.term, true,
                    false);

            // Sort full list by term (descending) then by course
            stcFull.sort(COURSE_SORT);

            final StringBuilder builder = new StringBuilder(40);

            for (final RawStcourse stc : stcFull) {
                final List<String> detailsList = new ArrayList<>(15);

                String pacing = null;
                String instrn = null;

                final RawCsection csect;
                if ("Y".equals(stc.iInProgress)) {
                    csect = systemData.getCourseSection(stc.course, stc.sect, stc.iTermKey);
                } else {
                    csect = systemData.getCourseSection(stc.course, stc.sect, stc.termKey);
                }

                if (csect != null) {
                    pacing = csect.pacingStructure;
                    instrn = csect.instrnType;
                }

                final String pacingName;

                if ("OT".equals(instrn)) {
                    pacingName = "Chall. Exam ";
                } else if ("M".equals(pacing)) {
                    pacingName = "Stu Manage  ";
                } else if ("O".equals(pacing)) {
                    pacingName = "CSU Online  ";
                } else if ("K".equals(pacing)) {
                    pacingName = "KEY Acad.   ";
                } else if ("I".equals(pacing)) {
                    pacingName = "Instr Led   ";
                } else if ("S".equals(pacing)) {
                    pacingName = "Standards   ";
                } else {
                    pacingName = "Unknown     ";
                }

                builder.append(stc.course);
                builder.append(" ".repeat(Math.max(0, 10 - stc.course.length())));
                builder.append(stc.sect);
                builder.append(" ".repeat(Math.max(0, 9 - stc.sect.length())));
                builder.append(pacingName);

                if (stc.openStatus == null) {
                    builder.append("       ");
                } else {
                    builder.append(' ');
                    builder.append(stc.openStatus);
                    builder.append("     ");
                }

                if ("Y".equals(stc.iInProgress)) {
                    builder.append(" Y      ");
                    if ("Y".equals(stc.iCounted)) {
                        builder.append("Y");
                    } else {
                        builder.append("N         ");
                        builder.append(TemporalUtils.FMT_MDY.format(stc.iDeadlineDt));
                    }
                }

                final String regString = builder.toString();
                this.current.add(regString);
                builder.setLength(0);

                // Construct details for this course
                builder.append("Course: ");
                builder.append(stc.course);

                final RawCourse course = cache.getSystemData().getCourse(stc.course);
                if (course != null) {
                    builder.append("  ");
                    builder.append(course.courseName);
                }

                final String str1 = builder.toString();
                detailsList.add(str1);
                builder.setLength(0);

                final CourseStatus status = CourseLogic.computeStatus(cache, stc);

                if ("Y".equals(stc.iInProgress)) {
                    builder.append("Incomplete from ");
                    builder.append(stc.iTermKey.longString);
                    if ("Y".equals(stc.iCounted)) {
                        builder.append(" (counted in pace)");
                    } else {
                        builder.append(" (deadline : ");
                        builder.append(TemporalUtils.FMT_MDY.format(stc.iDeadlineDt));
                        builder.append(")");
                    }

                    final String str2 = builder.toString();
                    detailsList.add(str2);
                    builder.setLength(0);
                }

                detailsList.add(CoreConstants.EMPTY);

                if ("OT".equals(status.csection.instrnType)) {
                    detailsList.add("Challenge Exam Credit");
                } else {
                    final CourseStatus.LegacyCourseStatus legacyStatus = status.legacyStatus;

                    if (legacyStatus == null) {
                        // A "Standards Mastery" course
                    } else {
                        // A legacy course
                        final float percentage = (float) legacyStatus.totalScore / 0.72f;

                        builder.append("Current average:     ");
                        builder.append(fmt.format(percentage));
                        builder.append("%");
                        final String str3 = builder.toString();
                        detailsList.add(str3);
                        builder.setLength(0);

                        final int score = legacyStatus.totalScore;
                        final int uePoints = legacyStatus.bestPassingUE[0] + legacyStatus.bestPassingUE[1]
                                + legacyStatus.bestPassingUE[2] + legacyStatus.bestPassingUE[3]
                                + legacyStatus.bestPassingFE;
                        final int rePoints = score - uePoints;

                        builder.append("Current UE points:   ");
                        builder.append(uePoints);
                        while (builder.length() < 30) {
                            builder.append(' ');
                        }
                        builder.append("Current RE points:   ");
                        builder.append(rePoints);

                        final String str4 = builder.toString();
                        detailsList.add(str4);
                        builder.setLength(0);

                        builder.append("Current point total: ");
                        builder.append(score);
                        builder.append(" / 72");
                        while (builder.length() < 30) {
                            builder.append(' ');
                        }
                        builder.append("Current Grade:       ");

                        if (status.csection.aMinScore == null) {
                            builder.append("Unknown");
                        } else if (score >= status.csection.aMinScore.intValue()) {
                            builder.append("A");
                        } else if (status.csection.bMinScore != null && score >= status.csection.bMinScore.intValue()) {
                            builder.append("B");
                        } else if (status.csection.cMinScore != null && score >= status.csection.cMinScore.intValue()) {
                            builder.append("C");
                        } else if (status.csection.dMinScore != null && score >= status.csection.dMinScore.intValue()) {
                            builder.append("D");
                        } else {
                            builder.append("U");
                        }

                        final String str5 = builder.toString();
                        detailsList.add(str5);
                        builder.setLength(0);

                        detailsList.add(CoreConstants.EMPTY);

                        detailsList.add("       Due    RE On   Best UE   Min     Points    Points   UE Exam");
                        detailsList.add("Unit   Date   Time?   Score     Score   From RE   From UE  Tries");
                        // .............. 1     Dec 31  Y       10        8       3         10       4

                        for (int i = 0; i < 4; ++i) {
                            builder.append(' ');
                            builder.append(i + 1);
                            builder.append("     ");
                            builder.append(TemporalUtils.FMT_MD.format(legacyStatus.reDueDates[i]));
                            while (builder.length() < 15) {
                                builder.append(' ');
                            }

                            final boolean pastDue = today.isAfter(legacyStatus.reDueDates[i]);

                            builder.append(legacyStatus.reOnTimes[i] ? 'Y' : pastDue ? 'N' : ' ');
                            builder.append("       ");
                            if (legacyStatus.numUE[i] == 0) {
                                builder.append('-');
                            } else {
                                builder.append(legacyStatus.bestPassingUE[i] == 0 ? legacyStatus.bestFailedUE[i] :
                                        legacyStatus.bestPassingUE[i]);
                            }
                            while (builder.length() < 33) {
                                builder.append(' ');
                            }
                            builder.append("8       ");

                            builder.append(legacyStatus.reOnTimes[i] ? '3' : pastDue ? '0' : '-');
                            builder.append("         ");
                            builder.append(legacyStatus.bestPassingUE[i]);
                            while (builder.length() < 60) {
                                builder.append(' ');
                            }
                            builder.append(legacyStatus.numUE[i]);

                            final String strUE = builder.toString();
                            detailsList.add(strUE);
                            builder.setLength(0);
                        }

                        builder.append("FIN    ");
                        builder.append(TemporalUtils.FMT_MD.format(legacyStatus.feDueDate));
                        while (builder.length() < 15) {
                            builder.append(' ');
                        }

                        builder.append("-       ");
                        if (legacyStatus.numFE == 0) {
                            builder.append('-');
                        } else {
                            builder.append(legacyStatus.bestPassingFE == 0 ? legacyStatus.bestFailedFE :
                                    legacyStatus.bestPassingFE);
                        }
                        while (builder.length() < 33) {
                            builder.append(' ');
                        }
                        builder.append("16      -         ");
                        builder.append(legacyStatus.bestPassingFE);
                        while (builder.length() < 60) {
                            builder.append(' ');
                        }
                        builder.append(legacyStatus.numFE);

                        final String strFE = builder.toString();
                        detailsList.add(strFE);
                        builder.setLength(0);

                    }
                }

                this.currentDetail.add(detailsList);
            }
        } catch (final SQLException ex) {
            final String msg = ex.getMessage();
            setError("Unable to query current courses", msg);
        }
    }

    /**
     * Gathers homework record.
     */
    private void gatherHomework() {

        this.homework.clear();

        final RawStudent stu = getStudent();

        final Cache cache = getCache();
        try {
            final List<RawSthomework> sthwFull = RawSthomeworkLogic.queryByStudent(cache, stu.stuId, true);

            // Sort full list chronologically
            sthwFull.sort(HW_SORT);

            final StringBuilder builder = new StringBuilder(40);

            for (final RawSthomework sthw : sthwFull) {
                final String start = clockTime(sthw.startTime);
                final String finish = clockTime(sthw.finishTime);
                final String elapsed = sthw.startTime == null || sthw.finishTime == null ? CoreConstants.EMPTY :
                        Integer.toString(sthw.finishTime.intValue() - sthw.startTime.intValue());


                builder.append(TemporalUtils.FMT_MDY_COMPACT_FIXED.format(sthw.hwDt));
                builder.append("  ");
                builder.append(sthw.course);
                while (builder.length() < 21) {
                    builder.append(' ');
                }
                builder.append(sthw.unit);
                builder.append("     ");
                builder.append(sthw.objective);
                builder.append("    ");
                builder.append(sthw.hwScore);
                builder.append("      ");
                builder.append(sthw.passed);
                builder.append("      ");
                builder.append(start);
                while (builder.length() < 53) {
                    builder.append(' ');
                }
                builder.append(finish);
                while (builder.length() < 62) {
                    builder.append(' ');
                }
                builder.append(elapsed);
                builder.append("    ");

                final String regString = builder.toString();
                this.homework.add(regString);

                builder.setLength(0);
            }

            this.firstHomeworkIndex = 0;
            this.homeworkCursorLine = 0;
        } catch (final SQLException ex) {
            final String msg = ex.getMessage();
            setError("Unable to query homework", msg);
        }
    }

    /**
     * Generates a clock time value (like "8:15" from an integer time (like 495).
     *
     * @param time the integer time
     * @return the clock time (an empty string if {@code time} is null)
     */
    private static String clockTime(final Integer time) {

        final String result;

        if (time == null) {
            result = CoreConstants.EMPTY;
        } else {
            final int value = time.intValue();
            final int hour = value / 60;
            final int min = value % 60;

            if (min < 10) {
                result = hour + ":0" + min;
            } else {
                result = hour + ":" + min;
            }
        }

        return result;
    }

    /**
     * Draws the history display.
     */
    private void drawHistory() {

        final Console console = getConsole();

        final RawStudent stu = getStudent();

        final Cache cache = getCache();
        try {
            final TermRec active = cache.getSystemData().getActiveTerm();

            if (this.history.isEmpty()) {
                final int intYear = active.term.year.intValue();
                final TermKey oTerm = new TermKey(active.term.name, intYear - 8);
                final String msg = SimpleBuilder.concat(stu.firstName, " has not taken any Precalculus courses since ",
                        oTerm.longString);
                console.print(msg, 13, 10);
                final int msgLen = msg.length();
                console.reverse(13, 10, msgLen);

                setError("Press any key to continue...");
                this.clearPressAnyKey = true;
            } else {
                final int numLines = console.getNumLines();
                final int visibleLines = numLines - 18;
                final int totalRows = this.history.size();

                drawBox(6, 6, 57, numLines - 10);
                console.print("PRECALCULUS COURSE REGISTRATION HISTORY", 15, 8);
                console.print("Course   Section   Term Year   Grade    Format", 9, 10);
                if (totalRows > visibleLines) {
                    console.print("Use the arrow keys to view more rows...", 8, numLines - 6);
                }

                int row = 11;
                final int lastRow = 10 + visibleLines;

                if (this.firstHistoryIndex > 0) {
                    console.print("\u25B2", 59, 10);
                }

                final boolean willFit = totalRows - this.firstHistoryIndex <= visibleLines;

                if (willFit) {
                    for (int i = this.firstHistoryIndex; i < totalRows; ++i) {
                        final String line = this.history.get(i);
                        console.print(line, 9, row);
                        ++row;
                    }
                } else {
                    for (int i = this.firstHistoryIndex; i < totalRows; ++i) {
                        final String line = this.history.get(i);
                        console.print(line, 9, row);
                        ++row;
                        if (row > lastRow) {
                            break;
                        }
                    }
                    console.print("\u25BC", 59, 11 + visibleLines);
                }

                console.setCursor(9, 11 + this.historyCursorLine - this.firstHistoryIndex);

                setError("F5 (or ctrl-e) = Exit");
            }
        } catch (final SQLException ex) {
            final String msg = ex.getMessage();
            setError("Unable to query course history", msg);
        }
    }

    /**
     * Draws the list of current courses.
     */
    private void drawCurrent() {

        final Console console = getConsole();
        final Cache cache = getCache();

        try {
            final TermRec active = cache.getSystemData().getActiveTerm();

            if (this.current.isEmpty()) {
                drawBox(6, 6, 57, 6);
                final String msg = SimpleBuilder.concat("This student is not registered for any");
                console.print(msg, 13, 8);
                final String msg2 = SimpleBuilder.concat("Precalculus courses in ", active.term.longString, ".");
                console.print(msg2, 13, 9);
                setError("Press any key to continue...");
                this.clearPressAnyKey = true;
                console.setCursor(-1, -1);
            } else {
                final int boxHeight = this.current.size() + 7;
                drawBox(6, 6, 77, boxHeight);
                final String header = SimpleBuilder.concat(active.term.longString, " Precalculus Courses");
                console.print(header, 20, 8);
                console.print("Course   Section   Format      Open   Inc?   Counted?   Deadline", 9, 10);

                int row = 11;
                for (final String str : this.current) {
                    console.print(str, 9, row);
                    ++row;
                }

                console.setCursor(9, 11 + this.currentCourseIndex);

                drawBox(6, 6 + boxHeight, 77, 18);

                final List<String> detailList = this.currentDetail.get(this.currentCourseIndex);
                row = 8 + boxHeight;
                for (final String str : detailList) {
                    console.print(str, 9, row);
                    ++row;
                }

                setError("F5 (or ctrl-e) = Exit");
            }
        } catch (final SQLException ex) {
            final String msg = ex.getMessage();
            setError("Unable to query course history", msg);
        }
    }

    /**
     * Draws the homework display.
     */
    private void drawHomework() {

        final Console console = getConsole();

        final RawStudent stu = getStudent();

        if (this.homework.isEmpty()) {
            final String msg = SimpleBuilder.concat(stu.firstName, " has not submitted any homework.");
            console.print(msg, 13, 10);
            final int msgLen = msg.length();
            console.reverse(13, 10, msgLen);

            setError("Press any key to continue...");
            this.clearPressAnyKey = true;
        } else {
            final int numLines = console.getNumLines();
            final int visibleLines = numLines - 18;
            final int totalRows = this.homework.size();

            drawBox(6, 6, 74, numLines - 10);
            console.print("RECORD OF ONLINE HOMEWORKS COMPLETED", 22, 8);
            console.print("   Date     Course  Unit  Obj  Score  Passed  Start  Finish  Spent", 9, 10);
            if (totalRows > visibleLines) {
                console.print("Use the arrow keys to view more rows...", 8, numLines - 6);
            }

            int row = 11;
            final int lastRow = 10 + visibleLines;

            if (this.firstHomeworkIndex > 0) {
                console.print("\u25B2", 76, 10);
            }

            final boolean willFit = totalRows - this.firstHomeworkIndex <= visibleLines;

            if (willFit) {
                for (int i = this.firstHomeworkIndex; i < totalRows; ++i) {
                    final String line = this.homework.get(i);
                    console.print(line, 9, row);
                    ++row;
                }
            } else {
                for (int i = this.firstHomeworkIndex; i < totalRows; ++i) {
                    final String line = this.homework.get(i);
                    console.print(line, 9, row);
                    ++row;
                    if (row > lastRow) {
                        break;
                    }
                }
                console.print("\u25BC", 76, 11 + visibleLines);
            }

            console.setCursor(9, 11 + this.homeworkCursorLine - this.firstHomeworkIndex);

            setError("F5 (or ctrl-e) = Exit");
        }
    }
    /**
     * Processes a key pressed.
     *
     * @param key       the key code
     * @param modifiers key modifiers
     * @return true if the screen should be repainted after this event
     */
    public boolean processKeyPressed(final int key, final int modifiers) {

        boolean repaint = false;

        if (isLocked()) {
            processKeyPressInLocked(key);
            repaint = true;
        } else if (isAcceptingPick()) {
            if (processKeyPressInAcceptingPick(key)) {
                reset();
            }
            repaint = true;
        } else if (isPicking()) {
            processKeyPressInPick(key, modifiers);
            repaint = true;
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_KP_RIGHT) {
            incrementSelection();
            repaint = true;
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_KP_LEFT) {
            decrementSelection();
            repaint = true;
        } else if (key == KeyEvent.VK_ENTER) {
            final int sel = getSelection();

            if (sel == 0) {
                doHistory();
            } else if (sel == 1) {
                doCurrent();
            } else if (sel == 2) {
                doHomework();
            } else if (sel == 3) {
                doPick();
            } else if (sel == 4) {
                doLock();
            } else if (sel == 5) {
                doQuit();
            }
            repaint = true;
        } else if (key == KeyEvent.VK_F5) {
            if (this.display != ScreenCourseDisplay.NONE) {
                reset();
                repaint = true;
            }
        } else if (key == KeyEvent.VK_UP || key == KeyEvent.VK_KP_UP) {
            final int sel = getSelection();

            if (sel == 0) {
                if (this.historyCursorLine > 0) {
                    --this.historyCursorLine;
                    if (this.historyCursorLine < this.firstHistoryIndex) {
                        this.firstHistoryIndex = this.historyCursorLine;
                    }
                    repaint = true;
                }
            } else if (sel == 1) {
                if (this.currentCourseIndex > 0) {
                    --this.currentCourseIndex;
                    repaint = true;
                }
            } else if (sel == 2) {
                if (this.homeworkCursorLine > 0) {
                    --this.homeworkCursorLine;
                    if (this.homeworkCursorLine < this.firstHomeworkIndex) {
                        this.firstHomeworkIndex = this.homeworkCursorLine;
                    }
                    repaint = true;
                }
            }
        } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_KP_DOWN) {
            final int sel = getSelection();

            if (sel == 0) {
                final int numHistory = this.history.size();

                if (this.historyCursorLine < (numHistory - 1)) {
                    final int numLines = getConsole().getNumLines();
                    final int visibleLines = numLines - 18;

                    ++this.historyCursorLine;
                    if (this.historyCursorLine - visibleLines + 1 > this.firstHistoryIndex) {
                        this.firstHistoryIndex = this.historyCursorLine - visibleLines + 1;
                    }
                    repaint = true;
                }
            } else if (sel == 1) {
                final int numCurremt = this.current.size();

                if (this.currentCourseIndex < (numCurremt - 1)) {
                    ++this.currentCourseIndex;
                    repaint = true;
                }
            } else if (sel == 2) {
                final int numHomework = this.homework.size();

                if (this.homeworkCursorLine < (numHomework - 1)) {
                    final int numLines = getConsole().getNumLines();
                    final int visibleLines = numLines - 18;

                    ++this.homeworkCursorLine;
                    if (this.homeworkCursorLine - visibleLines + 1 > this.firstHomeworkIndex) {
                        this.firstHomeworkIndex = this.homeworkCursorLine - visibleLines + 1;
                    }
                    repaint = true;
                }
            }
        }

        return repaint;
    }

    /**
     * Processes a key typed.
     *
     * @param character the character
     * @return true if the screen should be repainted after this event
     */
    public boolean processKeyTyped(final char character) {

        boolean repaint = true;

        if (isLocked()) {
            processKeyTypedInLocked(character);
        } else if (isPicking()) {
            processKeyTypedInPick(character);
        } else if (this.clearPressAnyKey) {
            reset();
            this.clearPressAnyKey = false;
        } else if ((int) character == (int) HISTORY_CHAR) {
            setSelection(0);
            doHistory();
        } else if ((int) character == (int) CURRENT_CHAR) {
            setSelection(1);
            doCurrent();
        } else if ((int) character == (int) HOMEWORK_CHAR) {
            setSelection(2);
            doHomework();
        } else if ((int) character == (int) PICK_CHAR) {
            setSelection(3);
            doPick();
        } else if ((int) character == (int) LOCK_CHAR) {
            setSelection(4);
            doLock();
        } else if ((int) character == (int) QUIT_CHAR) {
            setSelection(5);
            doQuit();
        } else {
            repaint = false;
        }

        return repaint;
    }

    /**
     * Handles the selection of the "History" item.
     */
    private void reset() {

        this.display = ScreenCourseDisplay.NONE;
        setSelection(0);
        clearErrors();

        getConsole().setCursor(-1, -1);
    }

    /**
     * Handles the selection of the "History" item.
     */
    private void doHistory() {

        if (getStudent() == null) {
            doPick();
        } else {
            gatherHistory();
            this.display = ScreenCourseDisplay.HISTORY;
        }
    }

    /**
     * Handles the selection of the "Current" item.
     */
    private void doCurrent() {

        if (getStudent() == null) {
            doPick();
        } else {
            gatherCurrent();
            this.display = ScreenCourseDisplay.CURRENT;
        }
    }

    /**
     * Handles the selection of the "Homework" item.
     */
    private void doHomework() {

        if (getStudent() == null) {
            doPick();
        } else {
            gatherHomework();
            this.display = ScreenCourseDisplay.HOMEWORK;
        }
    }

    /**
     * Handles the selection of the "Quit" item.
     */
    private void doQuit() {

        getConsole().setCursor(-1, -1);
        getMainWindow().goToMain();
    }

    /**
     * Possible displays on this screen.
     */
    private enum ScreenCourseDisplay {

        NONE,
        HISTORY,
        CURRENT,
        HOMEWORK,
    }

    /**
     * A comparator that sorts {@code RawStcourse} records on course ID then term.
     */
    private static class StCourseSort implements Comparator<RawStcourse> {

        /**
         * Constructs a new {@code StCourseSort}.
         */
        private StCourseSort() {

            // No action
        }

        /**
         * Compares two records for order.
         *
         * @param o1 the first object to be compared
         * @param o2 the second object to be compared
         * @return a negative integer, zero, or a positive integer as the first object is less than, equal to, or
         *         greater than the second object
         */
        @Override
        public final int compare(final RawStcourse o1, final RawStcourse o2) {

            int result = -RecBase.compareAllowingNull(o1.termKey, o2.termKey);

            if (result == 0) {
                result = RecBase.compareAllowingNull(o1.course, o2.course);
            }

            return result;
        }
    }

    /**
     * A comparator that sorts {@code RawSthomework} records chronologically.
     */
    private static class SthwSort implements Comparator<RawSthomework> {

        /**
         * Constructs a new {@code SthwSort}.
         */
        private SthwSort() {

            // No action
        }

        /**
         * Compares two records for order.
         *
         * @param o1 the first object to be compared
         * @param o2 the second object to be compared
         * @return a negative integer, zero, or a positive integer as the first object is less than, equal to, or
         *         greater than the second object
         */
        @Override
        public final int compare(final RawSthomework o1, final RawSthomework o2) {

            int result = RecBase.compareAllowingNull(o1.hwDt, o2.hwDt);

            if (result == 0) {
                result = RecBase.compareAllowingNull(o1.finishTime, o2.finishTime);
            }

            return result;
        }
    }
}

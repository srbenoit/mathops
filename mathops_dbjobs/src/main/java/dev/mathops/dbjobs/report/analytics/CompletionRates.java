package dev.mathops.dbjobs.report.analytics;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.dbjobs.file.DbjobsFileLoader;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

/**
 * This class loads a "stc" export from a recent term, looks at courses in which the student was enrolled as of census
 * date, and then computes "trajectories" and success rates based on mix of courses.
 */
final class CompletionRates implements Runnable {

    /** Map from student ID to map from course ID to registration for that student. */
    private final Map<String, Map<String, RawStcourse>> loaded;

    /** A formatter for percentages. */
    private final DecimalFormat pctFmt;

    /** A formatter for expectation values. */
    private final DecimalFormat expFmt;

    /**
     * Constructs a new {@code CompletionRates}.
     */
    private CompletionRates() {

        this.loaded = new HashMap<>(4000);
        this.pctFmt = new DecimalFormat("#.0%");
        this.expFmt = new DecimalFormat("#.00");
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    @Override
    public void run() {

        final File dir = new File("/Users/benoit.MATH4/OneDrive - Colostate/Desktop/SSI/");

        // Locate the STC file to load
        final JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(dir);
        chooser.setDialogTitle("Select 'stcourse' export file");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            if (loadStcFile(chooser.getSelectedFile())) {

                scanPace1();
                scanPace2();
                scanPace3();
                scanPace4();
                scanPace5();

            } else {
                JOptionPane.showMessageDialog(null, "Unable to load 'stcourse' export file");
            }
        }
    }

    /**
     * Attempts to load the "stcourse" extract file.
     *
     * @param f the file
     * @return true if successful; false if not
     */
    private boolean loadStcFile(final File f) {

        boolean ok = true;

        final String[] lines = DbjobsFileLoader.loadFileAsLines(f, true);
        if (lines == null) {
            ok = false;
        } else {
            // Scan once to determine most recent term with data
            int maxYear = 0;
            ETermName maxTerm = null;
            Log.info("File contains " + lines.length + " lines");
            for (final String line : lines) {
                try {
                    if (!line.isEmpty()) {
                        final String[] fields = line.split("\\|");
                        if (fields.length > 4) {
                            final int shortYear = Integer.parseInt(fields[4]);
                            final ETermName term = ETermName.forName(fields[3]);

                            if (term == null) {
                                Log.warning("Can't map '", fields[3], "' to term");
                            }

                            if (shortYear > 80) {
                                continue;
                            }

                            if (shortYear == maxYear) {
                                if ((maxTerm == ETermName.SPRING)
                                        || (maxTerm == ETermName.SUMMER && term == ETermName.FALL)) {
                                    maxTerm = term;
                                }
                            } else if (shortYear > maxYear) {
                                maxYear = shortYear;
                                maxTerm = term;
                            }
                        }
                    }
                } catch (final IllegalArgumentException ex) {
                    Log.warning("Failed to parse term or term year from: ", line, ex);
                    ok = false;
                    break;
                }
            }

            if (ok && maxYear > 0 && maxTerm != null) {
                Log.info("Detected Term is ", maxTerm, Integer.toString(maxYear));

                LocalDate census = null;
                if (maxTerm == ETermName.FALL) {
                    if (maxYear == 21) {
                        census = LocalDate.of(2021, Month.SEPTEMBER, 8);
                    } else if (maxYear == 22) {
                        census = LocalDate.of(2022, Month.SEPTEMBER, 7);
                    }
                } else if (maxTerm == ETermName.SPRING) {
                    if (maxYear == 22) {
                        census = LocalDate.of(2022, Month.FEBRUARY, 2);
                    } else if (maxYear == 23) {
                        census = LocalDate.of(2023, Month.FEBRUARY, 1);
                    }
                }

                if (census == null) {
                    Log.warning("Unable to determine census date");
                }

                // Now load all rows that match the specified term
                int totalCourses = 0;
                for (final String line : lines) {
                    try {
                        if (!line.isEmpty()) {
                            final String[] fields = line.split("\\|");

                            if ("550".equals(fields[2]) || fields[10].isEmpty() || "I".equals(fields[10])) {
                                continue;
                            }

                            final int shortYear = Integer.parseInt(fields[4]);
                            final ETermName term = ETermName.forName(fields[3]);

                            if (fields.length > 25 && shortYear == maxYear && maxTerm == term) {

                                final RawStcourse row = new RawStcourse();

                                row.termKey = new TermKey(maxTerm, 2000 + shortYear);
                                row.stuId = fields[0];
                                row.course = fields[1];
                                row.sect = fields[2];
                                row.paceOrder = fields[5].isEmpty() ? null : Integer.valueOf(fields[5]);
                                row.openStatus = fields[6];
                                row.gradingOption = fields[7];
                                row.completed = fields[8];
                                row.score = fields[9].isEmpty() ? null : Integer.valueOf(fields[9]);
                                row.courseGrade = fields[10];
                                row.prereqSatis = fields[11];
                                row.initClassRoll = fields[12];
                                row.stuProvided = fields[13];
                                row.finalClassRoll = fields[14];
                                row.examPlaced = fields[15];

                                final String rollDate = fields[26];
                                if (rollDate.length() >= 10) {
                                    try {
                                        final int mm = Integer.parseInt(rollDate.substring(0, 2));
                                        final int dd = Integer.parseInt(rollDate.substring(3, 5));
                                        final int yy = Integer.parseInt(rollDate.substring(6));
                                        row.lastClassRollDt = LocalDate.of(2000 + yy, mm, dd);
                                    } catch (final IllegalArgumentException ex) {
                                        Log.warning("Failed to parse roll date from: ", line, ex);
                                        ok = false;
                                        break;
                                    }
                                }

                                if (row.lastClassRollDt.isBefore(census)) {
                                    // A "drop", ignore
                                    continue;
                                }

                                final Map<String, RawStcourse> map = this.loaded.computeIfAbsent(row.stuId,
                                        s -> new HashMap<>(5));

                                final RawStcourse exist = map.get(row.course);
                                if (exist == null) {
                                    map.put(row.course, row);
                                    ++totalCourses;
                                } else // Multiple rows for a course!
                                    if (row.lastClassRollDt.isAfter(exist.lastClassRollDt)
                                            || (row.lastClassRollDt.equals(exist.lastClassRollDt)
                                            && "W".equals(exist.courseGrade))) {
                                        // Replace with more recent
                                        map.put(row.course, row);
                                    }
                            }
                        }
                    } catch (final IllegalArgumentException ex) {
                        Log.warning("Failed to parse term or term year from: ", line, ex);
                        ok = false;
                        break;
                    }
                }

                Log.info("Loaded " + totalCourses + " registrations for " + this.loaded.size() + " students");
            }
        }

        return ok;
    }

    /**
     * Tests whether a course was passed.
     *
     * @param reg the registration record
     * @return true if passed; false if not
     */
    private static boolean isPassed(final RawStcourse reg) {

        final boolean passed;

        final String grade = reg.courseGrade;

        if ("A".equals(grade) || "B".equals(grade) || "C".equals(grade) || "S".equals(grade)) {
            passed = true;
        } else {
            if ("D".equals(grade) || "F".equals(grade) || "U".equals(grade) || "W".equals(grade)) {
                // TODO:
            } else {
                Log.warning("Grade of " + grade + " for ", reg.stuId, " in sect ", reg.sect,
                        " with completed ", reg.completed);
            }
            passed = false;
        }

        return passed;
    }

    /**
     * Scans for pace 1 students.
     */
    private void scanPace1() {

        int m117Pass = 0;
        int m117Fail = 0;
        int m118Pass = 0;
        int m118Fail = 0;
        int m124Pass = 0;
        int m124Fail = 0;
        int m125Pass = 0;
        int m125Fail = 0;
        int m126Pass = 0;
        int m126Fail = 0;

        for (final Map<String, RawStcourse> map : this.loaded.values()) {
            if (map.size() == 1) {
                for (final Map.Entry<String, RawStcourse> entry : map.entrySet()) {
                    final RawStcourse reg = entry.getValue();

                    if (RawRecordConstants.M117.equals(entry.getKey())) {
                        if (isPassed(reg)) {
                            m117Pass++;
                        } else {
                            m117Fail++;
                        }
                    } else if (RawRecordConstants.M118.equals(entry.getKey())) {
                        if (isPassed(reg)) {
                            m118Pass++;
                        } else {
                            m118Fail++;
                        }
                    } else if (RawRecordConstants.M124.equals(entry.getKey())) {
                        if (isPassed(reg)) {
                            m124Pass++;
                        } else {
                            m124Fail++;
                        }
                    } else if (RawRecordConstants.M125.equals(entry.getKey())) {
                        if (isPassed(reg)) {
                            m125Pass++;
                        } else {
                            m125Fail++;
                        }
                    } else if (RawRecordConstants.M126.equals(entry.getKey())) {
                        if (isPassed(reg)) {
                            m126Pass++;
                        } else {
                            m126Fail++;
                        }
                    }
                }
            }
        }

        final double m117Success = (double) m117Pass / (double) (m117Pass + m117Fail);
        final double m118Success = (double) m118Pass / (double) (m118Pass + m118Fail);
        final double m124Success = (double) m124Pass / (double) (m124Pass + m124Fail);
        final double m125Success = (double) m125Pass / (double) (m125Pass + m125Fail);
        final double m126Success = (double) m126Pass / (double) (m126Pass + m126Fail);

        Log.fine("Students in 1-course pace:");
        Log.fine("  MATH 117: ", this.pctFmt.format(m117Success), " success");
        Log.fine("  MATH 118: ", this.pctFmt.format(m118Success), " success");
        Log.fine("  MATH 124: ", this.pctFmt.format(m124Success), " success");
        Log.fine("  MATH 125: ", this.pctFmt.format(m125Success), " success");
        Log.fine("  MATH 126: ", this.pctFmt.format(m126Success), " success");
    }

    /**
     * Scans for pace 2 students.
     */
    private void scanPace2() {

        final Pace2Results m117118 = new Pace2Results();
        final Pace2Results m117124 = new Pace2Results();
        final Pace2Results m117125 = new Pace2Results();
        final Pace2Results m117126 = new Pace2Results();

        final Pace2Results m118124 = new Pace2Results();
        final Pace2Results m118125 = new Pace2Results();
        final Pace2Results m118126 = new Pace2Results();

        final Pace2Results m124125 = new Pace2Results();
        final Pace2Results m124126 = new Pace2Results();

        final Pace2Results m125126 = new Pace2Results();

        for (final Map<String, RawStcourse> map : this.loaded.values()) {
            if (map.size() == 2) {
                final RawStcourse m117 = map.get(RawRecordConstants.M117);
                final RawStcourse m118 = map.get(RawRecordConstants.M118);
                final RawStcourse m124 = map.get(RawRecordConstants.M124);
                final RawStcourse m125 = map.get(RawRecordConstants.M125);
                final RawStcourse m126 = map.get(RawRecordConstants.M126);

                if (m117 == null) {
                    if (m118 == null) {
                        if (m124 == null) {
                            m125126.accumulate(m125, m126);
                        } else if (m125 == null) {
                            m124126.accumulate(m124, m126);
                        } else {
                            m124125.accumulate(m124, m125);
                        }
                    } else if (m124 == null) {
                        if (m125 == null) {
                            m118126.accumulate(m118, m126);
                        } else {
                            m118125.accumulate(m118, m125);
                        }
                    } else {
                        m118124.accumulate(m118, m124);
                    }
                } else if (m118 == null) {
                    if (m124 == null) {
                        if (m125 == null) {
                            m117126.accumulate(m117, m126);
                        } else {
                            m117125.accumulate(m117, m125);
                        }
                    } else {
                        m117124.accumulate(m117, m124);
                    }
                } else {
                    m117118.accumulate(m117, m118);
                }
            }
        }

        Log.fine(CoreConstants.EMPTY);
        Log.fine("Students in 2-course pace:");

        if (m117118.getN() > 0) {
            Log.fine("  MATH 117+118 (n=" + m117118.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m117118.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m117118.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m117118.getSecondCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m117118.getExpectedCreditsPassed()));
        }

        if (m117124.getN() > 0) {
            Log.fine("  MATH 117+124 (n=" + m117124.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m117124.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m117124.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m117124.getSecondCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m117124.getExpectedCreditsPassed()));
        }

        if (m117125.getN() > 0) {
            Log.fine("  MATH 117+125 (n=" + m117125.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m117125.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m117125.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m117125.getSecondCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m117125.getExpectedCreditsPassed()));
        }

        if (m117126.getN() > 0) {
            Log.fine("  MATH 117+126 (n=" + m117126.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m117126.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m117126.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m117126.getSecondCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m117126.getExpectedCreditsPassed()));
        }

        if (m118124.getN() > 0) {
            Log.fine("  MATH 118+124 (n=" + m118124.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m118124.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m118124.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m118124.getSecondCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m118124.getExpectedCreditsPassed()));
        }

        if (m118125.getN() > 0) {
            Log.fine("  MATH 118+125 (n=" + m118125.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m118125.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m118125.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m118125.getSecondCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m118125.getExpectedCreditsPassed()));
        }

        if (m118126.getN() > 0) {
            Log.fine("  MATH 118+126 (n=" + m118126.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m118126.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m118126.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m118126.getSecondCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m118126.getExpectedCreditsPassed()));
        }

        if (m124125.getN() > 0) {
            Log.fine("  MATH 124+125 (n=" + m124125.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m124125.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m124125.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m124125.getSecondCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m124125.getExpectedCreditsPassed()));
        }

        if (m124126.getN() > 0) {
            Log.fine("  MATH 124+126 (n=" + m124126.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m124126.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m124126.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m124126.getSecondCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m124126.getExpectedCreditsPassed()));
        }

        if (m125126.getN() > 0) {
            Log.fine("  MATH 125+126 (n=" + m125126.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m125126.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m125126.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m125126.getSecondCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m125126.getExpectedCreditsPassed()));
        }
    }

    /**
     * Scans for pace 3 students.
     */
    private void scanPace3() {

        final Pace3Results m117118124 = new Pace3Results();
        final Pace3Results m117118125 = new Pace3Results();
        final Pace3Results m117118126 = new Pace3Results();

        final Pace3Results m117124125 = new Pace3Results();
        final Pace3Results m117124126 = new Pace3Results();

        final Pace3Results m117125126 = new Pace3Results();

        final Pace3Results m118124125 = new Pace3Results();
        final Pace3Results m118124126 = new Pace3Results();

        final Pace3Results m118125126 = new Pace3Results();

        final Pace3Results m124125126 = new Pace3Results();

        for (final Map<String, RawStcourse> map : this.loaded.values()) {
            if (map.size() == 3) {
                final RawStcourse m117 = map.get(RawRecordConstants.M117);
                final RawStcourse m118 = map.get(RawRecordConstants.M118);
                final RawStcourse m124 = map.get(RawRecordConstants.M124);
                final RawStcourse m125 = map.get(RawRecordConstants.M125);
                final RawStcourse m126 = map.get(RawRecordConstants.M126);

                if (m117 == null) {
                    if (m118 == null) {
                        m124125126.accumulate(m124, m125, m126);
                    } else if (m124 == null) {
                        m118125126.accumulate(m118, m125, m126);
                    } else if (m125 == null) {
                        m118124126.accumulate(m118, m124, m126);
                    } else {
                        m118124125.accumulate(m118, m124, m125);
                    }
                } else if (m118 == null) {
                    // 117 is in the mix, 118 is not
                    if (m124 == null) {
                        m117125126.accumulate(m117, m125, m126);
                    } else if (m125 == null) {
                        m117124126.accumulate(m117, m124, m126);
                    } else {
                        m117124125.accumulate(m117, m124, m125);
                    }
                } else // Both 117 and 118 are on the mix
                    if (m124 != null) {
                        m117118124.accumulate(m117, m118, m124);
                    } else if (m125 != null) {
                        m117118125.accumulate(m117, m118, m125);
                    } else {
                        m117118126.accumulate(m117, m118, m126);
                    }
            }
        }

        Log.fine(CoreConstants.EMPTY);
        Log.fine("Students in 3-course pace:");

        if (m117118124.getN() > 0) {
            Log.fine("  MATH 117+118+124 (n=" + m117118124.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m117118124.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m117118124.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m117118124.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) m117118124.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) m117118124.getThirdCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m117118124.getExpectedCreditsPassed()));
        }

        if (m117118125.getN() > 0) {
            Log.fine("  MATH 117+118+125 (n=" + m117118125.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m117118125.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m117118125.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m117118125.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) m117118125.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) m117118125.getThirdCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m117118125.getExpectedCreditsPassed()));
        }

        if (m117118126.getN() > 0) {
            Log.fine("  MATH 117+118+126 (n=" + m117118126.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m117118126.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m117118126.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m117118126.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) m117118126.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) m117118126.getThirdCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m117118126.getExpectedCreditsPassed()));
        }

        if (m117124125.getN() > 0) {
            Log.fine("  MATH 117+124+125 (n=" + m117124125.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m117124125.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m117124125.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m117124125.getSecondCourseSuccessIfTried()));
            Log.fine("   Success in course 3: ",
                    this.pctFmt.format((double) m117124125.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) m117124125.getThirdCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m117124125.getExpectedCreditsPassed()));
        }

        if (m117124126.getN() > 0) {
            Log.fine("  MATH 117+124+126 (n=" + m117124126.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m117124126.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m117124126.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m117124126.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) m117124126.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) m117124126.getThirdCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m117124126.getExpectedCreditsPassed()));
        }

        if (m117125126.getN() > 0) {
            Log.fine("  MATH 117+125+125 (n=" + m117125126.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m117125126.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m117125126.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m117125126.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) m117125126.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) m117125126.getThirdCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m117125126.getExpectedCreditsPassed()));
        }

        if (m118124125.getN() > 0) {
            Log.fine("  MATH 118+124+125 (n=" + m118124125.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m118124125.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m118124125.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m118124125.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) m118124125.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) m118124125.getThirdCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m118124125.getExpectedCreditsPassed()));
        }

        if (m118124126.getN() > 0) {
            Log.fine("  MATH 118+124+126 (n=" + m118124126.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m118124126.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m118124126.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m118124126.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) m118124126.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) m118124126.getThirdCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m118124126.getExpectedCreditsPassed()));
        }

        if (m118125126.getN() > 0) {
            Log.fine("  MATH 118+125+126 (n=" + m118125126.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m118125126.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m118125126.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m118125126.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) m118125126.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) m118125126.getThirdCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m118125126.getExpectedCreditsPassed()));
        }

        if (m124125126.getN() > 0) {
            Log.fine("  MATH 124+125+126 (n=" + m124125126.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) m124125126.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) m124125126.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) m124125126.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) m124125126.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) m124125126.getThirdCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) m124125126.getExpectedCreditsPassed()));
        }
    }

    /**
     * Scans for pace 4 students.
     */
    private void scanPace4() {

        final Pace4Results allBut117 = new Pace4Results();
        final Pace4Results allBut118 = new Pace4Results();
        final Pace4Results allBut124 = new Pace4Results();
        final Pace4Results allBut125 = new Pace4Results();
        final Pace4Results allBut126 = new Pace4Results();

        for (final Map<String, RawStcourse> map : this.loaded.values()) {
            if (map.size() == 4) {
                final RawStcourse m117 = map.get(RawRecordConstants.M117);
                final RawStcourse m118 = map.get(RawRecordConstants.M118);
                final RawStcourse m124 = map.get(RawRecordConstants.M124);
                final RawStcourse m125 = map.get(RawRecordConstants.M125);
                final RawStcourse m126 = map.get(RawRecordConstants.M126);

                if (m117 == null) {
                    allBut117.accumulate(m118, m124, m125, m126);
                } else if (m118 == null) {
                    allBut118.accumulate(m117, m124, m125, m126);
                } else if (m124 == null) {
                    allBut124.accumulate(m117, m118, m125, m126);
                } else if (m125 == null) {
                    allBut125.accumulate(m117, m118, m124, m126);
                } else {
                    allBut126.accumulate(m117, m118, m124, m125);
                }
            }
        }

        Log.fine(CoreConstants.EMPTY);
        Log.fine("Students in 4-course pace:");

        if (allBut126.getN() > 0) {
            Log.fine("  MATH 117+118+124+125 (n=" + allBut126.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) allBut126.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) allBut126.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) allBut126.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) allBut126.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) allBut126.getThirdCourseSuccessIfTried()));
            Log.fine("    Success in course 4: ",
                    this.pctFmt.format((double) allBut126.getFourthCourseSuccess()));
            Log.fine("    Success in course 4 (if started): ",
                    this.pctFmt.format((double) allBut126.getFourthCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) allBut126.getExpectedCreditsPassed()));
        }

        if (allBut125.getN() > 0) {
            Log.fine("  MATH 117+118+124+126 (n=" + allBut125.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) allBut125.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) allBut125.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) allBut125.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) allBut125.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) allBut125.getThirdCourseSuccessIfTried()));
            Log.fine("    Success in course 4: ",
                    this.pctFmt.format((double) allBut125.getFourthCourseSuccess()));
            Log.fine("    Success in course 4 (if started): ",
                    this.pctFmt.format((double) allBut125.getFourthCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) allBut125.getExpectedCreditsPassed()));
        }

        if (allBut124.getN() > 0) {
            Log.fine("  MATH 117+118+125+126 (n=" + allBut124.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) allBut124.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) allBut124.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) allBut124.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) allBut124.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) allBut124.getThirdCourseSuccessIfTried()));
            Log.fine("    Success in course 4: ",
                    this.pctFmt.format((double) allBut124.getFourthCourseSuccess()));
            Log.fine("    Success in course 4 (if started): ",
                    this.pctFmt.format((double) allBut124.getFourthCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) allBut124.getExpectedCreditsPassed()));
        }

        if (allBut118.getN() > 0) {
            Log.fine("  MATH 117+124+125+126 (n=" + allBut118.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) allBut118.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) allBut118.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) allBut118.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) allBut118.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) allBut118.getThirdCourseSuccessIfTried()));
            Log.fine("    Success in course 4: ",
                    this.pctFmt.format((double) allBut118.getFourthCourseSuccess()));
            Log.fine("    Success in course 4 (if started): ",
                    this.pctFmt.format((double) allBut118.getFourthCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) allBut118.getExpectedCreditsPassed()));
        }

        if (allBut117.getN() > 0) {
            Log.fine("  MATH 118+124+125+126 (n=" + allBut117.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) allBut117.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) allBut117.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) allBut117.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) allBut117.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) allBut117.getThirdCourseSuccessIfTried()));
            Log.fine("    Success in course 4: ",
                    this.pctFmt.format((double) allBut117.getFourthCourseSuccess()));
            Log.fine("    Success in course 4 (if started): ",
                    this.pctFmt.format((double) allBut117.getFourthCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) allBut117.getExpectedCreditsPassed()));
        }
    }

    /**
     * Scans for pace 5 students.
     */
    private void scanPace5() {

        final Pace5Results results = new Pace5Results();

        for (final Map<String, RawStcourse> map : this.loaded.values()) {
            if (map.size() == 5) {
                final RawStcourse m117 = map.get(RawRecordConstants.M117);
                final RawStcourse m118 = map.get(RawRecordConstants.M118);
                final RawStcourse m124 = map.get(RawRecordConstants.M124);
                final RawStcourse m125 = map.get(RawRecordConstants.M125);
                final RawStcourse m126 = map.get(RawRecordConstants.M126);

                results.accumulate(m117, m118, m124, m125, m126);
            }
        }

        Log.fine(CoreConstants.EMPTY);
        Log.fine("Students in 5-course pace:");

        if (results.getN() > 0) {
            Log.fine("  MATH 117+118+124+125+126 (n=" + results.getN() + ")");
            Log.fine("    Success in course 1: ",
                    this.pctFmt.format((double) results.getFirstCourseSuccess()));
            Log.fine("    Success in course 2: ",
                    this.pctFmt.format((double) results.getSecondCourseSuccess()));
            Log.fine("    Success in course 2 (if started): ",
                    this.pctFmt.format((double) results.getSecondCourseSuccessIfTried()));
            Log.fine("    Success in course 3: ",
                    this.pctFmt.format((double) results.getThirdCourseSuccess()));
            Log.fine("    Success in course 3 (if started): ",
                    this.pctFmt.format((double) results.getThirdCourseSuccessIfTried()));
            Log.fine("    Success in course 4: ",
                    this.pctFmt.format((double) results.getFourthCourseSuccess()));
            Log.fine("    Success in course 4 (if started): ",
                    this.pctFmt.format((double) results.getFourthCourseSuccessIfTried()));
            Log.fine("    Success in course 5: ",
                    this.pctFmt.format((double) results.getFifthCourseSuccess()));
            Log.fine("    Success in course 5 (if started): ",
                    this.pctFmt.format((double) results.getFifthCourseSuccessIfTried()));
            Log.fine("    Expectation value for credits passed: ",
                    this.expFmt.format((double) results.getExpectedCreditsPassed()));
        }
    }

    /**
     * Results in a 2-course pace.
     */
    private static final class Pace2Results {

        /** The number of students who passed the first class. */
        int numPassedFirstClass;

        /** The number of students who failed the first class. */
        int numFailedFirstClass;

        /** The number of students who passed the second class. */
        int numPassedSecondClass;

        /** The number of students who tried and failed the second class. */
        int numTriedAndFailedSecondClass;

        /** The number of students who never tried the second class (hence failed). */
        int numNeverTriedSecondClass;

        /**
         * Constructs a new {@code Pace2Results}.
         */
        private Pace2Results() {

            // No action
        }

        /**
         * Accumulates results from a course.
         *
         * @param first  the first class
         * @param second the second class
         */
        void accumulate(final RawStcourse first, final RawStcourse second) {

            if (first == null || second == null) {
                throw new IllegalArgumentException("Arguments may not be null");
            }

            int numPassed = 0;
            if (isPassed(first)) {
                ++numPassed;
            }
            if (isPassed(second)) {
                ++numPassed;
            }

            if (numPassed == 0) {
                ++this.numFailedFirstClass;
                ++this.numNeverTriedSecondClass;
            } else if (numPassed == 1) {
                ++this.numPassedFirstClass;
                ++this.numTriedAndFailedSecondClass;
            } else {
                ++this.numPassedFirstClass;
                ++this.numPassedSecondClass;
            }
        }

        /**
         * Gets the number of students.
         *
         * @return the number of students
         */
        int getN() {

            return this.numPassedFirstClass + this.numFailedFirstClass;
        }

        /**
         * Gets the first course success rate.
         *
         * @return the first course success rate
         */
        float getFirstCourseSuccess() {

            return (float) this.numPassedFirstClass / (float) (this.numPassedFirstClass + this.numFailedFirstClass);
        }

        /**
         * Gets the second course success rate.
         *
         * @return the second course success rate
         */
        float getSecondCourseSuccess() {

            return (float) this.numPassedSecondClass / (float) (this.numPassedSecondClass
                    + this.numTriedAndFailedSecondClass + this.numNeverTriedSecondClass);
        }

        /**
         * Gets the second course success rate if the second course was attempted.
         *
         * @return the second course success rate
         */
        float getSecondCourseSuccessIfTried() {

            return (float) this.numPassedSecondClass
                    / (float) (this.numPassedSecondClass + this.numTriedAndFailedSecondClass);
        }

        /**
         * Calculates the expectation value for number of credits passed.
         *
         * @return the expectation value
         */
        float getExpectedCreditsPassed() {

            final float earned1 = getFirstCourseSuccess() - getSecondCourseSuccess();
            final float earned2 = getSecondCourseSuccess();

            return earned1 + earned2 * 2.0f;
        }
    }

    /**
     * Results in a 3-course pace.
     */
    private static final class Pace3Results {

        /** The number of students who passed the first class. */
        int numPassedFirstClass;

        /** The number of students who failed the first class. */
        int numFailedFirstClass;

        /** The number of students who passed the second class. */
        int numPassedSecondClass;

        /** The number of students who tried and failed the second class. */
        int numTriedAndFailedSecondClass;

        /** The number of students who never tried the second class (hence failed). */
        int numNeverTriedSecondClass;

        /** The number of students who passed the third class. */
        int numPassedThirdClass;

        /** The number of students who tried and failed the third class. */
        int numTriedAndFailedThirdClass;

        /** The number of students who never tried the third class (hence failed). */
        int numNeverTriedThirdClass;

        /**
         * Constructs a new {@code Pace3Results}.
         */
        private Pace3Results() {

            // No action
        }

        /**
         * Accumulates results from a course.
         *
         * @param first  the first class
         * @param second the second class
         * @param third  the third class
         */
        void accumulate(final RawStcourse first, final RawStcourse second, final RawStcourse third) {

            if (first == null || second == null || third == null) {
                throw new IllegalArgumentException("Arguments may not be null");
            }

            int numPassed = 0;
            if (isPassed(first)) {
                ++numPassed;
            }
            if (isPassed(second)) {
                ++numPassed;
            }
            if (isPassed(third)) {
                ++numPassed;
            }

            if (numPassed == 0) {
                ++this.numFailedFirstClass;
                ++this.numNeverTriedSecondClass;
                ++this.numNeverTriedThirdClass;
            } else if (numPassed == 1) {
                ++this.numPassedFirstClass;
                ++this.numTriedAndFailedSecondClass;
                ++this.numNeverTriedThirdClass;
            } else if (numPassed == 2) {
                ++this.numPassedFirstClass;
                ++this.numPassedSecondClass;
                ++this.numTriedAndFailedThirdClass;
            } else {
                ++this.numPassedFirstClass;
                ++this.numPassedSecondClass;
                ++this.numPassedThirdClass;
            }
        }

        /**
         * Gets the number of students.
         *
         * @return the number of students
         */
        int getN() {

            return this.numPassedFirstClass + this.numFailedFirstClass;
        }

        /**
         * Gets the first course success rate.
         *
         * @return the first course success rate
         */
        float getFirstCourseSuccess() {

            return (float) this.numPassedFirstClass
                    / (float) (this.numPassedFirstClass + this.numFailedFirstClass);
        }

        /**
         * Gets the second course success rate.
         *
         * @return the second course success rate
         */
        float getSecondCourseSuccess() {

            return (float) this.numPassedSecondClass / (float) (this.numPassedSecondClass
                    + this.numTriedAndFailedSecondClass + this.numNeverTriedSecondClass);
        }

        /**
         * Gets the second course success rate if the second course was attempted.
         *
         * @return the second course success rate
         */
        float getSecondCourseSuccessIfTried() {

            return (float) this.numPassedSecondClass
                    / (float) (this.numPassedSecondClass + this.numTriedAndFailedSecondClass);
        }

        /**
         * Gets the third course success rate.
         *
         * @return the third course success rate
         */
        float getThirdCourseSuccess() {

            return (float) this.numPassedThirdClass / (float) (this.numPassedThirdClass
                    + this.numTriedAndFailedThirdClass + this.numNeverTriedThirdClass);
        }

        /**
         * Gets the third course success rate if the third course was attempted.
         *
         * @return the third course success rate
         */
        float getThirdCourseSuccessIfTried() {

            return (float) this.numPassedThirdClass
                    / (float) (this.numPassedThirdClass + this.numTriedAndFailedThirdClass);
        }

        /**
         * Calculates the expectation value for number of credits passed.
         *
         * @return the expectation value
         */
        float getExpectedCreditsPassed() {

            final float earned1 = getFirstCourseSuccess() - getSecondCourseSuccess();
            final float earned2 = getSecondCourseSuccess() - getThirdCourseSuccess();
            final float earned3 = getThirdCourseSuccess();

            return earned1 + earned2 * 2.0f + earned3 * 3.0f;
        }
    }

    /**
     * Results in a 4-course pace.
     */
    private static final class Pace4Results {

        /** The number of students who passed the first class. */
        int numPassedFirstClass;

        /** The number of students who failed the first class. */
        int numFailedFirstClass;

        /** The number of students who passed the second class. */
        int numPassedSecondClass;

        /** The number of students who tried and failed the second class. */
        int numTriedAndFailedSecondClass;

        /** The number of students who never tried the second class (hence failed). */
        int numNeverTriedSecondClass;

        /** The number of students who passed the third class. */
        int numPassedThirdClass;

        /** The number of students who tried and failed the third class. */
        int numTriedAndFailedThirdClass;

        /** The number of students who never tried the third class (hence failed). */
        int numNeverTriedThirdClass;

        /** The number of students who passed the fourth class. */
        int numPassedFourthClass;

        /** The number of students who tried and failed the fourth class. */
        int numTriedAndFailedFourthClass;

        /** The number of students who never tried the fourth class (hence failed). */
        int numNeverTriedFourthClass;

        /**
         * Constructs a new {@code Pace4Results}.
         */
        private Pace4Results() {

            // No action
        }

        /**
         * Accumulates results from a course.
         *
         * @param first  the first class
         * @param second the second class
         * @param third  the third class
         * @param fourth the fourth class
         */
        void accumulate(final RawStcourse first, final RawStcourse second,
                        final RawStcourse third, final RawStcourse fourth) {

            if (first == null || second == null || third == null || fourth == null) {
                throw new IllegalArgumentException("Arguments may not be null");
            }

            int numPassed = 0;
            if (isPassed(first)) {
                ++numPassed;
            }
            if (isPassed(second)) {
                ++numPassed;
            }
            if (isPassed(third)) {
                ++numPassed;
            }
            if (isPassed(fourth)) {
                ++numPassed;
            }

            if (numPassed == 0) {
                ++this.numFailedFirstClass;
                ++this.numNeverTriedSecondClass;
                ++this.numNeverTriedThirdClass;
                ++this.numNeverTriedFourthClass;
            } else if (numPassed == 1) {
                ++this.numPassedFirstClass;
                ++this.numTriedAndFailedSecondClass;
                ++this.numNeverTriedThirdClass;
                ++this.numNeverTriedFourthClass;
            } else if (numPassed == 2) {
                ++this.numPassedFirstClass;
                ++this.numPassedSecondClass;
                ++this.numTriedAndFailedThirdClass;
                ++this.numNeverTriedFourthClass;
            } else if (numPassed == 3) {
                ++this.numPassedFirstClass;
                ++this.numPassedSecondClass;
                ++this.numPassedThirdClass;
                ++this.numTriedAndFailedFourthClass;
            } else {
                ++this.numPassedFirstClass;
                ++this.numPassedSecondClass;
                ++this.numPassedThirdClass;
                ++this.numPassedFourthClass;
            }
        }

        /**
         * Gets the number of students.
         *
         * @return the number of students
         */
        int getN() {

            return this.numPassedFirstClass + this.numFailedFirstClass;
        }

        /**
         * Gets the first course success rate.
         *
         * @return the first course success rate
         */
        float getFirstCourseSuccess() {

            return (float) this.numPassedFirstClass
                    / (float) (this.numPassedFirstClass + this.numFailedFirstClass);
        }

        /**
         * Gets the second course success rate.
         *
         * @return the second course success rate
         */
        float getSecondCourseSuccess() {

            return (float) this.numPassedSecondClass / (float) (this.numPassedSecondClass
                    + this.numTriedAndFailedSecondClass + this.numNeverTriedSecondClass);
        }

        /**
         * Gets the second course success rate if the second course was attempted.
         *
         * @return the second course success rate
         */
        float getSecondCourseSuccessIfTried() {

            return (float) this.numPassedSecondClass
                    / (float) (this.numPassedSecondClass + this.numTriedAndFailedSecondClass);
        }

        /**
         * Gets the third course success rate.
         *
         * @return the third course success rate
         */
        float getThirdCourseSuccess() {

            return (float) this.numPassedThirdClass / (float) (this.numPassedThirdClass
                    + this.numTriedAndFailedThirdClass + this.numNeverTriedThirdClass);
        }

        /**
         * Gets the third course success rate if the third course was attempted.
         *
         * @return the third course success rate
         */
        float getThirdCourseSuccessIfTried() {

            return (float) this.numPassedThirdClass
                    / (float) (this.numPassedThirdClass + this.numTriedAndFailedThirdClass);
        }

        /**
         * Gets the fourth course success rate.
         *
         * @return the fourth course success rate
         */
        float getFourthCourseSuccess() {

            return (float) this.numPassedFourthClass / (float) (this.numPassedFourthClass
                    + this.numTriedAndFailedFourthClass + this.numNeverTriedFourthClass);
        }

        /**
         * Gets the fourth course success rate if the fourth course was attempted.
         *
         * @return the fourth course success rate
         */
        float getFourthCourseSuccessIfTried() {

            return (float) this.numPassedFourthClass
                    / (float) (this.numPassedFourthClass + this.numTriedAndFailedFourthClass);
        }

        /**
         * Calculates the expectation value for number of credits passed.
         *
         * @return the expectation value
         */
        float getExpectedCreditsPassed() {

            final float earned1 = getFirstCourseSuccess() - getSecondCourseSuccess();
            final float earned2 = getSecondCourseSuccess() - getThirdCourseSuccess();
            final float earned3 = getThirdCourseSuccess() - getFourthCourseSuccess();
            final float earned4 = getFourthCourseSuccess();

            return earned1 + earned2 * 2.0f + earned3 * 3.0f + earned4 * 4.0f;
        }
    }

    /**
     * Results in a 5-course pace.
     */
    private static final class Pace5Results {

        /** The number of students who passed the first class. */
        int numPassedFirstClass;

        /** The number of students who failed the first class. */
        int numFailedFirstClass;

        /** The number of students who passed the second class. */
        int numPassedSecondClass;

        /** The number of students who tried and failed the second class. */
        int numTriedAndFailedSecondClass;

        /** The number of students who never tried the second class (hence failed). */
        int numNeverTriedSecondClass;

        /** The number of students who passed the third class. */
        int numPassedThirdClass;

        /** The number of students who tried and failed the third class. */
        int numTriedAndFailedThirdClass;

        /** The number of students who never tried the third class (hence failed). */
        int numNeverTriedThirdClass;

        /** The number of students who passed the fourth class. */
        int numPassedFourthClass;

        /** The number of students who tried and failed the fourth class. */
        int numTriedAndFailedFourthClass;

        /** The number of students who never tried the fourth class (hence failed). */
        int numNeverTriedFourthClass;

        /** The number of students who passed the fifth class. */
        int numPassedFifthClass;

        /** The number of students who tried and failed the fifth class. */
        int numTriedAndFailedFifthClass;

        /** The number of students who never tried the fifth class (hence failed). */
        int numNeverTriedFifthClass;

        /**
         * Constructs a new {@code Pace5Results}.
         */
        private Pace5Results() {

            // No action
        }

        /**
         * Accumulates results from a course.
         *
         * @param first  the first class
         * @param second the second class
         * @param third  the third class
         * @param fourth the fourth class
         * @param fifth  the fifth class
         */
        void accumulate(final RawStcourse first, final RawStcourse second,
                        final RawStcourse third, final RawStcourse fourth, final RawStcourse fifth) {

            if (first == null || second == null || third == null || fourth == null || fifth == null) {
                throw new IllegalArgumentException("Arguments may not be null");
            }

            int numPassed = 0;
            if (isPassed(first)) {
                ++numPassed;
            }
            if (isPassed(second)) {
                ++numPassed;
            }
            if (isPassed(third)) {
                ++numPassed;
            }
            if (isPassed(fourth)) {
                ++numPassed;
            }
            if (isPassed(fifth)) {
                ++numPassed;
            }

            if (numPassed == 0) {
                ++this.numFailedFirstClass;
                ++this.numNeverTriedSecondClass;
                ++this.numNeverTriedThirdClass;
                ++this.numNeverTriedFourthClass;
                ++this.numNeverTriedFifthClass;
            } else if (numPassed == 1) {
                ++this.numPassedFirstClass;
                ++this.numTriedAndFailedSecondClass;
                ++this.numNeverTriedThirdClass;
                ++this.numNeverTriedFourthClass;
                ++this.numNeverTriedFifthClass;
            } else if (numPassed == 2) {
                ++this.numPassedFirstClass;
                ++this.numPassedSecondClass;
                ++this.numTriedAndFailedThirdClass;
                ++this.numNeverTriedFourthClass;
                ++this.numNeverTriedFifthClass;
            } else if (numPassed == 3) {
                ++this.numPassedFirstClass;
                ++this.numPassedSecondClass;
                ++this.numPassedThirdClass;
                ++this.numTriedAndFailedFourthClass;
                ++this.numNeverTriedFifthClass;
            } else if (numPassed == 4) {
                ++this.numPassedFirstClass;
                ++this.numPassedSecondClass;
                ++this.numPassedThirdClass;
                ++this.numPassedFourthClass;
                ++this.numTriedAndFailedFifthClass;
            } else {
                ++this.numPassedFirstClass;
                ++this.numPassedSecondClass;
                ++this.numPassedThirdClass;
                ++this.numPassedFourthClass;
                ++this.numPassedFifthClass;
            }
        }

        /**
         * Gets the number of students.
         *
         * @return the number of students
         */
        int getN() {

            return this.numPassedFirstClass + this.numFailedFirstClass;
        }

        /**
         * Gets the first course success rate.
         *
         * @return the first course success rate
         */
        float getFirstCourseSuccess() {

            return (float) this.numPassedFirstClass
                    / (float) (this.numPassedFirstClass + this.numFailedFirstClass);
        }

        /**
         * Gets the second course success rate.
         *
         * @return the second course success rate
         */
        float getSecondCourseSuccess() {

            return (float) this.numPassedSecondClass / (float) (this.numPassedSecondClass
                    + this.numTriedAndFailedSecondClass + this.numNeverTriedSecondClass);
        }

        /**
         * Gets the second course success rate if the second course was attempted.
         *
         * @return the second course success rate
         */
        float getSecondCourseSuccessIfTried() {

            return (float) this.numPassedSecondClass
                    / (float) (this.numPassedSecondClass + this.numTriedAndFailedSecondClass);
        }

        /**
         * Gets the third course success rate.
         *
         * @return the third course success rate
         */
        float getThirdCourseSuccess() {

            return (float) this.numPassedThirdClass / (float) (this.numPassedThirdClass
                    + this.numTriedAndFailedThirdClass + this.numNeverTriedThirdClass);
        }

        /**
         * Gets the third course success rate if the third course was attempted.
         *
         * @return the third course success rate
         */
        float getThirdCourseSuccessIfTried() {

            return (float) this.numPassedThirdClass
                    / (float) (this.numPassedThirdClass + this.numTriedAndFailedThirdClass);
        }

        /**
         * Gets the fourth course success rate.
         *
         * @return the fourth course success rate
         */
        float getFourthCourseSuccess() {

            return (float) this.numPassedFourthClass / (float) (this.numPassedFourthClass
                    + this.numTriedAndFailedFourthClass + this.numNeverTriedFourthClass);
        }

        /**
         * Gets the fourth course success rate if the fourth course was attempted.
         *
         * @return the fourth course success rate
         */
        float getFourthCourseSuccessIfTried() {

            return (float) this.numPassedFourthClass
                    / (float) (this.numPassedFourthClass + this.numTriedAndFailedFourthClass);
        }

        /**
         * Gets the fifth course success rate.
         *
         * @return the fifth course success rate
         */
        float getFifthCourseSuccess() {

            return (float) this.numPassedFifthClass / (float) (this.numPassedFifthClass
                    + this.numTriedAndFailedFifthClass + this.numNeverTriedFifthClass);
        }

        /**
         * Gets the fifth course success rate if the fifth course was attempted.
         *
         * @return the fifth course success rate
         */
        float getFifthCourseSuccessIfTried() {

            return (float) this.numPassedFifthClass
                    / (float) (this.numPassedFifthClass + this.numTriedAndFailedFifthClass);
        }

        /**
         * Calculates the expectation value for number of credits passed.
         *
         * @return the expectation value
         */
        float getExpectedCreditsPassed() {

            final float earned1 = getFirstCourseSuccess() - getSecondCourseSuccess();
            final float earned2 = getSecondCourseSuccess() - getThirdCourseSuccess();
            final float earned3 = getThirdCourseSuccess() - getFourthCourseSuccess();
            final float earned4 = getFourthCourseSuccess() - getFifthCourseSuccess();
            final float earned5 = getFifthCourseSuccess();

            return earned1 + earned2 * 2.0f + earned3 * 3.0f + earned4 * 4.0f + earned5 * 5.0f;
        }
    }

    /**
     * Main method to launch the program.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        SwingUtilities.invokeLater(new CompletionRates());
    }
}

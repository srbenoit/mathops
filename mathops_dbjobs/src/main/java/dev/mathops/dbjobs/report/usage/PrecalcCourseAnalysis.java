package dev.mathops.dbjobs.report.usage;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Analyze a snapshot of precalculus course data.
 *
 * <p>
 * This class expects to find four text files and a directory in a provided data directory:
 * <ul>
 * <li>stcourse - an unload of the stcourse table, with '|' delimiters
 * <li>stexam - an unload of the stexam table, with '|' delimiters
 * <li>sthomework - an unload of the sthomework table, with '|' delimiters
 * <li>student - an unload of the student table, with '|' delimiters
 * <li>logs - a snapshot of the current server logs directory
 * </ul>
 */
final class PrecalcCourseAnalysis {

    /** A common string. */
    private static final String HEADER_ROW = "Date,0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23";

    /** Bit flag for MATH 117. */
    private static final int M117 = 0x01;

    /** Bit flag for MATH 118. */
    private static final int M118 = 0x02;

    /** Bit flag for MATH 124. */
    private static final int M124 = 0x04;

    /** Bit flag for MATH 125. */
    private static final int M125 = 0x08;

    /** Bit flag for MATH 126. */
    private static final int M126 = 0x10;

    /** The data directory. */
    private final File dir;

    /** The set of student IDs for which relevant STCOURSE records exist. */
    private final Set<String> studentIds;

    /** Loaded STCOURSE records. */
    private final List<StCourse> stcourse;

    /** Loaded STEXAM records. */
    private final List<StExam> stexam;

    /** Loaded STHOMEWORK records. */
    private final List<StHomework> sthomework;

    /** Dates spanned by analysis. */
    private List<LocalDate> dates;

    /** Histogram of exam/homework activity. */
    private Map<LocalDate, DayHistogram> histogram;

    /** Distribution of students by the set of courses they are enrolled in. */
    private final Map<Integer, List<String>> distribution;

    /**
     * Constructs a new {@code PrecalcCourseAnalysis}.
     *
     * @param theDir the data directory
     */
    private PrecalcCourseAnalysis(final File theDir) {

        this.dir = theDir;

        this.studentIds = new HashSet<>(3200);
        this.stcourse = new ArrayList<>(5600);
        this.stexam = new ArrayList<>(105000);
        this.sthomework = new ArrayList<>(54000);
        this.distribution = new HashMap<>(28);
    }

    /**
     * Perform analysis.
     *
     * @return true if successful
     */
    private boolean analyze() {

        Log.fine("ANALYZING:");

        final boolean ok = loadStCourse() && loadStExam() && loadStHomework();

        if (ok) {
            // Term start date is 08/14/2019
            final LocalDate startDate = LocalDate.of(2019, 8, 14);

            // Data collection date is 10/13/2019
            final LocalDate endDate = LocalDate.of(2019, 10, 13);

            // Construct a histogram of activity by hour of day.
            this.dates = new ArrayList<>(60);
            LocalDate date = startDate;
            while (!date.isAfter(endDate)) {
                this.dates.add(date);
                date = date.plusDays(1L);
            }

            this.histogram = new HashMap<>(this.dates.size());
            for (final LocalDate dt : this.dates) {
                this.histogram.put(dt, new DayHistogram());
            }

            for (final StExam exam : this.stexam) {
                final LocalDate examStartDate = exam.startTime.toLocalDate();
                final LocalDate examEndDate = exam.finishTime.toLocalDate();

                if (examStartDate.equals(examEndDate)) {
                    final DayHistogram hist = this.histogram.get(examStartDate);
                    if (hist != null) {
                        final int startMin = exam.startTime.getHour() * 60 + exam.startTime.getMinute();
                        final int finishMin = exam.finishTime.getHour() * 60 + exam.finishTime.getMinute();
                        hist.recordExam(startMin, finishMin, true);
                    }
                } else {
                    DayHistogram hist = this.histogram.get(examStartDate);
                    if (hist != null) {
                        final int startMin = exam.startTime.getHour() * 60 + exam.startTime.getMinute();
                        hist.recordExam(startMin, 1440, false);
                    }
                    hist = this.histogram.get(examEndDate);
                    if (hist != null) {
                        final int finishMin = exam.finishTime.getHour() * 60 + exam.finishTime.getMinute();
                        hist.recordExam(0, finishMin, true);
                    }
                }
            }

            for (final StHomework hw : this.sthomework) {
                final LocalDate hwStartDate = hw.startTime.toLocalDate();
                final LocalDate hwEndDate = hw.finishTime.toLocalDate();

                if (hwStartDate.equals(hwEndDate)) {
                    final DayHistogram hist = this.histogram.get(hwStartDate);
                    if (hist != null) {
                        final int startMin = hw.startTime.getHour() * 60 + hw.startTime.getMinute();
                        final int finishMin = hw.finishTime.getHour() * 60 + hw.finishTime.getMinute();
                        hist.recordHomework(startMin, finishMin, true);
                    }
                } else {
                    DayHistogram hist = this.histogram.get(hwStartDate);
                    if (hist != null) {
                        final int startMin = hw.startTime.getHour() * 60 + hw.startTime.getMinute();
                        hist.recordHomework(startMin, 1440, false);
                    }
                    hist = this.histogram.get(hwEndDate);
                    if (hist != null) {
                        final int finishMin = hw.finishTime.getHour() * 60 + hw.finishTime.getMinute();
                        hist.recordHomework(0, finishMin, true);
                    }
                }
            }

            // Find out how many students are in each collection of 1-5 courses
            int enrollments;

            for (final String studentId : this.studentIds) {
                enrollments = 0;
                for (final StCourse stc : this.stcourse) {
                    if (studentId.equals(stc.studentId)) {
                        if (RawRecordConstants.M117.equals(stc.courseId)) {
                            enrollments |= M117;
                        } else if (RawRecordConstants.M118.equals(stc.courseId)) {
                            enrollments |= M118;
                        } else if (RawRecordConstants.M124.equals(stc.courseId)) {
                            enrollments |= M124;
                        } else if (RawRecordConstants.M125.equals(stc.courseId)) {
                            enrollments |= M125;
                        } else if (RawRecordConstants.M126.equals(stc.courseId)) {
                            enrollments |= M126;
                        }
                    }
                }

                final Integer key = Integer.valueOf(enrollments);
                final List<String> list = this.distribution.computeIfAbsent(key, k -> new ArrayList<>(50));
                list.add(studentId);
            }
        }

        scanLogs();

        return ok;
    }

    /**
     * Loads the STCOURSE table.
     *
     * @return true if successful
     */
    private boolean loadStCourse() {

        boolean ok = true;

        final File file = new File(this.dir, "stcourse");
        try (final BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {

            String line = br.readLine();
            while (line != null) {
                final String[] split = line.split("\\|");

                final String term = split[3];
                if ("FA".equals(term)) {

                    final String year = split[4];
                    if ("19".equals(year)) {

                        final String section = split[2];
                        if ("001".equals(section) || "801".contentEquals(section)) {

                            final String open = split[6].isEmpty() ? null : split[6];

                            if ("Y".equals(open) || "N".equals(open) || open == null) {

                                final String studentId = split[0];
                                final String courseId = split[1];

                                this.studentIds.add(studentId);
                                this.stcourse.add(new StCourse(studentId, courseId));
                            }
                        }
                    }
                }

                line = br.readLine();
            }
        } catch (final NumberFormatException | IOException ex) {
            Log.warning(ex);
            ok = false;
        }

        Log.fine("    Loaded ", Integer.toString(this.stcourse.size()), " STCOURSE records for ",
                Integer.toString(this.studentIds.size()), " distinct students");

        return ok;
    }

    /**
     * Loads the STEXAM table.
     *
     * @return true if successful
     */
    private boolean loadStExam() {

        boolean ok = true;

        final File file = new File(this.dir, "stexam");
        try (final BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {

            String line = br.readLine();
            while (line != null) {
                final String[] split = line.split("\\|");

                final String courseId = split[10];
                if (RawRecordConstants.M117.contentEquals(courseId)
                        || RawRecordConstants.M118.contentEquals(courseId)
                        || RawRecordConstants.M124.contentEquals(courseId)
                        || RawRecordConstants.M125.contentEquals(courseId)
                        || RawRecordConstants.M126.contentEquals(courseId)) {

                    final String serial = split[0];

                    final String examDt = split[3];

                    if (examDt.length() == 10) {
                        final int month = Integer.parseInt(examDt.substring(0, 2));
                        final int day = Integer.parseInt(examDt.substring(3, 5));
                        final int year = Integer.parseInt(examDt.substring(6));

                        if (year >= 2019 && month > 8 || month == 8 && day >= 14) {

                            int start = Integer.parseInt(split[5]);
                            int startDayDelta = 0;
                            int finish = Integer.parseInt(split[6]);
                            int finishDayDelta = 0;

                            // Finish time may be greater than 1439 to allow short grace period
                            while (finish > 1439) {
                                finish -= 1440;
                                ++finishDayDelta;
                            }

                            // If start time is negative, it means exam was started previous day
                            while (start < 0) {
                                start += 1440;
                                --startDayDelta;
                            }

                            // If start is greater than finish, assume it was started the previous
                            // day
                            if (start > finish) {
                                --startDayDelta;
                            }

                            final LocalDateTime startTime = LocalDateTime.of(year, month, day, start / 60, start % 60)
                                    .plusDays(startDayDelta);

                            final LocalDateTime finishTime = LocalDateTime.of(year, month, day, finish / 60,
                                    finish % 60).plusDays(finishDayDelta);

                            if (startTime.isAfter(finishTime)) {
                                Log.warning(serial, " bad range: ", examDt, " / ", split[5], " - ", split[6]);
                            }

                            this.stexam.add(new StExam(startTime, finishTime));
                        }
                    } else {
                        Log.warning("Invalid exam date: ", examDt);
                    }
                }

                line = br.readLine();
            }
        } catch (final NumberFormatException | IOException ex) {
            Log.warning(ex);
            ok = false;
        }

        Log.fine("    Loaded ", Integer.toString(this.stexam.size()), " STEXAM records...");

        return ok;
    }

    /**
     * Loads the STHOMEWORK table.
     *
     * @return true if successful
     */
    private boolean loadStHomework() {

        boolean ok = true;

        final File file = new File(this.dir, "sthomework");
        try (final BufferedReader br = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {

            String line = br.readLine();
            while (line != null) {
                final String[] split = line.split("\\|");

                final String courseId = split[10];
                if (RawRecordConstants.M117.contentEquals(courseId)
                        || RawRecordConstants.M118.contentEquals(courseId)
                        || RawRecordConstants.M124.contentEquals(courseId)
                        || RawRecordConstants.M125.contentEquals(courseId)
                        || RawRecordConstants.M126.contentEquals(courseId)) {

                    final String serial = split[0];

                    final String examDt = split[3];

                    if (examDt.length() == 10) {
                        final int month = Integer.parseInt(examDt.substring(0, 2));
                        final int day = Integer.parseInt(examDt.substring(3, 5));
                        final int year = Integer.parseInt(examDt.substring(6));

                        if (year >= 2019 && month > 8 || month == 8 && day >= 14) {
                            int start = Integer.parseInt(split[5]);
                            int startDayDelta = 0;
                            int finish = Integer.parseInt(split[6]);
                            int finishDayDelta = 0;

                            // Finish time may be greater than 1439 to allow short grace period
                            while (finish > 1439) {
                                finish -= 1440;
                                ++finishDayDelta;
                            }

                            // If start time is negative, it means exam was started previous day
                            while (start < 0) {
                                start += 1440;
                                --startDayDelta;
                            }

                            // If start is greater than finish, assume it was started the previous
                            // day
                            if (start > finish) {
                                --startDayDelta;
                            }

                            final LocalDateTime startTime = LocalDateTime.of(year, month, day, start / 60, start % 60)
                                    .plusDays(startDayDelta);

                            final LocalDateTime finishTime = LocalDateTime.of(year, month, day, finish / 60,
                                    finish % 60).plusDays(finishDayDelta);

                            if (startTime.isAfter(finishTime)) {
                                Log.warning(serial, " bad range: ", examDt, " / ", split[5], " - ", split[6]);
                            }

                            this.sthomework.add(new StHomework(startTime, finishTime));
                        }
                    } else {
                        Log.warning("Invalid homework date: ", examDt);
                    }
                }

                line = br.readLine();
            }
        } catch (final NumberFormatException | IOException ex) {
            Log.warning(ex);
            ok = false;
        }

        Log.fine("    Loaded ", Integer.toString(this.sthomework.size()), " STHOMEWORK records...");

        return ok;
    }

    /**
     * Scan log files for user activity.
     */
    private void scanLogs() {

        Log.fine("    Scanning log files for user activity:");

        final File logdir = new File(this.dir, "logs");
        final File[] logFiles = logdir.listFiles();

        if (logFiles != null) {
            for (final File logfile : logFiles) {
                Log.fine("        ", logfile.getName());
                try (final BufferedReader br = new BufferedReader(new FileReader(logfile, StandardCharsets.UTF_8))) {

                    String line = br.readLine();
                    while (line != null) {

                        final int index = line.indexOf("I CourseSiteData took ");
                        if (index != -1) {
                            recordLogRow(line.substring(0, index));
                        }

                        line = br.readLine();
                    }
                } catch (final NumberFormatException | IOException ex) {
                    Log.warning(ex);
                }
            }
        }
    }

    /**
     * Records a row in a log file that indicates a user was active.
     *
     * @param line the log file line
     */
    private void recordLogRow(final String line) {

        final int firstSlash = line.indexOf('/');
        final int firstColon = line.indexOf(':');
        final int lastColon = line.lastIndexOf(':');

        if (firstSlash >= 2 && firstColon > firstSlash && lastColon > firstColon) {

            try {
                final int month = Integer.parseInt(line.substring(firstSlash - 2, firstSlash));
                final int day = Integer.parseInt(line.substring(firstSlash + 1, firstSlash + 3));

                final LocalDate when = LocalDate.of(2019, month, day);
                final DayHistogram hd = this.histogram.get(when);
                if (hd != null) {
                    final int hour = Integer.parseInt(line.substring(firstColon - 2, firstColon));
                    final String studentId = line.substring(lastColon + 1).trim();

                    hd.recordActivity(studentId, hour);
                }
            } catch (final NumberFormatException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Print report.
     */
    private void report() {

        Log.fine(CoreConstants.CRLF, "REPORT:");

        // --------------------------------------------------------------------------------------

        Log.fine("    Writing report of total exams and homeworks submitted per day");

        final HtmlBuilder txt1 = new HtmlBuilder(1000);
        txt1.addln("Date,Total Exams,Total Homeworks");
        for (final LocalDate dt : this.dates) {
            final DayHistogram hist = this.histogram.get(dt);

            txt1.add(dt.getYear()).add('-').add(dt.getMonthValue()).add('-').add(dt.getDayOfMonth())
                    .add(CoreConstants.COMMA_CHAR).add(hist.getTotalExams()).add(CoreConstants.COMMA_CHAR)
                    .addln(hist.getTotalHomeworks());
        }

        final File rep1 = new File(this.dir, "report-total-exams-homeworks-per-day.csv");

        try (final FileWriter w = new FileWriter(rep1, StandardCharsets.UTF_8)) {
            w.write(txt1.toString());
        } catch (final NumberFormatException | IOException ex) {
            Log.warning(ex);
        }

        // --------------------------------------------------------------------------------------

        Log.fine("    Writing report of exams finishing in each hour of each day");

        final HtmlBuilder txt2 = new HtmlBuilder(1000);
        txt2.addln(HEADER_ROW);
        for (final LocalDate dt : this.dates) {
            final DayHistogram hist = this.histogram.get(dt);

            txt2.add(dt.getYear()).add('-').add(dt.getMonthValue()).add('-').add(dt.getDayOfMonth());
            for (int i = 0; i < 24; ++i) {
                txt2.add(CoreConstants.COMMA_CHAR).add(hist.getNumExams(i));
            }
            txt2.addln();
        }

        final File rep2 = new File(this.dir, "report-finished-exams-per-hour.csv");

        try (final FileWriter w = new FileWriter(rep2, StandardCharsets.UTF_8)) {
            w.write(txt2.toString());
        } catch (final NumberFormatException | IOException ex) {
            Log.warning(ex);
        }

        // --------------------------------------------------------------------------------------

        Log.fine("    Writing report of exam minutes in each hour of each day");

        final HtmlBuilder txt3 = new HtmlBuilder(1000);
        txt3.addln(HEADER_ROW);
        for (final LocalDate dt : this.dates) {
            final DayHistogram hist = this.histogram.get(dt);

            txt3.add(dt.getYear()).add('-').add(dt.getMonthValue()).add('-').add(dt.getDayOfMonth());
            for (int i = 0; i < 24; ++i) {
                txt3.add(CoreConstants.COMMA_CHAR).add(hist.getExamMinutes(i));
            }
            txt3.addln();
        }

        final File rep3 = new File(this.dir, "report-exam-minutes-per-hour.csv");

        try (final FileWriter w = new FileWriter(rep3, StandardCharsets.UTF_8)) {
            w.write(txt3.toString());
        } catch (final NumberFormatException | IOException ex) {
            Log.warning(ex);
        }

        // --------------------------------------------------------------------------------------

        Log.fine("    Writing report of homeworks finishing in each hour of each day");

        final HtmlBuilder txt4 = new HtmlBuilder(1000);
        txt4.addln(HEADER_ROW);
        for (final LocalDate dt : this.dates) {
            final DayHistogram hist = this.histogram.get(dt);

            txt4.add(dt.getYear()).add('-').add(dt.getMonthValue()).add('-').add(dt.getDayOfMonth());
            for (int i = 0; i < 24; ++i) {
                txt4.add(CoreConstants.COMMA_CHAR).add(hist.getNumHomeworks(i));
            }
            txt4.addln();
        }

        final File rep4 = new File(this.dir, "report-finished-homeworks-per-hour.csv");

        try (final FileWriter w = new FileWriter(rep4, StandardCharsets.UTF_8)) {
            w.write(txt4.toString());
        } catch (final NumberFormatException | IOException ex) {
            Log.warning(ex);
        }

        // --------------------------------------------------------------------------------------

        Log.fine("    Writing report of homework minutes in each hour of each day");

        final HtmlBuilder txt5 = new HtmlBuilder(1000);
        txt5.addln(HEADER_ROW);
        for (final LocalDate dt : this.dates) {
            final DayHistogram hist = this.histogram.get(dt);

            txt5.add(dt.getYear()).add('-').add(dt.getMonthValue()).add('-').add(dt.getDayOfMonth());
            for (int i = 0; i < 24; ++i) {
                txt5.add(CoreConstants.COMMA_CHAR).add(hist.getHomeworkMinutes(i));
            }
            txt5.addln();
        }

        final File rep5 = new File(this.dir, "report-homework-minutes-per-hour.csv");

        try (final FileWriter w = new FileWriter(rep5, StandardCharsets.UTF_8)) {
            w.write(txt5.toString());
        } catch (final NumberFormatException | IOException ex) {
            Log.warning(ex);
        }

        // --------------------------------------------------------------------------------------

        Log.fine("    Writing report of active students per hour per day");

        final HtmlBuilder txt6 = new HtmlBuilder(1000);
        txt6.addln(HEADER_ROW);
        for (final LocalDate dt : this.dates) {
            final DayHistogram hist = this.histogram.get(dt);

            txt6.add(dt.getYear()).add('-').add(dt.getMonthValue()).add('-').add(dt.getDayOfMonth());
            for (int i = 0; i < 24; ++i) {
                txt6.add(CoreConstants.COMMA_CHAR).add(hist.getActiveStudents(i).size());
            }
            txt6.addln();
        }

        final File rep6 = new File(this.dir, "report-active-students-per-hour.csv");

        try (final FileWriter w = new FileWriter(rep6, StandardCharsets.UTF_8)) {
            w.write(txt6.toString());
        } catch (final NumberFormatException | IOException ex) {
            Log.warning(ex);
        }

        // --------------------------------------------------------------------------------------

        Log.fine("    Writing report of distribution of students by course mixture");

        final HtmlBuilder txt7 = new HtmlBuilder(1000);

        txt7.addln("One-course");
        List<String> list = this.distribution.get(Integer.valueOf(M117));
        txt7.addln("MATH 117,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M118));
        txt7.addln("MATH 118,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M124));
        txt7.addln("MATH 124,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M125));
        txt7.addln("MATH 125,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M126));
        txt7.addln("MATH 126,", list == null ? "0" : Integer.toString(list.size()));

        txt7.addln("Two-course");
        list = this.distribution.get(Integer.valueOf(M117 | M118));
        txt7.addln("MATH 117/118,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M117 | M124));
        txt7.addln("MATH 117/124,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M117 | M125));
        txt7.addln("MATH 117/125,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M117 | M126));
        txt7.addln("MATH 117/126,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M118 | M124));
        txt7.addln("MATH 118/124,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M118 | M125));
        txt7.addln("MATH 118/125,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M118 | M126));
        txt7.addln("MATH 118/126,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M124 | M125));
        txt7.addln("MATH 124/125,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M124 | M126));
        txt7.addln("MATH 124/126,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M125 | M126));
        txt7.addln("MATH 125/126,", list == null ? "0" : Integer.toString(list.size()));

        txt7.addln("Three-course");
        list = this.distribution.get(Integer.valueOf(M117 | M118 | M124));
        txt7.addln("MATH 117/118/124,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M117 | M118 | M125));
        txt7.addln("MATH 117/118/125,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M117 | M118 | M126));
        txt7.addln("MATH 117/118/126,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M117 | M124 | M125));
        txt7.addln("MATH 117/124/125,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M117 | M124 | M126));
        txt7.addln("MATH 117/124/126,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M117 | M125 | M126));
        txt7.addln("MATH 117/125/126,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M118 | M124 | M125));
        txt7.addln("MATH 118/124/125,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M118 | M124 | M126));
        txt7.addln("MATH 118/124/126,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M118 | M125 | M126));
        txt7.addln("MATH 118/125/126,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M124 | M125 | M126));
        txt7.addln("MATH 124/125/126,", list == null ? "0" : Integer.toString(list.size()));

        txt7.addln("Four-course");
        list = this.distribution.get(Integer.valueOf(M117 | M118 | M124 | M125));
        txt7.addln("MATH 117/118/124/125,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M117 | M118 | M124 | M126));
        txt7.addln("MATH 117/118/124/126,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M117 | M118 | M125 | M126));
        txt7.addln("MATH 117/118/125/126,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M117 | M124 | M125 | M126));
        txt7.addln("MATH 117/124/125/126,", list == null ? "0" : Integer.toString(list.size()));
        list = this.distribution.get(Integer.valueOf(M118 | M124 | M125 | M126));
        txt7.addln("MATH 118/124/125/126,", list == null ? "0" : Integer.toString(list.size()));

        txt7.addln("Five-course");
        list = this.distribution.get(Integer.valueOf(M117 | M118 | M124 | M125 | M126));
        txt7.addln("MATH 117/118/124/125/126,", list == null ? "0" : Integer.toString(list.size()));

        final File rep7 = new File(this.dir, "report-student-distribution.csv");

        try (final FileWriter w = new FileWriter(rep7, StandardCharsets.UTF_8)) {
            w.write(txt7.toString());
        } catch (final NumberFormatException | IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Run the analysis.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final File dir = new File("/Users/benoit/Desktop/pace_analysis");

        final PrecalcCourseAnalysis analysis = new PrecalcCourseAnalysis(dir);
        if (analysis.analyze()) {
            analysis.report();
        }
    }
}

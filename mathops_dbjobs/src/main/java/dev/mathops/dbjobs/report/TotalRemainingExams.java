package dev.mathops.dbjobs.report;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Generates a report that queries all active student registrations in the active term and determines how many exams the
 * student still needs to pass to complete all of their courses.
 */
final class TotalRemainingExams implements Runnable {

    /** Date/time formatter for report. */
    private static final DateTimeFormatter DTTM_FMT =
            DateTimeFormatter.ofPattern("yyyy'_'MM'_'dd'-'hh'_'mm'_'ss", Locale.US);

    /** Flag for review exam 1. */
    private static final int REVIEW_1 = 0x01;

    /** Flag for unit exam 1. */
    private static final int UNIT_1 = 0x02;

    /** Flag for review exam 2. */
    private static final int REVIEW_2 = 0x04;

    /** Flag for unit exam 2. */
    private static final int UNIT_2 = 0x08;

    /** Flag for review exam 3. */
    private static final int REVIEW_3 = 0x10;

    /** Flag for unit exam 3. */
    private static final int UNIT_3 = 0x20;

    /** Flag for review exam 4. */
    private static final int REVIEW_4 = 0x40;

    /** Flag for unit exam 4. */
    private static final int UNIT_4 = 0x80;

    /** Flag for final exam. */
    private static final int FINAL = 0x0100;

    /** Flag for unit 2 midterm. */
    private static final int MIDTERM_2 = 0x0200;

    /** Flag for unit 4 midterm. */
    private static final int MIDTERM_4 = 0x0400;

    /** The list of registrations. */
    private List<RawStcourse> registrations;

    /** The list of passing exams in MATH 117. */
    private List<RawStexam> exams117;

    /** The list of passing exams in MATH 118. */
    private List<RawStexam> exams118;

    /** The list of passing exams in MATH 124. */
    private List<RawStexam> exams124;

    /** The list of passing exams in MATH 125. */
    private List<RawStexam> exams125;

    /** The list of passing exams in MATH 126. */
    private List<RawStexam> exams126;

    /** Map from course to the counts of exams completed and remaining of each type. */
    private Map<String, CourseCounts> countsMap;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private TotalRemainingExams() {

        // No action
    }

    /**
     * Runs the report.
     */
    @Override
    public void run() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.REPORT_PATH);

        final HtmlBuilder report = new HtmlBuilder(10000);

        if (profile == null) {
            report.addln("*** ERROR: There is no database code profile named 'report'.");
        } else {
            Log.info("Using ", profile.id, " profile");
            final Cache cache = new Cache(profile);

            try {
                gatherData(cache);

                Log.info("Generating report of total exams remaining to be passed.....please wait.");

                compute();
                generateReport(report);
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }

        final LocalDateTime now = LocalDateTime.now();
        final File file = new File("/opt/zircon/reports/Total_Remaining_Exams_" + DTTM_FMT.format(now) + ".txt");
        try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(report.toString());
            Log.info("Report complete, written to: ", file.getAbsolutePath());
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Gathers data needed for the report.
     *
     * @param cache the data cache
     * @throws SQLException if there was an error accessing the database
     */
    private void gatherData(final Cache cache) throws SQLException {

        this.registrations = RawStcourseLogic.queryActiveForActiveTerm(cache);

        this.exams117 = RawStexamLogic.getExams(cache, RawRecordConstants.M117, true, "R", "U", "F");
        this.exams118 = RawStexamLogic.getExams(cache, RawRecordConstants.M118, true, "R", "U", "F");
        this.exams124 = RawStexamLogic.getExams(cache, RawRecordConstants.M124, true, "R", "U", "F");
        this.exams125 = RawStexamLogic.getExams(cache, RawRecordConstants.M125, true, "R", "U", "F");
        this.exams126 = RawStexamLogic.getExams(cache, RawRecordConstants.M126, true, "R", "U", "F");
    }

    /**
     * Computes summary data based on the registrations and exams queried.
     */
    private void compute() {

        // Map from course ID to map from student ID to passed exams
        final Map<String, Map<String, RegistrationStatus>> summary = new HashMap<>(2400);

        for (final RawStcourse stc : this.registrations) {

            final String courseId = stc.course;

            List<RawStexam> examList = null;

            if (RawRecordConstants.M117.equals(courseId)) {
                examList = this.exams117;
            } else if (RawRecordConstants.M118.equals(courseId)) {
                examList = this.exams118;
            } else if (RawRecordConstants.M124.equals(courseId)) {
                examList = this.exams124;
            } else if (RawRecordConstants.M125.equals(courseId)) {
                examList = this.exams125;
            } else if (RawRecordConstants.M126.equals(courseId)) {
                examList = this.exams126;
            }

            if (examList != null) {

                final Map<String, RegistrationStatus> studentMap = summary.computeIfAbsent(courseId,
                        s -> new HashMap<>(1000));

                final String stuId = stc.stuId;

                final RegistrationStatus status = new RegistrationStatus();
                studentMap.put(stuId, status);

                status.setRequiredExams(REVIEW_1 | REVIEW_2 | REVIEW_3 | REVIEW_4 | UNIT_1 | UNIT_2 | UNIT_3 | UNIT_4
                                        | FINAL);

                for (final RawStexam test : examList) {
                    if (stuId.equals(test.stuId)) {
                        final String type = test.examType;
                        final int unit = test.unit.intValue();

                        if ("R".equals(type)) {
                            if (unit == 1) {
                                status.setCompletedExams(status.getCompletedExams() | REVIEW_1);
                            } else if (unit == 2) {
                                status.setCompletedExams(status.getCompletedExams() | REVIEW_2);
                            } else if (unit == 3) {
                                status.setCompletedExams(status.getCompletedExams() | REVIEW_3);
                            } else if (unit == 4) {
                                status.setCompletedExams(status.getCompletedExams() | REVIEW_4);
                            }
                        } else if ("U".equals(type)) {
                            if (unit == 1) {
                                status.setCompletedExams(status.getCompletedExams() | UNIT_1);
                            } else if (unit == 2) {
                                status.setCompletedExams(status.getCompletedExams() | UNIT_2);
                            } else if (unit == 3) {
                                status.setCompletedExams(status.getCompletedExams() | UNIT_3);
                            } else if (unit == 4) {
                                status.setCompletedExams(status.getCompletedExams() | UNIT_4);
                            }
                        } else if ("F".equals(type)) {
                            status.setCompletedExams(status.getCompletedExams() | FINAL);
                        }
                    }
                }
            }
        }

        this.countsMap = new HashMap<>(summary.size());

        for (final Map.Entry<String, Map<String, RegistrationStatus>> entry : summary.entrySet()) {

            final CourseCounts counts = new CourseCounts();
            this.countsMap.put(entry.getKey(), counts);

            final Map<String, RegistrationStatus> studentMap = entry.getValue();
            for (final RegistrationStatus value : studentMap.values()) {

                if ((value.getRequiredExams() & REVIEW_1) == REVIEW_1) {
                    if ((value.getCompletedExams() & REVIEW_1) == REVIEW_1) {
                        counts.setReviewsComplete(counts.getReviewsComplete() + 1);
                    } else {
                        counts.setReviewsRemaining(counts.getReviewsRemaining() + 1);
                    }
                }

                if ((value.getRequiredExams() & REVIEW_2) == REVIEW_2) {
                    if ((value.getCompletedExams() & REVIEW_2) == REVIEW_2) {
                        counts.setReviewsComplete(counts.getReviewsComplete() + 1);
                    } else {
                        counts.setReviewsRemaining(counts.getReviewsRemaining() + 1);
                    }
                }

                if ((value.getRequiredExams() & REVIEW_3) == REVIEW_3) {
                    if ((value.getCompletedExams() & REVIEW_3) == REVIEW_3) {
                        counts.setReviewsComplete(counts.getReviewsComplete() + 1);
                    } else {
                        counts.setReviewsRemaining(counts.getReviewsRemaining() + 1);
                    }
                }

                if ((value.getRequiredExams() & REVIEW_4) == REVIEW_4) {
                    if ((value.getCompletedExams() & REVIEW_4) == REVIEW_4) {
                        counts.setReviewsComplete(counts.getReviewsComplete() + 1);
                    } else {
                        counts.setReviewsRemaining(counts.getReviewsRemaining() + 1);
                    }
                }

                if ((value.getRequiredExams() & UNIT_1) == UNIT_1) {
                    if ((value.getCompletedExams() & UNIT_1) == UNIT_1) {
                        counts.setUnitsComplete(counts.getUnitsComplete() + 1);
                    } else {
                        counts.setUnitsRemaining(counts.getUnitsRemaining() + 1);
                    }
                }

                if ((value.getRequiredExams() & UNIT_2) == UNIT_2) {
                    if ((value.getCompletedExams() & UNIT_2) == UNIT_2) {
                        counts.setUnitsComplete(counts.getUnitsComplete() + 1);
                    } else {
                        counts.setUnitsRemaining(counts.getUnitsRemaining() + 1);
                    }
                }

                if ((value.getRequiredExams() & UNIT_3) == UNIT_3) {
                    if ((value.getCompletedExams() & UNIT_3) == UNIT_3) {
                        counts.setUnitsComplete(counts.getUnitsComplete() + 1);
                    } else {
                        counts.setUnitsRemaining(counts.getUnitsRemaining() + 1);
                    }
                }

                if ((value.getRequiredExams() & UNIT_4) == UNIT_4) {
                    if ((value.getCompletedExams() & UNIT_4) == UNIT_4) {
                        counts.setUnitsComplete(counts.getUnitsComplete() + 1);
                    } else {
                        counts.setUnitsRemaining(counts.getUnitsRemaining() + 1);
                    }
                }

                if ((value.getRequiredExams() & MIDTERM_2) == MIDTERM_2) {
                    if ((value.getCompletedExams() & MIDTERM_2) == MIDTERM_2) {
                        counts.setMidtermsComplete(counts.getMidtermsComplete() + 1);
                    } else {
                        counts.setMidtermsRemaining(counts.getMidtermsRemaining() + 1);
                    }
                }

                if ((value.getRequiredExams() & MIDTERM_4) == MIDTERM_4) {
                    if ((value.getCompletedExams() & MIDTERM_4) == MIDTERM_4) {
                        counts.setMidtermsComplete(counts.getMidtermsComplete() + 1);
                    } else {
                        counts.setMidtermsRemaining(counts.getMidtermsRemaining() + 1);
                    }
                }

                if ((value.getRequiredExams() & FINAL) == FINAL) {
                    if ((value.getCompletedExams() & FINAL) == FINAL) {
                        counts.setFinalsComplete(counts.getFinalsComplete() + 1);
                    } else {
                        counts.setFinalsRemaining(counts.getFinalsRemaining() + 1);
                    }
                }
            }
        }
    }

    /**
     * Generates the report.
     *
     * @param report the {@code HtmlBuilder} to which to write error messages
     */
    private void generateReport(final HtmlBuilder report) {

        report.addln("                              *** CONFIDENTIAL ***")
                .addln("                   COLORADO STATE UNIVERSITY MATHEMATICS DEPT")
                .addln("                      TOTAL EXAMS REMAINING TO BE COMPLETED")
                .addln("                           REPORT DATE:  ", TemporalUtils.FMT_MDY.format(LocalDate.now()));

        int overallComplete = 0;
        int overallRemaining = 0;
        int overallCompleteProc = 0;
        int overallRemainingProc = 0;

        for (final Map.Entry<String, CourseCounts> entry : this.countsMap.entrySet()) {

            report.addln().addln(entry.getKey().replace("M ", "MATH ")).addln();

            final CourseCounts counts = entry.getValue();

            report.addln("   Review Exams: ", numString(counts.getReviewsComplete()), " out of ",
                    numString(counts.getReviewsComplete() + counts.getReviewsRemaining()),
                    " complete, ", numString(counts.getReviewsRemaining()), " remaining.");

            report.addln("     Unit Exams: ", numString(counts.getUnitsComplete()), " out of ",
                    numString(counts.getUnitsComplete() + counts.getUnitsRemaining()), " complete, ",
                    numString(counts.getUnitsRemaining()), " remaining.");

            report.addln("  Midterm Exams: ", numString(counts.getMidtermsComplete()), " out of ",
                    numString(counts.getMidtermsComplete() + counts.getMidtermsRemaining()),
                    " complete, ", numString(counts.getMidtermsRemaining()), " remaining.");

            report.addln("    Final Exams: ", numString(counts.getFinalsComplete()), " out of ",
                    numString(counts.getFinalsComplete() + counts.getFinalsRemaining()), " complete, ",
                    numString(counts.getFinalsRemaining()), " remaining.");

            final int totalComplete = counts.getReviewsComplete() + counts.getUnitsComplete()
                                      + counts.getMidtermsComplete() + counts.getFinalsComplete();
            final int totalRemaining = counts.getReviewsRemaining() + counts.getUnitsRemaining()
                                       + counts.getMidtermsRemaining() + counts.getFinalsRemaining();
            final int totalTotal = totalComplete + totalRemaining;

            report.addln("     Cumulative: ", numString(totalComplete), " out of ",
                    numString(totalTotal), " complete, ", numString(totalRemaining), " remaining.");

            overallComplete += totalComplete;
            overallRemaining += totalRemaining;

            overallCompleteProc += counts.getUnitsComplete();
            overallRemainingProc += counts.getUnitsRemaining();
        }

        report.addln().addln("ALL COURSES").addln();
        report.addln("     Cumulative: ", numString(overallComplete), " out of ",
                numString(overallComplete + overallRemaining), " complete, ",
                numString(overallRemaining), " remaining.");

        report.addln("      Proctored: ", numString(overallCompleteProc), " out of ",
                numString(overallCompleteProc + overallRemainingProc), " complete, ",
                numString(overallRemainingProc), " remaining.");
    }

    /**
     * Generates a fixed-length string representation of a number (assumed non-negative), right-justified.
     *
     * @param value the numeric value
     * @return the string
     */
    private static String numString(final int value) {

        final String result;

        if (value < 10) {
            result = "    " + value;
        } else if (value < 100) {
            result = "   " + value;
        } else if (value < 1000) {
            result = "  " + value;
        } else if (value < 10000) {
            result = CoreConstants.SPC + value;
        } else {
            result = Integer.toString(value);
        }

        return result;
    }

    /**
     * Main method to run the report.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        new TotalRemainingExams().run();
    }

    /**
     * Status of each registration record.
     */
    private static class RegistrationStatus {

        /** Required exams. */
        private int requiredExams;

        /** Completed exams. */
        private int completedExams;

        /**
         * Constructs a new {@code RegistrationStatus}.
         */
        RegistrationStatus() {

            // No action
        }

        /**
         * Gets the number of required exams.
         *
         * @return the count
         */
        final int getRequiredExams() {

            return this.requiredExams;
        }

        /**
         * Sets the number of required exams.
         *
         * @param theRequiredExams the new count
         */
        final void setRequiredExams(final int theRequiredExams) {

            this.requiredExams = theRequiredExams;
        }

        /**
         * Gets the number of completed exams.
         *
         * @return the count
         */
        final int getCompletedExams() {

            return this.completedExams;
        }

        /**
         * Sets the number of completed exams.
         *
         * @param theCompletedExams the new count
         */
        final void setCompletedExams(final int theCompletedExams) {

            this.completedExams = theCompletedExams;
        }
    }

    /**
     * Counts in each course.
     */
    private static class CourseCounts {

        /** Review exams complete. */
        private int reviewsComplete;

        /** Review exams remaining. */
        private int reviewsRemaining;

        /** Unit exams complete. */
        private int unitsComplete;

        /** Unit exams remaining. */
        private int unitsRemaining;

        /** Midterm exams complete. */
        private int midtermsComplete;

        /** Midterm exams remaining. */
        private int midtermsRemaining;

        /** Final exams complete. */
        private int finalsComplete;

        /** Final exams remaining. */
        private int finalsRemaining;

        /**
         * Constructs a new {@code CourseCounts}.
         */
        CourseCounts() {

            // No action
        }

        /**
         * Gets the number of review exams complete.
         *
         * @return the count
         */
        final int getReviewsComplete() {

            return this.reviewsComplete;
        }

        /**
         * Sets the number of review exams complete.
         *
         * @param theReviewsComplete the new count
         */
        final void setReviewsComplete(final int theReviewsComplete) {

            this.reviewsComplete = theReviewsComplete;
        }

        /**
         * Gets the number of review exams remaining.
         *
         * @return the count
         */
        final int getReviewsRemaining() {

            return this.reviewsRemaining;
        }

        /**
         * Sets the number of review exams remaining.
         *
         * @param theReviewsRemaining the new count
         */
        final void setReviewsRemaining(final int theReviewsRemaining) {

            this.reviewsRemaining = theReviewsRemaining;
        }

        /**
         * Gets the number of unit exams complete.
         *
         * @return the count
         */
        final int getUnitsComplete() {

            return this.unitsComplete;
        }

        /**
         * Sets the number of unit exams complete.
         *
         * @param theUnitsComplete the new count
         */
        final void setUnitsComplete(final int theUnitsComplete) {

            this.unitsComplete = theUnitsComplete;
        }

        /**
         * Gets the number of unit exams remaining.
         *
         * @return the count
         */
        final int getUnitsRemaining() {

            return this.unitsRemaining;
        }

        /**
         * Sets the number of unit exams remaining.
         *
         * @param theUnitsRemaining the new count
         */
        final void setUnitsRemaining(final int theUnitsRemaining) {

            this.unitsRemaining = theUnitsRemaining;
        }

        /**
         * Gets the number of midterms complete.
         *
         * @return the count
         */
        final int getMidtermsComplete() {

            return this.midtermsComplete;
        }

        /**
         * Sets the number of midterms complete.
         *
         * @param theMidtermsComplete the new count
         */
        final void setMidtermsComplete(final int theMidtermsComplete) {

            this.midtermsComplete = theMidtermsComplete;
        }

        /**
         * Gets the number of midterms remaining.
         *
         * @return the count
         */
        final int getMidtermsRemaining() {

            return this.midtermsRemaining;
        }

        /**
         * Sets the number of midterms remaining.
         *
         * @param theMidtermsRemaining the new count
         */
        final void setMidtermsRemaining(final int theMidtermsRemaining) {

            this.midtermsRemaining = theMidtermsRemaining;
        }

        /**
         * Gets the number of finals complete.
         *
         * @return the count
         */
        final int getFinalsComplete() {

            return this.finalsComplete;
        }

        /**
         * Sets the number of finals complete.
         *
         * @param theFinalsComplete the new count
         */
        final void setFinalsComplete(final int theFinalsComplete) {

            this.finalsComplete = theFinalsComplete;
        }

        /**
         * Gets the number of finals remaining.
         *
         * @return the count
         */
        final int getFinalsRemaining() {

            return this.finalsRemaining;
        }

        /**
         * Sets the number of finals remaining.
         *
         * @param theFinalsRemaining the new count
         */
        final void setFinalsRemaining(final int theFinalsRemaining) {

            this.finalsRemaining = theFinalsRemaining;
        }
    }
}

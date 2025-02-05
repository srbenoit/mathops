package dev.mathops.dbjobs.report;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Generates a report of all students who have forfeited working on a course in order to move to a new RE deadline
 * schedule -- open_status = N/G to either stay in same track or move to a lesser # of courses track (rather than
 * withdrawing to accomplish this change)
 */
public enum SpecialOpenReport {
    ;

    /**
     * Runs the report, writing the result to a file.
     */
    private static void runReport() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.REPORT_PATH);
        final Cache cache = new Cache(profile);

        try {
            final Collection<String> report = new ArrayList<>(100);

            report.add(CoreConstants.EMPTY);
            report.add("     Students with special open_status flags in STCOURSE");
            report.add("     Report Date:  " + TemporalUtils.FMT_MDY.format(LocalDate.now()));

            generate(cache, report);

            for (final String s : report) {
                Log.fine(s);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Generates the report and appends it to a list of strings. This can be called from external reports or batches to
     * embed this report in a larger report.
     *
     * @param cache  the data cache
     * @param report the list to which to append report lines
     * @throws SQLException if there was an error accessing the database
     */
    public static void generate(final Cache cache, final Collection<? super String> report) throws SQLException {

        int totalG = 0;
        int totalN = 0;

        final SystemData systemData = cache.getSystemData();
        final TermRec active = systemData.getActiveTerm();

        // Exclude OT and Dropped
        final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(cache, active.term, false, false);

        final List<Record> records = new ArrayList<>(20);

        for (final RawStcourse test : allRegs) {
            if (!"Y".equals(test.finalClassRoll) || "Y".equals(test.iInProgress)) {
                continue;
            }

            final String stat = test.openStatus;
            if ("G".equals(stat) || "N".equals(stat)) {

                if ("G".equals(stat)) {
                    ++totalG;
                } else {
                    ++totalN;
                }

                final RawCsection sect = systemData.getCourseSection(test.course, test.sect, active.term);

                if (sect != null && ("IMP".equals(sect.gradingStd) || "ONL".equals(sect.gradingStd))) {

                    final RawStudent stu = RawStudentLogic.query(cache, test.stuId, false);

                    if (stu != null) {
                        final Record rec = new Record();
                        rec.lastName = stu.lastName;
                        rec.firstName = stu.firstName;
                        rec.studentId = stu.stuId;
                        rec.course = test.course;
                        rec.sect = test.sect;
                        rec.paceOrder = test.paceOrder;
                        rec.openStatus = test.openStatus;
                        rec.grade = test.courseGrade;

                        records.add(rec);
                    }
                }
            }
        }

        if (!records.isEmpty()) {
            Collections.sort(records);

            report.add(CoreConstants.EMPTY);
            report.add("  Name                  Student ID   Course   Section  Order  Open_Status  Grade");
            report.add("  ----                  ----------   ------   -------  -----  -----------  -----");

            final HtmlBuilder htm = new HtmlBuilder(100);

            for (final Record rec : records) {
                htm.add("  ");
                htm.add(rec.lastName, ", ", rec.firstName).truncate(23).padToLength(23);
                htm.add(rec.studentId).padToLength(37);
                htm.add(rec.course).padToLength(48);
                htm.add(rec.sect).padToLength(57);
                htm.add(rec.paceOrder).padToLength(66);
                htm.add(rec.openStatus).padToLength(77);
                htm.add(rec.grade);

                report.add(htm.toString());
                htm.reset();
            }
        }

        report.add(CoreConstants.EMPTY);
        report.add("  # of students with N: " + totalN);
        report.add("  # of students with G: " + totalG);
        report.add("  Total # of special open_status: " + totalG);
    }

    /**
     * A record.
     */
    static class Record implements Comparable<Record> {

        /** The last name. */
        String lastName;

        /** The first name. */
        String firstName;

        /** The student ID. */
        String studentId;

        /** The course. */
        String course;

        /** The section. */
        String sect;

        /** The pace order. */
        Integer paceOrder;

        /** The open status. */
        String openStatus;

        /** The grade. */
        String grade;

        /**
         * Compares two records for order.
         */
        @Override
        public final int compareTo(final Record o) {

            int result;

            if (this.openStatus == null) {
                if (o.openStatus == null) {
                    result = 0;
                } else {
                    result = -1;
                }
            } else if (o.openStatus == null) {
                result = 1;
            } else {
                result = this.openStatus.compareTo(o.openStatus);
            }

            if (result == 0) {
                if (this.lastName == null) {
                    if (o.lastName == null) {
                        result = 0;
                    } else {
                        result = -1;
                    }
                } else if (o.lastName == null) {
                    result = 1;
                } else {
                    result = this.lastName.compareTo(o.lastName);
                }
            }

            if (result == 0) {
                if (this.firstName == null) {
                    if (o.firstName == null) {
                        result = 0;
                    } else {
                        result = -1;
                    }
                } else if (o.firstName == null) {
                    result = 1;
                } else {
                    result = this.firstName.compareTo(o.firstName);
                }
            }

            if (result == 0) {
                if (this.studentId == null) {
                    if (o.studentId == null) {
                        result = 0;
                    } else {
                        result = -1;
                    }
                } else if (o.studentId == null) {
                    result = 1;
                } else {
                    result = this.studentId.compareTo(o.studentId);
                }
            }

            if (result == 0) {
                if (this.sect == null) {
                    if (o.sect == null) {
                        result = 0;
                    } else {
                        result = -1;
                    }
                } else if (o.sect == null) {
                    result = 1;
                } else {
                    result = this.sect.compareTo(o.sect);
                }
            }

            if (result == 0) {
                if (this.course == null) {
                    if (o.course == null) {
                        result = 0;
                    } else {
                        result = -1;
                    }
                } else if (o.course == null) {
                    result = 1;
                } else {
                    result = this.course.compareTo(o.course);
                }
            }

            return result;
        }

        /**
         * Test two objects for equality.
         *
         * @param obj the object against which to compare
         * @return true if this object and {@code obj} are equal
         */
        @Override
        public final boolean equals(final Object obj) {

            return obj instanceof Record && compareTo((Record) obj) == 0;
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        runReport();
    }
}

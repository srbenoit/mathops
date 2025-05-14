package dev.mathops.dbjobs.eos.grading;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.dbjobs.EDebugMode;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a report of students with "special" open status field values.
 */
public class SpecialOpenReport implements Runnable {

    /** The data cache. */
    private final Cache cache;

    /**
     * Constructs a new {@code SpecialOpenReport}.
     *
     * @param theCache the data cache
     */
    private SpecialOpenReport(final Cache theCache) {

        this.cache = theCache;
    }

    /**
     * Runs the process.
     */
    public void run() {

        Log.info("Generating report of students with special open_status flags.....please wait.");

        final SystemData systemData = this.cache.getSystemData();

        try {
            final TermRec activeTerm = systemData.getActiveTerm();

            final List<RawStcourse> specialRegs = gatherSpecialRegs(activeTerm);
            if (specialRegs != null) {
                final int count = specialRegs.size();
                final List<ReportRow> rows = new ArrayList<>(count);
                try {
                    final List<RawCsection> sections = RawCsectionLogic.queryByTerm(this.cache, activeTerm.term);

                    for (final RawStcourse reg : specialRegs) {
                        final ReportRow row = constructRow(reg, sections);
                        if (row != null) {
                            rows.add(row);
                        }
                    }

                    rows.sort(null);

                    generateReport(rows);
                } catch (final SQLException ex) {
                    Log.warning("Failed to query course sections for ", activeTerm.term, ex);
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query the active term.", ex);
        }
    }

    /**
     * Constructs a report row for a registration.
     *
     * @param reg      the registration
     * @param sections the list of sections in the current term
     * @return the report row; null if an error occurred, or the registration is not "relevant"
     */
    private ReportRow constructRow(final RawStcourse reg, final List<RawCsection> sections) {

        ReportRow result = null;

        boolean searching = true;
        for (final RawCsection testSect : sections) {
            if (reg.course.equals(testSect.course) && reg.sect.equals(testSect.sect)
                && ("IMP".equals(testSect.gradingStd) || "ONL".equals(testSect.gradingStd))) {

                try {
                    final RawStudent stu = RawStudentLogic.query(this.cache, reg.stuId, false);
                    if (stu == null) {
                        Log.warning("Student ", reg.stuId, " not found");
                    } else {
                        result = new ReportRow(stu.lastName, stu.firstName, reg.stuId, reg.course, reg.sect,
                                reg.paceOrder, reg.openStatus, reg.courseGrade);
                    }
                } catch (final SQLException ex) {
                    Log.warning("Failed to query student ", reg.stuId, ex);
                }

                searching = false;
                break;
            }
        }

        if (searching) {
            Log.warning("No course section found for ", reg.course, " section ", reg.sect);
        }

        return result;
    }

    /**
     * Gathers the list of current-term registrations that have a "special" open status.
     *
     * @param activeTerm the active term
     * @return the list of registrations; null on failure
     */
    private List<RawStcourse> gatherSpecialRegs(final TermRec activeTerm) {

        List<RawStcourse> result = null;

        try {
            final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(this.cache, activeTerm.term, false, false);
            result = new ArrayList<>(100);

            for (final RawStcourse reg : allRegs) {
                if ("Y".equals(reg.iInProgress)) {
                    continue;
                }

                final String openStatus = reg.openStatus;

                if (("G".equals(openStatus) || "N".equals(openStatus)) && "Y".equals(reg.finalClassRoll)) {
                    result.add(reg);
                }
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query student course records.", ex);
        }

        return result;
    }

    /**
     * Generates the report.
     *
     * @param rows the report rows
     */
    private void generateReport(final List<ReportRow> rows) {

        final LocalDate today = LocalDate.now();
        final String todayStr = TemporalUtils.FMT_MDY.format(today);

        final HtmlBuilder builder = new HtmlBuilder(1000);

        builder.addln("              Students with special open_status flags in STCOURSE");
        builder.addln("                           Report Date: ", todayStr);
        builder.addln();
        builder.addln();

        builder.addln("Name                     Student ID  Course   Section  Order  Open-Status  Grade");
        builder.addln("-----------------------  ----------  -------  -------  -----  -----------  -----");

        int countG = 0;
        int countN = 0;
        for (final ReportRow row : rows) {
            final String lastName = row.lastName();
            final String firstName = row.firstName();
            final String stuId = row.stuId();
            final String course = row.course();
            final String sect = row.sect();
            final String openStatus = row.openStatus();
            final String grade = row.grade();

            final String name = lastName + ", " + firstName;
            appendFixedLen(builder, name, 23);
            builder.add("  ");
            appendFixedLen(builder, stuId, 10);
            builder.add("  ");
            appendFixedLen(builder, course, 7);
            builder.add("  ");
            appendFixedLen(builder, sect, 7);
            builder.add("  ");
            final String orderString = row.paceOrder() == null ? "      " : row.paceOrder().toString();
            appendFixedLen(builder, orderString, 5);
            builder.add("  ");
            appendFixedLen(builder, openStatus, 11);
            builder.addln("  ", grade);

            if ("G".equals(openStatus)) {
                ++countG;
            }
            if ("N".equals(openStatus)) {
                ++countN;
            }
        }

        builder.addln();
        builder.addln();

        final String countGStr = Integer.toString(countG);
        final String countNStr = Integer.toString(countN);
        final int total = countG + countN;
        final String totalStr = Integer.toString(total);

        builder.addln("# of students with N: ", countNStr);
        builder.addln("# of students with G: ", countGStr);
        builder.addln("Total # of special open status: ", totalStr);

        final String reportStr = builder.toString();
        Log.fine(reportStr);
    }

    /**
     * Appends a string to a {@code HtmlBuilder} that is either trimmed to a fixed length or padded with spaces to that
     * fixed length.
     *
     * @param builder the {@code HtmlBuilder} to which to append
     * @param str     the string to append
     * @param len     the fixed length (exactly this many characters will be appended)
     */
    private static void appendFixedLen(final HtmlBuilder builder, final String str, final int len) {

        final int strLen = str.length();
        if (strLen > len) {
            final String trimmed = str.substring(0, len);
            builder.add(trimmed);
        } else {
            builder.add(str);
            for (int i = strLen; i < len; ++i) {
                builder.add(' ');
            }
        }
    }

    /** A report row. */
    private record ReportRow(String lastName, String firstName, String stuId, String course, String sect,
                             Integer paceOrder, String openStatus, String grade) implements Comparable<ReportRow> {

        public ReportRow {
            if (openStatus == null) {
                throw new IllegalArgumentException("Open status may not be null.");
            }
            if (stuId == null) {
                throw new IllegalArgumentException("Student ID may not be null.");
            }
            if (course == null) {
                throw new IllegalArgumentException("Course may not be null.");
            }
            if (sect == null) {
                throw new IllegalArgumentException("Section may not be null.");
            }
        }

        /**
         * Compares two rows for order.
         *
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater
         *         than the specified object
         */
        @Override
        public int compareTo(final ReportRow o) {

            int result = this.openStatus.compareTo(o.openStatus());

            if (result == 0) {
                if (lastName() != null && firstName() != null && o.lastName() != null && o.firstName() != null) {
                    result = this.lastName.compareTo(o.lastName());
                    if (result == 0) {
                        result = this.firstName.compareTo(o.firstName());
                        if (result == 0) {
                            result = this.stuId.compareTo(o.stuId());
                        }
                    }
                } else {
                    result = this.stuId.compareTo(o.stuId());
                }

                if (result == 0) {
                    result = this.sect.compareTo(o.sect());
                    if (result == 0) {
                        result = this.course.compareTo(o.course());
                    }
                }
            }

            return result;
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);
        final Cache cache = new Cache(profile);

        final Runnable obj = new SpecialOpenReport(cache);
        obj.run();
    }
}

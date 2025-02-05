package dev.mathops.dbjobs.report;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Examines number of placement exams taken and outcomes over time, going back several years, to examine trends in
 * placement results. In particular, we are interested in the effects of the pandemic on placement outcomes.
 */
public enum AnnualPlacementStatistics {
    ;

    /** The number of years to include. */
    private static final int YEARS_TO_SCAN = 8;

    /** A date format for use in CSV output. */
    private static final DateTimeFormatter FMT_CSV = DateTimeFormatter.ofPattern("yyyy-MMM-dd", Locale.US);

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
            report.add("     Placement Tool Usage and Outcomes - Trends");
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
     * @throws SQLException if there is an error accessing the database
     */
    private static void generate(final Cache cache, final Collection<? super String> report) throws SQLException {

        // Placement season starts at the end of the Fall term
        final LocalDate today = LocalDate.now();

        // Go backwards YEARS_TO_SCAN years, and then align on a Sunday for the start date, so we
        // can collect placement attempts per week
        LocalDate start = today.minusDays(YEARS_TO_SCAN * 365);
        while (start.getDayOfWeek() != DayOfWeek.SUNDAY) {
            start = start.minusDays(1L);
        }

        final List<LocalDate> weekStarts = new ArrayList<>(YEARS_TO_SCAN * 53 + 1);
        LocalDate weekStart = start;
        while (weekStart.isBefore(today)) {
            weekStarts.add(weekStart);
            weekStart = weekStart.plusDays(7L);
        }

        final int numWeeks = weekStarts.size();

        report.add(CoreConstants.EMPTY);
        report.add("Computing Statistics for " + YEARS_TO_SCAN + " years, beginning "
                   + TemporalUtils.FMT_MDY.format(start) + ".");
        report.add(CoreConstants.EMPTY);

        // Query from the database.

        final List<RawStmpe> allAttempts = RawStmpeLogic.queryAll(cache);
        final List<RawMpeCredit> allCredit = RawMpeCreditLogic.queryAll(cache);

        // Create storage to accumulate attempts by week
        final List<List<PlacementRecord>> byWeek = new ArrayList<>(numWeeks);
        for (int i = 0; i < numWeeks; ++i) {
            byWeek.add(new ArrayList<>(100));
        }

        // Organize attempts by week
        int total = 0;
        for (final RawStmpe attempt : allAttempts) {
            if (attempt.examDt.isBefore(start)) {
                continue;
            }

            // Identify the week - it's the last week-start date that's not after "date"
            for (int i = numWeeks - 1; i >= 0; --i) {
                if (!weekStarts.get(i).isAfter(attempt.examDt)) {
                    byWeek.get(i).add(new PlacementRecord(attempt, allCredit));
                    ++total;
                    break;
                }
            }
        }

        report.add("Total placement attempts: " + total);
        report.add(CoreConstants.EMPTY);

        // Generate weekly and yearly statistics
        final Collection<WeekRecord> weekRecords = new ArrayList<>(numWeeks);

        for (int i = 0; i < numWeeks; ++i) {
            final List<PlacementRecord> records = byWeek.get(i);
            int num100C = 0;
            int num117 = 0;
            int num118 = 0;
            int num124 = 0;
            int num125 = 0;
            int num126 = 0;
            for (final PlacementRecord rec : records) {
                for (final RawMpeCredit credit : rec.credit) {
                    if (RawRecordConstants.M100C.equals(credit.course)) {
                        ++num100C;
                    } else if (RawRecordConstants.M117.equals(credit.course)
                               || RawRecordConstants.MATH117.equals(credit.course)) {
                        ++num117;
                    } else if (RawRecordConstants.M118.equals(credit.course)
                               || RawRecordConstants.MATH118.equals(credit.course)) {
                        ++num118;
                    } else if (RawRecordConstants.M124.equals(credit.course)
                               || RawRecordConstants.MATH124.equals(credit.course)) {
                        ++num124;
                    } else if (RawRecordConstants.M125.equals(credit.course)
                               || RawRecordConstants.MATH125.equals(credit.course)) {
                        ++num125;
                    } else if (RawRecordConstants.M126.equals(credit.course)
                               || RawRecordConstants.MATH126.equals(credit.course)) {
                        ++num126;
                    }
                }
            }

            weekRecords.add(new WeekRecord(weekStarts.get(i), records.size(), num100C, num117, num118, num124, num125,
                    num126));
        }

        final Collection<YearRecord> years = new ArrayList<>(YEARS_TO_SCAN);

        int year = start.getYear();
        int week = 0;
        int all = 0;
        int num100C = 0;
        int num117 = 0;
        int num118 = 0;
        int num124 = 0;
        int num125 = 0;
        int num126 = 0;
        for (final WeekRecord rec : weekRecords) {
            all += rec.numAttempts;
            num100C += rec.num100C;
            num117 += rec.num117;
            num118 += rec.num118;
            num124 += rec.num124;
            num125 += rec.num125;
            num126 += rec.num126;
            ++week;

            if (week == 52) {
                years.add(new YearRecord(year, all, num100C, num117, num118, num124, num125, num126));
                ++year;
                week = 0;
                all = 0;
                num100C = 0;
                num117 = 0;
                num118 = 0;
                num124 = 0;
                num125 = 0;
                num126 = 0;
            }
        }

        report.add("Weekly Statistics:");
        report.add(CoreConstants.EMPTY);

        report.add("  Week             Attempts   101   117   118   124   125   126");
        report.add("  ----             --------   ---   ---   ---   ---   ---   ---");

        final HtmlBuilder htm = new HtmlBuilder(100);
        for (final WeekRecord rec : weekRecords) {
            htm.add("  ");
            htm.add(TemporalUtils.FMT_MDY.format(rec.start));
            htm.padToLength(19);
            htm.add(rec.numAttempts);
            htm.padToLength(30);
            htm.add(rec.num100C);
            htm.padToLength(36);
            htm.add(rec.num117);
            htm.padToLength(42);
            htm.add(rec.num118);
            htm.padToLength(48);
            htm.add(rec.num124);
            htm.padToLength(54);
            htm.add(rec.num125);
            htm.padToLength(60);
            htm.add(rec.num126);

            report.add(htm.toString());
            htm.reset();
        }
        report.add(CoreConstants.EMPTY);
        report.add(CoreConstants.EMPTY);

        report.add("Yearly Statistics:");
        report.add(CoreConstants.EMPTY);

        report.add("  Year        Attempts   101     117     118     124     125     126");
        report.add("  ---------   --------   -----   -----   -----   -----   -----   -----");

        for (final YearRecord rec : years) {
            htm.add("  ");
            htm.add(rec.year).add('-').add(rec.year + 1);
            htm.padToLength(14);
            htm.add(rec.numAttempts);
            htm.padToLength(25);
            htm.add(rec.num100C);
            htm.padToLength(33);
            htm.add(rec.num117);
            htm.padToLength(41);
            htm.add(rec.num118);
            htm.padToLength(49);
            htm.add(rec.num124);
            htm.padToLength(57);
            htm.add(rec.num125);
            htm.padToLength(65);
            htm.add(rec.num126);

            report.add(htm.toString());
            htm.reset();
        }
        report.add(CoreConstants.EMPTY);
        report.add(CoreConstants.EMPTY);

        report.add("Weekly statistics in CSV format:");
        report.add(CoreConstants.EMPTY);

        report.add("Week,Attempts,101,117,118,124,125,126");

        for (final WeekRecord rec : weekRecords) {
            htm.add(FMT_CSV.format(rec.start));
            htm.add(CoreConstants.COMMA_CHAR);
            htm.add(rec.numAttempts);
            htm.add(CoreConstants.COMMA_CHAR);
            htm.add(rec.num100C);
            htm.add(CoreConstants.COMMA_CHAR);
            htm.add(rec.num117);
            htm.add(CoreConstants.COMMA_CHAR);
            htm.add(rec.num118);
            htm.add(CoreConstants.COMMA_CHAR);
            htm.add(rec.num124);
            htm.add(CoreConstants.COMMA_CHAR);
            htm.add(rec.num125);
            htm.add(CoreConstants.COMMA_CHAR);
            htm.add(rec.num126);

            report.add(htm.toString());
            htm.reset();
        }
        report.add(CoreConstants.EMPTY);
        report.add(CoreConstants.EMPTY);

        report.add("Yearly statistics in CSV format:");
        report.add(CoreConstants.EMPTY);

        report.add("Year,Attempts,101,117,118,124,125,126");

        for (final YearRecord rec : years) {
            htm.add(rec.year).add('-').add(rec.year + 1);
            htm.add(CoreConstants.COMMA_CHAR);
            htm.add(rec.numAttempts);
            htm.add(CoreConstants.COMMA_CHAR);
            htm.add(rec.num100C);
            htm.add(CoreConstants.COMMA_CHAR);
            htm.add(rec.num117);
            htm.add(CoreConstants.COMMA_CHAR);
            htm.add(rec.num118);
            htm.add(CoreConstants.COMMA_CHAR);
            htm.add(rec.num124);
            htm.add(CoreConstants.COMMA_CHAR);
            htm.add(rec.num125);
            htm.add(CoreConstants.COMMA_CHAR);
            htm.add(rec.num126);

            report.add(htm.toString());
            htm.reset();
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

    /**
     * A record of a single placement attempt.
     */
    static class PlacementRecord {

        /** Credit earned. */
        final List<RawMpeCredit> credit;

        /**
         * Constructs a new {@code PlacementRecord}.
         *
         * @param theAttempt the placement attempt
         * @param allCredit  the list of all credit records
         */
        PlacementRecord(final RawStmpe theAttempt, final Iterable<RawMpeCredit> allCredit) {

            this.credit = new ArrayList<>(3);

            for (final RawMpeCredit test : allCredit) {
                if (test.stuId.equals(theAttempt.stuId)) {
                    if (test.serialNbr == null || theAttempt.serialNbr == null) {
                        // Old records have no serial number - match on exam date
                        if (test.examDt.equals(theAttempt.examDt)) {
                            this.credit.add(test);
                        }
                    } else if (test.serialNbr.equals(theAttempt.serialNbr)) {
                        this.credit.add(test);
                    }
                }
            }
        }
    }

    /**
     * A record of a single week's outcomes.
     */
    static class WeekRecord {

        /** The week start date. */
        final LocalDate start;

        /** Total number of attempts. */
        final int numAttempts;

        /** Number with 100C credit. */
        final int num100C;

        /** Number with 117 credit. */
        final int num117;

        /** Number with 118 credit. */
        final int num118;

        /** Number with 124 credit. */
        final int num124;

        /** Number with 125 credit. */
        final int num125;

        /** Number with 126 credit. */
        final int num126;

        /**
         * Constructs a new {@code WeekRecord}.
         *
         * @param theStart       the week start date
         * @param theNumAttempts the number of attempts
         * @param theNum100C     the number with 100C credit
         * @param theNum117      the number with 117 credit
         * @param theNum118      the number with 118 credit
         * @param theNum124      the number with 124 credit
         * @param theNum125      the number with 125 credit
         * @param theNum126      the number with 126 credit
         */
        WeekRecord(final LocalDate theStart, final int theNumAttempts, final int theNum100C, final int theNum117,
                   final int theNum118, final int theNum124, final int theNum125, final int theNum126) {

            this.start = theStart;
            this.numAttempts = theNumAttempts;
            this.num100C = theNum100C;
            this.num117 = theNum117;
            this.num118 = theNum118;
            this.num124 = theNum124;
            this.num125 = theNum125;
            this.num126 = theNum126;
        }
    }

    /**
     * A record of a single year's outcomes.
     */
    static class YearRecord {

        /** The year. */
        final int year;

        /** Total number of attempts. */
        final int numAttempts;

        /** Number with 100C credit. */
        final int num100C;

        /** Number with 117 credit. */
        final int num117;

        /** Number with 118 credit. */
        final int num118;

        /** Number with 124 credit. */
        final int num124;

        /** Number with 125 credit. */
        final int num125;

        /** Number with 126 credit. */
        final int num126;

        /**
         * Constructs a new {@code YearRecord}.
         *
         * @param theYear        the year
         * @param theNumAttempts the number of attempts
         * @param theNum100C     the number with 100C credit
         * @param theNum117      the number with 117 credit
         * @param theNum118      the number with 118 credit
         * @param theNum124      the number with 124 credit
         * @param theNum125      the number with 125 credit
         * @param theNum126      the number with 126 credit
         */
        YearRecord(final int theYear, final int theNumAttempts, final int theNum100C, final int theNum117,
                   final int theNum118, final int theNum124, final int theNum125, final int theNum126) {

            this.year = theYear;
            this.numAttempts = theNumAttempts;
            this.num100C = theNum100C;
            this.num117 = theNum117;
            this.num118 = theNum118;
            this.num124 = theNum124;
            this.num125 = theNum125;
            this.num126 = theNum126;
        }
    }
}

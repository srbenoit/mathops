package dev.mathops.dbjobs.report;

import dev.mathops.commons.EqualityTests;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.text.builder.HtmlBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Generates a report that all distinct majors for students enrolled in courses in the active term, with a count of the
 * number of students in that major.
 */
final class CountsByMajor implements Runnable {

    /** Date/time formatter for report. */
    private static final DateTimeFormatter DTTM_FMT = //
            DateTimeFormatter.ofPattern("yyyy'_'MM'_'dd'-'hh'_'mm'_'ss", Locale.US);

    /** Map from program to the count. */
    private final Map<String, Integer> counts;

    /** Map from program prefix to the count. */
    private final Map<String, Integer> aggregate;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private CountsByMajor() {

        this.counts = new TreeMap<>();
        this.aggregate = new TreeMap<>();
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

            Log.info("Generating count of registrations by program.....please wait.");

            compute(cache);
            generateReport(report);
        }

        final LocalDateTime now = LocalDateTime.now();
        final File file = new File("/opt/zircon/reports/Count_by_major_" + DTTM_FMT.format(now) + ".txt");
        try (final FileWriter fw = new FileWriter(file, StandardCharsets.UTF_8)) {
            fw.write(report.toString());
            Log.info("Report complete, written to: ", file.getAbsolutePath());
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Computes summary data based on the registrations and exams queried.
     *
     * @param cache the data cache
     */
    private void compute(final Cache cache) {

        try {
            final TermRec activeTerm = cache.getSystemData().getActiveTerm();
            final List<RawStterm> stterms = RawSttermLogic.queryAllByTerm(cache, activeTerm.term);

            for (final RawStterm stterm : stterms) {
                final RawStudent stu = RawStudentLogic.query(cache, stterm.stuId, false);
                if (stu != null) {
                    final String code = stu.programCode == null || stu.programCode.length() < 4
                            ? "NONE" : stu.programCode;
                    final String prefix = code.substring(0, 4);

                    final Integer currentCount = this.counts.get(code);
                    if (currentCount == null) {
                        this.counts.put(code, Integer.valueOf(1));
                    } else {
                        this.counts.put(code, Integer.valueOf(currentCount.intValue() + 1));
                    }

                    final Integer currentAggregate = this.aggregate.get(prefix);
                    if (currentAggregate == null) {
                        this.aggregate.put(prefix, Integer.valueOf(1));
                    } else {
                        this.aggregate.put(prefix, Integer.valueOf(currentAggregate.intValue() + 1));
                    }
                }
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Generates the report.
     *
     * @param report the {@code HtmlBuilder} to which to write error messages
     */
    private void generateReport(final HtmlBuilder report) {

        final DecimalFormat fmt = new DecimalFormat("#.##");

        int total = 0;
        for (final Integer value : this.counts.values()) {
            total += value.intValue();
        }
        final double totalDouble = total;

        report.addln("Count of students enrolled in 1-credit Precalculus courses by program:").addln();

        for (final Map.Entry<String, Integer> entry : this.counts.entrySet()) {
            final StringBuilder key = new StringBuilder(16);
            key.append(entry.getKey());
            final int toAdd = 16 - key.length();
            key.append(" ".repeat(Math.max(0, toAdd)));

            final Integer count = entry.getValue();
            final double pct = count.doubleValue() * 100.0 / totalDouble;

            report.addln(key.toString(), ": ", count, " (", fmt.format(pct), "%)");
        }

        report.addln().addln("Aggregated counts by program prefix:").addln();

        for (final Map.Entry<String, Integer> entry : this.aggregate.entrySet()) {
            final StringBuilder key = new StringBuilder(16);
            key.append(entry.getKey());
            final int toAdd = 16 - key.length();
            key.append(" ".repeat(Math.max(0, toAdd)));

            final Integer count = entry.getValue();
            final double pct = count.doubleValue() * 100.0 / totalDouble;

            report.addln(key.toString(), ": ", count, " (", fmt.format(pct), "%)");
        }

        report.addln().addln("Aggregated counts sorted by percentage:").addln();

        final Map<String, Integer> sorted = EqualityTests.sortByValue(this.aggregate, true);

        for (final Map.Entry<String, Integer> entry : sorted.entrySet()) {
            final StringBuilder key = new StringBuilder(16);
            key.append(entry.getKey());
            final int toAdd = 16 - key.length();
            key.append(" ".repeat(Math.max(0, toAdd)));

            final Integer count = entry.getValue();
            final double pct = count.doubleValue() * 100.0 / totalDouble;

            report.addln(key.toString(), ": ", count, " (", fmt.format(pct), "%)");
        }
    }

    /**
     * Main method to run the report.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();
        new CountsByMajor().run();
    }
}

package dev.mathops.dbjobs.report.analytics;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.schema.AbstractImpl;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * A report that gathers all current-term enrollments in MATH courses, and prints a summary by major and which semester
 * the student is in.
 */
final class ODSCurrentEnrollmentsAllMath {

    /** Date/time formatter for report. */
    private static final DateTimeFormatter DTTM_FMT =
            DateTimeFormatter.ofPattern("yyyy'_'MM'_'dd'-'hh'_'mm'_'ss", Locale.US);

    /** The database profile through which to access the database. */
    private final Profile profile;

    /** A container for the data about a single course registration. */
    private record RegData(int pidm, String csuId, String subject, String course, String college, String dept,
                           String major, String cls) {
    }

    /**
     * Constructs a new {@code LongitudinalPerformance}.
     */
    private ODSCurrentEnrollmentsAllMath() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     */
    private void execute() {

        final Collection<String> report = new ArrayList<>(200);
        final Collection<String> csv = new ArrayList<>(200);

        if (this.profile == null) {
            Log.warning("Unable to create production profile.");
        } else {
            generateReport(report, csv);
        }

        final HtmlBuilder reportContent = new HtmlBuilder(10000);
        for (final String s : report) {
            reportContent.addln(s);
        }
        final HtmlBuilder csvContent = new HtmlBuilder(10000);
        for (final String s : csv) {
            csvContent.addln(s);
        }

        final LocalDateTime now = LocalDateTime.now();
        final String nowStr = DTTM_FMT.format(now);

        final File reportFile = new File("/opt/zircon/reports/AllMathByDept" + nowStr + ".txt");
        try (final FileWriter fw = new FileWriter(reportFile, StandardCharsets.UTF_8)) {
            fw.write(reportContent.toString());
            Log.info("Report complete, written to: ", reportFile.getAbsolutePath());
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        final File csvFile = new File("/opt/zircon/reports/AllMathByDept" + nowStr + ".csv");
        try (final FileWriter fw = new FileWriter(csvFile, StandardCharsets.UTF_8)) {
            fw.write(csvContent.toString());
            Log.info("CSV complete, written to: ", csvFile.getAbsolutePath());
        } catch (final IOException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Queries data from Banner and generates the report.
     *
     * @param report a list of strings to which to add report output lines
     * @param csv    a list of strings to which to add CSV report lines
     */
    private void generateReport(final Collection<? super String> report, final Collection<? super String> csv) {

        final LocalDate today = LocalDate.now();
        final String dateStr = TemporalUtils.FMT_MDY.format(today);
        final String reportHeader = SimpleBuilder.concat("MATH Enrollment Report as of ", dateStr);

        report.add(reportHeader);

        final Login login = this.profile.getLogin(ESchema.ODS);

        final DbConnection odsConn = login.checkOutConnection();

        try {
            final List<RegData> regs = new ArrayList<>(3500);

            queryODS(odsConn, regs);

            report.add("  There are " + regs.size() + " total registrations in MATH courses.");
            report.add(CoreConstants.EMPTY);

            processList(regs, report, csv);

            report.add(CoreConstants.EMPTY);
            report.add("Job completed");

        } catch (final SQLException ex) {
            Log.warning(ex);
            report.add("Unable to perform query: " + ex.getMessage());
        } finally {
            login.checkInConnection(odsConn);
        }
    }

    /**
     * Queries registration records from the ODS.
     *
     * @param bannerConn the database connection to the Live (Banner) database
     * @param regs       a list to which to add student course record
     * @throws SQLException if there is an error performing the query
     */
    private static void queryODS(final DbConnection bannerConn, final Collection<? super RegData> regs)
            throws SQLException {

        Log.info("Querying ODS...");

        try (final Statement stmt = bannerConn.createStatement()) {

            final String regSql = SimpleBuilder.concat(
                    "SELECT CSUS_SECTION_INFO_CUR.PERSON_UID pidm,",
                    "       CSUS_SECTION_INFO_CUR.ID csuid,",
                    "       CSUS_SECTION_INFO_CUR.SUBJECT subject,",
                    "       CSUS_SECTION_INFO_CUR.COURSE_IDENTIFICATION course,",
                    "       CSUS_TERM_INFO_CUR.PRIMARY_COLLEGE_DESC college,",
                    "       CSUS_TERM_INFO_CUR.PRIMARY_DEPARTMENT_DESC dept,",
                    "       CSUS_TERM_INFO_CUR.PRIMARY_MAJOR major,",
                    "       CSUS_TERM_INFO_CUR.STUDENT_CLASS cls",
                    " FROM CSUS_SECTION_INFO_CUR, CSUS_TERM_INFO_CUR ",
                    "WHERE CSUS_SECTION_INFO_CUR.SUBJECT='MATH' ",
                    "  AND CSUS_SECTION_INFO_CUR.PERSON_UID=CSUS_TERM_INFO_CUR.PIDM",
                    "  AND CSUS_TERM_INFO_CUR.STUDENT_LEVEL='UG'");

            try (final ResultSet rs = stmt.executeQuery(regSql)) {
                while (rs.next()) {

                    final int pidm = rs.getInt("pidm");
                    final String csuId = rs.getString("csuid");
                    final String subject = AbstractImpl.getString(rs, "subject");
                    final String course = rs.getString("course");
                    final String college = rs.getString("college");
                    final String dept = rs.getString("dept");
                    final String major = rs.getString("major");
                    final String cls = rs.getString("cls");

                    final RegData regRec = new RegData(pidm, csuId, subject, course, college, dept, major, cls);
                    regs.add(regRec);
                }
            }
        }
    }

    /**
     * Processes a list of registration records to generate report output.
     *
     * @param regs   the list of registration records
     * @param report the report to which to append
     * @param csv    the CSV file to which to append
     */
    private void processList(final List<RegData> regs, final Collection<? super String> report,
                             final Collection<? super String> csv) {

        // First, organize by course number
        final Map<String, List<RegData>> courseRegs = new TreeMap<>();

        for (final RegData reg : regs) {
            final String courseId = reg.course;
            final List<RegData> list = courseRegs.computeIfAbsent(courseId, s -> new ArrayList<>(100));
            list.add(reg);
        }

        csv.add("Course,College,Department,Total Enrollments,Freshmen,Sophomore,Junior,Senior,Other");

        final Map<String, List<RegData>> collegeRegs = new TreeMap<>();
        final Map<String, List<RegData>> deptRegs = new TreeMap<>();

        for (final Map.Entry<String, List<RegData>> courseEntry : courseRegs.entrySet()) {
            final String key = courseEntry.getKey();
            if (key.length() > 4) {
                final String courseId = key.substring(0, 4) + CoreConstants.SPC + key.substring(4);

                final List<RegData> courseRows = courseEntry.getValue();
                final int count = courseRows.size();
                final String countStr = Integer.toString(count);
                report.add(SimpleBuilder.concat("  ", courseId, ": ", countStr, " total undergraduate enrollments"));

                // Organize by College
                collegeRegs.clear();
                for (final RegData reg : courseRows) {
                    final String college = reg.college;
                    final List<RegData> list = collegeRegs.computeIfAbsent(college, s -> new ArrayList<>(50));
                    list.add(reg);
                }

                for (final Map.Entry<String, List<RegData>> collegeEntry : collegeRegs.entrySet()) {
                    final List<RegData> collegeRows = collegeEntry.getValue();

                    int num1 = 0;
                    int num2 = 0;
                    int num3 = 0;
                    int num4 = 0;
                    for (final RegData reg : collegeRows) {
                        final char ch = reg.cls.charAt(0);
                        if (ch == '1') {
                            ++num1;
                        } else if (ch == '2') {
                            ++num2;
                        } else if (ch == '3') {
                            ++num3;
                        } else if (ch == '4') {
                            ++num4;
                        }
                    }

                    final int collegeCount = collegeRows.size();
                    final String collegeCountStr = Integer.toString(collegeCount);
                    final String coll = collegeEntry.getKey();
                    report.add(SimpleBuilder.concat("      College [", coll, "] ", collegeCountStr,
                            " enrollments (" + num1 + " Fr., " + num2 + " So., " + num3 + " Jr., " + num4
                            + " Sr.)"));

                    // Organize by Department
                    deptRegs.clear();
                    for (final RegData reg : collegeRows) {
                        final String dept = reg.dept;
                        final List<RegData> list = deptRegs.computeIfAbsent(dept, s -> new ArrayList<>(50));
                        list.add(reg);
                    }

                    for (final Map.Entry<String, List<RegData>> deptEntry : deptRegs.entrySet()) {
                        final List<RegData> deptRows = deptEntry.getValue();

                        int numd1 = 0;
                        int numd2 = 0;
                        int numd3 = 0;
                        int numd4 = 0;
                        for (final RegData reg : deptRows) {
                            final char ch = reg.cls.charAt(0);
                            if (ch == '1') {
                                ++numd1;
                            } else if (ch == '2') {
                                ++numd2;
                            } else if (ch == '3') {
                                ++numd3;
                            } else if (ch == '4') {
                                ++numd4;
                            }
                        }

                        final int deptCount = deptRows.size();
                        final String deptCountStr = Integer.toString(deptCount);
                        final String dept = deptEntry.getKey();
                        report.add(SimpleBuilder.concat("          Dept [", dept, "] ", deptCountStr,
                                " enrollments (" + numd1 + " Fr., " + numd2 + " So., " + numd3 + " Jr., " + numd4
                                + " Sr.)"));

                        final String cleaned = dept.replace(",", "");

                        csv.add(SimpleBuilder.concat(courseId, CoreConstants.COMMA, coll, CoreConstants.COMMA,
                                cleaned, CoreConstants.COMMA, deptCountStr, CoreConstants.COMMA,
                                Integer.toString(numd1), CoreConstants.COMMA,
                                Integer.toString(numd2), CoreConstants.COMMA,
                                Integer.toString(numd3), CoreConstants.COMMA,
                                Integer.toString(numd4)));
                    }
                }
            }
        }
    }

    /**
     * Main method to run the job.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final ODSCurrentEnrollmentsAllMath job = new ODSCurrentEnrollmentsAllMath();
        job.execute();
    }
}

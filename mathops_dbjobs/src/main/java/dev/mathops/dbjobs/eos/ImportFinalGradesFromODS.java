package dev.mathops.dbjobs.eos;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.RawGradeRollLogic;
import dev.mathops.db.old.rawrecord.RawGradeRoll;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import dev.mathops.dbjobs.EDebugMode;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Imports the final "rolled" grades from the ODS and inserts them into the GRADE_ROLL table in PROD for statistics
 * generation at the end of the term.
 *
 * <p>
 * This queries for the "prior" term, so it should be run after PROD has been rolled over.
 */
public class ImportFinalGradesFromODS implements Runnable {

    /** Flag to run in "debug" mode which prints changes that would be performed rather than performing any changes. */
    private static final EDebugMode DEBUG_MODE = EDebugMode.DEBUG;

    /** The data cache. */
    private final Cache cache;

    /**
     * Constructs a new {@code ImportFinalGradesFromODS}.
     *
     * @param theCache the data cache
     */
    private ImportFinalGradesFromODS(final Cache theCache) {

        this.cache = theCache;
    }

    /**
     * Executes the job.
     */
    public void run() {

        final Collection<String> report = new ArrayList<>(10);

        final Profile profile = this.cache.getProfile();
        final Login odsLogin = profile.getFacet(ESchema.ODS).login;

        if (odsLogin == null) {
            report.add("Unable to obtain ODS database login.");
        } else {
            final SystemData systemData = this.cache.getSystemData();

            try {
                final TermRec priorTerm = systemData.getPriorTerm();

                final List<GradeRecord> list;

                if (priorTerm == null) {
                    report.add("Failed to query the prior term.");
                } else {
                    final DbConnection odsConn = odsLogin.checkOutConnection();
                    try {
                        report.add("Processing for the " + priorTerm.term.longString + " term");
                        list = queryOds(odsConn, priorTerm.term, report);

                        final int count = list.size();
                        report.add("Found " + count + " rows.");
                        processList(this.cache, priorTerm.term, list, report);
                        report.add("Job completed");
                    } finally {
                        odsLogin.checkInConnection(odsConn);
                    }
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                report.add("Unable to obtain connection to ODS database");
            }
        }

        final HtmlBuilder htm = new HtmlBuilder(1000);
        for (final String rep : report) {
            htm.addln(rep);
        }

        final String reportStr = htm.toString();
        Log.fine(reportStr);
    }

    /**
     * Queries transfer records from the ODS for the Spring semester.
     *
     * @param conn   the database connection
     * @param term   the term for which to query
     * @param report a list to which to add report lines
     * @return a list of transfer records
     * @throws SQLException if there is an error performing the query
     */
    private static List<GradeRecord> queryOds(final DbConnection conn, final TermKey term,
                                              final Collection<? super String> report) throws SQLException {

        final String termId;

        if (term.name == ETermName.SPRING) {
            termId = term.year.toString() + "10";
        } else if (term.name == ETermName.SUMMER) {
            termId = term.year.toString() + "60";
        } else {
            termId = term.year.toString() + "90";
        }

        final List<GradeRecord> result = new ArrayList<>(1000);

        try (final Statement stmt = conn.createStatement()) {

            final String sql = SimpleBuilder.concat(
                    "SELECT ID, COURSE_IDENTIFICATION, COURSE_SECTION_NUMBER, ",
                    "       NAME, FINAL_GRADE, ACADEMIC_PERIOD ",
                    "  FROM ODSMGR.STUDENT_COURSE ",
                    " WHERE (COURSE_IDENTIFICATION='MATH117'",
                    "      OR COURSE_IDENTIFICATION='MATH118'",
                    "      OR COURSE_IDENTIFICATION='MATH124'",
                    "      OR COURSE_IDENTIFICATION='MATH125'",
                    "      OR COURSE_IDENTIFICATION='MATH126')",
                    "    AND (COURSE_SECTION_NUMBER='001'",
                    "      OR COURSE_SECTION_NUMBER='002'",
                    "      OR COURSE_SECTION_NUMBER='003'",
                    "      OR COURSE_SECTION_NUMBER='004'",
                    "      OR COURSE_SECTION_NUMBER='005'",
                    "      OR COURSE_SECTION_NUMBER='006'",
                    "      OR COURSE_SECTION_NUMBER='007'",
                    "      OR COURSE_SECTION_NUMBER='008'",
                    "      OR COURSE_SECTION_NUMBER='009'",
                    "      OR COURSE_SECTION_NUMBER='550'",
                    "      OR COURSE_SECTION_NUMBER='801'",
                    "      OR COURSE_SECTION_NUMBER='809'",
                    "      OR COURSE_SECTION_NUMBER='401')",
                    "    AND ACADEMIC_PERIOD='" + termId + "'");

            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final String stuId = rs.getString("ID");
                    final String course = rs.getString("COURSE_IDENTIFICATION");
                    final String sect = rs.getString("COURSE_SECTION_NUMBER");
                    final String name = rs.getString("NAME");
                    final String grade = rs.getString("FINAL_GRADE");

                    // Null grade indicates Univ. withdrawal, so disregard
                    if (grade != null) {
                        if (stuId == null) {
                            report.add("ODS record had null student ID");
                        } else if (course == null) {
                            report.add("ODS record had null course");
                        } else if (sect == null) {
                            report.add("ODS record had null sect");
                        } else if (stuId.length() == 9) {
                            if (course.length() == 7) {
                                result.add(new GradeRecord(stuId, course, sect, name, grade, term));
                            } else {
                                report.add("ODS record had bad course: '" + course + "'");
                            }
                        } else {
                            report.add("ODS record had bad student ID: '" + stuId + "'");
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Processes a list of transfer records.
     *
     * @param cache  the data cache
     * @param term   the term
     * @param list   the list
     * @param report a list to which to add report lines
     * @throws SQLException if there is an error accessing the database
     */
    private static void processList(final Cache cache, final TermKey term, final Iterable<? extends GradeRecord> list,
                                    final Collection<? super String> report) throws SQLException {

        int count = 0;

        final List<RawGradeRoll> existings = RawGradeRollLogic.queryByTerm(cache, term);

        for (final GradeRecord rec : list) {
            final String stu = rec.stuId;
            final String cid = rec.course.replace("MATH", "M ");
            final String sect = rec.sect;

            boolean searching = true;
            final Iterator<RawGradeRoll> iter = existings.iterator();
            while (iter.hasNext()) {
                final RawGradeRoll existing = iter.next();
                if (existing.stuId.equals(stu) && existing.course.equals(cid) && existing.sect.equals(sect)) {
                    searching = false;
                    iter.remove();
                    break;
                }
            }

            if (searching) {
                report.add("Inserting record for " + stu + CoreConstants.SLASH + cid + " (" + sect + ") - "
                           + rec.grade + " for " + rec.name);

                if (DEBUG_MODE == EDebugMode.NORMAL) {
                    final RawGradeRoll toInsert = new RawGradeRoll(rec.term, stu, cid, sect, rec.name, rec.grade);

                    if (RawGradeRollLogic.insert(cache, toInsert)) {
                        ++count;
                    } else {
                        report.add("Insert failed");
                    }
                }
            }
        }

        report.add("Inserted " + count + " records");

        count = 0;
        if (!existings.isEmpty()) {
            report.add("Warning: There were " + existings.size() + " records in grade_roll that were not in ODS:");

            for (final RawGradeRoll row : existings) {
                report.add("    Deleting record for " + row.stuId + CoreConstants.SLASH + row.course + " ("
                           + row.sect + ") - " + row.gradeOpt + " for " + row.fullname + " in " + row.termKey.shortString);

                if (DEBUG_MODE == EDebugMode.NORMAL) {
                    if (RawGradeRollLogic.delete(cache, row)) {
                        ++count;
                    } else {
                        report.add("Delete failed");
                    }
                }
            }
        }

        report.add("Deleted " + count + " records");
    }

    /**
     * A grade record.
     */
    private static class GradeRecord {

        /** The student ID. */
        final String stuId;

        /** The course ID. */
        final String course;

        /** The section number. */
        final String sect;

        /** The student name. */
        final String name;

        /** The earned grade. */
        final String grade;

        /** The term. */
        final TermKey term;

        /**
         * Constructs a new {@code GradeRecord}.
         *
         * @param theStuId  the student ID
         * @param theCourse the course
         * @param theSect   the section number
         * @param theName   the student name
         * @param theGrade  the grade
         * @param theTerm   the term
         */
        GradeRecord(final String theStuId, final String theCourse, final String theSect,
                    final String theName, final String theGrade, final TermKey theTerm) {

            this.stuId = theStuId;
            this.course = theCourse;
            this.sect = theSect;
            this.name = theName;
            this.grade = theGrade;
            this.term = theTerm;
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

        final Runnable obj = new ImportFinalGradesFromODS(cache);
        obj.run();
    }
}

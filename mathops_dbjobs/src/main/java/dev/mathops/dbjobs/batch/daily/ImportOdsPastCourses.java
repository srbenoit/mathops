package dev.mathops.dbjobs.batch.daily;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawFfrTrnsLogic;
import dev.mathops.db.old.rawrecord.RawFfrTrns;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A class that performs an import of past course registrations that can clear Precalculus prerequisites.
 */
public final class ImportOdsPastCourses {

    /** The database profile through which to access the database. */
    private final DbProfile dbProfile;

    /** The database context. */
    private final DbContext odsCtx;

    /** The database context. */
    private final DbContext primaryCtx;

    /**
     * Constructs a new {@code ImportOdsPastCourses}.
     */
    public ImportOdsPastCourses() {

        final ContextMap map = ContextMap.getDefaultInstance();

        this.dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        this.odsCtx = this.dbProfile.getDbContext(ESchemaUse.ODS);
        this.primaryCtx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
    }

    /**
     * Executes the job.
     *
     * @return the report
     */
    public String execute() {

        final Collection<String> report = new ArrayList<>(10);

        if (this.dbProfile == null) {
            report.add("Unable to create production context.");
        } else if (this.odsCtx == null) {
            report.add("Unable to create ODS database context.");
        } else {
            try {
                final DbConnection primaryConn = this.primaryCtx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, primaryConn);

                try {
                    final DbConnection odsConn = this.odsCtx.checkOutConnection();

                    try {
                        report.add("Processing");
                        final List<TransferRecord> list = queryOds(odsConn, report);

                        report.add("Found " + list.size() + " rows.");
                        processList(cache, list, report);
                        report.add("Job completed");

                    } catch (final SQLException ex) {
                        Log.warning(ex);
                        report.add("Unable to perform query");
                    } finally {
                        this.odsCtx.checkInConnection(odsConn);
                    }
                } finally {
                    this.primaryCtx.checkInConnection(primaryConn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                report.add("Unable to obtain connection to ODS database");
            }
        }

        final HtmlBuilder htm = new HtmlBuilder(1000);
        htm.addln("<pre>");
        for (final String rep : report) {
            htm.addln(rep);
        }
        htm.addln("</pre>");

        return htm.toString();
    }

    /**
     * Queries transfer records from the ODS for the Spring semester.
     *
     * @param conn   the database connection
     * @param report a list to which to add report lines
     * @return a list of transfer records
     * @throws SQLException if there is an error performing the query
     */
    private static List<TransferRecord> queryOds(final DbConnection conn,
                                                 final Collection<? super String> report) throws SQLException {

        final List<TransferRecord> result = new ArrayList<>(1000);

        try (final Statement stmt = conn.createStatement()) {

            final String sql = "SELECT ID, COURSE_IDENTIFICATION, FINAL_GRADE FROM ODSMGR.STUDENT_COURSE "
                    + " WHERE (COURSE_IDENTIFICATION='MATH120' OR COURSE_IDENTIFICATION='MATH127')"
                    + "   AND (FINAL_GRADE = 'A' OR FINAL_GRADE = 'B' OR FINAL_GRADE = 'C' OR"
                    + "        FINAL_GRADE = 'D' OR FINAL_GRADE = 'S')";

            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final String stuId = rs.getString("ID");
                    final String course = rs.getString("COURSE_IDENTIFICATION");

                    if (stuId == null) {
                        report.add("ODS record had null student ID");
                    } else if (stuId.length() == 9) {
                        result.add(new TransferRecord(stuId, course));
                    } else {
                        report.add("ODS record had bad student ID: '" + stuId + "'");
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
     * @param list   the list
     * @param report a list to which to add report lines
     * @throws SQLException if there is an error accessing the database
     */
    private static void processList(final Cache cache, final Iterable<TransferRecord> list,
                                    final Collection<? super String> report) throws SQLException {

        final LocalDate now = LocalDate.now();
        int count = 0;

        for (final TransferRecord rec : list) {
            final String stu = rec.getStuId();
            final String cid = rec.getCourse().replace("MATH", "M ");

            final List<RawFfrTrns> existings = RawFfrTrnsLogic.queryByStudent(cache, stu);

            boolean searching = true;
            for (final RawFfrTrns existing : existings) {
                if (existing.course.equals(cid)) {
                    searching = false;
                    break;
                }
            }

            if (searching) {
                report.add("Inserting record for " + stu + CoreConstants.SLASH + cid + " - " + now);

                final RawFfrTrns toInsert = new RawFfrTrns(stu, cid, "C", now, null);

                if (RawFfrTrnsLogic.INSTANCE.insert(cache, toInsert)) {
                    ++count;
                } else {
                    report.add("Insert failed");
                }
            }
        }

        report.add("Inserted " + count + " records");
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final ImportOdsPastCourses job = new ImportOdsPastCourses();

        Log.fine(job.execute());
    }

    /**
     * A transfer record.
     */
    private static final class TransferRecord {

        /** The student ID. */
        private final String stuId;

        /** The course ID. */
        private final String course;

        /**
         * Constructs a new {@code TransferRecord}.
         *
         * @param theStuId  the student ID
         * @param theCourse the course
         */
        TransferRecord(final String theStuId, final String theCourse) {

            this.stuId = theStuId;
            this.course = theCourse;
        }

        /**
         * Gets the student ID.
         *
         * @return the student ID
         */
        String getStuId() {

            return this.stuId;
        }

        /**
         * Gets the course ID.
         *
         * @return the course ID
         */
        String getCourse() {

            return this.course;
        }
    }
}

package dev.mathops.dbjobs.batch.daily;

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
import dev.mathops.db.old.rawlogic.RawFfrTrnsLogic;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.rec.TermRec;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A class that performs an import of transfer credit data from the ODS.
 */
public final class ImportOdsTransferCredit {

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code ImportOdsTransferCredit}.
     */
    public ImportOdsTransferCredit() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     *
     * @return the report
     */
    public String execute() {

        final Collection<String> report = new ArrayList<>(10);

        if (this.profile == null) {
            report.add("Unable to create production profile.");
        } else {
            final Cache cache = new Cache(this.profile);

            try {
                final TermRec active = cache.getSystemData().getActiveTerm();

                final Login odsLogin = this.profile.getLogin(ESchema.ODS);
                final DbConnection odsConn = odsLogin.checkOutConnection();

                try {
                    List<TransferRecord> list = null;

                    if (active == null) {
                        report.add("Failed to query the active term.");
                    } else if (active.term.name == ETermName.SPRING) {
                        report.add("Processing under the SPRING term");
                        list = queryOdsSpring(odsConn, report);
                    } else if (active.term.name == ETermName.SUMMER) {
                        report.add("Processing under the SUMMER term");
                        list = queryOdsSummer(odsConn, report);
                    } else if (active.term.name == ETermName.FALL) {
                        report.add("Processing under the FALL term");
                        list = queryOdsFall(odsConn, report);
                    } else {
                        report.add("Active term has invalid term name:" + active.term.name);
                    }

                    if (list != null) {
                        report.add("Found " + list.size() + " rows.");
                        processList(cache, list, report);
                        report.add("Job completed");
                    }

                } catch (final SQLException ex) {
                    Log.warning(ex);
                    report.add("Unable to perform query");
                } finally {
                    odsLogin.checkInConnection(odsConn);
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
    private static List<TransferRecord> queryOdsSpring(final DbConnection conn,
                                                       final Collection<? super String> report) throws SQLException {

        final List<TransferRecord> result = new ArrayList<>(1000);

        try (final Statement stmt = conn.createStatement()) {

            final String sql = "SELECT A.ID x, B.COURSE_IDENTIFICATION y "
                               + "FROM CSUBAN.CSUS_SECTION_INFO_SPR A, ODSMGR.STUDENT_COURSE B "
                               + "WHERE A.PERSON_UID = B.PERSON_UID "
                               + " AND ((B.COURSE_IDENTIFICATION='MATH002'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH117'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH118'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH120'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH124'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH125'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH126'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH127'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH141'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH155'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH156'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH157'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH160'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH161'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH229')"
                               + "  AND (A.COURSE_IDENTIFICATION='MATH002'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH117'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH118'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH120'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH124'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH125'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH126'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH127')"
                               + "  AND (B.TRANSFER_COURSE_IND='Y'))";

            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final String stuId = rs.getString("x");
                    final String course = rs.getString("y");

                    if (stuId == null) {
                        report.add("ODS record had null student ID");
                    } else if (course == null) {
                        report.add("ODS record had null course");
                    } else if (stuId.length() == 9) {
                        if (course.length() == 7) {
                            result.add(new TransferRecord(stuId, course));
                        } else {
                            report.add("ODS record had bad course: '" + course + "'");
                        }
                    } else {
                        report.add("ODS record had bad student ID: '" + stuId + "'");
                    }
                }
            }
        }

        return result;
    }

    /**
     * Queries transfer records from the ODS for the Summer semester.
     *
     * @param conn   the database connection
     * @param report a list to which to add report lines
     * @return a list of transfer records
     * @throws SQLException if there is an error performing the query
     */
    private static List<TransferRecord> queryOdsSummer(final DbConnection conn,
                                                       final Collection<? super String> report) throws SQLException {

        final List<TransferRecord> result = new ArrayList<>(1000);

        try (final Statement stmt = conn.createStatement()) {

            final String sql = "SELECT A.ID x, B.COURSE_IDENTIFICATION y "
                               + "FROM CSUBAN.CSUS_SECTION_INFO_SMR A, ODSMGR.STUDENT_COURSE B "
                               + "WHERE A.PERSON_UID = B.PERSON_UID "
                               + " AND ((B.COURSE_IDENTIFICATION='MATH002'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH117'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH118'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH120'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH124'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH125'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH126'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH127'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH141'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH155'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH156'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH157'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH160'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH161'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH229')"
                               + "  AND (A.COURSE_IDENTIFICATION='MATH117'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH118'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH120'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH124'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH125'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH126'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH127')"
                               + "  AND (B.TRANSFER_COURSE_IND='Y'))";

            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final String stuId = rs.getString("x");
                    final String course = rs.getString("y");

                    if (stuId == null) {
                        report.add("ODS record had null student ID");
                    } else if (course == null) {
                        report.add("ODS record had null course");
                    } else if (stuId.length() == 9) {
                        if (course.length() == 7) {
                            result.add(new TransferRecord(stuId, course));
                        } else {
                            report.add("ODS record had bad course: '" + course + "'");
                        }
                    } else {
                        report.add("ODS record had bad student ID: '" + stuId + "'");
                    }
                }
            }
        }

        return result;
    }

    /**
     * Queries transfer records from the ODS for the Fall semester.
     *
     * @param conn   the database connection
     * @param report a list to which to add report lines
     * @return a list of transfer records
     * @throws SQLException if there is an error performing the query
     */
    private static List<TransferRecord> queryOdsFall(final DbConnection conn,
                                                     final Collection<? super String> report) throws SQLException {

        final List<TransferRecord> result = new ArrayList<>(1000);

        try (final Statement stmt = conn.createStatement()) {

            final String sql = "SELECT A.ID x, B.COURSE_IDENTIFICATION y "
                               + "FROM CSUBAN.CSUS_SECTION_INFO_FAL A, ODSMGR.STUDENT_COURSE B "
                               + "WHERE A.PERSON_UID = B.PERSON_UID "
                               + " AND ((B.COURSE_IDENTIFICATION='MATH002'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH117'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH118'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH120'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH124'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH125'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH126'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH127'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH141'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH155'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH156'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH157'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH160'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH161'"
                               + "    OR B.COURSE_IDENTIFICATION='MATH229')"
                               + "  AND (A.COURSE_IDENTIFICATION='MATH117'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH118'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH120'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH124'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH125'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH126'"
                               + "    OR A.COURSE_IDENTIFICATION='MATH127')"
                               + "  AND (B.TRANSFER_COURSE_IND='Y'))";

            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final String stuId = rs.getString("x");
                    final String course = rs.getString("y");

                    if (stuId == null) {
                        report.add("ODS record had null student ID");
                    } else if (course == null) {
                        report.add("ODS record had null course");
                    } else if (stuId.length() == 9) {
                        if (course.length() == 7) {
                            result.add(new TransferRecord(stuId, course));
                        } else {
                            report.add("ODS record had bad course: '" + course + "'");
                        }
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

                final RawFfrTrns toInsert = new RawFfrTrns(stu, cid, "T", now, null);

                if (RawFfrTrnsLogic.insert(cache, toInsert)) {
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

        final ImportOdsTransferCredit job = new ImportOdsTransferCredit();

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

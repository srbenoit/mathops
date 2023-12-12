package dev.mathops.dbjobs.batch.daily;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple class that connects to the ODS system and queries metadata for the views we use.
 */
final class DumpODSSchema {

    /** The database profile through which to access the database. */
    private final DbProfile dbProfile;

    /** The ODS database context. */
    private final DbContext odsCtx;

    /**
     * Constructs a new {@code DumpODSSchema}.
     */
    private DumpODSSchema() {

        final ContextMap map = ContextMap.getDefaultInstance();

        this.dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        this.odsCtx = this.dbProfile.getDbContext(ESchemaUse.ODS);
    }

    /**
     * Executes the job.
     */
    private void execute() {

        if (this.dbProfile == null) {
            Log.warning("Unable to create production context.");
        } else if (this.odsCtx == null) {
            Log.warning("Unable to create ODS database context.");
        } else {
            try {
                final DbConnection conn = this.odsCtx.checkOutConnection();

                try {
                    dumpMetadata(conn);
                    // debugQuery(conn);
                } finally {
                    this.odsCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                Log.warning("Unable to obtain connection to ODS database");
            }
        }
    }

//    /**
//     * Performs a debugging query to see why we get back many rows for applicant student query.
//     *
//     * @param conn the ODS connection
//     * @throws SQLException if there is an error accessing the database
//     */
//     private static void debugQuery(final DbConnection conn) throws SQLException {
//
//     // This query diagnoses why we get multiple records for students in the
//     // ODS applicant import - it appears to be for multiple applications.
//
//     final int curYear = LocalDate.now().getYear();
//     final String start = Integer.toString(curYear - 1) + "90";
//     final String end = Integer.toString(curYear + 2) + "90";
//
//     final String sql1 = "SELECT "
//     + " A.CSU_ID csuid, "
//     + " A.FIRST_NAME first, "
//     + " A.LAST_NAME last, "
//     + " A.PREFERRED_FIRST_NAME pref, "
//     + " A.MIDDLE_NAME middle, "
//     + " A.BIRTH_DATE bday, "
//     + " A.GENDER gender, "
//     + " A.RESIDENCY_STATE ressate, "
//     + " A.HS_CODE hscode, "
//     + " A.HS_GPA hsgpa, "
//     + " A.HS_CLASS_RANK hsrank, "
//     + " A.HS_CLASS_SIZE hssize, "
//     + " A.ACT_MATH act, "
//     + " A.SAT_MATH sat, "
//     + " A.SATR_MATH satr, "
//     + " A.EMAIL email "
//     + "FROM CSUBAN.CSUG_GP_ADMISSIONS A "
//     + "WHERE A.CSU_ID = '831645739' AND A.LAST_NAME Not Like '-Purge%'";
//
//     try (Statement stmt = conn.createStatement()) {
//     try (ResultSet rs = stmt.executeQuery(sql1)) {
//     while (rs.next()) {
//     Log.info("Found a CSUBAN.CSUG_GP_ADMISSIONS record");
//     }
//     }
//     }
//
//     final String sql2 = "SELECT "
//     + " B.PIDM pidm, "
//     + " B.TERM appterm, "
//     + " B.APLCT_LATEST_DECN decision, "
//     + " B.ADMITTED_FLAG admitted, "
//     + " B.ADM_TYPE admType, "
//     + " B.ADM_COLLEGE admCollege, "
//     + " B.ADM_DEPT admDept, "
//     + " B.ADM_PROGRAM_OF_STUDY admProgram, "
//     + " B.ADM_CAMPUS admCampus, "
//     + " B.ADM_RESIDENCY admResidency, "
//     + " B.APLN_DATE aplnDate "
//
//     + "FROM CSUBAN.CSUS_APPLICANT B "
//     + "WHERE B.CSU_ID = '831645739' AND (B.APLN_STATUS <> 'U') "
//     + " AND (B.APLCT_LATEST_DECN IS NULL OR B.APLCT_LATEST_DECN <> 'RA') "
//     + " AND (B.APLN_COUNT_PRIORITY_FLAG = 'Y') "
//     + " AND (B.TERM Between '" + start
//     + "' And '" + end + "') " 
//     + " AND ( (B.ADM_CAMPUS = 'MC')"
//     + " OR ((B.STUDENT_LEVEL = 'GR') And (B.STUDENT_TYPE = 'N')) "
//     + " OR ((B.STUDENT_LEVEL = 'UG') And (B.STUDENT_TYPE In ('N','T','R'))) "
//     + " OR ((B.STUDENT_LEVEL In ('UG','GR')) And (B.STUDENT_TYPE = 'E') "
//     + " And (SUBSTR(B.ADM_PROGRAM_OF_STUDY,1,2) = 'N2')))";
//
//     try (Statement stmt = conn.createStatement()) {
//     try (ResultSet rs = stmt.executeQuery(sql2)) {
//     while (rs.next()) {
//     Log.info("Found a CSUBAN.CSUS_APPLICANT record");
//
//     Log.info(" pidm = " + rs.getString("pidm"));
//     Log.info(" appterm = " + rs.getString("appterm"));
//     Log.info(" decision = " + rs.getString("decision"));
//     Log.info(" admitted = " + rs.getString("admitted"));
//     Log.info(" admType = " + rs.getString("admType"));
//     Log.info(" admCollege = " + rs.getString("admCollege"));
//     Log.info(" admDept = " + rs.getString("admDept"));
//     Log.info(" admProgram = " + rs.getString("admProgram"));
//     Log.info(" admCampus = " + rs.getString("admCampus"));
//     Log.info(" admResidency = " + rs.getString("admResidency"));
//     Log.info(" aplnDate = " + rs.getString("aplnDate"));
//     }
//     }
//     }
//
//     final String sql3 = "SELECT "
//     + " C.STUDENT_CLASS cls, "
//     + " C.PRIMARY_COLLEGE college, "
//     + " C.PRIMARY_DEPARTMENT dept, "
//     + " C.PROGRAM_OF_STUDY program, "
//     + " C.ANTICIPATED_GRAD_TERM gradterm, "
//     + " C.RESIDENCY res, "
//     + " C.CAMPUS campus "
//     + "FROM CSUBAN.CSUS_TERM_INFO_SPR C "
//     + "WHERE C.CSU_ID = '831645739'";
//
//     try (Statement stmt = conn.createStatement()) {
//     try (ResultSet rs = stmt.executeQuery(sql3)) {
//     while (rs.next()) {
//     Log.info("Found a CSUBAN.CSUS_TERM_INFO_SPR record");
//     }
//     }
//     }
//     }

    /**
     * Dumps metadata.
     *
     * @param conn the ODS connection
     * @throws SQLException if there is an error accessing the database
     */
    private static void dumpMetadata(final DbConnection conn) throws SQLException {

        final Connection jdbc = conn.getConnection();

        final DatabaseMetaData meta = jdbc.getMetaData();

        // TABLES

        final List<String> tables = new ArrayList<>(40);
        final List<String> schemas = new ArrayList<>(10);

        // try (ResultSet tablesRS = meta.getTables(CoreConstants.EMPTY, "CSUBAN", null,
        try (final ResultSet tablesRS = meta.getTables(CoreConstants.EMPTY, null, null, null)) {
            while (tablesRS.next()) {
                final String schema = tablesRS.getString("TABLE_SCHEM");

                if ("CSUBAN".equals(schema) || "ODSMGR".equals(schema)) {
                    final String tableName = tablesRS.getString("TABLE_NAME");

                    tables.add(tableName);
                    schemas.add(schema);
                }
            }
        }

        final int numTables = tables.size();

        Log.fine(CoreConstants.EMPTY);
        Log.fine("FOUND " + numTables + " TABLES:");
        Log.fine(CoreConstants.EMPTY);

        final List<String> fieldNames = new ArrayList<>(40);
        final List<String> fieldTypes = new ArrayList<>(40);

        for (int i = 0; i < numTables; ++i) {
            final String tableName = tables.get(i);
            final String schema = schemas.get(i);

            fieldNames.clear();
            fieldTypes.clear();
            int maxlen = 0;

            Log.fine("Table: ", tableName, " in schema ", schema);

            try (final ResultSet columnsRS = meta.getColumns(CoreConstants.EMPTY, schema, tableName, null)) {

                while (columnsRS.next()) {
                    final String colName = columnsRS.getString("COLUMN_NAME");
                    maxlen = Math.max(maxlen, colName.length());
                    final String type = columnsRS.getString("TYPE_NAME");
                    final int size = columnsRS.getInt("COLUMN_SIZE");

                    fieldNames.add(colName);
                    if (type.toLowerCase(Locale.ROOT).contains("char")) {
                        fieldTypes.add(type + "(" + size + ")");
                    } else {
                        fieldTypes.add(type);
                    }
                }

            }

            final int count = fieldNames.size();
            final HtmlBuilder builder = new HtmlBuilder(50);
            for (int j = 0; j < count; ++j) {
                builder.add(fieldNames.get(j));
                int len = builder.length();
                while (len < maxlen) {
                   builder.add(CoreConstants.SPC_CHAR);
                   ++len;
                }

                Log.fine(" ", builder.toString() , " : ", fieldTypes.get(j));
                builder.reset();
            }

            Log.fine(CoreConstants.EMPTY);
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final DumpODSSchema dumpODSSchema = new DumpODSSchema();
        dumpODSSchema.execute();
    }
}

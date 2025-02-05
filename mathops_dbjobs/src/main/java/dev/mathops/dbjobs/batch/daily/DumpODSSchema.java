package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * A simple class that connects to the ODS system and queries metadata for the views we use.
 */
final class DumpODSSchema {

    /** The database profile through which to access the database. */
    private final Profile profile;

    /**
     * Constructs a new {@code DumpODSSchema}.
     */
    private DumpODSSchema() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        this.profile = config.getCodeProfile(Contexts.BATCH_PATH);
    }

    /**
     * Executes the job.
     */
    private void execute() {

        if (this.profile == null) {
            Log.warning("Unable to create production profile.");
        } else {
            final Login odsLogin = this.profile.getLogin(ESchema.ODS);
            final DbConnection conn = odsLogin.checkOutConnection();

            try {
//                dumpMetadata(conn);
                debugQuery(conn);
            } catch (final SQLException ex) {
                Log.warning(ex);
                Log.warning("Unable to obtain connection to ODS database");
            } finally {
                odsLogin.checkInConnection(conn);
            }
        }
    }

    /**
     * Performs a debugging query to see why we get back many rows for applicant student query.
     *
     * @param conn the ODS connection
     * @throws SQLException if there is an error accessing the database
     */
    private static void debugQuery(final DbConnection conn) throws SQLException {

        final String sql = "SELECT COUNT(*) FROM CSUBAN.CSUG_GP_DEMO";

        try (final Statement stmt = conn.createStatement()) {
            try (final ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    Log.info(" number of rows = " + rs.getInt(1));
                }
            }
        }

        final String sql1 = "SELECT "
                            + " A.CSU_ID csuid, "
                            + " A.PIDM pidm, "
                            + " A.FIRST_NAME first, "
                            + " A.MIDDLE_NAME middle, "
                            + " A.LAST_NAME last, "
                            + " A.PREFERRED_FIRST_NAME pref, "
                            + " A.EMAIL email, "
                            + " A.BIRTH_DATE birth "
                            + "FROM CSUBAN.CSUG_GP_DEMO A "
                            + "WHERE A.CSU_ID = '831645739'";

        try (final Statement stmt = conn.createStatement()) {
            try (final ResultSet rs = stmt.executeQuery(sql1)) {
                while (rs.next()) {
                    Log.info("Found a CSUBAN.CSUG_GP_DEMO record");

                    Log.info(" csuid = " + rs.getString("csuid"));
                    Log.info(" pidm = " + rs.getString("pidm"));
                    Log.info(" first = " + rs.getString("first"));
                    Log.info(" middle = " + rs.getString("middle"));
                    Log.info(" last = " + rs.getString("last"));
                    Log.info(" pref = " + rs.getString("pref"));
                    Log.info(" email = " + rs.getString("email"));
                    Log.info(" birth = " + rs.getString("birth"));
                }
            }
        }

        final String sql2 = "SELECT "
                            + " A.PERSON_UID pidm, "
                            + " A.ID id, "
                            + " A.NAME name, "
                            + " A.HS_CODE hs_code, "
                            + " A.PRIMARY_COLLEGE college, "
                            + " A.PRIMARY_DEPARTMENT dept, "
                            + " A.PRIMARY_MAJOR major, "
                            + " A.PROGRAM_OF_STUDY program, "
                            + " A.START_DATE startDt, "
                            + " A.END_DATE endDt "
                            + "FROM CSUBAN.CSUS_ENROLL_MASTER_AH A "
                            + "WHERE A.PERSON_UID = 11612982";

        try (final Statement stmt = conn.createStatement()) {
            try (final ResultSet rs = stmt.executeQuery(sql2)) {
                while (rs.next()) {
                    Log.info("Found a CSUBAN.CSUS_ENROLL_MASTER_AH record");

                    Log.info(" pidm = " + rs.getString("pidm"));
                    Log.info(" id = " + rs.getString("id"));
                    Log.info(" name = " + rs.getString("name"));
                    Log.info(" hs_code = " + rs.getString("hs_code"));
                    Log.info(" college = " + rs.getString("college"));
                    Log.info(" dept = " + rs.getString("dept"));
                    Log.info(" major = " + rs.getString("major"));
                    Log.info(" program = " + rs.getString("program"));
                    Log.info(" start = " + rs.getDate("startDt"));
                    Log.info(" end = " + rs.getDate("endDt"));
                }
            }
        }

        final String sql3 = "SELECT "
                            + " A.PIDM pidm, "
                            + " A.CSU_ID id, "
                            + " A.NAME name, "
                            + " A.TERM term, "
                            + " A.PRIMARY_COLLEGE college, "
                            + " A.PRIMARY_DEPARTMENT dept, "
                            + " A.PRIMARY_MAJOR major, "
                            + " A.PROGRAM_OF_STUDY program "
                            + "FROM CSUBAN.CSUS_ENROLL_TERM_SUMMARY_AH A "
                            + "WHERE A.PIDM = 11612982";

        try (final Statement stmt = conn.createStatement()) {
            try (final ResultSet rs = stmt.executeQuery(sql3)) {
                while (rs.next()) {
                    Log.info("Found a CSUBAN.CSUS_ENROLL_TERM_SUMMARY_AH record");

                    Log.info(" pidm = " + rs.getString("pidm"));
                    Log.info(" csu ID = " + rs.getString("id"));
                    Log.info(" name = " + rs.getString("name"));
                    Log.info(" term = " + rs.getString("term"));
                    Log.info(" college = " + rs.getString("college"));
                    Log.info(" dept = " + rs.getString("dept"));
                    Log.info(" major = " + rs.getString("major"));
                    Log.info(" program = " + rs.getString("program"));
                }
            }
        }
    }

//    /**
//     * Dumps metadata.
//     *
//     * @param conn the ODS connection
//     * @throws SQLException if there is an error accessing the database
//     */
//    private static void dumpMetadata(final DbConnection conn) throws SQLException {
//
//        final Connection jdbc = conn.getConnection();
//
//        final DatabaseMetaData meta = jdbc.getMetaData();
//
//        // TABLES
//
//        final List<String> tables = new ArrayList<>(40);
//        final List<String> schemas = new ArrayList<>(10);
//
//        // try (ResultSet tablesRS = meta.getTables(CoreConstants.EMPTY, "CSUBAN", null,
//        try (final ResultSet tablesRS = meta.getTables(CoreConstants.EMPTY, null, null, null)) {
//            while (tablesRS.next()) {
//                final String schema = tablesRS.getString("TABLE_SCHEM");
//
//                if ("CSUBAN".equals(schema) || "ODSMGR".equals(schema)) {
//                    final String tableName = tablesRS.getString("TABLE_NAME");
//
//                    tables.add(tableName);
//                    schemas.add(schema);
//                }
//            }
//        }
//
//        final int numTables = tables.size();
//
//        Log.fine(CoreConstants.EMPTY);
//        Log.fine("FOUND " + numTables + " TABLES:");
//        Log.fine(CoreConstants.EMPTY);
//
//        final List<String> fieldNames = new ArrayList<>(40);
//        final List<String> fieldTypes = new ArrayList<>(40);
//
//        for (int i = 0; i < numTables; ++i) {
//            final String tableName = tables.get(i);
//            final String schema = schemas.get(i);
//
//            fieldNames.clear();
//            fieldTypes.clear();
//            int maxlen = 0;
//
//            Log.fine("Table: ", tableName, " in schema ", schema);
//
//            try (final ResultSet columnsRS = meta.getColumns(CoreConstants.EMPTY, schema, tableName, null)) {
//
//                while (columnsRS.next()) {
//                    final String colName = columnsRS.getString("COLUMN_NAME");
//                    maxlen = Math.max(maxlen, colName.length());
//                    final String type = columnsRS.getString("TYPE_NAME");
//                    final int size = columnsRS.getInt("COLUMN_SIZE");
//
//                    fieldNames.add(colName);
//                    if (type.toLowerCase(Locale.ROOT).contains("char")) {
//                        fieldTypes.add(type + "(" + size + ")");
//                    } else {
//                        fieldTypes.add(type);
//                    }
//                }
//
//            }
//
//            final int count = fieldNames.size();
//            final HtmlBuilder builder = new HtmlBuilder(50);
//            for (int j = 0; j < count; ++j) {
//                builder.add(fieldNames.get(j));
//                int len = builder.length();
//                while (len < maxlen) {
//                    builder.add(CoreConstants.SPC_CHAR);
//                    ++len;
//                }
//
//                Log.fine(" ", builder.toString(), " : ", fieldTypes.get(j));
//                builder.reset();
//            }
//
//            Log.fine(CoreConstants.EMPTY);
//        }
//    }

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

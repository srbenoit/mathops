package dev.mathops.app.finalgrading;

import dev.mathops.core.log.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Process to upload placement result codes directly to BANNER.
 */
final class UploadTestResultCodes {

    /** The connection. */
    private Connection mConnection;

    /**
     * Constructs a new {@code UploadTestResultCodes}.
     */
    private UploadTestResultCodes() {

        // No action
    }

    /**
     * Connects to oracle.
     *
     * @param useTest true to use test
     * @return true if OK
     */
    private boolean connectToOracle(final boolean useTest) {

        String pwd;
        boolean ok = false;

        try {
            DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        } catch (final SQLException ex) {
            Log.fine("Failed to load ORACLE JDBC driver.");
            return false;
        }
        final String url;
        if (useTest) {
            url = "jdbc:oracle:thin:@dbbantest.is.colostate.edu:1526:BANTEST";
            Log.fine("Connecting to BANTEST as 'math_web'...");
        } else {
            url = "jdbc:oracle:thin:@dbbanprod.is.colostate.edu:1526:BANPROD";
            Log.fine("Connecting to BANPROD as 'math_web'...");
        }
        Log.finest("  ENTER PASSWORD:");
        try (final BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            pwd = br.readLine();
            while (pwd != null) {
                try {
                    this.mConnection = DriverManager.getConnection(url, "math_web", pwd);
                    ok = true;
                    Log.fine("Connected to Oracle");
                    break;
                } catch (final SQLException e) {
                    Log.fine("Unable to log in: " + e.getMessage());
                    Log.finest("\n  ENTER PASSWORD:");
                    pwd = br.readLine();
                }
            }
        } catch (final IOException e) {
            Log.warning(e);
        }

        return ok;
    }

    /**
     * Disconnects from Oracle.
     */
    private void disconnectFromOracle() {

        try {
            this.mConnection.close();
        } catch (final SQLException e) {
            Log.warning(e);
        }
        this.mConnection = null;
        Log.fine("Disconnected from Oracle");
    }

    /**
     * Locates a stored procedure.
     *
     * @param name the name
     * @return true if OK
     */
    private boolean locateStoredProcedure(final String name) {

        boolean ok = false;

        try {
            final DatabaseMetaData meta = this.mConnection.getMetaData();

            try (final ResultSet rs1 = meta.getProcedures(null, null, name)) {
                while (rs1.next()) {
                    String str = rs1.getString("PROCEDURE_NAME");
                    Log.fine("Name: " + str);
                    str = rs1.getString("PROCEDURE_SCHEM");
                    Log.fine("Schema: " + str);
                    str = rs1.getString("REMARKS");
                    Log.fine("Remarks: " + str);
                    ok = true;
                }
            }

            try (final ResultSet rs2 = meta.getProcedureColumns(null, null, name, null)) {
                while (rs2.next()) {
                    String str = rs2.getString("COLUMN_NAME");
                    Log.fine("Column Name: " + str);
                    short sh = rs2.getShort("COLUMN_TYPE");
                    Log.fine("  Column Type: " + sh);
                    int i = rs2.getInt("DATA_TYPE");
                    Log.fine("  Data Type: " + i);
                    str = rs2.getString("TYPE_NAME");
                    Log.fine("  Type Name: " + str);
                    i = rs2.getInt("PRECISION");
                    Log.fine("  Precision: " + i);
                    i = rs2.getInt("LENGTH");
                    Log.fine("  Length: " + i);
                    sh = rs2.getShort("SCALE");
                    Log.fine("  Scale: " + sh);
                    sh = rs2.getShort("RADIX");
                    Log.fine("  Radix: " + sh);
                    sh = rs2.getShort("NULLABLE");
                    Log.fine("  Nullable: " + sh);
                    str = rs2.getString("REMARKS");
                    Log.fine("  Remarks: " + str);
                }
            }

            Log.fine("SOATEST Table Columns:");
            try (final ResultSet rs3 = meta.getColumns(null, null, "SOATEST", null)) {
                final StringBuilder sb = new StringBuilder(50);
                while (rs3.next()) {
                    sb.setLength(0);
                    sb.append(rs3.getString("COLUMN_NAME"));
                    sb.append(" ");
                    sb.append(rs3.getString("TYPE_NAME"));
                    sb.append("(");
                    sb.append(rs3.getString("COLUMN_SIZE"));
                    sb.append(")");
                    Log.fine(sb);
                }
            }
        } catch (final SQLException e) {
            Log.warning(e);
        }

        return ok;
    }

//    /**
//     * Dumps a table.
//     */
//    public void dumpTable() {
//
//        final StringBuilder sb = new StringBuilder(150);
//
//        Log.fine("STATUS     TERM            PIDM_STU GRADE  PIDM_PROF USER_ID    ERROR");
//        Log.fine("---------- --------------- -------- ------ --------- ---------- ------------");
//
//        try (final PreparedStatement stmt = this.mConnection.prepareStatement("SELECT * FROM SOATEST");
//             final ResultSet rs = stmt.executeQuery()) {
//
//            while (rs.next()) {
//                sb.setLength(0);
//
//                String str = rs.getString("SWRSTCR_STATUS");
//                if ((str != null) && (str.length() > 10)) {
//                    str = str.substring(0, 10);
//                }
//                sb.append(str);
//                while (sb.length() < 11) { // NOTE: length changes within loop
//                    sb.append(' ');
//                }
//
//                str = rs.getString("SWRSTCR_CRN_TERM");
//                if ((str != null) && (str.length() > 15)) {
//                    str = str.substring(0, 15);
//                }
//                sb.append(str);
//                while (sb.length() < 27) { // NOTE: length changes within loop
//                    sb.append(' ');
//                }
//
//                str = rs.getString("SWRSTCR_PIDM_STUD");
//                if ((str != null) && (str.length() > 8)) {
//                    str = str.substring(0, 8);
//                }
//                sb.append(str);
//                while (sb.length() < 36) { // NOTE: length changes within loop
//                    sb.append(' ');
//                }
//
//                str = rs.getString("SWRSTCR_GRDE_CODE");
//                if ((str != null) && (str.length() > 6)) {
//                    str = str.substring(0, 6);
//                }
//                sb.append(str);
//                while (sb.length() < 43) { // NOTE: length changes within loop
//                    sb.append(' ');
//                }
//
//                str = rs.getString("SWRSTCR_PIDM_PROF");
//                if ((str != null) && (str.length() > 8)) {
//                    str = str.substring(0, 8);
//                }
//                sb.append(str);
//                while (sb.length() < 53) { // NOTE: length changes within loop
//                    sb.append(' ');
//                }
//
//                str = rs.getString("SWRSTCR_USER_ID");
//                if ((str != null) && (str.length() > 10)) {
//                    str = str.substring(0, 10);
//                }
//                sb.append(str);
//                while (sb.length() < 64) { // NOTE: length changes within loop
//                    sb.append(' ');
//                }
//
//                str = rs.getString("SWRSTCR_ERROR_CODE");
//                sb.append(str);
//
//                Log.fine(sb);
//            }
//        } catch (final SQLException e) {
//            Log.fine("Error querying SOATEST: " + e.getMessage());
//        }
//    }

    /**
     * Main method.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {

        final UploadTestResultCodes obj = new UploadTestResultCodes();

        if (obj.connectToOracle(true)) {
            obj.locateStoredProcedure("P_CREATE");
            obj.disconnectFromOracle();
        }
    }
}

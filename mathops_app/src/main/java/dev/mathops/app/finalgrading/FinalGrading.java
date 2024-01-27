package dev.mathops.app.finalgrading;

import dev.mathops.commons.log.Log;
import oracle.jdbc.OracleDriver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Final grading process.
 */
final class FinalGrading {

    /** The connection. */
    private Connection mConnection;

    /** The list of records. */
    private List<FinalGradingRecord> mRecords;

    /**
     * Constructs a new {@code FinalGrading}.
     */
    private FinalGrading() {

        // No action
    }

    /**
     * Preload file.
     *
     * @param filename the filename
     * @return true if OK
     */
    private boolean preloadFile(final String filename) {

        boolean ok = false;

        int linenum = 0;

        this.mRecords = new ArrayList<>(100);
        try (final BufferedReader br = new BufferedReader(new FileReader(filename, StandardCharsets.UTF_8))) {

            String line = br.readLine();
            linenum++;
            while (line != null) {
                final String[] split = line.split(",");
                if (split.length == 5) {
                    final FinalGradingRecord rec = new FinalGradingRecord();
                    rec.term = split[0];
                    rec.csuid = split[1];
                    rec.grade = split[2];
                    rec.profCsuid = split[3];
                    rec.user = split[4];
                    this.mRecords.add(rec);
                } else {
                    Log.fine("Record at line " + linenum + " of '" + filename
                            + "' did not have 5 items separated by commas.");
                    Log.fine("The line was:");
                    Log.fine(line);
                }
                line = br.readLine();
                linenum++;
            }

            ok = true;
            Log.fine("Read " + this.mRecords.size() + " records from file");
        } catch (final IOException e) {
            Log.fine("Failed to read file '" + filename + "': " + e.getMessage());
        }

        return ok;
    }

    /**
     * Connects to oracle.
     *
     * @param useTest true to use test
     * @param usePprd true to use pprd
     * @return true if OK
     */
    private boolean connectToOracle(final boolean useTest, final boolean usePprd) {

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
            url = "jdbc:oracle:thin:@dbdevlaries.is.colostate.edu:1521/BANTEST";
            Log.fine("Connecting to BANTEST as 'math_web'...");
        } else if (usePprd) {
            url = "jdbc:oracle:thin:@dbbanpprd.is.colostate.edu:1525/BANPPRD";
            Log.fine("Connecting to BANPPRD as 'math_web'...");
        } else {
            url = "jdbc:oracle:thin:@dbbanprod.is.colostate.edu:1521/banprod.infosys.colostate.edu";
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

            try (final ResultSet rs1 = meta.getProcedures(null, "BANINST1", name)) {
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

            try (final ResultSet rs2 = meta.getProcedureColumns(null, "BANINST1", name, null)) {
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

            Log.fine("SWRSTCR Table Columns:");
            try (final ResultSet rs3 = meta.getColumns(null, null, "SWRSTCR", null)) {
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

    /**
     * Processes records.
     *
     * @param update true to update
     */
    private void processRecords(final boolean update) {

        final String sql = update
                ? "begin CSUS_API_MATH_GRDE.P_UPDATE(?,?,?,?,?); end;"
                : "begin CSUS_API_MATH_GRDE.P_CREATE(?,?,?,?,?); end;";

        try (final CallableStatement cstmt = this.mConnection.prepareCall(sql)) {

            int count = 0;
            for (final FinalGradingRecord rec : this.mRecords) {
                try {
                    cstmt.setString(1, rec.term);
                    cstmt.setString(2, rec.csuid);
                    cstmt.setString(3, rec.grade);
                    cstmt.setString(4, rec.profCsuid);
                    cstmt.setString(5, rec.user);
                    cstmt.execute();
                    count++;
                } catch (final SQLException e) {
                    Log.fine("Error: " + e.getMessage());
                    Log.fine("  Record on which the error occurred:");
                    Log.fine("  '" + rec.term + "','"
                            + rec.csuid + "','" + rec.grade + "','"
                            + rec.profCsuid + "','" + rec.user + "'");
                }

                if (update) {
                    Log.fine("Updated " + count + " records.");
                } else {
                    Log.fine("Created " + count + " records.");
                }
            }
        } catch (final SQLException e) {
            Log.fine("Unable to access procedure: " + e.getMessage());
        }
    }

    /**
     * Dumps a table.
     */
    private void dumpTable() {

        final StringBuilder sb = new StringBuilder(150);

        Log.fine("STATUS     TERM            PIDM_STU GRADE  PIDM_PROF USER_ID    ERROR");
        Log.fine("---------- --------------- -------- ------ --------- ---------- ------------");

        try (final PreparedStatement stmt = this.mConnection.prepareStatement("SELECT * FROM SWRSTCR");
             final ResultSet rs = stmt.executeQuery()) {


            while (rs.next()) {
                sb.setLength(0);

                String str = rs.getString("SWRSTCR_STATUS");
                if ((str != null) && (str.length() > 10)) {
                    str = str.substring(0, 10);
                }
                sb.append(str);
                while (sb.length() < 11) { // Length changes within loop
                    sb.append(' ');
                }

                str = rs.getString("SWRSTCR_CRN_TERM");
                if ((str != null) && (str.length() > 15)) {
                    str = str.substring(0, 15);
                }
                sb.append(str);
                while (sb.length() < 27) { // Length changes within loop
                    sb.append(' ');
                }

                str = rs.getString("SWRSTCR_PIDM_STUD");
                if ((str != null) && (str.length() > 8)) {
                    str = str.substring(0, 8);
                }
                sb.append(str);
                while (sb.length() < 36) { // Length changes within loop
                    sb.append(' ');
                }

                str = rs.getString("SWRSTCR_GRDE_CODE");
                if ((str != null) && (str.length() > 6)) {
                    str = str.substring(0, 6);
                }
                sb.append(str);
                while (sb.length() < 43) { // Length changes within loop
                    sb.append(' ');
                }

                str = rs.getString("SWRSTCR_PIDM_PROF");
                if ((str != null) && (str.length() > 8)) {
                    str = str.substring(0, 8);
                }
                sb.append(str);
                while (sb.length() < 53) { // Length changes within loop
                    sb.append(' ');
                }

                str = rs.getString("SWRSTCR_USER_ID");
                if ((str != null) && (str.length() > 10)) {
                    str = str.substring(0, 10);
                }
                sb.append(str);
                while (sb.length() < 64) { // Length changes within loop
                    sb.append(' ');
                }

                str = rs.getString("SWRSTCR_ERROR_CODE");
                sb.append(str);

                Log.fine(sb);
            }
        } catch (final SQLException e) {
            Log.fine("Error querying swrstcr: " + e.getMessage());
        }
    }

    /**
     * Main method.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {

        boolean locate = false;
        boolean test = false;
        boolean pprd = false;
        boolean prod = false;
        boolean dump = false;
        boolean update = false;
        String filename = null;

        for (final String arg : args) {
            if ("-locate".equalsIgnoreCase(arg)) {
                locate = true;
            } else if ("-test".equalsIgnoreCase(arg)) {
                test = true;
            } else if ("-pprd".equalsIgnoreCase(arg)) {
                pprd = true;
            } else if ("-prod".equalsIgnoreCase(arg)) {
                prod = true;
            } else if ("-dump".equalsIgnoreCase(arg)) {
                dump = true;
            } else if ("-update".equalsIgnoreCase(arg)) {
                update = true;
            } else if (filename == null) {
                filename = arg;
            } else {
                usage();
            }
        }

        // Need exactly one of 'test', 'prod', and 'pprd'
        if ((test && prod) || (test && pprd) || (prod && pprd) || !(test || prod || pprd)) {
            usage();
        }

        final FinalGrading obj = new FinalGrading();

        if (locate) {
            if (obj.connectToOracle(test, pprd)) {
                obj.locateStoredProcedure("P_CREATE");
                obj.locateStoredProcedure("P_UPDATE");
                obj.disconnectFromOracle();
            }
        } else if (dump) {
            if (obj.connectToOracle(test, pprd)) {
                obj.dumpTable();
                obj.disconnectFromOracle();
            }
        } else if ((obj.preloadFile(filename)) && (obj.connectToOracle(test, pprd))) {
            obj.processRecords(update);
            obj.disconnectFromOracle();
        }
    }

    /**
     * Prints usage.
     */
    private static void usage() {

        Log.fine("Usage: java -jar FinalGrading.jar -[test|prod|pprd] <filename> (-locate) (-dump)");

        Log.fine("  -test: Connect to BANTEST database");
        Log.fine("  -pprd: Connect to BANPPRD database");
        Log.fine("  -prod: Connect to BANPROD database");
        Log.fine("  -locate: Print out database objects rather than processing data");
        Log.fine("  -dump: Print contents of table rather than processing data");
        Log.fine("  -update: Call the 'p_update' function rather than 'p_create'");
        Log.fine("  filename: The filename to process.");
        System.exit(0);
    }

    /**
     * A final grading record.
     */
    static final class FinalGradingRecord {

        /** The term. */
        String term;

        /** The CSU ID. */
        String csuid;

        /** The grade. */
        String grade;

        /** The prof CSU ID. */
        String profCsuid;

        /** The user. */
        String user;

        /**
         * Constructs a new {@code FinalGradingRecord}.
         */
        FinalGradingRecord() {

            // No action
        }
    }
}

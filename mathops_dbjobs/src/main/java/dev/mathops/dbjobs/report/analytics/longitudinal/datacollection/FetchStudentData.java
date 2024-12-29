package dev.mathops.dbjobs.report.analytics.longitudinal.datacollection;

import dev.mathops.commons.log.Log;
import dev.mathops.db.DbConnection;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.StudentRec;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This class retrieves data about students from the ODS and stores it in a local file so we can load and process it
 * without having to do a lengthy ODS query each time.
 */
enum FetchStudentData {
    ;

    /**
     * Gathers student data.
     *
     * @param odsConn    the connection to the ODS
     * @param studentIds the set of student IDs of interest
     * @param target     the file to which to write results
     * @throws SQLException if there is an error accessing the database
     */
    static void gatherStudentData(final DbConnection odsConn, final Set<String> studentIds, final File target)
            throws SQLException {

        Log.fine("Gathering student data.");

        final List<StudentRec> studentRecords = collectStudents(odsConn, studentIds);

        final HtmlBuilder fileData = new HtmlBuilder(100000);
        fileData.addln("[");
        boolean comma = false;
        for (final StudentRec rec : studentRecords) {
            if (comma) {
                fileData.addln(",");
            }
            final String json = rec.toJson();
            fileData.add(json);
            comma = true;
        }
        fileData.addln();
        fileData.addln("]");

        final String absolutePath = target.getAbsolutePath();
        Log.fine("Writing student data file to ", absolutePath);

        try (final FileWriter fw = new FileWriter(target)) {
            final String dataString = fileData.toString();
            fw.write(dataString);
        } catch (final IOException ex) {
            Log.warning("Failed to write student JSON file.", ex);
        }

        Log.fine("Student data gathered.");
    }

    /**
     * Queries student records from the ODS.
     *
     * @param odsConn    the ODS database connection
     * @param studentIds the set of student IDs of interest
     * @return a list of student records
     * @throws SQLException if there is an error performing the query
     */
    private static List<StudentRec> collectStudents(final DbConnection odsConn, final Collection<String> studentIds)
            throws SQLException {

        final int count = studentIds.size();
        final List<StudentRec> result = new ArrayList<>(count);

        // TABLE: 'CSUBAN.CSUG_GP_DEMO'
        //     *  CSU_ID (VARCHAR2[63])
        //        PIDM (NUMBER[0])
        //        LEGAL_NAME (VARCHAR2[500])
        //        UPPER_NAME (VARCHAR2[60])
        //        FIRST_NAME (VARCHAR2[63])
        //        MIDDLE_NAME (VARCHAR2[63])
        //        LAST_NAME (VARCHAR2[63])
        //        NAME_SUFFIX (VARCHAR2[20])
        //        PREFERRED_FIRST_NAME (VARCHAR2[63])
        //        PREFERRED_LAST_NAME (VARCHAR2[60])
        //        EMAIL (VARCHAR2[255])
        //        ADDR_1 (VARCHAR2[255])
        //        ADDR_2 (VARCHAR2[255])
        //        ADDR_3 (VARCHAR2[255])
        //        BIRTH_DATE (DATE[7])
        //        AGE (NUMBER[0])
        //        CITY (VARCHAR2[63])
        //        COUNTY (VARCHAR2[63])
        //        COUNTY_DESC (VARCHAR2[255])
        //        STATE (VARCHAR2[63])
        //        STATE_DESC (VARCHAR2[255])
        //        NATION (VARCHAR2[63])
        //        NATION_DESC (VARCHAR2[255])
        //        ZIP (VARCHAR2[63])
        //        TELEPHONE (VARCHAR2[63])
        //        CONFIDENTIALITY_IND (VARCHAR2[1])
        //     *  GENDER (VARCHAR2[63])
        //        ETHNIC (VARCHAR2[63])
        //        ETHNIC_DESC (VARCHAR2[255])
        //        LOST_ID_DIGIT (VARCHAR2[300])
        //        INTERNATIONAL_FLAG (VARCHAR2[4000])
        //        CITIZENSHIP_COUNTRY_CODE (VARCHAR2[4000])
        //        CITIZENSHIP_COUNTRY_DESC (VARCHAR2[4000])
        //     *  HISPANIC_LATINO_ETHNICITY_IND (VARCHAR2[1])
        //     *  AMERICAN_INDIAN_RACE_IND (VARCHAR2[1])
        //     *  ASIAN_RACE_IND (VARCHAR2[1])
        //     *  BLACK_RACE_IND (VARCHAR2[1])
        //     *  HAWAIIAN_RACE_IND (VARCHAR2[1])
        //     *  WHITE_RACE_IND (VARCHAR2[1])
        //     *  MULTI_RACE_IND (VARCHAR2[1])
        // (CSU)  MULTI_SOURCE (VARCHAR2[6])
        //        MULTI_SOURCE_DESC (VARCHAR2[30])
        //        PRONOUN_CODE (VARCHAR2[4])
        //        PRONOUN_DESC (VARCHAR2[60])

        try (final Statement stmt = odsConn.createStatement()) {

            final String sql = SimpleBuilder.concat(
                    "SELECT CSU_ID, GENDER, HISPANIC_LATINO_ETHNICITY_IND, AMERICAN_INDIAN_RACE_IND, ASIAN_RACE_IND, ",
                    "BLACK_RACE_IND, HAWAIIAN_RACE_IND, WHITE_RACE_IND, MULTI_RACE_IND ",
                    "FROM CSUBAN.CSUG_GP_DEMO WHERE MULTI_SOURCE='CSU'");

            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final String csuId = rs.getString("CSU_ID");

                    if (studentIds.contains(csuId)) {
                        final String gender = rs.getString("GENDER");

                        final boolean hispanic = "Y".equals(rs.getString("HISPANIC_LATINO_ETHNICITY_IND"));
                        final boolean amerIndian = "Y".equals(rs.getString("AMERICAN_INDIAN_RACE_IND"));
                        final boolean asian = "Y".equals(rs.getString("ASIAN_RACE_IND"));
                        final boolean black = "Y".equals(rs.getString("BLACK_RACE_IND"));
                        final boolean hawaiian = "Y".equals(rs.getString("HAWAIIAN_RACE_IND"));
                        final boolean white = "Y".equals(rs.getString("WHITE_RACE_IND"));
                        final boolean multi = "Y".equals(rs.getString("MULTI_RACE_IND"));

                        final int ethnicity = (hispanic ? StudentRec.HISPANIC_LATINO : 0)
                                              + (amerIndian ? StudentRec.AMERICAN_INDIAN : 0)
                                              + (asian ? StudentRec.ASIAN : 0)
                                              + (black ? StudentRec.BLACK : 0)
                                              + (hawaiian ? StudentRec.HAWAIIAN : 0)
                                              + (white ? StudentRec.WHITE : 0)
                                              + (multi ? StudentRec.MULTI : 0);

                        final StudentRec rec = new StudentRec(csuId, gender, ethnicity);
                        result.add(rec);

                        studentIds.remove(csuId);
                    }
                }
            }

            // Dump a list of students for whom we got no data...
            for (final String csuId : studentIds) {
                Log.info("Missing student data for ", csuId);
            }
        }

        return result;
    }
}


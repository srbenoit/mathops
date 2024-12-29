package dev.mathops.dbjobs.report.analytics.longitudinal.datacollection;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.db.DbConnection;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.MajorProgramRec;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * This class retrieves data from the ODS and stores it in a local file so we can load and process it without having to
 * do a lengthy ODS query each time.
 */
public enum FetchMajorAndProgramData {
    ;

    /**
     * Gathers major/program data.
     *
     * @param odsConn             the connection to the ODS
     * @param startAcademicPeriod the starting academic period
     * @param endAcademicPeriod   the ending academic period
     * @param target              the file to which to write results
     * @throws SQLException if there is an error accessing the database
     */
    static void gatherMajorAndProgramData(final DbConnection odsConn, final int startAcademicPeriod,
                                          final int endAcademicPeriod, final File target) throws SQLException {

        Log.fine("Gathering major and program data.");

        final List<MajorProgramRec> majorProgramRecs = collectMajorsAndPrograms(odsConn, startAcademicPeriod,
                endAcademicPeriod);

        final HtmlBuilder fileData = new HtmlBuilder(100000);
        fileData.addln("[");
        boolean comma = false;
        for (final MajorProgramRec rec : majorProgramRecs) {
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
        Log.fine("Writing major and program data file to ", absolutePath);

        try (final FileWriter fw = new FileWriter(target)) {
            final String dataString = fileData.toString();
            fw.write(dataString);
        } catch (final IOException ex) {
            Log.warning("Failed to write major and program JSON file.", ex);
        }

        Log.fine("Major and program data gathered.");
    }

    /**
     * Queries major/program records from the ODS.
     *
     * @param odsConn             the database connection
     * @param startAcademicPeriod the starting academic period
     * @param endAcademicPeriod   the ending academic period
     * @return a list of major/program records
     * @throws SQLException if there is an error performing the query
     */
    private static List<MajorProgramRec> collectMajorsAndPrograms(final DbConnection odsConn,
                                                                  final int startAcademicPeriod,
                                                                  final int endAcademicPeriod)
            throws SQLException {

        final String startStr = Integer.toString(startAcademicPeriod);
        final String endStr = Integer.toString(endAcademicPeriod);

        final List<MajorProgramRec> result = new ArrayList<>(10000);

        // TABLE: 'CSUBAN.CSUS_ENROLL_TERM_SUMMARY_AH'
        //        PIDM (NUMBER[0])
        //     *  CSU_ID (VARCHAR2[63])
        //        NAME (VARCHAR2[255])
        //     *  TERM (VARCHAR2[63])
        //        TERM_DESC (VARCHAR2[255])
        //        YEAR (VARCHAR2[63])
        //        ACADEMIC_STANDING (VARCHAR2[63])
        //        ACADEMIC_STANDING_DESC (VARCHAR2[255])
        //        ADM_INDEX (NUMBER[0])
        //        ANTICIPATED_GRAD_ACAD_YR (VARCHAR2[63])
        //        ANTICIPATED_GRAD_ACAD_YR_DESC (VARCHAR2[255])
        //        ANTICIPATED_GRAD_DATE (DATE[7])
        //        ANTICIPATED_GRAD_TERM (VARCHAR2[63])
        //        CONTINUOUS_REG (VARCHAR2[1])
        //        CREDITS_CE (NUMBER[0])
        //        CREDITS_RI (NUMBER[0])
        //        CREDITS_NON_CSU (NUMBER[0])
        //        CREDITS_SI (NUMBER[0])
        //        CREDITS_OTHER (NUMBER[0])
        //        CREDITS_TOTAL (NUMBER[0])
        //        GPA (NUMBER[0])
        //        GPA_ATTEMPTED_CREDITS (NUMBER[0])
        //        GPA_CREDITS (NUMBER[0])
        //        GPA_EARNED_CREDITS (NUMBER[0])
        //        GPA_PASSED_CREDITS (NUMBER[0])
        //        DEANS_LIST (VARCHAR2[63])
        //        DEANS_LIST_DESC (VARCHAR2[255])
        //        CREDIT_LOAD (VARCHAR2[63])
        //        CAMPUS (VARCHAR2[63])
        //        CAMPUS_DESC (VARCHAR2[255])
        //     *  PRIMARY_COLLEGE (VARCHAR2[63])
        //        PRIMARY_COLLEGE_DESC (VARCHAR2[255])
        //     *  PRIMARY_DEPARTMENT (VARCHAR2[63])
        //        PRIMARY_DEPARTMENT_DESC (VARCHAR2[255])
        //     *  PRIMARY_MAJOR (VARCHAR2[63])
        //        PRIMARY_MAJOR_DESC (VARCHAR2[255])
        //     *  PROGRAM_OF_STUDY (VARCHAR2[63])
        //        PROGRAM_OF_STUDY_DESC (VARCHAR2[255])
        //        RESIDENCY (VARCHAR2[63])
        //        RESIDENCY_DESC (VARCHAR2[255])
        //        RESIDENCY_INDICATOR (VARCHAR2[1])
        //        STUDENT_CLASS (VARCHAR2[63])
        //        STUDENT_CLASS_DESC (VARCHAR2[255])
        //        STUDENT_LEVEL (VARCHAR2[63])
        //        STUDENT_LEVEL_DESC (VARCHAR2[255])
        //        STUDENT_TYPE (VARCHAR2[63])
        //        STUDENT_TYPE_DESC (VARCHAR2[255])
        //        TERM_DEGREE (VARCHAR2[63])
        //        TERM_DEGREE_DESC (VARCHAR2[255])
        //        ADMISSIONS_POPULATION (VARCHAR2[63])
        //        ADMISSIONS_POPULATION_DESC (VARCHAR2[255])
        //        SITE (VARCHAR2[63])
        //        SITE_DESC (VARCHAR2[255])
        //        PROGRAM_SITE (VARCHAR2[127])
        // (CSU)  MULTI_SOURCE (VARCHAR2[6])
        //        MULTI_SOURCE_DESC (VARCHAR2[30])
        //        EXTRACT_DATE (DATE[7])

        try (final Statement stmt = odsConn.createStatement()) {

            final String sql = SimpleBuilder.concat(
                    "SELECT DISTINCT PRIMARY_MAJOR, PRIMARY_MAJOR_DESC, PROGRAM_OF_STUDY, PROGRAM_OF_STUDY_DESC ",
                    "FROM CSUBAN.CSUS_ENROLL_TERM_SUMMARY_AH ",
                    "WHERE TERM >= ", startStr, " AND TERM <= ", endStr,
                    " AND MULTI_SOURCE = 'CSU'");

            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final String major = rs.getString("PRIMARY_MAJOR");
                    final String majorDesc = rs.getString("PRIMARY_MAJOR_DESC");
                    final String program = rs.getString("PROGRAM_OF_STUDY");
                    final String programDesc = rs.getString("PROGRAM_OF_STUDY_DESC");

                    final MajorProgramRec rec = new MajorProgramRec(major, majorDesc, program, programDesc);
                    result.add(rec);
                }
            }
        }

        return result;
    }

    /**
     * Loads the file of major/program records and stores the results in a list.
     *
     * @param source the file with source data
     * @return a list of all major/program records found
     */
    public static List<MajorProgramRec> load(final File source) {

        List<MajorProgramRec> result = null;

        final String data = FileLoader.loadFileAsString(source, true);

        if (data != null) {
            try {
                final Object parsed = JSONParser.parseJSON(data);

                if (parsed instanceof final Object[] array) {
                    final String arrayLenStr = Integer.toString(array.length);
                    Log.fine("    Loaded ", arrayLenStr, " major/program records from JSON file");
                    result = new ArrayList<>(500);

                    try {
                        for (final Object obj : array) {
                            if (obj instanceof final JSONObject json) {
                                final MajorProgramRec rec = MajorProgramRec.parse(json);
                                result.add(rec);
                            } else {
                                Log.warning("    Row in major/program JSON file is not JSON object.");
                            }
                        }

                        final int numStudents = result.size();
                        final String numStudentsStr = Integer.toString(numStudents);
                        Log.fine("    Loaded data for ", numStudentsStr, " majors/programs");
                    } catch (final IllegalArgumentException ex) {
                        Log.warning("    Unable to interpret a major/program record in the JSON file.", ex);
                    }
                } else {
                    Log.warning("    Unable to interpret major/program JSON file.");
                }
            } catch (final ParsingException ex) {
                Log.warning("    Unable to load major/program JSON file.", ex);
            }
        }

        return result;
    }
}


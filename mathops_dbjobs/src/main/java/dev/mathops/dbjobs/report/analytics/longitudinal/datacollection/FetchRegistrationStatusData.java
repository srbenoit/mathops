package dev.mathops.dbjobs.report.analytics.longitudinal.datacollection;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.db.DbConnection;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.EnrollmentRec;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.RegistrationStatusRec;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class retrieves data from the ODS and stores it in a local file so we can load and process it without having to
 * do a lengthy ODS query each time.
 */
public enum FetchRegistrationStatusData {
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

        Log.fine("Gathering registration status data.");

        final List<RegistrationStatusRec> registrationStatusRecs = collectRegistrationStatus(odsConn,
                startAcademicPeriod, endAcademicPeriod);

        final HtmlBuilder fileData = new HtmlBuilder(100000);
        fileData.addln("[");
        boolean comma = false;
        for (final RegistrationStatusRec rec : registrationStatusRecs) {
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
        Log.fine("Writing registration status data file to ", absolutePath);

        try (final FileWriter fw = new FileWriter(target)) {
            final String dataString = fileData.toString();
            fw.write(dataString);
        } catch (final IOException ex) {
            Log.warning("Failed to write registration status JSON file.", ex);
        }

        Log.fine("Registration status data gathered.");
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
    private static List<RegistrationStatusRec> collectRegistrationStatus(final DbConnection odsConn,
                                                                         final int startAcademicPeriod,
                                                                         final int endAcademicPeriod)
            throws SQLException {

        final List<RegistrationStatusRec> result = new ArrayList<>(100);

        final String startStr = Integer.toString(startAcademicPeriod);
        final String endStr = Integer.toString(endAcademicPeriod);

        // TABLE: 'ODSMGR.STUDENT_COURSE'
        //        PERSON_UID (NUMBER[0])
        // (join) ID (VARCHAR2[252])
        //        NAME (VARCHAR2[1020])
        //        ACADEMIC_YEAR (VARCHAR2[63])
        //        ACADEMIC_YEAR_DESC (VARCHAR2[255])
        // (join) ACADEMIC_PERIOD (VARCHAR2[63])
        //        ACADEMIC_PERIOD_DESC (VARCHAR2[255])
        //        SUB_ACADEMIC_PERIOD (VARCHAR2[63])
        //        SUB_ACADEMIC_PERIOD_DESC (VARCHAR2[255])
        // (join) COURSE_IDENTIFICATION (VARCHAR2[63])
        //        SUBJECT (VARCHAR2[63])
        //        SUBJECT_DESC (VARCHAR2[255])
        //        COURSE_NUMBER (VARCHAR2[63])
        // (join) COURSE_SECTION_NUMBER (VARCHAR2[63])
        //        COURSE_VERSION (VARCHAR2[255])
        //        BLOCK_REG_SEQUENCE_NUMBER (NUMBER[0])
        //        BLOCK_REGISTRATION (VARCHAR2[63])
        //        BLOCK_REGISTRATION_DESC (VARCHAR2[255])
        //        COURSE_REFERENCE_NUMBER (VARCHAR2[63])
        //        START_DATE (DATE[7])
        //        END_DATE (DATE[7])
        //        LAST_ATTEND_DATE (DATE[7])
        //        EXTENSIONS (NUMBER[0])
        //        FINALIZED_COMPLETION_DATE (DATE[7])
        //        INSTITUTION_COURSE_IND (VARCHAR2[1])
        //        IN_PROGRESS_COURSE_IND (VARCHAR2[1])
        //        TRANSFER_COURSE_IND (VARCHAR2[1])
        //        REGISTRATION_STATUS (VARCHAR2[63])
        //        REGISTRATION_STATUS_DESC (VARCHAR2[255])
        //        REGISTRATION_STATUS_DATE (DATE[7])
        //        COURSE_REGISTER_IND (VARCHAR2[1])
        //     *  WITHDRAWN_IND (VARCHAR2[1])
        //        WAITLIST_IND (VARCHAR2[1])
        //        REGISTER_CENSUS_DATE1_IND (VARCHAR2[1])
        //        REGISTER_CENSUS_DATE2_IND (VARCHAR2[1])
        //        SPECIAL_APPROVAL_IND (VARCHAR2[1])
        //        REGISTRATION_ERROR_FLAG (VARCHAR2[1])
        //        REGISTRATION_SEQUENCE_NUMBER (NUMBER[0])
        //        COURSE_LEVEL (VARCHAR2[63])
        //        COURSE_LEVEL_DESC (VARCHAR2[255])
        //        COLLEGE (VARCHAR2[63])
        //        COLLEGE_DESC (VARCHAR2[255])
        //        DEPARTMENT (VARCHAR2[63])
        //        DEPARTMENT_DESC (VARCHAR2[255])
        //        DIVISION (VARCHAR2[255])
        //        DIVISION_DESC (VARCHAR2[255])
        //        COURSE_TITLE_SHORT (VARCHAR2[63])
        //        COURSE_TITLE_LONG (VARCHAR2[255])
        //        GRADABLE_IND (VARCHAR2[1])
        //        SCHEDULE_TYPE (VARCHAR2[63])
        //        SCHEDULE_TYPE_DESC (VARCHAR2[255])
        //        INSTRUCTION_METHOD (VARCHAR2[63])
        //        INSTRUCTION_METHOD_DESC (VARCHAR2[255])
        //        INTEGRATION_PARTNER (VARCHAR2[63])
        //        INTEGRATION_PARTNER_DESC (VARCHAR2[255])
        //        INTEGRATION_PARTNER_SYSTEM (VARCHAR2[63])
        //        INTEGRATION_PARTNER_SYS_DESC (VARCHAR2[255])
        //        FEEDBACK_DETAIL_EXISTS_IND (VARCHAR2[1])
        //        COURSE_BILLING_CREDITS (NUMBER[0])
        //     *  COURSE_ATTEMPTED_IND (VARCHAR2[1])
        //        COURSE_EARNED_IND (VARCHAR2[1])
        //     *  COURSE_PASSED_IND (VARCHAR2[1])
        //     *  COURSE_FAILED_IND (VARCHAR2[1])
        //        CREDIT_FOR_GPA_IND (VARCHAR2[1])
        //        REPEAT_EVALUATE_IND (VARCHAR2[1])
        //        INCOMPLETE_GRADE_IND (VARCHAR2[1])
        //        LAST_DATE_ATTEND_REQUIRED_IND (VARCHAR2[1])
        //        CREDITS_ATTEMPTED (NUMBER[0])
        //        CREDITS_PASSED (NUMBER[0])
        //        CREDITS_FOR_GPA (NUMBER[0])
        //        CREDITS_EARNED (NUMBER[0])
        //        QUALITY_POINTS (NUMBER[0])
        //        TRADITIONAL_IND (VARCHAR2[1])
        //        EFF_ACADEMIC_PERIOD_OF_GRADE (VARCHAR2[63])
        //        COURSE_CREDITS (NUMBER[0])
        //        HOURS_ATTENDED (NUMBER[0])
        //        CHARGES_WAIVED_IND (VARCHAR2[1])
        //        FINAL_GRADE (VARCHAR2[63])
        //        GRADE_VALUE (NUMBER[0])
        //        GRADE_CHANGE_REASON (VARCHAR2[63])
        //        GRADE_CHANGE_REASON_DESC (VARCHAR2[255])
        //        GRADE_COMMENT (VARCHAR2[63])
        //        GRADE_COMMENT_DESC (VARCHAR2[255])
        //        MID_TERM_GRADE (VARCHAR2[63])
        //        INCOMPLETE_EXT_DATE (DATE[7])
        //        FINAL_GRADE_DATE (DATE[7])
        //        FINAL_GRADE_ROLL_IND (VARCHAR2[1])
        //        GRADE_TYPE (VARCHAR2[63])
        //        GRADE_TYPE_DESC (VARCHAR2[255])
        //        COUNT_IN_GPA_IND (VARCHAR2[1])
        //        REPEAT_COURSE_IND (VARCHAR2[1])
        //        SECTION_ADD_DATE (DATE[7])
        //        CAMPUS (VARCHAR2[63])
        //        CAMPUS_DESC (VARCHAR2[255])
        //        REGISTRATION_USER_ID (VARCHAR2[63])
        //        COURSE_ATTRIBUTES_COUNT (NUMBER[0])
        //        GRADE_CHANGE_COUNT (NUMBER[0])
        //        TRANSFER_COURSE_INSTITUTION (VARCHAR2[255])
        //        TRANSFER_COURSE_INST_DESC (VARCHAR2[255])
        //        COURSE_BILLING_HOLD (NUMBER[0])
        //        COURSE_CREDITS_HOLD (NUMBER[0])
        //        DUPLICATE_COURSE_OVERRIDE_IND (VARCHAR2[1])
        //        LINK_OVERRIDE_IND (VARCHAR2[1])
        //        COREQUISITE_OVERRIDE_IND (VARCHAR2[1])
        //        PREREQUISITE_OVERRIDE_IND (VARCHAR2[1])
        //        TIME_CONFLICT_OVERRIDE_IND (VARCHAR2[1])
        //        CAPACITY_OVERRIDE_IND (VARCHAR2[1])
        //        LEVEL_RESTRICT_OVERRIDE_IND (VARCHAR2[1])
        //        COLLEGE_RESTRICT_OVERRIDE_IND (VARCHAR2[1])
        //        MAJOR_RESTRICT_OVERRIDE_IND (VARCHAR2[1])
        //        CLASS_RESTRICT_OVERRIDE_IND (VARCHAR2[1])
        //        SPECIAL_APPROVAL_OVERRIDE_IND (VARCHAR2[1])
        //        REPEAT_COURSE_OVERRIDE_IND (VARCHAR2[1])
        //        REPEAT_COURSE_CREDIT_OVER_IND (VARCHAR2[1])
        //        TEST_SCORE_OVERRIDE_IND (VARCHAR2[1])
        //        CAMPUS_RESTRICT_OVERRIDE_IND (VARCHAR2[1])
        //        DEGREE_RESTRICT_OVERRIDE_IND (VARCHAR2[1])
        //        PROGRAM_RESTRICT_OVERRIDE_IND (VARCHAR2[1])
        //        DEPARTMENT_RESTRICT_OVER_IND (VARCHAR2[1])
        //        STUDENT_ATTRIBUTE_OVERRIDE_IND (VARCHAR2[1])
        //        STUDENT_COHORT_OVERRIDE_IND (VARCHAR2[1])
        //        MUTUTAL_EXCLUSION_OVERRIDE_IND (VARCHAR2[1])
        //        REGISTRATION_OVERRIDE_IND (VARCHAR2[1])
        //        STUDY_PATH_SEQUENCE (NUMBER[0])
        // (CSU)  MULTI_SOURCE (VARCHAR2[63])
        //        MULTI_SOURCE_DESC (VARCHAR2[255])
        //        PROCESS_GROUP (VARCHAR2[255])
        //        ADMINISTRATIVE_GROUP (VARCHAR2[255])

        try (final Statement stmt = odsConn.createStatement()) {

            final String sql = SimpleBuilder.concat(
                    "SELECT DISTINCT REGISTRATION_STATUS, REGISTRATION_STATUS_DESC ",
                    "FROM ODSMGR.STUDENT_COURSE ",
                    "WHERE MULTI_SOURCE = 'CSU' AND COURSE_LEVEL = 'UG'",
                    " AND to_number(ACADEMIC_PERIOD) >= ", startStr,
                    " AND to_number(ACADEMIC_PERIOD) <= ", endStr,
                    " AND REGISTRATION_STATUS IS NOT NULL");

            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final String regStatus = rs.getString("REGISTRATION_STATUS");
                    final String regStatusDesc = rs.getString("REGISTRATION_STATUS_DESC");

                    final RegistrationStatusRec rec = new RegistrationStatusRec(regStatus, regStatusDesc);
                    result.add(rec);
                }
            }
        }

        return result;
    }

    /**
     * Loads the file of enrollments and stores the results in a map.
     *
     * @param source the file with source data
     * @return a map from student ID to the list of all enrollments for that student
     */
    public static Map<String, List<EnrollmentRec>> load(final File source) {

        Map<String, List<EnrollmentRec>> result = null;

        final String data = FileLoader.loadFileAsString(source, true);

        if (data != null) {
            try {
                final Object parsed = JSONParser.parseJSON(data);

                if (parsed instanceof final Object[] array) {
                    final String arrayLenStr = Integer.toString(array.length);
                    Log.fine("    Loaded ", arrayLenStr, " enrollment records from JSON file");
                    result = new HashMap<>(100000);

                    try {
                        for (final Object obj : array) {
                            if (obj instanceof final JSONObject json) {
                                final EnrollmentRec rec = EnrollmentRec.parse(json);
                                final String stuId = rec.studentId();

                                final List<EnrollmentRec> list = result.computeIfAbsent(stuId,
                                        s -> new ArrayList<>(50));
                                list.add(rec);
                            } else {
                                Log.warning("    Row in enrollment JSON file is not JSON object.");
                            }
                        }

                        final int numStudents = result.size();
                        final String numStudentsStr = Integer.toString(numStudents);
                        Log.fine("    Loaded enrollment data for ", numStudentsStr, " students");
                    } catch (final IllegalArgumentException ex) {
                        Log.warning("    Unable to interpret an enrollment record in the JSON file.", ex);
                    }
                } else {
                    Log.warning("    Unable to interpret enrollment JSON file.");
                }
            } catch (final ParsingException ex) {
                Log.warning("    Unable to load enrollment JSON file.", ex);
            }
        }

        return result;
    }
}
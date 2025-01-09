package dev.mathops.dbjobs.report.analytics.longitudinal.datacollection;

import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.db.DbConnection;
import dev.mathops.dbjobs.report.analytics.longitudinal.data.EnrollmentRec;
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
 * This class retrieves enrollment data from the ODS and stores it in a local file so we can load and process it without
 * having to do a lengthy ODS query each time.
 */
public enum FetchEnrollmentData {
    ;

    /**
     * Gathers student enrollment data.
     *
     * @param odsConn             a connection to the ODS
     * @param startAcademicPeriod the starting academic period
     * @param endAcademicPeriod   the ending academic period
     * @param target              the file to which to write results
     * @param map                 a map from student ID to a list of academic periods in which they were enrolled
     */
    static void gatherEnrollmentData(final DbConnection odsConn, final int startAcademicPeriod,
                                     final int endAcademicPeriod, final File target,
                                     final Map<? super String, List<Integer>> map) throws SQLException {

        Log.fine("Gathering enrollment data.");

        final List<EnrollmentRec> enrollmentRecords = collectEnrollments(odsConn, startAcademicPeriod,
                endAcademicPeriod, map);

        Log.fine("Building enrollment dat file.");
        final HtmlBuilder fileData = new HtmlBuilder(100000);
        fileData.addln("[");
        boolean comma = false;
        for (final EnrollmentRec rec : enrollmentRecords) {
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
        Log.fine("Writing enrollment data file to ", absolutePath);

        try (final FileWriter fw = new FileWriter(target)) {
            final String dataString = fileData.toString();
            fw.write(dataString);
        } catch (final IOException ex) {
            Log.warning("Failed to write enrollment JSON file.", ex);
        }

        Log.fine("Enrollment data gathered.");
    }

    /**
     * Queries student course records from the ODS.
     *
     * @param odsConn             the database connection
     * @param startAcademicPeriod the starting academic period
     * @param endAcademicPeriod   the ending academic period
     * @param map                 a map from student ID to a list of academic periods in which they were enrolled or had
     *                            transfer or AP/IB?CLEP credit recorded
     * @return a list of student course records
     * @throws SQLException if there is an error performing the query
     */
    private static List<EnrollmentRec> collectEnrollments(final DbConnection odsConn,
                                                          final int startAcademicPeriod,
                                                          final int endAcademicPeriod,
                                                          final Map<? super String, List<Integer>> map)
            throws SQLException {

        final String startStr = Integer.toString(startAcademicPeriod);
        final String endStr = Integer.toString(endAcademicPeriod);

        final List<EnrollmentRec> result = new ArrayList<>(10000);

        int numAp = 0;
        int numTransfer = 0;
        int numLocal = 0;
        int numAttempted = 0;
        int numWithdrawn = 0;
        int numPassed = 0;
        int numFailed = 0;

        // TABLE: 'ODSMGR.STUDENT_COURSE'
        //        PERSON_UID (NUMBER[0])
        //        ID (VARCHAR2[252])
        //        NAME (VARCHAR2[1020])
        //        ACADEMIC_YEAR (VARCHAR2[63])
        //        ACADEMIC_YEAR_DESC (VARCHAR2[255])
        // (SPAN) ACADEMIC_PERIOD (VARCHAR2[63])
        //        ACADEMIC_PERIOD_DESC (VARCHAR2[255])
        //        SUB_ACADEMIC_PERIOD (VARCHAR2[63])
        //        SUB_ACADEMIC_PERIOD_DESC (VARCHAR2[255])
        //        COURSE_IDENTIFICATION (VARCHAR2[63])
        //        SUBJECT (VARCHAR2[63])
        //        SUBJECT_DESC (VARCHAR2[255])
        //        COURSE_NUMBER (VARCHAR2[63])
        //        COURSE_SECTION_NUMBER (VARCHAR2[63])
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
        // (UG)   COURSE_LEVEL (VARCHAR2[63])
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
                    "SELECT ID,",
                    "       ACADEMIC_PERIOD,",
                    "       COURSE_IDENTIFICATION,",
                    "       COURSE_SECTION_NUMBER,",
                    "       TRANSFER_COURSE_IND,",
                    "       GRADABLE_IND,",
                    "       FINAL_GRADE,",
                    "       TRANSFER_COURSE_INSTITUTION,",
                    "       REGISTRATION_STATUS,",
                    "       WITHDRAWN_IND,",
                    "       COURSE_ATTEMPTED_IND,",
                    "       COURSE_PASSED_IND,",
                    "       COURSE_FAILED_IND ",
                    "FROM ODSMGR.STUDENT_COURSE ",
                    "WHERE MULTI_SOURCE = 'CSU' AND COURSE_LEVEL = 'UG'",
                    " AND to_number(ACADEMIC_PERIOD) >= ", startStr,
                    " AND to_number(ACADEMIC_PERIOD) <= ", endStr,
                    " AND (REGISTRATION_STATUS IS NULL OR REGISTRATION_STATUS NOT IN (",
                    " 'XF','UW','UN','ZD','DD','ZD','DW','NG','XU','UC','UX','XD','CN','AU'))");

            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final String studentId = rs.getString("ID");
                    final int academicPeriod = rs.getInt("ACADEMIC_PERIOD");
                    final String course = rs.getString("COURSE_IDENTIFICATION");
                    final String section = rs.getString("COURSE_SECTION_NUMBER");
                    final String transferStr = rs.getString("TRANSFER_COURSE_IND");
                    final boolean transfer = "Y".equals(transferStr);
                    final String gradableStr = rs.getString("GRADABLE_IND");
                    final boolean gradable = "Y".equals(gradableStr);
                    final String grade = rs.getString("FINAL_GRADE");
                    final String inst = rs.getString("TRANSFER_COURSE_INSTITUTION");
                    final String regStatus = rs.getString("REGISTRATION_STATUS");
                    final String withdrawnStr = rs.getString("WITHDRAWN_IND");
                    boolean withdrawn = "Y".equals(withdrawnStr);
                    final String attemptedStr = rs.getString("COURSE_ATTEMPTED_IND");
                    final boolean attempted = "Y".equals(attemptedStr);
                    final String passedStr = rs.getString("COURSE_PASSED_IND");
                    boolean passed = "Y".equals(passedStr);
                    final String failedStr = rs.getString("COURSE_FAILED_IND");
                    boolean failed = "Y".equals(failedStr);

                    if ("NGC".equals(grade) || "NG".equals(grade) || "AU".equals(grade) || "TR".equals(grade)
                        || "I".equals(grade) || "XI".equals(grade)) {
                        // Skip non-graded components, audits, grad school transfers, and pending Incompletes
                        continue;
                    }

                    Double gradeValue = null;
                    if (grade != null) {
                        switch (grade) {
                            case "A+", "TA+", "RA+", "XA+" -> gradeValue = Double.valueOf(4.0);
                            case "A", "TA", "RA", "XA" -> gradeValue = Double.valueOf(4.0);
                            case "A-", "TA-", "RA-", "XA-" -> gradeValue = Double.valueOf(3.667);
                            case "B+", "TB+", "RB+", "XB+" -> gradeValue = Double.valueOf(3.333);
                            case "B", "TB", "RB", "XB" -> gradeValue = Double.valueOf(3.0);
                            case "B-", "TB-", "RB-", "XB-" -> gradeValue = Double.valueOf(2.667);
                            case "C+", "TC+", "RC+", "XC+" -> gradeValue = Double.valueOf(2.333);
                            case "C", "TC", "RC", "XC" -> gradeValue = Double.valueOf(2.0);
                            case "C-", "TC-", "RC-", "XC-" -> gradeValue = Double.valueOf(1.667);
                            case "D+", "TD+", "RD+", "XD+" -> gradeValue = Double.valueOf(1.333);
                            case "D", "TD", "RD", "XD" -> gradeValue = Double.valueOf(1.0);
                            case "D-", "TD-", "RD-", "XD-" -> gradeValue = Double.valueOf(0.667);
                            case "F", "TF", "RF", "XF" -> gradeValue = Double.valueOf(0.0);
                            case "U", "TU", "RU", "XU" -> gradeValue = Double.valueOf(0.0);
                            case "S", "TS", "XS" -> gradeValue = Double.valueOf(2.5);
                            case "W" -> withdrawn = true;
                            default -> Log.warning("Unrecognized grade: ", grade, " in ", course);
                        }
                    }

                    final boolean isApIbClep = inst != null && inst.startsWith("X9");
                    boolean valid = true;

                    if (isApIbClep) {
                        ++numAp;
                    } else if (transfer) {
                        ++numTransfer;
                    } else {
                        if (attempted) {
                            ++numAttempted;
                        }
                        if (withdrawn) {
                            ++numWithdrawn;
                        }
                        if (passed) {
                            ++numPassed;
                        }
                        if (failed) {
                            ++numFailed;
                        }

                        if (attempted || passed || failed || withdrawn) {
                            ++numLocal;
                        } else {
                            // This is the case with "U" grades and grades that were "repeat-delete" removed.
                            // Let's try to classify based on the grade

                            if (gradeValue != null) {
                                ++numLocal;
                                final double actualGrade = gradeValue.doubleValue();
                                if (actualGrade < 1.0) {
                                    failed = true;
                                    ++numAttempted;
                                    ++numFailed;
                                } else {
                                    passed = true;
                                    ++numAttempted;
                                    ++numFailed;
                                }
                            } else {
                                valid = false;
                                Log.warning("Strange Registration (ignoring):");
                                Log.fine("    studentId:", studentId);
                                Log.fine("    academicPeriod:", academicPeriod);
                                Log.fine("    courseIdentification:", course);
                                Log.fine("    section:", section);
                                Log.fine("    registrationStatus:", regStatus);
                                Log.fine("    transferCourseInd:", transfer);
                                Log.fine("    attemptedInd:", attempted);
                                Log.fine("    withdrawnInd:", withdrawn);
                                Log.fine("    gradableInd:", gradable);
                                Log.fine("    coursePassedInd:", passed);
                                Log.fine("    coursedFailedInd:", failed);
                                Log.fine("    finalGrade:", grade);
                                Log.fine("    gradeValue:", gradeValue);
                                Log.fine("    inst:", inst);
                            }
                        }
                    }

                    if (valid) {
                        final int flags = (transfer ? EnrollmentRec.TRANSFER : 0)
                                          + (isApIbClep ? EnrollmentRec.AP_IB_CLEP : 0)
                                          + (attempted ? EnrollmentRec.ATTEMPTED : 0)
                                          + (withdrawn ? EnrollmentRec.WITHDRAWN : 0)
                                          + (passed ? EnrollmentRec.PASSED : 0)
                                          + (failed ? EnrollmentRec.FAILED : 0)
                                          + (gradable ? EnrollmentRec.GRADABLE : 0);

                        final EnrollmentRec rec = new EnrollmentRec(studentId, academicPeriod, course, section,
                                flags, grade, gradeValue);
                        result.add(rec);

                        final List<Integer> list = map.computeIfAbsent(studentId, object -> new ArrayList<>(12));
                        final Integer newValue = Integer.valueOf(academicPeriod);
                        if (!list.contains(newValue)) {
                            list.add(newValue);
                        }
                    }
                }
            }

            final int total = result.size();
            final float attemptedPct = 100.0f * (float) numAttempted / (float) numLocal;
            final float withdrawPct = 100.0f * (float) numWithdrawn / (float) numLocal;
            final float passedPct = 100.0f * (float) numPassed / (float) numLocal;
            final float failedPct = 100.0f * (float) numFailed / (float) numLocal;

            Log.fine("Found ", total, " student course records");
            Log.fine("    ", numLocal, " local, ", numTransfer, " transfer, and ", numAp, " AP/IP/CLEP");
            Log.fine("    ", attemptedPct, "% of local courses attempted");
            Log.fine("    ", withdrawPct, "% of local courses withdrawn");
            Log.fine("    ", passedPct, "% of local courses passed");
            Log.fine("    ", failedPct, "% of local courses failed");
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


package dev.mathops.dbjobs.report.analytics.longitudinal;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class retrieves data from the ODS and stores it in a local file so we can load and process it without having to
 * do a lengthy ODS query each time.
 */
public final class RetrieveLongitudinalData {

    /** The database context. */
    private final DbContext odsCtx;

    /**
     * Constructs a new {@code RetrieveLongitudinalData}.
     */
    private RetrieveLongitudinalData() {

        final ContextMap map = ContextMap.getDefaultInstance();

        final DbProfile dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);

        this.odsCtx = dbProfile.getDbContext(ESchemaUse.ODS);
    }

    /**
     * Executes the job.
     *
     * @param startAcademicPeriod the starting academic period
     * @param endAcademicPeriod   the ending academic period
     * @param target              the file to which to write results
     * @return a report
     */
    public String execute(final int startAcademicPeriod, final int endAcademicPeriod, final File target) {

        final Collection<String> report = new ArrayList<>(10);

        if (this.odsCtx == null) {
            report.add("Unable to create ODS database context.");
        } else {
            final DbConnection odsConn = this.odsCtx.checkOutConnection();

            try {
                report.add("Processing");

                final List<StudentCourseRecord> studentCourseRecords = collectStudentCourses(odsConn,
                        startAcademicPeriod, endAcademicPeriod, report);

                final HtmlBuilder fileData = new HtmlBuilder(100000);
                fileData.addln("[");
                boolean comma = false;
                for (final StudentCourseRecord rec : studentCourseRecords) {
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
                report.add("Writing output file to " + absolutePath);

                try (final FileWriter fw = new FileWriter(target)) {
                    final String dataString = fileData.toString();
                    fw.write(dataString);
                } catch (final IOException ex) {
                    Log.warning(ex);
                    report.add("Failed to write target JSON file.");
                }

                report.add("Job completed");
            } catch (final SQLException ex) {
                Log.warning(ex);
                report.add("Failed to query ODS.");
            } finally {
                this.odsCtx.checkInConnection(odsConn);
            }
        }

        final HtmlBuilder htm = new HtmlBuilder(1000);
        for (final String rep : report) {
            htm.addln(rep);
        }

        return htm.toString();
    }

    /**
     * Queries transfer records from the ODS for the Spring semester.
     *
     * @param conn                the database connection
     * @param startAcademicPeriod the starting academic period
     * @param endAcademicPeriod   the ending academic period
     * @param report              a list to which to add report lines
     * @return a list of student course records
     * @throws SQLException if there is an error performing the query
     */
    private static List<StudentCourseRecord> collectStudentCourses(final DbConnection conn,
                                                                   final int startAcademicPeriod,
                                                                   final int endAcademicPeriod,
                                                                   final Collection<? super String> report)
            throws SQLException {

        final List<StudentCourseRecord> result = new ArrayList<>(10000);

        int numAp = 0;
        int numTransfer = 0;
        int numLocal = 0;
        int numPassed = 0;
        int numFailed = 0;
        int numWithdrawn = 0;

        // TABLE: ODSMGR.STUDENT_COURSE
        //        PERSON_UID [NUMBER(22)]
        //        ID [VARCHAR2(252)]
        //        NAME [VARCHAR2(1020)]
        //        ACADEMIC_YEAR [VARCHAR2(252)]
        //        ACADEMIC_YEAR_DESC [VARCHAR2(1020)]
        //        ACADEMIC_PERIOD [VARCHAR2(252)]
        //        ACADEMIC_PERIOD_DESC [VARCHAR2(1020)]
        //        SUB_ACADEMIC_PERIOD [VARCHAR2(252)]
        //        SUB_ACADEMIC_PERIOD_DESC [VARCHAR2(1020)]
        //        COURSE_IDENTIFICATION [VARCHAR2(252)]
        //        SUBJECT [VARCHAR2(252)]
        //        SUBJECT_DESC [VARCHAR2(1020)]
        //        COURSE_NUMBER [VARCHAR2(252)]
        //        COURSE_SECTION_NUMBER [VARCHAR2(252)]
        //        COURSE_VERSION [VARCHAR2(1020)]
        //        BLOCK_REG_SEQUENCE_NUMBER [NUMBER(22)]
        //        BLOCK_REGISTRATION [VARCHAR2(252)]
        //        BLOCK_REGISTRATION_DESC [VARCHAR2(1020)]
        //        COURSE_REFERENCE_NUMBER [VARCHAR2(252)]
        //        START_DATE [DATE(7)]
        //        END_DATE [DATE(7)]
        //        LAST_ATTEND_DATE [DATE(7)]
        //        EXTENSIONS [NUMBER(22)]
        //        FINALIZED_COMPLETION_DATE [DATE(7)]
        //        INSTITUTION_COURSE_IND [VARCHAR2(4)]
        //        IN_PROGRESS_COURSE_IND [VARCHAR2(4)]
        //        TRANSFER_COURSE_IND [VARCHAR2(4)]
        //        REGISTRATION_STATUS [VARCHAR2(252)]
        //        REGISTRATION_STATUS_DESC [VARCHAR2(1020)]
        //        REGISTRATION_STATUS_DATE [DATE(7)]
        //        COURSE_REGISTER_IND [VARCHAR2(4)]
        //        WITHDRAWN_IND [VARCHAR2(4)]
        //        WAITLIST_IND [VARCHAR2(4)]
        //        REGISTER_CENSUS_DATE1_IND [VARCHAR2(4)]
        //        REGISTER_CENSUS_DATE2_IND [VARCHAR2(4)]
        //        SPECIAL_APPROVAL_IND [VARCHAR2(4)]
        //        REGISTRATION_ERROR_FLAG [VARCHAR2(4)]
        //        REGISTRATION_SEQUENCE_NUMBER [NUMBER(22)]
        //        COURSE_LEVEL [VARCHAR2(252)]
        //        COURSE_LEVEL_DESC [VARCHAR2(1020)]
        //        COLLEGE [VARCHAR2(252)]
        //        COLLEGE_DESC [VARCHAR2(1020)]
        //        DEPARTMENT [VARCHAR2(252)]
        //        DEPARTMENT_DESC [VARCHAR2(1020)]
        //        DIVISION [VARCHAR2(1020)]
        //        DIVISION_DESC [VARCHAR2(1020)]
        //        COURSE_TITLE_SHORT [VARCHAR2(252)]
        //        COURSE_TITLE_LONG [VARCHAR2(1020)]
        //        GRADABLE_IND [VARCHAR2(4)]
        //        SCHEDULE_TYPE [VARCHAR2(252)]
        //        SCHEDULE_TYPE_DESC [VARCHAR2(1020)]
        //        INSTRUCTION_METHOD [VARCHAR2(252)]
        //        INSTRUCTION_METHOD_DESC [VARCHAR2(1020)]
        //        INTEGRATION_PARTNER [VARCHAR2(252)]
        //        INTEGRATION_PARTNER_DESC [VARCHAR2(1020)]
        //        INTEGRATION_PARTNER_SYSTEM [VARCHAR2(252)]
        //        INTEGRATION_PARTNER_SYS_DESC [VARCHAR2(1020)]
        //        FEEDBACK_DETAIL_EXISTS_IND [VARCHAR2(1)]
        //        COURSE_BILLING_CREDITS [NUMBER(22)]
        //        COURSE_ATTEMPTED_IND [VARCHAR2(4)]
        //        COURSE_EARNED_IND [VARCHAR2(4)]
        //        COURSE_PASSED_IND [VARCHAR2(4)]
        //        COURSE_FAILED_IND [VARCHAR2(4)]
        //        CREDIT_FOR_GPA_IND [VARCHAR2(4)]
        //        REPEAT_EVALUATE_IND [VARCHAR2(4)]
        //        INCOMPLETE_GRADE_IND [VARCHAR2(4)]
        //        LAST_DATE_ATTEND_REQUIRED_IND [VARCHAR2(4)]
        //        CREDITS_ATTEMPTED [NUMBER(22)]
        //        CREDITS_PASSED [NUMBER(22)]
        //        CREDITS_FOR_GPA [NUMBER(22)]
        //        CREDITS_EARNED [NUMBER(22)]
        //        QUALITY_POINTS [NUMBER(22)]
        //        TRADITIONAL_IND [VARCHAR2(4)]
        //        EFF_ACADEMIC_PERIOD_OF_GRADE [VARCHAR2(252)]
        //        COURSE_CREDITS [NUMBER(22)]
        //        HOURS_ATTENDED [NUMBER(22)]
        //        CHARGES_WAIVED_IND [VARCHAR2(4)]
        //        FINAL_GRADE [VARCHAR2(252)]
        //        GRADE_VALUE [NUMBER(22)]
        //        GRADE_CHANGE_REASON [VARCHAR2(252)]
        //        GRADE_CHANGE_REASON_DESC [VARCHAR2(1020)]
        //        GRADE_COMMENT [VARCHAR2(252)]
        //        GRADE_COMMENT_DESC [VARCHAR2(1020)]
        //        MID_TERM_GRADE [VARCHAR2(252)]
        //        INCOMPLETE_EXT_DATE [DATE(7)]
        //        FINAL_GRADE_DATE [DATE(7)]
        //        FINAL_GRADE_ROLL_IND [VARCHAR2(4)]
        //        GRADE_TYPE [VARCHAR2(252)]
        //        GRADE_TYPE_DESC [VARCHAR2(1020)]
        //        COUNT_IN_GPA_IND [VARCHAR2(4)]
        //        REPEAT_COURSE_IND [VARCHAR2(4)]
        //        SECTION_ADD_DATE [DATE(7)]
        //        CAMPUS [VARCHAR2(252)]
        //        CAMPUS_DESC [VARCHAR2(1020)]
        //        REGISTRATION_USER_ID [VARCHAR2(252)]
        //        COURSE_ATTRIBUTES_COUNT [NUMBER(22)]
        //        GRADE_CHANGE_COUNT [NUMBER(22)]
        //        TRANSFER_COURSE_INSTITUTION [VARCHAR2(1020)]
        //        TRANSFER_COURSE_INST_DESC [VARCHAR2(1020)]
        //        COURSE_BILLING_HOLD [NUMBER(22)]
        //        COURSE_CREDITS_HOLD [NUMBER(22)]
        //        DUPLICATE_COURSE_OVERRIDE_IND [VARCHAR2(4)]
        //        LINK_OVERRIDE_IND [VARCHAR2(4)]
        //        COREQUISITE_OVERRIDE_IND [VARCHAR2(4)]
        //        PREREQUISITE_OVERRIDE_IND [VARCHAR2(4)]
        //        TIME_CONFLICT_OVERRIDE_IND [VARCHAR2(4)]
        //        CAPACITY_OVERRIDE_IND [VARCHAR2(4)]
        //        LEVEL_RESTRICT_OVERRIDE_IND [VARCHAR2(4)]
        //        COLLEGE_RESTRICT_OVERRIDE_IND [VARCHAR2(4)]
        //        MAJOR_RESTRICT_OVERRIDE_IND [VARCHAR2(4)]
        //        CLASS_RESTRICT_OVERRIDE_IND [VARCHAR2(4)]
        //        SPECIAL_APPROVAL_OVERRIDE_IND [VARCHAR2(4)]
        //        REPEAT_COURSE_OVERRIDE_IND [VARCHAR2(4)]
        //        REPEAT_COURSE_CREDIT_OVER_IND [VARCHAR2(4)]
        //        TEST_SCORE_OVERRIDE_IND [VARCHAR2(4)]
        //        CAMPUS_RESTRICT_OVERRIDE_IND [VARCHAR2(4)]
        //        DEGREE_RESTRICT_OVERRIDE_IND [VARCHAR2(4)]
        //        PROGRAM_RESTRICT_OVERRIDE_IND [VARCHAR2(4)]
        //        DEPARTMENT_RESTRICT_OVER_IND [VARCHAR2(4)]
        //        STUDENT_ATTRIBUTE_OVERRIDE_IND [VARCHAR2(4)]
        //        STUDENT_COHORT_OVERRIDE_IND [VARCHAR2(4)]
        //        MUTUTAL_EXCLUSION_OVERRIDE_IND [VARCHAR2(4)]
        //        REGISTRATION_OVERRIDE_IND [VARCHAR2(4)]
        //        STUDY_PATH_SEQUENCE [NUMBER(22)]
        //        MULTI_SOURCE [VARCHAR2(252)]
        //        MULTI_SOURCE_DESC [VARCHAR2(1020)]
        //        PROCESS_GROUP [VARCHAR2(1020)]
        //        ADMINISTRATIVE_GROUP [VARCHAR2(1020)]

        try (final Statement stmt = conn.createStatement()) {

            final String startStr = Integer.toString(startAcademicPeriod);
            final String endStr = Integer.toString(endAcademicPeriod);

            final String sql = SimpleBuilder.concat(
                    "SELECT ID, ACADEMIC_PERIOD, COURSE_IDENTIFICATION, COURSE_SECTION_NUMBER, TRANSFER_COURSE_IND, ",
                    " REGISTRATION_STATUS, WITHDRAWN_IND, COURSE_PASSED_IND, COURSE_FAILED_IND, FINAL_GRADE, ",
                    " TRANSFER_COURSE_INSTITUTION",
                    " FROM ODSMGR.STUDENT_COURSE",
                    " WHERE ACADEMIC_PERIOD >= ", startStr, " AND ACADEMIC_PERIOD <= ", endStr,
                    " AND MULTI_SOURCE = 'CSU' AND COURSE_LEVEL = 'UG'",
                    " AND REGISTRATION_STATUS <> 'XF'",
                    " AND (TRANSFER_COURSE_IND = 'Y' OR REGISTER_CENSUS_DATE1_IND = 'Y')");
//                    " AND COURSE_IDENTIFICATION = 'MATH160'");

            try (final ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    final String studentId = rs.getString("ID");
                    final int academicPeriod = rs.getInt("ACADEMIC_PERIOD");
                    final String course = rs.getString("COURSE_IDENTIFICATION");
                    final String section = rs.getString("COURSE_SECTION_NUMBER");
                    final boolean transfer = "Y".equals(rs.getString("TRANSFER_COURSE_IND"));
                    final String regStatus = rs.getString("REGISTRATION_STATUS");
                    boolean withdrawn = "Y".equals(rs.getString("WITHDRAWN_IND"));
                    boolean passed = "Y".equals(rs.getString("COURSE_PASSED_IND"));
                    boolean failed = "Y".equals(rs.getString("COURSE_FAILED_IND"));
                    final String grade = rs.getString("FINAL_GRADE");
                    final String inst = rs.getString("TRANSFER_COURSE_INSTITUTION");

                    if ("NGC".equals(grade) || "NG".equals(grade) || "AU".equals(grade) || "TR".equals(grade)
                        || "I".equals(grade) || "XI".equals(grade)) {
                        // Skip non-graded components, audits, grad school transfers, and pending Incompletes
                        continue;
                    }

                    Double gradeValue = null;
                    if (grade != null) {
                        if ("A+".equals(grade) || "TA+".equals(grade)
                            || "RA+".equals(grade) || "XA+".equals(grade)) {
                            gradeValue = Double.valueOf(4.0);
                        } else if ("A".equals(grade) || "TA".equals(grade)
                                   || "RA".equals(grade) || "XA".equals(grade)) {
                            gradeValue = Double.valueOf(4.0);
                        } else if ("A-".equals(grade) || "TA-".equals(grade)
                                   || "RA-".equals(grade) || "XA-".equals(grade)) {
                            gradeValue = Double.valueOf(3.667);
                        } else if ("B+".equals(grade) || "TB+".equals(grade)
                                   || "RB+".equals(grade) || "XB+".equals(grade)) {
                            gradeValue = Double.valueOf(3.333);
                        } else if ("B".equals(grade) || "TB".equals(grade)
                                   || "RB".equals(grade) || "XB".equals(grade)) {
                            gradeValue = Double.valueOf(3.0);
                        } else if ("B-".equals(grade) || "TB-".equals(grade)
                                   || "RB-".equals(grade) || "XB-".equals(grade)) {
                            gradeValue = Double.valueOf(2.667);
                        } else if ("C+".equals(grade) || "TC+".equals(grade)
                                   || "RC+".equals(grade) || "XC+".equals(grade)) {
                            gradeValue = Double.valueOf(2.333);
                        } else if ("C".equals(grade) || "TC".equals(grade)
                                   || "RC".equals(grade) || "XC".equals(grade)) {
                            gradeValue = Double.valueOf(2.0);
                        } else if ("C-".equals(grade) || "TC-".equals(grade)
                                   || "RC-".equals(grade) || "XC-".equals(grade)) {
                            gradeValue = Double.valueOf(1.667);
                        } else if ("D+".equals(grade) || "TD+".equals(grade)
                                   || "RD+".equals(grade) || "XD+".equals(grade)) {
                            gradeValue = Double.valueOf(1.333);
                        } else if ("D".equals(grade) || "TD".equals(grade)
                                   || "RD".equals(grade) || "XD".equals(grade)) {
                            gradeValue = Double.valueOf(1.0);
                        } else if ("D-".equals(grade) || "TD-".equals(grade)
                                   || "RD-".equals(grade) || "XD-".equals(grade)) {
                            gradeValue = Double.valueOf(0.667);
                        } else if ("F".equals(grade) || "TF".equals(grade)
                                   || "RF".equals(grade) || "XF".equals(grade)) {
                            gradeValue = Double.valueOf(0.0);
                        } else if ("U".equals(grade) || "TU".equals(grade)
                                   || "RU".equals(grade) || "XU".equals(grade)) {
                            gradeValue = Double.valueOf(0.0);
                        } else if ("S".equals(grade) || "TS".equals(grade) || "XS".equals(grade)) {
                            gradeValue = Double.valueOf(2.5);
                        } else if ("W".equals(grade)) {
                            withdrawn = true;
                        } else {
                            Log.warning("Unrecognized grade: ", grade, " in ", course);
                        }
                    }

                    final boolean isApIbClep = inst != null && inst.startsWith("X9");
                    boolean valid = true;

                    if (isApIbClep) {
                        ++numAp;
                    } else if (transfer) {
                        ++numTransfer;
                    } else {
                        if (passed) {
                            ++numPassed;
                        }
                        if (failed) {
                            ++numFailed;
                        }
                        if (withdrawn) {
                            ++numWithdrawn;
                        }

                        if (passed || failed || withdrawn) {
                            ++numLocal;
                        } else {

                            // This is the case with "U" grades and grades that were "repeat-delete" removed.  Let's try
                            // to classify a bit based on the grade

                            if (gradeValue != null) {
                                ++numLocal;
                                final double actualGrade = gradeValue.doubleValue();
                                if (actualGrade < 1.0) {
                                    failed = true;
                                    ++numFailed;
                                } else {
                                    passed = true;
                                    ++numFailed;
                                }
                            } else {
                                valid = false;
                                report.add("Strange Registration (ignoring):");
                                report.add("    studentId:" + studentId);
                                report.add("    academicPeriod:" + academicPeriod);
                                report.add("    courseIdentification:" + course);
                                report.add("    section:" + section);
                                report.add("    transferCourseInd:" + transfer);
                                report.add("    registrationStatus:" + regStatus);
                                report.add("    withdrawnInd:" + withdrawn);
                                report.add("    coursePassedInd:" + passed);
                                report.add("    coursedFailedInd:" + failed);
                                report.add("    finalGrade:" + grade);
                                report.add("    gradeValue:" + gradeValue);
                                report.add("    inst:" + inst);
                            }
                        }
                    }

                    if (valid) {
                        final StudentCourseRecord rec = new StudentCourseRecord(studentId, academicPeriod, course,
                                section, transfer, isApIbClep, regStatus, withdrawn, passed, failed, grade, gradeValue);
                        result.add(rec);
                    }
                }
            }

            final int total = result.size();
            final float withdrawPct = 100.0f * (float) numWithdrawn / (float) numLocal;
            final float passedPct = 100.0f * (float) numPassed / (float) numLocal;
            final float failedPct = 100.0f * (float) numFailed / (float) numLocal;

            report.add("Found " + total + " student course records");
            report.add("    " + numLocal + " local, " + numTransfer + " transfer, and " + numAp + " AP/IP/CLEP");
            report.add("    " + withdrawPct + "% of local courses withdrawn");
            report.add("    " + passedPct + "% of local courses passed");
            report.add("    " + failedPct + "% of local courses failed");
        }

        return result;
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        final RetrieveLongitudinalData job = new RetrieveLongitudinalData();

        final File dir = new File("C:\\opt\\zircon\\data");

        if (dir.exists() || dir.mkdirs()) {
            final File target = new File(dir, "longitudinal_new.json");

            final String report = job.execute(201000, 202480, target);
            Log.fine(report);
        }
    }
}

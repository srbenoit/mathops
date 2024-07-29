package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawFinalCroll;
import dev.mathops.db.old.rawrecord.RawRecordConstants;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

/**
 * Tests for the {@code RawFinalCrollLogic} class.
 */
final class TestRawFinalCrollLogic {

    /** A term key used in test records. */
    private static final TermKey sm21 = new TermKey("SM21");

    /** A term key used in test records. */
    private static final TermKey fa21 = new TermKey("FA21");

    /** A float used in test records. */
    private static final Float float15 = Float.valueOf(1.5f);

    /** A float used in test records. */
    private static final Float float2 = Float.valueOf(2.0f);

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 2, 3);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 3, 4);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2021, 4, 5);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2021, 5, 6);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2021, 6, 7);

    /** A date used in test records. */
    private static final LocalDate date7 = LocalDate.of(2021, 7, 8);

    /** A date used in test records. */
    private static final LocalDate date8 = LocalDate.of(2021, 8, 9);

    /** A date used in test records. */
    private static final LocalDate date9 = LocalDate.of(2021, 8, 7);

    /** The database profile. */
    private static DbProfile dbProfile = null;

    /** The database context. */
    private static DbContext ctx = null;

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        dbProfile = ContextMap.getDefaultInstance().getCodeProfile(Contexts.INFORMIX_TEST_PATH);
        if (dbProfile == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_TEST_PROFILE));
        }

        ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);
        if (ctx == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PRIMARY_CONTEXT));
        }

        // Make sure we're in the TEST database
        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement();
                     final ResultSet rs = stmt.executeQuery("SELECT descr FROM which_db")) {

                    if (rs.next()) {
                        final String which = rs.getString(1);
                        if (which != null && !"TEST".equals(which.trim())) {
                            throw new IllegalArgumentException(
                                    TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                        }
                    } else {
                        throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                    }
                }
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            throw new IllegalArgumentException(ex);
        }

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM final_croll");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                // Want registrations that span multiple terms, multiple courses and sections within a single term, OT
                // and dropped records, a counted Incomplete, and a non-counted Incomplete

                // Student 111111111 has a SM21 registration in MATH 117/001 (finished) and FA21 registrations in
                // MATH 118/001 (open), 124/001 (not yet open), and 125/001 (dropped)

                final RawFinalCroll raw1 = new RawFinalCroll(sm21, // term
                        "111111111", // stu_id
                        RawRecordConstants.M117, // course
                        "001", // sect
                        Integer.valueOf(1), // pace_order
                        "N", // open_status
                        "A", // grading_option
                        "Y", // completed
                        Integer.valueOf(62), // score
                        "B", // course_grade
                        "Y", // prereq_satis
                        "Y", // init_class_roll
                        "N", // stu_provided
                        "Y", // final_class_roll
                        "B", // exam_placed
                        Integer.valueOf(1), // zero_unit
                        float15, // timeout_factor
                        null, // forfeit_i
                        "N", // i_in_progress
                        null, // i_counted
                        "C", // ctrl_test
                        null, // deferred_f_dt
                        Integer.valueOf(5), // bypass_timeout
                        "D", // instrn_type
                        "E", // registration_status
                        date1, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                final RawFinalCroll raw2 = new RawFinalCroll(fa21, // term
                        "111111111", // stu_id
                        RawRecordConstants.M118, // course
                        "001", // sect
                        Integer.valueOf(1), // pace_order
                        "Y", // open_status
                        "F", // grading_option
                        "N", // completed
                        null, // score
                        null, // course_grade
                        "Y", // prereq_satis
                        "N", // init_class_roll
                        "N", // stu_provided
                        "Y", // final_class_roll
                        "G", // exam_placed
                        null, // zero_unit
                        float2, // timeout_factor
                        null, // forfeit_i
                        "N", // i_in_progress
                        null, // i_counted
                        "H", // ctrl_test
                        date2, // deferred_f_dt
                        Integer.valueOf(0), // bypass_timeout
                        "I", // instrn_type
                        "RS", // registration_status
                        date3, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                final RawFinalCroll raw3 = new RawFinalCroll(fa21, // term
                        "111111111", // stu_id
                        RawRecordConstants.M124, // course
                        "001", // sect
                        Integer.valueOf(2), // pace_order
                        null, // open_status
                        "G", // grading_option
                        "N", // completed
                        null, // score
                        null, // course_grade
                        "N", // prereq_satis
                        "N", // init_class_roll
                        "N", // stu_provided
                        "Y", // final_class_roll
                        "G", // exam_placed
                        null, // zero_unit
                        float2, // timeout_factor
                        null, // forfeit_i
                        "N", // i_in_progress
                        null, // i_counted
                        "C", // ctrl_test
                        null, // deferred_f_dt
                        Integer.valueOf(0), // bypass_timeout
                        "I", // instrn_type
                        "RS", // registration_status
                        date4, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                final RawFinalCroll raw4 = new RawFinalCroll(fa21, // term
                        "111111111", // stu_id
                        RawRecordConstants.M125, // course
                        "001", // sect
                        Integer.valueOf(3), // pace_order
                        "D", // open_status
                        "G", // grading_option
                        "N", // completed
                        null, // score
                        null, // course_grade
                        "N", // prereq_satis
                        "N", // init_class_roll
                        "N", // stu_provided
                        "Y", // final_class_roll
                        "G", // exam_placed
                        null, // zero_unit
                        float2, // timeout_factor
                        null, // forfeit_i
                        "N", // i_in_progress
                        null, // i_counted
                        "C", // ctrl_test
                        null, // deferred_f_dt
                        Integer.valueOf(0), // bypass_timeout
                        "I", // instrn_type
                        "RS", // registration_status
                        date4, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                // Student 222222222 has a FA21 registration in MATH 125/401 (Incomplete from SM21),
                // counted, and a FA21 registration in MATH 126/002 (not yet open)

                final RawFinalCroll raw5 = new RawFinalCroll(fa21, // term
                        "222222222", // stu_id
                        RawRecordConstants.M125, // course
                        "401", // sect
                        Integer.valueOf(1), // pace_order
                        "Y", // open_statuss
                        "A", // grading_option
                        "N", // completed
                        null, // score
                        null, // course_grade
                        "Y", // prereq_satis
                        "Y", // init_class_roll
                        "Y", // stu_provided
                        "Y", // final_class_roll
                        "B", // exam_placed
                        null, // zero_unit
                        null, // timeout_factor
                        null, // forfeit_i
                        "Y", // i_in_progress
                        "Y", // i_counted
                        "N", // ctrl_test
                        null, // deferred_f_dt
                        Integer.valueOf(0), // bypass_timeout
                        "I", // instrn_type
                        "RS", // registration_status
                        date5, // last_class_roll_dt
                        new TermKey("SM21"), // i_term / i_term_yr
                        null); // i_deadline_dt

                final RawFinalCroll raw6 = new RawFinalCroll(fa21, // term
                        "222222222", // stu_id
                        RawRecordConstants.M126, // course
                        "002", // sect
                        Integer.valueOf(2), // pace_order
                        null, // open_status
                        "G", // grading_option
                        "N", // completed
                        null, // score
                        null, // course_grade
                        "Y", // prereq_satis
                        "N", // init_class_roll
                        "N", // stu_provided
                        "Y", // final_class_roll
                        "N", // exam_placed
                        null, // zero_unit
                        null, // timeout_factor
                        null, // forfeit_i
                        "N", // i_in_progress
                        null, // i_counted
                        "N", // ctrl_test
                        null, // deferred_f_dt
                        Integer.valueOf(0), // bypass_timeout
                        "I", // instrn_type
                        "RS", // registration_status
                        date6, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                // Student 333333333 has a FA21 registration in MATH 124/801 (Incomplete from SM21,
                // not counted), and a FA21 registration in MATH 125/550 and MATH 126/801 (not open)

                final RawFinalCroll raw7 = new RawFinalCroll(fa21, // term
                        "333333333", // stu_id
                        RawRecordConstants.M124, // course
                        "801", // sect
                        Integer.valueOf(3), // pace_order
                        "Y", // open_status
                        "A", // grading_option
                        "N", // completed
                        null, // score
                        null, // course_grade
                        "Y", // prereq_satis
                        "N", // init_class_roll
                        "N", // stu_provided
                        "Y", // final_class_roll
                        null, // exam_placed
                        null, // zero_unit
                        null, // timeout_factor
                        null, // forfeit_i
                        "Y", // i_in_progress
                        "N", // i_counted
                        "N", // ctrl_test
                        null, // deferred_f_dt
                        Integer.valueOf(0), // bypass_timeout
                        "I", // instrn_type
                        "RS", // registration_status
                        date7, // last_class_roll_dt
                        new TermKey("SM21"), // i_term / i_term_yr
                        date9); // i_deadline_dt

                final RawFinalCroll raw8 = new RawFinalCroll(fa21, // term
                        "333333333", // stu_id
                        RawRecordConstants.M125, // course
                        "550", // sect
                        null, // pace_order
                        null, // open_status
                        "X", // grading_option
                        "N", // completed
                        null, // score
                        null, // course_grade
                        null, // prereq_satis
                        "Y", // init_class_roll
                        "N", // stu_provided
                        "Y", // final_class_roll
                        null, // exam_placed
                        null, // zero_unit
                        null, // timeout_factor
                        null, // forfeit_i
                        "N", // i_in_progress
                        null, // i_counted
                        "N", // ctrl_test
                        null, // deferred_f_dt
                        Integer.valueOf(0), // bypass_timeout
                        "OT", // instrn_type
                        "RS", // registration_status
                        date7, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                final RawFinalCroll raw9 = new RawFinalCroll(fa21, // term
                        "333333333", // stu_id
                        RawRecordConstants.M126, // course
                        "801", // sect
                        null, // pace_order
                        null, // open_status
                        "X", // grading_option
                        "N", // completed
                        null, // score
                        null, // course_grade
                        null, // prereq_satis
                        "Y", // init_class_roll
                        "N", // stu_provided
                        "Y", // final_class_roll
                        null, // exam_placed
                        null, // zero_unit
                        null, // timeout_factor
                        null, // forfeit_i
                        "N", // i_in_progress
                        null, // i_counted
                        "N", // ctrl_test
                        null, // deferred_f_dt
                        Integer.valueOf(0), // bypass_timeout
                        "I", // instrn_type
                        "RS", // registration_status
                        date8, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                assertTrue(RawFinalCrollLogic.INSTANCE.insert(cache, raw1), "Failed to insert final_croll 1");
                assertTrue(RawFinalCrollLogic.INSTANCE.insert(cache, raw2), "Failed to insert final_croll 2");
                assertTrue(RawFinalCrollLogic.INSTANCE.insert(cache, raw3), "Failed to insert final_croll 3");
                assertTrue(RawFinalCrollLogic.INSTANCE.insert(cache, raw4), "Failed to insert final_croll 4");
                assertTrue(RawFinalCrollLogic.INSTANCE.insert(cache, raw5), "Failed to insert final_croll 5");
                assertTrue(RawFinalCrollLogic.INSTANCE.insert(cache, raw6), "Failed to insert final_croll 6");
                assertTrue(RawFinalCrollLogic.INSTANCE.insert(cache, raw7), "Failed to insert final_croll 7");
                assertTrue(RawFinalCrollLogic.INSTANCE.insert(cache, raw8), "Failed to insert final_croll 8");
                assertTrue(RawFinalCrollLogic.INSTANCE.insert(cache, raw9), "Failed to insert final_croll 9");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing tables: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0003() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawFinalCroll> all = RawFinalCrollLogic.INSTANCE.queryAll(cache);

                assertEquals(9, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;
                boolean found7 = false;
                boolean found8 = false;
                boolean found9 = false;

                for (final RawFinalCroll test : all) {
                    if ("SM21".equals(test.termKey.shortString)
                            && "111111111".equals(test.stuId)
                            && RawRecordConstants.M117.equals(test.course)
                            && "001".equals(test.sect)
                            && Integer.valueOf(1).equals(test.paceOrder)
                            && "N".equals(test.openStatus)
                            && "A".equals(test.gradingOption)
                            && "Y".equals(test.completed)
                            && Integer.valueOf(62).equals(test.score)
                            && "B".equals(test.courseGrade)
                            && "Y".equals(test.prereqSatis)
                            && "Y".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && "B".equals(test.examPlaced)
                            && Integer.valueOf(1).equals(test.zeroUnit)
                            && float15.equals(test.timeoutFactor)
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "C".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(5).equals(test.bypassTimeout)
                            && "D".equals(test.instrnType)
                            && "E".equals(test.registrationStatus)
                            && date1.equals(test.lastClassRollDt)
                            && test.iTermKey == null //
                            && test.iDeadlineDt == null) {

                        found1 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "111111111".equals(test.stuId)
                            && RawRecordConstants.M118.equals(test.course)
                            && "001".equals(test.sect)
                            && Integer.valueOf(1).equals(test.paceOrder)
                            && "Y".equals(test.openStatus)
                            && "F".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && "Y".equals(test.prereqSatis)
                            && "N".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && "G".equals(test.examPlaced)
                            && test.zeroUnit == null
                            && float2.equals(test.timeoutFactor)
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "H".equals(test.ctrlTest)
                            && date2.equals(test.deferredFDt)
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date3.equals(test.lastClassRollDt)
                            && test.iTermKey == null
                            && test.iDeadlineDt == null) {

                        found2 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "111111111".equals(test.stuId)
                            && RawRecordConstants.M124.equals(test.course)
                            && "001".equals(test.sect)
                            && Integer.valueOf(2).equals(test.paceOrder)
                            && test.openStatus == null
                            && "G".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && "N".equals(test.prereqSatis)
                            && "N".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && "G".equals(test.examPlaced)
                            && test.zeroUnit == null
                            && float2.equals(test.timeoutFactor)
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "C".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date4.equals(test.lastClassRollDt)
                            && test.iTermKey == null
                            && test.iDeadlineDt == null) {

                        found3 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "111111111".equals(test.stuId)
                            && RawRecordConstants.M125.equals(test.course)
                            && "001".equals(test.sect)
                            && Integer.valueOf(3).equals(test.paceOrder)
                            && "D".equals(test.openStatus)
                            && "G".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && "N".equals(test.prereqSatis)
                            && "N".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && "G".equals(test.examPlaced)
                            && test.zeroUnit == null
                            && float2.equals(test.timeoutFactor)
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "C".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date4.equals(test.lastClassRollDt)
                            && test.iTermKey == null
                            && test.iDeadlineDt == null) {

                        found4 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "222222222".equals(test.stuId)
                            && RawRecordConstants.M125.equals(test.course)
                            && "401".equals(test.sect)
                            && Integer.valueOf(1).equals(test.paceOrder)
                            && "Y".equals(test.openStatus)
                            && "A".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && "Y".equals(test.prereqSatis)
                            && "Y".equals(test.initClassRoll)
                            && "Y".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && "B".equals(test.examPlaced)
                            && test.zeroUnit == null
                            && test.timeoutFactor == null
                            && test.forfeitI == null
                            && "Y".equals(test.iInProgress)
                            && "Y".equals(test.iCounted)
                            && "N".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date5.equals(test.lastClassRollDt)
                            && "SM".equals(test.iTermKey.termCode)
                            && Integer.valueOf(21).equals(test.iTermKey.shortYear)
                            && test.iDeadlineDt == null) {

                        found5 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "222222222".equals(test.stuId)
                            && RawRecordConstants.M126.equals(test.course)
                            && "002".equals(test.sect)
                            && Integer.valueOf(2).equals(test.paceOrder)
                            && test.openStatus == null
                            && "G".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && "Y".equals(test.prereqSatis)
                            && "N".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && "N".equals(test.examPlaced)
                            && test.zeroUnit == null
                            && test.timeoutFactor == null
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "N".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date6.equals(test.lastClassRollDt)
                            && test.iTermKey == null
                            && test.iDeadlineDt == null) {

                        found6 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "333333333".equals(test.stuId)
                            && RawRecordConstants.M124.equals(test.course)
                            && "801".equals(test.sect)
                            && Integer.valueOf(3).equals(test.paceOrder)
                            && "Y".equals(test.openStatus)
                            && "A".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && "Y".equals(test.prereqSatis)
                            && "N".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && test.examPlaced == null
                            && test.zeroUnit == null
                            && test.timeoutFactor == null
                            && test.forfeitI == null
                            && "Y".equals(test.iInProgress)
                            && "N".equals(test.iCounted)
                            && "N".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date7.equals(test.lastClassRollDt)
                            && "SM".equals(test.iTermKey.termCode)
                            && Integer.valueOf(21).equals(test.iTermKey.shortYear)
                            && date9.equals(test.iDeadlineDt)) {

                        found7 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "333333333".equals(test.stuId)
                            && RawRecordConstants.M125.equals(test.course)
                            && "550".equals(test.sect)
                            && test.paceOrder == null
                            && test.openStatus == null
                            && "X".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && test.prereqSatis == null
                            && "Y".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && test.examPlaced == null
                            && test.zeroUnit == null
                            && test.timeoutFactor == null
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "N".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "OT".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date7.equals(test.lastClassRollDt)
                            && test.iTermKey == null
                            && test.iDeadlineDt == null) {

                        found8 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "333333333".equals(test.stuId)
                            && RawRecordConstants.M126.equals(test.course)
                            && "801".equals(test.sect)
                            && test.paceOrder == null
                            && test.openStatus == null
                            && "X".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && test.prereqSatis == null
                            && "Y".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && test.examPlaced == null
                            && test.zeroUnit == null
                            && test.timeoutFactor == null
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "N".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date8.equals(test.lastClassRollDt)
                            && test.iTermKey == null
                            && test.iDeadlineDt == null) {

                        found9 = true;
                    }
                }

                assertTrue(found1, "final_croll 1 not found");
                assertTrue(found2, "final_croll 2 not found");
                assertTrue(found3, "final_croll 3 not found");
                assertTrue(found4, "final_croll 4 not found");
                assertTrue(found5, "final_croll 5 not found");
                assertTrue(found6, "final_croll 6 not found");
                assertTrue(found7, "final_croll 7 not found");
                assertTrue(found8, "final_croll 8 not found");
                assertTrue(found9, "final_croll 9 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all final_croll rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawFinalCroll raw2 = new RawFinalCroll(fa21, // term
                        "111111111", // stu_id
                        RawRecordConstants.M118, // course
                        "001", // sect
                        Integer.valueOf(1), // pace_order
                        "Y", // open_status
                        "F", // grading_option
                        "N", // completed
                        null, // score
                        null, // course_grade
                        "Y", // prereq_satis
                        "N", // init_class_roll
                        "N", // stu_provided
                        "Y", // final_class_roll
                        "G", // exam_placed
                        null, // zero_unit
                        float2, // timeout_factor
                        null, // forfeit_i
                        "N", // i_in_progress
                        null, // i_counted
                        "H", // ctrl_test
                        date2, // deferred_f_dt
                        Integer.valueOf(0), // bypass_timeout
                        "I", // instrn_type
                        "RS", // registration_status
                        date3, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                final boolean result = RawFinalCrollLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawFinalCroll> all = RawFinalCrollLogic.INSTANCE.queryAll(cache);

                assertEquals(8, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;
                boolean found7 = false;
                boolean found8 = false;
                boolean found9 = false;

                for (final RawFinalCroll test : all) {
                    if ("SM21".equals(test.termKey.shortString)
                            && "111111111".equals(test.stuId)
                            && RawRecordConstants.M117.equals(test.course)
                            && "001".equals(test.sect)
                            && Integer.valueOf(1).equals(test.paceOrder)
                            && "N".equals(test.openStatus)
                            && "A".equals(test.gradingOption)
                            && "Y".equals(test.completed)
                            && Integer.valueOf(62).equals(test.score)
                            && "B".equals(test.courseGrade)
                            && "Y".equals(test.prereqSatis)
                            && "Y".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && "B".equals(test.examPlaced)
                            && Integer.valueOf(1).equals(test.zeroUnit)
                            && float15.equals(test.timeoutFactor)
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "C".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(5).equals(test.bypassTimeout)
                            && "D".equals(test.instrnType)
                            && "E".equals(test.registrationStatus)
                            && date1.equals(test.lastClassRollDt)
                            && test.iTermKey == null
                            && test.iDeadlineDt == null) {

                        found1 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "111111111".equals(test.stuId)
                            && RawRecordConstants.M124.equals(test.course)
                            && "001".equals(test.sect)
                            && Integer.valueOf(2).equals(test.paceOrder)
                            && test.openStatus == null
                            && "G".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && "N".equals(test.prereqSatis)
                            && "N".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && "G".equals(test.examPlaced)
                            && test.zeroUnit == null
                            && float2.equals(test.timeoutFactor)
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "C".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date4.equals(test.lastClassRollDt)
                            && test.iTermKey == null
                            && test.iDeadlineDt == null) {

                        found3 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "111111111".equals(test.stuId)
                            && RawRecordConstants.M125.equals(test.course)
                            && "001".equals(test.sect)
                            && Integer.valueOf(3).equals(test.paceOrder)
                            && "D".equals(test.openStatus)
                            && "G".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && "N".equals(test.prereqSatis)
                            && "N".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && "G".equals(test.examPlaced)
                            && test.zeroUnit == null
                            && float2.equals(test.timeoutFactor)
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "C".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date4.equals(test.lastClassRollDt)
                            && test.iTermKey == null
                            && test.iDeadlineDt == null) {

                        found4 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "222222222".equals(test.stuId)
                            && RawRecordConstants.M125.equals(test.course)
                            && "401".equals(test.sect)
                            && Integer.valueOf(1).equals(test.paceOrder)
                            && "Y".equals(test.openStatus)
                            && "A".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && "Y".equals(test.prereqSatis)
                            && "Y".equals(test.initClassRoll)
                            && "Y".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && "B".equals(test.examPlaced)
                            && test.zeroUnit == null
                            && test.timeoutFactor == null
                            && test.forfeitI == null
                            && "Y".equals(test.iInProgress)
                            && "Y".equals(test.iCounted)
                            && "N".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date5.equals(test.lastClassRollDt)
                            && "SM".equals(test.iTermKey.termCode)
                            && Integer.valueOf(21).equals(test.iTermKey.shortYear)
                            && test.iDeadlineDt == null) {

                        found5 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "222222222".equals(test.stuId)
                            && RawRecordConstants.M126.equals(test.course)
                            && "002".equals(test.sect)
                            && Integer.valueOf(2).equals(test.paceOrder)
                            && test.openStatus == null
                            && "G".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && "Y".equals(test.prereqSatis)
                            && "N".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && "N".equals(test.examPlaced)
                            && test.zeroUnit == null
                            && test.timeoutFactor == null
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "N".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date6.equals(test.lastClassRollDt)
                            && test.iTermKey == null //
                            && test.iDeadlineDt == null) {

                        found6 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "333333333".equals(test.stuId)
                            && RawRecordConstants.M124.equals(test.course)
                            && "801".equals(test.sect)
                            && Integer.valueOf(3).equals(test.paceOrder)
                            && "Y".equals(test.openStatus)
                            && "A".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && "Y".equals(test.prereqSatis)
                            && "N".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && test.examPlaced == null
                            && test.zeroUnit == null
                            && test.timeoutFactor == null
                            && test.forfeitI == null
                            && "Y".equals(test.iInProgress)
                            && "N".equals(test.iCounted)
                            && "N".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date7.equals(test.lastClassRollDt)
                            && "SM".equals(test.iTermKey.termCode)
                            && Integer.valueOf(21).equals(test.iTermKey.shortYear)
                            && date9.equals(test.iDeadlineDt)) {

                        found7 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "333333333".equals(test.stuId)
                            && RawRecordConstants.M125.equals(test.course)
                            && "550".equals(test.sect)
                            && test.paceOrder == null
                            && test.openStatus == null
                            && "X".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && test.prereqSatis == null
                            && "Y".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && test.examPlaced == null
                            && test.zeroUnit == null
                            && test.timeoutFactor == null
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "N".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "OT".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date7.equals(test.lastClassRollDt)
                            && test.iTermKey == null
                            && test.iDeadlineDt == null) {

                        found8 = true;
                    } else if ("FA21".equals(test.termKey.shortString)
                            && "333333333".equals(test.stuId)
                            && RawRecordConstants.M126.equals(test.course)
                            && "801".equals(test.sect)
                            && test.paceOrder == null
                            && test.openStatus == null
                            && "X".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && test.courseGrade == null
                            && test.prereqSatis == null
                            && "Y".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && test.examPlaced == null
                            && test.zeroUnit == null
                            && test.timeoutFactor == null
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "N".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date8.equals(test.lastClassRollDt)
                            && test.iTermKey == null
                            && test.iDeadlineDt == null) {

                        found9 = true;
                    }
                }

                assertTrue(found1, "final_croll 1 not found");
                assertTrue(found3, "final_croll 3 not found");
                assertTrue(found4, "final_croll 4 not found");
                assertTrue(found5, "final_croll 5 not found");
                assertTrue(found6, "final_croll 6 not found");
                assertTrue(found7, "final_croll 7 not found");
                assertTrue(found8, "final_croll 8 not found");
                assertTrue(found9, "final_croll 9 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting final_croll: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM final_croll");
                }

                conn.commit();

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning table: " + ex.getMessage());
        }
    }
}

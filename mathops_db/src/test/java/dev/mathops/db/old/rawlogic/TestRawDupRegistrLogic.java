package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.logic.DbConnection;
import dev.mathops.db.logic.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawDupRegistr;
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
 * Tests for the {@code RawDupRegistrLogic} class.
 */
final class TestRawDupRegistrLogic {

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
                    stmt.executeUpdate("DELETE FROM dup_registr");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawDupRegistr raw1 = new RawDupRegistr(sm21, // term
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

                final RawDupRegistr raw2 = new RawDupRegistr(fa21, // term
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

                assertTrue(RawDupRegistrLogic.INSTANCE.insert(cache, raw1), "Failed to insert dup_registr 1");
                assertTrue(RawDupRegistrLogic.INSTANCE.insert(cache, raw2), "Failed to insert dup_registr 2");
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
                final List<RawDupRegistr> all = RawDupRegistrLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;

                for (final RawDupRegistr test : all) {
                    if (sm21.equals(test.termKey)
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
                    } else if (fa21.equals(test.termKey)
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
                    }
                }

                assertTrue(found1, "dup_registr 1 not found");
                assertTrue(found2, "dup_registr 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all dup_registr rows: " + ex.getMessage());
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
                final RawDupRegistr raw2 = new RawDupRegistr(fa21, // term
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

                final boolean result = RawDupRegistrLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawDupRegistr> all = RawDupRegistrLogic.INSTANCE.queryAll(cache);

                assertEquals(1, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;

                for (final RawDupRegistr test : all) {
                    if (sm21.equals(test.termKey)
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
                        break;
                    }
                }

                assertTrue(found1, "dup_registr 1 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting dup_registr: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("deleteAll results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawDupRegistrLogic.deleteAll(cache), "deleteAll returned false");

                final List<RawDupRegistr> all = RawDupRegistrLogic.INSTANCE.queryAll(cache);

                assertTrue(all.isEmpty(), "Incorrect record count after deleteAll");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying dup_registr rows by term: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM dup_registr");
                }

                conn.commit();

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        }
    }
}

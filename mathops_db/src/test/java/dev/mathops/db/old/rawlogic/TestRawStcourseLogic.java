package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.TermRec;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

/**
 * Tests for the {@code RawStcourseLogic} class.
 */
final class TestRawStcourseLogic {

    /** A term key used in test records. */
    private static final TermKey sm21 = new TermKey("SM21");

    /** A term key used in test records. */
    private static final TermKey fa21 = new TermKey("FA21");

    /** A float used in test records. */
    private static final Float THREE_HALVES = Float.valueOf(1.5f);

    /** A float used in test records. */
    private static final Float TWO = Float.valueOf(2.0f);

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 8, 11);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 12, 14);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 11, 13);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2021, 11, 14);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2021, 5, 12);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2021, 8, 10);

    /** A date used in test records. */
    private static final LocalDate date7 = LocalDate.of(2021, 8, 5);

    /** A date used in test records. */
    private static final LocalDate date8 = LocalDate.of(2021, 8, 6);

    /** A date used in test records. */
    private static final LocalDate date9 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date10 = LocalDate.of(2021, 2, 3);

    /** A date used in test records. */
    private static final LocalDate date11 = LocalDate.of(2021, 3, 4);

    /** A date used in test records. */
    private static final LocalDate date12 = LocalDate.of(2021, 4, 5);

    /** A date used in test records. */
    private static final LocalDate date13 = LocalDate.of(2021, 6, 7);

    /** A date used in test records. */
    private static final LocalDate date14 = LocalDate.of(2021, 7, 8);

    /** A date used in test records. */
    private static final LocalDate date15 = LocalDate.of(2021, 8, 7);

    /** A date used in test records. */
    private static final LocalDate date16 = LocalDate.of(2021, 8, 9);

    /** A date used in test records. */
    private static final LocalDate date22 = LocalDate.of(2022, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date23 = LocalDate.of(2022, 5, 6);

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
                    stmt.executeUpdate("DELETE FROM stcourse");
                    stmt.executeUpdate("DELETE FROM term");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                // Want registrations that span multiple terms, multiple courses and sections within a single term,
                // OT and dropped records, a counted Incomplete, and a non-counted Incomplete

                final TermRec term1 = new TermRec(fa21, date1, date2, "2122", Integer.valueOf(0), date3, date4);

                final TermRec term2 = new TermRec(sm21, date5, date6, "2122", Integer.valueOf(-1), date7, date8);

                // Student 111111111 has a SM21 registration in MATH 117/001 (finished) and FA21 registrations in
                // MATH 118/001 (open), 124/001 (not yet open), 125/001 (dropped),and 126/001 (Ignored)

                final RawStcourse raw1 = new RawStcourse(sm21, // term
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
                        THREE_HALVES, // timeout_factor
                        null, // forfeit_i
                        "N", // i_in_progress
                        null, // i_counted
                        "C", // ctrl_test
                        null, // deferred_f_dt
                        Integer.valueOf(5), // bypass_timeout
                        "D", // instrn_type
                        "E", // registration_status
                        date9, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                final RawStcourse raw2 = new RawStcourse(fa21, // term
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
                        TWO, // timeout_factor
                        null, // forfeit_i
                        "N", // i_in_progress
                        null, // i_counted
                        "H", // ctrl_test
                        date10, // deferred_f_dt
                        Integer.valueOf(0), // bypass_timeout
                        "I", // instrn_type
                        "RS", // registration_status
                        date11, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                final RawStcourse raw3 = new RawStcourse(fa21, // term
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
                        TWO, // timeout_factor
                        null, // forfeit_i
                        "N", // i_in_progress
                        null, // i_counted
                        "C", // ctrl_test
                        null, // deferred_f_dt
                        Integer.valueOf(0), // bypass_timeout
                        "I", // instrn_type
                        "RS", // registration_status
                        date12, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                final RawStcourse raw4 = new RawStcourse(fa21, // term
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
                        TWO, // timeout_factor
                        null, // forfeit_i
                        "N", // i_in_progress
                        null, // i_counted
                        "C", // ctrl_test
                        null, // deferred_f_dt
                        Integer.valueOf(0), // bypass_timeout
                        "I", // instrn_type
                        "RS", // registration_status
                        date12, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                // Student 222222222 has a FA21 registration in MATH 125/401 (Incomplete from SM21), counted, and a
                // FA21 registration in MATH 126/002 (not yet open)

                final RawStcourse raw5 = new RawStcourse(fa21, // term
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
                        date12, // last_class_roll_dt
                        sm21, // i_term / i_term_yr
                        null); // i_deadline_dt

                final RawStcourse raw6 = new RawStcourse(fa21, // term
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
                        date13, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                // Student 333333333 has a FA21 registration in MATH 124/801 (Incomplete from SM21,
                // not counted), and a FA21 registration in MATH 125/550 and MATH 126/801 (not open)

                final RawStcourse raw7 = new RawStcourse(fa21, // term
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
                        date14, // last_class_roll_dt
                        sm21, // i_term / i_term_yr
                        date15); // i_deadline_dt

                final RawStcourse raw8 = new RawStcourse(fa21, // term
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
                        "M", // exam_placed
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
                        date14, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                final RawStcourse raw9 = new RawStcourse(fa21, // term
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
                        date16, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                final RawStcourse raw10 = new RawStcourse(fa21, // term
                        "111111111", // stu_id
                        RawRecordConstants.M126, // course
                        "001", // sect
                        Integer.valueOf(4), // pace_order
                        "G", // open_status
                        "I", // grading_option
                        "N", // completed
                        null, // score
                        null, // course_grade
                        "N", // prereq_satis
                        "N", // init_class_roll
                        "N", // stu_provided
                        "Y", // final_class_roll
                        null, // exam_placed
                        null, // zero_unit
                        TWO, // timeout_factor
                        null, // forfeit_i
                        "N", // i_in_progress
                        null, // i_counted
                        "Z", // ctrl_test
                        date22, // deferred_f_dt
                        Integer.valueOf(0), // bypass_timeout
                        "I", // instrn_type
                        "RS", // registration_status
                        date23, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                final SystemData systemData = cache.getSystemData();

                assertTrue(systemData.insertTerm(term1), "Failed to insert term 1");
                assertTrue(systemData.insertTerm(term2), "Failed to insert term 2");
                assertTrue(RawStcourseLogic.insert(cache, raw1), "Failed to insert stcourse 1");
                assertTrue(RawStcourseLogic.insert(cache, raw2), "Failed to insert stcourse 2");
                assertTrue(RawStcourseLogic.insert(cache, raw3), "Failed to insert stcourse 3");
                assertTrue(RawStcourseLogic.insert(cache, raw4), "Failed to insert stcourse 4");
                assertTrue(RawStcourseLogic.insert(cache, raw5), "Failed to insert stcourse 5");
                assertTrue(RawStcourseLogic.insert(cache, raw6), "Failed to insert stcourse 6");
                assertTrue(RawStcourseLogic.insert(cache, raw7), "Failed to insert stcourse 7");
                assertTrue(RawStcourseLogic.insert(cache, raw8), "Failed to insert stcourse 8");
                assertTrue(RawStcourseLogic.insert(cache, raw9), "Failed to insert stcourse 9");
                assertTrue(RawStcourseLogic.insert(cache, raw10), "Failed to insert stcourse 10");
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
                final List<RawStcourse> all = RawStcourseLogic.queryAll(cache);

                assertEquals(10, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;
                boolean found7 = false;
                boolean found8 = false;
                boolean found9 = false;
                boolean found10 = false;

                for (final RawStcourse test : all) {
                    if (isRec1(test)) {
                        found1 = true;
                    } else if (isRec2(test)) {
                        found2 = true;
                    } else if (isRec3(test)) {
                        found3 = true;
                    } else if (isRec4(test)) {
                        found4 = true;
                    } else if (isRec5(test)) {
                        found5 = true;
                    } else if (isRec6(test)) {
                        found6 = true;
                    } else if (isRec7(test)) {
                        found7 = true;
                    } else if (isRec8(test)) {
                        found8 = true;
                    } else if (isRec9(test)) {
                        found9 = true;
                    } else if (isRec10(test)) {
                        found10 = true;
                    }
                }

                assertTrue(found1, "Stcourse 1 not found");
                assertTrue(found2, "Stcourse 2 not found");
                assertTrue(found3, "Stcourse 3 not found");
                assertTrue(found4, "Stcourse 4 not found");
                assertTrue(found5, "Stcourse 5 not found");
                assertTrue(found6, "Stcourse 6 not found");
                assertTrue(found7, "Stcourse 7 not found");
                assertTrue(found8, "Stcourse 8 not found");
                assertTrue(found9, "Stcourse 9 not found");
                assertTrue(found10, "Stcourse 10 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stcourse rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByTerm results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryByTerm(cache, fa21, true, true);

                assertEquals(9, all.size(), "Incorrect record count from queryByTerm");

                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;
                boolean found7 = false;
                boolean found8 = false;
                boolean found9 = false;
                boolean found10 = false;

                for (final RawStcourse test : all) {
                    if (isRec2(test)) {
                        found2 = true;
                    } else if (isRec3(test)) {
                        found3 = true;
                    } else if (isRec4(test)) {
                        found4 = true;
                    } else if (isRec5(test)) {
                        found5 = true;
                    } else if (isRec6(test)) {
                        found6 = true;
                    } else if (isRec7(test)) {
                        found7 = true;
                    } else if (isRec8(test)) {
                        found8 = true;
                    } else if (isRec9(test)) {
                        found9 = true;
                    } else if (isRec10(test)) {
                        found10 = true;
                    }
                }

                assertTrue(found2, "Stcourse 2 not found");
                assertTrue(found3, "Stcourse 3 not found");
                assertTrue(found4, "Stcourse 4 not found");
                assertTrue(found5, "Stcourse 5 not found");
                assertTrue(found6, "Stcourse 6 not found");
                assertTrue(found7, "Stcourse 7 not found");
                assertTrue(found8, "Stcourse 8 not found");
                assertTrue(found9, "Stcourse 9 not found");
                assertTrue(found10, "Stcourse 10 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stcourse rows by term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByTerm results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryByTerm(cache, fa21, false, true);

                assertEquals(8, all.size(), "Incorrect record count from queryByTerm");

                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;
                boolean found7 = false;
                boolean found9 = false;
                boolean found10 = false;

                for (final RawStcourse test : all) {
                    if (isRec2(test)) {
                        found2 = true;
                    } else if (isRec3(test)) {
                        found3 = true;
                    } else if (isRec4(test)) {
                        found4 = true;
                    } else if (isRec5(test)) {
                        found5 = true;
                    } else if (isRec6(test)) {
                        found6 = true;
                    } else if (isRec7(test)) {
                        found7 = true;
                    } else if (isRec9(test)) {
                        found9 = true;
                    } else if (isRec10(test)) {
                        found10 = true;
                    }
                }

                assertTrue(found2, "Stcourse 2 not found");
                assertTrue(found3, "Stcourse 3 not found");
                assertTrue(found4, "Stcourse 4 not found");
                assertTrue(found5, "Stcourse 5 not found");
                assertTrue(found6, "Stcourse 6 not found");
                assertTrue(found7, "Stcourse 7 not found");
                assertTrue(found9, "Stcourse 9 not found");
                assertTrue(found10, "Stcourse 10 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stcourse rows by term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByTerm results")
    void test0006() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryByTerm(cache, fa21, true, false);

                assertEquals(8, all.size(), "Incorrect record count from queryByTerm");

                boolean found2 = false;
                boolean found3 = false;
                boolean found5 = false;
                boolean found6 = false;
                boolean found7 = false;
                boolean found8 = false;
                boolean found9 = false;
                boolean found10 = false;

                for (final RawStcourse test : all) {
                    if (isRec2(test)) {
                        found2 = true;
                    } else if (isRec3(test)) {
                        found3 = true;
                    } else if (isRec5(test)) {
                        found5 = true;
                    } else if (isRec6(test)) {
                        found6 = true;
                    } else if (isRec7(test)) {
                        found7 = true;
                    } else if (isRec8(test)) {
                        found8 = true;
                    } else if (isRec9(test)) {
                        found9 = true;
                    } else if (isRec10(test)) {
                        found10 = true;
                    }
                }

                assertTrue(found2, "Stcourse 2 not found");
                assertTrue(found3, "Stcourse 3 not found");
                assertTrue(found5, "Stcourse 5 not found");
                assertTrue(found6, "Stcourse 6 not found");
                assertTrue(found7, "Stcourse 7 not found");
                assertTrue(found8, "Stcourse 8 not found");
                assertTrue(found9, "Stcourse 9 not found");
                assertTrue(found10, "Stcourse 10 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stcourse rows by term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryOpenIncompletes results")
    void test0007() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryOpenIncompletes(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryOpenIncompletes");

                boolean found5 = false;
                boolean found7 = false;

                for (final RawStcourse test : all) {
                    if (isRec5(test)) {
                        found5 = true;
                    } else if (isRec7(test)) {
                        found7 = true;
                    }
                }

                assertTrue(found5, "Stcourse 5 not found");
                assertTrue(found7, "Stcourse 7 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stcourse open incompletes: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0008() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryActiveForActiveTerm(cache);

                assertEquals(6, all.size(), "Incorrect record count from queryActiveForActiveTerm");

                boolean found2 = false;
                boolean found3 = false;
                boolean found5 = false;
                boolean found6 = false;
                boolean found7 = false;
                boolean found9 = false;

                for (final RawStcourse test : all) {
                    if (isRec2(test)) {
                        found2 = true;
                    } else if (isRec3(test)) {
                        found3 = true;
                    } else if (isRec5(test)) {
                        found5 = true;
                    } else if (isRec6(test)) {
                        found6 = true;
                    } else if (isRec7(test)) {
                        found7 = true;
                    } else if (isRec9(test)) {
                        found9 = true;
                    }
                }

                assertTrue(found2, "Stcourse 2 not found");
                assertTrue(found3, "Stcourse 3 not found");
                assertTrue(found5, "Stcourse 5 not found");
                assertTrue(found6, "Stcourse 6 not found");
                assertTrue(found7, "Stcourse 7 not found");
                assertTrue(found9, "Stcourse 9 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stcourse rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0009() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryByStudent(cache, "111111111", true, true);

                assertEquals(5, all.size(), "Incorrect record count from queryByStudent");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found10 = false;

                for (final RawStcourse test : all) {
                    if (isRec1(test)) {
                        found1 = true;
                    } else if (isRec2(test)) {
                        found2 = true;
                    } else if (isRec3(test)) {
                        found3 = true;
                    } else if (isRec4(test)) {
                        found4 = true;
                    } else if (isRec10(test)) {
                        found10 = true;
                    }
                }

                assertTrue(found1, "Stcourse 1 not found");
                assertTrue(found2, "Stcourse 2 not found");
                assertTrue(found3, "Stcourse 3 not found");
                assertTrue(found4, "Stcourse 4 not found");
                assertTrue(found10, "Stcourse 10 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stcourse rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0010() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryByStudent(cache, "111111111", true, false);

                assertEquals(4, all.size(), "Incorrect record count from queryByStudent");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found10 = false;

                for (final RawStcourse test : all) {
                    if (isRec1(test)) {
                        found1 = true;
                    } else if (isRec2(test)) {
                        found2 = true;
                    } else if (isRec3(test)) {
                        found3 = true;
                    } else if (isRec10(test)) {
                        found10 = true;
                    }
                }

                assertTrue(found1, "Stcourse 1 not found");
                assertTrue(found2, "Stcourse 2 not found");
                assertTrue(found3, "Stcourse 3 not found");
                assertTrue(found10, "Stcourse 10 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stcourse rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0011() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryByStudent(cache, "222222222", true, true);

                assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

                boolean found5 = false;
                boolean found6 = false;

                for (final RawStcourse test : all) {
                    if (isRec5(test)) {
                        found5 = true;
                    } else if (isRec6(test)) {
                        found6 = true;
                    }
                }

                assertTrue(found5, "Stcourse 5 not found");
                assertTrue(found6, "Stcourse 6 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stcourse rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0012() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryByStudent(cache, "333333333", true, true);

                assertEquals(3, all.size(), "Incorrect record count from queryByStudent");

                boolean found7 = false;
                boolean found8 = false;
                boolean found9 = false;

                for (final RawStcourse test : all) {
                    if (isRec7(test)) {
                        found7 = true;
                    } else if (isRec8(test)) {
                        found8 = true;
                    } else if (isRec9(test)) {
                        found9 = true;
                    }
                }

                assertTrue(found7, "Stcourse 7 not found");
                assertTrue(found8, "Stcourse 8 not found");
                assertTrue(found9, "Stcourse 9 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stcourse rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0013() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryByStudent(cache, "333333333", false, false);

                assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

                boolean found7 = false;
                boolean found9 = false;

                for (final RawStcourse test : all) {
                    if (isRec7(test)) {
                        found7 = true;
                    } else if (isRec9(test)) {
                        found9 = true;
                    }
                }

                assertTrue(found7, "Stcourse 7 not found");
                assertTrue(found9, "Stcourse 9 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stcourse rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0014() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryByStudent(cache, "111111111", fa21, true, true);

                assertEquals(4, all.size(), "Incorrect record count from queryByStudent");

                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found10 = false;

                for (final RawStcourse test : all) {
                    if (isRec2(test)) {
                        found2 = true;
                    } else if (isRec3(test)) {
                        found3 = true;
                    } else if (isRec4(test)) {
                        found4 = true;
                    } else if (isRec10(test)) {
                        found10 = true;
                    }
                }

                assertTrue(found2, "Stcourse 2 not found");
                assertTrue(found3, "Stcourse 3 not found");
                assertTrue(found4, "Stcourse 4 not found");
                assertTrue(found10, "Stcourse 10 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stcourse rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0015() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryByStudent(cache, "111111111", fa21, true, false);

                assertEquals(3, all.size(), "Incorrect record count from queryByStudent");

                boolean found2 = false;
                boolean found3 = false;
                boolean found10 = false;

                for (final RawStcourse test : all) {
                    if (isRec2(test)) {
                        found2 = true;
                    } else if (isRec3(test)) {
                        found3 = true;
                    } else if (isRec10(test)) {
                        found10 = true;
                    }
                }

                assertTrue(found2, "Stcourse 2 not found");
                assertTrue(found3, "Stcourse 3 not found");
                assertTrue(found10, "Stcourse 10 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stcourse rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0016() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryByStudent(cache, "222222222", fa21, true, true);

                assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

                boolean found5 = false;
                boolean found6 = false;

                for (final RawStcourse test : all) {
                    if (isRec5(test)) {
                        found5 = true;
                    } else if (isRec6(test)) {
                        found6 = true;
                    }
                }

                assertTrue(found5, "Stcourse 5 not found");
                assertTrue(found6, "Stcourse 6 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stcourse rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0017() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryByStudent(cache, "333333333", fa21, true, true);

                assertEquals(3, all.size(), "Incorrect record count from queryByStudent");

                boolean found7 = false;
                boolean found8 = false;
                boolean found9 = false;

                for (final RawStcourse test : all) {
                    if (isRec7(test)) {
                        found7 = true;
                    } else if (isRec8(test)) {
                        found8 = true;
                    } else if (isRec9(test)) {
                        found9 = true;
                    }
                }

                assertTrue(found7, "Stcourse 7 not found");
                assertTrue(found8, "Stcourse 8 not found");
                assertTrue(found9, "Stcourse 9 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stcourse rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0018() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryByStudent(cache, "333333333", fa21, true, true);

                assertEquals(3, all.size(), "Incorrect record count from queryByStudent");

                boolean found7 = false;
                boolean found8 = false;
                boolean found9 = false;

                for (final RawStcourse test : all) {
                    if (isRec7(test)) {
                        found7 = true;
                    } else if (isRec8(test)) {
                        found8 = true;
                    } else if (isRec9(test)) {
                        found9 = true;
                    }
                }

                assertTrue(found7, "Stcourse 7 not found");
                assertTrue(found8, "Stcourse 8 not found");
                assertTrue(found9, "Stcourse 9 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stcourse rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0019() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryByStudent(cache, "333333333", fa21, false, false);

                assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

                boolean found7 = false;
                boolean found9 = false;

                for (final RawStcourse test : all) {
                    if (isRec7(test)) {
                        found7 = true;
                    } else if (isRec9(test)) {
                        found9 = true;
                    }
                }

                assertTrue(found7, "Stcourse 7 not found");
                assertTrue(found9, "Stcourse 9 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stcourse rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryCreditByExam results")
    void test0020() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.queryCreditByExam(cache, "333333333");

                assertEquals(1, all.size(), "Incorrect record count from queryCreditByExam");

                boolean found8 = false;

                for (final RawStcourse test : all) {
                    if (isRec8(test)) {
                        found8 = true;
                    }
                }

                assertTrue(found8, "Stcourse 8 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stcourse rows with OT type: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getActiveForStudent results")
    void test0021() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.getActiveForStudent(cache, "111111111", fa21);

                assertEquals(2, all.size(), "Incorrect record count from getActiveForStudent");

                boolean found2 = false;
                boolean found3 = false;

                for (final RawStcourse test : all) {
                    if (isRec2(test)) {
                        found2 = true;
                    } else if (isRec3(test)) {
                        found3 = true;
                    }
                }

                assertTrue(found2, "Stcourse 2 not found");
                assertTrue(found3, "Stcourse 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stcourse rows active for student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getPaced results")
    void test0022() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.getPaced(cache, "111111111");

                assertEquals(2, all.size(), "Incorrect record count from getPaced");

                boolean found2 = false;
                boolean found3 = false;

                for (final RawStcourse test : all) {
                    if (isRec2(test)) {
                        found2 = true;
                    } else if (isRec3(test)) {
                        found3 = true;
                    }
                }

                assertTrue(found2, "Stcourse 2 not found");
                assertTrue(found3, "Stcourse 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying paced stcourse rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getRegistration results")
    void test0023() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawStcourse reg = RawStcourseLogic.getRegistration(cache, "111111111", RawRecordConstants.M118);

                assertNotNull(reg, "null returned by getRegistration");

                final boolean found2 = isRec2(reg);

                assertTrue(found2, "Stcourse 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stcourse rows by student, course: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0024() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawStcourse reg = RawStcourseLogic.getRegistration(cache, "333333333", RawRecordConstants.M125);

                assertNotNull(reg, "null returned by getRegistration");

                final boolean found8 = isRec8(reg);

                assertTrue(found8, "Stcourse 8 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stcourse rows by student, course: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudentCourse results")
    void test0025() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawStcourse reg = RawStcourseLogic.getRegistration(cache, "111111111", RawRecordConstants.M125);

                assertNull(reg, "record returned by getRegistration");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stcourse rows by student, course: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getAllPriorCompleted results")
    void test0026() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.getAllPriorCompleted(cache, "111111111");

                assertEquals(1, all.size(), "Incorrect record count from getAllPriorCompleted");

                boolean found1 = false;

                for (final RawStcourse test : all) {
                    if (isRec1(test)) {
                        found1 = true;
                    }
                }

                assertTrue(found1, "Stcourse 1 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying prior completed stcourse rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getAllPrior results")
    void test0027() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.getAllPrior(cache, "111111111", fa21);

                assertEquals(1, all.size(), "Incorrect record count from getAllPrior");

                boolean found1 = false;

                for (final RawStcourse test : all) {
                    if (isRec1(test)) {
                        found1 = true;
                    }
                }

                assertTrue(found1, "Stcourse 1 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying prior stcourse rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getHistory results")
    void test0028() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStcourse> all = RawStcourseLogic.getHistory(cache, "111111111");

                assertEquals(5, all.size(), "Incorrect record count from queryHistory");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found10 = false;

                for (final RawStcourse test : all) {
                    if (isRec1(test)) {
                        found1 = true;
                    } else if (isRec2(test)) {
                        found2 = true;
                    } else if (isRec3(test)) {
                        found3 = true;
                    } else if (isRec4(test)) {
                        found4 = true;
                    } else if (isRec10(test)) {
                        found10 = true;
                    }
                }

                assertTrue(found1, "Stcourse 1 not found");
                assertTrue(found2, "Stcourse 2 not found");
                assertTrue(found3, "Stcourse 3 not found");
                assertTrue(found4, "Stcourse 4 not found");
                assertTrue(found10, "Stcourse 10 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stcourse history: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updatePaceOrder results")
    void test0029() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final boolean result = RawStcourseLogic.updatePaceOrder(cache, "111111111", RawRecordConstants.M118,
                        "001", fa21, Integer.valueOf(2));

                assertTrue(result, "updatePaceOrder returned false");

                final RawStcourse reg = RawStcourseLogic.getRegistration(cache, "111111111", RawRecordConstants.M118);

                assertNotNull(reg, "null returned by getRegistration");

                final boolean correct = fa21.equals(reg.termKey)
                        && "111111111".equals(reg.stuId)
                        && RawRecordConstants.M118.equals(reg.course)
                        && "001".equals(reg.sect)
                        && Integer.valueOf(2).equals(reg.paceOrder)
                        && "Y".equals(reg.openStatus)
                        && "F".equals(reg.gradingOption)
                        && "N".equals(reg.completed)
                        && reg.score == null
                        && reg.courseGrade == null
                        && "Y".equals(reg.prereqSatis)
                        && "N".equals(reg.initClassRoll)
                        && "N".equals(reg.stuProvided)
                        && "Y".equals(reg.finalClassRoll)
                        && "G".equals(reg.examPlaced)
                        && reg.zeroUnit == null
                        && TWO.equals(reg.timeoutFactor)
                        && reg.forfeitI == null
                        && "N".equals(reg.iInProgress)
                        && reg.iCounted == null
                        && "H".equals(reg.ctrlTest)
                        && date10.equals(reg.deferredFDt)
                        && Integer.valueOf(0).equals(reg.bypassTimeout)
                        && "I".equals(reg.instrnType)
                        && "RS".equals(reg.registrationStatus)
                        && date11.equals(reg.lastClassRollDt)
                        && reg.iTermKey == null
                        && reg.iDeadlineDt == null;

                assertTrue(correct, "updatePaceOrder did not update field");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updatePaceOrder: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateOpenStatusAndFinalClassRoll results")
    void test0030() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final LocalDate newLast = date23;
                final boolean result = RawStcourseLogic.updateOpenStatusAndFinalClassRoll(cache, "111111111",
                        RawRecordConstants.M118, "001", fa21, "N", "N", newLast);

                assertTrue(result, "updatePaceOrder returned false");

                final RawStcourse reg = RawStcourseLogic.getRegistration(cache, "111111111", RawRecordConstants.M118);

                assertNotNull(reg, "null returned by getRegistration");

                final boolean correct = fa21.equals(reg.termKey)
                        && "111111111".equals(reg.stuId)
                        && RawRecordConstants.M118.equals(reg.course)
                        && "001".equals(reg.sect)
                        && Integer.valueOf(2).equals(reg.paceOrder)
                        && "N".equals(reg.openStatus)
                        && "F".equals(reg.gradingOption)
                        && "N".equals(reg.completed)
                        && reg.score == null
                        && reg.courseGrade == null
                        && "Y".equals(reg.prereqSatis)
                        && "N".equals(reg.initClassRoll)
                        && "N".equals(reg.stuProvided)
                        && "N".equals(reg.finalClassRoll)
                        && "G".equals(reg.examPlaced)
                        && reg.zeroUnit == null
                        && TWO.equals(reg.timeoutFactor)
                        && reg.forfeitI == null
                        && "N".equals(reg.iInProgress)
                        && reg.iCounted == null
                        && "H".equals(reg.ctrlTest)
                        && date10.equals(reg.deferredFDt)
                        && Integer.valueOf(0).equals(reg.bypassTimeout)
                        && "I".equals(reg.instrnType)
                        && "RS".equals(reg.registrationStatus)
                        && newLast.equals(reg.lastClassRollDt)
                        && reg.iTermKey == null
                        && reg.iDeadlineDt == null;

                assertTrue(correct, "updateOpenStatusAndFinalClassRoll did not update field");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateOpenStatusAndFinalClassRoll: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updatePrereqSatisfied results")
    void test0031() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final boolean result = RawStcourseLogic.updatePrereqSatisfied(cache, "111111111",
                        RawRecordConstants.M118, "001", fa21, "P");

                assertTrue(result, "updatePrereqSatisfied returned false");

                final RawStcourse reg = RawStcourseLogic.getRegistration(cache, "111111111", RawRecordConstants.M118);

                assertNotNull(reg, "null returned by getRegistration");

                final boolean correct = fa21.equals(reg.termKey)
                        && "111111111".equals(reg.stuId)
                        && RawRecordConstants.M118.equals(reg.course)
                        && "001".equals(reg.sect)
                        && Integer.valueOf(2).equals(reg.paceOrder)
                        && "N".equals(reg.openStatus)
                        && "F".equals(reg.gradingOption)
                        && "N".equals(reg.completed)
                        && reg.score == null
                        && reg.courseGrade == null
                        && "P".equals(reg.prereqSatis)
                        && "N".equals(reg.initClassRoll)
                        && "N".equals(reg.stuProvided)
                        && "N".equals(reg.finalClassRoll)
                        && "G".equals(reg.examPlaced)
                        && reg.zeroUnit == null
                        && TWO.equals(reg.timeoutFactor)
                        && reg.forfeitI == null
                        && "N".equals(reg.iInProgress)
                        && reg.iCounted == null
                        && "H".equals(reg.ctrlTest)
                        && date10.equals(reg.deferredFDt)
                        && Integer.valueOf(0).equals(reg.bypassTimeout)
                        && "I".equals(reg.instrnType)
                        && "RS".equals(reg.registrationStatus)
                        && date23.equals(reg.lastClassRollDt)
                        && reg.iTermKey == null
                        && reg.iDeadlineDt == null;

                assertTrue(correct, "updateOpenStatusAndFinalClassRoll did not update field");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updatePrereqSatisfied: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateCompletedScoreGrade results")
    void test0032() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final boolean result = RawStcourseLogic.updateCompletedScoreGrade(cache, "111111111",
                        RawRecordConstants.M118, "001", fa21, "Y", Integer.valueOf(70), "A");

                assertTrue(result, "updateCompletedScoreGrade returned false");

                final RawStcourse reg = RawStcourseLogic.getRegistration(cache, "111111111", RawRecordConstants.M118);

                assertNotNull(reg, "null returned by getRegistration");

                final boolean correct = fa21.equals(reg.termKey)
                        && "111111111".equals(reg.stuId)
                        && RawRecordConstants.M118.equals(reg.course)
                        && "001".equals(reg.sect)
                        && Integer.valueOf(2).equals(reg.paceOrder)
                        && "N".equals(reg.openStatus)
                        && "F".equals(reg.gradingOption)
                        && "Y".equals(reg.completed)
                        && Integer.valueOf(70).equals(reg.score)
                        && "A".equals(reg.courseGrade)
                        && "P".equals(reg.prereqSatis)
                        && "N".equals(reg.initClassRoll)
                        && "N".equals(reg.stuProvided)
                        && "N".equals(reg.finalClassRoll)
                        && "G".equals(reg.examPlaced)
                        && reg.zeroUnit == null
                        && TWO.equals(reg.timeoutFactor)
                        && reg.forfeitI == null
                        && "N".equals(reg.iInProgress)
                        && reg.iCounted == null
                        && "H".equals(reg.ctrlTest)
                        && date10.equals(reg.deferredFDt)
                        && Integer.valueOf(0).equals(reg.bypassTimeout)
                        && "I".equals(reg.instrnType)
                        && "RS".equals(reg.registrationStatus)
                        && date23.equals(reg.lastClassRollDt)
                        && reg.iTermKey == null
                        && reg.iDeadlineDt == null;

                assertTrue(correct, "updateCompletedScoreGrade did not update field");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateCompletedScoreGrade: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateDroppedGrade results")
    void test0033() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final boolean result = RawStcourseLogic.updateDroppedGrade(cache, "111111111", RawRecordConstants.M125,
                        "001", fa21, date12, "F");
                assertTrue(result, "updateDroppedGrade returned false");

                final List<RawStcourse> all = RawStcourseLogic.queryByStudent(cache, "111111111", fa21, false,
                        true);

                Log.info("queryByStudent returned " + all.size());

                boolean found4 = false;

                for (final RawStcourse test : all) {

                    Log.info(test);

                    if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && RawRecordConstants.M125.equals(test.course)
                            && "001".equals(test.sect)
                            && Integer.valueOf(3).equals(test.paceOrder)
                            && "D".equals(test.openStatus)
                            && "G".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && "F".equals(test.courseGrade)
                            && "N".equals(test.prereqSatis)
                            && "N".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && "G".equals(test.examPlaced)
                            && test.zeroUnit == null
                            && TWO.equals(test.timeoutFactor)
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "C".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date12.equals(test.lastClassRollDt)
                            && test.iTermKey == null
                            && test.iDeadlineDt == null) {

                        found4 = true;
                        break;
                    }
                }

                assertTrue(found4, "updateDroppedGrade did not update field");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateDroppedGrade: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateInstructionType results")
    void test0034() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final boolean result = RawStcourseLogic.updateInstructionType(cache, "111111111",
                        RawRecordConstants.M118, "001", fa21, "Z");

                assertTrue(result, "updateInstructionType returned false");

                final RawStcourse reg = RawStcourseLogic.getRegistration(cache, "111111111", RawRecordConstants.M118);

                assertNotNull(reg, "null returned by getRegistration");

                final boolean correct = fa21.equals(reg.termKey)
                        && "111111111".equals(reg.stuId)
                        && RawRecordConstants.M118.equals(reg.course)
                        && "001".equals(reg.sect)
                        && Integer.valueOf(2).equals(reg.paceOrder)
                        && "N".equals(reg.openStatus)
                        && "F".equals(reg.gradingOption)
                        && "Y".equals(reg.completed)
                        && Integer.valueOf(70).equals(reg.score)
                        && "A".equals(reg.courseGrade)
                        && "P".equals(reg.prereqSatis)
                        && "N".equals(reg.initClassRoll)
                        && "N".equals(reg.stuProvided)
                        && "N".equals(reg.finalClassRoll)
                        && "G".equals(reg.examPlaced)
                        && reg.zeroUnit == null
                        && TWO.equals(reg.timeoutFactor)
                        && reg.forfeitI == null
                        && "N".equals(reg.iInProgress)
                        && reg.iCounted == null
                        && "H".equals(reg.ctrlTest)
                        && date10.equals(reg.deferredFDt)
                        && Integer.valueOf(0).equals(reg.bypassTimeout)
                        && "Z".equals(reg.instrnType)
                        && "RS".equals(reg.registrationStatus)
                        && date23.equals(reg.lastClassRollDt)
                        && reg.iTermKey == null
                        && reg.iDeadlineDt == null;

                assertTrue(correct, "updateInstructionType did not update field");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateInstructionType: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateGradingOption results")
    void test0035() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final boolean result = RawStcourseLogic.updateGradingOption(cache, "111111111",
                        RawRecordConstants.M118, "001", fa21, "X");

                assertTrue(result, "updateGradingOption returned false");

                final RawStcourse reg = RawStcourseLogic.getRegistration(cache, "111111111", RawRecordConstants.M118);

                assertNotNull(reg, "null returned by getRegistration");

                final boolean correct = fa21.equals(reg.termKey)
                        && "111111111".equals(reg.stuId)
                        && RawRecordConstants.M118.equals(reg.course)
                        && "001".equals(reg.sect)
                        && Integer.valueOf(2).equals(reg.paceOrder)
                        && "N".equals(reg.openStatus)
                        && "X".equals(reg.gradingOption)
                        && "Y".equals(reg.completed)
                        && Integer.valueOf(70).equals(reg.score)
                        && "A".equals(reg.courseGrade)
                        && "P".equals(reg.prereqSatis)
                        && "N".equals(reg.initClassRoll)
                        && "N".equals(reg.stuProvided)
                        && "N".equals(reg.finalClassRoll)
                        && "G".equals(reg.examPlaced)
                        && reg.zeroUnit == null
                        && TWO.equals(reg.timeoutFactor)
                        && reg.forfeitI == null
                        && "N".equals(reg.iInProgress)
                        && reg.iCounted == null
                        && "H".equals(reg.ctrlTest)
                        && date10.equals(reg.deferredFDt)
                        && Integer.valueOf(0).equals(reg.bypassTimeout)
                        && "Z".equals(reg.instrnType)
                        && "RS".equals(reg.registrationStatus)
                        && date23.equals(reg.lastClassRollDt)
                        && reg.iTermKey == null
                        && reg.iDeadlineDt == null;

                assertTrue(correct, "updateGradingOption did not update field");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateGradingOption: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateRegistrationStatus results")
    void test0036() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final boolean result = RawStcourseLogic.updateRegistrationStatus(cache, "111111111",
                        RawRecordConstants.M118, "001", fa21, "W");

                assertTrue(result, "updateGradingOption returned false");

                final RawStcourse reg = RawStcourseLogic.getRegistration(cache, "111111111", RawRecordConstants.M118);

                assertNotNull(reg, "null returned by getRegistration");

                final boolean correct = fa21.equals(reg.termKey)
                        && "111111111".equals(reg.stuId)
                        && RawRecordConstants.M118.equals(reg.course)
                        && "001".equals(reg.sect)
                        && Integer.valueOf(2).equals(reg.paceOrder)
                        && "N".equals(reg.openStatus)
                        && "X".equals(reg.gradingOption)
                        && "Y".equals(reg.completed)
                        && Integer.valueOf(70).equals(reg.score)
                        && "A".equals(reg.courseGrade)
                        && "P".equals(reg.prereqSatis)
                        && "N".equals(reg.initClassRoll)
                        && "N".equals(reg.stuProvided)
                        && "N".equals(reg.finalClassRoll)
                        && "G".equals(reg.examPlaced)
                        && reg.zeroUnit == null
                        && TWO.equals(reg.timeoutFactor)
                        && reg.forfeitI == null
                        && "N".equals(reg.iInProgress)
                        && reg.iCounted == null
                        && "H".equals(reg.ctrlTest)
                        && date10.equals(reg.deferredFDt)
                        && Integer.valueOf(0).equals(reg.bypassTimeout)
                        && "Z".equals(reg.instrnType)
                        && "W".equals(reg.registrationStatus)
                        && date23.equals(reg.lastClassRollDt)
                        && reg.iTermKey == null
                        && reg.iDeadlineDt == null;

                assertTrue(correct, "updateRegistrationStatus did not update field");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateRegistrationStatus: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0037() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawStcourse raw2 = new RawStcourse(fa21, // term
                        "111111111", // stu_id
                        RawRecordConstants.M118, // course
                        "001", // sect
                        Integer.valueOf(2), // pace_order
                        "N", // open_status
                        "X", // grading_option
                        "Y", // completed
                        Integer.valueOf(70), // score
                        "A", // course_grade
                        "P", // prereq_satis
                        "N", // init_class_roll
                        "N", // stu_provided
                        "N", // final_class_roll
                        "G", // exam_placed
                        null, // zero_unit
                        TWO, // timeout_factor
                        null, // forfeit_i
                        "N", // i_in_progress
                        null, // i_counted
                        "H", // ctrl_test
                        date10, // deferred_f_dt
                        Integer.valueOf(0), // bypass_timeout
                        "Z", // instrn_type
                        "W", // registration_status
                        date23, // last_class_roll_dt
                        null, // i_term / i_term_yr
                        null); // i_deadline_dt

                final boolean result = RawStcourseLogic.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawStcourse> all = RawStcourseLogic.queryAll(cache);

                assertEquals(9, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;
                boolean found7 = false;
                boolean found8 = false;
                boolean found9 = false;
                boolean found10 = false;

                for (final RawStcourse test : all) {
                    if (isRec1(test)) {
                        found1 = true;
                    } else if (isRec3(test)) {
                        found3 = true;
                    } else if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && RawRecordConstants.M125.equals(test.course)
                            && "001".equals(test.sect)
                            && Integer.valueOf(3).equals(test.paceOrder)
                            && "D".equals(test.openStatus)
                            && "G".equals(test.gradingOption)
                            && "N".equals(test.completed)
                            && test.score == null
                            && "F".equals(test.courseGrade)
                            && "N".equals(test.prereqSatis)
                            && "N".equals(test.initClassRoll)
                            && "N".equals(test.stuProvided)
                            && "Y".equals(test.finalClassRoll)
                            && "G".equals(test.examPlaced)
                            && test.zeroUnit == null
                            && TWO.equals(test.timeoutFactor)
                            && test.forfeitI == null
                            && "N".equals(test.iInProgress)
                            && test.iCounted == null
                            && "C".equals(test.ctrlTest)
                            && test.deferredFDt == null
                            && Integer.valueOf(0).equals(test.bypassTimeout)
                            && "I".equals(test.instrnType)
                            && "RS".equals(test.registrationStatus)
                            && date12.equals(test.lastClassRollDt)
                            && test.iTermKey == null
                            && test.iDeadlineDt == null) {

                        found4 = true;
                    } else if (isRec5(test)) {
                        found5 = true;
                    } else if (isRec6(test)) {
                        found6 = true;
                    } else if (isRec7(test)) {
                        found7 = true;
                    } else if (isRec8(test)) {
                        found8 = true;
                    } else if (isRec9(test)) {
                        found9 = true;
                    } else if (isRec10(test)) {
                        found10 = true;
                    }
                }

                assertTrue(found1, "Stcourse 1 not found");
                assertTrue(found3, "Stcourse 3 not found");
                assertTrue(found4, "Stcourse 4 not found");
                assertTrue(found5, "Stcourse 5 not found");
                assertTrue(found6, "Stcourse 6 not found");
                assertTrue(found7, "Stcourse 7 not found");
                assertTrue(found8, "Stcourse 8 not found");
                assertTrue(found9, "Stcourse 9 not found");
                assertTrue(found10, "Stcourse 10 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting users: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM stcourse");
                    stmt.executeUpdate("DELETE FROM term");
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

    /**
     * Tests whether a record matches the "rec1" test record.
     *
     * @param test the record to test
     * @return true if that record matches "rec1"
     */
    private static boolean isRec1(final RawStcourse test) {

        return sm21.equals(test.termKey)
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
                && THREE_HALVES.equals(test.timeoutFactor)
                && test.forfeitI == null
                && "N".equals(test.iInProgress)
                && test.iCounted == null
                && "C".equals(test.ctrlTest)
                && test.deferredFDt == null
                && Integer.valueOf(5).equals(test.bypassTimeout)
                && "D".equals(test.instrnType)
                && "E".equals(test.registrationStatus)
                && date9.equals(test.lastClassRollDt)
                && test.iTermKey == null //
                && test.iDeadlineDt == null;
    }

    /**
     * Tests whether a record matches the "rec2" test record.
     *
     * @param test the record to test
     * @return true if that record matches "rec2"
     */
    private static boolean isRec2(final RawStcourse test) {

        return fa21.equals(test.termKey)
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
                && TWO.equals(test.timeoutFactor)
                && test.forfeitI == null
                && "N".equals(test.iInProgress)
                && test.iCounted == null
                && "H".equals(test.ctrlTest)
                && date10.equals(test.deferredFDt)
                && Integer.valueOf(0).equals(test.bypassTimeout)
                && "I".equals(test.instrnType)
                && "RS".equals(test.registrationStatus)
                && date11.equals(test.lastClassRollDt)
                && test.iTermKey == null
                && test.iDeadlineDt == null;
    }

    /**
     * Tests whether a record matches the "rec3" test record.
     *
     * @param test the record to test
     * @return true if that record matches "rec3"
     */
    private static boolean isRec3(final RawStcourse test) {

        return fa21.equals(test.termKey)
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
                && TWO.equals(test.timeoutFactor)
                && test.forfeitI == null
                && "N".equals(test.iInProgress)
                && test.iCounted == null
                && "C".equals(test.ctrlTest)
                && test.deferredFDt == null
                && Integer.valueOf(0).equals(test.bypassTimeout)
                && "I".equals(test.instrnType)
                && "RS".equals(test.registrationStatus)
                && date12.equals(test.lastClassRollDt)
                && test.iTermKey == null
                && test.iDeadlineDt == null;
    }

    /**
     * Tests whether a record matches the "rec4" test record.
     *
     * @param test the record to test
     * @return true if that record matches "rec4"
     */
    private static boolean isRec4(final RawStcourse test) {

        return fa21.equals(test.termKey)
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
                && TWO.equals(test.timeoutFactor)
                && test.forfeitI == null
                && "N".equals(test.iInProgress)
                && test.iCounted == null
                && "C".equals(test.ctrlTest)
                && test.deferredFDt == null
                && Integer.valueOf(0).equals(test.bypassTimeout)
                && "I".equals(test.instrnType)
                && "RS".equals(test.registrationStatus)
                && date12.equals(test.lastClassRollDt)
                && test.iTermKey == null
                && test.iDeadlineDt == null;
    }

    /**
     * Tests whether a record matches the "rec5" test record.
     *
     * @param test the record to test
     * @return true if that record matches "rec5"
     */
    private static boolean isRec5(final RawStcourse test) {

        return fa21.equals(test.termKey)
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
                && date12.equals(test.lastClassRollDt)
                && sm21.equals(test.iTermKey)
                && test.iDeadlineDt == null;
    }

    /**
     * Tests whether a record matches the "rec6" test record.
     *
     * @param test the record to test
     * @return true if that record matches "rec6"
     */
    private static boolean isRec6(final RawStcourse test) {

        return fa21.equals(test.termKey)
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
                && date13.equals(test.lastClassRollDt)
                && test.iTermKey == null
                && test.iDeadlineDt == null;
    }

    /**
     * Tests whether a record matches the "rec7" test record.
     *
     * @param test the record to test
     * @return true if that record matches "rec7"
     */
    private static boolean isRec7(final RawStcourse test) {

        return fa21.equals(test.termKey)
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
                && date14.equals(test.lastClassRollDt)
                && sm21.equals(test.iTermKey)
                && date15.equals(test.iDeadlineDt);
    }

    /**
     * Tests whether a record matches the "rec8" test record.
     *
     * @param test the record to test
     * @return true if that record matches "rec8"
     */
    private static boolean isRec8(final RawStcourse test) {

        return fa21.equals(test.termKey)
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
                && "M".equals(test.examPlaced)
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
                && date14.equals(test.lastClassRollDt)
                && test.iTermKey == null
                && test.iDeadlineDt == null;
    }

    /**
     * Tests whether a record matches the "rec9" test record.
     *
     * @param test the record to test
     * @return true if that record matches "rec9"
     */
    private static boolean isRec9(final RawStcourse test) {

        return fa21.equals(test.termKey)
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
                && date16.equals(test.lastClassRollDt)
                && test.iTermKey == null
                && test.iDeadlineDt == null;
    }

    /**
     * Tests whether a record matches the "rec10" test record.
     *
     * @param test the record to test
     * @return true if that record matches "rec10"
     */
    private static boolean isRec10(final RawStcourse test) {

        return fa21.equals(test.termKey)
                && "111111111".equals(test.stuId)
                && RawRecordConstants.M126.equals(test.course)
                && "001".equals(test.sect)
                && Integer.valueOf(4).equals(test.paceOrder)
                && "G".equals(test.openStatus)
                && "I".equals(test.gradingOption)
                && "N".equals(test.completed)
                && test.score == null
                && test.courseGrade == null
                && "N".equals(test.prereqSatis)
                && "N".equals(test.initClassRoll)
                && "N".equals(test.stuProvided)
                && "Y".equals(test.finalClassRoll)
                && test.examPlaced == null
                && test.zeroUnit == null
                && TWO.equals(test.timeoutFactor)
                && test.forfeitI == null
                && "N".equals(test.iInProgress)
                && test.iCounted == null
                && "Z".equals(test.ctrlTest)
                && date22.equals(test.deferredFDt)
                && Integer.valueOf(0).equals(test.bypassTimeout)
                && "I".equals(test.instrnType)
                && "RS".equals(test.registrationStatus)
                && date23.equals(test.lastClassRollDt)
                && test.iTermKey == null
                && test.iDeadlineDt == null;
    }
}

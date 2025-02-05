package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawExamLogic} class.
 */
final class TestRawExamLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 1);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 1, 3);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2021, 1, 4);

    /** The database profile. */
    private static Profile profile = null;

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        profile = config.getCodeProfile(Contexts.INFORMIX_TEST_PATH);
        if (profile == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_TEST_PROFILE));
        }

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();
        final Cache cache = new Cache(profile);

        // Make sure we're in the TEST database
        try {
            try (final Statement stmt = conn.createStatement();
                 final ResultSet rs = stmt.executeQuery("SELECT descr FROM which_db")) {

                if (rs.next()) {
                    final String which = rs.getString(1);
                    if (which != null && !"TEST".equals(which.trim())) {
                        throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                    }
                } else {
                    throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                }
            }

            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM exam");
            }
            conn.commit();

            final RawExam raw1 = new RawExam("171RE", RawRecordConstants.M117, Integer.valueOf(1), "!!!!!",
                    "Unit 1 Review", "17.1.R", "R", date1, null, "Start 171RE");

            final RawExam raw2 = new RawExam("171UE", RawRecordConstants.M117, Integer.valueOf(1), "@@@@@",
                    "Unit 1 Exam", "17.1.U", "U", date2, null, "Start 171UE");

            final RawExam raw3 = new RawExam("172RE", RawRecordConstants.M117, Integer.valueOf(2), "#####",
                    "Unit 2 Review", "17.2.R", "R", date3, null, "Start 172RE");

            final RawExam raw4 = new RawExam("18FIN", RawRecordConstants.M118, Integer.valueOf(5), "$$$$$",
                    "Final", "18.5.F", "F", date4, null, "Start 18FIN");

            assertTrue(RawExamLogic.insert(cache, raw1), "Failed to insert exam");
            assertTrue(RawExamLogic.insert(cache, raw2), "Failed to insert exam");
            assertTrue(RawExamLogic.insert(cache, raw3), "Failed to insert exam");
            assertTrue(RawExamLogic.insert(cache, raw4), "Failed to insert exam");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing tables: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0003() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawExam> all = RawExamLogic.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final RawExam r : all) {

                if ("171RE".equals(r.version)
                    && RawRecordConstants.M117.equals(r.course)
                    && Integer.valueOf(1).equals(r.unit)
                    && "!!!!!".equals(r.vsnExplt)
                    && "Unit 1 Review".equals(r.title)
                    && "17.1.R".equals(r.treeRef)
                    && "R".equals(r.examType)
                    && date1.equals(r.activeDt)
                    && r.pullDt == null
                    && "Start 171RE".equals(r.buttonLabel)) {

                    found1 = true;
                } else if ("171UE".equals(r.version)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(1).equals(r.unit)
                           && "@@@@@".equals(r.vsnExplt)
                           && "Unit 1 Exam".equals(r.title)
                           && "17.1.U".equals(r.treeRef)
                           && "U".equals(r.examType)
                           && date2.equals(r.activeDt)
                           && r.pullDt == null
                           && "Start 171UE".equals(r.buttonLabel)) {

                    found2 = true;
                } else if ("172RE".equals(r.version)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(2).equals(r.unit)
                           && "#####".equals(r.vsnExplt)
                           && "Unit 2 Review".equals(r.title)
                           && "17.2.R".equals(r.treeRef)
                           && "R".equals(r.examType)
                           && date3.equals(r.activeDt)
                           && r.pullDt == null
                           && "Start 172RE".equals(r.buttonLabel)) {

                    found3 = true;
                } else if ("18FIN".equals(r.version)
                           && RawRecordConstants.M118.equals(r.course)
                           && Integer.valueOf(5).equals(r.unit)
                           && "$$$$$".equals(r.vsnExplt)
                           && "Final".equals(r.title)
                           && "18.5.F".equals(r.treeRef)
                           && "F".equals(r.examType)
                           && date4.equals(r.activeDt)
                           && r.pullDt == null
                           && "Start 18FIN".equals(r.buttonLabel)) {

                    found4 = true;
                } else {
                    Log.warning("Unexpected version ", r.version);
                    Log.warning("Unexpected course ", r.course);
                    Log.warning("Unexpected unit ", r.unit);
                    Log.warning("Unexpected vsnExplt ", r.vsnExplt);
                    Log.warning("Unexpected title ", r.title);
                    Log.warning("Unexpected treeRef ", r.treeRef);
                    Log.warning("Unexpected examType ", r.examType);
                    Log.warning("Unexpected activeDt ", r.activeDt);
                    Log.warning("Unexpected pullDt ", r.pullDt);
                    Log.warning("Unexpected buttonLabel ", r.buttonLabel);
                }
            }

            assertTrue(found1, "exam 1 not found");
            assertTrue(found2, "exam 2 not found");
            assertTrue(found3, "exam 3 not found");
            assertTrue(found4, "exam 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all exam rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryActiveByCourse results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawExam> all = RawExamLogic.queryActiveByCourse(cache, RawRecordConstants.M117);

            assertEquals(3, all.size(), "Incorrect record count from queryActiveByCourse");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawExam r : all) {

                if ("171RE".equals(r.version)
                    && RawRecordConstants.M117.equals(r.course)
                    && Integer.valueOf(1).equals(r.unit)
                    && "!!!!!".equals(r.vsnExplt)
                    && "Unit 1 Review".equals(r.title)
                    && "17.1.R".equals(r.treeRef)
                    && "R".equals(r.examType)
                    && date1.equals(r.activeDt)
                    && r.pullDt == null
                    && "Start 171RE".equals(r.buttonLabel)) {

                    found1 = true;
                } else if ("171UE".equals(r.version)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(1).equals(r.unit)
                           && "@@@@@".equals(r.vsnExplt)
                           && "Unit 1 Exam".equals(r.title)
                           && "17.1.U".equals(r.treeRef)
                           && "U".equals(r.examType)
                           && date2.equals(r.activeDt)
                           && r.pullDt == null
                           && "Start 171UE".equals(r.buttonLabel)) {

                    found2 = true;
                } else if ("172RE".equals(r.version)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(2).equals(r.unit)
                           && "#####".equals(r.vsnExplt)
                           && "Unit 2 Review".equals(r.title)
                           && "17.2.R".equals(r.treeRef)
                           && "R".equals(r.examType)
                           && date3.equals(r.activeDt)
                           && r.pullDt == null
                           && "Start 172RE".equals(r.buttonLabel)) {

                    found3 = true;
                } else {
                    Log.warning("Unexpected version ", r.version);
                    Log.warning("Unexpected course ", r.course);
                    Log.warning("Unexpected unit ", r.unit);
                    Log.warning("Unexpected vsnExplt ", r.vsnExplt);
                    Log.warning("Unexpected title ", r.title);
                    Log.warning("Unexpected treeRef ", r.treeRef);
                    Log.warning("Unexpected examType ", r.examType);
                    Log.warning("Unexpected activeDt ", r.activeDt);
                    Log.warning("Unexpected pullDt ", r.pullDt);
                    Log.warning("Unexpected buttonLabel ", r.buttonLabel);
                }
            }

            assertTrue(found1, "exam 1 not found");
            assertTrue(found2, "exam 2 not found");
            assertTrue(found3, "exam 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying exams by course: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryActiveByCourseUnit results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawExam> all = RawExamLogic.queryActiveByCourseUnit(cache, RawRecordConstants.M117,
                    Integer.valueOf(1));

            assertEquals(2, all.size(), "Incorrect record count from queryActiveByCourseUnit");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawExam r : all) {

                if ("171RE".equals(r.version)
                    && RawRecordConstants.M117.equals(r.course)
                    && Integer.valueOf(1).equals(r.unit)
                    && "!!!!!".equals(r.vsnExplt)
                    && "Unit 1 Review".equals(r.title)
                    && "17.1.R".equals(r.treeRef)
                    && "R".equals(r.examType)
                    && date1.equals(r.activeDt)
                    && r.pullDt == null
                    && "Start 171RE".equals(r.buttonLabel)) {

                    found1 = true;
                } else if ("171UE".equals(r.version)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(1).equals(r.unit)
                           && "@@@@@".equals(r.vsnExplt)
                           && "Unit 1 Exam".equals(r.title)
                           && "17.1.U".equals(r.treeRef)
                           && "U".equals(r.examType)
                           && date2.equals(r.activeDt)
                           && r.pullDt == null
                           && "Start 171UE".equals(r.buttonLabel)) {

                    found2 = true;
                } else {
                    Log.warning("Unexpected version ", r.version);
                    Log.warning("Unexpected course ", r.course);
                    Log.warning("Unexpected unit ", r.unit);
                    Log.warning("Unexpected vsnExplt ", r.vsnExplt);
                    Log.warning("Unexpected title ", r.title);
                    Log.warning("Unexpected treeRef ", r.treeRef);
                    Log.warning("Unexpected examType ", r.examType);
                    Log.warning("Unexpected activeDt ", r.activeDt);
                    Log.warning("Unexpected pullDt ", r.pullDt);
                    Log.warning("Unexpected buttonLabel ", r.buttonLabel);
                }
            }

            assertTrue(found1, "exam 1 not found");
            assertTrue(found2, "exam 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying exams by course unit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryActiveByCourseUnitType results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final RawExam r = RawExamLogic.queryActiveByCourseUnitType(cache, RawRecordConstants.M117,
                    Integer.valueOf(1), "R");

            assertNotNull(r, "No record returned by queryActiveByCourseUnitType");

            boolean found = false;

            if ("171RE".equals(r.version)
                && RawRecordConstants.M117.equals(r.course)
                && Integer.valueOf(1).equals(r.unit)
                && "!!!!!".equals(r.vsnExplt)
                && "Unit 1 Review".equals(r.title)
                && "17.1.R".equals(r.treeRef)
                && "R".equals(r.examType)
                && date1.equals(r.activeDt)
                && r.pullDt == null //
                && "Start 171RE".equals(r.buttonLabel)) {

                found = true;
            } else {
                Log.warning("Unexpected version ", r.version);
                Log.warning("Unexpected course ", r.course);
                Log.warning("Unexpected unit ", r.unit);
                Log.warning("Unexpected vsnExplt ", r.vsnExplt);
                Log.warning("Unexpected title ", r.title);
                Log.warning("Unexpected treeRef ", r.treeRef);
                Log.warning("Unexpected examType ", r.examType);
                Log.warning("Unexpected activeDt ", r.activeDt);
                Log.warning("Unexpected pullDt ", r.pullDt);
                Log.warning("Unexpected buttonLabel ", r.buttonLabel);
            }

            assertTrue(found, "exam 1 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying exams by course unit type: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            final RawExam r = RawExamLogic.query(cache, "171RE");

            assertNotNull(r, "No record returned by query");

            boolean found = false;

            if ("171RE".equals(r.version)
                && RawRecordConstants.M117.equals(r.course)
                && Integer.valueOf(1).equals(r.unit)
                && "!!!!!".equals(r.vsnExplt)
                && "Unit 1 Review".equals(r.title)
                && "17.1.R".equals(r.treeRef)
                && "R".equals(r.examType)
                && date1.equals(r.activeDt)
                && r.pullDt == null //
                && "Start 171RE".equals(r.buttonLabel)) {

                found = true;
            } else {
                Log.warning("Unexpected version ", r.version);
                Log.warning("Unexpected course ", r.course);
                Log.warning("Unexpected unit ", r.unit);
                Log.warning("Unexpected vsnExplt ", r.vsnExplt);
                Log.warning("Unexpected title ", r.title);
                Log.warning("Unexpected treeRef ", r.treeRef);
                Log.warning("Unexpected examType ", r.examType);
                Log.warning("Unexpected activeDt ", r.activeDt);
                Log.warning("Unexpected pullDt ", r.pullDt);
                Log.warning("Unexpected buttonLabel ", r.buttonLabel);
            }

            assertTrue(found, "exam 1 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying exam by version: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0008() {

        final Cache cache = new Cache(profile);

        try {
            final RawExam raw2 = new RawExam("171UE", RawRecordConstants.M117, Integer.valueOf(1), "@@@@@",
                    "Unit 1 Exam", "17.1.U", "U", date2, null, "Start 171UE");

            final boolean result = RawExamLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawExam> all = RawExamLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final RawExam r : all) {

                if ("171RE".equals(r.version)
                    && RawRecordConstants.M117.equals(r.course)
                    && Integer.valueOf(1).equals(r.unit)
                    && "!!!!!".equals(r.vsnExplt)
                    && "Unit 1 Review".equals(r.title)
                    && "17.1.R".equals(r.treeRef)
                    && "R".equals(r.examType)
                    && date1.equals(r.activeDt)
                    && r.pullDt == null
                    && "Start 171RE".equals(r.buttonLabel)) {

                    found1 = true;
                } else if ("172RE".equals(r.version)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(2).equals(r.unit)
                           && "#####".equals(r.vsnExplt)
                           && "Unit 2 Review".equals(r.title)
                           && "17.2.R".equals(r.treeRef)
                           && "R".equals(r.examType)
                           && date3.equals(r.activeDt)
                           && r.pullDt == null
                           && "Start 172RE".equals(r.buttonLabel)) {

                    found3 = true;
                } else if ("18FIN".equals(r.version)
                           && RawRecordConstants.M118.equals(r.course)
                           && Integer.valueOf(5).equals(r.unit)
                           && "$$$$$".equals(r.vsnExplt)
                           && "Final".equals(r.title)
                           && "18.5.F".equals(r.treeRef)
                           && "F".equals(r.examType)
                           && date4.equals(r.activeDt)
                           && r.pullDt == null
                           && "Start 18FIN".equals(r.buttonLabel)) {

                    found4 = true;
                } else {
                    Log.warning("Unexpected version ", r.version);
                    Log.warning("Unexpected course ", r.course);
                    Log.warning("Unexpected unit ", r.unit);
                    Log.warning("Unexpected vsnExplt ", r.vsnExplt);
                    Log.warning("Unexpected title ", r.title);
                    Log.warning("Unexpected treeRef ", r.treeRef);
                    Log.warning("Unexpected examType ", r.examType);
                    Log.warning("Unexpected activeDt ", r.activeDt);
                    Log.warning("Unexpected pullDt ", r.pullDt);
                    Log.warning("Unexpected buttonLabel ", r.buttonLabel);
                }
            }

            assertTrue(found1, "exam 1 not found");
            assertTrue(found3, "exam 3 not found");
            assertTrue(found4, "exam 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting exam: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM exam");
            }

            conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }
}

package dev.mathops.db.reclogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbUse;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.cfg.Facet;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.rec.MasteryExamRec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code MasteryExamLogic} class.
 */
final class TestMasteryExamLogic {

    /** A raw test record. */
    private static final MasteryExamRec RAW1 = new MasteryExamRec("C41_LT1_M",
            "ST", RawRecordConstants.M125, Integer.valueOf(1),
            Integer.valueOf(1), "25.1.1.ST", "125 1.1",
            "Button 125 1.1", LocalDateTime.of(2021, 1, 1, 0, 0, 0), null);

    /** A raw test record. */
    private static final MasteryExamRec RAW2 = new MasteryExamRec("C41_LT2_M",
            "ST", RawRecordConstants.M125, Integer.valueOf(1),
            Integer.valueOf(2), "25.1.2.ST", "125 1.2",
            "Button 125 1.2", LocalDateTime.of(2021, 1, 2, 0, 0, 0), null);

    /** A raw test record. */
    private static final MasteryExamRec RAW3 = new MasteryExamRec("C41_LT3_M",
            "ST", RawRecordConstants.M125, Integer.valueOf(1),
            Integer.valueOf(3), "25.1.3.ST", "125 1.3",
            "Button 125 1.3", LocalDateTime.of(2021, 1, 3, 0, 0, 0), null);

    /** A raw test record. */
    private static final MasteryExamRec RAW4 = new MasteryExamRec("C42_LT1_M",
            "ST", RawRecordConstants.M125, Integer.valueOf(2),
            Integer.valueOf(1), "25.2.1.ST", "125 2.1",
            "Button 125 2.1", LocalDateTime.of(2021, 1, 4, 0, 0, 0), null);

    /** A raw test record. */
    private static final MasteryExamRec RAW5 = new MasteryExamRec("C51_LT1_M",
            "ST", RawRecordConstants.M126, Integer.valueOf(1),
            Integer.valueOf(1), "26.1.1.ST", "126 1.1",
            "Button 126 1.1", LocalDateTime.of(2021, 1, 5, 0, 0, 0), null);

    /** A raw test record. */
    private static final MasteryExamRec RAW6 = new MasteryExamRec("C41_LT3_X",
            "XX", RawRecordConstants.M125, Integer.valueOf(1),
            Integer.valueOf(3), "25.1.3.XX", "125 1.3X",
            "Button 125 1.3X", LocalDateTime.of(2021, 1, 6, 0, 0, 0), null);

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final MasteryExamRec r) {

        Log.warning("Unexpected examId ", r.examId);
        Log.warning("Unexpected courseId ", r.courseId);
        Log.warning("Unexpected unit ", r.unit);
        Log.warning("Unexpected objective ", r.objective);
        Log.warning("Unexpected treeRef ", r.treeRef);
        Log.warning("Unexpected title ", r.title);
        Log.warning("Unexpected buttonLabel ", r.buttonLabel);
        Log.warning("Unexpected examType ", r.examType);
        Log.warning("Unexpected whenActive ", r.whenActive);
        Log.warning("Unexpected whenPulled ", r.whenPulled);
    }

    /**
     * Tests for the {@code MasteryExamLogic} class.
     */
    @Nested
    final class Informix {

        /** The Informix database profile. */
        static Profile informixProfile;

        /** The Informix database context. */
        static Login informixLogin;

        /** Initialize the test class. */
        @BeforeAll
        static void initTests() {

            final DatabaseConfig config = DatabaseConfig.getDefault();
            informixProfile = config.getCodeProfile(Contexts.INFORMIX_TEST_PATH);
            if (informixProfile == null) {
                throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_IFXTEST_PROFILE));
            }
            informixLogin = informixProfile.getLogin(ESchema.LEGACY);
            if (informixLogin == null) {
                throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PGPRIMARY_CONTEXT));
            }

            // Make sure the Informix connection is accessing the TEST database
            final Facet facet = informixProfile.getFacet(ESchema.LEGACY);
            if (facet.data.use != EDbUse.TEST) {
                throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, facet.data.use));
            }

            final DbConnection conn = informixLogin.checkOutConnection();
            final Cache cache = new Cache(informixProfile);

            try {
                try (final Statement stmt = conn.createStatement();
                     final ResultSet rs = stmt.executeQuery("SELECT descr FROM which_db")) {

                    if (rs.next()) {
                        final String which = rs.getString(1);
                        if (which == null || !"TEST".equals(which.trim())) {
                            throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                        }
                    } else {
                        throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                    }
                }

                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM mastery_exam");
                }
                conn.commit();

                final MasteryExamLogic logic = MasteryExamLogic.get(cache);

                assertTrue(logic.insert(cache, RAW1), "Failed to insert Informix mastery_exam");
                assertTrue(logic.insert(cache, RAW2), "Failed to insert Informix mastery_exam");
                assertTrue(logic.insert(cache, RAW3), "Failed to insert Informix mastery_exam");
                assertTrue(logic.insert(cache, RAW4), "Failed to insert Informix mastery_exam");
                assertTrue(logic.insert(cache, RAW5), "Failed to insert Informix mastery_exam");
                assertTrue(logic.insert(cache, RAW6), "Failed to insert Informix mastery_exam");
            } catch (final SQLException ex) {
                fail("Exception while initializing Informix 'mastery_exam' table: " + ex.getMessage());
                throw new IllegalArgumentException(ex);
            } finally {
                informixLogin.checkInConnection(conn);
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryAll results")
        void test0003() {

            final Cache cache = new Cache(informixProfile);
            final MasteryExamLogic logic = MasteryExamLogic.get(cache);

            try {
                final List<MasteryExamRec> all = logic.queryAll(cache);

                assertEquals(6, all.size(), "Incorrect record count from Informix queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;

                for (final MasteryExamRec r : all) {
                    if (RAW1.equals(r)) {
                        found1 = true;
                    } else if (RAW2.equals(r)) {
                        found2 = true;
                    } else if (RAW3.equals(r)) {
                        found3 = true;
                    } else if (RAW4.equals(r)) {
                        found4 = true;
                    } else if (RAW5.equals(r)) {
                        found5 = true;
                    } else if (RAW6.equals(r)) {
                        found6 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "Informix mastery_exam 1 not found");
                assertTrue(found2, "Informix mastery_exam 2 not found");
                assertTrue(found3, "Informix mastery_exam 3 not found");
                assertTrue(found4, "Informix mastery_exam 4 not found");
                assertTrue(found5, "Informix mastery_exam 5 not found");
                assertTrue(found6, "Informix mastery_exam 6 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Informix 'mastery_exam' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryActiveByCourse results")
        void test0004() {

            final Cache cache = new Cache(informixProfile);
            final MasteryExamLogic logic = MasteryExamLogic.get(cache);

            try {
                final List<MasteryExamRec> all = logic.queryActiveByCourse(cache, RawRecordConstants.M125);

                assertEquals(5, all.size(), "Incorrect record count from Informix queryActiveByCourse");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found6 = false;

                for (final MasteryExamRec r : all) {
                    if (RAW1.equals(r)) {
                        found1 = true;
                    } else if (RAW2.equals(r)) {
                        found2 = true;
                    } else if (RAW3.equals(r)) {
                        found3 = true;
                    } else if (RAW4.equals(r)) {
                        found4 = true;
                    } else if (RAW6.equals(r)) {
                        found6 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "Informix mastery_exam 1 not found");
                assertTrue(found2, "Informix mastery_exam 2 not found");
                assertTrue(found3, "Informix mastery_exam 3 not found");
                assertTrue(found4, "Informix mastery_exam 4 not found");
                assertTrue(found6, "Informix mastery_exam 6 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'mastery_exam' rows by course: "
                     + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryActiveByCourseUnit results")
        void test0005() {

            final Cache cache = new Cache(informixProfile);
            final MasteryExamLogic logic = MasteryExamLogic.get(cache);

            try {
                final List<MasteryExamRec> all = logic.queryActiveByCourseUnit(cache, RawRecordConstants.M125,
                        Integer.valueOf(1));

                assertEquals(4, all.size(), "Incorrect record count from Informix queryActiveByCourseUnit");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found6 = false;

                for (final MasteryExamRec r : all) {
                    if (RAW1.equals(r)) {
                        found1 = true;
                    } else if (RAW2.equals(r)) {
                        found2 = true;
                    } else if (RAW3.equals(r)) {
                        found3 = true;
                    } else if (RAW6.equals(r)) {
                        found6 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "Informix mastery_exam 1 not found");
                assertTrue(found2, "Informix mastery_exam 2 not found");
                assertTrue(found3, "Informix mastery_exam 3 not found");
                assertTrue(found6, "Informix mastery_exam 6 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'mastery_exam' rows by course unit: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryActiveByCourseUnitObjective results")
        void test0006() {

            final Cache cache = new Cache(informixProfile);
            final MasteryExamLogic logic = MasteryExamLogic.get(cache);

            try {
                final List<MasteryExamRec> all = logic.queryActiveByCourseUnitObjective(cache,
                        RawRecordConstants.M125, Integer.valueOf(1), Integer.valueOf(3));

                assertEquals(2, all.size(),
                        "Incorrect record count from Informix queryActiveByCourseUnitObjective");

                boolean found3 = false;
                boolean found6 = false;

                for (final MasteryExamRec r : all) {
                    if (RAW3.equals(r)) {
                        found3 = true;
                    } else if (RAW6.equals(r)) {
                        found6 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found3, "Informix mastery_exam 3 not found");
                assertTrue(found6, "Informix mastery_exam 6 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'mastery_exam' rows by course unit objective: "
                     + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryActiveByCourseUnitObjectiveType results")
        void test0007() {

            final Cache cache = new Cache(informixProfile);
            final MasteryExamLogic logic = MasteryExamLogic.get(cache);

            try {
                final MasteryExamRec r = logic.queryActive(cache,
                        RawRecordConstants.M125, Integer.valueOf(1), Integer.valueOf(3), "ST");

                assertNotNull(r, "No record returned by Informix queryActiveByCourseUnitObjective");

                if (!RAW3.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'mastery_exam' by course unit objective type: "
                     + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("query results")
        void test0008() {

            final Cache cache = new Cache(informixProfile);
            final MasteryExamLogic logic = MasteryExamLogic.get(cache);

            try {
                final MasteryExamRec r = logic.query(cache, "C41_LT1_M");

                assertNotNull(r, "No record returned by query");

                if (!RAW1.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying mastery_exam by version: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("delete results")
        void test0009() {

            final Cache cache = new Cache(informixProfile);
            final MasteryExamLogic logic = MasteryExamLogic.get(cache);

            try {
                final boolean result = logic.delete(cache, RAW5);
                assertTrue(result, "delete returned false");

                final List<MasteryExamRec> all = logic.queryAll(cache);

                assertEquals(5, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found6 = false;

                for (final MasteryExamRec r : all) {
                    if (RAW1.equals(r)) {
                        found1 = true;
                    } else if (RAW2.equals(r)) {
                        found2 = true;
                    } else if (RAW3.equals(r)) {
                        found3 = true;
                    } else if (RAW4.equals(r)) {
                        found4 = true;
                    } else if (RAW6.equals(r)) {
                        found6 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "mastery_exam 1 not found");
                assertTrue(found2, "mastery_exam 2 not found");
                assertTrue(found3, "mastery_exam 3 not found");
                assertTrue(found4, "mastery_exam 4 not found");
                assertTrue(found6, "mastery_exam 6 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting mastery_exams: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = informixLogin.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM mastery_exam");
                    }

                    conn.commit();

                } finally {
                    informixLogin.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while cleaning tables: " + ex.getMessage());
            }
        }
    }
}

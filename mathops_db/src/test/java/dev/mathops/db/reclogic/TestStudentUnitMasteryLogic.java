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
import dev.mathops.db.rec.StuUnitMasteryRec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code StudentUnitMasteryLogic} class.
 */
final class TestStudentUnitMasteryLogic {

    /** A raw test record. */
    private static final StuUnitMasteryRec RAW1 = new StuUnitMasteryRec(
            "111111111", "M 125", Integer.valueOf(1), Integer.valueOf(5), "4", "M", "ML", "A12");

    /** A raw test record. */
    private static final StuUnitMasteryRec RAW2 = new StuUnitMasteryRec(
            "111111111", "M 125", Integer.valueOf(2), Integer.valueOf(6), "5", "A00", "E", null);

    /** A raw test record. */
    private static final StuUnitMasteryRec RAW3 = new StuUnitMasteryRec(
            "111111111", "M 126", Integer.valueOf(1), Integer.valueOf(7), "6", "M", "M", "M");

    /** A raw test record. */
    private static final StuUnitMasteryRec RAW4 = new StuUnitMasteryRec(
            "222222222", "M 125", Integer.valueOf(1), Integer.valueOf(8), "7", "E", "ML", "A20");

    /** A raw test record. */
    private static final StuUnitMasteryRec RAW1NEWSCORE = new StuUnitMasteryRec(
            "111111111", "M 125", Integer.valueOf(1), Integer.valueOf(15), "4", "M", "ML", "A12");

    /** A raw test record. */
    private static final StuUnitMasteryRec RAW1NEWSR = new StuUnitMasteryRec(
            "111111111", "M 125", Integer.valueOf(1), Integer.valueOf(15), "P", "M", "ML", "A12");

    /** A raw test record. */
    private static final StuUnitMasteryRec RAW1NEWS1 = new StuUnitMasteryRec(
            "111111111", "M 125", Integer.valueOf(1), Integer.valueOf(15), "P", "M2", "ML", "A12");

    /** A raw test record. */
    private static final StuUnitMasteryRec RAW1NEWS2 = new StuUnitMasteryRec(
            "111111111", "M 125", Integer.valueOf(1), Integer.valueOf(15), "P", "M2", "L2", "A12");

    /** A raw test record. */
    private static final StuUnitMasteryRec RAW1NEWS3 = new StuUnitMasteryRec(
            "111111111", "M 125", Integer.valueOf(1), Integer.valueOf(15), "P", "M2", "L2", "P2");

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StuUnitMasteryRec r) {

        Log.warning("Unexpected stuId ", r.stuId);
        Log.warning("Unexpected courseId ", r.courseId);
        Log.warning("Unexpected unit ", r.unit);
        Log.warning("Unexpected score ", r.score);
        Log.warning("Unexpected srStatus ", r.srStatus);
        Log.warning("Unexpected s1Status ", r.s1Status);
        Log.warning("Unexpected s2Status ", r.s2Status);
        Log.warning("Unexpected s3Status ", r.s3Status);
    }

    /**
     * Tests for the {@code StudentUnitMasteryLogic} class.
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
                            throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST,
                                    which));
                        }
                    } else {
                        throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                    }
                }

                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM stu_unit_mastery");
                }
                conn.commit();

                final StuUnitMasteryLogic logic = StuUnitMasteryLogic.get(cache);

                assertTrue(logic.insert(cache, RAW1), "Failed to insert Informix stu_unit_mastery");
                assertTrue(logic.insert(cache, RAW2), "Failed to insert Informix stu_unit_mastery");
                assertTrue(logic.insert(cache, RAW3), "Failed to insert Informix stu_unit_mastery");
                assertTrue(logic.insert(cache, RAW4), "Failed to insert Informix stu_unit_mastery");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Informix 'stu_unit_mastery' table: " + ex.getMessage());
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
            final StuUnitMasteryLogic logic = StuUnitMasteryLogic.get(cache);

            try {
                final List<StuUnitMasteryRec> all = logic.queryAll(cache);

                assertEquals(4, all.size(), "Incorrect record count from Informix queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final StuUnitMasteryRec r : all) {
                    if (RAW1.equals(r)) {
                        found1 = true;
                    } else if (RAW2.equals(r)) {
                        found2 = true;
                    } else if (RAW3.equals(r)) {
                        found3 = true;
                    } else if (RAW4.equals(r)) {
                        found4 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "Informix stu_unit_mastery 1 not found");
                assertTrue(found2, "Informix stu_unit_mastery 2 not found");
                assertTrue(found3, "Informix stu_unit_mastery 3 not found");
                assertTrue(found4, "Informix stu_unit_mastery 4 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Informix 'stu_unit_mastery' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByStudent results")
        void test0004() {

            final Cache cache = new Cache(informixProfile);
            final StuUnitMasteryLogic logic = StuUnitMasteryLogic.get(cache);

            try {
                final List<StuUnitMasteryRec> all = logic.queryByStudent(cache, "111111111");

                assertEquals(3, all.size(), "Incorrect record count from Informix queryByStudent");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final StuUnitMasteryRec r : all) {
                    if (RAW1.equals(r)) {
                        found1 = true;
                    } else if (RAW2.equals(r)) {
                        found2 = true;
                    } else if (RAW3.equals(r)) {
                        found3 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "Informix stu_unit_mastery 1 not found");
                assertTrue(found2, "Informix stu_unit_mastery 2 not found");
                assertTrue(found3, "Informix stu_unit_mastery 3 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'stu_unit_mastery' rows by student: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByStudentCourse results")
        void test0005() {

            final Cache cache = new Cache(informixProfile);
            final StuUnitMasteryLogic logic = StuUnitMasteryLogic.get(cache);

            try {
                final List<StuUnitMasteryRec> all = logic.queryByStudentCourse(cache, "111111111", "M 125");

                assertEquals(2, all.size(), "Incorrect record count from Informix queryByStudentCourse");

                boolean found1 = false;
                boolean found2 = false;

                for (final StuUnitMasteryRec r : all) {
                    if (RAW1.equals(r)) {
                        found1 = true;
                    } else if (RAW2.equals(r)) {
                        found2 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "Informix stu_unit_mastery 1 not found");
                assertTrue(found2, "Informix stu_unit_mastery 2 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'stu_unit_mastery' rows by student and course: "
                     + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("query results")
        void test0006() {

            final Cache cache = new Cache(informixProfile);
            final StuUnitMasteryLogic logic = StuUnitMasteryLogic.get(cache);

            try {
                final StuUnitMasteryRec r = logic.query(cache, "111111111", "M 125", Integer.valueOf(1));

                assertNotNull(r, "No record returned by query");

                if (!RAW1.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying stu_unit_mastery by student, course, unit: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("updateScore results")
        void test0007() {

            final Cache cache = new Cache(informixProfile);
            final StuUnitMasteryLogic logic = StuUnitMasteryLogic.get(cache);

            try {
                final boolean result = logic.updateScore(cache, RAW1, RAW1NEWSCORE.score);
                assertTrue(result, "updateScore returned false");

                final StuUnitMasteryRec r = logic.query(cache, "111111111", "M 125", Integer.valueOf(1));

                assertNotNull(r, "No record returned by query");

                if (!RAW1NEWSCORE.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while updating score: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("updateSrStatus results")
        void test0008() {

            final Cache cache = new Cache(informixProfile);
            final StuUnitMasteryLogic logic = StuUnitMasteryLogic.get(cache);

            try {
                final boolean result = logic.updateSrStatus(cache, RAW1NEWSCORE, RAW1NEWSR.srStatus);
                assertTrue(result, "updateSrStatus returned false");

                final StuUnitMasteryRec r = logic.query(cache, "111111111", "M 125", Integer.valueOf(1));

                assertNotNull(r, "No record returned by query");

                if (!RAW1NEWSR.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while updating sr_status: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("updateS1Status results")
        void test0009() {

            final Cache cache = new Cache(informixProfile);
            final StuUnitMasteryLogic logic = StuUnitMasteryLogic.get(cache);

            try {
                final boolean result = logic.updateS1Status(cache, RAW1NEWSR, RAW1NEWS1.s1Status);
                assertTrue(result, "updateS1Status returned false");

                final StuUnitMasteryRec r = logic.query(cache, "111111111", "M 125", Integer.valueOf(1));

                assertNotNull(r, "No record returned by query");

                if (!RAW1NEWS1.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while updating s1_status: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("updateS2Status results")
        void test0010() {

            final Cache cache = new Cache(informixProfile);
            final StuUnitMasteryLogic logic = StuUnitMasteryLogic.get(cache);

            try {
                final boolean result = logic.updateS2Status(cache, RAW1NEWS1, RAW1NEWS2.s2Status);
                assertTrue(result, "updateS2Status returned false");

                final StuUnitMasteryRec r = logic.query(cache, "111111111", "M 125", Integer.valueOf(1));

                assertNotNull(r, "No record returned by query");

                if (!RAW1NEWS2.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while updating s2_status: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("updateS3Status results")
        void test0011() {

            final Cache cache = new Cache(informixProfile);
            final StuUnitMasteryLogic logic = StuUnitMasteryLogic.get(cache);

            try {
                final boolean result = logic.updateS3Status(cache, RAW1NEWS2, RAW1NEWS3.s3Status);
                assertTrue(result, "updateS3Status returned false");

                final StuUnitMasteryRec r = logic.query(cache, "111111111", "M 125", Integer.valueOf(1));

                assertNotNull(r, "No record returned by query");

                if (!RAW1NEWS3.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while updating s3_status: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("delete results")
        void test0012() {

            final Cache cache = new Cache(informixProfile);
            final StuUnitMasteryLogic logic = StuUnitMasteryLogic.get(cache);

            try {
                final boolean result = logic.delete(cache, RAW2);
                assertTrue(result, "delete returned false");

                final List<StuUnitMasteryRec> all = logic.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final StuUnitMasteryRec r : all) {
                    if (RAW1NEWS3.equals(r)) {
                        found1 = true;
                    } else if (RAW3.equals(r)) {
                        found3 = true;
                    } else if (RAW4.equals(r)) {
                        found4 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "stu_unit_mastery 1 not found");
                assertTrue(found3, "stu_unit_mastery 3 not found");
                assertTrue(found4, "stu_unit_mastery 4 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting stu_unit_masterys: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            final DbConnection conn = informixLogin.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM stu_unit_mastery");
                }

                conn.commit();
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while cleaning tables: " + ex.getMessage());
            } finally {
                informixLogin.checkInConnection(conn);
            }
        }
    }
}

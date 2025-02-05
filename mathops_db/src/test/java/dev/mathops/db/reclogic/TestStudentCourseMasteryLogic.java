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
import dev.mathops.db.rec.StudentCourseMasteryRec;
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
 * Tests for the {@code StudentCourseMasteryLogic} class.
 */
final class TestStudentCourseMasteryLogic {

    /** A raw test record. */
    private static final StudentCourseMasteryRec RAW1 = new StudentCourseMasteryRec("111111111", "M 125",
            Integer.valueOf(71), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(1));

    /** A raw test record. */
    private static final StudentCourseMasteryRec RAW2 = new StudentCourseMasteryRec("111111111", "M 126",
            Integer.valueOf(72), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(2));

    /** A raw test record. */
    private static final StudentCourseMasteryRec RAW3 = new StudentCourseMasteryRec("222222222", "M 125",
            Integer.valueOf(73), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(3));

    /** A raw test record. */
    private static final StudentCourseMasteryRec RAW1NEWMASTERY = new StudentCourseMasteryRec("111111111", "M 125",
            Integer.valueOf(71), Integer.valueOf(16), Integer.valueOf(17), Integer.valueOf(11));

    /** A raw test record. */
    private static final StudentCourseMasteryRec RAW1NEWEXP = new StudentCourseMasteryRec("111111111", "M 125",
            Integer.valueOf(71), Integer.valueOf(16), Integer.valueOf(17), Integer.valueOf(11));

    /** A raw test record. */
    private static final StudentCourseMasteryRec RAW1NEWSCORE = new StudentCourseMasteryRec("111111111", "M 125",
            Integer.valueOf(171), Integer.valueOf(16), Integer.valueOf(17), Integer.valueOf(11));

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StudentCourseMasteryRec r) {

        Log.warning("Unexpected stuId ", r.stuId);
        Log.warning("Unexpected courseId ", r.courseId);
        Log.warning("Unexpected score ", r.score);
        Log.warning("Unexpected nbr_mastered_h1 ", r.nbrMasteredH1);
        Log.warning("Unexpected nbr_mastered_h2 ", r.nbrMasteredH2);
        Log.warning("Unexpected nbr_eligible ", r.nbrEligible);
    }

    /**
     * Tests for the {@code StudentCourseMasteryLogic} class.
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
                    stmt.executeUpdate("DELETE FROM stu_course_mastery");
                }
                conn.commit();

                final StudentCourseMasteryLogic logic = StudentCourseMasteryLogic.get(cache);

                assertTrue(logic.insert(cache, RAW1), "Failed to insert Informix stu_course_mastery");
                assertTrue(logic.insert(cache, RAW2), "Failed to insert Informix stu_course_mastery");
                assertTrue(logic.insert(cache, RAW3), "Failed to insert Informix stu_course_mastery");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Informix 'stu_course_mastery' table: " + ex.getMessage());
                throw new IllegalArgumentException(ex);
            } finally {
                informixLogin.checkInConnection(conn);
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryAll")
        void test0003() {

            final Cache cache = new Cache(informixProfile);
            final StudentCourseMasteryLogic logic = StudentCourseMasteryLogic.get(cache);

            try {
                final List<StudentCourseMasteryRec> all = logic.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from Informix queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final StudentCourseMasteryRec r : all) {
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

                assertTrue(found1, "Informix stu_course_mastery 1 not found");
                assertTrue(found2, "Informix stu_course_mastery 2 not found");
                assertTrue(found3, "Informix stu_course_mastery 3 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Informix 'stu_course_mastery' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByStudent")
        void test0004() {

            final Cache cache = new Cache(informixProfile);
            final StudentCourseMasteryLogic logic = StudentCourseMasteryLogic.get(cache);

            try {
                final List<StudentCourseMasteryRec> all = logic.queryByStudent(cache,
                        "111111111");

                assertEquals(2, all.size(), "Incorrect record count from Informix queryByStudent");

                boolean found1 = false;
                boolean found2 = false;

                for (final StudentCourseMasteryRec r : all) {
                    if (RAW1.equals(r)) {
                        found1 = true;
                    } else if (RAW2.equals(r)) {
                        found2 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "Informix stu_course_mastery 1 not found");
                assertTrue(found2, "Informix stu_course_mastery 2 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'stu_course_mastery' rows by student: "
                     + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("query results")
        void test0005() {

            final Cache cache = new Cache(informixProfile);
            final StudentCourseMasteryLogic logic = StudentCourseMasteryLogic.get(cache);

            try {
                final StudentCourseMasteryRec r = logic.query(cache, "111111111", "M 125");

                assertNotNull(r, "No record returned by query");

                if (!RAW1.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying stu_course_mastery by version: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("updateMastery results")
        void test0006() {

            final Cache cache = new Cache(informixProfile);
            final StudentCourseMasteryLogic logic = StudentCourseMasteryLogic.get(cache);

            try {
                final boolean result = logic.updateMastery(cache, RAW1, RAW1NEWMASTERY.nbrMasteredH1,
                        RAW1NEWMASTERY.nbrMasteredH2, RAW1NEWMASTERY.nbrEligible);
                assertTrue(result, "updateMastery returned false");

                final StudentCourseMasteryRec r = logic.query(cache, "111111111", "M 125");

                assertNotNull(r, "No record returned by query");

                if (!RAW1NEWMASTERY.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while updating mastery: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("updateScore results")
        void test0008() {

            final Cache cache = new Cache(informixProfile);
            final StudentCourseMasteryLogic logic = StudentCourseMasteryLogic.get(cache);

            try {
                final boolean result = logic.updateScore(cache, RAW1NEWEXP, RAW1NEWSCORE.score);
                assertTrue(result, "updateScore returned false");

                final StudentCourseMasteryRec r = logic.query(cache, "111111111", "M 125");

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
        @DisplayName("delete results")
        void test0009() {

            final Cache cache = new Cache(informixProfile);
            final StudentCourseMasteryLogic logic = StudentCourseMasteryLogic.get(cache);

            try {
                final boolean result = logic.delete(cache, RAW2);
                assertTrue(result, "delete returned false");

                final List<StudentCourseMasteryRec> all = logic.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final StudentCourseMasteryRec r : all) {
                    if (RAW1NEWSCORE.equals(r)) {
                        found1 = true;
                    } else if (RAW3.equals(r)) {
                        found3 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "stu_course_mastery 1 not found");
                assertTrue(found3, "stu_course_mastery 3 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting stu_course_masterys: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            final DbConnection conn = informixLogin.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM stu_course_mastery");
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

package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbUse;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Facet;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.rec.term.StudentCourseMasteryRec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
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
    private static final StudentCourseMasteryRec RAW1 =
            new StudentCourseMasteryRec("811111111", "MATH 117",
                    "aAabbBccCdddeeEffFggghhhiiijjjkkklllmmMnnnoooppPqqqrrrssstttuuuvvvwwwxxx",
                    "YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYNYYNNNNN--------------------------",
                    "YyyYYyYYyyYYYyYYyYYYYyYYYyyyYyYyYYYyNNyNNNN-----------------------------",
                    "Y", Integer.valueOf(100));

    /** A raw test record. */
    private static final StudentCourseMasteryRec RAW2 =
            new StudentCourseMasteryRec("811111111", "MATH 118",
                    "AaabbBccCdddeeEffFggghhhiiijjjkkklllmmMnnnoooppPqqqrrrssstttuuuvvvwwwxxx",
                    "YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYNYYNNNNN---------------------------",
                    "yyYYyYYyyYYYyYYyYYYYyYYYyyyYyYyYYYyNNyNNNN------------------------------",
                    "N", Integer.valueOf(99));

    /** A raw test record. */
    private static final StudentCourseMasteryRec RAW3 =
            new StudentCourseMasteryRec("822222222", "MATH 125",
                    "aaAbbBccCdddeeEffFggghhhiiijjjkkklllmmMnnnoooppPqqqrrrssstttuuuvvvwwwxxx",
                    "YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYNYYNNNNN----------------------------",
                    "yYYyYYyyYYYyYYyYYYYyYYYyyyYyYyYYYyNNyNNNN-------------------------------",
                    "N", Integer.valueOf(98));

    /** A raw test record. */
    private static final StudentCourseMasteryRec UPD3 =
            new StudentCourseMasteryRec("822222222", "MATH 125",
                    "aaabbBccCdddeeEffFggghhhiiijjjkkklllmmMnnnoooppPqqqrrrssstttuuuvvvwwwxxx",
                    "YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYNYYNNNNN-----------------------------",
                    "YYyYYyyYYYyYYyYYYYyYYYyyyYyYyYYYyNNyNNNN--------------------------------",
                    "N", Integer.valueOf(97));

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StudentCourseMasteryRec r) {

        Log.warning("Unexpected student ID ", r.studentId);
        Log.warning("Unexpected course ID ", r.courseId);
        Log.warning("Unexpected course structure ", r.courseStructure);
        Log.warning("Unexpected homework status ", r.homeworkStatus);
        Log.warning("Unexpected mastery status ", r.masteryStatus);
        Log.warning("Unexpected completed ", r.completed);
        Log.warning("Unexpected score", r.score);
    }

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        profile = config.getCodeProfile(Contexts.POSTGRES_TEST_PATH);
        if (profile == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PGTEST_PROFILE));
        }
        login = profile.getLogin(ESchema.LEGACY);
        if (login == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PGPRIMARY_CONTEXT));
        }

        // Make sure the connection is accessing the TEST database
        final Facet facet = profile.getFacet(ESchema.LEGACY);
        if (facet.data.use != EDbUse.TEST) {
            throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, facet.data.use));
        }

        final Cache cache = new Cache(profile);
        final String prefix = cache.getSchemaPrefix(ESchema.TERM);
        if (prefix == null) {
            fail(TestRes.get(TestRes.ERR_NO_TERM_PREFIX));
        }

        final DbConnection conn = login.checkOutConnection();

        final String sql = "SELECT descr FROM " + prefix + ".which_db";

        try {
            try (final Statement stmt = conn.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql)) {

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
                stmt.executeUpdate("DELETE FROM " + prefix + ".student_course_mastery");
            }
            conn.commit();

            assertTrue(StudentCourseMasteryLogic.INSTANCE.insert(cache, RAW1),
                    "Failed to insert student course mastery");
            assertTrue(StudentCourseMasteryLogic.INSTANCE.insert(cache, RAW2),
                    "Failed to insert student course mastery");
            assertTrue(StudentCourseMasteryLogic.INSTANCE.insert(cache, RAW3),
                    "Failed to insert student course mastery");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'student_course_mastery' table: " + ex.getMessage());
            throw new IllegalArgumentException(ex);
        } finally {
            login.checkInConnection(conn);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0001() {
        final Cache cache = new Cache(profile);

        try {
            final List<StudentCourseMasteryRec> all = StudentCourseMasteryLogic.INSTANCE.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

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

            assertTrue(found1, "student_course_mastery 1 not found");
            assertTrue(found2, "student_course_mastery 2 not found");
            assertTrue(found3, "student_course_mastery 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'student_course_mastery' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final StudentCourseMasteryRec r = StudentCourseMasteryLogic.INSTANCE.query(cache, RAW1.studentId,
                    RAW1.courseId);

            assertNotNull(r, "No record returned by query");

            if (!RAW1.equals(r)) {
                printUnexpected(r);
                fail("Extra record found");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying student_course_mastery: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            final List<StudentCourseMasteryRec> all = StudentCourseMasteryLogic.INSTANCE.queryByStudent(cache,
                    RAW1.studentId);

            assertEquals(2, all.size(), "Incorrect record count from queryAll");

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

            assertTrue(found1, "student_course_mastery 1 not found");
            assertTrue(found2, "student_course_mastery 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'student_course_mastery' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            if (StudentCourseMasteryLogic.INSTANCE.update(cache, UPD3)) {
                final StudentCourseMasteryRec r = StudentCourseMasteryLogic.INSTANCE.query(cache, UPD3.studentId,
                        UPD3.courseId);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD3.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of student_course_mastery");
                }
            } else {
                fail("Failed to update student_course_mastery row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating student_course_mastery: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = StudentCourseMasteryLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<StudentCourseMasteryRec> all = StudentCourseMasteryLogic.INSTANCE.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final StudentCourseMasteryRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (UPD3.equals(r)) {
                    found3 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "student_course_mastery 1 not found");
            assertTrue(found3, "student_course_mastery 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting student_course_masterys: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Cache cache = new Cache(profile);
        final String prefix = cache.getSchemaPrefix(ESchema.TERM);
        if (prefix == null) {
            fail(TestRes.get(TestRes.ERR_NO_TERM_PREFIX));
        }

        try {
            final DbConnection conn = login.checkOutConnection();

            final String sql = "SELECT descr FROM " + prefix + ".which_db";

            try {
                try (final Statement stmt = conn.createStatement();
                     final ResultSet rs = stmt.executeQuery(sql)) {

                    if (rs.next()) {
                        final String which = rs.getString(1);
                        if (which == null || !"TEST".equals(which.trim())) {
                            throw new IllegalArgumentException(
                                    TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                        }
                    } else {
                        throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                    }
                }

                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM " + prefix + ".student_course_mastery");
                }

                conn.commit();
            } finally {
                login.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        }
    }
}

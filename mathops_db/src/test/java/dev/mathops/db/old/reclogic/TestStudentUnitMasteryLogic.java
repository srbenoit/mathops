package dev.mathops.db.old.reclogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.logic.DbConnection;
import dev.mathops.db.logic.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.EDbUse;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rec.StudentUnitMasteryRec;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Tests for the {@code StudentUnitMasteryLogic} class.
 */
final class TestStudentUnitMasteryLogic {

    /** A raw test record. */
    private static final StudentUnitMasteryRec RAW1 = new StudentUnitMasteryRec(
            "111111111", "M 125", Integer.valueOf(1), Integer.valueOf(5), "4", "M", "ML", "A12");

    /** A raw test record. */
    private static final StudentUnitMasteryRec RAW2 = new StudentUnitMasteryRec(
            "111111111", "M 125", Integer.valueOf(2), Integer.valueOf(6), "5", "A00", "E", null);

    /** A raw test record. */
    private static final StudentUnitMasteryRec RAW3 = new StudentUnitMasteryRec(
            "111111111", "M 126", Integer.valueOf(1), Integer.valueOf(7), "6", "M", "M", "M");

    /** A raw test record. */
    private static final StudentUnitMasteryRec RAW4 = new StudentUnitMasteryRec(
            "222222222", "M 125", Integer.valueOf(1), Integer.valueOf(8), "7", "E", "ML", "A20");

    /** A raw test record. */
    private static final StudentUnitMasteryRec RAW1NEWSCORE = new StudentUnitMasteryRec(
            "111111111", "M 125", Integer.valueOf(1), Integer.valueOf(15), "4", "M", "ML", "A12");

    /** A raw test record. */
    private static final StudentUnitMasteryRec RAW1NEWSR = new StudentUnitMasteryRec(
            "111111111", "M 125", Integer.valueOf(1), Integer.valueOf(15), "P", "M", "ML", "A12");

    /** A raw test record. */
    private static final StudentUnitMasteryRec RAW1NEWS1 = new StudentUnitMasteryRec(
            "111111111", "M 125", Integer.valueOf(1), Integer.valueOf(15), "P", "M2", "ML", "A12");

    /** A raw test record. */
    private static final StudentUnitMasteryRec RAW1NEWS2 = new StudentUnitMasteryRec(
            "111111111", "M 125", Integer.valueOf(1), Integer.valueOf(15), "P", "M2", "L2", "A12");

    /** A raw test record. */
    private static final StudentUnitMasteryRec RAW1NEWS3 = new StudentUnitMasteryRec(
            "111111111", "M 125", Integer.valueOf(1), Integer.valueOf(15), "P", "M2", "L2", "P2");

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StudentUnitMasteryRec r) {

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
        public static DbProfile informixProfile;

        /** The Informix database context. */
        public static DbContext informixCtx;

        /** Initialize the test class. */
        @BeforeAll
        static void initTests() {

            informixProfile = ContextMap.getDefaultInstance().getCodeProfile(Contexts.INFORMIX_TEST_PATH);
            if (informixProfile == null) {
                throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_IFXTEST_PROFILE));
            }

            informixCtx = informixProfile.getDbContext(ESchemaUse.PRIMARY);
            if (informixCtx == null) {
                throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_IFXPRIMARY_CONTEXT));
            }

            // Make sure the Informix connection is accessing the TEST database
            try {
                final DbConnection conn = informixCtx.checkOutConnection();

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
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                throw new IllegalArgumentException(ex);
            }

            try {
                final DbConnection conn = informixCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM stu_unit_mastery");
                    }
                    conn.commit();

                    final Cache cache = new Cache(informixProfile, conn);

                    assertTrue(StudentUnitMasteryLogic.INFORMIX.insert(cache, RAW1),
                            "Failed to insert Informix stu_unit_mastery");

                    assertTrue(StudentUnitMasteryLogic.INFORMIX.insert(cache, RAW2),
                            "Failed to insert Informix stu_unit_mastery");

                    assertTrue(StudentUnitMasteryLogic.INFORMIX.insert(cache, RAW3),
                            "Failed to insert Informix stu_unit_mastery");

                    assertTrue(StudentUnitMasteryLogic.INFORMIX.insert(cache, RAW4),
                            "Failed to insert Informix stu_unit_mastery");
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Informix 'stu_unit_mastery' table: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryAll results")
        void test0003() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<StudentUnitMasteryRec> all = StudentUnitMasteryLogic.INFORMIX.queryAll(cache);

                    assertEquals(4, all.size(), "Incorrect record count from Informix queryAll");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;

                    for (final StudentUnitMasteryRec r : all) {
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

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Informix 'stu_unit_mastery' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByStudent results")
        void test0004() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<StudentUnitMasteryRec> all =
                            StudentUnitMasteryLogic.INFORMIX.queryByStudent(cache, "111111111");

                    assertEquals(3, all.size(), "Incorrect record count from Informix queryByStudent");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;

                    for (final StudentUnitMasteryRec r : all) {
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

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'stu_unit_mastery' rows by student: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByStudentCourse results")
        void test0005() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<StudentUnitMasteryRec> all =
                            StudentUnitMasteryLogic.INFORMIX.queryByStudentCourse(cache,
                                    "111111111", "M 125");

                    assertEquals(2, all.size(), "Incorrect record count from Informix queryByStudentCourse");

                    boolean found1 = false;
                    boolean found2 = false;

                    for (final StudentUnitMasteryRec r : all) {
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

                } finally {
                    informixCtx.checkInConnection(conn);
                }
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

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final StudentUnitMasteryRec r = StudentUnitMasteryLogic.INFORMIX.query(cache,
                            "111111111", "M 125", Integer.valueOf(1));

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }

                } finally {
                    informixCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final boolean result = StudentUnitMasteryLogic.INFORMIX.updateScore(cache, RAW1,
                            RAW1NEWSCORE.score);
                    assertTrue(result, "updateScore returned false");

                    final StudentUnitMasteryRec r = StudentUnitMasteryLogic.INFORMIX.query(cache,
                            "111111111", "M 125", Integer.valueOf(1));

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1NEWSCORE.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    informixCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final boolean result = StudentUnitMasteryLogic.INFORMIX.updateSrStatus(cache,
                            RAW1NEWSCORE, RAW1NEWSR.srStatus);
                    assertTrue(result, "updateSrStatus returned false");

                    final StudentUnitMasteryRec r = StudentUnitMasteryLogic.INFORMIX.query(cache,
                            "111111111", "M 125", Integer.valueOf(1));

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1NEWSR.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    informixCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final boolean result = StudentUnitMasteryLogic.INFORMIX.updateS1Status(cache,
                            RAW1NEWSR, RAW1NEWS1.s1Status);
                    assertTrue(result, "updateS1Status returned false");

                    final StudentUnitMasteryRec r = StudentUnitMasteryLogic.INFORMIX.query(cache,
                            "111111111", "M 125", Integer.valueOf(1));

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1NEWS1.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    informixCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final boolean result = StudentUnitMasteryLogic.INFORMIX.updateS2Status(cache,
                            RAW1NEWS1, RAW1NEWS2.s2Status);
                    assertTrue(result, "updateS2Status returned false");

                    final StudentUnitMasteryRec r = StudentUnitMasteryLogic.INFORMIX.query(cache,
                            "111111111", "M 125", Integer.valueOf(1));

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1NEWS2.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    informixCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final boolean result = StudentUnitMasteryLogic.INFORMIX.updateS3Status(cache,
                            RAW1NEWS2, RAW1NEWS3.s3Status);
                    assertTrue(result, "updateS3Status returned false");

                    final StudentUnitMasteryRec r = StudentUnitMasteryLogic.INFORMIX.query(cache,
                            "111111111", "M 125", Integer.valueOf(1));

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1NEWS3.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    informixCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final boolean result = StudentUnitMasteryLogic.INFORMIX.delete(cache, RAW2);
                    assertTrue(result, "delete returned false");

                    final List<StudentUnitMasteryRec> all = StudentUnitMasteryLogic.INFORMIX.queryAll(cache);

                    assertEquals(3, all.size(), "Incorrect record count from queryAll after delete");

                    boolean found1 = false;
                    boolean found3 = false;
                    boolean found4 = false;

                    for (final StudentUnitMasteryRec r : all) {
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

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting stu_unit_masterys: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM stu_unit_mastery");
                    }

                    conn.commit();

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while cleaning tables: " + ex.getMessage());
            }
        }
    }

    /**
     * Tests for the {@code StudentUnitMasteryLogic} class.
     */
    @Nested
    final class Postgres {

        /** The PostgreSQL database profile. */
        public static DbProfile postgresProfile;

        /** The PostgreSQL database context. */
        public static DbContext postgresCtx;

        /** Initialize the test class. */
        @BeforeAll
        static void initTests() {

            postgresProfile = ContextMap.getDefaultInstance().getCodeProfile(Contexts.POSTGRES_TEST_PATH);
            if (postgresProfile == null) {
                throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PGTEST_PROFILE));
            }

            postgresCtx = postgresProfile.getDbContext(ESchemaUse.PRIMARY);
            if (postgresCtx == null) {
                throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PGPRIMARY_CONTEXT));
            }

            // Make sure the PostgreSQL connection is using a TEST schema
            if (postgresCtx.loginConfig.db.use != EDbUse.TEST) {
                throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST,
                        postgresCtx.loginConfig.db.use));
            }
            try {
                final DbConnection conn = postgresCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM term_t.stu_unit_mastery");
                    }
                    conn.commit();

                    final Cache cache = new Cache(postgresProfile, conn);

                    assertTrue(StudentUnitMasteryLogic.POSTGRES.insert(cache, RAW1),
                            "Failed to insert Postgres stu_unit_mastery");

                    assertTrue(StudentUnitMasteryLogic.POSTGRES.insert(cache, RAW2),
                            "Failed to insert Postgres stu_unit_mastery");

                    assertTrue(StudentUnitMasteryLogic.POSTGRES.insert(cache, RAW3),
                            "Failed to insert Postgres stu_unit_mastery");

                    assertTrue(StudentUnitMasteryLogic.POSTGRES.insert(cache, RAW4),
                            "Failed to insert Postgres stu_unit_mastery");
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Postgres 'term_t.stu_unit_mastery' table: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres queryAll results")
        void test0003() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<StudentUnitMasteryRec> all = StudentUnitMasteryLogic.POSTGRES.queryAll(cache);

                    assertEquals(4, all.size(), "Incorrect record count from Postgres queryAll");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;

                    for (final StudentUnitMasteryRec r : all) {
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

                    assertTrue(found1, "Postgres stu_unit_mastery 1 not found");
                    assertTrue(found2, "Postgres stu_unit_mastery 2 not found");
                    assertTrue(found3, "Postgres stu_unit_mastery 3 not found");
                    assertTrue(found4, "Postgres stu_unit_mastery 4 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Postgres 'stu_unit_mastery' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres queryByStudent results")
        void test0004() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<StudentUnitMasteryRec> all = StudentUnitMasteryLogic.POSTGRES.queryByStudent(cache,
                            "111111111");

                    assertEquals(3, all.size(), "Incorrect record count from Postgres queryByStudent");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;

                    for (final StudentUnitMasteryRec r : all) {
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

                    assertTrue(found1, "Postgres stu_unit_mastery 1 not found");
                    assertTrue(found2, "Postgres stu_unit_mastery 2 not found");
                    assertTrue(found3, "Postgres stu_unit_mastery 3 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Postgres 'stu_unit_mastery' rows by student: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres queryByStudentCourse results")
        void test0005() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<StudentUnitMasteryRec> all = StudentUnitMasteryLogic.POSTGRES.queryByStudentCourse(cache,
                            "111111111", "M 125");

                    assertEquals(2, all.size(), "Incorrect record count from Postgres queryByStudentCourse");

                    boolean found1 = false;
                    boolean found2 = false;

                    for (final StudentUnitMasteryRec r : all) {
                        if (RAW1.equals(r)) {
                            found1 = true;
                        } else if (RAW2.equals(r)) {
                            found2 = true;
                        } else {
                            printUnexpected(r);
                            fail("Extra record found");
                        }
                    }

                    assertTrue(found1, "Postgres stu_unit_mastery 1 not found");
                    assertTrue(found2, "Postgres stu_unit_mastery 2 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Postgres 'stu_unit_mastery' rows by student and course: "
                        + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("query results")
        void test0006() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final StudentUnitMasteryRec r = StudentUnitMasteryLogic.POSTGRES.query(cache, //
                            "111111111", "M 125", Integer.valueOf(1));

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }

                } finally {
                    postgresCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final boolean result = StudentUnitMasteryLogic.POSTGRES.updateScore(cache, RAW1,
                            RAW1NEWSCORE.score);
                    assertTrue(result, "updateScore returned false");

                    final StudentUnitMasteryRec r = StudentUnitMasteryLogic.POSTGRES.query(cache, //
                            "111111111", "M 125", Integer.valueOf(1));

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1NEWSCORE.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    postgresCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final boolean result = StudentUnitMasteryLogic.POSTGRES.updateSrStatus(cache,
                            RAW1NEWSCORE, RAW1NEWSR.srStatus);
                    assertTrue(result, "updateSrStatus returned false");

                    final StudentUnitMasteryRec r = StudentUnitMasteryLogic.POSTGRES.query(cache, //
                            "111111111", "M 125", Integer.valueOf(1));

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1NEWSR.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    postgresCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final boolean result = StudentUnitMasteryLogic.POSTGRES.updateS1Status(cache,
                            RAW1NEWSR, RAW1NEWS1.s1Status);
                    assertTrue(result, "updateS1Status returned false");

                    final StudentUnitMasteryRec r = StudentUnitMasteryLogic.POSTGRES.query(cache, //
                            "111111111", "M 125", Integer.valueOf(1));

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1NEWS1.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    postgresCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final boolean result = StudentUnitMasteryLogic.POSTGRES.updateS2Status(cache,
                            RAW1NEWS1, RAW1NEWS2.s2Status);
                    assertTrue(result, "updateS2Status returned false");

                    final StudentUnitMasteryRec r = StudentUnitMasteryLogic.POSTGRES.query(cache,
                            "111111111", "M 125", Integer.valueOf(1));

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1NEWS2.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    postgresCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final boolean result = StudentUnitMasteryLogic.POSTGRES.updateS3Status(cache, RAW1NEWS2,
                            RAW1NEWS3.s3Status);
                    assertTrue(result, "updateS3Status returned false");

                    final StudentUnitMasteryRec r = StudentUnitMasteryLogic.POSTGRES.query(cache, "111111111", "M 125",
                            Integer.valueOf(1));

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1NEWS3.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    postgresCtx.checkInConnection(conn);
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

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final boolean result = StudentUnitMasteryLogic.POSTGRES.delete(cache, RAW2);
                    assertTrue(result, "delete returned false");

                    final List<StudentUnitMasteryRec> all =
                            StudentUnitMasteryLogic.POSTGRES.queryAll(cache);

                    assertEquals(3, all.size(), //
                            "Incorrect record count from queryAll after delete");

                    boolean found1 = false;
                    boolean found3 = false;
                    boolean found4 = false;

                    for (final StudentUnitMasteryRec r : all) {
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

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting stu_unit_masterys: "
                        + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM term_t.stu_unit_mastery");
                    }

                    conn.commit();

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while cleaning tables: " + ex.getMessage());
            }
        }
    }
}

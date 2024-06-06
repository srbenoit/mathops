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
import dev.mathops.db.old.rec.StudentStandardMilestoneRec;

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
import java.time.LocalDate;
import java.util.List;

/**
 * Tests for the {@code StudentStandardMilestoneLogic} class.
 */
final class TestStudentStandardMilestoneLogic {

    /** A raw test record. */
    private static final StudentStandardMilestoneRec RAW1 =
            new StudentStandardMilestoneRec("111111111", "A", Integer.valueOf(5), Integer.valueOf(3),
                    Integer.valueOf(8), Integer.valueOf(1),
                    "OP", LocalDate.of(2023, 1, 1));

    /** A raw test record. */
    private static final StudentStandardMilestoneRec RAW2 =
            new StudentStandardMilestoneRec("111111111", "A", Integer.valueOf(5), Integer.valueOf(3),
                    Integer.valueOf(8), Integer.valueOf(1),
                    "MA", LocalDate.of(2023, 1, 2));

    /** A raw test record. */
    private static final StudentStandardMilestoneRec RAW3 =
            new StudentStandardMilestoneRec("111111111", "A", Integer.valueOf(5), Integer.valueOf(3),
                    Integer.valueOf(8), Integer.valueOf(2),
                    "OP", LocalDate.of(2023, 1, 3));

    /** A raw test record. */
    private static final StudentStandardMilestoneRec RAW4 =
            new StudentStandardMilestoneRec("111111111", "A", Integer.valueOf(5), Integer.valueOf(4),
                    Integer.valueOf(7), Integer.valueOf(1),
                    "EX", LocalDate.of(2023, 1, 4));

    /** A raw test record. */
    private static final StudentStandardMilestoneRec RAW5 =
            new StudentStandardMilestoneRec("111111111", "A", Integer.valueOf(4), Integer.valueOf(3),
                    Integer.valueOf(6), Integer.valueOf(2),
                    "CD", LocalDate.of(2023, 1, 5));

    /** A raw test record. */
    private static final StudentStandardMilestoneRec RAW6 =
            new StudentStandardMilestoneRec("222222222", "A", Integer.valueOf(5), Integer.valueOf(1),
                    Integer.valueOf(5), Integer.valueOf(3),
                    "MA", LocalDate.of(2023, 1, 6));

    /** A raw test record. */
    private static final StudentStandardMilestoneRec RAW1NEWDATE =
            new StudentStandardMilestoneRec("111111111", "A", Integer.valueOf(5), Integer.valueOf(3),
                    Integer.valueOf(8), Integer.valueOf(1), "OP", LocalDate.of(2023, 12, 10));

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StudentStandardMilestoneRec r) {

        Log.warning("Unexpected stuId ", r.stuId);
        Log.warning("Unexpected paceTrack ", r.paceTrack);
        Log.warning("Unexpected pace ", r.pace);
        Log.warning("Unexpected paceIndex ", r.paceIndex);
        Log.warning("Unexpected unit ", r.unit);
        Log.warning("Unexpected objective ", r.objective);
        Log.warning("Unexpected msType ", r.msType);
        Log.warning("Unexpected msDate ", r.msDate);
    }

    /**
     * Tests for the {@code StudentStandardMilestoneLogic} class.
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
                        stmt.executeUpdate("DELETE FROM stu_std_milestone");
                    }
                    conn.commit();

                    final Cache cache = new Cache(informixProfile, conn);

                    assertTrue(StudentStandardMilestoneLogic.INFORMIX.insert(cache, RAW1),
                            "Failed to insert Informix stu_std_milestone");

                    assertTrue(StudentStandardMilestoneLogic.INFORMIX.insert(cache, RAW2),
                            "Failed to insert Informix stu_std_milestone");

                    assertTrue(StudentStandardMilestoneLogic.INFORMIX.insert(cache, RAW3),
                            "Failed to insert Informix stu_std_milestone");

                    assertTrue(StudentStandardMilestoneLogic.INFORMIX.insert(cache, RAW4),
                            "Failed to insert Informix stu_std_milestone");

                    assertTrue(StudentStandardMilestoneLogic.INFORMIX.insert(cache, RAW5),
                            "Failed to insert Informix stu_std_milestone");

                    assertTrue(StudentStandardMilestoneLogic.INFORMIX.insert(cache, RAW6),
                            "Failed to insert Informix stu_std_milestone");
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Informix 'stu_std_milestone' table: " + ex.getMessage());
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
                    final List<StudentStandardMilestoneRec> all =
                            StudentStandardMilestoneLogic.INFORMIX.queryAll(cache);

                    assertEquals(6, all.size(), "Incorrect record count from Informix queryAll");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;
                    boolean found5 = false;
                    boolean found6 = false;

                    for (final StudentStandardMilestoneRec r : all) {
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

                    assertTrue(found1, "Informix stu_std_milestone 1 not found");
                    assertTrue(found2, "Informix stu_std_milestone 2 not found");
                    assertTrue(found3, "Informix stu_std_milestone 3 not found");
                    assertTrue(found4, "Informix stu_std_milestone 4 not found");
                    assertTrue(found5, "Informix stu_std_milestone 5 not found");
                    assertTrue(found6, "Informix stu_std_milestone 6 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Informix 'stu_std_milestone' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByStuPaceTrackPace results")
        void test0004() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<StudentStandardMilestoneRec> all =
                            StudentStandardMilestoneLogic.INFORMIX.queryByStuPaceTrackPace(cache, "111111111", "A",
                                    Integer.valueOf(5));

                    assertEquals(4, all.size(), "Incorrect record count from Informix queryByStuPaceTrackPace");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;

                    for (final StudentStandardMilestoneRec r : all) {
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

                    assertTrue(found1, "Informix stu_std_milestone 1 not found");
                    assertTrue(found2, "Informix stu_std_milestone 2 not found");
                    assertTrue(found3, "Informix stu_std_milestone 3 not found");
                    assertTrue(found4, "Informix stu_std_milestone 4 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'stu_std_milestone' rows by track and pace: "
                        + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByStuPaceTrackPaceIndex results")
        void test0005() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final List<StudentStandardMilestoneRec> all =
                            StudentStandardMilestoneLogic.INFORMIX.queryByStuPaceTrackPaceIndex(cache, "111111111",
                                    "A", Integer.valueOf(5), Integer.valueOf(3));
                    assertEquals(3, all.size(), "Incorrect record count from Informix queryByStuPaceTrackPaceIndex");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;

                    for (final StudentStandardMilestoneRec r : all) {
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

                    assertTrue(found1, "Informix stu_std_milestone 1 not found");
                    assertTrue(found2, "Informix stu_std_milestone 2 not found");
                    assertTrue(found3, "Informix stu_std_milestone 3 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'stu_std_milestone' rows by track, pace, index: "
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
                    final StudentStandardMilestoneRec r =
                            StudentStandardMilestoneLogic.INFORMIX.query(cache, "111111111", "A", Integer.valueOf(5),
                                    Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(1), "OP");

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
                fail("Exception while querying stu_std_milestone: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("updateDate results")
        void test0007() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final boolean result = StudentStandardMilestoneLogic.INFORMIX.updateDate(cache, RAW1,
                            RAW1NEWDATE.msDate);
                    assertTrue(result, "updateDate returned false");

                    final StudentStandardMilestoneRec r =
                            StudentStandardMilestoneLogic.INFORMIX.query(cache, "111111111", "A", Integer.valueOf(5),
                                    Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(1), "OP");

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1NEWDATE.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while updating date: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("delete results")
        void test0008() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();
                final Cache cache = new Cache(informixProfile, conn);

                try {
                    final boolean result = StudentStandardMilestoneLogic.INFORMIX.delete(cache, RAW5);
                    assertTrue(result, "delete returned false");

                    final List<StudentStandardMilestoneRec> all =
                            StudentStandardMilestoneLogic.INFORMIX.queryAll(cache);

                    assertEquals(5, all.size(), "Incorrect record count from queryAll after delete");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;
                    boolean found6 = false;

                    for (final StudentStandardMilestoneRec r : all) {
                        if (RAW1NEWDATE.equals(r)) {
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

                    assertTrue(found1, "stu_std_milestone 1 not found");
                    assertTrue(found2, "stu_std_milestone 2 not found");
                    assertTrue(found3, "stu_std_milestone 3 not found");
                    assertTrue(found4, "stu_std_milestone 4 not found");
                    assertTrue(found6, "stu_std_milestone 6 not found");

                } finally {
                    informixCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting stu_std_milestones: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = informixCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM stu_std_milestone");
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
     * Tests for the {@code StudentStandardMilestoneLogic} class.
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
                        stmt.executeUpdate("DELETE FROM term_t.stu_std_milestone");
                    }
                    conn.commit();

                    final Cache cache = new Cache(postgresProfile, conn);

                    assertTrue(StudentStandardMilestoneLogic.POSTGRES.insert(cache, RAW1),
                            "Failed to insert Postgres stu_std_milestone");

                    assertTrue(StudentStandardMilestoneLogic.POSTGRES.insert(cache, RAW2),
                            "Failed to insert Postgres stu_std_milestone");

                    assertTrue(StudentStandardMilestoneLogic.POSTGRES.insert(cache, RAW3),
                            "Failed to insert Postgres stu_std_milestone");

                    assertTrue(StudentStandardMilestoneLogic.POSTGRES.insert(cache, RAW4),
                            "Failed to insert Postgres stu_std_milestone");

                    assertTrue(StudentStandardMilestoneLogic.POSTGRES.insert(cache, RAW5),
                            "Failed to insert Postgres stu_std_milestone");

                    assertTrue(StudentStandardMilestoneLogic.POSTGRES.insert(cache, RAW6),
                            "Failed to insert Postgres stu_std_milestone");
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Postgres 'term_t.stu_std_milestone' table: " + ex.getMessage());
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
                    final List<StudentStandardMilestoneRec> all =
                            StudentStandardMilestoneLogic.POSTGRES.queryAll(cache);

                    assertEquals(6, all.size(), "Incorrect record count from Postgres queryAll");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;
                    boolean found5 = false;
                    boolean found6 = false;

                    for (final StudentStandardMilestoneRec r : all) {
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

                    assertTrue(found1, "Postgres stu_std_milestone 1 not found");
                    assertTrue(found2, "Postgres stu_std_milestone 2 not found");
                    assertTrue(found3, "Postgres stu_std_milestone 3 not found");
                    assertTrue(found4, "Postgres stu_std_milestone 4 not found");
                    assertTrue(found5, "Postgres stu_std_milestone 5 not found");
                    assertTrue(found6, "Postgres stu_std_milestone 6 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Postgres 'stu_std_milestone' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres queryByStuPaceTrackPace results")
        void test0004() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<StudentStandardMilestoneRec> all =
                            StudentStandardMilestoneLogic.POSTGRES.queryByStuPaceTrackPace(cache, "111111111", "A",
                                    Integer.valueOf(5));

                    assertEquals(4, all.size(), "Incorrect record count from Postgres queryByStuPaceTrackPace");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;

                    for (final StudentStandardMilestoneRec r : all) {
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

                    assertTrue(found1, "Postgres stu_std_milestone 1 not found");
                    assertTrue(found2, "Postgres stu_std_milestone 2 not found");
                    assertTrue(found3, "Postgres stu_std_milestone 3 not found");
                    assertTrue(found4, "Postgres stu_std_milestone 4 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Postgres 'stu_std_milestone' rows by track and pace: "
                        + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Postgres queryByStuPaceTrackPaceIndex results")
        void test0005() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final List<StudentStandardMilestoneRec> all =
                            StudentStandardMilestoneLogic.POSTGRES.queryByStuPaceTrackPaceIndex(cache, "111111111",
                                    "A", Integer.valueOf(5), Integer.valueOf(3));

                    assertEquals(3, all.size(), "Incorrect record count from Postgres queryByStuPaceTrackPaceIndex");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;

                    for (final StudentStandardMilestoneRec r : all) {
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

                    assertTrue(found1, "Postgres stu_std_milestone 1 not found");
                    assertTrue(found2, "Postgres stu_std_milestone 2 not found");
                    assertTrue(found3, "Postgres stu_std_milestone 3 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Postgres 'stu_std_milestone' rows by track, pace, index: "
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
                    final StudentStandardMilestoneRec r =
                            StudentStandardMilestoneLogic.POSTGRES.query(cache, "111111111", "A", Integer.valueOf(5),
                                    Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(1), "OP");

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
                fail("Exception while querying stu_std_milestone: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("updateDate results")
        void test0007() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final boolean result = StudentStandardMilestoneLogic.POSTGRES.updateDate(cache, RAW1,
                            RAW1NEWDATE.msDate);
                    assertTrue(result, "updateDate returned false");

                    final StudentStandardMilestoneRec r =
                            StudentStandardMilestoneLogic.POSTGRES.query(cache, "111111111", "A", Integer.valueOf(5),
                                    Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(1), "OP");

                    assertNotNull(r, "No record returned by query");

                    if (!RAW1NEWDATE.equals(r)) {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while updating date: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("delete results")
        void test0008() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();
                final Cache cache = new Cache(postgresProfile, conn);

                try {
                    final boolean result = StudentStandardMilestoneLogic.POSTGRES.delete(cache, RAW5);
                    assertTrue(result, "delete returned false");

                    final List<StudentStandardMilestoneRec> all =
                            StudentStandardMilestoneLogic.POSTGRES.queryAll(cache);

                    assertEquals(5, all.size(), "Incorrect record count from queryAll after delete");

                    boolean found1 = false;
                    boolean found2 = false;
                    boolean found3 = false;
                    boolean found4 = false;
                    boolean found6 = false;

                    for (final StudentStandardMilestoneRec r : all) {
                        if (RAW1NEWDATE.equals(r)) {
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

                    assertTrue(found1, "stu_std_milestone 1 not found");
                    assertTrue(found2, "stu_std_milestone 2 not found");
                    assertTrue(found3, "stu_std_milestone 3 not found");
                    assertTrue(found4, "stu_std_milestone 4 not found");
                    assertTrue(found6, "stu_std_milestone 6 not found");

                } finally {
                    postgresCtx.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting stu_std_milestones: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = postgresCtx.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM term_t.stu_std_milestone");
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

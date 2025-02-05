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
import dev.mathops.db.rec.AssignmentRec;
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
 * Tests for the {@code AssignmentLogic} class.
 */
final class TestAssignmentLogic {

    /** A raw test record. */
    private static final AssignmentRec RAW1 = new AssignmentRec("1718H", "HW", RawRecordConstants.M117,
            Integer.valueOf(1), Integer.valueOf(8), "17.1.1.8H", "1.1.8H", LocalDateTime.of(2021, 1, 1, 0, 0, 0), null);

    /** A raw test record. */
    private static final AssignmentRec RAW2 = new AssignmentRec("1718L", "LB", RawRecordConstants.M117,
            Integer.valueOf(1), Integer.valueOf(8), "17.1.1.8L", "1.1.8L", LocalDateTime.of(2021, 1, 2, 0, 0, 0), null);

    /** A raw test record. */
    private static final AssignmentRec RAW3 = new AssignmentRec("1719H", "HW", RawRecordConstants.M117,
            Integer.valueOf(1), Integer.valueOf(9), "17.1.1.9H", "1.1.9H", LocalDateTime.of(2021, 1, 3, 0, 0, 0), null);

    /** A raw test record. */
    private static final AssignmentRec RAW4 = new AssignmentRec("1721E", "HW", RawRecordConstants.M117,
            Integer.valueOf(2), Integer.valueOf(1), "17.1.2.1H", "1.2.1H", LocalDateTime.of(2021, 1, 4, 0, 0, 0), null);

    /** A raw test record. */
    private static final AssignmentRec RAW5 = new AssignmentRec("1818H", "HW", RawRecordConstants.M118,
            Integer.valueOf(1), Integer.valueOf(8), "18.1.1.8H", "1.1.8H", LocalDateTime.of(2021, 1, 5, 0, 0, 0), null);

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final AssignmentRec r) {

        Log.warning("Unexpected assignmentId ", r.assignmentId);
        Log.warning("Unexpected courseId ", r.courseId);
        Log.warning("Unexpected unit ", r.unit);
        Log.warning("Unexpected objective ", r.objective);
        Log.warning("Unexpected title ", r.title);
        Log.warning("Unexpected treeRef ", r.treeRef);
        Log.warning("Unexpected assignmentType ", r.assignmentType);
        Log.warning("Unexpected whenActive ", r.whenActive);
        Log.warning("Unexpected whenPulled ", r.whenPulled);
    }

    /**
     * Tests for the {@code AssignmentLogic} class.
     */
    @Nested
    final class Informix {

        /** The Informix database profile. */
        static Profile informixProfile;

        /** The Informix database login. */
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
                    stmt.executeUpdate("DELETE FROM homework");
                }
                conn.commit();

                final Cache cache = new Cache(informixProfile);
                final AssignmentLogic logic = AssignmentLogic.get(cache);

                assertTrue(logic.insert(cache, RAW1), "Failed to insert Informix homework");
                assertTrue(logic.insert(cache, RAW2), "Failed to insert Informix homework");
                assertTrue(logic.insert(cache, RAW3), "Failed to insert Informix homework");
                assertTrue(logic.insert(cache, RAW4), "Failed to insert Informix homework");
                assertTrue(logic.insert(cache, RAW5), "Failed to insert Informix homework");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Informix 'homework' table: " + ex.getMessage());
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
            final AssignmentLogic logic = AssignmentLogic.get(cache);

            try {
                final List<AssignmentRec> all = logic.queryAll(cache);

                assertEquals(5, all.size(), "Incorrect record count from Informix queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;

                for (final AssignmentRec r : all) {
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
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "Informix homework 1 not found");
                assertTrue(found2, "Informix homework 2 not found");
                assertTrue(found3, "Informix homework 3 not found");
                assertTrue(found4, "Informix homework 4 not found");
                assertTrue(found5, "Informix homework 5 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Informix 'homework' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryActiveByCourse results")
        void test0004() {

            final Cache cache = new Cache(informixProfile);
            final AssignmentLogic logic = AssignmentLogic.get(cache);

            try {
                final List<AssignmentRec> all = logic.queryActiveByCourse(cache, RawRecordConstants.M117, null);

                assertEquals(4, all.size(), "Incorrect record count from Informix queryActiveByCourse");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final AssignmentRec r : all) {
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

                assertTrue(found1, "Informix homework 1 not found");
                assertTrue(found2, "Informix homework 2 not found");
                assertTrue(found3, "Informix homework 3 not found");
                assertTrue(found4, "Informix homework 4 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'homework' rows by course: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("query results")
        void test0008() {

            final Cache cache = new Cache(informixProfile);
            final AssignmentLogic logic = AssignmentLogic.get(cache);

            try {
                final AssignmentRec r = logic.query(cache, "1718H");

                assertNotNull(r, "No record returned by Informix query");

                if (!RAW1.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying homework by version: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("delete results")
        void test0009() {

            final Cache cache = new Cache(informixProfile);
            final AssignmentLogic logic = AssignmentLogic.get(cache);

            try {
                final boolean result = logic.delete(cache, RAW2);
                assertTrue(result, "delete returned false");

                final List<AssignmentRec> all = logic.queryAll(cache);

                assertEquals(4, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;

                for (final AssignmentRec r : all) {
                    if (RAW1.equals(r)) {
                        found1 = true;
                    } else if (RAW3.equals(r)) {
                        found3 = true;
                    } else if (RAW4.equals(r)) {
                        found4 = true;
                    } else if (RAW5.equals(r)) {
                        found5 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found1, "homework 1 not found");
                assertTrue(found3, "homework 3 not found");
                assertTrue(found4, "homework 4 not found");
                assertTrue(found5, "homework 5 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting homeworks: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = informixLogin.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM homework");
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

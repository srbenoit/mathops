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
import dev.mathops.db.rec.StandardMilestoneRec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
 * Tests for the {@code StandardMilestoneLogic} class.
 */
final class TestStandardMilestoneLogic {

    /** A raw test record. */
    private static final StandardMilestoneRec RAW1 = new StandardMilestoneRec("A", Integer.valueOf(5),
            Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(1), "OP", LocalDate.of(2023, 1, 1));

    /** A raw test record. */
    private static final StandardMilestoneRec RAW2 = new StandardMilestoneRec("A", Integer.valueOf(5),
            Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(1), "MA", LocalDate.of(2023, 1, 2));

    /** A raw test record. */
    private static final StandardMilestoneRec RAW3 = new StandardMilestoneRec("A", Integer.valueOf(5),
            Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(2), "OP", LocalDate.of(2023, 1, 3));

    /** A raw test record. */
    private static final StandardMilestoneRec RAW4 = new StandardMilestoneRec("A", Integer.valueOf(5),
            Integer.valueOf(4), Integer.valueOf(7), Integer.valueOf(1), "EX", LocalDate.of(2023, 1, 4));

    /** A raw test record. */
    private static final StandardMilestoneRec RAW5 = new StandardMilestoneRec("A", Integer.valueOf(4),
            Integer.valueOf(3), Integer.valueOf(6), Integer.valueOf(2), "CD", LocalDate.of(2023, 1, 5));

    /** A raw test record. */
    private static final StandardMilestoneRec RAW6 = new StandardMilestoneRec("B", Integer.valueOf(5),
            Integer.valueOf(1), Integer.valueOf(5), Integer.valueOf(3), "MA", LocalDate.of(2023, 1, 6));

    /** A raw test record. */
    private static final StandardMilestoneRec RAW1NEWDATE = new StandardMilestoneRec("A", Integer.valueOf(5),
            Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(1), "OP", LocalDate.of(2023, 12, 10));

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StandardMilestoneRec r) {

        Log.warning("Unexpected paceTrack ", r.paceTrack);
        Log.warning("Unexpected pace ", r.pace);
        Log.warning("Unexpected paceIndex ", r.paceIndex);
        Log.warning("Unexpected unit ", r.unit);
        Log.warning("Unexpected objective ", r.objective);
        Log.warning("Unexpected msType ", r.msType);
        Log.warning("Unexpected msDate ", r.msDate);
    }

    /**
     * Tests for the {@code StandardMilestoneLogic} class.
     */
    @Nested
    final class Informix {

        /** The Informix database profile. */
        public static Profile informixProfile;

        /** The Informix database context. */
        public static Login informixLogin;

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
                    stmt.executeUpdate("DELETE FROM std_milestone");
                }
                conn.commit();

                final StandardMilestoneLogic logic = StandardMilestoneLogic.get(cache);

                assertTrue(logic.insert(cache, RAW1), "Failed to insert Informix std_milestone");
                assertTrue(logic.insert(cache, RAW2), "Failed to insert Informix std_milestone");
                assertTrue(logic.insert(cache, RAW3), "Failed to insert Informix std_milestone");
                assertTrue(logic.insert(cache, RAW4), "Failed to insert Informix std_milestone");
                assertTrue(logic.insert(cache, RAW5), "Failed to insert Informix std_milestone");
                assertTrue(logic.insert(cache, RAW6), "Failed to insert Informix std_milestone");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while initializing Informix 'std_milestone' table: " + ex.getMessage());
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

            final StandardMilestoneLogic logic = StandardMilestoneLogic.get(cache);

            try {
                final List<StandardMilestoneRec> all = logic.queryAll(cache);

                assertEquals(6, all.size(), "Incorrect record count from Informix queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;

                for (final StandardMilestoneRec r : all) {
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

                assertTrue(found1, "Informix std_milestone 1 not found");
                assertTrue(found2, "Informix std_milestone 2 not found");
                assertTrue(found3, "Informix std_milestone 3 not found");
                assertTrue(found4, "Informix std_milestone 4 not found");
                assertTrue(found5, "Informix std_milestone 5 not found");
                assertTrue(found6, "Informix std_milestone 6 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Informix 'std_milestone' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByPaceTrackPace results")
        void test0004() {

            final Cache cache = new Cache(informixProfile);
            final StandardMilestoneLogic logic = StandardMilestoneLogic.get(cache);

            try {
                final List<StandardMilestoneRec> all = logic.queryByPaceTrackPace(cache, "A", Integer.valueOf(5));

                assertEquals(4, all.size(), "Incorrect record count from Informix queryByPaceTrackPace");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final StandardMilestoneRec r : all) {
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

                assertTrue(found1, "Informix std_milestone 1 not found");
                assertTrue(found2, "Informix std_milestone 2 not found");
                assertTrue(found3, "Informix std_milestone 3 not found");
                assertTrue(found4, "Informix std_milestone 4 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'std_milestone' rows by track and pace: "
                     + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByPaceTrackPaceIndex results")
        void test0005() {

            final Cache cache = new Cache(informixProfile);
            final StandardMilestoneLogic logic = StandardMilestoneLogic.get(cache);

            try {
                final List<StandardMilestoneRec> all = logic.queryByPaceTrackPaceIndex(cache, "A",
                        Integer.valueOf(5), Integer.valueOf(3));
                assertEquals(3, all.size(), "Incorrect record count from Informix queryByPaceTrackPaceIndex");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final StandardMilestoneRec r : all) {
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

                assertTrue(found1, "Informix std_milestone 1 not found");
                assertTrue(found2, "Informix std_milestone 2 not found");
                assertTrue(found3, "Informix std_milestone 3 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'std_milestone' rows by track, pace, index: "
                     + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("query results")
        void test0006() {

            final Cache cache = new Cache(informixProfile);
            final StandardMilestoneLogic logic = StandardMilestoneLogic.get(cache);

            try {
                final StandardMilestoneRec r = logic.query(cache, "A", Integer.valueOf(5), Integer.valueOf(3),
                        Integer.valueOf(8), Integer.valueOf(1), "OP");

                assertNotNull(r, "No record returned by query");

                if (!RAW1.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying std_milestone: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("updateDate results")
        void test0007() {

            final Cache cache = new Cache(informixProfile);
            final StandardMilestoneLogic logic = StandardMilestoneLogic.get(cache);

            try {
                final boolean result = logic.updateDate(cache, RAW1, RAW1NEWDATE.msDate);
                assertTrue(result, "updateDate returned false");

                final StandardMilestoneRec r = logic.query(cache, "A", Integer.valueOf(5), Integer.valueOf(3),
                        Integer.valueOf(8), Integer.valueOf(1), "OP");

                assertNotNull(r, "No record returned by query");

                if (!RAW1NEWDATE.equals(r)) {
                    printUnexpected(r);
                    fail("Extra record found");
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

            final Cache cache = new Cache(informixProfile);
            final StandardMilestoneLogic logic = StandardMilestoneLogic.get(cache);

            try {
                final boolean result = logic.delete(cache, RAW5);
                assertTrue(result, "delete returned false");

                final List<StandardMilestoneRec> all = logic.queryAll(cache);

                assertEquals(5, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found6 = false;

                for (final StandardMilestoneRec r : all) {
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

                assertTrue(found1, "std_milestone 1 not found");
                assertTrue(found2, "std_milestone 2 not found");
                assertTrue(found3, "std_milestone 3 not found");
                assertTrue(found4, "std_milestone 4 not found");
                assertTrue(found6, "std_milestone 6 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while deleting std_milestones: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            try {
                final DbConnection conn = informixLogin.checkOutConnection();

                try {
                    try (final Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("DELETE FROM std_milestone");
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

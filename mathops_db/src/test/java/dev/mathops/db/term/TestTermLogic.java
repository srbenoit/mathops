package dev.mathops.db.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbUse;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.cfg.Facet;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.db.type.TermKey;
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
 * Tests for the {@code TermLogic} class.
 */
final class TestTermLogic {

    /** A raw test record. */
    private static final TermRec RAW1 = new TermRec(new TermKey(202190),
            LocalDate.of(2021, 9, 1), LocalDate.of(2021, 10, 1), "2122",
            Integer.valueOf(-2), LocalDate.of(2021, 9, 11), LocalDate.of(2021, 9, 21),
            LocalDate.of(2022, 10, 12));

    /** A raw test record. */
    private static final TermRec RAW2 = new TermRec(new TermKey(202210),
            LocalDate.of(2022, 1, 1), LocalDate.of(2022, 2, 1), "2122",
            Integer.valueOf(-1), LocalDate.of(2022, 1, 11), LocalDate.of(2022, 1, 21),
            LocalDate.of(2022, 10, 13));

    /** A raw test record. */
    private static final TermRec RAW3 = new TermRec(new TermKey(202260),
            LocalDate.of(2022, 6, 1), LocalDate.of(2022, 7, 1), "2223",
            Integer.valueOf(0), LocalDate.of(2022, 6, 11), LocalDate.of(2022, 6, 21),
            LocalDate.of(2022, 10, 14));

    /** A raw test record. */
    private static final TermRec RAW4 = new TermRec(new TermKey(202290),
            LocalDate.of(2022, 9, 2), LocalDate.of(2022, 10, 2), "2223",
            Integer.valueOf(1), LocalDate.of(2022, 9, 12), LocalDate.of(2022, 9, 22),
            LocalDate.of(2022, 10, 15));

    /** A raw test record. */
    private static final TermRec RAW5 = new TermRec(new TermKey(202310),
            LocalDate.of(2023, 1, 2), LocalDate.of(2023, 2, 2), "2223",
            Integer.valueOf(2), LocalDate.of(2023, 1, 12), LocalDate.of(2023, 1, 22),
            LocalDate.of(2022, 10, 16));

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final TermRec r) {

        Log.warning("Unexpected term ", r.term == null ? "null" : r.term.serializedString());
        Log.warning("Unexpected startDate ", r.startDate);
        Log.warning("Unexpected endDate ", r.endDate);
        Log.warning("Unexpected academicYear ", r.academicYear);
        Log.warning("Unexpected activeIndex ", r.activeIndex);
        Log.warning("Unexpected dropDeadline ", r.dropDeadline);
        Log.warning("Unexpected withdrawDeadline ", r.withdrawDeadline);
        Log.warning("Unexpected incDeadline ", r.incDeadline);
    }

    /**
     * Tests for the {@code TermLogic} class.
     */
    @Nested
    final class Informix {

        /** The Informix database profile. */
        static Profile informixProfile;

        /** The Informix database context. */
        static Facet informixFacet;

        /** Initialize the test class. */
        @BeforeAll
        static void initTests() {

            informixProfile = DatabaseConfig.getDefault().getCodeProfile(Contexts.INFORMIX_TEST_PATH);
            if (informixProfile == null) {
                throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_IFXTEST_PROFILE));
            }

            informixFacet = informixProfile.getFacet(ESchema.LEGACY);
            if (informixFacet == null) {
                throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_IFXPRIMARY_CONTEXT));
            }

            // Make sure the PostgreSQL connection is using a TEST schema
            if (informixFacet.data.use != EDbUse.TEST) {
                throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST,
                        informixFacet.data.use));
            }

            final DbConnection conn = informixFacet.login.checkOutConnection();
            final Cache cache = new Cache(informixProfile);

            // Make sure the Informix connection is accessing the TEST database
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
                    stmt.executeUpdate("DELETE FROM term");
                }
                conn.commit();

                assertTrue(TermLogic.Informix.INSTANCE.insert(cache, RAW1), "Failed to insert Informix term");
                assertTrue(TermLogic.Informix.INSTANCE.insert(cache, RAW2), "Failed to insert Informix term");
                assertTrue(TermLogic.Informix.INSTANCE.insert(cache, RAW3), "Failed to insert Informix term");
                assertTrue(TermLogic.Informix.INSTANCE.insert(cache, RAW4), "Failed to insert Informix term");
                assertTrue(TermLogic.Informix.INSTANCE.insert(cache, RAW5), "Failed to insert Informix term");
            } catch (final SQLException ex) {
                fail("Exception while initializing Informix 'term' table: " + ex.getMessage());
                throw new IllegalArgumentException(ex);
            } finally {
                informixFacet.login.checkInConnection(conn);
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryAll results")
        void test0003() {

            final Cache cache = new Cache(informixProfile);

            try {
                final List<TermRec> all = TermLogic.Informix.INSTANCE.queryAll(cache);

                assertEquals(5, all.size(), "Incorrect record count from Informix queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;

                for (final TermRec r : all) {
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

                assertTrue(found1, "Informix term 1 not found");
                assertTrue(found2, "Informix term 2 not found");
                assertTrue(found3, "Informix term 3 not found");
                assertTrue(found4, "Informix term 4 not found");
                assertTrue(found5, "Informix term 5 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying all Informix 'term' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryByIndex results")
        void test0004() {

            final Cache cache = new Cache(informixProfile);

            try {
                final TermRec index2 = TermLogic.Informix.INSTANCE.queryByIndex(cache, 2);

                assertNotNull(index2, "Informix queryByIndex returned null");
                assertEquals(RAW5, index2, "Informix term index 2 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'term' rows by index: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix getFutureTerms results")
        void test0005() {

            final Cache cache = new Cache(informixProfile);

            try {
                final List<TermRec> all = TermLogic.Informix.INSTANCE.getFutureTerms(cache);

                assertEquals(2, all.size(), "Incorrect record count from Informix getFutureTerms");

                boolean found4 = false;
                boolean found5 = false;

                for (final TermRec r : all) {
                    if (RAW4.equals(r)) {
                        found4 = true;
                    } else if (RAW5.equals(r)) {
                        found5 = true;
                    } else {
                        printUnexpected(r);
                        fail("Extra record found");
                    }
                }

                assertTrue(found4, "Informix term 4 not found");
                assertTrue(found5, "Informix term 5 not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix future 'term' rows: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryActive results")
        void test0006() {

            final Cache cache = new Cache(informixProfile);

            try {
                final TermRec active = TermLogic.Informix.INSTANCE.queryActive(cache);

                assertNotNull(active, "Informix queryActive returned null");
                assertEquals(RAW3, active, "Informix active term not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix active 'term' row: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryNext results")
        void test0007() {

            final Cache cache = new Cache(informixProfile);

            try {
                final TermRec active = TermLogic.Informix.INSTANCE.queryNext(cache);

                assertNotNull(active, "Informix queryNext returned null");
                assertEquals(RAW4, active, "Informix next term not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix next 'term' row: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix queryPrior results")
        void test0008() {

            final Cache cache = new Cache(informixProfile);

            try {
                final TermRec active = TermLogic.Informix.INSTANCE.queryPrior(cache);

                assertNotNull(active, "Informix queryPrior returned null");
                assertEquals(RAW2, active, "Informix prior term not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix prior 'term' row: " + ex.getMessage());
            }
        }

        /** Test case. */
        @Test
        @DisplayName("Informix query results")
        void test0009() {

            final Cache cache = new Cache(informixProfile);

            try {
                final TermRec active = TermLogic.Informix.INSTANCE.query(cache, RAW1.term);

                assertNotNull(active, "Informix query returned null");
                assertEquals(RAW1, active, "Informix term not found");
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while querying Informix 'term' row: " + ex.getMessage());
            }
        }

        /** Clean up. */
        @AfterAll
        static void cleanUp() {

            final DbConnection conn = informixFacet.login.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM term");
                }

                conn.commit();
            } catch (final SQLException ex) {
                Log.warning(ex);
                fail("Exception while cleaning tables: " + ex.getMessage());
            } finally {
                informixFacet.login.checkInConnection(conn);
            }
        }
    }
}

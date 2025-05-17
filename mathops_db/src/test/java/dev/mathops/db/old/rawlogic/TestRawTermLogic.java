package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.db.type.TermKey;
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
 * Tests for the {@code RawTermLogic} class.
 */
final class TestRawTermLogic {

    /** A term key used in test records. */
    private static final TermKey termFA21 = new TermKey("FA21");

    /** A term key used in test records. */
    private static final TermKey termSM21 = new TermKey("SM21");

    /** A term key used in test records. */
    private static final TermKey termSP22 = new TermKey("SP22");

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 8, 11);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 12, 14);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 11, 13);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2021, 11, 14);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2021, 5, 12);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2021, 8, 10);

    /** A date used in test records. */
    private static final LocalDate date7 = LocalDate.of(2021, 8, 5);

    /** A date used in test records. */
    private static final LocalDate date8 = LocalDate.of(2021, 8, 6);

    /** A date used in test records. */
    private static final LocalDate date9 = LocalDate.of(2021, 12, 15);

    /** A date used in test records. */
    private static final LocalDate date10 = LocalDate.of(2022, 5, 10);

    /** A date used in test records. */
    private static final LocalDate date11 = LocalDate.of(2022, 4, 16);

    /** A date used in test records. */
    private static final LocalDate date12 = LocalDate.of(2022, 4, 17);

    /** A date used in test records. */
    private static final LocalDate date13 = LocalDate.of(2023, 5, 18);

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
                        throw new IllegalArgumentException(
                                TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                    }
                } else {
                    throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                }
            }

            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM term");
            }
            conn.commit();

            final TermRec raw1 = new TermRec(termFA21, date1, date2, "2122", Integer.valueOf(0), date3, date4, date13);
            final TermRec raw2 = new TermRec(termSM21, date5, date6, "2122", Integer.valueOf(-1), date7, date8, date13);
            final TermRec raw3 = new TermRec(termSP22, date9, date10, "2122", Integer.valueOf(1), date11, date12,
                    date13);

            assertTrue(TermLogic.get(cache).insert(cache, raw1), "Failed to insert term");
            assertTrue(TermLogic.get(cache).insert(cache, raw2), "Failed to insert term");
            assertTrue(TermLogic.get(cache).insert(cache, raw3), "Failed to insert term");
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
            final List<TermRec> all = TermLogic.get(cache).queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final TermRec test : all) {

                if (termFA21.equals(test.term)
                    && date1.equals(test.startDate)
                    && date2.equals(test.endDate)
                    && "2122".equals(test.academicYear)
                    && Integer.valueOf(0).equals(test.activeIndex)
                    && date3.equals(test.dropDeadline)
                    && date4.equals(test.withdrawDeadline)
                    && date13.equals(test.incDeadline)) {

                    found1 = true;

                } else if (termSM21.equals(test.term)
                           && date5.equals(test.startDate)
                           && date6.equals(test.endDate)
                           && "2122".equals(test.academicYear)
                           && Integer.valueOf(-1).equals(test.activeIndex)
                           && date7.equals(test.dropDeadline)
                           && date8.equals(test.withdrawDeadline)
                           && date13.equals(test.incDeadline)) {

                    found2 = true;

                } else if (termSP22.equals(test.term)
                           && date9.equals(test.startDate)
                           && date10.equals(test.endDate)
                           && "2122".equals(test.academicYear)
                           && Integer.valueOf(1).equals(test.activeIndex)
                           && date11.equals(test.dropDeadline)
                           && date12.equals(test.withdrawDeadline)
                           && date13.equals(test.incDeadline)) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected termKey ", test.term);
                    Log.warning("Unexpected startDt ", test.startDate);
                    Log.warning("Unexpected endDt ", test.endDate);
                    Log.warning("Unexpected academicYr ", test.academicYear);
                    Log.warning("Unexpected activeIndex ", test.activeIndex);
                    Log.warning("Unexpected dropDeadline ", test.dropDeadline);
                    Log.warning("Unexpected wDropDt ", test.withdrawDeadline);
                }
            }

            assertTrue(found1, "Term 1 not found");
            assertTrue(found2, "Term 2 not found");
            assertTrue(found3, "Term 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all term rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryActive results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final TermRec test = TermLogic.get(cache).queryActive(cache);

            assertNotNull(test, "queryActive by term returned no record");

            final boolean found = termFA21.equals(test.term)
                                  && date1.equals(test.startDate)
                                  && date2.equals(test.endDate)
                                  && "2122".equals(test.academicYear)
                                  && Integer.valueOf(0).equals(test.activeIndex)
                                  && date3.equals(test.dropDeadline)
                                  && date4.equals(test.withdrawDeadline)
                                  && date13.equals(test.incDeadline);

            assertTrue(found, "Active term not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying active term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryPrior results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final TermRec test = TermLogic.get(cache).queryPrior(cache);

            assertNotNull(test, "queryPrior by term returned no record");

            final boolean found = termSM21.equals(test.term)
                                  && date5.equals(test.startDate)
                                  && date6.equals(test.endDate)
                                  && "2122".equals(test.academicYear)
                                  && Integer.valueOf(-1).equals(test.activeIndex)
                                  && date7.equals(test.dropDeadline)
                                  && date8.equals(test.withdrawDeadline)
                                  && date13.equals(test.incDeadline);

            assertTrue(found, "Prior term not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying prior term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryNext results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final TermRec test = TermLogic.get(cache).queryNext(cache);

            assertNotNull(test, "queryNext by term returned no record");

            final boolean found = termSP22.equals(test.term)
                                  && date9.equals(test.startDate)
                                  && date10.equals(test.endDate)
                                  && "2122".equals(test.academicYear)
                                  && Integer.valueOf(1).equals(test.activeIndex)
                                  && date11.equals(test.dropDeadline)
                                  && date12.equals(test.withdrawDeadline)
                                  && date13.equals(test.incDeadline);

            assertTrue(found, "Next term not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying next term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            final TermRec test = TermLogic.get(cache).query(cache, termSM21);

            assertNotNull(test, "query by term returned no record");

            final boolean found = termSM21.equals(test.term)
                                  && date5.equals(test.startDate)
                                  && date6.equals(test.endDate)
                                  && "2122".equals(test.academicYear)
                                  && Integer.valueOf(-1).equals(test.activeIndex)
                                  && date7.equals(test.dropDeadline)
                                  && date8.equals(test.withdrawDeadline)
                                  && date13.equals(test.incDeadline);

            assertTrue(found, "Queried term not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByIndex results")
    void test0008() {

        final Cache cache = new Cache(profile);

        try {
            final TermRec test = TermLogic.get(cache).queryByIndex(cache, -1);

            assertNotNull(test, "queryByIndex returned no record");

            final boolean found = termSM21.equals(test.term)
                                  && date5.equals(test.startDate)
                                  && date6.equals(test.endDate)
                                  && "2122".equals(test.academicYear)
                                  && Integer.valueOf(-1).equals(test.activeIndex)
                                  && date7.equals(test.dropDeadline)
                                  && date8.equals(test.withdrawDeadline)
                                  && date13.equals(test.incDeadline);

            assertTrue(found, "Queried index term not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying term by index: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getFutureTerms results")
    void test0009() {

        final Cache cache = new Cache(profile);

        try {
            final List<TermRec> all = TermLogic.get(cache).getFutureTerms(cache);

            assertEquals(1, all.size(), "Incorrect record count from getFutureTerms");

            boolean found3 = false;

            for (final TermRec test : all) {

                if (termSP22.equals(test.term)
                    && date9.equals(test.startDate)
                    && date10.equals(test.endDate)
                    && "2122".equals(test.academicYear)
                    && Integer.valueOf(1).equals(test.activeIndex)
                    && date11.equals(test.dropDeadline)
                    && date12.equals(test.withdrawDeadline)
                    && date13.equals(test.incDeadline)) {

                    found3 = true;
                    break;
                }
            }

            assertTrue(found3, "Term 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all future term rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0010() {

        final Cache cache = new Cache(profile);

        try {
            final TermRec raw2 = new TermRec(termSM21, date5, date6, "2122", Integer.valueOf(-1), date7, date8, date13);

            final boolean result = TermLogic.get(cache).delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<TermRec> all = TermLogic.get(cache).queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final TermRec test : all) {

                if (termFA21.equals(test.term)
                    && date1.equals(test.startDate)
                    && date2.equals(test.endDate)
                    && "2122".equals(test.academicYear)
                    && Integer.valueOf(0).equals(test.activeIndex)
                    && date3.equals(test.dropDeadline)
                    && date4.equals(test.withdrawDeadline)
                    && date13.equals(test.incDeadline)) {

                    found1 = true;

                } else if (termSP22.equals(test.term)
                           && date9.equals(test.startDate)
                           && date10.equals(test.endDate)
                           && "2122".equals(test.academicYear)
                           && Integer.valueOf(1).equals(test.activeIndex)
                           && date11.equals(test.dropDeadline)
                           && date12.equals(test.withdrawDeadline)
                           && date13.equals(test.incDeadline)) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected termKey ", test.term);
                    Log.warning("Unexpected startDt ", test.startDate);
                    Log.warning("Unexpected endDt ", test.endDate);
                    Log.warning("Unexpected academicYr ", test.academicYear);
                    Log.warning("Unexpected activeIndex ", test.activeIndex);
                    Log.warning("Unexpected dropDeadline ", test.dropDeadline);
                    Log.warning("Unexpected wDropDt ", test.withdrawDeadline);
                }
            }

            assertTrue(found1, "Term 1 not found");
            assertTrue(found3, "Term 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting term: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM term");
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

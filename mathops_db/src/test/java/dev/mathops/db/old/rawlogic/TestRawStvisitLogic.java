package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawStvisit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawStvisitLogic} class.
 */
final class TestRawStvisitLogic {

    /** A date/time used in test records. */
    private static final LocalDateTime datetime1 = LocalDateTime.of(2021, 1, 2, 13, 4, 5);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime2 = LocalDateTime.of(2021, 1, 2, 14, 5, 6);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime3 = LocalDateTime.of(2021, 2, 3, 13, 14, 15);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime4 = LocalDateTime.of(2021, 2, 3, 14, 15, 16);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime5 = LocalDateTime.of(2021, 2, 5, 9, 10, 11);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime6 = LocalDateTime.of(2021, 2, 5, 9, 11, 12);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime7 = LocalDateTime.of(2021, 10, 11, 12, 13, 14);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime8 = LocalDateTime.of(2021, 10, 11, 12, 13, 15);

//    /** A date/time used in test records. */
//    private static final LocalDateTime datetime9 = LocalDateTime.of(2021, 10, 11, 12, 13, 16);

    /** A date/time used in test records. */
    private static final LocalDateTime datetime10 = LocalDateTime.of(2021, 10, 11, 12, 13, 17);

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
                stmt.executeUpdate("DELETE FROM stvisit");
            }
            conn.commit();

            final RawStvisit raw1 = new RawStvisit("111111111", datetime1, datetime2, "TC", "100");
            final RawStvisit raw2 = new RawStvisit("111111111", datetime3, datetime4, "TC", "99");
            final RawStvisit raw3 = new RawStvisit("222222222", datetime5, datetime6, "LC", "AB");

            assertTrue(RawStvisitLogic.insert(cache, raw1), "Failed to insert stvisit 1");
            assertTrue(RawStvisitLogic.insert(cache, raw2), "Failed to insert stvisit 2");
            assertTrue(RawStvisitLogic.insert(cache, raw3), "Failed to insert stvisit 3");
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
            final List<RawStvisit> all = RawStvisitLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawStvisit test : all) {
                if ("111111111".equals(test.stuId)
                    && datetime1.equals(test.whenStarted)
                    && datetime2.equals(test.whenEnded)
                    && "TC".equals(test.location)
                    && "100".equals(test.seat)) {

                    found1 = true;
                } else if ("111111111".equals(test.stuId)
                           && datetime3.equals(test.whenStarted)
                           && datetime4.equals(test.whenEnded)
                           && "TC".equals(test.location)
                           && "99".equals(test.seat)) {

                    found2 = true;
                } else if ("222222222".equals(test.stuId)
                           && datetime5.equals(test.whenStarted)
                           && datetime6.equals(test.whenEnded)
                           && "LC".equals(test.location)
                           && "AB".equals(test.seat)) {

                    found3 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected whenStarted ", test.whenStarted);
                    Log.warning("Unexpected whenEnded ", test.whenEnded);
                    Log.warning("Unexpected location ", test.location);
                    Log.warning("Unexpected seat ", test.seat);
                }
            }

            assertTrue(found1, "Stvisit 1 not found");
            assertTrue(found2, "Stvisit 2 not found");
            assertTrue(found3, "Stvisit 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stvisit rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawStvisit> all = RawStvisitLogic.queryByStudent(cache, "111111111");

            assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawStvisit test : all) {
                if ("111111111".equals(test.stuId)
                    && datetime1.equals(test.whenStarted)
                    && datetime2.equals(test.whenEnded)
                    && "TC".equals(test.location)
                    && "100".equals(test.seat)) {

                    found1 = true;
                } else if ("111111111".equals(test.stuId)
                           && datetime3.equals(test.whenStarted)
                           && datetime4.equals(test.whenEnded)
                           && "TC".equals(test.location)
                           && "99".equals(test.seat)) {

                    found2 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected whenStarted ", test.whenStarted);
                    Log.warning("Unexpected whenEnded ", test.whenEnded);
                    Log.warning("Unexpected location ", test.location);
                    Log.warning("Unexpected seat ", test.seat);
                }
            }

            assertTrue(found1, "Stvisit 1 not found");
            assertTrue(found2, "Stvisit 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stvisit by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("startNewVisit results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            assertTrue(RawStvisitLogic.startNewVisit(cache, "123456789", datetime7, "TC", "1"),
                    "startNewVisit returned false");

            assertTrue(RawStvisitLogic.startNewVisit(cache, "123456789", datetime8, "LC", "2"),
                    "startNewVisit returned false");

            final List<RawStvisit> all = RawStvisitLogic.queryByStudent(cache, "123456789");

            assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawStvisit test : all) {
                if ("123456789".equals(test.stuId)
                    && datetime7.equals(test.whenStarted)
                    && datetime8.equals(test.whenEnded)
                    && "TC".equals(test.location)
                    && "1".equals(test.seat)) {

                    found1 = true;
                } else if ("123456789".equals(test.stuId)
                           && datetime8.equals(test.whenStarted)
                           && test.whenEnded == null
                           && "LC".equals(test.location)
                           && "2".equals(test.seat)) {

                    found2 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected whenStarted ", test.whenStarted);
                    Log.warning("Unexpected whenEnded ", test.whenEnded);
                    Log.warning("Unexpected location ", test.location);
                    Log.warning("Unexpected seat ", test.seat);
                }
            }

            assertTrue(found1, "Stvisit 1 not found");
            assertTrue(found2, "Stvisit 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while starting new visit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getInProgressStudentVisits results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawStvisit> all = RawStvisitLogic.getInProgressStudentVisits(cache, "123456789");

            assertEquals(1, all.size(), "Incorrect record count from queryByStudent");

            boolean found = false;

            for (final RawStvisit test : all) {
                if ("123456789".equals(test.stuId)
                    && datetime8.equals(test.whenStarted)
                    && test.whenEnded == null
                    && "LC".equals(test.location)
                    && "2".equals(test.seat)) {

                    found = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected whenStarted ", test.whenStarted);
                    Log.warning("Unexpected whenEnded ", test.whenEnded);
                    Log.warning("Unexpected location ", test.location);
                    Log.warning("Unexpected seat ", test.seat);
                }
            }

            assertTrue(found, "Stvisit not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying in-progress visits: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("endInProgressVisit results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            assertTrue(RawStvisitLogic.endInProgressVisit(cache, "123456789",
                    datetime10), "endInProgressVisit returned false");

            final List<RawStvisit> all = RawStvisitLogic.getInProgressStudentVisits(cache, "123456789");

            assertEquals(0, all.size(), "Incorrect record count from queryByStudent");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying in-progress visits: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0008() {

        final Cache cache = new Cache(profile);

        try {
            final RawStvisit raw2 = new RawStvisit("111111111", datetime3, datetime4, "TC", "99");

            final boolean result = RawStvisitLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawStvisit> all = RawStvisitLogic.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final RawStvisit test : all) {
                if ("111111111".equals(test.stuId)
                    && datetime1.equals(test.whenStarted)
                    && datetime2.equals(test.whenEnded)
                    && "TC".equals(test.location)
                    && "100".equals(test.seat)) {

                    found1 = true;
                } else if ("222222222".equals(test.stuId)
                           && datetime5.equals(test.whenStarted)
                           && datetime6.equals(test.whenEnded)
                           && "LC".equals(test.location)
                           && "AB".equals(test.seat)) {

                    found3 = true;
                } else if ("123456789".equals(test.stuId)
                           && datetime7.equals(test.whenStarted)
                           && datetime8.equals(test.whenEnded)
                           && "TC".equals(test.location)
                           && "1".equals(test.seat)) {

                    found4 = true;
                } else if ("123456789".equals(test.stuId)
                           && datetime8.equals(test.whenStarted)
                           && datetime10.equals(test.whenEnded)
                           && "LC".equals(test.location)
                           && "2".equals(test.seat)) {

                    found5 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected whenStarted ", test.whenStarted);
                    Log.warning("Unexpected whenEnded ", test.whenEnded);
                    Log.warning("Unexpected location ", test.location);
                    Log.warning("Unexpected seat ", test.seat);
                }
            }

            assertTrue(found1, "Stvisit 1 not found");
            assertTrue(found3, "Stvisit 3 not found");
            assertTrue(found4, "Stvisit 4 not found");
            assertTrue(found5, "Stvisit 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting stvisit: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM stvisit");
            }

            conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning table: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }
}

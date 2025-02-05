package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawMpeLog;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawMpeLogLogic} class.
 */
final class TestRawMpeLogLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(1999, 12, 1);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(1999, 12, 2);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(1999, 12, 3);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2021, 3, 4);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2021, 3, 5);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2021, 7, 8);

    /** A date used in test records. */
    private static final LocalDate date7 = LocalDate.of(2021, 8, 9);

    /** A date used in test records. */
    private static final LocalDate date8 = LocalDate.of(2021, 9, 10);

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
                stmt.executeUpdate("DELETE FROM mpe_log");
            }
            conn.commit();

            final RawMpeLog raw1 = new RawMpeLog("123456789", "9920", RawRecordConstants.M100P, "PPPPP",
                    date1, date2, date3, Long.valueOf(12345L), Integer.valueOf(1000), "123");

            final RawMpeLog raw2 = new RawMpeLog("123456789", "2021", RawRecordConstants.M100P, "MPTTC",
                    date4, date5, null, Long.valueOf(67890L), Integer.valueOf(900), "456");

            final RawMpeLog raw3 = new RawMpeLog("888888888", "2122", RawRecordConstants.M100P, "MPTUN",
                    date6, null, null, Long.valueOf(98989L), Integer.valueOf(800), null);

            assertTrue(RawMpeLogLogic.insert(cache, raw1), "Failed to insert mpe_log");
            assertTrue(RawMpeLogLogic.insert(cache, raw2), "Failed to insert mpe_log");
            assertTrue(RawMpeLogLogic.insert(cache, raw3), "Failed to insert mpe_log");
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
            final List<RawMpeLog> all = RawMpeLogLogic.queryAll(cache);

            assertEquals(3, all.size(), //
                    "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawMpeLog r : all) {

                if ("123456789".equals(r.stuId)
                    && "9920".equals(r.academicYr)
                    && RawRecordConstants.M100P.equals(r.course)
                    && "PPPPP".equals(r.version)
                    && date1.equals(r.startDt)
                    && date2.equals(r.examDt)
                    && date3.equals(r.recoverDt)
                    && Long.valueOf(12345L).equals(r.serialNbr)
                    && Integer.valueOf(1000).equals(r.startTime)
                    && "123".equals(r.calcNbr)) {

                    found1 = true;

                } else if ("123456789".equals(r.stuId)
                           && "2021".equals(r.academicYr)
                           && RawRecordConstants.M100P.equals(r.course)
                           && "MPTTC".equals(r.version)
                           && date4.equals(r.startDt)
                           && date5.equals(r.examDt)
                           && r.recoverDt == null
                           && Long.valueOf(67890L).equals(r.serialNbr)
                           && Integer.valueOf(900).equals(r.startTime)
                           && "456".equals(r.calcNbr)) {

                    found2 = true;

                } else if ("888888888".equals(r.stuId)
                           && "2122".equals(r.academicYr)
                           && RawRecordConstants.M100P.equals(r.course)
                           && "MPTUN".equals(r.version)
                           && date6.equals(r.startDt)
                           && r.examDt == null
                           && r.recoverDt == null
                           && Long.valueOf(98989L).equals(r.serialNbr)
                           && Integer.valueOf(800).equals(r.startTime)
                           && r.calcNbr == null) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected stuId ", r.stuId);
                    Log.warning("Unexpected academicYr ", r.academicYr);
                    Log.warning("Unexpected course ", r.course);
                    Log.warning("Unexpected version ", r.version);
                    Log.warning("Unexpected startDt ", r.startDt);
                    Log.warning("Unexpected examDt ", r.examDt);
                    Log.warning("Unexpected recoverDt ", r.recoverDt);
                    Log.warning("Unexpected serialNbr ", r.serialNbr);
                    Log.warning("Unexpected startTime ", r.startTime);
                    Log.warning("Unexpected calcNbr ", r.calcNbr);
                }
            }

            assertTrue(found1, "mpe_log 1 not found");
            assertTrue(found2, "mpe_log 2 not found");
            assertTrue(found3, "mpe_log 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all mpe_log rows: "
                 + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("indicateFinished results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            assertTrue(RawMpeLogLogic.indicateFinished(cache, //
                            "888888888", date6, Integer.valueOf(800),
                            date7, date8),
                    "indicateFinished failed");

            final List<RawMpeLog> all = RawMpeLogLogic.queryAll(cache);

            assertEquals(3, all.size(), //
                    "Incorrect record count from queryAll");

            boolean found = false;

            for (final RawMpeLog r : all) {
                if ("888888888".equals(r.stuId)
                    && "2122".equals(r.academicYr)
                    && RawRecordConstants.M100P.equals(r.course)
                    && "MPTUN".equals(r.version)
                    && date6.equals(r.startDt)
                    && date7.equals(r.examDt)
                    && date8.equals(r.recoverDt)
                    && Long.valueOf(98989L).equals(r.serialNbr)
                    && Integer.valueOf(800).equals(r.startTime)
                    && r.calcNbr == null) {

                    found = true;
                    break;
                }
            }

            assertTrue(found, "mpe_log was not updated");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating mpe_log row: "
                 + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final RawMpeLog raw3 = new RawMpeLog("888888888", "2122", RawRecordConstants.M100P, "MPTUN",
                    date6, null, null, Long.valueOf(98989L), Integer.valueOf(800), null);

            final boolean result = RawMpeLogLogic.delete(cache, raw3);
            assertTrue(result, "delete returned false");

            final List<RawMpeLog> all = RawMpeLogLogic.queryAll(cache);

            assertEquals(2, all.size(), //
                    "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawMpeLog r : all) {

                if ("123456789".equals(r.stuId)
                    && "9920".equals(r.academicYr)
                    && RawRecordConstants.M100P.equals(r.course)
                    && "PPPPP".equals(r.version)
                    && date1.equals(r.startDt)
                    && date2.equals(r.examDt)
                    && date3.equals(r.recoverDt)
                    && Long.valueOf(12345L).equals(r.serialNbr)
                    && Integer.valueOf(1000).equals(r.startTime)
                    && "123".equals(r.calcNbr)) {

                    found1 = true;

                } else if ("123456789".equals(r.stuId)
                           && "2021".equals(r.academicYr)
                           && RawRecordConstants.M100P.equals(r.course)
                           && "MPTTC".equals(r.version)
                           && date4.equals(r.startDt)
                           && date5.equals(r.examDt)
                           && r.recoverDt == null
                           && Long.valueOf(67890L).equals(r.serialNbr)
                           && Integer.valueOf(900).equals(r.startTime)
                           && "456".equals(r.calcNbr)) {

                    found2 = true;

                } else {
                    Log.warning("Unexpected stuId ", r.stuId);
                    Log.warning("Unexpected academicYr ", r.academicYr);
                    Log.warning("Unexpected course ", r.course);
                    Log.warning("Unexpected version ", r.version);
                    Log.warning("Unexpected startDt ", r.startDt);
                    Log.warning("Unexpected examDt ", r.examDt);
                    Log.warning("Unexpected recoverDt ", r.recoverDt);
                    Log.warning("Unexpected serialNbr ", r.serialNbr);
                    Log.warning("Unexpected startTime ", r.startTime);
                    Log.warning("Unexpected calcNbr ", r.calcNbr);
                }
            }

            assertTrue(found1, "mpe_log 1 not found");
            assertTrue(found2, "mpe_log 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting users: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM mpe_log");
            }

            conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        }
    }
}

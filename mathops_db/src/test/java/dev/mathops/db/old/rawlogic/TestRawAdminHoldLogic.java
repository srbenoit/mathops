package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawAdminHold;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawAdminHoldLogic} classes.
 */
final class TestRawAdminHoldLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 3, 4);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 5, 6);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2022, 11, 12);

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

        // Make sure we're in the TEST database
        try {
            final Login login = profile.getLogin(ESchema.LEGACY);
            final DbConnection conn = login.checkOutConnection();

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
            } finally {
                login.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            throw new IllegalArgumentException(ex);
        }

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();
        final Cache cache = new Cache(profile);

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM admin_hold");
            }
            conn.commit();

            final RawAdminHold raw1 = new RawAdminHold("888888888", "06", "F", Integer.valueOf(1), date1);
            final RawAdminHold raw2 = new RawAdminHold("888888889", "01", "N", Integer.valueOf(2), date2);
            final RawAdminHold raw3 = new RawAdminHold("888888889", "02", "N", Integer.valueOf(3), date3);

            assertTrue(RawAdminHoldLogic.insert(cache, raw1), "Failed to insert admin_hold");
            assertTrue(RawAdminHoldLogic.insert(cache, raw2), "Failed to insert admin_hold");
            assertTrue(RawAdminHoldLogic.insert(cache, raw3), "Failed to insert admin_hold");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing tables: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getAllAdminHolds results")
    void test0003() {

        try {
            final Cache cache = new Cache(profile);

            final List<RawAdminHold> all = RawAdminHoldLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from getAllAdminHolds");

            boolean found806 = false;
            boolean found901 = false;
            boolean found902 = false;

            for (final RawAdminHold test : all) {

                if ("888888888".equals(test.stuId)
                    && "06".equals(test.holdId)
                    && "F".equals(test.sevAdminHold)
                    && Integer.valueOf(1).equals(test.timesDisplay)
                    && date1.equals(test.createDt)) {
                    found806 = true;
                } else if ("888888889".equals(test.stuId)
                           && "01".equals(test.holdId)
                           && "N".equals(test.sevAdminHold)
                           && Integer.valueOf(2).equals(test.timesDisplay)
                           && date2.equals(test.createDt)) {
                    found901 = true;
                } else if ("888888889".equals(test.stuId)
                           && "02".equals(test.holdId)
                           && "N".equals(test.sevAdminHold)
                           && Integer.valueOf(3).equals(test.timesDisplay)
                           && date3.equals(test.createDt)) {
                    found902 = true;
                }
            }

            assertTrue(found806, "AdminHold 06 for 888 not found");
            assertTrue(found901, "AdminHold 01 for 889 not found");
            assertTrue(found902, "AdminHold 02 for 889 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all admin_hold rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getAdminHolds results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawAdminHold> all = RawAdminHoldLogic.queryByStudent(cache, "888888889");

            assertEquals(2, all.size(), "Incorrect record count from getAdminHolds");

            boolean found901 = false;
            boolean found902 = false;

            for (final RawAdminHold test : all) {

                if ("888888889".equals(test.stuId)
                    && "01".equals(test.holdId)
                    && "N".equals(test.sevAdminHold)
                    && Integer.valueOf(2).equals(test.timesDisplay)
                    && date2.equals(test.createDt)) {
                    found901 = true;
                } else if ("888888889".equals(test.stuId)
                           && "02".equals(test.holdId)
                           && "N".equals(test.sevAdminHold)
                           && Integer.valueOf(3).equals(test.timesDisplay)
                           && date3.equals(test.createDt)) {
                    found902 = true;
                }
            }

            assertTrue(found901, "AdminHold 01 for 889 not found");
            assertTrue(found902, "AdminHold 02 for 889 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying admin_hold rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Check of milestone data for student")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            assertFalse(RawAdminHoldLogic.hasFatalHold(cache, "888888887"),
                    "hasFatalHold was incorrect when no hold present");
            assertTrue(RawAdminHoldLogic.hasFatalHold(cache, "888888888"),
                    "hasFatalHold was incorrect when fatal hold present");
            assertFalse(RawAdminHoldLogic.hasFatalHold(cache, "888888889"),
                    "hasFatalHold was incorrect when fatal hold not present");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while inserting admin_hold rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final RawAdminHold toDelete = new RawAdminHold();

            // Primary key fields to locate record
            toDelete.stuId = "888888889";
            toDelete.holdId = "01";

            assertTrue(RawAdminHoldLogic.delete(cache, toDelete), "Delete admin_hold failed");

            final List<RawAdminHold> all = RawAdminHoldLogic.queryByStudent(cache, "888888889");

            assertEquals(1, all.size(), "Incorrect record count from getAdminHolds");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting admin_hold rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateAdminHoldDate")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            final RawAdminHold toUpdate = new RawAdminHold("888888888", "06", "F", Integer.valueOf(1), date4);

            assertTrue(RawAdminHoldLogic.updateAdminHoldDate(cache, toUpdate), "Update of admin_hold failed");

            final RawAdminHold test = RawAdminHoldLogic.query(cache, "888888888", "06");

            assertNotNull(test, "Query after update of admin_hold returned no record");

            assertEquals(date4, test.createDt, "Incorrect createDt after update of admin_hold");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting admin_hold rows: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM admin_hold");
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

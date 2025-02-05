package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawStresource;
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
 * Tests for the {@code RawStresourceLogic} class.
 */
final class TestRawStresourceLogic {

    /** A date used in test records. */
    private static final LocalDate date12 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date13 = LocalDate.of(2021, 1, 3);

    /** A date used in test records. */
    private static final LocalDate date24 = LocalDate.of(2021, 2, 4);

    /** A date used in test records. */
    private static final LocalDate date11 = LocalDate.of(2021, 1, 1);

    /** A date used in test records. */
    private static final LocalDate date34 = LocalDate.of(2021, 3, 4);

    /** A date used in test records. */
    private static final LocalDate date56 = LocalDate.of(2021, 5, 6);

    /** A date used in test records. */
    private static final LocalDate date33 = LocalDate.of(2021, 3, 3);

    /** A date used in test records. */
    private static final LocalDate date12b = LocalDate.of(2020, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date13b = LocalDate.of(2020, 1, 3);

    /** A date used in test records. */
    private static final LocalDate date24b = LocalDate.of(2020, 2, 4);

    /** A date used in test records. */
    private static final LocalDate date11b = LocalDate.of(2020, 1, 1);

    /** A date used in test records. */
    private static final LocalDate date1112 = LocalDate.of(2021, 11, 12);

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
                stmt.executeUpdate("DELETE FROM stresource");
            }
            conn.commit();

            final RawStresource raw1 = new RawStresource("111111111", "res1", date12, Integer.valueOf(300),
                    date13, date24, Integer.valueOf(400), Integer.valueOf(3), date11);

            final RawStresource raw2 = new RawStresource("111111111", "res2", date34, Integer.valueOf(500),
                    date56, null, null, Integer.valueOf(1), date33);

            final RawStresource raw3 = new RawStresource("222222222", "res2", date12b, Integer.valueOf(200),
                    date13b, date24b, Integer.valueOf(300), Integer.valueOf(9), date11b);

            assertTrue(RawStresourceLogic.insert(cache, raw1), "Failed to insert stresource 1");
            assertTrue(RawStresourceLogic.insert(cache, raw2), "Failed to insert stresource 2");
            assertTrue(RawStresourceLogic.insert(cache, raw3), "Failed to insert stresource 3");
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
            final List<RawStresource> all = RawStresourceLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawStresource test : all) {
                if ("111111111".equals(test.stuId)
                    && "res1".equals(test.resourceId)
                    && date12.equals(test.loanDt)
                    && Integer.valueOf(300).equals(test.startTime)
                    && date13.equals(test.dueDt)
                    && date24.equals(test.returnDt)
                    && Integer.valueOf(400).equals(test.finishTime)
                    && Integer.valueOf(3).equals(test.timesDisplay)
                    && date11.equals(test.createDt)) {

                    found1 = true;
                } else if ("111111111".equals(test.stuId)
                           && "res2".equals(test.resourceId)
                           && date34.equals(test.loanDt)
                           && Integer.valueOf(500).equals(test.startTime)
                           && date56.equals(test.dueDt)
                           && test.returnDt == null
                           && test.finishTime == null
                           && Integer.valueOf(1).equals(test.timesDisplay)
                           && date33.equals(test.createDt)) {

                    found2 = true;
                } else if ("222222222".equals(test.stuId)
                           && "res2".equals(test.resourceId)
                           && date12b.equals(test.loanDt)
                           && Integer.valueOf(200).equals(test.startTime)
                           && date13b.equals(test.dueDt)
                           && date24b.equals(test.returnDt)
                           && Integer.valueOf(300).equals(test.finishTime)
                           && Integer.valueOf(9).equals(test.timesDisplay)
                           && date11b.equals(test.createDt)) {

                    found3 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected resourceId ", test.resourceId);
                    Log.warning("Unexpected loanDt ", test.loanDt);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected dueDt ", test.dueDt);
                    Log.warning("Unexpected returnDt ", test.returnDt);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timesDisplay ", test.timesDisplay);
                    Log.warning("Unexpected createDt ", test.createDt);
                }
            }

            assertTrue(found1, "Stresource 1 not found");
            assertTrue(found2, "Stresource 2 not found");
            assertTrue(found3, "Stresource 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stresource rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawStresource> all = RawStresourceLogic.queryByStudent(cache, "111111111");

            assertEquals(2, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawStresource test : all) {
                if ("111111111".equals(test.stuId)
                    && "res1".equals(test.resourceId)
                    && date12.equals(test.loanDt)
                    && Integer.valueOf(300).equals(test.startTime)
                    && date13.equals(test.dueDt)
                    && date24.equals(test.returnDt)
                    && Integer.valueOf(400).equals(test.finishTime)
                    && Integer.valueOf(3).equals(test.timesDisplay)
                    && date11.equals(test.createDt)) {

                    found1 = true;
                } else if ("111111111".equals(test.stuId)
                           && "res2".equals(test.resourceId)
                           && date34.equals(test.loanDt)
                           && Integer.valueOf(500).equals(test.startTime)
                           && date56.equals(test.dueDt)
                           && test.returnDt == null
                           && test.finishTime == null
                           && Integer.valueOf(1).equals(test.timesDisplay)
                           && date33.equals(test.createDt)) {

                    found2 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected resourceId ", test.resourceId);
                    Log.warning("Unexpected loanDt ", test.loanDt);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected dueDt ", test.dueDt);
                    Log.warning("Unexpected returnDt ", test.returnDt);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timesDisplay ", test.timesDisplay);
                    Log.warning("Unexpected createDt ", test.createDt);
                }
            }

            assertTrue(found1, "Stresource 1 not found");
            assertTrue(found2, "Stresource 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stresource by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryOutstanding results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final RawStresource test = RawStresourceLogic.queryOutstanding(cache, "res2");

            assertNotNull(test, "No record from queryOutstanding");

            final boolean found = "111111111".equals(test.stuId)
                                  && "res2".equals(test.resourceId)
                                  && date34.equals(test.loanDt)
                                  && Integer.valueOf(500).equals(test.startTime)
                                  && date56.equals(test.dueDt)
                                  && test.returnDt == null
                                  && test.finishTime == null
                                  && Integer.valueOf(1).equals(test.timesDisplay)
                                  && date33.equals(test.createDt);

            assertTrue(found, "Stresource not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying outstanding row: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final RawStresource outstanding = RawStresourceLogic.queryOutstanding(cache, "res2");

            assertNotNull(outstanding, "No record from queryOutstanding");

            assertTrue(RawStresourceLogic.updateReturnDateTime(cache, outstanding, date1112, Integer.valueOf(987)),
                    "updateReturnDateTime failed");

            final List<RawStresource> all = RawStresourceLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawStresource test : all) {
                if ("111111111".equals(test.stuId)
                    && "res1".equals(test.resourceId)
                    && date12.equals(test.loanDt)
                    && Integer.valueOf(300).equals(test.startTime)
                    && date13.equals(test.dueDt)
                    && date24.equals(test.returnDt)
                    && Integer.valueOf(400).equals(test.finishTime)
                    && Integer.valueOf(3).equals(test.timesDisplay)
                    && date11.equals(test.createDt)) {

                    found1 = true;
                } else if ("111111111".equals(test.stuId)
                           && "res2".equals(test.resourceId)
                           && date34.equals(test.loanDt)
                           && Integer.valueOf(500).equals(test.startTime)
                           && date56.equals(test.dueDt)
                           && date1112.equals(test.returnDt)
                           && Integer.valueOf(987).equals(test.finishTime)
                           && Integer.valueOf(1).equals(test.timesDisplay)
                           && date33.equals(test.createDt)) {

                    found2 = true;
                } else if ("222222222".equals(test.stuId)
                           && "res2".equals(test.resourceId)
                           && date12b.equals(test.loanDt)
                           && Integer.valueOf(200).equals(test.startTime)
                           && date13b.equals(test.dueDt)
                           && date24b.equals(test.returnDt)
                           && Integer.valueOf(300).equals(test.finishTime)
                           && Integer.valueOf(9).equals(test.timesDisplay)
                           && date11b.equals(test.createDt)) {

                    found3 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected resourceId ", test.resourceId);
                    Log.warning("Unexpected loanDt ", test.loanDt);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected dueDt ", test.dueDt);
                    Log.warning("Unexpected returnDt ", test.returnDt);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timesDisplay ", test.timesDisplay);
                    Log.warning("Unexpected createDt ", test.createDt);
                }
            }

            assertTrue(found1, "Stresource 1 not found");
            assertTrue(found2, "Stresource 2 not found");
            assertTrue(found3, "Stresource 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stresource rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            final RawStresource raw2 = new RawStresource("111111111", "res2", date34,
                    Integer.valueOf(500), date56, null, null, Integer.valueOf(1),
                    date33);

            final boolean result = RawStresourceLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawStresource> all = RawStresourceLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final RawStresource test : all) {
                if ("111111111".equals(test.stuId)
                    && "res1".equals(test.resourceId)
                    && date12.equals(test.loanDt)
                    && Integer.valueOf(300).equals(test.startTime)
                    && date13.equals(test.dueDt)
                    && date24.equals(test.returnDt)
                    && Integer.valueOf(400).equals(test.finishTime)
                    && Integer.valueOf(3).equals(test.timesDisplay)
                    && date11.equals(test.createDt)) {

                    found1 = true;
                } else if ("222222222".equals(test.stuId)
                           && "res2".equals(test.resourceId)
                           && date12b.equals(test.loanDt)
                           && Integer.valueOf(200).equals(test.startTime)
                           && date13b.equals(test.dueDt)
                           && date24b.equals(test.returnDt)
                           && Integer.valueOf(300).equals(test.finishTime)
                           && Integer.valueOf(9).equals(test.timesDisplay)
                           && date11b.equals(test.createDt)) {

                    found3 = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected resourceId ", test.resourceId);
                    Log.warning("Unexpected loanDt ", test.loanDt);
                    Log.warning("Unexpected startTime ", test.startTime);
                    Log.warning("Unexpected dueDt ", test.dueDt);
                    Log.warning("Unexpected returnDt ", test.returnDt);
                    Log.warning("Unexpected finishTime ", test.finishTime);
                    Log.warning("Unexpected timesDisplay ", test.timesDisplay);
                    Log.warning("Unexpected createDt ", test.createDt);
                }
            }

            assertTrue(found1, "Stresource 1 not found");
            assertTrue(found3, "Stresource 3 not found");
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
                stmt.executeUpdate("DELETE FROM stresource");
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

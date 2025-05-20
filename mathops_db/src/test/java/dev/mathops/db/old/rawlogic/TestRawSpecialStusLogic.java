package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawSpecialStusLogic} class.
 */
final class TestRawSpecialStusLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 2, 3);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 3, 4);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2021, 1, 1);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2021, 2, 2);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2021, 3, 3);

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
                stmt.executeUpdate("DELETE FROM special_stus");
            }
            conn.commit();

            final RawSpecialStus raw1 = new RawSpecialStus("111111111", "DCE", date4, date1);
            final RawSpecialStus raw2 = new RawSpecialStus("111111111", "TUTOR", date5, date2);
            final RawSpecialStus raw3 = new RawSpecialStus("222222222", "DCE", date6, date3);

            assertTrue(RawSpecialStusLogic.insert(cache, raw1), "Failed to insert special_stus 1");
            assertTrue(RawSpecialStusLogic.insert(cache, raw2), "Failed to insert special_stus 2");
            assertTrue(RawSpecialStusLogic.insert(cache, raw3), "Failed to insert special_stus 3");
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
            final List<RawSpecialStus> all = RawSpecialStusLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawSpecialStus test : all) {
                if ("111111111".equals(test.stuId)
                    && "DCE".equals(test.stuType)
                    && date4.equals(test.startDt)
                    && date1.equals(test.endDt)) {

                    found1 = true;
                } else if ("111111111".equals(test.stuId)
                           && "TUTOR".equals(test.stuType)
                           && date5.equals(test.startDt)
                           && date2.equals(test.endDt)) {

                    found2 = true;
                } else if ("222222222".equals(test.stuId)
                           && "DCE".equals(test.stuType)
                           && date6.equals(test.startDt)
                           && date3.equals(test.endDt)) {

                    found3 = true;
                }
            }

            assertTrue(found1, "special_stus 1 not found");
            assertTrue(found2, "special_stus 2 not found");
            assertTrue(found3, "special_stus 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all special_stus rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSpecialStus> all = RawSpecialStusLogic.queryByStudent(cache, "111111111");

            assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawSpecialStus test : all) {
                if ("111111111".equals(test.stuId)
                    && "DCE".equals(test.stuType)
                    && date4.equals(test.startDt)
                    && date1.equals(test.endDt)) {

                    found1 = true;
                } else if ("111111111".equals(test.stuId)
                           && "TUTOR".equals(test.stuType)
                           && date5.equals(test.startDt)
                           && date2.equals(test.endDt)) {

                    found2 = true;
                }
            }

            assertTrue(found1, "special_stus 1 not found");
            assertTrue(found2, "special_stus 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying special_stus rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryActiveByStudent results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSpecialStus> all = RawSpecialStusLogic.queryActiveByStudent(cache, "111111111", date1);

            assertEquals(1, all.size(), "Incorrect record count from queryActiveByStudent");

            boolean found1 = false;

            for (final RawSpecialStus test : all) {
                if ("111111111".equals(test.stuId)
                    && "DCE".equals(test.stuType)
                    && date4.equals(test.startDt)
                    && date1.equals(test.endDt)) {

                    found1 = true;
                    break;
                }
            }

            assertTrue(found1, "special_stus 1 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying active special_stus rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByType results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSpecialStus> all = RawSpecialStusLogic.queryByType(cache, "DCE");

            assertEquals(2, all.size(), "Incorrect record count from queryByType");

            boolean found1 = false;
            boolean found3 = false;

            for (final RawSpecialStus test : all) {
                if ("111111111".equals(test.stuId)
                    && "DCE".equals(test.stuType)
                    && date4.equals(test.startDt)
                    && date1.equals(test.endDt)) {

                    found1 = true;
                } else if ("222222222".equals(test.stuId)
                           && "DCE".equals(test.stuType)
                           && date6.equals(test.startDt)
                           && date3.equals(test.endDt)) {

                    found3 = true;
                }
            }

            assertTrue(found1, "special_stus 1 not found");
            assertTrue(found3, "special_stus 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying special_stus rows by type: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryActiveByType results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawSpecialStus> all = RawSpecialStusLogic.queryActiveByType(cache, RawSpecialStus.DCE, date3);

            assertEquals(1, all.size(), "Incorrect record count from queryActiveByType");

            boolean found3 = false;

            for (final RawSpecialStus test : all) {
                if ("222222222".equals(test.stuId)
                    && "DCE".equals(test.stuType)
                    && date6.equals(test.startDt)
                    && date3.equals(test.endDt)) {

                    found3 = true;
                    break;
                }
            }

            assertTrue(found3, "special_stus 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying active special_stus rows by type: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("isSpecialType results")
    void test0008() {

        final Cache cache = new Cache(profile);

        try {
            final boolean is1 = RawSpecialStusLogic.isSpecialType(cache, "111111111", date4,
                    RawSpecialStus.DCE);

            assertTrue(is1, "isSpecialType 1 returned invalid result");

            final boolean is2 = RawSpecialStusLogic.isSpecialType(cache, "111111111", date1,
                    "FOO", RawSpecialStus.DCE, "BAR");

            assertTrue(is2, "isSpecialType 2 returned invalid result");

            final boolean is3 = RawSpecialStusLogic.isSpecialType(cache, "111111111", LocalDate.of(2020, 12, 31),
                    RawSpecialStus.DCE);

            assertFalse(is3, "isSpecialType 3 returned invalid result");

            final boolean is4 = RawSpecialStusLogic.isSpecialType(cache, "111111111", LocalDate.of(2021, 1, 3),
                    RawSpecialStus.DCE);

            assertFalse(is4, "isSpecialType 4 returned invalid result");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying active special_stus rows by type: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0009() {

        final Cache cache = new Cache(profile);

        try {
            final RawSpecialStus raw2 = new RawSpecialStus("111111111", "TUTOR", date5, date2);

            final boolean result = RawSpecialStusLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawSpecialStus> all = RawSpecialStusLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final RawSpecialStus test : all) {

                if ("111111111".equals(test.stuId)
                    && "DCE".equals(test.stuType)
                    && date4.equals(test.startDt)
                    && date1.equals(test.endDt)) {

                    found1 = true;
                } else if ("222222222".equals(test.stuId)
                           && "DCE".equals(test.stuType)
                           && date6.equals(test.startDt)
                           && date3.equals(test.endDt)) {

                    found3 = true;
                }
            }

            assertTrue(found1, "special_stus 1 not found");
            assertTrue(found3, "special_stus 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting special_stus: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM special_stus");
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

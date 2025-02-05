package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawMpecrDenied;
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
 * Tests for the {@code RawMpecrDeniedLogic} class.
 */
final class TestRawMpecrDeniedLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 2, 3);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 4, 5);

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
                stmt.executeUpdate("DELETE FROM mpecr_denied");
            }
            conn.commit();

            final RawMpecrDenied raw1 = new RawMpecrDenied("123456789", RawRecordConstants.M125, "Y",
                    date1, "WH", Long.valueOf(12345L), "MPTUN", "RM");

            final RawMpecrDenied raw2 = new RawMpecrDenied("123456789", RawRecordConstants.M124, "Y",
                    date1, "WH", Long.valueOf(12345L), "MPTUN", "RM");

            final RawMpecrDenied raw3 = new RawMpecrDenied("123456789", RawRecordConstants.M126, "N",
                    date2, "YD", Long.valueOf(12346L), "MPTTC", "TC");

            final RawMpecrDenied raw4 = new RawMpecrDenied("888888888", RawRecordConstants.M117, "Y",
                    date3, "EN", Long.valueOf(12347L), "MPTPU", null);

            assertTrue(RawMpecrDeniedLogic.insert(cache, raw1), "Failed to insert mpecr_denied");
            assertTrue(RawMpecrDeniedLogic.insert(cache, raw2), "Failed to insert mpecr_denied");
            assertTrue(RawMpecrDeniedLogic.insert(cache, raw3), "Failed to insert mpecr_denied");
            assertTrue(RawMpecrDeniedLogic.insert(cache, raw4), "Failed to insert mpecr_denied");
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
            final List<RawMpecrDenied> all = RawMpecrDeniedLogic.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final RawMpecrDenied r : all) {

                if ("123456789".equals(r.stuId)
                    && RawRecordConstants.M125.equals(r.course)
                    && "Y".equals(r.examPlaced)
                    && date1.equals(r.examDt)
                    && "WH".equals(r.whyDenied)
                    && Long.valueOf(12345L).equals(r.serialNbr)
                    && "MPTUN".equals(r.version)
                    && "RM".equals(r.examSource)) {

                    found1 = true;

                } else if ("123456789".equals(r.stuId)
                           && RawRecordConstants.M124.equals(r.course)
                           && "Y".equals(r.examPlaced)
                           && date1.equals(r.examDt)
                           && "WH".equals(r.whyDenied)
                           && Long.valueOf(12345L).equals(r.serialNbr)
                           && "MPTUN".equals(r.version)
                           && "RM".equals(r.examSource)) {

                    found2 = true;

                } else if ("123456789".equals(r.stuId)
                           && RawRecordConstants.M126.equals(r.course)
                           && "N".equals(r.examPlaced)
                           && date2.equals(r.examDt)
                           && "YD".equals(r.whyDenied)
                           && Long.valueOf(12346L).equals(r.serialNbr)
                           && "MPTTC".equals(r.version)
                           && "TC".equals(r.examSource)) {

                    found3 = true;

                } else if ("888888888".equals(r.stuId)
                           && RawRecordConstants.M117.equals(r.course)
                           && "Y".equals(r.examPlaced)
                           && date3.equals(r.examDt)
                           && "EN".equals(r.whyDenied)
                           && Long.valueOf(12347L).equals(r.serialNbr)
                           && "MPTPU".equals(r.version)
                           && r.examSource == null) {

                    found4 = true;

                } else {
                    Log.warning("Unexpected stuId ", r.stuId);
                    Log.warning("Unexpected course ", r.course);
                    Log.warning("Unexpected examPlaced ", r.examPlaced);
                    Log.warning("Unexpected examDt ", r.examDt);
                    Log.warning("Unexpected whyDenied ", r.whyDenied);
                    Log.warning("Unexpected serialNbr ", r.serialNbr);
                    Log.warning("Unexpected version ", r.version);
                    Log.warning("Unexpected examSource ", r.examSource);
                }
            }

            assertTrue(found1, "mpecr_denied 1 not found");
            assertTrue(found2, "mpecr_denied 2 not found");
            assertTrue(found3, "mpecr_denied 3 not found");
            assertTrue(found4, "mpecr_denied 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all mpecr_denied rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawMpecrDenied> all = RawMpecrDeniedLogic.queryByStudent(cache, "123456789");

            assertEquals(3, all.size(), "Incorrect record count from queryByStudent");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawMpecrDenied r : all) {

                if ("123456789".equals(r.stuId)
                    && RawRecordConstants.M125.equals(r.course)
                    && "Y".equals(r.examPlaced)
                    && date1.equals(r.examDt)
                    && "WH".equals(r.whyDenied)
                    && Long.valueOf(12345L).equals(r.serialNbr)
                    && "MPTUN".equals(r.version)
                    && "RM".equals(r.examSource)) {

                    found1 = true;

                } else if ("123456789".equals(r.stuId)
                           && RawRecordConstants.M124.equals(r.course)
                           && "Y".equals(r.examPlaced)
                           && date1.equals(r.examDt)
                           && "WH".equals(r.whyDenied)
                           && Long.valueOf(12345L).equals(r.serialNbr)
                           && "MPTUN".equals(r.version)
                           && "RM".equals(r.examSource)) {

                    found2 = true;

                } else if ("123456789".equals(r.stuId)
                           && RawRecordConstants.M126.equals(r.course)
                           && "N".equals(r.examPlaced)
                           && date2.equals(r.examDt)
                           && "YD".equals(r.whyDenied)
                           && Long.valueOf(12346L).equals(r.serialNbr)
                           && "MPTTC".equals(r.version)
                           && "TC".equals(r.examSource)) {

                    found3 = true;

                } else {
                    Log.warning("Unexpected stuId ", r.stuId);
                    Log.warning("Unexpected course ", r.course);
                    Log.warning("Unexpected examPlaced ", r.examPlaced);
                    Log.warning("Unexpected examDt ", r.examDt);
                    Log.warning("Unexpected whyDenied ", r.whyDenied);
                    Log.warning("Unexpected serialNbr ", r.serialNbr);
                    Log.warning("Unexpected version ", r.version);
                    Log.warning("Unexpected examSource ", r.examSource);
                }
            }

            assertTrue(found1, "mpecr_denied 1 not found");
            assertTrue(found2, "mpecr_denied 2 not found");
            assertTrue(found3, "mpecr_denied 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all mpecr_denied rows for student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByExam results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawMpecrDenied> all = RawMpecrDeniedLogic.queryByExam(cache, Long.valueOf(12345L));

            assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawMpecrDenied r : all) {

                if ("123456789".equals(r.stuId)
                    && RawRecordConstants.M125.equals(r.course)
                    && "Y".equals(r.examPlaced)
                    && date1.equals(r.examDt)
                    && "WH".equals(r.whyDenied)
                    && Long.valueOf(12345L).equals(r.serialNbr)
                    && "MPTUN".equals(r.version)
                    && "RM".equals(r.examSource)) {

                    found1 = true;

                } else if ("123456789".equals(r.stuId)
                           && RawRecordConstants.M124.equals(r.course)
                           && "Y".equals(r.examPlaced)
                           && date1.equals(r.examDt)
                           && "WH".equals(r.whyDenied)
                           && Long.valueOf(12345L).equals(r.serialNbr)
                           && "MPTUN".equals(r.version)
                           && "RM".equals(r.examSource)) {

                    found2 = true;

                } else {
                    Log.warning("Unexpected stuId ", r.stuId);
                    Log.warning("Unexpected course ", r.course);
                    Log.warning("Unexpected examPlaced ", r.examPlaced);
                    Log.warning("Unexpected examDt ", r.examDt);
                    Log.warning("Unexpected whyDenied ", r.whyDenied);
                    Log.warning("Unexpected serialNbr ", r.serialNbr);
                    Log.warning("Unexpected version ", r.version);
                    Log.warning("Unexpected examSource ", r.examSource);
                }
            }

            assertTrue(found1, "mpecr_denied 1 not found");
            assertTrue(found2, "mpecr_denied 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all mpecr_denied rows for exam: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final RawMpecrDenied raw2 = new RawMpecrDenied("123456789", RawRecordConstants.M124, "Y",
                    date1, "WH", Long.valueOf(12345L), "MPTUN", "RM");

            final boolean result = RawMpecrDeniedLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawMpecrDenied> all = RawMpecrDeniedLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final RawMpecrDenied r : all) {

                if ("123456789".equals(r.stuId)
                    && RawRecordConstants.M125.equals(r.course)
                    && "Y".equals(r.examPlaced)
                    && date1.equals(r.examDt)
                    && "WH".equals(r.whyDenied)
                    && Long.valueOf(12345L).equals(r.serialNbr)
                    && "MPTUN".equals(r.version)
                    && "RM".equals(r.examSource)) {

                    found1 = true;

                } else if ("123456789".equals(r.stuId)
                           && RawRecordConstants.M126.equals(r.course)
                           && "N".equals(r.examPlaced)
                           && date2.equals(r.examDt)
                           && "YD".equals(r.whyDenied)
                           && Long.valueOf(12346L).equals(r.serialNbr)
                           && "MPTTC".equals(r.version)
                           && "TC".equals(r.examSource)) {

                    found3 = true;

                } else if ("888888888".equals(r.stuId)
                           && RawRecordConstants.M117.equals(r.course)
                           && "Y".equals(r.examPlaced)
                           && date3.equals(r.examDt)
                           && "EN".equals(r.whyDenied)
                           && Long.valueOf(12347L).equals(r.serialNbr)
                           && "MPTPU".equals(r.version)
                           && r.examSource == null) {

                    found4 = true;

                } else {
                    Log.warning("Unexpected stuId ", r.stuId);
                    Log.warning("Unexpected course ", r.course);
                    Log.warning("Unexpected examPlaced ", r.examPlaced);
                    Log.warning("Unexpected examDt ", r.examDt);
                    Log.warning("Unexpected whyDenied ", r.whyDenied);
                    Log.warning("Unexpected serialNbr ", r.serialNbr);
                    Log.warning("Unexpected version ", r.version);
                    Log.warning("Unexpected examSource ", r.examSource);
                }
            }

            assertTrue(found1, "mpecr_denied 1 not found");
            assertTrue(found3, "mpecr_denied 3 not found");
            assertTrue(found4, "mpecr_denied 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting mpecr_denied: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM mpecr_denied");
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

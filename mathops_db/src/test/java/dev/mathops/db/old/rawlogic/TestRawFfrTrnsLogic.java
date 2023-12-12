package dev.mathops.db.old.rawlogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawRecordConstants;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

/**
 * Tests for the {@code RawFfrTrnsLogic} class.
 */
final class TestRawFfrTrnsLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 3, 4);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 5, 6);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2021, 7, 8);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2021, 9, 10);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2021, 11, 12);

    /** The database profile. */
    private static DbProfile dbProfile = null;

    /** The database context. */
    private static DbContext ctx = null;

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        dbProfile = ContextMap.getDefaultInstance().getCodeProfile(Contexts.INFORMIX_TEST_PATH);
        if (dbProfile == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_TEST_PROFILE));
        }

        ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);
        if (ctx == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PRIMARY_CONTEXT));
        }

        // Make sure we're in the TEST database
        try {
            final DbConnection conn = ctx.checkOutConnection();

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
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            throw new IllegalArgumentException(ex);
        }

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM ffr_trns");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawFfrTrns raw1 = new RawFfrTrns("111111111", RawRecordConstants.M117, "A", date1, date2);
                final RawFfrTrns raw2 = new RawFfrTrns("111111111", RawRecordConstants.M118, "B", date3, date4);
                final RawFfrTrns raw3 = new RawFfrTrns("222222222", RawRecordConstants.M124, "C", date5, date6);

                assertTrue(RawFfrTrnsLogic.INSTANCE.insert(cache, raw1), "Failed to insert ffr_trns 1");
                assertTrue(RawFfrTrnsLogic.INSTANCE.insert(cache, raw2), "Failed to insert ffr_trns 2");
                assertTrue(RawFfrTrnsLogic.INSTANCE.insert(cache, raw3), "Failed to insert ffr_trns 3");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing tables: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0003() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawFfrTrns> all = RawFfrTrnsLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawFfrTrns test : all) {
                    if ("111111111".equals(test.stuId)
                            && RawRecordConstants.M117.equals(test.course)
                            && "A".equals(test.examPlaced)
                            && date1.equals(test.examDt)
                            && date2.equals(test.dtCrRefused)) {

                        found1 = true;
                    } else if ("111111111".equals(test.stuId)
                            && RawRecordConstants.M118.equals(test.course)
                            && "B".equals(test.examPlaced)
                            && date3.equals(test.examDt)
                            && date4.equals(test.dtCrRefused)) {

                        found2 = true;
                    } else if ("222222222".equals(test.stuId)
                            && RawRecordConstants.M124.equals(test.course)
                            && "C".equals(test.examPlaced)
                            && date5.equals(test.examDt)
                            && date6.equals(test.dtCrRefused)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected examPlaced ", test.examPlaced);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected dtCrRefused ", test.dtCrRefused);
                    }
                }

                assertTrue(found1, "ffr_trns 1 not found");
                assertTrue(found2, "ffr_trns 2 not found");
                assertTrue(found3, "ffr_trns 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all ffr_trns rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAllByTerm results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawFfrTrns> all = RawFfrTrnsLogic.queryByStudent(cache, "111111111");

                assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

                boolean found1 = false;
                boolean found2 = false;

                for (final RawFfrTrns test : all) {
                    if ("111111111".equals(test.stuId)
                            && RawRecordConstants.M117.equals(test.course)
                            && "A".equals(test.examPlaced)
                            && date1.equals(test.examDt)
                            && date2.equals(test.dtCrRefused)) {

                        found1 = true;
                    } else if ("111111111".equals(test.stuId)
                            && RawRecordConstants.M118.equals(test.course)
                            && "B".equals(test.examPlaced)
                            && date3.equals(test.examDt)
                            && date4.equals(test.dtCrRefused)) {

                        found2 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected examPlaced ", test.examPlaced);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected dtCrRefused ", test.dtCrRefused);
                    }
                }

                assertTrue(found1, "ffr_trns 1 not found");
                assertTrue(found2, "ffr_trns 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all ffr_trns rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawFfrTrns raw2 = new RawFfrTrns("111111111", RawRecordConstants.M118, "B",
                        date3, date4);

                final boolean result = RawFfrTrnsLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawFfrTrns> all = RawFfrTrnsLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawFfrTrns test : all) {
                    if ("111111111".equals(test.stuId)
                            && RawRecordConstants.M117.equals(test.course)
                            && "A".equals(test.examPlaced)
                            && date1.equals(test.examDt)
                            && date2.equals(test.dtCrRefused)) {

                        found1 = true;
                    } else if ("222222222".equals(test.stuId)
                            && RawRecordConstants.M124.equals(test.course)
                            && "C".equals(test.examPlaced)
                            && date5.equals(test.examDt)
                            && date6.equals(test.dtCrRefused)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected examPlaced ", test.examPlaced);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected dtCrRefused ", test.dtCrRefused);
                    }
                }

                assertTrue(found1, "ffr_trns 1 not found");
                assertTrue(found3, "ffr_trns 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting ffr_trns: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM ffr_trns");
                }

                conn.commit();

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning table: " + ex.getMessage());
        }
    }
}

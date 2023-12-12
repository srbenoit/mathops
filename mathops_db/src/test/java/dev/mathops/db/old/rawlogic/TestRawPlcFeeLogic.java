package dev.mathops.db.old.rawlogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawPlcFee;
import dev.mathops.db.old.rawrecord.RawRecordConstants;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

/**
 * Tests for the {@code RawPlcFeeLogic} class.
 */
final class TestRawPlcFeeLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 1, 3);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 2, 2);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2021, 2, 3);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2021, 3, 2);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2021, 3, 3);

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
                    stmt.executeUpdate("DELETE FROM plc_fee");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawPlcFee raw1 = new RawPlcFee("111111111", RawRecordConstants.M100P, date1, date2);
                final RawPlcFee raw2 = new RawPlcFee("222222222", RawRecordConstants.M1170, date3, date4);
                final RawPlcFee raw3 = new RawPlcFee("333333333", RawRecordConstants.M100P, date5, date6);

                assertTrue(RawPlcFeeLogic.INSTANCE.insert(cache, raw1), "Failed to insert plc_fee 1");
                assertTrue(RawPlcFeeLogic.INSTANCE.insert(cache, raw2), "Failed to insert plc_fee 2");
                assertTrue(RawPlcFeeLogic.INSTANCE.insert(cache, raw3), "Failed to insert plc_fee 3");
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
                final List<RawPlcFee> all = RawPlcFeeLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawPlcFee test : all) {
                    if ("111111111".equals(test.stuId)
                            && RawRecordConstants.M100P.equals(test.course)
                            && date1.equals(test.examDt)
                            && date2.equals(test.billDt)) {

                        found1 = true;
                    } else if ("222222222".equals(test.stuId)
                            && RawRecordConstants.M1170.equals(test.course)
                            && date3.equals(test.examDt)
                            && date4.equals(test.billDt)) {

                        found2 = true;
                    } else if ("333333333".equals(test.stuId)
                            && RawRecordConstants.M100P.equals(test.course)
                            && date5.equals(test.examDt)
                            && date6.equals(test.billDt)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected billDt ", test.billDt);
                    }
                }

                assertTrue(found1, "plc_fee 1 not found");
                assertTrue(found2, "plc_fee 2 not found");
                assertTrue(found3, "plc_fee 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all plc_fee rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawPlcFee test = RawPlcFeeLogic.queryByStudent(cache, "111111111");

                assertNotNull(test, "No record from queryByStudent");

                final boolean found = "111111111".equals(test.stuId)
                        && RawRecordConstants.M100P.equals(test.course)
                        && date1.equals(test.examDt)
                        && date2.equals(test.billDt);

                assertTrue(found, "plc_fee 1 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all plc_fee rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryMostRecentBillDate results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final LocalDate mostRecent = RawPlcFeeLogic.queryMostRecentBillDate(cache);

                assertNotNull(mostRecent, "queryMostRecentBillDate returned null");

                assertEquals(date6, mostRecent, "queryMostRecentBillDate returned invalid date");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all most recent bill date: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0007() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawPlcFee raw2 = new RawPlcFee("222222222", RawRecordConstants.M1170, date3, date4);

                final boolean result = RawPlcFeeLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawPlcFee> all = RawPlcFeeLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawPlcFee test : all) {
                    if ("111111111".equals(test.stuId)
                            && RawRecordConstants.M100P.equals(test.course)
                            && date1.equals(test.examDt)
                            && date2.equals(test.billDt)) {

                        found1 = true;
                    } else if ("333333333".equals(test.stuId)
                            && RawRecordConstants.M100P.equals(test.course)
                            && date5.equals(test.examDt)
                            && date6.equals(test.billDt)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected billDt ", test.billDt);
                    }
                }

                assertTrue(found1, "plc_fee 1 not found");
                assertTrue(found3, "plc_fee 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting plc_fee: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM plc_fee");
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

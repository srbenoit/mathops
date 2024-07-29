package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
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
 * Tests for the {@code RawMpeCreditLogic} class.
 */
final class TestRawMpeCreditLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 1, 3);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 2, 3);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2021, 4, 5);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2021, 4, 6);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2021, 4, 7);

    /** A date used in test records. */
    private static final LocalDate date7 = LocalDate.of(2021, 6, 8);

    /** A date used in test records. */
    private static final LocalDate date8 = LocalDate.of(2021, 7, 9);

    /** A date used in test records. */
    private static final LocalDate date9 = LocalDate.of(2021, 6, 10);

    /** A date used in test records. */
    private static final LocalDate date10 = LocalDate.of(2021, 6, 11);

    /** A date used in test records. */
    private static final LocalDate date11 = LocalDate.of(2021, 8, 1);

    /** A date used in test records. */
    private static final LocalDate date12 = LocalDate.of(2021, 8, 2);

    /** A date used in test records. */
    private static final LocalDate date13 = LocalDate.of(2021, 8, 3);

    /** A date used in test records. */
    private static final LocalDate date14 = LocalDate.of(2021, 8, 4);

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
                    stmt.executeUpdate("DELETE FROM mpe_credit");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawMpeCredit raw1 = new RawMpeCredit("123456789", RawRecordConstants.M125, "P",
                        date1, null, Long.valueOf(12345L), "MPTUN", "RM");

                final RawMpeCredit raw2 = new RawMpeCredit("123456789", RawRecordConstants.M124, "C",
                        date1, date2, Long.valueOf(12345L), "MPTUN", "RM");

                final RawMpeCredit raw3 = new RawMpeCredit("123456789", RawRecordConstants.M126, "P",
                        date3, null, Long.valueOf(12346L), "MPTTC", "TC");

                final RawMpeCredit raw4 = new RawMpeCredit("888880001", RawRecordConstants.M100C, "P",
                        date4, null, Long.valueOf(12347L), "MPTPU", null);

                final RawMpeCredit raw5 = new RawMpeCredit("888880002", "M 100M", "P", date5, null,
                        Long.valueOf(12348L), "FEEFI", null);

                final RawMpeCredit raw6 = new RawMpeCredit("888880003", RawRecordConstants.M100T, "P",
                        date6, null, Long.valueOf(12349L), "FOFUM", null);

                assertTrue(RawMpeCreditLogic.INSTANCE.insert(cache, raw1), "Failed to insert mpe_credit");
                assertTrue(RawMpeCreditLogic.INSTANCE.insert(cache, raw2), "Failed to insert mpe_credit");
                assertTrue(RawMpeCreditLogic.INSTANCE.insert(cache, raw3), "Failed to insert mpe_credit");
                assertTrue(RawMpeCreditLogic.INSTANCE.insert(cache, raw4), "Failed to insert mpe_credit");
                assertTrue(RawMpeCreditLogic.INSTANCE.insert(cache, raw5), "Failed to insert mpe_credit");
                assertTrue(RawMpeCreditLogic.INSTANCE.insert(cache, raw6), "Failed to insert mpe_credit");
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
                final List<RawMpeCredit> all = RawMpeCreditLogic.INSTANCE.queryAll(cache);

                assertEquals(6, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;

                for (final RawMpeCredit r : all) {

                    if ("123456789".equals(r.stuId)
                            && RawRecordConstants.M125.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date1.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12345L).equals(r.serialNbr)
                            && "MPTUN".equals(r.version)
                            && "RM".equals(r.examSource)) {

                        found1 = true;

                    } else if ("123456789".equals(r.stuId)
                            && RawRecordConstants.M124.equals(r.course)
                            && "C".equals(r.examPlaced)
                            && date1.equals(r.examDt)
                            && date2.equals(r.dtCrRefused)
                            && Long.valueOf(12345L).equals(r.serialNbr)
                            && "MPTUN".equals(r.version)
                            && "RM".equals(r.examSource)) {

                        found2 = true;

                    } else if ("123456789".equals(r.stuId)
                            && RawRecordConstants.M126.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date3.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12346L).equals(r.serialNbr)
                            && "MPTTC".equals(r.version)
                            && "TC".equals(r.examSource)) {

                        found3 = true;

                    } else if ("888880001".equals(r.stuId)
                            && RawRecordConstants.M100C.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date4.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12347L).equals(r.serialNbr)
                            && "MPTPU".equals(r.version)
                            && r.examSource == null) {

                        found4 = true;

                    } else if ("888880002".equals(r.stuId)
                            && "M 100M".equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date5.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12348L).equals(r.serialNbr)
                            && "FEEFI".equals(r.version)
                            && r.examSource == null) {

                        found5 = true;

                    } else if ("888880003".equals(r.stuId)
                            && RawRecordConstants.M100T.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date6.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12349L).equals(r.serialNbr)
                            && "FOFUM".equals(r.version)
                            && r.examSource == null) {

                        found6 = true;

                    } else {
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected examPlaced ", r.examPlaced);
                        Log.warning("Unexpected examDt ", r.examDt);
                        Log.warning("Unexpected dtCrRefused ", r.dtCrRefused);
                        Log.warning("Unexpected serialNbr ", r.serialNbr);
                        Log.warning("Unexpected version ", r.version);
                        Log.warning("Unexpected examSource ", r.examSource);
                    }
                }

                assertTrue(found1, "mpe_credit 1 not found");
                assertTrue(found2, "mpe_credit 2 not found");
                assertTrue(found3, "mpe_credit 3 not found");
                assertTrue(found4, "mpe_credit 4 not found");
                assertTrue(found5, "mpe_credit 5 not found");
                assertTrue(found6, "mpe_credit 6 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all mpe_credit rows: " + ex.getMessage());
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
                final List<RawMpeCredit> all = RawMpeCreditLogic.queryByStudent(cache, "123456789");

                assertEquals(3, all.size(), "Incorrect record count from queryByStudent");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawMpeCredit r : all) {

                    if ("123456789".equals(r.stuId)
                            && RawRecordConstants.M125.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date1.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12345L).equals(r.serialNbr)
                            && "MPTUN".equals(r.version)
                            && "RM".equals(r.examSource)) {

                        found1 = true;

                    } else if ("123456789".equals(r.stuId)
                            && RawRecordConstants.M124.equals(r.course)
                            && "C".equals(r.examPlaced)
                            && date1.equals(r.examDt)
                            && date2.equals(r.dtCrRefused)
                            && Long.valueOf(12345L).equals(r.serialNbr)
                            && "MPTUN".equals(r.version)
                            && "RM".equals(r.examSource)) {

                        found2 = true;

                    } else if ("123456789".equals(r.stuId)
                            && RawRecordConstants.M126.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date3.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12346L).equals(r.serialNbr)
                            && "MPTTC".equals(r.version)
                            && "TC".equals(r.examSource)) {

                        found3 = true;

                    } else {
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected examPlaced ", r.examPlaced);
                        Log.warning("Unexpected examDt ", r.examDt);
                        Log.warning("Unexpected dtCrRefused ", r.dtCrRefused);
                        Log.warning("Unexpected serialNbr ", r.serialNbr);
                        Log.warning("Unexpected version ", r.version);
                        Log.warning("Unexpected examSource ", r.examSource);
                    }
                }

                assertTrue(found1, "mpe_credit 1 not found");
                assertTrue(found2, "mpe_credit 2 not found");
                assertTrue(found3, "mpe_credit 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all mpe_credit rows for student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByCourse results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawMpeCredit> all = RawMpeCreditLogic.queryByCourse(cache, RawRecordConstants.M125);

                assertEquals(1, all.size(), "Incorrect record count from queryByCourse");

                boolean found = false;

                for (final RawMpeCredit r : all) {

                    if ("123456789".equals(r.stuId)
                            && RawRecordConstants.M125.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date1.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12345L).equals(r.serialNbr)
                            && "MPTUN".equals(r.version)
                            && "RM".equals(r.examSource)) {

                        found = true;

                    } else {
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected examPlaced ", r.examPlaced);
                        Log.warning("Unexpected examDt ", r.examDt);
                        Log.warning("Unexpected dtCrRefused ", r.dtCrRefused);
                        Log.warning("Unexpected serialNbr ", r.serialNbr);
                        Log.warning("Unexpected version ", r.version);
                        Log.warning("Unexpected examSource ", r.examSource);
                    }
                }

                assertTrue(found, "mpe_credit 1 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all mpe_credit rows for course: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0006() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawMpeCredit raw2 = new RawMpeCredit("123456789", RawRecordConstants.M124, "C",
                        date1, date2, Long.valueOf(12345L), "MPTUN", "RM");

                final boolean result = RawMpeCreditLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawMpeCredit> all = RawMpeCreditLogic.INSTANCE.queryAll(cache);

                assertEquals(5, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;

                for (final RawMpeCredit r : all) {

                    if ("123456789".equals(r.stuId)
                            && RawRecordConstants.M125.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date1.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12345L).equals(r.serialNbr)
                            && "MPTUN".equals(r.version)
                            && "RM".equals(r.examSource)) {

                        found1 = true;

                    } else if ("123456789".equals(r.stuId)
                            && RawRecordConstants.M126.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date3.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12346L).equals(r.serialNbr)
                            && "MPTTC".equals(r.version)
                            && "TC".equals(r.examSource)) {

                        found3 = true;

                    } else if ("888880001".equals(r.stuId)
                            && RawRecordConstants.M100C.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date4.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12347L).equals(r.serialNbr)
                            && "MPTPU".equals(r.version)
                            && r.examSource == null) {

                        found4 = true;

                    } else if ("888880002".equals(r.stuId)
                            && "M 100M".equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date5.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12348L).equals(r.serialNbr)
                            && "FEEFI".equals(r.version)
                            && r.examSource == null) {

                        found5 = true;

                    } else if ("888880003".equals(r.stuId)
                            && RawRecordConstants.M100T.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date6.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12349L).equals(r.serialNbr)
                            && "FOFUM".equals(r.version)
                            && r.examSource == null) {

                        found6 = true;

                    } else {
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected examPlaced ", r.examPlaced);
                        Log.warning("Unexpected examDt ", r.examDt);
                        Log.warning("Unexpected dtCrRefused ", r.dtCrRefused);
                        Log.warning("Unexpected serialNbr ", r.serialNbr);
                        Log.warning("Unexpected version ", r.version);
                        Log.warning("Unexpected examSource ", r.examSource);
                    }
                }

                assertTrue(found1, "mpe_credit 1 not found");
                assertTrue(found3, "mpe_credit 3 not found");
                assertTrue(found4, "mpe_credit 4 not found");
                assertTrue(found5, "mpe_credit 5 not found");
                assertTrue(found6, "mpe_credit 6 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting users: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("apply(M 100T) when 'M 100C' exists")
    void test0007() {

        // "apply" with M 100T record, when an M 100C record already exists (should do nothing)

        final RawMpeCredit rec = new RawMpeCredit("888880001", RawRecordConstants.M100T, "P", date7,
                null, Long.valueOf(77701L), "FOBAR", "SR");

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                RawMpeCreditLogic.INSTANCE.apply(cache, rec);

                final List<RawMpeCredit> all = RawMpeCreditLogic.queryByStudent(cache, "888880001");

                assertEquals(1, all.size(), "Incorrect record count from queryByStudent");

                boolean found = false;

                for (final RawMpeCredit r : all) {

                    if ("888880001".equals(r.stuId)
                            && RawRecordConstants.M100C.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date4.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12347L).equals(r.serialNbr)
                            && "MPTPU".equals(r.version)
                            && r.examSource == null) {

                        found = true;

                    } else {
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected examPlaced ", r.examPlaced);
                        Log.warning("Unexpected examDt ", r.examDt);
                        Log.warning("Unexpected dtCrRefused ", r.dtCrRefused);
                        Log.warning("Unexpected serialNbr ", r.serialNbr);
                        Log.warning("Unexpected version ", r.version);
                        Log.warning("Unexpected examSource ", r.examSource);
                    }
                }

                assertTrue(found, "'M 100C' record was not left in place");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while applying 'M 100T' credit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("apply(M 100T) when 'M 100M' exists")
    void test0008() {

        // "apply" with M 100T record, when an M 100M record already exists (should do nothing)

        final RawMpeCredit rec = new RawMpeCredit("888880002", RawRecordConstants.M100T, "P", date7,
                null, Long.valueOf(77701L), "FOBAR", "SR");

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                RawMpeCreditLogic.INSTANCE.apply(cache, rec);

                final List<RawMpeCredit> all = RawMpeCreditLogic.queryByStudent(cache, "888880002");

                assertEquals(1, all.size(), "Incorrect record count from queryByStudent");

                boolean found = false;

                for (final RawMpeCredit r : all) {

                    if ("888880002".equals(r.stuId)
                            && "M 100M".equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date5.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12348L).equals(r.serialNbr)
                            && "FEEFI".equals(r.version)
                            && r.examSource == null) {

                        found = true;

                    } else {
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected examPlaced ", r.examPlaced);
                        Log.warning("Unexpected examDt ", r.examDt);
                        Log.warning("Unexpected dtCrRefused ", r.dtCrRefused);
                        Log.warning("Unexpected serialNbr ", r.serialNbr);
                        Log.warning("Unexpected version ", r.version);
                        Log.warning("Unexpected examSource ", r.examSource);
                    }
                }

                assertTrue(found, "'M 100M' record was not left in place");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while applying 'M 100T' credit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("apply(M 100T) when 'M 100T' exists")
    void test0009() {

        // "apply" with M 100T record, when an M 100T record already exists (should update)

        final RawMpeCredit rec = new RawMpeCredit("888880003", RawRecordConstants.M100T, "P", date7,
                null, Long.valueOf(77701L), "FOBAR", "SR");

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                RawMpeCreditLogic.INSTANCE.apply(cache, rec);

                final List<RawMpeCredit> all = RawMpeCreditLogic.queryByStudent(cache, "888880003");

                assertEquals(1, all.size(), //
                        "Incorrect record count from queryByStudent");

                boolean found = false;

                for (final RawMpeCredit r : all) {

                    if ("888880003".equals(r.stuId)
                            && RawRecordConstants.M100T.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date7.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(77701L).equals(r.serialNbr)
                            && "FOBAR".equals(r.version)
                            && "SR".equals(r.examSource)) {

                        found = true;

                    } else {
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected examPlaced ", r.examPlaced);
                        Log.warning("Unexpected examDt ", r.examDt);
                        Log.warning("Unexpected dtCrRefused ", r.dtCrRefused);
                        Log.warning("Unexpected serialNbr ", r.serialNbr);
                        Log.warning("Unexpected version ", r.version);
                        Log.warning("Unexpected examSource ", r.examSource);
                    }
                }

                assertTrue(found, "'M 100T' record was not updated");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while applying 'M 100T' credit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("apply(M 100T)")
    void test0010() {

        // "apply" with M 100T record, when an M 100T record already exists (should update)

        final RawMpeCredit rec = new RawMpeCredit("888880004", RawRecordConstants.M100T, "P", date7,
                null, Long.valueOf(77701L), "FOBAR", "SR");

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                RawMpeCreditLogic.INSTANCE.apply(cache, rec);

                final List<RawMpeCredit> all = RawMpeCreditLogic.queryByStudent(cache, "888880004");

                assertEquals(1, all.size(), //
                        "Incorrect record count from queryByStudent");

                boolean found = false;

                for (final RawMpeCredit r : all) {

                    if ("888880004".equals(r.stuId)
                            && RawRecordConstants.M100T.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date7.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(77701L).equals(r.serialNbr)
                            && "FOBAR".equals(r.version)
                            && "SR".equals(r.examSource)) {

                        found = true;

                    } else {
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected examPlaced ", r.examPlaced);
                        Log.warning("Unexpected examDt ", r.examDt);
                        Log.warning("Unexpected dtCrRefused ", r.dtCrRefused);
                        Log.warning("Unexpected serialNbr ", r.serialNbr);
                        Log.warning("Unexpected version ", r.version);
                        Log.warning("Unexpected examSource ", r.examSource);
                    }
                }

                assertTrue(found, "'M 100T' record was not inserted");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while applying 'M 100T' credit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("apply(M 100M) when 'M 100C' exists")
    void test0011() {

        // "apply" with M 100M record, when an M 100C record already exists (should do nothing)

        final RawMpeCredit rec = new RawMpeCredit("888880001", "M 100M", "P", date7, null,
                Long.valueOf(77701L), "FOBAR", "SR");

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                RawMpeCreditLogic.INSTANCE.apply(cache, rec);

                final List<RawMpeCredit> all = RawMpeCreditLogic.queryByStudent(cache, "888880001");

                assertEquals(1, all.size(), //
                        "Incorrect record count from queryByStudent");

                boolean found = false;

                for (final RawMpeCredit r : all) {

                    if ("888880001".equals(r.stuId)
                            && RawRecordConstants.M100C.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date4.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(12347L).equals(r.serialNbr)
                            && "MPTPU".equals(r.version)
                            && r.examSource == null) {

                        found = true;

                    } else {
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected examPlaced ", r.examPlaced);
                        Log.warning("Unexpected examDt ", r.examDt);
                        Log.warning("Unexpected dtCrRefused ", r.dtCrRefused);
                        Log.warning("Unexpected serialNbr ", r.serialNbr);
                        Log.warning("Unexpected version ", r.version);
                        Log.warning("Unexpected examSource ", r.examSource);
                    }
                }

                assertTrue(found, "'M 100C' record was not left in place");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while applying 'M 100M' credit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("apply(M 100M) when 'M 100M' exists")
    void test0012() {

        // "apply" with M 100M record, when an M 100M record already exists (should update)

        final RawMpeCredit rec = new RawMpeCredit("888880002", "M 100M", "P", date8, null,
                Long.valueOf(77702L), "FOBAZ", "ST");

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                RawMpeCreditLogic.INSTANCE.apply(cache, rec);

                final List<RawMpeCredit> all = RawMpeCreditLogic.queryByStudent(cache, "888880002");

                assertEquals(1, all.size(), "Incorrect record count from queryByStudent");

                boolean found = false;

                for (final RawMpeCredit r : all) {

                    if ("888880002".equals(r.stuId)
                            && "M 100M".equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date8.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(77702L).equals(r.serialNbr)
                            && "FOBAZ".equals(r.version)
                            && "ST".equals(r.examSource)) {

                        found = true;

                    } else {
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected examPlaced ", r.examPlaced);
                        Log.warning("Unexpected examDt ", r.examDt);
                        Log.warning("Unexpected dtCrRefused ", r.dtCrRefused);
                        Log.warning("Unexpected serialNbr ", r.serialNbr);
                        Log.warning("Unexpected version ", r.version);
                        Log.warning("Unexpected examSource ", r.examSource);
                    }
                }

                assertTrue(found, "'M 100M' record was not updated");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while applying 'M 100M' credit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("apply(M 100M) when 'M 100T' exists")
    void test0013() {

        // "apply" with M 100M record, when an M 100M record already exists (should update)

        final RawMpeCredit rec = new RawMpeCredit("888880003", "M 100M", "P", date9, null,
                Long.valueOf(77703L), "FOBIX", "SU");

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                RawMpeCreditLogic.INSTANCE.apply(cache, rec);

                final List<RawMpeCredit> all = RawMpeCreditLogic.queryByStudent(cache, "888880003");

                assertEquals(1, all.size(), //
                        "Incorrect record count from queryByStudent");

                boolean found = false;

                for (final RawMpeCredit r : all) {

                    if ("888880003".equals(r.stuId)
                            && "M 100M".equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date9.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(77703L).equals(r.serialNbr)
                            && "FOBIX".equals(r.version)
                            && "SU".equals(r.examSource)) {

                        found = true;

                    } else {
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected examPlaced ", r.examPlaced);
                        Log.warning("Unexpected examDt ", r.examDt);
                        Log.warning("Unexpected dtCrRefused ", r.dtCrRefused);
                        Log.warning("Unexpected serialNbr ", r.serialNbr);
                        Log.warning("Unexpected version ", r.version);
                        Log.warning("Unexpected examSource ", r.examSource);
                    }
                }

                assertTrue(found, "'M 100M' record was not inserted");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while applying 'M 100M' credit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("apply(M 100C) when 'M 100M' exists")
    void test0014() {

        // "apply" with M 100C record, when an M 100* record already exists (should delete/insert)

        final RawMpeCredit rec = new RawMpeCredit("888880002", RawRecordConstants.M100C, "P", date10,
                null, Long.valueOf(77704L), "FROBZ", "SV");

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                RawMpeCreditLogic.INSTANCE.apply(cache, rec);

                final List<RawMpeCredit> all = RawMpeCreditLogic.queryByStudent(cache, "888880002");

                assertEquals(1, all.size(), //
                        "Incorrect record count from queryByStudent");

                boolean found = false;

                for (final RawMpeCredit r : all) {

                    if ("888880002".equals(r.stuId)
                            && RawRecordConstants.M100C.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date10.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(77704L).equals(r.serialNbr)
                            && "FROBZ".equals(r.version)
                            && "SV".equals(r.examSource)) {

                        found = true;

                    } else {
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected examPlaced ", r.examPlaced);
                        Log.warning("Unexpected examDt ", r.examDt);
                        Log.warning("Unexpected dtCrRefused ", r.dtCrRefused);
                        Log.warning("Unexpected serialNbr ", r.serialNbr);
                        Log.warning("Unexpected version ", r.version);
                        Log.warning("Unexpected examSource ", r.examSource);
                    }
                }

                assertTrue(found, "'M 100C' record was not inserted");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while applying 'M 100C' credit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("apply(M 125) with existing result")
    void test0015() {

        // "apply" with M 125 record, no change in placement result

        final RawMpeCredit rec = new RawMpeCredit("123456789", RawRecordConstants.M125, "P", date11,
                null, Long.valueOf(88801L), "ZYXWV", "UT");

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                RawMpeCreditLogic.INSTANCE.apply(cache, rec);

                final List<RawMpeCredit> all = RawMpeCreditLogic.queryByStudent(cache, "123456789");

                assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

                boolean found = false;

                for (final RawMpeCredit r : all) {

                    if ("123456789".equals(r.stuId)
                            && RawRecordConstants.M125.equals(r.course)
                            && "P".equals(r.examPlaced)
                            && date11.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(88801L).equals(r.serialNbr)
                            && "ZYXWV".equals(r.version)
                            && "UT".equals(r.examSource)) {

                        found = true;
                        break;
                    }
                }

                assertTrue(found, "'M 125' record was not updated");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while applying 'M 125' credit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("apply(M 125) with improved result")
    void test0016() {

        // "apply" with M 125 record, placement result improved

        final RawMpeCredit rec = new RawMpeCredit("123456789", RawRecordConstants.M125, "C", date12,
                null, Long.valueOf(88802L), "UTSRQ", "US");

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                RawMpeCreditLogic.INSTANCE.apply(cache, rec);

                final List<RawMpeCredit> all = RawMpeCreditLogic.queryByStudent(cache, "123456789");

                assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

                boolean found = false;

                for (final RawMpeCredit r : all) {

                    if ("123456789".equals(r.stuId)
                            && RawRecordConstants.M125.equals(r.course)
                            && "C".equals(r.examPlaced)
                            && date12.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(88802L).equals(r.serialNbr)
                            && "UTSRQ".equals(r.version)
                            && "US".equals(r.examSource)) {

                        found = true;
                        break;
                    }
                }

                assertTrue(found, "'M 125' record was not updated");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while applying 'M 125' credit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("apply(M 125) with reduced result")
    void test0017() {

        // "apply" with M 125 record, placement result improved

        final RawMpeCredit rec = new RawMpeCredit("123456789", RawRecordConstants.M125, "P", date13,
                null, Long.valueOf(88803L), "PONML", "UT");

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                RawMpeCreditLogic.INSTANCE.apply(cache, rec);

                final List<RawMpeCredit> all = RawMpeCreditLogic.queryByStudent(cache, "123456789");

                assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

                boolean found = false;

                for (final RawMpeCredit r : all) {

                    if ("123456789".equals(r.stuId)
                            && RawRecordConstants.M125.equals(r.course)
                            && "C".equals(r.examPlaced)
                            && date12.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(88802L).equals(r.serialNbr)
                            && "UTSRQ".equals(r.version)
                            && "US".equals(r.examSource)) {

                        found = true;
                        break;
                    }
                }

                assertTrue(found, "'M 125' record was incorrectly updated");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while applying 'M 125' credit: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("apply(M 125) with no existing")
    void test0018() {

        // "apply" with M 125 record, placement result improved

        final RawMpeCredit rec = new RawMpeCredit("123456789", "M 130", "C", date14, null,
                Long.valueOf(88804L), "KJIHG", "UV");

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                RawMpeCreditLogic.INSTANCE.apply(cache, rec);

                final List<RawMpeCredit> all = RawMpeCreditLogic.queryByStudent(cache, "123456789");

                assertEquals(3, all.size(), "Incorrect record count from queryByStudent");

                boolean found = false;

                for (final RawMpeCredit r : all) {

                    if ("123456789".equals(r.stuId)
                            && "M 130".equals(r.course)
                            && "C".equals(r.examPlaced)
                            && date14.equals(r.examDt)
                            && r.dtCrRefused == null
                            && Long.valueOf(88804L).equals(r.serialNbr)
                            && "KJIHG".equals(r.version)
                            && "UV".equals(r.examSource)) {

                        found = true;
                        break;
                    }
                }

                assertTrue(found, "'M 125' record was not inserted");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while applying 'M 125' credit: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM mpe_credit");
                }

                conn.commit();

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        }
    }
}

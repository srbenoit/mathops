package dev.mathops.db.old.rawlogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawRemoteMpe;

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
 * Tests for the {@code RawRemoteMpeLogic} class.
 */
final class TestRawRemoteMpeLogic {

    /** A term key used in test records. */
    private static final TermKey fa21 = new TermKey(ETermName.FALL, 2021);

    /** A term key used in test records. */
    private static final TermKey sp22 = new TermKey(ETermName.SPRING, 2022);

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2022, 2, 3);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2022, 3, 4);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2023, 4, 5);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2024, 5, 6);

    /** A date used in test records. */
    private static final LocalDate date7 = LocalDate.of(2025, 6, 7);

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
                    stmt.executeUpdate("DELETE FROM remote_mpe");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawRemoteMpe raw1 = new RawRemoteMpe(fa21, fa21, RawRecordConstants.M100P, date1, date2);
                final RawRemoteMpe raw2 = new RawRemoteMpe(fa21, sp22, RawRecordConstants.M100P, date3, date4);
                final RawRemoteMpe raw3 = new RawRemoteMpe(sp22, sp22, RawRecordConstants.M100P, date4, date6);
                final RawRemoteMpe raw4 = new RawRemoteMpe(sp22, sp22, RawRecordConstants.M100T, date6, date7);

                assertTrue(RawRemoteMpeLogic.INSTANCE.insert(cache, raw1), "Failed to insert remote_mpe 1");
                assertTrue(RawRemoteMpeLogic.INSTANCE.insert(cache, raw2), "Failed to insert remote_mpe 2");
                assertTrue(RawRemoteMpeLogic.INSTANCE.insert(cache, raw3), "Failed to insert remote_mpe 3");
                assertTrue(RawRemoteMpeLogic.INSTANCE.insert(cache, raw4), "Failed to insert remote_mpe 4");
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
                final List<RawRemoteMpe> all = RawRemoteMpeLogic.INSTANCE.queryAll(cache);

                assertEquals(4, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final RawRemoteMpe test : all) {

                    if (fa21.equals(test.termKey)
                            && fa21.equals(test.aplnTerm)
                            && RawRecordConstants.M100P.equals(test.course)
                            && date1.equals(test.startDt)
                            && date2.equals(test.endDt)) {
                        found1 = true;
                    } else if (fa21.equals(test.termKey)
                            && sp22.equals(test.aplnTerm)
                            && RawRecordConstants.M100P.equals(test.course)
                            && date3.equals(test.startDt)
                            && date4.equals(test.endDt)) {
                        found2 = true;
                    } else if (sp22.equals(test.termKey)
                            && sp22.equals(test.aplnTerm)
                            && RawRecordConstants.M100P.equals(test.course)
                            && date4.equals(test.startDt)
                            && date6.equals(test.endDt)) {
                        found3 = true;
                    } else if (sp22.equals(test.termKey)
                            && sp22.equals(test.aplnTerm)
                            && RawRecordConstants.M100T.equals(test.course)
                            && date6.equals(test.startDt)
                            && date7.equals(test.endDt)) {
                        found4 = true;
                    } else {
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected aplnTerm ", test.aplnTerm);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected startDt ", test.startDt);
                        Log.warning("Unexpected endDt ", test.endDt);
                    }
                }

                assertTrue(found1, "remote_mpe 1 not found");
                assertTrue(found2, "remote_mpe 2 not found");
                assertTrue(found3, "remote_mpe 3 not found");
                assertTrue(found4, "remote_mpe 4 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all remote_mpe rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByApplicationTerm results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawRemoteMpe> all = RawRemoteMpeLogic.queryByApplicationTerm(cache, "SP22");

                assertEquals(3, all.size(), "Incorrect record count from queryByApplicationTerm");

                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final RawRemoteMpe test : all) {

                    if (fa21.equals(test.termKey)
                            && sp22.equals(test.aplnTerm)
                            && RawRecordConstants.M100P.equals(test.course)
                            && date3.equals(test.startDt)
                            && date4.equals(test.endDt)) {
                        found2 = true;
                    } else if (sp22.equals(test.termKey)
                            && sp22.equals(test.aplnTerm)
                            && RawRecordConstants.M100P.equals(test.course)
                            && date4.equals(test.startDt)
                            && date6.equals(test.endDt)) {
                        found3 = true;
                    } else if (sp22.equals(test.termKey)
                            && sp22.equals(test.aplnTerm)
                            && RawRecordConstants.M100T.equals(test.course)
                            && date6.equals(test.startDt)
                            && date7.equals(test.endDt)) {
                        found4 = true;
                    } else {
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected aplnTerm ", test.aplnTerm);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected startDt ", test.startDt);
                        Log.warning("Unexpected endDt ", test.endDt);
                    }
                }

                assertTrue(found2, "remote_mpe 2 not found");
                assertTrue(found3, "remote_mpe 3 not found");
                assertTrue(found4, "remote_mpe 4 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying remote_mpe by application term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawRemoteMpe> all = RawRemoteMpeLogic.queryByCourse(cache, RawRecordConstants.M100P);

                assertEquals(3, all.size(), "Incorrect record count from queryByCourse");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawRemoteMpe test : all) {

                    if (fa21.equals(test.termKey)
                            && fa21.equals(test.aplnTerm)
                            && RawRecordConstants.M100P.equals(test.course)
                            && date1.equals(test.startDt)
                            && date2.equals(test.endDt)) {
                        found1 = true;
                    } else if (fa21.equals(test.termKey)
                            && sp22.equals(test.aplnTerm)
                            && RawRecordConstants.M100P.equals(test.course)
                            && date3.equals(test.startDt)
                            && date4.equals(test.endDt)) {
                        found2 = true;
                    } else if (sp22.equals(test.termKey)
                            && sp22.equals(test.aplnTerm)
                            && RawRecordConstants.M100P.equals(test.course)
                            && date4.equals(test.startDt)
                            && date6.equals(test.endDt)) {
                        found3 = true;
                    } else {
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected aplnTerm ", test.aplnTerm);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected startDt ", test.startDt);
                        Log.warning("Unexpected endDt ", test.endDt);
                    }
                }

                assertTrue(found1, "remote_mpe 1 not found");
                assertTrue(found2, "remote_mpe 2 not found");
                assertTrue(found3, "remote_mpe 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all remote_mpe rows: " + ex.getMessage());
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
                final RawRemoteMpe raw2 = new RawRemoteMpe(fa21, sp22, RawRecordConstants.M100P, date3, date4);

                final boolean result = RawRemoteMpeLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawRemoteMpe> all = RawRemoteMpeLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final RawRemoteMpe test : all) {

                    if (fa21.equals(test.termKey)
                            && fa21.equals(test.aplnTerm)
                            && RawRecordConstants.M100P.equals(test.course)
                            && date1.equals(test.startDt)
                            && date2.equals(test.endDt)) {
                        found1 = true;
                    } else if (sp22.equals(test.termKey)
                            && sp22.equals(test.aplnTerm)
                            && RawRecordConstants.M100P.equals(test.course)
                            && date4.equals(test.startDt)
                            && date6.equals(test.endDt)) {
                        found3 = true;
                    } else if (sp22.equals(test.termKey)
                            && sp22.equals(test.aplnTerm)
                            && RawRecordConstants.M100T.equals(test.course)
                            && date6.equals(test.startDt)
                            && date7.equals(test.endDt)) {
                        found4 = true;
                    } else {
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected aplnTerm ", test.aplnTerm);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected startDt ", test.startDt);
                        Log.warning("Unexpected endDt ", test.endDt);
                    }
                }

                assertTrue(found1, "remote_mpe 1 not found");
                assertTrue(found3, "remote_mpe 3 not found");
                assertTrue(found4, "remote_mpe 4 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting remote_mpe: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM remote_mpe");
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

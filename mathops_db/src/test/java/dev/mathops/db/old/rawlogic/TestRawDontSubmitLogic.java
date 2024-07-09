package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawrecord.RawDontSubmit;
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
import java.util.List;

/**
 * Tests for the {@code RawDontSubmitLogic} classes.
 */
final class TestRawDontSubmitLogic {

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
                    stmt.executeUpdate("DELETE FROM dont_submit");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawDontSubmit raw1 = new RawDontSubmit(new TermKey(ETermName.SPRING, 2021),
                        RawRecordConstants.M117, "001");
                final RawDontSubmit raw2 = new RawDontSubmit(new TermKey(ETermName.SUMMER, 2022),
                        RawRecordConstants.M118, "002");
                final RawDontSubmit raw3 = new RawDontSubmit(new TermKey(ETermName.FALL, 2023),
                        RawRecordConstants.M124, "003");

                assertTrue(RawDontSubmitLogic.INSTANCE.insert(cache, raw1), "Failed to insert dont_submit");
                assertTrue(RawDontSubmitLogic.INSTANCE.insert(cache, raw2), "Failed to insert dont_submit");
                assertTrue(RawDontSubmitLogic.INSTANCE.insert(cache, raw3), "Failed to insert dont_submit");
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
                final List<RawDontSubmit> all = RawDontSubmitLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawDontSubmit test : all) {

                    if (test.termKey.name == ETermName.SPRING
                            && Integer.valueOf(2021).equals(test.termKey.year)
                            && RawRecordConstants.M117.equals(test.course)
                            && "001".equals(test.sect)) {
                        found1 = true;
                    } else if (test.termKey.name == ETermName.SUMMER
                            && Integer.valueOf(2022).equals(test.termKey.year)
                            && RawRecordConstants.M118.equals(test.course)
                            && "002".equals(test.sect)) {
                        found2 = true;
                    } else if (test.termKey.name == ETermName.FALL
                            && Integer.valueOf(2023).equals(test.termKey.year)
                            && RawRecordConstants.M124.equals(test.course)
                            && "003".equals(test.sect)) {
                        found3 = true;
                    } else {
                        Log.warning("Unexpected dont_submit: ", test);
                    }
                }

                assertTrue(found1, "DontSubmit for 117/001 not found");
                assertTrue(found2, "DontSubmit for 118/002 not found");
                assertTrue(found3, "DontSubmit for 124/003 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all dont_submit rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawDontSubmit toDelete = new RawDontSubmit(new TermKey(ETermName.SPRING, 2021),
                        RawRecordConstants.M117, "001");

                assertTrue(RawDontSubmitLogic.INSTANCE.delete(cache, toDelete), "Delete dont_submit failed");

                final List<RawDontSubmit> all = RawDontSubmitLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting dont_submit rows: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM dont_submit");
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

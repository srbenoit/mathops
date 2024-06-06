package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.logic.DbConnection;
import dev.mathops.db.logic.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawMsg;

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
 * Tests for the {@code RawMsgLogic} class.
 */
final class TestRawMsgLogic {

    /** A term key. */
    private static final TermKey fa20 = new TermKey("FA20");

    /** A term key. */
    private static final TermKey fa21 = new TermKey("FA21");

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
                    stmt.executeUpdate("DELETE FROM msg");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawMsg raw1 = new RawMsg(fa20, "TP1", "MSg01", "Subject1a", "Template1a");
                final RawMsg raw2 = new RawMsg(fa21, "TP1", "MSg01", "Subject1b", "Template1b");
                final RawMsg raw3 = new RawMsg(fa21, "TP2", "MSg02", "Subject2", "Template2");

                assertTrue(RawMsgLogic.INSTANCE.insert(cache, raw1), "Failed to insert msg 1");
                assertTrue(RawMsgLogic.INSTANCE.insert(cache, raw2), "Failed to insert msg 2");
                assertTrue(RawMsgLogic.INSTANCE.insert(cache, raw3), "Failed to insert msg 3");
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
                final List<RawMsg> all = RawMsgLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawMsg r : all) {

                    if (fa20.equals(r.termKey)
                            && "TP1".equals(r.touchPoint)
                            && "MSg01".equals(r.msgCode)
                            && "Subject1a".equals(r.subject)
                            && "Template1a".equals(r.template)) {

                        found1 = true;

                    } else if (fa21.equals(r.termKey)
                            && "TP1".equals(r.touchPoint)
                            && "MSg01".equals(r.msgCode)
                            && "Subject1b".equals(r.subject)
                            && "Template1b".equals(r.template)) {

                        found2 = true;

                    } else if (fa21.equals(r.termKey)
                            && "TP2".equals(r.touchPoint)
                            && "MSg02".equals(r.msgCode)
                            && "Subject2".equals(r.subject)
                            && "Template2".equals(r.template)) {

                        found3 = true;

                    } else {
                        Log.warning("Unexpected termKey ", r.termKey);
                        Log.warning("Unexpected touchPoint ", r.touchPoint);
                        Log.warning("Unexpected msgCode ", r.msgCode);
                        Log.warning("Unexpected subject ", r.subject);
                        Log.warning("Unexpected template ", r.template);
                    }
                }

                assertTrue(found1, "msg 1 not found");
                assertTrue(found2, "msg 2 not found");
                assertTrue(found3, "msg 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all msg rows: " + ex.getMessage());
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
                final RawMsg raw2 = new RawMsg(fa21, "TP1", "MSg01", "Subject1b", "Template1b");

                final boolean result = RawMsgLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawMsg> all = RawMsgLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawMsg r : all) {

                    if (fa20.equals(r.termKey)
                            && "TP1".equals(r.touchPoint)
                            && "MSg01".equals(r.msgCode)
                            && "Subject1a".equals(r.subject)
                            && "Template1a".equals(r.template)) {

                        found1 = true;

                    } else if (fa21.equals(r.termKey)
                            && "TP2".equals(r.touchPoint)
                            && "MSg02".equals(r.msgCode)
                            && "Subject2".equals(r.subject)
                            && "Template2".equals(r.template)) {

                        found3 = true;

                    } else {
                        Log.warning("Unexpected termKey ", r.termKey);
                        Log.warning("Unexpected touchPoint ", r.touchPoint);
                        Log.warning("Unexpected msgCode ", r.msgCode);
                        Log.warning("Unexpected subject ", r.subject);
                        Log.warning("Unexpected template ", r.template);
                    }
                }

                assertTrue(found1, "msg 1 not found");
                assertTrue(found3, "msg 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting msg: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM msg");
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

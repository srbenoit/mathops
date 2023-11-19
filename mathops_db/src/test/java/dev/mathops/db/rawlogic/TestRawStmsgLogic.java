package dev.mathops.db.rawlogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.cfg.ContextMap;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.rawrecord.RawStmsg;

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
 * Tests for the {@code RawStmsgLogic} class.
 */
final class TestRawStmsgLogic {

    /** A date used in test records. */
    private static final LocalDate date12 = LocalDate.of(2020, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date13 = LocalDate.of(2020, 1, 3);

    /** A date used in test records. */
    private static final LocalDate date14 = LocalDate.of(2020, 1, 4);

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
                    stmt.executeUpdate("DELETE FROM stmsg");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawStmsg raw1 = new RawStmsg("111111111", date12, Integer.valueOf(2), Integer.valueOf(1), "TP1",
                        "MSG1", "Sender1");

                final RawStmsg raw2 = new RawStmsg("111111111", date13, Integer.valueOf(3), Integer.valueOf(2), "TP2",
                        "MSG2", "Sender2");

                final RawStmsg raw3 = new RawStmsg("222222222", date14, Integer.valueOf(4), Integer.valueOf(3), "TP3",
                        "MSG3", "Sender3");

                assertTrue(RawStmsgLogic.INSTANCE.insert(cache, raw1), "Failed to insert stmsg 1");
                assertTrue(RawStmsgLogic.INSTANCE.insert(cache, raw2), "Failed to insert stmsg 2");
                assertTrue(RawStmsgLogic.INSTANCE.insert(cache, raw3), "Failed to insert stmsg 3");
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
                final List<RawStmsg> all = RawStmsgLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawStmsg test : all) {
                    if ("111111111".equals(test.stuId)
                            && date12.equals(test.msgDt)
                            && Integer.valueOf(2).equals(test.pace)
                            && Integer.valueOf(1).equals(test.courseIndex)
                            && "TP1".equals(test.touchPoint)
                            && "MSG1".equals(test.msgCode)
                            && "Sender1".equals(test.sender)) {

                        found1 = true;
                    } else if ("111111111".equals(test.stuId)
                            && date13.equals(test.msgDt)
                            && Integer.valueOf(3).equals(test.pace)
                            && Integer.valueOf(2).equals(test.courseIndex)
                            && "TP2".equals(test.touchPoint)
                            && "MSG2".equals(test.msgCode)
                            && "Sender2".equals(test.sender)) {

                        found2 = true;
                    } else if ("222222222".equals(test.stuId)
                            && date14.equals(test.msgDt)
                            && Integer.valueOf(4).equals(test.pace)
                            && Integer.valueOf(3).equals(test.courseIndex)
                            && "TP3".equals(test.touchPoint)
                            && "MSG3".equals(test.msgCode)
                            && "Sender3".equals(test.sender)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected msgDt ", test.msgDt);
                        Log.warning("Unexpected pace ", test.pace);
                        Log.warning("Unexpected courseIndex ", test.courseIndex);
                        Log.warning("Unexpected touchPoint ", test.touchPoint);
                        Log.warning("Unexpected msgCode ", test.msgCode);
                        Log.warning("Unexpected sender ", test.sender);
                    }
                }

                assertTrue(found1, "Stmsg 1 not found");
                assertTrue(found2, "Stmsg 2 not found");
                assertTrue(found3, "Stmsg 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stmsg rows: " + ex.getMessage());
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
                final List<RawStmsg> all = RawStmsgLogic.queryByStudent(cache, "111111111");

                assertEquals(2, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;

                for (final RawStmsg test : all) {
                    if ("111111111".equals(test.stuId)
                            && date12.equals(test.msgDt)
                            && Integer.valueOf(2).equals(test.pace)
                            && Integer.valueOf(1).equals(test.courseIndex)
                            && "TP1".equals(test.touchPoint)
                            && "MSG1".equals(test.msgCode)
                            && "Sender1".equals(test.sender)) {

                        found1 = true;
                    } else if ("111111111".equals(test.stuId)
                            && date13.equals(test.msgDt)
                            && Integer.valueOf(3).equals(test.pace)
                            && Integer.valueOf(2).equals(test.courseIndex)
                            && "TP2".equals(test.touchPoint)
                            && "MSG2".equals(test.msgCode)
                            && "Sender2".equals(test.sender)) {

                        found2 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected msgDt ", test.msgDt);
                        Log.warning("Unexpected pace ", test.pace);
                        Log.warning("Unexpected courseIndex ", test.courseIndex);
                        Log.warning("Unexpected touchPoint ", test.touchPoint);
                        Log.warning("Unexpected msgCode ", test.msgCode);
                        Log.warning("Unexpected sender ", test.sender);
                    }
                }

                assertTrue(found1, "Stmsg 1 not found");
                assertTrue(found2, "Stmsg 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stmsg rows by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("count results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final Integer count = RawStmsgLogic.count(cache);

                assertEquals(Integer.valueOf(3), count, "count returned incorrect value");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying count: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getLatest results")
    void test0006() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final LocalDate latest = RawStmsgLogic.getLatest(cache);

                assertEquals(date14, latest, "getLatest returned incorrect value");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying latest message date: " + ex.getMessage());
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
                final RawStmsg raw2 = new RawStmsg("111111111", date13, Integer.valueOf(3), Integer.valueOf(2), "TP2",
                        "MSG2", "Sender2");

                final boolean result = RawStmsgLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawStmsg> all = RawStmsgLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawStmsg test : all) {
                    if ("111111111".equals(test.stuId)
                            && date12.equals(test.msgDt)
                            && Integer.valueOf(2).equals(test.pace)
                            && Integer.valueOf(1).equals(test.courseIndex)
                            && "TP1".equals(test.touchPoint)
                            && "MSG1".equals(test.msgCode)
                            && "Sender1".equals(test.sender)) {

                        found1 = true;
                    } else if ("222222222".equals(test.stuId)
                            && date14.equals(test.msgDt)
                            && Integer.valueOf(4).equals(test.pace)
                            && Integer.valueOf(3).equals(test.courseIndex)
                            && "TP3".equals(test.touchPoint)
                            && "MSG3".equals(test.msgCode)
                            && "Sender3".equals(test.sender)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected msgDt ", test.msgDt);
                        Log.warning("Unexpected pace ", test.pace);
                        Log.warning("Unexpected courseIndex ", test.courseIndex);
                        Log.warning("Unexpected touchPoint ", test.touchPoint);
                        Log.warning("Unexpected msgCode ", test.msgCode);
                        Log.warning("Unexpected sender ", test.sender);
                    }
                }

                assertTrue(found1, "Stmsg 1 not found");
                assertTrue(found3, "Stmsg 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting stmsg: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM stmsg");
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

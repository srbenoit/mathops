package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawNewstu;

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
 * Tests for the {@code RawNewstuLogic} class.
 */
final class TestRawNewstuLogic {

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
                    stmt.executeUpdate("DELETE FROM newstu");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawNewstu raw1 = new RawNewstu("111111111", "A", "B", "C");
                final RawNewstu raw2 = new RawNewstu("222222222", "D", "E", "F");

                assertTrue(RawNewstuLogic.INSTANCE.insert(cache, raw1), "Failed to insert newstu 1");
                assertTrue(RawNewstuLogic.INSTANCE.insert(cache, raw2), "Failed to insert newstu 2");
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
                final List<RawNewstu> all = RawNewstuLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;

                for (final RawNewstu test : all) {
                    if ("111111111".equals(test.stuId)
                            && "A".equals(test.acadLevel)
                            && "B".equals(test.regType)
                            && "C".equals(test.term)) {

                        found1 = true;
                    } else if ("222222222".equals(test.stuId)
                            && "D".equals(test.acadLevel)
                            && "E".equals(test.regType)
                            && "F".equals(test.term)) {

                        found2 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected acadLevel ", test.acadLevel);
                        Log.warning("Unexpected regType ", test.regType);
                        Log.warning("Unexpected term ", test.term);
                    }
                }

                assertTrue(found1, "Newstu 1 not found");
                assertTrue(found2, "Newstu 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all newstu rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            final RawNewstu raw2 = new RawNewstu("222222222", "D", "E", "F");

            try {
                final boolean result = RawNewstuLogic.INSTANCE.delete(cache, raw2);

                assertTrue(result, "delete returned false");

                final List<RawNewstu> all = RawNewstuLogic.INSTANCE.queryAll(cache);

                assertEquals(1, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;

                for (final RawNewstu test : all) {
                    if ("111111111".equals(test.stuId)
                            && "A".equals(test.acadLevel)
                            && "B".equals(test.regType)
                            && "C".equals(test.term)) {

                        found1 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected acadLevel ", test.acadLevel);
                        Log.warning("Unexpected regType ", test.regType);
                        Log.warning("Unexpected term ", test.term);
                    }
                }

                assertTrue(found1, "Newstu 1 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleteing a newstu row: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("deleteAll results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final int count = RawNewstuLogic.deleteAll(cache);

                assertEquals(1, count, "Incorrect record count from deleteAll");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleteing all newstu rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0006() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawNewstu> all = RawNewstuLogic.INSTANCE.queryAll(cache);

                assertEquals(0, all.size(), "Incorrect record count from queryAll");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (
                final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all newstu rows: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM newstu");
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

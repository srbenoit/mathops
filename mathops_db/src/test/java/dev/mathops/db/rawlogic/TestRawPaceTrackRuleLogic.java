package dev.mathops.db.rawlogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.TermKey;
import dev.mathops.db.cfg.ContextMap;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.rawrecord.RawPaceTrackRule;

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
 * Tests for the {@code RawPaceTrackRuleLogic} class.
 */
final class TestRawPaceTrackRuleLogic {

    /** A term key. */
    private static final TermKey fa21 = new TermKey("FA21");

    /** A term key. */
    private static final TermKey fa20 = new TermKey("FA20");

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
                    stmt.executeUpdate("DELETE FROM pace_track_rule");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawPaceTrackRule raw1 = new RawPaceTrackRule(fa21, "A", Integer.valueOf(1), "B", "C");
                final RawPaceTrackRule raw2 = new RawPaceTrackRule(fa21, "A", Integer.valueOf(2), "D", "E");
                final RawPaceTrackRule raw3 = new RawPaceTrackRule(fa20, "A", Integer.valueOf(3), "F", "G");
                final RawPaceTrackRule raw4 = new RawPaceTrackRule(fa21, "B", Integer.valueOf(4), "H", "I");

                assertTrue(RawPaceTrackRuleLogic.INSTANCE.insert(cache, raw1), "Failed to insert pace_track_rule 1");
                assertTrue(RawPaceTrackRuleLogic.INSTANCE.insert(cache, raw2), "Failed to insert pace_track_rule 2");
                assertTrue(RawPaceTrackRuleLogic.INSTANCE.insert(cache, raw3), "Failed to insert pace_track_rule 3");
                assertTrue(RawPaceTrackRuleLogic.INSTANCE.insert(cache, raw4), "Failed to insert pace_track_rule 4");
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
                final List<RawPaceTrackRule> all = RawPaceTrackRuleLogic.INSTANCE.queryAll(cache);

                assertEquals(4, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final RawPaceTrackRule test : all) {
                    if (fa21.equals(test.termKey)
                            && "A".equals(test.subterm)
                            && Integer.valueOf(1).equals(test.pace)
                            && "B".equals(test.paceTrack)
                            && "C".equals(test.criteria)) {

                        found1 = true;
                    } else if (fa21.equals(test.termKey)
                            && "A".equals(test.subterm)
                            && Integer.valueOf(2).equals(test.pace)
                            && "D".equals(test.paceTrack)
                            && "E".equals(test.criteria)) {

                        found2 = true;
                    } else if (fa20.equals(test.termKey)
                            && "A".equals(test.subterm)
                            && Integer.valueOf(3).equals(test.pace)
                            && "F".equals(test.paceTrack)
                            && "G".equals(test.criteria)) {

                        found3 = true;
                    } else if (fa21.equals(test.termKey)
                            && "B".equals(test.subterm)
                            && Integer.valueOf(4).equals(test.pace)
                            && "H".equals(test.paceTrack)
                            && "I".equals(test.criteria)) {

                        found4 = true;
                    } else {
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected subterm ", test.subterm);
                        Log.warning("Unexpected pace ", test.pace);
                        Log.warning("Unexpected paceTrack ", test.paceTrack);
                        Log.warning("Unexpected criteria ", test.criteria);
                    }
                }

                assertTrue(found1, "PaceTrackRule 1 not found");
                assertTrue(found2, "PaceTrackRule 2 not found");
                assertTrue(found3, "PaceTrackRule 3 not found");
                assertTrue(found4, "PaceTrackRule 4 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all pace_track_rule rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByTerm results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawPaceTrackRule> all = RawPaceTrackRuleLogic.queryByTerm(cache, fa21);

                assertEquals(3, all.size(), "Incorrect record count from queryByTerm");

                boolean found1 = false;
                boolean found2 = false;
                boolean found4 = false;

                for (final RawPaceTrackRule test : all) {
                    if (fa21.equals(test.termKey)
                            && "A".equals(test.subterm)
                            && Integer.valueOf(1).equals(test.pace)
                            && "B".equals(test.paceTrack)
                            && "C".equals(test.criteria)) {

                        found1 = true;
                    } else if (fa21.equals(test.termKey)
                            && "A".equals(test.subterm)
                            && Integer.valueOf(2).equals(test.pace)
                            && "D".equals(test.paceTrack)
                            && "E".equals(test.criteria)) {

                        found2 = true;
                    } else if (fa21.equals(test.termKey)
                            && "B".equals(test.subterm)
                            && Integer.valueOf(4).equals(test.pace)
                            && "H".equals(test.paceTrack)
                            && "I".equals(test.criteria)) {

                        found4 = true;
                    } else {
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected subterm ", test.subterm);
                        Log.warning("Unexpected pace ", test.pace);
                        Log.warning("Unexpected paceTrack ", test.paceTrack);
                        Log.warning("Unexpected criteria ", test.criteria);
                    }
                }

                assertTrue(found1, "PaceTrackRule 1 not found");
                assertTrue(found2, "PaceTrackRule 2 not found");
                assertTrue(found4, "PaceTrackRule 4 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying pace_track_rule rows by term: " + ex.getMessage());
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
                final RawPaceTrackRule raw2 = new RawPaceTrackRule(fa21, "A", Integer.valueOf(2), "D", "E");

                final boolean result = RawPaceTrackRuleLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawPaceTrackRule> all = RawPaceTrackRuleLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final RawPaceTrackRule test : all) {
                    if (fa21.equals(test.termKey)
                            && "A".equals(test.subterm)
                            && Integer.valueOf(1).equals(test.pace)
                            && "B".equals(test.paceTrack)
                            && "C".equals(test.criteria)) {

                        found1 = true;
                    } else if (fa20.equals(test.termKey)
                            && "A".equals(test.subterm)
                            && Integer.valueOf(3).equals(test.pace)
                            && "F".equals(test.paceTrack)
                            && "G".equals(test.criteria)) {

                        found3 = true;
                    } else if (fa21.equals(test.termKey)
                            && "B".equals(test.subterm)
                            && Integer.valueOf(4).equals(test.pace)
                            && "H".equals(test.paceTrack)
                            && "I".equals(test.criteria)) {

                        found4 = true;
                    } else {
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected subterm ", test.subterm);
                        Log.warning("Unexpected pace ", test.pace);
                        Log.warning("Unexpected paceTrack ", test.paceTrack);
                        Log.warning("Unexpected criteria ", test.criteria);
                    }
                }

                assertTrue(found1, "pace_track_rule 1 not found");
                assertTrue(found3, "pace_track_rule 3 not found");
                assertTrue(found4, "pace_track_rule 4 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting pace_track_rule: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM pace_track_rule");
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

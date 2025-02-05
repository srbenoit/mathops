package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawPacingRules;
import dev.mathops.db.type.TermKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawPacingRulesLogic} class.
 */
final class TestRawPacingRulesLogic {

    /** A term key. */
    private static final TermKey fa21 = new TermKey("FA21");

    /** A term key. */
    private static final TermKey fa20 = new TermKey("FA20");

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
                stmt.executeUpdate("DELETE FROM pacing_rules");
            }
            conn.commit();

            final RawPacingRules raw1 = new RawPacingRules(fa21, "A", "B", "C");
            final RawPacingRules raw2 = new RawPacingRules(fa21, "A", "D", "E");
            final RawPacingRules raw3 = new RawPacingRules(fa20, "A", "F", "G");
            final RawPacingRules raw4 = new RawPacingRules(fa21, "B", "H", "I");

            assertTrue(RawPacingRulesLogic.insert(cache, raw1), "Failed to insert pacing_rules 1");
            assertTrue(RawPacingRulesLogic.insert(cache, raw2), "Failed to insert pacing_rules 2");
            assertTrue(RawPacingRulesLogic.insert(cache, raw3), "Failed to insert pacing_rules 3");
            assertTrue(RawPacingRulesLogic.insert(cache, raw4), "Failed to insert pacing_rules 4");
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
            final List<RawPacingRules> all = RawPacingRulesLogic.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final RawPacingRules test : all) {
                if (fa21.equals(test.termKey)
                    && "A".equals(test.pacingStructure)
                    && "B".equals(test.activityType)
                    && "C".equals(test.requirement)) {

                    found1 = true;
                } else if (fa21.equals(test.termKey)
                           && "A".equals(test.pacingStructure)
                           && "D".equals(test.activityType)
                           && "E".equals(test.requirement)) {

                    found2 = true;
                } else if (fa20.equals(test.termKey)
                           && "A".equals(test.pacingStructure)
                           && "F".equals(test.activityType)
                           && "G".equals(test.requirement)) {

                    found3 = true;
                } else if (fa21.equals(test.termKey)
                           && "B".equals(test.pacingStructure)
                           && "H".equals(test.activityType)
                           && "I".equals(test.requirement)) {

                    found4 = true;
                } else {
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected pacingStructure ", test.pacingStructure);
                    Log.warning("Unexpected activityType ", test.activityType);
                    Log.warning("Unexpected requirement ", test.requirement);
                }
            }

            assertTrue(found1, "PacingRules 1 not found");
            assertTrue(found2, "PacingRules 2 not found");
            assertTrue(found3, "PacingRules 3 not found");
            assertTrue(found4, "PacingRules 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all pacing_rules rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByTerm results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawPacingRules> all = RawPacingRulesLogic.queryByTerm(cache, fa21);

            assertEquals(3, all.size(), "Incorrect record count from queryByTerm");

            boolean found1 = false;
            boolean found2 = false;
            boolean found4 = false;

            for (final RawPacingRules test : all) {
                if (fa21.equals(test.termKey)
                    && "A".equals(test.pacingStructure)
                    && "B".equals(test.activityType)
                    && "C".equals(test.requirement)) {

                    found1 = true;
                } else if (fa21.equals(test.termKey)
                           && "A".equals(test.pacingStructure)
                           && "D".equals(test.activityType)
                           && "E".equals(test.requirement)) {

                    found2 = true;
                } else if (fa21.equals(test.termKey)
                           && "B".equals(test.pacingStructure)
                           && "H".equals(test.activityType)
                           && "I".equals(test.requirement)) {

                    found4 = true;
                } else {
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected pacingStructure ", test.pacingStructure);
                    Log.warning("Unexpected activityType ", test.activityType);
                    Log.warning("Unexpected requirement ", test.requirement);
                }
            }

            assertTrue(found1, "PacingRules 1 not found");
            assertTrue(found2, "PacingRules 2 not found");
            assertTrue(found4, "PacingRules 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying pacing_rules rows by term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByTermAndPacingStructure results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawPacingRules> all = RawPacingRulesLogic.queryByTermAndPacingStructure(cache, fa21, "A");

            assertEquals(2, all.size(), "Incorrect record count from queryByTerm");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawPacingRules test : all) {
                if (fa21.equals(test.termKey)
                    && "A".equals(test.pacingStructure)
                    && "B".equals(test.activityType)
                    && "C".equals(test.requirement)) {

                    found1 = true;
                } else if (fa21.equals(test.termKey)
                           && "A".equals(test.pacingStructure)
                           && "D".equals(test.activityType)
                           && "E".equals(test.requirement)) {

                    found2 = true;
                } else {
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected pacingStructure ", test.pacingStructure);
                    Log.warning("Unexpected activityType ", test.activityType);
                    Log.warning("Unexpected requirement ", test.requirement);
                }
            }

            assertTrue(found1, "PacingRules 1 not found");
            assertTrue(found2, "PacingRules 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying pacing_rules rows by term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("isRequired results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            assertTrue(RawPacingRulesLogic.isRequired(cache, fa21, "A", "B", "C"),
                    "Invalid result from isRequired");

            assertTrue(RawPacingRulesLogic.isRequired(cache, fa21, "A", "D", "E"),
                    "Invalid result from isRequired");

            assertFalse(RawPacingRulesLogic.isRequired(cache, fa21, "A", "B", "E"),
                    "Invalid result from isRequired");

            assertFalse(RawPacingRulesLogic.isRequired(cache, fa21, "A", "D", "C"),
                    "Invalid result from isRequired");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while testing activity requirements: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            final RawPacingRules raw2 = new RawPacingRules(fa21, "A", "D", "E");

            final boolean result = RawPacingRulesLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawPacingRules> all = RawPacingRulesLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final RawPacingRules test : all) {
                if (fa21.equals(test.termKey)
                    && "A".equals(test.pacingStructure)
                    && "B".equals(test.activityType)
                    && "C".equals(test.requirement)) {

                    found1 = true;
                } else if (fa20.equals(test.termKey)
                           && "A".equals(test.pacingStructure)
                           && "F".equals(test.activityType)
                           && "G".equals(test.requirement)) {

                    found3 = true;
                } else if (fa21.equals(test.termKey)
                           && "B".equals(test.pacingStructure)
                           && "H".equals(test.activityType)
                           && "I".equals(test.requirement)) {

                    found4 = true;
                } else {
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected pacingStructure ", test.pacingStructure);
                    Log.warning("Unexpected activityType ", test.activityType);
                    Log.warning("Unexpected requirement ", test.requirement);
                }
            }

            assertTrue(found1, "pacing_rules 1 not found");
            assertTrue(found3, "pacing_rules 3 not found");
            assertTrue(found4, "pacing_rules 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting pacing_rules: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM pacing_rules");
            }

            conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning table: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }
}

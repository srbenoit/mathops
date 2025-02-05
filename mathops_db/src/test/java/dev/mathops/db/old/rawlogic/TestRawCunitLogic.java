package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawrecord.RawCunit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawCunitLogic} class.
 */
final class TestRawCunitLogic {

    /** A term key used in test records. */
    private static final TermKey fa21 = new TermKey(ETermName.FALL, 2021);

    /** A term key used in test records. */
    private static final TermKey fa20 = new TermKey(ETermName.FALL, 2020);

    /** A float used in test records. */
    private static final Float float1 = Float.valueOf(1.0f);

    /** A float used in test records. */
    private static final Float float15 = Float.valueOf(1.5f);

    /** A float used in test records. */
    private static final Float float2 = Float.valueOf(2.0f);

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
                stmt.executeUpdate("DELETE FROM cunit");
            }
            conn.commit();

            final RawCunit raw1 = new RawCunit(fa21, RawRecordConstants.M117, Integer.valueOf(1), float1, "Unit 1",
                    Integer.valueOf(30), Integer.valueOf(10), Integer.valueOf(5), "INST");

            final RawCunit raw2 = new RawCunit(fa21, RawRecordConstants.M117, Integer.valueOf(2), float15, "Unit 2",
                    Integer.valueOf(40), Integer.valueOf(20), Integer.valueOf(10), "INST");

            final RawCunit raw3 = new RawCunit(fa21, RawRecordConstants.M117, Integer.valueOf(3), float2, "Unit 3",
                    Integer.valueOf(80), Integer.valueOf(40), Integer.valueOf(20), "FIN");

            final RawCunit raw4 = new RawCunit(fa20, RawRecordConstants.M117, Integer.valueOf(1), float1, "Unit 1",
                    Integer.valueOf(30), Integer.valueOf(10), Integer.valueOf(5), "INST");

            final RawCunit raw5 = new RawCunit(fa21, RawRecordConstants.M118, Integer.valueOf(1), float1, "Unit 1",
                    Integer.valueOf(30), Integer.valueOf(10), Integer.valueOf(5), "INST");

            assertTrue(RawCunitLogic.insert(cache, raw1), "Failed to insert cunit 1");
            assertTrue(RawCunitLogic.insert(cache, raw2), "Failed to insert cunit 2");
            assertTrue(RawCunitLogic.insert(cache, raw3), "Failed to insert cunit 3");
            assertTrue(RawCunitLogic.insert(cache, raw4), "Failed to insert cunit 4");
            assertTrue(RawCunitLogic.insert(cache, raw5), "Failed to insert cunit 5");
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
            final List<RawCunit> all = RawCunitLogic.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final RawCunit r : all) {

                if (fa21.equals(r.termKey)
                    && RawRecordConstants.M117.equals(r.course)
                    && Integer.valueOf(1).equals(r.unit)
                    && float1.equals(r.unitExamWgt)
                    && "Unit 1".equals(r.unitDesc)
                    && Integer.valueOf(30).equals(r.unitTimelimit)
                    && Integer.valueOf(10).equals(r.possibleScore)
                    && Integer.valueOf(5).equals(r.nbrQuestions)
                    && "INST".equals(r.unitType)) {

                    found1 = true;
                } else if (fa21.equals(r.termKey)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(2).equals(r.unit)
                           && float15.equals(r.unitExamWgt)
                           && "Unit 2".equals(r.unitDesc)
                           && Integer.valueOf(40).equals(r.unitTimelimit)
                           && Integer.valueOf(20).equals(r.possibleScore)
                           && Integer.valueOf(10).equals(r.nbrQuestions)
                           && "INST".equals(r.unitType)) {

                    found2 = true;
                } else if (fa21.equals(r.termKey)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(3).equals(r.unit)
                           && float2.equals(r.unitExamWgt)
                           && "Unit 3".equals(r.unitDesc)
                           && Integer.valueOf(80).equals(r.unitTimelimit)
                           && Integer.valueOf(40).equals(r.possibleScore)
                           && Integer.valueOf(20).equals(r.nbrQuestions)
                           && "FIN".equals(r.unitType)) {

                    found3 = true;
                } else if (fa20.equals(r.termKey)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(1).equals(r.unit)
                           && float1.equals(r.unitExamWgt)
                           && "Unit 1".equals(r.unitDesc)
                           && Integer.valueOf(30).equals(r.unitTimelimit)
                           && Integer.valueOf(10).equals(r.possibleScore)
                           && Integer.valueOf(5).equals(r.nbrQuestions)
                           && "INST".equals(r.unitType)) {

                    found4 = true;
                } else if (fa21.equals(r.termKey)
                           && RawRecordConstants.M118.equals(r.course)
                           && Integer.valueOf(1).equals(r.unit)
                           && float1.equals(r.unitExamWgt)
                           && "Unit 1".equals(r.unitDesc)
                           && Integer.valueOf(30).equals(r.unitTimelimit)
                           && Integer.valueOf(10).equals(r.possibleScore)
                           && Integer.valueOf(5).equals(r.nbrQuestions)
                           && "INST".equals(r.unitType)) {

                    found5 = true;
                }
            }

            assertTrue(found1, "cunit 1 not found");
            assertTrue(found2, "cunit 2 not found");
            assertTrue(found3, "cunit 3 not found");
            assertTrue(found4, "cunit 4 not found");
            assertTrue(found5, "cunit 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all cunit rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            final RawCunit raw2 = new RawCunit(fa21, RawRecordConstants.M117, Integer.valueOf(2), float15, "Unit 2",
                    Integer.valueOf(40), Integer.valueOf(20), Integer.valueOf(10), "INST");

            final boolean result = RawCunitLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawCunit> all = RawCunitLogic.queryAll(cache);

            assertEquals(4, all.size(), //
                    "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final RawCunit r : all) {

                if (fa21.equals(r.termKey)
                    && RawRecordConstants.M117.equals(r.course)
                    && Integer.valueOf(1).equals(r.unit)
                    && float1.equals(r.unitExamWgt)
                    && "Unit 1".equals(r.unitDesc)
                    && Integer.valueOf(30).equals(r.unitTimelimit)
                    && Integer.valueOf(10).equals(r.possibleScore)
                    && Integer.valueOf(5).equals(r.nbrQuestions)
                    && "INST".equals(r.unitType)) {

                    found1 = true;
                } else if (fa21.equals(r.termKey)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(3).equals(r.unit)
                           && float2.equals(r.unitExamWgt)
                           && "Unit 3".equals(r.unitDesc)
                           && Integer.valueOf(80).equals(r.unitTimelimit)
                           && Integer.valueOf(40).equals(r.possibleScore)
                           && Integer.valueOf(20).equals(r.nbrQuestions)
                           && "FIN".equals(r.unitType)) {

                    found3 = true;
                } else if (fa20.equals(r.termKey)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(1).equals(r.unit)
                           && float1.equals(r.unitExamWgt)
                           && "Unit 1".equals(r.unitDesc)
                           && Integer.valueOf(30).equals(r.unitTimelimit)
                           && Integer.valueOf(10).equals(r.possibleScore)
                           && Integer.valueOf(5).equals(r.nbrQuestions)
                           && "INST".equals(r.unitType)) {

                    found4 = true;
                } else if (fa21.equals(r.termKey)
                           && RawRecordConstants.M118.equals(r.course)
                           && Integer.valueOf(1).equals(r.unit)
                           && float1.equals(r.unitExamWgt)
                           && "Unit 1".equals(r.unitDesc)
                           && Integer.valueOf(30).equals(r.unitTimelimit)
                           && Integer.valueOf(10).equals(r.possibleScore)
                           && Integer.valueOf(5).equals(r.nbrQuestions)
                           && "INST".equals(r.unitType)) {

                    found5 = true;
                }
            }

            assertTrue(found1, "cunit 1 not found");
            assertTrue(found3, "cunit 3 not found");
            assertTrue(found4, "cunit 4 not found");
            assertTrue(found5, "cunit 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting cunit: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM cunit");
            }

            conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }
}

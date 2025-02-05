package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawParameters;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawParametersLogic} class.
 */
final class TestRawParametersLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 12, 10);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2022, 1, 2);

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
                stmt.executeUpdate("DELETE FROM parameters");
            }
            conn.commit();

            final RawParameters pgm1 = new RawParameters("prog1", "A", "B", "C", "D", "E", "F", "G", "H", "I", date1);

            final RawParameters pgm2 = new RawParameters("prog2", "Z", "Y", "X", "W", "V", "U", "T", "S", "R", date2);

            assertTrue(RawParametersLogic.insert(cache, pgm1), "Failed to insert parameters 1");
            assertTrue(RawParametersLogic.insert(cache, pgm2), "Failed to insert parameters 2");
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
            final List<RawParameters> all = RawParametersLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawParameters test : all) {
                if ("prog1".equals(test.pgmName)
                    && "A".equals(test.parm1)
                    && "B".equals(test.parm2)
                    && "C".equals(test.parm3)
                    && "D".equals(test.parm4)
                    && "E".equals(test.parm5)
                    && "F".equals(test.parm6)
                    && "G".equals(test.parm7)
                    && "H".equals(test.parm8)
                    && "I".equals(test.parm9)
                    && date1.equals(test.parm10)) {

                    found1 = true;
                } else if ("prog2".equals(test.pgmName)
                           && "Z".equals(test.parm1)
                           && "Y".equals(test.parm2)
                           && "X".equals(test.parm3)
                           && "W".equals(test.parm4)
                           && "V".equals(test.parm5)
                           && "U".equals(test.parm6)
                           && "T".equals(test.parm7)
                           && "S".equals(test.parm8)
                           && "R".equals(test.parm9)
                           && date2.equals(test.parm10)) {

                    found2 = true;
                }
            }

            assertTrue(found1, "Parameters 1 not found");
            assertTrue(found2, "Parameters 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all parameters rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final RawParameters test = RawParametersLogic.query(cache, "prog1");

            assertNotNull(test, "null returned by query");

            final boolean found = "prog1".equals(test.pgmName)
                                  && "A".equals(test.parm1)
                                  && "B".equals(test.parm2)
                                  && "C".equals(test.parm3)
                                  && "D".equals(test.parm4)
                                  && "E".equals(test.parm5)
                                  && "F".equals(test.parm6)
                                  && "G".equals(test.parm7)
                                  && "H".equals(test.parm8)
                                  && "I".equals(test.parm9)
                                  && date1.equals(test.parm10);

            assertTrue(found, "Parameters not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying parameters record: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateParm1 results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            assertTrue(RawParametersLogic.updateParm1(cache, "prog1", "NewA"), "False result from updateParm1");

            final RawParameters test = RawParametersLogic.query(cache, "prog1");

            assertNotNull(test, "null returned by query");

            final boolean found = "prog1".equals(test.pgmName)
                                  && "NewA".equals(test.parm1)
                                  && "B".equals(test.parm2)
                                  && "C".equals(test.parm3)
                                  && "D".equals(test.parm4)
                                  && "E".equals(test.parm5)
                                  && "F".equals(test.parm6)
                                  && "G".equals(test.parm7)
                                  && "H".equals(test.parm8)
                                  && "I".equals(test.parm9)
                                  && date1.equals(test.parm10);

            assertTrue(found, "Parameters not valid after updateParm1");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying parameters record: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateParm2 results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            assertTrue(RawParametersLogic.updateParm2(cache, "prog1", "NewB"), "False result from updateParm2");

            final RawParameters test = RawParametersLogic.query(cache, "prog1");

            assertNotNull(test, "null returned by query");

            final boolean found = "prog1".equals(test.pgmName)
                                  && "NewA".equals(test.parm1)
                                  && "NewB".equals(test.parm2)
                                  && "C".equals(test.parm3)
                                  && "D".equals(test.parm4)
                                  && "E".equals(test.parm5)
                                  && "F".equals(test.parm6)
                                  && "G".equals(test.parm7)
                                  && "H".equals(test.parm8)
                                  && "I".equals(test.parm9)
                                  && date1.equals(test.parm10);

            assertTrue(found, "Parameters not valid after updateParm2");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying parameters record: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateParm10 results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            assertTrue(RawParametersLogic.updateParm10(cache, "prog1",
                    LocalDate.of(2022, 3, 4)), "False result from updateParm10");

            final RawParameters test = RawParametersLogic.query(cache, "prog1");

            assertNotNull(test, "null returned by query");

            final boolean found = "prog1".equals(test.pgmName)
                                  && "NewA".equals(test.parm1)
                                  && "NewB".equals(test.parm2)
                                  && "C".equals(test.parm3)
                                  && "D".equals(test.parm4)
                                  && "E".equals(test.parm5)
                                  && "F".equals(test.parm6)
                                  && "G".equals(test.parm7)
                                  && "H".equals(test.parm8)
                                  && "I".equals(test.parm9)
                                  && LocalDate.of(2022, 3, 4).equals(test.parm10);

            assertTrue(found, "Parameters not valid after updateParm10");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying parameters record: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0008() {

        final Cache cache = new Cache(profile);

        try {
            final RawParameters pgm1 = new RawParameters("prog1", "A", "B", "C", "D", "E", "F", "G", "H", "I",
                    date1);

            final boolean result = RawParametersLogic.delete(cache, pgm1);
            assertTrue(result, "delete returned false");

            final List<RawParameters> all = RawParametersLogic.queryAll(cache);

            assertEquals(1, all.size(), "Incorrect record count from queryAll after delete");

            boolean found2 = false;

            for (final RawParameters test : all) {
                if ("prog2".equals(test.pgmName)
                    && "Z".equals(test.parm1)
                    && "Y".equals(test.parm2)
                    && "X".equals(test.parm3)
                    && "W".equals(test.parm4)
                    && "V".equals(test.parm5)
                    && "U".equals(test.parm6)
                    && "T".equals(test.parm7)
                    && "S".equals(test.parm8)
                    && "R".equals(test.parm9)
                    && date2.equals(test.parm10)) {

                    found2 = true;
                    break;
                }
            }

            assertTrue(found2, "Parameters 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting users: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM parameters");
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

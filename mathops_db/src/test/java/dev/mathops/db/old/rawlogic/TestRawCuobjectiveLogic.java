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
import dev.mathops.db.old.rawrecord.RawCuobjective;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.type.TermKey;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawCuobjectiveLogic} class.
 */
final class TestRawCuobjectiveLogic {

    /** A term key used in test records. */
    private static final TermKey fa21 = new TermKey(ETermName.FALL, 2021);

    /** A term key used in test records. */
    private static final TermKey fa20 = new TermKey(ETermName.FALL, 2020);

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 9, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 10, 3);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 11, 4);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2020, 8, 1);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2021, 9, 9);

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
                stmt.executeUpdate("DELETE FROM cuobjective");
            }
            conn.commit();

            // 117 unit 1 objective 1 in FA21
            final RawCuobjective raw1 = new RawCuobjective(fa21, RawRecordConstants.M117, Integer.valueOf(1),
                    Integer.valueOf(1), "Lesson 1.1", "1.1", date1);

            // 117 unit 1 objective 2 in FA21
            final RawCuobjective raw2 = new RawCuobjective(fa21, RawRecordConstants.M117, Integer.valueOf(1),
                    Integer.valueOf(2), "Lesson 1.2", "1.2", date2);

            // 117 unit 2 objective 1 in FA21
            final RawCuobjective raw3 = new RawCuobjective(fa21, RawRecordConstants.M117, Integer.valueOf(2),
                    Integer.valueOf(1), "Lesson 2.1", "2.1", date3);

            // 117 unit 1 objective 3 in FA20
            final RawCuobjective raw4 = new RawCuobjective(fa20, RawRecordConstants.M117, Integer.valueOf(1),
                    Integer.valueOf(3), "Lesson 1.3", "1.3", date4);

            // 118 unit 1 objective 1 in FA21
            final RawCuobjective raw5 = new RawCuobjective(fa21, RawRecordConstants.M118, Integer.valueOf(1),
                    Integer.valueOf(1), "Lesson 1.1", "1.1", date5);

            assertTrue(RawCuobjectiveLogic.insert(cache, raw1), "Failed to insert cuobjective 1");
            assertTrue(RawCuobjectiveLogic.insert(cache, raw2), "Failed to insert cuobjective 2");
            assertTrue(RawCuobjectiveLogic.insert(cache, raw3), "Failed to insert cuobjective 3");
            assertTrue(RawCuobjectiveLogic.insert(cache, raw4), "Failed to insert cuobjective 4");
            assertTrue(RawCuobjectiveLogic.insert(cache, raw5), "Failed to insert cuobjective 5");
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
            final List<RawCuobjective> all = RawCuobjectiveLogic.queryAll(cache);

            assertEquals(5, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final RawCuobjective r : all) {

                if (fa21.equals(r.termKey)
                    && RawRecordConstants.M117.equals(r.course)
                    && Integer.valueOf(1).equals(r.unit)
                    && Integer.valueOf(1).equals(r.objective)
                    && "Lesson 1.1".equals(r.lessonId)
                    && "1.1".equals(r.lessonNbr)
                    && date1.equals(r.startDt)) {

                    found1 = true;
                } else if (fa21.equals(r.termKey)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(1).equals(r.unit)
                           && Integer.valueOf(2).equals(r.objective)
                           && "Lesson 1.2".equals(r.lessonId)
                           && "1.2".equals(r.lessonNbr)
                           && date2.equals(r.startDt)) {

                    found2 = true;
                } else if (fa21.equals(r.termKey)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(2).equals(r.unit)
                           && Integer.valueOf(1).equals(r.objective)
                           && "Lesson 2.1".equals(r.lessonId)
                           && "2.1".equals(r.lessonNbr)
                           && date3.equals(r.startDt)) {

                    found3 = true;
                } else if (fa20.equals(r.termKey)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(1).equals(r.unit)
                           && Integer.valueOf(3).equals(r.objective)
                           && "Lesson 1.3".equals(r.lessonId)
                           && "1.3".equals(r.lessonNbr)
                           && date4.equals(r.startDt)) {

                    found4 = true;
                } else if (fa21.equals(r.termKey)
                           && RawRecordConstants.M118.equals(r.course)
                           && Integer.valueOf(1).equals(r.unit)
                           && Integer.valueOf(1).equals(r.objective)
                           && "Lesson 1.1".equals(r.lessonId)
                           && "1.1".equals(r.lessonNbr)
                           && date5.equals(r.startDt)) {

                    found5 = true;
                }
            }

            assertTrue(found1, "cuobjective 1 not found");
            assertTrue(found2, "cuobjective 2 not found");
            assertTrue(found3, "cuobjective 3 not found");
            assertTrue(found4, "cuobjective 4 not found");
            assertTrue(found5, "cuobjective 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all cuobjective rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final RawCuobjective raw2 = new RawCuobjective(fa21, RawRecordConstants.M117, Integer.valueOf(1),
                    Integer.valueOf(2), "Lesson 1.2", "1.2", date2);

            final boolean result = RawCuobjectiveLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawCuobjective> all = RawCuobjectiveLogic.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;
            boolean found5 = false;

            for (final RawCuobjective r : all) {

                if (fa21.equals(r.termKey)
                    && RawRecordConstants.M117.equals(r.course)
                    && Integer.valueOf(1).equals(r.unit)
                    && Integer.valueOf(1).equals(r.objective)
                    && "Lesson 1.1".equals(r.lessonId)
                    && "1.1".equals(r.lessonNbr)
                    && date1.equals(r.startDt)) {

                    found1 = true;
                } else if (fa21.equals(r.termKey)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(2).equals(r.unit)
                           && Integer.valueOf(1).equals(r.objective)
                           && "Lesson 2.1".equals(r.lessonId)
                           && "2.1".equals(r.lessonNbr)
                           && date3.equals(r.startDt)) {

                    found3 = true;
                } else if (fa20.equals(r.termKey)
                           && RawRecordConstants.M117.equals(r.course)
                           && Integer.valueOf(1).equals(r.unit)
                           && Integer.valueOf(3).equals(r.objective)
                           && "Lesson 1.3".equals(r.lessonId)
                           && "1.3".equals(r.lessonNbr)
                           && date4.equals(r.startDt)) {

                    found4 = true;
                } else if (fa21.equals(r.termKey)
                           && RawRecordConstants.M118.equals(r.course)
                           && Integer.valueOf(1).equals(r.unit)
                           && Integer.valueOf(1).equals(r.objective)
                           && "Lesson 1.1".equals(r.lessonId)
                           && "1.1".equals(r.lessonNbr)
                           && date5.equals(r.startDt)) {

                    found5 = true;
                }
            }

            assertTrue(found1, "cuobjective 1 not found");
            assertTrue(found3, "cuobjective 3 not found");
            assertTrue(found4, "cuobjective 4 not found");
            assertTrue(found5, "cuobjective 5 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting cuobjective: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM cuobjective");
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

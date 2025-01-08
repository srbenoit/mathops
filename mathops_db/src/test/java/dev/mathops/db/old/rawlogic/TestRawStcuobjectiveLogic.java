package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcuobjective;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Tests for the {@code RawStcuobjectiveLogic} class.
 */
final class TestRawStcuobjectiveLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 1, 5);

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
                    stmt.executeUpdate("DELETE FROM stcuobjective");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawStcuobjective raw1 = new RawStcuobjective(
                        "111111111",
                        RawRecordConstants.M117,
                        Integer.valueOf(1),
                        Integer.valueOf(2),
                        date1,
                        Integer.valueOf(999),
                        Integer.valueOf(3));

                final RawStcuobjective raw2 = new RawStcuobjective(
                        "111111111",
                        RawRecordConstants.M117,
                        Integer.valueOf(1),
                        Integer.valueOf(3),
                        date2,
                        Integer.valueOf(998),
                        Integer.valueOf(4));

                final RawStcuobjective raw3 = new RawStcuobjective(
                        "222222222",
                        RawRecordConstants.M118,
                        Integer.valueOf(2),
                        Integer.valueOf(4),
                        null,
                        null,
                        null);

                assertTrue(RawStcuobjectiveLogic.insert(cache, raw1), "Failed to insert stcuobjective 1");
                assertTrue(RawStcuobjectiveLogic.insert(cache, raw2), "Failed to insert stcuobjective 2");
                assertTrue(RawStcuobjectiveLogic.insert(cache, raw3), "Failed to insert stcuobjective 3");
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
                final List<RawStcuobjective> all = RawStcuobjectiveLogic.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawStcuobjective test : all) {
                    if ("111111111".equals(test.stuId)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && Integer.valueOf(2).equals(test.objective)
                            && date1.equals(test.lectureViewedDt)
                            && Integer.valueOf(999).equals(test.seed)
                            && Integer.valueOf(3).equals(test.lastComponentFinished)) {

                        found1 = true;
                    } else if ("111111111".equals(test.stuId)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && Integer.valueOf(3).equals(test.objective)
                            && date2.equals(test.lectureViewedDt)
                            && Integer.valueOf(998).equals(test.seed)
                            && Integer.valueOf(4).equals(test.lastComponentFinished)) {

                        found2 = true;
                    } else if ("222222222".equals(test.stuId)
                            && RawRecordConstants.M118.equals(test.course)
                            && Integer.valueOf(2).equals(test.unit)
                            && Integer.valueOf(4).equals(test.objective)
                            && test.lectureViewedDt == null
                            && test.seed == null
                            && test.lastComponentFinished == null) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected objective ", test.objective);
                        Log.warning("Unexpected lectureViewedDt ", test.lectureViewedDt);
                        Log.warning("Unexpected seed ", test.seed);
                        Log.warning("Unexpected lastComponentFinished ", test.lastComponentFinished);
                    }
                }

                assertTrue(found1, "Stcuobjective 1 not found");
                assertTrue(found2, "Stcuobjective 2 not found");
                assertTrue(found3, "Stcuobjective 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stcuobjective rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawStcuobjective test = RawStcuobjectiveLogic.query(cache, "111111111", RawRecordConstants.M117,
                        Integer.valueOf(1), Integer.valueOf(2));

                assertNotNull(test, "Null result from query");

                final boolean found = "111111111".equals(test.stuId)
                        && RawRecordConstants.M117.equals(test.course)
                        && Integer.valueOf(1).equals(test.unit)
                        && Integer.valueOf(2).equals(test.objective)
                        && date1.equals(test.lectureViewedDt)
                        && Integer.valueOf(999).equals(test.seed)
                        && Integer.valueOf(3).equals(test.lastComponentFinished);

                assertTrue(found, "Stcuobjective not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stcuobjectiv row: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("hasLectureBeenViewed results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final boolean test1 = RawStcuobjectiveLogic.hasLectureBeenViewed(cache, "111111111",
                        RawRecordConstants.M117, Integer.valueOf(1), Integer.valueOf(2));
                final boolean test2 = RawStcuobjectiveLogic.hasLectureBeenViewed(cache, "222222222",
                        RawRecordConstants.M118, Integer.valueOf(2), Integer.valueOf(4));

                assertTrue(test1, "Incorrect false from hasLectureBeenViewed");
                assertFalse(test2, "Incorrect true from hasLectureBeenViewed");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while testing for lecture view: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("recordLectureView results")
    void test0006() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final ZonedDateTime now = ZonedDateTime.now();

                final boolean test1 = RawStcuobjectiveLogic.recordLectureView(cache, "222222222",
                        RawRecordConstants.M118, Integer.valueOf(2), Integer.valueOf(4), now);

                assertTrue(test1, "False from recordLectureView");

                final RawStcuobjective test = RawStcuobjectiveLogic.query(cache, "222222222", RawRecordConstants.M118,
                        Integer.valueOf(2), Integer.valueOf(4));

                assertNotNull(test, "Null result from query");

                final boolean found = "222222222".equals(test.stuId)
                        && RawRecordConstants.M118.equals(test.course)
                        && Integer.valueOf(2).equals(test.unit)
                        && Integer.valueOf(4).equals(test.objective)
                        && now.toLocalDate().equals(test.lectureViewedDt)
                        && test.seed == null
                        && test.lastComponentFinished == null;

                assertTrue(found, "Stcuobjective not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while recording lecture view: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0007() {

        final ZonedDateTime now = ZonedDateTime.now();

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawStcuobjective raw2 = new RawStcuobjective(
                        "111111111",
                        RawRecordConstants.M117,
                        Integer.valueOf(1),
                        Integer.valueOf(3),
                        date2,
                        Integer.valueOf(998),
                        Integer.valueOf(4));

                final boolean result = RawStcuobjectiveLogic.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawStcuobjective> all = RawStcuobjectiveLogic.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawStcuobjective test : all) {
                    if ("111111111".equals(test.stuId)
                            && RawRecordConstants.M117.equals(test.course)
                            && Integer.valueOf(1).equals(test.unit)
                            && Integer.valueOf(2).equals(test.objective)
                            && date1.equals(test.lectureViewedDt)
                            && Integer.valueOf(999).equals(test.seed)
                            && Integer.valueOf(3).equals(test.lastComponentFinished)) {

                        found1 = true;
                    } else if ("222222222".equals(test.stuId)
                            && RawRecordConstants.M118.equals(test.course)
                            && Integer.valueOf(2).equals(test.unit)
                            && Integer.valueOf(4).equals(test.objective)
                            && now.toLocalDate().equals(test.lectureViewedDt)
                            && test.seed == null
                            && test.lastComponentFinished == null) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected course ", test.course);
                        Log.warning("Unexpected unit ", test.unit);
                        Log.warning("Unexpected objective ", test.objective);
                        Log.warning("Unexpected lectureViewedDt ", test.lectureViewedDt);
                        Log.warning("Unexpected seed ", test.seed);
                        Log.warning("Unexpected lastComponentFinished ", test.lastComponentFinished);
                    }
                }

                assertTrue(found1, "Stcuobjective 1 not found");
                assertTrue(found3, "Stcuobjective 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting users: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM stcuobjective");
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

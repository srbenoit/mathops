package dev.mathops.db.rawlogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.cfg.ContextMap;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.rawrecord.RawStsurveyqa;

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
 * Tests for the {@code RawStsurveyqa} classes.
 */
final class TestRawStsurveyqaLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 5, 6);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 7, 8);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 9, 10);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2022, 1, 1);

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
                    stmt.executeUpdate("DELETE FROM xxxxxxxxxxxxx");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);
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
    @DisplayName("")
    void test0001() {

        try {
            final DbConnection conn = this.ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM stsurveyqa");
                }

                conn.commit();

            } finally {
                this.ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        }

        return "Cleaned 'stsurveyqa' table";
    }

    /** Test case. */
    @Test
    @DisplayName("")
    void test0002() {

        try {
            final DbConnection conn = this.ctx.checkOutConnection();
            final Cache cache = new Cache(this.dbProfile, conn);

            try {
                final RawStsurveyqa raw1 = new RawStsurveyqa("111111111", "POOOO", date1, Integer.valueOf(1), "A",
                        Integer.valueOf(100));

                final RawStsurveyqa raw2 = new RawStsurveyqa("111111111", "POOOO", date1, Integer.valueOf(2), "B",
                        Integer.valueOf(110));

                final RawStsurveyqa raw3 = new RawStsurveyqa("222222222", "POOOO", date2, Integer.valueOf(1), "Z",
                        Integer.valueOf(900));

                final RawStsurveyqa raw4 = new RawStsurveyqa("222222222", "UOOOO", date3, Integer.valueOf(5), "Q",
                        Integer.valueOf(800));

                final RawStsurveyqa raw5 = new RawStsurveyqa("111111111", "POOOO", date4, Integer.valueOf(2), "C",
                        Integer.valueOf(357));

                final RawStsurveyqa raw6 = new RawStsurveyqa("222222222", "UOOOO", date3, Integer.valueOf(5), "S",
                        Integer.valueOf(700));

                assertTrue(RawStsurveyqaLogic.INSTANCE.insert(cache, raw1), "Failed to insert stsurveyqa 1");
                assertTrue(RawStsurveyqaLogic.INSTANCE.insert(cache, raw2), "Failed to insert stsurveyqa 2");
                assertTrue(RawStsurveyqaLogic.INSTANCE.insert(cache, raw3), "Failed to insert stsurveyqa 3");
                assertTrue(RawStsurveyqaLogic.INSTANCE.insert(cache, raw4), "Failed to insert stsurveyqa 4");
                assertTrue(RawStsurveyqaLogic.INSTANCE.insert(cache, raw5), "Failed to insert stsurveyqa 5");
                assertTrue(RawStsurveyqaLogic.INSTANCE.insert(cache, raw6), "Failed to insert stsurveyqa 6");

            } finally {
                this.ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while inserting stsurveyqa rows: " + ex.getMessage());
        }

        return "Inserted all test 'stsurveyqa' records";
    }

    /** Test case. */
    @Test
    @DisplayName("")
    void test0003() {

        try {
            final DbConnection conn = this.ctx.checkOutConnection();
            final Cache cache = new Cache(this.dbProfile, conn);

            try {
                final List<RawStsurveyqa> all = RawStsurveyqaLogic.INSTANCE.queryAll(cache);

                assertEquals(6, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;

                for (final RawStsurveyqa test : all) {

                    if ("111111111".equals(test.stuId)
                            && "POOOO".equals(test.version)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(1).equals(test.surveyNbr)
                            && "A".equals(test.stuAnswer)
                            && Integer.valueOf(100).equals(test.finishTime)) {

                        found1 = true;
                    } else if ("111111111".equals(test.stuId)
                            && "POOOO".equals(test.version)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(2).equals(test.surveyNbr)
                            && "B".equals(test.stuAnswer)
                            && Integer.valueOf(110).equals(test.finishTime)) {

                        found2 = true;
                    } else if ("222222222".equals(test.stuId)
                            && "POOOO".equals(test.version)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(1).equals(test.surveyNbr)
                            && "Z".equals(test.stuAnswer)
                            && Integer.valueOf(900).equals(test.finishTime)) {

                        found3 = true;
                    } else if ("222222222".equals(test.stuId)
                            && "UOOOO".equals(test.version)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(5).equals(test.surveyNbr)
                            && "Q".equals(test.stuAnswer)
                            && Integer.valueOf(800).equals(test.finishTime)) {

                        found4 = true;
                    } else if ("111111111".equals(test.stuId)
                            && "POOOO".equals(test.version)
                            && date4.equals(test.examDt)
                            && Integer.valueOf(2).equals(test.surveyNbr)
                            && "C".equals(test.stuAnswer)
                            && Integer.valueOf(357).equals(test.finishTime)) {

                        found5 = true;
                    } else if ("222222222".equals(test.stuId)
                            && "UOOOO".equals(test.version)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(5).equals(test.surveyNbr)
                            && "S".equals(test.stuAnswer)
                            && Integer.valueOf(700).equals(test.finishTime)) {

                        found6 = true;

                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected surveyNbr ", test.surveyNbr);
                        Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                    }
                }

                assertTrue(found1, "Stsurveyqa 1 not found");
                assertTrue(found2, "Stsurveyqa 2 not found");
                assertTrue(found3, "Stsurveyqa 3 not found");
                assertTrue(found4, "Stsurveyqa 4 not found");
                assertTrue(found5, "Stsurveyqa 5 not found");
                assertTrue(found6, "Stsurveyqa 6 not found");

            } finally {
                this.ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all surveyqa rows: " + ex.getMessage());
        }

        return "queryAll results were correct";
    }

    /** Test case. */
    @Test
    @DisplayName("")
    void test0004() {

        try {
            final DbConnection conn = this.ctx.checkOutConnection();
            final Cache cache = new Cache(this.dbProfile, conn);

            try {
                final List<RawStsurveyqa> all =
                        RawStsurveyqaLogic.queryLatestByStudentProfile(cache, "111111111", "POOOO");

                assertEquals(2, all.size(), "Incorrect record count from queryLatestByStudentProfile");

                boolean found1 = false;
                boolean found5 = false;

                for (final RawStsurveyqa test : all) {

                    if ("111111111".equals(test.stuId)
                            && "POOOO".equals(test.version)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(1).equals(test.surveyNbr)
                            && "A".equals(test.stuAnswer)
                            && Integer.valueOf(100).equals(test.finishTime)) {

                        found1 = true;
                    } else if ("111111111".equals(test.stuId)
                            && "POOOO".equals(test.version)
                            && date4.equals(test.examDt)
                            && Integer.valueOf(2).equals(test.surveyNbr)
                            && "C".equals(test.stuAnswer)
                            && Integer.valueOf(357).equals(test.finishTime)) {

                        found5 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected surveyNbr ", test.surveyNbr);
                        Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                    }
                }

                assertTrue(found1, "Stsurveyqa 1 not found");
                assertTrue(found5, "Stsurveyqa 5 not found");

            } finally {
                this.ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying latest stsurveyqa by student, version: " + ex.getMessage());
        }

        return "queryLatestByStudentProfile results were correct";
    }

    /** Test case. */
    @Test
    @DisplayName("")
    void test0005() {

        try {
            final DbConnection conn = this.ctx.checkOutConnection();
            final Cache cache = new Cache(this.dbProfile, conn);

            try {
                final List<RawStsurveyqa> all = RawStsurveyqaLogic.queryLatestByStudentProfile(cache, "222222222",
                        "UOOOO");

                assertEquals(1, all.size(), "Incorrect record count from queryLatestByStudentProfile");

                boolean found4 = false;

                for (final RawStsurveyqa test : all) {
                    if ("222222222".equals(test.stuId)
                            && "UOOOO".equals(test.version)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(5).equals(test.surveyNbr)
                            && "Q".equals(test.stuAnswer)
                            && Integer.valueOf(800).equals(test.finishTime)) {

                        found4 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected surveyNbr ", test.surveyNbr);
                        Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                    }
                }

                assertTrue(found4, "Stsurveyqa 4 not found");

            } finally {
                this.ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying latest stsurveyqa by student, version: " + ex.getMessage());
        }

        return "queryLatestByStudentProfile results were correct";
    }

    /** Test case. */
    @Test
    @DisplayName("")
    void test0006() {

        try {
            final DbConnection conn = this.ctx.checkOutConnection();
            final Cache cache = new Cache(this.dbProfile, conn);

            try {
                final RawStsurveyqa raw2 = new RawStsurveyqa("111111111", "POOOO", date1,
                        Integer.valueOf(2), "B", Integer.valueOf(110));

                final boolean result = RawStsurveyqaLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawStsurveyqa> all = RawStsurveyqaLogic.INSTANCE.queryAll(cache);

                assertEquals(5, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;
                boolean found6 = false;

                for (final RawStsurveyqa test : all) {

                    if ("111111111".equals(test.stuId)
                            && "POOOO".equals(test.version)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(1).equals(test.surveyNbr)
                            && "A".equals(test.stuAnswer)
                            && Integer.valueOf(100).equals(test.finishTime)) {

                        found1 = true;
                    } else if ("222222222".equals(test.stuId)
                            && "POOOO".equals(test.version)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(1).equals(test.surveyNbr)
                            && "Z".equals(test.stuAnswer)
                            && Integer.valueOf(900).equals(test.finishTime)) {

                        found3 = true;
                    } else if ("222222222".equals(test.stuId)
                            && "UOOOO".equals(test.version)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(5).equals(test.surveyNbr)
                            && "Q".equals(test.stuAnswer)
                            && Integer.valueOf(800).equals(test.finishTime)) {

                        found4 = true;
                    } else if ("111111111".equals(test.stuId)
                            && "POOOO".equals(test.version)
                            && date4.equals(test.examDt)
                            && Integer.valueOf(2).equals(test.surveyNbr)
                            && "C".equals(test.stuAnswer)
                            && Integer.valueOf(357).equals(test.finishTime)) {

                        found5 = true;
                    } else if ("222222222".equals(test.stuId)
                            && "UOOOO".equals(test.version)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(5).equals(test.surveyNbr)
                            && "S".equals(test.stuAnswer)
                            && Integer.valueOf(700).equals(test.finishTime)) {

                        found6 = true;

                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected surveyNbr ", test.surveyNbr);
                        Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                    }
                }

                assertTrue(found1, "Stsurveyqa 1 not found");
                assertTrue(found3, "Stsurveyqa 3 not found");
                assertTrue(found4, "Stsurveyqa 4 not found");
                assertTrue(found5, "Stsurveyqa 5 not found");
                assertTrue(found6, "Stsurveyqa 6 not found");

            } finally {
                this.ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting stsurveyqa: " + ex.getMessage());
        }

        return "delete results were correct";
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = this.ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM stsurveyqa");
                }

                conn.commit();
            } finally {
                this.ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        }
    }
}

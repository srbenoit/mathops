package dev.mathops.db.old.rawlogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawStmathplan;

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
 * Tests for the {@code RawStmathplanLogic} class.
 */
final class TestRawStmathplanLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 12, 1);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 12, 2);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 12, 3);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2021, 12, 4);

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
                    stmt.executeUpdate("DELETE FROM stmathplan");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawStmathplan raw1 = new RawStmathplan("111111111",
                        Integer.valueOf(123456),
                        "FA21",
                        "AAAAA",
                        date1,
                        Integer.valueOf(1),
                        "Answer1",
                        Integer.valueOf(123),
                        Long.valueOf(1234567890L));

                final RawStmathplan raw2 = new RawStmathplan("111111111",
                        Integer.valueOf(123456),
                        "FA21",
                        "BBBBB",
                        date2,
                        Integer.valueOf(2),
                        "Answer2",
                        Integer.valueOf(456),
                        Long.valueOf(1234567891L));

                final RawStmathplan raw3 = new RawStmathplan("222222222",
                        Integer.valueOf(654321),
                        "SP22",
                        "CCCCC",
                        date3,
                        Integer.valueOf(3),
                        "Answer3",
                        Integer.valueOf(789),
                        Long.valueOf(1234567892L));

                final RawStmathplan raw4 = new RawStmathplan("111111111",
                        Integer.valueOf(123456),
                        "FA21",
                        "AAAAA",
                        date4,
                        Integer.valueOf(1),
                        "Answer4",
                        Integer.valueOf(987),
                        Long.valueOf(1234567893L));

                assertTrue(RawStmathplanLogic.INSTANCE.insert(cache, raw1), "Failed to insert stmathplan 1");
                assertTrue(RawStmathplanLogic.INSTANCE.insert(cache, raw2), "Failed to insert stmathplan 2");
                assertTrue(RawStmathplanLogic.INSTANCE.insert(cache, raw3), "Failed to insert stmathplan 3");
                assertTrue(RawStmathplanLogic.INSTANCE.insert(cache, raw4), "Failed to insert stmathplan 4");
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
                final List<RawStmathplan> all = RawStmathplanLogic.INSTANCE.queryAll(cache);

                assertEquals(4, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final RawStmathplan test : all) {
                    if ("111111111".equals(test.stuId)
                            && Integer.valueOf(123456).equals(test.pidm)
                            && "FA21".equals(test.aplnTerm)
                            && "AAAAA".equals(test.version)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(1).equals(test.surveyNbr)
                            && "Answer1".equals(test.stuAnswer)
                            && Integer.valueOf(123).equals(test.finishTime)
                            && Long.valueOf(1234567890L).equals(test.session)) {

                        found1 = true;
                    } else if ("111111111".equals(test.stuId)
                            && Integer.valueOf(123456).equals(test.pidm)
                            && "FA21".equals(test.aplnTerm)
                            && "BBBBB".equals(test.version)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(2).equals(test.surveyNbr)
                            && "Answer2".equals(test.stuAnswer)
                            && Integer.valueOf(456).equals(test.finishTime)
                            && Long.valueOf(1234567891L).equals(test.session)) {

                        found2 = true;
                    } else if ("222222222".equals(test.stuId)
                            && Integer.valueOf(654321).equals(test.pidm)
                            && "SP22".equals(test.aplnTerm)
                            && "CCCCC".equals(test.version)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(3).equals(test.surveyNbr)
                            && "Answer3".equals(test.stuAnswer)
                            && Integer.valueOf(789).equals(test.finishTime)
                            && Long.valueOf(1234567892L).equals(test.session)) {

                        found3 = true;
                    } else if ("111111111".equals(test.stuId)
                            && Integer.valueOf(123456).equals(test.pidm)
                            && "FA21".equals(test.aplnTerm)
                            && "AAAAA".equals(test.version)
                            && date4.equals(test.examDt)
                            && Integer.valueOf(1).equals(test.surveyNbr)
                            && "Answer4".equals(test.stuAnswer)
                            && Integer.valueOf(987).equals(test.finishTime)
                            && Long.valueOf(1234567893L).equals(test.session)) {

                        found4 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected pidm ", test.pidm);
                        Log.warning("Unexpected aplnTerm ", test.aplnTerm);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected surveyNbr ", test.surveyNbr);
                        Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected session ", test.session);
                    }
                }

                assertTrue(found1, "Stmathplan 1 not found");
                assertTrue(found2, "Stmathplan 2 not found");
                assertTrue(found3, "Stmathplan 3 not found");
                assertTrue(found4, "Stmathplan 4 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stmathplan rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryLatestByStudentPage(String) results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStmathplan> all = RawStmathplanLogic.queryLatestByStudentPage(cache, "111111111",
                        "AAAAA");

                assertEquals(1, all.size(), "Incorrect record count from queryLatestByStudentPage");

                boolean found = false;

                for (final RawStmathplan test : all) {
                    if ("111111111".equals(test.stuId)
                            && Integer.valueOf(123456).equals(test.pidm)
                            && "FA21".equals(test.aplnTerm)
                            && "AAAAA".equals(test.version)
                            && date4.equals(test.examDt)
                            && Integer.valueOf(1).equals(test.surveyNbr)
                            && "Answer4".equals(test.stuAnswer)
                            && Integer.valueOf(987).equals(test.finishTime)
                            && Long.valueOf(1234567893L).equals(test.session)) {

                        found = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected pidm ", test.pidm);
                        Log.warning("Unexpected aplnTerm ", test.aplnTerm);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected surveyNbr ", test.surveyNbr);
                        Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected session ", test.session);
                    }
                }

                assertTrue(found, "Stmathplan 1 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying latest stmathplan by page: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryLatestByStudentPage(int) results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStmathplan> all = RawStmathplanLogic.queryLatestByStudentPage(cache,
                        Integer.valueOf(123456), "AAAAA");

                assertEquals(1, all.size(), "Incorrect record count from queryLatestByStudentPage");

                boolean found = false;

                for (final RawStmathplan test : all) {
                    if ("111111111".equals(test.stuId)
                            && Integer.valueOf(123456).equals(test.pidm)
                            && "FA21".equals(test.aplnTerm)
                            && "AAAAA".equals(test.version)
                            && date4.equals(test.examDt)
                            && Integer.valueOf(1).equals(test.surveyNbr)
                            && "Answer4".equals(test.stuAnswer)
                            && Integer.valueOf(987).equals(test.finishTime)
                            && Long.valueOf(1234567893L).equals(test.session)) {

                        found = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected pidm ", test.pidm);
                        Log.warning("Unexpected aplnTerm ", test.aplnTerm);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected surveyNbr ", test.surveyNbr);
                        Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected session ", test.session);
                    }
                }

                assertTrue(found, "Stmathplan 1 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying latest stmathplan by page: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0006() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStmathplan> all = RawStmathplanLogic.queryByStudent(cache, "111111111");

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found4 = false;

                for (final RawStmathplan test : all) {
                    if ("111111111".equals(test.stuId)
                            && Integer.valueOf(123456).equals(test.pidm)
                            && "FA21".equals(test.aplnTerm)
                            && "AAAAA".equals(test.version)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(1).equals(test.surveyNbr)
                            && "Answer1".equals(test.stuAnswer)
                            && Integer.valueOf(123).equals(test.finishTime)
                            && Long.valueOf(1234567890L).equals(test.session)) {

                        found1 = true;
                    } else if ("111111111".equals(test.stuId)
                            && Integer.valueOf(123456).equals(test.pidm)
                            && "FA21".equals(test.aplnTerm)
                            && "BBBBB".equals(test.version)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(2).equals(test.surveyNbr)
                            && "Answer2".equals(test.stuAnswer)
                            && Integer.valueOf(456).equals(test.finishTime)
                            && Long.valueOf(1234567891L).equals(test.session)) {

                        found2 = true;
                    } else if ("111111111".equals(test.stuId)
                            && Integer.valueOf(123456).equals(test.pidm)
                            && "FA21".equals(test.aplnTerm)
                            && "AAAAA".equals(test.version)
                            && date4.equals(test.examDt)
                            && Integer.valueOf(1).equals(test.surveyNbr)
                            && "Answer4".equals(test.stuAnswer)
                            && Integer.valueOf(987).equals(test.finishTime)
                            && Long.valueOf(1234567893L).equals(test.session)) {

                        found4 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected pidm ", test.pidm);
                        Log.warning("Unexpected aplnTerm ", test.aplnTerm);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected surveyNbr ", test.surveyNbr);
                        Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected session ", test.session);
                    }
                }

                assertTrue(found1, "Stmathplan 1 not found");
                assertTrue(found2, "Stmathplan 2 not found");
                assertTrue(found4, "Stmathplan 4 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying latest stmathplan by page: " + ex.getMessage());
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
                final RawStmathplan raw2 = new RawStmathplan("111111111",
                        Integer.valueOf(123456),
                        "FA21",
                        "BBBBB",
                        date2,
                        Integer.valueOf(2),
                        "Answer2",
                        Integer.valueOf(456),
                        Long.valueOf(1234567891L));

                final boolean result = RawStmathplanLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawStmathplan> all = RawStmathplanLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final RawStmathplan test : all) {
                    if ("111111111".equals(test.stuId)
                            && Integer.valueOf(123456).equals(test.pidm)
                            && "FA21".equals(test.aplnTerm)
                            && "AAAAA".equals(test.version)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(1).equals(test.surveyNbr)
                            && "Answer1".equals(test.stuAnswer)
                            && Integer.valueOf(123).equals(test.finishTime)
                            && Long.valueOf(1234567890L).equals(test.session)) {

                        found1 = true;
                    } else if ("222222222".equals(test.stuId)
                            && Integer.valueOf(654321).equals(test.pidm)
                            && "SP22".equals(test.aplnTerm)
                            && "CCCCC".equals(test.version)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(3).equals(test.surveyNbr)
                            && "Answer3".equals(test.stuAnswer)
                            && Integer.valueOf(789).equals(test.finishTime)
                            && Long.valueOf(1234567892L).equals(test.session)) {

                        found3 = true;
                    } else if ("111111111".equals(test.stuId)
                            && Integer.valueOf(123456).equals(test.pidm)
                            && "FA21".equals(test.aplnTerm)
                            && "AAAAA".equals(test.version)
                            && date4.equals(test.examDt)
                            && Integer.valueOf(1).equals(test.surveyNbr)
                            && "Answer4".equals(test.stuAnswer)
                            && Integer.valueOf(987).equals(test.finishTime)
                            && Long.valueOf(1234567893L).equals(test.session)) {

                        found4 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected pidm ", test.pidm);
                        Log.warning("Unexpected aplnTerm ", test.aplnTerm);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected surveyNbr ", test.surveyNbr);
                        Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected session ", test.session);
                    }
                }

                assertTrue(found1, "Stmathplan 1 not found");
                assertTrue(found3, "Stmathplan 3 not found");
                assertTrue(found4, "Stmathplan 4 not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting stmathplan: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM stmathplan");
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

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
import dev.mathops.db.rawrecord.RawUsers;

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
 * Tests for the {@code RawUsersLogic} class.
 */
final class TestRawUsersLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 1);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 2, 2);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 3, 3);

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
                    stmt.executeUpdate("DELETE FROM users");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawUsers raw1 = new RawUsers(new TermKey("FA21"), "111111111", Long.valueOf(1000L), "UOOOO",
                        date1, Integer.valueOf(2), "CA", "N");

                final RawUsers raw2 = new RawUsers(new TermKey("FA21"), "111111111", Long.valueOf(2000L), "UPPPP",
                        date2, Integer.valueOf(3), "CB", "Y");

                final RawUsers raw3 = new RawUsers(new TermKey("SP22"), "222222222", Long.valueOf(3000L), "UQQQQ",
                        date3, Integer.valueOf(4), "CC", "P");

                assertTrue(RawUsersLogic.INSTANCE.insert(cache, raw1), "Failed to insert users 1");
                assertTrue(RawUsersLogic.INSTANCE.insert(cache, raw2), "Failed to insert users 2");
                assertTrue(RawUsersLogic.INSTANCE.insert(cache, raw3), "Failed to insert users 3");
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
                final List<RawUsers> all = RawUsersLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawUsers test : all) {

                    if ("111111111".equals(test.stuId)
                            && "FA21".equals(test.termKey.shortString)
                            && Long.valueOf(1000L).equals(test.serialNbr)
                            && "UOOOO".equals(test.version)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(2).equals(test.examScore)
                            && "CA".equals(test.calcCourse)
                            && "N".equals(test.passed)) {

                        found1 = true;

                    } else if ("111111111".equals(test.stuId)
                            && "FA21".equals(test.termKey.shortString)
                            && Long.valueOf(2000L).equals(test.serialNbr)
                            && "UPPPP".equals(test.version)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(3).equals(test.examScore)
                            && "CB".equals(test.calcCourse)
                            && "Y".equals(test.passed)) {

                        found2 = true;

                    } else if ("222222222".equals(test.stuId)
                            && "SP22".equals(test.termKey.shortString)
                            && Long.valueOf(3000L).equals(test.serialNbr)
                            && "UQQQQ".equals(test.version)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(4).equals(test.examScore)
                            && "CC".equals(test.calcCourse)
                            && "P".equals(test.passed)) {

                        found3 = true;

                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected term ", test.termKey.shortString);
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected calcCourse ", test.calcCourse);
                        Log.warning("Unexpected passed ", test.passed);
                    }
                }

                assertTrue(found1, "Users 1 not found");
                assertTrue(found2, "Users 2 not found");
                assertTrue(found3, "Users 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all users rows: " + ex.getMessage());
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
                final List<RawUsers> all = RawUsersLogic.queryByStudent(cache, "111111111");

                assertEquals(2, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;

                for (final RawUsers test : all) {

                    if ("111111111".equals(test.stuId)
                            && "FA21".equals(test.termKey.shortString)
                            && Long.valueOf(1000L).equals(test.serialNbr)
                            && "UOOOO".equals(test.version)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(2).equals(test.examScore)
                            && "CA".equals(test.calcCourse)
                            && "N".equals(test.passed)) {

                        found1 = true;

                    } else if ("111111111".equals(test.stuId)
                            && "FA21".equals(test.termKey.shortString)
                            && Long.valueOf(2000L).equals(test.serialNbr)
                            && "UPPPP".equals(test.version)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(3).equals(test.examScore)
                            && "CB".equals(test.calcCourse)
                            && "Y".equals(test.passed)) {

                        found2 = true;

                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected term ", test.termKey.shortString);
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected calcCourse ", test.calcCourse);
                        Log.warning("Unexpected passed ", test.passed);
                    }
                }

                assertTrue(found1, "Users 1 not found");
                assertTrue(found2, "Users 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all users rows for student: " + ex.getMessage());
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
                final RawUsers raw2 = new RawUsers(new TermKey("FA21"), "111111111", Long.valueOf(2000L), "UPPPP",
                        date2, Integer.valueOf(3), "CB", "Y");

                final boolean result = RawUsersLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawUsers> all = RawUsersLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawUsers test : all) {

                    if ("111111111".equals(test.stuId)
                            && "FA21".equals(test.termKey.shortString)
                            && Long.valueOf(1000L).equals(test.serialNbr)
                            && "UOOOO".equals(test.version)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(2).equals(test.examScore)
                            && "CA".equals(test.calcCourse)
                            && "N".equals(test.passed)) {

                        found1 = true;

                    } else if ("222222222".equals(test.stuId)
                            && "SP22".equals(test.termKey.shortString)
                            && Long.valueOf(3000L).equals(test.serialNbr)
                            && "UQQQQ".equals(test.version)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(4).equals(test.examScore)
                            && "CC".equals(test.calcCourse)
                            && "P".equals(test.passed)) {

                        found3 = true;

                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected term ", test.termKey.shortString);
                        Log.warning("Unexpected serialNbr ", test.serialNbr);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected examScore ", test.examScore);
                        Log.warning("Unexpected calcCourse ", test.calcCourse);
                        Log.warning("Unexpected passed ", test.passed);
                    }
                }

                assertTrue(found1, "users 1 not found");
                assertTrue(found3, "users 3 not found");

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
                    stmt.executeUpdate("DELETE FROM users");
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

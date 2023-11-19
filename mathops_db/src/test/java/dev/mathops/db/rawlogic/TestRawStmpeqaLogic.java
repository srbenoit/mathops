package dev.mathops.db.rawlogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.DbContext;
import dev.mathops.db.cfg.ContextMap;
import dev.mathops.db.cfg.DbProfile;
import dev.mathops.db.cfg.ESchemaUse;
import dev.mathops.db.rawrecord.RawStmpe;
import dev.mathops.db.rawrecord.RawStmpeqa;

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
 * Tests for the {@code RawStmpeqaLogic} class.
 */
final class TestRawStmpeqaLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 12, 1);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2022, 11, 2);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2023, 10, 3);

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
                    stmt.executeUpdate("DELETE FROM stmpeqa");
                }
                conn.commit();

            } finally {
                this.ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning table: " + ex.getMessage());
        }

        return "Cleaned 'stmpeqa' table";
    }

    /** Test case. */
    @Test
    @DisplayName("")
    void test0002() {

        try {
            final DbConnection conn = this.ctx.checkOutConnection();
            final Cache cache = new Cache(this.dbProfile, conn);

            try {
                final RawStmpeqa raw1 = new RawStmpeqa("111111111",
                        "AAAAA",
                        date1,
                        Integer.valueOf(123456),
                        Integer.valueOf(1),
                        "Ans1",
                        "N",
                        "z",
                        "Tree.ref.1");

                final RawStmpeqa raw2 = new RawStmpeqa("111111111",
                        "AAAAA",
                        date1,
                        Integer.valueOf(123456),
                        Integer.valueOf(2),
                        "Ans2",
                        "Y",
                        "y",
                        "Tree.ref.2");

                final RawStmpeqa raw3 = new RawStmpeqa("222222222",
                        "BBBBB",
                        date2,
                        Integer.valueOf(222333),
                        Integer.valueOf(3),
                        "Ans3",
                        "A",
                        "x",
                        "Tree.ref.3");

                final RawStmpeqa raw4 = new RawStmpeqa("333333333",
                        "CCCCC",
                        date3,
                        Integer.valueOf(333444),
                        Integer.valueOf(4),
                        "Ans4",
                        "B",
                        "w",
                        "Tree.ref.4");

                assertTrue(RawStmpeqaLogic.INSTANCE.insert(cache, raw1), "Failed to insert stmpeqa 1");
                assertTrue(RawStmpeqaLogic.INSTANCE.insert(cache, raw2), "Failed to insert stmpeqa 2");
                assertTrue(RawStmpeqaLogic.INSTANCE.insert(cache, raw3), "Failed to insert stmpeqa 3");
                assertTrue(RawStmpeqaLogic.INSTANCE.insert(cache, raw4), "Failed to insert stmpeqa 4");

                conn.commit();

            } finally {
                this.ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while inserting stmpeqa rows: " + ex.getMessage());
        }

        return "Inserted all test 'stmpeqa' records";
    }

    /** Test case. */
    @Test
    @DisplayName("")
    void test0003() {

        try {
            final DbConnection conn = this.ctx.checkOutConnection();
            final Cache cache = new Cache(this.dbProfile, conn);

            try {
                final List<RawStmpeqa> all = RawStmpeqaLogic.INSTANCE.queryAll(cache);

                assertEquals(4, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final RawStmpeqa test : all) {
                    if ("111111111".equals(test.stuId)
                            && "AAAAA".equals(test.version)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(123456).equals(test.finishTime)
                            && Integer.valueOf(1).equals(test.questionNbr)
                            && "Ans1".equals(test.stuAnswer)
                            && "N".equals(test.ansCorrect)
                            && "z".equals(test.subtest)
                            && "Tree.ref.1".equals(test.treeRef)) {

                        found1 = true;
                    } else if ("111111111".equals(test.stuId)
                            && "AAAAA".equals(test.version)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(123456).equals(test.finishTime)
                            && Integer.valueOf(2).equals(test.questionNbr)
                            && "Ans2".equals(test.stuAnswer)
                            && "Y".equals(test.ansCorrect)
                            && "y".equals(test.subtest)
                            && "Tree.ref.2".equals(test.treeRef)) {

                        found2 = true;
                    } else if ("222222222".equals(test.stuId)
                            && "BBBBB".equals(test.version)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(222333).equals(test.finishTime)
                            && Integer.valueOf(3).equals(test.questionNbr)
                            && "Ans3".equals(test.stuAnswer)
                            && "A".equals(test.ansCorrect)
                            && "x".equals(test.subtest)
                            && "Tree.ref.3".equals(test.treeRef)) {

                        found3 = true;
                    } else if ("333333333".equals(test.stuId)
                            && "CCCCC".equals(test.version)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(333444).equals(test.finishTime)
                            && Integer.valueOf(4).equals(test.questionNbr)
                            && "Ans4".equals(test.stuAnswer)
                            && "B".equals(test.ansCorrect)
                            && "w".equals(test.subtest)
                            && "Tree.ref.4".equals(test.treeRef)) {

                        found4 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected questionNbr ", test.questionNbr);
                        Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                        Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                        Log.warning("Unexpected subtest ", test.subtest);
                        Log.warning("Unexpected treeRef ", test.treeRef);
                    }
                }

                assertTrue(found1, "Stmpeqa 1 not found");
                assertTrue(found2, "Stmpeqa 2 not found");
                assertTrue(found3, "Stmpeqa 3 not found");
                assertTrue(found4, "Stmpeqa 4 not found");

            } finally {
                this.ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stmpeqa rows: " + ex.getMessage());
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
                final List<RawStmpeqa> all = RawStmpeqaLogic.INSTANCE.queryAll(cache);

                assertEquals(4, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;

                for (final RawStmpeqa test : all) {
                    if ("111111111".equals(test.stuId)
                            && "AAAAA".equals(test.version)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(123456).equals(test.finishTime)
                            && Integer.valueOf(1).equals(test.questionNbr)
                            && "Ans1".equals(test.stuAnswer)
                            && "N".equals(test.ansCorrect)
                            && "z".equals(test.subtest)
                            && "Tree.ref.1".equals(test.treeRef)) {

                        found1 = true;
                    } else if ("111111111".equals(test.stuId)
                            && "AAAAA".equals(test.version)
                            && date1.equals(test.examDt)
                            && Integer.valueOf(123456).equals(test.finishTime)
                            && Integer.valueOf(2).equals(test.questionNbr)
                            && "Ans2".equals(test.stuAnswer)
                            && "Y".equals(test.ansCorrect)
                            && "y".equals(test.subtest)
                            && "Tree.ref.2".equals(test.treeRef)) {

                        found2 = true;
                    } else if ("222222222".equals(test.stuId)
                            && "BBBBB".equals(test.version)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(222333).equals(test.finishTime)
                            && Integer.valueOf(3).equals(test.questionNbr)
                            && "Ans3".equals(test.stuAnswer)
                            && "A".equals(test.ansCorrect)
                            && "x".equals(test.subtest)
                            && "Tree.ref.3".equals(test.treeRef)) {

                        found3 = true;
                    } else if ("333333333".equals(test.stuId)
                            && "CCCCC".equals(test.version)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(333444).equals(test.finishTime)
                            && Integer.valueOf(4).equals(test.questionNbr)
                            && "Ans4".equals(test.stuAnswer)
                            && "B".equals(test.ansCorrect)
                            && "w".equals(test.subtest)
                            && "Tree.ref.4".equals(test.treeRef)) {

                        found4 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected questionNbr ", test.questionNbr);
                        Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                        Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                        Log.warning("Unexpected subtest ", test.subtest);
                        Log.warning("Unexpected treeRef ", test.treeRef);
                    }
                }

                assertTrue(found1, "Stmpeqa 1 not found");
                assertTrue(found2, "Stmpeqa 2 not found");
                assertTrue(found3, "Stmpeqa 3 not found");
                assertTrue(found4, "Stmpeqa 4 not found");

            } finally {
                this.ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stmpeqa rows: " + ex.getMessage());
        }

        return "queryAll results were correct";
    }

    /** Test case. */
    @Test
    @DisplayName("")
    void test0007() {

        try {
            final DbConnection conn = this.ctx.checkOutConnection();
            final Cache cache = new Cache(this.dbProfile, conn);

            try {
                final RawStmpe stmpe = new RawStmpe("111111111", "AAAAA", "2021", date1,
                        Integer.valueOf(123000), Integer.valueOf(123456), "Doe", "John", "Q", Integer.valueOf(123),
                        Long.valueOf(987654321L), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3),
                        Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6), "Y", "P");

                final boolean result = RawStmpeqaLogic.deleteAllForExam(cache, stmpe);
                assertTrue(result, "deleteAllForExam returned false");

                final List<RawStmpeqa> all = RawStmpeqaLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after deleteAllForExam");

                boolean found3 = false;
                boolean found4 = false;

                for (final RawStmpeqa test : all) {
                    if ("222222222".equals(test.stuId)
                            && "BBBBB".equals(test.version)
                            && date2.equals(test.examDt)
                            && Integer.valueOf(222333).equals(test.finishTime)
                            && Integer.valueOf(3).equals(test.questionNbr)
                            && "Ans3".equals(test.stuAnswer)
                            && "A".equals(test.ansCorrect)
                            && "x".equals(test.subtest)
                            && "Tree.ref.3".equals(test.treeRef)) {

                        found3 = true;
                    } else if ("333333333".equals(test.stuId)
                            && "CCCCC".equals(test.version)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(333444).equals(test.finishTime)
                            && Integer.valueOf(4).equals(test.questionNbr)
                            && "Ans4".equals(test.stuAnswer)
                            && "B".equals(test.ansCorrect)
                            && "w".equals(test.subtest)
                            && "Tree.ref.4".equals(test.treeRef)) {

                        found4 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected questionNbr ", test.questionNbr);
                        Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                        Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                        Log.warning("Unexpected subtest ", test.subtest);
                        Log.warning("Unexpected treeRef ", test.treeRef);
                    }
                }

                assertTrue(found3, "Stmpeqa 3 not found");
                assertTrue(found4, "Stmpeqa 4 not found");

            } finally {
                this.ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after deleteAllForExam: " + ex.getMessage());
        }

        return "deleteAllForExam results were correct";
    }

    /** Test case. */
    @Test
    @DisplayName("")
    void test0008() {

        try {
            final DbConnection conn = this.ctx.checkOutConnection();
            final Cache cache = new Cache(this.dbProfile, conn);

            try {
                final RawStmpeqa raw3 = new RawStmpeqa("222222222",
                        "BBBBB",
                        date2,
                        Integer.valueOf(222333),
                        Integer.valueOf(3),
                        "Ans3",
                        "A",
                        "x",
                        "Tree.ref.3");

                final boolean result = RawStmpeqaLogic.INSTANCE.delete(cache, raw3);
                assertTrue(result, "delete returned false");

                final List<RawStmpeqa> all = RawStmpeqaLogic.INSTANCE.queryAll(cache);

                assertEquals(1, all.size(), "Incorrect record count from queryAll after delete");

                boolean found4 = false;

                for (final RawStmpeqa test : all) {
                    if ("333333333".equals(test.stuId)
                            && "CCCCC".equals(test.version)
                            && date3.equals(test.examDt)
                            && Integer.valueOf(333444).equals(test.finishTime)
                            && Integer.valueOf(4).equals(test.questionNbr)
                            && "Ans4".equals(test.stuAnswer)
                            && "B".equals(test.ansCorrect)
                            && "w".equals(test.subtest)
                            && "Tree.ref.4".equals(test.treeRef)) {

                        found4 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected version ", test.version);
                        Log.warning("Unexpected examDt ", test.examDt);
                        Log.warning("Unexpected finishTime ", test.finishTime);
                        Log.warning("Unexpected questionNbr ", test.questionNbr);
                        Log.warning("Unexpected stuAnswer ", test.stuAnswer);
                        Log.warning("Unexpected ansCorrect ", test.ansCorrect);
                        Log.warning("Unexpected subtest ", test.subtest);
                        Log.warning("Unexpected treeRef ", test.treeRef);
                    }
                }

                assertTrue(found4, "Stmpeqa 4 not found");

            } finally {
                this.ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after delete: " + ex.getMessage());
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
                    stmt.executeUpdate("DELETE FROM stmpeqa");
                }

                conn.commit();

            } finally {
                this.ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning table: " + ex.getMessage());
        }
    }
}

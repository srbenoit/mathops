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
import dev.mathops.db.rawrecord.RawExceptStu;
import dev.mathops.db.rawrecord.RawRecordConstants;

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
 * Tests for the {@code RawExceptStuLogic} class.
 */
final class TestRawExceptStuLogic {

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
                    stmt.executeUpdate("DELETE FROM except_stu");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawExceptStu raw1 = new RawExceptStu(new TermKey("FA21"), "123456789", RawRecordConstants.M117,
                        Integer.valueOf(1), "M 384", "Y", "001", "002");

                final RawExceptStu raw2 = new RawExceptStu(new TermKey("FA21"), "123456789", RawRecordConstants.M117,
                        Integer.valueOf(2), "M 384", "Y", "001", "002");

                final RawExceptStu raw3 = new RawExceptStu(new TermKey("FA21"), "987654321", RawRecordConstants.M125,
                        Integer.valueOf(4), "M 676", "N", "004", "1");

                assertTrue(RawExceptStuLogic.INSTANCE.insert(cache, raw1), "Failed to insert except_stu");
                assertTrue(RawExceptStuLogic.INSTANCE.insert(cache, raw2), "Failed to insert except_stu");
                assertTrue(RawExceptStuLogic.INSTANCE.insert(cache, raw3), "Failed to insert except_stu");
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
                final List<RawExceptStu> all = RawExceptStuLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                final TermKey fa21 = new TermKey("FA21");

                for (final RawExceptStu r : all) {

                    if (fa21.equals(r.termKey)
                            && "123456789".equals(r.stuId)
                            && RawRecordConstants.M117.equals(r.course)
                            && Integer.valueOf(1).equals(r.unit)
                            && "M 384".equals(r.courseEnroll)
                            && "Y".equals(r.hworkStatus)
                            && "001".equals(r.sect)
                            && "002".equals(r.sectEnroll)) {

                        found1 = true;

                    } else if (fa21.equals(r.termKey)
                            && "123456789".equals(r.stuId)
                            && RawRecordConstants.M117.equals(r.course)
                            && Integer.valueOf(2).equals(r.unit)
                            && "Y".equals(r.hworkStatus)
                            && "M 384".equals(r.courseEnroll)
                            && "001".equals(r.sect)
                            && "002".equals(r.sectEnroll)) {

                        found2 = true;

                    } else if (fa21.equals(r.termKey)
                            && "987654321".equals(r.stuId)
                            && RawRecordConstants.M125.equals(r.course)
                            && Integer.valueOf(4).equals(r.unit)
                            && "N".equals(r.hworkStatus)
                            && "M 676".equals(r.courseEnroll)
                            && "004".equals(r.sect)
                            && "1".equals(r.sectEnroll)) {

                        found3 = true;

                    } else {
                        Log.warning("Unexpected term ", r.termKey);
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected unit ", r.unit);
                        Log.warning("Unexpected courseEnroll ", r.courseEnroll);
                        Log.warning("Unexpected hworkStatus ", r.hworkStatus);
                        Log.warning("Unexpected sect ", r.sect);
                        Log.warning("Unexpected sectEnroll ", r.sectEnroll);
                    }
                }

                assertTrue(found1, "except_stu 1 not found");
                assertTrue(found2, "except_stu 2 not found");
                assertTrue(found3, "except_stu 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all except_stu rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudentCourse results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawExceptStu> all = RawExceptStuLogic.queryByStudentCourse(cache, "123456789",
                        RawRecordConstants.M117);

                assertEquals(2, all.size(), "Incorrect record count from queryByStudentCourse");

                boolean found1 = false;
                boolean found2 = false;

                final TermKey fa21 = new TermKey("FA21");

                for (final RawExceptStu r : all) {

                    if (fa21.equals(r.termKey)
                            && "123456789".equals(r.stuId)
                            && RawRecordConstants.M117.equals(r.course)
                            && Integer.valueOf(1).equals(r.unit)
                            && "M 384".equals(r.courseEnroll)
                            && "Y".equals(r.hworkStatus)
                            && "001".equals(r.sect)
                            && "002".equals(r.sectEnroll)) {

                        found1 = true;

                    } else if (fa21.equals(r.termKey)
                            && "123456789".equals(r.stuId)
                            && RawRecordConstants.M117.equals(r.course)
                            && Integer.valueOf(2).equals(r.unit)
                            && "Y".equals(r.hworkStatus)
                            && "M 384".equals(r.courseEnroll)
                            && "001".equals(r.sect)
                            && "002".equals(r.sectEnroll)) {

                        found2 = true;

                    } else {
                        Log.warning("Unexpected term ", r.termKey);
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected unit ", r.unit);
                        Log.warning("Unexpected courseEnroll ", r.courseEnroll);
                        Log.warning("Unexpected hworkStatus ", r.hworkStatus);
                        Log.warning("Unexpected sect ", r.sect);
                        Log.warning("Unexpected sectEnroll ", r.sectEnroll);
                    }
                }

                assertTrue(found1, "except_stu 1 not found");
                assertTrue(found2, "except_stu 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying except_stu rows by student course: " + ex.getMessage());
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
                final RawExceptStu raw2 = new RawExceptStu(new TermKey("FA21"), "123456789", RawRecordConstants.M117,
                        Integer.valueOf(2), "M 384", "Y", "001", "002");

                final boolean result = RawExceptStuLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawExceptStu> all = RawExceptStuLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                final TermKey fa21 = new TermKey("FA21");

                for (final RawExceptStu r : all) {

                    if (fa21.equals(r.termKey)
                            && "123456789".equals(r.stuId)
                            && RawRecordConstants.M117.equals(r.course)
                            && Integer.valueOf(1).equals(r.unit)
                            && "M 384".equals(r.courseEnroll)
                            && "Y".equals(r.hworkStatus)
                            && "001".equals(r.sect)
                            && "002".equals(r.sectEnroll)) {

                        found1 = true;

                    } else if (fa21.equals(r.termKey)
                            && "987654321".equals(r.stuId)
                            && RawRecordConstants.M125.equals(r.course)
                            && Integer.valueOf(4).equals(r.unit)
                            && "N".equals(r.hworkStatus)
                            && "M 676".equals(r.courseEnroll)
                            && "004".equals(r.sect)
                            && "1".equals(r.sectEnroll)) {

                        found3 = true;

                    } else {
                        Log.warning("Unexpected term ", r.termKey);
                        Log.warning("Unexpected stuId ", r.stuId);
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected unit ", r.unit);
                        Log.warning("Unexpected courseEnroll ", r.courseEnroll);
                        Log.warning("Unexpected hworkStatus ", r.hworkStatus);
                        Log.warning("Unexpected sect ", r.sect);
                        Log.warning("Unexpected sectEnroll ", r.sectEnroll);
                    }
                }

                assertTrue(found1, "except_stu 1 not found");
                assertTrue(found3, "except_stu 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting except_stu: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM except_stu");
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

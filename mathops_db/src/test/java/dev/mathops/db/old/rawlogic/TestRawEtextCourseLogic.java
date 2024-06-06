package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.logic.DbConnection;
import dev.mathops.db.logic.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawEtextCourse;
import dev.mathops.db.old.rawrecord.RawRecordConstants;

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
 * Tests for the {@code RawEtextCourseLogic} class.
 */
final class TestRawEtextCourseLogic {

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
                    stmt.executeUpdate("DELETE FROM etext_course");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawEtextCourse raw1 = new RawEtextCourse("PACE", RawRecordConstants.M117);
                final RawEtextCourse raw2 = new RawEtextCourse("PACE", RawRecordConstants.M118);
                final RawEtextCourse raw3 = new RawEtextCourse("ET117", RawRecordConstants.M117);

                assertTrue(RawEtextCourseLogic.INSTANCE.insert(cache, raw1), "Failed to insert etext_course");
                assertTrue(RawEtextCourseLogic.INSTANCE.insert(cache, raw2), "Failed to insert etext_course");
                assertTrue(RawEtextCourseLogic.INSTANCE.insert(cache, raw3), "Failed to insert etext_course");
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
                final List<RawEtextCourse> all = RawEtextCourseLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), //
                        "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawEtextCourse r : all) {

                    if ("PACE".equals(r.etextId)
                            && RawRecordConstants.M117.equals(r.course)) {

                        found1 = true;
                    } else if ("PACE".equals(r.etextId)
                            && RawRecordConstants.M118.equals(r.course)) {

                        found2 = true;
                    } else if ("ET117".equals(r.etextId)
                            && RawRecordConstants.M117.equals(r.course)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected etextId ", r.etextId);
                        Log.warning("Unexpected course ", r.course);
                    }
                }

                assertTrue(found1, "etext_course 1 not found");
                assertTrue(found2, "etext_course 2 not found");
                assertTrue(found3, "etext_course 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all etext_course rows: "
                    + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByEtext results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawEtextCourse> all = RawEtextCourseLogic.queryByEtext(cache, "PACE");

                assertEquals(2, all.size(), "Incorrect record count from queryByEtext");

                boolean found1 = false;
                boolean found2 = false;

                for (final RawEtextCourse r : all) {

                    if ("PACE".equals(r.etextId) && RawRecordConstants.M117.equals(r.course)) {

                        found1 = true;
                    } else if ("PACE".equals(r.etextId) && RawRecordConstants.M118.equals(r.course)) {

                        found2 = true;
                    } else {
                        Log.warning("Unexpected etextId ", r.etextId);
                        Log.warning("Unexpected course ", r.course);
                    }
                }

                assertTrue(found1, "etext_course 1 not found");
                assertTrue(found2, "etext_course 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying etext_course by etext: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByCourse results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawEtextCourse> all = RawEtextCourseLogic.queryByCourse(cache, RawRecordConstants.M117);

                assertEquals(2, all.size(), "Incorrect record count from queryByCourse");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawEtextCourse r : all) {

                    if ("PACE".equals(r.etextId) && RawRecordConstants.M117.equals(r.course)) {

                        found1 = true;
                    } else if ("ET117".equals(r.etextId) && RawRecordConstants.M117.equals(r.course)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected etextId ", r.etextId);
                        Log.warning("Unexpected course ", r.course);
                    }
                }

                assertTrue(found1, "etext_course 1 not found");
                assertTrue(found3, "etext_course 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying etext_course by course: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0006() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawEtextCourse raw2 = new RawEtextCourse("PACE", RawRecordConstants.M118);

                final boolean result = RawEtextCourseLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawEtextCourse> all = RawEtextCourseLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawEtextCourse r : all) {

                    if ("PACE".equals(r.etextId) && RawRecordConstants.M117.equals(r.course)) {

                        found1 = true;
                    } else if ("ET117".equals(r.etextId) && RawRecordConstants.M117.equals(r.course)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected etextId ", r.etextId);
                        Log.warning("Unexpected course ", r.course);
                    }
                }

                assertTrue(found1, "etext_course 1 not found");
                assertTrue(found3, "etext_course 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting etext_course: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM etext_course");
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

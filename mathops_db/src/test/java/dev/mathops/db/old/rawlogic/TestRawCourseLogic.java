package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawRecordConstants;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Tests for the {@code RawCourseLogic} class.
 */
final class TestRawCourseLogic {

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
                    stmt.executeUpdate("DELETE FROM course");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawCourse raw1 = new RawCourse(RawRecordConstants.M117, Integer.valueOf(4),
                        "College Algebra in Context I", Integer.valueOf(1), "Y", "MATH 117", null, "N", "Y");

                final RawCourse raw2 = new RawCourse(RawRecordConstants.M100T, Integer.valueOf(4),
                        "Entry Level Math Tutorial", Integer.valueOf(0), "N", "ELM Tutorial", "the", "Y", "N");

                assertTrue(RawCourseLogic.INSTANCE.insert(cache, raw1), "Failed to insert course");
                assertTrue(RawCourseLogic.INSTANCE.insert(cache, raw2), "Failed to insert course");
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
                final List<RawCourse> all = RawCourseLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;

                for (final RawCourse r : all) {

                    if (RawRecordConstants.M117.equals(r.course)
                            && Integer.valueOf(4).equals(r.nbrUnits)
                            && "College Algebra in Context I".equals(r.courseName)
                            && Integer.valueOf(1).equals(r.nbrCredits)
                            && "Y".equals(r.calcOk)
                            && "MATH 117".equals(r.courseLabel)
                            && r.inlinePrefix == null
                            && "N".equals(r.isTutorial)
                            && "Y".equals(r.requireEtext)) {
                        found1 = true;

                    } else if (RawRecordConstants.M100T.equals(r.course)
                            && Integer.valueOf(4).equals(r.nbrUnits)
                            && "Entry Level Math Tutorial".equals(r.courseName)
                            && Integer.valueOf(0).equals(r.nbrCredits)
                            && "N".equals(r.calcOk)
                            && "ELM Tutorial".equals(r.courseLabel)
                            && "the".equals(r.inlinePrefix)
                            && "Y".equals(r.isTutorial)
                            && "N".equals(r.requireEtext)) {
                        found2 = true;

                    } else {
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected nbrUnits ", r.nbrUnits);
                        Log.warning("Unexpected courseName ", r.courseName);
                        Log.warning("Unexpected nbrCredits ", r.nbrCredits);
                        Log.warning("Unexpected calcOk ", r.calcOk);
                        Log.warning("Unexpected courseLabel ", r.courseLabel);
                        Log.warning("Unexpected inlinePrefix ", r.inlinePrefix);
                        Log.warning("Unexpected isTutorial ", r.isTutorial);
                        Log.warning("Unexpected requireEtext ", r.requireEtext);
                    }
                }

                assertTrue(found1, "course 1 not found");
                assertTrue(found2, "course 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all course rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0008() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawCourse raw2 = new RawCourse(RawRecordConstants.M100T, Integer.valueOf(4),
                        "Entry Level Math Tutorial", Integer.valueOf(0), "N", "ELM Tutorial", "the", "Y", "N");

                final boolean result = RawCourseLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawCourse> all = RawCourseLogic.INSTANCE.queryAll(cache);

                assertEquals(1, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;

                for (final RawCourse r : all) {

                    if (RawRecordConstants.M117.equals(r.course)
                            && Integer.valueOf(4).equals(r.nbrUnits)
                            && "College Algebra in Context I".equals(r.courseName)
                            && Integer.valueOf(1).equals(r.nbrCredits)
                            && "Y".equals(r.calcOk)
                            && "MATH 117".equals(r.courseLabel)
                            && r.inlinePrefix == null
                            && "N".equals(r.isTutorial)
                            && "Y".equals(r.requireEtext)) {
                        found1 = true;

                    } else {
                        Log.warning("Unexpected course ", r.course);
                        Log.warning("Unexpected nbrUnits ", r.nbrUnits);
                        Log.warning("Unexpected courseName ", r.courseName);
                        Log.warning("Unexpected nbrCredits ", r.nbrCredits);
                        Log.warning("Unexpected calcOk ", r.calcOk);
                        Log.warning("Unexpected courseLabel ", r.courseLabel);
                        Log.warning("Unexpected inlinePrefix ", r.inlinePrefix);
                        Log.warning("Unexpected isTutorial ", r.isTutorial);
                        Log.warning("Unexpected requireEtext ", r.requireEtext);
                    }
                }

                assertTrue(found1, "course 1 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting course: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM course");
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

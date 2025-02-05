package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawCourseLogic} class.
 */
final class TestRawCourseLogic {

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
                stmt.executeUpdate("DELETE FROM course");
            }
            conn.commit();

            final RawCourse raw1 = new RawCourse(RawRecordConstants.M117, Integer.valueOf(4),
                    "College Algebra in Context I", Integer.valueOf(1), "Y", "MATH 117", null, "N", "Y");

            final RawCourse raw2 = new RawCourse(RawRecordConstants.M100T, Integer.valueOf(4),
                    "Entry Level Math Tutorial", Integer.valueOf(0), "N", "ELM Tutorial", "the", "Y", "N");

            assertTrue(RawCourseLogic.insert(cache, raw1), "Failed to insert course");
            assertTrue(RawCourseLogic.insert(cache, raw2), "Failed to insert course");
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
            final List<RawCourse> all = RawCourseLogic.queryAll(cache);

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
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all course rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0008() {

        final Cache cache = new Cache(profile);

        try {
            final RawCourse raw2 = new RawCourse(RawRecordConstants.M100T, Integer.valueOf(4),
                    "Entry Level Math Tutorial", Integer.valueOf(0), "N", "ELM Tutorial", "the", "Y", "N");

            final boolean result = RawCourseLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawCourse> all = RawCourseLogic.queryAll(cache);

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
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting course: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM course");
            }

            conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning table: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }
}

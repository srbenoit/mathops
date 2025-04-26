package dev.mathops.db.reclogic.term;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.EDbUse;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Facet;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.rec.term.StandardsCourseSectionRec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code StandardsCourseSectionLogic} class.
 */
final class TestStandardsCourseSectionLogic {

    /** A raw test record. */
    private static final StandardsCourseSectionRec RAW1 =
            new StandardsCourseSectionRec("MATH 117", "001", "665544",
                    LocalDate.of(2025, Month.JANUARY, 19), LocalDate.of(2025, Month.MAY, 11),
                    LocalDate.of(2025, Month.JANUARY, 21), LocalDate.of(2025, Month.MAY, 9),
                    "FULL", "SYS001", "FC", "RF", "Canvas.MATH117.001", "John Doe", "Weber", "123",
                    Integer.valueOf(99));

    /** A raw test record. */
    private static final StandardsCourseSectionRec RAW2 =
            new StandardsCourseSectionRec("MATH 117", "002", "554433",
                    LocalDate.of(2025, Month.MARCH, 1), LocalDate.of(2025, Month.MAY, 11),
                    LocalDate.of(2025, Month.MARCH, 3), LocalDate.of(2025, Month.MAY, 9),
                    "HALF2", "SYS002", "CE", "RH", "Canvas.MATH117.002", "Jane Doe", "Johnson", "222",
                    Integer.valueOf(88));

    /** A raw test record. */
    private static final StandardsCourseSectionRec RAW3 =
            new StandardsCourseSectionRec("MATH 118", "003", "443322",
                    LocalDate.of(2025, Month.APRIL, 1), LocalDate.of(2025, Month.APRIL, 27),
                    LocalDate.of(2025, Month.APRIL, 3), LocalDate.of(2025, Month.APRIL, 25),
                    "10:13", "SYS003", "ZY", "RO", "Canvas.MATH118.003", "Edgar Poe", "Baker", "221B",
                    Integer.valueOf(77));

    /** A raw test record. */
    private static final StandardsCourseSectionRec UPD3 =
            new StandardsCourseSectionRec("MATH 118", "003", "987654",
                    LocalDate.of(2025, Month.APRIL, 2), LocalDate.of(2025, Month.APRIL, 28),
                    LocalDate.of(2025, Month.APRIL, 4), LocalDate.of(2025, Month.APRIL, 26),
                    "11:14", "SYS004", "XY", "DO", "Canvas.MATH118.003.X", "Winston", "Ministry of Truth", "101",
                    Integer.valueOf(84));

    /** The database profile. */
    static Profile profile;

    /** The database login. */
    static Login login;

    /**
     * Prints an indication of an unexpected record.
     *
     * @param r the unexpected record
     */
    private static void printUnexpected(final StandardsCourseSectionRec r) {

        Log.warning("Unexpected course ID ", r.courseId);
        Log.warning("Unexpected section number ", r.sectionNbr);
        Log.warning("Unexpected CRN ", r.crn);
        Log.warning("Unexpected Aries start date ", r.ariesStartDate);
        Log.warning("Unexpected Aries end date ", r.ariesEndDate);
        Log.warning("Unexpected first class date ", r.firstClassDate);
        Log.warning("Unexpected last class date ", r.lastClassDate);
        Log.warning("Unexpected subterm ", r.subterm);
        Log.warning("Unexpected grading system ID ", r.gradingSystemId);
        Log.warning("Unexpected campus ", r.campus);
        Log.warning("Unexpected delivery mode ", r.deliveryMode);
        Log.warning("Unexpected Canvas ID ", r.canvasId);
        Log.warning("Unexpected instructor ", r.instructor);
        Log.warning("Unexpected building name ", r.buildingName);
        Log.warning("Unexpected room number ", r.roomNbr);
        Log.warning("Unexpected weekdays ", r.weekdays);
    }

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        profile = config.getCodeProfile(Contexts.POSTGRES_TEST_PATH);
        if (profile == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PGTEST_PROFILE));
        }
        login = profile.getLogin(ESchema.LEGACY);
        if (login == null) {
            throw new IllegalArgumentException(TestRes.get(TestRes.ERR_NO_PGPRIMARY_CONTEXT));
        }

        // Make sure the connection is accessing the TEST database
        final Facet facet = profile.getFacet(ESchema.LEGACY);
        if (facet.data.use != EDbUse.TEST) {
            throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, facet.data.use));
        }

        final Cache cache = new Cache(profile);
        final String prefix = cache.getSchemaPrefix(ESchema.TERM);
        if (prefix == null) {
            fail(TestRes.get(TestRes.ERR_NO_TERM_PREFIX));
        }

        final DbConnection conn = login.checkOutConnection();

        final String sql = "SELECT descr FROM " + prefix + ".which_db";

        try {
            try (final Statement stmt = conn.createStatement();
                 final ResultSet rs = stmt.executeQuery(sql)) {

                if (rs.next()) {
                    final String which = rs.getString(1);
                    if (which == null || !"TEST".equals(which.trim())) {
                        throw new IllegalArgumentException(TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                    }
                } else {
                    throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                }
            }

            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM " + prefix + ".standards_course_section");
            }
            conn.commit();

            assertTrue(StandardsCourseSectionLogic.INSTANCE.insert(cache, RAW1),
                    "Failed to insert standards course section");
            assertTrue(StandardsCourseSectionLogic.INSTANCE.insert(cache, RAW2),
                    "Failed to insert standards course section");
            assertTrue(StandardsCourseSectionLogic.INSTANCE.insert(cache, RAW3),
                    "Failed to insert standards course section");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while initializing 'standards_course_section' table: " + ex.getMessage());
            throw new IllegalArgumentException(ex);
        } finally {
            login.checkInConnection(conn);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAll results")
    void test0001() {
        final Cache cache = new Cache(profile);

        try {
            final List<StandardsCourseSectionRec> all = StandardsCourseSectionLogic.INSTANCE.queryAll(
                    cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final StandardsCourseSectionRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (RAW2.equals(r)) {
                    found2 = true;
                } else if (RAW3.equals(r)) {
                    found3 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "standards_course_section 1 not found");
            assertTrue(found2, "standards_course_section 2 not found");
            assertTrue(found3, "standards_course_section 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all 'standards_course_section' rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0002() {
        final Cache cache = new Cache(profile);

        try {
            final StandardsCourseSectionRec r = StandardsCourseSectionLogic.INSTANCE.query(cache, RAW1.courseId,
                    RAW1.sectionNbr);

            assertNotNull(r, "No record returned by query");

            if (!RAW1.equals(r)) {
                printUnexpected(r);
                fail("Extra record found");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying standards_course_section: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("update results")
    void test0003() {
        final Cache cache = new Cache(profile);

        try {
            if (StandardsCourseSectionLogic.INSTANCE.update(cache, UPD3)) {
                final StandardsCourseSectionRec r = StandardsCourseSectionLogic.INSTANCE.query(cache, UPD3.courseId,
                        UPD3.sectionNbr);

                assertNotNull(r, "No record returned by query after update");

                if (!UPD3.equals(r)) {
                    printUnexpected(r);
                    fail("Incorrect results after update of standards_course_section");
                }
            } else {
                fail("Failed to update standards_course_section row");
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while updating standards_course_section: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0004() {
        final Cache cache = new Cache(profile);

        try {
            final boolean result = StandardsCourseSectionLogic.INSTANCE.delete(cache, RAW2);
            assertTrue(result, "delete returned false");

            final List<StandardsCourseSectionRec> all = StandardsCourseSectionLogic.INSTANCE.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final StandardsCourseSectionRec r : all) {
                if (RAW1.equals(r)) {
                    found1 = true;
                } else if (UPD3.equals(r)) {
                    found3 = true;
                } else {
                    printUnexpected(r);
                    fail("Extra record found");
                }
            }

            assertTrue(found1, "standards_course_section 1 not found");
            assertTrue(found3, "standards_course_section 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting standards_course_sections: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Cache cache = new Cache(profile);
        final String prefix = cache.getSchemaPrefix(ESchema.TERM);
        if (prefix == null) {
            fail(TestRes.get(TestRes.ERR_NO_TERM_PREFIX));
        }

        try {
            final DbConnection conn = login.checkOutConnection();

            final String sql = "SELECT descr FROM " + prefix + ".which_db";

            try {
                try (final Statement stmt = conn.createStatement();
                     final ResultSet rs = stmt.executeQuery(sql)) {

                    if (rs.next()) {
                        final String which = rs.getString(1);
                        if (which == null || !"TEST".equals(which.trim())) {
                            throw new IllegalArgumentException(
                                    TestRes.fmt(TestRes.ERR_NOT_CONNECTED_TO_TEST, which));
                        }
                    } else {
                        throw new IllegalArgumentException(TestRes.get(TestRes.ERR_CANT_QUERY_WHICH_DB));
                    }
                }

                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM " + prefix + ".standards_course_section");
                }

                conn.commit();
            } finally {
                login.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        }
    }
}

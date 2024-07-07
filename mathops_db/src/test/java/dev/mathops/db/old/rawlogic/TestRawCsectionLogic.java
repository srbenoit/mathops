package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawrecord.RawCsection;
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
import java.time.LocalDate;
import java.util.List;

/**
 * Tests for the {@code RawCsectionLogic} class.
 */
final class TestRawCsectionLogic {

    /** A term key used in test records. */
    private static final TermKey fa21 = new TermKey(ETermName.FALL, 2021);

    /** A term key used in test records. */
    private static final TermKey fa20 = new TermKey(ETermName.FALL, 2020);

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2020, 2, 3);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2020, 5, 6);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2020, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2020, 6, 7);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2020, 4, 5);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2020, 2, 4);

    /** A date used in test records. */
    private static final LocalDate date7 = LocalDate.of(2020, 5, 7);

    /** A date used in test records. */
    private static final LocalDate date8 = LocalDate.of(2020, 1, 3);

    /** A date used in test records. */
    private static final LocalDate date9 = LocalDate.of(2020, 6, 8);

    /** A date used in test records. */
    private static final LocalDate date10 = LocalDate.of(2020, 4, 6);

    /** A date used in test records. */
    private static final LocalDate date11 = LocalDate.of(2019, 2, 4);

    /** A date used in test records. */
    private static final LocalDate date12 = LocalDate.of(2019, 5, 7);

    /** A date used in test records. */
    private static final LocalDate date13 = LocalDate.of(2019, 1, 3);

    /** A date used in test records. */
    private static final LocalDate date14 = LocalDate.of(2019, 6, 8);

    /** A date used in test records. */
    private static final LocalDate date15 = LocalDate.of(2019, 4, 6);

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
                    stmt.executeUpdate("DELETE FROM csection");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawCsection raw1 = new RawCsection(fa21, RawRecordConstants.M117, "001", "12345", date1, date2,
                        date3, date4, "RI", "Alice", "FC", "M", "MTWRF", "WB136", date5, "S", Integer.valueOf(65),
                        Integer.valueOf(62), Integer.valueOf(54), Integer.valueOf(1), "SUR-1", "A", "B", "C", "D", "E",
                        "F", "CANVAS-1", "FULL");

                final RawCsection raw2 = new RawCsection(fa21, RawRecordConstants.M117, "401", "12346", date6, date7,
                        date8, date9, "CE", "Bob", "LA", "O", "M W F", "WB137", date10, "T", Integer.valueOf(66),
                        Integer.valueOf(63), Integer.valueOf(55), Integer.valueOf(2), "SUR-2", "Z", "Y", "X", "W", "V",
                        "U", "CANVAS-2", "LATE");

                final RawCsection raw3 = new RawCsection(fa20, RawRecordConstants.M117, "001", "12347", date11, date12,
                        date13, date14, "OT", "Chris", "LO", "P", "T R", "WB138", date15, "U", Integer.valueOf(64),
                        Integer.valueOf(61), Integer.valueOf(53), Integer.valueOf(0), "SUR-3", "1", "2", "3", "4", "5",
                        "6", "CANVAS-3", "HALF");

                assertTrue(RawCsectionLogic.INSTANCE.insert(cache, raw1), "Failed to insert csection");
                assertTrue(RawCsectionLogic.INSTANCE.insert(cache, raw2), "Failed to insert csection");
                assertTrue(RawCsectionLogic.INSTANCE.insert(cache, raw3), "Failed to insert csection");
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
                final List<RawCsection> all = RawCsectionLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawCsection r : all) {

                    if (fa21.equals(r.termKey)
                            && RawRecordConstants.M117.equals(r.course)
                            && "001".equals(r.sect)
                            && "12345".equals(r.sectionId)
                            && date1.equals(r.ariesStartDt)
                            && date2.equals(r.ariesEndDt)
                            && date3.equals(r.startDt)
                            && date4.equals(r.examDeleteDt)
                            && "RI".equals(r.instrnType)
                            && "Alice".equals(r.instructor)
                            && "FC".equals(r.campus)
                            && "M".equals(r.pacingStructure)
                            && "MTWRF".equals(r.mtgDays)
                            && "WB136".equals(r.classroomId)
                            && date5.equals(r.lstStcrsCreatDt)
                            && "S".equals(r.gradingStd)
                            && Integer.valueOf(65).equals(r.aMinScore)
                            && Integer.valueOf(62).equals(r.bMinScore)
                            && Integer.valueOf(54).equals(r.cMinScore)
                            && Integer.valueOf(1).equals(r.dMinScore)
                            && "SUR-1".equals(r.surveyId)
                            && "A".equals(r.courseLabelShown)
                            && "B".equals(r.displayScore)
                            && "C".equals(r.displayGradeScale)
                            && "D".equals(r.countInMaxCourses)
                            && "E".equals(r.online)
                            && "F".equals(r.bogus)
                            && "CANVAS-1".equals(r.canvasId)
                            && "FULL".equals(r.subterm)) {

                        found1 = true;

                    } else if (fa21.equals(r.termKey)
                            && RawRecordConstants.M117.equals(r.course)
                            && "401".equals(r.sect)
                            && "12346".equals(r.sectionId)
                            && date6.equals(r.ariesStartDt)
                            && date7.equals(r.ariesEndDt)
                            && date8.equals(r.startDt)
                            && date9.equals(r.examDeleteDt)
                            && "CE".equals(r.instrnType)
                            && "Bob".equals(r.instructor)
                            && "LA".equals(r.campus)
                            && "O".equals(r.pacingStructure)
                            && "M W F".equals(r.mtgDays)
                            && "WB137".equals(r.classroomId)
                            && date10.equals(r.lstStcrsCreatDt)
                            && "T".equals(r.gradingStd)
                            && Integer.valueOf(66).equals(r.aMinScore)
                            && Integer.valueOf(63).equals(r.bMinScore)
                            && Integer.valueOf(55).equals(r.cMinScore)
                            && Integer.valueOf(2).equals(r.dMinScore)
                            && "SUR-2".equals(r.surveyId)
                            && "Z".equals(r.courseLabelShown)
                            && "Y".equals(r.displayScore)
                            && "X".equals(r.displayGradeScale)
                            && "W".equals(r.countInMaxCourses)
                            && "V".equals(r.online)
                            && "U".equals(r.bogus)
                            && "CANVAS-2".equals(r.canvasId)
                            && "LATE".equals(r.subterm)) {

                        found2 = true;

                    } else if (fa20.equals(r.termKey)
                            && RawRecordConstants.M117.equals(r.course)
                            && "001".equals(r.sect)
                            && "12347".equals(r.sectionId)
                            && date11.equals(r.ariesStartDt)
                            && date12.equals(r.ariesEndDt)
                            && date13.equals(r.startDt)
                            && date14.equals(r.examDeleteDt)
                            && "OT".equals(r.instrnType)
                            && "Chris".equals(r.instructor)
                            && "LO".equals(r.campus)
                            && "P".equals(r.pacingStructure)
                            && "T R".equals(r.mtgDays)
                            && "WB138".equals(r.classroomId)
                            && date15.equals(r.lstStcrsCreatDt)
                            && "U".equals(r.gradingStd)
                            && Integer.valueOf(64).equals(r.aMinScore)
                            && Integer.valueOf(61).equals(r.bMinScore)
                            && Integer.valueOf(53).equals(r.cMinScore)
                            && Integer.valueOf(0).equals(r.dMinScore)
                            && "SUR-3".equals(r.surveyId)
                            && "1".equals(r.courseLabelShown)
                            && "2".equals(r.displayScore)
                            && "3".equals(r.displayGradeScale)
                            && "4".equals(r.countInMaxCourses)
                            && "5".equals(r.online)
                            && "6".equals(r.bogus)
                            && "CANVAS-3".equals(r.canvasId)
                            && "HALF".equals(r.subterm)) {

                        found3 = true;

                    } else {
                        Log.warning("Bad termKey", r.termKey);
                        Log.warning("Bad course ", r.course);
                        Log.warning("Bad sect ", r.sect);
                        Log.warning("Bad sectionId ", r.sectionId);
                        Log.warning("Bad ariesStartDt ", r.ariesStartDt);
                        Log.warning("Bad ariesEndDt ", r.ariesEndDt);
                        Log.warning("Bad startDt ", r.startDt);
                        Log.warning("Bad examDeleteDt ", r.examDeleteDt);
                        Log.warning("Bad instrnType ", r.instrnType);
                        Log.warning("Bad instructor ", r.instructor);
                        Log.warning("Bad campus ", r.campus);
                        Log.warning("Bad pacingStructure ", r.pacingStructure);
                        Log.warning("Bad mtgDays ", r.mtgDays);
                        Log.warning("Bad classroomId ", r.classroomId);
                        Log.warning("Bad lstStcrsCreatDt ", r.lstStcrsCreatDt);
                        Log.warning("Bad gradingStd ", r.gradingStd);
                        Log.warning("Bad aMinScore ", r.aMinScore);
                        Log.warning("Bad bMinScore ", r.bMinScore);
                        Log.warning("Bad cMinScore ", r.cMinScore);
                        Log.warning("Bad dMinScore ", r.dMinScore);
                        Log.warning("Bad surveyId ", r.surveyId);
                        Log.warning("Bad courseLabelShown ", r.courseLabelShown);
                        Log.warning("Bad displayScore ", r.displayScore);
                        Log.warning("Bad displayGradeScale ", r.displayGradeScale);
                        Log.warning("Bad countInMaxCourses ", r.countInMaxCourses);
                        Log.warning("Bad online ", r.online);
                        Log.warning("Bad bogus ", r.bogus);
                        Log.warning("Bad canvasId ", r.canvasId);
                        Log.warning("Bad subterm ", r.subterm);
                    }
                }

                assertTrue(found1, "csection 1 not found");
                assertTrue(found2, "csection 2 not found");
                assertTrue(found3, "csection 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all csection rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByTerm results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawCsection> all = RawCsectionLogic.queryByTerm(cache, fa21);

                assertEquals(2, all.size(), "Incorrect record count from queryByTerm");

                boolean found1 = false;
                boolean found2 = false;

                for (final RawCsection r : all) {

                    if (fa21.equals(r.termKey)
                            && RawRecordConstants.M117.equals(r.course)
                            && "001".equals(r.sect)
                            && "12345".equals(r.sectionId)
                            && date1.equals(r.ariesStartDt)
                            && date2.equals(r.ariesEndDt)
                            && date3.equals(r.startDt)
                            && date4.equals(r.examDeleteDt)
                            && "RI".equals(r.instrnType)
                            && "Alice".equals(r.instructor)
                            && "FC".equals(r.campus)
                            && "M".equals(r.pacingStructure)
                            && "MTWRF".equals(r.mtgDays)
                            && "WB136".equals(r.classroomId)
                            && date5.equals(r.lstStcrsCreatDt)
                            && "S".equals(r.gradingStd)
                            && Integer.valueOf(65).equals(r.aMinScore)
                            && Integer.valueOf(62).equals(r.bMinScore)
                            && Integer.valueOf(54).equals(r.cMinScore)
                            && Integer.valueOf(1).equals(r.dMinScore)
                            && "SUR-1".equals(r.surveyId)
                            && "A".equals(r.courseLabelShown)
                            && "B".equals(r.displayScore)
                            && "C".equals(r.displayGradeScale)
                            && "D".equals(r.countInMaxCourses)
                            && "E".equals(r.online)
                            && "F".equals(r.bogus)
                            && "CANVAS-1".equals(r.canvasId)
                            && "FULL".equals(r.subterm)) {

                        found1 = true;

                    } else if (fa21.equals(r.termKey)
                            && RawRecordConstants.M117.equals(r.course)
                            && "401".equals(r.sect)
                            && "12346".equals(r.sectionId)
                            && date6.equals(r.ariesStartDt)
                            && date7.equals(r.ariesEndDt)
                            && date8.equals(r.startDt)
                            && date9.equals(r.examDeleteDt)
                            && "CE".equals(r.instrnType)
                            && "Bob".equals(r.instructor)
                            && "LA".equals(r.campus)
                            && "O".equals(r.pacingStructure)
                            && "M W F".equals(r.mtgDays)
                            && "WB137".equals(r.classroomId)
                            && date10.equals(r.lstStcrsCreatDt)
                            && "T".equals(r.gradingStd)
                            && Integer.valueOf(66).equals(r.aMinScore)
                            && Integer.valueOf(63).equals(r.bMinScore)
                            && Integer.valueOf(55).equals(r.cMinScore)
                            && Integer.valueOf(2).equals(r.dMinScore)
                            && "SUR-2".equals(r.surveyId)
                            && "Z".equals(r.courseLabelShown)
                            && "Y".equals(r.displayScore)
                            && "X".equals(r.displayGradeScale)
                            && "W".equals(r.countInMaxCourses)
                            && "V".equals(r.online)
                            && "U".equals(r.bogus)
                            && "CANVAS-2".equals(r.canvasId)
                            && "LATE".equals(r.subterm)) {

                        found2 = true;

                    } else {
                        Log.warning("Bad termKey ", r.termKey);
                        Log.warning("Bad course ", r.course);
                        Log.warning("Bad sect ", r.sect);
                        Log.warning("Bad ariesStartDt ", r.ariesStartDt);
                        Log.warning("Bad ariesEndDt ", r.ariesEndDt);
                        Log.warning("Bad startDt ", r.startDt);
                        Log.warning("Bad examDeleteDt ", r.examDeleteDt);
                        Log.warning("Bad instrnType ", r.instrnType);
                        Log.warning("Bad instructor ", r.instructor);
                        Log.warning("Bad campus ", r.campus);
                        Log.warning("Bad pacingStructure ", r.pacingStructure);
                        Log.warning("Bad mtgDays ", r.mtgDays);
                        Log.warning("Bad classroomId ", r.classroomId);
                        Log.warning("Bad lstStcrsCreatDt ", r.lstStcrsCreatDt);
                        Log.warning("Bad gradingStd ", r.gradingStd);
                        Log.warning("Bad aMinScore ", r.aMinScore);
                        Log.warning("Bad bMinScore ", r.bMinScore);
                        Log.warning("Bad cMinScore ", r.cMinScore);
                        Log.warning("Bad dMinScore ", r.dMinScore);
                        Log.warning("Bad surveyId ", r.surveyId);
                        Log.warning("Bad courseLabelShown ", r.courseLabelShown);
                        Log.warning("Bad displayScore ", r.displayScore);
                        Log.warning("Bad displayGradeScale ", r.displayGradeScale);
                        Log.warning("Bad countInMaxCourses ", r.countInMaxCourses);
                        Log.warning("Bad online ", r.online);
                        Log.warning("Bad bogus ", r.bogus);
                        Log.warning("Bad canvasId ", r.canvasId);
                        Log.warning("Bad subterm ", r.subterm);
                    }
                }

                assertTrue(found1, "csection 1 not found");
                assertTrue(found2, "csection 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying csections by term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0009() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawCsection raw2 = new RawCsection(fa21, RawRecordConstants.M117, "401", "12346", date6, date7,
                        date8, date9, "CE", "Bob", "LA", "O", "M W F", "WB137", date10, "T", Integer.valueOf(66),
                        Integer.valueOf(63), Integer.valueOf(55), Integer.valueOf(2), "SUR-2", "Z", "Y", "X", "W", "V",
                        "U", "CANVAS-2", "LATE");

                final boolean result = RawCsectionLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawCsection> all = RawCsectionLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawCsection r : all) {

                    if (fa21.equals(r.termKey)
                            && RawRecordConstants.M117.equals(r.course)
                            && "001".equals(r.sect)
                            && "12345".equals(r.sectionId)
                            && date1.equals(r.ariesStartDt)
                            && date2.equals(r.ariesEndDt)
                            && date3.equals(r.startDt)
                            && date4.equals(r.examDeleteDt)
                            && "RI".equals(r.instrnType)
                            && "Alice".equals(r.instructor)
                            && "FC".equals(r.campus)
                            && "M".equals(r.pacingStructure)
                            && "MTWRF".equals(r.mtgDays)
                            && "WB136".equals(r.classroomId)
                            && date5.equals(r.lstStcrsCreatDt)
                            && "S".equals(r.gradingStd)
                            && Integer.valueOf(65).equals(r.aMinScore)
                            && Integer.valueOf(62).equals(r.bMinScore)
                            && Integer.valueOf(54).equals(r.cMinScore)
                            && Integer.valueOf(1).equals(r.dMinScore)
                            && "SUR-1".equals(r.surveyId)
                            && "A".equals(r.courseLabelShown)
                            && "B".equals(r.displayScore)
                            && "C".equals(r.displayGradeScale)
                            && "D".equals(r.countInMaxCourses)
                            && "E".equals(r.online)
                            && "F".equals(r.bogus)
                            && "CANVAS-1".equals(r.canvasId)
                            && "FULL".equals(r.subterm)) {

                        found1 = true;

                    } else if (fa20.equals(r.termKey)
                            && RawRecordConstants.M117.equals(r.course)
                            && "001".equals(r.sect)
                            && "12347".equals(r.sectionId)
                            && date11.equals(r.ariesStartDt)
                            && date12.equals(r.ariesEndDt)
                            && date13.equals(r.startDt)
                            && date14.equals(r.examDeleteDt)
                            && "OT".equals(r.instrnType)
                            && "Chris".equals(r.instructor)
                            && "LO".equals(r.campus)
                            && "P".equals(r.pacingStructure)
                            && "T R".equals(r.mtgDays)
                            && "WB138".equals(r.classroomId)
                            && date15.equals(r.lstStcrsCreatDt)
                            && "U".equals(r.gradingStd)
                            && Integer.valueOf(64).equals(r.aMinScore)
                            && Integer.valueOf(61).equals(r.bMinScore)
                            && Integer.valueOf(53).equals(r.cMinScore)
                            && Integer.valueOf(0).equals(r.dMinScore)
                            && "SUR-3".equals(r.surveyId)
                            && "1".equals(r.courseLabelShown)
                            && "2".equals(r.displayScore)
                            && "3".equals(r.displayGradeScale)
                            && "4".equals(r.countInMaxCourses)
                            && "5".equals(r.online)
                            && "6".equals(r.bogus)
                            && "CANVAS-3".equals(r.canvasId)
                            && "HALF".equals(r.subterm)) {

                        found3 = true;

                    } else {
                        Log.warning("Bad termKey", r.termKey);
                        Log.warning("Bad course ", r.course);
                        Log.warning("Bad sect ", r.sect);
                        Log.warning("Bad sectionId ", r.sectionId);
                        Log.warning("Bad ariesStartDt ", r.ariesStartDt);
                        Log.warning("Bad ariesEndDt ", r.ariesEndDt);
                        Log.warning("Bad startDt ", r.startDt);
                        Log.warning("Bad examDeleteDt ", r.examDeleteDt);
                        Log.warning("Bad instrnType ", r.instrnType);
                        Log.warning("Bad instructor ", r.instructor);
                        Log.warning("Bad campus ", r.campus);
                        Log.warning("Bad pacingStructure ", r.pacingStructure);
                        Log.warning("Bad mtgDays ", r.mtgDays);
                        Log.warning("Bad classroomId ", r.classroomId);
                        Log.warning("Bad lstStcrsCreatDt ", r.lstStcrsCreatDt);
                        Log.warning("Bad gradingStd ", r.gradingStd);
                        Log.warning("Bad aMinScore ", r.aMinScore);
                        Log.warning("Bad bMinScore ", r.bMinScore);
                        Log.warning("Bad cMinScore ", r.cMinScore);
                        Log.warning("Bad dMinScore ", r.dMinScore);
                        Log.warning("Bad surveyId ", r.surveyId);
                        Log.warning("Bad courseLabelShown ", r.courseLabelShown);
                        Log.warning("Bad displayScore ", r.displayScore);
                        Log.warning("Bad displayGradeScale ", r.displayGradeScale);
                        Log.warning("Bad countInMaxCourses ", r.countInMaxCourses);
                        Log.warning("Bad online ", r.online);
                        Log.warning("Bad bogus ", r.bogus);
                        Log.warning("Bad canvasId ", r.canvasId);
                        Log.warning("Bad subterm ", r.subterm);
                    }
                }

                assertTrue(found1, "csection 1 not found");
                assertTrue(found3, "csection 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting csection: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM csection");
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

package dev.mathops.db.old.rawlogic;

import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;

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
import java.time.LocalDate;
import java.util.List;

/**
 * Tests for the {@code RawSttermLogic} class.
 */
final class TestRawSttermLogic {

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
                    stmt.executeUpdate("DELETE FROM stterm");
                    stmt.executeUpdate("DELETE FROM term");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final TermKey fa21 = new TermKey("FA21");
                final TermKey fa20 = new TermKey("FA20");

                final TermRec rawTerm = new TermRec(fa21, LocalDate.of(2021, 8, 11), LocalDate.of(2021, 12, 14), "2122",
                        Integer.valueOf(0), LocalDate.of(2021, 11, 13), LocalDate.of(2021, 11, 14));

                assertTrue(TermLogic.get(cache).insert(cache, rawTerm), "Failed to insert active term");

                final RawStterm raw1 = new RawStterm(fa21, "111111111", Integer.valueOf(1), "A",
                        RawRecordConstants.M118, "COH1", Integer.valueOf(10), "N");

                final RawStterm raw2 = new RawStterm(fa21, "222222222", Integer.valueOf(2), "B",
                        RawRecordConstants.M126, "COH2", Integer.valueOf(11), "Y");

                final RawStterm raw3 = new RawStterm(fa20, "111111111", Integer.valueOf(2), "B",
                        RawRecordConstants.M117, "COH0", Integer.valueOf(12), "Z");

                assertTrue(RawSttermLogic.INSTANCE.insert(cache, raw1), "Failed to insert stterm 1");
                assertTrue(RawSttermLogic.INSTANCE.insert(cache, raw2), "Failed to insert stterm 2");
                assertTrue(RawSttermLogic.INSTANCE.insert(cache, raw3), "Failed to insert stterm 3");
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
                final List<RawStterm> all = RawSttermLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                final TermKey fa21 = new TermKey("FA21");
                final TermKey fa20 = new TermKey("FA20");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawStterm test : all) {
                    if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && Integer.valueOf(1).equals(test.pace)
                            && "A".equals(test.paceTrack)
                            && RawRecordConstants.M118.equals(test.firstCourse)
                            && "COH1".equals(test.cohort)
                            && Integer.valueOf(10).equals(test.urgency)) {

                        found1 = true;
                    } else if (fa21.equals(test.termKey)
                            && "222222222".equals(test.stuId)
                            && Integer.valueOf(2).equals(test.pace)
                            && "B".equals(test.paceTrack)
                            && RawRecordConstants.M126.equals(test.firstCourse)
                            && "COH2".equals(test.cohort)
                            && Integer.valueOf(11).equals(test.urgency)) {

                        found2 = true;
                    } else if (fa20.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && Integer.valueOf(2).equals(test.pace)
                            && "B".equals(test.paceTrack)
                            && RawRecordConstants.M117.equals(test.firstCourse)
                            && "COH0".equals(test.cohort)
                            && Integer.valueOf(12).equals(test.urgency)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected pace ", test.pace);
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected paceTrack ", test.paceTrack);
                        Log.warning("Unexpected firstCourse ", test.firstCourse);
                        Log.warning("Unexpected cohort ", test.cohort);
                        Log.warning("Unexpected urgency ", test.urgency);
                    }
                }

                assertTrue(found1, "Stterm 1 not found");
                assertTrue(found2, "Stterm 2 not found");
                assertTrue(found3, "Stterm 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stterm rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAllByTerm results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final TermKey fa21 = new TermKey("FA21");
                final List<RawStterm> all = RawSttermLogic.queryAllByTerm(cache, fa21);

                assertEquals(2, all.size(), "Incorrect record count from queryAllByTerm");

                boolean found1 = false;
                boolean found2 = false;

                for (final RawStterm test : all) {
                    if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && Integer.valueOf(1).equals(test.pace)
                            && "A".equals(test.paceTrack)
                            && RawRecordConstants.M118.equals(test.firstCourse)
                            && "COH1".equals(test.cohort)
                            && Integer.valueOf(10).equals(test.urgency)) {

                        found1 = true;
                    } else if (fa21.equals(test.termKey)
                            && "222222222".equals(test.stuId)
                            && Integer.valueOf(2).equals(test.pace)
                            && "B".equals(test.paceTrack)
                            && RawRecordConstants.M126.equals(test.firstCourse)
                            && "COH2".equals(test.cohort)
                            && Integer.valueOf(11).equals(test.urgency)) {

                        found2 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected pace ", test.pace);
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected paceTrack ", test.paceTrack);
                        Log.warning("Unexpected firstCourse ", test.firstCourse);
                        Log.warning("Unexpected cohort ", test.cohort);
                        Log.warning("Unexpected urgency ", test.urgency);
                    }
                }

                assertTrue(found1, "Stterm 1 not found");
                assertTrue(found2, "Stterm 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stterm rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStterm> all = RawSttermLogic.queryByStudent(cache, "111111111");

                assertEquals(2, all.size(), "Incorrect record count from queryByStudent");

                final TermKey fa21 = new TermKey("FA21");
                final TermKey fa20 = new TermKey("FA20");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawStterm test : all) {
                    if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && Integer.valueOf(1).equals(test.pace)
                            && "A".equals(test.paceTrack)
                            && RawRecordConstants.M118.equals(test.firstCourse)
                            && "COH1".equals(test.cohort)
                            && Integer.valueOf(10).equals(test.urgency)) {

                        found1 = true;
                    } else if (fa20.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && Integer.valueOf(2).equals(test.pace)
                            && "B".equals(test.paceTrack)
                            && RawRecordConstants.M117.equals(test.firstCourse)
                            && "COH0".equals(test.cohort)
                            && Integer.valueOf(12).equals(test.urgency)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected pace ", test.pace);
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected paceTrack ", test.paceTrack);
                        Log.warning("Unexpected firstCourse ", test.firstCourse);
                        Log.warning("Unexpected cohort ", test.cohort);
                        Log.warning("Unexpected urgency ", test.urgency);
                    }
                }

                assertTrue(found1, "Stterm 1 not found");
                assertTrue(found3, "Stterm 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stterm by student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudentEtext results")
    void test0006() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final TermRec active = TermLogic.get(cache).queryActive(cache);

                final RawStterm test = RawSttermLogic.query(cache, active.term, "111111111");

                assertNotNull(test, "No record from query");

                final TermKey fa21 = new TermKey("FA21");

                boolean found = false;

                if (fa21.equals(test.termKey)
                        && "111111111".equals(test.stuId)
                        && Integer.valueOf(1).equals(test.pace)
                        && "A".equals(test.paceTrack)
                        && RawRecordConstants.M118.equals(test.firstCourse)
                        && "COH1".equals(test.cohort)
                        && Integer.valueOf(10).equals(test.urgency)) {

                    found = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected pace ", test.pace);
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected paceTrack ", test.paceTrack);
                    Log.warning("Unexpected firstCourse ", test.firstCourse);
                    Log.warning("Unexpected cohort ", test.cohort);
                    Log.warning("Unexpected urgency ", test.urgency);
                }

                assertTrue(found, "Stterm not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying stterm rows by term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query after updatePaceTrackFirstCourse results")
    void test0007() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final TermRec active = TermLogic.get(cache).queryActive(cache);

                assertTrue(RawSttermLogic.updatePaceTrackFirstCourse(cache, "111111111", active.term, 5, "Z",
                        RawRecordConstants.M124), "updatePaceTrackFirstCourse returned false");

                final RawStterm test = RawSttermLogic.query(cache, active.term, "111111111");

                assertNotNull(test, "No record from query");

                boolean found = false;

                if (active.term.equals(test.termKey)
                        && "111111111".equals(test.stuId)
                        && Integer.valueOf(5).equals(test.pace)
                        && "Z".equals(test.paceTrack)
                        && RawRecordConstants.M124.equals(test.firstCourse)
                        && "COH1".equals(test.cohort)
                        && Integer.valueOf(10).equals(test.urgency)) {

                    found = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected pace ", test.pace);
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected paceTrack ", test.paceTrack);
                    Log.warning("Unexpected firstCourse ", test.firstCourse);
                    Log.warning("Unexpected cohort ", test.cohort);
                    Log.warning("Unexpected urgency ", test.urgency);
                }

                assertTrue(found, "Stterm not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updatePaceTrackFirstCourse: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query after updateCohort results")
    void test0008() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final TermRec active = TermLogic.get(cache).queryActive(cache);

                assertTrue(RawSttermLogic.updateCohort(cache, "111111111", active.term, "FOO"),
                        "updateCohort returned false");

                final RawStterm test = RawSttermLogic.query(cache, active.term, "111111111");

                assertNotNull(test, "No record from query");

                boolean found = false;

                if (active.term.equals(test.termKey)
                        && "111111111".equals(test.stuId)
                        && Integer.valueOf(5).equals(test.pace)
                        && "Z".equals(test.paceTrack)
                        && RawRecordConstants.M124.equals(test.firstCourse) && "FOO".equals(test.cohort)
                        && Integer.valueOf(10).equals(test.urgency)) {

                    found = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected pace ", test.pace);
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected paceTrack ", test.paceTrack);
                    Log.warning("Unexpected firstCourse ", test.firstCourse);
                    Log.warning("Unexpected cohort ", test.cohort);
                    Log.warning("Unexpected urgency ", test.urgency);
                }

                assertTrue(found, "Stterm not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateCohort: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query after updateUrgency results")
    void test0009() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final TermRec active = TermLogic.get(cache).queryActive(cache);

                assertTrue(RawSttermLogic.updateUrgency(cache, "111111111", active.term, Integer.valueOf(20)),
                        "updateUrgency returned false");

                final RawStterm test = RawSttermLogic.query(cache, active.term, "111111111");

                assertNotNull(test, "No record from query");

                boolean found = false;

                if (active.term.equals(test.termKey)
                        && "111111111".equals(test.stuId)
                        && Integer.valueOf(5).equals(test.pace)
                        && "Z".equals(test.paceTrack)
                        && RawRecordConstants.M124.equals(test.firstCourse)
                        && "FOO".equals(test.cohort)
                        && Integer.valueOf(20).equals(test.urgency)) {

                    found = true;
                } else {
                    Log.warning("Unexpected stuId ", test.stuId);
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected pace ", test.pace);
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected paceTrack ", test.paceTrack);
                    Log.warning("Unexpected firstCourse ", test.firstCourse);
                    Log.warning("Unexpected cohort ", test.cohort);
                    Log.warning("Unexpected urgency ", test.urgency);
                }

                assertTrue(found, "Stterm not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateUrgency: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query after delete results")
    void test0010() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final TermRec active = TermLogic.get(cache).queryActive(cache);

                final RawStterm toDelete = RawSttermLogic.query(cache, active.term, "111111111");

                assertNotNull(toDelete, "No record from query");

                assertTrue(RawSttermLogic.INSTANCE.delete(cache, toDelete), "delete() returned false");

                final RawStterm test = RawSttermLogic.query(cache, active.term, "111111111");

                assertNull(test, "Record returned from query after delete");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after delete: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM stterm");
                    stmt.executeUpdate("DELETE FROM term");
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

package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawrecord.RawStmilestone;

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
 * Tests for the {@code RawStmilestoneLogic} class.
 */
final class TestRawStmilestoneLogic {

    /** A term key used in test records. */
    private static final TermKey fa21 = new TermKey(ETermName.FALL, 2021);

    /** A term key used in test records. */
    private static final TermKey sp22 = new TermKey(ETermName.SPRING, 2022);

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 12, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 12, 3);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 12, 4);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2022, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2023, 4, 5);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2021, 12, 15);

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
                    stmt.executeUpdate("DELETE FROM stmilestone");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawStmilestone raw1 = new RawStmilestone(fa21, "111111111", "A", Integer.valueOf(541), "RE",
                        date1, Integer.valueOf(1));
                final RawStmilestone raw2 = new RawStmilestone(fa21, "111111111", "A", Integer.valueOf(542), "UE",
                        date2, Integer.valueOf(2));
                final RawStmilestone raw3 = new RawStmilestone(fa21, "111111111", "B", Integer.valueOf(543), "FE",
                        date3, Integer.valueOf(3));
                final RawStmilestone raw4 = new RawStmilestone(fa21, "222222222", "C", Integer.valueOf(432), "RE",
                        date4, Integer.valueOf(0));
                final RawStmilestone raw5 = new RawStmilestone(sp22, "222222222", "D", Integer.valueOf(123), "F1",
                        date5, Integer.valueOf(4));

                assertTrue(RawStmilestoneLogic.INSTANCE.insert(cache, raw1), "Failed to insert stmilestone 1");
                assertTrue(RawStmilestoneLogic.INSTANCE.insert(cache, raw2), "Failed to insert stmilestone 2");
                assertTrue(RawStmilestoneLogic.INSTANCE.insert(cache, raw3), "Failed to insert stmilestone 3");
                assertTrue(RawStmilestoneLogic.INSTANCE.insert(cache, raw4), "Failed to insert stmilestone 4");
                assertTrue(RawStmilestoneLogic.INSTANCE.insert(cache, raw5), "Failed to insert stmilestone 5");
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
                final List<RawStmilestone> all = RawStmilestoneLogic.INSTANCE.queryAll(cache);

                assertEquals(5, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;
                boolean found4 = false;
                boolean found5 = false;

                for (final RawStmilestone test : all) {
                    if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(541).equals(test.msNbr)
                            && "RE".equals(test.msType)
                            && date1.equals(test.msDate)
                            && Integer.valueOf(1).equals(test.nbrAtmptsAllow)) {

                        found1 = true;
                    } else if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(542).equals(test.msNbr)
                            && "UE".equals(test.msType)
                            && date2.equals(test.msDate)
                            && Integer.valueOf(2).equals(test.nbrAtmptsAllow)) {

                        found2 = true;
                    } else if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && "B".equals(test.paceTrack)
                            && Integer.valueOf(543).equals(test.msNbr)
                            && "FE".equals(test.msType)
                            && date3.equals(test.msDate)
                            && Integer.valueOf(3).equals(test.nbrAtmptsAllow)) {

                        found3 = true;
                    } else if (fa21.equals(test.termKey)
                            && "222222222".equals(test.stuId)
                            && "C".equals(test.paceTrack)
                            && Integer.valueOf(432).equals(test.msNbr)
                            && "RE".equals(test.msType)
                            && date4.equals(test.msDate)
                            && Integer.valueOf(0).equals(test.nbrAtmptsAllow)) {

                        found4 = true;
                    } else if (sp22.equals(test.termKey)
                            && "222222222".equals(test.stuId)
                            && "D".equals(test.paceTrack)
                            && Integer.valueOf(123).equals(test.msNbr)
                            && "F1".equals(test.msType)
                            && date5.equals(test.msDate)
                            && Integer.valueOf(4).equals(test.nbrAtmptsAllow)) {

                        found5 = true;
                    } else {
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected paceTrack ", test.paceTrack);
                        Log.warning("Unexpected msNbr ", test.msNbr);
                        Log.warning("Unexpected msType ", test.msType);
                        Log.warning("Unexpected msDate ", test.msDate);
                        Log.warning("Unexpected nbrAtmptsAllow ", test.nbrAtmptsAllow);
                    }
                }

                assertTrue(found1, "Stmilestone 1 not found");
                assertTrue(found2, "Stmilestone 2 not found");
                assertTrue(found3, "Stmilestone 3 not found");
                assertTrue(found4, "Stmilestone 4 not found");
                assertTrue(found5, "Stmilestone 5 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all stmilestone rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getStudentMilestones(TermKey,String) results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStmilestone> all = RawStmilestoneLogic.getStudentMilestones(cache, fa21,
                        "111111111");

                assertEquals(3, all.size(), "Incorrect record count from getStudentMilestones(TermKey,String)");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawStmilestone test : all) {
                    if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(541).equals(test.msNbr)
                            && "RE".equals(test.msType)
                            && date1.equals(test.msDate)
                            && Integer.valueOf(1).equals(test.nbrAtmptsAllow)) {

                        found1 = true;
                    } else if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(542).equals(test.msNbr)
                            && "UE".equals(test.msType)
                            && date2.equals(test.msDate)
                            && Integer.valueOf(2).equals(test.nbrAtmptsAllow)) {

                        found2 = true;
                    } else if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && "B".equals(test.paceTrack)
                            && Integer.valueOf(543).equals(test.msNbr)
                            && "FE".equals(test.msType)
                            && date3.equals(test.msDate)
                            && Integer.valueOf(3).equals(test.nbrAtmptsAllow)) {

                        found3 = true;
                    } else {
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected paceTrack ", test.paceTrack);
                        Log.warning("Unexpected msNbr ", test.msNbr);
                        Log.warning("Unexpected msType ", test.msType);
                        Log.warning("Unexpected msDate ", test.msDate);
                        Log.warning("Unexpected nbrAtmptsAllow ", test.nbrAtmptsAllow);
                    }
                }

                assertTrue(found1, "Stmilestone 1 not found");
                assertTrue(found2, "Stmilestone 2 not found");
                assertTrue(found3, "Stmilestone 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying latest stmilestone by term, stu_id: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getStudentMilestones(TermKey,String,String) results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStmilestone> all = RawStmilestoneLogic.getStudentMilestones(cache, fa21, "A",
                        "111111111");

                assertEquals(2, all.size(), "Incorrect record count from getStudentMilestones(TermKey,String,String)");

                boolean found1 = false;
                boolean found2 = false;

                for (final RawStmilestone test : all) {
                    if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(541).equals(test.msNbr)
                            && "RE".equals(test.msType)
                            && date1.equals(test.msDate)
                            && Integer.valueOf(1).equals(test.nbrAtmptsAllow)) {

                        found1 = true;
                    } else if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(542).equals(test.msNbr)
                            && "UE".equals(test.msType)
                            && date2.equals(test.msDate)
                            && Integer.valueOf(2).equals(test.nbrAtmptsAllow)) {

                        found2 = true;
                    } else {
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected paceTrack ", test.paceTrack);
                        Log.warning("Unexpected msNbr ", test.msNbr);
                        Log.warning("Unexpected msType ", test.msType);
                        Log.warning("Unexpected msDate ", test.msDate);
                        Log.warning("Unexpected nbrAtmptsAllow ", test.nbrAtmptsAllow);
                    }
                }

                assertTrue(found1, "Stmilestone 1 not found");
                assertTrue(found2, "Stmilestone 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying latest stmilestone by term, track, stu_id: " + ex.getMessage());
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
                final RawStmilestone raw1 = new RawStmilestone(fa21, "111111111", "A", Integer.valueOf(541), "RE",
                        date6, Integer.valueOf(100));

                final boolean result = RawStmilestoneLogic.update(cache, raw1);

                assertTrue(result, "False result from update");

                final List<RawStmilestone> all = RawStmilestoneLogic.getStudentMilestones(cache, fa21, "A",
                        "111111111");

                assertEquals(2, all.size(), "Incorrect record count after update");

                boolean found1 = false;
                boolean found2 = false;

                for (final RawStmilestone test : all) {
                    if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(541).equals(test.msNbr)
                            && "RE".equals(test.msType)
                            && date6.equals(test.msDate)
                            && Integer.valueOf(100).equals(test.nbrAtmptsAllow)) {

                        found1 = true;
                    } else if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(542).equals(test.msNbr)
                            && "UE".equals(test.msType)
                            && date2.equals(test.msDate)
                            && Integer.valueOf(2).equals(test.nbrAtmptsAllow)) {

                        found2 = true;
                    } else {
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected paceTrack ", test.paceTrack);
                        Log.warning("Unexpected msNbr ", test.msNbr);
                        Log.warning("Unexpected msType ", test.msType);
                        Log.warning("Unexpected msDate ", test.msDate);
                        Log.warning("Unexpected nbrAtmptsAllow ", test.nbrAtmptsAllow);
                    }
                }

                assertTrue(found1, "Stmilestone 1 not found");
                assertTrue(found2, "Stmilestone 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after update: " + ex.getMessage());
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
                final RawStmilestone raw1 = new RawStmilestone(fa21, "111111111", "A", Integer.valueOf(541), "RE",
                        date6, Integer.valueOf(100));

                final boolean result = RawStmilestoneLogic.INSTANCE.delete(cache, raw1);

                assertTrue(result, "False result from delete");

                final List<RawStmilestone> all = RawStmilestoneLogic.getStudentMilestones(cache, fa21, "A",
                        "111111111");

                assertEquals(1, all.size(), "Incorrect record count after delete");

                boolean found2 = false;

                for (final RawStmilestone test : all) {
                    if (fa21.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(542).equals(test.msNbr)
                            && "UE".equals(test.msType)
                            && date2.equals(test.msDate)
                            && Integer.valueOf(2).equals(test.nbrAtmptsAllow)) {

                        found2 = true;
                    } else {
                        Log.warning("Unexpected termKey ", test.termKey);
                        Log.warning("Unexpected stuId ", test.stuId);
                        Log.warning("Unexpected paceTrack ", test.paceTrack);
                        Log.warning("Unexpected msNbr ", test.msNbr);
                        Log.warning("Unexpected msType ", test.msType);
                        Log.warning("Unexpected msDate ", test.msDate);
                        Log.warning("Unexpected nbrAtmptsAllow ", test.nbrAtmptsAllow);
                    }
                }

                assertTrue(found2, "Stmilestone 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying latest stmilestone after delete: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM stmilestone");
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

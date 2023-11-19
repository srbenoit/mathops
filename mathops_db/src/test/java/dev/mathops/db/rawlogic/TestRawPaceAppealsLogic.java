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
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.rawrecord.RawPaceAppeals;

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
 * Tests for the {@code RawPaceAppeals} classes.
 */
final class TestRawPaceAppealsLogic {

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(2021, 1, 1);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(2021, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(2021, 1, 3);

    /** A date used in test records. */
    private static final LocalDate date4 = LocalDate.of(2021, 1, 4);

    /** A date used in test records. */
    private static final LocalDate date5 = LocalDate.of(2021, 1, 5);

    /** A date used in test records. */
    private static final LocalDate date6 = LocalDate.of(2021, 2, 1);

    /** A date used in test records. */
    private static final LocalDate date7 = LocalDate.of(2021, 2, 2);

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
                    stmt.executeUpdate("DELETE FROM pace_appeals");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final TermKey termKey = new TermKey(ETermName.SPRING, 2022);
                final RawPaceAppeals raw1 = new RawPaceAppeals(termKey, "111111111",
                        date1, "N", Integer.valueOf(1),
                        "A", Integer.valueOf(111), "RE",
                        date2, null, null, "SCD Letter",
                        "2x Time on exams", "benoit");

                final RawPaceAppeals raw2 = new RawPaceAppeals(termKey, "111111111",
                        date3, "Y", Integer.valueOf(1),
                        "A", Integer.valueOf(112), "RE",
                        date4, date5, Integer.valueOf(3),
                        "Doctor's Note", "Excused absence",
                        "benoit");

                final RawPaceAppeals raw3 = new RawPaceAppeals(termKey, "222222222",
                        date6, "N", Integer.valueOf(1),
                        "A", Integer.valueOf(111), "UE",
                        date7, null, null, "SCD Letter",
                        "Quiet testing room", "pattison");

                assertTrue(RawPaceAppealsLogic.INSTANCE.insert(cache, raw1), "Failed to insert pace_appeals");
                assertTrue(RawPaceAppealsLogic.INSTANCE.insert(cache, raw2), "Failed to insert pace_appeals");
                assertTrue(RawPaceAppealsLogic.INSTANCE.insert(cache, raw3), "Failed to insert pace_appeals");
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
                final List<RawPaceAppeals> all = RawPaceAppealsLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");
                final TermKey termKey = new TermKey(ETermName.SPRING, 2022);

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawPaceAppeals test : all) {

                    if (termKey.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && date1.equals(test.appealDt)
                            && "N".equals(test.reliefGiven)
                            && Integer.valueOf(1).equals(test.pace)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(111).equals(test.msNbr)
                            && "RE".equals(test.msType)
                            && date2.equals(test.msDate)
                            && test.newDeadlineDt == null
                            && test.nbrAtmptsAllow == null
                            && "SCD Letter".equals(test.circumstances)
                            && "2x Time on exams".equals(test.comment)
                            && "benoit".equals(test.interviewer)) {

                        found1 = true;
                    } else if (termKey.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && date3.equals(test.appealDt)
                            && "Y".equals(test.reliefGiven)
                            && Integer.valueOf(1).equals(test.pace)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(112).equals(test.msNbr)
                            && "RE".equals(test.msType)
                            && date4.equals(test.msDate)
                            && date5.equals(test.newDeadlineDt)
                            && Integer.valueOf(3).equals(test.nbrAtmptsAllow)
                            && "Doctor's Note".equals(test.circumstances)
                            && "Excused absence".equals(test.comment)
                            && "benoit".equals(test.interviewer)) {

                        found2 = true;
                    } else if (termKey.equals(test.termKey)
                            && "222222222".equals(test.stuId)
                            && date6.equals(test.appealDt)
                            && "N".equals(test.reliefGiven)
                            && Integer.valueOf(1).equals(test.pace)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(111).equals(test.msNbr)
                            && "UE".equals(test.msType)
                            && date7.equals(test.msDate)
                            && test.newDeadlineDt == null
                            && test.nbrAtmptsAllow == null
                            && "SCD Letter".equals(test.circumstances)
                            && "Quiet testing room".equals(test.comment)
                            && "pattison".equals(test.interviewer)) {

                        found3 = true;
                    } else {
                        Log.warning("Bad termKey ", test.termKey);
                        Log.warning("Bad stuId ", test.stuId);
                        Log.warning("Bad appealDt ", test.appealDt);
                        Log.warning("Bad reliefGiven ", test.reliefGiven);
                        Log.warning("Bad pace ", test.pace);
                        Log.warning("Bad paceTrack ", test.paceTrack);
                        Log.warning("Bad msNbr ", test.msNbr);
                        Log.warning("Bad msType ", test.msType);
                        Log.warning("Bad msDate ", test.msDate);
                        Log.warning("Bad newDeadlineDt ", test.newDeadlineDt);
                        Log.warning("Bad nbrAtmptsAllow ", test.nbrAtmptsAllow);
                        Log.warning("Bad circumstances ", test.circumstances);
                        Log.warning("Bad comment ", test.comment);
                        Log.warning("Bad interviewer ", test.interviewer);
                    }
                }

                assertTrue(found1, "PaceAppeals 1 not found");
                assertTrue(found2, "PaceAppeals 2 not found");
                assertTrue(found3, "PaceAppeals 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all pace_appeals rows: " + ex.getMessage());
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
                final List<RawPaceAppeals> all = RawPaceAppealsLogic.queryByStudent(cache, "111111111");

                assertEquals(2, all.size(), "Incorrect record count from queryByStudent");
                final TermKey termKey = new TermKey(ETermName.SPRING, 2022);

                boolean found1 = false;
                boolean found2 = false;

                for (final RawPaceAppeals test : all) {

                    if (termKey.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && date1.equals(test.appealDt)
                            && "N".equals(test.reliefGiven)
                            && Integer.valueOf(1).equals(test.pace)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(111).equals(test.msNbr)
                            && "RE".equals(test.msType)
                            && date2.equals(test.msDate)
                            && test.newDeadlineDt == null
                            && test.nbrAtmptsAllow == null
                            && "SCD Letter".equals(test.circumstances)
                            && "2x Time on exams".equals(test.comment)
                            && "benoit".equals(test.interviewer)) {

                        found1 = true;
                    } else if (termKey.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && date3.equals(test.appealDt)
                            && "Y".equals(test.reliefGiven)
                            && Integer.valueOf(1).equals(test.pace)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(112).equals(test.msNbr)
                            && "RE".equals(test.msType)
                            && date4.equals(test.msDate)
                            && date5.equals(test.newDeadlineDt)
                            && Integer.valueOf(3).equals(test.nbrAtmptsAllow)
                            && "Doctor's Note".equals(test.circumstances)
                            && "Excused absence".equals(test.comment)
                            && "benoit".equals(test.interviewer)) {

                        found2 = true;
                    } else {
                        Log.warning("Bad termKey ", test.termKey);
                        Log.warning("Bad stuId ", test.stuId);
                        Log.warning("Bad appealDt ", test.appealDt);
                        Log.warning("Bad reliefGiven ", test.reliefGiven);
                        Log.warning("Bad pace ", test.pace);
                        Log.warning("Bad paceTrack ", test.paceTrack);
                        Log.warning("Bad msNbr ", test.msNbr);
                        Log.warning("Bad msType ", test.msType);
                        Log.warning("Bad msDate ", test.msDate);
                        Log.warning("Bad newDeadlineDt ", test.newDeadlineDt);
                        Log.warning("Bad nbrAtmptsAllow ", test.nbrAtmptsAllow);
                        Log.warning("Bad circumstances ", test.circumstances);
                        Log.warning("Bad comment ", test.comment);
                        Log.warning("Bad interviewer ", test.interviewer);
                    }
                }

                assertTrue(found1, "PaceAppeals 1 not found");
                assertTrue(found2, "PaceAppeals 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all pace_appeals by student: " + ex.getMessage());
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
                final List<RawPaceAppeals> pre = RawPaceAppealsLogic.queryByStudent(cache, "222222222");

                assertEquals(1, pre.size(), "Incorrect record count from queryByStudent");

                RawPaceAppealsLogic.INSTANCE.delete(cache, pre.get(0));

                final List<RawPaceAppeals> all = RawPaceAppealsLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");
                final TermKey termKey = new TermKey(ETermName.SPRING, 2022);

                boolean found1 = false;
                boolean found2 = false;

                for (final RawPaceAppeals test : all) {

                    if (termKey.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && date1.equals(test.appealDt)
                            && "N".equals(test.reliefGiven)
                            && Integer.valueOf(1).equals(test.pace)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(111).equals(test.msNbr)
                            && "RE".equals(test.msType)
                            && date2.equals(test.msDate)
                            && test.newDeadlineDt == null
                            && test.nbrAtmptsAllow == null
                            && "SCD Letter".equals(test.circumstances)
                            && "2x Time on exams".equals(test.comment)
                            && "benoit".equals(test.interviewer)) {

                        found1 = true;
                    } else if (termKey.equals(test.termKey)
                            && "111111111".equals(test.stuId)
                            && date3.equals(test.appealDt)
                            && "Y".equals(test.reliefGiven)
                            && Integer.valueOf(1).equals(test.pace)
                            && "A".equals(test.paceTrack)
                            && Integer.valueOf(112).equals(test.msNbr)
                            && "RE".equals(test.msType)
                            && date4.equals(test.msDate)
                            && date5.equals(test.newDeadlineDt)
                            && Integer.valueOf(3).equals(test.nbrAtmptsAllow)
                            && "Doctor's Note".equals(test.circumstances)
                            && "Excused absence".equals(test.comment)
                            && "benoit".equals(test.interviewer)) {

                        found2 = true;
                    } else {
                        Log.warning("Bad termKey ", test.termKey);
                        Log.warning("Bad stuId ", test.stuId);
                        Log.warning("Bad appealDt ", test.appealDt);
                        Log.warning("Bad reliefGiven ", test.reliefGiven);
                        Log.warning("Bad pace ", test.pace);
                        Log.warning("Bad paceTrack ", test.paceTrack);
                        Log.warning("Bad msNbr ", test.msNbr);
                        Log.warning("Bad msType ", test.msType);
                        Log.warning("Bad msDate ", test.msDate);
                        Log.warning("Bad newDeadlineDt ", test.newDeadlineDt);
                        Log.warning("Bad nbrAtmptsAllow ", test.nbrAtmptsAllow);
                        Log.warning("Bad circumstances ", test.circumstances);
                        Log.warning("Bad comment ", test.comment);
                        Log.warning("Bad interviewer ", test.interviewer);
                    }
                }

                assertTrue(found1, "PaceAppeals 1 not found");
                assertTrue(found2, "PaceAppeals 2 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all pace_appeals by student: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM pace_appeals");
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

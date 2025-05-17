package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.type.TermKey;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawPacingStructureLogic} class.
 */
final class TestRawPacingStructureLogic {

    /** A term key used in test records. */
    private static final TermKey fa20 = new TermKey("FA20");

    /** A term key used in test records. */
    private static final TermKey fa21 = new TermKey("FA21");

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
                stmt.executeUpdate("DELETE FROM pacing_structure");
                stmt.executeUpdate("DELETE FROM term");
            }
            conn.commit();

            final TermRec term1 = new TermRec(fa21, LocalDate.of(2021, 8, 11), LocalDate.of(2021, 12, 14), "2122",
                    Integer.valueOf(0), LocalDate.of(2021, 11, 13), LocalDate.of(2021, 11, 14)
                    , LocalDate.of(2022, 10, 12));

            final RawPacingStructure raw1 = new RawPacingStructure(fa21, "A", "B", "C", "D", Integer.valueOf(1),
                    "E", Integer.valueOf(2), Integer.valueOf(3), "F", "G", "H", "I", Integer.valueOf(4),
                    Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8), "J", "K", "L",
                    "M", "N", "O", "P", null);

            final RawPacingStructure raw2 = new RawPacingStructure(fa20, "a", "b", "c", "d", Integer.valueOf(11),
                    "e", Integer.valueOf(12), Integer.valueOf(13), "f", "g", "h", "i", Integer.valueOf(14),
                    Integer.valueOf(15), Integer.valueOf(16), Integer.valueOf(17), Integer.valueOf(18), "j", "k",
                    "l", "m", "n", "o", "p", null);

            final RawPacingStructure raw3 = new RawPacingStructure(fa21, "Z", "Y", "X", "W", Integer.valueOf(0),
                    "V", Integer.valueOf(1), Integer.valueOf(2), "U", "T", "S", "R", Integer.valueOf(3),
                    Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(7), "Q", "P", "O",
                    "N", "M", "L", "K", null);

            assertTrue(cache.getSystemData().insertTerm(term1), "Failed to insert term 1");

            assertTrue(RawPacingStructureLogic.insert(cache, raw1), "Failed to insert pacing_structure 1");
            assertTrue(RawPacingStructureLogic.insert(cache, raw2), "Failed to insert pacing_structure 2");
            assertTrue(RawPacingStructureLogic.insert(cache, raw3), "Failed to insert pacing_structure 3");
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
            final List<RawPacingStructure> all = RawPacingStructureLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawPacingStructure test : all) {
                if (fa21.equals(test.termKey)
                    && "A".equals(test.pacingStructure)
                    && "B".equals(test.defPaceTrack)
                    && "C".equals(test.requireLicensed)
                    && "D".equals(test.requirePartic)
                    && Integer.valueOf(1).equals(test.maxParticMissed)
                    && "E".equals(test.allowInc)
                    && Integer.valueOf(2).equals(test.maxCourses)
                    && Integer.valueOf(3).equals(test.nbrOpenAllowed)
                    && "F".equals(test.requireUnitExams)
                    && "G".equals(test.useMidterms)
                    && "H".equals(test.allowCoupons)
                    && "I".equals(test.couponsAfterWindow)
                    && Integer.valueOf(4).equals(test.usersProgressCr)
                    && Integer.valueOf(5).equals(test.hwProgressCr)
                    && Integer.valueOf(6).equals(test.reProgressCr)
                    && Integer.valueOf(7).equals(test.ueProgressCr)
                    && Integer.valueOf(8).equals(test.finProgressCr)
                    && "J".equals(test.pacingName)
                    && "K".equals(test.scheduleSource)
                    && "L".equals(test.srDueDateEnforced)
                    && "M".equals(test.reDueDateEnforced)
                    && "N".equals(test.ueDueDateEnforced)
                    && "O".equals(test.feDueDateEnforced)
                    && "P".equals(test.firstObjAvail)) {

                    found1 = true;
                } else if (fa20.equals(test.termKey)
                           && "a".equals(test.pacingStructure)
                           && "b".equals(test.defPaceTrack)
                           && "c".equals(test.requireLicensed)
                           && "d".equals(test.requirePartic)
                           && Integer.valueOf(11).equals(test.maxParticMissed)
                           && "e".equals(test.allowInc)
                           && Integer.valueOf(12).equals(test.maxCourses)
                           && Integer.valueOf(13).equals(test.nbrOpenAllowed)
                           && "f".equals(test.requireUnitExams)
                           && "g".equals(test.useMidterms)
                           && "h".equals(test.allowCoupons)
                           && "i".equals(test.couponsAfterWindow)
                           && Integer.valueOf(14).equals(test.usersProgressCr)
                           && Integer.valueOf(15).equals(test.hwProgressCr)
                           && Integer.valueOf(16).equals(test.reProgressCr)
                           && Integer.valueOf(17).equals(test.ueProgressCr)
                           && Integer.valueOf(18).equals(test.finProgressCr)
                           && "j".equals(test.pacingName)
                           && "k".equals(test.scheduleSource)
                           && "l".equals(test.srDueDateEnforced)
                           && "m".equals(test.reDueDateEnforced)
                           && "n".equals(test.ueDueDateEnforced)
                           && "o".equals(test.feDueDateEnforced)
                           && "p".equals(test.firstObjAvail)) {

                    found2 = true;
                } else if (fa21.equals(test.termKey)
                           && "Z".equals(test.pacingStructure)
                           && "Y".equals(test.defPaceTrack)
                           && "X".equals(test.requireLicensed)
                           && "W".equals(test.requirePartic)
                           && Integer.valueOf(0).equals(test.maxParticMissed)
                           && "V".equals(test.allowInc)
                           && Integer.valueOf(1).equals(test.maxCourses)
                           && Integer.valueOf(2).equals(test.nbrOpenAllowed)
                           && "U".equals(test.requireUnitExams)
                           && "T".equals(test.useMidterms)
                           && "S".equals(test.allowCoupons)
                           && "R".equals(test.couponsAfterWindow)
                           && Integer.valueOf(3).equals(test.usersProgressCr)
                           && Integer.valueOf(4).equals(test.hwProgressCr)
                           && Integer.valueOf(5).equals(test.reProgressCr)
                           && Integer.valueOf(6).equals(test.ueProgressCr)
                           && Integer.valueOf(7).equals(test.finProgressCr)
                           && "Q".equals(test.pacingName)
                           && "P".equals(test.scheduleSource)
                           && "O".equals(test.srDueDateEnforced)
                           && "N".equals(test.reDueDateEnforced)
                           && "M".equals(test.ueDueDateEnforced)
                           && "L".equals(test.feDueDateEnforced)
                           && "K".equals(test.firstObjAvail)) {

                    found3 = true;
                } else {
                    Log.warning("Bad termKey ", test.termKey);
                    Log.warning("Bad pacingStructure ", test.pacingStructure);
                    Log.warning("Bad defPaceTrack ", test.defPaceTrack);
                    Log.warning("Bad requireLicensed ", test.requireLicensed);
                    Log.warning("Bad requirePartic ", test.requirePartic);
                    Log.warning("Bad maxParticMissed ", test.maxParticMissed);
                    Log.warning("Bad allowInc ", test.allowInc);
                    Log.warning("Bad maxCourses ", test.maxCourses);
                    Log.warning("Bad nbrOpenAllowed ", test.nbrOpenAllowed);
                    Log.warning("Bad requireUnitExams ", test.requireUnitExams);
                    Log.warning("Bad useMidterms ", test.useMidterms);
                    Log.warning("Bad allowCoupons ", test.allowCoupons);
                    Log.warning("Bad couponsAfterWindow ", test.couponsAfterWindow);
                    Log.warning("Bad usersProgressCr ", test.usersProgressCr);
                    Log.warning("Bad hwProgressCr ", test.hwProgressCr);
                    Log.warning("Bad reProgressCr ", test.reProgressCr);
                    Log.warning("Bad ueProgressCr ", test.ueProgressCr);
                    Log.warning("Bad finProgressCr ", test.finProgressCr);
                    Log.warning("Bad pacingName ", test.pacingName);
                    Log.warning("Bad scheduleSource ", test.scheduleSource);
                    Log.warning("Bad srDueDateEnforced ", test.srDueDateEnforced);
                    Log.warning("Bad reDueDateEnforced ", test.reDueDateEnforced);
                    Log.warning("Bad ueDueDateEnforced ", test.ueDueDateEnforced);
                    Log.warning("Bad feDueDateEnforced ", test.feDueDateEnforced);
                    Log.warning("Bad firstObjAvail ", test.firstObjAvail);
                }
            }

            assertTrue(found1, "PacingStructure 1 not found");
            assertTrue(found2, "PacingStructure 2 not found");
            assertTrue(found3, "PacingStructure 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all pacing_structure rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByTerm results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawPacingStructure> all = RawPacingStructureLogic.queryByTerm(cache, fa21);

            assertEquals(2, all.size(), "Incorrect record count from queryByTerm");

            boolean found1 = false;
            boolean found3 = false;

            for (final RawPacingStructure test : all) {
                if (fa21.equals(test.termKey)
                    && "A".equals(test.pacingStructure)
                    && "B".equals(test.defPaceTrack)
                    && "C".equals(test.requireLicensed)
                    && "D".equals(test.requirePartic)
                    && Integer.valueOf(1).equals(test.maxParticMissed)
                    && "E".equals(test.allowInc)
                    && Integer.valueOf(2).equals(test.maxCourses)
                    && Integer.valueOf(3).equals(test.nbrOpenAllowed)
                    && "F".equals(test.requireUnitExams)
                    && "G".equals(test.useMidterms)
                    && "H".equals(test.allowCoupons)
                    && "I".equals(test.couponsAfterWindow)
                    && Integer.valueOf(4).equals(test.usersProgressCr)
                    && Integer.valueOf(5).equals(test.hwProgressCr)
                    && Integer.valueOf(6).equals(test.reProgressCr)
                    && Integer.valueOf(7).equals(test.ueProgressCr)
                    && Integer.valueOf(8).equals(test.finProgressCr)
                    && "J".equals(test.pacingName)
                    && "K".equals(test.scheduleSource)
                    && "L".equals(test.srDueDateEnforced)
                    && "M".equals(test.reDueDateEnforced)
                    && "N".equals(test.ueDueDateEnforced)
                    && "O".equals(test.feDueDateEnforced)
                    && "P".equals(test.firstObjAvail)) {

                    found1 = true;
                } else if (fa21.equals(test.termKey)
                           && "Z".equals(test.pacingStructure)
                           && "Y".equals(test.defPaceTrack)
                           && "X".equals(test.requireLicensed)
                           && "W".equals(test.requirePartic)
                           && Integer.valueOf(0).equals(test.maxParticMissed)
                           && "V".equals(test.allowInc)
                           && Integer.valueOf(1).equals(test.maxCourses)
                           && Integer.valueOf(2).equals(test.nbrOpenAllowed)
                           && "U".equals(test.requireUnitExams)
                           && "T".equals(test.useMidterms)
                           && "S".equals(test.allowCoupons)
                           && "R".equals(test.couponsAfterWindow)
                           && Integer.valueOf(3).equals(test.usersProgressCr)
                           && Integer.valueOf(4).equals(test.hwProgressCr)
                           && Integer.valueOf(5).equals(test.reProgressCr)
                           && Integer.valueOf(6).equals(test.ueProgressCr)
                           && Integer.valueOf(7).equals(test.finProgressCr)
                           && "Q".equals(test.pacingName)
                           && "P".equals(test.scheduleSource)
                           && "O".equals(test.srDueDateEnforced)
                           && "N".equals(test.reDueDateEnforced)
                           && "M".equals(test.ueDueDateEnforced)
                           && "L".equals(test.feDueDateEnforced)
                           && "K".equals(test.firstObjAvail)) {

                    found3 = true;
                } else {
                    Log.warning("Bad termKey ", test.termKey);
                    Log.warning("Bad pacingStructure ", test.pacingStructure);
                    Log.warning("Bad defPaceTrack ", test.defPaceTrack);
                    Log.warning("Bad requireLicensed ", test.requireLicensed);
                    Log.warning("Bad requirePartic ", test.requirePartic);
                    Log.warning("Bad maxParticMissed ", test.maxParticMissed);
                    Log.warning("Bad allowInc ", test.allowInc);
                    Log.warning("Bad maxCourses ", test.maxCourses);
                    Log.warning("Bad nbrOpenAllowed ", test.nbrOpenAllowed);
                    Log.warning("Bad requireUnitExams ", test.requireUnitExams);
                    Log.warning("Bad useMidterms ", test.useMidterms);
                    Log.warning("Bad allowCoupons ", test.allowCoupons);
                    Log.warning("Bad couponsAfterWindow ", test.couponsAfterWindow);
                    Log.warning("Bad usersProgressCr ", test.usersProgressCr);
                    Log.warning("Bad hwProgressCr ", test.hwProgressCr);
                    Log.warning("Bad reProgressCr ", test.reProgressCr);
                    Log.warning("Bad ueProgressCr ", test.ueProgressCr);
                    Log.warning("Bad finProgressCr ", test.finProgressCr);
                    Log.warning("Bad pacingName ", test.pacingName);
                    Log.warning("Bad scheduleSource ", test.scheduleSource);
                    Log.warning("Bad srDueDateEnforced ", test.srDueDateEnforced);
                    Log.warning("Bad reDueDateEnforced ", test.reDueDateEnforced);
                    Log.warning("Bad ueDueDateEnforced ", test.ueDueDateEnforced);
                    Log.warning("Bad feDueDateEnforced ", test.feDueDateEnforced);
                    Log.warning("Bad firstObjAvail ", test.firstObjAvail);
                }
            }

            assertTrue(found1, "PacingStructure 1 not found");
            assertTrue(found3, "PacingStructure 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying pacing_structure rows by term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query(String,String) results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final RawPacingStructure rec = RawPacingStructureLogic.query(cache, fa21, "A");

            assertNotNull(rec, "Null result from query");

            final boolean found1 = fa21.equals(rec.termKey)
                                   && "A".equals(rec.pacingStructure)
                                   && "B".equals(rec.defPaceTrack)
                                   && "C".equals(rec.requireLicensed)
                                   && "D".equals(rec.requirePartic)
                                   && Integer.valueOf(1).equals(rec.maxParticMissed)
                                   && "E".equals(rec.allowInc)
                                   && Integer.valueOf(2).equals(rec.maxCourses)
                                   && Integer.valueOf(3).equals(rec.nbrOpenAllowed)
                                   && "F".equals(rec.requireUnitExams)
                                   && "G".equals(rec.useMidterms)
                                   && "H".equals(rec.allowCoupons)
                                   && "I".equals(rec.couponsAfterWindow)
                                   && Integer.valueOf(4).equals(rec.usersProgressCr)
                                   && Integer.valueOf(5).equals(rec.hwProgressCr)
                                   && Integer.valueOf(6).equals(rec.reProgressCr)
                                   && Integer.valueOf(7).equals(rec.ueProgressCr)
                                   && Integer.valueOf(8).equals(rec.finProgressCr)
                                   && "J".equals(rec.pacingName)
                                   && "K".equals(rec.scheduleSource)
                                   && "L".equals(rec.srDueDateEnforced)
                                   && "M".equals(rec.reDueDateEnforced)
                                   && "N".equals(rec.ueDueDateEnforced)
                                   && "O".equals(rec.feDueDateEnforced)
                                   && "P".equals(rec.firstObjAvail);

            assertTrue(found1, "PacingStructure 1 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying pacing_structure row: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query(String) results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final RawPacingStructure rec = RawPacingStructureLogic.query(cache, "A");

            assertNotNull(rec, "Null result from query");

            final boolean found1 = fa21.equals(rec.termKey)
                                   && "A".equals(rec.pacingStructure)
                                   && "B".equals(rec.defPaceTrack)
                                   && "C".equals(rec.requireLicensed)
                                   && "D".equals(rec.requirePartic)
                                   && Integer.valueOf(1).equals(rec.maxParticMissed)
                                   && "E".equals(rec.allowInc)
                                   && Integer.valueOf(2).equals(rec.maxCourses)
                                   && Integer.valueOf(3).equals(rec.nbrOpenAllowed)
                                   && "F".equals(rec.requireUnitExams)
                                   && "G".equals(rec.useMidterms)
                                   && "H".equals(rec.allowCoupons)
                                   && "I".equals(rec.couponsAfterWindow)
                                   && Integer.valueOf(4).equals(rec.usersProgressCr)
                                   && Integer.valueOf(5).equals(rec.hwProgressCr)
                                   && Integer.valueOf(6).equals(rec.reProgressCr)
                                   && Integer.valueOf(7).equals(rec.ueProgressCr)
                                   && Integer.valueOf(8).equals(rec.finProgressCr)
                                   && "J".equals(rec.pacingName)
                                   && "K".equals(rec.scheduleSource)
                                   && "L".equals(rec.srDueDateEnforced)
                                   && "M".equals(rec.reDueDateEnforced)
                                   && "N".equals(rec.ueDueDateEnforced)
                                   && "O".equals(rec.feDueDateEnforced)
                                   && "P".equals(rec.firstObjAvail);

            assertTrue(found1, "PacingStructure 1 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying pacing_structure row: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            final RawPacingStructure raw2 = new RawPacingStructure(fa20, "a", "b", "c", "d", Integer.valueOf(11),
                    "e", Integer.valueOf(12), Integer.valueOf(13), "f", "g", "h", "i", Integer.valueOf(14),
                    Integer.valueOf(15), Integer.valueOf(16), Integer.valueOf(17), Integer.valueOf(18), "j", "k",
                    "l", "m", "n", "o", "p", null);

            final boolean result = RawPacingStructureLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawPacingStructure> all = RawPacingStructureLogic.queryAll(cache);

            assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;

            for (final RawPacingStructure test : all) {
                if (fa21.equals(test.termKey)
                    && "A".equals(test.pacingStructure)
                    && "B".equals(test.defPaceTrack)
                    && "C".equals(test.requireLicensed)
                    && "D".equals(test.requirePartic)
                    && Integer.valueOf(1).equals(test.maxParticMissed)
                    && "E".equals(test.allowInc)
                    && Integer.valueOf(2).equals(test.maxCourses)
                    && Integer.valueOf(3).equals(test.nbrOpenAllowed)
                    && "F".equals(test.requireUnitExams)
                    && "G".equals(test.useMidterms)
                    && "H".equals(test.allowCoupons)
                    && "I".equals(test.couponsAfterWindow)
                    && Integer.valueOf(4).equals(test.usersProgressCr)
                    && Integer.valueOf(5).equals(test.hwProgressCr)
                    && Integer.valueOf(6).equals(test.reProgressCr)
                    && Integer.valueOf(7).equals(test.ueProgressCr)
                    && Integer.valueOf(8).equals(test.finProgressCr)
                    && "J".equals(test.pacingName)
                    && "K".equals(test.scheduleSource)
                    && "L".equals(test.srDueDateEnforced)
                    && "M".equals(test.reDueDateEnforced)
                    && "N".equals(test.ueDueDateEnforced)
                    && "O".equals(test.feDueDateEnforced)
                    && "P".equals(test.firstObjAvail)) {

                    found1 = true;
                } else if (fa21.equals(test.termKey)
                           && "Z".equals(test.pacingStructure)
                           && "Y".equals(test.defPaceTrack)
                           && "X".equals(test.requireLicensed)
                           && "W".equals(test.requirePartic)
                           && Integer.valueOf(0).equals(test.maxParticMissed)
                           && "V".equals(test.allowInc)
                           && Integer.valueOf(1).equals(test.maxCourses)
                           && Integer.valueOf(2).equals(test.nbrOpenAllowed)
                           && "U".equals(test.requireUnitExams)
                           && "T".equals(test.useMidterms)
                           && "S".equals(test.allowCoupons)
                           && "R".equals(test.couponsAfterWindow)
                           && Integer.valueOf(3).equals(test.usersProgressCr)
                           && Integer.valueOf(4).equals(test.hwProgressCr)
                           && Integer.valueOf(5).equals(test.reProgressCr)
                           && Integer.valueOf(6).equals(test.ueProgressCr)
                           && Integer.valueOf(7).equals(test.finProgressCr)
                           && "Q".equals(test.pacingName)
                           && "P".equals(test.scheduleSource)
                           && "O".equals(test.srDueDateEnforced)
                           && "N".equals(test.reDueDateEnforced)
                           && "M".equals(test.ueDueDateEnforced)
                           && "L".equals(test.feDueDateEnforced)
                           && "K".equals(test.firstObjAvail)) {

                    found3 = true;
                } else {
                    Log.warning("Bad termKey ", test.termKey);
                    Log.warning("Bad pacingStructure ", test.pacingStructure);
                    Log.warning("Bad defPaceTrack ", test.defPaceTrack);
                    Log.warning("Bad requireLicensed ", test.requireLicensed);
                    Log.warning("Bad requirePartic ", test.requirePartic);
                    Log.warning("Bad maxParticMissed ", test.maxParticMissed);
                    Log.warning("Bad allowInc ", test.allowInc);
                    Log.warning("Bad maxCourses ", test.maxCourses);
                    Log.warning("Bad nbrOpenAllowed ", test.nbrOpenAllowed);
                    Log.warning("Bad requireUnitExams ", test.requireUnitExams);
                    Log.warning("Bad useMidterms ", test.useMidterms);
                    Log.warning("Bad allowCoupons ", test.allowCoupons);
                    Log.warning("Bad couponsAfterWindow ", test.couponsAfterWindow);
                    Log.warning("Bad usersProgressCr ", test.usersProgressCr);
                    Log.warning("Bad hwProgressCr ", test.hwProgressCr);
                    Log.warning("Bad reProgressCr ", test.reProgressCr);
                    Log.warning("Bad ueProgressCr ", test.ueProgressCr);
                    Log.warning("Bad finProgressCr ", test.finProgressCr);
                    Log.warning("Bad pacingName ", test.pacingName);
                    Log.warning("Bad scheduleSource ", test.scheduleSource);
                    Log.warning("Bad srDueDateEnforced ", test.srDueDateEnforced);
                    Log.warning("Bad reDueDateEnforced ", test.reDueDateEnforced);
                    Log.warning("Bad ueDueDateEnforced ", test.ueDueDateEnforced);
                    Log.warning("Bad feDueDateEnforced ", test.feDueDateEnforced);
                    Log.warning("Bad firstObjAvail ", test.firstObjAvail);
                }
            }

            assertTrue(found1, "pacing_structure 1 not found");
            assertTrue(found3, "pacing_structure 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting pacing_structure: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM pacing_structure");
                stmt.executeUpdate("DELETE FROM term");
            }

            conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while cleaning tables: " + ex.getMessage());
        } finally {
            login.checkInConnection(conn);
        }
    }
}

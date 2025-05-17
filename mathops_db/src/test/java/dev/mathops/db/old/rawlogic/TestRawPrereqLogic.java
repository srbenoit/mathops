package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawrecord.RawPrereq;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code RawPrereqLogic} class.
 */
final class TestRawPrereqLogic {

    /** A term key used in test records. */
    private static final TermKey fa21 = new TermKey("FA21");

    /** A term key used in test records. */
    private static final TermKey sp22 = new TermKey("SP22");

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
                stmt.executeUpdate("DELETE FROM prereq");
                stmt.executeUpdate("DELETE FROM term");
            }
            conn.commit();

            final TermRec rawTerm = new TermRec(fa21, LocalDate.of(2021, 8, 11), LocalDate.of(2021, 12, 14),
                    "2122", Integer.valueOf(0), LocalDate.of(2021, 11, 13), LocalDate.of(2021, 11, 14)
                    , LocalDate.of(2022, 10, 12));

            assertTrue(cache.getSystemData().insertTerm(rawTerm), "Failed to insert active term");

            final RawPrereq raw1 = new RawPrereq(fa21, RawRecordConstants.M126, RawRecordConstants.M125);
            final RawPrereq raw2 = new RawPrereq(fa21, RawRecordConstants.M126, "M 160");
            final RawPrereq raw3 = new RawPrereq(fa21, RawRecordConstants.M125, RawRecordConstants.M118);
            final RawPrereq raw4 = new RawPrereq(sp22, RawRecordConstants.M118, RawRecordConstants.M117);

            assertTrue(RawPrereqLogic.insert(cache, raw1), "Failed to insert prereq 1");
            assertTrue(RawPrereqLogic.insert(cache, raw2), "Failed to insert prereq 2");
            assertTrue(RawPrereqLogic.insert(cache, raw3), "Failed to insert prereq 3");
            assertTrue(RawPrereqLogic.insert(cache, raw4), "Failed to insert prereq 4");
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
            final List<RawPrereq> all = RawPrereqLogic.queryAll(cache);

            assertEquals(4, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final RawPrereq test : all) {
                if (fa21.equals(test.termKey)
                    && RawRecordConstants.M126.equals(test.course)
                    && RawRecordConstants.M125.equals(test.prerequisite)) {

                    found1 = true;
                } else if (fa21.equals(test.termKey)
                           && RawRecordConstants.M126.equals(test.course)
                           && "M 160".equals(test.prerequisite)) {

                    found2 = true;
                } else if (fa21.equals(test.termKey)
                           && RawRecordConstants.M125.equals(test.course)
                           && RawRecordConstants.M118.equals(test.prerequisite)) {

                    found3 = true;
                } else if (sp22.equals(test.termKey)
                           && RawRecordConstants.M118.equals(test.course)
                           && RawRecordConstants.M117.equals(test.prerequisite)) {

                    found4 = true;
                } else {
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected prerequisite ", test.prerequisite);
                }
            }

            assertTrue(found1, "Prereq 1 not found");
            assertTrue(found2, "Prereq 2 not found");
            assertTrue(found3, "Prereq 3 not found");
            assertTrue(found4, "Prereq 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all prereq rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByTerm results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawPrereq> all = RawPrereqLogic.queryByTerm(cache, fa21);

            assertEquals(3, all.size(), "Incorrect record count from queryByTerm");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawPrereq test : all) {
                if (fa21.equals(test.termKey)
                    && RawRecordConstants.M126.equals(test.course)
                    && RawRecordConstants.M125.equals(test.prerequisite)) {

                    found1 = true;
                } else if (fa21.equals(test.termKey)
                           && RawRecordConstants.M126.equals(test.course)
                           && "M 160".equals(test.prerequisite)) {

                    found2 = true;
                } else if (fa21.equals(test.termKey)
                           && RawRecordConstants.M125.equals(test.course)
                           && RawRecordConstants.M118.equals(test.prerequisite)) {

                    found3 = true;
                } else {
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected prerequisite ", test.prerequisite);
                }
            }

            assertTrue(found1, "prereq 1 not found");
            assertTrue(found2, "prereq 2 not found");
            assertTrue(found3, "prereq 3 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying prereq rows by term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByTermAndCourse results")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawPrereq> all = RawPrereqLogic.queryByTermAndCourse(cache, fa21,
                    RawRecordConstants.M126);

            assertEquals(2, all.size(), "Incorrect record count from queryByTermAndCourse");

            boolean found1 = false;
            boolean found2 = false;

            for (final RawPrereq test : all) {
                if (fa21.equals(test.termKey)
                    && RawRecordConstants.M126.equals(test.course)
                    && RawRecordConstants.M125.equals(test.prerequisite)) {

                    found1 = true;
                } else if (fa21.equals(test.termKey)
                           && RawRecordConstants.M126.equals(test.course)
                           && "M 160".equals(test.prerequisite)) {

                    found2 = true;
                } else {
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected prerequisite ", test.prerequisite);
                }
            }

            assertTrue(found1, "prereq 1 not found");
            assertTrue(found2, "prereq 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying prereq rows by term: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("getPrerequisitesByCourse results")
    void test0006() {

        final Cache cache = new Cache(profile);

        try {
            final List<String> all = RawPrereqLogic.getPrerequisitesByCourse(cache, RawRecordConstants.M126);

            assertEquals(2, all.size(), "Incorrect record count from getPrerequisitesByCourse");

            boolean found1 = false;
            boolean found2 = false;

            for (final String test : all) {
                if (RawRecordConstants.M125.equals(test)) {
                    found1 = true;
                } else if ("M 160".equals(test)) {
                    found2 = true;
                }
            }

            assertTrue(found1, "prereq 1 not found");
            assertTrue(found2, "prereq 2 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying prereqs by course: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0007() {

        final Cache cache = new Cache(profile);

        try {
            final RawPrereq raw2 = new RawPrereq(fa21, RawRecordConstants.M126, "M 160");

            final boolean result = RawPrereqLogic.delete(cache, raw2);
            assertTrue(result, "delete returned false");

            final List<RawPrereq> all = RawPrereqLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll after delete");

            boolean found1 = false;
            boolean found3 = false;
            boolean found4 = false;

            for (final RawPrereq test : all) {
                if (fa21.equals(test.termKey)
                    && RawRecordConstants.M126.equals(test.course)
                    && RawRecordConstants.M125.equals(test.prerequisite)) {

                    found1 = true;
                } else if (fa21.equals(test.termKey)
                           && RawRecordConstants.M125.equals(test.course)
                           && RawRecordConstants.M118.equals(test.prerequisite)) {

                    found3 = true;
                } else if (sp22.equals(test.termKey)
                           && RawRecordConstants.M118.equals(test.course)
                           && RawRecordConstants.M117.equals(test.prerequisite)) {

                    found4 = true;
                } else {
                    Log.warning("Unexpected termKey ", test.termKey);
                    Log.warning("Unexpected course ", test.course);
                    Log.warning("Unexpected prerequisite ", test.prerequisite);
                }
            }

            assertTrue(found1, "Prereq 1 not found");
            assertTrue(found3, "Prereq 3 not found");
            assertTrue(found4, "Prereq 4 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting prereq: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM prereq");
                stmt.executeUpdate("DELETE FROM term");
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

package dev.mathops.db.old.rawlogic;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.ETermName;
import dev.mathops.db.old.rawrecord.RawApplicant;
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
 * Tests for the {@code RawApplicantLogic} classes.
 */
final class TestRawApplicantLogic {

    /** A termkey used in test records. */
    private static final TermKey sp21 = new TermKey(ETermName.SPRING, 2021);

    /** A termkey used in test records. */
    private static final TermKey sm22 = new TermKey(ETermName.SUMMER, 2022);

    /** A termkey used in test records. */
    private static final TermKey fa23 = new TermKey(ETermName.FALL, 2023);

    /** A date used in test records. */
    private static final LocalDate date1 = LocalDate.of(1995, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date2 = LocalDate.of(1996, 2, 3);

    /** A date used in test records. */
    private static final LocalDate date3 = LocalDate.of(1997, 3, 4);

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
                stmt.executeUpdate("DELETE FROM applicant");
            }
            conn.commit();

            final RawApplicant raw1 = new RawApplicant("888888881", "Alice", "Cooper", date1, "E1", "A", "C1",
                    "Prog1", "HS01", "1.0", "R1", "RS1", "RC1", "1.11", Integer.valueOf(100), Integer.valueOf(1100),
                    Integer.valueOf(21), Integer.valueOf(501), Integer.valueOf(111111), sp21);

            final RawApplicant raw2 = new RawApplicant("888888882", "Bob", "Dylan", date2, "E2", "B", "C2",
                    "Prog2", "HS02", "2.0", "R2", "RS2", "RC2", "2.22", Integer.valueOf(200), Integer.valueOf(2200),
                    Integer.valueOf(22), Integer.valueOf(502), Integer.valueOf(222222), sm22);

            final RawApplicant raw3 = new RawApplicant("888888883", "Carol", "Burnett", date3, "E3", "C", "C3",
                    "Prog3", "HS03", "3.0", "R3", "RS3", "RC3", "3.33", Integer.valueOf(300), Integer.valueOf(3300),
                    Integer.valueOf(23), Integer.valueOf(503), Integer.valueOf(333333), fa23);

            assertTrue(RawApplicantLogic.insert(cache, raw1), "Failed to insert applicant");
            assertTrue(RawApplicantLogic.insert(cache, raw2), "Failed to insert applicant");
            assertTrue(RawApplicantLogic.insert(cache, raw3), "Failed to insert applicant");
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
            final List<RawApplicant> all = RawApplicantLogic.queryAll(cache);

            assertEquals(3, all.size(), "Incorrect record count from queryAll");

            boolean found1 = false;
            boolean found2 = false;
            boolean found3 = false;

            for (final RawApplicant test : all) {

                if ("888888881".equals(test.stuId)
                    && "Alice".equals(test.firstName)
                    && "Cooper".equals(test.lastName)
                    && date1.equals(test.birthdate)
                    && "E1".equals(test.ethnicity)
                    && "A".equals(test.gender)
                    && "C1".equals(test.college)
                    && "Prog1".equals(test.progStudy)
                    && "HS01".equals(test.hsCode)
                    && "1.0".equals(test.trCredits)
                    && "R1".equals(test.resident)
                    && "RS1".equals(test.residentState)
                    && "RC1".equals(test.residentCounty)
                    && "1.11".equals(test.hsGpa)
                    && Integer.valueOf(100).equals(test.hsClassRank)
                    && Integer.valueOf(1100).equals(test.hsSizeClass)
                    && Integer.valueOf(21).equals(test.actScore)
                    && Integer.valueOf(501).equals(test.satScore)
                    && Integer.valueOf(111111).equals(test.pidm)
                    && sp21.equals(test.aplnTerm)) {
                    found1 = true;
                } else if ("888888882".equals(test.stuId)
                           && "Bob".equals(test.firstName)
                           && "Dylan".equals(test.lastName)
                           && date2.equals(test.birthdate)
                           && "E2".equals(test.ethnicity)
                           && "B".equals(test.gender)
                           && "C2".equals(test.college)
                           && "Prog2".equals(test.progStudy)
                           && "HS02".equals(test.hsCode)
                           && "2.0".equals(test.trCredits)
                           && "R2".equals(test.resident)
                           && "RS2".equals(test.residentState)
                           && "RC2".equals(test.residentCounty)
                           && "2.22".equals(test.hsGpa)
                           && Integer.valueOf(200).equals(test.hsClassRank)
                           && Integer.valueOf(2200).equals(test.hsSizeClass)
                           && Integer.valueOf(22).equals(test.actScore)
                           && Integer.valueOf(502).equals(test.satScore)
                           && Integer.valueOf(222222).equals(test.pidm)
                           && sm22.equals(test.aplnTerm)) {
                    found2 = true;
                } else if ("888888883".equals(test.stuId)
                           && "Carol".equals(test.firstName)
                           && "Burnett".equals(test.lastName)
                           && date3.equals(test.birthdate)
                           && "E3".equals(test.ethnicity)
                           && "C".equals(test.gender)
                           && "C3".equals(test.college)
                           && "Prog3".equals(test.progStudy)
                           && "HS03".equals(test.hsCode)
                           && "3.0".equals(test.trCredits)
                           && "R3".equals(test.resident)
                           && "RS3".equals(test.residentState)
                           && "RC3".equals(test.residentCounty)
                           && "3.33".equals(test.hsGpa)
                           && Integer.valueOf(300).equals(test.hsClassRank)
                           && Integer.valueOf(3300).equals(test.hsSizeClass)
                           && Integer.valueOf(23).equals(test.actScore)
                           && Integer.valueOf(503).equals(test.satScore)
                           && Integer.valueOf(333333).equals(test.pidm)
                           && fa23.equals(test.aplnTerm)) {
                    found3 = true;
                } else {
                    Log.warning("Unexpected applicant: ", test);
                }
            }

            assertTrue(found1, "applicant for 881 not found");
            assertTrue(found2, "applicant for 882 not found");
            assertTrue(found3, "applicant for 883 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all applicant rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByStudent results")
    void test0004() {

        final Cache cache = new Cache(profile);

        try {
            final List<RawApplicant> all = RawApplicantLogic.queryByStudent(cache, "888888881");

            assertEquals(1, all.size(), "Incorrect record count from queryByStudent");

            boolean found1 = false;

            for (final RawApplicant test : all) {

                if ("888888881".equals(test.stuId)
                    && "Alice".equals(test.firstName)
                    && "Cooper".equals(test.lastName)
                    && date1.equals(test.birthdate)
                    && "E1".equals(test.ethnicity)
                    && "A".equals(test.gender)
                    && "C1".equals(test.college)
                    && "Prog1".equals(test.progStudy)
                    && "HS01".equals(test.hsCode)
                    && "1.0".equals(test.trCredits)
                    && "R1".equals(test.resident)
                    && "RS1".equals(test.residentState)
                    && "RC1".equals(test.residentCounty)
                    && "1.11".equals(test.hsGpa)
                    && Integer.valueOf(100).equals(test.hsClassRank)
                    && Integer.valueOf(1100).equals(test.hsSizeClass)
                    && Integer.valueOf(21).equals(test.actScore)
                    && Integer.valueOf(501).equals(test.satScore)
                    && Integer.valueOf(111111).equals(test.pidm)
                    && sp21.equals(test.aplnTerm)) {
                    found1 = true;
                    break;
                }
            }

            assertTrue(found1, "applicant for 881 not found");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying single applicant rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Delete of applicant")
    void test0005() {

        final Cache cache = new Cache(profile);

        try {
            final RawApplicant toDelete = new RawApplicant();

            // Primary key fields to locate record
            toDelete.stuId = "888888881";

            assertTrue(RawApplicantLogic.delete(cache, toDelete), "Delete applicant failed");

            final List<RawApplicant> all = RawApplicantLogic.queryByStudent(cache, "888888881");

            assertEquals(0, all.size(), "Incorrect record count from getApplicants");
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting applicant: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        final Login login = profile.getLogin(ESchema.LEGACY);
        final DbConnection conn = login.checkOutConnection();

        try {
            try (final Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("DELETE FROM applicant");
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

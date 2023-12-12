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
import dev.mathops.db.old.rawrecord.RawStudent;

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
 * Tests for the {@code RawStudentLogic} class.
 */
final class TestRawStudentLogic {

    /** A term key used in test records. */
    private static final TermKey termSP20 = new TermKey("SP20");

    /** A term key used in test records. */
    private static final TermKey termSP24 = new TermKey("SP24");

    /** A term key used in test records. */
    private static final TermKey termSM22 = new TermKey("SM22");

    /** A term key used in test records. */
    private static final TermKey termFA30 = new TermKey("FA30");

    /** A term key used in test records. */
    private static final TermKey termFA20 = new TermKey("FA20");

    /** A term key used in test records. */
    private static final TermKey termFSM24 = new TermKey("SM24");

    /** A date used in test records. */
    private static final LocalDate date12 = LocalDate.of(2000, 1, 2);

    /** A date used in test records. */
    private static final LocalDate date34 = LocalDate.of(2020, 3, 4);

    /** A date used in test records. */
    private static final LocalDate date56 = LocalDate.of(2021, 5, 6);

    /** A date used in test records. */
    private static final LocalDate date1011 = LocalDate.of(2009, 10, 11);

    /** A date used in test records. */
    private static final LocalDate date1112 = LocalDate.of(1990, 11, 12);

    /** A float used in test records. */
    private static final Float HALF = Float.valueOf(0.5f);

    /** A float used in test records. */
    private static final Float THREE_HALVES = Float.valueOf(1.5f);

    /** A float used in test records. */
    private static final Float PI = Float.valueOf(3.14f);

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
                    stmt.executeUpdate("DELETE FROM student");
                }
                conn.commit();

                final Cache cache = new Cache(dbProfile, conn);

                final RawStudent raw1 = new RawStudent("111111111",
                        Integer.valueOf(12345), "Anderson", "Thomas",
                        "Neo", "A", termSP20,
                        "CL", "CO", "DE",
                        "PC", "MINOR", termSP24,
                        "1.5", "HS1", "3.75",
                        Integer.valueOf(100), Integer.valueOf(150), Integer.valueOf(23),
                        Integer.valueOf(620), "3", "RE",
                        date12, "ET", "G",
                        "Y", "DS", "N", THREE_HALVES,
                        "L", "CA", "SE", "AE",
                        "PWD", "AT", "OE", "P",
                        date34, null, null);

                final RawStudent raw2 = new RawStudent("222222222",
                        Integer.valueOf(67890), "Smith", "Agent",
                        "Agent Smith", "Q",
                        termFA20, "cl", "co",
                        "de", "pc", "Minor",
                        termFSM24, "12", "HS2",
                        "1.75", Integer.valueOf(1000), Integer.valueOf(1500),
                        Integer.valueOf(24), Integer.valueOf(630), "4",
                        "re", date1112, "et",
                        "g", "N", "ds", "Y",
                        HALF, "Y", "ca", "se",
                        "ae", "pwd", "at", "de",
                        "Q", date56, null, null);

                final RawStudent raw3 = new RawStudent("3333333333",
                        Integer.valueOf(99999), "Smith", "Adam",
                        "Goober", "H", termFA20,
                        "cl", "co", "de",
                        "pc", "Minor", termFSM24,
                        "12", "HS2", "1.75",
                        Integer.valueOf(1000), Integer.valueOf(1500), Integer.valueOf(24),
                        Integer.valueOf(630), "4", "re",
                        date1112, "et", "g",
                        "N", "ds", "Y", HALF,
                        "Y", "ca", "se", "ae",
                        "pwd", "at", "de", "Q",
                        date56, null, null);

                assertTrue(RawStudentLogic.INSTANCE.insert(cache, raw1), "Failed to insert student 1");
                assertTrue(RawStudentLogic.INSTANCE.insert(cache, raw2), "Failed to insert student 2");
                assertTrue(RawStudentLogic.INSTANCE.insert(cache, raw3), "Failed to insert student 3");
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
                final List<RawStudent> all = RawStudentLogic.INSTANCE.queryAll(cache);

                assertEquals(3, all.size(), "Incorrect record count from queryAll");

                boolean found1 = false;
                boolean found2 = false;
                boolean found3 = false;

                for (final RawStudent test : all) {
                    if ("111111111".equals(test.stuId)
                            && Integer.valueOf(12345).equals(test.pidm)
                            && "Anderson".equals(test.lastName)
                            && "Thomas".equals(test.firstName)
                            && "Neo".equals(test.prefName)
                            && "A".equals(test.middleInitial)
                            && termSP20.equals(test.aplnTerm)
                            && "CL".equals(test.clazz)
                            && "CO".equals(test.college)
                            && "DE".equals(test.dept)
                            && "PC".equals(test.programCode)
                            && "MINOR".equals(test.minor)
                            && termSP24.equals(test.estGraduation)
                            && "1.5".equals(test.trCredits)
                            && "HS1".equals(test.hsCode)
                            && "3.75".equals(test.hsGpa)
                            && Integer.valueOf(100).equals(test.hsClassRank)
                            && Integer.valueOf(150).equals(test.hsSizeClass)
                            && Integer.valueOf(23).equals(test.actScore)
                            && Integer.valueOf(620).equals(test.satScore)
                            && "3".equals(test.apScore)
                            && "RE".equals(test.resident)
                            && date12.equals(test.birthdate)
                            && "ET".equals(test.ethnicity)
                            && "G".equals(test.gender)
                            && "Y".equals(test.discipHistory)
                            && "DS".equals(test.discipStatus)
                            && "N".equals(test.sevAdminHold)
                            && THREE_HALVES.equals(test.timelimitFactor)
                            && "L".equals(test.licensed)
                            && "CA".equals(test.campus)
                            && "SE".equals(test.stuEmail)
                            && "AE".equals(test.adviserEmail)
                            && "PWD".equals(test.password)
                            && "AT".equals(test.admitType)
                            && "OE".equals(test.orderEnforce)
                            && "P".equals(test.pacingStructure)
                            && date34.equals(test.createDt)) {

                        found1 = true;
                    } else if ("222222222".equals(test.stuId)
                            && Integer.valueOf(67890).equals(test.pidm)
                            && "Smith".equals(test.lastName)
                            && "Agent".equals(test.firstName)
                            && "Agent Smith".equals(test.prefName)
                            && "Q".equals(test.middleInitial)
                            && termFA20.equals(test.aplnTerm)
                            && "cl".equals(test.clazz)
                            && "co".equals(test.college)
                            && "de".equals(test.dept)
                            && "pc".equals(test.programCode)
                            && "Minor".equals(test.minor)
                            && termFSM24.equals(test.estGraduation)
                            && "12".equals(test.trCredits)
                            && "HS2".equals(test.hsCode)
                            && "1.75".equals(test.hsGpa)
                            && Integer.valueOf(1000).equals(test.hsClassRank)
                            && Integer.valueOf(1500).equals(test.hsSizeClass)
                            && Integer.valueOf(24).equals(test.actScore)
                            && Integer.valueOf(630).equals(test.satScore)
                            && "4".equals(test.apScore)
                            && "re".equals(test.resident)
                            && date1112.equals(test.birthdate)
                            && "et".equals(test.ethnicity)
                            && "g".equals(test.gender)
                            && "N".equals(test.discipHistory)
                            && "ds".equals(test.discipStatus)
                            && "Y".equals(test.sevAdminHold)
                            && HALF.equals(test.timelimitFactor)
                            && "Y".equals(test.licensed)
                            && "ca".equals(test.campus)
                            && "se".equals(test.stuEmail)
                            && "ae".equals(test.adviserEmail)
                            && "pwd".equals(test.password)
                            && "at".equals(test.admitType)
                            && "de".equals(test.orderEnforce)
                            && "Q".equals(test.pacingStructure)
                            && date56.equals(test.createDt)) {

                        found2 = true;
                    } else if ("333333333".equals(test.stuId)
                            && Integer.valueOf(99999).equals(test.pidm)
                            && "Smith".equals(test.lastName)
                            && "Adam".equals(test.firstName)
                            && "Goober".equals(test.prefName)
                            && "H".equals(test.middleInitial)
                            && termFA20.equals(test.aplnTerm)
                            && "cl".equals(test.clazz)
                            && "co".equals(test.college)
                            && "de".equals(test.dept)
                            && "pc".equals(test.programCode)
                            && "Minor".equals(test.minor)
                            && termFSM24.equals(test.estGraduation)
                            && "12".equals(test.trCredits)
                            && "HS2".equals(test.hsCode)
                            && "1.75".equals(test.hsGpa)
                            && Integer.valueOf(1000).equals(test.hsClassRank)
                            && Integer.valueOf(1500).equals(test.hsSizeClass)
                            && Integer.valueOf(24).equals(test.actScore)
                            && Integer.valueOf(630).equals(test.satScore)
                            && "4".equals(test.apScore)
                            && "re".equals(test.resident)
                            && date1112.equals(test.birthdate)
                            && "et".equals(test.ethnicity)
                            && "g".equals(test.gender)
                            && "N".equals(test.discipHistory)
                            && "ds".equals(test.discipStatus)
                            && "Y".equals(test.sevAdminHold)
                            && HALF.equals(test.timelimitFactor)
                            && "Y".equals(test.licensed)
                            && "ca".equals(test.campus)
                            && "se".equals(test.stuEmail)
                            && "ae".equals(test.adviserEmail)
                            && "pwd".equals(test.password)
                            && "at".equals(test.admitType)
                            && "de".equals(test.orderEnforce)
                            && "Q".equals(test.pacingStructure)
                            && date56.equals(test.createDt)) {

                        found3 = true;
                    } else {
                        Log.warning("Bad stuId ", test.stuId);
                        Log.warning("Bad pidm ", test.pidm);
                        Log.warning("Bad lastName ", test.lastName);
                        Log.warning("Bad firstName ", test.firstName);
                        Log.warning("Bad prefName ", test.prefName);
                        Log.warning("Bad middleInitial ", test.middleInitial);
                        Log.warning("Bad aplnTerm ", test.aplnTerm);
                        Log.warning("Bad clazz ", test.clazz);
                        Log.warning("Bad college ", test.college);
                        Log.warning("Bad dept ", test.dept);
                        Log.warning("Bad programCode ", test.programCode);
                        Log.warning("Bad minor ", test.minor);
                        Log.warning("Bad estGraduation ", test.estGraduation);
                        Log.warning("Bad trCredits ", test.trCredits);
                        Log.warning("Bad hsCode ", test.hsCode);
                        Log.warning("Bad hsGpa ", test.hsGpa);
                        Log.warning("Bad hsClassRank ", test.hsClassRank);
                        Log.warning("Bad hsSizeClass ", test.hsSizeClass);
                        Log.warning("Bad actScore ", test.actScore);
                        Log.warning("Bad satScore ", test.satScore);
                        Log.warning("Bad apScore ", test.apScore);
                        Log.warning("Bad resident ", test.resident);
                        Log.warning("Bad birthdate ", test.birthdate);
                        Log.warning("Bad ethnicity ", test.ethnicity);
                        Log.warning("Bad gender ", test.gender);
                        Log.warning("Bad discipHistory ", test.discipHistory);
                        Log.warning("Bad discipStatus ", test.discipStatus);
                        Log.warning("Bad sevAdminHold ", test.sevAdminHold);
                        Log.warning("Bad timelimitFactor ", test.timelimitFactor);
                        Log.warning("Bad licensed ", test.licensed);
                        Log.warning("Bad campus ", test.campus);
                        Log.warning("Bad stuEmail ", test.stuEmail);
                        Log.warning("Bad adviserEmail ", test.adviserEmail);
                        Log.warning("Bad password ", test.password);
                        Log.warning("Bad admitType ", test.admitType);
                        Log.warning("Bad orderEnforce ", test.orderEnforce);
                        Log.warning("Bad pacingStructure ", test.pacingStructure);
                        Log.warning("Bad createDt ", test.createDt);
                    }
                }

                assertTrue(found1, "Student 1 not found");
                assertTrue(found2, "Student 2 not found");
                assertTrue(found3, "Student 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all student rows: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("query results")
    void test0004() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(12345).equals(stu.pidm)
                        && "Anderson".equals(stu.lastName)
                        && "Thomas".equals(stu.firstName)
                        && "Neo".equals(stu.prefName)
                        && "A".equals(stu.middleInitial)
                        && termSP20.equals(stu.aplnTerm)
                        && "CL".equals(stu.clazz)
                        && "CO".equals(stu.college)
                        && "DE".equals(stu.dept)
                        && "PC".equals(stu.programCode)
                        && "MINOR".equals(stu.minor)
                        && termSP24.equals(stu.estGraduation)
                        && "1.5".equals(stu.trCredits)
                        && "HS1".equals(stu.hsCode)
                        && "3.75".equals(stu.hsGpa)
                        && Integer.valueOf(100).equals(stu.hsClassRank)
                        && Integer.valueOf(150).equals(stu.hsSizeClass)
                        && Integer.valueOf(23).equals(stu.actScore)
                        && Integer.valueOf(620).equals(stu.satScore)
                        && "3".equals(stu.apScore)
                        && "RE".equals(stu.resident)
                        && date12.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying student: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByInternalId results")
    void test0005() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawStudent stu = RawStudentLogic.queryByInternalId(cache, Integer.valueOf(12345));

                assertNotNull(stu, "No record returned by queryByInternalId");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(12345).equals(stu.pidm)
                        && "Anderson".equals(stu.lastName)
                        && "Thomas".equals(stu.firstName)
                        && "Neo".equals(stu.prefName)
                        && "A".equals(stu.middleInitial)
                        && termSP20.equals(stu.aplnTerm)
                        && "CL".equals(stu.clazz)
                        && "CO".equals(stu.college)
                        && "DE".equals(stu.dept)
                        && "PC".equals(stu.programCode)
                        && "MINOR".equals(stu.minor)
                        && termSP24.equals(stu.estGraduation)
                        && "1.5".equals(stu.trCredits)
                        && "HS1".equals(stu.hsCode)
                        && "3.75".equals(stu.hsGpa)
                        && Integer.valueOf(100).equals(stu.hsClassRank)
                        && Integer.valueOf(150).equals(stu.hsSizeClass)
                        && Integer.valueOf(23).equals(stu.actScore)
                        && Integer.valueOf(620).equals(stu.satScore)
                        && "3".equals(stu.apScore)
                        && "RE".equals(stu.resident)
                        && date12.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying student by PIDM: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByLastName with match results")
    void test0006() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawStudent stu = RawStudentLogic.queryByLastName(cache, "AnDeRsOn");

                assertNotNull(stu, "No record returned by queryByLastName");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(12345).equals(stu.pidm)
                        && "Anderson".equals(stu.lastName)
                        && "Thomas".equals(stu.firstName)
                        && "Neo".equals(stu.prefName)
                        && "A".equals(stu.middleInitial)
                        && termSP20.equals(stu.aplnTerm)
                        && "CL".equals(stu.clazz)
                        && "CO".equals(stu.college)
                        && "DE".equals(stu.dept)
                        && "PC".equals(stu.programCode)
                        && "MINOR".equals(stu.minor)
                        && termSP24.equals(stu.estGraduation)
                        && "1.5".equals(stu.trCredits)
                        && "HS1".equals(stu.hsCode)
                        && "3.75".equals(stu.hsGpa)
                        && Integer.valueOf(100).equals(stu.hsClassRank)
                        && Integer.valueOf(150).equals(stu.hsSizeClass)
                        && Integer.valueOf(23).equals(stu.actScore)
                        && Integer.valueOf(620).equals(stu.satScore)
                        && "3".equals(stu.apScore)
                        && "RE".equals(stu.resident)
                        && date12.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying student by PIDM: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByLastName with match results")
    void test0007() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawStudent stu = RawStudentLogic.queryByLastName(cache, "Smith");

                assertNull(stu, "No record returned by queryByLastName");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying student by PIDM: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByName with match results")
    void test0008() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawStudent stu = RawStudentLogic.queryByName(cache, "ThomaS", "AnDeRsOn");

                assertNotNull(stu, "No record returned by queryByName");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(12345).equals(stu.pidm)
                        && "Anderson".equals(stu.lastName)
                        && "Thomas".equals(stu.firstName)
                        && "Neo".equals(stu.prefName)
                        && "A".equals(stu.middleInitial)
                        && termSP20.equals(stu.aplnTerm)
                        && "CL".equals(stu.clazz)
                        && "CO".equals(stu.college)
                        && "DE".equals(stu.dept)
                        && "PC".equals(stu.programCode)
                        && "MINOR".equals(stu.minor)
                        && termSP24.equals(stu.estGraduation)
                        && "1.5".equals(stu.trCredits)
                        && "HS1".equals(stu.hsCode)
                        && "3.75".equals(stu.hsGpa)
                        && Integer.valueOf(100).equals(stu.hsClassRank)
                        && Integer.valueOf(150).equals(stu.hsSizeClass)
                        && Integer.valueOf(23).equals(stu.actScore)
                        && Integer.valueOf(620).equals(stu.satScore)
                        && "3".equals(stu.apScore)
                        && "RE".equals(stu.resident)
                        && date12.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying student by PIDM: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryByName with no match results")
    void test0009() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final RawStudent stu = RawStudentLogic.queryByName(cache, "Thomas", "Smith");

                assertNull(stu, "Bad record returned by queryByName");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying student by PIDM: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAllByName results")
    void test0010() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStudent> all = RawStudentLogic.queryAllByName(cache, "A%", "Smith");

                assertEquals(2, all.size(), "Incorrect record count from queryAllByName");

                boolean found2 = false;
                boolean found3 = false;

                for (final RawStudent test : all) {
                    if ("222222222".equals(test.stuId)
                            && Integer.valueOf(67890).equals(test.pidm)
                            && "Smith".equals(test.lastName)
                            && "Agent".equals(test.firstName)
                            && "Agent Smith".equals(test.prefName)
                            && "Q".equals(test.middleInitial)
                            && termFA20.equals(test.aplnTerm)
                            && "cl".equals(test.clazz)
                            && "co".equals(test.college)
                            && "de".equals(test.dept)
                            && "pc".equals(test.programCode)
                            && "Minor".equals(test.minor)
                            && termFSM24.equals(test.estGraduation)
                            && "12".equals(test.trCredits)
                            && "HS2".equals(test.hsCode)
                            && "1.75".equals(test.hsGpa)
                            && Integer.valueOf(1000).equals(test.hsClassRank)
                            && Integer.valueOf(1500).equals(test.hsSizeClass)
                            && Integer.valueOf(24).equals(test.actScore)
                            && Integer.valueOf(630).equals(test.satScore)
                            && "4".equals(test.apScore)
                            && "re".equals(test.resident)
                            && date1112.equals(test.birthdate)
                            && "et".equals(test.ethnicity)
                            && "g".equals(test.gender)
                            && "N".equals(test.discipHistory)
                            && "ds".equals(test.discipStatus)
                            && "Y".equals(test.sevAdminHold)
                            && HALF.equals(test.timelimitFactor)
                            && "Y".equals(test.licensed)
                            && "ca".equals(test.campus)
                            && "se".equals(test.stuEmail)
                            && "ae".equals(test.adviserEmail)
                            && "pwd".equals(test.password)
                            && "at".equals(test.admitType)
                            && "de".equals(test.orderEnforce)
                            && "Q".equals(test.pacingStructure)
                            && date56.equals(test.createDt)) {

                        found2 = true;
                    } else if ("333333333".equals(test.stuId)
                            && Integer.valueOf(99999).equals(test.pidm)
                            && "Smith".equals(test.lastName)
                            && "Adam".equals(test.firstName)
                            && "Goober".equals(test.prefName)
                            && "H".equals(test.middleInitial)
                            && termFA20.equals(test.aplnTerm)
                            && "cl".equals(test.clazz)
                            && "co".equals(test.college)
                            && "de".equals(test.dept)
                            && "pc".equals(test.programCode)
                            && "Minor".equals(test.minor)
                            && termFSM24.equals(test.estGraduation)
                            && "12".equals(test.trCredits)
                            && "HS2".equals(test.hsCode)
                            && "1.75".equals(test.hsGpa)
                            && Integer.valueOf(1000).equals(test.hsClassRank)
                            && Integer.valueOf(1500).equals(test.hsSizeClass)
                            && Integer.valueOf(24).equals(test.actScore)
                            && Integer.valueOf(630).equals(test.satScore)
                            && "4".equals(test.apScore)
                            && "re".equals(test.resident)
                            && date1112.equals(test.birthdate)
                            && "et".equals(test.ethnicity)
                            && "g".equals(test.gender)
                            && "N".equals(test.discipHistory)
                            && "ds".equals(test.discipStatus)
                            && "Y".equals(test.sevAdminHold)
                            && HALF.equals(test.timelimitFactor)
                            && "Y".equals(test.licensed)
                            && "ca".equals(test.campus)
                            && "se".equals(test.stuEmail)
                            && "ae".equals(test.adviserEmail)
                            && "pwd".equals(test.password)
                            && "at".equals(test.admitType)
                            && "de".equals(test.orderEnforce)
                            && "Q".equals(test.pacingStructure)
                            && date56.equals(test.createDt)) {

                        found3 = true;
                    }
                }

                assertTrue(found2, "Student 2 not found");
                assertTrue(found3, "Student 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all student rows by name: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("queryAllByName results")
    void test0011() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                final List<RawStudent> all = RawStudentLogic.queryAllByName(cache, "J%", "Smith");

                assertEquals(0, all.size(), "Incorrect record count from queryAllByName");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying all student rows by name: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateInternalId results")
    void test0012() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateInternalId(cache, "111111111", Integer.valueOf(123450)),
                        "updateInternalId failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Anderson".equals(stu.lastName)
                        && "Thomas".equals(stu.firstName)
                        && "Neo".equals(stu.prefName)
                        && "A".equals(stu.middleInitial)
                        && termSP20.equals(stu.aplnTerm)
                        && "CL".equals(stu.clazz)
                        && "CO".equals(stu.college)
                        && "DE".equals(stu.dept)
                        && "PC".equals(stu.programCode)
                        && "MINOR".equals(stu.minor)
                        && termSP24.equals(stu.estGraduation)
                        && "1.5".equals(stu.trCredits)
                        && "HS1".equals(stu.hsCode)
                        && "3.75".equals(stu.hsGpa)
                        && Integer.valueOf(100).equals(stu.hsClassRank)
                        && Integer.valueOf(150).equals(stu.hsSizeClass)
                        && Integer.valueOf(23).equals(stu.actScore)
                        && Integer.valueOf(620).equals(stu.satScore)
                        && "3".equals(stu.apScore)
                        && "RE".equals(stu.resident)
                        && date12.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateInternalId: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateName results")
    void test0013() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateName(cache, "111111111", "Benoit", "Steven", "Steve", "R"),
                        "updateName failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSP20.equals(stu.aplnTerm)
                        && "CL".equals(stu.clazz)
                        && "CO".equals(stu.college)
                        && "DE".equals(stu.dept)
                        && "PC".equals(stu.programCode)
                        && "MINOR".equals(stu.minor)
                        && termSP24.equals(stu.estGraduation)
                        && "1.5".equals(stu.trCredits)
                        && "HS1".equals(stu.hsCode)
                        && "3.75".equals(stu.hsGpa)
                        && Integer.valueOf(100).equals(stu.hsClassRank)
                        && Integer.valueOf(150).equals(stu.hsSizeClass)
                        && Integer.valueOf(23).equals(stu.actScore)
                        && Integer.valueOf(620).equals(stu.satScore)
                        && "3".equals(stu.apScore)
                        && "RE".equals(stu.resident)
                        && date12.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateName: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateApplicationTerm results")
    void test0014() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateApplicationTerm(cache, "111111111", termSM22),
                        "updateApplicationTerm failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "CL".equals(stu.clazz)
                        && "CO".equals(stu.college)
                        && "DE".equals(stu.dept)
                        && "PC".equals(stu.programCode)
                        && "MINOR".equals(stu.minor)
                        && termSP24.equals(stu.estGraduation)
                        && "1.5".equals(stu.trCredits)
                        && "HS1".equals(stu.hsCode)
                        && "3.75".equals(stu.hsGpa)
                        && Integer.valueOf(100).equals(stu.hsClassRank)
                        && Integer.valueOf(150).equals(stu.hsSizeClass)
                        && Integer.valueOf(23).equals(stu.actScore)
                        && Integer.valueOf(620).equals(stu.satScore)
                        && "3".equals(stu.apScore)
                        && "RE".equals(stu.resident)
                        && date12.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateApplicationTerm: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateClassLevel results")
    void test0015() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateClassLevel(cache, "111111111", "X"), "updateClassLevel failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "CO".equals(stu.college)
                        && "DE".equals(stu.dept)
                        && "PC".equals(stu.programCode)
                        && "MINOR".equals(stu.minor)
                        && termSP24.equals(stu.estGraduation)
                        && "1.5".equals(stu.trCredits)
                        && "HS1".equals(stu.hsCode)
                        && "3.75".equals(stu.hsGpa)
                        && Integer.valueOf(100).equals(stu.hsClassRank)
                        && Integer.valueOf(150).equals(stu.hsSizeClass)
                        && Integer.valueOf(23).equals(stu.actScore)
                        && Integer.valueOf(620).equals(stu.satScore)
                        && "3".equals(stu.apScore)
                        && "RE".equals(stu.resident)
                        && date12.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateClassLevel: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateProgram results")
    void test0016() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateProgram(cache, "111111111", "AA", "BB", "CC", "DD"),
                        "updateProgram failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termSP24.equals(stu.estGraduation)
                        && "1.5".equals(stu.trCredits)
                        && "HS1".equals(stu.hsCode)
                        && "3.75".equals(stu.hsGpa)
                        && Integer.valueOf(100).equals(stu.hsClassRank)
                        && Integer.valueOf(150).equals(stu.hsSizeClass)
                        && Integer.valueOf(23).equals(stu.actScore)
                        && Integer.valueOf(620).equals(stu.satScore)
                        && "3".equals(stu.apScore)
                        && "RE".equals(stu.resident)
                        && date12.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateProgram: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateAnticGradTerm results")
    void test0017() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateAnticGradTerm(cache, "111111111", termFA30),
                        "updateAnticGradTerm failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.5".equals(stu.trCredits)
                        && "HS1".equals(stu.hsCode)
                        && "3.75".equals(stu.hsGpa)
                        && Integer.valueOf(100).equals(stu.hsClassRank)
                        && Integer.valueOf(150).equals(stu.hsSizeClass)
                        && Integer.valueOf(23).equals(stu.actScore)
                        && Integer.valueOf(620).equals(stu.satScore)
                        && "3".equals(stu.apScore)
                        && "RE".equals(stu.resident)
                        && date12.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateAnticGradTerm: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateNumTransferCredits results")
    void test0018() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateNumTransferCredits(cache, "111111111", "1.99"),
                        "updateNumTransferCredits failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.99".equals(stu.trCredits)
                        && "HS1".equals(stu.hsCode)
                        && "3.75".equals(stu.hsGpa)
                        && Integer.valueOf(100).equals(stu.hsClassRank)
                        && Integer.valueOf(150).equals(stu.hsSizeClass)
                        && Integer.valueOf(23).equals(stu.actScore)
                        && Integer.valueOf(620).equals(stu.satScore)
                        && "3".equals(stu.apScore)
                        && "RE".equals(stu.resident)
                        && date12.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateNumTransferCredits: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateHighSchool results")
    void test0019() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateHighSchool(cache, "111111111", "HS4", "4.0", Integer.valueOf(1),
                        Integer.valueOf(2)), "updateHighSchool failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.99".equals(stu.trCredits)
                        && "HS4".equals(stu.hsCode)
                        && "4.0".equals(stu.hsGpa)
                        && Integer.valueOf(1).equals(stu.hsClassRank)
                        && Integer.valueOf(2).equals(stu.hsSizeClass)
                        && Integer.valueOf(23).equals(stu.actScore)
                        && Integer.valueOf(620).equals(stu.satScore)
                        && "3".equals(stu.apScore)
                        && "RE".equals(stu.resident)
                        && date12.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateHighSchool: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateTestScores results")
    void test0020() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateTestScores(cache, "111111111", Integer.valueOf(25),
                        Integer.valueOf(700), "0"), "updateTestScores failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.99".equals(stu.trCredits)
                        && "HS4".equals(stu.hsCode)
                        && "4.0".equals(stu.hsGpa)
                        && Integer.valueOf(1).equals(stu.hsClassRank)
                        && Integer.valueOf(2).equals(stu.hsSizeClass)
                        && Integer.valueOf(25).equals(stu.actScore)
                        && Integer.valueOf(700).equals(stu.satScore)
                        && "0".equals(stu.apScore)
                        && "RE".equals(stu.resident)
                        && date12.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateTestScores: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateResidency results")
    void test0021() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateResidency(cache, "111111111", "RR"), "updateTestScores failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.99".equals(stu.trCredits)
                        && "HS4".equals(stu.hsCode)
                        && "4.0".equals(stu.hsGpa)
                        && Integer.valueOf(1).equals(stu.hsClassRank)
                        && Integer.valueOf(2).equals(stu.hsSizeClass)
                        && Integer.valueOf(25).equals(stu.actScore)
                        && Integer.valueOf(700).equals(stu.satScore)
                        && "0".equals(stu.apScore)
                        && "RR".equals(stu.resident)
                        && date12.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateResidency: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateBirthDate results")
    void test0022() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateBirthDate(cache, "111111111", date1011), "updateBirthDate failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.99".equals(stu.trCredits)
                        && "HS4".equals(stu.hsCode)
                        && "4.0".equals(stu.hsGpa)
                        && Integer.valueOf(1).equals(stu.hsClassRank)
                        && Integer.valueOf(2).equals(stu.hsSizeClass)
                        && Integer.valueOf(25).equals(stu.actScore)
                        && Integer.valueOf(700).equals(stu.satScore)
                        && "0".equals(stu.apScore)
                        && "RR".equals(stu.resident)
                        && date1011.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "G".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateBirthDate: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateGender results")
    void test0023() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateGender(cache, "111111111", "Z"), "updateGender failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.99".equals(stu.trCredits)
                        && "HS4".equals(stu.hsCode)
                        && "4.0".equals(stu.hsGpa)
                        && Integer.valueOf(1).equals(stu.hsClassRank)
                        && Integer.valueOf(2).equals(stu.hsSizeClass)
                        && Integer.valueOf(25).equals(stu.actScore)
                        && Integer.valueOf(700).equals(stu.satScore)
                        && "0".equals(stu.apScore)
                        && "RR".equals(stu.resident)
                        && date1011.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "Z".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "N".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateGender: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateHoldSeverity results")
    void test0024() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateHoldSeverity(cache, "111111111", "V"), "updateHoldSeverity failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.99".equals(stu.trCredits)
                        && "HS4".equals(stu.hsCode)
                        && "4.0".equals(stu.hsGpa)
                        && Integer.valueOf(1).equals(stu.hsClassRank)
                        && Integer.valueOf(2).equals(stu.hsSizeClass)
                        && Integer.valueOf(25).equals(stu.actScore)
                        && Integer.valueOf(700).equals(stu.satScore)
                        && "0".equals(stu.apScore)
                        && "RR".equals(stu.resident)
                        && date1011.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "Z".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "V".equals(stu.sevAdminHold)
                        && THREE_HALVES.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateHoldSeverity: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateTimeLimitFactor results")
    void test0025() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateTimeLimitFactor(cache, "111111111", PI),
                        "updateTimeLimitFactor failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.99".equals(stu.trCredits)
                        && "HS4".equals(stu.hsCode)
                        && "4.0".equals(stu.hsGpa)
                        && Integer.valueOf(1).equals(stu.hsClassRank)
                        && Integer.valueOf(2).equals(stu.hsSizeClass)
                        && Integer.valueOf(25).equals(stu.actScore)
                        && Integer.valueOf(700).equals(stu.satScore)
                        && "0".equals(stu.apScore)
                        && "RR".equals(stu.resident)
                        && date1011.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "Z".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "V".equals(stu.sevAdminHold)
                        && PI.equals(stu.timelimitFactor)
                        && "L".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateTimeLimitFactor: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateLicensed results")
    void test0026() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateLicensed(cache, "111111111", "W"), "updateLicensed failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.99".equals(stu.trCredits)
                        && "HS4".equals(stu.hsCode)
                        && "4.0".equals(stu.hsGpa)
                        && Integer.valueOf(1).equals(stu.hsClassRank)
                        && Integer.valueOf(2).equals(stu.hsSizeClass)
                        && Integer.valueOf(25).equals(stu.actScore)
                        && Integer.valueOf(700).equals(stu.satScore)
                        && "0".equals(stu.apScore)
                        && "RR".equals(stu.resident)
                        && date1011.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "Z".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "V".equals(stu.sevAdminHold)
                        && PI.equals(stu.timelimitFactor)
                        && "W".equals(stu.licensed)
                        && "CA".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateLicensed: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateCampus results")
    void test0027() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateCampus(cache, "111111111", "1"), "updateCampus failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.99".equals(stu.trCredits)
                        && "HS4".equals(stu.hsCode)
                        && "4.0".equals(stu.hsGpa)
                        && Integer.valueOf(1).equals(stu.hsClassRank)
                        && Integer.valueOf(2).equals(stu.hsSizeClass)
                        && Integer.valueOf(25).equals(stu.actScore)
                        && Integer.valueOf(700).equals(stu.satScore)
                        && "0".equals(stu.apScore)
                        && "RR".equals(stu.resident)
                        && date1011.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "Z".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "V".equals(stu.sevAdminHold)
                        && PI.equals(stu.timelimitFactor)
                        && "W".equals(stu.licensed)
                        && "1".equals(stu.campus)
                        && "SE".equals(stu.stuEmail)
                        && "AE".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateCampus: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateEmail results")
    void test0028() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateEmail(cache, "111111111", "stu@mail", "adv@mail"),
                        "updateEmail failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.99".equals(stu.trCredits)
                        && "HS4".equals(stu.hsCode)
                        && "4.0".equals(stu.hsGpa)
                        && Integer.valueOf(1).equals(stu.hsClassRank)
                        && Integer.valueOf(2).equals(stu.hsSizeClass)
                        && Integer.valueOf(25).equals(stu.actScore)
                        && Integer.valueOf(700).equals(stu.satScore)
                        && "0".equals(stu.apScore)
                        && "RR".equals(stu.resident)
                        && date1011.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "Z".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "V".equals(stu.sevAdminHold)
                        && PI.equals(stu.timelimitFactor)
                        && "W".equals(stu.licensed)
                        && "1".equals(stu.campus)
                        && "stu@mail".equals(stu.stuEmail)
                        && "adv@mail".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "AT".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateEmail: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateAdmission results")
    void test0029() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateAdmission(cache, "111111111", "2", Boolean.TRUE),
                        "updateAdmission failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.99".equals(stu.trCredits)
                        && "HS4".equals(stu.hsCode)
                        && "4.0".equals(stu.hsGpa)
                        && Integer.valueOf(1).equals(stu.hsClassRank)
                        && Integer.valueOf(2).equals(stu.hsSizeClass)
                        && Integer.valueOf(25).equals(stu.actScore)
                        && Integer.valueOf(700).equals(stu.satScore)
                        && "0".equals(stu.apScore)
                        && "RR".equals(stu.resident)
                        && date1011.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "Z".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "V".equals(stu.sevAdminHold)
                        && PI.equals(stu.timelimitFactor)
                        && "W".equals(stu.licensed)
                        && "1".equals(stu.campus)
                        && "stu@mail".equals(stu.stuEmail)
                        && "adv@mail".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "2".equals(stu.admitType)
                        && "OE".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateAdmission: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updateCourseOrder results")
    void test0030() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updateCourseOrder(cache, "111111111", "3"), "updateCourseOrder failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.99".equals(stu.trCredits)
                        && "HS4".equals(stu.hsCode)
                        && "4.0".equals(stu.hsGpa)
                        && Integer.valueOf(1).equals(stu.hsClassRank)
                        && Integer.valueOf(2).equals(stu.hsSizeClass)
                        && Integer.valueOf(25).equals(stu.actScore)
                        && Integer.valueOf(700).equals(stu.satScore)
                        && "0".equals(stu.apScore)
                        && "RR".equals(stu.resident)
                        && date1011.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "Z".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "V".equals(stu.sevAdminHold)
                        && PI.equals(stu.timelimitFactor)
                        && "W".equals(stu.licensed)
                        && "1".equals(stu.campus)
                        && "stu@mail".equals(stu.stuEmail)
                        && "adv@mail".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "2".equals(stu.admitType)
                        && "3".equals(stu.orderEnforce)
                        && "P".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updateCourseOrder: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("updatePacingStructure results")
    void test0031() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {
                assertTrue(RawStudentLogic.updatePacingStructure(cache, "111111111", "4"),
                        "updatePacingStructure failed");

                final RawStudent stu = RawStudentLogic.query(cache, "111111111", false);

                assertNotNull(stu, "No record returned by query");

                final boolean found = "111111111".equals(stu.stuId)
                        && Integer.valueOf(123450).equals(stu.pidm)
                        && "Benoit".equals(stu.lastName)
                        && "Steven".equals(stu.firstName)
                        && "Steve".equals(stu.prefName)
                        && "R".equals(stu.middleInitial)
                        && termSM22.equals(stu.aplnTerm)
                        && "X".equals(stu.clazz)
                        && "AA".equals(stu.college)
                        && "BB".equals(stu.dept)
                        && "CC".equals(stu.programCode)
                        && "DD".equals(stu.minor)
                        && termFA30.equals(stu.estGraduation)
                        && "1.99".equals(stu.trCredits)
                        && "HS4".equals(stu.hsCode)
                        && "4.0".equals(stu.hsGpa)
                        && Integer.valueOf(1).equals(stu.hsClassRank)
                        && Integer.valueOf(2).equals(stu.hsSizeClass)
                        && Integer.valueOf(25).equals(stu.actScore)
                        && Integer.valueOf(700).equals(stu.satScore)
                        && "0".equals(stu.apScore)
                        && "RR".equals(stu.resident)
                        && date1011.equals(stu.birthdate)
                        && "ET".equals(stu.ethnicity)
                        && "Z".equals(stu.gender)
                        && "Y".equals(stu.discipHistory)
                        && "DS".equals(stu.discipStatus)
                        && "V".equals(stu.sevAdminHold)
                        && PI.equals(stu.timelimitFactor)
                        && "W".equals(stu.licensed)
                        && "1".equals(stu.campus)
                        && "stu@mail".equals(stu.stuEmail)
                        && "adv@mail".equals(stu.adviserEmail)
                        && "PWD".equals(stu.password)
                        && "2".equals(stu.admitType)
                        && "3".equals(stu.orderEnforce)
                        && "4".equals(stu.pacingStructure)
                        && date34.equals(stu.createDt);

                assertTrue(found, "Student not found");
            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while querying after updatePacingStructure: " + ex.getMessage());
        }
    }

    /** Test case. */
    @Test
    @DisplayName("delete results")
    void test0032() {

        try {
            final DbConnection conn = ctx.checkOutConnection();
            final Cache cache = new Cache(dbProfile, conn);

            try {

                final RawStudent raw2 = new RawStudent("222222222",
                        Integer.valueOf(67890), "Smith", "Agent",
                        "Agent Smith", "Q",
                        termFA20, "cl", "co",
                        "de", "pc", "Minor",
                        termFSM24, "12", "HS2",
                        "1.75", Integer.valueOf(1000), Integer.valueOf(1500),
                        Integer.valueOf(24), Integer.valueOf(630), "4",
                        "re", date1112, "et",
                        "g", "N", "ds", "Y",
                        HALF, "Y", "ca", "se",
                        "ae", "pwd", "at", "de",
                        "Q", date56, null, null);

                final boolean result = RawStudentLogic.INSTANCE.delete(cache, raw2);
                assertTrue(result, "delete returned false");

                final List<RawStudent> all = RawStudentLogic.INSTANCE.queryAll(cache);

                assertEquals(2, all.size(), "Incorrect record count from queryAll after delete");

                boolean found1 = false;
                boolean found3 = false;

                for (final RawStudent test : all) {
                    if ("111111111".equals(test.stuId)
                            && Integer.valueOf(123450).equals(test.pidm)
                            && "Benoit".equals(test.lastName)
                            && "Steven".equals(test.firstName)
                            && "Steve".equals(test.prefName)
                            && "R".equals(test.middleInitial)
                            && termSM22.equals(test.aplnTerm)
                            && "X".equals(test.clazz)
                            && "AA".equals(test.college)
                            && "BB".equals(test.dept)
                            && "CC".equals(test.programCode)
                            && "DD".equals(test.minor)
                            && termFA30.equals(test.estGraduation)
                            && "1.99".equals(test.trCredits)
                            && "HS4".equals(test.hsCode)
                            && "4.0".equals(test.hsGpa)
                            && Integer.valueOf(1).equals(test.hsClassRank)
                            && Integer.valueOf(2).equals(test.hsSizeClass)
                            && Integer.valueOf(25).equals(test.actScore)
                            && Integer.valueOf(700).equals(test.satScore)
                            && "0".equals(test.apScore)
                            && "RR".equals(test.resident)
                            && date1011.equals(test.birthdate)
                            && "ET".equals(test.ethnicity)
                            && "Z".equals(test.gender)
                            && "Y".equals(test.discipHistory)
                            && "DS".equals(test.discipStatus)
                            && "V".equals(test.sevAdminHold)
                            && PI.equals(test.timelimitFactor)
                            && "W".equals(test.licensed)
                            && "1".equals(test.campus)
                            && "stu@mail".equals(test.stuEmail)
                            && "adv@mail".equals(test.adviserEmail)
                            && "PWD".equals(test.password)
                            && "2".equals(test.admitType)
                            && "3".equals(test.orderEnforce)
                            && "4".equals(test.pacingStructure)
                            && date34.equals(test.createDt)) {

                        found1 = true;
                    } else if ("333333333".equals(test.stuId)
                            && Integer.valueOf(99999).equals(test.pidm)
                            && "Smith".equals(test.lastName)
                            && "Adam".equals(test.firstName)
                            && "Goober".equals(test.prefName)
                            && "H".equals(test.middleInitial)
                            && termFA20.equals(test.aplnTerm)
                            && "cl".equals(test.clazz)
                            && "co".equals(test.college)
                            && "de".equals(test.dept)
                            && "pc".equals(test.programCode)
                            && "Minor".equals(test.minor)
                            && termFSM24.equals(test.estGraduation)
                            && "12".equals(test.trCredits)
                            && "HS2".equals(test.hsCode)
                            && "1.75".equals(test.hsGpa)
                            && Integer.valueOf(1000).equals(test.hsClassRank)
                            && Integer.valueOf(1500).equals(test.hsSizeClass)
                            && Integer.valueOf(24).equals(test.actScore)
                            && Integer.valueOf(630).equals(test.satScore)
                            && "4".equals(test.apScore)
                            && "re".equals(test.resident)
                            && date1112.equals(test.birthdate)
                            && "et".equals(test.ethnicity)
                            && "g".equals(test.gender)
                            && "N".equals(test.discipHistory)
                            && "ds".equals(test.discipStatus)
                            && "Y".equals(test.sevAdminHold)
                            && HALF.equals(test.timelimitFactor)
                            && "Y".equals(test.licensed)
                            && "ca".equals(test.campus)
                            && "se".equals(test.stuEmail)
                            && "ae".equals(test.adviserEmail)
                            && "pwd".equals(test.password)
                            && "at".equals(test.admitType)
                            && "de".equals(test.orderEnforce)
                            && "Q".equals(test.pacingStructure)
                            && date56.equals(test.createDt)) {

                        found3 = true;
                    } else {
                        Log.warning("Bad stuId ", test.stuId);
                        Log.warning("Bad pidm ", test.pidm);
                        Log.warning("Bad lastName ", test.lastName);
                        Log.warning("Bad firstName ", test.firstName);
                        Log.warning("Bad prefName ", test.prefName);
                        Log.warning("Bad middleInitial ", test.middleInitial);
                        Log.warning("Bad aplnTerm ", test.aplnTerm);
                        Log.warning("Bad clazz ", test.clazz);
                        Log.warning("Bad college ", test.college);
                        Log.warning("Bad dept ", test.dept);
                        Log.warning("Bad programCode ", test.programCode);
                        Log.warning("Bad minor ", test.minor);
                        Log.warning("Bad estGraduation ", test.estGraduation);
                        Log.warning("Bad trCredits ", test.trCredits);
                        Log.warning("Bad hsCode ", test.hsCode);
                        Log.warning("Bad hsGpa ", test.hsGpa);
                        Log.warning("Bad hsClassRank ", test.hsClassRank);
                        Log.warning("Bad hsSizeClass ", test.hsSizeClass);
                        Log.warning("Bad actScore ", test.actScore);
                        Log.warning("Bad satScore ", test.satScore);
                        Log.warning("Bad apScore ", test.apScore);
                        Log.warning("Bad resident ", test.resident);
                        Log.warning("Bad birthdate ", test.birthdate);
                        Log.warning("Bad ethnicity ", test.ethnicity);
                        Log.warning("Bad gender ", test.gender);
                        Log.warning("Bad discipHistory ", test.discipHistory);
                        Log.warning("Bad discipStatus ", test.discipStatus);
                        Log.warning("Bad sevAdminHold ", test.sevAdminHold);
                        Log.warning("Bad timelimitFactor ", test.timelimitFactor);
                        Log.warning("Bad licensed ", test.licensed);
                        Log.warning("Bad campus ", test.campus);
                        Log.warning("Bad stuEmail ", test.stuEmail);
                        Log.warning("Bad adviserEmail ", test.adviserEmail);
                        Log.warning("Bad password ", test.password);
                        Log.warning("Bad admitType ", test.admitType);
                        Log.warning("Bad orderEnforce ", test.orderEnforce);
                        Log.warning("Bad pacingStructure ", test.pacingStructure);
                        Log.warning("Bad createDt ", test.createDt);
                    }
                }

                assertTrue(found1, "Student 1 not found");
                assertTrue(found3, "Student 3 not found");

            } finally {
                ctx.checkInConnection(conn);
            }
        } catch (final SQLException ex) {
            Log.warning(ex);
            fail("Exception while deleting student: " + ex.getMessage());
        }
    }

    /** Clean up. */
    @AfterAll
    static void cleanUp() {

        try {
            final DbConnection conn = ctx.checkOutConnection();

            try {
                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("DELETE FROM student");
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

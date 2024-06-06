package dev.mathops.db.logic;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawDiscipline;
import dev.mathops.db.old.rawrecord.RawExceptStu;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawMpecrDenied;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;
import dev.mathops.db.old.rawrecord.RawPendingExam;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStcunit;
import dev.mathops.db.old.rawrecord.RawStcuobjective;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStmsg;
import dev.mathops.db.old.rawrecord.RawStresource;
import dev.mathops.db.old.rawrecord.RawStsurveyqa;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rawrecord.RawStvisit;
import dev.mathops.db.old.rec.MasteryAttemptRec;
import dev.mathops.db.old.rec.StandardMilestoneRec;
import dev.mathops.db.old.rec.StudentCourseMasteryRec;
import dev.mathops.db.old.rec.StudentStandardMilestoneRec;
import dev.mathops.db.old.rec.StudentUnitMasteryRec;
import dev.mathops.db.old.svc.term.TermRec;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code StudentData} class.
 */
final class TestStudentData {

    /** The test student ID. */
    private static final String TEST_STUDENT_ID = "823251213";

    /** A common string. */
    private static final String FOUND = "Found ";

    /** The database context. */
    private static DbContext ctx = null;

    /** The data cache. */
    private static Cache cache = null;

    /** TThe student data object being tested. */
    private static StudentData data = null;

    /**
     * Constructs a new {@code TestStudentData}.
     */
    TestStudentData() {

        // No action
    }

    /** Initialize the test class. */
    @BeforeAll
    static void initTests() {

        final ContextMap map = ContextMap.getDefaultInstance();
        DbConnection.registerDrivers();

        final DbProfile dbProfile = map.getCodeProfile(Contexts.BATCH_PATH);
        if (dbProfile == null) {
            Log.warning("Code profile not found");
        } else {
            ctx = dbProfile.getDbContext(ESchemaUse.PRIMARY);

            try {
                final DbConnection conn = ctx.checkOutConnection();
                cache = new Cache(dbProfile, conn);
                data = new StudentData(cache, TEST_STUDENT_ID, ELiveRefreshes.NONE);
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query active term")
    void test0001() {

        try {
            final long t0 = System.currentTimeMillis();
            final TermRec result1 = data.getActiveTerm();
            final long t1 = System.currentTimeMillis();
            final TermRec result2 = data.getActiveTerm();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load active term");

            final long duration1 = t1 - t0;
            final String dur1 = Long.toString(duration1);
            Log.info("Found active term ", result1.term.longString, " in ", dur1, " ms.");

            assertSame(result1, result2, "Re-queried active term not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried active term in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving active term: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query student record")
    void test0002() {

        try {
            final long t0 = System.currentTimeMillis();
            final RawStudent result1 = data.getStudentRecord();
            final long t1 = System.currentTimeMillis();
            final RawStudent result2 = data.getStudentRecord();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load student record");

            final long duration1 = t1 - t0;
            final String screenName = result1.getScreenName();
            final String dur1 = Long.toString(duration1);
            Log.info("Found student record for ", screenName, " in ", dur1, " ms.");

            assertSame(result1, result2, "Re-queried student record not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried student  record in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving student record: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query administrative holds")
    void test0003() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawAdminHold> result1 = data.getHolds();
            final long t1 = System.currentTimeMillis();
            final List<RawAdminHold> result2 = data.getHolds();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load administrative holds");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " administrative holds in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried administrative holds list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried administrative holds in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving administrative holds: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query disciplinary actions")
    void test0004() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawDiscipline> result1 = data.getDisciplinaryActions();
            final long t1 = System.currentTimeMillis();
            final List<RawDiscipline> result2 = data.getDisciplinaryActions();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load disciplinary actions");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " disciplinary actions in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried disciplinary actions list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried disciplinary actions in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving disciplinary actions: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query visiting student registrations")
    void test0005() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawExceptStu> result1 = data.getVisitingRegistrations();
            final long t1 = System.currentTimeMillis();
            final List<RawExceptStu> result2 = data.getVisitingRegistrations();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load visiting student registrations");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " visiting student registrations in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried visiting student registrations list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried visiting student registrations in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving visiting student registrations: ",
                    exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query transfer credit")
    void test0006() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawFfrTrns> result1 = data.getTransferCredit();
            final long t1 = System.currentTimeMillis();
            final List<RawFfrTrns> result2 = data.getTransferCredit();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load transfer credit");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " transfer credit in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried transfer credit list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried transfer credit in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving transfer credit: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query resources on loan")
    void test0007() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawStresource> result1 = data.getResourcesOnLoan();
            final long t1 = System.currentTimeMillis();
            final List<RawStresource> result2 = data.getResourcesOnLoan();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load resources on loan");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " resources on loan in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried resources on loan list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried resources on loan in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving resources on loan: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query messages sent")
    void test0008() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawStmsg> result1 = data.getMessagesSent();
            final long t1 = System.currentTimeMillis();
            final List<RawStmsg> result2 = data.getMessagesSent();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load messages sent");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " messages sent in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried messages sent list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried messages sent in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving messages sent: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query center visits")
    void test0009() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawStvisit> result1 = data.getCenterVisits();
            final long t1 = System.currentTimeMillis();
            final List<RawStvisit> result2 = data.getCenterVisits();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load center visits");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " center visits in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried center visits list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried center visits in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving center visits: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query pending exams")
    void test0010() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawPendingExam> result1 = data.getPendingExams();
            final long t1 = System.currentTimeMillis();
            final List<RawPendingExam> result2 = data.getPendingExams();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load pending exams");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " pending exams in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried pending exams list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried pending exams in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving pending exams: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query special categories")
    void test0011() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawSpecialStus> result1 = data.getSpecialCategories();
            final long t1 = System.currentTimeMillis();
            final List<RawSpecialStus> result2 = data.getSpecialCategories();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load special categories");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " special categories in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried special categories list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried special categories in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving special categories: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query math plan responses")
    void test0012() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawStmathplan> result1 = data.getMathPlanResponses();
            final long t1 = System.currentTimeMillis();
            final List<RawStmathplan> result2 = data.getMathPlanResponses();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load math plan responses");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " math plan responses in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried math plan responses list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried math plan responses in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving math plan responses: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query placement attempts")
    void test0013() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawStmpe> result1 = data.getPlacementAttempts();
            final long t1 = System.currentTimeMillis();
            final List<RawStmpe> result2 = data.getPlacementAttempts();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load placement attempts");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " placement attempts in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried placement attempts list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried placement attempts in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving placement attempts: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query placement credit")
    void test0014() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawMpeCredit> result1 = data.getPlacementCredit();
            final long t1 = System.currentTimeMillis();
            final List<RawMpeCredit> result2 = data.getPlacementCredit();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load placement credit");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " placement credit in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried placement credit list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried placement credit in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving placement credit: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query placement credit denied")
    void test0015() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawMpecrDenied> result1 = data.getPlacementDenied();
            final long t1 = System.currentTimeMillis();
            final List<RawMpecrDenied> result2 = data.getPlacementDenied();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load placement credit denied");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " placement credit denied in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried placement credit denied list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried placement credit denied in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving placement credit denied: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query challenge exam attempts")
    void test0016() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawStchallenge> result1 = data.getChallengeExams();
            final long t1 = System.currentTimeMillis();
            final List<RawStchallenge> result2 = data.getChallengeExams();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load challenge exam attempts");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " challenge exam attempts in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried challenge exam attempts list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried challenge exam attempts in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving challenge exam attempts: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query survey responses")
    void test0017() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawStsurveyqa> result1 = data.getSurveyResponses();
            final long t1 = System.currentTimeMillis();
            final List<RawStsurveyqa> result2 = data.getSurveyResponses();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load survey responses");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " survey responses in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried survey responses list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried survey responses in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving survey responses: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query registrations")
    void test0018() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawStcourse> result1 = data.getRegistrations();
            final long t1 = System.currentTimeMillis();
            final List<RawStcourse> result2 = data.getRegistrations();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load registrations");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " registrations in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried registrations list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried registrations in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving registrations: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query active registrations")
    void test0019() {

        try {
            final TermRec active = data.getActiveTerm();
            final long t0 = System.currentTimeMillis();
            final List<RawStcourse> result1 = data.getActiveRegistrations(active.term);
            final long t1 = System.currentTimeMillis();
            final List<RawStcourse> result2 = data.getActiveRegistrations(active.term);
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load active registrations");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " active registrations in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried active registrations list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried active registrations in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving active registrations: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query completed registrations")
    void test0020() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawStcourse> result1 = data.getCompletedRegistrations();
            final long t1 = System.currentTimeMillis();
            final List<RawStcourse> result2 = data.getCompletedRegistrations();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load completed registrations");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " completed registrations in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried completed registrations list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried completed registrations in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving completed registrations: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query student term configurations")
    void test0021() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawStterm> result1 = data.getStudentTerms();
            final long t1 = System.currentTimeMillis();
            final List<RawStterm> result2 = data.getStudentTerms();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load student term configurations");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " student term configurations in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried student term configurations list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried student term configurations in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving student term configurations: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query student exams")
    void test0022() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawStexam> result1 = data.getStudentExams();
            final long t1 = System.currentTimeMillis();
            final List<RawStexam> result2 = data.getStudentExams();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load student exams");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " student exams in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried student exams list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried student exams in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving student exams: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query student homework")
    void test0023() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawSthomework> result1 = data.getStudentHomework();
            final long t1 = System.currentTimeMillis();
            final List<RawSthomework> result2 = data.getStudentHomework();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load student homework");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " student homework in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried student homework list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried student homework in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving student homework: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query student course unit status")
    void test0024() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawStcunit> result1 = data.getStudentCourseUnits();
            final long t1 = System.currentTimeMillis();
            final List<RawStcunit> result2 = data.getStudentCourseUnits();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load student course unit status");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " student course unit status in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried student course unit status list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried student course unit status in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving student course unit status: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query student course objective status")
    void test0025() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawStcuobjective> result1 = data.getStudentCourseObjectives();
            final long t1 = System.currentTimeMillis();
            final List<RawStcuobjective> result2 = data.getStudentCourseObjectives();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load student course objective status");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " student course objective status in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried student course objective status list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried student course objective status in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving student course objective status: ",
                    exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query student standard mastery attempts")
    void test0026() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<MasteryAttemptRec> result1 = data.getMasteryAttempts();
            final long t1 = System.currentTimeMillis();
            final List<MasteryAttemptRec> result2 = data.getMasteryAttempts();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load student standard mastery attempts");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " student standard mastery attempts in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried student standard mastery attempts list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried student standard mastery attempts in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving student standard mastery attempts: ",
                    exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query student course mastery status")
    void test0027() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<StudentCourseMasteryRec> result1 = data.getStudentCourseMastery();
            final long t1 = System.currentTimeMillis();
            final List<StudentCourseMasteryRec> result2 = data.getStudentCourseMastery();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load student course mastery status");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " student course mastery status in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried student course mastery status list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried student course mastery status in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving student course mastery status: ",
                    exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query student unit mastery status")
    void test0028() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<StudentUnitMasteryRec> result1 = data.getStudentUnitMastery();
            final long t1 = System.currentTimeMillis();
            final List<StudentUnitMasteryRec> result2 = data.getStudentUnitMastery();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load student unit mastery status");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " student unit mastery status in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried student unit mastery status list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried student unit mastery status in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving student unit mastery status: ",
                    exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query course milestones")
    void test0029() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawMilestone> result1 = data.getMilestones();
            final long t1 = System.currentTimeMillis();
            final List<RawMilestone> result2 = data.getMilestones();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load course milestones");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " course milestones in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried course milestones list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried course milestones in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving course milestones: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query student course milestone overrides")
    void test0030() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawStmilestone> result1 = data.getStudentMilestones();
            final long t1 = System.currentTimeMillis();
            final List<RawStmilestone> result2 = data.getStudentMilestones();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load student course milestone overrides");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " student course milestone overrides in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried student course milestone overrides list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried student course milestone overrides in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving student course milestone overrides: ",
                    exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query standard milestones")
    void test0031() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<StandardMilestoneRec> result1 = data.getStandardMilestones();
            final long t1 = System.currentTimeMillis();
            final List<StandardMilestoneRec> result2 = data.getStandardMilestones();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load standard milestones");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " standard milestones in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried standard milestones list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried standard milestones in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving standard milestones: ", exMsg);
            fail(msg);
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Query student standard milestone overrides")
    void test0033() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<StudentStandardMilestoneRec> result1 = data.getStudentStandardMilestones();
            final long t1 = System.currentTimeMillis();
            final List<StudentStandardMilestoneRec> result2 = data.getStudentStandardMilestones();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load student standard milestone overrides");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " student standard milestone overrides in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried student standard milestone overrides list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried student standard milestone overrides in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving student standard milestone overrides: ",
                    exMsg);
            fail(msg);
        }
    }


    /** Test case. */
    @Test
    @DisplayName("Query student deadline appeals")
    void test0034() {

        try {
            final long t0 = System.currentTimeMillis();
            final List<RawPaceAppeals> result1 = data.getDeadlineAppeals();
            final long t1 = System.currentTimeMillis();
            final List<RawPaceAppeals> result2 = data.getDeadlineAppeals();
            final long t2 = System.currentTimeMillis();

            assertNotNull(result1, "Unable to load student deadline appeals");

            final long duration1 = t1 - t0;
            final int count = result1.size();
            final String countStr = Integer.toString(count);
            final String dur1 = Long.toString(duration1);
            Log.info(FOUND, countStr, " student deadline appeals in ", dur1, " ms.");

            assertEquals(result1, result2, "Re-queried student deadline appeals list not same as original");

            final long duration2 = t2 - t1;
            final String dur2 = Long.toString(duration2);
            Log.info("Re-queried student deadline appeals in ", dur2, " ms.");
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while retrieving student deadline appeals: ",
                    exMsg);
            fail(msg);
        }
    }

    /**
     * Clean up.
     */
    @AfterAll
    static void cleanUp() {

        if (ctx != null && cache != null) {
            ctx.checkInConnection(cache.conn);
        }
    }
}

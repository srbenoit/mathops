package dev.mathops.db.logic;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@code ChallengeExamLogic} class.
 */
final class TestChallengeExamLogic {

    /** A test student ID. */
    private static final String TEST_STUDENT_1 = "999011121";

    /** A test student ID. */
    private static final String TEST_STUDENT_2 = "999011122";

    /** A test student ID. */
    private static final String TEST_STUDENT_3 = "999033121";

    /** A test student ID. */
    private static final String TEST_STUDENT_4 = "833165649";

    /** The database context. */
    private static DbContext ctx = null;

    /** The data cache. */
    private static Cache cache = null;

    /** TThe student data object being tested. */
    private static StudentData data1 = null;

    /** TThe student data object being tested. */
    private static StudentData data2 = null;

    /** TThe student data object being tested. */
    private static StudentData data3 = null;

    /** TThe student data object being tested. */
    private static StudentData data4 = null;
    /**
     * Constructs a new {@code TestChallengeExamLogic}.
     */
    TestChallengeExamLogic() {

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
                data1 = new StudentData(cache, TEST_STUDENT_1, ELiveRefreshes.NONE);
                data2 = new StudentData(cache, TEST_STUDENT_2, ELiveRefreshes.NONE);
                data3 = new StudentData(cache, TEST_STUDENT_3, ELiveRefreshes.NONE);
                data4 = new StudentData(cache, TEST_STUDENT_4, ELiveRefreshes.NONE);
            } catch (final SQLException ex) {
                Log.warning(ex);
            }
        }
    }

    /** Test case. */
    @Test
    @DisplayName("Examine challenge status of test students")
    void test0001() {

        try {
            final ChallengeExamLogic logic1 = new ChallengeExamLogic(data1);
            Log.fine("Student who has taken unproctored 'POOOO', has 2 proctored attempts remaining:");
            mainPrintResult(logic1);

            Log.fine(CoreConstants.EMPTY);

            final ChallengeExamLogic logic2 = new ChallengeExamLogic(data2);
            Log.fine("Student who has taken proctored, has unproctored and proctored remaining:");
            mainPrintResult(logic2);

            Log.fine(CoreConstants.EMPTY);

            final ChallengeExamLogic logic3 = new ChallengeExamLogic(data3);
            Log.fine("Student who has taken 2 proctored, has no attempts remaining:");
            mainPrintResult(logic3);

            Log.fine(CoreConstants.EMPTY);

            final ChallengeExamLogic logic4 = new ChallengeExamLogic(data4);
            Log.fine("Test student with course credit:");
            mainPrintResult(logic4);
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String exMsg = ex.getMessage();
            final String msg = SimpleBuilder.concat("Exception while computing challenge status: ", exMsg);
            fail(msg);
        }
    }

    /**
     * Prints results of one test.
     *
     * @param logic the logic object
     */
    private static void mainPrintResult(final ChallengeExamLogic logic) {

        Log.fine(" MATH 117:");
        mainPrintStatus(logic.getStatus(RawRecordConstants.M117));

        Log.fine(" MATH 118:");
        mainPrintStatus(logic.getStatus(RawRecordConstants.M118));

        Log.fine(" MATH 124:");
        mainPrintStatus(logic.getStatus(RawRecordConstants.M124));

        Log.fine(" MATH 125:");
        mainPrintStatus(logic.getStatus(RawRecordConstants.M125));

        Log.fine(" MATH 126:");
        mainPrintStatus(logic.getStatus(RawRecordConstants.M126));
    }

    /**
     * Prints the contents of a {@code ChallengeExamStatus}.
     *
     * @param status the {@code ChallengeExamStatus} whose contents to print
     */
    private static void mainPrintStatus(final ChallengeExamStatus status) {

        if (status.availableExamId != null) {
            Log.fine(" Version available : ", status.availableExamId);
        }
        if (status.reasonUnavailable != null) {
            Log.fine(" Why Unavailable : ", status.reasonUnavailable);
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

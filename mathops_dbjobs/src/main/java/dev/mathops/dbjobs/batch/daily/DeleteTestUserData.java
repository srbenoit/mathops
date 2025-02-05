package dev.mathops.dbjobs.batch.daily;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawMpeLogLogic;
import dev.mathops.db.old.rawlogic.RawMpecrDeniedLogic;
import dev.mathops.db.old.rawlogic.RawPaceAppealsLogic;
import dev.mathops.db.old.rawlogic.RawPendingExamLogic;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStcuobjectiveLogic;
import dev.mathops.db.old.rawlogic.RawStetextLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawStmathplanLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStmsgLogic;
import dev.mathops.db.old.rawlogic.RawStresourceLogic;
import dev.mathops.db.old.rawlogic.RawStsurveyqaLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawlogic.RawUsersLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawMpeLog;
import dev.mathops.db.old.rawrecord.RawMpecrDenied;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;
import dev.mathops.db.old.rawrecord.RawPendingExam;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStcuobjective;
import dev.mathops.db.old.rawrecord.RawStetext;
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
import dev.mathops.db.old.rawrecord.RawUsers;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Deletes data like exams, placement attempts, placement results, and so forth for test users.
 */
public enum DeleteTestUserData {
    ;

    /** The list of test student IDs whose data to clear. */
    private static final List<String> TEST_STUS = Arrays.asList("111223333", "888888888", "823251213");

    /** The prefix for bulk test student IDs. */
    private static final String TEST_PREFIX = "99";

    /** Flag to set batch into debug mode. */
    private static final boolean DEBUG = false;

    /**
     * Executes the job.
     */
    public static void execute() {

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);
        final Cache cache = new Cache(profile);

        Log.info("Running DELETE_TEST_USER_DATA job");

        try {
            cleanAdminHold(cache);
            cleanMpeCredit(cache);
            cleanMpecrDenied(cache);
            cleanMpeLog(cache);
            cleanPaceApeals(cache);
            cleanPendingExam(cache);
            cleanStchallenge(cache);
            cleanStcourse(cache);
            cleanStcuobjective(cache);
            cleanStetext(cache);
            cleanStexam(cache);
            cleanSthomework(cache);
            cleanStmathplan(cache);
            cleanStmilestone(cache);
            cleanStmpe(cache);
            cleanStmsg(cache);
            cleanStresource(cache);
            cleanStsurveyqa(cache);
            cleanStterm(cache);
            cleanUsers(cache);
            resetStudent(cache);
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Removes rows from the "admin_hold" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanAdminHold(final Cache cache) throws SQLException {

        final List<RawAdminHold> all = RawAdminHoldLogic.queryAll(cache);

        for (final RawAdminHold rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting ADMIN_HOLD record for ", stuId);
                if (!DEBUG) {
                    RawAdminHoldLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "mpe_credit" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanMpeCredit(final Cache cache) throws SQLException {

        final List<RawMpeCredit> all = RawMpeCreditLogic.queryAll(cache);

        for (final RawMpeCredit rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting MPE_CREDIT record for ", stuId);
                if (!DEBUG) {
                    RawMpeCreditLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "mpecr_denied" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanMpecrDenied(final Cache cache) throws SQLException {

        final List<RawMpecrDenied> all = RawMpecrDeniedLogic.queryAll(cache);

        for (final RawMpecrDenied rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting MPE_CR_DENIED record for ", stuId);
                if (!DEBUG) {
                    RawMpecrDeniedLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "mpe_log" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanMpeLog(final Cache cache) throws SQLException {

        final List<RawMpeLog> all = RawMpeLogLogic.queryAll(cache);

        for (final RawMpeLog rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting MPE_LOG record for ", stuId);
                if (!DEBUG) {
                    RawMpeLogLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "pace_appeals" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanPaceApeals(final Cache cache) throws SQLException {

        final List<RawPaceAppeals> all = RawPaceAppealsLogic.queryAll(cache);

        for (final RawPaceAppeals rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting PACE_APPEALS record for ", stuId);
                if (!DEBUG) {
                    RawPaceAppealsLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "pending_exam" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanPendingExam(final Cache cache) throws SQLException {

        final List<RawPendingExam> all = RawPendingExamLogic.queryAll(cache);

        for (final RawPendingExam rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting PENDING_EXAM record for ", stuId);
                if (!DEBUG) {
                    RawPendingExamLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "stchallenge" and "stchallengeqa" tables for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanStchallenge(final Cache cache) throws SQLException {

        final List<RawStchallenge> all = RawStchallengeLogic.queryAll(cache);

        for (final RawStchallenge rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting STCHALLENGE record for ", stuId);
                if (!DEBUG) {
                    RawStchallengeLogic.deleteAttemptAndAnswers(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "stcourse" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanStcourse(final Cache cache) throws SQLException {

        final List<RawStcourse> all = RawStcourseLogic.queryAll(cache);

        for (final RawStcourse rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting STCOURSE record for ", stuId);
                if (!DEBUG) {
                    RawStcourseLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "stcuobjective" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanStcuobjective(final Cache cache) throws SQLException {

        final List<RawStcuobjective> all = RawStcuobjectiveLogic.queryAll(cache);

        for (final RawStcuobjective rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting STCUOBJECTIVE record for ", stuId);
                if (!DEBUG) {
                    RawStcuobjectiveLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "stetext" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanStetext(final Cache cache) throws SQLException {

        final List<RawStetext> all = RawStetextLogic.queryAll(cache);

        for (final RawStetext rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting STETEXT record for ", stuId);
                if (!DEBUG) {
                    RawStetextLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "stexam" and "stqa" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanStexam(final Cache cache) throws SQLException {

        final List<RawStexam> all = RawStexamLogic.queryAll(cache);

        for (final RawStexam rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting STEXAM record for ", stuId);
                if (!DEBUG) {
                    RawStexamLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "sthomework" and "sthomeworkqa" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanSthomework(final Cache cache) throws SQLException {

        final List<RawSthomework> all = RawSthomeworkLogic.queryAll(cache);

        for (final RawSthomework rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting STHOMEWORK record for ", stuId);
                if (!DEBUG) {
                    RawSthomeworkLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "admin_hold" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanStmathplan(final Cache cache) throws SQLException {

        final List<RawStmathplan> all = RawStmathplanLogic.queryAll(cache);

        for (final RawStmathplan rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting STMATHPLAN record for ", stuId);
                if (!DEBUG) {
                    RawStmathplanLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "admin_hold" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanStmilestone(final Cache cache) throws SQLException {

        final List<RawStmilestone> all = RawStmilestoneLogic.queryAll(cache);

        for (final RawStmilestone rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting STMILESTONE record for ", stuId);
                if (!DEBUG) {
                    RawStmilestoneLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "admin_hold" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanStmpe(final Cache cache) throws SQLException {

        final List<RawStmpe> all = RawStmpeLogic.queryAll(cache);

        for (final RawStmpe rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting STMPE record for ", stuId);
                if (!DEBUG) {
                    RawStmpeLogic.deleteExamAndAnswers(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "admin_hold" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanStmsg(final Cache cache) throws SQLException {

        final List<RawStmsg> all = RawStmsgLogic.queryAll(cache);

        for (final RawStmsg rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting STMSG record for ", stuId);
                if (!DEBUG) {
                    RawStmsgLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "admin_hold" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanStresource(final Cache cache) throws SQLException {

        final List<RawStresource> all = RawStresourceLogic.queryAll(cache);

        for (final RawStresource rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting STRESOURCE record for ", stuId);
                if (!DEBUG) {
                    RawStresourceLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "admin_hold" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanStsurveyqa(final Cache cache) throws SQLException {

        final List<RawStsurveyqa> all = RawStsurveyqaLogic.queryAll(cache);

        for (final RawStsurveyqa rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting STSURVEYQA record for ", stuId);
                if (!DEBUG) {
                    RawStsurveyqaLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "admin_hold" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanStterm(final Cache cache) throws SQLException {

        final List<RawStterm> all = RawSttermLogic.queryAll(cache);

        for (final RawStterm rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting STTERM record for ", stuId);
                if (!DEBUG) {
                    RawSttermLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "admin_hold" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void cleanUsers(final Cache cache) throws SQLException {

        final List<RawUsers> all = RawUsersLogic.queryAll(cache);

        for (final RawUsers rec : all) {
            final String stuId = rec.stuId;

            if (TEST_STUS.contains(stuId) || stuId.startsWith(TEST_PREFIX)) {
                Log.info("    Deleting USERS record for ", stuId);
                if (!DEBUG) {
                    RawUsersLogic.delete(cache, rec);
                }
            }
        }
    }

    /**
     * Removes rows from the "admin_hold" table for test users.
     *
     * @param cache the cache
     * @throws SQLException if there is an error accessing the database
     */
    private static void resetStudent(final Cache cache) throws SQLException {

        for (final String stuId : TEST_STUS) {
            final RawStudent stu = RawStudentLogic.query(cache, stuId, false);

            if (stu != null) {
                if (!"Y".equals(stu.licensed)) {
                    Log.info("    Updating LICENSED on student ", stuId);
                    RawStudentLogic.updateLicensed(cache, stuId, "Y");
                }

                if (stu.sevAdminHold != null) {
                    Log.info("    Updating SEV_ADMIN_HOLD on student ", stuId);
                    RawStudentLogic.updateHoldSeverity(cache, stuId, null);
                }
            }
        }
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        execute();
    }
}

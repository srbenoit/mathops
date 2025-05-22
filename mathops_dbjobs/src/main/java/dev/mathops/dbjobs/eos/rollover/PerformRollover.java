package dev.mathops.dbjobs.eos.rollover;

import dev.mathops.commons.ESuccessFailure;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawlogic.LogicUtils;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.dbjobs.EDebugMode;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Performs the roll-over process.
 */
public class PerformRollover implements Runnable {

    /** Flag to run in "debug" mode which prints changes that would be performed rather than performing any changes. */
    private static final EDebugMode DEBUG_MODE = EDebugMode.DEBUG;

    /** The data cache. */
    private final Cache cache;

    /**
     * Constructs a new {@code PerformRollover}.
     *
     * @param theCache the data cache
     */
    private PerformRollover(final Cache theCache) {

        this.cache = theCache;
    }

    /**
     * Runs the process.
     */
    public void run() {

        final TermLogic termLogic = TermLogic.get(this.cache);

        try {
            final TermRec activeTerm = termLogic.queryActive(this.cache);
            final TermRec nextTerm = termLogic.queryNext(this.cache);
            final TermRec priorTerm = termLogic.queryPrior(this.cache);

            if (activeTerm == null) {
                Log.warning("Unable to query the active term");
            } else if (nextTerm == null) {
                Log.warning("Unable to query the next term");
            } else if (priorTerm == null) {
                Log.warning("Unable to query the prior term");
            } else {
                Log.info("Active term: ", activeTerm.term.longString);
                Log.info("Next term: ", nextTerm.term.longString);
                Log.info("Prior term: ", priorTerm.term.longString);

                doRollover(activeTerm, nextTerm, priorTerm);
            }
        } catch (final SQLException ex) {
            Log.warning("Failed to query the active, next, or prior term.", ex);
        }
    }

    /**
     * Performs rollover tasks.
     *
     * @param activeTerm the active term
     * @param nextTerm   the next term
     * @param priorTerm  the prior term
     */
    private void doRollover(final TermRec activeTerm, final TermRec nextTerm, final TermRec priorTerm) {

        if (cleanHolds() == ESuccessFailure.SUCCESS
            && cleanBogusMapping(activeTerm) == ESuccessFailure.SUCCESS
            && cleanCalcs() == ESuccessFailure.SUCCESS
            && cleanChallengeFee(activeTerm) == ESuccessFailure.SUCCESS
            && cleanCrSection(activeTerm) == ESuccessFailure.SUCCESS
            && cleanCuSection(activeTerm) == ESuccessFailure.SUCCESS
            && cleanDontSubmit(activeTerm) == ESuccessFailure.SUCCESS
            && cleanEtextKey(activeTerm) == ESuccessFailure.SUCCESS
            && cleanExceptStu() == ESuccessFailure.SUCCESS
            && cleanMilestone(activeTerm) == ESuccessFailure.SUCCESS
            && cleanMpeLog() == ESuccessFailure.SUCCESS
            && cleanNewStu() == ESuccessFailure.SUCCESS
            && cleanPacingRules(activeTerm) == ESuccessFailure.SUCCESS
            && cleanPacingStructure(activeTerm) == ESuccessFailure.SUCCESS
            && cleanPaceTrackRule(activeTerm) == ESuccessFailure.SUCCESS
            && cleanMilestoneAppeal() == ESuccessFailure.SUCCESS
            && cleanPaceAppeals() == ESuccessFailure.SUCCESS
            && cleanPendingExam() == ESuccessFailure.SUCCESS
            && cleanSpecialStus() == ESuccessFailure.SUCCESS
            && cleanStc() == ESuccessFailure.SUCCESS
            && cleanStEtext(activeTerm) == ESuccessFailure.SUCCESS
            && cleanStMilestone() == ESuccessFailure.SUCCESS
            && cleanStTerm() == ESuccessFailure.SUCCESS
            && cleanStPaceSummary(activeTerm) == ESuccessFailure.SUCCESS
            && cleanStResource(activeTerm) == ESuccessFailure.SUCCESS
            && cleanStSurveyQa() == ESuccessFailure.SUCCESS
            && cleanUsers() == ESuccessFailure.SUCCESS
            && cleanDelphi() == ESuccessFailure.SUCCESS
            && cleanDelphiCheck() == ESuccessFailure.SUCCESS
            && cleanDupRegistr() == ESuccessFailure.SUCCESS
            && cleanFcrStudent() == ESuccessFailure.SUCCESS
            && cleanFinalCroll() == ESuccessFailure.SUCCESS
            && cleanFfrTrns(activeTerm) == ESuccessFailure.SUCCESS
            && cleanMpeCredit(activeTerm) == ESuccessFailure.SUCCESS
            && cleanMpeCrDenied(activeTerm) == ESuccessFailure.SUCCESS
            && cleanGradeRoll(activeTerm) == ESuccessFailure.SUCCESS
            && cleanPrevMilestoneAppeal(activeTerm) == ESuccessFailure.SUCCESS
            && cleanPrevExtensions(activeTerm) == ESuccessFailure.SUCCESS
            && cleanPrevStlmiss(activeTerm) == ESuccessFailure.SUCCESS
            && cleanPrevStmilestone(activeTerm) == ESuccessFailure.SUCCESS
            && cleanPrevStterm(activeTerm) == ESuccessFailure.SUCCESS
            && cleanprevStlock(activeTerm) == ESuccessFailure.SUCCESS
            && cleanPlcFee(activeTerm) == ESuccessFailure.SUCCESS
            && cleanRemoteMpe(activeTerm) == ESuccessFailure.SUCCESS
            && cleanStChallengeQa() == ESuccessFailure.SUCCESS
            && cleanStMpeQa() == ESuccessFailure.SUCCESS
            && cleanStudent(activeTerm) == ESuccessFailure.SUCCESS
            && updateStudent() == ESuccessFailure.SUCCESS
            && processIncompletes(activeTerm, nextTerm) == ESuccessFailure.SUCCESS
            && cleanStcourse(activeTerm) == ESuccessFailure.SUCCESS
            && processExams(activeTerm, nextTerm) == ESuccessFailure.SUCCESS
            && cleanStChallenge(activeTerm) == ESuccessFailure.SUCCESS
            && cleanStMpe(activeTerm) == ESuccessFailure.SUCCESS
            && processHomeworks(activeTerm, nextTerm) == ESuccessFailure.SUCCESS
            && rollPrereq(activeTerm, nextTerm) == ESuccessFailure.SUCCESS
            && rollSurveyQa(activeTerm, nextTerm) == ESuccessFailure.SUCCESS
            && cleanNextCampusCalendar() == ESuccessFailure.SUCCESS
            && cleanNextCSection() == ESuccessFailure.SUCCESS
            && cleanNextMilestone() == ESuccessFailure.SUCCESS
            && cleanNextRemoteMpe() == ESuccessFailure.SUCCESS
            && cleanNextSemesterCalendar() == ESuccessFailure.SUCCESS
            && rollTerm() == ESuccessFailure.SUCCESS
        ) {
            Log.warning("Rollover process completed successfully");
        } else {
            Log.warning("Rollover process terminated with an error.");
        }
    }

    /**
     * Deletes administrative holds except hold 05 (Deferred F) and hold 06 (Discretionary hold) or for any hold whose
     * hold number starts with "4" (overdue resources lends).
     *
     * @return success or failure
     */
    private ESuccessFailure cleanHolds() {

        Log.info("> Cleaning the admin_hold table.");

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        try {
            final List<RawAdminHold> holds = RawAdminHoldLogic.queryAll(this.cache);

            int count = 0;
            for (final RawAdminHold hold : holds) {
                final String holdId = hold.holdId;
                // FIXME: This should be a field on the "admin_hold" record - retain across terms?
                if ("06".equals(holdId) || holdId.startsWith("4")) {
                    continue;
                }

                if (DEBUG_MODE == EDebugMode.DEBUG) {
                    Log.info("> Deleting hold ", holdId, " for ", hold.stuId);
                } else {
                    RawAdminHoldLogic.delete(this.cache, hold);
                }
                ++count;
            }
            if (count == 0) {
                Log.info("> There were no administrative holds to delete.");
            } else {
                final String countStr = Integer.toString(count);
                Log.info("> Deleted ", countStr, " administrative holds.");
            }
        } catch (final SQLException ex) {
            Log.warning("> Failed to clean administrative holds.", ex);
            result = ESuccessFailure.FAILURE;
        }

        return result;
    }

    /**
     * Deletes bogus_mapping rows that are more than 6 years old.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure cleanBogusMapping(final TermRec activeTerm) {

        Log.info("> Cleaning the bogus_mapping table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 6;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM bogus_mapping WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "bogus_mapping");
    }

    /**
     * Deletes all calcs rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanCalcs() {

        Log.info("> Cleaning the calcs table.");
        return doSql("DELETE FROM calcs", "calcs");
    }

    /**
     * Deletes all challenge_fee rows that are 15 years old or more.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure cleanChallengeFee(final TermRec activeTerm) {

        Log.info("> Cleaning the challenge_fee table.");

        final LocalDate thresholdDate = activeTerm.startDate.minusYears(15L);
        final String sql = SimpleBuilder.concat("DELETE FROM challenge_fee WHERE bill_dt < ",
                LogicUtils.sqlDateValue(thresholdDate));

        return doSql(sql, "challenge_fee");
    }

    /**
     * Deletes all crsection rows that are 2 years old.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure cleanCrSection(final TermRec activeTerm) {

        Log.info("> Cleaning the crsection table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 2;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM crsection WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "crsection");
    }

    /**
     * Deletes all cusection rows that are 2 years old.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure cleanCuSection(final TermRec activeTerm) {

        Log.info("> Cleaning the cusection table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 2;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM cusection WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "cusection");
    }

    /**
     * Deletes all dont_submit rows that are 6 years old.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure cleanDontSubmit(final TermRec activeTerm) {

        Log.info("> Cleaning the dont_submit table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 6;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM dont_submit WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "dont_submit");
    }

    /**
     * Deletes etext_key rows activated more than 1 year ago.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure cleanEtextKey(final TermRec activeTerm) {

        Log.info("> Cleaning the etext_key table.");

        final LocalDate threshold = activeTerm.startDate.minusYears(1L);
        final String sql = SimpleBuilder.concat("DELETE FROM etext_key WHERE active_dt IS NOT NULL AND active_dt<",
                LogicUtils.sqlDateValue(threshold));

        return doSql(sql, "etext_key");
    }

    /**
     * Deletes all except_stu rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanExceptStu() {

        Log.info("> Cleaning the except_stu table.");

        return doSql("DELETE FROM except_stu", "except_stu");
    }

    /**
     * Deletes all milestone rows that are 2 years old.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure cleanMilestone(final TermRec activeTerm) {

        Log.info("> Cleaning the milestone table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 2;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM milestone WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "milestone");
    }

    /**
     * Deletes all mpe_log rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanMpeLog() {

        Log.info("> Cleaning the mpe_log table.");

        return doSql("DELETE FROM mpe_log", "mpe_log");
    }

    /**
     * Deletes all newstu rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanNewStu() {

        Log.info("> Cleaning the newstu table.");

        return doSql("DELETE FROM newstu", "newstu");
    }

    /**
     * Deletes all pacing_rules rows that are 2 years old.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure cleanPacingRules(final TermRec activeTerm) {

        Log.info("> Cleaning the pacing_rules table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 2;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM pacing_rules WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "pacing_rules");
    }

    /**
     * Deletes all pacing_structure rows that are 2 years old.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure cleanPacingStructure(final TermRec activeTerm) {

        Log.info("> Cleaning the pacing_structure table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 2;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM pacing_structure WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "pacing_structure");
    }

    /**
     * Deletes all pace_track_rule rows that are 2 years old.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure cleanPaceTrackRule(final TermRec activeTerm) {

        Log.info("> Cleaning the pace_track_rule table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 2;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM pace_track_rule WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "pace_track_rule");
    }

    /**
     * Deletes all milestone_appeal rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanMilestoneAppeal() {

        Log.info("> Cleaning the milestone_appeal table.");

        return doSql("DELETE FROM milestone_appeal", "milestone_appeal");
    }

    /**
     * Deletes all pace_appeals rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanPaceAppeals() {

        Log.info("> Cleaning the pace_appeals table.");

        return doSql("DELETE FROM pace_appeals", "pace_appeals");
    }

    /**
     * Deletes all pending_exam rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanPendingExam() {

        Log.info("> Cleaning the pending_exam table.");

        return doSql("DELETE FROM pending_exam", "pending_exam");
    }

    /**
     * Deletes all special_stus rows that are not 'ADMIN', 'STEVE', or 'ADVISER'.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanSpecialStus() {

        Log.info("> Cleaning the special_stus table.");

        return doSql("DELETE FROM special_stus WHERE stu_type NOT IN ('ADMIN','STEVE','ADVISER')", "special_stus");
    }

    /**
     * Deletes all stc rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanStc() {

        Log.info("> Cleaning the stc table.");

        return doSql("DELETE FROM stc", "stc");
    }

    /**
     * Deletes all stetext rows that are expired or refunded, then cleans any that are more than 15 years old.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanStEtext(final TermRec activeTerm) {

        Log.info("> Cleaning the stetext table.");

        final String sql1 = SimpleBuilder.concat("DELETE FROM stetext WHERE ((expiration_dt <= ",
                LogicUtils.sqlDateValue(activeTerm.endDate), " AND expiration_dt IS NOT NULL) ",
                "OR refund_dt IS NOT NULL) AND stu_id NOT IN (SELECT stu_id FROM stcourse WHERE course_grade = 'I')");

        final LocalDate threshold = activeTerm.startDate.minusYears(15L);
        final String sql2 = SimpleBuilder.concat("DELETE FROM stetext WHERE active_dt < ",
                LogicUtils.sqlDateValue(threshold));

        ESuccessFailure result = doSql(sql1, "stetext");
        if (result == ESuccessFailure.SUCCESS) {
            result = doSql(sql2, "stetext");
        }

        return result;
    }

    /**
     * Deletes all stmilestone rows that are not associated with an Incomplete.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanStMilestone() {

        Log.info("> Cleaning the stmilestone table.");

        final String sql = SimpleBuilder.concat("DELETE FROM stmilestone",
                " WHERE stu_id NOT IN (SELECT stu_id FROM stcourse WHERE course_grade = 'I')",
                " AND stu_id NOT IN (SELECT stu_id FROM stcourse WHERE i_in_progress = 'Y')");

        return doSql(sql, "stmilestone");
    }

    /**
     * Deletes all stterm rows that are not associated with an Incomplete.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanStTerm() {

        Log.info("> Cleaning the stterm table.");

        final String sql = SimpleBuilder.concat("DELETE FROM stterm",
                " WHERE stu_id NOT IN (SELECT stu_id FROM stcourse WHERE course_grade = 'I')",
                " AND stu_id NOT IN (SELECT stu_id FROM stcourse WHERE i_in_progress = 'Y')");

        return doSql(sql, "stterm");
    }

    /**
     * Deletes all stpace-summary rows that are 5 years old.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure cleanStPaceSummary(final TermRec activeTerm) {

        Log.info("> Cleaning the stpace_summary table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 5;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM stpace_summary WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "stpace_summary");
    }

    /**
     * Deletes all stresource rows that are 5 years old.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure cleanStResource(final TermRec activeTerm) {

        Log.info("> Cleaning the stresource table.");

        final String sql = "DELETE FROM stresource WHERE return_dt IS NOT NULL";

        return doSql(sql, "stresource");
    }

    /**
     * Deletes all stsurveyqa rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanStSurveyQa() {

        Log.info("> Cleaning the stsurveyqa table.");

        final String sql = "DELETE FROM stsurveyqa";

        return doSql(sql, "stsurveyqa");
    }

    /**
     * Deletes all users rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanUsers() {

        Log.info("> Cleaning the users table.");

        final String sql = "DELETE FROM users";

        return doSql(sql, "users");
    }

    /**
     * Deletes all delphi rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanDelphi() {

        Log.info("> Cleaning the delphi table.");

        final String sql = "DELETE FROM delphi";

        return doSql(sql, "delphi");
    }

    /**
     * Deletes all delphi_check rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanDelphiCheck() {

        Log.info("> Cleaning the delphi_check table.");

        final String sql = "DELETE FROM delphi_check";

        return doSql(sql, "delphi_check");
    }

    /**
     * Deletes all dup_registr rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanDupRegistr() {

        Log.info("> Cleaning the dup_registr table.");

        final String sql = "DELETE FROM dup_registr";

        return doSql(sql, "dup_registr");
    }

    /**
     * Deletes all fcr_student rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanFcrStudent() {

        Log.info("> Cleaning the fcr_student table.");

        final String sql = "DELETE FROM fcr_student";

        return doSql(sql, "fcr_student");
    }

    /**
     * Deletes all final_croll rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanFinalCroll() {

        Log.info("> Cleaning the final_croll table.");

        final String sql = "DELETE FROM final_croll";

        return doSql(sql, "final_croll");
    }

    /**
     * Deletes all ffr_trns rows that are more than 8 years old.
     *
     * @param activeTerm the active term record
     * @return success or failure
     */
    private ESuccessFailure cleanFfrTrns(final TermRec activeTerm) {

        Log.info("> Cleaning the ffr_trns table.");

        final LocalDate threshold = activeTerm.startDate.minusYears(8L);
        final String sql = SimpleBuilder.concat("DELETE FROM ffr_trns WHERE exam_dt <= ",
                LogicUtils.sqlDateValue(threshold));

        return doSql(sql, "ffr_trns");
    }

    /**
     * Deletes all mpe_credit rows that are more than 8 years old.
     *
     * @param activeTerm the active term record
     * @return success or failure
     */
    private ESuccessFailure cleanMpeCredit(final TermRec activeTerm) {

        Log.info("> Cleaning the mpe_credit table.");

        final LocalDate threshold = activeTerm.startDate.minusYears(8L);
        final String sql = SimpleBuilder.concat("DELETE FROM mpe_credit WHERE exam_dt <= ",
                LogicUtils.sqlDateValue(threshold));

        return doSql(sql, "mpe_credit");
    }

    /**
     * Deletes all mpecr_denied rows that are more than 8 years old.
     *
     * @param activeTerm the active term record
     * @return success or failure
     */
    private ESuccessFailure cleanMpeCrDenied(final TermRec activeTerm) {

        Log.info("> Cleaning the mpecr_denied table.");

        final LocalDate threshold = activeTerm.startDate.minusYears(8L);
        final String sql = SimpleBuilder.concat("DELETE FROM mpecr_denied WHERE exam_dt <= ",
                LogicUtils.sqlDateValue(threshold));

        return doSql(sql, "mpecr_denied");
    }

    /**
     * Deletes all grade_roll rows that are 12 years old.
     *
     * @param activeTerm the active term record
     * @return success or failure
     */
    private ESuccessFailure cleanGradeRoll(final TermRec activeTerm) {

        Log.info("> Cleaning the grade_roll table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 12;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM grade_roll WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "grade_roll");
    }

    /**
     * Deletes all prev_milestone_appeal rows that are 5 years old.
     *
     * @param activeTerm the active term record
     * @return success or failure
     */
    private ESuccessFailure cleanPrevMilestoneAppeal(final TermRec activeTerm) {

        Log.info("> Cleaning the prev_milestone_appeal table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 5;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM prev_milestone_appeal WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "prev_milestone_appeal");
    }

    /**
     * Deletes all prev_extensions rows that are 5 years old.
     *
     * @param activeTerm the active term record
     * @return success or failure
     */
    private ESuccessFailure cleanPrevExtensions(final TermRec activeTerm) {

        Log.info("> Cleaning the prev_extensions table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 5;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM prev_extensions WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "prev_extensions");
    }

    /**
     * Deletes all prev_stlmiss rows that are 5 years old.
     *
     * @param activeTerm the active term record
     * @return success or failure
     */
    private ESuccessFailure cleanPrevStlmiss(final TermRec activeTerm) {

        Log.info("> Cleaning the prev_stlmiss table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 5;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM prev_stlmiss WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "prev_stlmiss");
    }

    /**
     * Deletes all prev_stmilestone rows that are 5 years old.
     *
     * @param activeTerm the active term record
     * @return success or failure
     */
    private ESuccessFailure cleanPrevStmilestone(final TermRec activeTerm) {

        Log.info("> Cleaning the prev_stmilestone table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 5;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM prev_stmilestone WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "prev_stmilestone");
    }

    /**
     * Deletes all prev_stterm rows that are 5 years old.
     *
     * @param activeTerm the active term record
     * @return success or failure
     */
    private ESuccessFailure cleanPrevStterm(final TermRec activeTerm) {

        Log.info("> Cleaning the prev_stterm table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 5;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM prev_stterm WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "prev_stterm");
    }

    /**
     * Deletes all prev_stlock rows that are 5 years old.
     *
     * @param activeTerm the active term record
     * @return success or failure
     */
    private ESuccessFailure cleanprevStlock(final TermRec activeTerm) {

        Log.info("> Cleaning the prev_stlock table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 5;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM prev_stlock WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "prev_stlock");
    }

    /**
     * Deletes all plc_fee rows that are 15 years old or more.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure cleanPlcFee(final TermRec activeTerm) {

        Log.info("> Cleaning the plc_fee table.");

        final LocalDate thresholdDate = activeTerm.startDate.minusYears(15L);
        final String sql = SimpleBuilder.concat("DELETE FROM plc_fee WHERE bill_dt < ",
                LogicUtils.sqlDateValue(thresholdDate));

        return doSql(sql, "plc_fee");
    }

    /**
     * Deletes all remote_mpe rows for the concluding term.
     *
     * @param activeTerm the active term record
     * @return success or failure
     */
    private ESuccessFailure cleanRemoteMpe(final TermRec activeTerm) {

        Log.info("> Cleaning the remote_mpe table.");

        final String sql = SimpleBuilder.concat("DELETE FROM remote_mpe WHERE term_yr=", activeTerm.term.shortYear,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "remote_mpe");
    }

    /**
     * Deletes all stchallengeqa rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanStChallengeQa() {

        Log.info("> Cleaning the stchallengeqa table.");

        final String sql = "DELETE FROM stchallengeqa";

        return doSql(sql, "stchallengeqa");
    }

    /**
     * Deletes all stmpeqa rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanStMpeQa() {

        Log.info("> Cleaning the stmpeqa table.");

        final String sql = "DELETE FROM stmpeqa";

        return doSql(sql, "stmpeqa");
    }

    /**
     * Deletes students who are no longer referenced by any student-referencing tables, then resets the remaining
     * student records to set licensed = 'N', order_enforce = 'N', pacing_structure = null, sev_admin_hold = null,
     * campus= null.
     *
     * @param activeTerm the active term
     * @return success or failure
     */
    private ESuccessFailure cleanStudent(final TermRec activeTerm) {

        Log.info("> Cleaning the student table.");

        final LocalDate thresholdDate = activeTerm.startDate.minusYears(6L);

        final String sql = SimpleBuilder.concat(
                "DELETE FROM student WHERE create_dt <= ", LogicUtils.sqlDateValue(thresholdDate),
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM admin_hold)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM challenge_fee)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM discipline)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM ffr_trns)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM grade_roll)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM mpe_credit)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM mpecr_denied)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM plc_fee)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM prev_milestone_appeal)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM prev_extensions)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM prev_stlock)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM prev_stlmiss)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM prev_stmilestone)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM prev_stterm)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM stchallenge)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM stcourse)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM stcunit)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM stcuobjective)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM stetext)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM stexam)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM sthomework)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM stmathplan)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM stmilestone)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM stmpe)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM stpace_summary)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM stresource)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM stsurveyqa)",
                " AND stu_id NOT IN (SELECT DISTINCT stu_id FROM stterm)");

        return doSql(sql, "student");
    }

    /**
     * Reset student records to set licensed = 'N', order_enforce = 'N', pacing_structure = null, sev_admin_hold = null,
     * campus = null, then updates discipline status to reflect ongoing sanctions rather than term-specific sanctions.
     *
     * @return success or failure
     */
    private ESuccessFailure updateStudent() {

        Log.info("> Updating the student table.");

        final String sql1 = SimpleBuilder.concat("UPDATE student SET (licensed,order_enforce,pacing_structure,",
                "sev_admin_hold,campus) = ('N', 'N', NULL, NULL, NULL)");
        final String sql2 = SimpleBuilder.concat("UPDATE student SET discip_status = NULL WHERE discip_status = '10'");
        final String sql3 = SimpleBuilder.concat("UPDATE student SET discip_status = '05' WHERE discip_status = '11'");
        final String sql4 = SimpleBuilder.concat("UPDATE student SET discip_status = '06' WHERE discip_status = '12'");
        final String sql5 = SimpleBuilder.concat("UPDATE student SET discip_status = '09' WHERE discip_status = '13'");
        final String sql6 = SimpleBuilder.concat("UPDATE student SET discip_status = '08' WHERE discip_status = '14'");

        ESuccessFailure result = doSql(sql1, "student");
        if (result == ESuccessFailure.SUCCESS) {
            result = doSql(sql2, "student");
            if (result == ESuccessFailure.SUCCESS) {
                result = doSql(sql3, "student");
                if (result == ESuccessFailure.SUCCESS) {
                    result = doSql(sql4, "student");
                    if (result == ESuccessFailure.SUCCESS) {
                        result = doSql(sql5, "student");
                        if (result == ESuccessFailure.SUCCESS) {
                            result = doSql(sql6, "student");
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Make the following changes to 'incomplete' records:
     * <pre>
     *    i_in_progress  = "Y"
     *    bypass_timeout = 0
     *    timeout_factor = NULL
     *    course_grade   = NULL
     *    exam_placed    = NULL
     *    forfeit_i      = NULL
     *    term           = next term
     *    term_yr        = next term_yr
     *    i_term_yr      = term_yr
     *    i_term         = term
     *    i_deadline_dt  = term.i_deadline_dt
     * </pre>
     *
     * @param activeTerm the active term
     * @param nextTerm   the next term
     * @return success or failure
     */
    private ESuccessFailure processIncompletes(final TermRec activeTerm, final TermRec nextTerm) {

        Log.info("> Processing Incompletes.");

        final String activeTermCode = LogicUtils.sqlStringValue(activeTerm.term.termCode);
        final String activeTermYear = LogicUtils.sqlIntegerValue(activeTerm.term.shortYear);
        final String nextTermCode = LogicUtils.sqlStringValue(nextTerm.term.termCode);
        final String nextTermYear = LogicUtils.sqlIntegerValue(nextTerm.term.shortYear);
        ;
        final String activeIncDeadline = LogicUtils.sqlDateValue(activeTerm.incDeadline);

        final String sql1 = SimpleBuilder.concat(
                "UPDATE stcourse SET bypass_timeout=0, timeout_factor=NULL, course_grade=NULL, exam_placed=NULL, ",
                "forfeit_i=NULL, term=", nextTermCode, ", term_yr=", nextTermYear,
                " WHERE i_in_progress = 'Y' AND  (i_deadline_dt IS NOT NULL AND i_deadline_dt >= ",
                LogicUtils.sqlDateValue(nextTerm.startDate), ")");

        final String sql2 = SimpleBuilder.concat(
                "UPDATE stcourse SET i_in_progress='Y', bypass_timeout=0, timeout_factor=NULL, course_grade=NULL, ",
                "exam_placed=NULL, forfeit_i=NULL, term=", nextTermCode, ", term_yr=", nextTermYear, ", i_term=",
                activeTermCode, ", i_term_yr=", activeTermYear, " WHERE course_grade = 'I' AND term=", activeTermCode,
                " AND term_yr=", activeTermYear);

        final String sql3 = SimpleBuilder.concat("UPDATE stcourse SET i_deadline_dt=",
                activeIncDeadline, " WHERE i_in_progress = 'Y' AND i_deadline_dt IS NULL");

        ESuccessFailure result = doSql(sql1, "stcourse");
        if (result == ESuccessFailure.SUCCESS) {
            result = doSql(sql2, "stcourse");
            if (result == ESuccessFailure.SUCCESS) {
                result = doSql(sql3, "stcourse");
            }
        }

        return result;
    }

    /**
     * Deletes all stcourse rows that are 8 years old.
     *
     * @param activeTerm the active term record
     * @return success or failure
     */
    private ESuccessFailure cleanStcourse(final TermRec activeTerm) {

        Log.info("> Cleaning the stcourse table.");

        final int deleteYear = activeTerm.term.shortYear.intValue() - 8;
        final String deleteYearStr = Integer.toString(deleteYear);
        final String sql = SimpleBuilder.concat("DELETE FROM stcourse WHERE term_yr=", deleteYearStr,
                " AND term='", activeTerm.term.termCode, "'");

        return doSql(sql, "stcourse");
    }

    /**
     * Process stexam records.  The goal is to retain all exams for students who have INC grades and to retain
     * non-passing ELM Exam rows for students still working on prereq for M 117.
     *
     * <pre>
     *  Set all passed records to D or p, as appropriate
     *  For each ELM Tutorial in stexam:
     *      retain ELM review exams with passed = "Y", "N" or "P" if taken
     *        within past 4 years
     *      if there is no M100C row in mpe_credit
     *        Change passed exams to Y
     *        Change previously passed exams to P
     *        Change failed exams to F
     *  For each Precalc Tutorial in stexam:
     *      if currently a SUMMER term, retain Review Exams with passed = "Y","N"
     *         otherwise, delete all rows (work is allowed June 1 thru FA add
     *         deadline only)
     *      if currently a SUMMER term, update STETEXT.expiration_dt from SM date
     *         to TERM.i_deadline_dt (last day of classes) so continue to have
     *         etext access
     *  For each i_in_progress in stcourse:
     *      for all exams taken since (including) the term in which
     *      the incomplete was issued
     *        Change passed exams to Y (or P, as appropriate)
     *        Change failed exams to F
     *      change student.pacing_structure to csection.pacing_structure
     *
     *  Delete all stexam rows where passed != Y and != P and != F
     *  Change all stexam rows to passed = N where passed = F
     * </pre>
     *
     * @param activeTerm the active term
     * @param nextTerm   the next term
     * @return success or failure
     */
    private ESuccessFailure processExams(final TermRec activeTerm, final TermRec nextTerm) {

        Log.info("> Processing exams.");

        final String sql1 = "UPDATE stexam SET passed = 'D' WHERE passed = 'Y'";
        final String sql2 = "UPDATE stexam SET passed = 'p' WHERE passed = 'P'";

        ESuccessFailure result = doSql(sql1, "stexam");
        if (result == ESuccessFailure.SUCCESS) {
            result = doSql(sql2, "stexam");
        }

        final SystemData systemData = this.cache.getSystemData();

        // Keep exams from ending term for Incompletes
        if (result == ESuccessFailure.SUCCESS) {
            try {
                final List<RawStcourse> incompletes = RawStcourseLogic.queryOpenIncompletes(this.cache);
                for (final RawStcourse inc : incompletes) {
                    if (inc.iTermKey == null) {
                        Log.warning("There was an Incomplete with no incomplete term set");
                        continue;
                    }
                    final TermRec iTerm = systemData.getTerm(inc.iTermKey);
                    final RawCsection cSection = systemData.getCourseSection(inc.course, inc.sect, inc.iTermKey);

                    final String sql3 = SimpleBuilder.concat("UPDATE stexam SET passed = 'Y' ",
                            "WHERE stexam.stu_id = ", LogicUtils.sqlStringValue(inc.stuId),
                            " AND stexam.course = ", LogicUtils.sqlStringValue(inc.course),
                            " AND passed='D' AND stexam.exam_dt >= ", LogicUtils.sqlDateValue(iTerm.startDate),
                            " AND stexam.exam_dt <= ", LogicUtils.sqlDateValue(activeTerm.endDate));

                    final String sql4 = SimpleBuilder.concat("UPDATE stexam SET passed = 'P' ",
                            "WHERE stexam.stu_id = ", LogicUtils.sqlStringValue(inc.stuId),
                            " AND stexam.course = ", LogicUtils.sqlStringValue(inc.course),
                            " AND passed = 'p' AND stexam.exam_dt >= ", LogicUtils.sqlDateValue(iTerm.startDate),
                            " AND stexam.exam_dt <= ", LogicUtils.sqlDateValue(activeTerm.endDate));

                    final String sql5 = SimpleBuilder.concat("UPDATE stexam SET passed = 'F' ",
                            "WHERE stexam.stu_id = ", LogicUtils.sqlStringValue(inc.stuId),
                            " AND stexam.course = ", LogicUtils.sqlStringValue(inc.course), " AND passed = 'N'");

                    final String sql6 = SimpleBuilder.concat("UPDATE stexam SET passed = 'g' ",
                            "WHERE stexam.stu_id = ", LogicUtils.sqlStringValue(inc.stuId),
                            " AND stexam.course = ", LogicUtils.sqlStringValue(inc.course), " AND passed = 'G'");

                    final String sql7 = SimpleBuilder.concat("UPDATE stexam SET passed = 'v' ",
                            "WHERE stexam.stu_id = ", LogicUtils.sqlStringValue(inc.stuId),
                            " AND stexam.course = ", LogicUtils.sqlStringValue(inc.course), " AND passed = 'V'");

                    final String sql8 = SimpleBuilder.concat(
                            "UPDATE student SET pacing_structure = ",
                            LogicUtils.sqlStringValue(cSection.pacingStructure),
                            " WHERE student.stu_id =", LogicUtils.sqlStringValue(inc.stuId));

                    result = doSql(sql3, "stexam");
                    if (result == ESuccessFailure.SUCCESS) {
                        result = doSql(sql4, "stexam");
                        if (result == ESuccessFailure.SUCCESS) {
                            result = doSql(sql5, "stexam");
                            if (result == ESuccessFailure.SUCCESS) {
                                result = doSql(sql6, "stexam");
                                if (result == ESuccessFailure.SUCCESS) {
                                    result = doSql(sql7, "stexam");
                                    if (result == ESuccessFailure.SUCCESS) {
                                        result = doSql(sql8, "student");
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (final SQLException ex) {
                Log.warning("ERROR: failed to query open Incompletes", ex);
                result = ESuccessFailure.FAILURE;
            }
        }

        // ELM Tutorial -- keep history of those in progress to 4 years
        if (result == ESuccessFailure.SUCCESS) {
            final LocalDate threshold = activeTerm.startDate.minusYears(4L);

            final String sql10 = SimpleBuilder.concat("UPDATE stexam SET passed = 'Y' ",
                    "WHERE stexam.course = 'M 100T' AND passed = 'D'",
                    " AND stexam.exam_dt >= ", LogicUtils.sqlDateValue(threshold),
                    " AND stexam.stu_id NOT IN (SELECT stu_id FROM mpe_credit WHERE course = 'M 100C')");

            final String sql11 = SimpleBuilder.concat("UPDATE stexam SET passed = 'F' ",
                    "WHERE stexam.course = 'M 100T' AND passed = 'N'",
                    " AND stexam.exam_dt >= ", LogicUtils.sqlDateValue(threshold),
                    " AND stexam.stu_id NOT IN (SELECT stu_id FROM mpe_credit WHERE course = 'M 100C')");

            final String sql12 = SimpleBuilder.concat("UPDATE stexam SET passed = 'P' ",
                    "WHERE stexam.course = 'M 100T' AND passed = 'p'",
                    " AND stexam.exam_dt >= ", LogicUtils.sqlDateValue(threshold),
                    " AND stexam.stu_id NOT IN (SELECT stu_id FROM mpe_credit WHERE course = 'M 100C')");

            final String sql13 = SimpleBuilder.concat("UPDATE stexam SET passed = 'v' ",
                    "WHERE stexam.course = 'M 100T' AND passed = 'V'",
                    " AND stexam.exam_dt >= ", LogicUtils.sqlDateValue(threshold),
                    " AND stexam.stu_id NOT IN (SELECT stu_id FROM mpe_credit WHERE course = 'M 100C')");

            final String sql14 = SimpleBuilder.concat("UPDATE stexam SET passed = 'g' ",
                    "WHERE stexam.course = 'M 100T' AND passed = 'G'",
                    " AND stexam.exam_dt >= ", LogicUtils.sqlDateValue(threshold),
                    " AND stexam.stu_id NOT IN (SELECT stu_id FROM mpe_credit WHERE course = 'M 100C')");

            result = doSql(sql10, "stexam");
            if (result == ESuccessFailure.SUCCESS) {
                result = doSql(sql11, "stexam");
                if (result == ESuccessFailure.SUCCESS) {
                    result = doSql(sql12, "stexam");
                    if (result == ESuccessFailure.SUCCESS) {
                        result = doSql(sql13, "stexam");
                        if (result == ESuccessFailure.SUCCESS) {
                            result = doSql(sql14, "stexam");
                        }
                    }
                }
            }
        }

        // Precalculus Tutorial - keep history if this is a Summer -> Fall rollover
        if (result == ESuccessFailure.SUCCESS) {
            if ("SM".equals(activeTerm.term.termCode)) {
                final String sql20 = SimpleBuilder.concat("UPDATE stexam SET passed = 'Y' ",
                        "WHERE stexam.course IN ('M 1170','M 1180','M 1240','M 1250','M 1260') AND passed = 'D'");

                final String sql21 = SimpleBuilder.concat("UPDATE stexam SET passed = 'F' ",
                        "WHERE stexam.course IN ('M 1170','M 1180','M 1240','M 1250','M 1260') AND passed = 'N'");

                final String sql22 = SimpleBuilder.concat("UPDATE stexam SET passed = 'P' ",
                        "WHERE stexam.course IN ('M 1170','M 1180','M 1240','M 1250','M 1260') AND passed = 'p'");

                final String sql23 = SimpleBuilder.concat("UPDATE stexam SET passed = 'v' ",
                        "WHERE stexam.course IN ('M 1170','M 1180','M 1240','M 1250','M 1260') AND passed = 'V'");

                final String sql24 = SimpleBuilder.concat("UPDATE stexam SET passed = 'g' ",
                        "WHERE stexam.course IN ('M 1170','M 1180','M 1240','M 1250','M 1260') AND passed = 'G'");

                final String sql25 = SimpleBuilder.concat(
                        "DELETE FROM stexam WHERE passed NOT IN ('Y','P','F','g','v') AND exam_dt >= ",
                        LogicUtils.sqlDateValue(activeTerm.startDate));

                final String sql26 = "UPDATE stexam SET passed = 'P' WHERE passed = 'D'";
                final String sql27 = "UPDATE stexam SET passed = 'P' WHERE passed = 'p'";

                result = doSql(sql20, "stexam");
                if (result == ESuccessFailure.SUCCESS) {
                    result = doSql(sql21, "stexam");
                    if (result == ESuccessFailure.SUCCESS) {
                        result = doSql(sql22, "stexam");
                        if (result == ESuccessFailure.SUCCESS) {
                            result = doSql(sql23, "stexam");
                            if (result == ESuccessFailure.SUCCESS) {
                                result = doSql(sql24, "stexam");
                                if (result == ESuccessFailure.SUCCESS) {
                                    result = doSql(sql25, "stexam");
                                    if (result == ESuccessFailure.SUCCESS) {
                                        result = doSql(sql26, "stexam");
                                        if (result == ESuccessFailure.SUCCESS) {
                                            result = doSql(sql27, "stexam");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                final String sql28 = "DELETE FROM stexam WHERE passed NOT IN ('Y','P','F','g','v')";
                result = doSql(sql28, "stexam");
            }
        }

        if (result == ESuccessFailure.SUCCESS) {
            final String sql30 = "UPDATE stexam SET passed = 'N' WHERE passed = 'F'";
            final String sql31 = "UPDATE stexam SET passed = 'V' WHERE passed = 'v'";
            final String sql32 = "UPDATE stexam SET passed = 'G' WHERE passed = 'g'";

            result = doSql(sql30, "stexam");
            if (result == ESuccessFailure.SUCCESS) {
                result = doSql(sql31, "stexam");
                if (result == ESuccessFailure.SUCCESS) {
                    result = doSql(sql32, "stexam");
                }
            }
        }

        return result;
    }

    /**
     * Deletes all stchallenge rows older than 15 years.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanStChallenge(final TermRec activeTerm) {

        Log.info("> Cleaning the stchallenge table.");

        final LocalDate thresholdDate = activeTerm.endDate.minusYears(15L);
        final String sql = SimpleBuilder.concat("DELETE FROM stchallenge WHERE exam_dt <= ",
                LogicUtils.sqlDateValue(thresholdDate));

        return doSql(sql, "stchallenge");
    }

    /**
     * Deletes all stmpe rows older than 8 years
     *
     * @return success or failure
     */
    private ESuccessFailure cleanStMpe(final TermRec activeTerm) {

        Log.info("> Cleaning the stmpe table.");

        final LocalDate thresholdDate = activeTerm.endDate.minusYears(15L);
        final String sql = SimpleBuilder.concat("DELETE FROM stmpe WHERE exam_dt <= ",
                LogicUtils.sqlDateValue(thresholdDate),
                " AND stu_id NOT IN (SELECT stu_id FROM stexam WHERE course = 'M 100T')");

        return doSql(sql, "stmpe");
    }

    /**
     * Process sthomework records.  The goal is to retain all homeworks for Incompletes.
     *
     * @param activeTerm the active term
     * @param nextTerm   the next term
     * @return success or failure
     */
    private ESuccessFailure processHomeworks(final TermRec activeTerm, final TermRec nextTerm) {

        Log.info("> Processing homeworks.");

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        try {
            final List<RawStcourse> incompletes = RawStcourseLogic.queryOpenIncompletes(this.cache);
            final Set<String> studentsWithIncompletes = new HashSet<>(20);
            for (final RawStcourse reg : incompletes) {
                studentsWithIncompletes.add(reg.stuId);
            }

            final List<RawSthomework> sthomework = RawSthomeworkLogic.queryAll(this.cache);

            for (final RawSthomework hwRow : sthomework) {
                if (studentsWithIncompletes.contains(hwRow.stuId)) {
                    boolean searching = true;
                    for (final RawStcourse reg : incompletes) {
                        if (reg.stuId.equals(hwRow.stuId) && reg.course.equals(hwRow.course)) {
                            searching = false;
                            break;
                        }
                    }
                    if (searching) {
                        if (DEBUG_MODE == EDebugMode.DEBUG) {
                            Log.info("Deleting STHOMEWORK row for ", hwRow.stuId, " in ", hwRow.course, " unit ",
                                    hwRow.unit, " obj ", hwRow.objective);
                        } else {
                            RawSthomeworkLogic.delete(this.cache, hwRow);
                        }
                    }
                } else {
                    if (DEBUG_MODE == EDebugMode.DEBUG) {
                        Log.info("Deleting STHOMEWORK row for ", hwRow.stuId, " in ", hwRow.course, " unit ",
                                hwRow.unit, " obj ", hwRow.objective);
                    } else {
                        RawSthomeworkLogic.delete(this.cache, hwRow);
                    }
                }
            }
        } catch (final SQLException ex) {
            Log.warning("ERROR: failed to query open Incompletes or student homeworks", ex);
            result = ESuccessFailure.FAILURE;
        }

        return result;
    }

    /**
     * Rolls prereq rows to the upcoming term.
     *
     * @param activeTerm the active term
     * @param nextTerm   the next term
     * @return success or failure
     */
    private ESuccessFailure rollPrereq(final TermRec activeTerm, final TermRec nextTerm) {

        Log.info("> Rolling prereq table.");

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        final String sql1 = SimpleBuilder.concat("DELETE FROM prereq ",
                "WHERE term_yr != ", LogicUtils.sqlIntegerValue(activeTerm.term.shortYear),
                " AND term != ", LogicUtils.sqlStringValue(activeTerm.term.termCode));

        final String sql2 = SimpleBuilder.concat("UPDATE prereq SET term = ",
                LogicUtils.sqlStringValue(nextTerm.term.termCode), ", term_yr = ",
                LogicUtils.sqlIntegerValue(nextTerm.term.shortYear));

        result = doSql(sql1, "prereq");
        if (result == ESuccessFailure.SUCCESS) {
            result = doSql(sql2, "prereq");
        }

        return result;
    }

    /**
     * Rolls surveyqa rows to the upcoming term.
     *
     * @param activeTerm the active term
     * @param nextTerm   the next term
     * @return success or failure
     */
    private ESuccessFailure rollSurveyQa(final TermRec activeTerm, final TermRec nextTerm) {

        Log.info("> Rolling surveyqa table.");

        ESuccessFailure result;

        final String sql1 = SimpleBuilder.concat("DELETE FROM surveyqa ",
                "WHERE term_yr != ", LogicUtils.sqlIntegerValue(activeTerm.term.shortYear),
                " AND term != ", LogicUtils.sqlStringValue(activeTerm.term.termCode));

        final String sql2 = SimpleBuilder.concat("UPDATE surveyqa SET term = ",
                LogicUtils.sqlStringValue(nextTerm.term.termCode), ", term_yr = ",
                LogicUtils.sqlIntegerValue(nextTerm.term.shortYear));

        result = doSql(sql1, "surveyqa");
        if (result == ESuccessFailure.SUCCESS) {
            result = doSql(sql2, "surveyqa");
        }

        return result;
    }

    /**
     * Deletes all next_campus_calendar rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanNextCampusCalendar() {

        Log.info("> Cleaning next_campus_calendar table.");

        return doSql("DELETE from next_campus_calendar", "next_campus_calendar");
    }

    /**
     * Deletes all next_csection rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanNextCSection() {

        Log.info("> Cleaning next_csection table.");

        return doSql("DELETE from next_csection", "next_csection");
    }

    /**
     * Deletes all next_milestone rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanNextMilestone() {

        Log.info("> Cleaning next_milestone table.");

        return doSql("DELETE from next_milestone", "next_milestone");
    }

    /**
     * Deletes all next_remote_mpe rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanNextRemoteMpe() {

        Log.info("> Cleaning next_remote_mpe table.");

        return doSql("DELETE from next_remote_mpe", "next_remote_mpe");
    }

    /**
     * Deletes all next_semester_calendar rows.
     *
     * @return success or failure
     */
    private ESuccessFailure cleanNextSemesterCalendar() {

        Log.info("> Cleaning next_semester_calendar table.");

        return doSql("DELETE from next_semester_calendar", "next_semester_calendar");
    }

    /**
     * Rolls term rows.
     *
     * @return success or failure
     */
    private ESuccessFailure rollTerm() {

        Log.info("> Rolling the term table.");

        final String sql1 = "UPDATE term SET active = 'N' WHERE active = 'P'";
        final String sql2 = "UPDATE term SET active = 'P' WHERE active = 'Y'";
        final String sql3 = "UPDATE term SET active = 'Y' WHERE active = 'X'";
        final String sql4 = "UPDATE term SET active = 'X' WHERE active = '2'";
        final String sql5 = "UPDATE term SET active = '2' WHERE active = '3'";
        final String sql6 = "UPDATE term SET active = '3' WHERE active = '4'";
        final String sql7 = "UPDATE term set active_index = active_index - 1";

        ESuccessFailure result = doSql(sql1, "term");
        if (result == ESuccessFailure.SUCCESS) {
            result = doSql(sql2, "term");
            if (result == ESuccessFailure.SUCCESS) {
                result = doSql(sql3, "term");
                if (result == ESuccessFailure.SUCCESS) {
                    result = doSql(sql4, "term");
                    if (result == ESuccessFailure.SUCCESS) {
                        result = doSql(sql5, "term");
                        if (result == ESuccessFailure.SUCCESS) {
                            result = doSql(sql6, "term");
                            if (result == ESuccessFailure.SUCCESS) {
                                result = doSql(sql7, "term");
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Executes an SQL statement.
     *
     * @param sql       the SQL statement
     * @param tableName the table name, for error logging
     * @return success or failure
     */
    private ESuccessFailure doSql(final String sql, final String tableName) {

        ESuccessFailure result = ESuccessFailure.SUCCESS;

        if (DEBUG_MODE == EDebugMode.DEBUG) {
            Log.info("> ", sql);
        } else {
            try {
                final DbConnection conn = this.cache.checkOutConnection(ESchema.LEGACY);

                try (final Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    conn.commit();
                } finally {
                    Cache.checkInConnection(conn);
                }
            } catch (final SQLException ex) {
                Log.warning("> Failed to clean ", tableName, " records.", ex);
                result = ESuccessFailure.FAILURE;
            }
        }

        return result;
    }

    /**
     * Main method to execute the batch job.
     *
     * @param args command-line arguments.
     */
    public static void main(final String... args) {

        DbConnection.registerDrivers();

        final DatabaseConfig config = DatabaseConfig.getDefault();
        final Profile profile = config.getCodeProfile(Contexts.BATCH_PATH);
        final Cache cache = new Cache(profile);

        final Runnable obj = new PerformRollover(cache);
        obj.run();
    }
}
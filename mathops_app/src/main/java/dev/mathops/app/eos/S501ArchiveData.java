package dev.mathops.app.eos;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawWhichDb;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.sql.SQLException;
import java.util.List;

/**
 * STEP 501: Archive data to the term database.
 *
 * <p>
 * Logic to copy all data that is to be archived from the production database to the term archive database.
 */
final class S501ArchiveData extends StepExecutable {

    /** TRUE to simply print what actions would be taken, FALSE to actually take actions. */
    private static final boolean DEBUG = true;

    /** The cache for the production database. */
    private final Cache productionCache;

    /** The cache for the archive database. */
    private final Cache archiveCache;

    /** The panel to update with status. */
    private final JProgressBar progress;

    /**
     * Constructs a new {@code S501ArchiveData}.
     *
     * @param theOwner           the step list that will hold the step
     * @param status             the status display
     * @param theProductionCache the data cache for the production database
     * @param theArchiveCache    the data cache for the archive database
     */
    S501ArchiveData(final StepList theOwner, final StepDisplay status, final Cache theProductionCache,
                    final Cache theArchiveCache) {

        super(theOwner, 501, "Copy production data into the archive database", null, status);

        this.productionCache = theProductionCache;
        this.archiveCache = theArchiveCache;

        final JPanel myStatus = new JPanel(new BorderLayout());
        this.progress = new JProgressBar(0, 100);
        this.progress.setStringPainted(true);
        this.progress.setString(CoreConstants.EMPTY);

        myStatus.add(this.progress, StackedBorderLayout.CENTER);
    }

    /**
     * A worker that manages updates during the execution of the step.
     */
    static class S501Worker extends SwingWorker<Boolean, StepStatus> {

        /** TRUE to simply print what actions would be taken, FALSE to actually take actions. */
        private static final boolean DEBUG = true;

        /** The owning step. */
        private final S501ArchiveData owner;

        /**
         * Constructs a new {@code S501Worker}.
         */
        S501Worker(final S501ArchiveData theOwner) {

            this.owner = theOwner;
        }

        /**
         * Called on the AWT event dispatch thread after "doInBackground" has completed.
         */
        public void done() {

            this.owner.progress.setString("Finished");
            this.owner.progress.setValue(100);
            this.owner.setFinished(true);
        }

        /**
         * Called on the AWT event dispatch thread asynchronously with data from "publish".
         *
         * @param chunks the chunks being processed
         */
        @Override
        protected void process(final List<StepStatus> chunks) {

            if (!chunks.isEmpty()) {
                final StepStatus last = chunks.getLast();

                final String task = last.currentTask();
                final int percent = last.percentComplete();

                this.owner.progress.setString(task);
                this.owner.progress.setValue(percent);
            }
        }

        /**
         * Fires a "publish" action to send status to the UI.
         *
         * @param percentage the percentage complete
         * @param task       the current task
         */
        private void firePublish(final int percentage, final String task) {

            publish(new StepStatus(percentage, task));
        }

        /**
         * Executes table construction logic on a worker thread.
         *
         * @return TRUE if successful; FALSE if not
         */
        @Override
        protected Boolean doInBackground() {

            Boolean result = Boolean.TRUE;

            firePublish(0, "Checking identities of source and target database...");

            if (areDatabasesCorrect()) {
                try {
                    final TermRec activeTerm = this.owner.productionCache.getSystemData().getActiveTerm();

                    if (activeTerm == null) {
                        firePublish(0, "NO ACTIVE TERM FOUND");
                        result = Boolean.FALSE;
                    } else {
                        final TermKey activeKey = activeTerm.term;
                        firePublish(1, "The active term is " + activeKey.longString);

                        archiveAdminHold(activeKey);
                    }
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    firePublish(0, "FAILED TO QUERY ACTIVE TERM");
                    result = Boolean.FALSE;
                }
            } else {
                result = Boolean.FALSE;
            }

            return result;
        }

        /**
         * Verifies that the "production" connection is a database that has a "which_db" table that contains "PROD" in
         * its 'desc' field for its single record, and that the "archive" connection is a database without a "which_db"
         * table. This operation also verifies that the connections to both databases are working.
         *
         * <p>
         * Note that this operation is performed even if DEBUG is true, since it alters no data.
         *
         * @return true if databases are verified; false if not
         */
        private boolean areDatabasesCorrect() {

            boolean ok = true;

            try {
                final RawWhichDb which = this.owner.productionCache.getSystemData().getWhichDb();
                if (which == null) {
                    Log.warning("No 'which_db' record found in production database");
                    ok = false;
                } else if (!"PROD".equals(which.descr)) {
                    Log.warning("'which_db' record production database has '", which.descr, "' rather than 'PROD'.");
                    ok = false;
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to query 'which_db' table in PROD");
                ok = false;
            }

            if (ok) {
                try {
                    final RawWhichDb which = this.owner.archiveCache.getSystemData().getWhichDb();
                    Log.warning("A 'which_db' record was found in archive database - there should be none.");
                    ok = false;
                } catch (final SQLException ex) {
                    // No action - this is the expected behavior
                }
            }

            return ok;
        }

        /**
         * Archives data from the "admin_hold" table.
         *
         * @param activeTerm the active term key
         */
        private void archiveAdminHold(final TermKey activeTerm) {

            final String sql = "SELECT * FROM admin_hold"
                               + " WHERE create_dt >= (SELECT start_dt FROM term WHERE active='Y'"
                               + "   AND (hold_id IN ('06','30') OR hold_id MATCHES '4?'";
        }
    }
}

//    { Archive bogus_mapping data }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/bogus_map.arc
//    SELECT * FROM bogus_mapping
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//
//    { Archive calculator data - exchanges or make-up exams }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/calcs.arc
//    SELECT * FROM calcs;
//
//
//    { Archive challenge_fee records. }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/challenge_fee.arc
//    SELECT * FROM challenge_fee
//    WHERE bill_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//    { Archive client_pc data - PCs in the Testing Center }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/client_pc.arc
//    SELECT * FROM client_pc;
//
//
//    { Archive course data, no longer adding term and term_yr fields }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/course.arc
//    SELECT * FROM course;
//
//
//    { Archive crsection records. }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/crsection.arc SELECT * FROM crsection
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//    UPDATE crsection SET (term,term_yr) =
//            ((SELECT term    FROM term WHERE active = "X"),
//            (SELECT term_yr FROM term WHERE active = "X"))
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//    LOAD FROM /usr/informix/a/math/ARC_FILES/crsection.arc
//    INSERT INTO crsection;
//
//
//    { Archive cunit data; roll active term over to next term;
//        reLOAD active term data; delete FROM 2 academic years ago;
//        save data because of incompletes }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/cunit.arc
//    SELECT * FROM cunit
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//    UPDATE cunit SET (term,term_yr) =
//            ((SELECT term FROM term WHERE active = "X"),
//            (SELECT term_yr FROM term WHERE active = "X"))
//    WHERE term = (SELECT term FROM term WHERE active = "Y")
//    AND term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//    DELETE FROM cunit
//    WHERE  term    =  (SELECT term    FROM term WHERE active = "Y")
//    AND   (term_yr = ((SELECT term_yr FROM term WHERE active = "Y") - 2)
//    OR     term_yr = ((SELECT term_yr FROM term WHERE active = "Y") + 98));
//
//    LOAD FROM /usr/informix/a/math/ARC_FILES/cunit.arc insert into cunit;
//
//
//    { Archive cuobjective data; roll active term over to next term;
//        reLOAD active term data; delete FROM 2 academic years ago;
//        save data because of incompletes }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/cuobj.arc
//    SELECT * FROM cuobjective
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//    UPDATE cuobjective SET (term,term_yr) =
//            ((SELECT term FROM term WHERE active = "X"),
//            (SELECT term_yr FROM term WHERE active = "X"))
//    WHERE term = (SELECT term FROM term WHERE active = "Y")
//    AND term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//    DELETE FROM cuobjective
//    WHERE  term    =  (SELECT term    FROM term WHERE active = "Y")
//    AND   (term_yr = ((SELECT term_yr FROM term WHERE active = "Y") - 2)
//    OR     term_yr = ((SELECT term_yr FROM term WHERE active = "Y") + 98));
//
//    LOAD FROM /usr/informix/a/math/ARC_FILES/cuobj.arc insert into cuobjective;
//
//
//    { Archive cusection records. }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/cusection.arc SELECT * FROM cusection
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//    UPDATE cusection SET (term,term_yr) =
//            ((SELECT term    FROM term WHERE active = "X"),
//            (SELECT term_yr FROM term WHERE active = "X"))
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//    LOAD FROM /usr/informix/a/math/ARC_FILES/cusection.arc
//    INSERT INTO cusection;
//
//
//    { Archive discipline data }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/discipline.arc
//    SELECT * FROM discipline
//    WHERE dt_incident >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//    { Archive dont_submit records }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/dont_submit.arc
//    SELECT * FROM dont_submit
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//
//    { Archive etext data }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/etext.arc SELECT * FROM etext;
//
//
//    { Archive etext_course data }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/etext_course.arc
//    SELECT * FROM etext_course;
//
//
//    { Archive etext_key data }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/etext_key.arc
//    SELECT * FROM etext_key;
//
//
//    { Archive exam data; no longer add term and term_yr }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/exam.arc
//    SELECT * FROM exam;
//
//
//    { Archive examqa records }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/examqa.arc
//    SELECT * FROM examqa;
//
//
//    { Archive except_stu records }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/except.arc
//    SELECT * FROM except_stu;
//
//
//    { Archive ffr_trns records. }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/ffr_trns.arc
//    SELECT * FROM ffr_trns
//    WHERE exam_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//    { Archive grading_std data. }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/grading_std.arc
//    SELECT * FROM grading_std;
//
//
//    { Archive high_school records to keep consistent with Math Day records }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/highsch.arc
//    SELECT * FROM high_schools;
//
//
//    { Archive hold_type records to keep compatible with admin_holds archived }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/hold_type.arc SELECT * FROM hold_type;
//
//
//    { Archive homework data. }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/homework.arc
//    SELECT * FROM homework;
//
//
//    { Archive student records from Math Day }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/mdstudent.arc
//    SELECT * FROM mdstudent
//    WHERE create_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//    { Archive milestone data; load NEXT semester pre-edited data }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/milestone.arc
//    SELECT * FROM milestone
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//    LOAD FROM /usr/informix/a/math/NEXT_data/next_milestone
//    INSERT INTO milestone;
//
//
//    { Archive mpe records. }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/mpe.arc SELECT * FROM mpe;
//
//
//    { Archive mpe_credit records. }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/mpe_credit.arc
//    SELECT * FROM mpe_credit
//    WHERE exam_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//    { Archive mpe_log records. }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/mpe_log.arc
//    SELECT * FROM mpe_log
//    WHERE start_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//    { Archive mpecr_denied records. }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/mpecr_denied.arc
//    SELECT * FROM mpecr_denied
//    WHERE exam_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//    { Archive msg records. }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/msg.arc
//    SELECT * FROM msg
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//
//    { Archive msg_lookup records. }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/msg_lookup.arc
//    SELECT * FROM msg_lookup;
//
//
//    { Archive campus_calendar records; load NEXT semester pre-edited data }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/campus_cal.arc
//    SELECT * FROM campus_calendar;
//
//    DELETE FROM campus_calendar
//    WHERE campus_dt IS NOT NULL;
//
//    LOAD FROM /usr/informix/a/math/NEXT_data/next_campus_cal
//    INSERT INTO campus_calendar;
//
//
//    { Archive pace_track_rule data }
//    { Must LOAD new data using eos_rollover.4gl b/c SM data does not exist, so }
//    { load file would be empty! }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/pace_rule.arc
//    SELECT * FROM pace_track_rule
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//
//
//    { Archive pacing_rules data. }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/pacing_rules.arc
//    SELECT * FROM pacing_rules
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//    UPDATE pacing_rules SET (term,term_yr) =
//            ((SELECT term    FROM term WHERE active = "X"),
//            (SELECT term_yr FROM term WHERE active = "X"))
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//    LOAD FROM /usr/informix/a/math/ARC_FILES/pacing_rules.arc
//    INSERT INTO pacing_rules;
//
//
//    { Archive pacing_structure data. }
//    UNLOAD TO /usr/informix/a/math/ARC_FILES/pacing_str.arc
//    SELECT * FROM pacing_structure
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//    UPDATE pacing_structure SET (term,term_yr) =
//            ((SELECT term    FROM term WHERE active = "X"),
//            (SELECT term_yr FROM term WHERE active = "X"))
//    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//    LOAD FROM /usr/informix/a/math/ARC_FILES/pacing_str.arc
//    INSERT INTO pacing_structure;
//
//
//    { Archive parameter records to facilitate report generation in term db's }
//        UNLOAD TO /usr/informix/a/math/ARC_FILES/parm.arc
//        SELECT * FROM parameters;
//
//
//        { Archive milestone_appeal records and load into prev_milestone_appeal. }
//        UNLOAD TO /usr/informix/a/math/ARC_FILES/ms_appl.arc
//        SELECT * FROM milestone_appeal;
//
//        LOAD FROM /usr/informix/a/math/ARC_FILES/ms_appl.arc
//        INSERT INTO prev_milestone_appeal;
//
//
//        { Archive pace_appeals records and load into prev_extensions. }
//        UNLOAD TO /usr/informix/a/math/ARC_FILES/pace_appl.arc
//        SELECT * FROM pace_appeals;
//
//        LOAD FROM /usr/informix/a/math/ARC_FILES/pace_appl.arc
//        INSERT INTO prev_extensions;
//
//
//        { Archive plc_fee records. }
//        UNLOAD TO /usr/informix/a/math/ARC_FILES/plc_fee.arc
//        SELECT * FROM plc_fee
//        WHERE bill_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//        { Archive prereq records. }
//        UNLOAD TO /usr/informix/a/math/ARC_FILES/prereq.arc
//        SELECT * FROM prereq;
//
//
//        { Archive remote_mpe data; load NEXT semester pre-edited data }
//        UNLOAD TO /usr/informix/a/math/ARC_FILES/remote.arc
//        SELECT * FROM remote_mpe;
//
//        LOAD FROM /usr/informix/a/math/NEXT_data/next_remote
//        INSERT INTO remote_mpe;
//
//
//        { Archive resource records. }
//        UNLOAD TO /usr/informix/a/math/ARC_FILES/resource.arc
//        SELECT * FROM resource;
//
//
//        { Archive weekly semester_calendar records; load NEXT semester pre-edited data }
//        UNLOAD TO /usr/informix/a/math/ARC_FILES/semester_cal.arc
//        SELECT * FROM semester_calendar;
//
//        DELETE FROM semester_calendar
//        WHERE term IS NOT NULL;
//
//        LOAD FROM /usr/informix/a/math/NEXT_data/next_semester
//        INSERT INTO semester_calendar;
//
//
//        { Archive special_stus records. }
//        UNLOAD TO /usr/informix/a/math/ARC_FILES/special.arc
//        SELECT * FROM special_stus;
//
//
//        { Process stcourse records to delete dropped courses and bogus rows created }
//        { by the User's Survey }
//            DELETE FROM stcourse
//            WHERE sect IN (SELECT bogus_sect FROM bogus_mapping
//                WHERE term = (SELECT term FROM term WHERE active = "Y")
//            AND term_yr = (SELECT term_yr FROM term WHERE active = "Y"))
//            AND term = (SELECT term FROM term WHERE active = "Y")
//            AND term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//            DELETE FROM stcourse
//            WHERE (last_class_roll_dt <
//                    (SELECT last_rec_dt FROM term WHERE active = "Y")
//            OR last_class_roll_dt IS NULL)
//            AND   (open_status = "D" AND open_status IS NOT NULL)
//            AND   final_class_roll = "N";
//
//
//            { Unload rows from incompletes whose deadlines hit during the current term }
//            { NOTE: SP incs finished in SM will be rolled over during FA EOS processing. }
//
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/incomplete.arc
//            SELECT * FROM stcourse
//            WHERE i_deadline_dt <= (SELECT end_dt FROM term WHERE active = "Y")
//            AND   i_deadline_dt >  (SELECT end_dt FROM term WHERE active = "P")
//            AND   i_deadline_dt IS NOT NULL;
//
//
//            { Archive stchallenge records. }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stchallenge.arc
//            SELECT * FROM stchallenge
//            WHERE exam_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//            { Archive sthallengeqa records. }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stchallengeqa.arc
//            SELECT * FROM stchallengeqa;
//
//
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stcourse.arc SELECT * FROM stcourse
//            WHERE term           = (SELECT term    FROM term WHERE active = "Y")
//            AND   term_yr        = (SELECT term_yr FROM term WHERE active = "Y")
//            AND   i_in_progress != "Y";
//
//
//            { Archive stcuobjective rows, UNLOAD rows for students with an incomplete, }
//            { delete all stcuobjective rows, then reLOAD for students with an incomplete }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stcuobj.arc SELECT *
//                FROM stcuobjective;
//
//            SELECT stcourse.stu_id,stcourse.course FROM stcourse
//            WHERE (stcourse.course_grade = "I"
//                    OR stcourse.i_in_progress = "Y")
//            AND    stcourse.term    = (SELECT term    FROM term WHERE active = "Y")
//            AND    stcourse.term_yr = (SELECT term_yr FROM term WHERE active = "Y")
//            INTO TEMP inc_stcuobjs;
//
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stcuobjective.incs
//            SELECT
//            stcuobjective.stu_id,
//                    stcuobjective.course,
//                    stcuobjective.unit,
//                    stcuobjective.objective,
//                    stcuobjective.lecture_viewed_dt,
//                    stcuobjective.seed,
//                    stcuobjective.last_component_finished FROM stcuobjective,inc_stcuobjs
//            WHERE stcuobjective.stu_id = inc_stcuobjs.stu_id
//            AND   stcuobjective.course = inc_stcuobjs.course;
//
//            { clear all data from stcuobjective before reloading rows from incompletes }
//            DELETE FROM stcuobjective
//            WHERE stu_id IS NOT NULL;
//
//            DROP TABLE inc_stcuobjs;
//
//            LOAD FROM /usr/informix/a/math/ARC_FILES/stcuobjective.incs insert into stcuobjective;
//
//
//            { Archive stexam records. }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stexam.arc
//            SELECT * FROM stexam
//            WHERE exam_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//            { Archive stetext records. }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stetext.arc
//            SELECT * FROM stetext
//            WHERE active_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//            { Archive sthomework records. }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/sthw.arc
//            SELECT * FROM sthomework
//            WHERE hw_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//            { Archive stmdscores records. }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stmdscores.arc
//            SELECT * FROM stmdscores;
//
//
//            { Archive stmilestone records and load into prev_stmilestone. }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stmile.arc
//            SELECT * FROM stmilestone;
//
//            LOAD FROM /usr/informix/a/math/ARC_FILES/stmile.arc
//            INSERT INTO prev_stmilestone;
//
//
//            { Archive stmpe records. }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stmpe.arc
//            SELECT * FROM stmpe
//            WHERE exam_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//            { Archive stmpeqa records. }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stmpeqa.arc
//            SELECT * FROM stmpeqa;
//
//
//            { Archive stmsg records. }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stmsg.arc
//            SELECT * FROM stmsg
//            WHERE msg_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//            { Archive stpace_summary records. }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stpace_summ.arc
//            SELECT * FROM stpace_summary;
//
//
//            { Archive stresource records. }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stresource.arc
//            SELECT * FROM stresource
//            WHERE loan_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//
//            { Archive surveyqa records. }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/surveyqa.arc SELECT * FROM surveyqa;
//
//
//            { Archive stsurveyqa records. }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/stsurvqa.arc
//            SELECT * FROM stsurveyqa;
//
//
//            { Archive sthwqa rows, UNLOAD rows for students with an incomplete, }
//            { delete all sthwqa rows, then reLOAD for students with an incomplete }
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/sthwqa.arc SELECT * FROM sthwqa;
//
//            SELECT stcourse.stu_id,sthomework.serial_nbr,sthomework.version
//            FROM stcourse,sthomework
//            WHERE (stcourse.course_grade = "I"
//                    OR stcourse.i_in_progress = "Y")
//            AND    stcourse.stu_id  = sthomework.stu_id
//            AND    stcourse.course  = sthomework.course
//            AND    stcourse.term    = (SELECT term    FROM term WHERE active = "Y")
//            AND    stcourse.term_yr = (SELECT term_yr FROM term WHERE active = "Y")
//            INTO TEMP inc_hws;
//
//            UNLOAD TO /usr/informix/a/math/ARC_FILES/sthwqa.incs
//            SELECT
//            sthwqa.serial_nbr,
//                    sthwqa.question_nbr,
//                    sthwqa.answer_nbr,
//                    sthwqa.objective,
//                    sthwqa.stu_answer,
//                    inc_hws.stu_id,
//                    sthwqa.version,
//                    sthwqa.ans_correct,
//                    sthwqa.hw_dt,
//                    sthwqa.finish_time FROM sthwqa,inc_hws
//            WHERE sthwqa.stu_id     = inc_hws.stu_id
//            AND   sthwqa.serial_nbr = inc_hws.serial_nbr
//            AND   sthwqa.version    = inc_hws.version;
//
//            DROP TABLE sthwqa;
//            { NOTE: this requires that PERMS.SQL be run to reset permissions. }
//
//            CREATE TABLE "math".sthwqa
//                (
//                        serial_nbr integer not null ,
//                question_nbr smallint not null ,
//                answer_nbr smallint not null ,
//                objective char(6) not null ,
//                stu_answer varchar(100) not null ,
//                stu_id char(9) not null ,
//                version char(5) not null ,
//                ans_correct char(1) not null ,
//                hw_dt date not null ,
//                finish_time integer
//  )  extent size 32768 next size 16384 lock mode row;
//            revoke all on "math".sthwqa from "public";
//
//            DROP TABLE inc_hws;
//
//            LOAD FROM /usr/informix/a/math/ARC_FILES/sthwqa.incs insert into sthwqa;
//            CREATE INDEX "math".i_sthwqa on "math".sthwqa (stu_id);
//            CREATE INDEX "math".i2_sthwqa on "math".sthwqa (serial_nbr,question_nbr,answer_nbr);
//
//
//            { Archive stqa rows (including User's Exam), UNLOAD rows for students with }
//                { an incomplete or non-passing ELM Exam (for online display of MPE results -- }
//                { only use as of SM06), UNLOAD rows for students who are still working on the }
//                { ELM Tutorial for the upcoming term, delete all stqa rows, then reLOAD for }
//                { students with an incomplete, ELM Exam or active ELM Tutorial }
//                UNLOAD TO /usr/informix/a/math/ARC_FILES/stqa.arc SELECT * FROM stqa
//                WHERE exam_dt >= (SELECT start_dt FROM term WHERE active = "Y");
//
//                SELECT stcourse.stu_id,stexam.serial_nbr,stexam.version FROM stcourse,stexam
//                WHERE (stcourse.course_grade = "I"
//                        OR stcourse.i_in_progress = "Y")
//                AND    stcourse.stu_id  = stexam.stu_id
//                AND    stcourse.course  = stexam.course
//                AND    stcourse.term    = (SELECT term    FROM term WHERE active = "Y")
//                AND    stcourse.term_yr = (SELECT term_yr FROM term WHERE active = "Y")
//                INTO TEMP inc_exams;
//
//                UNLOAD TO /usr/informix/a/math/ARC_FILES/stqa.incs
//                SELECT stqa.* FROM stqa,inc_exams
//                WHERE stqa.stu_id     = inc_exams.stu_id
//                AND   stqa.serial_nbr = inc_exams.serial_nbr
//                AND   stqa.version    = inc_exams.version;
//
//                UNLOAD TO /usr/informix/a/math/ARC_FILES/stqa.elms
//                SELECT stqa.* FROM stexam,stqa
//                WHERE stqa.stu_id     = stexam.stu_id
//                AND   stqa.serial_nbr = stexam.serial_nbr
//                AND   stqa.version    = stexam.version
//                AND   stexam.course = "M 100E"
//                AND   stexam.exam_score < 21
//                AND   stexam.stu_id NOT IN (SELECT DISTINCT stu_id FROM mpe_credit
//                    WHERE course = "M 100C")
//                AND   stexam.exam_dt >= (SELECT start_dt FROM term
//                WHERE term = (SELECT term FROM term WHERE active = "Y")
//                AND (term_yr = ((SELECT term_yr FROM term WHERE active = "Y") - 5)
//                OR term_yr = ((SELECT term_yr FROM term WHERE active = "Y") + 95)));
//
//                { Keep stqa rows for students who have worked on the ELM Tutorial but have}
//                { never placed into MATH 117 -- up to 4 years; this logic is compatible with }
//                { eos_roll.4gl }
//                UNLOAD TO /usr/informix/a/math/ARC_FILES/stqa.tuts
//                SELECT stqa.* FROM stexam,stqa
//                WHERE stqa.stu_id     = stexam.stu_id
//                AND   stqa.serial_nbr = stexam.serial_nbr
//                AND   stqa.version    = stexam.version
//                AND   stexam.course = "M 100T"
//                AND   stexam.exam_dt >= (SELECT start_dt FROM term
//                WHERE term = (SELECT term FROM term WHERE active = "Y")
//                AND (term_yr = ((SELECT term_yr FROM term WHERE active = "Y") - 4)
//                OR term_yr = ((SELECT term_yr FROM term WHERE active = "Y") + 96)))
//                AND   stexam.stu_id NOT IN (SELECT DISTINCT stu_id FROM mpe_credit
//                    WHERE course = "M 100C");
//
//                DROP TABLE stqa;
//                { NOTE: this requires that PERMS.SQL be run to reset permissions. }
//
//                CREATE TABLE stqa
//                    (
//                            serial_nbr   INTEGER not null,
//                    question_nbr SMALLINT not null,
//                    answer_nbr   SMALLINT,
//                    objective    CHAR(10),
//                    stu_answer   VARCHAR(100),
//                    stu_id       CHAR(9) NOT NULL,
//                version      CHAR(5) NOT NULL,
//                ans_correct  CHAR(1),
//                    exam_dt      DATE,
//                    subtest      CHAR(1),
//                    finish_time  INTEGER
//  ) extent size 65536 next size 32768 lock mode row;
//
//                DROP TABLE inc_exams;
//
//                LOAD FROM /usr/informix/a/math/ARC_FILES/stqa.incs insert into stqa;
//                LOAD FROM /usr/informix/a/math/ARC_FILES/stqa.elms insert into stqa;
//                LOAD FROM /usr/informix/a/math/ARC_FILES/stqa.tuts insert into stqa;
//                CREATE INDEX "math".i_stqa on "math".stqa (stu_id);
//                CREATE INDEX "math".i2_stqa on "math".stqa (serial_nbr,question_nbr,answer_nbr);
//
//
//                { Archive stterm records and load into prev_stterm. }
//                UNLOAD TO /usr/informix/a/math/ARC_FILES/stterm.arc
//                SELECT * FROM stterm
//                WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//                AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//                LOAD FROM /usr/informix/a/math/ARC_FILES/stterm.arc
//                INSERT INTO prev_stterm;
//
//
//                { Archive student records }
//                UNLOAD TO /usr/informix/a/math/ARC_FILES/student.arc
//                SELECT distinct student.* FROM student
//                WHERE create_dt >= (SELECT start_dt FROM term WHERE active = "Y")
//                OR stu_id IN (SELECT distinct stu_id FROM admin_hold)
//                OR stu_id IN (SELECT distinct stu_id FROM calcs)
//                OR stu_id IN (SELECT distinct stu_id FROM challenge_fee
//                    WHERE bill_dt >= (SELECT start_dt FROM term WHERE active = "Y"))
//                OR stu_id IN (SELECT distinct stu_id FROM discipline
//                    WHERE dt_incident >= (SELECT start_dt FROM term WHERE active = "Y"))
//                OR stu_id IN (SELECT distinct stu_id FROM except_stu)
//                OR stu_id IN (SELECT distinct stu_id FROM ffr_trns
//                    WHERE exam_dt >= (SELECT start_dt FROM term WHERE active = "Y"))
//                OR stu_id IN (SELECT distinct stu_id FROM mdstudent
//                    WHERE create_dt >= (SELECT start_dt FROM term WHERE active = "Y"))
//                OR stu_id IN (SELECT distinct stu_id FROM mpe_credit
//                    WHERE exam_dt >= (SELECT start_dt FROM term WHERE active = "Y"))
//                OR stu_id IN (SELECT distinct stu_id FROM mpecr_denied
//                    WHERE exam_dt >= (SELECT start_dt FROM term WHERE active = "Y"))
//                OR stu_id IN (SELECT distinct stu_id FROM milestone_appeal)
//                OR stu_id IN (SELECT distinct stu_id FROM pace_appeals)
//                OR stu_id IN (SELECT distinct stu_id FROM plc_fee
//                    WHERE bill_dt >= (SELECT start_dt FROM term WHERE active = "Y"))
//                OR stu_id IN (SELECT distinct stu_id FROM special_stus)
//                OR stu_id IN (SELECT distinct stu_id FROM stchallenge
//                    WHERE exam_dt >= (SELECT start_dt FROM term WHERE active = "Y"))
//                OR stu_id in (SELECT distinct stu_id FROM stcourse
//                    WHERE term = (SELECT term FROM term WHERE active = "Y")
//                AND term_yr = (SELECT term_yr FROM term WHERE active = "Y"))
//                OR stu_id IN (SELECT distinct stu_id FROM stetext
//                    WHERE active_dt >= (SELECT start_dt FROM term WHERE active = "Y"))
//                OR stu_id IN (SELECT distinct stu_id FROM stexam
//                    WHERE exam_dt >= (SELECT start_dt FROM term WHERE active = "Y"))
//                OR stu_id IN (SELECT distinct stu_id FROM sthomework
//                    WHERE hw_dt >= (SELECT start_dt FROM term WHERE active = "Y"))
//                OR stu_id IN (SELECT distinct stu_id FROM stmpe
//                    WHERE exam_dt >= (SELECT start_dt FROM term WHERE active = "Y"))
//                OR stu_id IN (SELECT distinct stu_id FROM stresource
//                    WHERE loan_dt >= (SELECT start_dt FROM term WHERE active = "Y"))
//                OR stu_id IN (SELECT distinct stu_id FROM stsurveyqa
//                    WHERE exam_dt >= (SELECT start_dt FROM term WHERE active = "Y"))
//                OR stu_id IN (SELECT distinct stu_id FROM users);
//
//
//                { Archive term record. }
//                UNLOAD TO /usr/informix/a/math/ARC_FILES/term.arc
//                SELECT * FROM term WHERE active = "Y";
//
//
//                { Archive testing_center data. }
//                UNLOAD TO /usr/informix/a/math/ARC_FILES/tc.arc SELECT * FROM testing_centers;
//
//
//                { Archive users records. }
//                UNLOAD TO /usr/informix/a/math/ARC_FILES/users.arc SELECT * FROM users;
//
//
//                { Archive user_clearance records. }
//                UNLOAD TO /usr/informix/a/math/ARC_FILES/user_clear.arc
//                SELECT * FROM user_clearance;
//
//
//                { Archive csection data; delete bogus sections; delete rows from 8 academic
//                    years ago to keep compatible with stcourse; load NEXT semester pre-edited
//                    data; update next term's instructor if not BENOIT }
//
//                    UNLOAD TO /usr/informix/a/math/ARC_FILES/csection.arc SELECT * FROM csection
//                    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//                    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//                    { Update future term row for ELM/PreCalc Tutorial because "next_csection" data
//                        includes this -- program will bomb if try to update/create a duplicate row }
//                    UPDATE csection SET (term,term_yr) =
//                        ((SELECT term    FROM term WHERE active = "2"),
//                    (SELECT term_yr FROM term WHERE active = "2"))
//                    WHERE term    = (SELECT term    FROM term WHERE active = "X")
//                    AND   term_yr = (SELECT term_yr FROM term WHERE active = "X")
//                    AND   course IN ("M 100T","M 100E","M 170E","M 170T");
//
//                    { Delete future term row for PreCalc Tutorial because "next_csection" data
//                        includes this -- program will bomb if try to update/create a duplicate row }
//                    DELETE FROM csection
//                    WHERE term    = (SELECT term    FROM term WHERE active = "X")
//                    AND   term_yr = (SELECT term_yr FROM term WHERE active = "X")
//                    AND course IN ("M 1170","M 1180","M 1240","M 1250","M 1260");
//
//                    DELETE FROM csection WHERE bogus = "Y"
//                    AND term    = (SELECT term    FROM term WHERE active = "Y")
//                    AND term_yr = (SELECT term_yr FROM term WHERE active = "Y")
//                    AND course NOT MATCHES "M 100*"
//                    AND course NOT IN ("M 1170","M 1180","M 1240","M 1250","M 1260");
//
//                    DELETE FROM csection
//                    WHERE term    =  (SELECT term    FROM term WHERE active = "Y")
//                    AND   term_yr = ((SELECT term_yr FROM term WHERE active = "Y") - 8);
//
//                    LOAD FROM /usr/informix/a/math/NEXT_data/next_csection
//                    INSERT INTO csection;
//
//                    UPDATE csection SET instructor = "BENOIT"
//                    WHERE term    = (SELECT term    FROM term WHERE active = "X")
//                    AND   term_yr = (SELECT term_yr FROM term WHERE active = "X")
//                    AND   instructor != "BENOIT";
//
//                    { Archive csection_pace_track records. }
//
//                    UNLOAD TO /usr/informix/a/math/ARC_FILES/csection_pace_track.arc SELECT * FROM csection_pace_track
//                    WHERE term    = (SELECT term    FROM term WHERE active = "Y")
//                    AND   term_yr = (SELECT term_yr FROM term WHERE active = "Y");
//
//                    DELETE FROM csection_pace_track
//                    WHERE term    =  (SELECT term    FROM term WHERE active = "Y")
//                    AND   term_yr = ((SELECT term_yr FROM term WHERE active = "Y") - 8);


package dev.mathops.app.database.eos;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCalcs;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawChallengeFee;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.db.old.rawrecord.RawCohort;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawCunit;
import dev.mathops.db.old.rawrecord.RawCuobjective;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawDiscipline;
import dev.mathops.db.old.rawrecord.RawDontSubmit;
import dev.mathops.db.old.rawrecord.RawEtext;
import dev.mathops.db.old.rawrecord.RawEtextCourse;
import dev.mathops.db.old.rawrecord.RawEtextKey;
import dev.mathops.db.old.rawrecord.RawExam;
import dev.mathops.db.old.rawrecord.RawExamQa;
import dev.mathops.db.old.rawrecord.RawExceptStu;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawGradeRoll;
import dev.mathops.db.old.rawrecord.RawGradingStd;
import dev.mathops.db.old.rawrecord.RawHighSchools;
import dev.mathops.db.old.rawrecord.RawHoldType;
import dev.mathops.db.old.rawrecord.RawLogins;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawMilestoneAppeal;
import dev.mathops.db.old.rawrecord.RawMpe;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawMpeLog;
import dev.mathops.db.old.rawrecord.RawMpecrDenied;
import dev.mathops.db.old.rawrecord.RawMsg;
import dev.mathops.db.old.rawrecord.RawMsgLookup;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;
import dev.mathops.db.old.rawrecord.RawPaceTrackRule;
import dev.mathops.db.old.rawrecord.RawPacingRules;
import dev.mathops.db.old.rawrecord.RawPacingStructure;
import dev.mathops.db.old.rawrecord.RawParameters;
import dev.mathops.db.old.rawrecord.RawPlcFee;
import dev.mathops.db.old.rawrecord.RawPrereq;
import dev.mathops.db.old.rawrecord.RawRemoteMpe;
import dev.mathops.db.old.rawrecord.RawResource;
import dev.mathops.db.old.rawrecord.RawSemesterCalendar;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStchallengeqa;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStcunit;
import dev.mathops.db.old.rawrecord.RawStcuobjective;
import dev.mathops.db.old.rawrecord.RawStetext;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawSthwqa;
import dev.mathops.db.old.rawrecord.RawStmathplan;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStmpeqa;
import dev.mathops.db.old.rawrecord.RawStmsg;
import dev.mathops.db.old.rawrecord.RawStpaceSummary;
import dev.mathops.db.old.rawrecord.RawStqa;
import dev.mathops.db.old.rawrecord.RawStresource;
import dev.mathops.db.old.rawrecord.RawStsurveyqa;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rawrecord.RawStvisit;
import dev.mathops.db.old.rawrecord.RawSurveyqa;
import dev.mathops.db.old.rawrecord.RawTestingCenter;
import dev.mathops.db.old.rawrecord.RawUserClearance;
import dev.mathops.db.old.rawrecord.RawUsers;
import dev.mathops.db.old.rec.AssignmentRec;
import dev.mathops.db.old.rec.MasteryAttemptQaRec;
import dev.mathops.db.old.rec.MasteryAttemptRec;
import dev.mathops.db.old.rec.MasteryExamRec;
import dev.mathops.db.old.rec.ReportPermsRec;
import dev.mathops.db.old.rec.StandardMilestoneRec;
import dev.mathops.db.old.rec.StudentCourseMasteryRec;
import dev.mathops.db.old.rec.StudentStandardMilestoneRec;
import dev.mathops.db.old.rec.StudentUnitMasteryRec;
import dev.mathops.db.old.svc.term.TermRec;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * STEP 500: Creating the archive tables.
 *
 * <p>
 * Logic to create all needed tables in the archive database.  The archive database must have been created, and logging
 * must be turned on ("ontape -s -U "term****") and we must be able to connect to it and pass a connection object into
 * this class's methods.
 */
final class S500CreateArchiveTables extends StepExecutable {

    /** The data cache for the archive database. */
    private final Cache archiveCache;

    /** The panel to update with status. */
    private final JProgressBar progress;

    /**
     * Constructs a new {@code S500CreateArchiveTables}.  This should be called on the AWT event dispatch thread.
     *
     * @param theOwner        the step list that will hold the step
     * @param statusDisplay   the status display
     * @param theArchiveCache the data cache for the archive database
     */
    S500CreateArchiveTables(final StepList theOwner, final StepDisplay statusDisplay, final Cache theArchiveCache) {

        super(theOwner, 500, "Create tables in the term archive database", null, statusDisplay);

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
    static class S500Worker extends SwingWorker<Boolean, StepStatus> {

        /** TRUE to simply print what actions would be taken, FALSE to actually take actions. */
        private static final boolean DEBUG = true;

        /** The owning step. */
        private final S500CreateArchiveTables owner;

        /**
         * Constructs a new {@code S500Worker}.
         */
        S500Worker(final S500CreateArchiveTables theOwner) {

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

            firePublish(0, "Checking validity of the archive database...");

            if (isDatabaseCorrect()) {
                firePublish(1, "Archive database is valid.");

                if (wasAdminHoldCreated()
                    && wasCalcsCreated()
                    && wasCampusCalendarCreated()
                    && wasChallengeFeeCreated()
                    && wasClientPcCreated()
                    && wasCohortCreated()
                    && wasCourseCreated()
                    && wasCsectionCreated()
                    && wasCunitCreated()
                    && wasCuobjectiveCreated()
                    && wasCusectionCreated()
                    && wasDisciplineCreated()
                    && wasDontSubmitCreated()
                    && wasETextCreated()
                    && wasETextCourseCreated()
                    && wasETextKeyCreated()
                    && wasExamCreated()
                    && wasExamQACreated()
                    && wasExceptStuCreated()
                    && wasFfrTrnsCreated()
                    && wasGradeRollCreated()
                    && wasGradingStdCreated()
                    && wasHighSchoolsCreated()
                    && wasHoldTypeCreated()
                    && wasHomeworkCreated()
                    && wasIndexDescriptionsCreated()
                    && wasIndexFrequencyCreated()
                    && wasLoginsCreated()
                    && wasMasteryAttemptCreated()
                    && wasMasteryAttemptQaCreated()
                    && wasMasteryExamCreated()
                    && wasMilestoneCreated()
                    && wasMilestoneAppealCreated()
                    && wasMpeCreated()
                    && wasMpeCreditCreated()
                    && wasMpeLogCreated()
                    && wasMpecrDeniedCreated()
                    && wasMsgCreated()
                    && wasMsgLookupCreated()
                    && wasPaceAppealsCreated()
                    && wasPaceTrackRuleCreated()
                    && wasPacingRulesCreated()
                    && wasPacingStructureCreated()
                    && wasParametersCreated()
                    && wasPlcFeeCreated()
                    && wasPrereqCreated()
                    && wasRemoteMpeCreated()
                    && wasReportPermsCreated()
                    && wasResourceCreated()
                    && wasSemesterCalendarCreated()
                    && wasSpecialStusCreated()
                    && wasStChallengeCreated()
                    && wasStChallengeQaCreated()
                    && wasStCourseCreated()
                    && wasStCunitCreated()
                    && wasStCuobjectiveCreated()
                    && wasStdMilestoneCreated()
                    && wasStETextCreated()
                    && wasStExamCreated()
                    && wasStHomeworkCreated()
                    && wasStHwQaCreated()
                    && wasStMathPlanCreated()
                    && wasStMilestoneCreated()
                    && wasStMpeCreated()
                    && wasStMpeQaCreated()
                    && wasStMsgCreated()
                    && wasStPaceSummaryCreated()
                    && wasStQaCreated()
                    && wasStResourceCreated()
                    && wasStSurveyQaCreated()
                    && wasStTermCreated()
                    && wasStuCourseMasteryCreated()
                    && wasStuStdMilestoneCreated()
                    && wasStuUnitMasteryCreated()
                    && wasStudentCreated()
                    && wasStVisitCreated()
                    && wasSurveyQaCreated()
                    && wasTermCreated()
                    && wasTestingCentersCreated()
                    && wasUserClearanceCreated()
                    && wasUsersCreated()) {

                    try {
                        this.owner.archiveCache.conn.commit();
                        firePublish(100, "Archive database tables created.");
                    } catch (final SQLException ex) {
                        Log.warning("Failed to commit table creations", ex);
                        result = Boolean.FALSE;
                    }
                } else {
                    result = Boolean.FALSE;
                }
            }

            return result;
        }

        /**
         * Verifies that none of the expected tables exist already.
         *
         * @return true if databases are verified; false if not
         */
        private boolean isDatabaseCorrect() {

            final String sql = "SELECT COUNT(*) FROM systables WHERE tabname=?";

            final String[] tables = {RawAdminHold.TABLE_NAME, RawCalcs.TABLE_NAME, RawCampusCalendar.TABLE_NAME,
                    RawChallengeFee.TABLE_NAME, RawClientPc.TABLE_NAME, RawCohort.TABLE_NAME, RawCourse.TABLE_NAME,
                    RawCsection.TABLE_NAME, RawCunit.TABLE_NAME, RawCuobjective.TABLE_NAME, RawCusection.TABLE_NAME,
                    RawDiscipline.TABLE_NAME, RawDontSubmit.TABLE_NAME, RawEtext.TABLE_NAME, RawEtextCourse.TABLE_NAME,
                    RawEtextKey.TABLE_NAME, RawExam.TABLE_NAME, RawExamQa.TABLE_NAME, RawExceptStu.TABLE_NAME,
                    RawFfrTrns.TABLE_NAME, RawGradeRoll.TABLE_NAME, RawGradingStd.TABLE_NAME, RawHighSchools.TABLE_NAME,
                    RawHoldType.TABLE_NAME, AssignmentRec.TABLE_NAME, "index_descriptions", "index_frequency",
                    RawLogins.TABLE_NAME, MasteryAttemptRec.TABLE_NAME, MasteryAttemptQaRec.TABLE_NAME,
                    MasteryExamRec.TABLE_NAME, RawMilestone.TABLE_NAME, RawMilestoneAppeal.TABLE_NAME,
                    RawMpe.TABLE_NAME,
                    RawMpeCredit.TABLE_NAME, RawMpeLog.TABLE_NAME, RawMpecrDenied.TABLE_NAME, RawMsg.TABLE_NAME,
                    RawMsgLookup.TABLE_NAME, RawPaceAppeals.TABLE_NAME, RawPaceTrackRule.TABLE_NAME,
                    RawPacingRules.TABLE_NAME, RawPacingStructure.TABLE_NAME, RawParameters.TABLE_NAME,
                    RawPlcFee.TABLE_NAME, RawPrereq.TABLE_NAME, RawRemoteMpe.TABLE_NAME, ReportPermsRec.TABLE_NAME,
                    RawResource.TABLE_NAME, RawSemesterCalendar.TABLE_NAME, RawSpecialStus.TABLE_NAME,
                    RawStchallenge.TABLE_NAME, RawStchallengeqa.TABLE_NAME, RawStcourse.TABLE_NAME,
                    RawStcunit.TABLE_NAME,
                    RawStcuobjective.TABLE_NAME, StandardMilestoneRec.TABLE_NAME, RawStetext.TABLE_NAME,
                    RawStexam.TABLE_NAME, RawSthomework.TABLE_NAME, RawSthwqa.TABLE_NAME, RawStmathplan.TABLE_NAME,
                    RawStmilestone.TABLE_NAME, RawStmpe.TABLE_NAME, RawStmpeqa.TABLE_NAME, RawStmsg.TABLE_NAME,
                    RawStpaceSummary.TABLE_NAME, RawStqa.TABLE_NAME, RawStresource.TABLE_NAME, RawStsurveyqa.TABLE_NAME,
                    RawStterm.TABLE_NAME, StudentCourseMasteryRec.TABLE_NAME, StudentStandardMilestoneRec.TABLE_NAME,
                    StudentUnitMasteryRec.TABLE_NAME, RawStudent.TABLE_NAME, RawStvisit.TABLE_NAME,
                    RawSurveyqa.TABLE_NAME,
                    TermRec.TABLE_NAME, RawTestingCenter.TABLE_NAME, RawUserClearance.TABLE_NAME, RawUsers.TABLE_NAME};

            boolean ok = true;

            try (final PreparedStatement pstmt = this.owner.archiveCache.conn.prepareStatement(sql)) {

                for (final String table : tables) {
                    pstmt.setString(1, table);

                    try (final ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            final int count = rs.getInt(1);
                            if (count > 0) {
                                Log.warning(table, " table already exists in archive database");
                                ok = false;
                                break;
                            }
                        } else {
                            Log.warning("Unable to query 'systables' for existence of ", table);
                            ok = false;
                            break;
                        }
                    }
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to query 'systables' table in archive database", ex);
                ok = false;
            }

            return ok;
        }

        /**
         * Creates the "admin_hold" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasAdminHoldCreated() {

            boolean ok = true;

            firePublish(2, "Creating 'admin_hold' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.admin_hold (
                          stu_id          char(9)  not null,
                          hold_id         char(2),
                          sev_admin_hold  char(1),
                          times_display   integer,
                          create_dt       date);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawAdminHold.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'admin_hold' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "calcs" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasCalcsCreated() {

            boolean ok = true;

            firePublish(3, "Creating 'calcs' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.calcs (
                          stu_id      char(9)  not null,
                          issued_nbr  char(7)  not null,
                          return_nbr  char(7)  not null,
                          serial_nbr  integer  not null,
                          exam_dt     date     not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawCalcs.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'calcs' table in archive database");
                    ok = false;
                }
            }

            // revoke all on "math".pace_track_rule from "public" as "math";

            return ok;
        }

        /**
         * Creates the "campus_calendar" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasCampusCalendarCreated() {

            boolean ok = true;

            firePublish(4, "Creating 'campus_calendar' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.campus_calendar (
                          campus_dt    date      not null,
                          dt_desc      char(20)  not null,
                          open_time1   char(10),
                          open_time2   char(10),
                          close_time1  char(10),
                          close_time2  char(10),
                          weekdays_1   char(20),
                          weekdays_2   char(20));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawCampusCalendar.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'campus_calendar' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "challenge_fee" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasChallengeFeeCreated() {

            boolean ok = true;

            firePublish(5, "Creating 'challenge_fee' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.challenge_fee (
                          stu_id   char(9)   not null,
                          course   char(10)  not null,
                          exam_dt  date      not null,
                          bill_dt  date      not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawChallengeFee.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'challenge_fee' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "client_pc" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasClientPcCreated() {

            boolean ok = true;

            firePublish(6, "Creating 'client_pc' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.client_pc (
                          computer_id        char(40)                 not null,
                          testing_center_id  char(14)                 not null,
                          station_nbr        char(10),
                          computer_desc      varchar(80),
                          icon_x             smallint,
                          icon_y             smallint,
                          pc_usage           char(1)                  not null,
                          current_status     smallint                 not null,
                          dtime_created      datetime year to second  not null,
                          dtime_approved     datetime year to second,
                          mac_address        char(12),
                          power_status       char(1),
                          power_on_due       integer,
                          last_ping          integer,
                          current_stu_id     char(9),
                          current_course     char(10),
                          current_unit       smallint,
                          current_version    char(5));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawClientPc.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'client_pc' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "cohort" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasCohortCreated() {

            boolean ok = true;

            firePublish(8, "Creating 'cohort' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.cohort (
                          cohort      char(8)   not null,
                          size        smallint  not null,
                          instructor  char(30));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawCohort.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'cohort' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "course" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasCourseCreated() {

            boolean ok = true;

            firePublish(9, "Creating 'course' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.course (
                          course         char(10)  not null,
                          nbr_units      smallint  not null,
                          course_name    char(50),
                          nbr_credits    smallint  not null,
                          calc_ok        char(1),
                          course_label   char(40)  not null,
                          inline_prefix  char(20),
                          is_tutorial    char(1)   not null,
                          require_etext  char(1)   not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawCourse.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'course' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "csection" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasCsectionCreated() {

            boolean ok = true;

            firePublish(11, "Creating 'csection' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.csection (
                          course                char(10)  not null,
                          sect                  char(4)   not null,
                          term                  char(2)   not null,
                          term_yr               smallint  not null,
                          section_id            char(6),
                          aries_start_dt        date,
                          aries_end_dt          date,
                          start_dt              date,
                          exam_delete_dt        date,
                          instrn_type           char(2),
                          instructor            char(30),
                          campus                char(2)   not null,
                          pacing_structure      char(1),
                          mtg_days              char(5),
                          classroom_id          char(14),
                          lst_stcrs_creat_dt    date,
                          grading_std           char(3),
                          a_min_score           smallint,
                          b_min_score           smallint,
                          c_min_score           smallint,
                          d_min_score           smallint,
                          survey_id             char(5),
                          course_label_shown    char(1),
                          display_score         char(1),
                          display_grade_scale   char(1),
                          count_in_max_courses  char(1),
                          online                char(1)   not null,
                          bogus                 char(1)   not null,
                          canvas_id             char(40),
                          subterm               char(4))""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawCsection.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'csection' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "cunit" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasCunitCreated() {

            boolean ok = true;

            firePublish(12, "Creating 'cunit' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.cunit (
                          course          char(10)  not null,
                          unit            smallint  not null,
                          term            char(2)   not null,
                          term_yr         smallint  not null,
                          unit_exam_wgt   decimal(3,2),
                          unit_desc       char(50),
                          unit_timelimit  smallint,
                          possible_score  smallint,
                          nbr_questions   smallint,
                          unit_type       char(4));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawCunit.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'cunit' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "cuobjective" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasCuobjectiveCreated() {

            boolean ok = true;

            firePublish(14, "Creating 'cuobjective' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.cuobjective (
                          course      char(10)  not null,
                          unit        smallint  not null,
                          term        char(2)   not null,
                          term_yr     smallint  not null,
                          objective   smallint  not null,
                          lesson_id   char(40),
                          lesson_nbr  char(10),
                          start_dt    date);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawCuobjective.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'cuobjective' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "cusection" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasCusectionCreated() {

            boolean ok = true;

            firePublish(15, "Creating 'cusection' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.cusection (
                          course             char(10)  not null,
                          sect               char(4)   not null,
                          unit               smallint  not null,
                          term               char(2)   not null,
                          term_yr            smallint  not null,
                          timeout            smallint  not null,
                          re_mastery_score   smallint,
                          ue_mastery_score   smallint,
                          hw_mastery_score   smallint  not null,
                          hw_moveon_score    smallint  not null,
                          nbr_atmpts_allow   smallint  not null,
                          atmpts_per_review  smallint  not null,
                          first_test_dt      date      not null,
                          last_test_dt       date      not null,
                          begin_test_period  integer   not null,
                          end_test_period    integer   not null,
                          coupon_cost        smallint,
                          last_coupon_dt     date,
                          show_test_window   char(1),
                          unproctored_exam   char(1),
                          re_points_ontime   smallint);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawCusection.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'cusection' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "discipline" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasDisciplineCreated() {

            boolean ok = true;

            firePublish(16, "Creating 'discipline' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.discipline (
                          stu_id          char(9)   not null,
                          dt_incident     date      not null,
                          incident_type   char(2)   not null,
                          course          char(10)  not null,
                          unit            smallint  not null,
                          cheat_desc      char(100),
                          action_type     char(2),
                          action_comment  char(100),
                          interviewer     char(20),
                          proctor         char(20));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawDiscipline.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'discipline' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "dont_submit" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasDontSubmitCreated() {

            boolean ok = true;

            firePublish(17, "Creating 'dont_submit' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.dont_submit (
                          course   char(10)  not null,
                          sect     char(4)   not null,
                          term     char(2)   not null,
                          term_yr  smallint  not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawDontSubmit.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'dont_submit' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "etext" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasETextCreated() {

            boolean ok = true;

            firePublish(18, "Creating 'etext' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.etext (
                          etext_id       char(6)  not null,
                          retention      char(1)  not null,
                          purchase_url   varchar(140),
                          refund_period  smallint,
                          key_entry      char(1)  not null,
                          active         char(1)  not null,
                          button_label   char(80));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawEtext.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'etext' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "etext_course" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasETextCourseCreated() {

            boolean ok = true;

            firePublish(20, "Creating 'etext_course' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.etext_course (
                          etext_id  char(6)   not null,
                          course    char(10)  not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawEtextCourse.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'etext_course' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "etext_key" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasETextKeyCreated() {

            boolean ok = true;

            firePublish(21, "Creating 'etext_key' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.etext_key (
                          etext_id   char(6)   not null,
                          etext_key  char(20)  not null,
                          active_dt  datetime year to second);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawEtextKey.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'etext_key' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "exam" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasExamCreated() {

            boolean ok = true;

            firePublish(22, "Creating 'exam' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.exam (
                          version       char(5)   not null,
                          course        char(10)  not null,
                          unit          smallint  not null,
                          vsn_explt     char(7),
                          title         char(30),
                          tree_ref      char(40),
                          exam_type     char(2)   not null,
                          active_dt     date      not null,
                          pull_dt       date,
                          button_label  char(50));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawExam.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'exam' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "examqa" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasExamQACreated() {

            boolean ok = true;

            firePublish(23, "Creating 'examqa' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.examqa (
                          version           char(5)   not null,
                          problem_nbr       smallint  not null,
                          exam_section_nbr  smallint,
                          question_nbr      smallint  not null,
                          correct_answer    char(5)   not null,
                          objective         char(6),
                          bogus             char(1)   not null,
                          subtest           char(1));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawExamQa.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'examqa' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "except_stu" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasExceptStuCreated() {

            boolean ok = true;

            firePublish(24, "Creating 'except_stu' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.except_stu (
                          stu_id         char(9)   not null,
                          course         char(10)  not null,
                          unit           smallint  not null,
                          course_enroll  char(10)  not null,
                          hwork_status   char(1)   not null,
                          term           char(2)   not null,
                          term_yr        smallint  not null,
                          sect           char(4)   not null,
                          sect_enroll    char(4)   not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawExceptStu.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'except_stu' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "ffr_trns" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasFfrTrnsCreated() {

            boolean ok = true;

            firePublish(26, "Creating 'ffr_trns' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.ffr_trns (
                          stu_id         char(9)   not null,
                          course         char(10)  not null,
                          exam_placed    char(1)   not null,
                          exam_dt        date      not null,
                          dt_cr_refused  date);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawFfrTrns.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'ffr_trns' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "grade_roll" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasGradeRollCreated() {

            boolean ok = true;

            firePublish(27, "Creating 'grade_roll' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.grade_roll (
                          stu_id     char(9)   not null,
                          course     char(10)  not null,
                          sect       char(4)   not null,
                          fullname   char(26)  not null,
                          grade_opt  char(2),
                          term       char(2),
                          term_yr    smallint);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawGradeRoll.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'grade_roll' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "grading_std" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasGradingStdCreated() {

            boolean ok = true;

            firePublish(28, "Creating 'grading_std' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.grading_std (
                          grading_std          char(3)       not null,
                          only_over_mastery    char(1)       not null,
                          allow_point_coupons  char(1)       not null,
                          max_coupon_points    smallint,
                          coupon_factor        decimal(3,2)  not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawGradingStd.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'grading_std' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "high_schools" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasHighSchoolsCreated() {

            boolean ok = true;

            firePublish(29, "Creating 'high_schools' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.high_schools (
                          hs_code   char(6)   not null,
                          hs_name   char(35)  not null,
                          addres_1  char(35),
                          city      char(18),
                          state     char(2),
                          zip_code  char(10));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawHighSchools.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'high_schools' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "hold_type" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasHoldTypeCreated() {

            boolean ok = true;

            firePublish(30, "Creating 'hold_type' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.hold_type (
                          hold_id         char(2),
                          sev_admin_hold  char(1)   not null,
                          hold_type       char(10)  not null,
                          add_hold        char(1)   not null,
                          delete_hold     char(1)   not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawHoldType.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'hold_type' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "homework" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasHomeworkCreated() {

            boolean ok = true;

            firePublish(32, "Creating 'homework' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.homework (
                          version    char(20)  not null,
                          course     char(10)  not null,
                          unit       smallint  not null,
                          objective  smallint  not null,
                          title      char(32)  not null,
                          tree_ref   char(60)  not null,
                          hw_type    char(2)   not null,
                          active_dt  date      not null,
                          pull_dt    date);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(AssignmentRec.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'homework' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "index_descriptions" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasIndexDescriptionsCreated() {

            boolean ok = true;

            firePublish(33, "Creating 'index_descriptions' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.index_descriptions (
                          tblname   char(20)   not null,
                          indxname  char(18)   not null,
                          indxtype  char(15),
                          indxkeys  char(150)  not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet("index_descriptions");
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'index_descriptions' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "index_frequency" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasIndexFrequencyCreated() {

            boolean ok = true;

            firePublish(34, "Creating 'index_frequency' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.index_frequency (
                          on_demand     char(1),
                          daily_indx    char(1),
                          weekend_indx  char(1),
                          tblname       char(20)  not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet("index_frequency");
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'index_frequency' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "logins" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasLoginsCreated() {

            boolean ok = true;

            firePublish(35, "Creating 'logins' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.logins (
                          user_id             char(9)                  not null,
                          user_type           char(3)                  not null,
                          user_name           char(20)                 not null,
                          stored_key          char(64),
                          server_key          char(64),
                          dtime_created       datetime year to second  not null,
                          dtime_expires       datetime year to second,
                          dtime_last_login    datetime year to second,
                          force_pw_change     char(1)                  not null,
                          email               char(40),
                          salt                char(32),
                          nbr_invalid_atmpts  smallint);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawLogins.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'logins' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "mastery_attempt" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasMasteryAttemptCreated() {

            boolean ok = true;

            firePublish(36, "Creating 'mastery_attempt' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.mastery_attempt (
                          serial_nbr       integer                  not null,
                          exam_id          char(20)                 not null,
                          stu_id           char(9)                  not null,
                          when_started     datetime year to second  not null,
                          when_finished    datetime year to second  not null,
                          exam_score       smallint                 not null,
                          mastery_score    smallint,
                          passed           char(1)                  not null,
                          is_first_passed  char(1),
                          exam_source      char(2));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(MasteryAttemptRec.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'mastery_attempt' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "mastery_attempt_qa" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasMasteryAttemptQaCreated() {

            boolean ok = true;

            firePublish(38, "Creating 'mastery_attempt_qa' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.mastery_attempt_qa (
                          serial_nbr    integer   not null,
                          exam_id       char(20)  not null,
                          question_nbr  smallint  not null,
                          correct       char(1)   not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(MasteryAttemptQaRec.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'mastery_attempt_qa' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "mastery_exam" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasMasteryExamCreated() {

            boolean ok = true;

            firePublish(39, "Creating 'mastery_exam' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.mastery_exam (
                          exam_id       char(20)  not null,
                          exam_type     char(2)   not null,
                          course_id     char(10)  not null,
                          unit          smallint  not null,
                          objective     smallint  not null,
                          title         char(60),
                          tree_ref      char(60),
                          button_label  char(50),
                          when_active   date,
                          when_pulled   date);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(MasteryExamRec.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'mastery_exam' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "milestone" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasMilestoneCreated() {

            boolean ok = true;

            firePublish(40, "Creating 'milestone' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.milestone (
                          term              char(2)   not null,
                          term_yr           smallint  not null,
                          pace              smallint  not null,
                          pace_track        char(2)   not null,
                          ms_nbr            smallint  not null,
                          ms_type           char(3)   not null,
                          ms_date           date      not null,
                          nbr_atmpts_allow  smallint);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawMilestone.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'milestone' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "milestone_appeal" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasMilestoneAppealCreated() {

            boolean ok = true;

            firePublish(41, "Creating 'milestone_appeal' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.milestone_appeal (
                          stu_id            char(9)                  not null,
                          term              char(2)                  not null,
                          term_yr           smallint                 not null,
                          appeal_date_time  datetime year to second  not null,
                          appeal_type       char(3)                  not null,
                          pace              smallint,
                          pace_track        char(2),
                          ms_nbr            smallint,
                          ms_type           char(2),
                          prior_ms_date     date,
                          new_ms_dt         date,
                          attempts_allowed  smallint,
                          circumstances     char(200)                not null,
                          comment           char(200),
                          interviewer       char(20)                 not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawMilestoneAppeal.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'milestone_appeal' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "mpe" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasMpeCreated() {

            boolean ok = true;

            firePublish(42, "Creating 'mpe' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.mpe (
                          version               char(5)   not null,
                          max_online_atmpts     smallint  not null,
                          max_proctored_atmpts  smallint  not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawMpe.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'mpe' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "mpe_credit" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasMpeCreditCreated() {

            boolean ok = true;

            firePublish(44, "Creating 'mpe_credit' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.mpe_credit (
                          stu_id         char(9)   not null,
                          course         char(10)  not null,
                          exam_placed    char(1)   not null,
                          exam_dt        date      not null,
                          dt_cr_refused  date,
                          serial_nbr     integer,
                          version        char(5),
                          exam_source    char(2));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawMpeCredit.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'mpe_credit' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "mpe_log" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasMpeLogCreated() {

            boolean ok = true;

            firePublish(45, "Creating 'mpe_log' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.mpe_log (
                          stu_id       char(9)   not null,
                          academic_yr  char(4),
                          course       char(10)  not null,
                          version      char(5)   not null,
                          start_dt     date      not null,
                          exam_dt      date,
                          recover_dt   date,
                          serial_nbr   integer   not null,
                          start_time   integer   not null,
                          calc_nbr     char(4));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawMpeLog.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'mpe_log' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "mpecr_denied" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasMpecrDeniedCreated() {

            boolean ok = true;

            firePublish(46, "Creating 'mpecr_denied' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.mpecr_denied (
                          stu_id       char(9)   not null,
                          course       char(10)  not null,
                          exam_placed  char(1)   not null,
                          exam_dt      date      not null,
                          why_denied   char(2)   not null,
                          serial_nbr   integer,
                          version      char(5),
                          exam_source  char(2));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawMpecrDenied.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'mpecr_denied' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "msg" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasMsgCreated() {

            boolean ok = true;

            firePublish(47, "Creating 'msg' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.msg (
                          term         char(2)   not null,
                          term_yr      smallint  not null,
                          touch_point  char(3)   not null,
                          msg_code     char(8)   not null,
                          subject      char(60),
                          template     lvarchar(2000));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawMsg.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'msg' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "msg_lookup" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasMsgLookupCreated() {

            boolean ok = true;

            firePublish(48, "Creating 'msg_lookup' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.msg_lookup (
                          domain  char(20)   not null,
                          code    char(2)    not null,
                          value   char(200)  not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawMsgLookup.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'msg_lookup' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "pace_appeals" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasPaceAppealsCreated() {

            boolean ok = true;

            firePublish(50, "Creating 'pace_appeals' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.pace_appeals (
                          stu_id            char(9)    not null,
                          term              char(2)    not null,
                          term_yr           smallint   not null,
                          appeal_dt         date       not null,
                          relief_given      char(1),
                          pace              smallint   not null,
                          pace_track        char(2),
                          ms_nbr            smallint   not null,
                          ms_type           char(8),
                          ms_date           date       not null,
                          new_deadline_dt   date,
                          nbr_atmpts_allow  smallint,
                          circumstances     char(200)  not null,
                          comment           char(200),
                          interviewer       char(20)   not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawPaceAppeals.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'pace_appeals' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "pace_track_rule" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasPaceTrackRuleCreated() {

            boolean ok = true;

            firePublish(51, "Creating 'pace_track_rule' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.pace_track_rule (
                          term        char(2)   not null,
                          term_yr     smallint  not null,
                          subterm     char(4)   not null,
                          pace        smallint  not null,
                          pace_track  char(2)   not null,
                          criteria    char(30)  not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawPaceTrackRule.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'pace_track_rule' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "pacing_rules" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasPacingRulesCreated() {

            boolean ok = true;

            firePublish(52, "Creating 'pacing_rules' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.pacing_rules (
                          pacing_structure  char(1)   not null,
                          term              char(2)   not null,
                          term_yr           smallint  not null,
                          activity_type     char(2)   not null,
                          requirement       char(4)   not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawPacingRules.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'pacing_rules' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "pacing_structure" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasPacingStructureCreated() {

            boolean ok = true;

            firePublish(53, "Creating 'pacing_structure' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.pacing_structure (
                          pacing_structure      char(1)   not null,
                          term                  char(2)   not null,
                          term_yr               smallint  not null,
                          def_pace_track        char(2),
                          require_licensed      char(1)   not null,
                          require_partic        char(1)   not null,
                          max_partic_missed     smallint  not null,
                          allow_inc             char(1)   not null,
                          max_courses           smallint  not null,
                          nbr_open_allowed      smallint  not null,
                          require_unit_exams    char(1),
                          use_midterms          char(1),
                          allow_coupons         char(1),
                          coupons_after_window  char(1),
                          users_progress_cr     smallint,
                          hw_progress_cr        smallint,
                          re_progress_cr        smallint,
                          ue_progress_cr        smallint,
                          fin_progress_cr       smallint,
                          pacing_name           char(30),
                          schedule_source       char(9),
                          sr_due_date_enforced  char(1),
                          re_due_date_enforced  char(1),
                          ue_due_date_enforced  char(1),
                          fe_due_date_enforced  char(1),
                          first_obj_avail       char(1));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawPacingStructure.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'pacing_structure' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "parameters" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasParametersCreated() {

            boolean ok = true;

            firePublish(54, "Creating 'parameters' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.parameters (
                          pgm_name  char(20)  not null,
                          parm1     char(20),
                          parm2     char(20),
                          parm3     char(20),
                          parm4     char(20),
                          parm5     char(20),
                          parm6     char(20),
                          parm7     char(20),
                          parm8     char(20),
                          parm9     char(20),
                          parm10    date);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawParameters.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'parameters' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "plc_fee" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasPlcFeeCreated() {

            boolean ok = true;

            firePublish(56, "Creating 'plc_fee' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.plc_fee (
                          stu_id   char(9)   not null,
                          course   char(10)  not null,
                          exam_dt  date      not null,
                          bill_dt  date      not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawPlcFee.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'plc_fee' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "prereq" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasPrereqCreated() {

            boolean ok = true;

            firePublish(57, "Creating 'prereq' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.prereq (
                          course        char(10)  not null,
                          term          char(2)   not null,
                          term_yr       smallint  not null,
                          prerequisite  char(6)   not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawPrereq.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'prereq' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "remote_mpe" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasRemoteMpeCreated() {

            boolean ok = true;

            firePublish(58, "Creating 'remote_mpe' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.remote_mpe (
                          term       char(2)   not null,
                          term_yr    smallint  not null,
                          apln_term  char(4)   not null,
                          course     char(10),
                          start_dt   date      not null,
                          end_dt     date      not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawRemoteMpe.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'remote_mpe' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "report_perms" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasReportPermsCreated() {

            boolean ok = true;

            firePublish(59, "Creating 'report_perms' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.report_perms (
                          stu_id      char(9)   not null,
                          rpt_id      char(9)   not null,
                          perm_level  smallint  not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(ReportPermsRec.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'report_perms' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "resource" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasResourceCreated() {

            boolean ok = true;

            firePublish(60, "Creating 'resource' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.resource (
                          resource_id    char(7)   not null,
                          resource_type  char(2)   not null,
                          resource_desc  char(80),
                          days_allowed   smallint  not null,
                          holds_allowed  smallint  not null,
                          hold_id        char(2)   not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawResource.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'resource' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "semester_calendar" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasSemesterCalendarCreated() {

            boolean ok = true;

            firePublish(62, "Creating 'semester_calendar' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.semester_calendar (
                          term      char(2)   not null,
                          term_yr   smallint  not null,
                          week_nbr  smallint  not null,
                          start_dt  date      not null,
                          end_dt    date      not null);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawSemesterCalendar.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'semester_calendar' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "special_stus" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasSpecialStusCreated() {

            boolean ok = true;

            firePublish(63, "Creating 'special_stus' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.special_stus (
                          stu_id    char(9)  not null,
                          stu_type  char(7)  not null,
                          start_dt  date,
                          end_dt    date);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawSpecialStus.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'special_stus' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stchallenge" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStChallengeCreated() {

            boolean ok = true;

            firePublish(64, "Creating 'stchallenge' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stchallenge (
                          stu_id          char(9)   not null,
                          course          char(10)  not null,
                          version         char(5)   not null,
                          academic_yr     char(4)   not null,
                          exam_dt         date      not null,
                          start_time      integer,
                          finish_time     integer   not null,
                          last_name       char(30),
                          first_name      char(30),
                          middle_initial  char(1),
                          seq_nbr         smallint,
                          serial_nbr      integer,
                          score           smallint,
                          passed          char(1)   not null,
                          how_validated   char(1));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStchallenge.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stchallenge' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stchallengeqa" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStChallengeQaCreated() {

            boolean ok = true;

            firePublish(65, "Creating 'stchallengeqa' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stchallengeqa (
                          stu_id        char(9)   not null,
                          course        char(10)  not null,
                          version       char(5)   not null,
                          exam_dt       date,
                          finish_time   integer,
                          question_nbr  smallint,
                          stu_answer    char(5),
                          ans_correct   char(1));""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStchallengeqa.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stchallengeqa' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stcourse" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStCourseCreated() {

            boolean ok = true;

            firePublish(66, "Creating 'stcourse' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stcourse (
                          stu_id               char(9)   not null,
                          course               char(10)  not null,
                          sect                 char(4)   not null,
                          term                 char(2)   not null,
                          term_yr              smallint  not null,
                          pace_order           smallint,
                          open_status          char(1),
                          grading_option       char(2),
                          completed            char(1)   not null,
                          score                smallint,
                          course_grade         char(2),
                          prereq_satis         char(1),
                          init_class_roll      char(1)   not null,
                          stu_provided         char(1),
                          final_class_roll     char(1)   not null,
                          exam_placed          char(1),
                          zero_unit            smallint,
                          timeout_factor       decimal(3,2),
                          forfeit_i            char(1),
                          i_in_progress        char(1)   not null,
                          i_counted            char(1),
                          ctrl_test            char(1)   not null,
                          deferred_f_dt        date,
                          bypass_timeout       smallint  not null,
                          instrn_type          char(2),
                          registration_status  char(2),
                          last_class_roll_dt   date,
                          i_term               char(2),
                          i_term_yr            smallint,
                          i_deadline_dt        date);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStcourse.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stcourse' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stcunit" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStCunitCreated() {

            boolean ok = true;

            firePublish(68, "Creating 'stcunit' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stcunit (
                          stu_id            char(9)   not null,
                          course            char(10)  not null,
                          unit              smallint  not null,
                          review_status     char(1)   not null,
                          review_score      smallint,
                          review_points     smallint,
                          proctored_status  char(1)   not null,
                          proctored_score   smallint,
                          proctored_points  smallint);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStcunit.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stcunit' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stcuobjective" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStCuobjectiveCreated() {

            boolean ok = true;

            firePublish(69, "Creating 'stcuobjective' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stcuobjective (
                          stu_id                   char(9)   not null,
                          course                   char(10)  not null,
                          unit                     smallint  not null,
                          objective                smallint  not null,
                          lecture_viewed_dt        date,
                          seed                     integer,
                          last_component_finished  smallint);""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStcuobjective.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stcuobjective' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "std_milestone" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStdMilestoneCreated() {

            boolean ok = true;

            firePublish(70, "Creating 'std_milestone' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.std_milestone (
                          pace_track  char(1)   not null,
                          pace        smallint  not null,
                          pace_index  smallint  not null,
                          unit        smallint  not null,
                          objective   smallint  not null,
                          ms_type     char(2)   not null,
                          ms_date     date      not null)""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(StandardMilestoneRec.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'std_milestone' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stetext" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStETextCreated() {

            boolean ok = true;

            firePublish(71, "Creating 'stetext' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stetext (
                          stu_id              char(9) not null,
                          etext_id            char(6) not null,
                          active_dt           date not null,
                          etext_key           char(20),
                          expiration_dt       date,
                          refund_deadline_dt  date,
                          refund_dt           date,
                          refund_reason       char(60))""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStetext.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stetext' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stexam" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStExamCreated() {

            boolean ok = true;

            firePublish(72, "Creating 'stexam' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stexam (
                          serial_nbr       integer   not null,
                          version          char(20)  not null,
                          stu_id           char(9)   not null,
                          exam_dt          date      not null,
                          exam_score       smallint  not null,
                          mastery_score    smallint,
                          start_time       integer   not null,
                          finish_time      integer   not null,
                          time_ok          char(1)   not null,
                          passed           char(1)   not null,
                          seq_nbr          smallint,
                          course           char(10)  not null,
                          unit             smallint  not null,
                          exam_type        char(2)   not null,
                          is_first_passed  char(1),
                          exam_source      char(2),
                          calc_nbr         char(7))""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStexam.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stexam' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "sthomework" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStHomeworkCreated() {

            boolean ok = true;

            firePublish(74, "Creating 'sthomework' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.sthomework (
                          serial_nbr       integer   not null,
                          version          char(20)  not null,
                          stu_id           char(9)   not null,
                          hw_dt            date      not null,
                          hw_score         smallint  not null,
                          start_time       integer   not null,
                          finish_time      integer   not null,
                          time_ok          char(1)   not null,
                          passed           char(1)   not null,
                          hw_type          char(2)   not null,
                          course           char(10)  not null,
                          sect             char(4)   not null,
                          unit             smallint  not null,
                          objective        char(6)   not null,
                          hw_coupon        char(1)   not null,
                          used_dt          date,
                          used_serial_nbr  integer)""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawSthomework.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'sthomework' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "sthwqa" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStHwQaCreated() {

            boolean ok = true;

            firePublish(75, "Creating 'sthwqa' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.sthwqa (
                          serial_nbr    integer       not null,
                          question_nbr  smallint      not null,
                          answer_nbr    smallint      not null,
                          objective     char(6)       not null,
                          stu_answer    varchar(100)  not null,
                          stu_id        char(9)       not null,
                          version       char(5)       not null,
                          ans_correct   char(1)       not null,
                          hw_dt         date          not null,
                          finish_time   integer)""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawSthwqa.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'sthwqa' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stmathplan" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStMathPlanCreated() {

            boolean ok = true;

            firePublish(76, "Creating 'stmathplan' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stmathplan (
                          stu_id       char(9)   not null,
                          pidm         integer   not null,
                          apln_term    char(4),
                          version      char(5)   not null,
                          exam_dt      date      not null,
                          survey_nbr   smallint  not null,
                          stu_answer   char(50),
                          finish_time  integer   not null,
                          session      bigint)""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStmathplan.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stmathplan' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stmilestone" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStMilestoneCreated() {

            boolean ok = true;

            firePublish(77, "Creating 'stmilestone' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stmilestone (
                          stu_id            char(9)   not null,
                          term              char(2)   not null,
                          term_yr           smallint  not null,
                          pace_track        char(2)   not null,
                          ms_nbr            smallint  not null,
                          ms_type           char(8)   not null,
                          ms_date           date      not null,
                          nbr_atmpts_allow  smallint)""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStmilestone.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stmilestone' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stmpe" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStMpeCreated() {

            boolean ok = true;

            firePublish(78, "Creating 'stmpe' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stmpe (
                          stu_id          char(9)   not null,
                          version         char(5)   not null,
                          academic_yr     char(4)   not null,
                          exam_dt         date      not null,
                          start_time      integer,
                          finish_time     integer   not null,
                          last_name       char(30),
                          first_name      char(30),
                          middle_initial  char(1),
                          seq_nbr         smallint,
                          serial_nbr      integer,
                          sts_a           smallint,
                          sts_117         smallint,
                          sts_118         smallint,
                          sts_124         smallint,
                          sts_125         smallint,
                          sts_126         smallint,
                          placed          char(1)   not null,
                          how_validated   char(1))""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStmpe.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stmpe' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stmpeqa" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStMpeQaCreated() {

            boolean ok = true;

            firePublish(80, "Creating 'stmpeqa' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stmpeqa (
                          stu_id        char(9)   not null,
                          version       char(5)   not null,
                          exam_dt       date,
                          finish_time   integer,
                          question_nbr  smallint,
                          stu_answer    char(5),
                          ans_correct   char(1),
                          subtest       char(3),
                          tree_ref      char(40))""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStmpeqa.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stmpeqa' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stmsg" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStMsgCreated() {

            boolean ok = true;

            firePublish(81, "Creating 'stmsg' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stmsg (
                          stu_id        char(9)   not null,
                          msg_dt        date      not null,
                          pace          smallint,
                          course_index  smallint,
                          touch_point   char(3)   not null,
                          msg_code      char(8)   not null,
                          sender        char(50))""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStmsg.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stmsg' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stpace_summary" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStPaceSummaryCreated() {

            boolean ok = true;

            firePublish(82, "Creating 'stpace_summary' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stpace_summary (
                          stu_id         char(9)   not null,
                          course         char(10)  not null,
                          sect           char(4)   not null,
                          term           char(2)   not null,
                          term_yr        smallint  not null,
                          i_in_progress  char(1)   not null,
                          pace           smallint  not null,
                          pace_track     char(2),
                          pace_order     smallint  not null,
                          ms_nbr         smallint  not null,
                          ms_unit        smallint  not null,
                          ms_date        date      not null,
                          new_ms_date    char(1),
                          exam_dt        date      not null,
                          re_points      smallint  not null)""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStpaceSummary.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stpace_summary' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stqa" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStQaCreated() {

            boolean ok = true;

            firePublish(83, "Creating 'stqa' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stqa (
                          serial_nbr    integer       not null,
                          question_nbr  smallint      not null,
                          answer_nbr    smallint,
                          objective     char(10),
                          stu_answer    varchar(100),
                          stu_id        char(9)       not null,
                          version       char(5)       not null,
                          ans_correct   char(1),
                          exam_dt       date,
                          subtest       char(1),
                          finish_time   integer)""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStqa.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stqa' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stresource" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStResourceCreated() {

            boolean ok = true;

            firePublish(84, "Creating 'stresource' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stresource (
                          stu_id         char(9)   not null,
                          resource_id    char(7)   not null,
                          loan_dt        date      not null,
                          start_time     integer   not null,
                          due_dt         date      not null,
                          return_dt      date,
                          finish_time    integer,
                          times_display  smallint  not null,
                          create_dt      date)""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStresource.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stresource' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stsurveyqa" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStSurveyQaCreated() {

            boolean ok = true;

            firePublish(86, "Creating 'stsurveyqa' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stsurveyqa (
                          stu_id       char(9)   not null,
                          version      char(5)   not null,
                          exam_dt      date      not null,
                          survey_nbr   smallint  not null,
                          stu_answer   char(50),
                          finish_time  integer   not null)""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStsurveyqa.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stsurveyqa' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stterm" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStTermCreated() {

            boolean ok = true;

            firePublish(87, "Creating 'stterm' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stterm (
                          stu_id          char(9)   not null,
                          term            char(2)   not null,
                          term_yr         smallint  not null,
                          pace            smallint  not null,
                          pace_track      char(2)   not null,
                          first_course    char(10)  not null,
                          cohort          char(8),
                          urgency         smallint,
                          canvas_id       char(8),
                          case_mgr        char(20),
                          do_not_disturb  char(1))""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStterm.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stterm' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stu_course_mastery" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStuCourseMasteryCreated() {

            boolean ok = true;

            firePublish(88, "Creating 'stu_course_mastery' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stu_course_mastery (
                          stu_id           char(9)   not null,
                          course_id        char(10)  not null,
                          score            smallint  not null,
                          nbr_mastered_h1  smallint  not null,
                          nbr_mastered_h2  smallint  not null,
                          nbr_eligible     smallint  not null,
                          explor_1_status  char(2),
                          explor_2_status  char(2))""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(StudentCourseMasteryRec.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stu_course_mastery' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stu_std_milestone" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStuStdMilestoneCreated() {

            boolean ok = true;

            firePublish(89, "Creating 'stu_std_milestone' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stu_std_milestone (
                          stu_id      char(9)   not null,
                          pace_track  char(1)   not null,
                          pace        smallint  not null,
                          pace_index  smallint  not null,
                          unit        smallint  not null,
                          objective   smallint  not null,
                          ms_type     char(2)   not null,
                          ms_date     date      not null)""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(StudentStandardMilestoneRec.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stu_std_milestone' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stu_unit_mastery" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStuUnitMasteryCreated() {

            boolean ok = true;

            firePublish(90, "Creating 'stu_unit_mastery' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stu_unit_mastery (
                          stu_id     char(9)   not null,
                          course_id  char(10)  not null,
                          unit       smallint  not null,
                          score      smallint  not null,
                          sr_status  char(2),
                          s1_status  char(3),
                          s2_status  char(3),
                          s3_status  char(3))""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(StudentUnitMasteryRec.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stu_unit_mastery' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "student" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStudentCreated() {

            boolean ok = true;

            firePublish(92, "Creating 'student' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.student (
                          stu_id            char(9)       not null,
                          pidm              integer,
                          last_name         char(30),
                          first_name        char(30),
                          pref_name         char(30),
                          middle_initial    char(1),
                          apln_term         char(4),
                          class             char(2),
                          college           char(2),
                          dept              char(4),
                          program_code      char(14),
                          minor             char(6),
                          est_graduation    char(4),
                          tr_credits        char(5),
                          hs_code           char(6),
                          hs_gpa            char(4),
                          hs_class_rank     smallint,
                          hs_size_class     smallint,
                          act_score         smallint,
                          sat_score         smallint,
                          ap_score          char(3),
                          resident          char(4),
                          birthdate         date,
                          ethnicity         char(2),
                          gender            char(1),
                          discip_history    char(1)       not null,
                          discip_status     char(2),
                          sev_admin_hold    char(1),
                          timelimit_factor  decimal(3,2),
                          licensed          char(1)       not null,
                          campus            char(20),
                          stu_email         char(60),
                          adviser_email     char(60),
                          password          char(3),
                          admit_type        char(2),
                          order_enforce     char(2)       not null,
                          pacing_structure  char(1),
                          create_dt         date,
                          extension_days    smallint,
                          canvas_id         char(10))""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStudent.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'student' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "stvisit" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasStVisitCreated() {

            boolean ok = true;

            firePublish(93, "Creating 'stvisit' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.stvisit (
                          stu_id        char(9)                  not null,
                          when_started  datetime year to second  not null,
                          when_ended    datetime year to second,
                          location      char(2)                  not null,
                          seat          char(3))""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawStvisit.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'stvisit' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "surveyqa" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasSurveyQaCreated() {

            boolean ok = true;

            firePublish(94, "Creating 'surveyqa' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.surveyqa (
                          term            char(2)   not null,
                          term_yr         smallint  not null,
                          version         char(5)   not null,
                          survey_nbr      smallint  not null,
                          question_desc   char(30)  not null,
                          type_question   char(6),
                          answer          char(5)   not null,
                          answer_desc     char(30)  not null,
                          answer_meaning  char(6),
                          must_answer     char(1),
                          tree_ref        char(40))""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawSurveyqa.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'surveyqa' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "term" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasTermCreated() {

            boolean ok = true;

            firePublish(95, "Creating 'term' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.term (
                          term             char(2)   not null,
                          term_yr          smallint  not null,
                          start_dt         date,
                          end_dt           date,
                          ar_info_rcvd     char(2),
                          max_ip_courses   smallint,
                          max_mp_courses   smallint,
                          academic_yr      char(4),
                          ctrl_enforce     char(1)   not null,
                          ctrl_pwd         char(8),
                          ckout_pwd        char(8),
                          last_rec_dt      date,
                          active           char(1),
                          active_index     smallint,
                          i_deadline_dt    date,
                          w_drop_dt        date,
                          view_enforce     char(1),
                          view_warning     smallint,
                          view_required    smallint,
                          disp_admin_hold  smallint)""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(TermRec.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'term' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "testing_centers" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasTestingCentersCreated() {

            boolean ok = true;

            firePublish(96, "Creating 'testing_centers' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.testing_centers (
                          testing_center_id  char(14)                 not null,
                          tc_name            char(40)                 not null,
                          addres_1           char(35),
                          addres_2           char(35),
                          addres_3           char(35),
                          city               char(18)                 not null,
                          state              char(2)                  not null,
                          zip_code           char(10)                 not null,
                          active             char(1)                  not null,
                          dtime_created      datetime year to second  not null,
                          dtime_approved     datetime year to second,
                          dtime_denied       datetime year to second,
                          dtime_revoked      datetime year to second,
                          is_remote          char(1)                  not null,
                          is_proctored       char(1)                  not null)""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawTestingCenter.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'testing_centers' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "user_clearance" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasUserClearanceCreated() {

            boolean ok = true;

            firePublish(98, "Creating 'user_clearance' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.user_clearance (
                          login           char(8)   not null,
                          clear_function  char(9)   not null,
                          clear_type      smallint  not null,
                          clear_passwd    char(8))""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawUserClearance.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'user_clearance' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Creates the "users" table.
         *
         * @return true if the table was created; false if not
         */
        private boolean wasUsersCreated() {

            boolean ok = true;

            firePublish(99, "Creating 'users' table...");

            if (!DEBUG) {
                final String sql = """
                        CREATE TABLE math.users (
                          stu_id       char(9)   not null,
                          term         char(2)   not null,
                          term_yr      smallint  not null,
                          serial_nbr   integer,
                          version      char(5)   not null,
                          exam_dt      date,
                          exam_score   smallint,
                          calc_course  char(2)   not null,
                          passed       char(1))""";

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                    ok = werePermissionsSet(RawUsers.TABLE_NAME);
                } catch (final SQLException ex) {
                    Log.warning("Failed to create 'users' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }

        /**
         * Sets permissions on a table.
         *
         * @param tableName the table name
         */
        private boolean werePermissionsSet(final String tableName) {

            Log.info("Setting permissions on '", tableName, "' table...");

            boolean ok = true;

            if (!DEBUG) {
                final String sql = SimpleBuilder.concat("REVOKE ALL ON math.", tableName, " FROM public AS math");

                try (final Statement stmt = this.owner.archiveCache.conn.createStatement()) {
                    stmt.executeUpdate(sql);
                } catch (final SQLException ex) {
                    Log.warning("Failed to revoke public permissions on '", tableName, "' table in archive database");
                    ok = false;
                }
            }

            return ok;
        }
    }
}

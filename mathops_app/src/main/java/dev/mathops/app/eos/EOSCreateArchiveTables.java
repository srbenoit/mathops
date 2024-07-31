package dev.mathops.app.eos;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.ContextMap;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Logic to create all needed tables in the archive database.  The archive database must have been created, and logging
 * must be turned on ("ontape -s -U "term****") and we must be able to connect to it and pass a connection object into
 * this class's methods.
 */
public class EOSCreateArchiveTables {

    /** TRUE to simply print what actions would be taken, FALSE to actually take actions. */
    private static final boolean DEBUG = true;

    /** The database connection to the archive database. */
    private final DbConnection archiveConnection;

    /**
     * Constructs a new {@code EOSCreateArchiveTables}.
     *
     * @param theArchiveConnection the database connection to the archive database
     */
    public EOSCreateArchiveTables(final DbConnection theArchiveConnection) {

        this.archiveConnection = theArchiveConnection;
    }

    /**
     * Performs the table creation process.
     */
    public void run() {

        if (isDatabaseCorrect()) {
            Log.info("Archive database is valid...");

            if (wasAdminHoldCreated()
                    && wasCalcsCreated()
                    && wasCampusCalendarCreated()
                    && wasChallengeFeeCreated()
                    && wasClientPcCreated()
                    && wasCohortCreated()
                    && wasCourseCreated()
                    && wasCrsectionCreated()
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
                    && wasExceptStuCreated()) {
                Log.info("Archive database tables created");
            }
        }
    }

    /**
     * Verifies that none of the expected tables exist already.
     *
     * @return true if databases are verified; false if not
     */
    private boolean isDatabaseCorrect() {

        final String sql = "SELECT COUNT(*) FROM systables WHERE tabname=?";

        final String[] tables = {"admin_hold"};

        boolean ok = true;

        try (final PreparedStatement pstmt = this.archiveConnection.prepareStatement(sql)) {

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

        Log.info("Creating 'admin_hold' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.admin_hold ("
                    + " stu_id          char(9)  not null,"
                    + " hold_id         char(2),"
                    + " sev_admin_hold  char(1),"
                    + " times_display   integer,"
                    + " create_dt       date);";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("admin_hold");
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

        Log.info("Creating 'calcs' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.calcs ("
                    + " stu_id      char(9)  not null,"
                    + " issued_nbr  char(7)  not null,"
                    + " return_nbr  char(7)  not null,"
                    + " serial_nbr  integer  not null,"
                    + " exam_dt     date     not null);";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("calcs");
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

        Log.info("Creating 'campus_calendar' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.campus_calendar ("
                    + " campus_dt    date      not null,"
                    + " dt_desc      char(20)  not null,"
                    + " open_time1   char(10),"
                    + " open_time2   char(10),"
                    + " close_time1  char(10),"
                    + " close_time2  char(10),"
                    + " weekdays_1   char(20),"
                    + " weekdays_2   char(20));";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("campus_calendar");
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

        Log.info("Creating 'challenge_fee' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.challenge_fee ("
                    + " stu_id   char(9)   not null,"
                    + " course   char(10)  not null,"
                    + " exam_dt  date      not null,"
                    + " bill_dt  date      not null);";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("challenge_fee");
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

        Log.info("Creating 'client_pc' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.client_pc ("
                    + " computer_id        char(40)                 not null,"
                    + " testing_center_id  char(14)                 not null,"
                    + " station_nbr        char(10),"
                    + " computer_desc      varchar(80),"
                    + " icon_x             smallint,"
                    + " icon_y             smallint,"
                    + " pc_usage           char(1)                  not null,"
                    + " current_status     smallint                 not null,"
                    + " dtime_created      datetime year to second  not null,"
                    + " dtime_approved     datetime year to second,"
                    + " mac_address        char(12),"
                    + " power_status       char(1),"
                    + " power_on_due       integer,"
                    + " last_ping          integer,"
                    + " current_stu_id     char(9),"
                    + " current_course     char(10),"
                    + " current_unit       smallint,"
                    + " current_version    char(5));";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("client_pc");
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

        Log.info("Creating 'cohort' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.cohort ("
                    + " cohort      char(8)   not null,"
                    + " size        smallint  not null,"
                    + " instructor  char(30));";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("cohort");
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

        Log.info("Creating 'course' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.course ("
                    + " course         char(10)  not null,"
                    + " nbr_units      smallint  not null,"
                    + " course_name    char(50),"
                    + " nbr_credits    smallint  not null,"
                    + " calc_ok        char(1),"
                    + " course_label   char(40)  not null,"
                    + " inline_prefix  char(20),"
                    + " is_tutorial    char(1)   not null,"
                    + " require_etext  char(1)   not null);";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("course");
            } catch (final SQLException ex) {
                Log.warning("Failed to create 'course' table in archive database");
                ok = false;
            }
        }

        return ok;
    }

    /**
     * Creates the "crsection" table.
     *
     * @return true if the table was created; false if not
     */
    private boolean wasCrsectionCreated() {

        boolean ok = true;

        Log.info("Creating 'crsection' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.crsection ("
                    + " course              char(10)  not null,"
                    + " sect                char(4)   not null,"
                    + " unit                smallint  not null,"
                    + " term                char(2)   not null,"
                    + " term_yr             smallint  not null,"
                    + " re_start_dt         date,"
                    + " re_end_dt           date,"
                    + " re_first_credit_dt  date,"
                    + " re_last_credit_dt   date,"
                    + " re_points_ontime    smallint,"
                    + " re_points_late      smallint);";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("crsection");
            } catch (final SQLException ex) {
                Log.warning("Failed to create 'crsection' table in archive database");
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

        Log.info("Creating 'csection' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.csection ("
                    + " course                char(10)  not null,"
                    + " sect                  char(4)   not null,"
                    + " term                  char(2)   not null,"
                    + " term_yr               smallint  not null,"
                    + " section_id            char(6),"
                    + " aries_start_dt        date,"
                    + " aries_end_dt          date,"
                    + " start_dt              date,"
                    + " exam_delete_dt        date,"
                    + " instrn_type           char(2),"
                    + " instructor            char(30),"
                    + " campus                char(2)   not null,"
                    + " pacing_structure      char(1),"
                    + " mtg_days              char(5),"
                    + " classroom_id          char(14),"
                    + " lst_stcrs_creat_dt    date,"
                    + " grading_std           char(3),"
                    + " a_min_score           smallint,"
                    + " b_min_score           smallint,"
                    + " c_min_score           smallint,"
                    + " d_min_score           smallint,"
                    + " survey_id             char(5),"
                    + " course_label_shown    char(1),"
                    + " display_score         char(1),"
                    + " display_grade_scale   char(1),"
                    + " count_in_max_courses  char(1),"
                    + " online                char(1)   not null,"
                    + " bogus                 char(1)   not null,"
                    + " canvas_id             char(40),"
                    + " subterm               char(4))";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("csection");
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

        Log.info("Creating 'cunit' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.cunit ("
                    + " course          char(10)  not null,"
                    + " unit            smallint  not null,"
                    + " term            char(2)   not null,"
                    + " term_yr         smallint  not null,"
                    + " unit_exam_wgt   decimal(3,2),"
                    + " unit_desc       char(50),"
                    + " unit_timelimit  smallint,"
                    + " possible_score  smallint,"
                    + " nbr_questions   smallint,"
                    + " unit_type       char(4));";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("cunit");
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

        Log.info("Creating 'cuobjective' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.cuobjective ("
                    + " course      char(10)  not null,"
                    + " unit        smallint  not null,"
                    + " term        char(2)   not null,"
                    + " term_yr     smallint  not null,"
                    + " objective   smallint  not null,"
                    + " lesson_id   char(40),"
                    + " lesson_nbr  char(10),"
                    + " start_dt    date);";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("cuobjective");
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

        Log.info("Creating 'cusection' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.cusection ("
                    + " course             char(10)  not null,"
                    + " sect               char(4)   not null,"
                    + " unit               smallint  not null,"
                    + " term               char(2)   not null,"
                    + " term_yr            smallint  not null,"
                    + " timeout            smallint  not null,"
                    + " re_mastery_score   smallint,"
                    + " ue_mastery_score   smallint,"
                    + " hw_mastery_score   smallint  not null,"
                    + " hw_moveon_score    smallint  not null,"
                    + " nbr_atmpts_allow   smallint  not null,"
                    + " atmpts_per_review  smallint  not null,"
                    + " first_test_dt      date      not null,"
                    + " last_test_dt       date      not null,"
                    + " begin_test_period  integer   not null,"
                    + " end_test_period    integer   not null,"
                    + " coupon_cost        smallint,"
                    + " last_coupon_dt     date,"
                    + " show_test_window   char(1),"
                    + " unproctored_exam   char(1),"
                    + " re_points_ontime   smallint);";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("cusection");
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

        Log.info("Creating 'discipline' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.discipline ("
                    + " stu_id          char(9)   not null,"
                    + " dt_incident     date      not null,"
                    + " incident_type   char(2)   not null,"
                    + " course          char(10)  not null,"
                    + " unit            smallint  not null,"
                    + " cheat_desc      char(100),"
                    + " action_type     char(2),"
                    + " action_comment  char(100),"
                    + " interviewer     char(20),"
                    + " proctor         char(20));";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("discipline");
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

        Log.info("Creating 'dont_submit' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.dont_submit ("
                    + " course   char(10)  not null,"
                    + " sect     char(4)   not null,"
                    + " term     char(2)   not null,"
                    + " term_yr  smallint  not null);";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("dont_submit");
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

        Log.info("Creating 'etext' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.etext ("
                    + " etext_id       char(6)  not null,"
                    + " retention      char(1)  not null,"
                    + " purchase_url   varchar(140),"
                    + " refund_period  smallint,"
                    + " key_entry      char(1)  not null,"
                    + " active         char(1)  not null,"
                    + " button_label   char(80));";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("etext");
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

        Log.info("Creating 'etext_course' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.etext_course ("
                    + " etext_id  char(6)   not null,"
                    + " course    char(10)  not null);";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("etext_course");
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

        Log.info("Creating 'etext_key' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.etext_key ("
                    + " etext_id   char(6)   not null,"
                    + " etext_key  char(20)  not null,"
                    + " active_dt  datetime year to second);";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("etext_key");
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

        Log.info("Creating 'exam' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.exam ("
                    + " version       char(5)   not null,"
                    + " course        char(10)  not null,"
                    + " unit          smallint  not null,"
                    + " vsn_explt     char(7),"
                    + " title         char(30),"
                    + " tree_ref      char(40),"
                    + " exam_type     char(2)   not null,"
                    + " active_dt     date      not null,"
                    + " pull_dt       date,"
                    + " button_label  char(50));";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("exam");
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

        Log.info("Creating 'examqa' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.examqa ("
                    + " version           char(5)   not null,"
                    + " problem_nbr       smallint  not null,"
                    + " exam_section_nbr  smallint,"
                    + " question_nbr      smallint  not null,"
                    + " correct_answer    char(5)   not null,"
                    + " objective         char(6),"
                    + " bogus             char(1)   not null,"
                    + " subtest           char(1));";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("examqa");
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

        Log.info("Creating 'except_stu' table...");

        if (!DEBUG) {
            final String sql = "CREATE TABLE math.except_stu ("
                    + " stu_id         char(9)   not null,"
                    + " course         char(10)  not null,"
                    + " unit           smallint  not null,"
                    + " course_enroll  char(10)  not null,"
                    + " hwork_status   char(1)   not null,"
                    + " term           char(2)   not null,"
                    + " term_yr        smallint  not null,"
                    + " sect           char(4)   not null,"
                    + " sect_enroll    char(4)   not null);";

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
                ok = setPermissions("except_stu");
            } catch (final SQLException ex) {
                Log.warning("Failed to create 'except_stu' table in archive database");
                ok = false;
            }
        }

        return ok;
    }

//    { TABLE "math".semester_calendar row size = 14 number of columns = 5 index size = 0 }
//    create table "math".semester_calendar
//            (
//                    term char(2) not null ,
//    term_yr smallint not null ,
//    week_nbr smallint not null ,
//    start_dt date not null ,
//    end_dt date not null
//            ) lock mode row;
//    revoke all on "math".semester_calendar from "public";
//
//
//    { TABLE "math".prereq row size = 16 number of columns = 4 index size = 0 }
//    create table "math".prereq
//            (
//                    course char(10) not null ,
//    term char(2) not null ,
//    term_yr smallint not null ,
//    prerequisite char(6) not null
//            ) extent size 8 next size 8 lock mode row;
//    revoke all on "math".prereq from "public";
//
//
//    { TABLE "math".mpe row size = 9 number of columns = 2 index size = 0 }
//    create table "math".mpe
//            (
//                    version char(5) not null ,
//    max_online_atmpts smallint not null,
//    max_proctored_atmpts smallint not null
//            ) extent size 8 next size 8 lock mode row;
//    revoke all on "math".mpe from "public";
//
//
//    { TABLE "math".parameters row size = 204 number of columns = 11 index size = 0 }
//    create table "math".parameters
//            (
//                    pgm_name char(20) not null ,
//    parm1 char(20),
//    parm2 char(20),
//    parm3 char(20),
//    parm4 char(20),
//    parm5 char(20),
//    parm6 char(20),
//    parm7 char(20),
//    parm8 char(20),
//    parm9 char(20),
//    parm10 date
//  ) extent size 8 next size 8 lock mode row;
//    revoke all on "math".parameters from "public";
//
//    { TABLE "math".high_schools row size = 406 number of columns = 18 index size = 15 }
//    create table "math".high_schools
//            (
//                    hs_code char(6) not null ,
//    hs_name char(35) not null ,
//    addres_1 char(35),
//    city char(18),
//    state char(2),
//    zip_code char(10)
//  ) extent size 512 next size 64 lock mode row;
//    revoke all on "math".high_schools from "public";
//
//
//    { TABLE "math".milestone_appeal row size = 464 number of columns = 15 index size = 14 }
//    create table "math".milestone_appeal
//            (
//                    stu_id char(9) not null,
//    term char(2) not null,
//    term_yr smallint not null,
//    appeal_date_time datetime year to second not null,
//    appeal_type char(3) not null,
//    pace smallint,
//    pace_track char(2),
//    ms_nbr smallint,
//    ms_type char(2),
//    prior_ms_date date,
//    new_ms_dt date,
//    attempts_allowed smallint,
//    circumstances char(200) not null,
//    comment char(200),
//    interviewer char(20) not null
//            ) lock mode row;
//    revoke all on "math".milestone_appeal from "public";
//
//
//    { TABLE "math".pace_appeals row size = 454 number of columns = 15 index size = 14 }
//    create table "math".pace_appeals
//            (
//                    stu_id char(9) not null ,
//    term char(2) not null ,
//    term_yr smallint not null ,
//    appeal_dt date not null ,
//    relief_given char(1),
//    pace smallint not null,
//    pace_track char(2),
//    ms_nbr smallint not null ,
//    ms_type char(2),
//    ms_date date not null,
//    new_deadline_dt date,
//    nbr_atmpts_allow smallint ,
//    circumstances char(200) not null ,
//    comment char(200),
//    interviewer char(20) not null
//            ) lock mode row;
//    revoke all on "math".pace_appeals from "public";
//
//
//    { TABLE "math".surveyqa row size = 89 number of columns = 10 index size = 22 }
//    create table "math".surveyqa
//            (
//                    term char(2) not null ,
//    term_yr smallint not null ,
//    version char(5) not null ,
//    survey_nbr smallint not null ,
//    question_desc char(30) not null ,
//    type_question char(6),
//    answer char(5) not null ,
//    answer_desc char(30) not null ,
//    answer_meaning char(6),
//    must_answer char(1),
//    tree_ref char(40)
//  ) extent size 64 next size 16 lock mode row;
//    revoke all on "math".surveyqa from "public";
//    create cluster index "math".i_survqa on "math".surveyqa (term,
//                                                             term_yr,version,survey_nbr);
//
//
//    { TABLE "math".user_clearance row size = 24 number of columns = 4 index size = 0
//    }
//    create table "math".user_clearance
//            (
//                    login char(8) not null ,
//    clear_function char(6) not null ,
//    clear_type smallint not null ,
//    clear_passwd char(8)
//  ) extent size 8 next size 8 lock mode row;
//    revoke all on "math".user_clearance from "public";
//
//
//    { TABLE "math".ffr_trns row size = 24 number of columns = 5 index size = 28 }
//    create table "math".ffr_trns
//            (
//                    stu_id char(9) not null ,
//    course char(10) not null ,
//    exam_placed char(1) not null ,
//    exam_dt date not null ,
//    dt_cr_refused date
//  ) extent size 8 next size 8 lock mode row;
//    revoke all on "math".ffr_trns from "public";
//
//
//
//    { TABLE "math".mpe_credit row size = 28 number of columns = 6 index size = 20 }
//    create table "math".mpe_credit
//            (
//                    stu_id char(9) not null ,
//    course char(10) not null ,
//    exam_placed char(1) not null ,
//    exam_dt date not null ,
//    dt_cr_refused date,
//    serial_nbr integer,
//    version char(5),
//    exam_source char(2)
//  ) lock mode row;
//    revoke all on "math".mpe_credit from "public";
//
//
//
//    { TABLE "math".mpe_log row size = 40 number of columns = 8 index size = 14 }
//    create table "math".mpe_log
//            (
//                    stu_id char(9) not null ,
//    academic_yr char(4),
//    course char(10) not null ,
//    version char(5) not null ,
//    start_dt date not null ,
//    exam_dt date,
//    recover_dt date,
//    serial_nbr integer not null ,
//    start_time integer not null ,
//    calc_nbr char(4)
//  ) lock mode row;
//    revoke all on "math".mpe_log from "public";
//
//
//
//    { TABLE "math".mpecr_denied row size = 26 number of columns = 6 index size = 20 }
//    create table "math".mpecr_denied
//            (
//                    stu_id char(9) not null ,
//    course char(10) not null ,
//    exam_placed char(1) not null ,
//    exam_dt date not null ,
//    why_denied char(2) not null ,
//    serial_nbr integer,
//    version char(5),
//    exam_source char(2)
//  ) lock mode row;
//    revoke all on "math".mpecr_denied from "public";
//
//
//
//    { TABLE "math".special_stus row size = 9 number of columns = 1 index size = 19 }
//    create table "math".special_stus
//            (
//                    stu_id char(9) not null ,
//    stu_type char(7) not null ,
//    start_dt date ,
//    end_dt date
//  ) lock mode row;
//    revoke all on "math".special_stus from "public";
//
//
//    { TABLE "math".term row size = 75 number of columns = 23 index size = 0 }
//    create table "math".term
//            (
//                    term char(2) not null ,
//    term_yr smallint not null ,
//    start_dt date,
//    end_dt date,
//    ar_info_rcvd char(2),
//    max_ip_courses smallint,
//    max_mp_courses smallint,
//    academic_yr char(4),
//    ctrl_enforce char(1) not null ,
//    ctrl_pwd char(8),
//    ckout_pwd char(8),
//    last_rec_dt date,
//    active char(1),
//    active_index smallint,
//    i_deadline_dt date,
//    w_drop_dt date,
//    view_enforce char(1),
//    view_warning smallint,
//    view_required smallint,
//    disp_admin_hold smallint
//  ) extent size 8 next size 8 lock mode row;
//    revoke all on "math".term from "public";
//
//
//    { TABLE "math".hold_type row size = 15 number of columns = 5 index size = 0 }
//    create table "math".hold_type
//            (
//                    hold_id char(2),
//    sev_admin_hold char(1) not null ,
//    hold_type char(10) not null ,
//    add_hold char(1) not null ,
//    delete_hold char(1) not null
//            ) extent size 8 next size 8 lock mode row;
//    revoke all on "math".hold_type from "public";
//
//
//    { TABLE "math".msg row size = 121 number of columns = 6 index size = 0 }
//    create table "math".msg
//            (
//                    term  char(2) not null,
//    term_yr smallint not null,
//    touch_point char(3) not null,
//    msg_code char(8) not null,
//    subject  char(60),
//    template  lvarchar(2000)
//  ) extent size 16 next size 16 lock mode row;
//    revoke all on "math".msg from "public";
//
//
//    { TABLE "math".msg_lookup row size = 94 number of columns = 3 index size = 27 }
//    create table "math".msg_lookup
//            (
//                    domain char(20) not null,
//    code char(2) not null,
//    value char(200) not null
//            ) extent size 8 next size 8 lock mode row;
//    revoke all on "math".msg_lookup from "public";
//
//
//
//    { TABLE "math".student row size = 190 number of columns = 37 index size = 14 }
//    create table "math".student
//            (
//                    stu_id char(9) not null ,
//    pidm integer,
//    last_name char(30),
//    first_name char(30),
//    pref_name char(30),
//    middle_initial char(1),
//    apln_term char(4),
//    class char(2),
//    college char(2),
//    dept char(4),
//    program_code char(14),
//    minor char(6),
//    est_graduation char(4),
//    tr_credits char(5),
//    hs_code char(6),
//    hs_gpa char(4),
//    hs_class_rank smallint,
//    hs_size_class smallint,
//    act_score smallint,
//    sat_score smallint,
//    ap_score char(3),
//    resident char(4),
//    birthdate date,
//    ethnicity char(2),
//    gender char(1),
//    discip_history char(1) not null ,
//    discip_status char(2),
//    sev_admin_hold char(1),
//    timelimit_factor decimal(3,2),
//    licensed char(1) not null ,
//    campus char(20),
//    stu_email char(60),
//    adviser_email char(60),
//    password char(3),
//    admit_type char(2),
//    order_enforce char(2) not null ,
//    pacing_structure char(1),
//    create_dt date,
//    extension_days smallint,
//    canvas_id char(10)
//  ) lock mode row;
//    revoke all on "math".student from "public";
//
//
//
//    { TABLE "math".users row size = 37 number of columns = 12 index size = 25 }
//    create table "math".users
//            (
//                    stu_id char(9) not null ,
//    term char(2) not null ,
//    term_yr smallint not null ,
//    serial_nbr integer,
//    version char(5) not null ,
//    exam_dt date,
//    exam_score smallint,
//    calc_course char(2) not null ,
//    passed char(1)
//  ) extent size 256 next size 64 lock mode row;
//    revoke all on "math".users from "public";
//
//
//    { TABLE "math".mdstudent }
//    create table "math".mdstudent
//            (
//                    stu_id char(9) not null ,
//    last_name char(30) not null,
//    first_name char(30) not null,
//    hs_code char(6) not null,
//    hs_grad_yr smallint,
//    gender char(1),
//    active char(1),
//    stu_nbr smallint not null,
//    create_dt date
//  ) extent size 64 next size 16 lock mode row;
//    revoke all on "math".mdstudent from "public";
//
//
//    { TABLE "math".stmdscores row size = 21 number of columns = 6 index size = 34 }
//    create table "math".stmdscores
//            (
//                    stu_id char(9) not null ,
//    term char(2) not null ,
//    term_yr smallint not null ,
//    exam_dt date not null ,
//    sts_nbr smallint not null ,
//    sts_score smallint not null
//            ) extent size 128 next size 64 lock mode row;
//    revoke all on "math".stmdscores from "public";
//
//
//
//
//    { TABLE "math".grade_roll row size = 51 number of columns = 7 index size = 34 }
//    create table "math".grade_roll
//            (
//                    stu_id char(9) not null ,
//    course char(10) not null ,
//    sect char(4) not null ,
//    fullname char(26) not null ,
//    grade_opt char(2),
//    term char(2),
//    term_yr smallint
//  ) extent size 256 next size 64 lock mode row;
//    revoke all on "math".grade_roll from "public";
//
//
//    { TABLE "math".plc_fee row size = 23 number of columns = 4 index size = 19 }
//    create table "math".plc_fee
//            (
//                    stu_id char(9) not null ,
//    course char(10) not null ,
//    exam_dt date not null ,
//    bill_dt date not null
//            ) extent size 2048 next size 256 lock mode row;
//    revoke all on "math".plc_fee from "public";
//
//
//    { TABLE "math".index_frequency row size = 21 number of columns = 4 index size = 0
//    }
//    create table "math".index_frequency
//            (
//                    on_demand char(1),
//    daily_indx char(1),
//    weekend_indx char(1),
//    tblname char(20) not null
//            ) extent size 8 next size 8 lock mode row;
//    revoke all on "math".index_frequency from "public";
//
//
//    { TABLE "math".index_descriptions row size = 201 number of columns = 4 index size
//            = 0 }
//    create table "math".index_descriptions
//            (
//                    tblname char(20) not null ,
//    indxname char(18) not null ,
//    indxtype char(15),
//    indxkeys char(150) not null
//            ) extent size 8 next size 8 lock mode row;
//    revoke all on "math".index_descriptions from "public";
//
//
//    { TABLE "math".homework row size = 99 number of columns = 9 index size = 13 }
//    create table "math".homework
//            (
//                    version char(20) not null ,
//    course char(10) not null ,
//    unit smallint not null ,
//    objective smallint not null ,
//    title char(32) not null ,
//    tree_ref char(60) not null ,
//    hw_type char(2) not null ,
//    active_dt date not null ,
//    pull_dt date
//  )  extent size 128 next size 64 lock mode row;
//    revoke all on "math".homework from "public";
//
//
//    { TABLE "math".grading_std row size = 10 number of columns = 5 index size = 0 }
//    create table "math".grading_std
//            (
//                    grading_std char(3) not null ,
//    only_over_mastery char(1) not null ,
//    allow_point_coupons char(1) not null ,
//    max_coupon_points smallint,
//    coupon_factor decimal(3,2) not null
//            ) lock mode row;
//    revoke all on "math".grading_std from "public";
//
//
//    { TABLE "math".pacing_rules row size = 11 number of columns = 5 index size = 0 }
//    create table "math".pacing_rules
//            (
//                    pacing_structure char(1) not null ,
//    term char(2) not null ,
//    term_yr smallint not null ,
//    activity_type char(2) not null ,
//    requirement char(4) not null
//            ) lock mode row;
//    revoke all on "math".pacing_rules from "public";
//
//
//
//    { TABLE "math".pacing_structure row size = 71 number of columns = 24
//        index size = 0 }
//    create table "math".pacing_structure
//            (
//                    pacing_structure char(1) not null ,
//    term char(2) not null ,
//    term_yr smallint not null ,
//    def_pace_track char(2) ,
//    require_licensed char(1) not null ,
//    require_partic char(1) not null ,
//    max_partic_missed smallint not null ,
//    allow_inc char(1) not null ,
//    max_courses smallint not null ,
//    nbr_open_allowed smallint not null ,
//    require_unit_exams char(1),
//    use_midterms char(1),
//    allow_coupons char(1),
//    coupons_after_window char(1),
//    users_progress_cr smallint,
//    hw_progress_cr smallint,
//    re_progress_cr smallint,
//    ue_progress_cr smallint,
//    fin_progress_cr smallint,
//    pacing_name char(30),
//    schedule_source char(9),
//    sr_due_date_enforced char(1),
//    re_due_date_enforced char(1),
//    ue_due_date_enforced char(1),
//    fe_due_date_enforced char(1),
//    first_obj_avail char(1)
//  ) lock mode row;
//    revoke all on "math".pacing_structure from "public";
//
//
//
//    { TABLE "math".remote_mpe row size = 16 number of columns = 5 index size = 0 }
//    create table "math".remote_mpe
//            (
//                    term char(2) not null ,
//    term_yr smallint not null ,
//    apln_term char(4) not null ,
//    course char(10) ,
//    start_dt date not null ,
//    end_dt date not null
//            )  extent size 8 next size 8 lock mode row;
//    revoke all on "math".remote_mpe from "public";
//
//
//    { TABLE "math".stcuobjective row size = 29 number of columns = 7 index size = 0 }
//    create table "math".stcuobjective
//            (
//                    stu_id char(9) not null ,
//    course char(10) not null ,
//    unit smallint not null ,
//    objective smallint not null ,
//    lecture_viewed_dt date,
//    seed integer,
//    last_component_finished smallint
//  ) lock mode row;
//    revoke all on "math".stcuobjective from "public";
//
//
//    { TABLE "math".sthomework row size = 63 number of columns = 17 index size = 23 }
//    create table "math".sthomework
//            (
//                    serial_nbr integer not null ,
//                    version char(20) not null ,
//    stu_id char(9) not null ,
//    hw_dt date not null ,
//    hw_score smallint not null ,
//    start_time integer not null ,
//    finish_time integer not null ,
//    time_ok char(1) not null ,
//    passed char(1) not null ,
//    hw_type char(2) not null ,
//    course char(10) not null ,
//    sect char(4) not null ,
//    unit smallint not null ,
//    objective char(6) not null ,
//    hw_coupon char(1) not null ,
//    used_dt date,
//    used_serial_nbr integer
//  ) extent size 32768 next size 16384 lock mode row;
//    revoke all on "math".sthomework from "public";
//
//
//    { TABLE "math".sthwqa row size = 138 number of columns = 10 index size = 34 }
//    create table "math".sthwqa
//            (
//                    serial_nbr integer not null ,
//                    question_nbr smallint not null ,
//                    answer_nbr smallint not null ,
//                    objective char(6) not null ,
//    stu_answer varchar(100) not null ,
//    stu_id char(9) not null ,
//    version char(20) not null ,
//    ans_correct char(1) not null ,
//    hw_dt date not null ,
//    finish_time integer
//  )  extent size 32768 next size 16384 lock mode row;
//    revoke all on "math".sthwqa from "public";
//
//
//    { TABLE "math".stpace_summary row size = 43 number of columns = 14 index size = 14
//    }
//    create table "math".stpace_summary
//            (
//                    stu_id char(9) not null ,
//    course char(10) not null ,
//    sect char(4) not null ,
//    term char(2) not null ,
//    term_yr smallint not null ,
//    i_in_progress char(1) not null ,
//    pace smallint not null ,
//    pace_track char(2) ,
//    pace_order smallint not null ,
//    ms_nbr smallint not null ,
//    ms_unit smallint not null ,
//    ms_date date not null ,
//    new_ms_date char(1),
//    exam_dt date not null ,
//    re_points smallint not null
//            ) lock mode row;
//    revoke all on "math".stpace_summary from "public";
//
//
//    { TABLE "math".testing_centers row size = 214 number of columns = 17 index size = 0 }
//    create table "math".testing_centers
//            (
//                    testing_center_id char(14) not null ,
//    tc_name char(40) not null ,
//    addres_1 char(35),
//    addres_2 char(35),
//    addres_3 char(35),
//    city char(18) not null ,
//    state char(2) not null ,
//    zip_code char(10) not null ,
//    active char(1) not null ,
//    dtime_created datetime year to second not null ,
//    dtime_approved datetime year to second,
//    dtime_denied datetime year to second,
//    dtime_revoked datetime year to second,
//    is_remote char(1) not null ,
//    is_proctored char(1) not null
//            ) extent size 8 next size 8 lock mode row;
//    revoke all on "math".testing_centers from "public";
//
//
//    { TABLE "math".resource row size = 93 number of columns = 5 index size = 16 }
//    create table "math".resource
//            (
//                    resource_id char(7) not null ,
//    resource_type char(2) not null ,
//    resource_desc char(80),
//    days_allowed smallint not null ,
//    holds_allowed smallint not null ,
//    hold_id char(2) not null
//            ) extent size 32 next size 8 lock mode row;
//    revoke all on "math".resource from "public";
//
//
//    { TABLE "math".stresource row size = 42 number of columns = 9 index size = 19 }
//    create table "math".stresource
//            (
//                    stu_id char(9) not null ,
//    resource_id char(7) not null ,
//    loan_dt date not null ,
//    start_time integer not null ,
//    due_dt date not null ,
//    return_dt date,
//    finish_time integer,
//    times_display smallint not null ,
//    create_dt date
//  ) extent size 128 next size 32 lock mode row;
//    revoke all on "math".stresource from "public";
//
//
//    { TABLE "math".stsurveyqa row size = 29 number of columns = 6 index size = 39 }
//    create table "math".stsurveyqa
//            (
//                    stu_id char(9) not null ,
//    version char(5) not null ,
//    exam_dt date not null ,
//    survey_nbr smallint not null ,
//    stu_answer char(50),
//    finish_time integer not null
//            ) extent size 512 next size 128 lock mode row;
//    revoke all on "math".stsurveyqa from "public";
//
//
//    { TABLE "math".stexam row size = 81 number of columns = 14 index size = 60 }
//    create table "math".stexam
//            (
//                    serial_nbr integer not null ,
//                    version char(5) not null ,
//    stu_id char(9) not null ,
//    exam_dt date not null,
//    exam_score smallint not null,
//    mastery_score smallint,
//    start_time integer not null,
//    finish_time integer not null,
//    time_ok char(1) not null,
//    passed char(1) not null,
//    seq_nbr smallint,
//    course char(10) not null ,
//    unit smallint not null ,
//    exam_type char(2) not null ,
//    is_first_passed char(1),
//    exam_source char(2),
//    calc_nbr char(7)
//  ) extent size 16384 next size 1024 lock mode row;
//    revoke all on "math".stexam from "public";
//
//
//    { TABLE "math".stetext row size = 111 number of columns = 8 index size = 14 }
//    create table "math".stetext
//            (
//                    stu_id char(9) not null ,
//    etext_id char(6) not null ,
//    active_dt date not null ,
//    etext_key char(20),
//    expiration_dt date,
//    refund_deadline_dt date,
//    refund_dt date,
//    refund_reason char(60)
//  ) lock mode row;
//    revoke all on "math".stetext from "public";
//
//
//    { TABLE "math".stcourse row size = 64 number of columns = 27 index size = 40 }
//    create table "math".stcourse
//            (
//                    stu_id char(9) not null ,
//    course char(10) not null ,
//    sect char(4) not null ,
//    term char(2) not null ,
//    term_yr smallint not null ,
//    pace_order smallint,
//    open_status char(1),
//    grading_option char(2),
//    completed char(1) not null ,
//    score smallint,
//    course_grade char(2),
//    prereq_satis char(1),
//    init_class_roll char(1) not null ,
//    stu_provided char(1),
//    final_class_roll char(1) not null ,
//    exam_placed char(1),
//    zero_unit smallint,
//    timeout_factor decimal(3,2),
//    forfeit_i char(1),
//    i_in_progress char(1) not null ,
//    i_counted char(1),
//    ctrl_test char(1) not null ,
//    deferred_f_dt date,
//    bypass_timeout smallint not null ,
//    instrn_type char(2),
//    registration_status char(2),
//    last_class_roll_dt date,
//    i_term char(2),
//    i_term_yr smallint,
//    i_deadline_dt date
//  ) extent size 256 next size 64 lock mode row;
//    revoke all on "math".stcourse from "public";
//
//    create cluster index "math".i_stcourse on "math".stcourse (stu_id,
//                                                               term,term_yr,course,sect);
//
//
//
//    { TABLE "math".stchallenge row size = 111 number of columns = 19 index size = 14 }
//    create table "math".stchallenge
//            (
//                    stu_id char(9) not null ,
//    course char(10) not null ,
//    version char(5) not null ,
//    academic_yr char(4) not null ,
//    exam_dt date not null ,
//    start_time integer,
//    finish_time integer not null ,
//    last_name char(30),
//    first_name char(30),
//    middle_initial char(1),
//    seq_nbr smallint,
//    serial_nbr integer,
//    score smallint,
//    passed char(1) not null ,
//    how_validated char(1)
//  ) lock mode row;
//    revoke all on "math".stchallenge from "public";
//
//
//    { TABLE "math".stmpe row size = 111 number of columns = 19 index size = 14 }
//    create table "math".stmpe
//            (
//                    stu_id char(9) not null ,
//    version char(5) not null ,
//    academic_yr char(4) not null ,
//    exam_dt date not null ,
//    start_time integer,
//    finish_time integer not null ,
//    last_name char(30),
//    first_name char(30),
//    middle_initial char(1),
//    seq_nbr smallint,
//    serial_nbr integer,
//    sts_a smallint,
//    sts_117 smallint,
//    sts_118 smallint,
//    sts_124 smallint,
//    sts_125 smallint,
//    sts_126 smallint,
//    placed char(1) not null ,
//    how_validated char(1)
//  ) lock mode row;
//    revoke all on "math".stmpe from "public";
//
//
//    create table "math".stmsg
//            (
//                    stu_id char(9) not null,
//    msg_dt date not null,
//    pace smallint,
//    course_index smallint,
//    touch_point char(3) not null ,
//    msg_code char(8) not null ,
//    sender char(50)
//  ) lock mode row;
//    revoke all on "math".stmsg from "public";
//
//
//
//    { TABLE "math".stqa row size = 31 number of columns = 8 index size = 34 }
//    create table "math".stqa
//            (
//                    serial_nbr integer not null,
//                    question_nbr smallint not null,
//                    answer_nbr smallint,
//                    objective char(10),
//    stu_answer varchar(100),
//    stu_id char(9) not null ,
//    version char(5) not null ,
//    ans_correct char(1),
//    exam_dt date,
//    subtest char(1),
//    finish_time integer
//  ) extent size 65536 next size 16384 lock mode row;
//    revoke all on "math".stqa from "public";
//
//
//    { TABLE "math".stchallengeqa row size = 33 number of columns = 8 index size = 22 }
//    create table "math".stchallengeqa
//            (
//                    stu_id char(9) not null ,
//    course char(10) not null ,
//    version char(5) not null ,
//    exam_dt date,
//    finish_time integer,
//    question_nbr smallint,
//    stu_answer char(5),
//    ans_correct char(1)
//  ) extent size 512 next size 256 lock mode row;
//    revoke all on "math".stchallengeqa from "public";
//
//
//    { TABLE "math".stmpeqa row size = 33 number of columns = 8 index size = 22 }
//    create table "math".stmpeqa
//            (
//                    stu_id char(9) not null ,
//    version char(5) not null ,
//    exam_dt date,
//    finish_time integer,
//    question_nbr smallint,
//    stu_answer char(5),
//    ans_correct char(1),
//    subtest char(30),
//    tree_ref char(40)
//  ) extent size 512 next size 256 lock mode row;
//    revoke all on "math".stmpeqa from "public";
//
//
//    { The tables below are designed to help with managing the DEADLINE schedule }
//
//
//    { TABLE "math".milestone row size = 17 number of columns = 7 index size = 0 }
//    create table "math".milestone
//            (
//                    term char(2) not null ,
//    term_yr smallint not null ,
//    pace smallint not null ,
//    pace_track char(2) not null ,
//    ms_nbr smallint not null ,
//    ms_type char(3) not null ,
//    ms_date date not null ,
//    nbr_atmpts_allow smallint
//  ) lock mode row;
//    revoke all on "math".milestone from "public" as "math";
//
//
//    { TABLE "math".stmilestone row size = 20 number of columns = 5 index size = 14 }
//    create table "math".stmilestone
//            (
//                    stu_id char(9) not null ,
//    term char(2) not null ,
//    term_yr smallint not null ,
//    pace_track char(2) not null ,
//    ms_nbr smallint not null ,
//    ms_type char(3) not null ,
//    ms_date date not null ,
//    nbr_atmpts_allow smallint ,
//    ext_type char(3)
//  ) lock mode row;
//    revoke all on "math".stmilestone from "public" as "math";
//    create index "math".i_stmile on "math".stmilestone (stu_id) using btree ;
//
//
//    { TABLE "math".pace_track_rule row size = 66 number of columns = 4 index size = 0 }
//    create table "math".pace_track_rule
//            (
//                    term char(2) not null ,
//    term_yr smallint not null ,
//    subterm char(4) not null ,
//    pace smallint not null ,
//    pace_track char(2) not null ,
//    criteria char(30) not null
//            ) lock mode row;
//    revoke all on "math".pace_track_rule from "public" as "math";
//
//
//    { TABLE "math".stterm row size = 62 number of columns = 11 index size = 18 }
//    create table "math".stterm
//            (
//                    stu_id char(9) not null ,
//    term char(2) not null ,
//    term_yr smallint not null ,
//    pace smallint not null ,
//    pace_track char(2) not null ,
//    first_course char(10) not null ,
//    cohort char(8) ,
//    urgency smallint ,
//    canvas_id char(8) ,
//    case_mgr char(20) ,
//    do_not_disturb char(1)
//  ) lock mode row;
//    revoke all on "math".stterm from "public" as "math";
//    create index "math".i_stterm on "math".stterm (stu_id) using btree;
//

    /**
     * Sets permissions on a table.
     *
     * @param tableName the table name
     */
    private boolean setPermissions(final String tableName) {

        Log.info("Setting permissions on '", tableName, "' table...");

        boolean ok = true;

        if (!DEBUG) {
            final String sql = SimpleBuilder.concat("REVOKE ALL ON math.", tableName, " FROM public AS math");

            try (final Statement stmt = this.archiveConnection.createStatement()) {
                stmt.executeUpdate(sql);
            } catch (final SQLException ex) {
                Log.warning("Failed to revoke public permissions on '", tableName, "' table in archive database");
                ok = false;
            }
        }

        return ok;
    }

    /**
     * Main method to execute the process.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final ContextMap map = ContextMap.getDefaultInstance();
        final DbProfile profile = map.getCodeProfile("SM24");
        final DbContext ctx = profile.getDbContext(ESchemaUse.PRIMARY);

        final DbConnection conn = ctx.checkOutConnection();

        try {
            final EOSCreateArchiveTables job = new EOSCreateArchiveTables(conn);
            job.run();
        } finally {
            ctx.checkInConnection(conn);
        }
    }
}

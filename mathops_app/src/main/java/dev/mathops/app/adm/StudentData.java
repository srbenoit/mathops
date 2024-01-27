package dev.mathops.app.adm;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbUtils;
import dev.mathops.db.type.TermKey;
import dev.mathops.db.enums.EDisciplineActionType;
import dev.mathops.db.enums.EDisciplineIncidentType;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawChallengeFeeLogic;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawFfrTrnsLogic;
import dev.mathops.db.old.rawlogic.RawMilestoneLogic;
import dev.mathops.db.old.rawlogic.RawMpeCreditLogic;
import dev.mathops.db.old.rawlogic.RawPaceAppealsLogic;
import dev.mathops.db.old.rawlogic.RawPlcFeeLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStchallengeLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.old.rawlogic.RawSthomeworkLogic;
import dev.mathops.db.old.rawlogic.RawStmilestoneLogic;
import dev.mathops.db.old.rawlogic.RawStmpeLogic;
import dev.mathops.db.old.rawlogic.RawStqaLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawChallengeFee;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawDiscipline;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawMilestone;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawPaceAppeals;
import dev.mathops.db.old.rawrecord.RawPlcFee;
import dev.mathops.db.old.rawrecord.RawSpecialStus;
import dev.mathops.db.old.rawrecord.RawStchallenge;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStexam;
import dev.mathops.db.old.rawrecord.RawSthomework;
import dev.mathops.db.old.rawrecord.RawStmilestone;
import dev.mathops.db.old.rawrecord.RawStmpe;
import dev.mathops.db.old.rawrecord.RawStqa;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A container for all data needed by the admin app to populate displays. This is queried once when a student is picked,
 * and then is used to update all tabs, and to support creation of a printed report if needed.
 */
@Deprecated
public final class StudentData {

    /** A commonly-used string. */
    private static final String F_COURSE = "course";

    /** A commonly-used string. */
    private static final String F_UNIT = "unit";

    /** The active term key. */
    public final TermKey activeKey;

    /** The student record. */
    public final RawStudent student;

    /** The student record. */
    public final RawStterm studentTerm;

    /** All holds on the student account. */
    public final List<RawAdminHold> studentHolds;

    /** All milestones for the student's pace and track. */
    public final List<RawMilestone> milestones;

    /** All student milestone overrides for the student. */
    public final List<RawStmilestone> studentMilestones;

    /** All student course records (including OT credit, but not dropped courses). */
    public final List<RawStcourse> studentCoursesPastAndCurrent;

    /** Course section records for the current-term. */
    public final List<RawCsection> currentTermCourseSections;

    /** Recorded student visits. */
    public final List<RawFfrTrns> studentTransferCredit;

    /** All student exams on record. */
    public final List<RawStexam> studentExams;

    /** Answers for all student exams on record. */
    public final List<RawStqa> studentExamAnswers;

    /** All student homeworks on record. */
    public final List<RawSthomework> studentHomeworks;

    /** All student placement attempts on record. */
    public final List<RawStmpe> studentPlacementAttempts;

    /** All student challenge attempts on record. */
    public final List<RawStchallenge> studentChallengeAttempts;

    /** All placement credit on the student's record. */
    public final List<RawMpeCredit> studentPlacementCredit;

    /** All discipline records for the student. */
    public final List<RawDiscipline> studentDisciplines;

    /** All pace appeals for the student. */
    public final List<RawPaceAppeals> paceAppeals;

    /** Special categories to which the student belongs. */
    public final List<RawSpecialStus> studentCategories;

    /** Placement fee assessed. */
    public final RawPlcFee placementFee;

    /** Challenge fees assessed. */
    public final List<RawChallengeFee> challengeFees;

    /** Error messages accumulated while gathering data. */
    private final List<String> errorMessages;

    /**
     * Constructs a new {@code StudentData} object, populating all fields.
     *
     * @param cache      the data cache
     * @param fixed      the fixed data
     * @param theStudent the student record
     * @throws SQLException if there is an error reading from the database
     */
    public StudentData(final Cache cache, final FixedData fixed, final RawStudent theStudent) throws SQLException {

        this.errorMessages = new ArrayList<>(10);

        this.student = theStudent;
        final String stuId = this.student.stuId;

        this.activeKey = fixed.activeTerm.term;

        this.studentTerm = RawSttermLogic.query(cache, this.activeKey, stuId);

        this.milestones = this.studentTerm == null ? new ArrayList<>(0)
                : RawMilestoneLogic.getAllMilestones(cache, this.activeKey,
                this.studentTerm.pace.intValue(), this.studentTerm.paceTrack);

        this.studentHolds = RawAdminHoldLogic.queryByStudent(cache, stuId);

        this.studentMilestones = this.studentTerm == null ? new ArrayList<>(0) : RawStmilestoneLogic
                .getStudentMilestones(cache, this.activeKey, this.studentTerm.paceTrack, stuId);

        this.studentCoursesPastAndCurrent =
                RawStcourseLogic.queryByStudent(cache, stuId, true, true);
        this.currentTermCourseSections = RawCsectionLogic.queryByTerm(cache, this.activeKey);

        this.studentTransferCredit = RawFfrTrnsLogic.queryByStudent(cache, stuId);

        this.studentExams = RawStexamLogic.queryByStudent(cache, stuId, true);
        this.studentExams.sort(new RawStexam.FinishDateTimeComparator());
        this.studentExamAnswers = RawStqaLogic.queryByStudent(cache, stuId);

        this.studentHomeworks = RawSthomeworkLogic.queryByStudent(cache, stuId, true);
        this.studentHomeworks.sort(new RawSthomework.FinishDateTimeComparator());

        this.studentPlacementAttempts = RawStmpeLogic.queryLegalByStudent(cache, stuId);
        this.studentPlacementAttempts.sort(new RawStmpe.FinishDateTimeComparator());
        this.studentPlacementCredit = RawMpeCreditLogic.queryByStudent(cache, stuId);
        this.studentChallengeAttempts = RawStchallengeLogic.queryByStudent(cache, stuId);

        this.studentDisciplines = loadStudentDisciplines(cache.conn, fixed);
        this.paceAppeals = RawPaceAppealsLogic.queryByStudent(cache, stuId);

        this.studentCategories = RawSpecialStusLogic.queryByStudent(cache, stuId);

        this.placementFee = RawPlcFeeLogic.queryByStudent(cache, stuId);
        this.challengeFees = RawChallengeFeeLogic.queryByStudent(cache, stuId);
    }

    /**
     * Loads all "discipline" records for a student.
     *
     * @param conn  the database connection
     * @param fixed the fixed data
     * @return the list of discipline records
     */
    private List<RawDiscipline> loadStudentDisciplines(final DbConnection conn, final FixedData fixed) {

        final List<RawDiscipline> result = new ArrayList<>(10);

        if (fixed.getClearanceLevel("STU_DISCP") != null) {
            final String stuId = this.student.stuId;

            final String sql = "SELECT * FROM discipline WHERE stu_id='" + stuId + "'";

            try (final Statement s = conn.createStatement()) {
                try (final ResultSet rs = s.executeQuery(sql)) {
                    while (rs.next()) {
                        try {
                            final LocalDate date = DbUtils.getDate(rs, "dt_incident");
                            final String type = DbUtils.getString(rs, "incident_type");
                            final String course = DbUtils.getString(rs, F_COURSE);
                            final Integer unit = DbUtils.getInteger(rs, F_UNIT);
                            final String desc = DbUtils.getString(rs, "cheat_desc");
                            final String action = DbUtils.getString(rs, "action_type");
                            final String comment = DbUtils.getString(rs, "action_comment");
                            final String interviewer = DbUtils.getString(rs, "interviewer");
                            final String proctor = DbUtils.getString(rs, "proctor");

                            EDisciplineIncidentType incid = EDisciplineIncidentType.forCode(type);
                            if (incid == null) {
                                Log.warning("Invalid discipline incident type code: ", type);
                                incid = EDisciplineIncidentType.OTHER;
                            }

                            EDisciplineActionType act = EDisciplineActionType.forCode(action);
                            if (act == null) {
                                Log.warning("Invalid discipline action type code: ", action);
                                act = EDisciplineActionType.OTHER;
                            }

                            result.add(new RawDiscipline(stuId, date, incid.code, course, unit, desc, act.code,
                                    comment, interviewer, proctor));

                        } catch (final IllegalArgumentException ex) {
                            Log.warning(ex);
                            this.errorMessages.add("Invalid discipline record: " + ex.getMessage());
                        }
                    }
                }
            } catch (final SQLException ex) {
                Log.warning(ex);
                this.errorMessages.add("Unable to query 'discipline' table: " + ex.getMessage());
            }
        }

        return result;
    }
}

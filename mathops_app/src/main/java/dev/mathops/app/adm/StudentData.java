package dev.mathops.app.adm;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.schema.DbUtils;
import dev.mathops.db.schema.ESchema;
import dev.mathops.db.field.EDisciplineActionType;
import dev.mathops.db.field.EDisciplineIncidentType;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.schema.legacy.impl.RawAdminHoldLogic;
import dev.mathops.db.schema.legacy.impl.RawChallengeFeeLogic;
import dev.mathops.db.schema.legacy.impl.RawFfrTrnsLogic;
import dev.mathops.db.schema.legacy.impl.RawMilestoneAppealLogic;
import dev.mathops.db.schema.legacy.impl.RawMpeCreditLogic;
import dev.mathops.db.schema.legacy.impl.RawPaceAppealsLogic;
import dev.mathops.db.schema.legacy.impl.RawPlcFeeLogic;
import dev.mathops.db.schema.legacy.impl.RawSpecialStusLogic;
import dev.mathops.db.schema.legacy.impl.RawStchallengeLogic;
import dev.mathops.db.schema.legacy.impl.RawStcourseLogic;
import dev.mathops.db.schema.legacy.impl.RawStexamLogic;
import dev.mathops.db.schema.legacy.impl.RawSthomeworkLogic;
import dev.mathops.db.schema.legacy.impl.RawStmilestoneLogic;
import dev.mathops.db.schema.legacy.impl.RawStmpeLogic;
import dev.mathops.db.schema.legacy.impl.RawStqaLogic;
import dev.mathops.db.schema.legacy.impl.RawSttermLogic;
import dev.mathops.db.schema.legacy.impl.RawStudentLogic;
import dev.mathops.db.schema.legacy.rec.RawAdminHold;
import dev.mathops.db.schema.legacy.rec.RawChallengeFee;
import dev.mathops.db.schema.legacy.rec.RawCsection;
import dev.mathops.db.schema.legacy.rec.RawDiscipline;
import dev.mathops.db.schema.legacy.rec.RawFfrTrns;
import dev.mathops.db.schema.legacy.rec.RawMilestone;
import dev.mathops.db.schema.legacy.rec.RawMilestoneAppeal;
import dev.mathops.db.schema.legacy.rec.RawMpeCredit;
import dev.mathops.db.schema.legacy.rec.RawPaceAppeals;
import dev.mathops.db.schema.legacy.rec.RawPlcFee;
import dev.mathops.db.schema.legacy.rec.RawSpecialStus;
import dev.mathops.db.schema.legacy.rec.RawStchallenge;
import dev.mathops.db.schema.legacy.rec.RawStcourse;
import dev.mathops.db.schema.legacy.rec.RawStexam;
import dev.mathops.db.schema.legacy.rec.RawSthomework;
import dev.mathops.db.schema.legacy.rec.RawStmilestone;
import dev.mathops.db.schema.legacy.rec.RawStmpe;
import dev.mathops.db.schema.legacy.rec.RawStqa;
import dev.mathops.db.schema.legacy.rec.RawStterm;
import dev.mathops.db.schema.legacy.rec.RawStudent;
import dev.mathops.db.field.TermKey;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A container for all data needed by the admin app to populate displays. This is queried once when a student is picked,
 * and then is used to update all tabs, and to support creation of a printed report if needed.
 */
public final class StudentData {

    /** A commonly-used string. */
    private static final String F_COURSE = "course";

    /** A commonly-used string. */
    private static final String F_UNIT = "unit";

    /** The active term key. */
    public final TermKey activeKey;

    /** The student record. */
    public RawStudent student;

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

    /** The registrations that participate in the N-course pace, in order, with non-null pace_order. */
    public final List<RawStcourse> pacedRegistrations;

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

    /** All milestoneappeals for the student. */
    public final List<RawMilestoneAppeal> milestoneAppeals;

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
    public StudentData(final Cache cache, final UserData fixed, final RawStudent theStudent) throws SQLException {

        this.errorMessages = new ArrayList<>(10);

        this.student = theStudent;
        final String stuId = this.student.stuId;

        final SystemData systemData = cache.getSystemData();

        this.activeKey = systemData.getActiveTerm().term;

        this.studentTerm = RawSttermLogic.query(cache, this.activeKey, stuId);

        this.milestones = this.studentTerm == null ? new ArrayList<>(0)
                : systemData.getMilestones(this.activeKey, this.studentTerm.pace, this.studentTerm.paceTrack);

        this.studentHolds = RawAdminHoldLogic.queryByStudent(cache, stuId);

        this.studentMilestones = this.studentTerm == null ? new ArrayList<>(0) : RawStmilestoneLogic
                .getStudentMilestones(cache, this.activeKey, this.studentTerm.paceTrack, stuId);
        this.studentMilestones.sort(null);

        this.studentCoursesPastAndCurrent = RawStcourseLogic.queryByStudent(cache, stuId, true, true);
        this.currentTermCourseSections = systemData.getCourseSections(this.activeKey);

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

        this.studentDisciplines = loadStudentDisciplines(cache, fixed);
        this.paceAppeals = RawPaceAppealsLogic.queryByStudent(cache, stuId);
        this.milestoneAppeals = RawMilestoneAppealLogic.queryByStudent(cache, stuId);

        this.studentCategories = RawSpecialStusLogic.queryByStudent(cache, stuId);

        this.placementFee = RawPlcFeeLogic.queryByStudent(cache, stuId);
        this.challengeFees = RawChallengeFeeLogic.queryByStudent(cache, stuId);

        this.pacedRegistrations = this.studentTerm == null ? new ArrayList<>(0) : organizeRegistrations();
    }

    /**
     * Organizes course registrations into an ordered list with pace order assigned to each registration.
     *
     * @return the organized list of registrations
     */
    private List<RawStcourse> organizeRegistrations() {

        final List<RawStcourse> regs = new ArrayList<>(this.studentCoursesPastAndCurrent);

        // Remove any that are dropped, not in the current term, or a non-counted Incomplete
        regs.removeIf(test -> "D".equals(test.openStatus) || !test.termKey.equals(this.studentTerm.termKey)
                              || ("Y".equals(test.iInProgress) && "N".equals(test.iCounted)));
        final int numRegs = regs.size();

        // Assign pace order if any regs do not yet have a pace order
        final List<RawStcourse> toassign = new ArrayList<>(numRegs);
        final List<Integer> orders = new ArrayList<>(numRegs);

        for (int i = 1; i <= numRegs; ++i) {
            orders.add(Integer.valueOf(i));
        }

        for (final RawStcourse reg : regs) {
            final Integer order = reg.paceOrder;
            if (order == null) {
                toassign.add(reg);
            } else if (order.intValue() >= numRegs) {
                reg.paceOrder = null;
                toassign.add(reg);
            } else {
                orders.remove(order);
            }
        }

        // At this point, "toassign" has all the registrations that need a pace order, and "orders" has the ordered list
        // of unassigned order numbers.

        if (!toassign.isEmpty()) {
            // Sort courses to be assigned by course ID as a default ordering.
            Collections.sort(toassign);
            for (final RawStcourse row : toassign) {
                row.paceOrder = orders.removeFirst();
            }
        }

        // At this point, all courses in "regs" have a pace order, but they may not be in the proper order

        final SortedMap<Integer, RawStcourse> sorted = new TreeMap<>();
        for (final RawStcourse reg : regs) {
            sorted.put(reg.paceOrder, reg);
        }

        regs.clear();
        regs.addAll(sorted.values());

        return regs;
    }

    /**
     * Loads all "discipline" records for a student.
     *
     * @param cache the data cache
     * @param fixed the fixed data
     * @return the list of discipline records
     */
    private List<RawDiscipline> loadStudentDisciplines(final Cache cache, final UserData fixed) {

        final List<RawDiscipline> result = new ArrayList<>(10);

        if (fixed.getClearanceLevel("STU_DISCP") != null) {
            final String stuId = this.student.stuId;

            final String sql = "SELECT * FROM discipline WHERE stu_id='" + stuId + "'";

            final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

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
            } finally {
                Cache.checkInConnection(conn);
            }
        }

        return result;
    }

    /**
     * Called when the student record has been altered.
     *
     * @param cache the data cache
     */
    public void updateStudent(final Cache cache) {

        final String stuId = this.student.stuId;

        try {
            final RawStudent newStu = RawStudentLogic.query(cache, stuId, false);
            this.student = newStu;
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Called when pace appeal or milestone appeal data is altered.
     *
     * @param cache the data cache
     */
    public void updatePaceAppeals(final Cache cache) {

        final String stuId = this.student.stuId;

        try {
            final List<RawPaceAppeals> newPaceAppeals = RawPaceAppealsLogic.queryByStudent(cache, stuId);
            this.paceAppeals.clear();
            this.paceAppeals.addAll(newPaceAppeals);

            final List<RawMilestoneAppeal> newMilestoneAppeals = RawMilestoneAppealLogic.queryByStudent(cache, stuId);
            this.milestoneAppeals.clear();
            this.milestoneAppeals.addAll(newMilestoneAppeals);

            final List<RawStmilestone> newMilestones = RawStmilestoneLogic.getStudentMilestones(cache, this.activeKey,
                    stuId);
            this.studentMilestones.clear();
            this.studentMilestones.addAll(newMilestones);
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }

    /**
     * Called when the transfer credit data is altered.
     *
     * @param cache the data cache
     */
    public void updateTransferCreditList(final Cache cache) {

        final String stuId = this.student.stuId;

        try {
            final List<RawFfrTrns> newList = RawFfrTrnsLogic.queryByStudent(cache, stuId);
            this.studentTransferCredit.clear();
            this.studentTransferCredit.addAll(newList);
        } catch (final SQLException ex) {
            Log.warning(ex);
        }
    }
}

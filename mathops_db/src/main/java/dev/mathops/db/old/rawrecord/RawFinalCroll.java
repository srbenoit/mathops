package dev.mathops.db.old.rawrecord;

import dev.mathops.db.type.TermKey;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A raw "final_croll" record.
 */
public final class RawFinalCroll extends RawTermRecordBase implements Comparable<RawFinalCroll> {

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_COURSE = "course";

    /** A field name. */
    private static final String FLD_SECT = "sect";

    /** A field name. */
    private static final String FLD_PACE_ORDER = "pace_order";

    /** A field name. */
    private static final String FLD_OPEN_STATUS = "open_status";

    /** A field name. */
    private static final String FLD_GRADING_OPTION = "grading_option";

    /** A field name. */
    private static final String FLD_COMPLETED = "completed";

    /** A field name. */
    private static final String FLD_SCORE = "score";

    /** A field name. */
    private static final String FLD_COURSE_GRADE = "course_grade";

    /** A field name. */
    private static final String FLD_PREREQ_SATIS = "prereq_satis";

    /** A field name. */
    private static final String FLD_INIT_CLASS_ROLL = "init_class_roll";

    /** A field name. */
    private static final String FLD_STU_PROVIDED = "stu_provided";

    /** A field name. */
    private static final String FLD_FINAL_CLASS_ROLL = "final_class_roll";

    /** A field name. */
    private static final String FLD_EXAM_PLACED = "exam_placed";

    /** A field name. */
    private static final String FLD_ZERO_UNIT = "zero_unit";

    /** A field name. */
    private static final String FLD_TIMEOUT_FACTOR = "timeout_factor";

    /** A field name. */
    private static final String FLD_FORFEIT_I = "forfeit_i";

    /** A field name. */
    private static final String FLD_I_IN_PROGRESS = "i_in_progress";

    /** A field name. */
    private static final String FLD_I_COUNTED = "i_counted";

    /** A field name. */
    private static final String FLD_CTRL_TEST = "ctrl_test";

    /** A field name. */
    private static final String FLD_DEFERRED_F_DT = "deferred_f_dt";

    /** A field name. */
    private static final String FLD_BYPASS_TIMEOUT = "bypass_timeout";

    /** A field name. */
    private static final String FLD_INSTRN_TYPE = "instrn_type";

    /** A field name. */
    private static final String FLD_REGISTRATION_STATUS = "registration_status";

    /** A field name. */
    private static final String FLD_LAST_CLASS_ROLL_DT = "last_class_roll_dt";

    /** A field name. */
    private static final String FLD_I_TERM = "i_term";

    /** A field name. */
    private static final String FLD_I_TERM_YR = "i_term_yr";

    /** A field name. */
    private static final String FLD_I_DEADLINE_DT = "i_deadline_dt";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'course' field value. */
    public String course;

    /** The 'sect' field value. */
    public String sect;

    /** The 'pace_order' field value. */
    public Integer paceOrder;

    /** The 'open_status' field value. */
    public String openStatus;

    /** The 'grading_option' field value. */
    public String gradingOption;

    /** The 'completed' field value. */
    public String completed;

    /** The 'score' field value. */
    public Integer score;

    /** The 'course_grade' field value. */
    public String courseGrade;

    /**
     * The 'prereq_satis' field value ("Y" if yes, "N" if no, "P" if provisionally yes - this is used for CSU Online
     * sections who are not required to satisfy the prereq for MATH 117, but are given a longer Skills Review if they
     * have not).
     */
    public String prereqSatis;

    /** The 'init_class_roll' field value. */
    public String initClassRoll;

    /** The 'stu_provided' field value. */
    public String stuProvided;

    /** The 'final_class_roll' field value. */
    public String finalClassRoll;

    /** The 'exam_placed' field value. */
    public String examPlaced;

    /** The 'zero_unit' field value. */
    public Integer zeroUnit;

    /** The 'timeout_factor' field value. */
    public Float timeoutFactor;

    /** The 'forfeit_i' field value. */
    public String forfeitI;

    /** The 'i_in_progress' field value. */
    public String iInProgress;

    /** The 'i_counted' field value. */
    public String iCounted;

    /** The 'ctrl_test' field value. */
    public String ctrlTest;

    /** The 'deferred_f_dt' field value. */
    public LocalDate deferredFDt;

    /** The 'bypass_timeout' field value. */
    public Integer bypassTimeout;

    /** The 'instrn_type' field value. */
    public String instrnType;

    /** The 'registration_status' field value. */
    public String registrationStatus;

    /** The 'last_class_roll_dt' field value. */
    public LocalDate lastClassRollDt;

    /** The 'i_term' and 'i_term_yr' field values. */
    public TermKey iTermKey;

    /** The 'i_deadline_dt' field value. */
    public LocalDate iDeadlineDt;

    /**
     * Constructs a new {@code RawFinalCroll}.
     */
    private RawFinalCroll() {

        super();
    }

    /**
     * Constructs a new {@code RawFinalCroll}.
     *
     * @param theTermKey            the term key
     * @param theStuId              The 'stu_id' field value
     * @param theCourse             the 'course' field value
     * @param theSect               the 'sect' field value
     * @param thePaceOrder          the 'pace_order' field value
     * @param theOpenStatus         the 'open_status' field value
     * @param theGradingOption      the 'grading_option' field value
     * @param theCompleted          the 'completed' field value
     * @param theScore              the 'score' field value
     * @param theCourseGrade        the 'course_grade' field value
     * @param thePrereqSatis        the 'prereq_satis' field value
     * @param theInitClassRoll      the 'init_class_roll' field value
     * @param theStuProvided        the 'stu_provided' field value
     * @param theFinalClassRoll     the 'final_class_roll' field value
     * @param theExamPlaced         the 'exam_placed' field value
     * @param theZeroUnit           the 'zero_unit' field value
     * @param theTimeoutFactor      the 'timeout_factor' field value
     * @param theForfeitI           the 'forfeit_i' field value
     * @param theIInProgress        the 'i_in_progress' field value
     * @param theICounted           the 'i_counted' field value
     * @param theCtrlTest           the 'ctrl_test' field value
     * @param theDeferredFDt        the 'deferred_f_dt' field value
     * @param theBypassTimeout      the 'bypass_timeout' field value
     * @param theInstrnType         the 'instrn_type' field value
     * @param theRegistrationStatus the 'registration_status' field value
     * @param theLastClassRollDt    the 'last_class_roll_dt' field value
     * @param theITermKey           the 'i_term' and 'i_term_yr' field values
     * @param theIDeadlineDt        the 'i_deadline_dt' field value
     */
    public RawFinalCroll(final TermKey theTermKey, final String theStuId, final String theCourse,
                         final String theSect, final Integer thePaceOrder, final String theOpenStatus,
                         final String theGradingOption, final String theCompleted, final Integer theScore,
                         final String theCourseGrade, final String thePrereqSatis, final String theInitClassRoll,
                         final String theStuProvided, final String theFinalClassRoll, final String theExamPlaced,
                         final Integer theZeroUnit, final Float theTimeoutFactor, final String theForfeitI,
                         final String theIInProgress, final String theICounted, final String theCtrlTest,
                         final LocalDate theDeferredFDt, final Integer theBypassTimeout, final String theInstrnType,
                         final String theRegistrationStatus, final LocalDate theLastClassRollDt,
                         final TermKey theITermKey, final LocalDate theIDeadlineDt) {

        super(theTermKey);

        this.stuId = theStuId;
        this.course = theCourse;
        this.sect = theSect;
        this.paceOrder = thePaceOrder;
        this.openStatus = theOpenStatus;
        this.gradingOption = theGradingOption;
        this.completed = theCompleted;
        this.score = theScore;
        this.courseGrade = theCourseGrade;
        this.prereqSatis = thePrereqSatis;
        this.initClassRoll = theInitClassRoll;
        this.stuProvided = theStuProvided;
        this.finalClassRoll = theFinalClassRoll;
        this.examPlaced = theExamPlaced;
        this.zeroUnit = theZeroUnit;
        this.timeoutFactor = theTimeoutFactor;
        this.forfeitI = theForfeitI;
        this.iInProgress = theIInProgress;
        this.iCounted = theICounted;
        this.ctrlTest = theCtrlTest;
        this.deferredFDt = theDeferredFDt;
        this.bypassTimeout = theBypassTimeout;
        this.instrnType = theInstrnType;
        this.registrationStatus = theRegistrationStatus;
        this.lastClassRollDt = theLastClassRollDt;
        this.iTermKey = theITermKey;
        this.iDeadlineDt = theIDeadlineDt;
    }

    /**
     * Extracts an "final_croll" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawFinalCroll fromResultSet(final ResultSet rs) throws SQLException {

        final RawFinalCroll result = new RawFinalCroll();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.course = getStringField(rs, FLD_COURSE);
        result.sect = getStringField(rs, FLD_SECT);
        result.termKey = getTermAndYear(rs, FLD_TERM, FLD_TERM_YR);
        try {
            result.paceOrder = getIntegerField(rs, FLD_PACE_ORDER);
        } catch (final SQLException ex) {
            result.paceOrder = null;
        }
        result.openStatus = getStringField(rs, FLD_OPEN_STATUS);
        result.gradingOption = getStringField(rs, FLD_GRADING_OPTION);
        result.completed = getStringField(rs, FLD_COMPLETED);
        result.score = getIntegerField(rs, FLD_SCORE);
        result.courseGrade = getStringField(rs, FLD_COURSE_GRADE);
        result.prereqSatis = getStringField(rs, FLD_PREREQ_SATIS);
        result.initClassRoll = getStringField(rs, FLD_INIT_CLASS_ROLL);
        result.stuProvided = getStringField(rs, FLD_STU_PROVIDED);
        result.finalClassRoll = getStringField(rs, FLD_FINAL_CLASS_ROLL);
        result.examPlaced = getStringField(rs, FLD_EXAM_PLACED);
        result.zeroUnit = getIntegerField(rs, FLD_ZERO_UNIT);
        result.timeoutFactor = getFloatField(rs, FLD_TIMEOUT_FACTOR);
        result.forfeitI = getStringField(rs, FLD_FORFEIT_I);
        result.iInProgress = getStringField(rs, FLD_I_IN_PROGRESS);
        try {
            result.iCounted = getStringField(rs, FLD_I_COUNTED);
        } catch (final SQLException ex) {
            result.iCounted = null;
        }
        result.ctrlTest = getStringField(rs, FLD_CTRL_TEST);
        result.deferredFDt = getDateField(rs, FLD_DEFERRED_F_DT);
        result.bypassTimeout = getIntegerField(rs, FLD_BYPASS_TIMEOUT);
        result.instrnType = getStringField(rs, FLD_INSTRN_TYPE);
        try {
            result.registrationStatus = getStringField(rs, FLD_REGISTRATION_STATUS);
        } catch (final SQLException ex) {
            result.registrationStatus = null;
        }
        result.lastClassRollDt = getDateField(rs, FLD_LAST_CLASS_ROLL_DT);
        result.iTermKey = getTermAndYear(rs, FLD_I_TERM, FLD_I_TERM_YR);
        result.iDeadlineDt = getDateField(rs, FLD_I_DEADLINE_DT);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawFinalCroll o) {

        int result = compareAllowingNull(this.termKey, o.termKey);

        if (result == 0) {
            result = compareAllowingNull(this.stuId, o.stuId);

            if (result == 0) {
                result = compareAllowingNull(this.course, o.course);

                if (result == 0) {
                    result = this.sect.compareTo(o.sect);

                    if (result == 0) {
                        result = compareAllowingNull(this.lastClassRollDt, o.lastClassRollDt);

                        if (result == 0) {
                            result = compareAllowingNull(this.iTermKey, o.iTermKey);

                            if (result == 0) {
                                result = compareAllowingNull(this.prereqSatis, o.prereqSatis);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(40);

        appendField(htm, FLD_TERM, this.termKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE, this.course);
        htm.add(DIVIDER);
        appendField(htm, FLD_SECT, this.sect);
        htm.add(DIVIDER);
        appendField(htm, FLD_PACE_ORDER, this.paceOrder);
        htm.add(DIVIDER);
        appendField(htm, FLD_OPEN_STATUS, this.openStatus);
        htm.add(DIVIDER);
        appendField(htm, FLD_GRADING_OPTION, this.gradingOption);
        htm.add(DIVIDER);
        appendField(htm, FLD_COMPLETED, this.completed);
        htm.add(DIVIDER);
        appendField(htm, FLD_SCORE, this.score);
        htm.add(DIVIDER);
        appendField(htm, FLD_COURSE_GRADE, this.courseGrade);
        htm.add(DIVIDER);
        appendField(htm, FLD_PREREQ_SATIS, this.prereqSatis);
        htm.add(DIVIDER);
        appendField(htm, FLD_INIT_CLASS_ROLL, this.initClassRoll);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_PROVIDED, this.stuProvided);
        htm.add(DIVIDER);
        appendField(htm, FLD_FINAL_CLASS_ROLL, this.finalClassRoll);
        htm.add(DIVIDER);
        appendField(htm, FLD_EXAM_PLACED, this.examPlaced);
        htm.add(DIVIDER);
        appendField(htm, FLD_ZERO_UNIT, this.zeroUnit);
        htm.add(DIVIDER);
        appendField(htm, FLD_TIMEOUT_FACTOR, this.timeoutFactor);
        htm.add(DIVIDER);
        appendField(htm, FLD_FORFEIT_I, this.forfeitI);
        htm.add(DIVIDER);
        appendField(htm, FLD_I_IN_PROGRESS, this.iInProgress);
        htm.add(DIVIDER);
        appendField(htm, FLD_I_COUNTED, this.iCounted);
        htm.add(DIVIDER);
        appendField(htm, FLD_CTRL_TEST, this.ctrlTest);
        htm.add(DIVIDER);
        appendField(htm, FLD_DEFERRED_F_DT, this.deferredFDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_BYPASS_TIMEOUT, this.bypassTimeout);
        htm.add(DIVIDER);
        appendField(htm, FLD_INSTRN_TYPE, this.instrnType);
        htm.add(DIVIDER);
        appendField(htm, FLD_REGISTRATION_STATUS, this.registrationStatus);
        htm.add(DIVIDER);
        appendField(htm, FLD_LAST_CLASS_ROLL_DT, this.lastClassRollDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_I_TERM, this.iTermKey);
        htm.add(DIVIDER);
        appendField(htm, FLD_I_DEADLINE_DT, this.iDeadlineDt);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.termKey)
                + Objects.hashCode(this.stuId)
                + Objects.hashCode(this.course)
                + Objects.hashCode(this.sect)
                + Objects.hashCode(this.paceOrder)
                + Objects.hashCode(this.openStatus)
                + Objects.hashCode(this.gradingOption)
                + Objects.hashCode(this.completed)
                + Objects.hashCode(this.score)
                + Objects.hashCode(this.courseGrade)
                + Objects.hashCode(this.prereqSatis)
                + Objects.hashCode(this.initClassRoll)
                + Objects.hashCode(this.stuProvided)
                + Objects.hashCode(this.finalClassRoll)
                + Objects.hashCode(this.examPlaced)
                + Objects.hashCode(this.zeroUnit)
                + Objects.hashCode(this.timeoutFactor)
                + Objects.hashCode(this.forfeitI)
                + Objects.hashCode(this.iInProgress)
                + Objects.hashCode(this.iCounted)
                + Objects.hashCode(this.ctrlTest)
                + Objects.hashCode(this.deferredFDt)
                + Objects.hashCode(this.bypassTimeout)
                + Objects.hashCode(this.instrnType)
                + Objects.hashCode(this.registrationStatus)
                + Objects.hashCode(this.lastClassRollDt)
                + Objects.hashCode(this.iTermKey)
                + Objects.hashCode(this.iDeadlineDt);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the other object
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final RawFinalCroll rec) {
            equal = Objects.equals(this.termKey, rec.termKey)
                    && Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.course, rec.course)
                    && Objects.equals(this.sect, rec.sect)
                    && Objects.equals(this.paceOrder, rec.paceOrder)
                    && Objects.equals(this.openStatus, rec.openStatus)
                    && Objects.equals(this.gradingOption, rec.gradingOption)
                    && Objects.equals(this.completed, rec.completed)
                    && Objects.equals(this.score, rec.score)
                    && Objects.equals(this.courseGrade, rec.courseGrade)
                    && Objects.equals(this.prereqSatis, rec.prereqSatis)
                    && Objects.equals(this.initClassRoll, rec.initClassRoll)
                    && Objects.equals(this.stuProvided, rec.stuProvided)
                    && Objects.equals(this.finalClassRoll, rec.finalClassRoll)
                    && Objects.equals(this.examPlaced, rec.examPlaced)
                    && Objects.equals(this.zeroUnit, rec.zeroUnit)
                    && Objects.equals(this.timeoutFactor, rec.timeoutFactor)
                    && Objects.equals(this.forfeitI, rec.forfeitI)
                    && Objects.equals(this.iInProgress, rec.iInProgress)
                    && Objects.equals(this.iCounted, rec.iCounted)
                    && Objects.equals(this.ctrlTest, rec.ctrlTest)
                    && Objects.equals(this.deferredFDt, rec.deferredFDt)
                    && Objects.equals(this.bypassTimeout, rec.bypassTimeout)
                    && Objects.equals(this.instrnType, rec.instrnType)
                    && Objects.equals(this.registrationStatus, rec.registrationStatus)
                    && Objects.equals(this.lastClassRollDt, rec.lastClassRollDt)
                    && Objects.equals(this.iTermKey, rec.iTermKey)
                    && Objects.equals(this.iDeadlineDt, rec.iDeadlineDt);
        } else {
            equal = false;
        }

        return equal;
    }
}

package dev.mathops.db.rec;

import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An immutable raw "rule_set" record.
 *
 * <p>
 * Every section of a course in a term specifies a rule set that governs how that section will operate.  Not all rule
 * sets are compatible, and a student may only register for combinations of sections whose rule sets are compatible.
 * To do otherwise will cause a hold to be placed on the student's account until the registration mismatch is
 * corrected.
 *
 * <p>
 * Rule sets are "compatible" if they share the same schedule source, maximum number of courses, number of courses
 * allowed to be open at once, and licensing requirement.
 */
public final class RuleSetRec extends RecBase implements Comparable<RuleSetRec> {

    /** A field name. */
    private static final String FLD_RULE_SET_ID = "rule_set_id";

    /** A field name. */
    private static final String FLD_RULE_SET_NAME = "rule_set_name";

    /** A field name. */
    private static final String FLD_SCHEDULE_SOURCE = "schedule_source";

    /** A field name. */
    private static final String FLD_MAX_COURSES = "max_courses";

    /** A field name. */
    private static final String FLD_NBR_OPEN_ALLOWED = "nbr_open_allowed";

    /** A field name. */
    private static final String FLD_REQUIRE_LICENSED = "require_licensed";

    /** A field name. */
    private static final String FLD_ALLOW_INC = "allow_inc";

    /** A field name. */
    private static final String FLD_INC_MIN_COMPLETED = "inc_min_units_completed";

    /** The unique (within a term) ID of the rule set.  A one-character string. */
    public final String ruleSetId;

    /** The friendly name of the rule set. */
    public final String ruleSetName;

    /** The source of schedule information: "pace" if based on the student's pace. */
    public final String scheduleSource;

    /**
     * The maximum number of courses that can contribute to the student's pace.  Incomplete courses from earlier
     * semesters that are configured not to contribute are not counted. */
    public final Integer maxCourses;

    /**
     * The maximum number of courses the student can be working on at the same time.  Setting this to N does not
     * preclude a student from having N courses open and still going back to do work in completed courses to
     * increase their score in those courses.
     */
    public final Integer nbrOpenAllowed;

    /** TRUE if the rule set requires the student to pass a licensing exam before entering any courses.  */
    public final Boolean requireLicensed;

    /** TRUE if the student can earn an Incomplete in a course that was started but not completed. */
    public final Boolean allowInc;

    /**
     * When the rule set permits Incompletes, this stores the minimum number of units/modules/lessons of a course that
     * must be completed in order to earn an Incomplete in that course.
     */
    public final Integer incMinCompleted;

    /**
     * Constructs a new {@code RuleSetRec}.
     *
     * @param theRuleSetId       the rule set ID (may not be {@code null})
     * @param theRuleSetName     the rule set name
     * @param theScheduleSource  the schedule source
     * @param theMaxCourses      the maximum number of courses that may contribute to pace (may not be {@code null})
     * @param theNbrOpenAllowed  the maximum number of courses open but not completed (may not be {@code null})
     * @param theRequireLicensed TRUE if student must pass licensing exam to access course (may not be {@code null})
     * @param theAllowInc        TRUE if students can earn an Incomplete in a course (may not be {@code null})
     * @param theIncMinCompleted the minimum number of units/modules/lessons of a course that must be completed in
     *                           order to earn an Incomplete in that course
     */
    public RuleSetRec(final String theRuleSetId, final String theRuleSetName, final String theScheduleSource,
                      final Integer theMaxCourses, final Integer theNbrOpenAllowed, final Boolean theRequireLicensed,
                      final Boolean theAllowInc, final Integer theIncMinCompleted) {

        super();

        if (theRuleSetId == null) {
            throw new IllegalArgumentException("Rule set ID may not be null");
        }
        if (theMaxCourses == null) {
            throw new IllegalArgumentException("Maximum courses may not be null");
        }
        if (theNbrOpenAllowed == null) {
            throw new IllegalArgumentException("Number open allowed may not be null");
        }
        if (theRequireLicensed == null) {
            throw new IllegalArgumentException("License requirement may not be null");
        }
        if (theAllowInc == null) {
            throw new IllegalArgumentException("Incomplete flag may not be null");
        }

        this.ruleSetId = theRuleSetId;
        this.ruleSetName = theRuleSetName;
        this.scheduleSource = theScheduleSource;
        this.maxCourses = theMaxCourses;
        this.nbrOpenAllowed = theNbrOpenAllowed;
        this.requireLicensed = theRequireLicensed;
        this.allowInc = theAllowInc;
        this.incMinCompleted = theIncMinCompleted;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RuleSetRec o) {

        return this.ruleSetId.compareTo(o.ruleSetId);
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

        appendField(htm, FLD_RULE_SET_ID, this.ruleSetId);
        htm.add(DIVIDER);
        appendField(htm, FLD_RULE_SET_NAME, this.ruleSetName);
        htm.add(DIVIDER);
        appendField(htm, FLD_SCHEDULE_SOURCE, this.scheduleSource);
        htm.add(DIVIDER);
        appendField(htm, FLD_MAX_COURSES, this.maxCourses);
        htm.add(DIVIDER);
        appendField(htm, FLD_NBR_OPEN_ALLOWED, this.nbrOpenAllowed);
        htm.add(DIVIDER);
        appendField(htm, FLD_REQUIRE_LICENSED, this.requireLicensed);
        htm.add(DIVIDER);
        appendField(htm, FLD_ALLOW_INC, this.allowInc);
        htm.add(DIVIDER);
        appendField(htm, FLD_INC_MIN_COMPLETED, this.incMinCompleted);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.ruleSetId.hashCode() + Objects.hashCode(this.ruleSetName)
                + Objects.hashCode(this.scheduleSource)
                + this.maxCourses.hashCode() + this.nbrOpenAllowed.hashCode()
                + this.requireLicensed.hashCode() + this.allowInc.hashCode()
                + Objects.hashCode(this.incMinCompleted);
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
        } else if (obj instanceof final RuleSetRec rec) {
            equal = this.ruleSetId.equals(rec.ruleSetId)
                    && Objects.equals(this.ruleSetName, rec.ruleSetName)
                    && Objects.equals(this.scheduleSource, rec.scheduleSource)
                    && this.maxCourses.equals(rec.maxCourses)
                    && this.nbrOpenAllowed.equals(rec.nbrOpenAllowed)
                    && this.requireLicensed.equals(rec.requireLicensed)
                    && this.allowInc.equals(rec.allowInc)
                    && Objects.equals(this.incMinCompleted, rec.incMinCompleted);
        } else {
            equal = false;
        }

        return equal;
    }
}

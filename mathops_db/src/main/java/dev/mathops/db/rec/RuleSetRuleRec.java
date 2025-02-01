package dev.mathops.db.rec;

import dev.mathops.text.builder.HtmlBuilder;

import java.util.Objects;

/**
 * An immutable raw "rule_set_rule" record.
 *
 * <p>
 * Each rule set may define any number of rules that govern what tasks a student must complete to gain access to
 * subsequent tasks.
 */
public final class RuleSetRuleRec extends RecBase implements Comparable<RuleSetRuleRec> {

    /** A field name. */
    private static final String FLD_RULE_SET_ID = "rule_set_id";

    /** A field name. */
    private static final String FLD_ACTIVITY_TYPE = "activity_type";

    /** A field name. */
    private static final String FLD_REQUIREMENT = "requirement";

    /** The unique (within a term) ID of the rule set.  A one-character string. */
    public final String ruleSetId;

    /** The activity to which the student wants to gain access. */
    public final String activityType;

    /**
     * The requirement the student needs to complete.  If there are multiple records for rhe same 'activityType',
     * the student must complete all of their requirements to gain access to that activity.
     */
    public final String requirement;

    /**
     * Constructs a new {@code RuleSetRuleRec}.
     *
     * @param theRuleSetId    the rule set ID (may not be {@code null})
     * @param theActivityType the activity to which the student wants to gain access (may not be {@code null})
     * @param theRequirement  the requirement the student needs to complete (may not be {@code null})
     */
    public RuleSetRuleRec(final String theRuleSetId, final String theActivityType, final String theRequirement) {

        super();

        if (theRuleSetId == null) {
            throw new IllegalArgumentException("Rule set ID may not be null");
        }
        if (theActivityType == null) {
            throw new IllegalArgumentException("Activity type may not be null");
        }
        if (theRequirement == null) {
            throw new IllegalArgumentException("Requirement may not be null");
        }

        this.ruleSetId = theRuleSetId;
        this.activityType = theActivityType;
        this.requirement = theRequirement;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RuleSetRuleRec o) {

        int result = this.ruleSetId.compareTo(o.ruleSetId);
        if (result == 0) {
            result = this.activityType.compareTo(o.activityType);
            if (result == 0) {
                result = this.requirement.compareTo(o.requirement);
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

        appendField(htm, FLD_RULE_SET_ID, this.ruleSetId);
        htm.add(DIVIDER);
        appendField(htm, FLD_ACTIVITY_TYPE, this.activityType);
        htm.add(DIVIDER);
        appendField(htm, FLD_REQUIREMENT, this.requirement);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.ruleSetId.hashCode() + this.activityType.hashCode() + this.requirement.hashCode();
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
        } else if (obj instanceof final RuleSetRuleRec rec) {
            equal = this.ruleSetId.equals(rec.ruleSetId)
                    && Objects.equals(this.activityType, rec.activityType)
                    && Objects.equals(this.requirement, rec.requirement);
        } else {
            equal = false;
        }

        return equal;
    }
}

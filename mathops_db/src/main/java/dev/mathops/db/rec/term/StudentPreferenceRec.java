package dev.mathops.db.rec.term;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An immutable raw "student preference" record.
 *
 * <p>
 * Each record represents a student's preference for a specific variable/parameter/setting.
 *
 * <p>
 * The primary key on the underlying table is the student ID, course ID, module number, and standard number.
 */
public final class StudentPreferenceRec extends RecBase implements Comparable<StudentPreferenceRec> {

    /** The table name. */
    public static final String TABLE_NAME = "student_preference";

    /** The 'student_id' field value. */
    public final String studentId;

    /** The 'pref_key' field value. */
    public final String prefKey;

    /** The 'pref_value' field value. */
    public final Integer prefValue;

    /**
     * Constructs a new {@code StudentPreference}.
     *
     * @param theStudentId the student ID
     * @param thePrefKey   the preference key ID
     * @param thePrefValue the preference value
     */
    public StudentPreferenceRec(final String theStudentId, final String thePrefKey, final Integer thePrefValue) {

        super();

        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (thePrefKey == null) {
            throw new IllegalArgumentException("Preference key may not be null");
        }
        if (thePrefValue == null) {
            throw new IllegalArgumentException("Preference value may not be null");
        }

        this.studentId = theStudentId;
        this.prefKey = thePrefKey;
        this.prefValue = thePrefValue;
    }

    /**
     * Compares two records for order.  Order is based on student ID then preference key.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StudentPreferenceRec o) {

        int result = compareAllowingNull(this.studentId, o.studentId);

        if (result == 0) {
            result = compareAllowingNull(this.prefKey, o.prefKey);
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

        appendField(htm, DataDict.FLD_STUDENT_ID, this.studentId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_PREF_KEY, this.prefKey);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_PREF_VALUE, this.prefValue);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.studentId.hashCode()
               + this.prefKey.hashCode()
               + this.prefValue.hashCode();
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
        } else if (obj instanceof final StudentPreferenceRec rec) {
            equal = this.studentId.equals(rec.studentId)
                    && this.prefKey.equals(rec.prefKey)
                    && this.prefValue.equals(rec.prefValue);
        } else {
            equal = false;
        }

        return equal;
    }
}

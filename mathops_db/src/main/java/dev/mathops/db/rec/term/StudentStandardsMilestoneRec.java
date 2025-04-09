package dev.mathops.db.rec.term;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDate;

/**
 * An immutable raw "student standard milestone" record.
 *
 * <p>
 * Each record represents an override, for a single student, of a milestone date for one standards-based course
 * milestone in a particular pace, pace track, and pace index.
 *
 * <p>
 * The primary key on the underlying table is the student ID, pace track, pace, pace index, module number, and milestone
 * type.
 */
public final class StudentStandardsMilestoneRec extends RecBase implements Comparable<StudentStandardsMilestoneRec> {

    /** The table name. */
    public static final String TABLE_NAME = "student_standards_milestone";

    /** The 'student_id' field value. */
    public final String studentId;

    /** The 'pace_track' field value. */
    public final String paceTrack;

    /** The 'pace' field value. */
    public final Integer pace;

    /** The 'pace_index' field value. */
    public final Integer paceIndex;

    /** The 'module_nbr' field value. */
    public final Integer moduleNbr;

    /** The 'ms_type' field value. */
    public final String msType;

    /** The 'ms_date' field value. */
    public final LocalDate msDate;

    /**
     * Constructs a new {@code StandardsMilestoneRec}.
     *
     * @param theStudentId the student ID
     * @param thePaceTrack the pace track
     * @param thePace      the pace
     * @param thePaceIndex the pace index
     * @param theModuleNbr the module number
     * @param theMsType    the milestone type
     * @param theMsDate    the milestone date
     */
    public StudentStandardsMilestoneRec(final String theStudentId, final String thePaceTrack, final Integer thePace,
                                        final Integer thePaceIndex, final Integer theModuleNbr,
                                        final String theMsType, final LocalDate theMsDate) {

        super();

        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (thePaceTrack == null) {
            throw new IllegalArgumentException("Pace track may not be null");
        }
        if (thePace == null) {
            throw new IllegalArgumentException("Pace may not be null");
        }
        if (thePaceIndex == null) {
            throw new IllegalArgumentException("Pace index may not be null");
        }
        if (theModuleNbr == null) {
            throw new IllegalArgumentException("Module number may not be null");
        }
        if (theMsType == null) {
            throw new IllegalArgumentException("Milestone type may not be null");
        }
        if (theMsDate == null) {
            throw new IllegalArgumentException("Milestone date may not be null");
        }

        this.studentId = theStudentId;
        this.paceTrack = thePaceTrack;
        this.pace = thePace;
        this.paceIndex = thePaceIndex;
        this.moduleNbr = theModuleNbr;
        this.msType = theMsType;
        this.msDate = theMsDate;
    }

    /**
     * Compares two records for order.  Order is based on pace track, pace, pace index, module number, then milestone
     * type, and finally on student ID.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StudentStandardsMilestoneRec o) {

        int result = compareAllowingNull(this.paceTrack, o.paceTrack);

        if (result == 0) {
            result = compareAllowingNull(this.pace, o.pace);
            if (result == 0) {
                result = compareAllowingNull(this.paceIndex, o.paceIndex);
                if (result == 0) {
                    result = compareAllowingNull(this.moduleNbr, o.moduleNbr);
                    if (result == 0) {
                        result = compareAllowingNull(this.msType, o.msType);
                        if (result == 0) {
                            result = compareAllowingNull(this.studentId, o.studentId);
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

        appendField(htm, DataDict.FLD_STUDENT_ID, this.studentId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_PACE_TRACK, this.paceTrack);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_PACE, this.pace);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_PACE_INDEX, this.paceIndex);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MODULE_NBR, this.moduleNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MS_TYPE, this.msType);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_MS_DATE, this.msDate);

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
               + this.paceTrack.hashCode()
               + this.pace.hashCode()
               + this.paceIndex.hashCode()
               + this.moduleNbr.hashCode()
               + this.msType.hashCode()
               + this.msDate.hashCode();
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
        } else if (obj instanceof final StudentStandardsMilestoneRec rec) {
            equal = this.studentId.equals(rec.studentId)
                    && this.paceTrack.equals(rec.paceTrack)
                    && this.pace.equals(rec.pace)
                    && this.paceIndex.equals(rec.paceIndex)
                    && this.moduleNbr.equals(rec.moduleNbr)
                    && this.msType.equals(rec.msType)
                    && this.msDate.equals(rec.msDate);
        } else {
            equal = false;
        }

        return equal;
    }
}

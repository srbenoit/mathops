package dev.mathops.db.old.rawrecord;

import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.Comparator;
import java.util.Objects;

/**
 * A raw "special_stus" record.
 */
public final class RawSpecialStus extends RecBase implements Comparable<RawSpecialStus> {

    /** A student type that grants administrative access to a user. */
    public static final String ADMIN = "ADMIN";

    /** A student type that indicates a user can act as an adviser. */
    public static final String ADVISER = "ADVISER";

    /** A student type that indicates a user is an athlete. */
    public static final String ATHLETE = "ATHLETE";

    /** A student type that indicates a user is a Bookstore manager. */
    public static final String BOOKSTO = "BOOKSTO";

    /** A student type that indicates a user is a "Continuing Ed" student. */
    public static final String DCE = "DCE";

    /** A student type that indicates a user is a "Continuing Ed" student and not enrolled in a degree program. */
    public static final String DCEN = "DCEN";

    /** A student type that grants access to the ELM Tutorial without having to do Math Placement. */
    public static final String ELM = "ELM";

    /** A student type that indicates a user an employee who can be scheduled. */
    public static final String EMPLOY = "EMPLOY";

    /** A student type that indicates a user is an incoming Engineering student for a Placement report. */
    public static final String ENGRPLC = "ENGRPLC";

    /** A student type that indicates a user is an Engineering student. */
    public static final String ENGRSTU = "ENGRSTU";

    /** A student type that indicates a user a manager of employees. */
    public static final String MANAGER = "MANAGER";

    /** A student type that indicates a student can take the Placement Tool a third time. */
    public static final String MPT3 = "MPT3";

    /** A student type that indicates a user is a student in MATH 116. */
    public static final String M116 = "M116";

    /** A student type that indicates a student is enrolled in MATH 384 and should have access to course materials. */
    public static final String M384 = "M384";

    /** A student type that indicates a student is part of an orientation session for placement reporting. */
    public static final String ORIENTN = "ORIENTN";

    /** A student type that grants access to the MATH 117 Precalculus Tutorial if not normally eligible. */
    public static final String PCT117 = "PCT117";

    /** A student type that grants access to the MATH 118 Precalculus Tutorial if not normally eligible. */
    public static final String PCT118 = "PCT118";

    /** A student type that grants access to the MATH 124 Precalculus Tutorial if not normally eligible. */
    public static final String PCT124 = "PCT124";

    /** A student type that grants access to the MATH 125 Precalculus Tutorial if not normally eligible. */
    public static final String PCT125 = "PCT125";

    /** A student type that grants access to the MATH 126 Precalculus Tutorial if not normally eligible. */
    public static final String PCT126 = "PCT126";

    /** A student type that indicates a user can act as a proctor. */
    public static final String PROCTOR = "PROCTOR";

    /** A student type that indicates a user who is in a resident course can take exams as if a distance student. */
    public static final String RAMWORK = "RAMWORK";

    /** A student type that indicates a user is a resident student but can use ProctorU to take exams. */
    public static final String RIUSEPU = "RIUSEPU";

    /** A student type that indicates a user can skip the user's exam. */
    public static final String SKIP_UE = "SKIP-UE";

    /** A student type that indicates a user is a superuser. */
    public static final String STEVE = "STEVE";

    /** A student type that indicates a user a staff member who can log employee activity. */
    public static final String STAFF = "STAFF";

    /** A student type that indicates a student is a tutor and should have access to course materials. */
    public static final String TUTOR = "TUTOR";

    /** The table name. */
    public static final String TABLE_NAME = "special_stus";

    /** A field name. */
    private static final String FLD_STU_ID = "stu_id";

    /** A field name. */
    private static final String FLD_STU_TYPE = "stu_type";

    /** A field name. */
    private static final String FLD_START_DT = "start_dt";

    /** A field name. */
    private static final String FLD_END_DT = "end_dt";

    /** The 'stu_id' field value. */
    public String stuId;

    /** The 'stu_type' field value. */
    public String stuType;

    /** The 'start_dt' field value. */
    public LocalDate startDt;

    /** The 'end_dt' field value. */
    public LocalDate endDt;

    /**
     * Constructs a new {@code RawSpecialStus}.
     */
    private RawSpecialStus() {

        super();
    }

    /**
     * Constructs a new {@code RawSpecialStus}.
     *
     * @param theStuId   the student ID
     * @param theStuType the student type
     * @param theStartDt the start date
     * @param theEndDt   the end date
     */
    public RawSpecialStus(final String theStuId, final String theStuType,
                          final LocalDate theStartDt, final LocalDate theEndDt) {

        super();

        this.stuId = theStuId;
        this.stuType = theStuType;
        this.startDt = theStartDt;
        this.endDt = theEndDt;
    }

    /**
     * Tests whether this record is active as of a given date.
     *
     * @param today the date
     * @return true if the record is active on the specified date
     */
    public boolean isActive(final ChronoLocalDate today) {

        boolean active = this.startDt == null || !this.startDt.isAfter(today);

        if (this.endDt != null && this.endDt.isBefore(today)) {
            active = false;
        }

        return active;
    }

    /**
     * Extracts a "special_stus" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawSpecialStus fromResultSet(final ResultSet rs) throws SQLException {

        final RawSpecialStus result = new RawSpecialStus();

        result.stuId = getStringField(rs, FLD_STU_ID);
        result.stuType = getStringField(rs, FLD_STU_TYPE);
        result.startDt = getDateField(rs, FLD_START_DT);
        result.endDt = getDateField(rs, FLD_END_DT);

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
    public int compareTo(final RawSpecialStus o) {

        int result = this.stuId.compareTo(o.stuId);

        if (result == 0) {
            result = this.stuType.compareTo(o.stuType);
        }

        return result;
    }

    /**
     * A comparator that can be used to sort a list of {@code RawSpecialStus} by start date.
     */
    public static final class StartDateComparator implements Comparator<RawSpecialStus> {

        /**
         * Perform the comparison.
         */
        @Override
        public int compare(final RawSpecialStus o1, final RawSpecialStus o2) {

            int result = compareAllowingNull(o1.startDt, o2.startDt);
            if (result == 0) {
                result = compareAllowingNull(o1.stuId, o2.stuId);
                if (result == 0) {
                    result = compareAllowingNull(o1.stuType, o2.stuType);
                }
            }

            return result;
        }
    }

    /**
     * A comparator that can be used to sort a list of {@code RawSpecialStus} by end date.
     */
    public static final class EndDateComparator implements Comparator<RawSpecialStus> {

        /**
         * Perform the comparison.
         */
        @Override
        public int compare(final RawSpecialStus o1, final RawSpecialStus o2) {

            int result = compareAllowingNull(o1.endDt, o2.endDt);
            if (result == 0) {
                result = compareAllowingNull(o1.stuId, o2.stuId);
                if (result == 0) {
                    result = compareAllowingNull(o1.stuType, o2.stuType);
                }
            }

            return result;
        }
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

        appendField(htm, FLD_STU_ID, this.stuId);
        htm.add(DIVIDER);
        appendField(htm, FLD_STU_TYPE, this.stuType);
        htm.add(DIVIDER);
        appendField(htm, FLD_START_DT, this.startDt);
        htm.add(DIVIDER);
        appendField(htm, FLD_END_DT, this.endDt);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.stuId)
               + Objects.hashCode(this.stuType)
               + Objects.hashCode(this.startDt)
               + Objects.hashCode(this.endDt);
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
        } else if (obj instanceof final RawSpecialStus rec) {
            equal = Objects.equals(this.stuId, rec.stuId)
                    && Objects.equals(this.stuType, rec.stuType)
                    && Objects.equals(this.startDt, rec.startDt)
                    && Objects.equals(this.endDt, rec.endDt);
        } else {
            equal = false;
        }

        return equal;
    }
}

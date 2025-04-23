package dev.mathops.db.rec.term;

import dev.mathops.db.DataDict;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.time.LocalDate;
import java.util.Objects;

/**
 * An immutable raw "standards course section" record.
 *
 * <p>
 * Each record represents a section of a standards-based course in a term.
 *
 * <p>
 * The primary key on the underlying table is the course ID and section number.
 */
public final class StandardsCourseSectionRec extends RecBase implements Comparable<StandardsCourseSectionRec> {

    /** The table name for serialization of records. */
    public static final String TABLE_NAME = "standards_course_section";

    /** The 'course_id' field value. */
    public final String courseId;

    /** The 'section_nbr' field value. */
    public final String sectionNbr;

    /** The 'crn' field value. */
    public final String crn;

    /** The 'aries_start_date' field value. */
    public final LocalDate ariesStartDate;

    /** The 'aries_end_date' field value. */
    public final LocalDate ariesEndDate;

    /** The 'first_class_date' field value. */
    public final LocalDate firstClassDate;

    /** The 'last_class_date' field value. */
    public final LocalDate lastClassDate;

    /** The 'subterm' field value. */
    public final String subterm;

    /** The 'grading_system_id' field value. */
    public final String gradingSystemId;

    /** The 'campus' field value. */
    public final String campus;

    /** The 'delivery_mode' field value. */
    public final String deliveryMode;

    /** The 'canvas_id' field value. */
    public final String canvasId;

    /** The 'instructor' field value. */
    public final String instructor;

    /** The 'building_name' field value. */
    public final String buildingName;

    /** The 'room_nbr' field value. */
    public final String roomNbr;

    /** The 'weekdays' field value. */
    public final Integer weekdays;

    /**
     * Constructs a new {@code StandardsCourseSectionRec}.
     *
     * @param theCourseId        the course ID
     * @param theSectionNbr      the section number
     * @param theCrn             the section CRN (internal ID used when submitting grades)
     * @param theAriesStartDate  the start date in University systems
     * @param theAriesEndDate    the end date in University systems
     * @param theFirstClassDate  the first date classes meet
     * @param theLastClassDate   the last date classes meet
     * @param theSubterm         the subterm in which the section runs ('FULL', 'HALF1', 'HALF2', or 'NN:MM' for weeks)
     * @param theGradingSystemId the grading system ID
     * @param theCampus          the campus identifier
     * @param theDeliveryMode    the delivery mode ('RF'=Resident Face-to-Face, 'RH'=Resident Hybrid, 'RO'=Resident
     *                           Online, 'DO'=Distance Online)
     * @param theCanvasId        the ID of the section in the Canvas LMS
     * @param theInstructor      for instructor name
     * @param theBuildingName    the name of the building where classes meet
     * @param theRoomNbr         the room number where classes meet
     * @param theWeekdays        a bitwise OR of the weekdays on which class meets each week
     */
    public StandardsCourseSectionRec(final String theCourseId, final String theSectionNbr, final String theCrn,
                                     final LocalDate theAriesStartDate, final LocalDate theAriesEndDate,
                                     final LocalDate theFirstClassDate, final LocalDate theLastClassDate,
                                     final String theSubterm, final String theGradingSystemId, final String theCampus,
                                     final String theDeliveryMode, final String theCanvasId, final String theInstructor,
                                     final String theBuildingName, final String theRoomNbr, final Integer theWeekdays) {

        super();

        if (theCourseId == null) {
            throw new IllegalArgumentException("Course ID may not be null");
        }
        if (theSectionNbr == null) {
            throw new IllegalArgumentException("Section number may not be null");
        }
        if (theCrn == null) {
            throw new IllegalArgumentException("CRN may not be null");
        }
        if (theAriesStartDate == null) {
            throw new IllegalArgumentException("Aries start date may not be null");
        }
        if (theAriesEndDate == null) {
            throw new IllegalArgumentException("Aries end date may not be null");
        }
        if (theFirstClassDate == null) {
            throw new IllegalArgumentException("First class date may not be null");
        }
        if (theLastClassDate == null) {
            throw new IllegalArgumentException("Last class date may not be null");
        }
        if (theSubterm == null) {
            throw new IllegalArgumentException("Subterm may not be null");
        }
        if (theGradingSystemId == null) {
            throw new IllegalArgumentException("Grading system ID may not be null");
        }
        if (theCampus == null) {
            throw new IllegalArgumentException("Campus may not be null");
        }
        if (theDeliveryMode == null) {
            throw new IllegalArgumentException("Mode may not be null");
        }

        this.courseId = theCourseId;
        this.sectionNbr = theSectionNbr;
        this.crn = theCrn;
        this.ariesStartDate = theAriesStartDate;
        this.ariesEndDate = theAriesEndDate;
        this.firstClassDate = theFirstClassDate;
        this.lastClassDate = theLastClassDate;
        this.subterm = theSubterm;
        this.gradingSystemId = theGradingSystemId;
        this.campus = theCampus;
        this.deliveryMode = theDeliveryMode;
        this.canvasId = theCanvasId;
        this.instructor = theInstructor;
        this.buildingName = theBuildingName;
        this.roomNbr = theRoomNbr;
        this.weekdays = theWeekdays;
    }

    /**
     * Compares two records for order.  Order is based on course ID then section number.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final StandardsCourseSectionRec o) {

        int result = this.courseId.compareTo(o.courseId);

        if (result == 0) {
            result = this.sectionNbr.compareTo(o.sectionNbr);
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

        appendField(htm, DataDict.FLD_COURSE_ID, this.courseId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_SECTION_NBR, this.sectionNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_CRN, this.crn);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ARIES_START_DATE, this.ariesStartDate);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ARIES_END_DATE, this.ariesEndDate);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_FIRST_CLASS_DATE, this.firstClassDate);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_LAST_CLASS_DATE, this.lastClassDate);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_SUBTERM, this.subterm);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_GRADING_SYSTEM_ID, this.gradingSystemId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_CAMPUS, this.campus);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_DELIVERY_MODE, this.deliveryMode);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_CANVAS_ID, this.canvasId);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_INSTRUCTOR, this.instructor);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_BUILDING_NAME, this.buildingName);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_ROOM_NBR, this.roomNbr);
        htm.add(DIVIDER);
        appendField(htm, DataDict.FLD_WEEKDAYS, this.weekdays);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return this.courseId.hashCode()
               + this.sectionNbr.hashCode()
               + this.crn.hashCode()
               + this.ariesStartDate.hashCode()
               + this.ariesEndDate.hashCode()
               + this.firstClassDate.hashCode()
               + this.lastClassDate.hashCode()
               + this.subterm.hashCode()
               + this.gradingSystemId.hashCode()
               + this.campus.hashCode()
               + this.deliveryMode.hashCode()
               + Objects.hashCode(this.canvasId)
               + Objects.hashCode(this.instructor)
               + Objects.hashCode(this.buildingName)
               + Objects.hashCode(this.roomNbr)
               + Objects.hashCode(this.weekdays);
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
        } else if (obj instanceof final StandardsCourseSectionRec rec) {
            equal = this.courseId.equals(rec.courseId)
                    && this.sectionNbr.equals(rec.sectionNbr)
                    && this.crn.equals(rec.crn)
                    && this.ariesStartDate.equals(rec.ariesStartDate)
                    && this.ariesEndDate.equals(rec.ariesEndDate)
                    && this.firstClassDate.equals(rec.firstClassDate)
                    && this.lastClassDate.equals(rec.lastClassDate)
                    && this.subterm.equals(rec.subterm)
                    && this.gradingSystemId.equals(rec.gradingSystemId)
                    && this.campus.equals(rec.campus)
                    && this.deliveryMode.equals(rec.deliveryMode)
                    && Objects.equals(this.canvasId, rec.canvasId)
                    && Objects.equals(this.instructor, rec.instructor)
                    && Objects.equals(this.buildingName, rec.buildingName)
                    && Objects.equals(this.roomNbr, rec.roomNbr)
                    && Objects.equals(this.weekdays, rec.weekdays);
        } else {
            equal = false;
        }

        return equal;
    }
}

package dev.mathops.app.scheduling;

/**
 * A section of a course offered in a semester.
 */
final class OfferedSection {

    /** Weekday code for Monday. */
    public static final int MONDAY = 0x01;

    /** Weekday code for Tuesday. */
    public static final int TUESDAY = 0x02;

    /** Weekday code for Wednesday. */
    public static final int WEDNESDAY = 0x04;

    /** Weekday code for Thursday. */
    public static final int THURSDAY = 0x08;

    /** Weekday code for Friday. */
    public static final int FRIDAY = 0x10;

    /** Weekday code for Monday + Wednesday + Friday. */
    public static final int MWF = MONDAY | WEDNESDAY | FRIDAY;

    /** Weekday code for Tuesday + Thursday. */
    public static final int TR = TUESDAY | THURSDAY;

    /** The course ID. */
    final String courseId;

    /** The section. */
    final int section;

    /** The number of seats offered. */
    final int numSeats;

    /** The number of credits. */
    final int numCredits;

    /** The weekdays the course meets (an OR of weekday constants defined in this class). */
    final int weekdays;

    /** The hour block the course meets. */
    final int hourBlock;

    /** The weekdays the lab meets (an OR of weekday constants defined in this class, 0 if no lab). */
    final int labWeekdays;

    /** The hour block the lab meets. */
    final int labHourBlock;

    /** The number of hour blocks the lab occupies. */
    final int labLengthInBlocks;

    /** The number of students currently enrolled. */
    private int numberEnrolled = 0;

    /**
     * Constructs a new {@code OfferedSection} with no students enrolled and no lab.
     *
     * @param theCourseId   the course ID
     * @param theSection    the section
     * @param theNumSeats   the number of seats offered
     * @param theNumCredits the number of credits
     * @param theWeekdays   the weekdays the course meets
     * @param theHourBlock  the hour block the course meets
     */
    OfferedSection(final String theCourseId, final int theSection, final int theNumSeats, final int theNumCredits,
                   final int theWeekdays, final int theHourBlock) {

        this.courseId = theCourseId;
        this.section = theSection;
        this.numSeats = theNumSeats;
        this.numCredits = theNumCredits;
        this.weekdays = theWeekdays;
        this.hourBlock = theHourBlock;
        this.labWeekdays = 0;
        this.labHourBlock = 0;
        this.labLengthInBlocks = 0;
    }

    /**
     * Constructs a new {@code OfferedSection} with no students enrolled and with a lab.
     *
     * @param theCourseId          the course ID
     * @param theSection           the section
     * @param theNumSeats          the number of seats offered
     * @param theNumCredits        the number of credits
     * @param theWeekdays          the weekdays the course meets
     * @param theHourBlock         the hour block the course meets
     * @param theLabWeekdays       the weekdays the lab meets
     * @param theLabHourBlock      the hour block the lab meets
     * @param theLabLengthInBlocks the number of hour blocks the lab occupies
     */
    OfferedSection(final String theCourseId, final int theSection, final int theNumSeats, final int theNumCredits,
                   final int theWeekdays, final int theHourBlock, final int theLabWeekdays, final int theLabHourBlock,
                   final int theLabLengthInBlocks) {

        this.courseId = theCourseId;
        this.section = theSection;
        this.numSeats = theNumSeats;
        this.numCredits = theNumCredits;
        this.weekdays = theWeekdays;
        this.hourBlock = theHourBlock;
        this.labWeekdays = theLabWeekdays;
        this.labHourBlock = theLabHourBlock;
        this.labLengthInBlocks = theLabLengthInBlocks;
    }

    /**
     * Tests whether the section is full.
     *
     * @return true if full
     */
    boolean isFull() {

        return this.numberEnrolled >= this.numSeats;
    }

    /**
     * Adds an enrolled student.
     */
    void addEnrolled() {

        if (this.numberEnrolled < this.numSeats) {
            ++this.numberEnrolled;
        }
    }

    /**
     * Gets the number of students enrolled.
     *
     * @return the number enrolled
     */
    int getNumberEnrolled() {

        return this.numberEnrolled;
    }
}

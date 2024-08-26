package dev.mathops.app.scheduling;

/**
 * A section of a course offered in a semester.
 */
final class OfferedSection {

    final String courseId;

    final int section;

    final int numSeats;

    final int numCredits;

    final int weekdays;

    final int hourBlock;

    final int labWeekdays;

    final int labHourBlock;

    final int labLengthInBlocks;

    private int numberEnrolled = 0;

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

    boolean isFull() {

        return this.numberEnrolled >= this.numSeats;
    }

    void addEnrolled() {

        if (this.numberEnrolled < this.numSeats) {
            ++this.numberEnrolled;
        }
    }

    int getNumberEnrolled() {

        return this.numberEnrolled;
    }
}

package dev.mathops.app.sim.registration;

import dev.mathops.commons.builder.HtmlBuilder;

/**
 * A section that will meet in a room on some combination of Monday, Wednesday, and Friday, for an integer number of
 * hours.
 */
final class SectionMWF extends AbstractSection {

    /** The days the section meets (some combination of Monday, Wednesday, or Friday). */
    private final EMeetingDaysMWF meetingDays;

    /**
     * Constructs a new {@code SectionMWF}.
     *
     * @param theId           the section iD
     * @param theMeetingDays  the days the section meets
     * @param theRoom         the room in which the section will meet
     * @param theCourse       the course
     * @param theNumSeats     the number seats needed
     * @param theUsage        the usage
     * @param theBlocksPerDay the number of 50-minute blocks the course will meet each day
     */
    SectionMWF(final int theId, final EMeetingDaysMWF theMeetingDays, final Room theRoom, final Course theCourse,
               final int theNumSeats, final ERoomUsage theUsage, final int theBlocksPerDay) {

        super(theId, theRoom, theCourse, theNumSeats, theUsage, theBlocksPerDay);

        this.meetingDays = theMeetingDays;
    }

    /**
     * Gets the days the section meets (some combination of Monday, Wednesday, or Friday).
     *
     * @return the meeting days
     */
    EMeetingDaysMWF meetingDays() {

        return this.meetingDays;
    }

    /**
     * Generates a string representation of the section.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder builder = new HtmlBuilder(100);

        final int id = id();
        final Course course = course();
        final Room room = room();
        final int numSeats = numSeats();
        final int blocksPerDay = blocksPerDay();

        builder.add("Section ");
        builder.add(id);
        builder.add(" of ", course, " meeting ", this.meetingDays, " in ", room, " for (");

        if (blocksPerDay == 1) {
            builder.add("1) 50-minute block");
        } else {
            builder.add(blocksPerDay);
            builder.add(") 50-minute blocks");
        }
        builder.add(" each day with ");
        builder.add(numSeats);
        builder.add(" seats used.");

        return builder.toString();
    }
}

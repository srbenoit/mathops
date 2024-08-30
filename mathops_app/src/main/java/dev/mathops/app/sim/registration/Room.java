package dev.mathops.app.sim.registration;

import dev.mathops.commons.log.Log;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * A model of a room in which classes or labs can be scheduled.
 */
final class Room implements Comparable<Room> {

    /** The number of weekdays. */
    private static final int NUM_WEEKDAYS = 5;

    /** A zero-length array to return if an invalid weekday is requested. */
    private static final int[] EMPTY_BLOCK_SCHEDULE = new int[0];

    /** The unique room ID. */
    private final String id;

    /** The seating capacity. */
    private final int capacity;

    /** The number of hours per day the room is available. */
    private final int hoursPerDay;

    /**
     * The list of "assignments" currently active, including the number of hours per week, and whether it needs to be
     * contiguous.
     */
    private final List<RoomAssignment> assignments;

    /**
     * A grid of 25-minute blocks available to schedule classes or labs.  A "1 contact hour" block would consume two of
     * these (adjacent).  An "N contact hour contiguous" block would consume 2N of these (contiguous). An "N contact
     * hour" block will consume 2N of these, but they could be arranged in pairs or threes over multiple days.  Each
     * entry in this grid is the "assignment number" that is allocated to those blocks.  This class is free to move the
     * blocks associated with an "assignment number" around to try to accommodate requests for new assignments, but can
     * (on request) provide the current set of blocks occupied by any assignment.
     */
    private final int[][] timeBlockGrid;

    /** The number of blocks free in each column of {@code timeBlockGrid}. */
    private final int[] blocksFree;

    /**
     * Constructs a new {@code Room}.
     *
     * @param theId          the unique room ID
     * @param theCapacity    the seating capacity
     * @param theHoursPerDay the number of hours per day the room is available
     */
    Room(final String theId, final int theCapacity, final int theHoursPerDay) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("Room ID may not be null or blank");
        }
        if (theCapacity < 1) {
            throw new IllegalArgumentException("Room capacity may not be less than 1");
        }
        if (theHoursPerDay < 1) {
            throw new IllegalArgumentException("Number of hours per day may not be less than 1");
        }

        this.id = theId;
        this.capacity = theCapacity;
        this.hoursPerDay = theHoursPerDay;

        this.assignments = new ArrayList<>(100);

        final int blocksPerDay = theHoursPerDay * 2;
        this.timeBlockGrid = new int[NUM_WEEKDAYS][blocksPerDay];
        this.blocksFree = new int[NUM_WEEKDAYS];
        Arrays.fill(this.blocksFree, blocksPerDay);
    }

    /**
     * Gets the unique room ID.
     *
     * @return the room ID
     */
    String getId() {

        return this.id;
    }

    /**
     * Gets the seating capacity.
     *
     * @return the seating capacity
     */
    int getCapacity() {

        return this.capacity;
    }

    /**
     * Gets the number of hours per day the room is available.
     *
     * @return the number of hours per day
     */
    int getHoursPerDay() {

        return this.hoursPerDay;
    }

    /**
     * Removes all assignments and resets the time schedule.
     */
    void clearAssignments() {

        this.assignments.clear();
        for (int i = 0; i < NUM_WEEKDAYS; ++i) {
            Arrays.fill(this.timeBlockGrid[i], 0);
        }

        final int blocksPerDay = this.hoursPerDay * 2;
        Arrays.fill(this.blocksFree, blocksPerDay);
    }

    /**
     * Attempts to add a room assignment.  This process might attempt move existing assignments around to make space for
     * the requested assignment.
     *
     * @param num25MinBlocks the number of 25-minute blocks needed
     * @param type           the assignment type
     * @return an object with the room assignment if it was made, or without if there was insufficient available time to
     *         make the assignment (the assignment ID will be unique within the room)
     */
    Optional<RoomAssignment> addAssignment(final int num25MinBlocks, final EAssignmentType type) {

        // See if we can add the ra without rearranging.

        final int newId = this.assignments.size() + 1;

        RoomAssignment ra;

        if (type == EAssignmentType.CONTIGUOUS) {
            ra = addContiguousAssignment(num25MinBlocks, newId);
        } else if (type == EAssignmentType.GROUPS_OF_2) {
            ra = addGroupsOf2Assignment(num25MinBlocks, newId);
        } else if (type == EAssignmentType.GROUPS_OF_3) {
            ra = addGroupsOf3Assignment(num25MinBlocks, newId);
        } else {
            final int minFreeMW = Math.min(this.blocksFree[0], this.blocksFree[2]);
            final int minFreeMWF = Math.min(minFreeMW, this.blocksFree[4]);
            final int minFreeTR = Math.min(this.blocksFree[1], this.blocksFree[3]);

            if (minFreeMWF > minFreeTR) {
                ra = addGroupsOf2Assignment(num25MinBlocks, newId);
                if (ra == null) {
                    ra = addGroupsOf3Assignment(num25MinBlocks, newId);
                }
            } else {
                ra = addGroupsOf3Assignment(num25MinBlocks, newId);
                if (ra == null) {
                    ra = addGroupsOf2Assignment(num25MinBlocks, newId);
                }
            }
        }

        return ra == null ? Optional.empty() : Optional.of(ra);
    }

    /**
     * Attempts to add a contiguous assignment.
     *
     * @param num25MinBlocks the number of 25-minute blocks needed
     * @param newId          the assignment ID to use
     * @return an object with the room assignment if it was made, or without if there was insufficient available time to
     *         make the assignment (the assignment ID will be unique within the room)
     */
    private RoomAssignment addContiguousAssignment(final int num25MinBlocks, final int newId) {

        final int dayIndex = findDayWithContiguousBlocks(num25MinBlocks);

        RoomAssignment ra = null;

        if (dayIndex >= 0) {
            ra = addAssignedBlock(dayIndex, num25MinBlocks, newId, EAssignmentType.CONTIGUOUS);
        }

        return ra;
    }

    /**
     * Attempts to add an assignment that is broken into groups of 2 25-minute blocks spread over different days.
     *
     * @param num25MinBlocks the number of 25-minute blocks needed
     * @param newId          the assignment ID to use
     * @return an object with the room assignment if it was made, or without if there was insufficient available time to
     *         make the assignment (the assignment ID will be unique within the room)
     */
    private RoomAssignment addGroupsOf2Assignment(final int num25MinBlocks, final int newId) {

        RoomAssignment ra = null;

        final int numGroups = (num25MinBlocks + 1) / 2;

        if (numGroups == 1) {
            ra = addOneGroupOf2(newId);
        } else if (numGroups == 2) {
            ra = addTwoGroupsOf2(newId);
        } else if (numGroups == 3) {
            ra = addThreeGroupsOf2(newId);
        } else if (numGroups == 4) {
            ra = addFourGroupsOf2(newId);
        } else if (numGroups == 5) {
            ra = addFiveGroupsOf2(newId);
        } else {
            Log.warning("ERROR: A class using groups of 2 with " + num25MinBlocks + " is not yet supported");
        }

        return ra;
    }

    /**
     * Adds a course that occupies a single group of 2 25-minute blocks on a single day.
     *
     * <p>
     * This method will search for a day of the week that has more free blocks than any other, and attempt to assign the
     * class there.  It will prefer a Mon/Wed/Fri date (since Tue/Thr are probably going to be used for contiguous or
     * blocks of 3), but will assign to Tue/Thr if needed.
     *
     * @param newId the new assignment ID
     * @return the room assignment if successful; {@code null} if not
     */
    private RoomAssignment addOneGroupOf2(final int newId) {

        RoomAssignment ra = null;

        final int minFreeMW = Math.min(this.blocksFree[0], this.blocksFree[2]);
        final int minFreeMWF = Math.min(minFreeMW, this.blocksFree[4]);

        final boolean monOk = this.blocksFree[0] >= 2;
        final boolean wedOk = this.blocksFree[2] >= 2;
        final boolean friOk = this.blocksFree[4] >= 2;

        if (this.blocksFree[0] >= minFreeMWF && monOk) {
            ra = addAssignedBlock(0, 2, newId, EAssignmentType.GROUPS_OF_2);
        } else if (this.blocksFree[2] >= minFreeMWF && wedOk) {
            ra = addAssignedBlock(2, 2, newId, EAssignmentType.GROUPS_OF_2);
        } else {
            // Mon/Wed/Fri all have the same number of blocks free - prefer Monday, all else being equal

            if (monOk) {
                ra = addAssignedBlock(0, 2, newId, EAssignmentType.GROUPS_OF_2);
            } else {
                final int minFreeTR = Math.min(this.blocksFree[1], this.blocksFree[3]);
                final boolean tueOk = this.blocksFree[1] >= 2;
                final boolean thrOk = this.blocksFree[3] >= 2;

                if (this.blocksFree[1] >= minFreeTR && tueOk) {
                    ra = addAssignedBlock(1, 2, newId, EAssignmentType.GROUPS_OF_2);
                } else if (this.blocksFree[3] >= minFreeTR && thrOk) {
                    ra = addAssignedBlock(3, 2, newId, EAssignmentType.GROUPS_OF_2);
                } else {
                    // Tue/Thur have the same number of days free - prefer Tuesday, all else being equal
                    if (tueOk) {
                        ra = addAssignedBlock(1, 2, newId, EAssignmentType.GROUPS_OF_2);
                    }
                }

                // TODO: Otherwise, try to rebuild the schedule
            }
        }

        return ra;
    }

    /**
     * Adds a course that occupies two groups of 2 25-minute blocks on separate days.
     *
     * <p>
     * This method will look for two days in M/W/F that have fewer than the other, and if found, assign this class
     * there.  Otherwise, assign to any two M/W/F days with space, and if there are none, assign to T/R if they have
     * space (the intent is to keep Tue/Thr for contiguous blocks and those that come in groups of 3, if possible.
     *
     * @param newId the new assignment ID
     * @return the room assignment if successful; {@code null} if not
     */
    private RoomAssignment addTwoGroupsOf2(final int newId) {

        RoomAssignment ra = null;

        final boolean monOk = this.blocksFree[0] >= 2;
        final boolean wedOk = this.blocksFree[2] >= 2;
        final boolean friOk = this.blocksFree[4] >= 2;

        final int minFreeMW = Math.min(this.blocksFree[0], this.blocksFree[2]);
        final int minFreeMWF = Math.min(minFreeMW, this.blocksFree[4]);

        final boolean monHasLess = this.blocksFree[0] > minFreeMWF;
        final boolean wedHasLess = this.blocksFree[2] > minFreeMWF;
        final boolean friHasLess = this.blocksFree[4] > minFreeMWF;

        if (monHasLess && monOk && wedHasLess && wedOk) {
            markAssignedBlock(0, 2, newId);
            ra = addAssignedBlock(2, 2, newId, EAssignmentType.GROUPS_OF_2);
        } else if (monHasLess && monOk && friHasLess && friOk) {
            markAssignedBlock(0, 2, newId);
            ra = addAssignedBlock(4, 2, newId, EAssignmentType.GROUPS_OF_2);
        } else if (wedHasLess && wedOk && friHasLess && friOk) {
            markAssignedBlock(2, 2, newId);
            ra = addAssignedBlock(4, 2, newId, EAssignmentType.GROUPS_OF_2);
        } else {
            // Mon/Wed/Fri all have the same number of hours free

            if (monOk && wedOk) {
                markAssignedBlock(0, 2, newId);
                ra = addAssignedBlock(2, 2, newId, EAssignmentType.GROUPS_OF_2);
            } else {
                final boolean tueOk = this.blocksFree[1] >= 2;
                final boolean thrOk = this.blocksFree[3] >= 2;

                if (tueOk && thrOk) {
                    markAssignedBlock(1, 2, newId);
                    ra = addAssignedBlock(3, 2, newId, EAssignmentType.GROUPS_OF_2);
                }

                // TODO: Otherwise, try to rebuild schedule
            }
        }

        return ra;
    }

    /**
     * Adds a course that occupies three groups of 2 25-minute blocks on separate days.
     *
     * <p>
     * This method will check whether M/W/F all have space, and assign the class there if possible.
     *
     * @param newId the new assignment ID
     * @return the room assignment if successful; {@code null} if not
     */
    private RoomAssignment addThreeGroupsOf2(final int newId) {

        RoomAssignment ra = null;

        // Try to assign to M/W/F

        final boolean monOk = this.blocksFree[0] >= 2;
        final boolean wedOk = this.blocksFree[2] >= 2;
        final boolean friOk = this.blocksFree[4] >= 2;

        if (monOk && wedOk && friOk) {
            markAssignedBlock(0, 2, newId);
            markAssignedBlock(2, 2, newId);
            ra = addAssignedBlock(4, 2, newId, EAssignmentType.GROUPS_OF_2);
        }

        // TODO: Otherwise, try to rebuild schedule

        return ra;
    }

    /**
     * Adds a course that occupies four groups of 2 25-minute blocks on separate days.
     *
     * <p>
     * This method will check for one day with fewer free blocks than the others, and try to assign the class to the
     * remaining days.
     *
     * @param newId the new assignment ID
     * @return the room assignment if successful; {@code null} if not
     */
    private RoomAssignment addFourGroupsOf2(final int newId) {

        RoomAssignment ra = null;

        // Try to assign to all but the most full day
        final int minFreeMT = Math.min(this.blocksFree[0], this.blocksFree[1]);
        final int minFreeWR = Math.min(this.blocksFree[2], this.blocksFree[3]);
        final int minFreeMTWR = Math.min(minFreeMT, minFreeWR);
        final int minFreeAll = Math.min(this.blocksFree[4], minFreeMTWR);

        final boolean monOk = this.blocksFree[0] >= 2;
        final boolean tueOk = this.blocksFree[1] >= 2;
        final boolean wedOk = this.blocksFree[2] >= 2;
        final boolean thrOk = this.blocksFree[3] >= 2;
        final boolean friOk = this.blocksFree[4] >= 2;

        // Try MWRF first, then MTWF, then MTWR, then MTRF, then TWRF
        if (this.blocksFree[1] == minFreeAll && monOk && wedOk && thrOk && friOk) {
            markAssignedBlock(0, 2, newId);
            markAssignedBlock(2, 2, newId);
            markAssignedBlock(3, 2, newId);
            ra = addAssignedBlock(4, 2, newId, EAssignmentType.GROUPS_OF_2);
        } else if (this.blocksFree[3] == minFreeAll && monOk && tueOk && wedOk && friOk) {
            markAssignedBlock(0, 2, newId);
            markAssignedBlock(1, 2, newId);
            markAssignedBlock(2, 2, newId);
            ra = addAssignedBlock(4, 2, newId, EAssignmentType.GROUPS_OF_2);
        } else if (this.blocksFree[4] == minFreeAll && monOk && tueOk && wedOk && thrOk) {
            markAssignedBlock(0, 2, newId);
            markAssignedBlock(1, 2, newId);
            markAssignedBlock(2, 2, newId);
            ra = addAssignedBlock(3, 2, newId, EAssignmentType.GROUPS_OF_2);
        } else if (this.blocksFree[2] == minFreeAll && monOk && tueOk && thrOk && friOk) {
            markAssignedBlock(0, 2, newId);
            markAssignedBlock(1, 2, newId);
            markAssignedBlock(3, 2, newId);
            ra = addAssignedBlock(4, 2, newId, EAssignmentType.GROUPS_OF_2);
        } else if (this.blocksFree[0] == minFreeAll && tueOk && wedOk && thrOk && friOk) {
            markAssignedBlock(1, 2, newId);
            markAssignedBlock(2, 2, newId);
            markAssignedBlock(3, 2, newId);
            ra = addAssignedBlock(4, 2, newId, EAssignmentType.GROUPS_OF_2);
        }

        // TODO: Otherwise, rebuild the schedule

        return ra;
    }

    /**
     * Adds a course that occupies five groups of 2 25-minute blocks on separate days.
     *
     * <p>
     * This method simply checks that all 5 days can hold the assignment, and assigns the class if so.
     *
     * @param newId the new assignment ID
     * @return the room assignment if successful; {@code null} if not
     */
    private RoomAssignment addFiveGroupsOf2(final int newId) {

        RoomAssignment ra = null;

        final boolean monOk = this.blocksFree[0] >= 2;
        final boolean tueOk = this.blocksFree[1] >= 2;
        final boolean wedOk = this.blocksFree[2] >= 2;
        final boolean thrOk = this.blocksFree[3] >= 2;
        final boolean friOk = this.blocksFree[4] >= 2;

        if (monOk && tueOk && wedOk && thrOk && friOk) {
            markAssignedBlock(0, 2, newId);
            markAssignedBlock(1, 2, newId);
            markAssignedBlock(2, 2, newId);
            markAssignedBlock(3, 2, newId);
            ra = addAssignedBlock(4, 2, newId, EAssignmentType.GROUPS_OF_2);
        }

        // TODO: Otherwise, rebuild the schedule

        return ra;
    }

    /**
     * Attempts to add an assignment that is broken into groups of 3 25-minute blocks spread over different days.
     *
     * @param num25MinBlocks the number of 25-minute blocks needed
     * @param newId          the assignment ID to use
     * @return an object with the room assignment if it was made, or without if there was insufficient available time to
     *         make the assignment (the assignment ID will be unique within the room)
     */
    private RoomAssignment addGroupsOf3Assignment(final int num25MinBlocks, final int newId) {

        RoomAssignment ra = null;

        final int numGroups = (num25MinBlocks + 2) / 3;

        if (numGroups == 1) {
            ra = addOneGroupOf3(newId);
        } else if (numGroups == 2) {
            ra = addTwoGroupsOf3(newId);
        } else if (numGroups == 3) {
            ra = addThreeGroupsOf3(newId);
        } else {
            Log.warning("ERROR: A class using groups of 3 with " + num25MinBlocks + " is not yet supported");
        }

        return ra;
    }

    /**
     * Adds a course that occupies a single group of 3 25-minute blocks on a single day.
     *
     * <p>
     * This method will search for a day of the week that has more free blocks than any other, and attempt to assign the
     * class there.  It will prefer a Tue/Thr date, but will assign to Mon/Wed/Fri if needed.
     *
     * @param newId the new assignment ID
     * @return the room assignment if successful; {@code null} if not
     */
    private RoomAssignment addOneGroupOf3(final int newId) {

        RoomAssignment ra = null;

        final int minFreeTR = Math.min(this.blocksFree[1], this.blocksFree[3]);
        final boolean tueOk = this.blocksFree[1] >= 2;
        final boolean thrOk = this.blocksFree[3] >= 2;

        if (this.blocksFree[1] >= minFreeTR && tueOk) {
            ra = addAssignedBlock(1, 3, newId, EAssignmentType.GROUPS_OF_3);
        } else if (thrOk) {
            ra = addAssignedBlock(3, 3, newId, EAssignmentType.GROUPS_OF_3);
        } else {
            final int minFreeMW = Math.min(this.blocksFree[0], this.blocksFree[2]);
            final int minFreeMWF = Math.min(minFreeMW, this.blocksFree[4]);

            final boolean monOk = this.blocksFree[0] >= 3;
            final boolean wedOk = this.blocksFree[2] >= 3;
            final boolean friOk = this.blocksFree[4] >= 3;

            if (this.blocksFree[0] >= minFreeMWF && monOk) {
                ra = addAssignedBlock(0, 3, newId, EAssignmentType.GROUPS_OF_3);
            } else if (this.blocksFree[2] >= minFreeMWF && wedOk) {
                ra = addAssignedBlock(2, 3, newId, EAssignmentType.GROUPS_OF_3);
            } else if (this.blocksFree[4] >= minFreeMWF && friOk) {
                ra = addAssignedBlock(4, 3, newId, EAssignmentType.GROUPS_OF_3);
            } else if (monOk) {
                ra = addAssignedBlock(0, 3, newId, EAssignmentType.GROUPS_OF_3);
            } else if (wedOk) {
                ra = addAssignedBlock(2, 3, newId, EAssignmentType.GROUPS_OF_3);
            } else if (friOk) {
                ra = addAssignedBlock(4, 3, newId, EAssignmentType.GROUPS_OF_3);
            }

            // TODO: Otherwise, try to rebuild the schedule
        }

        return ra;
    }

    /**
     * Adds a course that occupies two groups of 3 25-minute blocks on separate days.
     *
     * <p>
     * This method will check whether Tue/Tur can hold the course, and assign it there if so.  If not, it will look for
     * space in Mon/Wed/Fri, but it will allocate blocks of 4 rather than 3 to keep the schedule aligned.
     *
     * @param newId the new assignment ID
     * @return the room assignment if successful; {@code null} if not
     */
    private RoomAssignment addTwoGroupsOf3(final int newId) {

        RoomAssignment ra = null;

        final boolean tueOk = this.blocksFree[1] >= 3;
        final boolean thrOk = this.blocksFree[3] >= 3;

        if (tueOk && thrOk) {
            markAssignedBlock(1, 3, newId);
            ra = addAssignedBlock(3, 3, newId, EAssignmentType.GROUPS_OF_3);
        } else {
            final int minFreeMW = Math.min(this.blocksFree[0], this.blocksFree[2]);
            final int minFreeMWF = Math.min(minFreeMW, this.blocksFree[4]);

            final boolean monOk = this.blocksFree[0] >= 4;
            final boolean wedOk = this.blocksFree[2] >= 4;
            final boolean friOk = this.blocksFree[4] >= 4;

            if (this.blocksFree[0] == minFreeMWF && wedOk && friOk) {
                markAssignedBlock(2, 4, newId);
                ra = addAssignedBlock(4, 4, newId, EAssignmentType.GROUPS_OF_3);
            } else if (this.blocksFree[2] == minFreeMWF && monOk && friOk) {
                markAssignedBlock(0, 4, newId);
                ra = addAssignedBlock(4, 4, newId, EAssignmentType.GROUPS_OF_3);
            } else if (this.blocksFree[4] == minFreeMWF && monOk && wedOk) {
                markAssignedBlock(0, 4, newId);
                ra = addAssignedBlock(2, 4, newId, EAssignmentType.GROUPS_OF_3);
            }

            // TODO: Otherwise, try to rebuild schedule
        }

        return ra;
    }

    /**
     * Adds a course that occupies three groups of 2 25-minute blocks on separate days.
     *
     * <p>
     * This method will check whether M/W/F all have space, and assign the class there if possible, but it will allocate
     * blocks of 4 rather than 3 to keep the schedule aligned.
     *
     * @param newId the new assignment ID
     * @return the room assignment if successful; {@code null} if not
     */
    private RoomAssignment addThreeGroupsOf3(final int newId) {

        RoomAssignment ra = null;

        // Try to assign to M/W/F

        final boolean monOk = this.blocksFree[0] >= 4;
        final boolean wedOk = this.blocksFree[2] >= 4;
        final boolean friOk = this.blocksFree[4] >= 4;

        if (monOk && wedOk && friOk) {
            markAssignedBlock(0, 4, newId);
            markAssignedBlock(2, 4, newId);
            ra = addAssignedBlock(4, 4, newId, EAssignmentType.GROUPS_OF_3);
        }

        // TODO: Otherwise, try to rebuild schedule

        return ra;
    }

    /**
     * Searches for a day index for which there are at least some number of blocks in a contiguous run.
     *
     * @param num25MinBlocks the number of blocks to find
     * @return the day index; -1 if there are no days with the required capacity
     */
    private int findDayWithContiguousBlocks(final int num25MinBlocks) {

        final int dayIndex;

        // There is a preference order for contiguous blocks...

        if (this.blocksFree[3] >= num25MinBlocks) { // Thursday
            dayIndex = 3;
        } else if (this.blocksFree[1] >= num25MinBlocks) { // Tuesday
            dayIndex = 1;
        } else if (this.blocksFree[4] >= num25MinBlocks) { // Friday
            dayIndex = 4;
        } else if (this.blocksFree[2] >= num25MinBlocks) { // Wednesday
            dayIndex = 2;
        } else if (this.blocksFree[0] >= num25MinBlocks) { // Monday
            dayIndex = 0;
        } else {
            dayIndex = -1;
        }

        return dayIndex;
    }

    /**
     * Adds an assigned block to a day's schedule.  It is assumed that the day has already been verified to have
     * sufficient capacity for the assignment
     *
     * @param dayIndex       the day index (from 0 to NUM_WEEKDAYS - 1)
     * @param num25MinBlocks the number of 25-minute blocks to consume within the indicated day
     * @param assignmentId   the assignment ID with which to populate those blocks
     * @param type           the assignment type
     * @return the new assignment object
     */
    private RoomAssignment addAssignedBlock(final int dayIndex, final int num25MinBlocks, final int assignmentId,
                                            final EAssignmentType type) {

        markAssignedBlock(dayIndex, num25MinBlocks, assignmentId);

        final RoomAssignment assignment = new RoomAssignment(this, assignmentId, num25MinBlocks, type);
        this.assignments.add(assignment);

        return assignment;
    }

    /**
     * Marks a range of time blocks on a specified day with a given assignment ID, and reduces the number of free blocks
     * on that day by that number of blocks.
     *
     * @param dayIndex       the day index (from 0 to NUM_WEEKDAYS - 1)
     * @param num25MinBlocks the number of 25-minute blocks to consume within the indicated day
     * @param assignmentId   the assignment ID with which to populate those blocks
     */
    private void markAssignedBlock(final int dayIndex, final int num25MinBlocks, final int assignmentId) {

        final int[] array = this.timeBlockGrid[dayIndex];
        final int len = array.length;

        int start = -1;
        for (int i = 0; i < len; ++i) {
            if (array[i] == 0) {
                start = i;
                break;
            }
        }
        final int remain = len - start;

        if (remain <= num25MinBlocks) {
            for (int i = 0; i < num25MinBlocks; ++i) {
                array[start + i] = assignmentId;
            }
            this.blocksFree[dayIndex] -= num25MinBlocks;
        } else {
            Log.warning("ERROR: Unable to assign block - insufficient capacity in day");
        }
    }

    /**
     * Gets the block schedule for a single weekday.  The returned array is a copy - modification of that array will not
     * affect the contents of this class.
     *
     * @param theWeekday the weekday
     * @return an array where each entry represents a 25-minute block of time, and the value is either 0 if the block is
     *         not occupied, or the ID of the assignment that occupies the room at that time
     */
    int[] getBlockSchedule(final DayOfWeek theWeekday) {

        final int index = theWeekday.getValue() - 1;

        final int[] result;

        if (index < 0 || index >= this.timeBlockGrid.length) {
            result = EMPTY_BLOCK_SCHEDULE;
        } else {
            result = this.timeBlockGrid[index].clone();
        }

        return result;
    }

    /**
     * Computes a hash code for the object.
     *
     * @return the hash code
     */
    public int hashCode() {

        return this.id.hashCode();
    }

    /**
     * Tests whether this object is equal to another.  Equality of this class is tested only on equality of the unique
     * course ID.
     *
     * @param obj the other object
     * @return true if this object is equal
     */
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final Room room) {
            final String objId = room.getId();
            equal = this.id.equals(objId);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Compares this object to another for order.  Ordering is based on room capacity.
     *
     * @param o the object to be compared
     * @return the value 0 if this object's capacity equals the other object's capacity a value less than 0 if this
     *         object's capacity is less than that of the other; and a value greater than 0 if this object's capacity is
     *         greater than that of the other
     */
    @Override
    public int compareTo(final Room o) {

        final int cap = o.getCapacity();

        return Integer.compare(this.capacity, cap);
    }
}

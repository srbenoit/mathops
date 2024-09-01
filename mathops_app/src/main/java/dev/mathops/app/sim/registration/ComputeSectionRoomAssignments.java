package dev.mathops.app.sim.registration;

import dev.mathops.commons.log.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Given a list of courses with required seat counts and a set of available rooms, this class calculates the set of
 * sections needed and assigns each section to a room.
 */
enum ComputeSectionRoomAssignments {
    ;

    /**
     * Calculates sections and room assignments.
     *
     * @param courses the list of courses offered (with the number of seats needed populated)
     * @param rooms   the set of available rooms
     * @return true if a set of section assignments was found that provides room space for all courses; false if not
     */
    static boolean canCompute(final Collection<Course> courses, final Rooms rooms) {

        for (final Course course : courses) {
            course.clearRoomAssignments();
        }

        rooms.reset();

        boolean result = true;

        // Consider each possible usage, in turn.
        for (final ERoomUsage usage : ERoomUsage.values()) {

            // Build a list of courses that have yet to be assigned; we will delete items from this list as they are
            // assigned.  This list will be sorted based on the number of seats needed (small to large)
            final List<Course> toBeAssigned = makeListOfCoursesToBeAssigned(courses, usage);

            assignRooms(toBeAssigned, usage, rooms);
            final int numUnassigned = toBeAssigned.size();

            if (numUnassigned > 0) {
                // NOTE: this leaves the room data in an incomplete state, but on a failure here, the data is discarded
                result = false;
                break;
            }
        }

        if (result) {
            // At this point, the rooms hold the list of their assignments - copy this information over to the courses
            // so we can present each course's information more easily
            for (final Room room : rooms.getRooms()) {
                for (final RoomAssignment assignment : room.getAssignments()) {
                    final ERoomUsage usage = assignment.usage();
                    assignment.course().addRoomAssignment(usage, assignment);
                }
            }
        }

        return result;
    }

    /**
     * Scan a list of courses for all that have a nonzero number of needed seats and have a need for a specified room
     * usage, and assemble into a list of courses to be assigned to rooms.  The resulting list is sorted by number of
     * seats needed (low to high).
     *
     * @param courses the list of all courses
     * @param usage   the usage currently being assigned
     * @return the list of courses that need to be assigned to rooms
     */
    private static List<Course> makeListOfCoursesToBeAssigned(final Collection<Course> courses,
                                                              final ERoomUsage usage) {

        final int count = courses.size();

        final List<Course> toBeAssigned = new ArrayList<>(count);
        for (final Course course : courses) {
            if (course.getNumSeatsNeeded() > 0 && course.getUsages().contains(usage)) {
                toBeAssigned.add(course);
            }
        }

        toBeAssigned.sort((o1, o2) -> {
            final int o1Needed = o1.getNumSeatsNeeded();
            final int o2Needed = o2.getNumSeatsNeeded();
            return Integer.compare(o1Needed, o2Needed);
        });

        final int numToBeAssigned = toBeAssigned.size();
        final String numToBeAssignedStr = Integer.toString(numToBeAssigned);
        final String usageName = usage.name();
        Log.info("There are ", numToBeAssignedStr, " courses that need to be assigned a ", usageName);

        return toBeAssigned;
    }

    /**
     * Scans a list of courses to find the smallest number of contact hours.
     *
     * @param usage   the room usage
     * @param courses the list of courses
     * @return the smallest number of class contact hours found for the specified usage
     */
    private static int getSmallestContactHours(final ERoomUsage usage, final Iterable<Course> courses) {

        int smallest = Integer.MAX_VALUE;

        for (final Course course : courses) {
            final int contactHours = course.getContactHours(usage);
            if (contactHours < smallest) {
                smallest = contactHours;
            }
        }

        return smallest;
    }

    /**
     * Given a set of rooms and a minimum number of contact hours, identifies a sub-list that has at least that minimum
     * number of hours available in a week.
     *
     * @param rooms                the set of all rooms
     * @param smallestContactHours the smallest allowed number of available hours
     * @return the list of classrooms having at least the smallest number of available hours
     */
    private static List<Room> findRoomsOfInterest(final Rooms rooms, final int smallestContactHours) {

        final List<Room> allRooms = rooms.getRooms();
        final int numRooms = allRooms.size();

        final List<Room> roomsOfInterest = new ArrayList<>(numRooms);
        for (final Room room : allRooms) {
            if (room.getTotalHoursFree() >= smallestContactHours) {
                roomsOfInterest.add(room);
            }
        }

        return roomsOfInterest;
    }

    /**
     * Given a list of courses to be assigned, and a list of groups of rooms, attempts to assign as many courses to
     * groups as possible.
     *
     * @param toBeAssigned the list of courses that need to be assigned to rooms for this specific usage (this list is
     *                     altered within this method - courses are removed as they are assigned)
     * @param usage        the usage being assigned
     * @param groups       a list of potential classroom groups across which to try to split sections that are too large
     *                     to schedule in a single room
     */
    private static void assignSectionsToGroups(final Iterable<Course> toBeAssigned, final ERoomUsage usage,
                                               final Iterable<Rooms> groups) {

        final Collection<RoomAssignment> assignmentsMade = new ArrayList<>(10);

        for (final Rooms group : groups) {
            final int seatsAvail = group.totalCapacity();
            final int hoursAvail = group.totalHoursFree();
            final List<Room> rooms = group.getRooms();

            final Iterator<Course> iterator = toBeAssigned.iterator();
            while (iterator.hasNext()) {
                final Course course = iterator.next();

                if (course.areRoomsCompatible(usage, group)) {
                    final int hoursNeeded = course.getContactHours(usage);
                    final int seatsNeeded = course.getNumSeatsNeeded();

                    if (hoursNeeded <= hoursAvail && seatsNeeded <= seatsAvail) {

                        assignmentsMade.clear();
                        boolean fail = false;

                        for (final Room room : rooms) {
                            final int roomCap = room.getCapacity();
                            final int seatsToAssign = Math.min(seatsNeeded, roomCap);
                            final EAssignmentType type = course.getAssignmentType(usage);

                            final Optional<RoomAssignment> assignment = room.addAssignment(hoursNeeded * 2, type,
                                    course, seatsToAssign, usage);

                            if (assignment.isPresent()) {
                                assignmentsMade.add(assignment.get());
                            } else {
                                fail = true;
                                break;
                            }
                        }

                        if (fail) {
                            // At least one room could not accommodate the course (based on the needed distribution of
                            // blocks of time), so roll back all assignments for this group
                            for (final RoomAssignment assign : assignmentsMade) {
                                assign.room().removeAssignment(assign);
                            }
                        } else {
                            // This course has been assigned successfully
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    /**
     * Assigns course sections to classrooms.
     *
     * @param toBeAssigned the list of courses that need to be assigned to rooms (this list is altered within this
     *                     method - courses are removed as they are assigned)
     * @param usage        the room usage being considered
     * @param rooms        the set of rooms
     */
    private static void assignRooms(final List<Course> toBeAssigned, final ERoomUsage usage, final Rooms rooms) {

        // Classroom Pass 1: take the smallest classroom and assign it to all courses that will fit; then move to the
        // next smallest classroom, and so on, for all classrooms

        assignSingleSectionCourses(toBeAssigned, usage, rooms);

        // At this point, all classes that can be offered in one section have been processed.  If we are finished, do
        // no more work.  Otherwise, we need to split high-enrollment courses into multiple sections.

        if (!toBeAssigned.isEmpty()) {

            // Classroom Pass 2: Generate all combinations of 2 rooms, and sort that list by total capacity.  Then
            // repeat the above assignment process for these groups.

            assign2SectionCourses(toBeAssigned, usage, rooms);

            // At this point, all classes that can be offered in one or two sections have been processed.  If we are
            // finished, do no more work.

            if (!toBeAssigned.isEmpty()) {

                // Classroom Pass 3: Generate all combinations of 3 rooms, and sort that list by total capacity.  Then
                // repeat the above assignment process for these groups.

                assign3SectionCourses(toBeAssigned, usage, rooms);

                if (!toBeAssigned.isEmpty()) {

                    // Classroom Pass 4: all that remain at this point are courses too large to split into 3 or fewer
                    // sections.  We change our strategy here to grab the course with the greatest size, and start
                    // assigning classrooms from the largest downward, until we run out of classrooms our have assigned
                    // all courses.

                    assignLargeCourses(toBeAssigned, usage, rooms);
                }
            }
        }
    }

    /**
     * Scans for all courses that can be accommodated with a single section in a room, and makes those assignments,
     * removing those courses from the list to be assigned.
     *
     * @param toBeAssigned the list of courses that need to be assigned to classrooms (this list is altered within this
     *                     method - courses are removed as they are assigned)
     * @param usage        the room usage being considered
     * @param rooms        the set of rooms
     */
    private static void assignSingleSectionCourses(final Iterable<Course> toBeAssigned, final ERoomUsage usage,
                                                   final Rooms rooms) {

        for (final Room room : rooms.getRooms()) {
            final int seats = room.getCapacity();

            final Iterator<Course> iterator = toBeAssigned.iterator();
            while (iterator.hasNext()) {
                final Course course = iterator.next();

                if (course.isRoomCompatible(usage, room)) {
                    final int seatsNeeded = course.getNumSeatsNeeded();
                    if (seatsNeeded <= seats) {
                        final int hours = course.getContactHours(usage);
                        final EAssignmentType type = course.getAssignmentType(usage);

                        final Optional<RoomAssignment> assignment = room.addAssignment(
                                hours * 2, type, course, seatsNeeded, usage);

                        if (assignment.isPresent()) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    /**
     * Scans for all courses that can be accommodated with two sections and makes those assignments, removing those
     * courses from the list to be assigned.
     *
     * @param toBeAssigned the list of courses that need to be assigned to classrooms (this list is altered within this
     *                     method - courses are removed as they are assigned)
     * @param usage        the room usage being considered
     * @param rooms        the set of rooms
     */
    private static void assign2SectionCourses(final Iterable<Course> toBeAssigned, final ERoomUsage usage,
                                              final Rooms rooms) {

        // Find the smallest number of hours needed for any remaining course, and then remove from consideration any
        // classrooms that do not have at least that many hours of availability per week remaining

        final int smallestContactHours = getSmallestContactHours(usage, toBeAssigned);
        final List<Room> roomsOfInterest = findRoomsOfInterest(rooms, smallestContactHours);

        // Make sure there are at least 2 classrooms that could accommodate courses - if not, we're done!

        final int numOfInterest = roomsOfInterest.size();
        if (numOfInterest > 1) {

            // Generate a set of all groups of 2 classrooms, then assign as many courses to those groups as possible

            final List<Rooms> groups = makeGroupsOf2(roomsOfInterest);
            assignSectionsToGroups(toBeAssigned, usage, groups);
        }
    }

    /**
     * Creates all possible groups of 2 classrooms from a list of classrooms of interest.
     *
     * @param roomsOfInterest the list of classrooms of interest
     * @return the list of groups of 2 classrooms, sorted by total capacity (if there are N classrooms of interest, this
     *         list will contain (N)(N-1) entries)
     */
    private static List<Rooms> makeGroupsOf2(final List<Room> roomsOfInterest) {

        final int size = roomsOfInterest.size();
        final int numGroups = size * (size - 1);
        final List<Rooms> groups = new ArrayList<>(numGroups);

        for (int i = 0; i < (size - 1); ++i) {
            final Room room1 = roomsOfInterest.get(i);
            for (int j = i + 1; j < size; ++j) {
                final Room room2 = roomsOfInterest.get(j);
                final Rooms group = new Rooms(room1, room2);
                groups.add(group);
            }
        }

        groups.sort(null);

        return groups;
    }

    /**
     * Scans for all courses that can be accommodated with three sections and makes those assignments, removing those
     * courses from the list to be assigned.
     *
     * @param toBeAssigned the list of courses that need to be assigned to classrooms (this list is altered within this
     *                     method - courses are removed as they are assigned)
     * @param usage        the room usage being considered
     * @param rooms        the set of rooms
     */
    private static void assign3SectionCourses(final Iterable<Course> toBeAssigned, final ERoomUsage usage,
                                              final Rooms rooms) {

        // Find the smallest number of hours needed for any remaining course, and then remove from consideration any
        // classrooms that do not have at least that many hours of availability per week remaining

        final int smallestContactHours = getSmallestContactHours(ERoomUsage.CLASSROOM, toBeAssigned);
        final List<Room> roomsOfInterest = findRoomsOfInterest(rooms, smallestContactHours);

        // Make sure there are at least 3 classrooms that could accommodate courses - if not, we're done!

        final int numOfInterest = roomsOfInterest.size();
        if (numOfInterest > 2) {

            // Generate a set of all groups of 3 classrooms, then assign as many courses to those groups as possible

            final List<Rooms> groups = makeGroupsOf3(roomsOfInterest);
            assignSectionsToGroups(toBeAssigned, usage, groups);
        }
    }

    /**
     * Creates all possible groups of 3 classrooms from a list of classrooms of interest.
     *
     * @param roomsOfInterest the list of classrooms of interest
     * @return the list of groups of 3 classrooms, sorted by total capacity (if there are N classrooms of interest, this
     *         list will contain (N)(N-1)(N-2) entries)
     */
    private static List<Rooms> makeGroupsOf3(final List<Room> roomsOfInterest) {

        final int size = roomsOfInterest.size();
        final int numGroups = size * (size - 1) * (size - 2);
        final List<Rooms> groups = new ArrayList<>(numGroups);

        for (int i = 0; i < (size - 2); ++i) {
            final Room room1 = roomsOfInterest.get(i);
            for (int j = i + 1; j < (size - 1); ++j) {
                final Room room2 = roomsOfInterest.get(j);
                for (int k = j + 1; k < size; ++k) {
                    final Room room3 = roomsOfInterest.get(k);
                    final Rooms group = new Rooms(room1, room2, room3);
                    groups.add(group);
                }
            }
        }

        groups.sort(null);

        return groups;
    }

    /**
     * Scans for all courses that cannot be accommodated with three or fewer sections and makes those assignments,
     * removing those courses from the list to be assigned.
     *
     * @param toBeAssigned the list of courses that need to be assigned to classrooms (this list is altered within this
     *                     method - courses are removed as they are assigned)
     * @param usage        the room usage being considered
     * @param rooms        the set of rooms
     */
    private static void assignLargeCourses(final List<Course> toBeAssigned, final ERoomUsage usage, final Rooms rooms) {

        // As before, we find the smallest number of hours needed for any remaining course, and then remove from
        // consideration any classrooms that do not have at least that many hours of availability per week remaining

        final int smallestContactHours = getSmallestContactHours(usage, toBeAssigned);
        final List<Room> roomsOfInterest = findRoomsOfInterest(rooms, smallestContactHours);

        // The list of courses to be assigned will be sorted in increasing order by needed capacity, so we start at the
        // end (the largest capacity need) and work downward.  For each course, we work downward through compatible
        // classrooms (from largest to smallest), and try to assemble a set of classrooms that can meet the need.

        final int numCourses = toBeAssigned.size();
        final int numClassrooms = roomsOfInterest.size();

        // We track assignments made for each course so that, if we cannot ultimately assign the course, we can remove
        // its partial list of assignments from classrooms
        final Collection<RoomAssignment> assignmentsMade = new ArrayList<>(10);

        for (int i = numCourses - 1; i >= 0; --i) {
            final Course course = toBeAssigned.get(i);
            final int hoursNeeded = course.getContactHours(usage);
            int seatsNeeded = course.getNumSeatsNeeded();
            final EAssignmentType type = course.getAssignmentType(usage);

            // Pass 1: scan from the largest room down to the smallest, assigning as possible, until we have met the
            // required capacity.

            assignmentsMade.clear();
            RoomAssignment last = null;
            for (int j = numClassrooms - 1; j >= 0; --j) {
                final Room room = roomsOfInterest.get(j);

                if (course.isRoomCompatible(usage, room)) {
                    final int roomCap = room.getCapacity();
                    final int seatsToAssign = Math.min(roomCap, seatsNeeded);

                    final Optional<RoomAssignment> assignment = room.addAssignment(hoursNeeded * 2, type, course,
                            seatsToAssign, usage);

                    if (assignment.isPresent()) {
                        last = assignment.get();
                        assignmentsMade.add(last);
                        seatsNeeded -= seatsToAssign;
                        if (seatsNeeded <= 0) {
                            break;
                        }
                    }
                }
            }

            if (seatsNeeded <= 0) {

                // Pass 2: the last room we allocated might not have been the smallest that could have still met class
                // capacity needs, so start from the small end and see if we could make an alternative assignment that
                // meets needs more efficiently.

                if (Objects.nonNull(last)) {

                    // The list of potential classrooms will work - make the assignments
                    final int numSeatsInLast = last.numSeats();

                    for (int j = 0; j < numClassrooms; ++j) {
                        final Room room = roomsOfInterest.get(j);
                        if (room.getCapacity() < numSeatsInLast) {
                            continue;
                        }

                        final Optional<RoomAssignment> assignment = room.addAssignment(hoursNeeded * 2, type, course,
                                numSeatsInLast, usage);

                        if (assignment.isPresent()) {
                            // Alternate assignment worked - replace the earlier "last" assignment
                            last.room().removeAssignment(last);
                            break;
                        }
                    }

                    // This course has been assigned - remove it from the "toBeAssigned" list
                    toBeAssigned.remove(i);
                }
            }
        }
    }
}

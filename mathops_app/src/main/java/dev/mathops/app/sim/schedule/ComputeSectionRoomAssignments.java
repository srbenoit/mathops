package dev.mathops.app.sim.schedule;

import dev.mathops.app.sim.courses.Course;
import dev.mathops.app.sim.rooms.ERoomUsage;
import dev.mathops.app.sim.rooms.Room;
import dev.mathops.app.sim.rooms.Rooms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
     * @return the sections for each course if a set of section assignments was found that provides room space for all
     *         courses; an empty map if not
     */
    static Map<Course, List<AbstractSection>> compute(final Collection<Course> courses, final List<Room> rooms) {

        for (final Room room : rooms) {
            room.clearSections();
        }

        boolean success = true;
        // Consider each possible usage, in turn.
        for (final ERoomUsage usage : ERoomUsage.values()) {

            // Build a list of courses that have yet to be assigned; we will delete items from this list as they are
            // assigned.  This list will be sorted based on the number of seats needed (small to large)
            final List<Course> toBeAssigned = makeListOfCoursesToBeAssigned(courses, usage);

            assignRooms(toBeAssigned, usage, rooms);
            final int numUnassigned = toBeAssigned.size();

            if (numUnassigned > 0) {
                // NOTE: this leaves the room data in an incomplete state, but on a failure here, the data is discarded
                success = false;
                break;
            }
        }

        final int numCourses = courses.size();
        final Map<Course, List<AbstractSection>> result = new HashMap<>(numCourses);

        if (success) {
            // Copy room section data to courses if successful
            for (final Room room : rooms) {
                for (final SectionMWF sect : room.getSectionsMWF()) {
                    final Course course = sect.course();
                    final List<AbstractSection> list = result.computeIfAbsent(course, x -> new ArrayList<>(10));
                    list.add(sect);
                }
                for (final SectionTR sect : room.getSectionsTR()) {
                    final Course course = sect.course();
                    final List<AbstractSection> list = result.computeIfAbsent(course, x -> new ArrayList<>(10));
                    list.add(sect);
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

        return toBeAssigned;
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

        final Collection<AbstractSection> assignmentsMade = new ArrayList<>(10);
        final Collection<Room> iterationRooms = new ArrayList<>(5);

        for (final Rooms group : groups) {
            final int seatsAvail = group.totalCapacity();
            final int hoursAvail = group.totalHoursFree();
            iterationRooms.clear();
            group.getRooms(iterationRooms);

            final Iterator<Course> iterator = toBeAssigned.iterator();
            while (iterator.hasNext()) {
                final Course course = iterator.next();

                if (course.areRoomsCompatible(usage, iterationRooms)) {
                    final int hoursNeeded = course.getContactHours(usage);
                    int seatsNeeded = course.getNumSeatsNeeded();

                    if (hoursNeeded <= hoursAvail && seatsNeeded <= seatsAvail) {
                        // This group could potentially handle this course

                        assignmentsMade.clear();

                        for (final Room room : iterationRooms) {
                            if (course.isRoomCompatible(usage, room)) {
                                final int roomCap = room.getCampusRoom().getCapacity();
                                final int maxSeats = Math.min(roomCap, course.enrollmentCap);

                                final int seatsToAssign = Math.min(seatsNeeded, maxSeats);
                                final EAssignmentType type = course.getAssignmentType(usage);

                                final AbstractSection sect = addSectionToRoom(type, room, hoursNeeded, course,
                                        seatsToAssign, usage);

                                if (sect != null) {
                                    assignmentsMade.add(sect);
                                    seatsNeeded -= seatsToAssign;
                                    if (seatsNeeded <= 0) {
                                        break;
                                    }
                                }
                            }
                        }

                        if (seatsNeeded > 0) {
                            // There is not enough capacity in this group to accommodate the course - undo our
                            // assignments and try the next group
                            for (final AbstractSection assign : assignmentsMade) {
                                if (assign instanceof final SectionMWF s1) {
                                    assign.room().removeSection(s1);
                                } else if (assign instanceof final SectionTR s2) {
                                    assign.room().removeSection(s2);
                                }
                            }
                        } else {
                            // This course has been assigned successfully!
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    /**
     * Try to assign a section of a course in a room (that has been verified to be compatible with the course).
     *
     * @param type          the assignment type
     * @param room          the room
     * @param hoursNeeded   the number of hours needed
     * @param course        the course
     * @param seatsToAssign the number of seats to assign to this room
     * @param usage         the usage
     * @return the new number of seats needed
     */
    private static AbstractSection addSectionToRoom(final EAssignmentType type, final Room room, final int hoursNeeded,
                                                    final Course course, final int seatsToAssign,
                                                    final ERoomUsage usage) {

        AbstractSection result = null;

        if (type == EAssignmentType.BLOCKS_OF_50) {
            final Optional<SectionMWF> section = room.addSectionMWF(hoursNeeded, EAssignmentType.BLOCKS_OF_50, course,
                    seatsToAssign, usage);

            if (section.isPresent()) {
                result = section.get();
            }
        } else if (type == EAssignmentType.BLOCKS_OF_75) {
            if (hoursNeeded == 1) {
                final Optional<SectionTR> section = room.addSectionTR(1, EAssignmentType.BLOCKS_OF_75, course,
                        seatsToAssign, usage);
                if (section.isPresent()) {
                    result = section.get();
                }
            } else if (hoursNeeded == 2 || hoursNeeded == 3) {
                final Optional<SectionTR> section = room.addSectionTR(2, EAssignmentType.BLOCKS_OF_75, course,
                        seatsToAssign, usage);
                if (section.isPresent()) {
                    result = section.get();
                }
            }
        } else if (type == EAssignmentType.BLOCKS_OF_50_OR_75) {
            final Optional<SectionMWF> sectionMWF = room.addSectionMWF(hoursNeeded, EAssignmentType.BLOCKS_OF_50,
                    course, seatsToAssign, usage);

            if (sectionMWF.isPresent()) {
                result = sectionMWF.get();
            } else {
                if (hoursNeeded == 1) {
                    final Optional<SectionTR> section = room.addSectionTR(1, EAssignmentType.BLOCKS_OF_75, course,
                            seatsToAssign, usage);
                    if (section.isPresent()) {
                        result = section.get();
                    }
                } else if (hoursNeeded == 2 || hoursNeeded == 3) {
                    final Optional<SectionTR> section = room.addSectionTR(2, EAssignmentType.BLOCKS_OF_75, course,
                            seatsToAssign, usage);
                    if (section.isPresent()) {
                        result = section.get();
                    }
                }
            }
        } else if (type == EAssignmentType.CONTIGUOUS) {

            final int freeM = room.getFreeBlocksM();
            final int freeT = room.getFreeBlocksT();
            final int freeW = room.getFreeBlocksW();
            final int freeR = room.getFreeBlocksR();
            final int freeF = room.getFreeBlocksF();

            final int maxFreeTR = Math.max(freeT, freeR);
            final int maxFreeMW = Math.max(freeM, freeW);
            final int maxFreeMWF = Math.max(maxFreeMW, freeF);

            if (maxFreeTR >= hoursNeeded) {
                if (hoursNeeded == 1) {
                    final Optional<SectionTR> sectionTR = room.addSectionTR(1, EAssignmentType.CONTIGUOUS, course,
                            seatsToAssign, usage);
                    if (sectionTR.isPresent()) {
                        result = sectionTR.get();
                    }
                } else if (hoursNeeded == 2 || hoursNeeded == 3) {
                    final Optional<SectionTR> sectionTR = room.addSectionTR(2, EAssignmentType.CONTIGUOUS, course,
                            seatsToAssign, usage);
                    if (sectionTR.isPresent()) {
                        result = sectionTR.get();
                    }
                }

                if (result == null) {
                    final Optional<SectionMWF> section = room.addSectionMWF(hoursNeeded, EAssignmentType.CONTIGUOUS,
                            course, seatsToAssign, usage);

                    if (section.isPresent()) {
                        result = section.get();
                    }
                }
            } else {
                final Optional<SectionMWF> section = room.addSectionMWF(hoursNeeded, EAssignmentType.CONTIGUOUS,
                        course, seatsToAssign, usage);

                if (section.isPresent()) {
                    result = section.get();
                } else {
                    if (hoursNeeded == 1) {
                        final Optional<SectionTR> sectionTR = room.addSectionTR(1, EAssignmentType.CONTIGUOUS, course,
                                seatsToAssign, usage);
                        if (sectionTR.isPresent()) {
                            result = sectionTR.get();
                        }
                    } else if (hoursNeeded == 2 || hoursNeeded == 3) {
                        final Optional<SectionTR> sectionTR = room.addSectionTR(2, EAssignmentType.CONTIGUOUS, course,
                                seatsToAssign, usage);
                        if (sectionTR.isPresent()) {
                            result = sectionTR.get();
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Assigns course sections to classrooms.
     *
     * @param toBeAssigned the list of courses that need to be assigned to rooms (this list is altered within this
     *                     method - courses are removed as they are assigned)
     * @param usage        the room usage being considered
     * @param rooms        the set of rooms
     */
    private static void assignRooms(final List<Course> toBeAssigned, final ERoomUsage usage,
                                    final List<Room> rooms) {

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
                                                   final Iterable<Room> rooms) {

        for (final Room room : rooms) {
            final int roomCap = room.getCampusRoom().getCapacity();

            final Iterator<Course> iterator = toBeAssigned.iterator();
            while (iterator.hasNext()) {
                final Course course = iterator.next();

                if (course.isRoomCompatible(usage, room)) {
                    final int seatsNeeded = course.getNumSeatsNeeded();
                    final int maxSeats = Math.min(roomCap, course.enrollmentCap);

                    if (seatsNeeded <= maxSeats) {
                        final int hours = course.getContactHours(usage);
                        final EAssignmentType type = course.getAssignmentType(usage);
                        final AbstractSection sect = addSectionToRoom(type, room, hours, course, seatsNeeded, usage);

                        if (Objects.nonNull(sect)) {
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
                                              final List<Room> rooms) {

        // Generate a set of all groups of 2 classrooms, then assign as many courses to those groups as possible

        final List<Rooms> groups = makeGroupsOf2(rooms);
        assignSectionsToGroups(toBeAssigned, usage, groups);
    }

    /**
     * Creates all possible groups of 2 classrooms from a list of classrooms of interest.
     *
     * @param rooms the list of classrooms of interest
     * @return the list of groups of 2 classrooms, sorted by total capacity (if there are N classrooms of interest, this
     *         list will contain (N+1)(N)/2 entries)
     */
    private static List<Rooms> makeGroupsOf2(final List<Room> rooms) {

        final int size = rooms.size();
        final int numGroups = size * (size - 1);
        final List<Rooms> groups = new ArrayList<>(numGroups);

        // Note, we include pairings with the same room in each slot, like "room 1 and room 1".

        for (int i = 0; i < size; ++i) {
            final Room room1 = rooms.get(i);
            for (int j = i; j < size; ++j) {
                final Room room2 = rooms.get(j);
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
                                              final List<Room> rooms) {

        final List<Rooms> groups = makeGroupsOf3(rooms);
        assignSectionsToGroups(toBeAssigned, usage, groups);
    }

    /**
     * Creates all possible groups of 3 classrooms from a list of classrooms of interest.
     *
     * @param roomsOfInterest the list of classrooms of interest
     * @return the list of groups of 3 classrooms, sorted by total capacity (if there are N classrooms of interest, this
     *         list will contain (N+1)(N)(N-1)/2 entries)
     */
    private static List<Rooms> makeGroupsOf3(final List<Room> roomsOfInterest) {

        final int size = roomsOfInterest.size();
        final int numGroups = size * (size - 1) * (size - 2);
        final List<Rooms> groups = new ArrayList<>(numGroups);

        // Note, we include triples with the same room in each slot, like "room 1 and room 1 and room 1".

        for (int i = 0; i < size; ++i) {
            final Room room1 = roomsOfInterest.get(i);
            for (int j = i; j < size; ++j) {
                final Room room2 = roomsOfInterest.get(j);
                for (int k = j; k < size; ++k) {
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
    private static void assignLargeCourses(final List<Course> toBeAssigned, final ERoomUsage usage,
                                           final List<Room> rooms) {

        // The list of courses to be assigned will be sorted in increasing order by needed capacity, so we start at the
        // end (the largest capacity need) and work downward.  For each course, we work downward through compatible
        // classrooms (from largest to smallest), and try to assemble a set of classrooms that can meet the need.

        final int numCourses = toBeAssigned.size();
        final int numRooms = rooms.size();

        // We track assignments made for each course so that, if we cannot ultimately assign the course, we can remove
        // its partial list of assignments from classrooms
        final Collection<AbstractSection> assignmentsMade = new ArrayList<>(10);

        for (int i = numCourses - 1; i >= 0; --i) {
            final Course course = toBeAssigned.get(i);

            final int hoursNeeded = course.getContactHours(usage);
            int seatsNeeded = course.getNumSeatsNeeded();
            final EAssignmentType type = course.getAssignmentType(usage);

            // Pass 1: scan from the largest room down to the smallest, assigning as possible, until we have met the
            // required capacity.

            assignmentsMade.clear();
            AbstractSection last = null;
            for (int j = numRooms - 1; j >= 0; --j) {
                final Room room = rooms.get(j);

                if (course.isRoomCompatible(usage, room)) {
                    final int roomCap = room.getCampusRoom().getCapacity();
                    final int maxSeats = Math.min(roomCap, course.enrollmentCap);
                    final int seatsToAssign = Math.min(seatsNeeded, maxSeats);

                    final AbstractSection sect = addSectionToRoom(type, room, hoursNeeded, course, seatsToAssign,
                            usage);

                    if (Objects.nonNull(sect)) {
                        last = sect;
                        assignmentsMade.add(last);
                        seatsNeeded -= seatsToAssign;
                        if (seatsNeeded <= 0) {
                            break;
                        }
                        ++j; // Try this room again!
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

                    for (int j = 0; j < numRooms; ++j) {
                        final Room room = rooms.get(j);
                        if (room == last.room()) {
                            break;
                        }
                        if (room.getCampusRoom().getCapacity() < numSeatsInLast) {
                            continue;
                        }
                        if (course.isRoomCompatible(usage, room)) {
                            final AbstractSection sect = addSectionToRoom(type, room, hoursNeeded, course,
                                    numSeatsInLast, usage);

                            if (Objects.nonNull(sect)) {
                                // Alternate assignment worked - replace the earlier "last" assignment
                                if (last instanceof final SectionMWF s1) {
                                    last.room().removeSection(s1);
                                } else if (last instanceof final SectionTR s2) {
                                    last.room().removeSection(s2);
                                }
                                break;
                            }
                        }
                    }

                    // This course has been assigned - remove it from the "toBeAssigned" list
                    toBeAssigned.remove(i);
                }
            } else {
                // A failed attempt, so we need to roll back assignments that were made
                for (final AbstractSection toRemove : assignmentsMade) {
                    if (last instanceof final SectionMWF s1) {
                        toRemove.room().removeSection(s1);
                    } else if (last instanceof final SectionTR s2) {
                        toRemove.room().removeSection(s2);
                    }
                }
            }
        }
    }
}

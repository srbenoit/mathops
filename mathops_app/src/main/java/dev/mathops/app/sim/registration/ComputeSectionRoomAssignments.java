package dev.mathops.app.sim.registration;

import dev.mathops.commons.log.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Given a list of courses with required seat counts and lists of available classrooms and labs, this class calculates
 * the set of sections and assigns each section to a room.
 */
enum ComputeSectionRoomAssignments {
    ;

    /**
     * Calculates sections and room assignments.
     *
     * @param courses       the list of courses offered (with the number of seats needed populated)
     * @param allClassrooms the list of available classrooms
     * @param allLabs       the list of available labs
     * @return the smallest of the number of remaining classroom capacity (hours per week) and the remaining lab
     *         capacity (hours per week), or -1 if there was not enough classroom or lab space to accommodate all
     *         courses
     */
    static int compute(final Collection<OfferedCourse> courses, final List<AvailableClassroom> allClassrooms,
                       final List<AvailableLab> allLabs) {

        initializeClassroomsAndLabs(allClassrooms, allLabs);

        final List<OfferedCourse> toBeAssignedClassrooms = makeListOfCoursesToBeAssignedClassrooms(courses);
        final List<OfferedCourse> toBeAssignedLabs = makeListOfCoursesToBeAssignedLabs(courses);

        final int classroomResult = assignClassrooms(toBeAssignedClassrooms, allClassrooms);
        final int labResult = classroomResult < 0 ? -1 : assignLabs(toBeAssignedLabs, allLabs);

        return Math.min(classroomResult, labResult);
    }

    /**
     * Initializes classroom and lab data structures by setting the hours remaining per week to the daily hours times 5
     * days, and clearing the list of sections assigned to each classroom or lab.  This also ensures that lists of
     * classrooms and labs are sorted by room capacity (from low to high).
     *
     * @param allClassrooms the list of all classrooms
     * @param allLabs       the list of all labs
     */
    private static void initializeClassroomsAndLabs(final List<AvailableClassroom> allClassrooms,
                                                    final List<AvailableLab> allLabs) {

        for (final AvailableClassroom classroom : allClassrooms) {
            classroom.setHoursRemainingInWeek(5 * classroom.hoursPerDay);
            classroom.clearAssignedSections();
        }

        for (final AvailableLab lab : allLabs) {
            lab.setHoursRemainingInWeek(5 * lab.hoursPerDay);
            lab.clearAssignedSections();
        }

        allClassrooms.sort(null);
        allLabs.sort(null);
    }

    /**
     * Scan a list of courses for all that have a nonzero number of needed seats and a nonzero number of classroom hours
     * per week, and assemble into a list of courses to be assigned to rooms.  The resulting list is sorted by number of
     * seats needed (low to high).
     *
     * @param courses the list of all courses
     * @return the list of courses that need to be assigned to classrooms
     */
    private static List<OfferedCourse> makeListOfCoursesToBeAssignedClassrooms(
            final Collection<OfferedCourse> courses) {

        final int count = courses.size();

        final List<OfferedCourse> toBeAssigned = new ArrayList<>(count);
        for (final OfferedCourse course : courses) {
            if (course.getNumSeatsNeeded() > 0 && course.classContactHours > 0) {
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
        Log.info("There are ", numToBeAssignedStr, " courses that need to be assigned to classrooms.");

        return toBeAssigned;
    }

    /**
     * Scan a list of courses for all that have a nonzero number of needed seats and a nonzero number of lab hours per
     * week, and assemble into a list of courses to be assigned to rooms.  The resulting list is sorted by number of
     * seats needed (low to high).
     *
     * @param courses the list of all courses
     * @return the list of courses that need to be assigned to labs
     */
    private static List<OfferedCourse> makeListOfCoursesToBeAssignedLabs(final Collection<OfferedCourse> courses) {

        final int count = courses.size();

        final List<OfferedCourse> toBeAssigned = new ArrayList<>(count);
        for (final OfferedCourse course : courses) {
            if (course.getNumSeatsNeeded() > 0 && course.labContactHours > 0) {
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
        Log.info("There are ", numToBeAssignedStr, " courses that need to be assigned to labs.");

        return toBeAssigned;
    }

    /**
     * Scans a list of courses to find the smallest number of class contact hours.
     *
     * @param courses the list of courses
     * @return the smallest number of class contact hours found
     */
    private static int getSmallestClassContactHours(final Iterable<OfferedCourse> courses) {

        int smallest = Integer.MAX_VALUE;

        for (final OfferedCourse course : courses) {
            if (course.classContactHours < smallest) {
                smallest = course.classContactHours;
            }
        }

        return smallest;
    }

    /**
     * Given a list of classrooms and a minimum number of class contact hours, identifies a sub-list that has at least
     * that minimum number of hours available in a week.
     *
     * @param allClassrooms        the list of all classrooms
     * @param smallestContactHours the smallest allowed number of available hours
     * @return the list of classrooms having at least the smallest number of available hours
     */
    private static List<AvailableClassroom> findClassroomsOfInterest(final Collection<AvailableClassroom> allClassrooms,
                                                                     final int smallestContactHours) {

        final int numClassrooms = allClassrooms.size();

        final List<AvailableClassroom> classroomsOfInterest = new ArrayList<>(numClassrooms);
        for (final AvailableClassroom classroom : allClassrooms) {
            if (classroom.getHoursRemainingInWeek() >= smallestContactHours) {
                classroomsOfInterest.add(classroom);
            }
        }

        return classroomsOfInterest;
    }

    /**
     * Assigns course sections to classrooms.
     *
     * @param toBeAssigned  the list of courses that need to be assigned to classrooms (this list is altered within this
     *                      method - courses are removed as they are assigned)
     * @param allClassrooms the list of available classrooms
     * @return the number of remaining classroom capacity (hours per week); -1 if there was not sufficient classroom
     *         capacity to assign all courses
     */
    private static int assignClassrooms(final List<OfferedCourse> toBeAssigned,
                                        final Collection<AvailableClassroom> allClassrooms) {

        // Classroom Pass 1: take the smallest classroom and assign it to all courses that will fit; then move to the
        // next smallest classroom, and so on, for all classrooms

        assignSingleSectionCourses(toBeAssigned, allClassrooms);

        // At this point, all classes that can be offered in one section have been processed.  If we are finished, do
        // no more work.  Otherwise, we need to split high-enrollment courses into multiple sections.

        if (!toBeAssigned.isEmpty()) {

            // Classroom Pass 2: Generate all combinations of 2 rooms, and sort that list by total capacity.  Then
            // repeat the above assignment process for these groups.

            assign2SectionCourses(toBeAssigned, allClassrooms);

            // At this point, all classes that can be offered in one or two sections have been processed.  If we are
            // finished, do no more work.

            if (!toBeAssigned.isEmpty()) {

                // Classroom Pass 3: Generate all combinations of 3 rooms, and sort that list by total capacity.  Then
                // repeat the above assignment process for these groups.

                assign3SectionCourses(toBeAssigned, allClassrooms);

                if (!toBeAssigned.isEmpty()) {

                    // Classroom Pass 4: all that remain at this point are courses too large to split into 3 or fewer
                    // sections.  We change our strategy here to grab the course with the greatest size, and start
                    // assigning classrooms from the largest downward, until we run out of classrooms our have assigned
                    // all courses.

                    assignLargeCourses(toBeAssigned, allClassrooms);
                }
            }
        }

        int result;

        if (toBeAssigned.isEmpty()) {
            // All classes have been assigned - see how many free hours per week remain in classrooms
            result = 0;
            for (final AvailableClassroom classroom : allClassrooms) {
                result += classroom.getHoursRemainingInWeek();
            }
        } else {
            // We were not able to assign all classes - there must not be enough classroom space

            result = -1;
        }

        return result;
    }

    /**
     * Scans for all courses that can be accommodated with a single section in a classroom, and makes those assignments,
     * removing those courses from the list to be assigned.
     *
     * @param toBeAssigned  the list of courses that need to be assigned to classrooms (this list is altered within this
     *                      method - courses are removed as they are assigned)
     * @param allClassrooms the list of available classrooms
     */
    private static void assignSingleSectionCourses(final Iterable<OfferedCourse> toBeAssigned,
                                                   final Iterable<AvailableClassroom> allClassrooms) {

        for (final AvailableClassroom classroom : allClassrooms) {
            final int seats = classroom.capacity;
            final int hoursAvail = classroom.getHoursRemainingInWeek();

            final Iterator<OfferedCourse> iterator = toBeAssigned.iterator();
            while (iterator.hasNext()) {
                final OfferedCourse course = iterator.next();
                if (course.isClassroomCompatible(classroom)) {
                    final int hoursNeeded = course.classContactHours;
                    final int seatsNeeded = course.getNumSeatsNeeded();

                    if (hoursNeeded <= hoursAvail && seatsNeeded <= seats) {
                        final AssignedSection sect = new AssignedSection(course, seatsNeeded);
                        classroom.decreaseHoursRemaining(hoursNeeded);
                        classroom.addAssignedSection(sect);
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * Scans for all courses that can be accommodated with two sections and makes those assignments, removing those
     * courses from the list to be assigned.
     *
     * @param toBeAssigned  the list of courses that need to be assigned to classrooms (this list is altered within this
     *                      method - courses are removed as they are assigned)
     * @param allClassrooms the list of available classrooms
     */
    private static void assign2SectionCourses(final Iterable<OfferedCourse> toBeAssigned,
                                              final Collection<AvailableClassroom> allClassrooms) {

        // Find the smallest number of hours needed for any remaining course, and then remove from consideration any
        // classrooms that do not have at least that many hours of availability per week remaining

        final int smallestContactHours = getSmallestClassContactHours(toBeAssigned);
        final List<AvailableClassroom> classroomsOfInterest = findClassroomsOfInterest(allClassrooms,
                smallestContactHours);

        // Make sure there are at least 2 classrooms that could accommodate courses - if not, we're done!

        final int numOfInterest = classroomsOfInterest.size();
        if (numOfInterest > 1) {

            // Generate a set of all groups of 2 classrooms, then assign as many courses to those groups as possible

            final List<AvailableClassroomGroup> groups = makeGroupsOf2(classroomsOfInterest);
            assignSectionsToGroups(toBeAssigned, groups);
        }
    }

    /**
     * Creates all possible groups of 2 classrooms from a list of classrooms of interest.
     *
     * @param classroomsOfInterest the list of classrooms of interest
     * @return the list of groups of 2 classrooms, sorted by total capacity (if there are N classrooms of interest, this
     *         list will contain (N)(N-1) entries)
     */
    private static List<AvailableClassroomGroup> makeGroupsOf2(final List<AvailableClassroom> classroomsOfInterest) {

        final int size = classroomsOfInterest.size();
        final int numGroups = size * (size - 1);
        final List<AvailableClassroomGroup> groups = new ArrayList<>(numGroups);

        for (int i = 0; i < (size - 1); ++i) {
            final AvailableClassroom room1 = classroomsOfInterest.get(i);
            for (int j = i + 1; j < size; ++j) {
                final AvailableClassroom room2 = classroomsOfInterest.get(j);
                final AvailableClassroomGroup group = new AvailableClassroomGroup(room1, room2);
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
     * @param toBeAssigned  the list of courses that need to be assigned to classrooms (this list is altered within this
     *                      method - courses are removed as they are assigned)
     * @param allClassrooms the list of available classrooms
     */
    private static void assign3SectionCourses(final Iterable<OfferedCourse> toBeAssigned,
                                              final Collection<AvailableClassroom> allClassrooms) {

        // Find the smallest number of hours needed for any remaining course, and then remove from consideration any
        // classrooms that do not have at least that many hours of availability per week remaining

        final int smallestContactHours = getSmallestClassContactHours(toBeAssigned);
        final List<AvailableClassroom> classroomsOfInterest = findClassroomsOfInterest(allClassrooms,
                smallestContactHours);

        // Make sure there are at least 3 classrooms that could accommodate courses - if not, we're done!

        final int numOfInterest = classroomsOfInterest.size();
        if (numOfInterest > 2) {

            // Generate a set of all groups of 3 classrooms, then assign as many courses to those groups as possible

            final List<AvailableClassroomGroup> groups = makeGroupsOf3(classroomsOfInterest);
            assignSectionsToGroups(toBeAssigned, groups);
        }
    }

    /**
     * Creates all possible groups of 3 classrooms from a list of classrooms of interest.
     *
     * @param classroomsOfInterest the list of classrooms of interest
     * @return the list of groups of 3 classrooms, sorted by total capacity (if there are N classrooms of interest, this
     *         list will contain (N)(N-1)(N-2) entries)
     */
    private static List<AvailableClassroomGroup> makeGroupsOf3(final List<AvailableClassroom> classroomsOfInterest) {

        final int size = classroomsOfInterest.size();
        final int numGroups = size * (size - 1) * (size - 2);
        final List<AvailableClassroomGroup> groups = new ArrayList<>(numGroups);

        for (int i = 0; i < (size - 2); ++i) {
            final AvailableClassroom room1 = classroomsOfInterest.get(i);
            for (int j = i + 1; j < (size - 1); ++j) {
                final AvailableClassroom room2 = classroomsOfInterest.get(j);
                for (int k = j + 1; k < size; ++k) {
                    final AvailableClassroom room3 = classroomsOfInterest.get(k);
                    final AvailableClassroomGroup group = new AvailableClassroomGroup(room1, room2, room3);
                    groups.add(group);
                }
            }
        }

        groups.sort(null);

        return groups;
    }

    /**
     * Scans for all courses that can be accommodated with four sections and makes those assignments, removing those
     * courses from the list to be assigned.
     *
     * @param toBeAssigned  the list of courses that need to be assigned to classrooms (this list is altered within this
     *                      method - courses are removed as they are assigned)
     * @param allClassrooms the list of available classrooms
     */
    private static void assignLargeCourses(final List<OfferedCourse> toBeAssigned,
                                           final Collection<AvailableClassroom> allClassrooms) {

        // As before, we find the smallest number of hours needed for any remaining course, and then remove from
        // consideration any classrooms that do not have at least that many hours of availability per week remaining

        final int smallestContactHours = getSmallestClassContactHours(toBeAssigned);
        final List<AvailableClassroom> classroomsOfInterest = findClassroomsOfInterest(allClassrooms,
                smallestContactHours);

        // The list of courses to be assigned will be sorted in increasing order by needed capacity, so we start at the
        // end (the largest capacity need) and work downward.  For each course, we work downward through compatible
        // classrooms (from largest to smallest), and try to assemble a set of classrooms that can meet the need.

        final int numCourses = toBeAssigned.size();
        final int numClassrooms = classroomsOfInterest.size();

        final Collection<AvailableClassroom> potentialClassrooms = new ArrayList<>(10);

        for (int i = numCourses - 1; i >= 0; --i) {
            final OfferedCourse course = toBeAssigned.get(i);
            final int hoursNeeded = course.classContactHours;
            int seatsNeeded = course.getNumSeatsNeeded();

            // Note: As we scan downward in classroom capacity, we might end up using a large room for a very small
            // set of students at the end.  To avoid this, we keep track of the "last" room allocated (when that room
            // would take needed capacity down to zero), and keep scanning for smaller rooms that could also serve as
            // the "last" room.

            potentialClassrooms.clear();
            AvailableClassroom last = null;
            for (int j = numClassrooms - 1; j >= 0; --j) {
                final AvailableClassroom classroom = classroomsOfInterest.get(j);
                if (course.isClassroomCompatible(classroom) && classroom.getHoursRemainingInWeek() >= hoursNeeded) {
                    if (last == null) {
                        // We are not yet scanning for the smallest "last" room)
                        if (seatsNeeded > classroom.capacity) {
                            // This one will not be the last - track it and move on
                            potentialClassrooms.add(classroom);
                            seatsNeeded -= classroom.capacity;
                        } else {
                            // This one would work as the "last" room - track as the current "last" room, but don't
                            // add yet to "potentialClassrooms" until we're sure it's the right "last" room to use.
                            last = classroom;
                        }
                    } else if (seatsNeeded <= classroom.capacity) {
                        // This room is smaller and will also work as the "last" room - track it as the current "last"
                        // room, but keep scanning
                        last = classroom;
                    }
                }
            }

            // If "last" is null here, we failed to find enough available classroom space, so do nothing.
            if (Objects.nonNull(last)) {
                potentialClassrooms.add(last);

                // The list of potential classrooms will work - make the assignments
                int stillNeeded = course.getNumSeatsNeeded();
                for (final AvailableClassroom classroom : potentialClassrooms) {

                    final AssignedSection sect;

                    if (classroom.capacity > stillNeeded) {
                        sect = new AssignedSection(course, stillNeeded);
                        stillNeeded = 0;
                    } else {
                        sect = new AssignedSection(course, classroom.capacity);
                        stillNeeded -= classroom.capacity;
                    }

                    classroom.decreaseHoursRemaining(hoursNeeded);
                    classroom.addAssignedSection(sect);
                }

                // This course has been assigned - remove it from the "toBeAssigned" list
                toBeAssigned.remove(i);
            }
        }
    }

    /**
     * Given a list of courses to be assigned, and a list of classroom groups, attempts to assign as many courses to
     * groups as possible.
     *
     * @param toBeAssigned the list of courses that need to be assigned to classrooms (this list is altered within this
     *                     method - courses are removed as they are assigned)
     * @param groups       the list of classroom groups
     */
    private static void assignSectionsToGroups(final Iterable<OfferedCourse> toBeAssigned,
                                               final Iterable<AvailableClassroomGroup> groups) {

        for (final AvailableClassroomGroup group : groups) {
            final int seats = group.totalCapacity;
            final int hoursAvail = group.getHoursRemainingInWeek();

            final Iterator<OfferedCourse> iterator = toBeAssigned.iterator();
            while (iterator.hasNext()) {
                final OfferedCourse course = iterator.next();
                if (course.isClassroomGroupCompatible(group)) {

                    final int hoursNeeded = course.classContactHours;
                    int seatsNeeded = course.getNumSeatsNeeded();

                    if (hoursNeeded <= hoursAvail && seatsNeeded <= seats) {
                        for (final AvailableClassroom classroom : group.getClassrooms()) {

                            final AssignedSection sect;
                            if (seatsNeeded >= classroom.capacity) {
                                sect = new AssignedSection(course, classroom.capacity);
                                seatsNeeded -= classroom.capacity;
                            } else {
                                sect = new AssignedSection(course, seatsNeeded);
                            }
                            classroom.decreaseHoursRemaining(hoursNeeded);
                            classroom.addAssignedSection(sect);
                        }

                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * Assigns course sections to labs.
     *
     * @param toBeAssigned the list of courses that need to be assigned to labs (this list is altered within this method
     *                     - courses are removed as they are assigned)
     * @param allLabs      the list of available labs
     * @return the number of remaining lab capacity (hours per week); -1 if there was not sufficient lab capacity to
     *         assign all courses
     */
    private static int assignLabs(final List<OfferedCourse> toBeAssigned, final List<AvailableLab> allLabs) {

        // TODO:
        return 0;
    }
}

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
    static int compute(final Collection<OfferedCourse> courses, final List<AvailableClassroomOld> allClassrooms,
                       final List<AvailableLab> allLabs) {

        for (final OfferedCourse course : courses) {
            course.clearAssignedSections();
        }

        initializeClassroomsAndLabs(allClassrooms, allLabs);

        final List<OfferedCourse> toBeAssignedClassrooms = makeListOfCoursesToBeAssignedClassrooms(courses);
        final List<OfferedCourse> toBeAssignedLabs = makeListOfCoursesToBeAssignedLabs(courses);

        final int classroomResult = assignClassrooms(toBeAssignedClassrooms, allClassrooms);
        Log.info("    Classroom result is " + classroomResult);

        final int labResult = classroomResult < 0 ? -1 : assignLabs(toBeAssignedLabs, allLabs);
        Log.info("    Lab result is " + labResult);

        final int result = Math.min(classroomResult, labResult);

        if (result >= 0) {
            for (final AvailableClassroomOld classroom : allClassrooms) {
                for (final AssignedSection section : classroom.getAssignedSections()) {
                    section.course.addAssignedClassSection(section);
                }
            }
            for (final AvailableLab labs : allLabs) {
                for (final AssignedSection section : labs.getAssignedSections()) {
                    section.course.addAssignedLabSection(section);
                }
            }
        }

        return result;
    }

    /**
     * Initializes classroom and lab data structures by setting the hours remaining per week to the daily hours times 5
     * days, and clearing the list of sections assigned to each classroom or lab.  This also ensures that lists of
     * classrooms and labs are sorted by room capacity (from low to high).
     *
     * @param allClassrooms the list of all classrooms
     * @param allLabs       the list of all labs
     */
    private static void initializeClassroomsAndLabs(final List<AvailableClassroomOld> allClassrooms,
                                                    final List<AvailableLab> allLabs) {

        for (final AvailableClassroomOld classroom : allClassrooms) {
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
     * Scans a list of courses to find the smallest number of lab contact hours.
     *
     * @param courses the list of courses
     * @return the smallest number of lab contact hours found
     */
    private static int getSmallestLabContactHours(final Iterable<OfferedCourse> courses) {

        int smallest = Integer.MAX_VALUE;

        for (final OfferedCourse course : courses) {
            if (course.labContactHours < smallest) {
                smallest = course.labContactHours;
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
    private static List<AvailableClassroomOld> findClassroomsOfInterest(final Collection<AvailableClassroomOld> allClassrooms,
                                                                        final int smallestContactHours) {

        final int numClassrooms = allClassrooms.size();

        final List<AvailableClassroomOld> classroomsOfInterest = new ArrayList<>(numClassrooms);
        for (final AvailableClassroomOld classroom : allClassrooms) {
            if (classroom.getHoursRemainingInWeek() >= smallestContactHours) {
                classroomsOfInterest.add(classroom);
            }
        }

        return classroomsOfInterest;
    }

    /**
     * Given a list of labs and a minimum number of lab contact hours, identifies a sub-list that has at least that
     * minimum number of hours available in a week.
     *
     * @param allLabs              the list of all labs
     * @param smallestContactHours the smallest allowed number of available hours
     * @return the list of labs having at least the smallest number of available hours
     */
    private static List<AvailableLab> findLabsOfInterest(final Collection<AvailableLab> allLabs,
                                                         final int smallestContactHours) {

        // FIXME: For labs we need the hours to be contiguous, so we really want to track hours free in a day, not
        //  in the entire week, and we want to consume hours in blocks tied to a single day

        final int numLabs = allLabs.size();

        final List<AvailableLab> labsOfInterest = new ArrayList<>(numLabs);
        for (final AvailableLab classroom : allLabs) {
            if (classroom.getHoursRemainingInWeek() >= smallestContactHours) {
                labsOfInterest.add(classroom);
            }
        }

        return labsOfInterest;
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
                        for (final AvailableClassroomOld classroom : group.getClassrooms()) {

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
     * Given a list of courses whose lab component is to be assigned, and a list of lab groups, attempts to assign as
     * many courses to groups as possible.
     *
     * @param toBeAssigned the list of courses that need to be assigned to classrooms (this list is altered within this
     *                     method - courses are removed as they are assigned)
     * @param groups       the list of classroom groups
     */
    private static void assignSectionsToLabGroups(final Iterable<OfferedCourse> toBeAssigned,
                                                  final Iterable<AvailableLabGroup> groups) {

        for (final AvailableLabGroup group : groups) {
            final int seats = group.totalCapacity;
            final int hoursAvail = group.getHoursRemainingInWeek();

            final Iterator<OfferedCourse> iterator = toBeAssigned.iterator();
            while (iterator.hasNext()) {
                final OfferedCourse course = iterator.next();
                if (course.isLabGroupCompatible(group)) {

                    final int hoursNeeded = course.labContactHours;
                    int seatsNeeded = course.getNumSeatsNeeded();

                    if (hoursNeeded <= hoursAvail && seatsNeeded <= seats) {
                        for (final AvailableLab lab : group.getLabs()) {

                            final AssignedSection sect;
                            if (seatsNeeded >= lab.capacity) {
                                sect = new AssignedSection(course, lab.capacity);
                                seatsNeeded -= lab.capacity;
                            } else {
                                sect = new AssignedSection(course, seatsNeeded);
                            }
                            lab.decreaseHoursRemaining(hoursNeeded);
                            lab.addAssignedSection(sect);
                        }

                        iterator.remove();
                    }
                }
            }
        }
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
                                        final Collection<AvailableClassroomOld> allClassrooms) {

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
            for (final AvailableClassroomOld classroom : allClassrooms) {
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
                                                   final Iterable<AvailableClassroomOld> allClassrooms) {

        for (final AvailableClassroomOld classroom : allClassrooms) {
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
                                              final Collection<AvailableClassroomOld> allClassrooms) {

        // Find the smallest number of hours needed for any remaining course, and then remove from consideration any
        // classrooms that do not have at least that many hours of availability per week remaining

        final int smallestContactHours = getSmallestClassContactHours(toBeAssigned);
        final List<AvailableClassroomOld> classroomsOfInterest = findClassroomsOfInterest(allClassrooms,
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
    private static List<AvailableClassroomGroup> makeGroupsOf2(final List<AvailableClassroomOld> classroomsOfInterest) {

        final int size = classroomsOfInterest.size();
        final int numGroups = size * (size - 1);
        final List<AvailableClassroomGroup> groups = new ArrayList<>(numGroups);

        for (int i = 0; i < (size - 1); ++i) {
            final AvailableClassroomOld room1 = classroomsOfInterest.get(i);
            for (int j = i + 1; j < size; ++j) {
                final AvailableClassroomOld room2 = classroomsOfInterest.get(j);
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
                                              final Collection<AvailableClassroomOld> allClassrooms) {

        // Find the smallest number of hours needed for any remaining course, and then remove from consideration any
        // classrooms that do not have at least that many hours of availability per week remaining

        final int smallestContactHours = getSmallestClassContactHours(toBeAssigned);
        final List<AvailableClassroomOld> classroomsOfInterest = findClassroomsOfInterest(allClassrooms,
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
    private static List<AvailableClassroomGroup> makeGroupsOf3(final List<AvailableClassroomOld> classroomsOfInterest) {

        final int size = classroomsOfInterest.size();
        final int numGroups = size * (size - 1) * (size - 2);
        final List<AvailableClassroomGroup> groups = new ArrayList<>(numGroups);

        for (int i = 0; i < (size - 2); ++i) {
            final AvailableClassroomOld room1 = classroomsOfInterest.get(i);
            for (int j = i + 1; j < (size - 1); ++j) {
                final AvailableClassroomOld room2 = classroomsOfInterest.get(j);
                for (int k = j + 1; k < size; ++k) {
                    final AvailableClassroomOld room3 = classroomsOfInterest.get(k);
                    final AvailableClassroomGroup group = new AvailableClassroomGroup(room1, room2, room3);
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
     * @param toBeAssigned  the list of courses that need to be assigned to classrooms (this list is altered within this
     *                      method - courses are removed as they are assigned)
     * @param allClassrooms the list of available classrooms
     */
    private static void assignLargeCourses(final List<OfferedCourse> toBeAssigned,
                                           final Collection<AvailableClassroomOld> allClassrooms) {

        // As before, we find the smallest number of hours needed for any remaining course, and then remove from
        // consideration any classrooms that do not have at least that many hours of availability per week remaining

        final int smallestContactHours = getSmallestClassContactHours(toBeAssigned);
        final List<AvailableClassroomOld> classroomsOfInterest = findClassroomsOfInterest(allClassrooms,
                smallestContactHours);

        // The list of courses to be assigned will be sorted in increasing order by needed capacity, so we start at the
        // end (the largest capacity need) and work downward.  For each course, we work downward through compatible
        // classrooms (from largest to smallest), and try to assemble a set of classrooms that can meet the need.

        final int numCourses = toBeAssigned.size();
        final int numClassrooms = classroomsOfInterest.size();

        final Collection<AvailableClassroomOld> potentialClassrooms = new ArrayList<>(10);

        for (int i = numCourses - 1; i >= 0; --i) {
            final OfferedCourse course = toBeAssigned.get(i);
            final int hoursNeeded = course.classContactHours;
            int seatsNeeded = course.getNumSeatsNeeded();

            // Note: As we scan downward in classroom capacity, we might end up using a large room for a very small
            // set of students at the end.  To avoid this, we keep track of the "last" room allocated (when that room
            // would take needed capacity down to zero), and keep scanning for smaller rooms that could also serve as
            // the "last" room.

            potentialClassrooms.clear();
            AvailableClassroomOld last = null;
            for (int j = numClassrooms - 1; j >= 0; --j) {
                final AvailableClassroomOld classroom = classroomsOfInterest.get(j);
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
                for (final AvailableClassroomOld classroom : potentialClassrooms) {

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
     * Assigns course sections to labs.
     *
     * @param toBeAssigned the list of courses that need to be assigned to labs (this list is altered within this method
     *                     - courses are removed as they are assigned)
     * @param allLabs      the list of available labs
     * @return the number of remaining lab capacity (hours per week); -1 if there was not sufficient lab capacity to
     *         assign all courses
     */
    private static int assignLabs(final List<OfferedCourse> toBeAssigned, final List<AvailableLab> allLabs) {

        // Lab Pass 1: take the smallest lab and assign it to all courses that will fit; then move to the next smallest
        // lab, and so on, for all labs

        assignSingleSectionLabs(toBeAssigned, allLabs);

        // At this point, all labs that can be offered in one section have been processed.  If we are finished, do no
        // more work.  Otherwise, we need to split high-enrollment labs into multiple sections.

        if (!toBeAssigned.isEmpty()) {

            // Lab Pass 2: Generate all combinations of 2 labs, and sort that list by total capacity.  Then repeat the
            // above assignment process for these groups.

            assign2SectionLabs(toBeAssigned, allLabs);

            // At this point, all labs that can be offered in one or two sections have been processed.  If we are
            // finished, do no more work.

            if (!toBeAssigned.isEmpty()) {

                // Lab Pass 3: Generate all combinations of 3 labs, and sort that list by total capacity.  Then repeat
                // the above assignment process for these groups.

                assign3SectionLabs(toBeAssigned, allLabs);

                if (!toBeAssigned.isEmpty()) {

                    // Lab Pass 4: all that remain at this point are labs too large to split into 3 or fewer sections.
                    // We change our strategy here to grab the lab with the greatest size, and start assigning labs from
                    // the largest downward, until we run out of lab our have assigned all courses.

                    assignLargeLabs(toBeAssigned, allLabs);
                }
            }
        }

        int result;

        if (toBeAssigned.isEmpty()) {
            // All labs have been assigned - see how many free hours per week remain in labs
            result = 0;
            for (final AvailableLab lab : allLabs) {
                result += lab.getHoursRemainingInWeek();
            }
        } else {
            // We were not able to assign all labs - there must not be enough lab space
            result = -1;
        }

        return result;
    }

    /**
     * Scans for all courses whose lab component can be accommodated with a single section in a lab, and makes those
     * assignments, removing those courses from the list to be assigned.
     *
     * @param toBeAssigned the list of courses whose lab component need to be assigned to labs (this list is altered
     *                     within this method - courses are removed as they are assigned)
     * @param allLabs      the list of available labs
     */
    private static void assignSingleSectionLabs(final Iterable<OfferedCourse> toBeAssigned,
                                                final Iterable<AvailableLab> allLabs) {

        for (final AvailableLab lab : allLabs) {
            final int seats = lab.capacity;
            final int hoursAvail = lab.getHoursRemainingInWeek();

            final Iterator<OfferedCourse> iterator = toBeAssigned.iterator();
            while (iterator.hasNext()) {
                final OfferedCourse course = iterator.next();
                if (course.isLabCompatible(lab)) {
                    final int hoursNeeded = course.labContactHours;
                    final int seatsNeeded = course.getNumSeatsNeeded();

                    if (hoursNeeded <= hoursAvail && seatsNeeded <= seats) {
                        final AssignedSection sect = new AssignedSection(course, seatsNeeded);
                        lab.decreaseHoursRemaining(hoursNeeded);
                        lab.addAssignedSection(sect);
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * Scans for all courses whose lab component can be accommodated with two sections and makes those assignments,
     * removing those courses from the list to be assigned.
     *
     * @param toBeAssigned the list of courses that need to be assigned to labs (this list is altered within this method
     *                     - courses are removed as they are assigned)
     * @param allLabs      the list of available labs
     */
    private static void assign2SectionLabs(final Iterable<OfferedCourse> toBeAssigned,
                                           final Collection<AvailableLab> allLabs) {

        // Find the smallest number of hours needed for any remaining course, and then remove from consideration any
        // classrooms that do not have at least that many hours of availability per week remaining

        final int smallestContactHours = getSmallestLabContactHours(toBeAssigned);
        final List<AvailableLab> labsOfInterest = findLabsOfInterest(allLabs, smallestContactHours);

        // Make sure there are at least 2 labs that could accommodate courses - if not, we're done!

        final int numOfInterest = labsOfInterest.size();
        if (numOfInterest > 1) {

            // Generate a set of all groups of 2 labs, then assign as many courses to those groups as possible

            final List<AvailableLabGroup> groups = makeGroupsOf2Labs(labsOfInterest);
            assignSectionsToLabGroups(toBeAssigned, groups);
        }
    }

    /**
     * Creates all possible groups of 2 labs from a list of labs of interest.
     *
     * @param labsOfInterest the list of labs of interest
     * @return the list of groups of 2 labs, sorted by total capacity (if there are N labs of interest, this list will
     *         contain (N)(N-1) entries)
     */
    private static List<AvailableLabGroup> makeGroupsOf2Labs(final List<AvailableLab> labsOfInterest) {

        final int size = labsOfInterest.size();
        final int numGroups = size * (size - 1);
        final List<AvailableLabGroup> groups = new ArrayList<>(numGroups);

        for (int i = 0; i < (size - 1); ++i) {
            final AvailableLab lab1 = labsOfInterest.get(i);
            for (int j = i + 1; j < size; ++j) {
                final AvailableLab lab2 = labsOfInterest.get(j);
                final AvailableLabGroup group = new AvailableLabGroup(lab1, lab2);
                groups.add(group);
            }
        }

        groups.sort(null);

        return groups;
    }

    /**
     * Scans for all courses whose lab components can be accommodated with three sections and makes those assignments,
     * removing those courses from the list to be assigned.
     *
     * @param toBeAssigned the list of courses that need to be assigned to labs (this list is altered within this method
     *                     - courses are removed as they are assigned)
     * @param allLabs      the list of available labs
     */
    private static void assign3SectionLabs(final Iterable<OfferedCourse> toBeAssigned,
                                           final Collection<AvailableLab> allLabs) {

        // Find the smallest number of hours needed for any remaining course, and then remove from consideration any
        // classrooms that do not have at least that many hours of availability per week remaining

        final int smallestContactHours = getSmallestLabContactHours(toBeAssigned);
        final List<AvailableLab> labsOfInterest = findLabsOfInterest(allLabs, smallestContactHours);

        // Make sure there are at least 3 labs that could accommodate courses - if not, we're done!

        final int numOfInterest = labsOfInterest.size();
        if (numOfInterest > 2) {

            // Generate a set of all groups of 3 labs, then assign as many courses to those groups as possible

            final List<AvailableLabGroup> groups = makeGroupsOf3Labs(labsOfInterest);
            assignSectionsToLabGroups(toBeAssigned, groups);
        }
    }

    /**
     * Creates all possible groups of 3 labs from a list of labs of interest.
     *
     * @param labsOfInterest the list of labs of interest
     * @return the list of groups of 3 labs, sorted by total capacity (if there are N labs of interest, this list will
     *         contain (N)(N-1)(N-2) entries)
     */
    private static List<AvailableLabGroup> makeGroupsOf3Labs(final List<AvailableLab> labsOfInterest) {

        final int size = labsOfInterest.size();
        final int numGroups = size * (size - 1) * (size - 2);
        final List<AvailableLabGroup> groups = new ArrayList<>(numGroups);

        for (int i = 0; i < (size - 2); ++i) {
            final AvailableLab lab1 = labsOfInterest.get(i);
            for (int j = i + 1; j < (size - 1); ++j) {
                final AvailableLab lab2 = labsOfInterest.get(j);
                for (int k = j + 1; k < size; ++k) {
                    final AvailableLab lab3 = labsOfInterest.get(k);
                    final AvailableLabGroup group = new AvailableLabGroup(lab1, lab2, lab3);
                    groups.add(group);
                }
            }
        }

        groups.sort(null);

        return groups;
    }

    /**
     * Scans for all courses whose lab component cannot be accommodated with three or fewer sections and makes those
     * assignments, removing those courses from the list to be assigned.
     *
     * @param toBeAssigned the list of courses that need to be assigned to labs (this list is altered within this method
     *                     - courses are removed as they are assigned)
     * @param allLabs      the list of available classrooms
     */
    private static void assignLargeLabs(final List<OfferedCourse> toBeAssigned,
                                        final Collection<AvailableLab> allLabs) {

        // As before, we find the smallest number of hours needed for any remaining course, and then remove from
        // consideration any labs that do not have at least that many hours of availability per week remaining

        final int smallestContactHours = getSmallestLabContactHours(toBeAssigned);
        final List<AvailableLab> labsOfInterest = findLabsOfInterest(allLabs, smallestContactHours);

        // The list of courses whose lab component is to be assigned will be sorted in increasing order by needed
        // capacity, so we start at the end (the largest capacity need) and work downward.  For each course, we work
        // downward through compatible labs (from largest to smallest), and try to assemble a set of labs that can meet
        // the need.

        final int numCourses = toBeAssigned.size();
        final int numLabs = labsOfInterest.size();

        final Collection<AvailableLab> potentialLabs = new ArrayList<>(10);

        for (int i = numCourses - 1; i >= 0; --i) {
            final OfferedCourse course = toBeAssigned.get(i);
            final int hoursNeeded = course.labContactHours;
            int seatsNeeded = course.getNumSeatsNeeded();

            // Note: As we scan downward in classroom capacity, we might end up using a large room for a very small
            // set of students at the end.  To avoid this, we keep track of the "last" room allocated (when that room
            // would take needed capacity down to zero), and keep scanning for smaller rooms that could also serve as
            // the "last" room.

            potentialLabs.clear();
            AvailableLab last = null;
            for (int j = numLabs - 1; j >= 0; --j) {
                final AvailableLab lab = labsOfInterest.get(j);
                if (course.isLabCompatible(lab) && lab.getHoursRemainingInWeek() >= hoursNeeded) {
                    if (last == null) {
                        // We are not yet scanning for the smallest "last" room)
                        if (seatsNeeded > lab.capacity) {
                            // This one will not be the last - track it and move on
                            potentialLabs.add(lab);
                            seatsNeeded -= lab.capacity;
                        } else {
                            // This one would work as the "last" lab - track as the current "last" lab, but don't add
                            // yet to "potentialLabs" until we're sure it's the right "last" lab to use.
                            last = lab;
                        }
                    } else if (seatsNeeded <= lab.capacity) {
                        // This lab is smaller and will also work as the "last" lab - track it as the current "last"
                        // lab, but keep scanning
                        last = lab;
                    }
                }
            }

            // If "last" is null here, we failed to find enough available lab space, so do nothing.
            if (Objects.nonNull(last)) {
                potentialLabs.add(last);

                // The list of potential classrooms will work - make the assignments
                int stillNeeded = course.getNumSeatsNeeded();
                for (final AvailableLab lab : potentialLabs) {

                    final AssignedSection sect;

                    if (lab.capacity > stillNeeded) {
                        sect = new AssignedSection(course, stillNeeded);
                        stillNeeded = 0;
                    } else {
                        sect = new AssignedSection(course, lab.capacity);
                        stillNeeded -= lab.capacity;
                    }

                    lab.decreaseHoursRemaining(hoursNeeded);
                    lab.addAssignedSection(sect);
                }

                // This course has been assigned - remove it from the "toBeAssigned" list
                toBeAssigned.remove(i);
            }
        }
    }

}

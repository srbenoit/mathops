package dev.mathops.db.old.logic.mathplan.data;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.rec.LiveCsuCredit;
import dev.mathops.db.rec.LiveTransferCredit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A sequence of courses, including pre-arrival courses, semester 1 courses, semester 2 courses, and courses to be taken
 * after semester 2.
 */
public final class CourseSequence {

    /** The core course group. */
    private final CourseGroup core;

    /** The pre-arrival courses. */
    private final Map<String, CourseInfo> preArrivalCourses;

    /** The semester 1 course groups. */
    private final Set<CourseInfoGroup> semester1CourseGroups;

    /** The semester 1 courses. */
    private final Map<String, CourseInfo> semester1Courses;

    /** The semester 2 course groups. */
    private final Set<CourseInfoGroup> semester2CourseGroups;

    /** The semester 2 courses. */
    private final Map<String, CourseInfo> semester2Courses;

    /** The additional course groups. */
    private final Set<CourseInfoGroup> additionalCourseGroups;

    /** The additional courses. */
    private final Map<String, CourseInfo> additionalCourses;

    /** Flag indicating selected majors required a variety of CALC 1 courses. */
    private boolean multipleCalc1;

    /** Flag indicating selected majors required a variety of CALC 2 courses. */
    private boolean multipleCalc2;

    /**
     * Constructs a new {@code CourseSequence}.
     *
     * @param theCore the core course group
     */
    CourseSequence(final CourseGroup theCore) {

        this.core = theCore;

        this.preArrivalCourses = new TreeMap<>();
        this.semester1CourseGroups = new TreeSet<>();
        this.semester1Courses = new TreeMap<>();
        this.semester2CourseGroups = new TreeSet<>();
        this.semester2Courses = new TreeMap<>();
        this.additionalCourseGroups = new TreeSet<>();
        this.additionalCourses = new TreeMap<>();
        this.multipleCalc1 = false;
        this.multipleCalc2 = false;
    }

    /**
     * Eliminates any duplicate courses or course groups, keeping only the earliest instance.
     */
    void eliminateDuplicates() {

        this.additionalCourseGroups.removeAll(this.semester2CourseGroups);
        this.additionalCourseGroups.removeAll(this.semester1CourseGroups);
        for (final String key : this.semester2Courses.keySet()) {
            this.additionalCourses.remove(key);
        }
        for (final String key : this.semester1Courses.keySet()) {
            this.additionalCourses.remove(key);
        }
        for (final String key : this.preArrivalCourses.keySet()) {
            this.additionalCourses.remove(key);
        }

        this.semester2CourseGroups.removeAll(this.semester1CourseGroups);
        for (final String key : this.semester1Courses.keySet()) {
            this.semester2Courses.remove(key);
        }
        for (final String key : this.preArrivalCourses.keySet()) {
            this.semester2Courses.remove(key);
        }

        for (final String key : this.preArrivalCourses.keySet()) {
            this.semester1Courses.remove(key);
        }
    }

    /**
     * Takes all course groups with a single course and moves them to a single course entry in the corresponding course
     * set, removing them from the group set.
     *
     * @param allCourses the map from course ID to course data object for all courses
     */
    void convertOneCourseGroups(final Map<String, RawCourse> allCourses) {

        extractOneCourseGroups(this.semester1CourseGroups, this.semester1Courses, allCourses);
        extractOneCourseGroups(this.semester2CourseGroups, this.semester2Courses, allCourses);
        extractOneCourseGroups(this.additionalCourseGroups, this.additionalCourses, allCourses);
    }

    /**
     * Takes all course groups with a single course and moves them to a single course entry in the corresponding course
     * set, removing them from the group set.
     *
     * @param groupSet   the set of course groups
     * @param courseSet  the set of courses
     * @param allCourses the map from course ID to course data object for all courses
     */
    private static void extractOneCourseGroups(final Iterable<CourseInfoGroup> groupSet,
                                               final Map<? super String, ? super CourseInfo> courseSet,
                                               final Map<String, RawCourse> allCourses) {

        final Iterator<CourseInfoGroup> iterator = groupSet.iterator();
        while (iterator.hasNext()) {
            final List<String> courses = iterator.next().getCourseNumbers();

            if (courses.size() == 1) {
                iterator.remove();

                final String first = courses.getFirst();
                final RawCourse course = allCourses.get(first);
                if (course != null) {
                    final CourseInfo info = new CourseInfo(course, false);
                    courseSet.put(course.course, info);
                }
            }
        }
    }

    /**
     * Searches for courses that fall into a needed course group but that occur later than the group, and move those
     * courses to the place where the group occurs, reducing the credit/course requirement of the group accordingly
     * (this may remove groups).
     */
    public void resolveGroups() {

        final Iterable<CourseInfoGroup> copy1 = new ArrayList<>(this.semester1CourseGroups);
        for (final CourseInfoGroup group : copy1) {
            boolean groupSatisfied = resolveGroup(group, this.semester1Courses, this.semester2Courses);

            if (groupSatisfied) {
                this.semester1CourseGroups.remove(group);
            } else {
                groupSatisfied = resolveGroup(group, this.semester1Courses, this.additionalCourses);

                if (groupSatisfied) {
                    this.semester1CourseGroups.remove(group);
                }
            }
        }

        final Iterable<CourseInfoGroup> copy2 = new ArrayList<>(this.semester2CourseGroups);
        for (final CourseInfoGroup group : copy2) {
            final boolean groupSatisfied = resolveGroup(group, this.semester2Courses, this.additionalCourses);

            if (groupSatisfied) {
                this.semester2CourseGroups.remove(group);
            }
        }
    }

    /**
     * Given a course group, the course list for the group's semester, and a course list from a later semester, scans
     * for a course in the later semester that is named in the course group. If any are found, they are moved to the
     * group's semester, and the group's requirements are decreased accordingly.
     *
     * @param group          the course group
     * @param groupSemester  the course list for the group's semester
     * @param sourceSemester the course list from the future semester
     * @return {@code true} if one or more courses was moved to the group semester and the result satisfies the group
     *         (meaning the group may now be removed)
     */
    private static boolean resolveGroup(final CourseInfoGroup group,
                                        final Map<? super String, ? super CourseInfo> groupSemester,
                                        final Map<String, CourseInfo> sourceSemester) {

        final CourseGroup courseGroup = group.getCourseGroup();

        Integer numCredits = courseGroup.nbrCredits;
        boolean groupSatisfied = false;

        for (final String course : courseGroup.courseNumbers) {

            if (sourceSemester.containsKey(course)) {
                final CourseInfo info = sourceSemester.remove(course);
                groupSemester.put(course, info);

                if (numCredits == null) {
                    groupSatisfied = true;
                    break;
                }

                final int courseCredits = info.course.nbrCredits.intValue();

                final int count = numCredits.intValue();

                if (courseCredits >= count) {
                    groupSatisfied = true;
                    break;
                }

                numCredits = Integer.valueOf(count - courseCredits);

                group.setCourseGroup(new CourseGroup(courseGroup.groupCode, numCredits,
                        courseGroup.lowestLastCourse, courseGroup.courseNumbers));
            }
        }

        return groupSatisfied;
    }

    /**
     * Examines each course group in the sequence. If the courses in that group's semester or any prior semester satisfy
     * the group, the group is removed.
     *
     * @param allCourses a map from course ID to course data; used to get number of credits for each course
     */
    void cleanNeedlessGroups(final Map<String, RawCourse> allCourses) {

        final Map<String, CourseInfo> priorAndCurrent = new TreeMap<>();

        // Clean semester 1 groups (looks at pre-arrival and semester 1 courses)
        priorAndCurrent.putAll(this.preArrivalCourses);
        priorAndCurrent.putAll(this.semester1Courses);
        final Collection<CourseInfoGroup> priorAndCurGroups = new HashSet<>(this.semester1CourseGroups);
        this.semester1CourseGroups.removeIf(g1 -> g1.isSatisfiedBy(priorAndCurrent.keySet(), allCourses)
                || g1.isSatisfiedByGroup(priorAndCurGroups, allCourses));

        // Clean semester 2 groups (looks at pre-arrival, semester 1, and semester 2 courses)
        priorAndCurrent.putAll(this.semester2Courses);
        priorAndCurGroups.addAll(this.semester2CourseGroups);
        this.semester2CourseGroups.removeIf(g2 -> g2.isSatisfiedBy(priorAndCurrent.keySet(), allCourses)
                || g2.isSatisfiedByGroup(priorAndCurGroups, allCourses));

        // Clean additional groups (looks at pre-arrival, semester 1, semester 2, and additional courses)
        priorAndCurrent.putAll(this.additionalCourses);
        priorAndCurGroups.addAll(this.additionalCourseGroups);
        this.additionalCourseGroups.removeIf(g3 -> g3.isSatisfiedBy(priorAndCurrent.keySet(), allCourses)
                || g3.isSatisfiedByGroup(priorAndCurGroups, allCourses));
    }

    /**
     * If there are multiple courses in the sequence for which credit may only be earned for one, replace them with the
     * highest course in that list, but place that course at the position of the earliest (or most critical) of any
     * entry in that list.
     *
     * @param allCourses a map from course ID to course record
     */
    void mergeNoCreditForBothCourses(final Map<String, RawCourse> allCourses) {

        final Collection<String> all = new HashSet<>(this.preArrivalCourses.keySet());
        all.addAll(this.semester1Courses.keySet());
        all.addAll(this.semester2Courses.keySet());
        all.addAll(this.additionalCourses.keySet());

        for (final CourseInfoGroup group : this.semester1CourseGroups) {
            all.add(group.getLowestLastCourse());
        }
        for (final CourseInfoGroup group : this.semester2CourseGroups) {
            all.add(group.getLowestLastCourse());
        }
        for (final CourseInfoGroup group : this.additionalCourseGroups) {
            all.add(group.getLowestLastCourse());
        }

        // TODO: Drive this into data - "credit-for-only-one-of" groups

        // GROUP 1: Credit for only one of MATH 141, MATH 155, MATH 159, MATH 160

        if (all.contains("M 160") && all.contains("M 141")) {
            this.multipleCalc1 = true;
            replaceCourse("M 160", "M 141", allCourses);
        }
        if (all.contains("M 155") && all.contains("M 141")) {
            this.multipleCalc1 = true;
            replaceCourse("M 155", "M 141", allCourses);
        }
        if (all.contains("M 160") && all.contains("M 155")) {
            this.multipleCalc1 = true;
            replaceCourse("M 160", "M 155", allCourses);
        }

        // GROUP 2: Credit for only one of MATH 255, MATH 261

        if (all.contains("M 161") && all.contains("M 255")) {
            this.multipleCalc2 = true;
            replaceCourse("M 161", "M 255", allCourses);
        }

        // GROUP 2: Credit for only one of MATH 230, MATH 330

        if (all.contains("M 330") && all.contains("M 230")) {
            replaceCourse("M 330", "M 230", allCourses);
        }

        // GROUP 2: Credit for only one of MATH 340, MATH 345

        // ODE courses
        if (all.contains("M 345") && all.contains("M 340")) {
            replaceCourse("M 345", "M 340", allCourses);
        }
    }

    /**
     * When there are several courses for which credit may not be earned together, removes all but one, but moves the
     * one that remains in the earliest position of any.
     *
     * @param leaveCourse    the course that should remain
     * @param courseToRemove the course to be removed
     * @param allCourses     a map from course ID to course record
     */
    private void replaceCourse(final String leaveCourse, final String courseToRemove,
                               final Map<String, RawCourse> allCourses) {

        // Identify the earliest semester where "courseToRemove" occurs (0, 1, 2, 3), and remove
        // both courses (or any single-course group containing either choice) as we go
        int earliest = 4;

        if (this.additionalCourses.remove(courseToRemove) != null
                || this.additionalCourses.remove(leaveCourse) != null) {
            earliest = 3;
        }

        final Iterator<CourseInfoGroup> additional = this.additionalCourseGroups.iterator();
        while (additional.hasNext()) {
            final CourseInfoGroup grp = additional.next();
            if (grp.getCourseNumbers().contains(courseToRemove)
                    || grp.getCourseNumbers().contains(leaveCourse)) {
                additional.remove();
                earliest = 3;
            }
        }

        if (this.semester2Courses.remove(courseToRemove) != null
                || this.semester2Courses.remove(leaveCourse) != null) {
            earliest = 2;
        }

        final Iterator<CourseInfoGroup> sem2 = this.semester2CourseGroups.iterator();
        while (sem2.hasNext()) {
            final CourseInfoGroup grp = sem2.next();
            if (grp.getCourseNumbers().contains(courseToRemove)
                    || grp.getCourseNumbers().contains(leaveCourse)) {
                sem2.remove();
                earliest = 2;
            }
        }

        if (this.semester1Courses.remove(courseToRemove) != null
                || this.semester1Courses.remove(leaveCourse) != null) {
            earliest = 1;
        }

        final Iterator<CourseInfoGroup> sem1 = this.semester1CourseGroups.iterator();
        while (sem1.hasNext()) {
            final CourseInfoGroup grp = sem1.next();
            if (grp.getCourseNumbers().contains(courseToRemove)
                    || grp.getCourseNumbers().contains(leaveCourse)) {
                sem1.remove();
                earliest = 1;
            }
        }

        if (this.preArrivalCourses.remove(courseToRemove) != null
                || this.preArrivalCourses.remove(leaveCourse) != null) {
            earliest = 0;
        }

        // Add the "to leave" course at the earliest spot

        this.preArrivalCourses.remove(leaveCourse);
        this.semester1Courses.remove(leaveCourse);
        this.semester2Courses.remove(leaveCourse);
        this.additionalCourses.remove(leaveCourse);

        final CourseInfo info = new CourseInfo(allCourses.get(leaveCourse), false);

        switch (earliest) {
            case 0:
                this.preArrivalCourses.put(leaveCourse, info);
                break;
            case 1:
                this.semester1Courses.put(leaveCourse, info);
                break;
            case 2:
                this.semester2Courses.put(leaveCourse, info);
                break;
            case 3:
                this.additionalCourses.put(leaveCourse, info);
                break;
            default:
                break;
        }
    }

    /**
     * For each course that the student has completed at CSU, or for which they have transfer credit, or in which they
     * have earned placement credit, or that they have placed out of with any placement activity, note that status in
     * the course or group information.
     *
     * @param data the student data with past word on record
     */
    void markStatus(final MathPlanStudentData data) {

        for (final CourseInfo info : this.preArrivalCourses.values()) {
            markStatus(info, data);
        }
        for (final CourseInfo info : this.semester1Courses.values()) {
            markStatus(info, data);
        }
        for (final CourseInfo info : this.semester2Courses.values()) {
            markStatus(info, data);
        }
        for (final CourseInfo info : this.additionalCourses.values()) {
            markStatus(info, data);
        }
        for (final CourseInfoGroup group : this.semester1CourseGroups) {
            markStatus(group, data);
        }
        for (final CourseInfoGroup group : this.semester2CourseGroups) {
            markStatus(group, data);
        }
        for (final CourseInfoGroup group : this.additionalCourseGroups) {
            markStatus(group, data);
        }
    }

    /**
     * Marks the status of a single course information object based on completed courses, transfer credit, and placement
     * results. If there are multiple types of result for one course, placement results take priority (since they
     * automatically satisfy any grade requirements), followed by completed courses, then transfer credit.
     *
     * @param group the course group info object
     * @param data  the student data with past word on record
     */
    private static void markStatus(final CourseInfoGroup group, final MathPlanStudentData data) {

        for (final CourseInfo info : group.getCourseInfos()) {
            markStatus(info, data);
        }

        // If this is a group with a number of required credits, see if the actual number of
        // credits is smaller based on completed courses.

        final Integer numCredits = group.getNumCredits();
        if (numCredits == null) {
            // See if the group is satisfied because one or more courses is satisfied
            for (final CourseInfo info : group.getCourseInfos()) {
                if (info.status.sufficient) {
                    group.setSatisfied(true);
                    break;
                }
            }
        } else {
            int numSatisfied = 0;
            for (final CourseInfo info : group.getCourseInfos()) {
                if (info.status.sufficient && info.status != ECourseStatus.PLACED_OUT) {
                    // Placement cannot reduce credit counts in named course lists.
                    final Integer credits = info.course.nbrCredits;
                    if (credits != null) {
                        numSatisfied += credits.intValue();
                    }
                }
            }
            if (numSatisfied >= numCredits.intValue()) {
                group.setNumRemainingCredits(Integer.valueOf(0));
                group.setSatisfied(true);
            } else {
                group.setNumRemainingCredits(Integer.valueOf(numCredits.intValue() - numSatisfied));
            }
        }
    }

    /**
     * Marks the status of a single course information object based on completed courses, transfer credit, and placement
     * results. If there are multiple types of result for one course, placement results take priority (since they
     * automatically satisfy any grade requirements), followed by completed courses, then transfer credit.
     *
     * @param info the course info object
     * @param data the student data with past word on record
     */
    private static void markStatus(final CourseInfo info, final MathPlanStudentData data) {

        final String courseId = info.course.course;

        boolean needToTest = info.status == ECourseStatus.NONE;

        if (needToTest) {
            for (final RawMpeCredit row : data.getPlacementCredit()) {
                final String rowId = row.course;
                if (courseId.equals(rowId)) {
                    // There was a placement result for the course
                    if ("C".equals(row.examPlaced)) {
                        info.status = ECourseStatus.MATH_CHALLENGE_EXAM;
                    } else {
                        info.status = ECourseStatus.PLACED_OUT;
                    }
                    needToTest = false;
                    break;
                }
            }
        }

        if (needToTest) {
            // No placement result, check for courses completed at CSU that have a non-null grade
            // GPA (a null GPA indicates a non-passing grade).

            for (final LiveCsuCredit row : data.getCompletedCourses()) {
                final String rowId = row.courseId;
                if (courseId.equals(rowId) && row.getGradeGpa() != null) {
                    // The student has passed the course at CSU (this may occur more than once,
                    // and if so, track only the highest earned grade)
                    final Float rowGrade = row.getGradeGpa();

                    if (info.earnedGrade == null || info.earnedGrade.floatValue() < rowGrade.floatValue()) {
                        info.earnedGrade = rowGrade;

                        if ((info.requiredGrade == null)
                                || (info.earnedGrade.floatValue() >= info.requiredGrade.floatValue())) {
                            info.status = ECourseStatus.COMPLETED_AT_CSU;
                        } else {
                            info.status = ECourseStatus.COMPLETED_AT_CSU_GRADE_TOO_LOW;
                        }
                    }
                }
            }

            final String testId = info.course.course;
            final boolean isPrecalc = RawRecordConstants.M117.equals(testId)
                    || RawRecordConstants.M118.equals(testId) || RawRecordConstants.M124.equals(testId)
                    || RawRecordConstants.M125.equals(testId) || RawRecordConstants.M126.equals(testId);

            for (final LiveTransferCredit row : data.getLiveTransferCredit()) {
                final String rowId = row.courseId;
                final Float gpa = row.getGradeGpa();

                if ((courseId.equals(rowId) && gpa != null) && (info.earnedGrade == null
                        || info.earnedGrade.floatValue() < gpa.floatValue())) {
                    info.earnedGrade = gpa;

                    if ((info.requiredGrade == null)
                            || (info.earnedGrade.floatValue() >= info.requiredGrade.floatValue())) {
                        info.status = ECourseStatus.TRANSFER_CREDIT;
                    } else {
                        info.status = ECourseStatus.TRANSFER_CREDIT_GRADE_TOO_LOW;
                    }
                } else if (isPrecalc && ("M 160".equals(rowId) || "M 161".equals(rowId))) {
                    // Calc clears prereq for all Precalc courses
                    info.status = ECourseStatus.PLACED_OUT;
                }
            }
        }
    }

    /**
     * Fills in any missing prerequisite courses in a course sequence.
     *
     * @param prereqData a map from course ID to its required prerequisites
     * @param courseData a map from course ID to course data
     * @param data       the student data with past word on record
     */
    void ensurePrerequisitesMet(final Map<String, ? extends List<RequiredPrereq>> prereqData,
                                final Map<String, RawCourse> courseData, final MathPlanStudentData data) {

        checkSemester1Prerequisites(prereqData, courseData, data);
        checkSemester2Prerequisites(prereqData, courseData, data);
        checkAdditionalPrerequisites(prereqData, courseData, data);
    }

    /**
     * Checks a single course in the pre-arrival category. If the course's prerequisites are not present they are added,
     * and this method is called recursively to add any prerequisites for the newly added course.
     *
     * @param course     the course whose prerequisites to test (should already be in the list)
     * @param prereqData a map from course ID to its required prerequisites
     * @param courseData a map from course ID to course data
     * @param data       the student data with past word on record
     */
    private void checkSinglePreArrivalPrerequisite(final String course,
                                                   final Map<String, ? extends List<RequiredPrereq>> prereqData,
                                                   final Map<String, RawCourse> courseData, final MathPlanStudentData data) {

        final List<RequiredPrereq> prereqList = prereqData.get(course);
        if (prereqList != null) {
            for (final RequiredPrereq prereq : prereqList) {
                if (isPrereqNeeded(prereq, this.preArrivalCourses, this.preArrivalCourses, null, null, false)) {
                    final String courseToAdd = prereq.prereqCourses.getFirst();

                    final RawCourse crs = courseData.get(courseToAdd);
                    if (crs != null) {
                        final CourseInfo info = new CourseInfo(crs, true);
                        info.requiredGrade = prereq.prereqGrades.isEmpty() ? null : prereq.prereqGrades.getFirst();
                        info.status = isAlreadySatisfied(info, data);

                        this.preArrivalCourses.put(courseToAdd, info);
                        checkSinglePreArrivalPrerequisite(courseToAdd, prereqData, courseData, data);
                    }
                }
            }
        }
    }

    /**
     * Checks a single course in the semester 1 category. If the course's prerequisites are not present they are added,
     * and this method is called recursively to add any prerequisites for the newly added course.
     *
     * @param course     the course whose prerequisites to test (should already be in the list)
     * @param prereqData a map from course ID to its required prerequisites
     * @param courseData a map from course ID to course data
     * @param data       the student data with past word on record
     */
    private void checkSingleSem1Prerequisite(final String course,
                                             final Map<String, ? extends List<RequiredPrereq>> prereqData,
                                             final Map<String, RawCourse> courseData, final MathPlanStudentData data) {

        final List<RequiredPrereq> prereqList = prereqData.get(course);
        if (prereqList != null) {
            for (final RequiredPrereq prereq : prereqList) {
                if (isPrereqNeeded(prereq, this.semester1Courses, this.preArrivalCourses, this.semester1CourseGroups,
                        null, false)) {
                    final String courseToAdd = prereq.prereqCourses.getFirst();
                    final RawCourse crs = courseData.get(courseToAdd);

                    if (crs != null) {
                        final CourseInfo info = new CourseInfo(crs, true);
                        info.requiredGrade = prereq.prereqGrades.isEmpty() ? null : prereq.prereqGrades.getFirst();
                        info.status = isAlreadySatisfied(info, data);

                        if (Boolean.TRUE.equals(prereq.mayBeConcurrent)) {
                            this.semester1Courses.put(courseToAdd, info);
                            checkSingleSem1Prerequisite(courseToAdd, prereqData, courseData, data);
                        } else {
                            this.preArrivalCourses.put(courseToAdd, info);
                            checkSinglePreArrivalPrerequisite(courseToAdd, prereqData, courseData, data);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks a single course in the semester 2 category. If the course's prerequisites are not present they are added,
     * and this method is called recursively to add any prerequisites for the newly added course.
     *
     * @param course     the course whose prerequisites to test (should already be in the list)
     * @param prereqData a map from course ID to its required prerequisites
     * @param courseData a map from course ID to course data
     * @param data       the student data with past word on record
     */
    private void checkSingleSem2Prerequisite(final String course,
                                             final Map<String, ? extends List<RequiredPrereq>> prereqData,
                                             final Map<String, RawCourse> courseData, final MathPlanStudentData data) {

        final Map<String, CourseInfo> prior = new HashMap<>(this.semester1Courses);
        prior.putAll(this.preArrivalCourses);

        final List<RequiredPrereq> prereqList = prereqData.get(course);
        if (prereqList != null) {
            for (final RequiredPrereq prereq : prereqList) {
                if (isPrereqNeeded(prereq, this.semester2Courses, prior, this.semester2CourseGroups,
                        this.semester1CourseGroups, false)) {
                    final String courseToAdd = prereq.prereqCourses.getFirst();
                    final RawCourse crs = courseData.get(courseToAdd);

                    if (crs != null) {
                        final CourseInfo info = new CourseInfo(crs, true);
                        info.requiredGrade = prereq.prereqGrades.isEmpty() ? null : prereq.prereqGrades.getFirst();
                        info.status = isAlreadySatisfied(info, data);

                        if (Boolean.TRUE.equals(prereq.mayBeConcurrent)) {
                            this.semester2Courses.put(courseToAdd, info);
                            checkSingleSem2Prerequisite(courseToAdd, prereqData, courseData, data);
                        } else {
                            this.semester1Courses.put(courseToAdd, info);
                            checkSingleSem1Prerequisite(courseToAdd, prereqData, courseData, data);
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks a single course in the additional category. If the course's prerequisites are not present they are added,
     * and this method is called recursively to add any prerequisites for the newly added course.
     *
     * @param course     the course whose prerequisites to test (should already be in the list)
     * @param prereqData a map from course ID to its required prerequisites
     * @param courseData a map from course ID to course data
     * @param data       the student data with past word on record
     */
    private void checkSingleAdditionalPrerequisite(final String course,
                                                   final Map<String, ? extends List<RequiredPrereq>> prereqData,
                                                   final Map<String, RawCourse> courseData,
                                                   final MathPlanStudentData data) {

        final Map<String, CourseInfo> prior = new HashMap<>(this.semester2Courses);
        prior.putAll(this.semester1Courses);
        prior.putAll(this.preArrivalCourses);

        final Collection<CourseInfoGroup> priorGroups = new HashSet<>(this.semester1CourseGroups);
        priorGroups.addAll(this.semester2CourseGroups);

        final List<RequiredPrereq> prereqList = prereqData.get(course);
        if (prereqList != null) {
            for (final RequiredPrereq prereq : prereqList) {
                if (isPrereqNeeded(prereq, this.additionalCourses, prior, this.additionalCourseGroups, priorGroups,
                        true)) {
                    final String courseToAdd = prereq.prereqCourses.getFirst();
                    final RawCourse crs = courseData.get(courseToAdd);

                    if (crs != null) {
                        final CourseInfo info = new CourseInfo(crs, true);
                        info.requiredGrade = prereq.prereqGrades.isEmpty() ? null : prereq.prereqGrades.getFirst();
                        info.status = isAlreadySatisfied(info, data);

                        this.additionalCourses.put(courseToAdd, info);
                        checkSingleAdditionalPrerequisite(courseToAdd, prereqData, courseData, data);
                    }
                }
            }
        }
    }

    /**
     * Fills in any missing prerequisite courses for semester 1 (this may add courses to semester 1 and pre-arrival).
     *
     * @param prereqData a map from course ID to its required prerequisites
     * @param courseData a map from course ID to course data
     * @param data       the student data with past word on record
     */
    private void checkSemester1Prerequisites(final Map<String, ? extends List<RequiredPrereq>> prereqData,
                                             final Map<String, RawCourse> courseData,
                                             final MathPlanStudentData data) {

        final Map<String, CourseInfo> current = new HashMap<>(this.semester1Courses);
        final Map<String, CourseInfo> prior = new HashMap<>(10);
        final Iterable<CourseInfoGroup> currentGroups = new HashSet<>(this.semester1CourseGroups);

        // Courses first... (loop over a copy since the course list may be altered within loop)
        final Map<String, CourseInfo> copy = new HashMap<>(this.semester1Courses);
        for (final String course : copy.keySet()) {
            checkSingleSem1Prerequisite(course, prereqData, courseData, data);
        }

        // Now course groups (only multi-course groups remain)...
        for (final CourseInfoGroup group : this.semester1CourseGroups) {

            final List<String> courseNumbers = group.getCourseNumbers();
            if (group.getNumCredits() == null) {
                // "Pick one course from this list" - see if any course has prereqs satisfied
                final boolean unsatisfied = isPickOneGroupPrereqNeeded(group, prereqData, current, prior, currentGroups,
                        null, false);

                if (unsatisfied) {
                    // Add the prerequisites for the first course
                    for (final RequiredPrereq prereq : prereqData.get(courseNumbers.getFirst())) {

                        if (isPrereqNeeded(prereq, current, prior, this.semester1CourseGroups, null, false)) {
                            // Add the prerequisite course and check its prerequisites
                            final String courseToAdd = prereq.prereqCourses.getFirst();
                            final RawCourse crs = courseData.get(courseToAdd);

                            if (crs != null) {
                                final CourseInfo info = new CourseInfo(crs, true);
                                info.requiredGrade = prereq.prereqGrades.isEmpty() ? null :
                                        prereq.prereqGrades.getFirst();
                                info.status = isAlreadySatisfied(info, data);

                                if (Boolean.TRUE.equals(prereq.mayBeConcurrent)) {
                                    this.semester1Courses.put(courseToAdd, info);
                                    current.put(courseToAdd, info);
                                    checkSingleSem1Prerequisite(courseToAdd, prereqData, courseData, data);
                                } else {
                                    this.preArrivalCourses.put(courseToAdd, info);
                                    prior.put(courseToAdd, info);
                                    checkSinglePreArrivalPrerequisite(courseToAdd, prereqData, courseData, data);
                                }
                            }
                        }
                    }
                }
            } else {
                // "Pick N credits from the following list" - count total credits of courses that
                // are present or that have prerequisites satisfied
                final Collection<String> found = new ArrayList<>(10);
                if (countTotalCreditsWithPrereqMet(group, prereqData, courseData, current, prior, currentGroups, null,
                        found, false) == 0) {

                    // Add the prerequisite for the first course in the group with course data
                    for (final String course : courseNumbers) {
                        final RawCourse cdata = courseData.get(course);
                        if (cdata == null) {
                            continue;
                        }

                        // Add the prerequisites for this course
                        final List<RequiredPrereq> prereqList = prereqData.get(course);
                        if (prereqList != null) {
                            for (final RequiredPrereq prereq : prereqList) {
                                if (isPrereqNeeded(prereq, current, prior, currentGroups, null, false)) {
                                    // Add the prerequisite course and check its prerequisites
                                    final String courseToAdd = prereq.prereqCourses.getFirst();
                                    final RawCourse crs = courseData.get(courseToAdd);

                                    if (crs != null) {
                                        final CourseInfo info = new CourseInfo(crs, true);
                                        info.requiredGrade = prereq.prereqGrades.isEmpty() ? null
                                                : prereq.prereqGrades.getFirst();
                                        info.status = isAlreadySatisfied(info, data);

                                        if (Boolean.TRUE.equals(prereq.mayBeConcurrent)) {
                                            this.semester1Courses.put(courseToAdd, info);
                                            current.put(courseToAdd, info);
                                            checkSingleSem1Prerequisite(courseToAdd, prereqData, courseData, data);
                                        } else {
                                            this.preArrivalCourses.put(courseToAdd, info);
                                            prior.put(courseToAdd, info);
                                            checkSinglePreArrivalPrerequisite(courseToAdd, prereqData, courseData,
                                                    data);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Fills in any missing prerequisite courses for semester 2 (this may add courses to semester 1 and pre-arrival).
     *
     * @param prereqData a map from course ID to its required prerequisites
     * @param courseData a map from course ID to course data
     * @param data       the student data with past word on record
     */
    private void checkSemester2Prerequisites(final Map<String, ? extends List<RequiredPrereq>> prereqData,
                                             final Map<String, RawCourse> courseData,
                                             final MathPlanStudentData data) {

        final Map<String, CourseInfo> current = new HashMap<>(this.semester2Courses);
        final Map<String, CourseInfo> prior = new HashMap<>(this.semester1Courses);
        prior.putAll(this.preArrivalCourses);
        final Iterable<CourseInfoGroup> currentGroups = new HashSet<>(this.semester2CourseGroups);
        final Iterable<CourseInfoGroup> priorGroups = new HashSet<>(this.semester1CourseGroups);

        // Courses first... (loop over a copy since the course list may be altered within loop)
        final Map<String, CourseInfo> copy = new HashMap<>(this.semester2Courses);
        for (final String course : copy.keySet()) {
            checkSingleSem2Prerequisite(course, prereqData, courseData, data);
        }

        // Now course groups (only multi-course groups remain)...
        for (final CourseInfoGroup group : this.semester2CourseGroups) {

            final List<String> courseNumbers = group.getCourseNumbers();
            if (group.getNumCredits() == null) {
                // "Pick one course from this list" - see if any course has prereqs satisfied
                final boolean unsatisfied = isPickOneGroupPrereqNeeded(group, prereqData, current, prior, currentGroups,
                        priorGroups, false);

                if (unsatisfied) {
                    // Add the prerequisites for the first course
                    for (final RequiredPrereq prereq : prereqData.get(courseNumbers.getFirst())) {

                        if (isPrereqNeeded(prereq, current, prior, currentGroups, priorGroups, false)) {
                            // Add the prerequisite course and check its prerequisites
                            final String courseToAdd = prereq.prereqCourses.getFirst();
                            final RawCourse crs = courseData.get(courseToAdd);

                            if (crs != null) {
                                final CourseInfo info = new CourseInfo(crs, true);
                                info.requiredGrade = prereq.prereqGrades.isEmpty() ? null
                                        : prereq.prereqGrades.getFirst();
                                info.status = isAlreadySatisfied(info, data);

                                if (Boolean.TRUE.equals(prereq.mayBeConcurrent)) {
                                    this.semester2Courses.put(courseToAdd, info);
                                    current.put(courseToAdd, info);
                                    checkSingleSem2Prerequisite(courseToAdd, prereqData, courseData, data);
                                } else {
                                    this.semester1Courses.put(courseToAdd, info);
                                    prior.put(courseToAdd, info);
                                    checkSingleSem1Prerequisite(courseToAdd, prereqData, courseData, data);
                                }
                            }
                        }
                    }
                }
            } else {
                // "Pick N credits from the following list" - count total credits of courses that
                // are present or that have prerequisites satisfied
                final Collection<String> found = new ArrayList<>(10);
                if (countTotalCreditsWithPrereqMet(group, prereqData, courseData, current, prior,
                        currentGroups, priorGroups, found, false) == 0) {

                    // Add the prerequisite for the first course in the group with course data
                    for (final String course : courseNumbers) {
                        final RawCourse cdata = courseData.get(course);
                        if (cdata == null) {
                            continue;
                        }

                        // Add the prerequisites for this course
                        final List<RequiredPrereq> prereqList = prereqData.get(course);
                        if (prereqList != null) {
                            for (final RequiredPrereq prereq : prereqList) {
                                if (isPrereqNeeded(prereq, current, prior, currentGroups, priorGroups, false)) {
                                    // Add the prerequisite course and check its prerequisites
                                    final String courseToAdd = prereq.prereqCourses.getFirst();
                                    final RawCourse crs = courseData.get(courseToAdd);

                                    if (crs != null) {
                                        final CourseInfo info = new CourseInfo(crs, true);
                                        info.requiredGrade = prereq.prereqGrades.isEmpty() ? null
                                                : prereq.prereqGrades.getFirst();
                                        info.status = isAlreadySatisfied(info, data);

                                        if (Boolean.TRUE.equals(prereq.mayBeConcurrent)) {
                                            this.semester2Courses.put(courseToAdd, info);
                                            current.put(courseToAdd, info);
                                            checkSingleSem2Prerequisite(courseToAdd, prereqData, courseData, data);
                                        } else {
                                            this.semester1Courses.put(courseToAdd, info);
                                            prior.put(courseToAdd, info);
                                            checkSingleSem1Prerequisite(courseToAdd, prereqData, courseData, data);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Fills in any missing prerequisite courses for additional courses (this will only add courses to the additional
     * course set).
     *
     * @param prereqData a map from course ID to its required prerequisites
     * @param courseData a map from course ID to course data
     * @param data       the student data with past word on record
     */
    private void checkAdditionalPrerequisites(final Map<String, ? extends List<RequiredPrereq>> prereqData,
                                              final Map<String, RawCourse> courseData, final MathPlanStudentData data) {

        final Map<String, CourseInfo> current = new HashMap<>(this.additionalCourses);
        final Map<String, CourseInfo> prior = new HashMap<>(this.semester2Courses);
        prior.putAll(this.semester1Courses);
        prior.putAll(this.preArrivalCourses);
        final Iterable<CourseInfoGroup> currentGroups = new HashSet<>(this.additionalCourseGroups);
        final Collection<CourseInfoGroup> priorGroups = new HashSet<>(this.semester2CourseGroups);
        priorGroups.addAll(this.semester1CourseGroups);

        // Courses first... (loop over a copy since the course list may be altered within loop)
        final Map<String, CourseInfo> copy = new HashMap<>(this.additionalCourses);
        for (final String course : copy.keySet()) {
            checkSingleAdditionalPrerequisite(course, prereqData, courseData, data);
        }

        // Now course groups (only multi-course groups remain)...
        for (final CourseInfoGroup group : this.additionalCourseGroups) {

            final List<String> courseNumbers = group.getCourseNumbers();
            if (group.getNumCredits() == null) {
                // "Pick one course from this list" - see if any course has prereqs satisfied
                final boolean unsatisfied = isPickOneGroupPrereqNeeded(group, prereqData, current, prior, currentGroups,
                        priorGroups, true);

                if (unsatisfied) {
                    // Add the prerequisites for the first course
                    final String first = courseNumbers.getFirst();
                    for (final RequiredPrereq prereq : prereqData.get(first)) {

                        if (isPrereqNeeded(prereq, current, prior, currentGroups, priorGroups, true)) {
                            // Add the prerequisite course and check its prerequisites
                            final String courseToAdd = prereq.prereqCourses.getFirst();
                            final RawCourse crs = courseData.get(courseToAdd);

                            if (crs != null) {
                                final CourseInfo info = new CourseInfo(crs, true);
                                info.requiredGrade = prereq.prereqGrades.isEmpty() ? null
                                        : prereq.prereqGrades.getFirst();
                                info.status = isAlreadySatisfied(info, data);

                                this.additionalCourses.put(courseToAdd, info);
                                current.put(courseToAdd, info);
                                checkSingleAdditionalPrerequisite(courseToAdd, prereqData, courseData, data);
                            }
                        }
                    }
                }
            } else {
                // "Pick N credits from the following list" - if at least one course in the list
                // has prereqs met, consider the group to have prereqs met

                final Collection<String> found = new ArrayList<>(10);
                if (countTotalCreditsWithPrereqMet(group, prereqData, courseData, current, prior,
                        currentGroups, priorGroups, found, true) == 0) {

                    // Add the prerequisite for the first course in the group with course data
                    for (final String course : courseNumbers) {
                        final RawCourse cdata = courseData.get(course);
                        if (cdata == null) {
                            continue;
                        }

                        // Add the prerequisites for this course
                        final List<RequiredPrereq> prereqList = prereqData.get(course);
                        if (prereqList != null) {
                            for (final RequiredPrereq prereq : prereqList) {
                                if (isPrereqNeeded(prereq, current, prior, currentGroups, priorGroups, true)) {
                                    // Add the prerequisite course and check its prerequisites
                                    final String courseToAdd = prereq.prereqCourses.getFirst();
                                    final RawCourse crs = courseData.get(courseToAdd);

                                    if (crs != null) {
                                        final CourseInfo info = new CourseInfo(crs, true);
                                        info.requiredGrade = prereq.prereqGrades.isEmpty() ? null
                                                : prereq.prereqGrades.getFirst();
                                        info.status = isAlreadySatisfied(info, data);

                                        this.additionalCourses.put(courseToAdd, info);
                                        current.put(courseToAdd, info);
                                        checkSingleAdditionalPrerequisite(courseToAdd, prereqData, courseData, data);
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Tests whether the prerequisite for a course is not yet satisfied.
     *
     * @param req           the required prerequisite
     * @param current       courses in the current term
     * @param prior         courses in all prior terms
     * @param currentGroups course groups in the current term
     * @param priorGroups   course groups in the prior term
     * @param isAdditional  true if we're working in the "additional" block, where all courses may be considered as "not
     *                      concurrent"
     * @return {@code true} if the prerequisite is not yet satisfied
     */
    private static boolean isPrereqNeeded(final RequiredPrereq req, final Map<String, CourseInfo> current,
                                          final Map<String, CourseInfo> prior,
                                          final Iterable<CourseInfoGroup> currentGroups,
                                          final Iterable<CourseInfoGroup> priorGroups,
                                          final boolean isAdditional) {

        boolean prereqNeeded = true;

        // Prerequisite is met if ANY courses in the prerequisite list are present (they can be
        // in the current term only if they can be taken concurrently)

        final List<String> reqCourses = req.prereqCourses;
        for (final String reqCourse : reqCourses) {
            if (Boolean.TRUE.equals(req.mayBeConcurrent) || isAdditional) {
                if (current.containsKey(reqCourse) || prior.containsKey(reqCourse)) {
                    prereqNeeded = false;
                    break;
                }
            } else if (prior.containsKey(reqCourse)) {
                prereqNeeded = false;
                break;
            }
        }

        if (prereqNeeded && Boolean.TRUE.equals(req.mayBeConcurrent) && currentGroups != null) {
            for (final CourseInfoGroup group : currentGroups) {
                if (group.getNumCredits() == null) {
                    final List<String> courseNumbers = group.getCourseNumbers();
                    boolean satisfied = true;
                    for (final String courseId : reqCourses) {
                        if (!courseNumbers.contains(courseId)) {
                            satisfied = false;
                            break;
                        }
                    }

                    if (satisfied) {
                        prereqNeeded = false;
                        break;
                    }
                }
            }
        }

        if (prereqNeeded && priorGroups != null) {
            for (final CourseInfoGroup group : priorGroups) {
                if (group.getNumCredits() == null) {
                    final List<String> courseNumbers = group.getCourseNumbers();
                    boolean satisfied = true;
                    for (final String courseId : reqCourses) {
                        if (!courseNumbers.contains(courseId)) {
                            satisfied = false;
                            break;
                        }
                    }

                    if (satisfied) {
                        prereqNeeded = false;
                        break;
                    }
                }
            }
        }

        return prereqNeeded;
    }

    /**
     * Tests whether the prerequisites for a course group have not yet been met.
     *
     * @param group         the course group to test
     * @param prereqData    a map from course ID to list of course prerequisites
     * @param current       courses in the current term
     * @param prior         courses in all prior terms
     * @param currentGroups course groups in the current term
     * @param priorGroups   course groups in the prior term
     * @param isAdditional  true if we're working in the "additional" block, where all courses may be considered as "not
     *                      concurrent"
     * @return {@code true} if the prerequisite is nt yet met
     */
    private static boolean isPickOneGroupPrereqNeeded(final CourseInfoGroup group,
                                                      final Map<String, ? extends List<RequiredPrereq>> prereqData,
                                                      final Map<String, CourseInfo> current,
                                                      final Map<String, CourseInfo> prior,
                                                      final Iterable<CourseInfoGroup> currentGroups,
                                                      final Iterable<CourseInfoGroup> priorGroups,
                                                      final boolean isAdditional) {

        boolean needed = true;

        for (final String course : group.getCourseNumbers()) {
            needed = false;
            final List<RequiredPrereq> prereqList = prereqData.get(course);
            if (prereqList != null) {
                for (final RequiredPrereq prereq : prereqList) {
                    if (isPrereqNeeded(prereq, current, prior, currentGroups, priorGroups, isAdditional)) {
                        needed = true;
                        break;
                    }
                }
            }
            if (!needed) {
                break;
            }
        }

        return needed;
    }

    /**
     * Tests whether a course has already been satisfied as a prerequisite because it was completed at CSU, came in as
     * transfer credit, or has a placement result.
     *
     * @param info the course information object, including required grade
     * @param data the student data with past word on record
     * @return a course status indicating how the prerequisite was satisfied, or {@code ECourseStatus.NONE} if the
     *         prerequisite has not been satisfied
     */
    private static ECourseStatus isAlreadySatisfied(final CourseInfo info, final MathPlanStudentData data) {

        final String courseId = info.course.course;

        ECourseStatus satisfied = ECourseStatus.NONE;

        for (final RawMpeCredit row : data.getPlacementCredit()) {
            if (row.course.equals(courseId)) {
                satisfied = "C".equals(row.examPlaced) ? ECourseStatus.MATH_CHALLENGE_EXAM : ECourseStatus.PLACED_OUT;
                break;
            }
        }

        if (satisfied == ECourseStatus.NONE) {
            for (final LiveCsuCredit row : data.getCompletedCourses()) {
                if (row.courseId.equals(courseId) && row.getGradeGpa() != null) {
                    if (info.requiredGrade == null) {
                        satisfied = ECourseStatus.COMPLETED_AT_CSU;
                    } else if (row.getGradeGpa() != null) {
                        if (row.getGradeGpa().floatValue() >= info.requiredGrade.floatValue()) {
                            satisfied = ECourseStatus.COMPLETED_AT_CSU;
                        } else {
                            satisfied = ECourseStatus.COMPLETED_AT_CSU_GRADE_TOO_LOW;
                        }
                    }
                    break;
                }
            }
        }

        if (satisfied == ECourseStatus.NONE) {
            for (final LiveTransferCredit row : data.getLiveTransferCredit()) {
                final String rowId = row.courseId;
                final Float gpa = row.getGradeGpa();

                if (rowId.equals(courseId) && gpa != null) {
                    if ((info.requiredGrade == null) || (gpa.floatValue() >= info.requiredGrade.floatValue())) {
                        satisfied = ECourseStatus.TRANSFER_CREDIT;
                    } else {
                        satisfied = ECourseStatus.TRANSFER_CREDIT_GRADE_TOO_LOW;
                    }
                    break;
                }
            }
        }

        return satisfied;
    }

    /**
     * Counts the total number of credits of all courses in a course group that are either present in current/prior term
     * courses, or whose prerequisites are present.
     *
     * @param group         the course group to test
     * @param prereqData    a map from course ID to list of course prerequisites
     * @param courseData    a map from course ID to course data
     * @param current       courses in the current term
     * @param prior         courses in all prior terms
     * @param currentGroups course groups in the current term
     * @param priorGroups   course groups in the prior term
     * @param havePrereqs   a list to which to add course IDs that are found
     * @param isAdditional  true if we're working in the "additional" block, where all courses may be considered as "not
     *                      concurrent"
     * @return the total number of credits of courses present or with prerequisites met
     */
    private static int countTotalCreditsWithPrereqMet(final CourseInfoGroup group,
                                                      final Map<String, ? extends List<RequiredPrereq>> prereqData,
                                                      final Map<String, RawCourse> courseData,
                                                      final Map<String, CourseInfo> current,
                                                      final Map<String, CourseInfo> prior,
                                                      final Iterable<CourseInfoGroup> currentGroups,
                                                      final Iterable<CourseInfoGroup> priorGroups,
                                                      final Collection<? super String> havePrereqs,
                                                      final boolean isAdditional) {

        int total = 0;
        for (final String course : group.getCourseNumbers()) {
            final RawCourse cdata = courseData.get(course);
            if (cdata == null) {
                Log.warning(Res.fmt(Res.COURSE_NOT_DEFINED, course));
                continue;
            }

            final boolean satisfied = current.containsKey(course) || prior.containsKey(course);

            if (satisfied) {
                total += cdata.nbrCredits.intValue();
                havePrereqs.add(course);
            } else {
                boolean allprereqsmet = true;
                final List<RequiredPrereq> prereqList = prereqData.get(course);
                if (prereqList != null) {
                    for (final RequiredPrereq prereq : prereqList) {
                        if (isPrereqNeeded(prereq, current, prior, currentGroups, priorGroups, isAdditional)) {
                            allprereqsmet = false;
                            break;
                        }
                    }
                }
                if (allprereqsmet) {
                    total += cdata.nbrCredits.intValue();
                    havePrereqs.add(course);
                }
            }
        }

        return total;
    }

    /**
     * Simplifies "select N credits" groups by reducing the actual number of required credits based on courses that are
     * part of the group and that appear earlier in the program, or as individual courses in the same semester.
     *
     * <p>
     * Note that on entry, actual credit numbers may already have been reduced by having courses within the group
     * satisfied by prior work or placement. This step is considering prior courses in the course sequence that satisfy
     * all or part of the group's remaining credit count.
     *
     * @param courseData a map from course ID to course data
     */
    void simplifyGroups(final Map<String, RawCourse> courseData) {

        for (final CourseInfoGroup group : this.semester1CourseGroups) {
            if (group.getNumCredits() == null) {
                continue;
            }

            for (final CourseInfo info : group.getCourseInfos()) {
                final String courseId = info.course.course;

                if (this.preArrivalCourses.containsKey(courseId) || this.semester1Courses.containsKey(courseId)) {

                    final RawCourse course = courseData.get(courseId);
                    final Integer numCredits = course == null ? null : course.nbrCredits;
                    if (numCredits != null) {
                        if (group.getNumRemainingCredits().intValue() <= numCredits.intValue()) {
                            group.setNumRemainingCredits(Integer.valueOf(0));
                            group.setSatisfied(true);
                        } else {
                            group.setNumRemainingCredits(
                                    Integer.valueOf(group.getNumRemainingCredits().intValue() - numCredits.intValue()));
                        }
                    }
                }
            }
        }

        for (final CourseInfoGroup group : this.semester2CourseGroups) {
            if (group.getNumCredits() == null) {
                continue;
            }

            for (final CourseInfo info : group.getCourseInfos()) {
                final String courseId = info.course.course;

                if (this.preArrivalCourses.containsKey(courseId) || this.semester1Courses.containsKey(courseId) ||
                        this.semester2Courses.containsKey(courseId)) {

                    final RawCourse course = courseData.get(courseId);
                    final Integer numCredits = course == null ? null : course.nbrCredits;
                    if (numCredits != null) {
                        if (group.getNumRemainingCredits().intValue() <= numCredits.intValue()) {
                            group.setNumRemainingCredits(Integer.valueOf(0));
                            group.setSatisfied(true);
                        } else {
                            group.setNumRemainingCredits(
                                    Integer.valueOf(group.getNumRemainingCredits().intValue() - numCredits.intValue()));
                        }
                    }
                }
            }
        }

        for (final CourseInfoGroup group : this.additionalCourseGroups) {
            if (group.getNumCredits() == null) {
                continue;
            }

            for (final CourseInfo info : group.getCourseInfos()) {
                final String courseId = info.course.course;

                if (this.preArrivalCourses.containsKey(courseId) || this.semester1Courses.containsKey(courseId) ||
                        this.semester2Courses.containsKey(courseId) || this.additionalCourses.containsKey(courseId)) {

                    final RawCourse course = courseData.get(courseId);
                    final Integer numCredits = course == null ? null : course.nbrCredits;
                    if (numCredits != null) {
                        if (group.getNumRemainingCredits().intValue() <= numCredits.intValue()) {
                            group.setNumRemainingCredits(Integer.valueOf(0));
                            group.setSatisfied(true);
                        } else {
                            group.setNumRemainingCredits(
                                    Integer.valueOf(group.getNumRemainingCredits().intValue() - numCredits.intValue()));
                        }
                    }
                }
            }
        }
    }

    /**
     * Ensures that some number of core mathematics courses are included in semester 1. This may result in courses being
     * moved forward from semester 2 or later.
     *
     * @param numCredits the number of core courses to be included in semester 1
     */
    void ensureCoreSem1(final int numCredits) {

        final int numCoreInSem1 = countCore(this.semester1Courses, this.semester1CourseGroups);

        if (numCoreInSem1 < numCredits) {
            // Need to move core credits forward to semester 1
            int remain = numCredits;

            final Iterator<CourseInfoGroup> iterator1 = this.semester2CourseGroups.iterator();
            while (iterator1.hasNext() && remain > 0) {
                final CourseInfoGroup next = iterator1.next();
                if (next.getGroupCode().startsWith("AUCC")) {
                    iterator1.remove();
                    this.semester1CourseGroups.add(next);
                    if (next.getNumRemainingCredits() != null) {
                        remain -= next.getNumRemainingCredits().intValue();
                    }
                }
            }

            if (remain > 0) {
                final Iterator<CourseInfoGroup> iterator2 = this.additionalCourseGroups.iterator();
                while (iterator2.hasNext() && remain > 0) {
                    final CourseInfoGroup next = iterator2.next();
                    if (next.getGroupCode().startsWith("AUCC")) {
                        iterator2.remove();
                        this.semester1CourseGroups.add(next);
                        if (next.getNumRemainingCredits() != null) {
                            remain -= next.getNumRemainingCredits().intValue();
                        }
                    }
                }
            }

            if (remain > 0) {
                final Iterator<CourseInfo> iterator3 = this.semester2Courses.values().iterator();
                while (iterator3.hasNext() && remain > 0) {
                    final CourseInfo next = iterator3.next();
                    final String id = next.course.course;

                    if (this.core.courseNumbers.contains(id)) {
                        iterator3.remove();
                        this.semester1Courses.put(id, next);
                        remain -= next.course.nbrCredits.intValue();
                    }
                }
            }

            if (remain > 0) {
                final Iterator<CourseInfo> iterator4 = this.additionalCourses.values().iterator();
                while (iterator4.hasNext() && remain > 0) {
                    final CourseInfo next = iterator4.next();
                    final String id = next.course.course;

                    if (this.core.courseNumbers.contains(id)) {
                        iterator4.remove();
                        this.semester1Courses.put(id, next);
                        remain -= next.course.nbrCredits.intValue();
                    }
                }
            }
        }
    }

    /**
     * Ensures that some number of core mathematics courses are included in semester 1 or 2. This may result in courses
     * being moved forward from the "additional" category.
     *
     * @param numCredits the number of core courses to be included in semester 1 and 2
     */
    void ensureCoreSem12(final int numCredits) {

        final int numCoreInSem12 = countCore(this.semester1Courses, this.semester1CourseGroups)
                + countCore(this.semester2Courses, this.semester2CourseGroups);

        if (numCoreInSem12 < numCredits) {
            // Need to move core credits forward to semester 2
            int remain = numCredits;

            final Iterator<CourseInfoGroup> iterator2 = this.additionalCourseGroups.iterator();
            while (iterator2.hasNext() && remain > 0) {
                final CourseInfoGroup next = iterator2.next();
                if (next.getGroupCode().startsWith("AUCC")) {
                    iterator2.remove();
                    this.semester2CourseGroups.add(next);
                    if (next.getNumRemainingCredits() != null) {
                        remain -= next.getNumRemainingCredits().intValue();
                    }
                }
            }

            if (remain > 0) {
                final Iterator<CourseInfo> iterator3 = this.additionalCourses.values().iterator();
                while (iterator3.hasNext() && remain > 0) {
                    final CourseInfo next = iterator3.next();
                    final String id = next.course.course;

                    if (this.core.courseNumbers.contains(id)) {
                        iterator3.remove();
                        this.semester2Courses.put(id, next);
                        remain -= next.course.nbrCredits.intValue();
                    }
                }
            }
        }
    }

    /**
     * Counts the credits of core mathematics in a course list and course group set.
     *
     * @param courses the course list
     * @param groups  the course group set
     * @return the number of credits of core course work in the course list and group set
     */
    private int countCore(final Map<String, CourseInfo> courses, final Iterable<CourseInfoGroup> groups) {

        int total = 0;

        for (final Map.Entry<String, CourseInfo> entry : courses.entrySet()) {
            if (this.core.courseNumbers.contains(entry.getKey())) {
                total += entry.getValue().course.nbrCredits.intValue();
            }
        }

        for (final CourseInfoGroup grp : groups) {
            if (grp.getGroupCode().startsWith("AUCC")) {
                total += grp.getNumCredits().intValue();
            }
        }

        return total;
    }

    /**
     * Tests whether this sequence has any courses in the "pre-arrival" category.
     *
     * @return the number of pre-arrival prerequisites that remain to satisfy.
     */
    public int hasPreArrivalData() {

        int hasPre = 0;

        for (final CourseInfo info : this.preArrivalCourses.values()) {
            if (!info.status.sufficient) {
                ++hasPre;
            }
        }

        return hasPre;
    }

    /**
     * Gets the pre-arrival courses.
     *
     * @return the pre-arrival courses
     */
    public Map<String, CourseInfo> getPreArrivalCourses() {

        return this.preArrivalCourses;
    }

    /**
     * Counts the number of courses listed in the pre-arrival prerequisites that have not been satisfied.
     *
     * @return the number of unsatisfied prerequisites
     */
    public int getNumPrearrivalPrereqs() {

        int count = 0;

        for (final CourseInfo info : this.preArrivalCourses.values()) {
            if (info.status == ECourseStatus.NONE) {
                ++count;
            }
        }

        return count;
    }

    /**
     * Tests whether this sequence has any courses in the "semester 1" category.
     *
     * @return {@code true} if there are semester 1 courses
     */
    public boolean hasSemester1Data() {

        boolean hasSem1 = false;

        for (final CourseInfo info : this.semester1Courses.values()) {
            if (!info.status.sufficient) {
                hasSem1 = true;
                break;
            }
        }

        if (!hasSem1) {
            for (final CourseInfoGroup group : this.semester1CourseGroups) {
                if (!group.isSatisfied()) {
                    hasSem1 = true;
                    break;
                }
            }
        }

        return hasSem1;
    }

    /**
     * Tests whether there are any courses in semester 1 that the student can place out of using a placement exam.
     *
     * @return {@code true} if there are semester 1 courses that the student could place out of
     */
    public boolean isPrecalcCourseInSemester1() {

        boolean req = this.semester1Courses.containsKey(RawRecordConstants.M117)
                || this.semester1Courses.containsKey(RawRecordConstants.M118)
                || this.semester1Courses.containsKey(RawRecordConstants.M124)
                || this.semester1Courses.containsKey(RawRecordConstants.M125)
                || this.semester1Courses.containsKey(RawRecordConstants.M126);

        if (!req) {
            for (final CourseInfoGroup grp : this.semester1CourseGroups) {
                final String code = grp.getGroupCode();
                if ("AGED3".equals(code) || "ANIM3".equals(code)) {
                    req = true;
                    break;
                }
            }
        }

        return req;
    }

    /**
     * Tests whether there are any courses in semester 2 that the student can place out of using a placement exam.
     *
     * @return {@code true} if there are semester 2 courses that the student could place out of
     */
    boolean isPrecalcCourseInSemester2() {

        boolean req = this.semester2Courses.containsKey(RawRecordConstants.M117)
                || this.semester2Courses.containsKey(RawRecordConstants.M118)
                || this.semester2Courses.containsKey(RawRecordConstants.M124)
                || this.semester2Courses.containsKey(RawRecordConstants.M125)
                || this.semester2Courses.containsKey(RawRecordConstants.M126);

        if (!req) {
            for (final CourseInfoGroup grp : this.semester2CourseGroups) {
                final String code = grp.getGroupCode();
                if ("AGED3".equals(code) || "ANIM3".equals(code)) {
                    req = true;
                    break;
                }
            }
        }

        return req;
    }

    /**
     * Tests whether MATH 101 is needed in semester 1.
     *
     * @return {@code true} if MATH 101 is needed in semester 1.
     */
    boolean needs101InSem1() {

        return isCourseInSemester1("M 101") || isGroupInSemester1("AUCC3") || isGroupInSemester1("AUCC2");
    }

    /**
     * Tests whether MATH 117 is needed in semester 1.
     *
     * @return {@code true} if MATH 117 is needed in semester 1.
     */
    boolean needs117InSem1() {

        final boolean needs117;

        if (isGroupInSemester1("AUCC2") || isGroupInSemester1("AUCC3")) {
            needs117 = false;
        } else {
            needs117 = isCourseInSemester1(RawRecordConstants.M117) || isGroupInSemester1("AGED3")
                    || isGroupInSemester1("ANIM3");
        }

        return needs117;
    }

    /**
     * Tests whether MATH 101 is needed in semester 2.
     *
     * @return {@code true} if MATH 101 is needed in semester 2.
     */
    public boolean needs101InSem2() {

        return isCourseInSemester2("M 101") || isGroupInSemester2("AUCC3") || isGroupInSemester2("AUCC2");
    }

    /**
     * Tests whether a specific course is in semester 1.
     *
     * @param course the course ID
     * @return {@code true} if the course is listed in semester 1 (courses, not groups)
     */
    public boolean isCourseInSemester1(final String course) {

        return this.semester1Courses.containsKey(course);
    }

    /**
     * Tests whether a specific group is in semester 1.
     *
     * @param group the group code
     * @return {@code true} if the group is listed in semester 1
     */
    boolean isGroupInSemester1(final String group) {

        boolean found = false;

        for (final CourseInfoGroup g : this.semester1CourseGroups) {
            final String code = g.getGroupCode();
            if (group.equals(code)) {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Tests whether a specific course is in semester 2.
     *
     * @param course the course ID
     * @return {@code true} if the course is listed in semester 2 (courses, not groups)
     */
    private boolean isCourseInSemester2(final String course) {

        return this.semester2Courses.containsKey(course);
    }

    /**
     * Tests whether a specific group is in semester 2.
     *
     * @param group the group code
     * @return {@code true} if the group is listed in semester 2
     */
    private boolean isGroupInSemester2(final String group) {

        boolean found = false;

        for (final CourseInfoGroup g : this.semester2CourseGroups) {
            final String code = g.getGroupCode();
            if (group.equals(code)) {
                found = true;
                break;
            }
        }

        return found;
    }

    /**
     * Tests whether the student is currently able to register for at least one first-semester course in this sequence.
     *
     * @param canRegisterFor the set of course IDs that the student can register for
     * @return {@code true} if the student can register for at least one first-semester course; {@code false} if there
     *         are no first-semester courses, or the student is not eligible for any of them
     */
    boolean isEligibleToRegisterForSemester1(final Collection<String> canRegisterFor) {

        boolean eligible = false;

        if (!canRegisterFor.isEmpty()) {
            for (final String course : this.semester1Courses.keySet()) {
                if (canRegisterFor.contains(course)) {
                    eligible = true;
                    break;
                }
            }

            if (!eligible) {
                outer:
                for (final CourseInfoGroup grp : this.semester1CourseGroups) {
                    for (final String course : grp.getCourseNumbers()) {
                        if (canRegisterFor.contains(course)) {
                            eligible = true;
                            break outer;
                        }
                    }
                }
            }
        }

        return eligible;
    }

    ///**
    // * Tests whether the student is currently able to register for at least one second-semester
    // * course in this sequence.
    // *
    // * @param canRegisterFor the set of course IDs that the student can register for
    // * @return {@code true} if the student can register for at least one second-semester
    // *         course; {@code false} if there are no second-semester courses, or the student is
    // *         not eligible for any of them
    // */
    // public boolean isEligibleToRegisterForSemester2(final Set<String> canRegisterFor) {
    //
    // boolean eligible = false;
    //
    // if (!canRegisterFor.isEmpty()) {
    // for (String course : this.sem2Courses.keySet()) {
    // if (canRegisterFor.contains(course)) {
    // eligible = true;
    // break;
    // }
    // }
    //
    // if (!eligible) {
    // outer: for (CourseInfoGroup grp : this.sem2CourseGroups) {
    // for (String course : grp.getCourseNumbers()) {
    // if (canRegisterFor.contains(course)) {
    // eligible = true;
    // break outer;
    // }
    // }
    // }
    // }
    // }
    //
    // return eligible;
    // }

    /**
     * Gets the semester 1 course groups.
     *
     * @return the semester 1 course groups
     */
    public Set<CourseInfoGroup> getSemester1CourseGroups() {

        return this.semester1CourseGroups;
    }

    /**
     * Gets the semester 1 courses.
     *
     * @return the semester 1 courses
     */
    public Map<String, CourseInfo> getSemester1Courses() {

        return this.semester1Courses;
    }

    /**
     * Tests whether this sequence has any courses in the "semester 2" category.
     *
     * @return {@code true} if there are semester 2 courses
     */
    public boolean hasSemester2Data() {

        boolean hasSem2 = false;

        for (final CourseInfo info : this.semester2Courses.values()) {
            if (!info.status.sufficient) {
                hasSem2 = true;
                break;
            }
        }

        if (!hasSem2) {
            for (final CourseInfoGroup group : this.semester2CourseGroups) {
                if (!group.isSatisfied()) {
                    hasSem2 = true;
                    break;
                }
            }
        }

        return hasSem2;
    }

    /**
     * Gets the semester 2 course groups.
     *
     * @return the semester 2 course groups
     */
    public Set<CourseInfoGroup> getSemester2CourseGroups() {

        return this.semester2CourseGroups;
    }

    /**
     * Gets the semester 2 courses.
     *
     * @return the semester 2 courses
     */
    public Map<String, CourseInfo> getSemester2Courses() {

        return this.semester2Courses;
    }

    /**
     * Tests whether this sequence has any courses in the "additional" category.
     *
     * @return {@code true} if there are additional courses
     */
    public boolean hasAdditionalData() {

        boolean hasAdd = false;

        for (final CourseInfo info : this.additionalCourses.values()) {
            if (!info.status.sufficient) {
                hasAdd = true;
                break;
            }
        }

        if (!hasAdd) {
            for (final CourseInfoGroup group : this.additionalCourseGroups) {
                if (!group.isSatisfied()) {
                    hasAdd = true;
                    break;
                }
            }
        }

        return hasAdd;
    }

    /**
     * Gets the additional course groups.
     *
     * @return the additional course groups
     */
    public Set<CourseInfoGroup> getAdditionalCourseGroups() {

        return this.additionalCourseGroups;
    }

    /**
     * Gets the additional courses.
     *
     * @return the additional courses
     */
    public Map<String, CourseInfo> getAdditionalCourses() {

        return this.additionalCourses;
    }

    /**
     * Tests whether the sequence included multiple calculus I courses.
     *
     * @return {@code true} if there were multiple calculus I courses
     */
    public boolean hasMultipleCalc1() {

        return this.multipleCalc1;
    }

    /**
     * Tests whether the sequence included multiple calculus II courses.
     *
     * @return {@code true} if there were multiple calculus II courses
     */
    public boolean hasMultipleCalc2() {

        return this.multipleCalc2;
    }

    ///**
    // * Dumps the internal data to a log.
    // *
    // * @param title a title for the log record
    // */
    // public void dumpLog(final String title) {
    //
    // final HtmlBuilder str = new HtmlBuilder(120);
    //
    // str.add(CoreConstants.SPC, title, " PRE: ");  
    // dumpCourseList(str, this.preArrivalCourses, null);
    // str.add(" S1: ");
    // dumpCourseList(str, this.sem1Courses, this.sem1CourseGroups);
    // str.add(" S2: ");
    // dumpCourseList(str, this.sem2Courses, this.sem2CourseGroups);
    // str.add(" ADD: ");
    // dumpCourseList(str, this.additionalCourses, this.additionalCourseGroups);
    //
    // Log.fine(str.toString());
    // }

    ///**
    // * Dumps a course list and a course group list to an {@code HtmlBuilder}.
    // *
    // * @param str the {@code HtmlBuilder} to which to append
    // * @param courses the courses to emit
    // * @param groups the groups to emit
    // */
    // private static void dumpCourseList(final HtmlBuilder str, final Map<String, CourseInfo>
    // courses,
    // final Set<CourseInfoGroup> groups) {
    //
    // boolean comma = false;
    // for (String key : courses.keySet()) {
    // if (comma) {
    // str.add(',');
    // }
    // str.add(key);
    // comma = true;
    // }
    //
    // if (groups != null) {
    // for (CourseInfoGroup grp : groups) {
    // if (comma) {
    // str.add(',');
    // }
    // str.add('[');
    // str.add(grp.getCourseGroup().getGroupCode());
    // str.add(']');
    // comma = true;
    // }
    // }
    //
    // if (!comma) {
    // str.add("(none)");
    // }
    // }
}

package dev.mathops.db.old.logic.mathplan.data;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.logic.mathplan.MathPlanLogic;
import dev.mathops.db.old.rawrecord.RawCourse;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A container for the course recommendations for a student based on a selection of majors.
 */
public final class CourseRecommendations {

    /** The critical course sequence. */
    public final CourseSequence criticalSequence;

    /** The recommended course sequence. */
    final CourseSequence recommendedSequence;

    /** The typical course sequence. */
    public final CourseSequence typicalSequence;

    /**
     * Constructs a new {@code CourseRecommendations} by analyzing requirements for a collection of selected majors to
     * determine the courses to recommend.
     *
     * @param requirements the map from major to its list of requirements (could be empty)
     * @param data         the student data with past word on record
     * @param logic        the logic object
     */
    CourseRecommendations(final Iterable<MajorMathRequirement> requirements, final MathPlanStudentData data,
                          final MathPlanLogic logic) {

        final CourseGroup core = logic.getCourseGroups().get("AUCC3");

        this.criticalSequence = new CourseSequence(core);
        this.recommendedSequence = new CourseSequence(core);
        this.typicalSequence = new CourseSequence(core);

        final Map<String, RawCourse> courseData = logic.getCourses();
        final Map<String, CourseGroup> groupData = logic.getCourseGroups();

        // Install math requirements for ALL indicated majors
        for (final MajorMathRequirement req : requirements) {

            // NOTE: The MajorMathRequirement container has names of course groups only - no
            // individual courses, although many of its groups are single-course "groups".

            // The "Critical" path has all critical courses in their proper semesters, and all
            // non-critical or other courses in the "additional" semester
            addRequiredGroups(this.criticalSequence.getSemester1CourseGroups(), req.sem1Critical,
                    groupData, courseData);
            addRequiredGroups(this.criticalSequence.getSemester2CourseGroups(), req.sem2Critical,
                    groupData, courseData);
            addRequiredGroups(this.criticalSequence.getAdditionalCourseGroups(),
                    req.sem1Recommended, groupData, courseData);
            addRequiredGroups(this.criticalSequence.getAdditionalCourseGroups(),
                    req.sem2Recommended, groupData, courseData);
            addRequiredGroups(this.criticalSequence.getAdditionalCourseGroups(), req.sem1Typical,
                    groupData, courseData);
            addRequiredGroups(this.criticalSequence.getAdditionalCourseGroups(), req.sem2Typical,
                    groupData, courseData);
            addRequiredGroups(this.criticalSequence.getAdditionalCourseGroups(), req.additional,
                    groupData, courseData);

            // The "Recommended" path has all critical and recommended courses in their proper
            // semesters, and all others in the "additional" semester
            addRequiredGroups(this.recommendedSequence.getSemester1CourseGroups(), req.sem1Critical,
                    groupData, courseData);
            addRequiredGroups(this.recommendedSequence.getSemester1CourseGroups(),
                    req.sem1Recommended, groupData, courseData);
            addRequiredGroups(this.recommendedSequence.getSemester2CourseGroups(), req.sem2Critical,
                    groupData, courseData);
            addRequiredGroups(this.recommendedSequence.getSemester2CourseGroups(),
                    req.sem2Recommended, groupData, courseData);
            addRequiredGroups(this.recommendedSequence.getAdditionalCourseGroups(), req.sem1Typical,
                    groupData, courseData);
            addRequiredGroups(this.recommendedSequence.getAdditionalCourseGroups(), req.sem2Typical,
                    groupData, courseData);
            addRequiredGroups(this.recommendedSequence.getAdditionalCourseGroups(), req.additional,
                    groupData, courseData);

            // The "Typical" path installs all courses in their proper semesters
            addRequiredGroups(this.typicalSequence.getSemester1CourseGroups(), req.sem1Critical,
                    groupData, courseData);
            addRequiredGroups(this.typicalSequence.getSemester1CourseGroups(), req.sem1Recommended,
                    groupData, courseData);
            addRequiredGroups(this.typicalSequence.getSemester1CourseGroups(), req.sem1Typical,
                    groupData, courseData);
            addRequiredGroups(this.typicalSequence.getSemester2CourseGroups(), req.sem2Critical,
                    groupData, courseData);
            addRequiredGroups(this.typicalSequence.getSemester2CourseGroups(), req.sem2Recommended,
                    groupData, courseData);
            addRequiredGroups(this.typicalSequence.getSemester2CourseGroups(), req.sem2Typical,
                    groupData, courseData);
            addRequiredGroups(this.typicalSequence.getAdditionalCourseGroups(), req.additional,
                    groupData, courseData);
        }

        // dumpLog("MajorMathRequirements are all loaded");

        // Eliminate any duplicate entries, keeping only the earliest instance
        this.criticalSequence.eliminateDuplicates();
        this.recommendedSequence.eliminateDuplicates();
        this.typicalSequence.eliminateDuplicates();

        // dumpLog("Duplicates have been eliminated");

        // Convert all course groups containing a single course to single course entries
        this.criticalSequence.convertOneCourseGroups(courseData);
        this.recommendedSequence.convertOneCourseGroups(courseData);
        this.typicalSequence.convertOneCourseGroups(courseData);

        // dumpLog("Single-course groups have been converted");

        // If any of the single courses extracted above satisfies any remaining multi-course
        // groups, remove the multi-course group
        this.criticalSequence.cleanNeedlessGroups(courseData);
        this.recommendedSequence.cleanNeedlessGroups(courseData);
        this.typicalSequence.cleanNeedlessGroups(courseData);

        // dumpLog("Needless groups have been cleaned");

        // If multiple courses for which credit cannot be earned together exist, merge them
        this.criticalSequence.mergeNoCreditForBothCourses(courseData);
        this.recommendedSequence.mergeNoCreditForBothCourses(courseData);
        this.typicalSequence.mergeNoCreditForBothCourses(courseData);

        // dumpLog("Mutual exclusion courses have been merged");

        // Mark courses or course groups that the student has completed by completing courses or
        // with transfer credit, has earned placement credit, or has placed out of
        this.criticalSequence.markStatus(data);
        this.recommendedSequence.markStatus(data);
        this.typicalSequence.markStatus(data);

        // dumpLog("Course status has been marked");

        // For each sequence, add any missing prerequisite courses (after this call, all sequences
        // are "valid" in that students following that sequence will never get to a term and have
        // to take a course for which prerequisites have not been met).

        final Map<String, List<RequiredPrereq>> prereqData = logic.getRequiredPrereqs();

        this.criticalSequence.ensurePrerequisitesMet(prereqData, courseData, data);
        this.recommendedSequence.ensurePrerequisitesMet(prereqData, courseData, data);
        this.typicalSequence.ensurePrerequisitesMet(prereqData, courseData, data);

        // dumpLog("Prerequisites have been added");

        // Re-scan to eliminate any duplicate entries created during prerequisite processing
        this.criticalSequence.eliminateDuplicates();
        this.recommendedSequence.eliminateDuplicates();
        this.typicalSequence.eliminateDuplicates();

        // dumpLog("Duplicates have been eliminated again");

        // Simplify groups
        this.criticalSequence.simplifyGroups(courseData);
        this.recommendedSequence.simplifyGroups(courseData);
        this.typicalSequence.simplifyGroups(courseData);

        // dumpLog("Course groups have been simplified");

        // Adding prerequisites may have satisfied some groups - clean them again
        this.criticalSequence.cleanNeedlessGroups(courseData);
        this.recommendedSequence.cleanNeedlessGroups(courseData);
        this.typicalSequence.cleanNeedlessGroups(courseData);

        // dumpLog("Needless groups have been cleaned again");

        // Move individual courses forward to the group they could satisfy
        this.criticalSequence.resolveGroups();
        this.recommendedSequence.resolveGroups();
        this.typicalSequence.resolveGroups();

        // dumpLog("Course groups have been resolved");

        // If student is closing in on the 60-credit limit, do some additional CRITICAL marking
        mark60CreditCriticals(data);

        // Collect a list of ALL required courses to see if the student needs a calculus course
        final Map<String, CourseInfo> all = new HashMap<>(10);
        all.putAll(this.criticalSequence.getPreArrivalCourses());
        all.putAll(this.criticalSequence.getSemester1Courses());
        all.putAll(this.criticalSequence.getSemester2Courses());
        all.putAll(this.criticalSequence.getAdditionalCourses());

        for (final CourseInfoGroup g : this.criticalSequence.getSemester1CourseGroups()) {
            final String lowest = g.getLowestLastCourse();
            final RawCourse lowestCourse = courseData.get(lowest);
            all.put(lowest, new CourseInfo(lowestCourse, false));
        }

        for (final CourseInfoGroup g : this.criticalSequence.getSemester2CourseGroups()) {
            final String lowest = g.getLowestLastCourse();
            final RawCourse lowestCourse = courseData.get(lowest);
            all.put(lowest, new CourseInfo(lowestCourse, false));
        }

        for (final CourseInfoGroup g : this.criticalSequence.getAdditionalCourseGroups()) {
            final String lowest = g.getLowestLastCourse();
            final RawCourse lowestCourse = courseData.get(lowest);
            all.put(lowest, new CourseInfo(lowestCourse, false));
        }

//        boolean needsCalc = false;
//        boolean needsMath160 = false;
//        for (final String courseId : all.keySet()) {
//            if ("M 160".equals(courseId)) {
//                needsCalc = true;
//                needsMath160 = true;
//            } else if ("M 155".equals(courseId) || "M 141".equals(courseId)) {
//                needsCalc = true;
//            }
//        }
    }

    /**
     * Given a list of course group names (Strings) and a map from group name to group, adds all groups represented by
     * the list of names to a list of groups.
     *
     * @param requirements the list to which to add groups
     * @param groupNames   the list of group names
     * @param groupData    the map from group name to group
     * @param courseData   map from course ID to course info
     */
    private static void addRequiredGroups(final Collection<? super CourseInfoGroup> requirements,
                                          final Iterable<String> groupNames, final Map<String, CourseGroup> groupData,
                                          final Map<String, RawCourse> courseData) {

        for (final String name : groupNames) {
            final CourseGroup grp = groupData.get(name);
            if (grp == null) {
                final String msg = Res.fmt(Res.NO_COURSE_GROUP_WITH_CODE, name);
                Log.warning(msg);
            } else {
                requirements.add(new CourseInfoGroup(grp, courseData));
            }
        }
    }

    /**
     * Tests whether the student is close to the 60-credit limit (based on transfer credits and completed courses, but
     * still has not completed 3 credits of core mathematics). This may cause courses in the first or second semester to
     * be marked "critical".
     *
     * @param data student data
     */
    private void mark60CreditCriticals(final MathPlanStudentData data) {

        final double coreCompleted = data.getCreditsOfCoreCompleted();
        final int coreRemaining = (int) Math.ceil(3.0 - coreCompleted);

        if (coreRemaining > 0) {
            final double numTransfer = data.countTransferCredits();

            if (numTransfer >= 45.0) {
                // Student will likely hit 60 credits at end of semester 1, so needed core credits
                // in semester 1 should be CRITICAL
                this.criticalSequence.ensureCoreSem1(coreRemaining);
                this.recommendedSequence.ensureCoreSem1(coreRemaining);
                this.typicalSequence.ensureCoreSem1(coreRemaining);
            } else if (numTransfer >= 30.0) {
                // Student will likely hit 60 credits at end of semester 1, so needed core credits
                // in semester 1 or 2 should be CRITICAL
                this.criticalSequence.ensureCoreSem12(coreRemaining);
                this.recommendedSequence.ensureCoreSem12(coreRemaining);
                this.typicalSequence.ensureCoreSem12(coreRemaining);
            }
        }
    }
}

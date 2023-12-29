package dev.mathops.web.site.course.data;

import java.util.ArrayList;
import java.util.List;

/**
 * The data for a course module.
 */
public final class ModuleData {

    /** The owning course. */
    public final CourseData course;

    /** The module number, from 1 to 8. */
    public final int moduleNumber;

    /** The module title. */
    public final String moduleTitle;

    /** The Skills Review data. */
    public final SkillsReviewData skillsReview;

    /** The Skills Review data. */
    public final String thumbnailImage;

    /** The list of learning targets. */
    public final List<LearningTargetData> learningTargets;

    /**
     * Constructs a new {@code ModuleData}.
     *
     * @param theCourse         the owning course
     * @param theModuleNumber   the module number, such as "1"
     * @param theModuleTitle    the module title
     * @param theSRAssignmentId the Skills Review assignment ID
     * @param theThumbnailImage the thumbnail image filename
     */
    ModuleData(final CourseData theCourse, final int theModuleNumber, final String theModuleTitle,
               final String theSRAssignmentId, final String theThumbnailImage) {

        this.course = theCourse;
        this.moduleNumber = theModuleNumber;
        this.moduleTitle = theModuleTitle;
        this.skillsReview = new SkillsReviewData(theCourse, theModuleNumber, theSRAssignmentId);
        this.thumbnailImage = theThumbnailImage;

        this.learningTargets = new ArrayList<>(3);
    }

    /**
     * Creates a {@code LearningTargetData} for a learning target and adds it to the module.
     *
     * @param theUnit         the unit number
     * @param theObjective    the objective number
     * @param theTargetNumber the learning target number, such as "1.1"
     * @param theAssignmentId the assignment ID
     * @param theMainOutcome  the main outcome, typically of the form "I can ..., including:"
     * @param theSuboutcomes  a list of sub-outcomes
     * @return the generated module
     */
    LearningTargetData addLearningTarget(final int theUnit, final int theObjective, final String theTargetNumber,
                                         final String theAssignmentId, final String theMainOutcome,
                                         final String... theSuboutcomes) {

        final LearningTargetData target = new LearningTargetData(this, theUnit, theObjective, theTargetNumber,
                theAssignmentId, theMainOutcome, theSuboutcomes);

        this.learningTargets.add(target);

        return target;
    }
}

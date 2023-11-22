package dev.mathops.web.site.course.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a Skills Review section of a course module.
 */
public final class SkillsReviewData {

    /** The owning course. */
    private final CourseData course;

    /** The module number. */
    public final int moduleNumber;

    /** The assignment ID. */
    public final String assignmentId;

    /** The list of example blocks. */
    public final List<ExampleBlock> exampleBlocks;

    /**
     * Constructs a new {@code SkillsReviewData}.
     *
     * @param theCourse       the owning course
     * @param theModuleNumber the module number, such as "1"
     * @param theAssignmentId the assignment ID
     */
    SkillsReviewData(final CourseData theCourse, final int theModuleNumber,
                     final String theAssignmentId) {

        this.course = theCourse;
        this.moduleNumber = theModuleNumber;
        this.assignmentId = theAssignmentId;
        this.exampleBlocks = new ArrayList<>(10);
    }

    /**
     * Creates and adds an example block.
     *
     * @param theTitle the title
     * @return the created example block
     */
    ExampleBlock addExampleBlock(final String theTitle) {

        final ExampleBlock block = new ExampleBlock(this.course, theTitle);
        this.exampleBlocks.add(block);

        return block;
    }
}

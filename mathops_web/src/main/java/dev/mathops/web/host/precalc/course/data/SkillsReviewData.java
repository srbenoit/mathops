package dev.mathops.web.host.precalc.course.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Data for a Skills Review section of a course module.
 */
public final class SkillsReviewData {

    /** The owning course. */
    private final CourseData course;

    /** The assignment ID. */
    public final String assignmentId;

    /** The list of example blocks. */
    public final List<ExampleBlock> exampleBlocks;

    /**
     * Constructs a new {@code SkillsReviewData}.
     *
     * @param theCourse       the owning course
     * @param theAssignmentId the assignment ID
     */
    SkillsReviewData(final CourseData theCourse, final String theAssignmentId) {

        this.course = theCourse;
        this.assignmentId = theAssignmentId;
        this.exampleBlocks = new ArrayList<>(10);
    }

    /**
     * Creates and adds an example block.
     *
     * @param theTitle the title
     * @return the created example block
     */
    ExampleBlock addExBlock(final String theTitle) {

        final ExampleBlock block = new ExampleBlock(this.course, theTitle);
        this.exampleBlocks.add(block);

        return block;
    }
}

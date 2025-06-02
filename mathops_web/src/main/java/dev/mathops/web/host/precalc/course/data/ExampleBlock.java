package dev.mathops.web.host.precalc.course.data;

import java.util.ArrayList;
import java.util.List;

/**
 * A block of examples
 */
public final class ExampleBlock {

    /** The owning course. */
    public final CourseData course;

    /** The block title. */
    public final String title;

    /** The individual examples. */
    public final List<ExampleData> examples;

    /**
     * Constructs a new {@code ExampleBlock}.
     *
     * @param theCourse the owning course
     * @param theTitle  the block title
     */
    ExampleBlock(final CourseData theCourse, final String theTitle) {

        this.course = theCourse;
        this.title = theTitle;
        this.examples = new ArrayList<>(10);
    }

    /**
     * Creates a new example and adds it to this block.
     *
     * @param theMediaId the media ID
     * @param theLabel   the label for the new example
     * @return this object, to allow invocation chaining
     */
    ExampleBlock addEx(final String theMediaId, final String theLabel) {

        this.examples.add(new ExampleData(theMediaId, theLabel));

        return this;
    }
}

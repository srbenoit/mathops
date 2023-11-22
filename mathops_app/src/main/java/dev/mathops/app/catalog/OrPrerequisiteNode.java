package dev.mathops.app.catalog;

import java.util.Arrays;
import java.util.List;

/**
 * A node in a prerequisite tree in which the student must complete at least one of the child prerequisites.
 */
final class OrPrerequisiteNode extends AbstractPrerequisiteNode {

    /** The list of possible ways to satisfy the prerequisite. */
    private final List<AbstractPrerequisiteNode> children;

    /**
     * Constructs a new {@code OrPrerequisiteNode}.
     *
     * @param theChildren the list of required courses (may not be null or empty)
     * @throws IllegalArgumentException if {@code theChildren} is null or empty
     */
    OrPrerequisiteNode(final AbstractPrerequisiteNode... theChildren) {

        super();

        if (theChildren == null || theChildren.length == 0) {
            throw new IllegalArgumentException("Child list may not be null or empty");
        }

        this.children = Arrays.asList(theChildren);
    }

    /**
     * Gets the number of courses in this option.
     *
     * @return the number of courses
     */
    public int getNumChildren() {

        return this.children.size();
    }

    /**
     * Gets a particular course.
     *
     * @param index the 0-based index
     * @return the course
     */
    public AbstractPrerequisiteNode getChild(final int index) {

        return this.children.get(index);
    }
}

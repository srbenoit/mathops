package dev.mathops.dbjobs.report.analytics.longitudinal.program;

import dev.mathops.text.builder.HtmlBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * A student's path.
 */
public final class Path implements Comparable<Path> {

    /** A string representation of the path, used for sorting. */
    private final String key;

    /**
     * The nodes in the path - the first is of the form "Needs MATH XXX", the next N are course IDs, and the last is
     * either "Completed" or "Did not complete".
     */
    private final List<String> nodes;

    /** The number of times this path has been found in a population and student completed requirements. */
    private int numCompleted;

    /** The number of times this path has been found in a population and student did not completed requirements. */
    private int numDidNotComplete;

    /** The list of child paths. */
    private final List<Path> childPaths;

    /**
     * Constructs a new {@code Path}.
     *
     * @param theNodes the nodes in the path
     */
    Path(final List<String> theNodes) {

        final HtmlBuilder builder = new HtmlBuilder(100);
        final String first = theNodes.getFirst();
        builder.add(first);

        final int size = theNodes.size();
        for (int i = 1; i < size; ++i) {
            final String node = theNodes.get(i);
            builder.add("~", node);
        }

        this.key = builder.toString();
        this.nodes = new ArrayList<>(theNodes);
        this.childPaths = new ArrayList<>(10);
    }

    /**
     * Gets the number of times this path has been detected in a population in which the student completed program
     * requirements.
     *
     * @return the count
     */
    int getNumCompleted() {

        return this.numCompleted;
    }

    /**
     * Gets the number of times this path has been detected in a population in which the student did not complete
     * program requirements.
     *
     * @return the count
     */
    int getNumDidNotComplete() {

        return this.numDidNotComplete;
    }

    /**
     * Increments the number of times this path has been detected in a population.
     *
     * @param completed true if the student completed program requirements
     */
    void incrementCount(final boolean completed) {

        if (completed) {
            ++this.numCompleted;
        } else {
            ++this.numDidNotComplete;
        }
    }

    /**
     * Gets the number of child paths.
     *
     * @return the number of child paths
     */
    int getNumChildren() {

        return this.childPaths.size();
    }

    /**
     * Gets the number of "completed" and "did not complete" for this node and all descendants.
     *
     * @return the total count
     */
    int getTotalCount() {

        int total = this.numCompleted + this.numDidNotComplete;

        for (final Path child : this.childPaths) {
            total += child.getTotalCount();
        }

        return total;
    }

    /**
     * Gets a child path.
     *
     * @param index the 0-based index of the child path
     * @return the child path
     */
    Path getChild(final int index) {

        return this.childPaths.get(index);
    }

    /**
     * Given a list of {@code Path} objects, finds all that are direct children of this object and adds them to this
     * object's "childPaths" list, while deleting them from the provided list.  For each child, this method is called on
     * the child to collect that child's children, and recursively until all descendant paths are detected.
     *
     * @param paths the paths to scan
     */
    void collectChildPaths(final List<Path> paths) {

        final int mySize = size();

        // Step 1: Look for all "immediate children" with exactly one more MATH course.
        final int numPaths = paths.size();
        for (int index = numPaths - 1; index >= 0; --index) {
            final Path potentialChild = paths.get(index);

            final int childSize = potentialChild.size();
            if (childSize == mySize + 1 && potentialChild.isDescendantOf(this)) {
                this.childPaths.add(potentialChild);
                paths.remove(index);
                // NOTE: we don't recursively call this method here since it could alter the list we're iterating...
            }
        }

        // Recursively gather deeper descendants of those "immediate children".
        for (final Path child : this.childPaths) {
            child.collectChildPaths(paths);
        }
    }

    /**
     * Gets the key.
     *
     * @return the key
     */
    String getKey() {

        return this.key;
    }

    /**
     * Gets the key that would belong to this path's parent.
     *
     * @return the parent key
     */
    String makeParentKey() {

        final int keyLen = this.key.length();
        final HtmlBuilder builder = new HtmlBuilder(keyLen);

        final String first = this.nodes.getFirst();
        builder.add(first);

        final int size = this.nodes.size() - 1;
        for (int i = 1; i < size; ++i) {
            final String node = this.nodes.get(i);
            builder.add("~", node);
        }

        return builder.toString();
    }

    /**
     * Makes a parent path when a path is found that has no parent.
     *
     * @return the parent path
     */
    Path makeParentPath() {

        final List<String> shortened = new ArrayList<>(this.nodes);
        shortened.removeLast();

        return new Path(shortened);
    }

    /**
     * Gets the number of nodes in the path.
     *
     * @return the number of nodes
     */
    int size() {

        return this.nodes.size();
    }

    /**
     * Gets a node in the path.
     *
     * @param index the index (0-based)
     * @return the node at the requested index
     */
    String getNode(final int index) {

        return this.nodes.get(index);
    }

    /**
     * Tests whether this path is a descendant of a parent path.  If N is the size of the parent path, then this would
     * require that this node's size is at least N + 1, and that the first N nodes in this object's path match those in
     * the parent object.
     *
     * @param parent the parent path
     * @return true if this node is a descendant of the parent node
     */
    boolean isDescendantOf(final Path parent) {

        final int parentSize = parent.size();
        final int mySize = size();

        boolean isDescendant = false;

        if (mySize > parentSize) {
            isDescendant = true;
            for (int i = 0; isDescendant && i < parentSize; ++i) {
                final String parentNode = parent.getNode(i);
                final String myNode = getNode(i);
                isDescendant = parentNode.equals(myNode);
            }
        }

        return isDescendant;
    }

    /**
     * Compares two Paths for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final Path o) {

        final String oKey = o.getKey();
        return -this.key.compareTo(oKey);
    }
}

package dev.mathops.db.old.rawrecord;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.rec.RecBase;
import dev.mathops.text.builder.HtmlBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A raw "tree_path" record. A tree path is a slash-separated list of identifiers that starts and ends with a slash.
 */
public final class RawTreePath extends RecBase implements Comparable<RawTreePath> {

    /** A field name. */
    private static final String FLD_IDENT = "ident";

    /** A field name. */
    private static final String FLD_PARENT_IDENT = "parent_ident";

    /** A field name. */
    private static final String FLD_DEPTH = "depth";

    /** A field name. */
    private static final String FLD_SORT_ORDER = "sort_order";

    /** A field name. */
    private static final String FLD_LABEL = "label";

    /** The 'ident' field value. */
    public String ident;

    /** The 'parent_ident' field value (null for top-level nodes). */
    public String parentIdent;

    /** The 'depth' field value (0 for top-level nodes, 1 for their children, etc.). */
    public Integer depth;

    /** The 'sort_order' field value. */
    public Integer sortOrder;

    /** The 'label' field value. */
    public String label;

    /**
     * Constructs a new {@code RawTreePath}.
     */
    private RawTreePath() {

        super();
    }

    /**
     * Constructs a new {@code RawTreePath}.
     *
     * @param theIdent       the 'ident' field value
     * @param theParentIdent the 'parent_ident' field value
     * @param theDepth       the 'depth' field value
     * @param theSortOrder   the 'sort_order' field value
     * @param theLabel       the 'label' field value
     */
    public RawTreePath(final String theIdent, final String theParentIdent, final Integer theDepth,
                       final Integer theSortOrder, final String theLabel) {

        super();

        this.ident = theIdent;
        this.parentIdent = theParentIdent;
        this.depth = theDepth;
        this.sortOrder = theSortOrder;
        this.label = theLabel;
    }

    /**
     * Extracts a "tree_path" record from a result set.
     *
     * @param rs the result set from which to retrieve the record
     * @return the record
     * @throws SQLException if there is an error accessing the database
     */
    public static RawTreePath fromResultSet(final ResultSet rs) throws SQLException {

        final RawTreePath result = new RawTreePath();

        result.ident = getStringField(rs, FLD_IDENT);
        result.parentIdent = getStringField(rs, FLD_PARENT_IDENT);
        result.depth = getIntegerField(rs, FLD_DEPTH);
        result.sortOrder = getIntegerField(rs, FLD_SORT_ORDER);
        result.label = getStringField(rs, FLD_LABEL);

        return result;
    }

    /**
     * Compares two records for order.
     *
     * @param o the object to be compared
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     *         the specified object
     */
    @Override
    public int compareTo(final RawTreePath o) {

        return compareAllowingNull(this.sortOrder, o.sortOrder);
    }

    /**
     * A tree node used to organize {@code RawTreePath} objects into a tree.
     */
    public static class TreeNode implements Comparable<TreeNode> {

        /** The parent node; null only for top-level nodes. */
        final TreeNode parent;

        /** The tree path. */
        public final RawTreePath treePath;

        /** The list of child nodes. */
        public final List<TreeNode> nodes;

        /**
         * Constructs a new {@code TreeNode}.
         *
         * @param theParent   the parent node
         * @param theTreePath the tree path
         */
        public TreeNode(final TreeNode theParent, final RawTreePath theTreePath) {

            this.parent = theParent;
            this.treePath = theTreePath;
            this.nodes = new ArrayList<>(10);
        }

        /**
         * Compares two records for order.
         *
         * @param o the object to be compared
         * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater
         *         than the specified object
         */
        @Override
        public int compareTo(final TreeNode o) {

            return compareAllowingNull(this.treePath.sortOrder, o.treePath.sortOrder);
        }

        /**
         * Gets the complete tree path, which is a slash-separated list of identifiers that starts and ends with a
         * slash.
         *
         * @return the path
         */
        public String getPath() {

            final StringBuilder path = new StringBuilder(64);
            if (this.parent == null) {
                path.append(CoreConstants.SLASH);
            } else {
                path.append(this.parent.getPath());
            }

            // Parent path will end in SLASH

            path.append(this.treePath.ident).append(CoreConstants.SLASH);

            return path.toString();
        }
    }

    /**
     * Generates a string serialization of the record. Each concrete subclass should have a constructor that accepts a
     * single {@code String} to reconstruct the object from this string.
     *
     * @return the string
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(40);

        appendField(htm, FLD_IDENT, this.ident);
        htm.add(DIVIDER);
        appendField(htm, FLD_PARENT_IDENT, this.parentIdent);
        htm.add(DIVIDER);
        appendField(htm, FLD_DEPTH, this.depth);
        htm.add(DIVIDER);
        appendField(htm, FLD_SORT_ORDER, this.sortOrder);
        htm.add(DIVIDER);
        appendField(htm, FLD_LABEL, this.label);

        return htm.toString();
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {

        return Objects.hashCode(this.ident)
                + Objects.hashCode(this.parentIdent)
                + Objects.hashCode(this.depth)
                + Objects.hashCode(this.sortOrder)
                + Objects.hashCode(this.label);
    }

    /**
     * Tests whether this object is equal to another.
     *
     * @param obj the other object
     * @return true if equal; false if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final RawTreePath rec) {
            equal = Objects.equals(this.ident, rec.ident)
                    && Objects.equals(this.parentIdent, rec.parentIdent)
                    && Objects.equals(this.depth, rec.depth)
                    && Objects.equals(this.sortOrder, rec.sortOrder)
                    && Objects.equals(this.label, rec.label);
        } else {
            equal = false;
        }

        return equal;
    }
}

package dev.mathops.db.old.rawlogic;

import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.ESchema;
import dev.mathops.db.old.rawrecord.RawTreePath;
import dev.mathops.text.builder.SimpleBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A utility class to work with tree path records.
 *
 * <pre>
 * Table:  'tree_path'
 *
 * Column name          Type                      Nulls   Key
 * -------------------  ------------------------  ------  -----
 * ident                char(12)                  no      PK
 * parent_ident         char(12)                  yes     PK
 * depth                smallint                  no      PK
 * sort_order           smallint                  no
 * label                char(32)                  yes
 * </pre>
 */
public enum RawTreePathLogic {
    ;

    /**
     * Inserts a new record.
     *
     * @param cache  the data cache
     * @param record the record to insert
     * @return {@code true} if successful; {@code false} otherwise
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean insert(final Cache cache, final RawTreePath record)
            throws SQLException {

        if (record.ident == null || record.depth == null || record.sortOrder == null) {
            throw new SQLException("Null value in primary key or required field.");
        }

        final String sql = SimpleBuilder.concat(
                "INSERT INTO tree_path (ident,parent_ident,depth,sort_order,label) VALUES (",
                LogicUtils.sqlStringValue(record.ident), ",",
                LogicUtils.sqlStringValue(record.parentIdent), ",",
                LogicUtils.sqlIntegerValue(record.depth), ",",
                LogicUtils.sqlIntegerValue(record.sortOrder), ",",
                LogicUtils.sqlStringValue(record.label), ")");

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Deletes a record.
     *
     * @param cache  the data cache
     * @param record the record to delete
     * @return {@code true} if successful; {@code false} if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean delete(final Cache cache, final RawTreePath record) throws SQLException {

        final String sql = SimpleBuilder.concat("DELETE FROM tree_path WHERE ident=",
                LogicUtils.sqlStringValue(record.ident),
                "  AND parent_ident=", LogicUtils.sqlStringValue(record.parentIdent),
                "  AND depth=", LogicUtils.sqlIntegerValue(record.depth));

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Gets all records.
     *
     * @param cache the data cache
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawTreePath> queryAll(final Cache cache) throws SQLException {

        return executeListQuery(cache, "SELECT * FROM tree_path");
    }

    /**
     * Gets all records with a specified depth and parent identifier. Results are sorted by sortOrder.
     *
     * @param cache          the data cache
     * @param theDepth       the depth
     * @param theParentIdent the parent identifier
     * @return the list of matching records
     * @throws SQLException if there is an error accessing the database
     */
    public static List<RawTreePath> queryByDepthAndParent(final Cache cache, final int theDepth,
                                                          final String theParentIdent) throws SQLException {

        final String sql = SimpleBuilder.concat(
                "SELECT * FROM tree_path WHERE depth=", Integer.toString(theDepth),
                "   AND parent_ident=", LogicUtils.sqlStringValue(theParentIdent),
                " ORDER BY sort_order");

        return executeListQuery(cache, sql);
    }

    /**
     * Gets a single record.
     *
     * @param cache          the data cache
     * @param theIdent       the identifier of the record to query
     * @param theDepth       the depth
     * @param theParentIdent the parent identifier of the record to query
     * @return the tree path record; null if none found
     * @throws SQLException if there is an error accessing the database
     */
    public static RawTreePath query(final Cache cache, final String theIdent, final int theDepth,
                                    final String theParentIdent) throws SQLException {

        final String sql;

        if (theDepth == 0 || theParentIdent == null) {
            sql = SimpleBuilder.concat(
                    "SELECT * FROM tree_path WHERE ident=", LogicUtils.sqlStringValue(theIdent),
                    "   AND depth=", Integer.toString(theDepth),
                    "   AND parent_ident IS NULL",
                    " ORDER BY sort_order");
        } else {
            sql = SimpleBuilder.concat(
                    "SELECT * FROM tree_path WHERE ident=", LogicUtils.sqlStringValue(theIdent),
                    "   AND depth=", Integer.toString(theDepth),
                    "   AND parent_ident=", LogicUtils.sqlStringValue(theParentIdent),
                    " ORDER BY sort_order");
        }

        return executeSingleQuery(cache, sql);
    }

    /**
     * Organizes a list of records into a tree.
     *
     * @param records the list of records - any records that do not have a connected path back to the root (".") path
     *                will not be included in the resulting tree
     * @return a list of top-level {@code RawTree>ath.TreeNode} objects
     */
    public static List<RawTreePath.TreeNode> organizeIntoTree(final List<RawTreePath> records) {

        final List<RawTreePath.TreeNode> result = new ArrayList<>(10);

        buildNodesForParent(null, new ArrayList<>(records), result);

        return result;
    }

    /**
     * Recursively builds a list of nodes with a specified parent.
     *
     * @param parentNode the parent node
     * @param records    the list of records from which to draw to create tree nodes
     * @param result     a list to which to add tree nodes
     */
    private static void buildNodesForParent(final RawTreePath.TreeNode parentNode, final List<RawTreePath> records,
                                            final List<RawTreePath.TreeNode> result) {

        final int parentDepth = parentNode == null ? -1 : parentNode.treePath.depth.intValue();
        final int childDepth = parentDepth + 1;

        final Iterator<RawTreePath> iter = records.iterator();
        while (iter.hasNext()) {
            final RawTreePath row = iter.next();
            if (row.depth.intValue() != childDepth) {
                continue;
            }

            final boolean match = (parentNode == null && row.parentIdent == null)
                                  || (parentNode != null && parentNode.treePath.ident.equals(row.parentIdent));

            if (match) {
                iter.remove();
                final RawTreePath.TreeNode node = new RawTreePath.TreeNode(parentNode, row);
                result.add(node);
            }
        }

        // Do the following after iteration, so we don't get concurrent modification exceptions
        for (final RawTreePath.TreeNode node : result) {
            buildNodesForParent(node, records, node.nodes);
        }

        Collections.sort(result);
    }

    /**
     * Updates the sort order on a tree path.
     *
     * @param cache        the data cache
     * @param toUpdate     the record to update
     * @param theSortOrder the new sort order
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateSortOrder(final Cache cache, final RawTreePath toUpdate,
                                          final Integer theSortOrder) throws SQLException {

        final String sql;

        if (toUpdate.parentIdent == null) {
            sql = SimpleBuilder.concat("UPDATE tree_path SET sort_order=", LogicUtils.sqlIntegerValue(theSortOrder),
                    " WHERE ident=", LogicUtils.sqlStringValue(toUpdate.ident),
                    "   AND depth=", LogicUtils.sqlIntegerValue(toUpdate.depth),
                    "   AND parent_ident IS NULL");
        } else {
            sql = SimpleBuilder.concat("UPDATE tree_path SET sort_order=", LogicUtils.sqlIntegerValue(theSortOrder),
                    " WHERE ident=", LogicUtils.sqlStringValue(toUpdate.ident),
                    "   AND depth=", LogicUtils.sqlIntegerValue(toUpdate.depth),
                    "   AND parent_ident=", LogicUtils.sqlStringValue(toUpdate.parentIdent));
        }

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Updates the label on a tree path.
     *
     * @param cache    the data cache
     * @param toUpdate the record to update
     * @param theLabel the new label
     * @return true if successful; false if not
     * @throws SQLException if there is an error accessing the database
     */
    public static boolean updateLabel(final Cache cache, final RawTreePath toUpdate,
                                      final String theLabel) throws SQLException {

        final String sql;

        if (toUpdate.parentIdent == null) {
            sql = SimpleBuilder.concat("UPDATE tree_path SET label=", LogicUtils.sqlStringValue(theLabel),
                    " WHERE ident=", LogicUtils.sqlStringValue(toUpdate.ident),
                    "   AND depth=", LogicUtils.sqlIntegerValue(toUpdate.depth),
                    "   AND parent_ident IS NULL");
        } else {
            sql = SimpleBuilder.concat("UPDATE tree_path SET label=", LogicUtils.sqlStringValue(theLabel),
                    " WHERE ident=", LogicUtils.sqlStringValue(toUpdate.ident),
                    "   AND depth=", LogicUtils.sqlIntegerValue(toUpdate.depth),
                    "   AND parent_ident=", LogicUtils.sqlStringValue(toUpdate.parentIdent));
        }

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement()) {
            final boolean result = stmt.executeUpdate(sql) == 1;

            if (result) {
                conn.commit();
            } else {
                conn.rollback();
            }

            return result;
        } finally {
            Cache.checkInConnection(conn);
        }
    }

    /**
     * Executes a query that returns a list of records.
     *
     * @param cache the data cache
     * @param sql   the query
     * @return the list of records
     * @throws SQLException if there is an error accessing the database
     */
    private static List<RawTreePath> executeListQuery(final Cache cache, final String sql) throws SQLException {

        final List<RawTreePath> result = new ArrayList<>(50);

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                result.add(RawTreePath.fromResultSet(rs));
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }

    /**
     * Executes a query that returns a single records.
     *
     * @param cache the data cache
     * @param sql   the query
     * @return the record found; null if none returned
     * @throws SQLException if there is an error accessing the database
     */
    private static RawTreePath executeSingleQuery(final Cache cache, final String sql) throws SQLException {

        RawTreePath result = null;

        final DbConnection conn = cache.checkOutConnection(ESchema.LEGACY);

        try (final Statement stmt = conn.createStatement();
             final ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                result = RawTreePath.fromResultSet(rs);
            }
        } finally {
            Cache.checkInConnection(conn);
        }

        return result;
    }
}

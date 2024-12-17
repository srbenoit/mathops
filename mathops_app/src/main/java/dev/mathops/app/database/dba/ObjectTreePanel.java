package dev.mathops.app.database.dba;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * A panel that displays database objects (schemas, tables, views, roles, tablespaces).  Selecting one of these objects
 * notifies the owning {@code MainWindow}, which can update window contents accordingly.
 */
final class ObjectTreePanel extends JPanel implements ActionListener, TreeSelectionListener {

    /** The owning {@code MainWindow}. */
    private final MainWindow owner;

    /** The tree model. */
    private final DefaultTreeModel treeModel;

    /** The node that holds schemas. */
    private final DefaultMutableTreeNode schemas;

    /** The tree. */
    private final JTree tree;

    /** The refresh button. */
    private final JButton refresh;

    /**
     * Constructs a new {@code ObjectTreePanel}.  This call does not populate the object tree.
     *
     * @param theOwner the owning {@code MainWindow}
     */
    ObjectTreePanel(final MainWindow theOwner) {

        super(new StackedBorderLayout());

        setBorder(BorderFactory.createEtchedBorder());
        setPreferredSize(new Dimension(240, 768));

        this.owner = theOwner;

        final DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        this.schemas = new DefaultMutableTreeNode("Schemas");

        root.add(this.schemas);

        this.treeModel = new DefaultTreeModel(root);

        this.tree = new JTree(this.treeModel);
        this.tree.setRootVisible(false);
        this.tree.setShowsRootHandles(true);
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        final JScrollPane treeScroll = new JScrollPane(this.tree);
        add(treeScroll, StackedBorderLayout.CENTER);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        this.refresh = new JButton("Refresh");
        buttons.add(this.refresh);
        add(buttons, StackedBorderLayout.SOUTH);
    }

    /**
     * Initializes the window.  This is separated from the constructor because it leaks references to this object which
     * is not completely constructed during the constructor.
     */
    void init() {

        this.refresh.addActionListener(this);
        this.tree.addTreeSelectionListener(this);
    }

    /**
     * Queries the database and rebuilds the object tree.
     *
     * @param conn the database connection
     */
    void refresh(final Connection conn) {

        // Get the list of existing schema nodes - we will add new nodes we find, and delete nodes that are no longer
        // there, but we will leave nodes in place that are still valid to avoid "jank" from rebuilding the tree.

        final int numExistingSchemas = this.schemas.getChildCount();
        final Map<String, DefaultMutableTreeNode> existingSchemaNodes = new HashMap<>(numExistingSchemas);
        for (int i = 0; i < numExistingSchemas; ++i) {
            final TreeNode node = this.schemas.getChildAt(i);
            if (node instanceof final DefaultMutableTreeNode mutableNode) {
                final Object userObject = mutableNode.getUserObject();
                if (userObject instanceof final String schemaName) {
                    existingSchemaNodes.put(schemaName, mutableNode);
                }
            }
        }

        try (final Statement statement = conn.createStatement()) {
            final List<String> schemaNames = querySchemaList(statement);
            final Map<String, List<String>> tableMap = queryTables(statement, schemaNames);
            final Map<String, List<String>> viewMap = queryViews(statement, schemaNames);

            reconcileSchemaNodes(existingSchemaNodes, schemaNames, tableMap, viewMap);

            this.tree.expandRow(0);
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String[] msg = {"Unable to retrieve database objects", ex.getMessage()};
            JOptionPane.showMessageDialog(null, msg, "Math Database Administrator", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Queries for the current list of all schemas in the database.  Schemas whose name begins with "pg_" and the
     * PostgreSQL information schema are ignored.
     *
     * @param statement a JDBC {@code Statement} to use to perform the query
     * @return the list of schemas
     * @throws SQLException if there is an error querying the list of schemas
     */
    private static List<String> querySchemaList(final Statement statement) throws SQLException {

        final List<String> schemaNames = new ArrayList<>(20);
        final ResultSet rs1 = statement.executeQuery("SELECT * FROM information_schema.schemata");
        while (rs1.next()) {
            final String name = rs1.getString("schema_name");
            if (name.startsWith("pg_") || "information_schema".equals(name)) {
                continue;
            }
            schemaNames.add(name);
        }

        schemaNames.sort(null);

        return schemaNames;
    }

    /**
     * Queries for the current list of all tables in the database.  Schemas whose name begins with "pg_" and the
     * PostgreSQL information schema are ignored.
     *
     * @param statement   a JDBC {@code Statement} to use to perform the query
     * @param schemaNames the names of the schemas of interest
     * @return a map from schema name to the list of tables found within that schema
     * @throws SQLException if there is an error querying the list of tables
     */
    private Map<String, List<String>> queryTables(final Statement statement, final List<String> schemaNames)
            throws SQLException {

        final int numSchemas = schemaNames.size();
        final Map<String, List<String>> tableMap = new HashMap<>(numSchemas);
        for (final String schema : schemaNames) {
            final List<String> list = new ArrayList<>(50);
            tableMap.put(schema, list);
        }

        // Query all the tables and create a map from schema name to the list of tables in that schema
        // We ignore tables not in one of the schemas of interest above
        final ResultSet rs2 = statement.executeQuery("SELECT * FROM information_schema.tables ORDER BY table_name");
        while (rs2.next()) {
            final String schema = rs2.getString("table_schema");
            if (schemaNames.contains(schema)) {
                final String table = rs2.getString("table_name");
                final List<String> list = tableMap.get(schema);
                list.add(table);
            }
        }

        return tableMap;
    }

    /**
     * Queries for the current list of all views in the database.
     *
     * @param statement   a JDBC {@code Statement} to use to perform the query
     * @param schemaNames the names of the schemas of interest
     * @return a map from schema name to the list of views found within that schema
     * @throws SQLException if there is an error querying the list of views
     */
    private Map<String, List<String>> queryViews(final Statement statement, final List<String> schemaNames)
            throws SQLException {

        final int numSchemas = schemaNames.size();
        final Map<String, List<String>> viewMap = new HashMap<>(numSchemas);
        for (final String schema : schemaNames) {
            final List<String> list = new ArrayList<>(5);
            viewMap.put(schema, list);
        }

        // Query all the tables and create a map from schema name to the list of tables in that schema
        // We ignore tables not in one of the schemas of interest above
        final ResultSet rs2 = statement.executeQuery("SELECT * FROM information_schema.views ORDER BY table_name");
        while (rs2.next()) {
            final String schema = rs2.getString("table_schema");
            if (schemaNames.contains(schema)) {
                final String table = rs2.getString("table_name");
                final List<String> list = viewMap.get(schema);
                list.add(table);
            }
        }

        return viewMap;
    }

    /**
     * Reconciles the list of schemas queried from the database with the set of schema nodes in the tree view, updating
     * the tree view as needed.
     *
     * @param existingSchemaNodes a map from schema name to the tree node in the tree view
     * @param schemaNames         the sorted list of schema names queried from the server
     * @param tableMap            a map from schema name to the sorted list of names of tables queried from the server
     *                            within that schema
     * @param viewMap             a map from schema name to the sorted list of names of views queried from the server
     *                            within that schema
     */
    private void reconcileSchemaNodes(final Map<String, ? extends DefaultMutableTreeNode> existingSchemaNodes,
                                      final Iterable<String> schemaNames,
                                      final Map<String, ? extends List<String>> tableMap,
                                      final Map<String, ? extends List<String>> viewMap) {

        for (final String schemaName : schemaNames) {
            DefaultMutableTreeNode schemaNode = existingSchemaNodes.remove(schemaName);
            DefaultMutableTreeNode tablesNode = null;
            DefaultMutableTreeNode viewsNode = null;

            if (schemaNode == null) {
                schemaNode = new DefaultMutableTreeNode(schemaName);

                final int count = this.schemas.getChildCount();
                int index = 0;
                while (index < count) {
                    final TreeNode node = this.schemas.getChildAt(index);
                    if (node instanceof final DefaultMutableTreeNode mutableNode) {
                        final Object userObject = mutableNode.getUserObject();
                        if (userObject instanceof final String nodeName) {
                            if (nodeName.compareTo(schemaName) > 0) {
                                break;
                            }
                        }
                    }
                    ++index;
                }

                tablesNode = new DefaultMutableTreeNode("Tables");
                viewsNode = new DefaultMutableTreeNode("Views");
                schemaNode.add(tablesNode);
                schemaNode.add(viewsNode);

                this.schemas.insert(schemaNode, index);
            } else {
                final int numSchemaNodeChildren = schemaNode.getChildCount();
                for (int i = 0; i < numSchemaNodeChildren; ++i) {
                    final TreeNode child = schemaNode.getChildAt(i);
                    if (child instanceof final DefaultMutableTreeNode childNode) {
                        final Object userObject = childNode.getUserObject();
                        if ("Tables".equals(userObject)) {
                            tablesNode = childNode;
                        } else if ("Views".equals(userObject)) {
                            viewsNode = childNode;
                        }
                    }
                }
            }

            if (tablesNode != null) {
                final List<String> tableNames = tableMap.get(schemaName);
                populateTables(tableNames, tablesNode);
            }

            if (viewsNode != null) {
                final List<String> viewNames = viewMap.get(schemaName);
                populateViews(viewNames, viewsNode);
            }
        }

        // Remove schema nodes that are no longer present
        for (final DefaultMutableTreeNode toRemove : existingSchemaNodes.values()) {
            this.schemas.remove(toRemove);
        }
        existingSchemaNodes.clear();
    }

    /**
     * Creates table nodes for all tables in a schema.
     *
     * @param tableNames the list of tables currently in the schema
     * @param tablesNode the tables node with the old list of tables (to be updated)
     */
    private void populateTables(final List<String> tableNames, final DefaultMutableTreeNode tablesNode) {

        final int numExistingTables = tablesNode.getChildCount();
        final Map<String, DefaultMutableTreeNode> existingTableNodes = new HashMap<>(numExistingTables);
        for (int i = 0; i < numExistingTables; ++i) {
            final TreeNode node = tablesNode.getChildAt(i);
            if (node instanceof final DefaultMutableTreeNode mutableNode) {
                final Object userObject = mutableNode.getUserObject();
                if (userObject instanceof final String tableName) {
                    existingTableNodes.put(tableName, mutableNode);
                }
            }
        }

        for (final String tableName : tableNames) {
            DefaultMutableTreeNode tableNode = existingTableNodes.remove(tableName);

            if (tableNode == null) {
                tableNode = new DefaultMutableTreeNode(tableName);

                final int count = tablesNode.getChildCount();
                int index = 0;
                while (index < count) {
                    final TreeNode node = tablesNode.getChildAt(index);
                    if (node instanceof final DefaultMutableTreeNode mutableNode) {
                        final Object userObject = mutableNode.getUserObject();
                        if (userObject instanceof final String nodeName) {
                            if (nodeName.compareTo(tableName) > 0) {
                                break;
                            }
                        }
                    }
                    ++index;
                }

                tablesNode.insert(tableNode, index);
            }
        }
    }

    /**
     * Creates view nodes for all views in a schema.
     *
     * @param viewNames the list of views currently in the schema
     * @param viewNode  the views node with the old list of tables (to be updated)
     */
    private void populateViews(final List<String> viewNames, final DefaultMutableTreeNode viewNode) {

        final int numExistingViews = viewNode.getChildCount();
        final Map<String, DefaultMutableTreeNode> existingViewNodes = new HashMap<>(numExistingViews);
        for (int i = 0; i < numExistingViews; ++i) {
            final TreeNode node = viewNode.getChildAt(i);
            if (node instanceof final DefaultMutableTreeNode mutableNode) {
                final Object userObject = mutableNode.getUserObject();
                if (userObject instanceof final String viewName) {
                    existingViewNodes.put(viewName, mutableNode);
                }
            }
        }

        for (final String viewName : viewNames) {
            DefaultMutableTreeNode tableNode = existingViewNodes.remove(viewName);

            if (tableNode == null) {
                tableNode = new DefaultMutableTreeNode(viewName);

                final int count = viewNode.getChildCount();
                int index = 0;
                while (index < count) {
                    final TreeNode node = viewNode.getChildAt(index);
                    if (node instanceof final DefaultMutableTreeNode mutableNode) {
                        final Object userObject = mutableNode.getUserObject();
                        if (userObject instanceof final String nodeName) {
                            if (nodeName.compareTo(viewName) > 0) {
                                break;
                            }
                        }
                    }
                    ++index;
                }

                viewNode.insert(tableNode, index);
            }
        }
    }

    /**
     * Called when the "Refresh" button is pressed.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        this.owner.refresh();
    }

    /**
     * Called when the tree selection changes.
     *
     * @param e the event that characterizes the change
     */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {

        final TreePath selection = this.tree.getSelectionPath();

        Log.info("Selection is ", selection);
    }
}

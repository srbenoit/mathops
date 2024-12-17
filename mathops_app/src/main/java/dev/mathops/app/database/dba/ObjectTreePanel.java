package dev.mathops.app.database.dba;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import java.awt.Dimension;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * A panel that displays database objects (schemas, tables, views, roles, tablespaces).  Selecting one of these objects
 * notifies the owning {@code MainWindow}, which can update window contents accordingly.
 */
final class ObjectTreePanel extends JPanel {

    /** The owning {@code MainWindow}. */
    private final MainWindow owner;

    /** The tree model. */
    private final DefaultTreeModel treeModel;

    /** The node that holds schemas. */
    private final DefaultMutableTreeNode schemas;

    /** The node that holds roles. */
    private final DefaultMutableTreeNode roles;

    /** The node that holds tablespaces. */
    private final DefaultMutableTreeNode tablespaces;

    /** The tree. */
    private final JTree tree;

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
        this.roles = new DefaultMutableTreeNode("Roles");
        this.tablespaces = new DefaultMutableTreeNode("Tablespaces");

        root.add(this.schemas);
        root.add(this.roles);
        root.add(this.tablespaces);

        this.treeModel = new DefaultTreeModel(root);

        this.tree = new JTree(this.treeModel);
        this.tree.setRootVisible(false);
        this.tree.setShowsRootHandles(true);
        add(this.tree, StackedBorderLayout.CENTER);
    }

    /**
     * Queries the database and rebuilds the object tree.
     *
     * @param conn the database connection
     */
    void refresh(final Connection conn) {

        // Save any selection so we can restore it after the refresh...
        final TreePath[] paths = this.tree.getSelectionPaths();

        this.schemas.removeAllChildren();
        this.roles.removeAllChildren();
        this.tablespaces.removeAllChildren();

        try (final Statement statement = conn.createStatement()) {

            // Query all the schemas
            final List<String> schemaNames = new ArrayList<>(20);
            final ResultSet rs1 = statement.executeQuery("SELECT * FROM pg_namespace");
            while (rs1.next()) {
                final String name = rs1.getString("nspname");
                if (name.startsWith("pg_")) {
                    continue;
                }
                schemaNames.add(name);
            }

            schemaNames.sort(null);

            for (final String name : schemaNames) {
                final DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
                this.schemas.add(node);
            }

            // Query all the roles

            // Query all the tablespaces
            final List<String> tablespaceNames = new ArrayList<>(20);
            final ResultSet rs3 = statement.executeQuery("SELECT * FROM pg_tablespace");
            while (rs3.next()) {
                final String name = rs3.getString("spcname");
                if (name.startsWith("pg_")) {
                    continue;
                }
                tablespaceNames.add(name);
            }

            tablespaceNames.sort(null);

            for (final String name : tablespaceNames) {
                final DefaultMutableTreeNode node = new DefaultMutableTreeNode(name);
                this.tablespaces.add(node);
            }

            // TODO: Query tables, views, and indexes
        } catch (final SQLException ex) {
            Log.warning(ex);
            final String[] msg = {"Unable to retrieve database objects", ex.getMessage()};
            JOptionPane.showMessageDialog(null, msg, "Math Database Administrator", JOptionPane.ERROR_MESSAGE);
        }
    }
}

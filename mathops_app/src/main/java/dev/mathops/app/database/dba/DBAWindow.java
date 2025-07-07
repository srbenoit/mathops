package dev.mathops.app.database.dba;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * The main window.
 */
final class DBAWindow extends JFrame implements ActionListener, TreeSelectionListener {

    /** The database ribbon. */
    private final DatabaseRibbon ribbon;

    /** The tree showing schemas and their tables. */
    private final SchemaTableTree schemaTableTree;

    /** The panel that shows the currently-selected table. */
    private final TablePane tablePane;

    /** The list of currently-selected databases. */
    private final List<Database> selectedDatabases;

    /**
     * Constructs a new {@code DBAWindow}.
     *
     * @param config the database configuration
     */
    DBAWindow(final DatabaseConfig config) {

        super("Math Database Administrator");

        this.selectedDatabases = new ArrayList<>(10);

        final JPanel content = new JPanel(new StackedBorderLayout());
        setContentPane(content);

        this.ribbon = new DatabaseRibbon(config, this);
        content.add(this.ribbon, StackedBorderLayout.NORTH);

        this.tablePane = new TablePane();
        content.add(this.tablePane, StackedBorderLayout.CENTER);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int minWidth = Math.max(1024, screen.width / 2);
        final int w = Math.min(screen.width, minWidth);
        final int minHeight = Math.max(768, screen.height * 2 / 3);
        final int h = Math.min(screen.height, minHeight);
        content.setPreferredSize(new Dimension(w, h));

        this.schemaTableTree = new SchemaTableTree(this);
        content.add(this.schemaTableTree, StackedBorderLayout.WEST);
    }

    /**
     * Called when an action is invoked (a checkbox in the database ribbon is toggled).
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        this.ribbon.getSelectedDatabases(this.selectedDatabases);

        final SchemaTable sel = this.schemaTableTree.getSelection();
        this.tablePane.select(sel, this.selectedDatabases);
    }

    /**
     * Called when the tree selection changes in the schema/table tree view.
     *
     * @param e the event to be processed
     */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {

        final SchemaTable sel = this.schemaTableTree.getSelection();
        this.tablePane.select(sel, this.selectedDatabases);
    }
}

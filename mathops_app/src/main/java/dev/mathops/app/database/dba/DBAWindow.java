package dev.mathops.app.database.dba;

import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.cfg.Database;
import dev.mathops.db.cfg.DatabaseConfig;
import dev.mathops.db.cfg.Login;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main window.
 */
final class DBAWindow extends JFrame implements ActionListener, TreeSelectionListener {

    /** A map from database to its connection. */
    private final Map<Database, Login> logins;

    /** The database ribbon. */
    private final DatabaseRibbon ribbon;

    /** The tree showing schemas and their tables. */
    private final SchemaTableTree schemaTableTree;

    /** The panel that shows the currently-selected table. */
    private final TablePane tablePane;

    /** The list of currently-selected database uses. */
    private final List<DatabaseUse> selectedDatabaseUses;

    /**
     * Constructs a new {@code DBAWindow}.
     *
     * @param config the database configuration
     */
    DBAWindow(final DatabaseConfig config) {

        super("Math Database Administrator");

        this.logins = new HashMap<>(10);
        this.selectedDatabaseUses = new ArrayList<>(10);

        final JPanel content = new JPanel(new StackedBorderLayout());
        setContentPane(content);

        this.ribbon = new DatabaseRibbon(config, this, this.logins);
        content.add(this.ribbon, StackedBorderLayout.NORTH);

        this.tablePane = new TablePane(config);
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

        this.ribbon.getSelectedDatabaseUses(this.selectedDatabaseUses);

        final SchemaTable sel = this.schemaTableTree.getSelection();
        this.tablePane.select(sel, this.selectedDatabaseUses, this.logins);
    }

    /**
     * Called when the tree selection changes in the schema/table tree view.
     *
     * @param e the event to be processed
     */
    @Override
    public void valueChanged(final TreeSelectionEvent e) {

        final SchemaTable sel = this.schemaTableTree.getSelection();
        this.tablePane.select(sel, this.selectedDatabaseUses, this.logins);
    }
}

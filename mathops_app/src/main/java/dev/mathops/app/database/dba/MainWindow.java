package dev.mathops.app.database.dba;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.cfg.LoginConfig;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;

/** The main window. */
final class MainWindow extends JFrame {

    /** The login configuration that can create connections to the database. */
    private final LoginConfig login;

    /** The content pane. */
    private final JPanel content;

    /** The object tree panel. */
    private ObjectTreePanel objectTree = null;

    /** The panel currently being displayed. */
    private JPanel currentPanel;

    /** The schema panel. */
    private SchemaPanel schemaPanel;

    /** The table panel. */
    private TablePanel tablePanel;

    /** The view panel. */
    private ViewPanel viewPanel;

    /**
     * Constructs a new {@code MainWindow}.
     *
     * @param theLogin the login configuration that can create connections to the database
     */
    MainWindow(final LoginConfig theLogin) {

        super("Math Database Administrator");

        this.login = theLogin;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.content = new JPanel(new StackedBorderLayout());
        final Border padding = BorderFactory.createEmptyBorder(1, 2, 1, 2);
        this.content.setBorder(padding);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int minWidth = Math.max(1024, screen.width / 3);
        final int w = Math.min(screen.width, minWidth);
        final int minHeight = Math.max(768, screen.height / 2);
        final int h = Math.min(screen.height, minHeight);
        setPreferredSize(new Dimension(w, h));

        setContentPane(this.content);

        this.schemaPanel = new SchemaPanel();
        this.tablePanel = new TablePanel();
        this.viewPanel = new ViewPanel();
    }

    /**
     * Initializes the window.  This is separated from the constructor because it leaks references to this object which
     * is not completely constructed during the constructor.
     */
    void init() {

        this.objectTree = new ObjectTreePanel(this);
        this.objectTree.init();
        this.content.add(this.objectTree, StackedBorderLayout.WEST);

        refresh();
    }

    /**
     * Refreshes the display, querying the database for the current set of schemas, tables, views, etc.
     */
    void refresh() {

        if (this.objectTree != null) {
            try (final Connection conn = this.login.openConnection()) {
                this.objectTree.refresh(conn);
            } catch (final SQLException ex) {
                Log.warning(ex);
                final String[] msg = {"Unable to connect to PostgreSQL database:", ex.getMessage()};
                JOptionPane.showMessageDialog(null, msg, "Import Database", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Clears the display when no display-worthy item is selected in the tree view.
     */
    void clearDisplay() {

        if (this.currentPanel != null) {
            this.content.remove(this.currentPanel);
            this.currentPanel = null;
        }
    }

    /**
     * Called when a schema is selected in the tree view.
     *
     * @param schemaName the schema name
     */
    void schemaSelected(final String schemaName) {

        Log.info("Selected schema ", schemaName);

        clearDisplay();

        this.schemaPanel.setSchema(schemaName);
        this.content.add(this.schemaPanel, StackedBorderLayout.CENTER);
        this.currentPanel = this.schemaPanel;
        this.content.invalidate();
        this.content.revalidate();
        this.content.repaint();
    }

    /**
     * Called when a table is selected in the tree view.
     *
     * @param schemaName the schema name
     * @param tableName  the table name
     */
    void tableSelected(final String schemaName, final String tableName) {

        Log.info("Selected table: ", tableName, " in schema ", schemaName);

        clearDisplay();

        this.tablePanel.setTable(schemaName, tableName);
        this.content.add(this.tablePanel, StackedBorderLayout.CENTER);
        this.currentPanel = this.tablePanel;
        this.content.invalidate();
        this.content.revalidate();
        this.content.repaint();
    }

    /**
     * Called when a view is selected in the tree view.
     *
     * @param schemaName the schema name
     * @param viewName   the view name
     */
    void viewSelected(final String schemaName, final String viewName) {

        Log.info("Selected view: ", viewName, " in schema ", schemaName);

        clearDisplay();

        this.viewPanel.setView(schemaName, viewName);
        this.content.add(this.viewPanel, StackedBorderLayout.CENTER);
        this.currentPanel = this.viewPanel;
        this.content.invalidate();
        this.content.revalidate();
        this.content.repaint();
    }
}

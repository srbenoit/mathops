package dev.mathops.app.database.dba;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.Cache;
import dev.mathops.db.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The main window.
 */
final class MainWindow extends JFrame {

    /** The caches for each login configuration provided. */
    private final List<Cache> caches;

    /** The content pane. */
    private final JPanel content;

    /** The object tree panel. */
    private ObjectTreePanel objectTree = null;

    /** The panel currently being displayed. */
    private JPanel currentPanel;

    /** The schema panel. */
    private final SchemaPanel schemaPanel;

    /** The table panel. */
    private final TablePanel tablePanel;

    /** The view panel. */
    private final ViewPanel viewPanel;

    /**
     * Constructs a new {@code MainWindow}.
     *
     * @param theProfiles the database profiles (each with a distinct login)
     */
    MainWindow(final Collection<DbProfile> theProfiles) {

        super("Math Database Administrator");

        final int numLogins = theProfiles.size();
        this.caches = new ArrayList<>(numLogins);

        for (final DbProfile profile : theProfiles) {
            try {
                final DbContext primary = profile.getDbContext(ESchemaUse.PRIMARY);
                final DbConnection conn = primary.checkOutConnection();
                final Cache cache = new Cache(profile, conn);
                this.caches.add(cache);
            } catch (final SQLException ex) {
                Log.warning("Failed to connect to ", ex);
            }
        }

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

        this.schemaPanel = new SchemaPanel(this.caches);
        this.tablePanel = new TablePanel(this.caches);
        this.viewPanel = new ViewPanel(this.caches);
    }

    /**
     * Initializes the window.  This is separated from the constructor because it leaks references to this object which
     * is not completely constructed during the constructor.
     */
    void init() {

        this.objectTree = new ObjectTreePanel(this);
        this.objectTree.init();
        this.content.add(this.objectTree, StackedBorderLayout.WEST);
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

//        Log.info("Selected schema ", schemaName);

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

//        Log.info("Selected table: ", tableName, " in schema ", schemaName);

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

//        Log.info("Selected view: ", viewName, " in schema ", schemaName);

        clearDisplay();

        this.viewPanel.setView(schemaName, viewName);
        this.content.add(this.viewPanel, StackedBorderLayout.CENTER);
        this.currentPanel = this.viewPanel;
        this.content.invalidate();
        this.content.revalidate();
        this.content.repaint();
    }
}

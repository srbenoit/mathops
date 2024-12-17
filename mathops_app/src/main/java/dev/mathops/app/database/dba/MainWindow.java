package dev.mathops.app.database.dba;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.cfg.LoginConfig;

import java.awt.Dimension;
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
        this.content.setPreferredSize(new Dimension(1024, 768));
        setContentPane(this.content);
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
}

package dev.mathops.app.database.dba;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import java.awt.Dimension;
import java.sql.Connection;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;

/** The main window. */
final class MainWindow extends JFrame {

    /** The JDBC connection. */
    private final Connection conn;

    /** The content pane. */
    private final JPanel content;

    /**
     * Constructs a new {@code MainWindow}.
     */
    MainWindow(final Connection theConn) {

        super("Math Database Administrator");

        this.conn = theConn;

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

        final ObjectTreePanel objectTree = new ObjectTreePanel(this);
        objectTree.refresh(this.conn);
        this.content.add(objectTree, StackedBorderLayout.WEST);
    }
}

package dev.mathops.app.adm.student;

import dev.mathops.commons.ui.UIUtilities;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.Serial;

/**
 * A window where the user can choose a server host, and a database cluster (selections are persisted via the
 * preferences API), and then enters a username and password to connect to the server. Once a connection is created, the
 * databases in that cluster become available, and the main application window opens.
 */
class LogWindow extends JFrame {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -7001065845560469690L;

    /**
     * Constructs a new {@code LogWindow}
     *
     * @param logText the text to display
     */
    LogWindow(final String logText) {

        super("Log");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new BorderLayout());
        content.setPreferredSize(new Dimension(800, 600));
        setContentPane(content);

        final JTextArea logArea = new JTextArea(logText);
        final JScrollPane scroll = new JScrollPane(logArea);
        content.add(scroll, BorderLayout.CENTER);

        UIUtilities.packAndCenter(this);
    }
}

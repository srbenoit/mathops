package dev.mathops.app.adm.office;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.Serial;

/**
 * A pop-up window that displays the log for debugging.
 */
final class LogWindow extends JFrame {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -7001065845560469690L;

    /** Preferred window width. */
    private static final int PREF_WIDTH = 800;

    /** Preferred window height. */
    private static final int PREF_HEIGHT = 600;

    /**
     * Constructs a new {@code LogWindow}
     *
     * @param logText the text to display
     */
    LogWindow(final String logText) {

        super(Res.get(Res.LOG_TITLE));

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new BorderLayout());
        content.setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));
        setContentPane(content);

        final JTextArea logArea = new JTextArea(logText);
        final JScrollPane scroll = new JScrollPane(logArea);
        content.add(scroll, BorderLayout.CENTER);
        pack();

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension size = getSize();

        final int xPixel = (int) ((double) (screen.width - size.width) * 0.5);
        final int yPixel = (int) ((double) (screen.height - size.height) * 0.5);

        setLocation(xPixel, yPixel);
        setVisible(true);
    }
}

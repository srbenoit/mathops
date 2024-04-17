package dev.mathops.db.oldadmin;

import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.db.old.Cache;

import javax.swing.JFrame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * The main window.
 */
public final class MainWindow extends JFrame implements KeyListener {

    /** The console. */
    private Console console = null;

    /** The cache. */
    private final Cache cache;

    /** The main screen. */
    private ScreenMain main;

    /** The currently active screen. */
    private IScreen activeScreen;

    /**
     * Constructs a new {@code MainWindow}.
     *
     * @param theCache the cache
     */
    MainWindow(final Cache theCache) {

        super("MATH ADMIN");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        this.cache = theCache;

        this.console = new Console(100, 40);
        this.console.addKeyListener(this);
        setContentPane(this.console);
    }

    /**
     * Displays the window.
     */
    public void display() {

        this.main = new ScreenMain(this.cache, this);
        this.activeScreen = this.main;

        this.activeScreen.draw(this.console);

        UIUtilities.packAndCenter(this);
        setVisible(true);
        this.console.requestFocus();
    }

    /**
     * Called when the application is exited.
     */
    public void quit() {

        setVisible(false);
        dispose();
    }

    /**
     * Called when a key is typed.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyTyped(final KeyEvent e) {

        final char character = e.getKeyChar();
        if (this.activeScreen.processKeyTyped(character)) {
            this.activeScreen.draw(this.console);
        }
    }

    /**
     * Called when a key is pressed.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyPressed(final KeyEvent e) {

        final int key = e.getKeyCode();
        if (this.activeScreen.processKeyPressed(key)) {
            this.activeScreen.draw(this.console);
        }
    }

    /**
     * Called when a key is released.
     *
     * @param e the event to be processed
     */
    @Override
    public void keyReleased(final KeyEvent e) {

        // No action
    }
}

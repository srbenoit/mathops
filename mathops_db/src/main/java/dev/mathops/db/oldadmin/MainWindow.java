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

    /** Data on the logged in user. */
    private final UserData userData;

    /** The main screen. */
    private ScreenMain main;

    /** The currently active screen. */
    private IScreen activeScreen;

    /**
     * Constructs a new {@code MainWindow}.
     *
     * @param theCache the cache
     * @param username the username of the logged-in user
     */
    MainWindow(final Cache theCache, final String username) {

        super("ADMIN");

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        this.cache = theCache;

        this.userData = new UserData(this.cache, username);

        this.console = new Console(100, 40);
        this.console.addKeyListener(this);
        setContentPane(this.console);
    }

    /**
     * Gets the console.
     *
     * @return the console
     */
    Console getConsole() {

        return this.console;
    }

    /**
     * Gets the user data for the logged-in user.
     *
     * @return the user data
     */
    UserData getUserData() {

        return this.userData;
    }

    /**
     * Displays the window.
     */
    public void display() {

        this.main = new ScreenMain(this.cache, this);
        this.activeScreen = this.main;

        this.activeScreen.draw();

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
            this.activeScreen.draw();
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
        final int mods = e.getModifiersEx();

        if (this.activeScreen.processKeyPressed(key, mods)) {
            this.activeScreen.draw();
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

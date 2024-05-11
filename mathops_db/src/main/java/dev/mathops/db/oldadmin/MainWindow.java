package dev.mathops.db.oldadmin;

import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawStudent;

import javax.swing.JFrame;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

/**
 * The main window.
 */
public final class MainWindow extends JFrame implements KeyListener, MouseListener {

    /** The console. */
    private Console console = null;

    /** The cache. */
    private final Cache cache;

    /** Data on the logged in user. */
    private final UserData userData;

    /** The main screen. */
    private ScreenMain main;

    /** The course screen. */
    private ScreenCourse course;

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
        this.console.addMouseListener(this);
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
        this.course = new ScreenCourse(this.cache, this);

        this.activeScreen = this.main;

        this.activeScreen.draw();

        UIUtilities.packAndCenter(this);
        setVisible(true);
        this.console.requestFocus();
    }

    /**
     * Jumps to the Course screen.
     *
     * @param student the student
     */
    public void goToCourse(final RawStudent student) {

        this.course.setStudent(student);
        this.activeScreen = this.course;

        this.activeScreen.draw();
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

    /**
     * Called when a mouse button is clicked in the window.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

        final int btn = e.getButton();

        Log.info("Mouse clicked: Btn = " + btn);

        if (btn == 3) {
            try {
                final CharSequence data = (CharSequence) Toolkit.getDefaultToolkit()
                        .getSystemClipboard().getData(DataFlavor.stringFlavor);
                if (data != null) {
                    final int len = data.length();
                    boolean draw = false;
                    for (int i = 0; i < len; ++i) {
                        final char character = data.charAt(i);
                        if (this.activeScreen.processKeyTyped(character)) {
                            draw = true;
                        }
                    }
                    if (draw) {
                        this.activeScreen.draw();
                    }
                }
            } catch (final UnsupportedFlavorException | IOException ex) {
                Log.warning("Paste with no STRING flavor data on clipboard.");
            }
        }
    }

    /**
     * Called when a mouse button is pressed in the window.
     *
     * @param e the event to be processed
     */
    @Override
    public void mousePressed(final MouseEvent e) {

    }

    /**
     * Called when a mouse button is released in the window.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

    }

    /**
     * Called when the mouse enters the window.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseEntered(final MouseEvent e) {

        final Cursor textCursor = Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR);
        this.setCursor(textCursor);

    }

    /**
     * Called when the mouse leaves the window.
     *
     * @param e the event to be processed
     */
    @Override
    public void mouseExited(final MouseEvent e) {

        final Cursor defCursor = Cursor.getDefaultCursor();
        this.setCursor(defCursor);
    }
}

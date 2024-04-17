package dev.mathops.db.oldadmin;

/**
 * The interface implemented by a screen.
 */
public interface IScreen {

    /**
     * Draws the screen to a console.
     *
     * @param console the console
     */
    void draw(Console console);

    /**
     * Processes a key press
     *
     * @param key the key code
     * @return true if the screen should be repainted after this event
     */
    boolean processKeyPressed(int key);

    /**
     * Processes a key typed.
     *
     * @param character the character
     * @return true if the screen should be repainted after this event
     */
    boolean processKeyTyped(char character);
}

package dev.mathops.db.oldadmin;

/**
 * The interface implemented by a screen.
 */
public interface IScreen {

    /**
     * Draws the screen to a console.
     */
    void draw();

    /**
     * Processes a key press
     *
     * @param key the key code
     * @param modifiers key modifiers
     * @return true if the screen should be repainted after this event
     */
    boolean processKeyPressed(int key, int modifiers);

    /**
     * Processes a key typed.
     *
     * @param character the character
     * @return true if the screen should be repainted after this event
     */
    boolean processKeyTyped(char character);
}

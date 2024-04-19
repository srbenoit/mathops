package dev.mathops.db.oldadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.Arrays;

/**
 * A field in which text can be entered.
 */
final class Field {

    /** The character to echo for password fields. */
    private static final char STAR = '*';

    /** The x position on the screen. */
    private final int x;

    /** The y position on the screen. */
    private final int y;

    /** The maximum length. */
    private final int maxLen;

    /** True if this is a password field that should echo '*' for all characters. */
    private final boolean password;

    /** The characters (spaces when blank). */
    private final char[] characters;

    /** The valid character set (null if any character is allowed). */
    private final String validChars;

    /**
     * Constructs a new {@code Field}.
     *
     * @param theX the x position on the screen
     * @param theY the y position on the screen
     * @param theMaxLen the maximum length
     * @param isPassword true if this is a password field
     */
    Field(final int theX, final int theY, final int theMaxLen, final boolean isPassword,
          final String theValidChars) {

        this.x = theX;
        this.y = theY;
        this.maxLen = theMaxLen;
        this.password = isPassword;
        this.characters = new char[theMaxLen];
        this.validChars = theValidChars;

        clear();
    }

    /**
     * Draws the contents of the field to the console.
     *
     * @param console the console
     */
    void draw(final Console console) {

        final String str;

        if (this.password) {
            final char[] masked = this.characters.clone();
            final int len = masked.length;
            for (int i = 0; i < len; ++i) {
                if ((int) masked[i] != (int) CoreConstants.SPC_CHAR) {
                    masked[i] = STAR;
                }
            }
            str = new String(masked);
        } else {
            str = new String(this.characters);
        }

        console.print(str, this.x, this.y);
    }

    /**
     * Activates the field.  This sets the cursor to the position following the last non-space character, then draws
     * the field.
     *
     * @param console the console
     */
    void activate(final Console console) {

        int last = this.maxLen;
        while (last > 0 && this.characters[last - 1] == CoreConstants.SPC_CHAR) {
            --last;
        }

        console.setCursor(this.x + last, this.y);
        draw(console);
    }

    /**
     * Processes a character typed while the field is active (focused).  This draws the console if the key changes
     * the field's state at all.
     *
     * @param console the console
     * @param ch the character typed
     */
    void processChar(final Console console, final char ch) {

        if (!Character.isISOControl(ch)) {
            final Point cursor = console.getCursorPoint();

            if (cursor.y == this.y && cursor.x >= this.x) {
                final int cursorPos = cursor.x - this.x;

                if (cursorPos < this.maxLen && (this.validChars == null || this.validChars.indexOf((int) ch) != -1)) {
                    this.characters[cursorPos] = ch;
                    console.setCursor(cursor.x + 1, cursor.y);
                    draw(console);
                }
            }
        }
    }

    /**
     * Processes a key press while the field is active (focused).  This draws the console if the key changes
     * the field's state at all.  This will process the LEFT and RIGHT arrow keys, the BACKSPACE key, and the DELETE
     * key.  Keys like ENTER or TAB should be handled by the screen.
     *
     * @param console the console
     * @param key the key code of the key typed
     */
    void processKey(final Console console, final int key) {

        final Point cursor = console.getCursorPoint();

        if (cursor.y == this.y && cursor.x >= this.x) {
            final int cursorPos = cursor.x - this.x;

            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_KP_LEFT) {
                if (cursorPos > 0) {
                    console.setCursor(cursor.x - 1, cursor.y);
                }
            } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_KP_RIGHT) {
                if (cursorPos < this.maxLen) {
                    console.setCursor(cursor.x + 1, cursor.y);
                }
            } else if (key == KeyEvent.VK_BACK_SPACE) {
                if (cursorPos > 0) {
                    console.setCursor(cursor.x - 1, cursor.y);
                    this.characters[cursorPos - 1] = CoreConstants.SPC_CHAR;
                }
            } else if (key == KeyEvent.VK_DELETE) {
                for (int i = cursorPos + 1; i < this.maxLen; ++i) {
                    this.characters[i - 1] = this.characters[i];
                    this.characters[this.maxLen - 1] = CoreConstants.SPC_CHAR;
                }
            }
        }
    }

    /**
     * Retrieves the field value as a {@code String}.
     *
     * @return the field value
     */
    String getValue() {

        return new String(this.characters).trim();
    }

    /**
     * Clears the field.
     */
    void clear() {

        Arrays.fill(this.characters, CoreConstants.SPC_CHAR);
    }
}

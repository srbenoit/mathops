package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import dev.mathops.core.CoreConstants;

import java.awt.event.KeyEvent;

/**
 * WABBITEMU SOURCE: hardware/keys.h, "KEYPROG" struct.
 *
 * <p>
 * SRB: I have added key location since Java key codes do not distinguish left and right variants of keys.
 */
public final class KEYPROG {

    /** The virtual key code. */
    private final int vk;

    /** The key location (standard, left, right, mumpad). */
    private final int location;

    /** The group. */
    private final int group;

    /** The bit. */
    private final int bit;

    /**
     * Constructs a new {@code KEYPROG}.
     *
     * @param theVk       the virtual key code
     * @param theLocation the key location
     * @param theGroup    the group
     * @param theBit      the bit
     */
    KEYPROG(final int theVk, final int theLocation, final int theGroup, final int theBit) {

        super();

        this.vk = theVk;
        this.location = theLocation;
        this.group = theGroup;
        this.bit = theBit;
    }

    /**
     * Gets the virtual key code.
     *
     * @return the key code (a constant from java.awt.event.KeyEvent)
     */
    int getVk() {

        return this.vk;
    }

    /**
     * Gets the key location.
     *
     * @return the key location (one of KEY_LOCATION_STANDARD, KEY_LOCATION_LEFT, KEY_LOCATION_RIGHT, or
     *         KEY_LOCATION_NUMPAD from java.awt.event.KeyEvent)
     */
    public int getLocation() {

        return this.location;
    }

    /**
     * Gets the key group.
     *
     * @return the key group
     */
    public int getGroup() {

        return this.group;
    }

    /**
     * Gets the key bit.
     *
     * @return the key bit
     */
    public int getBit() {

        return this.bit;
    }

    /**
     * Generates the string representation of the object for storage in preferences.
     */
    @Override
    public String toString() {

        return "{" + this.vk + CoreConstants.COMMA_CHAR + this.location + CoreConstants.COMMA_CHAR + this.group +
                CoreConstants.COMMA_CHAR + this.bit + '}';
    }

    /**
     * Finds a {@code KEYPROG} that corresponds to a character.
     *
     * @param chr the character
     * @return the {@code KEYPROG}; {@code null} if none found
     */
    public static KEYPROG fromChar(final char chr) {

        final int vcCode = switch (chr) {
            case 'a', 'A' -> KeyEvent.VK_A;
            case 'b', 'B' -> KeyEvent.VK_B;
            case 'c', 'C' -> KeyEvent.VK_C;
            case 'd', 'D' -> KeyEvent.VK_D;
            case 'e', 'E' -> KeyEvent.VK_E;
            case 'f', 'F' -> KeyEvent.VK_F;
            case 'g', 'G' -> KeyEvent.VK_G;
            case 'h', 'H' -> KeyEvent.VK_H;
            case 'i', 'I' -> KeyEvent.VK_I;
            case 'j', 'J' -> KeyEvent.VK_J;
            case 'k', 'K' -> KeyEvent.VK_K;
            case 'l', 'L' -> KeyEvent.VK_L;
            case 'm', 'M' -> KeyEvent.VK_M;
            case 'n', 'N' -> KeyEvent.VK_N;
            case 'o', 'O' -> KeyEvent.VK_O;
            case 'p', 'P' -> KeyEvent.VK_P;
            case 'q', 'Q' -> KeyEvent.VK_Q;
            case 'r', 'R' -> KeyEvent.VK_R;
            case 's', 'S' -> KeyEvent.VK_S;
            case 't', 'T' -> KeyEvent.VK_T;
            case 'u', 'U' -> KeyEvent.VK_U;
            case 'v', 'V' -> KeyEvent.VK_V;
            case 'w', 'W' -> KeyEvent.VK_W;
            case 'x', 'X' -> KeyEvent.VK_X;
            case 'y', 'Y' -> KeyEvent.VK_Y;
            case 'z', 'Z' -> KeyEvent.VK_Z;
            case ' ' -> KeyEvent.VK_SPACE;
            case '0' -> KeyEvent.VK_0;
            case '1' -> KeyEvent.VK_1;
            case '2' -> KeyEvent.VK_2;
            case '3' -> KeyEvent.VK_3;
            case '4' -> KeyEvent.VK_4;
            case '5' -> KeyEvent.VK_5;
            case '6' -> KeyEvent.VK_6;
            case '7' -> KeyEvent.VK_7;
            case '8' -> KeyEvent.VK_8;
            case '9' -> KeyEvent.VK_9;
            case '\n' -> KeyEvent.VK_ENTER;
            case '.' -> KeyEvent.VK_DECIMAL;
            case CoreConstants.COMMA_CHAR -> KeyEvent.VK_COMMA;
            case '+' -> KeyEvent.VK_ADD;
            case '-' -> KeyEvent.VK_MINUS;
            case '*' -> KeyEvent.VK_MULTIPLY;
            case '/' -> KeyEvent.VK_DIVIDE;
            case '[' -> KeyEvent.VK_OPEN_BRACKET;
            case ']' -> KeyEvent.VK_CLOSE_BRACKET;
            default -> -1;
        };

        KEYPROG result = null;

        final int len = Keypad.KEYGRPS.length;
        for (int i = 0; i < len; ++i) {
            if (Keypad.KEYGRPS[i].vk == vcCode) {
                result = Keypad.KEYGRPS[i];
                break;
            }
        }

        return result;
    }
}

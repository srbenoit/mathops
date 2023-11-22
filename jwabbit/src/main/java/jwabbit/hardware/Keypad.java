package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.iface.Calc;
import jwabbit.iface.CalcState;
import jwabbit.iface.EnumKeypadState;

import java.awt.event.KeyEvent;

/**
 * WABBITEMU SOURCE: hardware/keys.h, "keypad" struct.
 */
public final class Keypad {

    /** WABBITEMU SOURCE: hardware/keys.c, "keygrps" array. */
    static final KEYPROG[] KEYGRPS = {new KEYPROG(KeyEvent.VK_A, KeyEvent.KEY_LOCATION_STANDARD, 5, 6),
            new KEYPROG(KeyEvent.VK_B, KeyEvent.KEY_LOCATION_STANDARD, 4, 6),
            new KEYPROG(KeyEvent.VK_C, KeyEvent.KEY_LOCATION_STANDARD, 3, 6),
            new KEYPROG(KeyEvent.VK_D, KeyEvent.KEY_LOCATION_STANDARD, 5, 5),
            new KEYPROG(KeyEvent.VK_E, KeyEvent.KEY_LOCATION_STANDARD, 4, 5),
            new KEYPROG(KeyEvent.VK_F, KeyEvent.KEY_LOCATION_STANDARD, 3, 5),
            new KEYPROG(KeyEvent.VK_G, KeyEvent.KEY_LOCATION_STANDARD, 2, 5),
            new KEYPROG(KeyEvent.VK_H, KeyEvent.KEY_LOCATION_STANDARD, 1, 5),
            new KEYPROG(KeyEvent.VK_I, KeyEvent.KEY_LOCATION_STANDARD, 5, 4),
            new KEYPROG(KeyEvent.VK_J, KeyEvent.KEY_LOCATION_STANDARD, 4, 4),
            new KEYPROG(KeyEvent.VK_K, KeyEvent.KEY_LOCATION_STANDARD, 3, 4),
            new KEYPROG(KeyEvent.VK_L, KeyEvent.KEY_LOCATION_STANDARD, 2, 4),
            new KEYPROG(KeyEvent.VK_M, KeyEvent.KEY_LOCATION_STANDARD, 1, 4),
            new KEYPROG(KeyEvent.VK_N, KeyEvent.KEY_LOCATION_STANDARD, 5, 3),
            new KEYPROG(KeyEvent.VK_O, KeyEvent.KEY_LOCATION_STANDARD, 4, 3),
            new KEYPROG(KeyEvent.VK_P, KeyEvent.KEY_LOCATION_STANDARD, 3, 3),
            new KEYPROG(KeyEvent.VK_Q, KeyEvent.KEY_LOCATION_STANDARD, 2, 3),
            new KEYPROG(KeyEvent.VK_R, KeyEvent.KEY_LOCATION_STANDARD, 1, 3),
            new KEYPROG(KeyEvent.VK_S, KeyEvent.KEY_LOCATION_STANDARD, 5, 2),
            new KEYPROG(KeyEvent.VK_T, KeyEvent.KEY_LOCATION_STANDARD, 4, 2),
            new KEYPROG(KeyEvent.VK_U, KeyEvent.KEY_LOCATION_STANDARD, 3, 2),
            new KEYPROG(KeyEvent.VK_V, KeyEvent.KEY_LOCATION_STANDARD, 2, 2),
            new KEYPROG(KeyEvent.VK_W, KeyEvent.KEY_LOCATION_STANDARD, 1, 2),
            new KEYPROG(KeyEvent.VK_X, KeyEvent.KEY_LOCATION_STANDARD, 5, 1),
            new KEYPROG(KeyEvent.VK_Y, KeyEvent.KEY_LOCATION_STANDARD, 4, 1),
            new KEYPROG(KeyEvent.VK_Z, KeyEvent.KEY_LOCATION_STANDARD, 3, 1),
            new KEYPROG(KeyEvent.VK_SPACE, KeyEvent.KEY_LOCATION_STANDARD, 4, 0),
            new KEYPROG(KeyEvent.VK_DOWN, KeyEvent.KEY_LOCATION_STANDARD, 0, 0),
            new KEYPROG(KeyEvent.VK_LEFT, KeyEvent.KEY_LOCATION_STANDARD, 0, 1),
            new KEYPROG(KeyEvent.VK_RIGHT, KeyEvent.KEY_LOCATION_STANDARD, 0, 2),
            new KEYPROG(KeyEvent.VK_UP, KeyEvent.KEY_LOCATION_STANDARD, 0, 3),
            new KEYPROG(KeyEvent.VK_0, KeyEvent.KEY_LOCATION_STANDARD, 4, 0),
            new KEYPROG(KeyEvent.VK_1, KeyEvent.KEY_LOCATION_STANDARD, 4, 1),
            new KEYPROG(KeyEvent.VK_2, KeyEvent.KEY_LOCATION_STANDARD, 3, 1),
            new KEYPROG(KeyEvent.VK_3, KeyEvent.KEY_LOCATION_STANDARD, 2, 1),
            new KEYPROG(KeyEvent.VK_4, KeyEvent.KEY_LOCATION_STANDARD, 4, 2),
            new KEYPROG(KeyEvent.VK_5, KeyEvent.KEY_LOCATION_STANDARD, 3, 2),
            new KEYPROG(KeyEvent.VK_6, KeyEvent.KEY_LOCATION_STANDARD, 2, 2),
            new KEYPROG(KeyEvent.VK_7, KeyEvent.KEY_LOCATION_STANDARD, 4, 3),
            new KEYPROG(KeyEvent.VK_8, KeyEvent.KEY_LOCATION_STANDARD, 3, 3),
            new KEYPROG(KeyEvent.VK_9, KeyEvent.KEY_LOCATION_STANDARD, 2, 3),
            new KEYPROG(KeyEvent.VK_ENTER, KeyEvent.KEY_LOCATION_STANDARD, 1, 0),
            new KEYPROG(KeyEvent.VK_DECIMAL, KeyEvent.KEY_LOCATION_STANDARD, 3, 0),
            new KEYPROG(KeyEvent.VK_COMMA, KeyEvent.KEY_LOCATION_STANDARD, 4, 4),
            new KEYPROG(KeyEvent.VK_ADD, KeyEvent.KEY_LOCATION_NUMPAD, 1, 1),
            new KEYPROG(KeyEvent.VK_SUBTRACT, KeyEvent.KEY_LOCATION_NUMPAD, 1, 2),
            new KEYPROG(KeyEvent.VK_MULTIPLY, KeyEvent.KEY_LOCATION_NUMPAD, 1, 3),
            new KEYPROG(KeyEvent.VK_DIVIDE, KeyEvent.KEY_LOCATION_NUMPAD, 1, 4),
            new KEYPROG(KeyEvent.VK_OPEN_BRACKET, KeyEvent.KEY_LOCATION_STANDARD, 3, 4),
            new KEYPROG(KeyEvent.VK_CLOSE_BRACKET, KeyEvent.KEY_LOCATION_STANDARD, 2, 4),
            new KEYPROG(KeyEvent.VK_F1, KeyEvent.KEY_LOCATION_STANDARD, 6, 4),
            new KEYPROG(KeyEvent.VK_F2, KeyEvent.KEY_LOCATION_STANDARD, 6, 3),
            new KEYPROG(KeyEvent.VK_F3, KeyEvent.KEY_LOCATION_STANDARD, 6, 2),
            new KEYPROG(KeyEvent.VK_F4, KeyEvent.KEY_LOCATION_STANDARD, 6, 1),
            new KEYPROG(KeyEvent.VK_F5, KeyEvent.KEY_LOCATION_STANDARD, 6, 0),
            new KEYPROG(KeyEvent.VK_ESCAPE, KeyEvent.KEY_LOCATION_STANDARD, 6, 6),
            new KEYPROG(KeyEvent.VK_SHIFT, KeyEvent.KEY_LOCATION_LEFT, 6, 5), // l shift
            new KEYPROG(KeyEvent.VK_CONTROL, KeyEvent.KEY_LOCATION_LEFT, 5, 7), // l control
            new KEYPROG(KeyEvent.VK_SHIFT, KeyEvent.KEY_LOCATION_RIGHT, 1, 6), // l shift
            new KEYPROG(KeyEvent.VK_MINUS, KeyEvent.KEY_LOCATION_STANDARD, 2, 0),
            new KEYPROG(KeyEvent.VK_EQUALS, KeyEvent.KEY_LOCATION_NUMPAD, 4, 7),
            new KEYPROG(KeyEvent.VK_INSERT, KeyEvent.KEY_LOCATION_STANDARD, 2, 6),
            new KEYPROG(KeyEvent.VK_DELETE, KeyEvent.KEY_LOCATION_STANDARD, 6, 7),
            new KEYPROG(KeyEvent.VK_HOME, KeyEvent.KEY_LOCATION_STANDARD, 5, 6),
            new KEYPROG(KeyEvent.VK_END, KeyEvent.KEY_LOCATION_STANDARD, 3, 7),
            new KEYPROG(KeyEvent.VK_NUMPAD0, KeyEvent.KEY_LOCATION_NUMPAD, 4, 0),
            new KEYPROG(KeyEvent.VK_NUMPAD1, KeyEvent.KEY_LOCATION_NUMPAD, 4, 1),
            new KEYPROG(KeyEvent.VK_NUMPAD2, KeyEvent.KEY_LOCATION_NUMPAD, 3, 1),
            new KEYPROG(KeyEvent.VK_NUMPAD3, KeyEvent.KEY_LOCATION_NUMPAD, 2, 1),
            new KEYPROG(KeyEvent.VK_NUMPAD4, KeyEvent.KEY_LOCATION_NUMPAD, 4, 2),
            new KEYPROG(KeyEvent.VK_NUMPAD5, KeyEvent.KEY_LOCATION_NUMPAD, 3, 2),
            new KEYPROG(KeyEvent.VK_NUMPAD6, KeyEvent.KEY_LOCATION_NUMPAD, 2, 2),
            new KEYPROG(KeyEvent.VK_NUMPAD7, KeyEvent.KEY_LOCATION_NUMPAD, 4, 3),
            new KEYPROG(KeyEvent.VK_NUMPAD8, KeyEvent.KEY_LOCATION_NUMPAD, 3, 3),
            new KEYPROG(KeyEvent.VK_NUMPAD9, KeyEvent.KEY_LOCATION_NUMPAD, 2, 3),
            new KEYPROG(KeyEvent.VK_F12, KeyEvent.KEY_LOCATION_STANDARD,
                    HardwareConstants.KEYGROUP_ON, HardwareConstants.KEYBIT_ON),};

    /** Current key group being polled. */
    private int group;

    /** List of key states. */
    private final int[][] keys;

    /** On pressed state. */
    private int onPressed;

    /** States when a key was pressed so we can match it on key release. */
    private final EnumKeypadState[][] stateOnPress;

    /** Last time on was pressed. */
    private long onLastPressed;

    /**
     * Constructs a new {@code Keypad}.
     */
    private Keypad() {

        super();

        this.keys = new int[8][8];
        this.stateOnPress = new EnumKeypadState[8][8];
    }

    /**
     * Sets the group.
     *
     * @param theGroup the group
     */
    public void setGroup(final int theGroup) {

        this.group = theGroup;
    }

    /**
     * Gets the group.
     *
     * @return the group
     */
    public int getGroup() {

        return this.group;
    }

    /**
     * Gets an entry in the keys array.
     *
     * @param theGroup the group
     * @param theBit   the bit
     * @return the keys array element
     */
    public int getKey(final int theGroup, final int theBit) {

        return this.keys[theGroup][theBit];
    }

    /**
     * Gets the on pressed value.
     *
     * @return the on pressed value
     */
    public int getOnPressed() {

        return this.onPressed;
    }

    /**
     * Gets the on last pressed value.
     *
     * @return the on last pressed value
     */
    public long getOnLastPressed() {

        return this.onLastPressed;
    }

    /**
     * Constructs and initializes a keypad.
     *
     * <p>
     * WABBITEMU SOURCE: hardware/keys.c, "keypad_init" function.
     *
     * @return the initialized keypad
     */
    static Keypad keypadInit() {

        return new Keypad();
    }

    /**
     * Handles a keypad press.
     *
     * <p>
     * WABBITEMU SOURCE: hardware/keys.c, "keypad_press" function.
     *
     * @param calc     the calculator to test for key enabled status
     * @param theGroup the group
     * @param theBit   the bit
     */
    public void keypadPress(final Calc calc, final int theGroup, final int theBit) {

        if (theGroup == HardwareConstants.KEYGROUP_ON && theBit == HardwareConstants.KEYBIT_ON) {
            this.onPressed |= HardwareConstants.KEY_KEYBOARDPRESS;
        } else {
            final EnumKeypadState state = CalcState.getKeypadState(calc);
            if (calc.isKeyEnabled(theGroup, theBit, state)) {
                this.keys[theGroup][theBit] |= HardwareConstants.KEY_KEYBOARDPRESS;
                this.stateOnPress[theGroup][theBit] = state;
            }
        }
    }

    /**
     * Handles a keypad key press.
     * <p>
     * WABBITEMU SOURCE: hardware/keys.c, "keypad_key_press" function.
     *
     * @param calc    the calculator to test for key enabled status
     * @param vk      the virtual key
     * @param loc     the keyboard location (left, right)
     * @param changed an optional array into which to store changed status
     */
    public void keypadKeyPress(final Calc calc, final int vk, final int loc, final boolean[] changed) {

        for (final KEYPROG keygrp : KEYGRPS) {
            if (keygrp.getVk() == vk && keygrp.getLocation() == loc) {
                final int theGroup = keygrp.getGroup();
                final int theBit = keygrp.getBit();

                final int orig;
                if (theGroup == HardwareConstants.KEYGROUP_ON && theBit == HardwareConstants.KEYBIT_ON) {
                    orig = this.onPressed;
                } else {
                    orig = this.keys[theGroup][theBit];
                }

                final EnumKeypadState state = CalcState.getKeypadState(calc);
                if (calc.isKeyEnabled(theGroup, theBit, state)) {
                    this.stateOnPress[theGroup][theBit] = state;
                    keypadPress(calc, theGroup, theBit);
                }

                if (changed != null) {
                    if (theGroup == HardwareConstants.KEYGROUP_ON && theBit == HardwareConstants.KEYBIT_ON) {
                        changed[0] = orig != this.onPressed;
                    } else {
                        changed[0] = orig != this.keys[theGroup][theBit];
                    }
                }

                return;
            }
        }
    }

    /**
     * Handles a key release.
     *
     * <p>
     * WABBITEMU SOURCE: hardware/keys.c, "keypad_release" function.
     *
     * @param calc     the calculator to test for key enabled status
     * @param theGroup the group
     * @param theBit   the bit
     */
    public void keypadRelease(final Calc calc, final int theGroup, final int theBit) {

        if (theGroup == HardwareConstants.KEYGROUP_ON && theBit == HardwareConstants.KEYBIT_ON) {
            this.onPressed &= ~HardwareConstants.KEY_KEYBOARDPRESS;
        } else {
            if (calc.isKeyEnabled(theGroup, theBit, this.stateOnPress[theGroup][theBit])) {
                this.keys[theGroup][theBit] &= ~HardwareConstants.KEY_KEYBOARDPRESS;
                this.stateOnPress[theGroup][theBit] = null;
            }
        }
    }

    /**
     * Handles a keypad key release.
     * <p>
     * WABBITEMU SOURCE: hardware/keys.c, "keypad_key_release" function.
     *
     * @param calc the calculator to test for key enabled state
     * @param vk   the virtual key
     * @param loc  the keyboard location (left, right)
     */
    public void keypadKeyRelease(final Calc calc, final int vk, final int loc) {

        for (final KEYPROG keygrp : KEYGRPS) {

            if (keygrp.getVk() == vk && keygrp.getLocation() == loc) {

                final EnumKeypadState state = this.stateOnPress[keygrp.getGroup()][keygrp.getBit()];
                if (calc.isKeyEnabled(keygrp.getGroup(), keygrp.getBit(), state)) {
                    keypadRelease(calc, keygrp.getGroup(), keygrp.getBit());
                }
                return;
            }
        }
    }
}

package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.iface.EnumKeypadState;

/**
 * A name associated with a key (group and bit) and keypad state (normal, second, alpha).
 */
public final class KeyName {

    /** The key name. */
    private final String name;

    /** The group. */
    private final int group;

    /** The bit. */
    private final int bit;

    /** The keypad state. */
    private final EnumKeypadState state;

    /**
     * Constructs a new {@code KeyName}.
     *
     * @param theName  the key name
     * @param theGroup the group
     * @param theBit   the bit
     * @param theState the keypad state
     */
    KeyName(final String theName, final int theGroup, final int theBit, final EnumKeypadState theState) {

        this.name = theName;
        this.group = theGroup;
        this.bit = theBit;
        this.state = theState;
    }

    /**
     * Gets the key name.
     *
     * @return the key name
     */
    public String getName() {

        return this.name;
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
     * Gets the keypad state.
     *
     * @return the keypad state
     */
    public EnumKeypadState getState() {

        return this.state;
    }
}

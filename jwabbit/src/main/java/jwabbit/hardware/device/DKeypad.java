package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;
import jwabbit.hardware.Keypad;

/**
 * The keypad device, shared by all hardware.
 */
public final class DKeypad extends AbstractDevice {

    /** The keypad to which this device interfaces. */
    private Keypad keypad;

    /**
     * Constructs a new {@code DKeypad}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public DKeypad(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.keypad = null;
    }

    /**
     * Gets the keypad to which this device interfaces.
     *
     * @return the keypad
     */
    public Keypad getKeypad() {

        return this.keypad;
    }

    /**
     * Sets the keypad to which this device interfaces.
     *
     * @param theKeypad the keypad
     */
    public void setKeypad(final Keypad theKeypad) {

        this.keypad = theKeypad;
    }

    /**
     * Runs the device code.
     *
     * <p>
     * WABBITEMU SOURCE: hardware/keys.c, "keypad" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {

            final int[] keymap = {0, 0, 0, 0, 0, 0, 0, 0};
            for (int group = 0; group < 7; ++group) {
                for (int keybit = 0; keybit < 8; ++keybit) {
                    if (this.keypad.getKey(group, keybit) != 0) {
                        keymap[group] |= 1 << keybit;
                    }
                }
            }

            final int[] keymapbug = {0, 0, 0, 0, 0, 0, 0, 0};
            for (int group = 0; group < 7; ++group) {
                for (int i = 0; i < 7; ++i) {
                    if ((keymap[group] & keymap[i]) != 0) {
                        keymapbug[group] |= keymap[group] | keymap[i];
                    }
                }
            }

            int result = 0;
            for (int group = 0; group < 7; ++group) {
                if ((this.keypad.getGroup() & (1 << group)) != 0) {
                    result |= keymapbug[group];
                }
            }

            cpu.setBus(~result);
            cpu.setInput(false);
        } else if (cpu.isOutput()) {
            final int group = ~cpu.getBus();
            this.keypad.setGroup(group);
            cpu.setOutput(false);
        }
    }
}

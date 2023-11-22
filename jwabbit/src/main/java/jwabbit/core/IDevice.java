package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * The interface for a device.
 *
 * <p>
 * WABBITEMU SOURCE: core/core.h: "device" struct.
 */
public interface IDevice {

    /**
     * Tests whether this device is active.
     *
     * @return {@code true} if device is active
     */
    boolean isActive();

    /**
     * Sets the active state of the device.
     *
     * @param isActive {@code true} if device is active
     */
    void setActive(boolean isActive);

    /**
     * Tests for a breakpoint.
     *
     * @return {@code true} if device has a breakpoint
     */
    boolean isBreakpoint();

    /**
     * Sets the breakpoint state of the device.
     *
     * @param isBreakpoint {@code true} if device has a breakpoint
     */
    void setBreakpoint(boolean isBreakpoint);

    /**
     * Tests for a protected port.
     *
     * @return {@code true} if device is a protected port
     */
    boolean isProtected();

    /**
     * Sets the protected state of the device.
     *
     * @param isProtected {@code true} if device is a protected port
     */
    void setProtected(boolean isProtected);

    /**
     * Runs the device code.
     *
     * @param cpu the CPU
     */
    void runCode(CPU cpu);

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    void clear();
}

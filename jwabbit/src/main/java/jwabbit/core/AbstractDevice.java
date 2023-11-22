package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * A device. The "aux" field below stores a wide variety of data types, inviting subclassing, and the code field also
 * invites subclassing. Therefore, this is an abstract base class that implements the IDevice interface, with several
 * subclasses. The devp function takes a CPU_t argument and a device_t argument; we need only the former since it will
 * now be a method of the device class
 *
 * <p>
 * WABBITEMU SOURCE: core/core.h: "device" struct.
 */
public abstract class AbstractDevice implements IDevice {

    /** The index at which the device is installed. */
    private final int devIndex;

    /** Flag indicating this device is active. */
    private boolean active;

    /** True if breakpoint set. */
    private boolean breakpoint;

    /** True if a protected port. */
    private boolean protectedPort;

    /**
     * Constructs a new {@code AbstractDevice}.
     *
     * @param theDevIndex the index at which the device is installed
     */
    protected AbstractDevice(final int theDevIndex) {

        super();

        this.devIndex = theDevIndex;

        this.active = false;
        this.breakpoint = false;
        this.protectedPort = false;
    }

    /**
     * Gets the index at which this device is installed.
     *
     * @return the device index
     */
    protected final int getDevIndex() {

        return this.devIndex;
    }

    /**
     * Tests whether this device is active.
     *
     * @return {@code true} if device is active
     */
    @Override
    public final boolean isActive() {

        return this.active;
    }

    /**
     * Sets the active state of the device.
     *
     * @param isActive {@code true} if device is active
     */
    @Override
    public final void setActive(final boolean isActive) {

        this.active = isActive;
    }

    /**
     * Tests for a breakpoint.
     *
     * @return {@code true} if device has a breakpoint
     */
    @Override
    public final boolean isBreakpoint() {

        return this.breakpoint;
    }

    /**
     * Sets the breakpoint state of the device.
     *
     * @param isBreakpoint {@code true} if device has a breakpoint
     */
    @Override
    public final void setBreakpoint(final boolean isBreakpoint) {

        this.breakpoint = isBreakpoint;
    }

    /**
     * Tests for a protected port.
     *
     * @return {@code true} if device is a protected port
     */
    @Override
    public final boolean isProtected() {

        return this.protectedPort;
    }

    /**
     * Sets the protected state of the device.
     *
     * @param isProtected {@code true} if device is a protected port
     */
    @Override
    public final void setProtected(final boolean isProtected) {

        this.protectedPort = isProtected;
    }
}

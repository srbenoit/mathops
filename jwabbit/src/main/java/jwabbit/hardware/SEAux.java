package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: hardware/83psehw.h, "SE_AUX" struct.
 */
public final class SEAux {

    /** The clock. */
    private final Clock clock;

    /** The delay. */
    private final Delay delay;

    /** The MD5. */
    private final MD5 md5;

    /** The link assist. */
    private final LinkAssist linka;

    /** The crystal. */
    private final XTAL xtal;

    /** The GPIO. */
    private int gpio;

    /** The GPIO write elapsed. */
    private double gpioWriteElapsed;

    /** The UDB. */
    private final USB usb;

    /**
     * Constructs a new {@code SEAux}.
     */
    public SEAux() {

        this.clock = new Clock();
        this.delay = new Delay();
        this.md5 = new MD5();
        this.linka = new LinkAssist();
        this.xtal = new XTAL();
        this.usb = new USB();
        this.gpio = 0;
    }

    /**
     * Gets the clock.
     *
     * @return the clock
     */
    public Clock getClock() {

        return this.clock;
    }

    /**
     * Gets the delay.
     *
     * @return the delay
     */
    public Delay getDelay() {

        return this.delay;
    }

    /**
     * Gets the MD5.
     *
     * @return the MD5
     */
    public MD5 getMd5() {

        return this.md5;
    }

    /**
     * Gets the link assist.
     *
     * @return the link assist
     */
    public LinkAssist getLinka() {

        return this.linka;
    }

    /**
     * Gets the crystal.
     *
     * @return the crystal
     */
    public XTAL getXtal() {

        return this.xtal;
    }

    /**
     * Sets the GPIO.
     *
     * @param theGpio the GPIO
     */
    public void setGpio(final int theGpio) {

        this.gpio = theGpio & 0x00FF;
    }

    /**
     * Gets the GPIO.
     *
     * @return the GPIO
     */
    public int getGpio() {

        return this.gpio;
    }

    /**
     * Gets the USB.
     *
     * @return the USB
     */
    public USB getUsb() {

        return this.usb;
    }
}

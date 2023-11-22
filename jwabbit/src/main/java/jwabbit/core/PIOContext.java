package jwabbit.core;

import jwabbit.hardware.AbstractLCDBase;
import jwabbit.hardware.Keypad;
import jwabbit.hardware.Link;
import jwabbit.hardware.LinkAssist;
import jwabbit.hardware.SEAux;
import jwabbit.hardware.STDINT;

/**
 * A PIO context.
 * <p>
 * WABBITEMU SOURCE: core/core.h: "pio_context" struct.
 */
public final class PIOContext {

    /** The maximum number of devices. */
    private static final int MAX_DEVICES = 256;

    /** The calculator model. */
    private EnumCalcModel model;

    /** The LCD. */
    private AbstractLCDBase lcd;

    /** The Keypad. */
    private Keypad keypad;

    /** The Link. */
    private Link link;

    /** The STDINT. */
    private STDINT stdint;

    /** The SE_AUX. */
    private SEAux seAux;

    /** The LINKASSIST. */
    private LinkAssist linkAssist;

    /** The devices. */
    private final IDevice[] devices;

    /** The most recently read data on the device (-1 if never read). */
    private final int[] mostRecentInput;

    /** The most recently written data on the device (-1 if never written). */
    private final int[] mostRecentOutput;

    /** The interrupts. */
    private final Interrupt[] interrupt;

    /** The number of interrupts. */
    private int numInterrupt;

    /** The breakpoint callback. */
    private ICpuCallback breakpointCallback;

    /**
     * Constructs a new {@code PIOContext}.
     */
    public PIOContext() {

        super();

        this.model = EnumCalcModel.TI_81;

        this.lcd = null;
        this.keypad = null;
        this.link = null;
        this.stdint = null;
        this.seAux = null;

        this.devices = new IDevice[MAX_DEVICES];
        this.mostRecentInput = new int[MAX_DEVICES];
        this.mostRecentOutput = new int[MAX_DEVICES];
        this.interrupt = new Interrupt[MAX_DEVICES];

        for (int i = 0; i < MAX_DEVICES; ++i) {
            this.mostRecentInput[i] = -1;
            this.mostRecentOutput[i] = -1;
            this.interrupt[i] = new Interrupt();
        }
    }

    /**
     * Initializes the context, as if a "memset 0" was used on the structure.
     */
    void clear() {

        this.model = EnumCalcModel.TI_81;
        this.lcd = null;
        this.keypad = null;
        this.link = null;
        this.stdint = null;
        this.seAux = null;
        this.linkAssist = null;

        for (final IDevice device : this.devices) {
            if (device != null) {
                device.clear();
            }
        }

        for (final Interrupt value : this.interrupt) {
            value.clear();
        }

        this.numInterrupt = 0;
        this.breakpointCallback = null;
    }

    /**
     * Sets the breakpoint callback.
     *
     * @param callback the callback
     */
    public void setBreakpointCallback(final ICpuCallback callback) {

        this.breakpointCallback = callback;
    }

    /**
     * Gets the breakpoint callback.
     *
     * @return the callback
     */
    public ICpuCallback getBreakpointCallback() {

        return this.breakpointCallback;
    }

    /**
     * Gets the calculator model.
     *
     * @return the model
     */
    public EnumCalcModel getModel() {

        return this.model;
    }

    /**
     * Sets the calculator model.
     *
     * @param theModel the model
     */
    public void setModel(final EnumCalcModel theModel) {

        this.model = theModel;
    }

    /**
     * Gets the LCD.
     *
     * @return the LCD
     */
    public AbstractLCDBase getLcd() {

        return this.lcd;
    }

    /**
     * Sets the LCD.
     *
     * @param theLcd the LCD
     */
    public void setLcd(final AbstractLCDBase theLcd) {

        this.lcd = theLcd;
    }

    /**
     * Gets the keypad.
     *
     * @return the keypad
     */
    public Keypad getKeypad() {

        return this.keypad;
    }

    /**
     * Sets the keypad.
     *
     * @param theKeypad the keypad
     */
    public void setKeypad(final Keypad theKeypad) {

        this.keypad = theKeypad;
    }

    /**
     * Gets the link.
     *
     * @return the link
     */
    public Link getLink() {

        return this.link;
    }

    /**
     * Sets the link.
     *
     * @param theLink the link
     */
    public void setLink(final Link theLink) {

        this.link = theLink;
    }

    /**
     * Gets the STDINT.
     *
     * @return the STDINT
     */
    public STDINT getStdint() {

        return this.stdint;
    }

    /**
     * Sets the STDINT.
     *
     * @param theStdint the STDINT
     */
    public void setStdint(final STDINT theStdint) {

        this.stdint = theStdint;
    }

    /**
     * Gets the SE_AUX.
     *
     * @return the SE_AUX
     */
    public SEAux getSeAux() {

        return this.seAux;
    }

    /**
     * Sets the SE_AUX.
     *
     * @param theSeAux the SE_AUX
     */
    public void setSeAux(final SEAux theSeAux) {

        this.seAux = theSeAux;
    }

    /**
     * Gets the LINKASSIST.
     *
     * @return the LINKASSIST
     */
    public LinkAssist getLinkAssist() {

        return this.linkAssist;
    }

    /**
     * Sets the LINKASSIST.
     *
     * @param theLinkAssist the LINKASSIST
     */
    public void setLinkAssist(final LinkAssist theLinkAssist) {

        this.linkAssist = theLinkAssist;
    }

    /**
     * Gets the device indexes.
     *
     * @return an array of device indexes of all devices in use
     */
    public int[] getDeviceIndexes() {

        int count = 0;
        for (final IDevice device : this.devices) {
            if (device != null) {
                ++count;
            }
        }

        final int[] result = new int[count];
        int index = 0;
        final int numDevices = this.devices.length;
        for (int i = 0; i < numDevices; ++i) {
            if (this.devices[i] != null) {
                result[index] = i;
                ++index;
            }
        }

        return result;
    }

    /**
     * Gets the device at a particular index.
     *
     * @param index the index
     * @return the device at that index
     */
    public IDevice getDevice(final int index) {

        return this.devices[index];
    }

    /**
     * Sets the device at a particular index.
     *
     * @param index     the index
     * @param theDevice the device to store at that index
     */
    public void setDevice(final int index, final IDevice theDevice) {

        this.devices[index] = theDevice;
        this.mostRecentInput[index] = -1;
        this.mostRecentOutput[index] = -1;
    }

    /**
     * Sets the most recently input data on a device.
     *
     * @param index the device index
     * @param data  the value input
     */
    void setMostRecentInput(final int index, final int data) {

        this.mostRecentInput[index] = data;
    }

    /**
     * Gets the most recently input data on a device.
     *
     * @param index the device index
     * @return the input value
     */
    public int getMostRecentInput(final int index) {

        return this.mostRecentInput[index];
    }

    /**
     * Sets the most recently output data on a device.
     *
     * @param index the device index
     * @param data  the value output
     */
    void setMostRecentOutput(final int index, final int data) {

        this.mostRecentOutput[index] = data;
    }

    /**
     * Gets the most recently output data on a device.
     *
     * @param index the device index
     * @return the output value
     */
    public int getMostRecentOutput(final int index) {

        return this.mostRecentOutput[index];
    }

    /**
     * Gets the maximum interrupt index.
     *
     * @return the maximum index
     */
    int getMaxInterrupt() {

        return this.interrupt.length - 1;
    }

    /**
     * Gets the interrupt at a particular index.
     *
     * @param index the index
     * @return the interrupt at that index (never null)
     */
    public Interrupt getInterrupt(final int index) {

        return this.interrupt[index];
    }

    /**
     * Gets the number of interrupts.
     *
     * @return the number of interrupts
     */
    public int getNumInterrupt() {

        return this.numInterrupt;
    }

    /**
     * Sets the number of interrupts.
     *
     * @param theNum the number of interrupts
     */
    void setNumInterrupt(final int theNum) {

        this.numInterrupt = theNum;
    }
}

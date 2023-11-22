package jwabbit.hardware.device;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.AbstractDevice;
import jwabbit.core.CPU;

/**
 * The port remapping device for the TI 83pse hardware.
 */
public final class D83psePortChunkRemap extends AbstractDevice {

    /** The array of port data. */
    private int[] data;

    /**
     * Constructs a new {@code Device83psePortChunkRemap}.
     *
     * @param theDevIndex the index at which this device is installed
     */
    public D83psePortChunkRemap(final int theDevIndex) {

        super(theDevIndex);
    }

    /**
     * Clears the structure as if "memset(0)" were called.
     */
    @Override
    public void clear() {

        this.data = null;
    }

    /**
     * Gets the data value array.
     *
     * @return the data value array
     */
    public int[] getData() {

        return this.data;
    }

    /**
     * Sets the port data array.
     *
     * @param theData the data value array
     */
    public void setData(final int[] theData) {

        this.data = theData;
    }

    /**
     * WABBITEMU SOURCE: hardware/83psehw.c, "port_chunk_remap_83pse" function.
     *
     * @param cpu the CPU
     */
    @Override
    public void runCode(final CPU cpu) {

        if (cpu.isInput()) {
            cpu.setBus(this.data[0]);
            cpu.setInput(false);
        } else {
            this.data[0] = cpu.getBus();
            cpu.setOutput(false);
        }
    }
}

package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.CPU;
import jwabbit.core.Memory;
import jwabbit.core.MemoryContext;
import jwabbit.log.LoggedObject;
import jwabbit.utilities.AUDIO;
import jwabbit.utilities.IntelHex;
import jwabbit.utilities.TIFILE;
import jwabbit.utilities.TIFlash;
import jwabbit.utilities.Var;

import java.io.FileInputStream;

/**
 * WABBITEMU SOURCE: hardware/link.h, "link" struct.
 */
public final class Link {

    /** What we wrote to the link port. */
    private final int[] host = new int[1];

    /** What they wrote to the link port. */
    private int[] client;

    /** Amount already sent over vlink. */
    private final int vlinkSend;

    /** Amount already received over the link. */
    private final int vlinkRecv;

    /** Size of the var currently on the link (if known). */
    private final int vlinkSize;

    /** The audio object. */
    private final AUDIO audio;

    /** Data out. */
    private final int[] vout = new int[1];

    /** Virtual link data. */
    private final int[] vin;

    /** If were connected to a hub, has the hub value changed. */
    private boolean hasChanged;

    /** When the data changed. */
    private long changedTime;

    /**
     * Constructs a new {@code Link}.
     */
    public Link() {

        super();

        this.client = this.host;
        this.vlinkSend = 0;
        this.vlinkRecv = 0;
        this.vlinkSize = 0;
        this.vin = null;
        this.hasChanged = false;
        this.changedTime = 0L;
        this.audio = new AUDIO();
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public int getHost() {

        return this.host[0];
    }

    /**
     * Gets the host array.
     *
     * @return the host array
     */
    public int[] getHostArray() {

        return this.host;
    }

    /**
     * Sets the host.
     *
     * @param theHost the host
     */
    public void setHost(final int theHost) {

        this.host[0] = theHost & 0x00FF;
    }

    /**
     * Gets the client.
     *
     * @return the client
     */
    public int[] getClient() {

        return this.client;
    }

    /**
     * Sets the client.
     *
     * @param theClient the client
     */
    public void setClient(final int[] theClient) {

        this.client = theClient;
    }

    /**
     * Gets the V-link send.
     *
     * @return the V-link send
     */
    public int getVlinkSend() {

        return this.vlinkSend;
    }

    /**
     * Gets the V-link receive.
     *
     * @return the V-link receive
     */
    public int getVlinkRecv() {

        return this.vlinkRecv;
    }

    /**
     * Gets the V-link size.
     *
     * @return the V-link size
     */
    public int getVlinkSize() {

        return this.vlinkSize;
    }

    /**
     * Gets the audio object size.
     *
     * @return the audio object
     */
    public AUDIO getAudio() {

        return this.audio;
    }

    /**
     * Gets the V-out.
     *
     * @return the V-out
     */
    public int getVout() {

        return this.vout[0];
    }

    /**
     * Gets the V-in.
     *
     * @return the V-in
     */
    public int[] getVin() {

        return this.vin;
    }

    /**
     * Gets the changed flag.
     *
     * @return true if changed
     */
    public boolean isHasChanged() {

        return this.hasChanged;
    }

    /**
     * Sets the changed flag.
     *
     * @param theHasChanged true if changed
     */
    public void setHasChanged(final boolean theHasChanged) {

        this.hasChanged = theHasChanged;
    }

    /**
     * Gets the changed time.
     *
     * @return the changed time
     */
    public long getChangedTime() {

        return this.changedTime;
    }

    /**
     * Sets the changed time.
     *
     * @param theChangedTime the changed time
     */
    public void setChangedTime(final long theChangedTime) {

        this.changedTime = theChangedTime;
    }

    /**
     * Writes the boot page.
     *
     * <p>
     * WABBITEMU SOURCE: hardware/link.c, "writeboot" function.
     *
     * @param infile the input file to read
     * @param memc   the memory context
     * @param page   the boot page, or -1 to use the first page after flash pages
     */
    public static void writeboot(final FileInputStream infile, final MemoryContext memc, final int page) {

        if (infile == null) {
            LoggedObject.LOG.warning("Writeboot called with null infile");
            return;
        }

        int thePage = page;
        if (thePage == -1) {
            thePage += memc.getFlash().getPages();
        }

        final IntelHex ihex = new IntelHex();
        final Memory flash = memc.getFlash();

        for (; ; ) {
            if (Var.readIntelHex(infile, ihex) == 0) {
                return;
            }
            switch (ihex.getType()) {
                case 0x00:
                    for (int j = 0; j < ihex.getDataSize(); ++j) {
                        flash.set(thePage * Memory.PAGE_SIZE + (ihex.getAddress() & 0x3FFF) + j, ihex.getData()[j]);
                    }
                    break;
                case 0x02:
                    break;
                default:
                    return;
            }
        }
    }

    /**
     * WABBITEMU SOURCE: hardware/linksendvar.c, "forceload_os" function.
     *
     * @param cpu    the CPU
     * @param tifile the file
     */
    public static void forceloadOS(final CPU cpu, final TIFILE tifile) {

        int page;
        final Memory dest = cpu.getMemoryContext().getFlash();

        if (dest == null) {
            LoggedObject.LOG.warning("force loading OS on CPU with null flash");
            return;
        }

        final TIFlash flash = tifile.getFlash();
        if (flash == null) {
            LoggedObject.LOG.warning("force loading OS from TIFILE with null flash");
            return;
        }

        final int startPage;
        switch (cpu.getPIOContext().getModel()) {
            case TI_84P:
                startPage = 0x20;
                break;
            case TI_83PSE:
            case TI_84PSE:
                startPage = 0x60;
                break;
            case TI_83P:
            case TI_84PCSE:
                startPage = 0x00;
                break;

            case INVALID_MODEL:
            case TI_73:
            case TI_81:
            case TI_82:
            case TI_83:
            case TI_85:
            case TI_86:
            default:
                return;
        }

        final int[][] flashData = flash.getData();
        final int flashDataLen = flashData.length;

        for (int i = 0; i < flashDataLen; ++i) {
            if (flashData[i] == null) {
                continue;
            }

            if (i > 0x10) {
                page = startPage + i;
            } else {
                page = i;
            }
            final int sector = (page / 4) << 2;
            final int size;

            if (sector >= cpu.getMemoryContext().getFlash().getPages() - 4) {
                size = Memory.PAGE_SIZE << 1;
            } else {
                size = Memory.PAGE_SIZE << 2;
            }

            for (int j = 0; j < size; ++j) {
                dest.set(sector * Memory.PAGE_SIZE + j, 0xFF);
            }
        }

        for (int i = 0; i < flashDataLen; ++i) {
            if (flashData[i] == null) {
                continue;
            }

            if (i > 0x10) {
                page = startPage + i;
            } else {
                page = i;
            }

            final int[] source = flashData[i];
            for (int j = 0; j < Memory.PAGE_SIZE; ++j) {
                dest.set(page * Memory.PAGE_SIZE + j, source[j]);
            }
        }

        // valid OS
        dest.set(0x56, 0x5A);
        dest.set(0x57, 0xA5);
    }
}

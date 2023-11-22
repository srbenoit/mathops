package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import java.util.Arrays;

/**
 * WABBITEMU SOURCE: core/core.h: "profiler" macro.
 */
final class Profiler {

    /** WABBITEMU SOURCE: core/core.h: "MIN_BLOCK_SIZE" macro. */
    private static final int MIN_BLOCK_SIZE = 16;

    /** Max flash page size. */
    private static final int MAX_FLASH_PAGE_SIZE = 0x80;

    /** Max ram page size. */
    private static final int MAX_RAM_PAGE_SIZE = 0x08;

    /** Running flag. */
    private boolean running;

    /** Block size. */
    private int blockSize;

    /** Total time. */
    private long totalTime;

    /** Flash data. */
    private final long[][] flashData = new long[MAX_FLASH_PAGE_SIZE][Memory.PAGE_SIZE / MIN_BLOCK_SIZE];

    /** Ram data. */
    private final long[][] ramData = new long[MAX_RAM_PAGE_SIZE][Memory.PAGE_SIZE / MIN_BLOCK_SIZE];

    /**
     * Constructs a new {@code Profiler}.
     */
    Profiler() {

        // No action
    }

    /**
     * Clears the structure as if memset(0) were called.
     */
    void clear() {

        this.running = false;
        this.blockSize = 0;
        this.totalTime = 0L;
        for (final long[] flashDatum : this.flashData) {
            Arrays.fill(flashDatum, 0L);
        }
        for (final long[] ramDatum : this.ramData) {
            Arrays.fill(ramDatum, 0L);
        }
    }

    /**
     * Tests whether the profiler is running.
     *
     * @return true if running
     */
    public boolean isRunning() {

        return this.running;
    }

    /**
     * Sets the total time.
     *
     * @param theTotalTime the total time
     */
    void setTotalTime(final long theTotalTime) {

        this.totalTime = theTotalTime;
    }

    /**
     * Gets the total time.
     *
     * @return the total time
     */
    long getTotalTime() {

        return this.totalTime;
    }

    /**
     * Gets the block size.
     *
     * @return the block size
     */
    int getBlockSize() {

        return this.blockSize;
    }

    /**
     * Gets the flash data buffer.
     *
     * @return the flash data buffer
     */
    long[][] getFlashData() {

        return this.flashData;
    }

    /**
     * Gets the RAM data buffer.
     *
     * @return the RAM data buffer
     */
    long[][] getRamData() {

        return this.ramData;
    }
}

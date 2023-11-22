package dev.mathops.assessment;

/**
 * Interface implemented by an instructional cache allowing it to be triggered to rescan its data directory for changed
 * files.
 */
@FunctionalInterface
interface InstructionalCacheInt {

    /**
     * Indicates that the cache should rescan its data directory for updated files.
     */
    void rescan();
}

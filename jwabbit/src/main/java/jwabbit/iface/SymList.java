package jwabbit.iface;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: interface/state.h, "symlist" struct.
 */
final class SymList {

    /** The last index. */
    private int lastIndex;

    /** The symbols. */
    private final Symbol83p[] symbols;

    /** The count. */
    private int count;

    /**
     * Constructs a new {@code Symbol83p}.
     */
    SymList() {

        this.lastIndex = -1;
        final int numSym = 2048;
        this.symbols = new Symbol83p[numSym];
        for (int i = 0; i < numSym; ++i) {
            this.symbols[i] = new Symbol83p();
        }
        this.count = 0;
    }

    /**
     * Sets the programs index.
     *
     * @param theProgramsIndex the programs index
     */
    void setProgramsIndex(final int theProgramsIndex) {

    }

    /**
     * Gets the last index.
     *
     * @return the last index
     */
    int getLastIndex() {

        return this.lastIndex;
    }

    /**
     * Sets the last index.
     *
     * @param theLastIndex the last index
     */
    void setLastIndex(final int theLastIndex) {

        this.lastIndex = theLastIndex;
    }

    /**
     * Gets the symbol at a particular index.
     *
     * @param index the index
     * @return the symbol
     */
    Symbol83p getSymbol(final int index) {

        return this.symbols[index];
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public int getCount() {

        return this.count;
    }

    /**
     * Sets the count.
     *
     * @param theCount the count
     */
    public void setCount(final int theCount) {

        this.count = theCount;
    }
}

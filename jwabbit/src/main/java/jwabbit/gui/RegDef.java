package jwabbit.gui;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: gui/registry.c, "reg_default_t" struct.
 */
class RegDef {

    /** A registry entry type. */
    static final int DWORD = 1;

    /** A registry entry type. */
    static final int BOOLEAN = 2;

    /** A registry entry type. */
    static final int STRING = 3;

    /** The name. */
    private final String valueName;

    /** The type. */
    private final int type;

    /** The value. */
    private final Object value;

    /**
     * Constructs a new {@code RegDefault}.
     *
     * @param theValueName the value name
     * @param theType      the type
     * @param theValue     the value
     */
    RegDef(final String theValueName, final int theType, final Object theValue) {

        this.valueName = theValueName;
        this.type = theType;
        this.value = theValue;

        if (theType == DWORD) {
            if (!(theValue instanceof Integer)) {
                throw new IllegalArgumentException("REG_DWORD specified but value is " + theValue.getClass().getName());
            }
        } else if (theType == BOOLEAN) {
            if (!(theValue instanceof Boolean)) {
                throw new IllegalArgumentException("REG_BOOLEAN specified but value is "
                        + theValue.getClass().getName());
            }
        } else if (theType == STRING) {
            if (!(theValue instanceof String)) {
                throw new IllegalArgumentException("REG_SZ specified but value is " + theValue.getClass().getName());
            }
        } else {
            throw new IllegalArgumentException("Invalid type: " + theType);
        }
    }

    /**
     * Gets the value name.
     *
     * @return the value name
     */
    final String getValueName() {

        return this.valueName;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public final int getType() {

        return this.type;
    }

    /**
     * Gets the value as an integer.
     *
     * @return the value
     */
    final Integer getIntegerValue() {

        return (this.value instanceof Integer) ? (Integer) this.value : null;
    }

    /**
     * Gets the value as a Boolean.
     *
     * @return the value
     */
    public final Boolean getBooleanValue() {

        return (this.value instanceof Boolean) ? (Boolean) this.value : null;
    }

    /**
     * Gets the value as a String.
     *
     * @return the value
     */
    final String getStringValue() {

        return (this.value instanceof String) ? (String) this.value : null;
    }
}

package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * The supported calculator models. WARNING: Comparisons are made on the ordinals of values in this enumeration, so the
 * order of values should not be changed.
 *
 * <p>
 * WABBITEMU SOURCE: core/modeltypes.h, "CalcModel" enum.
 */
public enum EnumCalcModel {

    /** TI_81. */
    TI_81,

    /** TI_82. */
    TI_82,

    /** TI_83. */
    TI_83,

    /** TI_85. */
    TI_85,

    /** TI_86. */
    TI_86,

    /** TI_73. */
    TI_73,

    /** TI_83P. */
    TI_83P,

    /** TI_83PSE. */
    TI_83PSE,

    /** TI_84P. */
    TI_84P,

    /** TI_84PSE. */
    TI_84PSE,

    /** TI_84PCSE. */
    TI_84PCSE,

    /**
     * modeltypes.h: an invalid model (-1 in the original, but we want to preserve TI-81 ordinal as zero since its used
     * as an array index).
     */
    INVALID_MODEL
}

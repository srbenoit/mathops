package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: hardware/link.h, "LINK_ERR" enum.
 */
enum EnumLinkErr {

    /** No error. */
    LERR_SUCCESS,

    /** Not the correct model for file. */
    LERR_MODEL,

    /** Invalid TIFILE in argument. */
    LERR_FILE
}

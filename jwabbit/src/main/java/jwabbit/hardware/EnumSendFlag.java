package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * WABBITEMU SOURCE: hardware/link.h, "SEND_FLAG" enum.
 */
public enum EnumSendFlag {

    /** Sends based on current flag settings. */
    SEND_CUR,

    /** Sends to RAM, regardless of flag settings. */
    SEND_RAM,

    /** Sends to archive, regardless of flag settings. */
    SEND_ARC
}

package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * Flash commands.
 *
 * <p>
 * WABBITEMU SOURCE: core/core.h, "FLASH_COMMAND" enum.
 */
public enum EnumFlashCommand {

    /** FLASH_READ. */
    FLASH_READ,
    /** FLASH_AA. */
    FLASH_AA,
    /** FLASH_55. */
    FLASH_55,
    /** FLASH_PROGRAM. */
    FLASH_PROGRAM,
    /** FLASH_ERASE. */
    FLASH_ERASE,
    /** FLASH_ERASE_AA. */
    FLASH_ERASE_AA,
    /** FLASH_ERASE_55. */
    FLASH_ERASE_55,
    /** FLASH_FASTMODE. */
    FLASH_FASTMODE,
    /** FLASH_FASTMODE_PROG. */
    FLASH_FASTMODE_PROG,
    /** FLASH_FASTMODE_EXIT. */
    FLASH_FASTMODE_EXIT,
    /** FLASH_AUTOSELECT. */
    FLASH_AUTOSELECT,
    /** FLASH_ERROR. */
    FLASH_ERROR
}

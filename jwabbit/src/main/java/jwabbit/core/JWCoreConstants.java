package jwabbit.core;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * Constants provided by the core package.
 */
public enum JWCoreConstants {
    ;

    /** WABBITEMU SOURCE: core/alu.h: "SIGN_MASK" macro. */
    public static final int SIGN_MASK = 0x80;

    /** WABBITEMU SOURCE: core/alu.h: "ZERO_MASK" macro. */
    public static final int ZERO_MASK = 0x40;

    /** WABBITEMU SOURCE: core/alu.h: "HC_MASK" macro. */
    public static final int HC_MASK = 0x10;

    /** WABBITEMU SOURCE: core/alu.h: "PV_MASK" macro. */
    public static final int PV_MASK = 0x04;

    /** WABBITEMU SOURCE: core/alu.h: "N_MASK" macro. */
    public static final int N_MASK = 0x02;

    /** WABBITEMU SOURCE: core/alu.h: "CARRY_MASK" macro. */
    public static final int CARRY_MASK = 0x01;

    /** WABBITEMU SOURCE: core/core.h: "FPS" macro. */
    public static final int FPS = 50;

    /** WABBITEMU SOURCE: core/core.h: "MHZ_2" macro. */
    public static final int MHZ_2 = 2000000;

    /** WABBITEMU SOURCE: core/core.h: "MHZ_4_8" macro. */
    public static final int MHZ_4_8 = 4800000;

    /** WABBITEMU SOURCE: core/core.h: "MHZ_6" macro. */
    public static final int MHZ_6 = 6000000;

    /** WABBITEMU SOURCE: core/core.h: "MHZ_15" macro. */
    public static final int MHZ_15 = 15000000;

    /** WABBITEMU SOURCE: core/core.h: "MHZ_20" macro. */
    public static final int MHZ_20 = 20000000;

    /** WABBITEMU SOURCE: core/core.h: "MHZ_25" macro. */
    public static final int MHZ_25 = 25000000;

    /** WABBITEMU SOURCE: core/core.h: "IX_PREFIX" macro. */
    public static final int IX_PREFIX = 0xDD;

    /** WABBITEMU SOURCE: core/core.h: "RAM_PROT_MODE" enum, "MODE0" value. */
    public static final int MODE0 = 0;

    /** WABBITEMU SOURCE: core/core.h: "BREAK_TYPE" enum, "NORMAL_BREAK" value. */
    public static final int NORMAL_BREAK = 0x1;

    /** WABBITEMU SOURCE: core/core.h: "BREAK_TYPE" enum, "MEM_WRITE_BREAK" value. */
    public static final int MEM_WRITE_BREAK = 0x2;

    /** WABBITEMU SOURCE: core/core.h: "BREAK_TYPE" enum, "MEM_READ_BREAK" value. */
    public static final int MEM_READ_BREAK = 0x4;

    /** WABBITEMU SOURCE: core/alu.h: "X5_MASK" macro. */
    static final int X5_MASK = 0x20;

    /** WABBITEMU SOURCE: core/alu.h: "X3_MASK" macro. */
    static final int X3_MASK = 0x08;

    /** WABBITEMU SOURCE: core/core.h: "HALT_SCALE" macro. */
    static final int HALT_SCALE = 3;

    /** WABBITEMU SOURCE: core/core.c: "FLASH_BYTE_PROGRAM" macro. */
    static final int FLASH_BYTE_PROGRAM = 0xA0;

    /** WABBITEMU SOURCE: core/core.c: "FLASH_BYTE_ERASE" macro. */
    static final int FLASH_BYTE_ERASE = 0x80;

    /** WABBITEMU SOURCE: core/core.c: "FLASH_BYTE_FASTMODE" macro. */
    static final int FLASH_BYTE_FASTMODE = 0x20;

    /** WABBITEMU SOURCE: core/core.c: "FLASH_BYTE_AUTOSELECT" macro. */
    static final int FLASH_BYTE_AUTOSELECT = 0x90;

    /** WABBITEMU SOURCE: core/core.c: "FLASH_BYTE_FASTMODE_EXIT" macro. */
    static final int FLASH_BYTE_FASTMODE_EXIT = 0x90;

    /** WABBITEMU SOURCE: core/core.c: "FLASH_BYTE_FASTMODE_PROG" macro. */
    static final int FLASH_BYTE_FASTMODE_PROG = 0xA0;

}

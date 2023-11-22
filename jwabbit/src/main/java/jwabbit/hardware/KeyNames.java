package jwabbit.hardware;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.iface.EnumKeypadState;

/**
 * The list of key names with group/bit/state values for each supported model.
 */
public enum KeyNames {
    ;

    /**
     * Key names for the 83+, 83PSE, 84PSE.
     */
    public static final KeyName[] KEY_NAMES_83P_83PSE_84PSE =
            {new KeyName("DEL", 6, 7, EnumKeypadState.NORMAL), //
                    new KeyName("DEL", 6, 7, EnumKeypadState.ALPHA), //
                    new KeyName("INS", 6, 7, EnumKeypadState.SECOND), //

                    new KeyName("MODE", 6, 6, EnumKeypadState.NORMAL), //
                    new KeyName("MODE", 6, 6, EnumKeypadState.ALPHA), //
                    new KeyName("QUIT", 6, 6, EnumKeypadState.SECOND), //

                    new KeyName("2ND", 6, 5, EnumKeypadState.NORMAL), //
                    new KeyName("2ND", 6, 5, EnumKeypadState.ALPHA), //
                    new KeyName("2ND", 6, 5, EnumKeypadState.SECOND), //

                    new KeyName("Y=", 6, 4, EnumKeypadState.NORMAL), //
                    new KeyName("F1", 6, 4, EnumKeypadState.ALPHA), //
                    new KeyName("STAT PLOT", 6, 4, EnumKeypadState.SECOND), //

                    new KeyName("WINDOW", 6, 3, EnumKeypadState.NORMAL), //
                    new KeyName("F2", 6, 3, EnumKeypadState.ALPHA), //
                    new KeyName("TBLSET", 6, 3, EnumKeypadState.SECOND), //

                    new KeyName("ZOOM", 6, 2, EnumKeypadState.NORMAL), //
                    new KeyName("F3", 6, 2, EnumKeypadState.ALPHA), //
                    new KeyName("FORMAT", 6, 2, EnumKeypadState.SECOND), //

                    new KeyName("TRACE", 6, 1, EnumKeypadState.NORMAL), //
                    new KeyName("F4", 6, 1, EnumKeypadState.ALPHA), //
                    new KeyName("CALC", 6, 1, EnumKeypadState.SECOND), //

                    new KeyName("GRAPH", 6, 0, EnumKeypadState.NORMAL), //
                    new KeyName("F5", 6, 0, EnumKeypadState.ALPHA), //
                    new KeyName("TABLE", 6, 0, EnumKeypadState.SECOND), //

                    new KeyName("ALPHA", 5, 7, EnumKeypadState.NORMAL), //
                    new KeyName("ALPHA", 5, 7, EnumKeypadState.ALPHA), //
                    new KeyName("A-LOCK", 5, 7, EnumKeypadState.SECOND), //

                    new KeyName("MATH", 5, 6, EnumKeypadState.NORMAL), //
                    new KeyName("A", 5, 6, EnumKeypadState.ALPHA), //
                    new KeyName("TEST", 5, 6, EnumKeypadState.SECOND), //

                    new KeyName("X^-1", 5, 5, EnumKeypadState.NORMAL), //
                    new KeyName("D", 5, 5, EnumKeypadState.ALPHA), //
                    new KeyName("MATRX", 5, 5, EnumKeypadState.SECOND), //

                    new KeyName("X^2", 5, 4, EnumKeypadState.NORMAL), //
                    new KeyName("I", 5, 4, EnumKeypadState.ALPHA), //
                    new KeyName("SQRT", 5, 4, EnumKeypadState.SECOND), //

                    new KeyName("LOG", 5, 3, EnumKeypadState.NORMAL), //
                    new KeyName("N", 5, 3, EnumKeypadState.ALPHA), //
                    new KeyName("10^X", 5, 3, EnumKeypadState.SECOND), //

                    new KeyName("LN", 5, 2, EnumKeypadState.NORMAL), //
                    new KeyName("S", 5, 2, EnumKeypadState.ALPHA), //
                    new KeyName("E^X", 5, 2, EnumKeypadState.SECOND), //

                    new KeyName("STP>", 5, 1, EnumKeypadState.NORMAL), //
                    new KeyName("X", 5, 1, EnumKeypadState.ALPHA), //
                    new KeyName("RCL", 5, 1, EnumKeypadState.SECOND), //

                    new KeyName("ON", 5, 0, EnumKeypadState.NORMAL), //
                    new KeyName("ON", 5, 0, EnumKeypadState.ALPHA), //
                    new KeyName("OFF", 5, 0, EnumKeypadState.SECOND), //

                    new KeyName("X,T,THETA,N", 4, 7, EnumKeypadState.NORMAL), //
                    new KeyName("X,T,THETA,N", 4, 7, EnumKeypadState.ALPHA), //
                    new KeyName("LINK", 4, 7, EnumKeypadState.SECOND), //

                    new KeyName("APPS", 4, 6, EnumKeypadState.NORMAL), //
                    new KeyName("B", 4, 6, EnumKeypadState.ALPHA), //
                    new KeyName("ANGLE", 4, 6, EnumKeypadState.SECOND), //

                    new KeyName("SIN", 4, 5, EnumKeypadState.NORMAL), //
                    new KeyName("E", 4, 5, EnumKeypadState.ALPHA), //
                    new KeyName("SIN^-1", 4, 5, EnumKeypadState.SECOND), //

                    new KeyName(",", 4, 4, EnumKeypadState.NORMAL), //
                    new KeyName("J", 4, 4, EnumKeypadState.ALPHA), //
                    new KeyName("EE", 4, 4, EnumKeypadState.SECOND), //

                    new KeyName("7", 4, 3, EnumKeypadState.NORMAL), //
                    new KeyName("O", 4, 3, EnumKeypadState.ALPHA), //
                    new KeyName("u", 4, 3, EnumKeypadState.SECOND), //

                    new KeyName("4", 4, 2, EnumKeypadState.NORMAL), //
                    new KeyName("T", 4, 2, EnumKeypadState.ALPHA), //
                    new KeyName("L4", 4, 2, EnumKeypadState.SECOND), //

                    new KeyName("1", 4, 1, EnumKeypadState.NORMAL), //
                    new KeyName("Y", 4, 1, EnumKeypadState.ALPHA), //
                    new KeyName("L1", 4, 1, EnumKeypadState.SECOND), //

                    new KeyName("0", 4, 0, EnumKeypadState.NORMAL), //
                    new KeyName(" ", 4, 0, EnumKeypadState.ALPHA), //
                    new KeyName("CATALOG", 4, 0, EnumKeypadState.SECOND), //

                    new KeyName("STAT", 3, 7, EnumKeypadState.NORMAL), //
                    new KeyName("STAT", 3, 7, EnumKeypadState.ALPHA), //
                    new KeyName("LIST", 3, 7, EnumKeypadState.SECOND), //

                    new KeyName("PRGM", 3, 6, EnumKeypadState.NORMAL), //
                    new KeyName("C", 3, 6, EnumKeypadState.ALPHA), //
                    new KeyName("DRAW", 3, 6, EnumKeypadState.SECOND), //

                    new KeyName("COS", 3, 5, EnumKeypadState.NORMAL), //
                    new KeyName("F", 3, 5, EnumKeypadState.ALPHA), //
                    new KeyName("COS^-1", 3, 5, EnumKeypadState.SECOND), //

                    new KeyName("(", 3, 4, EnumKeypadState.NORMAL), //
                    new KeyName("K", 3, 4, EnumKeypadState.ALPHA), //
                    new KeyName("{", 3, 4, EnumKeypadState.SECOND), //

                    new KeyName("8", 3, 3, EnumKeypadState.NORMAL), //
                    new KeyName("P", 3, 3, EnumKeypadState.ALPHA), //
                    new KeyName("v", 3, 3, EnumKeypadState.SECOND), //

                    new KeyName("5", 3, 2, EnumKeypadState.NORMAL), //
                    new KeyName("U", 3, 2, EnumKeypadState.ALPHA), //
                    new KeyName("L5", 3, 2, EnumKeypadState.SECOND), //

                    new KeyName("2", 3, 1, EnumKeypadState.NORMAL), //
                    new KeyName("Z", 3, 1, EnumKeypadState.ALPHA), //
                    new KeyName("L2", 3, 1, EnumKeypadState.SECOND), //

                    new KeyName(".", 3, 0, EnumKeypadState.NORMAL), //
                    new KeyName(":", 3, 0, EnumKeypadState.ALPHA), //
                    new KeyName("i", 3, 0, EnumKeypadState.SECOND), //

                    new KeyName("VARS", 2, 6, EnumKeypadState.NORMAL), //
                    new KeyName("VARS", 2, 6, EnumKeypadState.ALPHA), //
                    new KeyName("DISTR", 2, 6, EnumKeypadState.SECOND), //

                    new KeyName("TAN", 2, 5, EnumKeypadState.NORMAL), //
                    new KeyName("G", 2, 5, EnumKeypadState.ALPHA), //
                    new KeyName("TAN^-1", 2, 5, EnumKeypadState.SECOND), //

                    new KeyName(")", 2, 4, EnumKeypadState.NORMAL), //
                    new KeyName("L", 2, 4, EnumKeypadState.ALPHA), //
                    new KeyName("}", 2, 4, EnumKeypadState.SECOND), //

                    new KeyName("9", 2, 3, EnumKeypadState.NORMAL), //
                    new KeyName("Q", 2, 3, EnumKeypadState.ALPHA), //
                    new KeyName("w", 2, 3, EnumKeypadState.SECOND), //

                    new KeyName("6", 2, 2, EnumKeypadState.NORMAL), //
                    new KeyName("V", 2, 2, EnumKeypadState.ALPHA), //
                    new KeyName("L6", 2, 2, EnumKeypadState.SECOND), //

                    new KeyName("3", 2, 1, EnumKeypadState.NORMAL), //
                    new KeyName("THETA", 2, 1, EnumKeypadState.ALPHA), //
                    new KeyName("L3", 2, 1, EnumKeypadState.SECOND), //

                    new KeyName("(-)", 2, 0, EnumKeypadState.NORMAL), //
                    new KeyName("?", 2, 0, EnumKeypadState.ALPHA), //
                    new KeyName("ANS", 2, 0, EnumKeypadState.SECOND), //

                    new KeyName("CLEAR", 1, 6, EnumKeypadState.NORMAL), //
                    new KeyName("CLEAR", 1, 6, EnumKeypadState.ALPHA), //
                    new KeyName("CLEAR", 1, 6, EnumKeypadState.SECOND), //

                    new KeyName("^", 1, 5, EnumKeypadState.NORMAL), //
                    new KeyName("H", 1, 5, EnumKeypadState.ALPHA), //
                    new KeyName("PI", 1, 5, EnumKeypadState.SECOND), //

                    new KeyName("/", 1, 4, EnumKeypadState.NORMAL), //
                    new KeyName("M", 1, 4, EnumKeypadState.ALPHA), //
                    new KeyName("e", 1, 4, EnumKeypadState.SECOND), //

                    new KeyName("x", 1, 3, EnumKeypadState.NORMAL), //
                    new KeyName("R", 1, 3, EnumKeypadState.ALPHA), //
                    new KeyName("[", 1, 3, EnumKeypadState.SECOND), //

                    new KeyName("-", 1, 2, EnumKeypadState.NORMAL), //
                    new KeyName("W", 1, 2, EnumKeypadState.ALPHA), //
                    new KeyName("]", 1, 2, EnumKeypadState.SECOND), //

                    new KeyName("+", 1, 1, EnumKeypadState.NORMAL), //
                    new KeyName("\"", 1, 1, EnumKeypadState.ALPHA), //
                    new KeyName("MEM", 1, 1, EnumKeypadState.SECOND), //

                    new KeyName("ENTER", 1, 0, EnumKeypadState.NORMAL), //
                    new KeyName("SOLVE", 1, 0, EnumKeypadState.ALPHA), //
                    new KeyName("ENTRY", 1, 0, EnumKeypadState.SECOND), //

                    new KeyName("UP", 0, 3, EnumKeypadState.NORMAL), //
                    new KeyName("BRIGHTNESS UP", 0, 3, EnumKeypadState.ALPHA), //
                    new KeyName("CONTRAST UP", 0, 3, EnumKeypadState.SECOND), //

                    new KeyName("RIGHT", 0, 2, EnumKeypadState.NORMAL), //
                    new KeyName("RIGHT", 0, 2, EnumKeypadState.ALPHA), //
                    new KeyName("RIGHT", 0, 2, EnumKeypadState.SECOND), //

                    new KeyName("LEFT", 0, 1, EnumKeypadState.NORMAL), //
                    new KeyName("LEFT", 0, 1, EnumKeypadState.ALPHA), //
                    new KeyName("LEFT", 0, 1, EnumKeypadState.SECOND), //

                    new KeyName("DOWN", 0, 0, EnumKeypadState.NORMAL), //
                    new KeyName("BRIGHTNESS DOWN", 0, 0, EnumKeypadState.ALPHA), //
                    new KeyName("CONTRAST DOWN", 0, 0, EnumKeypadState.SECOND), //
            };
}

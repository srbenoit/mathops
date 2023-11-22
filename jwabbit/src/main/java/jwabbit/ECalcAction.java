package jwabbit;

/* * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This
software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

/**
 * Actions that can be sent to a calculator thread.
 */
public enum ECalcAction {

    /** Reset the calculator and leave in a non-running state. */
    RESET,

    /** Turn calculator on and leave in a non-running state. */
    TURN_ON,

    /** Go to a running state. */
    RUN,

    /** Execute a single step. */
    STEP,

    /** Execute some number of steps. */
    STEP_N,

    /** Execute steps until the PC reaches some target value. */
    STEP_UNTIL_PC,

    /** Step until the PC reaches the instruction following the current instruction. */
    STEP_OVER,

    /** Step until a return has been executed. */
    STEP_OUT,

    /** Go to a non-running state. */
    STOP,

    /** Enable sound. */
    ENABLE_SOUND,

    /** Disable sound. */
    DISABLE_SOUND,

    /** Take a static screenshot. */
    TAKE_SCREENSHOT,

    /** Start recording. */
    START_RECORD,

    /** End recording. */
    END_RECORD,

    /** Set speed to 25%. */
    SET_SPEED_25,

    /** Set speed to 50%. */
    SET_SPEED_50,

    /** Set speed to 100%. */
    SET_SPEED_100,

    /** Set speed to 200%. */
    SET_SPEED_200,

    /** Set speed to 400%. */
    SET_SPEED_400,

    /** Set speed to maximum. */
    SET_SPEED_MAX,

    /** Set profile. */
    SET_PROFILE,

    /** Connect virtual link cable. */
    CONNECT_VLINK,

    /** Get last answer. */
    GET_LAST_ANSWER,

    /** Copy last answer to clipboard. */
    COPY_LAST_ANSWER,

    /** Copy text to a field. */
    COPY_TEXT,

    /** Paste clipboard into calculator. */
    PASTE,

    /** Go to non-running state and open debugger. */
    DEBUG,

    /** Terminate the calculator, close its window and debugger (if shown). */
    CLOSE,

    /** A key is pressed. */
    KEY_PRESSED,

    /** A key is released. */
    KEY_RELEASED,

    /** A keyboard key is pressed. */
    KEY_KEYPRESSED,

    /** A keyboard key is released. */
    KEY_KEYRELEASED,

    /** A breakpoint is being toggled. */
    BREAKPOINT_TOGGLE,

    /** Requests an update of the LCD data. */
    UPDATE_LCD,

    /** Requests that the calculator thread call its calc state listener. */
    REQUEST_STATE,

    /** Requests that the setup wizard be re-run. */
    RUN_WIZARD
}

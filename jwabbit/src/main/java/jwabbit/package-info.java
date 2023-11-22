/**
 * A Java port of the Wabbitemu TI calculator emulator.
 * <p>
 * The C/C++ version does a lot of pre-allocated memory arrays whose contents are then manipulated, often through
 * pointer arithmetic. The first technique is discouraged in Java, and the second unsupported, which has led to some
 * internal changes to the code structure.
 * <p>
 * In general, the paradigm is that there are some number of slots, each of which may hold a calculator, and these
 * calculators may communicate with one another via virtual links. Each calculator may have an attached debugger and a
 * UI that displays the calculator's LCD and an optional graphic skin that support keypad entry via mouse clicks. The UI
 * also supports screen shots, recording GIF or AVI animations of activities, cut and paste, and drag and drop.
 */
package jwabbit;

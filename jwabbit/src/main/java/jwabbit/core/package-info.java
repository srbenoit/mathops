/**
 * The Z-80 CPU core emulator.
 *
 * <pre>
 *  CPU
 *  |
 *  +- TimerContext
 *  |
 *  +- MemoryContext
 *  |
 *  +- PIOContext
 *  |   |
 *  |   +- LCDBase (LCD or ColorLCD)
 *  |   |
 *  |   +- Keypad
 *  |   |
 *  |   +- Link
 *  |   |
 *  |   +- STDINT
 *  |   |
 *  |   +- SE_AUX or LINKASSIST
 *  |   |
 *  |   +- Device array
 *  |   |
 *  |   +- Interrupt array
 *  |
 *  +- Profiler
 * </pre>
 */
package jwabbit.core;

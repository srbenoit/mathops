/**
 * Provides management of logging from classes.
 *
 * <pre>
 * Synchronized
 *  |
 *  +- ObjLoggerBase
 *  |   |
 *  |   +- LeveledLogger
 *  |       |
 *  |       +- ObjLogger
 *  |
 *  +- LogEntryList
 *      |
 *      +- ObjLoggerWriter
 *
 * LogEnry
 *
 * LoggedObject
 *  |
 *  +- LoggedLocked
 *
 * LoggedPanel
 *
 * LoggedThread
 *
 * LogRotator
 * </pre>
 */
package jwabbit.log;

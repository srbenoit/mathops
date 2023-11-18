/**
 * Provides management of logging from classes. There are two "flavors" of logging. First, if an {@code Installation} is
 * provided, SERVER logging is used, where logs may be written to files in a log directory and rotated according to
 * limits on file size and number of log files retained. Otherwise, CLIENT logging is used, where log data is simply
 * written to the console.
 * <p>
 * Within a VM, logging begins in CLIENT mode, and remains in that mode until a valid {@code Installation} is provided
 * (where "valid" means that the base directory exists, and it is possible to create and write to the log directory
 * specified in the paths defined in that base directory).
 *
 * <pre>
 * Synchronized
 *  |
 *  +- LogBase
 *  |   |
 *  |   +- Log (singleton)
 *  |
 *  +- LogEntryList
 *      |
 *      +- LogWriter
 *
 * ELogSetting
 *
 * LogSettings
 *
 * LogEnry
 *
 * LogRotator (utility)
 * </pre>
 */
package dev.mathops.core.log;

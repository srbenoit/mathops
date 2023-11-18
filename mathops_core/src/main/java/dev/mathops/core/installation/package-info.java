/**
 * Provides management of installation objects, each of which represents an installation with a specified base directory
 * and configuration properties.
 *
 * <p>
 * An installation, which provides a set of directory paths or configuration settings based on data read from a
 * properties file. Each installation has a base directory from which the configuration properties are read.
 *
 * <p>
 * The classes in this package do not interpret the parameters in any way - they simply provide them to downstream
 * packages. An installation object can be passed to provide access to the installation configuration data.
 *
 * <p>
 * The {@code Installations} object tracks all active installations, and also provides a thread-local variable that
 * stores the {@code Installation} used by each thread. This thread-local is used by the logging subsystem to determine
 * the log directory and logging configuration parameters to use.
 */
package dev.mathops.core.installation;

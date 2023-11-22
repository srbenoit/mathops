/**
 * Classes to perform the same functions as Java WebStart to support versioning of local applications, but in a way
 * compatible with Java versions later than 1.8.
 *
 * <p>
 * This system assumes there is an application installation directory, with the following structure:
 *
 * <pre>
 * /appdir - main application directory
 *   app.xml        (descriptor of main application)
 *   ...others...   (files as specified in descriptor that are part of main program; jar files
 *                   will be added to classpath, and must contain the main class specified in the
 *                   descriptor XML file)
 *   /launch - location of pre-launch and launch programs
 *     prelaunch.xml (descriptor for the prelaunch.jar file and others needed by pre-launcher)
 *     prelaunch.jar
 *     launch.xml (descriptor for the launch.jar file and others needed by launcher)
 *     launch.jar
 *   /update - location for downloaded updates
 *     app.xml
 *     ...others, as specified in app.xml...
 *     /launch - latest downloads of pre-launch and launch programs (whether or not installed)
 *       prelaunch.xml
 *       prelaunch.jar
 *       launch.xml
 *       launch.jar
 *   /download - temporary storage for updates as they are downloaded and verified
 *   /archive - archive of a few recent versions that could be recovered and run
 * </pre>
 *
 * <p>
 * On startup, the application executes a pre-launcher, which presents a generic Splash screen, and tests the main
 * launcher program for updates (it does not go to the network; it simply tests the update subdirectory for a newer
 * version of the "launch" program, and installs it if found). The pre-launch program would not be capable of updating
 * itself, but it can update the main launcher. When it starts the main launcher, it immediately exits.
 *
 * <p>
 * The main launcher presents a new splash screen, and tests both the pre-launcher and the main application for updates
 * (again, not testing network resources; just using what's in the '/update' subdirectory, if anything. Once everything
 * is up to date, it starts the main program (with home directory set to the application directory), but does not exit.
 * Instead, it checks the network for any updates needed for the pre-launcher, launcher, and main application, and
 * downloads any that are available.
 *
 * <p>
 * The primary application does not need to do any tests for updates (in fact, it need not be aware that such a system
 * exists). However, if it wishes to, it can test for the presence of the 'update' subdirectory, and display a message
 * to the user if an update is waiting to be installed, giving the user an option to restart and update.
 *
 * <p>
 * The pre-loader, loader, and main application each include a descriptor XML file with information about the
 * executable, including a name, version number and release date, the list of files it contains, and the expected
 * SHA-256 hash of each file, along with the class name of the main class to execute.
 */
package dev.mathops.app.webstart;

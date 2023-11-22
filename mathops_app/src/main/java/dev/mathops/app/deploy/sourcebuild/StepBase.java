package dev.mathops.app.deploy.sourcebuild;

import dev.mathops.core.log.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * The base class for all steps with methods to help print status and results.
 */
enum StepBase {
    ;

    /** The /opt directory. */
    static final File OPT = new File("/opt");

    /**
     * Prints the step text.
     *
     * @param stepNum  the step number (less than 99)
     * @param stepText the text description of the text (58 characters or less)
     */
    static void printStepText(final int stepNum, final String stepText) {

        final StringBuilder builder = new StringBuilder(70);
        builder.append("\u001B[35m");

        if (stepNum < 10) {
            builder.append(' ');
        }
        builder.append(stepNum);
        builder.append(". ");
        if (stepText.length() > 58) {
            builder.append(stepText, 0, 58);
        } else {
            builder.append(stepText);
        }
        builder.append("\u001B[0m");
        while (builder.length() < 64) { // Length changes in loop
            builder.append('.');
        }

        Log.fine(builder);
    }

    /**
     * Prints a [PASS] ending to a step line.
     *
     * @return {@code true}
     */
    static boolean indicatePass() {

        Log.fine("\u001B[32m[PASS]\u001B[0m");

        return true;
    }

    /**
     * Prints a [FAIL] ending to a step line and a fail message.
     *
     * @param messages the messages (72 characters or less each)
     * @return {@code false}
     */
    static boolean indicateFail(final String... messages) {

        Log.fine("\u001B[91m[FAIL]\u001B[0m");

        for (final String message : messages) {
            Log.fine("\u001B[36;1m    *** " + message + "\u001B[0m");
        }

        return false;
    }

    /**
     * Prints a [FAIL] ending to a step line and a fail message.
     *
     * @param messages the messages (72 characters or less each)
     * @return {@code false}
     */
    static boolean indicateFail(final Iterable<String> messages) {

        Log.fine("\u001B[91m[FAIL]\u001B[0m");

        for (final String message : messages) {
            Log.fine("\u001B[36;1m    *** " + message + "\u001B[0m");
        }

        return false;
    }

    /**
     * Executes a command with a shell process using provided environment variables.
     *
     * @param workingDir the working directory in which to execute the command
     * @param env        a map from variable name to value with environment variables to set ({@code null} or empty if
     *                   none)
     * @param log        a {@code StringBuilder} to which to append log data
     * @param cmds       the commands to execute (for example, "ls" and "-l" to execute "ls -l")
     * @return {@code true} if the command succeeded (returned 0)
     */
    static boolean didExecSucceed(final File workingDir, final Map<String, String> env,
                                  final StringBuilder log, final String... cmds) {

        boolean ok = true;

        final ProcessBuilder pb = new ProcessBuilder(cmds);

        if (env != null) {
            pb.environment().putAll(env);
        }

        pb.directory(workingDir);
        pb.redirectErrorStream(true);

        try {
            final Process proc = pb.start();

            try (final BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {

                String line = in.readLine();
                while (line != null) {
                    if (ServerSourceBuild.DEBUG) {
                        Log.fine(line);
                    }
                    log.append(line).append('\n');
                    line = in.readLine();
                }

                final int exitValue = proc.waitFor();
                if (exitValue != 0) {
                    ok = false;
                }
            }
        } catch (final InterruptedException | IOException ex) {
            ok = false;
        }

        if (!ok) {
            final StringBuilder s = new StringBuilder(50);
            for (final String c : cmds) {
                s.append(' ').append(c);
            }
            indicateFail("Command failed:" + s);
        }

        return ok;
    }
}

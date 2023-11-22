package dev.mathops.app.deploy.sourcebuild;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Verifies that all needed files are present in the build source tree and filenames have expected formats.
 */
enum VerifySourceTree {
    ;

    /**
     * Performs the verification.
     *
     * @param state the build state
     * @return {@code true} if the source tree is acceptable
     */
    static boolean checkSourceTree(final BuildState state) {

        StepBase.printStepText(1, "Verify build source tree structure");

        return doesOptBuildExist(state)
                && doesOptBuildSrcExist(state)
                && doUsrLocalFilesExist()
                && areSourceFilesPresent(state)
                && StepBase.indicatePass();
    }

    /**
     * Verifies that the "/opt/build" directory exists.
     *
     * @param state the state (if successful, the /opt/build directory is stored therein)
     * @return {@code true} if the directory exists
     */
    private static boolean doesOptBuildExist(final BuildState state) {

        final boolean ok;

        final File optBuild = new File("/opt/build");

        if (optBuild.exists() && optBuild.isDirectory()) {
            state.setOptBuild(optBuild);
            ok = true;
        } else {
            ok = StepBase.indicateFail("/opt/build directory does not exist");
        }

        return ok;
    }

    /**
     * Verifies that required builds in the "/usr/local" directory exists.
     *
     * @return {@code true} if all required files exist (or at least a representative file of each required package)
     */
    private static boolean doUsrLocalFilesExist() {

        final boolean ok;

        final File perl = new File("/usr/local/perl/bin/perl");

        if (perl.exists()) {
            ok = true;
        } else {
            ok = StepBase.indicateFail("/usr/local/perl/bin/perl does not exist");
        }

        return ok;
    }

    /**
     * Verifies that the "/opt/build/src" directory exists.
     *
     * @param state the state (if successful, the /opt/build/src directory is stored therein)
     * @return {@code true} if the directory exists
     */
    private static boolean doesOptBuildSrcExist(final BuildState state) {

        final boolean ok;

        final File optBuildSrc = new File(state.getOptBuild(), "src");

        if (optBuildSrc.exists() && optBuildSrc.isDirectory()) {
            state.setOptBuildSrc(optBuildSrc);
            ok = true;
        } else {
            ok = StepBase.indicateFail("/opt/build/src directory does not exist");
        }

        return ok;
    }

    /**
     * Verifies that all required source files exist in "/opt/build/src".
     *
     * @param state the state (if successful, the /opt/build directory is stored therein)
     * @return {@code true} if the directory exists
     */
    private static boolean areSourceFilesPresent(final BuildState state) {

        final EnumSet<EStateSourceFile> found = EnumSet.noneOf(EStateSourceFile.class);

        final File[] files = state.getOptBuildSrc().listFiles();
        if (files != null) {
            for (final File file : files) {
                if (file.isDirectory()) {
                    continue;
                }

                for (final EStateSourceFile test : EStateSourceFile.values()) {
                    if (!found.contains(test)) {
                        testFile(state, file, test, found);
                    }
                }
            }
        }

        final boolean ok;

        if (found.size() == EStateSourceFile.values().length) {
            ok = true;
        } else {
            final Collection<String> messages = new ArrayList<>(10);
            for (final EStateSourceFile test : EStateSourceFile.values()) {
                if (!found.contains(test)) {
                    messages.add(test.label + " not found");
                }
            }

            ok = StepBase.indicateFail(messages);
        }

        return ok;
    }

    /**
     * Tests whether a filename matches a state source file's filename pattern.
     *
     * @param state the state to which to add the file if found
     * @param file  the file to test
     * @param key   the key against which to test
     * @param found the set to which to add if found
     */
    private static void testFile(final BuildState state, final File file, final EStateSourceFile key,
                                 final Collection<? super EStateSourceFile> found) {

        final String name = file.getName();
        final String expect = key.filename;
        final int ver = expect.indexOf("{ver}");

        boolean match;
        if (ver == -1) {
            match = name.equals(expect);
        } else {
            final String prefix = expect.substring(0, ver);
            final String suffix = expect.substring(ver + 5);

            if (name.startsWith(prefix) && name.endsWith(suffix)) {
                final String version = name.substring(prefix.length(), name.length() - suffix.length());
                match = isVersionValid(version);
            } else {
                match = false;
            }
        }

        if (match && !file.canRead()) {
            match = false;
        }

        if (match && !state.hasSourceFile(key)) {
            state.setSourceFile(key, file);
            found.add(key);
        }
    }

    /**
     * Tests a version string.
     *
     * @param version the version string
     * @return {@code true} if it is valid
     */
    private static boolean isVersionValid(final String version) {

        final String[] split = version.split("\\.");

        boolean ok = false;
        for (final String s : split) {
            try {
                Integer.parseInt(s);
                ok = true;
            } catch (final NumberFormatException ex) {
                // Allow the last character to be a lowercase letter
                final int len = s.length();
                if (len <= 1) {
                    ok = false;
                    break;
                }
                final char last = s.charAt(len - 1);
                if (last < 'a' || last > 'z') {
                    ok = false;
                    break;
                }
                try {
                    Integer.parseInt(s.substring(0, len - 1));
                    ok = true;
                } catch (final NumberFormatException ex2) {
                    ok = false;
                    break;
                }
            }
        }

        return ok;
    }
}

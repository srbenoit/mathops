package dev.mathops.app.deploy.sourcebuild;

import java.io.File;

/**
 * Verifies that all needed files are present in the build source tree and filenames have expected formats.
 */
enum CleanBuildArea {
    ;

    /**
     * Performs the verification.
     *
     * @param state the build state
     * @return {@code true} if the source tree is acceptable
     */
    static boolean checkBuild(final BuildState state) {

        StepBase.printStepText(2, "Clean build directories");

        return isBuildSuccessful(state) && StepBase.indicatePass();
    }

    /**
     * Cleans the "/opt/build" directory.
     *
     * @param state the state
     * @return {@code true} if the directory was successfully cleaned
     */
    private static boolean isBuildSuccessful(final BuildState state) {

        boolean ok = true;

        final File optBuild = state.getOptBuild();

        final StringBuilder log = new StringBuilder(500);
        final File[] list = optBuild.listFiles();
        if (list != null) {
            for (final File file : list) {
                if ("src".equals(file.getName())) {
                    continue;
                }

                if (file.isDirectory()) {
                    ok = StepBase.didExecSucceed(optBuild, null, log, "rm", "-Rf", file.getAbsolutePath());
                } else {
                    ok = file.delete();
                }

                if (!ok) {
                    break;
                }
            }
        }

        return ok;
    }
}

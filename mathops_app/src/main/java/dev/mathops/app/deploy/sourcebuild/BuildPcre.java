package dev.mathops.app.deploy.sourcebuild;

import dev.mathops.core.log.Log;

import java.io.File;

/**
 * Builds PCRE for Apache HTTPD.
 */
enum BuildPcre {
    ;

    /**
     * Performs the build.
     *
     * @param state the build state
     * @return {@code true} if the build succeeded
     */
    static boolean checkBuild(final BuildState state) {

        StepBase.printStepText(7, "Build PCRE");

        return isBuildSuccessful(state, state.getHttpTargetDir().getAbsolutePath()) && StepBase.indicatePass();
    }

    /**
     * Performs the build.
     *
     * @param state the state
     * @param path  the path into which to target the build
     * @return {@code true} if the build succeeded
     */
    private static boolean isBuildSuccessful(final BuildState state, final String path) {

        boolean ok = true;

        final File dir = state.getUncompressedDir(EStateSourceFile.PCRE);

        StringBuilder log = new StringBuilder(2000);
        StepBase.didExecSucceed(dir, null, log, "./configure", "--prefix=" + path);

        if (log.toString().contains(" configuration summary:")) {
            StepBase.didExecSucceed(dir, null, log, "make");

            log = new StringBuilder(5000);
            StepBase.didExecSucceed(dir, null, log, "make", "test");
            Log.fine(log);

            if (log.toString().contains("# PASS:  5")) {
                StepBase.didExecSucceed(dir, null, log, "make", "install");

                final File testBin = new File(path + "/bin/pcregrep");
                if (!testBin.exists()) {
                    ok = StepBase.indicateFail("Failed to install PCRE");
                }
            } else {
                ok = StepBase.indicateFail("Failed to test build of PCRE");
            }
        } else {
            ok = StepBase.indicateFail("Failed to configure PCRE for build");
        }

        return ok;
    }
}

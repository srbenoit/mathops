package dev.mathops.app.deploy.sourcebuild;

import java.io.File;

/**
 * Builds PostgreSQL.
 */
enum BuildPostgres {
    ;

    /**
     * Performs the build.
     *
     * @param state the build state
     * @return {@code true} if the build succeeded
     */
    static boolean checkBuild(final BuildState state) {

        StepBase.printStepText(15, "Build PostgreSQL");

        return isBuildSuccessful(state, state.getPostgresqlTargetDir().getAbsolutePath()) && StepBase.indicatePass();
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

        final File dir = state.getUncompressedDir(EStateSourceFile.POSTGRESQL);

        final StringBuilder log = new StringBuilder(2000);
        StepBase.didExecSucceed(dir, null, log, "./configure", "--prefix=" + path);

        if (log.toString().contains("configure: creating ./config.status")) {

            final StringBuilder log2 = new StringBuilder(10000);

            StepBase.didExecSucceed(dir, null, log2, "make");

            if (log2.toString().contains("All of PostgreSQL successfully made. Ready to install.")) {

                StepBase.didExecSucceed(dir, null, log, "make", "install");

                final File testBin = new File(path + "/bin/pg_ctl");
                if (!testBin.exists()) {
                    ok = StepBase.indicateFail("Failed to install PostrgeSQL");
                }
            } else {
                ok = StepBase.indicateFail("Failed to build PostrgeSQL");
            }
        } else {
            ok = StepBase.indicateFail("Failed to configure PostrgeSQL for build");
        }

        return ok;
    }
}

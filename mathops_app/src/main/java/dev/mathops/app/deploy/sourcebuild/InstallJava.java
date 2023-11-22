package dev.mathops.app.deploy.sourcebuild;

import java.io.File;

/**
 * Checks for an existing Java in /opt, installs Java if needed, but does not yet set the symbolic link.
 */
enum InstallJava {
    ;

    /**
     * Performs the verification.
     *
     * @param state the build state
     * @return {@code true} if the source tree is acceptable
     */
    static boolean checkInstall(final BuildState state) {

        StepBase.printStepText(4, "Install Java");

        return testAndMove(state) && StepBase.indicatePass();
    }

    /**
     * Tests for /opt/{java sdk} with the same name as the uncompressed JDK in /opt/build.
     *
     * @param state the state
     * @return {@code true} if the directory exists
     */
    private static boolean testAndMove(final BuildState state) {

        final boolean ok;

        final File dir = state.getUncompressedDir(EStateSourceFile.OPENJDK);
        final File opt = new File(StepBase.OPT, dir.getName());

        if (opt.exists() && opt.isDirectory()) {
            ok = true;
        } else {
            final String target = "/opt/" + dir.getName() + state.dateSuffix;

            final StringBuilder log = new StringBuilder(500);
            ok = StepBase.didExecSucceed(state.getOptBuild(), null, log, "mv", dir.getName(), target);

            if (!ok) {
                StepBase.indicateFail("Failed to install Tomcat");
            }
        }

        return ok;
    }
}

package dev.mathops.app.deploy.sourcebuild;

import java.io.File;

/**
 * Copies the uncompressed Tomcat into /opt.
 */
enum InstallTomcat {
    ;

    /**
     * Performs the verification.
     *
     * @param state the build state
     * @return {@code true} if the source tree is acceptable
     */
    static boolean checkInstall(final BuildState state) {

        StepBase.printStepText(16, "Install Tomcat");

        return move(state) && StepBase.indicatePass();
    }

    /**
     * Moves the uncompressed Tomcat into /opt.
     *
     * @param state the state
     * @return {@code true} if the directory exists
     */
    private static boolean move(final BuildState state) {

        final File dir = state.getUncompressedDir(EStateSourceFile.APACHE_TOMCAT);

        final StringBuilder log = new StringBuilder(500);
        final boolean ok = StepBase.didExecSucceed(state.getOptBuild(), null, log, "mv", dir.getName(), "/opt/.");

        if (!ok) {
            StepBase.indicateFail("Failed to install Tomcat");
        }

        return ok;
    }
}

package dev.mathops.app.deploy.sourcebuild;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds Xerces for Shibboleth SP.
 */
enum BuildXerces {
    ;

    /**
     * Performs the build.
     *
     * @param state the build state
     * @return {@code true} if the build succeeded
     */
    static boolean checkBuild(final BuildState state) {

        StepBase.printStepText(10, "Build Xerces");

        return isBuildSuccessful(state, state.getShibbolethTargetDir().getAbsolutePath()) && StepBase.indicatePass();
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

        final File dir = state.getUncompressedDir(EStateSourceFile.XERCES);

        final Map<String, String> env = new HashMap<>(1);
        env.put("PKG_CONFIG_PATH", path + "/lib/pkgconfig");

        StringBuilder log = new StringBuilder(2000);
        StepBase.didExecSucceed(dir, env, log, "./configure", "--prefix=" + path, "--with-curl");

        if (log.toString().contains("configure: creating ./config.status")) {
            StepBase.didExecSucceed(dir, null, log, "make");

            log = new StringBuilder(5000);
            StepBase.didExecSucceed(dir, null, log, "make", "check");

            final String logstr = log.toString();
            if (logstr.contains("# PASS:  34") && logstr.contains("# XFAIL: 10")) {

                StepBase.didExecSucceed(dir, null, log, "make", "install");

                final File testLib = new File(path + "/lib/libxerces-c.so");
                if (!testLib.exists()) {
                    ok = StepBase.indicateFail("Failed to install Xerces");
                }
            } else {
                ok = StepBase.indicateFail("Failed to test build of Xerces");
            }
        } else {
            ok = StepBase.indicateFail("Failed to configure Xerces for build");
        }

        return ok;
    }
}

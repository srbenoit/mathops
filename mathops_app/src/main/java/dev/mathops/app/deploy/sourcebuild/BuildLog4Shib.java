package dev.mathops.app.deploy.sourcebuild;

import dev.mathops.core.log.Log;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds log4shib for Shibboleth SP.
 */
enum BuildLog4Shib {
    ;

    /**
     * Performs the build.
     *
     * @param state the build state
     * @return {@code true} if the build succeeded
     */
    static boolean checkBuild(final BuildState state) {

        StepBase.printStepText(9, "Build log4shib");

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

        final File dir = state.getUncompressedDir(EStateSourceFile.LOG4SHIB);

        final Map<String, String> env = new HashMap<>(1);
        env.put("PKG_CONFIG_PATH", path + "/lib/pkgconfig");

        StringBuilder log = new StringBuilder(2000);
        StepBase.didExecSucceed(dir, env, log, "./configure", "--disable-doxygen", "--prefix=" + path);

        if (log.toString().contains("configure: creating ./config.status")) {
            StepBase.didExecSucceed(dir, null, log, "make");

            log = new StringBuilder(5000);
            StepBase.didExecSucceed(dir, null, log, "make", "check");
            Log.fine(log);

            if (log.toString().contains("# PASS:  11")) {
                StepBase.didExecSucceed(dir, null, log, "make", "install");

                final File testLib = new File(path + "/lib/liblog4shib.so");
                if (!testLib.exists()) {
                    ok = StepBase.indicateFail("Failed to install log4shib");
                }
            } else {
                ok = StepBase.indicateFail("Failed to test build of log4shib");
            }
        } else {
            ok = StepBase.indicateFail("Failed to configure log4shib for build");
        }

        return ok;
    }
}

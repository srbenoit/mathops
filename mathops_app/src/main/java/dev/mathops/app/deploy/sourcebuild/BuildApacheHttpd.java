package dev.mathops.app.deploy.sourcebuild;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds Apache HTTPD.
 */
enum BuildApacheHttpd {
    ;

    /**
     * Performs the build.
     *
     * @param state the build state
     * @return {@code true} if the build succeeded
     */
    static boolean checkBuild(final BuildState state) {

        StepBase.printStepText(8, "Build Apache HTTPD");

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

        boolean ok;

        final File dir = state.getUncompressedDir(EStateSourceFile.HTTPD);
        final File apr = state.getUncompressedDir(EStateSourceFile.APR);
        final File aprUtil = state.getUncompressedDir(EStateSourceFile.APR_UTIL);

        StringBuilder log = new StringBuilder(2000);
        ok = StepBase.didExecSucceed(state.getOptBuild(), null, log, "mv", apr.getName(), dir.getName() + "/srclib/apr")
                && StepBase.didExecSucceed(state.getOptBuild(), null, log, "mv", aprUtil.getName(), dir.getName() + "/srclib/apr-util");

        if (ok) {

            final Map<String, String> env = new HashMap<>(1);
            env.put("PKG_CONFIG_PATH", path + "/lib/pkgconfig");

            log = new StringBuilder(2000);
            StepBase.didExecSucceed(dir, env, log, "./configure", "--prefix=" + path, "--with-included-apr", "--enable-ssl",
                    "--with-ssl", "--enable-mods-static=ssl", "--with-pcre");

            if (log.toString().contains("configure: summary of build options:")) {
                StepBase.didExecSucceed(dir, null, log, "make");
                StepBase.didExecSucceed(dir, null, log, "make", "install");

                final File testBin = new File(path + "/bin/apachectl");
                if (!testBin.exists()) {
                    ok = StepBase.indicateFail("Failed to install httpd");
                }
            } else {
                ok = StepBase.indicateFail("Failed to configure httpd for build");
            }
        } else {
            ok = StepBase.indicateFail("Failed to move apr/unr-util into httpd directory");
        }

        return ok;
    }
}

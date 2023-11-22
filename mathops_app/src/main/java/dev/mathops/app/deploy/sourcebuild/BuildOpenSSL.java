package dev.mathops.app.deploy.sourcebuild;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds OpenSSL twice - once in the target directory for Apache httpd, and once in the target directory for Shibboleth
 * SP.
 */
enum BuildOpenSSL {
    ;

    /**
     * Performs the build.
     *
     * @param state the build state
     * @return {@code true} if the build succeeded
     */
    static boolean checkBuild(final BuildState state) {

        StepBase.printStepText(5, "Build OpenSSL");

        return isBuildSuccessful(state, state.getHttpTargetDir().getAbsolutePath())
                && isBuildSuccessful(state, state.getShibbolethTargetDir().getAbsolutePath()) && StepBase.indicatePass();
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

        final File dir = state.getUncompressedDir(EStateSourceFile.OPENSSL);

        final Map<String, String> env = new HashMap<>(1);
        env.put("PERL", "/usr/local/perl/bin/perl");

        StringBuilder log = new StringBuilder(2000);
        StepBase.didExecSucceed(dir, env, log, "./config", "--prefix=" + path, "--openssldir=" + path);

        if (log.toString().contains("OpenSSL has been successfully configured")) {

            StepBase.didExecSucceed(dir, env, log, "make");

            log = new StringBuilder(5000);
            StepBase.didExecSucceed(dir, null, log, "make", "test");

            if (log.toString().contains("All tests successful.")) {
                StepBase.didExecSucceed(dir, null, log, "make", "install");
                StepBase.didExecSucceed(dir, null, log, "make", "clean");
                StepBase.didExecSucceed(StepBase.OPT, null, log, "chmod", "755", path);

                final File testBin = new File(path + "/bin/openssl");
                if (!testBin.exists()) {
                    ok = StepBase.indicateFail("Failed to install OpenSSL");
                }
            } else {
                ok = StepBase.indicateFail("Failed to test build of OpenSSL");
            }
        } else {
            ok = StepBase.indicateFail("Failed to configure OpenSSL for build");
        }

        return ok;
    }
}

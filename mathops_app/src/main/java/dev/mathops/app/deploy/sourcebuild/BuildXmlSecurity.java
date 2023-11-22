package dev.mathops.app.deploy.sourcebuild;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds XML-Security for Shibboleth SP.
 */
enum BuildXmlSecurity {
    ;

    /**
     * Performs the build.
     *
     * @param state the build state
     * @return {@code true} if the build succeeded
     */
    static boolean checkBuild(final BuildState state) {

        StepBase.printStepText(11, "Build XML-Security");

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

        final File dir = state.getUncompressedDir(EStateSourceFile.XML_SECURITY);

        final Map<String, String> env = new HashMap<>(1);
        env.put("PKG_CONFIG_PATH", path + "/lib/pkgconfig");
        env.put("LD_LIBRARY_PATH", path + "/lib");

        final StringBuilder log = new StringBuilder(2000);
        StepBase.didExecSucceed(dir, env, log, "./configure", "--prefix=" + path, "--without-xalan", "--with-openssl");

        if (log.toString().contains("configure: creating ./config.status")) {
            StepBase.didExecSucceed(dir, null, log, "make");
            StepBase.didExecSucceed(dir, null, log, "make", "install");

            final File testLib = new File(path + "/lib/libxml-security-c.so");
            if (!testLib.exists()) {
                ok = StepBase.indicateFail("Failed to install XML-Security");
            }
        } else {
            ok = StepBase.indicateFail("Failed to configure XML-Security for build");
        }

        return ok;
    }
}

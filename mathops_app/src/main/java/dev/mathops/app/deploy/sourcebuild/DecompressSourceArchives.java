package dev.mathops.app.deploy.sourcebuild;

import dev.mathops.core.CoreConstants;

import java.io.File;

/**
 * Decompresses all source archives to /opt/build and sets ownership to $USER and top-level directory permissions to
 * 755.
 */
enum DecompressSourceArchives {
    ;

    /**
     * Performs the decompress operation.
     *
     * @param state the build state
     * @return {@code true} if all archives were decompressed successfully
     */
    static boolean checkDecompress(final BuildState state) {

        StepBase.printStepText(3, "Decompress source archives");

        return wasDecompressSuccessful(state) && StepBase.indicatePass();
    }

    /**
     * Decompresses all archives to /opt/build.
     *
     * @param state the state
     * @return {@code true} if all archives were decompressed
     */
    private static boolean wasDecompressSuccessful(final BuildState state) {

        boolean ok = true;

        final File optBuild = state.getOptBuild();

        for (final EStateSourceFile key : EStateSourceFile.values()) {
            final String filename = key.filename;

            if (filename.endsWith(".tar.gz")) {
                final File file = state.getSourceFile(key);
                final File dir = new File(optBuild, file.getName().replace(".tar.gz", CoreConstants.EMPTY)
                        .replace("_linux-x64_bin", CoreConstants.EMPTY).replace("openjdk", "jdk"));

                final StringBuilder log = new StringBuilder(5000);
                ok = StepBase.didExecSucceed(optBuild, null, log, "tar", "xzf", file.getAbsolutePath());

                if (ok) {
                    ok = StepBase.didExecSucceed(optBuild, null, log, "chmod", "755", dir.getName());
                    if (ok) {
                        state.setUncompressedDir(key, dir);
                    }
                }
                if (!ok) {
                    break;
                }
            }
        }

        return ok;
    }
}

package dev.mathops.core.log;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.installation.Installation;
import dev.mathops.core.installation.Installations;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Tests for the classes supporting the LogWriter type.
 */
final class TestLogWriter {

    /** A log message. */
    private static final String MSG01 = "This is log message 1, exceeding 10 bytes";

    /** A log message. */
    private static final String MSG02 = "This is log message 2, exceeding 10 bytes";

    /** A log message. */
    private static final String MSG03 = "This is log message 3, exceeding 10 bytes";

    /** A log message. */
    private static final String MSG04 = "This is log message 4, exceeding 10 bytes";

    /** A log message. */
    private static final String MSG05 = "This is log message 5, exceeding 10 bytes";

    /** A log message. */
    private static final String MSG06 = "This is log message 6, exceeding 10 bytes";

    /** A log message. */
    private static final String MSG07 = "This is log message 7, exceeding 10 bytes";

    /** A log message. */
    private static final String MSG08 = "This is log message 8, exceeding 10 bytes";

    /** A log message. */
    private static final String MSG09 = "This is log message 9, exceeding 10 bytes";

    /** A log message. */
    private static final String MSG10 = "This is log message 10, exceeding 10 bytes";

    /** A log message. */
    private static final String MSG11 = "This is log message 11, exceeding 10 bytes";

    /** A log message. */
    private static final String LINE1 = "Line 1";

    /** A log message. */
    private static final String LINE2 = "Line 2";

    /** A log message. */
    private static final String LINE3 = "Line 3";

    /** A log message. */
    private static final String LINE4 = "Line 4";

    /** A log message. */
    private static final String LINE5 = "Line 5";

    /** A log message. */
    private static final String LINE6 = "Line 6";

    /** The temporary installation path. */
    private static File installDir = null;

    /** Settings saved before testing and restored after testing. */
    private static LogSettings savedSettings = null;

    /**
     * Sets up the test base directories.
     */
    @BeforeAll
    static void runBeforeClass() {

        // Store the current log settings, so we can restore after the test
        savedSettings = new LogSettings(LoggingSubsystem.getSettings());

        final File tempDir = new File(System.getProperty("java.io.tmpdir"));
        installDir = new File(tempDir, "zircon_temp_inst");
        if (installDir.exists() || installDir.mkdirs()) {

            final HtmlBuilder builder = new HtmlBuilder(300);

            builder.addln("log-dir=$BASE_DIR/logs");
            builder.addln("log-levels=ALL");
            builder.addln("log-to-console=true");
            builder.addln("log-to-file=true");
            builder.addln("log-file-count=40");
            builder.addln("log-file-size-limit=20000000");
            builder.addln("log-file-name-base=zircon");

            try (final FileOutputStream fw = new FileOutputStream(new File(installDir, "installation.properties"))) {
                fw.write(builder.toString().getBytes(StandardCharsets.UTF_8));
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        } else {
            Log.warning("Unable to create ", installDir.getAbsolutePath());
        }
    }

    /**
     * Cleans up the test base directories.
     */
    @AfterAll
    static void runAfterClass() {

        LoggingSubsystem.getSettings().setFrom(savedSettings);

        if (installDir.exists()) {
            final File[] files = installDir.listFiles();
            if (files != null) {
                for (final File file : files) {
                    if (file.exists() && !file.delete()) {
                        Log.warning("Failed to delete ", file.getAbsolutePath());
                    }
                }
            }
            if (!installDir.delete()) {
                Log.warning("Unable to delete ", installDir.getAbsolutePath());
            }
        }
    }

    /**
     * Writes some messages to the console. This test always passes, but logs written to the console should be checked.
     */
    @Test
    @DisplayName("Log to console: There should have been two lines written")
    void test001() {

        final LogWriter writer = new LogWriter();

        writer.writeConsole("Log to console: This should be the first line", false);
        writer.writeConsole(", with a newline", true);
        writer.getSettings().setLogToConsole(false);
        writer.writeConsole("Log to console: This should NOT appear", true);
        writer.getSettings().setLogToConsole(true);
        writer.writeConsole("Log to console: This should be the second line", false);
        writer.writeConsole(", with a linefeed", true);

        assertNotNull(writer.getSettings(), "Always succeeds");
    }

    /**
     * Test logging to a list.
     */
    @Test
    @DisplayName("Log to list")
    void test002() {

        final LogWriter writer = new LogWriter();

        writer.getSettings().setLogToConsole(false);
        writer.startList(5);
        assertEquals(0, writer.getNumInList(), "Log to list: list did not start empty");
        writer.writeMessage(LINE1, true);
        assertEquals(1, writer.getNumInList(), "Log to list: bad list size after 1 message");
        writer.writeMessage(LINE2, true);
        assertEquals(2, writer.getNumInList(), "Log to list: bad list size after 2 messages");
        writer.writeMessage(LINE3, true);
        assertEquals(3, writer.getNumInList(), "Log to list: bad list size after 3 messages");
        writer.writeMessage(LINE4, true);
        assertEquals(4, writer.getNumInList(), "Log to list: bad list size after 4 messages");
        writer.writeMessage(LINE5, true);
        assertEquals(5, writer.getNumInList(), "Log to list: bad list size after 5 messages");
        writer.addToList(LINE6);
        assertEquals(5, writer.getNumInList(), "Log to list: bad list size after 6 messages");
        writer.stopList();
        assertEquals(5, writer.getNumInList(), "Log to list: bad list size after stopping");

        assertEquals(LINE2, writer.getListMessage(0).message, "Log to list: list message 1 content");
        assertEquals(LINE3, writer.getListMessage(1).message, "Log to list: list message 2 content");
        assertEquals(LINE4, writer.getListMessage(2).message, "Log to list: list message 3 content");
        assertEquals(LINE5, writer.getListMessage(3).message, "Log to list: list message 4 content");
        assertEquals(LINE6, writer.getListMessage(4).message, "Log to list: list message 5 content");
        writer.clearList();
        assertEquals(0, writer.getNumInList(), "Log to list: bad list size after clearing");
    }

    /**
     * Test writing log messages to files and advancing to new files based on size cap.
     */
    @Test
    @DisplayName("Log to files")
    void test003() {

        final Installation inst = Installations.get().getInstallation(installDir, null);
        LoggingSubsystem.setInstallation(inst);

        final LogWriter writer = new LogWriter();

        writer.getSettings().setLogToFiles(true);
        final File logDir = writer.determineLogDir();
        writer.getSettings().setLogToFiles(false);

        writer.getSettings().setLogToConsole(false);
        writer.getSettings().setFilenameBase("testlog");
        writer.getSettings().setLogFileSizeLimit(10);
        writer.getSettings().setLogFileCount(10);

        writer.getSettings().setLogToFiles(true);
        writer.writeMessage(MSG01, true);
        writer.writeMessage(MSG02, true);
        writer.writeMessage(MSG03, true);
        writer.writeMessage(MSG04, true);
        writer.writeMessage(MSG05, true);
        writer.writeMessage(MSG06, true);
        writer.writeMessage(MSG07, true);
        writer.writeMessage(MSG08, true);
        writer.writeMessage(MSG09, true);
        writer.writeMessage(MSG10, true);
        writer.writeMessage(MSG11, true);
        writer.getSettings().setLogToFiles(false);

        // We should now have 10 files, named testlog_001.log --> testlog_010.log
        // containing the most recent 10 logged messages, most recent in the _001 log file

        final File log01 = new File(logDir, "testlog_001.log");
        final File log02 = new File(logDir, "testlog_002.log");
        final File log03 = new File(logDir, "testlog_003.log");
        final File log04 = new File(logDir, "testlog_004.log");
        final File log05 = new File(logDir, "testlog_005.log");
        final File log06 = new File(logDir, "testlog_006.log");
        final File log07 = new File(logDir, "testlog_007.log");
        final File log08 = new File(logDir, "testlog_008.log");
        final File log09 = new File(logDir, "testlog_009.log");
        final File log10 = new File(logDir, "testlog_010.log");
        final File log11 = new File(logDir, "testlog_011.log");

        assertTrue(log01.exists(), "Log to files: log file 1 did not exist");
        assertTrue(log02.exists(), "Log to files: log file 2 did not exist");
        assertTrue(log03.exists(), "Log to files: log file 3 did not exist");
        assertTrue(log04.exists(), "Log to files: log file 4 did not exist");
        assertTrue(log05.exists(), "Log to files: log file 5 did not exist");
        assertTrue(log06.exists(), "Log to files: log file 6 did not exist");
        assertTrue(log07.exists(), "Log to files: log file 7 did not exist");
        assertTrue(log08.exists(), "Log to files: log file 8 did not exist");
        assertTrue(log09.exists(), "Log to files: log file 9 did not exist");
        assertTrue(log10.exists(), "Log to files: log file 10 did not exist");
        assertFalse(log11.exists(), "Log to files: log file 11 exists");

        assertEquals(MSG11 + CoreConstants.CRLF, getFile(log01), "Log to files: log file 1 content");
        assertEquals(MSG10 + CoreConstants.CRLF, getFile(log02), "Log to files: log file 2 content");
        assertEquals(MSG09 + CoreConstants.CRLF, getFile(log03), "Log to files: log file 3 content");
        assertEquals(MSG08 + CoreConstants.CRLF, getFile(log04), "Log to files: log file 4 content");
        assertEquals(MSG07 + CoreConstants.CRLF, getFile(log05), "Log to files: log file 5 content");
        assertEquals(MSG06 + CoreConstants.CRLF, getFile(log06), "Log to files: log file 6 content");
        assertEquals(MSG05 + CoreConstants.CRLF, getFile(log07), "Log to files: log file 7 content");
        assertEquals(MSG04 + CoreConstants.CRLF, getFile(log08), "Log to files: log file 8 content");
        assertEquals(MSG03 + CoreConstants.CRLF, getFile(log09), "Log to files: log file 9 content");
        assertEquals(MSG02 + CoreConstants.CRLF, getFile(log10), "Log to files: log file 10 content");

        assertTrue(!log01.exists() || log01.delete(), "Log to files: delete 1");
        assertTrue(!log02.exists() || log02.delete(), "Log to files: delete 2");
        assertTrue(!log03.exists() || log03.delete(), "Log to files: delete 3");
        assertTrue(!log04.exists() || log04.delete(), "Log to files: delete 4");
        assertTrue(!log05.exists() || log05.delete(), "Log to files: delete 5");
        assertTrue(!log06.exists() || log06.delete(), "Log to files: delete 6");
        assertTrue(!log07.exists() || log07.delete(), "Log to files: delete 7");
        assertTrue(!log08.exists() || log08.delete(), "Log to files: delete 8");
        assertTrue(!log09.exists() || log09.delete(), "Log to files: delete 9");
        assertTrue(!log10.exists() || log10.delete(), "Log to files: delete 10");
    }

    /**
     * Loads a text file, storing the file contents in a {@code String}. Lines in the returned file are separated by
     * single '\n' characters regardless of the line terminator in the source file. The last line will end with a '\n'
     * character, whether there was one in the input file.
     *
     * @param file the file to read
     * @return the loaded file contents, or {@code null} if unable to load
     */
    private static String getFile(final File file) {

        String result;

        try (final InputStream input = new FileInputStream(file)) {
            final HtmlBuilder builder = new HtmlBuilder((int) file.length());

            try (final BufferedReader rdr = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                for (String line = rdr.readLine(); line != null; line = rdr.readLine()) {
                    builder.addln(line);
                }
            }

            result = builder.toString();
        } catch (final IOException ex) {
            result = null;
        }

        return result;
    }
}

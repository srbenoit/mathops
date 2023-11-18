package dev.mathops.core.log;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the classes supporting the {@code LogSettings} type.
 */
final class TestLogSettings {

    /** Commonly used string. */
    private static final String ALL = "ALL";

    /** Commonly used string. */
    private static final String LOGS = "logs";

    /** Commonly used string. */
    private static final String ZIRCON = "zircon";

    /**
     * Tests the state of a {@code LogSettings} object after construction. All fields should be initialized to the
     * defaults from {@code ELogSetting}.
     */
    @Test
    @DisplayName("LogSettings state on construction")
    void test001() {

        final LogSettings settings = new LogSettings();

        assertEquals(ALL, settings.getLogLevelName(), "Log level name default is ALL");
        assertEquals(LogBase.ALL, settings.getLogLevel(), "Log level default is ALL");
        assertTrue(settings.isLogToConsole(), "Log to console default is true");
        assertFalse(settings.isLogToFiles(), "Log to file default is false");
        assertEquals(LOGS, settings.getLogFilePath(), "Log file path default is logs");
        assertEquals(Integer.MAX_VALUE, settings.getLogFileCount(), "Log file count default is unlimited");
        assertEquals(Integer.MAX_VALUE, settings.getLogFileSizeLimit(), "Log file size default is unlimited");
        assertEquals(ZIRCON, settings.getFilenameBase(), "Log filename base default is zircon");
        assertTrue(settings.isAppend(), "Log append default us true");
        assertFalse(settings.isDirty(), "Log settings are initially not dirty");
    }

    /**
     * Tests the state of a {@code LogSettings} object after construction and the setting of a null
     * {@code Installation}.
     */
    @Test
    @DisplayName("LogSettings state on construction with null installation")
    void test002() {

        final LogSettings settings = new LogSettings();

        // Test configuration with null installation
        LoggingSubsystem.configureSettings(settings, null);

        assertEquals(ALL, settings.getLogLevelName(), "Log level name with null config is ALL");
        assertEquals(LogBase.ALL, settings.getLogLevel(), "Log level with null config is ALL");
        assertTrue(settings.isLogToConsole(), "Log to console with null config is true");
        assertFalse(settings.isLogToFiles(), "Log to file with null config is false");
        assertEquals(LOGS, settings.getLogFilePath(), "Log file path with null config is logs");
        assertEquals(Integer.MAX_VALUE, settings.getLogFileCount(), "Log file count with null config is unlimited");
        assertEquals(Integer.MAX_VALUE, settings.getLogFileSizeLimit(), "Log file size with null config is unlimited");
        assertEquals(ZIRCON, settings.getFilenameBase(), "Log filename base with null config is zircon");
        assertTrue(settings.isAppend(), "Log append with null config us true");
        assertFalse(settings.isDirty(), "Log settings after load with null config not dirty");
    }

    /**
     * Tests the state of a {@code LogSettings} object after construction and the setting of a non-null
     * {@code Installation}.
     */
    @Test
    @DisplayName("LogSettings state on construction with installation")
    void test003() {

        final LogSettings settings = new LogSettings();

        // Test configuration with null installation
        LoggingSubsystem.configureSettings(settings, null);

        assertEquals(ALL, settings.getLogLevelName(), "Log level name with null installation is ALL");
        assertEquals(LogBase.ALL, settings.getLogLevel(), "Log level with null installation is ALL");
        assertTrue(settings.isLogToConsole(), "Log to console with null installation is true");
        assertFalse(settings.isLogToFiles(), "Log to file with null installation is false");
        assertEquals(LOGS, settings.getLogFilePath(), "Log file path with null installation is logs");
        assertEquals(Integer.MAX_VALUE, settings.getLogFileCount(),
                "Log file count with null installation is unlimited");
        assertEquals(Integer.MAX_VALUE, settings.getLogFileSizeLimit(),
                "Log file size with null installation is unlimited");
        assertEquals(ZIRCON, settings.getFilenameBase(), "Log filename base with null installation is zircon");
        assertTrue(settings.isAppend(), "Log append with null installation us true");
        assertFalse(settings.isDirty(), "Log settings after load with null installation not dirty");
    }
}

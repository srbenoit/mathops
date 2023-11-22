package dev.mathops.core.file;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.log.Log;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

/**
 * Tests for the FileLoader class.
 */
final class TestFileLoader {

    /** A test string. */
    private static final String PREFIX = "foo";

    /** A test string. */
    private static final String SUFFIX = "bar";

    /** A test string. */
    private static final String TXT_FILENAME = "TestTextFile.bin";

    /** A test string. */
    private static final String NONEXIST_FILENAME = "Bogus.txt";

    /** A test string. */
    private static final String PROPERTIES_BASE = "test";

    /** A test string. */
    private static final String PROPERTY_1_NAME = "property1";

    /** A test string. */
    private static final String PROPERTY_2_NAME = "property2";

    /** A test string. */
    private static final String TEST_TXT_FILE = "This is a test text file.";

    /** A test string. */
    private static final String HAS_2_LINES = "It has two lines.";

    /** A test string. */
    private static final String TEST_STRING_1 = "This is a test string.";

    /** A test string. */
    private static final String TEST_STRING_2 = "This is another test string.";

    /** A test string. */
    private static final String ADVICE = "Do not run with scissors";

    /** A test string. */
    private static final String PROP_VALUE = "loadFileAsProperties() property value";

    /** A test string. */
    private static final String QBF = "The quick brown fox jumped over the lazy dog";

    /** A test string. */
    private static final String LOCALE_SPAIN = "es";

    /** A test string. */
    private static final String SPADVICE = "No corra con las tijeras";

    /** A test string. */
    private static final String SP_PROP_VALUE = "loadFileAsProperties() spanish property value";

    /** A test string. */
    private static final String CR_PROP_VALUE = "loadFileAsProperties() class-relative property value";

    /** A test string. */
    private static final String SP_CR_PROP_VALUE = "loadFileAsProperties() class-relative spanish property value";

    /** A test string. */
    private static final String SPQBF =
            "El r\u00e1pido zorro marr\u00f3n salt\u00f3 sobre el perro perezoso";

    /** A test string. */
    private static final String BOGUS = "Bogus";

    /**
     * Test case.
     *
     * @throws IOException if write or read fails.
     */
    @Test
    @DisplayName("loadFileAsString(file, false)")
    void test001() throws IOException {

        final File file = File.createTempFile(PREFIX, SUFFIX);

        try (final FileOutputStream writer = new FileOutputStream(file)) {
            writer.write(TEST_STRING_1.getBytes(StandardCharsets.UTF_8));
        }

        final String str = FileLoader.loadFileAsString(file, false);
        deleteFile(file);

        assertEquals(TEST_STRING_1, str, "loadFileAsString() file content");
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsString(class, file, false)")
    void test002() {

        final String str = FileLoader.loadFileAsString(TestFileLoader.class, TXT_FILENAME, false);

        assertEquals(TEST_TXT_FILE + CoreConstants.CRLF + HAS_2_LINES + CoreConstants.CRLF, str, 
                "loadFileAsString() class-relative file content");
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsString(file, false) on nonexistent file")
    void test003() {

        final File file = new File(NONEXIST_FILENAME);
        final String str = FileLoader.loadFileAsString(file, false);

        assertNull(str, "loadFileAsString() nonexistent file");
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsString(class, file, false) on nonexistent file")
    void test004() {

        final String str = FileLoader.loadFileAsString(TestFileLoader.class, NONEXIST_FILENAME, false);

        assertNull(str, "loadFileAsString() nonexistent class-relative file");
    }

    /**
     * Test case.
     *
     * @throws IOException if write or read fails.
     */
    @Test
    @DisplayName("loadFileAsLines(file, false) line count")
    void test005() throws IOException {

        final File file = File.createTempFile(PREFIX, SUFFIX);

        try (final FileOutputStream writer = new FileOutputStream(file)) {
            writer.write(TEST_STRING_1.getBytes(StandardCharsets.UTF_8));
            writer.write(CoreConstants.CRLF.getBytes(StandardCharsets.UTF_8));
            writer.write(TEST_STRING_2.getBytes(StandardCharsets.UTF_8));
        }

        final String[] lines = FileLoader.loadFileAsLines(file, false);
        deleteFile(file);

        assertEquals(lines.length, 2, "loadFileAsLines() file line count");
    }

    /**
     * Test case.
     *
     * @throws IOException if write or read fails.
     */
    @Test
    @DisplayName("loadFileAsLines(file, false) line 1 content")
    void test006() throws IOException {

        final File file = File.createTempFile(PREFIX, SUFFIX);

        try (final FileOutputStream writer = new FileOutputStream(file)) {
            writer.write(TEST_STRING_1.getBytes(StandardCharsets.UTF_8));
            writer.write(CoreConstants.CRLF.getBytes(StandardCharsets.UTF_8));
            writer.write(TEST_STRING_2.getBytes(StandardCharsets.UTF_8));
        }

        final String[] lines = FileLoader.loadFileAsLines(file, false);
        deleteFile(file);

        assertEquals(TEST_STRING_1, lines[0], "loadFileAsLines() file line 1 content");
    }

    /**
     * Test case.
     *
     * @throws IOException if write or read fails.
     */
    @Test
    @DisplayName("loadFileAsLines(file, false) line 2 content")
    void test007() throws IOException {

        final File file = File.createTempFile(PREFIX, SUFFIX);

        try (final FileOutputStream writer = new FileOutputStream(file)) {
            writer.write(TEST_STRING_1.getBytes(StandardCharsets.UTF_8));
            writer.write(CoreConstants.CRLF.getBytes(StandardCharsets.UTF_8));
            writer.write(TEST_STRING_2.getBytes(StandardCharsets.UTF_8));
        }

        final String[] lines = FileLoader.loadFileAsLines(file, false);
        deleteFile(file);

        assertEquals(TEST_STRING_2, lines[1], "loadFileAsLines() file line 2 content");
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsLines(class, file, false) line count")
    void test008() {

        final String[] lines = FileLoader.loadFileAsLines(TestFileLoader.class, TXT_FILENAME, false);

        assertEquals(2, lines.length, "loadFileAsLines() class-relative file line count");
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsLines(class, file, false) line 1 content")
    void test009() {

        final String[] lines = FileLoader.loadFileAsLines(TestFileLoader.class, TXT_FILENAME, false);

        assertEquals(TEST_TXT_FILE, lines[0], "loadFileAsLines class-relative file line 1 content");
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsLines(class, file, false) line 2 content")
    void test010() {

        final String[] lines = FileLoader.loadFileAsLines(TestFileLoader.class, TXT_FILENAME, false);

        assertEquals(HAS_2_LINES, lines[1], "loadFileAsLines class-relative file line 2 content");
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsLines(file, false) for nonexistent file")
    void test011() {

        final String[] lines = FileLoader.loadFileAsLines(new File(NONEXIST_FILENAME), false);

        assertNull(lines, "loadFileAsLines() nonexistent file");
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsLines(class, file, false) for nonexistent file")
    void test012() {

        final String[] lines =
                FileLoader.loadFileAsLines(TestFileLoader.class, NONEXIST_FILENAME, false);

        assertNull(lines, "loadFileAsLines() nonexistent class-relative file");
    }

    /**
     * Test case.
     *
     * @throws IOException if write or read fails.
     */
    @Test
    @DisplayName("loadFileAsBytes(file, false)")
    void test013() throws IOException {

        final File file = File.createTempFile(PREFIX, SUFFIX);

        try (final FileOutputStream writer = new FileOutputStream(file)) {
            writer.write(TEST_STRING_1.getBytes(StandardCharsets.UTF_8));
        }

        final byte[] loaded = FileLoader.loadFileAsBytes(file, false);
        deleteFile(file);

        assertArrayEquals(TEST_STRING_1.getBytes(StandardCharsets.UTF_8), loaded, "loadFileAsBytes() file content");
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsBytes(class, file, false)")
    void test014() {

        final byte[] loaded = FileLoader.loadFileAsBytes(TestFileLoader.class, TXT_FILENAME, false);
        final byte[] expect = (TEST_TXT_FILE + CoreConstants.CRLF + HAS_2_LINES
                + CoreConstants.CRLF).getBytes(StandardCharsets.UTF_8);

        assertArrayEquals(expect, loaded, "loadFileAsBytes() class-relative file content");
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsBytes(file, false) with nonexistent file")
    void test015() {

        final File file = new File(NONEXIST_FILENAME);
        final byte[] bytes = FileLoader.loadFileAsBytes(file, false);

        assertNull(bytes, "loadFileAsBytes() nonexistent file");
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsBytes(class, file, false) with nonexistent file")
    void test016() {

        final byte[] bytes = FileLoader.loadFileAsBytes(TestFileLoader.class, NONEXIST_FILENAME, false);

        assertNull(bytes, "loadFileAsBytes() nonexistent class-relative file");
    }

    /**
     * Test case.
     *
     * @throws IOException if write or read fails.
     */
    @Test
    @DisplayName("loadFileAsImage(file, false)")
    void test017() throws IOException {

        final BufferedImage img = makeTestImage();

        final File file = File.createTempFile(PREFIX, SUFFIX);

        // NOTE: Must use a loss-less format for the write operation, since we compare pixels
        final ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
        try (final FileImageOutputStream out = new FileImageOutputStream(file)) {
            writer.setOutput(out);
            writer.write(img);
        }

        final BufferedImage data = FileLoader.loadFileAsImage(file, false);
        deleteFile(file);

        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                assertEquals(img.getRGB(x, y), data.getRGB(x, y), "loadFileAsImage() file content");
            }
        }
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsImage(class, file, false)")
    void test018() {

        final BufferedImage img = makeTestImage();

        final BufferedImage data = FileLoader.loadFileAsImage(TestFileLoader.class, "test.png", false);

        for (int x = 0; x < 10; ++x) {
            for (int y = 0; y < 10; ++y) {
                assertEquals(img.getRGB(x, y), data.getRGB(x, y), "loadFileAsImage() class-relative file content");
            }
        }
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsImage(file, false) with nonexistent file")
    void test019() {

        final File file = new File(NONEXIST_FILENAME);
        final BufferedImage data = FileLoader.loadFileAsImage(file, false);

        assertNull(data, "loadFileAsImage() nonexistent file");
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsImage(class, file, false) with nonexistent file")
    void test020() {

        final BufferedImage data = FileLoader.loadFileAsImage(TestFileLoader.class, NONEXIST_FILENAME, false);

        assertNull(data, "loadFileAsImage() nonexistent class-relative file");
    }

    /**
     * Test case.
     *
     * @throws IOException if write or read fails.
     */
    @Test
    @DisplayName("loadFileAsProperties(file, base, false) English")
    void test021() throws IOException {

        final Path dirPath = Files.createTempDirectory(PREFIX);
        final File dir = dirPath.toFile();

        final File file = new File(dir, "test_en.properties");

        try (final FileOutputStream writer = new FileOutputStream(file)) {
            writer.write("property1=Do not run with scissors".getBytes(StandardCharsets.UTF_8));
            writer.write(CoreConstants.CRLF.getBytes(StandardCharsets.UTF_8));
            writer.write("property2=The quick brown fox jumped over the lazy dog".getBytes(StandardCharsets.UTF_8));
            writer.write(CoreConstants.CRLF.getBytes(StandardCharsets.UTF_8));
        }

        final Locale current = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        final Properties data = FileLoader.loadFileAsProperties(dir, PROPERTIES_BASE, false);
        Locale.setDefault(current);
        deleteFile(file);

        assertEquals(ADVICE, data.getProperty(PROPERTY_1_NAME), PROP_VALUE);
        assertEquals(QBF, data.getProperty(PROPERTY_2_NAME), PROP_VALUE);
    }

    /**
     * Test case.
     *
     * @throws IOException if write or read fails.
     */
    @Test
    @DisplayName("loadFileAsProperties(file, base, false) Spanish")
    void test022() throws IOException {

        final Path dirPath = Files.createTempDirectory(PREFIX);
        final File dir = dirPath.toFile();

        final File file = new File(dir, "test_es.properties");

        try (final FileOutputStream writer = new FileOutputStream(file)) {
            writer.write(("property1=" + SPADVICE).getBytes(StandardCharsets.UTF_8));
            writer.write(CoreConstants.CRLF.getBytes(StandardCharsets.UTF_8));
            writer.write(("property2=" + SPQBF).getBytes(StandardCharsets.UTF_8));
            writer.write(CoreConstants.CRLF.getBytes(StandardCharsets.UTF_8));
        }

        final Locale current = Locale.getDefault();
        Locale.setDefault(new Locale(LOCALE_SPAIN));

        Log.info(FileLoader.loadFileAsString(file, false));

        final Properties data = FileLoader.loadFileAsProperties(dir, PROPERTIES_BASE, false);
        Log.info(data.toString());

        Locale.setDefault(current);
        deleteFile(file);

        assertEquals(SPADVICE, data.getProperty(PROPERTY_1_NAME), SP_PROP_VALUE);
        assertEquals(SPQBF, data.getProperty(PROPERTY_2_NAME), SP_PROP_VALUE);
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsProperties(file, base, false) locale-driven English")
    void test023() {

        final Locale current = Locale.getDefault();
        Locale.setDefault(new Locale("en"));
        final Properties data = FileLoader.loadFileAsProperties(TestFileLoader.class, PROPERTIES_BASE,
                new Properties(), false);
        Locale.setDefault(current);

        assertEquals(ADVICE, data.getProperty(PROPERTY_1_NAME), CR_PROP_VALUE);
        assertEquals(QBF, data.getProperty(PROPERTY_2_NAME), CR_PROP_VALUE);
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsProperties(file, base, false) locale-driven Spanish")
    void test024() {

        final Locale current = Locale.getDefault();
        Locale.setDefault(new Locale(LOCALE_SPAIN));
        final Properties data = FileLoader.loadFileAsProperties(TestFileLoader.class, PROPERTIES_BASE,
                new Properties(), false);
        Locale.setDefault(current);

        Log.info("From property file: ", data.getProperty(PROPERTY_2_NAME));

        assertEquals(SPADVICE, data.getProperty(PROPERTY_1_NAME), SP_CR_PROP_VALUE);
        assertEquals(SPQBF, data.getProperty(PROPERTY_2_NAME), SP_CR_PROP_VALUE);
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsProperties(file, base, false) nonexistent path")
    void test025() {

        final File file = new File(System.getProperty("user.home"), "sadfsaerefasdfsad");
        final Properties data = FileLoader.loadFileAsProperties(file, BOGUS, false);

        assertNull(data, "loadFileAsProperties() nonexistent path");
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsProperties(class, file, base, false) nonexistent path")
    void test026() {

        final Properties def = new Properties();
        final Properties data = FileLoader.loadFileAsProperties(TestFileLoader.class, "Nonexistent", def, false);

        assertEquals(data, def, "loadFileAsProperties() nonexistent class-relative file with defaults");
    }

    /**
     * Test case.
     *
     * @throws IOException if write or read fails.
     */
    @Test
    @DisplayName("loadFileAsProperties(file, base, false) nonexistent file")
    void test027() throws IOException {

        final File temp = Files.createTempDirectory(PREFIX).toFile();
        final File file = new File(temp, "Bogus_en.properties");
        if (!file.mkdirs()) {
            Log.warning("Failed to create directory");
        }

        final Properties data = FileLoader.loadFileAsProperties(file.getParentFile(), BOGUS, false);

        assertNull(data, "loadFileAsProperties() nonexistent file");
    }

    /** Test case. */
    @Test
    @DisplayName("loadFileAsProperties(class, file, base, false) nonexistent file")
    void test028() {

        final Properties def = new Properties();
        final Properties data = FileLoader.loadFileAsProperties(TestFileLoader.class, "bogus", def, false);

        assertEquals(data, def, "loadFileAsProperties() invalid class-relative file with defaults");
    }

    /**
     * Deletes a file.
     *
     * @param file the file
     */
    private static void deleteFile(final File file) {

        if (file.exists() && !file.delete()) {
            Log.warning("Failed to delete file");
        }
    }

    /**
     * Creates a test image.
     *
     * @return the test image
     */
    private static BufferedImage makeTestImage() {

        final BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);

        final Graphics grx = img.getGraphics();
        grx.setColor(Color.WHITE);
        grx.fillRect(0, 0, 10, 10);
        grx.setColor(Color.RED);
        grx.drawLine(0, 0, 9, 9);
        grx.setColor(Color.BLUE);
        grx.drawLine(9, 0, 0, 9);
        grx.setColor(Color.MAGENTA);
        grx.drawRect(0, 0, 9, 9);

        return img;
    }
}

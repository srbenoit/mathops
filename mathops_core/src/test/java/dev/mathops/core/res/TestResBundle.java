package dev.mathops.core.res;

import dev.mathops.core.CoreConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;

/**
 * Tests for the ResBundle class.
 */
final class TestResBundle extends ResBundle {

    /** A frequently used string. */
    private static final String HELLO_STR = "Hello {0}";

    /** A frequently used string. */
    private static final String YES_STR = "Yes";

    /** A frequently used string. */
    private static final String HELLO_YES_STR = "Hello Yes";

    /** A frequently used string. */
    private static final String HOLA_STR = "Hola {0}";

    /** A frequently used string. */
    private static final String SI_STR = "Si";

    /** A frequently used string. */
    private static final String HOLA_SI_STR = "Hola Si";

    /** A frequently used string. */
    private static final String HALLO_STR = "Hallo {0}";

    /** A frequently used string. */
    private static final String JA_STR = "Ja";

    /** A frequently used string. */
    private static final String HALLO_JA_STR = "Hallo Ja";

    /** A frequently used string. */
    private static final String ENGLISH = "en";

    /** A frequently used string. */
    private static final String SPANISH = "es";

    /** A frequently used string. */
    private static final String US = "US";

    /** A frequently used string. */
    private static final String SPAIN = "SP";

    /** A frequently used string. */
    private static final String GERMAN = "de";

    /** A frequently used string. */
    private static final String VARIANT = "qux";

    /** A frequently used string. */
    private static final String FRENCH = "fr";

    /** A frequently used string. */
    private static final String LA = "la";

    /** A frequently used string. */
    private static final String A = "a";

    /** A frequently used string. */
    private static final String B = "b";

    /** A message key. */
    private static final String HELLO = key(0);

    /** A message key. */
    private static final String YES = key(1);

    /** A message key. */
    private static final String MISSING = key(2);

    /** A common string. */
    private static final String UNUSED = "unused";

    /** The resources - an array of key-values pairs. */
    private static final String[][] EN_US = {{HELLO, HELLO_STR}, {YES, YES_STR}};

    /** The resources - an array of key-values pairs. */
    private static final String[][] ES_ALL = {{HELLO, HOLA_STR}, {YES, SI_STR}};

    /** The resources - an array of key-values pairs. */
    private static final String[][] DE_ALL = {{HELLO, HALLO_STR}, {YES, JA_STR}};

    /**
     * Constructs a new {@code IvtResBundle}.
     */
    public TestResBundle() {

        super(Locale.US, EN_US);

        addMessages(new Locale(SPANISH), ES_ALL);
        addMessages(new Locale(GERMAN), DE_ALL);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Exception on missing Locale")
    void test001() {

        final String[][] junk = new String[1][];
        junk[0] = new String[]{A, B};

        assertThrows(IllegalArgumentException.class, ()->addMessages(null, junk), "No exception on missing Locale");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Exception on null message array")
    void test002() {

        assertThrows(IllegalArgumentException.class, ()->addMessages(new Locale(FRENCH), null),
                "No exception on null message arrays");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Exception on null message sub-array")
    void test003() {

        final String[][] junk = new String[1][];

        assertThrows(IllegalArgumentException.class, ()->addMessages(new Locale(FRENCH), junk),
                "No exception on null message sub-array");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Exception on message sub-array of length 3")
    void test004() {

        final String[][] junk = new String[1][];
        junk[0] = new String[3];

        assertThrows(IllegalArgumentException.class, ()->addMessages(new Locale(FRENCH), junk),
                "No exception on message sub-array of length 3");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Exception on null messages")
    void test005() {

        final String[][] junk = new String[1][];
        junk[0] = new String[2];

        assertThrows(IllegalArgumentException.class, () -> addMessages(new Locale(FRENCH), junk),
                "No exception on null messages");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Exception on null message 1")
    void test006() {

        final String[][] junk = new String[1][];
        junk[0] = new String[2];
        junk[0][0] = A;

        assertThrows(IllegalArgumentException.class, () -> addMessages(new Locale(FRENCH), junk),
                "No exception on null message 1");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("Exception on null message 0")
    void test007() {

        final String[][] junk = new String[1][];
        junk[0] = new String[2];
        junk[0][1] = A;

        assertThrows(IllegalArgumentException.class, () -> addMessages(new Locale(FRENCH), junk),
                "No exception on null message 0");
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("getMsg() for 'en', 'US', 'qux'")
    void test008() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(new Locale(ENGLISH, US, VARIANT));
        assertEquals(HELLO_STR, getMsg(HELLO), "GetEnUsQux Hello");
        assertEquals(YES_STR, getMsg(YES), "GetEnUsQux Yes");
        assertEquals(CoreConstants.EMPTY, getMsg(MISSING), "GetEnUsQux Empty");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("getMsg() for 'en', 'US'")
    void test009() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(Locale.US);
        assertEquals(HELLO_STR, getMsg(HELLO), "GetEnUs Hello");
        assertEquals(YES_STR, getMsg(YES), "GetEnUs Yes");
        assertEquals(CoreConstants.EMPTY, getMsg(MISSING), "GetEnUs Empty");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("getMsg() for 'en'")
    void test010() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        assertEquals(HELLO_STR, getMsg(HELLO), "GetEn Hello");
        assertEquals(YES_STR, getMsg(YES), "GetEn Yes");
        assertEquals(CoreConstants.EMPTY, getMsg(MISSING), "GetEn Empty");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("getMsg() for 'es', 'SP', 'qux'")
    void test011() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(new Locale(SPANISH, SPAIN, VARIANT));
        assertEquals(HOLA_STR, getMsg(HELLO), "GetEsSpQux Hello");
        assertEquals(SI_STR, getMsg(YES), "GetEsSpQux Yes");
        assertEquals(CoreConstants.EMPTY, getMsg(MISSING), "GetEsSpQux Empty");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("getMsg() for 'es', 'SP'")
    void test012() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(new Locale(SPANISH, SPAIN));
        assertEquals(HOLA_STR, getMsg(HELLO), "GetEsSp Hello");
        assertEquals(SI_STR, getMsg(YES), "GetEsSp Yes");
        assertEquals(CoreConstants.EMPTY, getMsg(MISSING), "GetEsSp Empty");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("getMsg() for 'es'")
    void test013() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(new Locale(SPANISH));
        assertEquals(HOLA_STR, getMsg(HELLO), "GetEs Hello");
        assertEquals(SI_STR, getMsg(YES), "GetEs Yes");
        assertEquals(CoreConstants.EMPTY, getMsg(MISSING), "GetEs Empty");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("getMsg() for 'de'")
    void test014() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(new Locale(GERMAN));
        assertEquals(HALLO_STR, getMsg(HELLO), "GetDe Hello");
        assertEquals(JA_STR, getMsg(YES), "GetDe Yes");
        assertEquals(CoreConstants.EMPTY, getMsg(MISSING), "GetDe Empty");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("getMsg() for 'la'")
    void test015() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(new Locale(LA));
        assertEquals(HELLO_STR, getMsg(HELLO), "GetLa Hello");
        assertEquals(YES_STR, getMsg(YES), "GetLa Yes");
        assertEquals(CoreConstants.EMPTY, getMsg(MISSING), "GetLa Empty");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("formatMsg() for 'en', 'US', 'qux'")
    void test016() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(new Locale(ENGLISH, US, VARIANT));
        assertEquals(HELLO_YES_STR, formatMsg(HELLO, YES_STR), "FmtEnUsQux Hello Yes");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("formatMsg() for 'en', 'US'")
    void test017() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(Locale.US);
        assertEquals(HELLO_YES_STR, formatMsg(HELLO, YES_STR), "FmtEnUs Hello Yes");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("formatMsg() for 'en'")
    void test018() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        assertEquals(HELLO_YES_STR, formatMsg(HELLO, YES_STR), "FmtEn Hello Yes");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("formatMsg() for 'es', 'SP', 'qux'")
    void test019() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(new Locale(SPANISH, SPAIN, VARIANT));
        assertEquals(HOLA_SI_STR, formatMsg(HELLO, SI_STR), "FmtEsSpQux Hola Si");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("formatMsg() for 'es', 'SP'")
    void test020() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(new Locale(SPANISH, SPAIN));
        assertEquals(HOLA_SI_STR, formatMsg(HELLO, SI_STR), "FmtEsSp Hola Si");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("formatMsg() for 'es'")
    void test021() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(new Locale(SPANISH));
        assertEquals(HOLA_SI_STR, formatMsg(HELLO, SI_STR), "FmtEs Hola Si");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("formatMsg() for 'de'")
    void test022() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(new Locale(GERMAN));
        assertEquals(HALLO_JA_STR, formatMsg(HELLO, JA_STR), "FmtDe Hallo Ja");
        Locale.setDefault(def);
    }

    /**
     * A test case.
     */
    @Test
    @DisplayName("formatMsg() for 'la'")
    void test023() {

        final Locale def = Locale.getDefault();
        Locale.setDefault(new Locale(LA));
        assertEquals(HELLO_YES_STR, formatMsg(HELLO, YES_STR), "FmtLa Hello Yes");
        Locale.setDefault(def);
    }
}

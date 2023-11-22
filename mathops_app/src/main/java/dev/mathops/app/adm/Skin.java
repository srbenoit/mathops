package dev.mathops.app.adm;

import dev.mathops.core.log.Log;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Fonts and colors.
 */
public enum Skin {
    ;

    /** A font for body text. */
    public static final Font BODY_12_FONT;

    /** A font for bold body text. */
    public static final Font BOLD_12_FONT;

    /** A font for buttons. */
    public static final Font BUTTON_13_FONT;

    /** A font for buttons. */
    public static final Font BUTTON_15_FONT;

    /** A font for buttons. */
    public static final Font BUTTON_BOLD_13_FONT;

    /** A mono-spaced font for tables. */
    public static final Font MONO_12_FONT;

    /** A mono-spaced font for tables. */
    public static final Font MONO_14_FONT;

    /** A mono-spaced font for tables. */
    public static final Font MONO_16_FONT;

    /** A font for medium headers. */
    public static final Font MEDIUM_HEADER_15_FONT;

    /** A font for medium headers. */
    public static final Font MEDIUM_HEADER_18_FONT;

    /** A plain font in the smaller than the medium header font. */
    public static final Font MEDIUM_13_FONT;

    /** A plain font in the same size as the medium header font. */
    public static final Font MEDIUM_15_FONT;

    /** A plain font in the same size as the medium header font. */
    public static final Font MEDIUM_18_FONT;

    /** A font for large headers. */
    public static final Font BIG_HEADER_18_FONT;

    /** A font for large headers. */
    public static final Font BIG_HEADER_22_FONT;

    /** A font for large headers. */
    public static final Font SUB_HEADER_16_FONT;

    /** A font for large buttons. */
    public static final Font BIG_BUTTON_16_FONT;

    /** A font for large buttons. */
    public static final Font BIG_BUTTON_20_FONT;

    /** A font for symbols. */
    public static final Font SYMBOL_16_ONT;

    /** A font for tabs. */
    /* default */ static final Font TAB_15_FONT;

    /** A color for labels. */
    public static final Color LABEL_COLOR = new Color(30, 30, 160);

    /** A color for labels. */
    public static final Color LABEL_COLOR2 = new Color(70, 70, 70);

    /** A color for labels. */
    public static final Color LABEL_COLOR3 = new Color(160, 0, 0);

    /** A color for error messages. */
    public static final Color ERROR_COLOR = new Color(180, 0, 0);

    /** A color for backgrounds (HSV = 120/34/95). */
    public static final Color LT_GREEN = new Color(160, 242, 160);

    /** A color for backgrounds (HSV = 120/12/98). */
    public static final Color LIGHTER_GREEN = new Color(220, 250, 220);

    /** A color for backgrounds (HSV = 120/5/100). */
    public static final Color OFF_WHITE_GREEN = new Color(243, 255, 243);

    /** A color for backgrounds. */
    public static final Color OFF_WHITE_RED = new Color(250, 240, 240);

    /** A color for backgrounds. */
    public static final Color OFF_WHITE_CYAN = new Color(230, 250, 250);

    /** A color for backgrounds. */
    public static final Color OFF_WHITE_MAGENTA = new Color(250, 240, 250);

    /** A color for backgrounds. */
    public static final Color OFF_WHITE_YELLOW = new Color(250, 250, 210);

    /** A color for backgrounds. */
    public static final Color OFF_WHITE_BLUE = new Color(240, 240, 250);

    /** A color for backgrounds. */
    public static final Color OFF_WHITE_LIME = new Color(242, 250, 235);

    /** A color for backgrounds. */
    public static final Color OFF_WHITE_BROWN = new Color(250, 243, 235);

    /** A color for backgrounds. */
    public static final Color OFF_WHITE_SEA = new Color(235, 250, 242);

    /** A color for backgrounds. */
    public static final Color OFF_WHITE_GRAY = new Color(235, 235, 235);

    /** A color for backgrounds. */
    /* default */ static final Color TABLE_ROW_HIGHLIGHT = new Color(244, 244, 244);

    /** A color for backgrounds. */
    /* default */ static final Color TABLE_HEADER_HIGHLIGHT = new Color(250, 250, 210);

    /** A color for backgrounds. */
    public static final Color WHITE = new Color(255, 255, 255);

    /** A color for backgrounds. */
    public static final Color LIGHTEST = new Color(244, 244, 240);

    /** A color for backgrounds. */
    public static final Color LIGHT = new Color(233, 233, 230);

    /** A color for backgrounds. */
    public static final Color LT_RED = new Color(243, 160, 160);

    /** A color for backgrounds. */
    public static final Color LIGHTER_RED = new Color(0xFF, 0xDD, 0xDD);

    /** A color for backgrounds. */
    public static final Color LT_CYAN = new Color(160, 243, 243);

    /** A color for backgrounds. */
    public static final Color LIGHTER_CYAN = new Color(0xCC, 0xFF, 0xFF);

    /** A color for backgrounds. */
    public static final Color LT_MAGENTA = new Color(243, 160, 243);

    /** A color for backgrounds. */
    public static final Color LIGHTER_MAGENTA = new Color(0xDD, 0xFF, 0xDD);

    /** A color for backgrounds. */
    public static final Color LT_YELLOW = new Color(243, 243, 120);

    /** A color for backgrounds. */
    public static final Color LT_LIME = new Color(182, 243, 120);

    /** A color for backgrounds. */
    public static final Color LT_BROWN = new Color(243, 181, 120);

    /** A color for backgrounds. */
    public static final Color LT_SEA = new Color(120, 243, 182);

    /** A color for backgrounds. */
    public static final Color LIGHTER_YELLOW = new Color(0xFF, 0xFF, 0xE0);

    /** A color for backgrounds. */
    public static final Color LT_BLUE = new Color(160, 160, 243);

    /** A color for backgrounds. */
    public static final Color LIGHTER_GRAY = new Color(0xEE, 0xEE, 0xEE);

    /** A color for backgrounds. */
    public static final Color MEDIUM = new Color(192, 192, 197);

    /** A color for backgrounds. */
    public static final Color DARK = new Color(128, 128, 128);

    /** A color for backgrounds. */
    public static final Color BLACK = Color.BLACK;

    /** Background color for fields with no error. */
    public static final Color FIELD_BG = Color.WHITE;

    /** Background color for fields with error. */
    public static final Color FIELD_ERROR_BG = new Color(255, 220, 200);

    static {

        Font regFont;

        try (final InputStream in = Skin.class.getResourceAsStream("OpenSans-Regular.ttf")) {
            regFont = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (final IOException | FontFormatException ex) {
            Log.warning(ex);
            regFont = new Font(Font.SANS_SERIF, Font.PLAIN, 1);
        }

        Font boldFont;

        try (final InputStream in = Skin.class.getResourceAsStream("OpenSans-Semibold.ttf")) {
            boldFont = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (final IOException | FontFormatException ex) {
            Log.warning(ex);
            boldFont = new Font(Font.SANS_SERIF, Font.BOLD, 1);
        }

        Font monoFont;

        try (final InputStream in = Skin.class.getResourceAsStream("JetBrainsMono-Light.ttf")) {
            monoFont = Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (final IOException | FontFormatException ex) {
            Log.warning(ex);
            monoFont = new Font(Font.MONOSPACED, Font.PLAIN, 1);
        }

        BODY_12_FONT = regFont.deriveFont(12.0f);
        BOLD_12_FONT = boldFont.deriveFont(12.0f);
        BUTTON_13_FONT = regFont.deriveFont(13.0f);
        BUTTON_15_FONT = regFont.deriveFont(15.0f);
        BUTTON_BOLD_13_FONT = boldFont.deriveFont(13.0f);
        MONO_12_FONT = monoFont.deriveFont(12.0f);
        MONO_14_FONT = monoFont.deriveFont(14.0f);
        MONO_16_FONT = monoFont.deriveFont(16.0f);
        MEDIUM_HEADER_15_FONT = boldFont.deriveFont(15.0f);
        MEDIUM_HEADER_18_FONT = boldFont.deriveFont(18.0f);
        MEDIUM_13_FONT = regFont.deriveFont(13.0f);
        MEDIUM_15_FONT = regFont.deriveFont(15.0f);
        MEDIUM_18_FONT = regFont.deriveFont(18.0f);
        BIG_HEADER_18_FONT = boldFont.deriveFont(18.0f);
        BIG_HEADER_22_FONT = boldFont.deriveFont(22.0f);
        SUB_HEADER_16_FONT = boldFont.deriveFont(16.0f);
        BIG_BUTTON_16_FONT = regFont.deriveFont(16.0f);
        BIG_BUTTON_20_FONT = regFont.deriveFont(20.0f);
        SYMBOL_16_ONT = new Font(Font.SANS_SERIF, Font.PLAIN, 16);
        TAB_15_FONT = MEDIUM_HEADER_15_FONT;
    }
}

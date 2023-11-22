package dev.mathops.assessment.formula.edit;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads a Serif font and a Sans font and creates a 1-point font object for each.
 */
public final class Fonts {

    /** The single instance. */
    private static Fonts INSTANCE;

    /** The one-point version of Serif. */
    private Font onePointSerif;

    /** The one-point version of Sans. */
    private Font onePointSans;

    /** Graphics object used to create font metrics. */
    private final Graphics2D g2d;

    /** Map from point size to sized Serif fonts. */
    private final Map<Integer, Font> serifSized;

    /** Map from point size to font Serif metrics. */
    private final Map<Integer, FontMetrics> serifSizedMetrics;

    /** Map from point size to sized Sans-serif fonts. */
    private final Map<Integer, Font> sansSized;

    /** Map from point size to font Sans-serif metrics. */
    private final Map<Integer, FontMetrics> sansMetrics;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private Fonts() {

        final BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        this.g2d = img.createGraphics();

        this.serifSized = new HashMap<>(6);
        this.serifSizedMetrics = new HashMap<>(6);

        this.sansSized = new HashMap<>(6);
        this.sansMetrics = new HashMap<>(6);
    }

    /**
     * Gets the single instance, creating it it if has not yet been created.
     *
     * @return the instance
     */
    public static Fonts getInstance() {

        synchronized (CoreConstants.INSTANCE_SYNCH) {

            if (INSTANCE == null) {
                INSTANCE = new Fonts();
            }

            return INSTANCE;
        }
    }

    /**
     * Returns a one-point instance of the STIX font.
     *
     * @return the one-point instance; null if unable to load
     */
    private Font loadOnePointStix() {

        synchronized (this) {
            if (this.onePointSerif == null) {
                final byte[] data = FileLoader.loadFileAsBytes(getClass(), //
                        "STIX2Math.otf", true);
                if (data != null) {
                    final ByteArrayInputStream fontStream = new ByteArrayInputStream(data);
                    try {
                        this.onePointSerif = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                    } catch (final FontFormatException ex) {
                        Log.warning("Unable to interpret font file contents.", ex);
                    } catch (final IOException ex) {
                        Log.warning("Unable to read font file contents.", ex);
                    }
                }
            }

            return this.onePointSerif;
        }
    }

    /**
     * Returns a one-point instance of the OpenSans font.
     *
     * @return the one-point instance; null if unable to load
     */
    private Font loadOnePointOpenSans() {

        synchronized (this) {
            if (this.onePointSans == null) {
                final byte[] data = FileLoader.loadFileAsBytes(getClass(), //
                        "RobotoCondensed-Regular.otf", true);
                if (data != null) {
                    final ByteArrayInputStream fontStream = new ByteArrayInputStream(data);
                    try {
                        this.onePointSans = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                    } catch (final FontFormatException ex) {
                        Log.warning("Unable to interpret font file contents.", ex);
                    } catch (final IOException ex) {
                        Log.warning("Unable to read font file contents.", ex);
                    }
                }
            }

            return this.onePointSans;
        }
    }

    /**
     * Gets a sized version of the Stix font.
     *
     * @param pointSize the point size
     * @return the font
     */
    public Font getSizedStix(final int pointSize) {

        final Integer key = Integer.valueOf(pointSize);

        synchronized (this) {
            Font result = this.serifSized.get(key);

            if (result == null) {
                final Font onePt = loadOnePointStix();
                if (onePt != null) {
                    result = onePt.deriveFont((float) pointSize);
                    this.g2d.setFont(result);
                    this.serifSized.put(key, result);
                    this.serifSizedMetrics.put(key, this.g2d.getFontMetrics());
                }
            }

            return result;
        }
    }

    /**
     * Gets font metrics corresponding to a sized version of the Stix font.
     *
     * @param pointSize the point size
     * @return the font metrics
     */
    public FontMetrics getSizedStixMetrics(final int pointSize) {

        final Integer key = Integer.valueOf(pointSize);

        synchronized (this) {
            FontMetrics result = this.serifSizedMetrics.get(key);

            if (result == null) {
                final Font onePt = loadOnePointStix();
                if (onePt != null) {
                    final Font f = onePt.deriveFont((float) pointSize);
                    this.g2d.setFont(f);
                    this.serifSized.put(key, f);
                    result = this.g2d.getFontMetrics();
                    this.serifSizedMetrics.put(key, result);
                }
            }

            return result;
        }
    }

    /**
     * Gets a sized version of the OpenSans font.
     *
     * @param pointSize the point size
     * @return the font
     */
    public Font getSizedOpenSans(final int pointSize) {

        final Integer key = Integer.valueOf(pointSize);

        synchronized (this) {
            Font result = this.sansSized.get(key);

            if (result == null) {
                final Font onePt = loadOnePointOpenSans();
                if (onePt != null) {
                    result = onePt.deriveFont((float) pointSize);
                    this.g2d.setFont(result);
                    this.sansSized.put(key, result);
                    this.sansMetrics.put(key, this.g2d.getFontMetrics());
                }
            }

            return result;
        }
    }

    /**
     * Gets font metrics corresponding to a sized version of the OpenSans font.
     *
     * @param pointSize the point size
     * @return the font metrics
     */
    public FontMetrics getSizedOpenSansMetrics(final int pointSize) {

        final Integer key = Integer.valueOf(pointSize);

        synchronized (this) {
            FontMetrics result = this.sansMetrics.get(key);

            if (result == null) {
                final Font onePt = loadOnePointStix();
                if (onePt != null) {
                    final Font f = onePt.deriveFont((float) pointSize);
                    this.g2d.setFont(f);
                    this.sansSized.put(key, f);
                    result = this.g2d.getFontMetrics();
                    this.sansMetrics.put(key, result);
                }
            }

            return result;
        }
    }

    /**
     * Gets the font metrics.
     *
     * @param font the font
     * @return the metrics
     */
    public FontMetrics getMetrics(final Font font) {

        synchronized (this) {
            this.g2d.setFont(font);
            return this.g2d.getFontMetrics();
        }
    }
}

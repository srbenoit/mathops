package dev.mathops.core.ui;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.Base64;
import dev.mathops.core.parser.xml.XmlEscaper;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * A rendered image of a TeX object, which includes the image itself and the vertical offset of the bottom of the image
 * from the baseline of surrounding text. This object can produce a PNG formatted representation of the image and the
 * HTML &lt;img&gt; tag to display the image,
 */
public final class HtmlImage {

    /** The image. */
    private final BufferedImage img;

    /**
     * The vertical offset of the bottom of the image from the surrounding text baseline (negative numbers indicate the
     * image is set below the baseline of surrounding text).
     */
    private final double vOffset;

    /** The point size at which the object is rendered. */
    private final double pointSize;

    /** The cached PNG file representation of the image. */
    private byte[] png;

    /**
     * Constructs a new {@code HtmlImage}.
     *
     * @param theImg       the image
     * @param theVOffset   the vertical offset of the bottom of the image from the surrounding text baseline (negative
     *                     numbers indicate the image is set below the baseline of surrounding text)
     * @param thePointSize the point size at which the image is rendered
     */
    public HtmlImage(final BufferedImage theImg, final double theVOffset, final double thePointSize) {

        this.img = theImg;
        this.vOffset = theVOffset;
        this.pointSize = thePointSize;
        this.png = null;
    }

    /**
     * Generates a PNG file of the image.
     *
     * @return the PNG file contents, {@code null} if the PNG could not be generated
     */
    private byte[] toPng() {

        if (this.png == null) {
            final Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("png");

            if (iter.hasNext()) {
                final ImageWriter writer = iter.next();

                try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                     final Closeable stream = new MemoryCacheImageOutputStream(baos)) {

                    writer.setOutput(stream);
                    writer.write(this.img);
                    this.png = baos.toByteArray();
                } catch (final IOException ex) {
                    Log.warning(ex);
                }
            }
        }

        return (this.png == null) ? null : this.png.clone();
    }

    /**
     * Generates an inline &lt;img&gt; tag for the image, including the proper vertical offset to align it with
     * surrounding text, and including the specified alternate text.
     *
     * @param alt       the alternate text
     * @param overScale the scale by which the backing image is larger than one pixel per point
     * @return the inline image tag
     */
    public String toImg(final String alt, final double overScale) {

        final HtmlBuilder xml = new HtmlBuilder(2000);
        final byte[] base64 = Base64.encode(toPng()).getBytes(StandardCharsets.UTF_8);

        final double scale = 1.0 / this.pointSize / overScale;

        xml.add("<img style='vertical-align:baseline;margin-bottom:",
                Double.toString(this.vOffset / this.pointSize), "em;width:",
                Double.toString((double) this.img.getWidth() * scale), "em;height:",
                Double.toString((double) this.img.getHeight() * scale), "em;' src='data:image/png;base64,",
                new String(base64, StandardCharsets.UTF_8));

        if (alt != null) {
            xml.add("' alt='", XmlEscaper.escape(alt), "' title='", XmlEscaper.escape(alt));
        }

        xml.add("'/>");

        return xml.toString();
    }
}

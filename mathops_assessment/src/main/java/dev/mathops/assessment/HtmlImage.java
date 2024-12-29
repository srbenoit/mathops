package dev.mathops.assessment;

import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.Base64;
import dev.mathops.text.parser.xml.XmlEscaper;

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
    private byte[] png = null;

    /** Alt text. */
    private final String alt;

    /**
     * Constructs a new {@code HtmlImage}.
     *
     * @param theImg       the image
     * @param theVOffset   the vertical offset of the bottom of the image from the surrounding text baseline (negative
     *                     numbers indicate the image is set below the baseline of surrounding text)
     * @param thePointSize the point size at which the image is rendered
     * @param theAlt       the alt text
     */
    public HtmlImage(final BufferedImage theImg, final double theVOffset, final double thePointSize,
                     final String theAlt) {

        this.img = theImg;
        this.vOffset = theVOffset;
        this.pointSize = thePointSize;
        this.alt = theAlt;
    }

    /**
     * Generates a PNG file of the image.
     *
     * @return the PNG file contents, {@code null} if the PNG could not be generated
     */
    private byte[] toPng() {

        if (this.png == null) {
            final Iterator<ImageWriter> iterator = ImageIO.getImageWritersByFormatName("png");

            if (iterator.hasNext()) {
                final ImageWriter writer = iterator.next();

                try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
                     final Closeable stream = new MemoryCacheImageOutputStream(out)) {

                    writer.setOutput(stream);
                    writer.write(this.img);
                    this.png = out.toByteArray();
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
     * @param overScale the scale by which the backing image is larger than one pixel per point
     * @return the inline image tag
     */
    public String toImg(final double overScale) {

        final HtmlBuilder xml = new HtmlBuilder(2000);
        final byte[] data = toPng();
        final byte[] base64 = Base64.encode(data).getBytes(StandardCharsets.UTF_8);

        final double scale = 1.0 / this.pointSize / overScale;

        final String marginBottomStr = Double.toString(this.vOffset / this.pointSize);

        final int imgWidth = this.img.getWidth();
        final int imgHeight = this.img.getHeight();
        final String widthStr = Double.toString((double) imgWidth * scale);
        final String heightStr = Double.toString((double) imgHeight * scale);
        xml.add("<img style='vertical-align:baseline;margin-bottom:", marginBottomStr, "em;width:", widthStr,
                "em;height:", heightStr, "em;' src='data:image/png;base64,",
                new String(base64, StandardCharsets.UTF_8));

        if (this.alt != null) {
            final String escaped = XmlEscaper.escape(this.alt);
            xml.add("' alt='", escaped, "' title='", escaped);
        }

        xml.add("'/>");

        return xml.toString();
    }
}

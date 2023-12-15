package dev.mathops.assessment.document.template;

import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.ELayoutMode;
import dev.mathops.assessment.document.EPrimaryBaseline;
import dev.mathops.assessment.document.inst.DocImageInst;
import dev.mathops.assessment.document.inst.DocObjectInstStyle;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.xml.XmlEscaper;
import dev.mathops.core.ui.ColorNames;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serial;
import java.net.URL;
import java.util.Objects;
import java.util.Set;

/**
 * An image in a document.
 */
public final class DocImage extends AbstractDocObjectTemplate implements ImageObserver {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4380476680775193342L;

    /** The text contents of the object. */
    private BufferedImage image;

    /** The scaled width. */
    private NumberOrFormula scaledWidth;

    /** The scaled height. */
    private NumberOrFormula scaledHeight;

    /** The URL from which to load the image. */
    private URL source;

    /** The alt text. */
    private final String altText;

    /**
     * Construct a new {@code DocImage} object.
     *
     * @param theAltText the alt text
     */
    DocImage(final String theAltText) {

        super();

        this.altText = theAltText;
    }

    /**
     * Gets the alt text.
     *
     * @return the alt text
     */
    public String getAltText() {

        return this.altText;
    }

    /**
     * Serialize the object, which writes the {@code BufferedImage} as a PNG stream.
     *
     * @param out the output stream to which to write
     * @throws IOException if there is an error writing to the stream
     */
    @Serial
    private void writeObject(final ObjectOutputStream out) throws IOException {

        out.defaultWriteObject();

        ImageIO.write(this.image, "png", out); // png is lossless
    }

    /**
     * Deserialize the object, which creates the {@code BufferedImage}.
     *
     * @param in the input stream from which to read
     * @throws IOException            if there is an error reading from the stream
     * @throws ClassNotFoundException if the default deserialization finds an invalid class
     */
    @Serial
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {

        in.defaultReadObject();

        this.image = ImageIO.read(in);
    }

    /**
     * Perform a deep clone of the object, cloning all contained sub-objects.
     *
     * @return the cloned object
     */
    @Override
    public DocImage deepCopy() {

        final DocImage copy = new DocImage(this.altText);

        copy.copyObjectFrom(this);

        if (this.scaledWidth != null) {
            copy.scaledWidth = this.scaledWidth.deepCopy();
        }

        if (this.scaledHeight != null) {
            copy.scaledHeight = this.scaledHeight.deepCopy();
        }

        copy.source = this.source;
        copy.image = this.image;

        return copy;
    }

    /**
     * Sets the scaled width.
     *
     * @param theScaledWidth the scaled width
     */
    void setScaledWidth(final NumberOrFormula theScaledWidth) {

        this.scaledWidth = theScaledWidth;
    }

//    /**
//     * Gets the scaled width.
//     *
//     * @return the scaled width
//     */
//    public NumberOrFormula getScaledWidth() {
//
//        return this.scaledWidth;
//    }

    /**
     * Sets the scaled height.
     *
     * @param theScaledHeight the scaled height
     */
    void setScaledHeight(final NumberOrFormula theScaledHeight) {

        this.scaledHeight = theScaledHeight;
    }

//    /**
//     * Gets the scaled height.
//     *
//     * @return the scaled height
//     */
//    public NumberOrFormula getScaledHeight() {
//
//        return this.scaledHeight;
//    }

    /**
     * Get the source {@code URL} for the image.
     *
     * @return the source {@code URL}
     */
    public URL getSource() {

        return this.source;
    }

    /**
     * Set the source {@code URL}, reloading the image.
     *
     * @param theSource the new source {@code URL}
     */
    public void setSource(final URL theSource) {

        this.source = theSource;
        loadImage();
    }

    /**
     * Get the image.
     *
     * @return the image
     */
    public BufferedImage getImage() {

        return this.image;
    }

    /**
     * Load the image from the source file.
     */
    private void loadImage() {

        // Flush the existing image if it exists
        if (this.image != null) {
            this.image.flush();
            this.image = null;
        }

        if (this.source != null) {
            try {
                this.image = ImageIO.read(this.source);
            } catch (final IOException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Get the left alignment for the object.
     *
     * @return the object insets
     */
    @Override
    public int getLeftAlign() {

        return BASELINE;
    }

    /**
     * Recompute the size of the bounding box of the object, based on the current image size.
     *
     * @param context  the evaluation context
     * @param mathMode text mode, inline-math mode, or display-math mode
     */
    @Override
    public void doLayout(final EvalContext context, final ELayoutMode mathMode) {

        int w = 0;
        int h = 0;

        final Object scaledWidthValue = this.scaledWidth == null ? null : this.scaledWidth.evaluate(context);
        final Object scaledHeightValue = this.scaledHeight == null ? null : this.scaledHeight.evaluate(context);

        final Number scaledWidthNumber = scaledWidthValue instanceof final Number swn ? swn : null;
        final Number scaledHeightNumber = scaledHeightValue instanceof final Number shn ? shn : null;

        // If neither scaledWidth nor scaledHeight are present, we rely on the image's natural size
        if (scaledWidthNumber == null) {
            if (scaledHeightNumber == null) {
                if (this.image != null) {
                    w = (int) ((float) this.image.getWidth() * getScale());
                    h = (int) ((float) this.image.getHeight() * getScale());
                }
            } else {
                // We have a scaled height but no scaled width - we want to preserve the image's
                // aspect ratio.
                if (this.image == null) {
                    // No image, placeholder will be square
                    w = (int) (scaledHeightNumber.floatValue() * getScale());
                    h = w;
                } else {
                    final float factor = scaledHeightNumber.floatValue() / (float) this.image.getHeight();
                    w = (int) ((float) this.image.getWidth() * factor * getScale());
                    h = (int) ((float) this.image.getHeight() * factor * getScale());
                }
            }
        } else if (scaledHeightNumber == null) {
            // We have a scaled width but no scaled height - we want to preserve the image's
            // aspect ratio.
            if (this.image == null) {
                // No image, placeholder will be square
                w = (int) (scaledWidthNumber.floatValue() * getScale());
                h = w;
            } else {
                final float factor = scaledWidthNumber.floatValue() / (float) this.image.getWidth();
                w = (int) ((float) this.image.getWidth() * factor * getScale());
                h = (int) ((float) this.image.getHeight() * factor * getScale());
            }
        } else {
            // We have both a scaled height and scaled width - honor those
            w = (int) (scaledWidthNumber.floatValue() * getScale());
            h = (int) (scaledHeightNumber.floatValue() * getScale());
        }

        setBaseLine(h);
        setCenterLine(getBaseLine() / 2);

        setWidth(w);
        setHeight(h);
    }

    /**
     * Draw the image.
     *
     * @param grx the {@code Graphics} object to which to draw the image
     */
    @Override
    public void paintComponent(final Graphics grx, final ELayoutMode mathMode) {

        prePaint(grx);

        innerPaintComponent(grx);

        final int w = getWidth();
        final int h = getHeight();

        if (w > 0 && h > 0) {
            if (this.image == null) {
                // Draw a placeholder
                grx.setColor(ColorNames.getColor("gray90"));
                grx.fillRect(0, 0, w, h);
                grx.setColor(Color.BLACK);
                grx.drawLine(0, 0, w, h);
                grx.drawLine(w, 0, 0, h);
                grx.drawRect(0, 0, w - 1, h - 1);
            } else {
                grx.drawImage(this.image, 0, 0, w, h, 0, 0, this.image.getWidth(), this.image.getHeight(), null);
            }
        }

        postPaint(grx);
    }

    /**
     * Implementation of the {@code ImageObserver} interface to allow image drawing.
     *
     * @param img       the image
     * @param infoflags flags indicating information now available
     * @param x      x position
     * @param y      y position
     * @param width     image width
     * @param height    image height
     * @return false if all needed data has arrived
     */
    @Override
    public boolean imageUpdate(final Image img, final int infoflags, final int x, final int y, final int width,
                               final int height) {

        return false;
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public void accumulateParameterNames(final Set<String> set) {

        // No action
    }

    /**
     * Generate a {@code String} representation of the paragraph (for diagnostic purposes).
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return "Image";
    }

    /**
     * Generates an instance of this document object based on a realized evaluation context.
     *
     * <p>
     * All variable references are replaced with their values from the context. Formulas may remain that depend on input
     * variables, but no references to non-input variables should remain.
     *
     * @param evalContext the evaluation context
     * @return the instance document object; null if unable to create the instance
     */
    @Override
    public DocImageInst createInstance(final EvalContext evalContext) {

        final DocObjectInstStyle objStyle = new DocObjectInstStyle(getColorName(), getFontName(), (float)getFontSize(),
                getFontStyle());

        final String alt = getAltText();
        final String actualAlt = alt == null ? null : generateStringContents(evalContext, alt);

        return new DocImageInst(objStyle, null, this.source.toExternalForm(), (double) getWidth(), (double) getHeight(),
                EPrimaryBaseline.TYPOGRAPHIC, actualAlt);
    }

    /**
     * Write the XML representation of the object to a {@code HtmlBuilder}.
     *
     * @param xml    the {@code HtmlBuilder} to which to write the XML
     * @param indent the number of spaces to indent the printout
     */
    @Override
    public void toXml(final HtmlBuilder xml, final int indent) {

        xml.add("<image");

        if (this.scaledWidth != null && this.scaledWidth.getNumber() != null) {
            xml.add(" width=\"", this.scaledWidth.getNumber(), CoreConstants.QUOTE);
        }

        if (this.scaledHeight != null && this.scaledHeight.getNumber() != null) {
            xml.add(" height=\"", this.scaledHeight.getNumber(), CoreConstants.QUOTE);
        }
        if (this.source != null) {
            final String src = this.source.toExternalForm();
            xml.add(" src=\"", XmlEscaper.escape(src), CoreConstants.QUOTE);
        }

        final String alt = getAltText();
        if (alt != null) {
            xml.add(" alt='", XmlEscaper.escape(alt), "'");
        }

        if ((this.scaledWidth == null || this.scaledWidth.getFormula() == null) //
                && (this.scaledHeight == null || this.scaledHeight.getFormula() == null)) {
            xml.add("/>");
        } else {
            xml.add(">");

            if (this.scaledWidth != null && this.scaledWidth.getFormula() != null) {
                xml.add("<width>");
                this.scaledWidth.getFormula().appendChildrenXml(xml);
                xml.add("</width>");
            }

            if (this.scaledHeight != null && this.scaledHeight.getFormula() != null) {
                xml.add("<height>");
                this.scaledHeight.getFormula().appendChildrenXml(xml);
                xml.add("</height>");
            }

            xml.add("</image>");
        }
    }

    /**
     * Write the LaTeX representation of the object to a string buffer.
     *
     * @param dir          the directory in which the LaTeX source files are being written
     * @param fileIndex    a 1-integer array containing an index used to uniquely name files to be included by the LaTeX
     *                     file; the value should be updated if the method writes any files
     * @param overwriteAll a 1-boolean array whose only entry contains True if the user has selected "overwrite all";
     *                     false to ask the user each time (this method can update this value to true if it is false and
     *                     the user is asked "Overwrite? [YES] [ALL] [NO]" and chooses [ALL])
     * @param builder      the {@code HtmlBuilder} to which to write the LaTeX
     * @param showAnswers  true to show answers in any inputs embedded in the document; false if answers should not be
     *                     shown
     * @param mode         the current LaTeX mode (T=text, $=in-line math, M=math)
     */
    @Override
    public void toLaTeX(final File dir, final int[] fileIndex,
                        final boolean[] overwriteAll, final HtmlBuilder builder, final boolean showAnswers,
                        final char[] mode, final EvalContext context) {

        // TODO: Download image and write to a file in the "dir" directory, obtaining its proper
        // size in the process, and if that succeeds, increment the newFileIndex value.
    }

    /**
     * Print the contained object tree in HTML format, as a set of nested unordered list tags.
     *
     * @param ps the {@code PrintStream} to which to print the tree
     */
    @Override
    public void printTree(final PrintStream ps) {

        ps.print("<li>Image");

        if (this.source != null) {
            ps.print(" (source = ");
            ps.print(this.source);
            ps.println(')');
        }

        ps.print("</li>");
    }

    /**
     * Implementation of {@code hashCode}.
     *
     * @return the hash code of the object
     */
    @Override
    public int hashCode() {

        return docObjectHashCode() + Objects.hashCode(this.source) + Objects.hashCode(this.altText);
    }

    /**
     * Implementation of {@code equals} to compare two {@code DocObject} objects for equality.
     *
     * @param obj the object to be compared to this object
     * @return {@code true} if the objects are equal; {@code false} otherwise
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj == this) {
            equal = true;
        } else if (obj instanceof final DocImage img) {
            equal = docObjectEquals(img) && Objects.equals(this.source, img.source)
                    && Objects.equals(this.altText, img.altText);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Logs messages to indicate why this object is not equal to another.
     *
     * @param other  the other object
     * @param indent the indent level
     */
    @Override
    public void whyNotEqual(final Object other, final int indent) {

        if (other instanceof final DocImage obj) {
            docObjectWhyNotEqual(obj, indent);

            if (!Objects.equals(this.source, obj.source)) {
                Log.info(makeIndent(indent), "UNEQUAL DocImage (source: " + this.source + "!=" + obj.source + ")");
            }
            if (!Objects.equals(this.altText, obj.altText)) {
                Log.info(makeIndent(indent), "UNEQUAL DocImage (altText: " + this.altText + "!=" + obj.altText + ")");
            }
        } else {
            Log.info(makeIndent(indent), "UNEQUAL DocImage because other is ", other.getClass().getName());
        }
    }
}

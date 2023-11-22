package dev.mathops.assessment.document;

import java.awt.Font;
import java.io.Serial;
import java.io.Serializable;

/**
 * A container for color and font style information.
 */
public final class DocObjectStyle implements Serializable {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -3515538439980675871L;

    /** The foreground color name to use for any text using this format. */
    public String colorName;

    /** The name of the font to use (default is SERIF). */
    public String fontName;

    /** The point size of font to use. */
    public float fontSize;

    /** A scaling factor to apply to the default font size. */
    public float fontScale = 1.0f;

    /** The font style. */
    public Integer fontStyle;

    /** A cached font to use when rendering. */
    public Font font;

    /**
     * Construct a new {@code DocObjectStyle}.
     */
    public DocObjectStyle() {

        // No action
    }
}

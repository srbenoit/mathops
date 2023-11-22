package dev.mathops.app.ops.snapin;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.Serial;

/**
 * The base class for a thumbnail panel.
 */
public class AbstractThumbnailButton extends JButton {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -4491554939110553148L;

    /** The font. */
    protected static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 12);

    /** A black border. */
    private final MatteBorder blackBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.LIGHT_GRAY);

    /** A yellow border. */
    private final MatteBorder highlightedBorder = BorderFactory.createMatteBorder(2, 2, 2, 2, new Color(100, 100, 130));

    /**
     * Constructs a new {@code AbstractThumbnailPanel}.
     */
    public AbstractThumbnailButton() {

        super();

        setPreferredSize(new Dimension(80, 80));
        setBorder(this.blackBorder);
    }

    /**
     * Sets the preferred size of this component.
     *
     * @param preferredSize the preferred size
     */
    public final void setPreferredSize(final Dimension preferredSize) {

        super.setPreferredSize(preferredSize);
    }
    /**
     * Sets the border of this component.
     *
     * @param border the border
     */
    public final void setBorder(final Border border) {

        super.setBorder(border);
    }

    /**
     * Sets the "highlighted" state of the button.
     *
     * @param highlighted true to highlight; false if not
     */
    public final void setHighlighted(final boolean highlighted) {

        setBorder(highlighted ? this.highlightedBorder : this.blackBorder);
    }
}

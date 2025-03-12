package dev.mathops.app.adm;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import java.awt.LayoutManager;
import java.io.Serial;

/**
 * The base class for panels under the "Admin" outer tab.
 */
public class AdmPanelBase extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1468620848130541459L;

    /**
     * Constructs a new {@code AdmPanelBase}.
     */
    protected AdmPanelBase() {

        super(new StackedBorderLayout(5, 5));

        setBackground(Skin.OFF_WHITE_GREEN);

        final Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        final Border etchedBorder = BorderFactory.createEtchedBorder();
        final CompoundBorder border = BorderFactory.createCompoundBorder(etchedBorder, padding);
        setBorder(border);
    }

    /**
     * Creates a header label.
     *
     * @param str         the label string
     * @param borderAbove true to include a border with extra space above
     * @return the label
     */
    public static JLabel makeHeader(final String str, final boolean borderAbove) {

        final JLabel result = new JLabel(str);

        if (borderAbove) {
            final Border topBottomPad = BorderFactory.createEmptyBorder(5, 0, 5, 0);
            final MatteBorder overline = BorderFactory.createMatteBorder(1, 0, 0, 0, Skin.MEDIUM);
            final CompoundBorder border = BorderFactory.createCompoundBorder(overline, topBottomPad);
            result.setBorder(border);
        } else {
            final Border bottomPad = BorderFactory.createEmptyBorder(0, 0, 5, 0);
            result.setBorder(bottomPad);
        }

        result.setFont(Skin.BIG_HEADER_22_FONT);
        result.setForeground(Skin.LABEL_COLOR);

        return result;
    }

    /**
     * Creates a label.
     *
     * @param str the label string
     * @return the label
     */
    protected static JLabel makeLabel(final String str) {

        final JLabel result = new JLabel(str);

        result.setFont(Skin.BODY_12_FONT);
        result.setForeground(Skin.LABEL_COLOR);

        return result;
    }

    /**
     * Creates a label.
     *
     * @param str the label string
     * @return the label
     */
    public static JLabel makeLabelMedium(final String str) {

        final JLabel result = new JLabel(str);

        result.setFont(Skin.MEDIUM_15_FONT);
        result.setForeground(Skin.LABEL_COLOR);

        return result;
    }

    /**
     * Creates a label.
     *
     * @param str the label string
     * @return the label
     */
    public static JLabel makeLabelMedium2(final String str) {

        final JLabel result = new JLabel(str);

        result.setFont(Skin.MEDIUM_18_FONT);
        result.setForeground(Skin.LABEL_COLOR);

        return result;
    }

    /**
     * Creates a label with boldface font.
     *
     * @param str the label string
     * @return the label
     */
    protected static JLabel makeBoldLabel(final String str) {

        final JLabel result = new JLabel(str);

        result.setFont(Skin.BOLD_12_FONT);
        result.setForeground(Skin.LABEL_COLOR);

        return result;
    }

    /**
     * Creates a non-editable text field.
     *
     * @param len the length (number of characters)
     * @return the text field
     */
    protected static JTextField makeTextField(final int len) {

        final JTextField result = new JTextField(len);

        result.setEditable(false);
        result.setFont(Skin.BODY_12_FONT);

        return result;
    }

    /**
     * Creates a non-editable text field.
     *
     * @param len the length (number of characters)
     * @return the text field
     */
    public static JTextField makeTextFieldMedium(final int len) {

        final JTextField result = new JTextField(len);

        result.setEditable(false);
        result.setFont(Skin.MEDIUM_15_FONT);

        return result;
    }

    /**
     * Creates a non-editable error message field.
     *
     * @return the text field
     */
    protected static JLabel makeError() {

        final JLabel result = new JLabel(CoreConstants.SPC);

        result.setFont(Skin.BOLD_12_FONT);
        result.setForeground(Skin.ERROR_COLOR);

        return result;
    }

    /**
     * Creates a panel whose background is set to the "off-white" color.
     *
     * @param layout the layout for the new panel
     * @return the generated panel
     */
    public static JPanel makeOffWhitePanel(final LayoutManager layout) {

        final JPanel result = new JPanel(layout);

        result.setBackground(Skin.OFF_WHITE_GREEN);

        return result;
    }
}

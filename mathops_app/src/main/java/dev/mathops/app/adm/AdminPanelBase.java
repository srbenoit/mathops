package dev.mathops.app.adm;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.LayoutManager;
import java.io.Serial;

/**
 * The base class for panels under the "Admin" outer tab.
 */
public class AdminPanelBase extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1468620848130541459L;

    /**
     * Constructs a new {@code AdminPanelBase}.
     */
    protected AdminPanelBase() {

        super(new StackedBorderLayout(5, 5));

        setBackground(Skin.OFF_WHITE_GREEN);
        setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    /**
     * Creates a header label.
     *
     * @param str              the label string
     * @param borderAbove      true to include a border with extra space above
     * @return the label
     */
    protected static JLabel makeHeader(final String str, final boolean borderAbove) {

        final JLabel result = new JLabel(str);

        if (borderAbove) {
            result.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, Skin.MEDIUM),
                    BorderFactory.createEmptyBorder(5, 0, 5, 0)));
        } else {
            result.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        }

        result.setFont(Skin.BIG_HEADER_22_FONT);
        result.setForeground(Skin.LABEL_COLOR);

        return result;
    }

    /**
     * Creates a label.
     *
     * @param str              the label string
     * @return the label
     */
    protected static JLabel makeLabel(final String str) {

        final JLabel result = new JLabel(str);

        result.setFont(Skin.BODY_12_FONT);
        result.setForeground(Skin.LABEL_COLOR);

        return result;
    }

    /**
     * Creates a label with boldface font.
     *
     * @param str              the label string
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
     * @param len              the length (number of characters)
     * @return the text field
     */
    protected static JTextField makeTextField(final int len) {

        final JTextField result = new JTextField(len);

        result.setEditable(false);
        result.setFont(Skin.BODY_12_FONT);

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
    protected static JPanel makeOffWhitePanel(final LayoutManager layout) {

        final JPanel result = new JPanel(layout);

        result.setBackground(Skin.OFF_WHITE_GREEN);

        return result;
    }
}

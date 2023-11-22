package jwabbit.gui.wizard;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * The base class for wizard pages.
 */
class WizardPage extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -5711213561862917715L;

    /** The background color for wizard page. */
    private final Color bgcolor;

    /** Wizard. */
    private final RomWizardFrame wizard;

    /** Page name. */
    private final String pageName;

    /** Next page. */
    private transient WizardPage nextPage;

    /**
     * Constructs a new {@code WizardPage}.
     *
     * @param theParent   the parent ROM wizard
     * @param thePageName the unique name of the page
     * @param theIcon     the icon to show next to the page title
     * @param theTitle    the page title
     */
    WizardPage(final RomWizardFrame theParent, final String thePageName, final BufferedImage theIcon,
               final String theTitle) {

        super(new BorderLayout(0, 20));

        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        final Color bg = getBackground();
        this.bgcolor = new Color(bg.getRed() + (255 - bg.getRed()) / 4,
                bg.getGreen() + (255 - bg.getGreen()) / 3, bg.getBlue() + (255 - bg.getBlue()) / 2);
        setBackground(this.bgcolor);

        this.wizard = theParent;
        this.pageName = thePageName;

        final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        flow.setBackground(this.bgcolor);
        add(flow, BorderLayout.PAGE_START);

        if (theIcon != null) {
            flow.add(new JLabel(new ImageIcon(theIcon)));
        }

        // Construct the title
        final JLabel title = new JLabel(theTitle);
        final Font fnt = title.getFont();
        title.setFont(fnt.deriveFont(Font.BOLD));
        flow.add(title);
    }

    /**
     * Gets the background color.
     *
     * @return the background color
     */
    final Color getBgColor() {

        return this.bgcolor;
    }

    /**
     * Gets the parent ROM wizard.
     *
     * @return the parent
     */
    final RomWizardFrame getWizard() {

        return this.wizard;
    }

    /**
     * Gets the name of the page.
     *
     * @return the name
     */
    final String getPageName() {

        return this.pageName;
    }

    /**
     * Sets the next panel.
     *
     * @param theNextPage the new next panel
     */
    final void setNextPage(final WizardPage theNextPage) {

        this.nextPage = theNextPage;
    }

    /**
     * Gets the next panel.
     *
     * @return the next panel
     */
    final WizardPage getNextPage() {

        return this.nextPage;
    }
}

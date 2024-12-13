package dev.mathops.app.database.eos;

import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A panel used as a section with a heading that allows its contents to be collapsed or expanded.
 */
public final class Section extends JPanel implements ActionListener {

    /** An action command. */
    private static final String EXPAND_COLLAPSE_CMD = "EC";

    /** A button to expand or collapse the section. */
    private final JButton expandCollapse;

    /** The "expand" icon. */
    private final ImageIcon expandIcon;

    /** The "collapse" icon. */
    private final ImageIcon collapseIcon;

    /** The section contents. */
    private final JPanel contents;

    /** Flag indicating this section is expanded. */
    private boolean expanded = true;

    /**
     * Constructs a new {@code section}
     *
     * @param sectionHeading the heading text
     * @param theExpandImg   the icon image for "expand" buttons (null if unable to load)
     * @param theCollapseImg the icon image for "collapse" buttons (null if unable to load)
     */
    Section(final String sectionHeading, final Image theExpandImg, final Image theCollapseImg) {

        super(new StackedBorderLayout());

        this.expandIcon = theExpandImg == null ? null : new ImageIcon(theExpandImg);
        this.collapseIcon = theCollapseImg == null ? null : new ImageIcon(theCollapseImg);

        final Font buttonFont = new Font(Font.MONOSPACED, Font.BOLD, 16);
        this.expandCollapse = new JButton();
        this.expandCollapse.setFont(buttonFont);
        this.expandCollapse.setActionCommand(EXPAND_COLLAPSE_CMD);
        this.expandCollapse.addActionListener(this);

        if (this.collapseIcon == null) {
            this.expandCollapse.setText("-");
        } else {
            this.expandCollapse.setIcon(this.collapseIcon);
        }

        final Font headingFont = new Font(Font.DIALOG, Font.BOLD, 11);
        final JLabel label = new JLabel(sectionHeading);
        label.setFont(headingFont);

        final JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 3));
        top.add(this.expandCollapse);
        top.add(label);
        add(top, StackedBorderLayout.NORTH);

        this.contents = new JPanel(new StackedBorderLayout());
        add(this.contents, StackedBorderLayout.NORTH);
    }

    /**
     * Adds a content panel.
     *
     * @param toAdd the content panel to add
     */
    void addContents(final JPanel toAdd) {

        this.contents.add(toAdd, StackedBorderLayout.NORTH);
    }

    /**
     * Called when the heading is expanded or collapsed.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (EXPAND_COLLAPSE_CMD.equals(cmd)) {
            if (this.expanded) {
                collapse();
            } else {
                expand();
            }

            invalidate();
            revalidate();
            repaint();
        }
    }

    /**
     * Collapses the section.
     */
    void collapse() {

        remove(this.contents);
        this.expandCollapse.setText("+");
        this.expanded = false;

        if (this.expandIcon == null) {
            this.expandCollapse.setText("+");
            this.expandCollapse.setIcon(null);
        } else {
            this.expandCollapse.setText(null);
            this.expandCollapse.setIcon(this.expandIcon);
        }
    }

    /**
     * Expands the section.
     */
    void expand() {

        add(this.contents, StackedBorderLayout.NORTH);
        this.expandCollapse.setText("-");
        this.expanded = true;

        if (this.collapseIcon == null) {
            this.expandCollapse.setText("-");
            this.expandCollapse.setIcon(null);
        } else {
            this.expandCollapse.setText(null);
            this.expandCollapse.setIcon(this.collapseIcon);
        }
    }
}

package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.gui.Gui;
import jwabbit.gui.fonts.Fonts;
import jwabbit.log.LoggedPanel;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * A pane that allows collapse or expand.
 */
class CollapsePane extends LoggedPanel implements ActionListener {

    /** Heading color. */
    static final Color HEAD_COLOR = new Color(0x00, 0x33, 0x99);
    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 7574644305508136192L;

    /** The down icon image. */
    private final Icon downIcon;

    /** The down hover icon image. */
    private final Icon downHover;

    /** The up icon image. */
    private final Icon upIcon;

    /** The up hover icon image. */
    private final Icon upHover;

    /** The collapse/expand button. */
    private final JButton btn;

    /** Flag indicating panel is expanded. */
    private boolean expanded;

    /** The panel this panel displays in the center pane when expanded. */
    private final JPanel center;

    /**
     * Constructs a new {@code CollapsePane}.
     *
     * @param title     the title for the pane
     * @param theCenter the panel this panel displays in its center when expanded
     */
    CollapsePane(final String title, final JPanel theCenter) {

        super(new BorderLayout());

        setBackground(Color.WHITE);
        theCenter.setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));

        final Font font = Fonts.getSans().deriveFont(Font.BOLD, 11.0f);

        final JPanel toprow = new JPanel(new BorderLayout());
        toprow.setBackground(Color.WHITE);
        final JLabel head = new JLabel(title);
        head.setFont(font);
        head.setForeground(HEAD_COLOR);
        toprow.add(head, BorderLayout.LINE_START);
        toprow.add(new LinePane(), BorderLayout.CENTER);

        add(toprow, BorderLayout.PAGE_START);

        this.downIcon = loadIcon("down.png");
        this.downHover = loadIcon("downh.png");
        this.upIcon = loadIcon("up.png");
        this.upHover = loadIcon("uph.png");

        this.btn = new JButton(this.upIcon);
        this.btn.setRolloverIcon(this.upHover);
        this.btn.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        this.btn.setContentAreaFilled(false);
        this.btn.setBorderPainted(false);
        this.btn.setOpaque(false);
        this.btn.setFocusPainted(false);

        toprow.add(this.btn, BorderLayout.LINE_END);
        this.btn.addActionListener(this);

        final Dimension pref = toprow.getPreferredSize();
        toprow.setPreferredSize(new Dimension(180, pref.height));

        this.center = theCenter;
        add(this.center, BorderLayout.CENTER);

        this.expanded = true;
    }

    /**
     * Gets the panel this panel displays in the center pane when expanded.
     *
     * @return the center panel
     */
    final JPanel getCenter() {

        return this.center;
    }

    /**
     * Loads an icon from the image directory.
     *
     * @param name the file name
     * @return the icon; null if an error occurs
     */
    private static Icon loadIcon(final String name) {

        final BufferedImage img = Gui.loadImage(name);

        return img == null ? null : new ImageIcon(img);
    }

    /**
     * Handles click on the collapse/expand button.
     *
     * @param e the action event
     */
    @Override
    public final void actionPerformed(final ActionEvent e) {

        if (this.expanded) {
            // Collapse
            remove(this.center);
            this.btn.setIcon(this.downIcon);
            this.btn.setRolloverIcon(this.downHover);
            revalidate();
            this.expanded = false;
        } else {
            // Expand
            add(this.center, BorderLayout.CENTER);
            this.btn.setIcon(this.upIcon);
            this.btn.setRolloverIcon(this.upHover);
            revalidate();
            this.expanded = true;
        }
    }

    /**
     * A pane that provides a background solid line.
     */
    private static final class LinePane extends JPanel {

        /** Line color. */
        static final Color LINE_COLOR = new Color(0x66, 0x88, 0xAA);

        /** Version number for serialization. */
        @Serial
        private static final long serialVersionUID = -2915996715480913535L;

        /**
         * Constructs a new {@code LinePane}.
         */
        LinePane() {

            super();

            setBackground(Color.WHITE);
            setBorder(BorderFactory.createMatteBorder(0, 4, 0, 4, Color.WHITE));
        }

        /**
         * Paints the component.
         *
         * @param g the {@code Graphics} to which to paint
         */
        @Override
        public void paintComponent(final Graphics g) {

            super.paintComponent(g);

            g.setColor(LINE_COLOR);
            final int y = (getHeight() + 1) / 2;
            g.drawLine(0, y, getWidth(), y);
        }
    }
}

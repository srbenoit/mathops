package jwabbit.gui.options;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.gui.CalcUI;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * A panel that lets the user click on a color choice.
 */
final class ColorChoicesPane extends JPanel implements MouseListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 6433807922596919112L;

    /** The available colors. */
    private static final Color[] COLORS = {CalcUI.CHARCOAL, CalcUI.GRAY, CalcUI.SILVER, CalcUI.PINK,
            CalcUI.BRICKRED, CalcUI.DARKRED, CalcUI.RED, CalcUI.BLUE, CalcUI.DARKBLUE, CalcUI.LIGHTBLUE,
            CalcUI.PURPLE, CalcUI.TAN, CalcUI.BROWN, CalcUI.ORANGE, CalcUI.GREEN, CalcUI.YELLOW};

    /** The color palette image. */
    private final BufferedImage img;

    /** The selected color index. */
    private int selectedIndex;

    /**
     * Constructs a new {@code ColorChoicesPane}.
     */
    ColorChoicesPane() {

        super();
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(128, 32));

        this.img = new BufferedImage(128, 32, BufferedImage.TYPE_INT_ARGB);
        final Graphics grx = this.img.getGraphics();
        grx.setColor(Color.WHITE);
        grx.fillRect(0, 0, 127, 31);
        for (int x = 0; x < 8; ++x) {
            for (int y = 0; y < 2; ++y) {
                grx.setColor(Color.GRAY);
                grx.drawLine(x << 4, y << 4, (x << 4) + 15, y << 4);
                grx.drawLine(x << 4, y << 4, x << 4, (y << 4) + 15);
                grx.setColor(COLORS[x + (y << 3)]);
                grx.fillRect((x << 4) + 2, (y << 4) + 2, 12, 12);
            }
        }

        this.selectedIndex = -1;

        addMouseListener(this);
    }

    /**
     * Paints the pane.
     *
     * @param g the {@code Graphics} to which to paint
     */
    @Override
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);

        g.drawImage(this.img, 0, 0, null);

        if (this.selectedIndex != -1) {
            g.setColor(Color.YELLOW);
            g.drawRect(16 * (this.selectedIndex % 8), 16 * (this.selectedIndex / 8), 7, 7);
        }
    }

    /**
     * Handles mouse click events.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseClicked(final MouseEvent e) {

        // No action
    }

    /**
     * Handles mouse press events.
     *
     * @param e the mouse event
     */
    @Override
    public void mousePressed(final MouseEvent e) {

        final int x = e.getX() / 16;
        final int y = e.getY() / 16;

        final int index = x + 8 * y;

        if (index >= 0 && index < COLORS.length) {
            this.selectedIndex = index;
        }
        repaint();
    }

    /**
     * Handles mouse release events.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseReleased(final MouseEvent e) {

        // No action
    }

    /**
     * Handles mouse entered events.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseEntered(final MouseEvent e) {

        // No action
    }

    /**
     * Handles mouse exited events.
     *
     * @param e the mouse event
     */
    @Override
    public void mouseExited(final MouseEvent e) {

        // No action
    }
}

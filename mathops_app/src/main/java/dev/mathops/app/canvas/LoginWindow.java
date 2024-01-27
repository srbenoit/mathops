package dev.mathops.app.canvas;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.app.canvas.data.UserInfo;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A window where the user can enter the URL of the canvas host and an access token to connect to that host. Once a
 * connection is established, the main application window is opened.
 */
final class LoginWindow implements Runnable, ActionListener {

    /** An action command. */
    private static final String LOGIN_CMD = "LOGIN";

    /** An action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** The initial host URL. */
    private final String initialHost;

    /** The initial access token. */
    private final String initialToken;

    /** The frame. */
    private JFrame frame;

    /** The host URL. */
    private JTextField host;

    /** The access token. */
    private JTextField token;

    /** An error message. */
    private JLabel error;

    /**
     * Constructs a new {@code LoginWindow}
     *
     * @param theInitHost        the host URL to pre-populate (from command-line)
     * @param theInitAccessToken the access token to pre-populate (from command-line)
     */
    LoginWindow(final String theInitHost, final String theInitAccessToken) {

        this.initialHost = theInitHost;
        this.initialToken = theInitAccessToken;
    }

    /**
     * Constructs the UI in the AWT event thread.
     */
    @Override
    public void run() {

        this.frame = new JFrame(Res.get(Res.TITLE));
        this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        this.frame.setContentPane(content);

        // NORTH: Header
        final JPanel north = new JPanel(new BorderLayout());
        north.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(0, 0, 4, 0)));

        final JLabel header = new JLabel(Res.get(Res.LOGIN_TITLE));
        final Font bigHeaderFont = header.getFont().deriveFont(18.0f);
        header.setFont(bigHeaderFont);
        header.setHorizontalAlignment(SwingConstants.CENTER);
        north.add(header, BorderLayout.CENTER);
        content.add(north, BorderLayout.PAGE_START);

        // CENTER: Fields
        final JPanel center = new JPanel(new StackedBorderLayout(6, 6));
        center.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));

        final JLabel usernameLbl = new JLabel(Res.get(Res.LOGIN_HOST_FIELD_LBL));
        final Font boldTextFont = header.getFont().deriveFont(Font.BOLD, 12.0f);
        usernameLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        usernameLbl.setFont(boldTextFont);

        final JLabel passwordLbl = new JLabel(Res.get(Res.LOGIN_TOKEN_FIELD_LBL));
        passwordLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        passwordLbl.setFont(boldTextFont);

        final Dimension hostLblSize = usernameLbl.getPreferredSize();
        final Dimension tokenLblSize = passwordLbl.getPreferredSize();
        final Dimension lblSize = new Dimension(Math.max(hostLblSize.width, tokenLblSize.width),
                Math.max(hostLblSize.height, tokenLblSize.height));

        usernameLbl.setPreferredSize(lblSize);
        passwordLbl.setPreferredSize(lblSize);

        final JPanel hostPanel = new JPanel(new BorderLayout(10, 10));
        hostPanel.add(usernameLbl, BorderLayout.LINE_START);
        this.host = new JTextField(44);
        hostPanel.add(this.host);
        hostPanel.add(Box.createRigidArea(new Dimension(30, 1)), BorderLayout.LINE_END);
        center.add(hostPanel, StackedBorderLayout.NORTH);

        final JPanel tokenPanel = new JPanel(new BorderLayout(10, 10));
        tokenPanel.add(passwordLbl, BorderLayout.LINE_START);
        this.token = new JTextField(44);
        tokenPanel.add(this.token);
        tokenPanel.add(Box.createRigidArea(new Dimension(30, 1)), BorderLayout.LINE_END);
        center.add(tokenPanel, StackedBorderLayout.NORTH);

        final JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        this.error = new JLabel(CoreConstants.SPC);
        this.error.setFont(boldTextFont);
        errorPanel.add(this.error);
        center.add(errorPanel, StackedBorderLayout.NORTH);

        content.add(center, BorderLayout.CENTER);

        // SOUTH: Buttons
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        buttons.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        final JButton loginBtn = new JButton(Res.get(Res.LOGIN_LOGIN_BTN));
        final Font buttonFont = loginBtn.getFont().deriveFont(13.0f);
        loginBtn.setFont(buttonFont);
        loginBtn.setActionCommand(LOGIN_CMD);
        loginBtn.addActionListener(this);

        final JButton cancelBtn = new JButton(Res.get(Res.LOGIN_CANCEL_BTN));
        cancelBtn.setFont(buttonFont);
        cancelBtn.setActionCommand(CANCEL_CMD);
        cancelBtn.addActionListener(this);

        buttons.add(loginBtn);
        buttons.add(cancelBtn);
        content.add(buttons, BorderLayout.PAGE_END);

        this.frame.getRootPane().setDefaultButton(loginBtn);

        this.frame.pack();

        final GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        final GraphicsDevice selected = env.getDefaultScreenDevice();

        final Rectangle bounds = selected.getDefaultConfiguration().getBounds();

        final Dimension size = this.frame.getSize();
        this.frame.setLocation(bounds.x + (bounds.width - size.width) / 2,
                bounds.y + (bounds.height - size.height) / 2);
        this.frame.setVisible(true);

        if (this.initialHost != null) {
            this.host.setText(this.initialHost);
        }
        if (this.initialToken != null) {
            this.token.setText(this.initialToken);
        }

        if (this.initialHost == null) {
            this.host.requestFocus();
        } else if (this.initialToken == null) {
            this.token.requestFocus();
        }
    }

    /**
     * Called on the AWT event dispatch thread when a button is pressed.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();
        String err = null;

        if (LOGIN_CMD.equals(cmd)) {
            this.error.setText(CoreConstants.SPC);

            int good = 0;

            final String h = this.host.getText();

            if (h == null || h.isEmpty()) {
                err = Res.get(Res.LOGIN_NO_HOST_ERR);
            } else {
                ++good;
            }

            final String t = this.token.getText();
            if (t == null || t.isEmpty()) {
                err = Res.get(Res.LOGIN_NO_TOKEN_ERR);
            } else {
                ++good;
            }

            if (good == 2) {
                final CanvasApi api = new CanvasApi(h, t);
                final UserInfo userInfo = api.fetchUser();

                if (userInfo == null) {
                    err = Res.get(Res.LOGIN_FAILED);
                    this.error.setText(err);
                } else {
                    this.frame.setVisible(false);
                    this.frame.dispose();

                    new MainWindow(api, userInfo).setVisible(true);
                }
            } else if (err != null) {
                this.error.setText(err);
            }
        } else if (CANCEL_CMD.equals(cmd)) {
            this.frame.setVisible(false);
            this.frame.dispose();
        }
    }
}

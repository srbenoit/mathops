package dev.mathops.app.checkin;

import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.font.BundledFontManager;
import dev.mathops.text.builder.SimpleBuilder;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A dialog to gather the database login information to be used to connect to the configured database.
 */
public final class LoginDialog implements ActionListener {

    /** A common string. */
    private static final String CONNECT = "Connect";

    /** A common string. */
    private static final String CANCEL = "Cancel";

    /** The width of fields. */
    private static final int FIELD_WIDTH = 12;

    /** The frame. */
    private JFrame frame;

    /** The driver name. */
    private final String driverName;

    /** The default username. */
    private final String defaultUsername;

    /** Flag indicating Cancel button was pressed. */
    private boolean cancel = false;

    /** Flag indicating Connect button was pressed. */
    private boolean connect = false;

    /** Color for fields that have been properly filled out. */
    private Color okColor;

    /** Color for fields that are empty or have been improperly filled out. */
    private Color badColor;

    /** The login username field. */
    private JTextField usernameFld;

    /** The password field. */
    private JPasswordField passwordFld;

    /** The Cancel button. */
    private JButton cancelBtn;

    /** The Connect button. */
    private JButton connectBtn;

    /**
     * Constructs a new {@code LoginDialog}.
     *
     * @param theDriverName the driver name
     * @param defUsername   the default username
     */
    public LoginDialog(final String theDriverName, final String defUsername) {

        this.driverName = theDriverName;
        this.defaultUsername = defUsername;

        buildUI();
    }

    /**
     * Constructs the user interface.
     */
    private void buildUI() {

        // final Color col = new Color(220, 220, 220);

        this.frame = new JFrame(CONNECT);

        this.okColor = Color.WHITE;
        this.badColor = new Color(255, 255, 200);

        // Generate a content pane with a margin.
        final JPanel content = new JPanel(new BorderLayout(5, 5));
        final Border emptyBorder1 = BorderFactory.createEmptyBorder(15, 15, 3, 15);
        content.setBorder(emptyBorder1);
        this.frame.setContentPane(content);

        final BundledFontManager bfm = BundledFontManager.getInstance();

        final JLabel lbl = new JLabel("Database Connection (" + this.driverName + ")");
        final Font font = bfm.getFont(BundledFontManager.SANS, 16.0, Font.PLAIN);
        lbl.setFont(font);
        content.add(lbl, BorderLayout.PAGE_START);

        final JPanel fields = new JPanel(new GridLayout(2, 1, 5, 0));
        final Border emptyBorder = BorderFactory.createEmptyBorder(15, 10, 5, 10);
        fields.setBorder(emptyBorder);

        // Generate field name labels and set to a common size
        final JLabel[] names = {new JLabel("Login Username:"), new JLabel("Login Password:")};
        UIUtilities.makeLabelsSameSizeRightAligned(names);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        flow1.add(names[0]);
        this.usernameFld = new JTextField(this.defaultUsername, FIELD_WIDTH);
        this.usernameFld.setBackground(this.okColor);
        flow1.add(this.usernameFld);
        fields.add(flow1);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        flow2.add(names[1]);
        this.passwordFld = new JPasswordField(FIELD_WIDTH);
        this.passwordFld.setBackground(this.okColor);
        flow2.add(this.passwordFld);
        fields.add(flow2);

        content.add(fields, BorderLayout.CENTER);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING, 5, 2));
        this.cancelBtn = new JButton(CANCEL);
        this.cancelBtn.setActionCommand(CANCEL);
        this.cancelBtn.addActionListener(this);
        buttons.add(this.cancelBtn);
        this.connectBtn = new JButton(CONNECT);
        this.connectBtn.setActionCommand(CONNECT);
        this.connectBtn.addActionListener(this);
        buttons.add(this.connectBtn);

        // Make "Connect" react to Return key
        this.frame.getRootPane().setDefaultButton(this.connectBtn);

        content.add(buttons, BorderLayout.PAGE_END);
        this.frame.pack();

        if (this.defaultUsername != null && !this.defaultUsername.isEmpty()) {
            this.passwordFld.requestFocus();
        }

        // Center on the screen
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int frameWidth = this.frame.getWidth();
        final int frameHeight = this.frame.getHeight();
        this.frame.setLocation((screen.width - frameWidth) / 2, (screen.height - frameHeight) / 2);
    }

    /**
     * Handler for action events generated by buttons.
     *
     * @param e the action event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();
        boolean isConnect = true;

        this.cancel = CANCEL.equals(cmd);
        this.connect = false;

        if (CONNECT.equals(cmd)) {
            this.usernameFld.setBackground(this.okColor);
            this.passwordFld.setBackground(this.okColor);

            // validate fields
            if (this.passwordFld.getPassword().length == 0) {
                this.passwordFld.setBackground(this.badColor);
                this.passwordFld.requestFocus();
                isConnect = false;
            }

            if (this.usernameFld.getText().isEmpty()) {
                this.usernameFld.setBackground(this.badColor);
                this.usernameFld.requestFocus();
                isConnect = false;
            }

            this.connect = isConnect;
        }
    }

    /**
     * Closes the dialog.
     */
    public void close() {

        this.frame.setVisible(false);
        this.frame.dispose();
    }

    /**
     * Displays the dialog and waits for the user to press either Connect or Cancel.
     *
     * @return {@code true} if "Connect" was pressed, {@code false} if "Cancel" was pressed
     */
    public boolean gatherInformation() {

        // reset and show dialog
        this.cancel = false;
        this.connect = false;

        this.frame.setVisible(true);
        this.frame.toFront();
        this.cancelBtn.setEnabled(true);
        this.connectBtn.setEnabled(true);
        this.usernameFld.setEnabled(true);
        this.passwordFld.setEnabled(true);
        final Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
        this.frame.setCursor(defaultCursor);
        this.usernameFld.requestFocus();

        if (this.usernameFld.getText().isEmpty()) {
            this.usernameFld.requestFocus();
        } else if (this.passwordFld.getPassword().length == 0) {
            this.passwordFld.requestFocus();
        }

        while ((!this.cancel) && (!this.connect)) {
            try {
                Thread.sleep(50L);
            } catch (final InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        if (this.connect) {
            this.cancelBtn.setEnabled(false);
            this.connectBtn.setEnabled(false);
            this.usernameFld.setEnabled(false);
            this.passwordFld.setEnabled(false);
            final Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            this.frame.setCursor(waitCursor);
        }

        return this.connect;
    }

    /**
     * Gets the entered login username.
     *
     * @return the entered login username
     */
    public String getUsername() {

        return this.usernameFld.getText();
    }

    /**
     * Gets the entered login password.
     *
     * @return the entered login password
     */
    public char[] getPassword() {

        return this.passwordFld.getPassword();
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final String cancelStr = Boolean.toString(this.cancel);
        final String connectStr = Boolean.toString(this.connect);

        return SimpleBuilder.concat("LoginDialog{", "frame=", this.frame, ", driverName='", this.driverName,
                "', defaultUsername='", this.defaultUsername, "', cancel=", cancelStr, ", connect=", connectStr,
                ", okColor=", this.okColor, ", badColor=", this.badColor, ", usernameFld=", this.usernameFld,
                ", passwordFld=", this.passwordFld, ", cancelBtn=", this.cancelBtn, ", connectBtn=", this.connectBtn,
                "}");
    }
}

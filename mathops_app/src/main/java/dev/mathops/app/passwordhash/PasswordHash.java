package dev.mathops.app.passwordhash;

import dev.mathops.app.IGuiBuilder;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.parser.HexEncoder;
import dev.mathops.commons.ui.ColorNames;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;

/**
 * A utility program to generate hash values from passwords.
 */
public final class PasswordHash implements IGuiBuilder, ActionListener, Runnable {

    /** The length of a salt value (20 * 6 bits per = 120 bits of size). */
    private static final int SALT_LEN = 20;

    /** The field where the password is entered. */
    private JPasswordField password;

    /** The field in which to place the generated (random) salt. */
    private JTextField salt;

    /** The field in which to place the generated salted hash. */
    private JTextField saltedHash;

    /**
     * Construct a new {@code PasswordHash}.
     */
    private PasswordHash() {

        // No action
    }

    /**
     * Builds the user interface and displays the main frame.
     */
    private void go() {

        SwingUtilities.invokeLater(this);
    }

    /**
     * Constructs the user interface in the AWT event thread.
     */
    @Override
    public void run() {

        final JFrame frame = new JFrame(Res.get(Res.FRAME_TITLE));
        buildUI(frame);

        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    /**
     * Construct the GUI, to be called in the AWT event thread.
     *
     * @param frame the frame
     */
    @Override
    public void buildUI(final JFrame frame) {

        final Color bg = ColorNames.getColor("SteelBlue");
        final Color fg = ColorNames.getColor("cornsilk");

        // Create the content panel
        final JPanel content = new JPanel();
        content.setBackground(bg);
        content.setLayout(new BorderLayout(2, 2));
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(3, 3, 3, 3), BorderFactory.createEtchedBorder()));
        frame.setContentPane(content);

        // Create the top line: password entry
        JPanel inner = new JPanel();
        inner.setBorder(BorderFactory.createEmptyBorder(4, 10, 2, 10));
        inner.setBackground(bg);
        inner.setLayout(new FlowLayout(FlowLayout.CENTER));
        final JLabel label = new JLabel(Res.get(Res.PWD_PROMPT));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setForeground(fg);
        inner.add(label);
        this.password = new JPasswordField(20);
        inner.add(this.password);
        content.add(inner, BorderLayout.NORTH);

        // Create the generate button
        inner = new JPanel();
        inner.setBorder(BorderFactory.createEmptyBorder(4, 10, 2, 10));
        inner.setBackground(bg);
        inner.setLayout(new GridLayout(3, 1, 2, 2));
        final JButton btn = new JButton(Res.get(Res.BUTTON_LABEL));
        btn.addActionListener(this);
        inner.add(btn);

        final JLabel saltLbl = new JLabel(Res.get(Res.SALT_LABEL));
        final JLabel hash2Lbl = new JLabel(Res.get(Res.SALTED_HASH_LABEL));
        saltLbl.setHorizontalAlignment(SwingConstants.RIGHT);
        hash2Lbl.setHorizontalAlignment(SwingConstants.RIGHT);

        saltLbl.setForeground(fg);
        hash2Lbl.setForeground(fg);

        final Dimension saltSize = saltLbl.getPreferredSize();
        final Dimension hash2Size = hash2Lbl.getPreferredSize();

        final Dimension size =
                new Dimension(Math.max(saltSize.width, hash2Size.width), saltSize.height);
        saltLbl.setPreferredSize(size);
        hash2Lbl.setPreferredSize(size);

        final JPanel flow1 = new JPanel(new BorderLayout(10, 0));
        flow1.setOpaque(false);
        this.salt = new JTextField(CoreConstants.EMPTY, 42);
        this.salt.setEditable(false);
        flow1.add(saltLbl, BorderLayout.WEST);
        flow1.add(this.salt, BorderLayout.CENTER);
        inner.add(flow1);

        final JPanel flow2 = new JPanel(new BorderLayout(10, 0));
        flow2.setOpaque(false);
        this.saltedHash = new JTextField(CoreConstants.EMPTY, 42);
        this.saltedHash.setEditable(false);
        flow2.add(hash2Lbl, BorderLayout.WEST);
        flow2.add(this.saltedHash, BorderLayout.CENTER);
        inner.add(flow2);

        content.add(inner, BorderLayout.CENTER);

        final JLabel explanation = new JLabel(Res.get(Res.EXPLAIN));
        explanation.setForeground(fg);
        explanation.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(explanation, BorderLayout.SOUTH);

        frame.pack();
    }

    /**
     * Handle an action command by generating the SHA-1 hash of the entered password.
     *
     * @param evt the action event
     */
    @Override
    public void actionPerformed(final ActionEvent evt) {

        if (this.password.getPassword() != null) {

            try {
                // Generate a random salt
                final String saltString = SaltedHasher.makeRandomSalt(SALT_LEN);
                this.salt.setText(saltString);

                // Generate the salted hash
                final byte[] saltedHashBytes = new SaltedHasher().compute(saltString,
                        String.valueOf(this.password.getPassword()));
                this.saltedHash.setText(HexEncoder.encodeLowercase(saltedHashBytes));
            } catch (final NoSuchAlgorithmException ex) {
                this.saltedHash.setText(Res.get(Res.ERROR_MSG));
            }
        }
    }

    /**
     * Main method to run the program.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        new PasswordHash().go();
    }
}

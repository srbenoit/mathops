package dev.mathops.app.webstart;

import dev.mathops.app.AppFileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.HexEncoder;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;

/**
 * A Swing app that allows the user to select a file, then computes the SHA-256 hash of the file and generates the XML
 * element representing the file's descriptor.
 */
final class FileDescriptorGen implements Runnable, ActionListener {

    /** An action command. */
    private static final String COPY = "COPY";

    /** An action command. */
    private static final String CHOOSE = "CHOOSE";

    /** The frame. */
    private JFrame frame;

    /** The field that will be populated with file descriptor information. */
    private JTextField text;

    /** A representation of the current date, for use as a release date. */
    private JTextField date;

    /**
     * Private constructor to prevent direct instantiation.
     */
    private FileDescriptorGen() {

        // No action
    }

    /**
     * Creates the UI in the AWT even thread.
     */
    @Override
    public void run() {

        this.frame = new JFrame("File Descriptor Generator");
        this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final JPanel content = new JPanel(new BorderLayout(5, 5));
        this.frame.setContentPane(content);

        content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        final JLabel top = new JLabel("File descriptor element:");
        content.add(top, BorderLayout.NORTH);
        final JPanel center1 = new JPanel(new BorderLayout());
        content.add(center1, BorderLayout.CENTER);
        final JPanel center2 = new JPanel(new BorderLayout());
        center1.add(center2, BorderLayout.CENTER);
        final JPanel center3 = new JPanel(new BorderLayout());
        center2.add(center3, BorderLayout.CENTER);

        this.text = new JTextField(80);
        center1.add(this.text, BorderLayout.NORTH);

        center2.add(new JLabel("Today's Date:"), BorderLayout.NORTH);

        this.date = new JTextField(40);
        center3.add(this.date, BorderLayout.NORTH);

        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        content.add(buttons, BorderLayout.SOUTH);

        final JButton copy = new JButton("Copy");
        copy.setActionCommand(COPY);
        buttons.add(copy);
        copy.addActionListener(this);

        final JButton choose = new JButton("Choose");
        choose.setActionCommand(CHOOSE);
        buttons.add(choose);
        choose.addActionListener(this);

        this.frame.pack();
        final Dimension size = this.frame.getSize();
        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        this.frame.setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 3);
        this.frame.setVisible(true);
    }

    /**
     * Called when the copy button is pressed.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (COPY.equals(cmd)) {
            final String txt = this.text.getText();

            final Transferable stringSelection = new StringSelection(txt);
            final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        } else if (CHOOSE.equals(cmd)) {
            chooseFile();
        }
    }

    /**
     * Opens the file selector, generates the data, and displays the result.
     */
    private void chooseFile() {

        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);

        final File base = new File("/Users/benoit/dev/git/bls/bls/lib");
        if (base.exists() && base.isDirectory()) {
            chooser.setCurrentDirectory(base);
        }

        if (chooser.showOpenDialog(this.frame) == JFileChooser.APPROVE_OPTION) {
            final File file = chooser.getSelectedFile();
            processFile(file);
        } else {
            SwingUtilities.invokeLater(() -> {
                this.frame.setVisible(false);
                this.frame.dispose();
            });
        }
    }

    /**
     * Processes a single file.
     *
     * @param f the file
     */
    private void processFile(final File f) {

        final byte[] bytes = AppFileLoader.loadFileAsBytes(f, true);

        try {
            final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            final String hex = HexEncoder.encodeLowercase(sha256.digest(bytes));

            final StringBuilder xml = new StringBuilder(50);
            xml.append("<file");
            xml.append(" name='").append(f.getName()).append('\'');
            xml.append(" size='").append(bytes.length).append('\'');
            xml.append(" sha256='").append(hex).append('\'');
            xml.append("/>");

            final ZonedDateTime now = ZonedDateTime.now();

            SwingUtilities.invokeLater(() -> {
                this.text.setText(xml.toString());
                this.date.setText(now.toString());
            });
        } catch (final NoSuchAlgorithmException ex) {
            Log.warning("Unable to create SHA-256 message digest", ex);
        }
    }

    /**
     * Main method to launch application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        final FileDescriptorGen gen = new FileDescriptorGen();

        try {
            SwingUtilities.invokeAndWait(gen);
            gen.chooseFile();

        } catch (final InvocationTargetException | InterruptedException ex) {
            Log.warning("Failed to start program", ex);
        }
    }
}

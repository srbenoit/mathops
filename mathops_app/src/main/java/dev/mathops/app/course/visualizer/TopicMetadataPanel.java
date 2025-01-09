package dev.mathops.app.course.visualizer;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.ui.UIUtilities;
import dev.mathops.commons.ui.layout.StackedBorderLayout;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.json.JSONParser;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A panel that displays the metadata found in a topic module and provides a button to edit the JSON file.
 */
final class TopicMetadataPanel extends JPanel implements ActionListener {

    /** An action command. */
    private static final String EDIT_CMD = "EDIT";

    /** An action command. */
    private static final String OPEN_DIR_CMD = "OPEN_DIR";

    /** The title. */
    private final JLabel titleLbl;

    /** The authors. */
    private final JLabel authorsLbl;

    /** The title. */
    private final JTextArea descriptionArea;

    /** The title. */
    private final JTextArea goalsArea;

    /** The button to open the topic module directory. */
    private final JButton openDir;

    /** The button to edit the file. */
    private final JButton edit;

    /** The current metadata file (may not exist). */
    private File jsonFile = null;

    /**
     * Constructs a new {@code TopicMetadataPanel}.
     *
     * @param lineColor the color for line borders
     */
    TopicMetadataPanel(final Color lineColor) {

        super(new StackedBorderLayout());

        final Border etched = BorderFactory.createEtchedBorder();

        this.titleLbl = new JLabel();
        this.authorsLbl = new JLabel();
        this.descriptionArea = new JTextArea(4, 50);
        final Color bg = this.descriptionArea.getBackground();
        this.descriptionArea.setEditable(false);
        this.descriptionArea.setLineWrap(true);
        this.descriptionArea.setWrapStyleWord(true);
        this.descriptionArea.setBorder(etched);
        this.descriptionArea.setBackground(bg);
        this.goalsArea = new JTextArea(6, 50);
        this.goalsArea.setEditable(false);
        this.goalsArea.setLineWrap(true);
        this.goalsArea.setWrapStyleWord(true);
        this.goalsArea.setBorder(etched);
        this.goalsArea.setBackground(bg);

        final Class<? extends TopicMetadataPanel> cls = getClass();
        final BufferedImage folderImage = FileLoader.loadFileAsImage(cls, "folder.png", false);
        final BufferedImage docImage = FileLoader.loadFileAsImage(cls, "file-doc.png", false);

        if (folderImage == null) {
            this.openDir = new JButton("Open Directory");
        } else {
            final ImageIcon iconImage = new ImageIcon(folderImage);
            this.openDir = new JButton(iconImage);
            this.openDir.setToolTipText("Open Directory");
        }

        if (docImage == null) {
            this.edit = new JButton("Edit Metadata");
        } else {
            final ImageIcon iconImage = new ImageIcon(docImage);
            this.edit = new JButton(iconImage);
            this.edit.setToolTipText("Edit Metadata");
        }

        final JLabel[] labels = new JLabel[4];
        labels[0] = new JLabel("Title:");
        labels[1] = new JLabel("Author(s):");
        labels[2] = new JLabel("Description:");
        labels[3] = new JLabel("Goals:");
        UIUtilities.makeLabelsSameSizeRightAligned(labels);
        final int labelWidth = labels[0].getPreferredSize().width;
        final Border topPad2 = BorderFactory.createEmptyBorder(2, 0, 0, 0);

        final JPanel top = new JPanel(new StackedBorderLayout());
        add(top, StackedBorderLayout.NORTH);

        final JPanel topLeft = new JPanel(new StackedBorderLayout());
        top.add(topLeft, StackedBorderLayout.WEST);

        final JPanel titleLine = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
        titleLine.add(labels[0]);
        titleLine.add(this.titleLbl);
        topLeft.add(titleLine, StackedBorderLayout.NORTH);

        final JPanel authorsLine = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
        authorsLine.add(labels[1]);
        authorsLine.add(this.authorsLbl);
        topLeft.add(authorsLine, StackedBorderLayout.NORTH);

        final JPanel descriptionLine = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
        final JPanel line2First = new JPanel(new StackedBorderLayout());
        line2First.setBorder(topPad2);
        line2First.add(labels[2], StackedBorderLayout.NORTH);
        final int descriptionHeight = this.descriptionArea.getPreferredSize().height;
        final Dimension line2FirstSize = new Dimension(labelWidth, descriptionHeight);
        line2First.setPreferredSize(line2FirstSize);
        descriptionLine.add(line2First);
        descriptionLine.add(this.descriptionArea);
        add(descriptionLine, StackedBorderLayout.NORTH);

        final JPanel goalsLine = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));

        final JPanel line3First = new JPanel(new StackedBorderLayout());
        line3First.setBorder(topPad2);
        line3First.add(labels[3], StackedBorderLayout.NORTH);
        final int goalsHeight = this.goalsArea.getPreferredSize().height;
        final Dimension line3FirstSize = new Dimension(labelWidth, goalsHeight);
        line3First.setPreferredSize(line3FirstSize);
        goalsLine.add(line3First);
        goalsLine.add(this.goalsArea);
        add(goalsLine, StackedBorderLayout.NORTH);

        final JPanel topRight = new JPanel(new FlowLayout(FlowLayout.TRAILING, 6, 2));
        topRight.add(this.edit);
        topRight.add(this.openDir);
        top.add(topRight, StackedBorderLayout.EAST);
    }

    /**
     * Initializes the panel.  Called after the constructor since this method leaks 'this' and the object is not fully
     * constructed within the constructor.
     */
    void init() {

        this.edit.setActionCommand(EDIT_CMD);
        this.edit.addActionListener(this);

        this.openDir.setActionCommand(OPEN_DIR_CMD);
        this.openDir.addActionListener(this);
    }

    /**
     * Clears all fields in the panel.
     *
     * @param titleText text to display in the "title" field
     */
    private void clear(final String titleText) {

        this.titleLbl.setText(titleText);
        this.authorsLbl.setText(CoreConstants.SPC);
        this.descriptionArea.setText(CoreConstants.EMPTY);
        this.goalsArea.setText(CoreConstants.EMPTY);
    }

    /**
     * Refreshes the contents of the panel by attempting to read the "metadata.json" file from a topic module directory
     * and populating fields with the data from that file.
     *
     * @param topicModuleDir the topic module directory
     */
    void refresh(final File topicModuleDir) {

        if (topicModuleDir == null) {
            this.jsonFile = null;
            clear(CoreConstants.SPC);
        } else {
            this.jsonFile = new File(topicModuleDir, "metadata.json");

            if (!this.jsonFile.exists()) {
                createBlankMetadataFile();
            }

            if (this.jsonFile.exists()) {
                final String fileData = FileLoader.loadFileAsString(this.jsonFile, false);
                if (fileData == null) {
                    clear("(Unable to read metadata)");
                } else {
                    try {
                        final Object parsed = JSONParser.parseJSON(fileData);
                        if (parsed instanceof final JSONObject parsedJson) {
                            populateFields(parsedJson);
                        } else {
                            clear("(Unable to interpret metadata)");
                        }
                    } catch (final ParsingException ex) {
                        clear("(Unable to parse metadata)");
                        Log.warning("Failed to parse 'metadata.json'", ex);
                    }
                }
            } else {
                clear("(No metadata found)");
            }
        }
    }

    /**
     * Creates an empty metadata JSON file when one is not found.
     */
    private void createBlankMetadataFile() {

        final String contents = """
                {
                  "title":        "",
                  "description":  "",
                  "authors":      "",
                  "goals":        "",
                  "attributions":
                  [
                    {
                      "resource":    "",
                      "author":      "",
                      "source":      "",
                      "license":     ""
                    }
                  ]
                }
                """;

        try (final FileWriter writer = new FileWriter(this.jsonFile, StandardCharsets.UTF_8)) {
            writer.write(contents);
        } catch (final IOException ex) {
            Log.warning("Failed to create metadat file.", ex);
        }
    }

    /**
     * Extracts fields from the parsed JSON file and populates the display.
     *
     * @param parsedJson the parsed JSON object
     */
    private void populateFields(final JSONObject parsedJson) {

        final String title = parsedJson.getStringProperty("title");
        final String authors = parsedJson.getStringProperty("authors");
        final String description = parsedJson.getStringProperty("description");
        final String goals = parsedJson.getStringProperty("goals");

        this.titleLbl.setText(title == null ? CoreConstants.SPC : title);
        this.authorsLbl.setText(authors == null ? CoreConstants.SPC : authors);
        this.descriptionArea.setText(description == null ? CoreConstants.EMPTY : description);
        this.goalsArea.setText(goals == null ? CoreConstants.EMPTY : goals);
    }

    /**
     * Called when the "edit file" button is pressed.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        if (this.jsonFile != null) {
            final String cmd = e.getActionCommand();

            if (EDIT_CMD.equals(cmd)) {
                try {
                    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                        final String path = this.jsonFile.getCanonicalPath();
                        final String[] command = {"rundll32", "url.dll,FileProtocolHandler", path};
                        Runtime.getRuntime().exec(command);
                    } else {
                        java.awt.Desktop.getDesktop().edit(this.jsonFile);
                    }
                } catch (final IOException ex) {
                    Log.warning("Failed to edit metadata file.", ex);
                }
            } else if (OPEN_DIR_CMD.equals(cmd)) {
                final File dir = this.jsonFile.getParentFile();

                try {
                    java.awt.Desktop.getDesktop().open(dir);
                } catch (final IOException ex) {
                    Log.warning("Failed to open topic directory.", ex);
                }
            }
        }
    }
}

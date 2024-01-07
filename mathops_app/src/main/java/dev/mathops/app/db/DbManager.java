package dev.mathops.app.db;

import dev.mathops.app.db.config.CodeContextPane;
import dev.mathops.app.db.config.model.CfgDatabaseLayerModel;
import dev.mathops.core.file.FileLoader;
import dev.mathops.core.log.Log;
import dev.mathops.db.config.CfgDatabaseLayer;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * The database manager application.
 *
 * <p>
 * This application presents a window with four tabs:
 * <dl>
 *     <dt>Configuration</dt>
 *     <dd>This tab allows the user to view and edit the XML configuration file that defines the server instances,
 *     databases, logins, data profiles, and the web and code contexts.</dd>
 *     <dt>Validation</dt>
 *     <dd>This tab allows the user to inspect and verify database structures to ensure all needed tables and fields
 *     are present and users have necessary permissions.</dd>
 *     <dt>Data</dt>
 *     <dd>This tab allows the user to query, insert, update, and delete records from any table.</dd>
 *     <dt>Reporting</dt>
 *     <dd>This tab supports generation of reports from the database.</dd>
 * </dl>
 */
public class DbManager extends Application {

    /** The gap between icon and text on buttons. */
    private static final double BUTTON_GAP = 16.0;

    /** The currently loaded database configuration. */
    private CfgDatabaseLayer config;

    /** The mutable version of the loaded database configuration. */
    private CfgDatabaseLayerModel mutableConfig;

    /**
     * Constructs a new {@code DbManager}.
     */
    public DbManager() {

        super();

    }

    /**
     * Starts the application.
     * @param stage the stage
     */
    @Override
    public void start(final Stage stage) {

        stage.setTitle("Database Manager");

        final Parent content = createContent();
        final Scene scene = new Scene(content);

        stage.setScene(scene);

        stage.show();
    }

    /**
     * Creates the content of the main stage.
     *
     * @return the content
     */
    private Parent createContent() {

        final TabPane tabPane = new TabPane();
        tabPane.setPrefSize(1100.0, 800.0);

        final Tab configurationTab = makeNonclosableTab("Configuration");
        final Parent configurationTabContent = createConfigurationTabContent();
        configurationTab.setContent(configurationTabContent);
        tabPane.getTabs().add(configurationTab);

        final Tab validationTab = makeNonclosableTab("Validation");
        tabPane.getTabs().add(validationTab);

        final Tab dataTab = makeNonclosableTab("Data");
        tabPane.getTabs().add(dataTab);

        final Tab reportsTab = makeNonclosableTab("Reports");
        tabPane.getTabs().add(reportsTab);

        return tabPane;
    }

    /**
     * Makes a non-closable tab for a tab pane.
     *
     * @param label the tab label
     * @return the tab
     */
    private static Tab makeNonclosableTab(final String label) {

        final Tab tab = new Tab();

        tab.setClosable(false);
        tab.setText(label);

        return tab;
    }

    /**
     * Creates the content of the configuration tab.
     *
     * @return the content
     */
    private Parent createConfigurationTabContent() {

        final BorderPane content = new BorderPane();

        final StackPane center = new StackPane();
        final BorderWidths centerPadding = new BorderWidths(20.0, 20.0, 20.0, 0.0);
        center.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, null, null, centerPadding)));

        final Font buttonFont = new Font(14.0);
        final Font headerFont = new Font(22.0);

        final BorderPane instancesDetail = new BorderPane();
        final Text instancesHeader = makeHeader("Instances", headerFont);
        instancesDetail.setTop(instancesHeader);
        instancesDetail.setVisible(false);

        final BorderPane profilesDetail = new BorderPane();
        final Text profilesHeader = makeHeader("Data Profiles", headerFont);
        profilesDetail.setTop(profilesHeader);
        profilesDetail.setVisible(false);

        final BorderPane webDetail = new BorderPane();
        final Text webHeader = makeHeader("Web Contexts", headerFont);
        webDetail.setTop(webHeader);
        webDetail.setVisible(false);

        final BorderPane codeDetail = new BorderPane();
        final Text codeHeader = makeHeader("Code Contexts", headerFont);
        codeDetail.setTop(codeHeader);
        codeDetail.setVisible(false);

        VBox codeDetailCenter = new VBox();
        HBox codeDetailCenterLeft = new HBox();
        codeDetailCenter.getChildren().add(codeDetailCenterLeft);
        codeDetail.setCenter(codeDetailCenter);


        try {
            final URL fxmlUrl = CodeContextPane.class.getResource("CodeContextPane.fxml");
            if (fxmlUrl == null) {
                Log.warning("CodeContextPane.fxml resource not found");
            } else {
                final HBox box = FXMLLoader.load(fxmlUrl);
                if (box != null) {
                    codeDetailCenterLeft.getChildren().add(box);
                }
            }
        } catch (final IOException ex) {
            Log.warning(ex);
        }

        final ObservableList<Node> centerChildren = center.getChildren();
        centerChildren.addAll(instancesDetail, profilesDetail, webDetail, codeDetail);

        content.setCenter(center);

        final VBox left = new VBox(14.0);
        left.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, null, null, new BorderWidths(20.0))));
        content.setLeft(left);

        final Button btn1 = makeSideButton("Instances", buttonFont, "servers48.png");
        btn1.setOnAction(e -> {showNodeHideNodes(centerChildren, instancesDetail);});

        final Button btn2 = makeSideButton("Data Profiles", buttonFont, "profiles48.png");
        btn2.setOnAction(e -> {showNodeHideNodes(centerChildren, profilesDetail);});

        final Button btn3 = makeSideButton("Web Contexts", buttonFont, "webcontexts48.png");
        btn3.setOnAction(e -> {showNodeHideNodes(centerChildren, webDetail);});

        final Button btn4 = makeSideButton("Code Contexts", buttonFont, "codecontexts48.png");
        btn4.setOnAction(e -> {showNodeHideNodes(centerChildren, codeDetail);});

        left.getChildren().addAll(btn1, btn2, btn3, btn4);

        final HBox bottom = new HBox(14.0);
        final BorderWidths bottomPadding = new BorderWidths(10.0, 20.0, 10.0, 20.0);
        bottom.setBorder(new Border(new BorderStroke(Color.TRANSPARENT, null, null, bottomPadding),
                new BorderStroke(Color.SILVER, BorderStrokeStyle.SOLID, null, new BorderWidths(1.0, 0.0, 0.0, 0.0))));
        content.setBottom(bottom);

        final Button btn5 = makeBottomButton("Apply Changes and Save to XML", buttonFont, "save_apply32.png");
        final Button btn6 = makeBottomButton("Discard Changes and Revert to Saved XML", buttonFont, "revert32.png");
        bottom.getChildren().addAll(btn5, btn6);

        return content;
    }

    /**
     * Generates a side button with label and icon.
     *
     * @param label the button label
     * @param font the button font
     * @param iconFilename the filename of the icon
     */
    private static Button makeSideButton(final String label, final Font font, final String iconFilename) {

        final Button btn = new Button(label);

        btn.setFont(font);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setGraphicTextGap(BUTTON_GAP);
        btn.setMaxWidth(Double.MAX_VALUE);

        try (final InputStream in = FileLoader.openInputStream(DbManager.class, iconFilename, true)) {
            final Image instancesImg = new Image(in);
            final ImageView instancesIcon = new ImageView(instancesImg);
            btn.setGraphic(instancesIcon);
        } catch (final IOException ex) {
            Log.warning("Failed to load ", label, "' icon from '", iconFilename, "'", ex);
        }

        return btn;
    }

    /**
     * Generates a bottom button with label and icon.
     *
     * @param label the button label
     * @param font the button font
     * @param iconFilename the filename of the icon
     */
    private static Button makeBottomButton(final String label, final Font font, final String iconFilename) {

        final Button btn = new Button(label);

        btn.setFont(font);
        btn.setGraphicTextGap(BUTTON_GAP);
        btn.setMaxWidth(Double.MAX_VALUE);

        try (final InputStream in = FileLoader.openInputStream(DbManager.class, iconFilename, true)) {
            final Image instancesImg = new Image(in);
            final ImageView instancesIcon = new ImageView(instancesImg);
            btn.setGraphic(instancesIcon);
        } catch (final IOException ex) {
            Log.warning("Failed to load '", label, "' icon from '", iconFilename, "'",  ex);
        }

        return btn;
    }

    /**
     * Makes a header.
     *
     * @param label the label
     * @param font the font
     * @return the header
     */
    private static Text makeHeader(final String label, final Font font) {

        final Text header =new Text(label);
        header.setFont(font);

        return header;
    }

    /**
     * Makes a single node in a list of nodes visible.
     *
     * @param nodes the list of nodes
     * @param nodeToShow the single node to make visible
     */
    private static void showNodeHideNodes(final Iterable<? extends Node> nodes, final Node nodeToShow) {

        for (final Node node : nodes) {
            final boolean equal = node.equals(nodeToShow);
            node.setVisible(equal);
        }
    }

    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments
     */
    public static void main(final String... args) {

        launch(args);
    }
}

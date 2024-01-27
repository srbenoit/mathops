package dev.mathops.app.db.config;

import dev.mathops.app.db.config.model.CfgDataProfileModel;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import javafx.scene.control.ComboBox;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.TextField;

/**
 * The controller for a pane to view/edit a code context.
 */
public final class CodeContextPane implements Initializable {

    @FXML // fx:id="idField"
    public TextField idField; // Value injected by FXMLLoader

    @FXML // fx:id="profileCombo"
    public ComboBox<CfgDataProfileModel> profileCombo; // Value injected by FXMLLoader

    /**
     * Constructs a new {@code CodeContextPane}.
     */
    public CodeContextPane() {

    }

    /**
     * This method is called by the FXMLLoader when initialization is complete
     *
     * @param url            the location of the FXML file
     * @param resourceBundle the resources
     */
    @Override
    public void initialize(final URL url, final ResourceBundle resourceBundle) {

        assert this.profileCombo != null : "fx:id=\"profileCombo\" was not injected: check 'CodeContextPane.fxml'.";


//        this.idField.textProperty().bind();




//
//        // populate the fruit combo box with item choices.
//        this.profileCombo.getItems().setAll("Apple", "Orange", "Pear");
//
//        // listen for changes to the fruit combo box selection and update the displayed fruit image accordingly.
//        this.profileCombo.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
//            @Override public void changed(ObservableValue<? extends String> selected, String oldFruit, String newFruit) {
//                if (oldFruit != null) {
//                    switch(oldFruit) {
//                        case "Apple": appleImage.setVisible(false); break;
//                        case "Orange": orangeImage.setVisible(false); break;
//                        case "Pear": pearImage.setVisible(false); break;
//                    }
//                }
//                if (newFruit != null) {
//                    switch(newFruit) {
//                        case "Apple": appleImage.setVisible(true); break;
//                        case "Orange": orangeImage.setVisible(true); break;
//                        case "Pear": pearImage.setVisible(true); break;
//                    }
//                }
//            }
//        });
    }

    /**
     * Called when the "Delete" button is pressed.
     */
    public void handleDeleteAction() {

        Log.info("Delete");
    }

    /**
     * Called when the "Delete" button is pressed.
     */
    public void handlePrintAction() {

        Log.info("Print");
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("CodeContextPane{idField=", this.idField, ", profileCombo=", this.profileCombo,
                "}");
    }
}

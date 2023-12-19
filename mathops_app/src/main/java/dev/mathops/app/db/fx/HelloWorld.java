package dev.mathops.app.db.fx;

import dev.mathops.app.AppFileLoader;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.net.URL;

public class HelloWorld extends Application {


    @Override
    public void start(final Stage primaryStage) {

        primaryStage.setTitle("Hello World!");
        Button btn = new Button("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(btn);

        final Scene scene = new Scene(root, 300, 250);
        final URL cssResource = AppFileLoader.getResource(HelloWorld.class, "dark.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toString());
        }

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
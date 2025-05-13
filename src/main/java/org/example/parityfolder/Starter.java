package org.example.parityfolder;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class Starter extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Starter.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Image image = new Image("icona.png");
        stage.getIcons().add(image);
        stage.setTitle("Updatinator");
        stage.setScene(scene);
        stage.show();
        HelloController cunt = (HelloController)fxmlLoader.getController();
        cunt.addListener();
    }

    public static void main(String[] args) {
        launch();
    }
}
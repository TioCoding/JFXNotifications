package com.notification;

import com.notification.notification.JFXNotifications;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Created by juancontinue on 26/09/17. CLARIO
 */
public class Main extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("DEMO NOTIFICATION JFOENIX");

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Demo.fxml"));
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        primaryStage.show();
    }

}

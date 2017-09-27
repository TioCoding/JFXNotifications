package com.notification.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXRadioButton;
import com.notification.notification.JFXNotifications;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.util.Duration;

/**
 * Created by Juan Junior on 27/09/17. Tio-Coding
 */
public class DemoController {

    @FXML
    private JFXRadioButton rbSuccess;

    @FXML
    private JFXRadioButton rbMessage;

    @FXML
    private JFXRadioButton rbWarning;

    @FXML
    private JFXRadioButton rbError;

    @FXML
    private JFXButton btnTopRight;

    @FXML
    private JFXButton btnTopLeft;

    @FXML
    private JFXButton btnTopCenter;

    @FXML
    private JFXButton btnCenterLeft;

    @FXML
    private JFXButton btnCenter;

    @FXML
    private JFXButton btnCenterRight;

    @FXML
    private JFXButton btnBottomLeft;

    @FXML
    private JFXButton btnBottomCenter;

    @FXML
    private JFXButton btnBottomRight;

    @FXML
    public void initialize() {
        btnTopRight.setOnAction(evt -> showNotification(Pos.TOP_RIGHT));
        btnTopLeft.setOnAction(evt -> showNotification(Pos.TOP_LEFT));
        btnTopCenter.setOnAction(evt -> showNotification(Pos.TOP_CENTER));
        btnCenterLeft.setOnAction(evt -> showNotification(Pos.CENTER_LEFT));
        btnCenter.setOnAction(evt -> showNotification(Pos.CENTER));
        btnCenterRight.setOnAction(evt -> showNotification(Pos.CENTER_RIGHT));
        btnBottomLeft.setOnAction(evt -> showNotification(Pos.BOTTOM_LEFT));
        btnBottomCenter.setOnAction(evt -> showNotification(Pos.BOTTOM_CENTER));
        btnBottomRight.setOnAction(evt -> showNotification(Pos.BOTTOM_RIGHT));
    }

    private void showNotification(Pos position) {
        if (rbSuccess.isSelected()) {
            JFXNotifications.create().title("Notificacion satisfactoria")
                    .text("Este es un mensaje de prueba, de satisfactorio R")
                    .hideAfter(Duration.seconds(5))
                    .position(position).showSuccess();
        }
        if (rbMessage.isSelected()) {
            JFXNotifications.create().title("Notificacion mensaje")
                    .text("Este es un mensaje de prueba, de mensaje")
                    .hideAfter(Duration.seconds(5))
                    .position(position).showMessage();
        }
        if (rbWarning.isSelected()) {
            JFXNotifications.create().title("Notificacion peligro")
                    .text("Este es un mensaje de prueba, de peligro")
                    .hideAfter(Duration.seconds(5))
                    .position(position).showWarning();
        }
        if (rbError.isSelected()) {
            JFXNotifications.create().title("Notificacion error")
                    .text("Este es un mensaje de prueba, de error")
                    .hideAfter(Duration.seconds(5))
                    .position(position).showError();
        }
    }

}

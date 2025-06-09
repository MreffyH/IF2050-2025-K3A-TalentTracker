package com.example.controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SelectionViewController {

    @FXML
    void handleCEOButton(ActionEvent event) throws IOException {
        loadScene(event, "/AttendanceDashboardCEO.fxml", "/stylesCEO.css");
    }

    @FXML
    void handleStaffButton(ActionEvent event) throws IOException {
        loadScene(event, "/AttendanceDashboardStaff.fxml", "/styles.css");
    }

    private void loadScene(ActionEvent event, String fxmlFile, String cssFile) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource(cssFile).toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.centerOnScreen();
        stage.show();
    }
} 
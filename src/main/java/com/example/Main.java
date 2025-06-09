package com.example;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load the FXML file
        Parent root = FXMLLoader.load(getClass().getResource("/AttendanceDashboard.fxml"));
        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        
        // Set up the stage (the window)
        primaryStage.setMaximized(true); // Make the window full screen (Deleted Later)
        primaryStage.setTitle("AttendanceDashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
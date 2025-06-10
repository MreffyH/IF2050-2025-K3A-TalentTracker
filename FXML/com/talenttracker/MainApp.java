package com.talenttracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/talenttracker/view/AddProject.fxml")); // Adjust path as needed
            primaryStage.setTitle("TalentTracker - Add New Project");
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/talenttracker/view/style.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

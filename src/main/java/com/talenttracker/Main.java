package com.talenttracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.text.Font;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Create the main container
            BorderPane mainContainer = new BorderPane();
            mainContainer.setStyle("-fx-background-color: #FFB7CA;");

            // Load and set the header
            FXMLLoader headerLoader = new FXMLLoader(getClass().getResource("/view/HeaderView.fxml"));
            Parent header = headerLoader.load();
            mainContainer.setTop(header);

            // Load and set the dashboard content
            FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("/view/DashboardView.fxml"));
            Parent dashboard = dashboardLoader.load();
            mainContainer.setCenter(dashboard);

            // Create the scene
            Scene scene = new Scene(mainContainer, 1920, 1080);
            
            // Load CSS
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            primaryStage.setTitle("Talent Tracker");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Error loading FXML files:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
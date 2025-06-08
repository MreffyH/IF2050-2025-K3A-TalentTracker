package com.talenttracker;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import java.io.IOException;

public class Main extends Application {

    private static String loggedInUserRole;
    private static String loggedInUserFullName;

    @Override
    public void start(Stage primaryStage) throws IOException {
        // DatabaseManager.setupDatabase(); // No longer needed
        Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        primaryStage.setTitle("Talent Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void setLoggedInUser(String role, String fullName) {
        loggedInUserRole = role;
        loggedInUserFullName = fullName;
    }

    public static String getLoggedInUserRole() {
        return loggedInUserRole;
    }

    public static String getLoggedInUserFullName() {
        return loggedInUserFullName;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
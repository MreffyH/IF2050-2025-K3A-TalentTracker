package com.talenttracker;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static String loggedInUserRole;
    private static String loggedInUserFullName;
    private static int loggedInUserId;

    public static String getLoggedInUserFullName() {
        return loggedInUserFullName;
    }

    public static int getLoggedInUserId() {
        return loggedInUserId;
    }

    public static String getLoggedInUserRole() {
        return loggedInUserRole;
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static void setLoggedInUser(int id, String fullName, String role) {
        loggedInUserId = id;
        loggedInUserFullName = fullName;
        loggedInUserRole = role;
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        primaryStage.setTitle("Talent Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
} 

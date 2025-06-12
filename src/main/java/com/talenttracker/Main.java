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
    private static int loggedInUserId;

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        primaryStage.setTitle("Talent Tracker");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void setLoggedInUser(int id, String fullName, String role) {
        loggedInUserId = id;
        loggedInUserRole = role;
        loggedInUserFullName = fullName;
    }

    public static String getLoggedInUserRole() {
        return loggedInUserRole;
    }

    public static String getLoggedInUserFullName() {
        return loggedInUserFullName;
    }

    public static int getLoggedInUserId() {
        return loggedInUserId;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
package com.talenttracker;

import java.io.File;
import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Path to the FXML file in the FXML folder
        URL fxmlUrl = new File("FXML/Project.fxml").toURI().toURL();
        Parent root = FXMLLoader.load(fxmlUrl);
        
        primaryStage.setTitle("TalentTracker - Add New Project");
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
} 
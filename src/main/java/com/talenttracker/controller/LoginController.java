package com.talenttracker.controller;

import com.talenttracker.Main;
import com.talenttracker.DatabaseManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Email and password cannot be empty.");
            return;
        }

        String[] userInfo = DatabaseManager.verifyUser(email, password);

        if (userInfo != null) {
            int userId = Integer.parseInt(userInfo[0]);
            String role = userInfo[1];
            String fullName = userInfo[2];
            
            Main.setLoggedInUser(userId, role, fullName);

            try {
                String viewPath;
                if ("Artist".equalsIgnoreCase(role)) {
                    viewPath = "/view/DashboardViewArtist.fxml";
                } else if ("CEO".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role)) {
                    viewPath = "/view/DashboardView.fxml";
                } else {
                    errorLabel.setText("Unknown role: " + role);
                    return;
                }
                
                // Load the main layout
                FXMLLoader mainLoader = new FXMLLoader(getClass().getResource("/view/MainLayout.fxml"));
                BorderPane mainLayout = mainLoader.load();

                // Load the dashboard view
                FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource(viewPath));
                Parent dashboardView = dashboardLoader.load();

                // Set the dashboard as the center of the main layout
                mainLayout.setCenter(dashboardView);

                Stage stage = (Stage) emailField.getScene().getWindow();
                Scene scene = new Scene(mainLayout, 1920, 1080);
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                errorLabel.setText("Failed to load dashboard view.");
            }
        } else {
            errorLabel.setText("Invalid email or password.");
        }
    }
} 
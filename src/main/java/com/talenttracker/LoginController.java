package com.talenttracker;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private CheckBox showPasswordCheckBox;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerLink;

    @FXML
    public void initialize() {
        // Bind visibility of password fields to checkbox
        visiblePasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        
        visiblePasswordField.managedProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordField.managedProperty().bind(showPasswordCheckBox.selectedProperty().not());

        // Bind text content of both fields together
        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        // Add listeners to check for input
        emailField.textProperty().addListener((obs, oldVal, newVal) -> updateButtonState());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> updateButtonState());

        // Set initial state of the button
        updateButtonState();

        loginButton.setOnAction(event -> handleLogin());
    }

    private void updateButtonState() {
        boolean allFieldsFilled = !emailField.getText().trim().isEmpty() &&
                                  !passwordField.getText().trim().isEmpty();

        if (allFieldsFilled) {
            loginButton.setStyle("-fx-background-radius: 32; -fx-background-color: #333333; -fx-font-size: 22; -fx-font-weight: bold; -fx-opacity: 1.0;");
            loginButton.setDisable(false);
        } else {
            loginButton.setStyle("-fx-background-radius: 32; -fx-background-color: #BDBDBD; -fx-font-size: 22; -fx-font-weight: bold; -fx-opacity: 1.0;");
            loginButton.setDisable(true);
        }
    }

    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        String[] userInfo = DatabaseManager.verifyUser(email, password);

        if (userInfo != null) {
            // Successful login
            String userRole = userInfo[0];
            String fullName = userInfo[1];
            showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + fullName + "!");
            // Here you would typically navigate to another screen based on the role
            // For now, we'll just show a success message.
            // Example: openDashboard(userRole);
            try {
                Stage stage = (Stage) loginButton.getScene().getWindow();
                Parent root = FXMLLoader.load(getClass().getResource("dashboard.fxml"));
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Failed login
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void openRegisterScreen() throws IOException {
        Stage stage = (Stage) registerLink.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("register.fxml"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
} 
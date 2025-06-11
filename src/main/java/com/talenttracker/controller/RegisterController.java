package com.talenttracker.controller;

import com.talenttracker.dao.UserDAO;
import java.io.IOException;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class RegisterController {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField visiblePasswordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private CheckBox showPasswordCheckBox;

    @FXML
    private Button createAccountButton;

    @FXML
    private Hyperlink loginLink;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        visiblePasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        
        visiblePasswordField.managedProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordField.managedProperty().bind(showPasswordCheckBox.selectedProperty().not());

        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> updateButtonState());
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> updateButtonState());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> updateButtonState());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> updateButtonState());
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateButtonState());

        updateButtonState();

        createAccountButton.setOnAction(event -> {
            try {
                handleRegister();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateButtonState() {
        boolean allFieldsFilled = !firstNameField.getText().trim().isEmpty() &&
                                  !lastNameField.getText().trim().isEmpty() &&
                                  !emailField.getText().trim().isEmpty() &&
                                  !passwordField.getText().trim().isEmpty() &&
                                  roleComboBox.getValue() != null;

        if (allFieldsFilled) {
            createAccountButton.setStyle("-fx-background-radius: 32; -fx-background-color: #333333; -fx-font-size: 22; -fx-opacity: 1.0;");
            createAccountButton.setDisable(false);
        } else {
            createAccountButton.setStyle("-fx-background-radius: 32; -fx-background-color: #BDBDBD; -fx-font-size: 22; -fx-opacity: 1.0;");
            createAccountButton.setDisable(true);
        }
    }

    private void handleRegister() throws IOException {
        String fullName = firstNameField.getText() + " " + lastNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleComboBox.getValue();

        try {
            boolean success = userDAO.addUser(fullName, email, password, role);

            if (success) {
                showAlert(AlertType.INFORMATION, "Registration Successful!", "You can now log in with your new account.")
                    .ifPresent(response -> {
                        try {
                            openLoginScreen();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            } else {
                showAlert(AlertType.ERROR, "Registration Failed", "Could not create account. The email might already be in use.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Database Error", "An error occurred during registration. Please try again.");
        }
    }

    private java.util.Optional<javafx.scene.control.ButtonType> showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }

    @FXML
    private void openLoginScreen() throws IOException {
        Stage stage = (Stage) loginLink.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/view/login.fxml"));
        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
} 
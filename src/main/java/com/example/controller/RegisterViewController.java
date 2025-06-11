package com.example.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.example.dao.UserDAO;
import com.example.model.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class RegisterViewController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ChoiceBox<String> roleChoiceBox;

    @FXML
    private Text messageText;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void register() {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String role = roleChoiceBox.getValue();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
            messageText.setText("All fields are required.");
            return;
        }

        // Basic email validation
        if (!email.matches("^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$")) {
            messageText.setText("Invalid email format.");
            return;
        }

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);

        try {
            userDAO.addUser(user);
            messageText.setText("Registration successful!");
            goToLogin();
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) { // MySQL error code for duplicate entry
                messageText.setText("Email already exists.");
            } else {
                messageText.setText("Database error. Please check connection details.");
            }
            e.printStackTrace();
        } catch (IOException e) {
            messageText.setText("Failed to go to login page.");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToLogin() throws IOException {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/LoginView.fxml"));
        Scene scene = new Scene(root, 1920, 1080);
        stage.setScene(scene);
    }
} 
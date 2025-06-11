package com.example.controller;

import java.io.IOException;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import com.example.dao.UserDAO;
import com.example.model.User;
import com.example.util.SessionManager;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class LoginViewController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Text messageText;

    private UserDAO userDAO = new UserDAO();

    @FXML
    private void login() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            messageText.setText("Email and password are required.");
            return;
        }

        try {
            User user = userDAO.getUserByEmail(email);

            if (user != null && BCrypt.checkpw(password, user.getPassword())) {
                messageText.setText("Login successful!");
                // Store user in session
                SessionManager.getInstance().setLoggedInUser(user);
                // Navigate to the appropriate dashboard
                navigateToDashboard(user.getRole());
            } else {
                messageText.setText("Invalid email or password.");
            }
        } catch (SQLException e) {
            messageText.setText("Database error. Please check connection details.");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToRegister() throws IOException {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/RegisterView.fxml"));
        Scene scene = new Scene(root, 1920, 1080);
        stage.setScene(scene);
    }

    private void navigateToDashboard(String role) {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            Parent root;
            if ("CEO".equalsIgnoreCase(role)) {
                root = FXMLLoader.load(getClass().getResource("/AttendanceDashboardCEO.fxml"));
            } else {
                root = FXMLLoader.load(getClass().getResource("/AttendanceDashboardStaff.fxml"));
            }
            Scene scene = new Scene(root, 1920, 1080);
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
            messageText.setText("Failed to load dashboard.");
        }
    }
} 
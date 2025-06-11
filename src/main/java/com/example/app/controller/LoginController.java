package com.example.app.controller;

import java.io.IOException;
import java.util.List;

import com.example.app.dao.UserDAO;
import com.example.app.dao.UserDAOImpl;
import com.example.app.model.User;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private ComboBox<String> roleComboBox;
    private UserDAO userDAO;

    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("CEO", "Staff"));
        userDAO = new UserDAOImpl();
    }

    @FXML
    private void handleLoginButtonAction() {
        String selectedRole = roleComboBox.getValue();
        if (selectedRole == null) {
            showAlert("Login Error", "Please select a role.");
            return;
        }

        List<User> users = userDAO.getUsersByRole(selectedRole);
        if (users.isEmpty()) {
            showAlert("Login Error", "No user found in the database for the role: " + selectedRole);
            return;
        }
        
        // For this placeholder, we log in as the first user found with the selected role.
        User loggedInUser = users.get(0);

        try {
            Stage loginStage = (Stage) roleComboBox.getScene().getWindow();
            loginStage.close();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/app/view/projek.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.initializeWithUser(loggedInUser);

            Stage mainStage = new Stage();
            mainStage.setTitle("Project Management System");
            Scene scene = new Scene(root, 1920, 1080);
            mainStage.setMaximized(true);
            scene.getStylesheets().add(getClass().getResource("/com/example/app/view/styles.css").toExternalForm());
            mainStage.setScene(scene);
            mainStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Could not load the main application window.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 
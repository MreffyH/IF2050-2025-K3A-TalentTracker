package com.talenttracker.controller;

import com.talenttracker.Main;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.User;
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
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.sql.SQLException;

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

    private final UserDAO userDAO = new UserDAO();

    @FXML
    public void initialize() {
        visiblePasswordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordField.visibleProperty().bind(showPasswordCheckBox.selectedProperty().not());
        
        visiblePasswordField.managedProperty().bind(showPasswordCheckBox.selectedProperty());
        passwordField.managedProperty().bind(showPasswordCheckBox.selectedProperty().not());

        visiblePasswordField.textProperty().bindBidirectional(passwordField.textProperty());

        emailField.textProperty().addListener((obs, oldVal, newVal) -> updateButtonState());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> updateButtonState());

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

        try {
            User user = userDAO.verifyUser(email, password);

            if (user != null) {
                Main.setLoggedInUser(user.getId(), user.getRole(), user.getFullName());

                showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + user.getFullName() + "!");

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainLayout.fxml"));
                    BorderPane mainLayout = loader.load();

                    String viewPath = "Artist".equalsIgnoreCase(user.getRole()) ? "/view/DashboardViewArtist.fxml" : "/view/DashboardView.fxml";
                    FXMLLoader contentLoader = new FXMLLoader(getClass().getResource(viewPath));
                    mainLayout.setCenter(contentLoader.load());

                    if ("Artist".equalsIgnoreCase(user.getRole())) {
                        DashboardArtistController artistController = contentLoader.getController();
                        artistController.setArtistId(Main.getLoggedInUserId());
                    }

                    Stage stage = (Stage) loginButton.getScene().getWindow();
                    Scene scene = new Scene(mainLayout, 1920, 1080);
                    scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                    stage.setScene(scene);
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load the dashboard.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid email or password.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "An error occurred while trying to log in.");
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
        Parent root = FXMLLoader.load(getClass().getResource("/view/register.fxml"));
        Scene scene = new Scene(root, 1920, 1080);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
} 
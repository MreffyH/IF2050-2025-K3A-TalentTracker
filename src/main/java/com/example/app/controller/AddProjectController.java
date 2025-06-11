package com.example.app.controller;

import com.example.app.dao.ProjectDAO;
import com.example.app.dao.ProjectDAOImpl;
import com.example.app.dao.UserDAO;
import com.example.app.dao.UserDAOImpl;
import com.example.app.model.Project;
import com.example.app.model.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AddProjectController {

    @FXML private TextField projectIdField;
    @FXML private TextField projectNameField;
    @FXML private TextArea projectDescriptionArea;
    @FXML private ComboBox<String> projectTypeComboBox;
    @FXML private ComboBox<User> ceoComboBox;
    @FXML private ComboBox<User> staffComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    private ProjectDAO projectDAO;
    private UserDAO userDAO;

    public AddProjectController() {
        this.projectDAO = new ProjectDAOImpl();
        this.userDAO = new UserDAOImpl();
    }

    @FXML
    public void initialize() {
        projectTypeComboBox.getItems().addAll("Collab", "Live", "Comeback");
        projectTypeComboBox.setValue("Collab");

        // Load CEOs and Staff into ComboBoxes
        List<User> ceos = userDAO.getUsersByRole("CEO");
        ceoComboBox.setItems(FXCollections.observableArrayList(ceos));

        List<User> staff = userDAO.getUsersByRole("Staff");
        staffComboBox.setItems(FXCollections.observableArrayList(staff));
    }

    @FXML
    private void handleSubmitButtonAction() {
        try {
            User selectedCEO = ceoComboBox.getValue();
            User selectedStaff = staffComboBox.getValue();

            if (selectedCEO == null || selectedStaff == null) {
                showAlert(Alert.AlertType.ERROR, "Form Error!", "Please select a CEO and a Staff member.");
                return;
            }

            Project newProject = new Project();
            newProject.setIdProject(Integer.parseInt(projectIdField.getText()));
            newProject.setProjectName(projectNameField.getText());
            newProject.setDescription(projectDescriptionArea.getText());
            newProject.setType(projectTypeComboBox.getValue());
            newProject.setIdCEO(selectedCEO.getIdUser());
            newProject.setIdStaff(selectedStaff.getIdUser());

            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                showAlert(Alert.AlertType.ERROR, "Form Error!", "Start and End dates are required.");
                return;
            }

            newProject.setStartDate(LocalDateTime.of(startDate, LocalTime.MIDNIGHT));
            newProject.setEndDate(LocalDateTime.of(endDate, LocalTime.MIDNIGHT));

            boolean success = projectDAO.addProject(newProject);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Project added successfully!");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add project. The Project ID might already exist, or another database rule was violated.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Form Error!", "Please enter a valid number for the Project ID.");
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) projectNameField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 
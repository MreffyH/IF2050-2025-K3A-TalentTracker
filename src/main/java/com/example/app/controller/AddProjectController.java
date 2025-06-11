package com.example.app.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.app.dao.ProjectArtistDAO;
import com.example.app.dao.ProjectArtistDAOImpl;
import com.example.app.dao.ProjectDAO;
import com.example.app.dao.ProjectDAOImpl;
import com.example.app.dao.UserDAO;
import com.example.app.dao.UserDAOImpl;
import com.example.app.model.Project;
import com.example.app.model.User;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class AddProjectController {

    @FXML private TextField projectIdField;
    @FXML private TextField projectNameField;
    @FXML private TextArea projectDescriptionArea;
    @FXML private ComboBox<String> projectTypeComboBox;
    @FXML private ComboBox<User> staffComboBox;
    @FXML private ComboBox<User> artistComboBox;
    @FXML private FlowPane assignedArtistsPane;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    private ProjectDAO projectDAO;
    private UserDAO userDAO;
    private ProjectArtistDAO projectArtistDAO;

    private ObservableList<User> availableArtists = FXCollections.observableArrayList();
    private List<User> assignedArtists = new ArrayList<>();
    private User loggedInCEO;

    public AddProjectController() {
        this.projectDAO = new ProjectDAOImpl();
        this.userDAO = new UserDAOImpl();
        this.projectArtistDAO = new ProjectArtistDAOImpl();
    }

    @FXML
    public void initialize() {
        projectTypeComboBox.getItems().addAll("Collab", "Live", "Comeback");
        projectTypeComboBox.setValue("Collab");

        staffComboBox.setItems(FXCollections.observableArrayList(userDAO.getUsersByRole("Staff")));
        
        availableArtists.setAll(userDAO.getUsersByRole("Artist"));
        artistComboBox.setItems(availableArtists);
    }

    public void setLoggedInCEO(User ceo) {
        this.loggedInCEO = ceo;
    }

    @FXML
    private void handleAddArtistAction() {
        User selectedArtist = artistComboBox.getValue();
        if (selectedArtist != null) {
            assignedArtists.add(selectedArtist);
            availableArtists.remove(selectedArtist);
            artistComboBox.getSelectionModel().clearSelection();
            createArtistTag(selectedArtist);
        }
    }

    private void createArtistTag(User artist) {
        Label nameLabel = new Label(artist.getFullName());
        Button removeButton = new Button("x");
        removeButton.getStyleClass().add("remove-button");
        HBox tag = new HBox(nameLabel, removeButton);
        tag.getStyleClass().add("artist-tag");
        tag.setSpacing(5);

        removeButton.setOnAction(event -> {
            assignedArtists.remove(artist);
            availableArtists.add(artist);
            assignedArtistsPane.getChildren().remove(tag);
        });

        assignedArtistsPane.getChildren().add(tag);
    }

    @FXML
    private void handleSubmitButtonAction() {
        try {
            User selectedStaff = staffComboBox.getValue();

            if (loggedInCEO == null || selectedStaff == null || assignedArtists.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Form Error!", "A logged-in CEO, a selected Staff member, and at least one Artist are required.");
                return;
            }

            Project newProject = new Project();
            newProject.setIdProject(Integer.parseInt(projectIdField.getText()));
            newProject.setProjectName(projectNameField.getText());
            newProject.setDescription(projectDescriptionArea.getText());
            newProject.setType(projectTypeComboBox.getValue());
            newProject.setIdCEO(loggedInCEO.getIdUser());
            newProject.setIdStaff(selectedStaff.getIdUser());
            
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                showAlert(Alert.AlertType.ERROR, "Form Error!", "Start and End dates are required.");
                return;
            }

            newProject.setStartDate(LocalDateTime.of(startDate, LocalTime.MIDNIGHT));
            newProject.setEndDate(LocalDateTime.of(endDate, LocalTime.MIDNIGHT));

            boolean projectSuccess = projectDAO.addProject(newProject);

            if (projectSuccess) {
                List<Integer> artistIds = assignedArtists.stream()
                                                         .map(User::getIdUser)
                                                         .collect(Collectors.toList());
                projectArtistDAO.addArtistsToProject(newProject.getIdProject(), artistIds);
                
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
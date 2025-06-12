package com.talenttracker.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.User;
import com.talenttracker.dao.ProjectArtistDAO;
import com.talenttracker.dao.ProjectDAO;
import com.talenttracker.model.Project;

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
import javafx.util.StringConverter;

import java.sql.SQLException;

public class AddProjectController {

    @FXML private Label nextIdLabel;
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
        this.projectDAO = new ProjectDAO();
        this.userDAO = new UserDAO();
        this.projectArtistDAO = new ProjectArtistDAO();
    }

    @FXML
    public void initialize() {
        projectTypeComboBox.getItems().addAll("Collab", "Live", "Comeback");
        projectTypeComboBox.setValue("Collab");

        StringConverter<User> userConverter = new StringConverter<>() {
            @Override
            public String toString(User user) {
                return user == null ? "" : user.getFullName();
            }

            @Override
            public User fromString(String string) {
                return null; // Not needed for selection
            }
        };

        staffComboBox.setConverter(userConverter);
        artistComboBox.setConverter(userConverter);

        try {
            staffComboBox.setItems(FXCollections.observableArrayList(userDAO.getUsersByRole("Staff")));
            availableArtists.setAll(userDAO.getUsersByRole("Artist"));
            artistComboBox.setItems(availableArtists);

            int nextId = projectDAO.getNextProjectId();
            nextIdLabel.setText("Suggested ID: " + nextId);
            projectIdField.setText(String.valueOf(nextId));
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load initial data.");
        }
    }

    public void setLoggedInCEO(User ceo) {
        this.loggedInCEO = ceo;
    }

    @FXML
    public void handleAddArtistAction() {
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
    public void handleSubmitButtonAction() {
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
            newProject.setIdCEO(loggedInCEO.getId());
            newProject.setIdStaff(selectedStaff.getId());
            
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
                                                         .map(User::getId)
                                                         .collect(Collectors.toList());
                projectArtistDAO.addArtistsToProject(newProject.getIdProject(), artistIds);
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Project added successfully!");
                closeWindow();
            } else {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add project. The Project ID might already exist, or another database rule was violated.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Form Error!", "Please enter a valid number for the Project ID.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "A database error occurred while adding the project.");
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
package com.example.app.controller;

import com.example.app.model.Project;
import com.example.app.model.Staff;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AddProjectController {

    @FXML private TextField projectTitleField;
    @FXML private TextArea projectDescriptionArea;
    @FXML private ComboBox<String> projectTypeComboBox;
    @FXML private DatePicker projectDeadlinePicker;
    @FXML private TextField staffInputField;
    @FXML private FlowPane selectedStaffFlowPane;
    @FXML private VBox staffSuggestionsVBox;

    private List<Staff> allStaff = new ArrayList<>();
    private List<Staff> selectedStaff = new ArrayList<>();
    private Project newProject;

    @FXML
    public void initialize() {
        projectTypeComboBox.getItems().addAll("Collab", "Live", "Comeback");
        setupDummyStaff();
        setupStaffAutocomplete();
    }
    
    private void setupDummyStaff() {
        allStaff.add(new Staff("s001", "John Doe"));
        allStaff.add(new Staff("s002", "Jane Smith"));
        allStaff.add(new Staff("s003", "Peter Jones"));
        allStaff.add(new Staff("s004", "Mary Williams"));
        allStaff.add(new Staff("s005", "David Brown"));
    }
    
    private void setupStaffAutocomplete() {
        staffInputField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.trim().isEmpty()) {
                staffSuggestionsVBox.setVisible(false);
            } else {
                List<Staff> suggestions = allStaff.stream()
                    .filter(staff -> staff.getName().toLowerCase().contains(newValue.toLowerCase()) && !selectedStaff.contains(staff))
                    .collect(Collectors.toList());
                populateStaffSuggestions(suggestions);
                staffSuggestionsVBox.setVisible(!suggestions.isEmpty());
            }
        });
    }
    
    private void populateStaffSuggestions(List<Staff> suggestions) {
        staffSuggestionsVBox.getChildren().clear();
        for (Staff staff : suggestions) {
            Label suggestionLabel = new Label(staff.getName());
            suggestionLabel.getStyleClass().add("staff-suggestion"); // For styling
            suggestionLabel.setOnMouseClicked(event -> {
                selectStaff(staff);
                staffInputField.clear();
                staffSuggestionsVBox.setVisible(false);
            });
            staffSuggestionsVBox.getChildren().add(suggestionLabel);
        }
    }
    
    private void selectStaff(Staff staff) {
        selectedStaff.add(staff);
        addStaffTag(staff);
    }
    
    private void addStaffTag(Staff staff) {
        Label tag = new Label(staff.getName());
        Button removeBtn = new Button("x");
        HBox tagBox = new HBox(tag, removeBtn);
        tagBox.getStyleClass().add("staff-tag"); // For styling
        
        removeBtn.setOnAction(event -> {
            selectedStaff.remove(staff);
            selectedStaffFlowPane.getChildren().remove(tagBox);
        });
        
        selectedStaffFlowPane.getChildren().add(tagBox);
    }

    public Project getNewProject() {
        return newProject;
    }

    @FXML
    private void handleSubmitButtonAction() {
        newProject = new Project(
            projectTitleField.getText(),
            projectDescriptionArea.getText(),
            projectDeadlinePicker.getValue(),
            projectTypeComboBox.getValue(),
            new ArrayList<>(selectedStaff)
        );
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) projectTitleField.getScene().getWindow();
        stage.close();
    }
} 
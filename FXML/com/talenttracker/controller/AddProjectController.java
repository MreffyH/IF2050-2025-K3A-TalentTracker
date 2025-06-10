package com.talenttracker.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import javafx.scene.layout.VBox;

public class AddProjectController {

    @FXML
    private TextField projectTitleField;
    @FXML
    private TextArea projectDescriptionArea;
    @FXML
    private ComboBox<String> projectTypeComboBox;
    @FXML
    private DatePicker projectDeadlinePicker;
    @FXML
    private TextField staffInputField;
    @FXML
    private FlowPane selectedStaffFlowPane;
    @FXML
    private VBox staffSuggestionsVBox;

    // Sample staff database (Replace with actual data source like a database or API)
    private final List<Staff> staffDatabase = new ArrayList<>(List.of(
            new Staff("ST001", "John Smith", "Producer"),
            new Staff("ST002", "Sarah Johnson", "Sound Engineer"),
            new Staff("ST003", "Mike Chen", "Video Director"),
            new Staff("ST004", "Emily Davis", "Marketing Manager"),
            new Staff("ST005", "Alex Rodriguez", "Choreographer"),
            new Staff("ST006", "Lisa Wang", "Stylist"),
            new Staff("ST007", "David Brown", "Photographer"),
            new Staff("ST008", "Anna Kim", "Social Media Manager")
    ));

    private final ObservableList<Staff> selectedStaff = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Populate Project Type ComboBox
        projectTypeComboBox.setItems(FXCollections.observableArrayList(
                "Collaboration", "Live Performance", "Comeback"
        ));

        // Staff input listener for suggestions
        staffInputField.textProperty().addListener((observable, oldValue, newValue) -> {
            showStaffSuggestions(newValue);
        });

        // Add dummy staff input field to flowpane initially
        // The staffInputField is already defined in FXML, so we just manage its visibility and order
        // selectedStaffFlowPane.getChildren().add(staffInputField);

        // Hide suggestions when staffInputField loses focus
        staffInputField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { // If focus is lost
                // A small delay to allow click event on suggestions to register
                new Thread(() -> {
                    try {
                        Thread.sleep(100); // Adjust as needed
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!staffSuggestionsVBox.isHover()) { // Only hide if mouse isn't over suggestions
                        staffSuggestionsVBox.setVisible(false);
                        staffSuggestionsVBox.setManaged(false);
                    }
                }).start();
            }
        });
    }

    private void showStaffSuggestions(String query) {
        staffSuggestionsVBox.getChildren().clear();
        if (query.isEmpty()) {
            staffSuggestionsVBox.setVisible(false);
            staffSuggestionsVBox.setManaged(false);
            return;
        }

        List<Staff> matches = staffDatabase.stream()
                .filter(staff -> (staff.getName().toLowerCase().contains(query.toLowerCase()) ||
                                 staff.getId().toLowerCase().contains(query.toLowerCase())) &&
                                 !selectedStaff.contains(staff))
                .collect(Collectors.toList());

        if (!matches.isEmpty()) {
            for (Staff staff : matches) {
                Label suggestionLabel = new Label(staff.getName() + " (" + staff.getId() + ") - " + staff.getRole());
                suggestionLabel.getStyleClass().add("staff-suggestion");
                suggestionLabel.setOnMouseClicked(event -> {
                    addStaffTag(staff);
                    staffInputField.clear();
                    staffSuggestionsVBox.setVisible(false);
                    staffSuggestionsVBox.setManaged(false);
                });
                staffSuggestionsVBox.getChildren().add(suggestionLabel);
            }
            staffSuggestionsVBox.setVisible(true);
            staffSuggestionsVBox.setManaged(true);
        } else {
            staffSuggestionsVBox.setVisible(false);
            staffSuggestionsVBox.setManaged(false);
        }
    }

    private void addStaffTag(Staff staff) {
        if (!selectedStaff.contains(staff)) {
            selectedStaff.add(staff);
            updateSelectedStaffTags();
        }
    }

    private void removeStaffTag(Staff staff) {
        selectedStaff.remove(staff);
        updateSelectedStaffTags();
    }

    private void updateSelectedStaffTags() {
        // Clear all existing tags except the staffInputField
        List<javafx.scene.Node> nodesToRemove = selectedStaffFlowPane.getChildren().stream()
                                                    .filter(node -> node != staffInputField)
                                                    .collect(Collectors.toList());
        selectedStaffFlowPane.getChildren().removeAll(nodesToRemove);


        // Add selected staff as tags before the input field
        for (Staff staff : selectedStaff) {
            HBox tag = new HBox(5); // 5 is spacing
            tag.getStyleClass().add("staff-tag");
            Label nameLabel = new Label(staff.getName() + " (" + staff.getRole() + ")");
            Button removeButton = new Button("×");
            removeButton.getStyleClass().add("remove-btn");
            removeButton.setOnAction(event -> removeStaffTag(staff));
            tag.getChildren().addAll(nameLabel, removeButton);
            selectedStaffFlowPane.getChildren().add(selectedStaffFlowPane.getChildren().size() -1, tag); // Insert before staffInputField
        }
    }

    @FXML
    private void handleSubmitButtonAction() {
        String title = projectTitleField.getText();
        String description = projectDescriptionArea.getText();
        String type = projectTypeComboBox.getValue();
        LocalDate deadline = projectDeadlinePicker.getValue();
        List<Staff> assignedStaff = new ArrayList<>(selectedStaff); // Copy of selected staff

        if (title.isEmpty() || description.isEmpty() || type == null || deadline == null || assignedStaff.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Missing Information");
            alert.setHeaderText(null);
            alert.setContentText("Please fill in all project details and assign at least one staff member.");
            alert.showAndWait();
            return;
        }

        System.out.println("Project Title: " + title);
        System.out.println("Project Description: " + description);
        System.out.println("Project Type: " + type);
        System.out.println("Project Deadline: " + deadline);
        System.out.println("Assigned Staff:");
        assignedStaff.forEach(staff -> System.out.println("  - " + staff.getName() + " (" + staff.getId() + ")"));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Project Creation Status");
        alert.setHeaderText(null);
        alert.setContentText("Project created successfully!");
        alert.showAndWait();

        // Clear form after submission
        projectTitleField.clear();
        projectDescriptionArea.clear();
        projectTypeComboBox.getSelectionModel().clearSelection();
        projectDeadlinePicker.setValue(null);
        selectedStaff.clear();
        updateSelectedStaffTags(); // Clear staff tags
    }

    // Simple Staff data model
    private static class Staff {
        private final String id;
        private final String name;
        private final String role;

        public Staff(String id, String name, String role) {
            this.id = id;
            this.name = name;
            this.role = role;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getRole() {
            return role;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Staff staff = (Staff) o;
            return id.equals(staff.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }
    }
}

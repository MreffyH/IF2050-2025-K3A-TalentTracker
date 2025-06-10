package com.talenttracker.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;


public class ProjectController {

    @FXML
    private TextField nameField;

    @FXML
    private TextArea descriptionField;

    @FXML
    private DatePicker deadlinePicker;

    @FXML
    private TextField staffField;

    @FXML
    public void initialize() {
        // Format the date picker to show DD/MM/YY
        StringConverter<LocalDate> converter = new StringConverter<>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yy");

            @Override
            public String toString(LocalDate date) {
                return (date != null) ? dateFormatter.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty())
                        ? LocalDate.parse(string, dateFormatter)
                        : null;
            }
        };
        deadlinePicker.setConverter(converter);
    }

    @FXML
    void handleNavigation(ActionEvent event) {
        String page = ((Button) event.getSource()).getText();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Navigation");
        alert.setHeaderText(null);
        alert.setContentText("Navigating to " + page);
        alert.showAndWait();
    }

    @FXML
    void handleAddProject(ActionEvent event) {
        if (nameField.getText().trim().isEmpty()) {
            showError("Project name is required");
            return;
        }
        if (descriptionField.getText().trim().isEmpty()) {
            showError("Project description is required");
            return;
        }
        if (deadlinePicker.getValue() == null) {
            showError("Please select a deadline date");
            return;
        }
        if (staffField.getText().trim().isEmpty()) {
            showError("Staff information is required");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Project added successfully!");
        alert.showAndWait();

        nameField.clear();
        descriptionField.clear();
        deadlinePicker.setValue(null);
        staffField.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 
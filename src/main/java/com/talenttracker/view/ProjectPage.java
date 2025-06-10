package com.talenttracker.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ProjectPage extends VBox {
    private TextField nameField;
    private TextArea descriptionField;
    private DatePicker deadlinePicker;
    private TextField staffField;
    
    public ProjectPage() {
        // Set up the main container
        this.setStyle("-fx-background-color: #FFD1DC;"); // Light pink background
        this.setSpacing(20);
        this.setPadding(new Insets(0, 0, 20, 0));

        // Add header
        HBox header = createHeader();
        
        // Add content
        VBox content = createContent();

        // Add all to main container
        this.getChildren().addAll(header, content);
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setStyle("-fx-background-color: #1a237e;"); // Dark blue header
        header.setSpacing(20);

        // Create logo placeholder
        Rectangle logo = new Rectangle(40, 40);
        logo.setFill(Color.WHITE);
        header.getChildren().add(logo);

        // Navigation buttons
        Button dashboardBtn = createNavButton("Dashboard");
        Button projectBtn = createNavButton("Project");
        Button attendanceBtn = createNavButton("Attendance");
        Button scheduleBtn = createNavButton("Schedule");

        // Set button actions
        dashboardBtn.setOnAction(e -> handleNavigation("Dashboard"));
        projectBtn.setOnAction(e -> handleNavigation("Project"));
        attendanceBtn.setOnAction(e -> handleNavigation("Attendance"));
        scheduleBtn.setOnAction(e -> handleNavigation("Schedule"));

        projectBtn.setStyle("-fx-background-color: #303f9f;"); // Highlight current page

        // Profile section
        HBox profileSection = new HBox();
        profileSection.setAlignment(Pos.CENTER_RIGHT);
        profileSection.setSpacing(10);
        HBox.setHgrow(profileSection, Priority.ALWAYS);

        Label nameLabel = new Label("Afnan Haykal");
        nameLabel.setStyle("-fx-text-fill: white;");
        Label roleLabel = new Label("CEO");
        roleLabel.setStyle("-fx-text-fill: white;");
        
        // Profile picture placeholder
        Rectangle profilePic = new Rectangle(40, 40);
        profilePic.setFill(Color.WHITE);

        profileSection.getChildren().addAll(nameLabel, roleLabel, profilePic);

        header.getChildren().addAll(dashboardBtn, projectBtn, attendanceBtn, scheduleBtn, profileSection);
        return header;
    }

    private void handleNavigation(String page) {
        // For now, just show an alert that navigation was triggered
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Navigation");
        alert.setHeaderText(null);
        alert.setContentText("Navigating to " + page);
        alert.showAndWait();
    }

    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
        button.setFont(Font.font("System", FontWeight.BOLD, 14));
        return button;
    }

    private VBox createContent() {
        VBox content = new VBox();
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(20, 40, 20, 40));
        content.setSpacing(15);

        // Title
        Label title = new Label("Add New Projects");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #2c3e50;");

        // Form fields
        nameField = createTextField("Names");
        descriptionField = createTextArea("Descriptions");
        
        // Date Picker for deadline
        deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Select Deadline Date");
        deadlinePicker.setPrefHeight(40);
        deadlinePicker.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 5; " +
            "-fx-padding: 10;"
        );
        
        // Format the date as DD/MM/YY
        StringConverter<LocalDate> converter = new StringConverter<LocalDate>() {
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

        staffField = createTextField("Add Staff");

        // Add button
        Button addButton = new Button("Add");
        addButton.setPrefWidth(Double.MAX_VALUE);
        addButton.setPrefHeight(40);
        addButton.setStyle(
            "-fx-background-color: #7CB342; " + // Green color
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-background-radius: 5;"
        );
        
        // Add button action
        addButton.setOnAction(this::handleAddProject);

        content.getChildren().addAll(
            title,
            nameField,
            descriptionField,
            deadlinePicker,
            staffField,
            addButton
        );

        return content;
    }

    private void handleAddProject(ActionEvent event) {
        // Validate inputs
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

        // Show success message
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Project added successfully!");
        alert.showAndWait();

        // Clear form
        nameField.clear();
        descriptionField.clear();
        deadlinePicker.setValue(null);
        staffField.clear();
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private TextField createTextField(String promptText) {
        TextField field = new TextField();
        field.setPromptText(promptText);
        field.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 5; " +
            "-fx-padding: 10;"
        );
        field.setPrefHeight(40);
        return field;
    }

    private TextArea createTextArea(String promptText) {
        TextArea area = new TextArea();
        area.setPromptText(promptText);
        area.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 5; " +
            "-fx-padding: 10;"
        );
        area.setPrefRowCount(3);
        return area;
    }
} 
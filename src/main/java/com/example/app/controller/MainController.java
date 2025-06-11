package com.example.app.controller;

import com.example.app.model.Project;
import com.example.app.model.Staff;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MainController {

    @FXML
    private TilePane projectsTilePane;

    private List<Project> projects = new ArrayList<>();

    @FXML
    protected void navigateToAddProject() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/app/view/add_project.fxml"));
        Parent root = loader.load();

        AddProjectController controller = loader.getController();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Add New Project");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/app/view/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();

        Project newProject = controller.getNewProject();
        if (newProject != null) {
            projects.add(newProject);
            refreshProjectsGrid();
        }
    }

    private void refreshProjectsGrid() {
        projectsTilePane.getChildren().clear();
        for (Project project : projects) {
            VBox projectCard = new VBox();
            projectCard.getStyleClass().add("project-card");
            
            Label title = new Label(project.getTitle());
            title.getStyleClass().add("project-title");
            
            Label description = new Label(project.getDescription());
            description.getStyleClass().add("project-description");
            description.setWrapText(true);

            projectCard.getChildren().addAll(title, description);

            if (project.getDeadline() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                Label deadline = new Label("Due: " + project.getDeadline().format(formatter));
                deadline.getStyleClass().add("project-deadline");
                projectCard.getChildren().add(deadline);
            }

            if (project.getAssignedStaff() != null && !project.getAssignedStaff().isEmpty()) {
                Label staffLabel = new Label("Staff: " + project.getAssignedStaff().stream().map(Staff::getName).collect(Collectors.joining(", ")));
                staffLabel.getStyleClass().add("project-staff");
                projectCard.getChildren().add(staffLabel);
            }
            
            projectsTilePane.getChildren().add(projectCard);
        }
    }
} 
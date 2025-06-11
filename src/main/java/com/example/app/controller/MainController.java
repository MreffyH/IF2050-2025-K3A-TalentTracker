package com.example.app.controller;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.example.app.dao.ProjectDAO;
import com.example.app.dao.ProjectDAOImpl;
import com.example.app.model.Project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController {

    @FXML
    private TilePane projectsTilePane;

    private ProjectDAO projectDAO;

    public MainController() {
        this.projectDAO = new ProjectDAOImpl();
    }

    @FXML
    public void initialize() {
        refreshProjectsGrid();
    }

    @FXML
    protected void navigateToAddProject() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/app/view/add_project.fxml"));
        Parent root = loader.load();

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Add New Project");
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/app/view/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();

        refreshProjectsGrid();
    }

    private void refreshProjectsGrid() {
        projectsTilePane.getChildren().clear();
        List<Project> projects = projectDAO.getAllProjects();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

        for (Project project : projects) {
            VBox projectCard = new VBox();
            projectCard.getStyleClass().add("project-card");

            Label title = new Label(project.getProjectName());
            title.getStyleClass().add("project-title");

            Label description = new Label(project.getDescription());
            description.getStyleClass().add("project-description");
            description.setWrapText(true);

            Label type = new Label("Type: " + project.getType());
            type.getStyleClass().add("project-status");

            Label staff = new Label("Staff ID: " + project.getIdStaff() + " | CEO ID: " + project.getIdCEO());
            staff.getStyleClass().add("project-staff");

            projectCard.getChildren().addAll(title, description, type, staff);
            
            if (project.getStartDate() != null) {
                Label date = new Label("Dates: " + project.getStartDate().format(formatter) + " - " + project.getEndDate().format(formatter));
                date.getStyleClass().add("project-date");
                projectCard.getChildren().add(date);
            }

            projectsTilePane.getChildren().add(projectCard);
        }
    }
} 
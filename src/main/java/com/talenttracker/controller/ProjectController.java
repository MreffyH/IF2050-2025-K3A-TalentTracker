package com.talenttracker.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.talenttracker.dao.ProjectArtistDAO;
import com.talenttracker.dao.ProjectDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.Project;
import com.talenttracker.model.User;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProjectController {

    @FXML
    private TilePane projectsTilePane;
    @FXML
    private Button addProjectButton;

    private ProjectDAO projectDAO;
    private ProjectArtistDAO projectArtistDAO;
    private UserDAO userDAO;
    private User loggedInUser;

    public ProjectController() {
        this.projectDAO = new ProjectDAO();
        this.projectArtistDAO = new ProjectArtistDAO();
        this.userDAO = new UserDAO();
    }

    @FXML
    public void initialize() {
    }

    public void initializeWithUser(User user) {
        this.loggedInUser = user;
        String role = user.getRole();

        if ("CEO".equals(role)) {
            addProjectButton.setDisable(false);
            addProjectButton.setVisible(true);
        } else {
            addProjectButton.setDisable(true);
            addProjectButton.setVisible(false);
        }
        refreshProjectsGrid();
    }

    @FXML
    protected void navigateToAddProject() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/add_project.fxml"));
        Parent root = loader.load();

        AddProjectController addProjectController = loader.getController();
        addProjectController.setLoggedInCEO(this.loggedInUser);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Add New Project");
        Scene scene = new Scene(root, 1920, 1080);
        stage.setMaximized(true);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();

        refreshProjectsGrid();
    }

    private void refreshProjectsGrid() {
        projectsTilePane.getChildren().clear();
        try {
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
                
                User staffUser = userDAO.getUserById(project.getIdStaff());
                String staffName = (staffUser != null) ? staffUser.getFullName() : "Unknown";
                Label staffLabel = new Label("Staff: " + staffName);
                staffLabel.getStyleClass().add("project-staff");

                List<User> artists = projectArtistDAO.getArtistsForProject(project.getIdProject());
                String artistNames = artists.stream().map(User::getFullName).collect(Collectors.joining(", "));
                Label artistsLabel = new Label("Artists: " + (artistNames.isEmpty() ? "None" : artistNames));
                artistsLabel.getStyleClass().add("project-artists");

                projectCard.getChildren().addAll(title, description, type, staffLabel, artistsLabel);
                
                if (project.getStartDate() != null) {
                    Label date = new Label("Dates: " + project.getStartDate().format(formatter) + " - " + project.getEndDate().format(formatter));
                    date.getStyleClass().add("project-date");
                    projectCard.getChildren().add(date);
                }

                projectsTilePane.getChildren().add(projectCard);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 
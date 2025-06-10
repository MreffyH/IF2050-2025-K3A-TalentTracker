package com.talenttracker.controller;

import com.talenttracker.Main;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import javafx.stage.Stage;

public class HeaderController {
    @FXML
    private ImageView logoImage;
    
    @FXML
    private ImageView profileImage;
    
    @FXML
    private Label nameLabel;
    
    @FXML
    private Label roleLabel;

    @FXML
    private Label projectLabel;
    @FXML
    private Label attendanceLabel;

    @FXML
    public void initialize() {
        try {
            // Load logo image
            String logoPath = Paths.get(System.getProperty("user.dir"), "img", "LogoSN.png").toString();
            File logoFile = new File(logoPath);
            if (logoFile.exists()) {
                Image logo = new Image(logoFile.toURI().toString());
                logoImage.setImage(logo);
            }
            
            // Load profile image
            String profilePath = Paths.get(System.getProperty("user.dir"), "img", "ProfileIcon.png").toString();
            File profileFile = new File(profilePath);
            if (profileFile.exists()) {
                Image profile = new Image(profileFile.toURI().toString());
                profileImage.setImage(profile);
                // Make profile image circular
                profileImage.setClip(new Circle(20, 20, 20));
            }
            
            // Set user info
            String userRole = Main.getLoggedInUserRole();
            nameLabel.setText(Main.getLoggedInUserFullName());
            roleLabel.setText(userRole);

            if ("Artist".equalsIgnoreCase(userRole)) {
                if (projectLabel != null) {
                    projectLabel.setVisible(false);
                    projectLabel.setManaged(false);
                }
                if (attendanceLabel != null) {
                    attendanceLabel.setVisible(false);
                    attendanceLabel.setManaged(false);
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void navigateToDashboard() {
        try {
            BorderPane mainContainer = (BorderPane) logoImage.getScene().getRoot();
            String viewPath = "Artist".equalsIgnoreCase(Main.getLoggedInUserRole()) ? "/view/DashboardViewArtist.fxml" : "/view/DashboardView.fxml";
            FXMLLoader loader = new FXMLLoader(getClass().getResource(viewPath));
            mainContainer.setCenter(loader.load());

            if ("Artist".equalsIgnoreCase(Main.getLoggedInUserRole())) {
                DashboardArtistController controller = loader.getController();
                controller.setArtistId(Main.getLoggedInUserId());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void navigateToProject() {
        // Navigation will be implemented later
    }
    
    @FXML
    public void navigateToAttendance() {
        // Navigation will be implemented later
    }
    
    @FXML
    public void navigateToSchedule() {
        try {
            BorderPane mainContainer = (BorderPane) logoImage.getScene().getRoot();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CalendarView.fxml"));
            mainContainer.setCenter(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 
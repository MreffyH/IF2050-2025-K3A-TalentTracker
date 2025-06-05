package com.talenttracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import java.io.File;
import java.nio.file.Paths;

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
            nameLabel.setText("Reffy Mahardika");
            roleLabel.setText("Staff");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    public void navigateToDashboard() {
        // Navigation will be implemented later
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
        // Navigation will be implemented later
    }
} 
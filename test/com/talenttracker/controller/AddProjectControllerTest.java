package com.talenttracker.controller;

import com.talenttracker.dao.ProjectArtistDAO;
import com.talenttracker.dao.ProjectDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.Project;
import com.talenttracker.model.User;
import com.talenttracker.util.DatabaseUtil;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class AddProjectControllerTest {

    private UserDAO userDAO;
    private ProjectDAO projectDAO;
    private ProjectArtistDAO projectArtistDAO;

    private User testCEO;
    private User testStaff;
    private User testArtist;
    private String testCeoEmail = "testceo@talent.com";
    private String testStaffEmail = "teststaff@talent.com";
    private String testArtistEmail = "testartist@talent.com";

    // Store the controller instance to be accessible in the test method
    private AddProjectController controller;

    @Start
    private void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/add_project.fxml"));
        Parent root = loader.load();
        // Get the controller instance and store it
        controller = loader.getController();
        // IMPORTANT: Do NOT pass data to the controller here, as test data is not yet set up.
        stage.setScene(new Scene(root));
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp() throws SQLException {
        userDAO = new UserDAO();
        projectDAO = new ProjectDAO();
        projectArtistDAO = new ProjectArtistDAO();

        // Clean up tables
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM ProjectArtist");
            stmt.executeUpdate("DELETE FROM Project");
            stmt.executeUpdate("DELETE FROM User WHERE email LIKE 'test%@talent.com'");
        }

        // Create test users
        userDAO.addUser("Test CEO", testCeoEmail, "password", "CEO");
        userDAO.addUser("Test Staff", testStaffEmail, "password", "Staff");
        userDAO.addUser("Test Artist", testArtistEmail, "password", "Artist");

        // Retrieve users to get their generated IDs
        testCEO = userDAO.getUserByEmail(testCeoEmail);
        testStaff = userDAO.getUserByEmail(testStaffEmail);
        testArtist = userDAO.getUserByEmail(testArtistEmail);
    }

    @AfterEach
    void tearDown() throws SQLException {
         // Clean up tables
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM ProjectArtist");
            stmt.executeUpdate("DELETE FROM Project");
            stmt.executeUpdate("DELETE FROM User WHERE email LIKE 'test%@talent.com'");
        }
    }

    @Test
    void testAddProject_Success(FxRobot robot) throws SQLException, InterruptedException {
        // Get the next project ID before interacting with the UI thread.
        int nextId = projectDAO.getNextProjectId();

        // Direct controller interaction, with pauses to make it visible.
        robot.interact(() -> controller.setLoggedInCEO(testCEO));
        Thread.sleep(500);

        robot.interact(() -> {
            robot.lookup("#projectTypeComboBox").queryComboBox().getSelectionModel().select("Comeback");
        });
        Thread.sleep(500);

        robot.interact(() -> {
            robot.lookup("#projectIdField").queryAs(TextField.class).setText(String.valueOf(nextId));
        });
        Thread.sleep(500);
        
        robot.interact(() -> {
            robot.lookup("#projectNameField").queryAs(TextField.class).setText("New Test Project");
        });
        Thread.sleep(500);
        
        robot.interact(() -> {
            robot.lookup("#projectDescriptionArea").queryAs(TextArea.class).setText("This is a test description.");
        });
        Thread.sleep(500);

        robot.interact(() -> {
            robot.lookup("#staffComboBox").queryComboBox().setValue(testStaff);
        });
        Thread.sleep(500);

        robot.interact(() -> {
            robot.lookup("#startDatePicker").queryAs(DatePicker.class).setValue(LocalDate.of(2025, 6, 1));
        });
        Thread.sleep(500);

        robot.interact(() -> {
            robot.lookup("#endDatePicker").queryAs(DatePicker.class).setValue(LocalDate.of(2025, 12, 31));
        });
        Thread.sleep(500);

        // Add artist
        robot.interact(() -> {
            robot.lookup("#artistComboBox").queryComboBox().setValue(testArtist);
        });
        Thread.sleep(500);
        robot.interact(() -> controller.handleAddArtistAction());
        Thread.sleep(500);

        // Submit form
        robot.interact(() -> controller.handleSubmitButtonAction());
        Thread.sleep(2000); // Longer pause to see result

        // Give the background tasks (like DB insertion) a moment to complete.
        WaitForAsyncUtils.waitForFxEvents();

        // Verify the results in the database
        Project createdProject = projectDAO.getProjectById(nextId);

        assertNotNull(createdProject, "Project should be created in the database.");
        assertEquals("New Test Project", createdProject.getProjectName());
        assertEquals("Comeback", createdProject.getType());
        assertEquals(testCEO.getId(), createdProject.getIdCEO());
        assertEquals(testStaff.getId(), createdProject.getIdStaff());

        List<User> artistsOnProject = projectArtistDAO.getArtistsForProject(createdProject.getIdProject());
        assertFalse(artistsOnProject.isEmpty(), "Artist should be assigned to the project.");
        assertEquals(1, artistsOnProject.size());
        assertEquals(testArtist.getId(), artistsOnProject.get(0).getId());
    }
}

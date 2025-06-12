package com.talenttracker.controller;

import com.talenttracker.dao.*;
import com.talenttracker.model.*;
import com.talenttracker.util.DatabaseUtil;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
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
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class DashboardArtistControllerTest {

    private UserDAO userDAO;
    private AlbumDAO albumDAO;
    private ProjectDAO projectDAO;
    private ProjectArtistDAO projectArtistDAO;
    private FansDAO fansDAO;
    private SocialMediaDAO socialMediaDAO;
    private StatsDAO statsDAO;
    private DashboardArtistController controller;
    private User testArtist;
    private User testCEO;
    private User testStaff;

    @Start
    private void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DashboardViewArtist.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp() throws SQLException {
        // DAOs
        userDAO = new UserDAO();
        albumDAO = new AlbumDAO();
        projectDAO = new ProjectDAO();
        projectArtistDAO = new ProjectArtistDAO();
        fansDAO = new FansDAO();
        socialMediaDAO = new SocialMediaDAO();
        statsDAO = new StatsDAO();

        // Clean up database
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM FansResponse");
            stmt.executeUpdate("DELETE FROM Popularity");
            stmt.executeUpdate("DELETE FROM TopAlbum");
            stmt.executeUpdate("DELETE FROM ProjectArtist");
            stmt.executeUpdate("DELETE FROM Project");
            stmt.executeUpdate("DELETE FROM `user` WHERE email LIKE 'testartist@%' OR email LIKE 'testceo@%' OR email LIKE 'teststaff@%'");
        }

        // Create test users
        userDAO.addUser("Test Artist", "testartist@talent.com", "password", "Artist");
        testArtist = userDAO.getUserByEmail("testartist@talent.com");
        assertNotNull(testArtist, "Test artist should be created");

        userDAO.addUser("Test CEO", "testceo@talent.com", "password", "CEO");
        testCEO = userDAO.getUserByEmail("testceo@talent.com");

        userDAO.addUser("Test Staff", "teststaff@talent.com", "password", "Staff");
        testStaff = userDAO.getUserByEmail("teststaff@talent.com");
        
        // Create Projects
        int nextProjId = projectDAO.getNextProjectId();
        Project project1 = new Project(nextProjId, "Comeback Stage", "Music Show", LocalDate.now().minusMonths(1).atStartOfDay(), LocalDate.now().plusMonths(1).atStartOfDay(), "A project for the new comeback.", testCEO.getId(), testStaff.getId());
        projectDAO.addProject(project1);
        projectArtistDAO.addArtistsToProject(nextProjId, List.of(testArtist.getId()));

        // Create Albums
        albumDAO.addAlbum("Eclipse", 5000, testArtist.getId());
        albumDAO.addAlbum("Starlight", 8000, testArtist.getId());

        // Create Social Media Stats
        socialMediaDAO.addSocialMediaData("X", 120000, testArtist.getId(), LocalDate.now().minusDays(15));
        socialMediaDAO.addSocialMediaData("TikTok", 65000, testArtist.getId(), LocalDate.now().minusDays(15));
        socialMediaDAO.addSocialMediaData("Instagram", 100000, testArtist.getId(), LocalDate.now().minusMonths(2));
        
        // Create Fan Responses
        fansDAO.addFanResponse(1, testArtist.getId(), "X", "Loved the new song!", "Positive", java.sql.Timestamp.valueOf(LocalDate.now().minusDays(1).atStartOfDay()));
        fansDAO.addFanResponse(2, testArtist.getId(), "Instagram", "The MV was a bit boring.", "Negative", java.sql.Timestamp.valueOf(LocalDate.now().minusDays(2).atStartOfDay()));
        fansDAO.addFanResponse(3, testArtist.getId(), "TikTok", "When is the next album?", "Neutral", java.sql.Timestamp.valueOf(LocalDate.now().minusDays(3).atStartOfDay()));
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Clean up database again to be safe
         try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM FansResponse");
            stmt.executeUpdate("DELETE FROM Popularity");
            stmt.executeUpdate("DELETE FROM TopAlbum");
            stmt.executeUpdate("DELETE FROM ProjectArtist");
            stmt.executeUpdate("DELETE FROM Project");
            stmt.executeUpdate("DELETE FROM `user` WHERE email LIKE 'testartist@%' OR email LIKE 'testceo@%' OR email LIKE 'teststaff@%'");
        }
    }

    @Test
    void testInitialDataLoadsCorrectly(FxRobot robot) throws TimeoutException {
        // Set the artist on the controller
        Platform.runLater(() -> controller.setArtistId(testArtist.getId()));

        // Wait for the name label to be populated, which indicates data loading has started
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> "Test Artist".equals(robot.lookup("#profileNameLabel").queryAs(Label.class).getText()));
        
        // Further wait for charts to be populated
        WaitForAsyncUtils.waitForFxEvents();

        // Assertions
        assertEquals("Test Artist", robot.lookup("#profileNameLabel").queryAs(Label.class).getText());

        // Check Bar Chart (Top Albums)
        BarChart<String, Number> barChart = robot.lookup("#topAlbumChart").queryAs(BarChart.class);
        // This month's filter is on by default, so we expect 1 album from this month
        assertEquals(2, barChart.getData().get(0).getData().size(), "Bar chart should show 2 albums for 'This Month'");
        
        List<String> albumNames = barChart.getData().get(0).getData().stream()
                .map(javafx.scene.chart.XYChart.Data::getXValue)
                .collect(java.util.stream.Collectors.toList());

        assertTrue(albumNames.contains("Eclipse"), "Bar chart should contain 'Eclipse'");
        assertTrue(albumNames.contains("Starlight"), "Bar chart should contain 'Starlight'");

        // Check Pie Chart (Sentiment)
        PieChart pieChart = robot.lookup("#sentimentChart").queryAs(PieChart.class);
        assertEquals(3, pieChart.getData().size(), "Pie chart should have 3 sentiment slices.");

        // Check Line Chart (Social Media)
        LineChart<String, Number> lineChart = robot.lookup("#socialMediaChart").queryAs(LineChart.class);
        assertEquals(3, lineChart.getData().size(), "Line chart should have 3 series (X, TikTok, Instagram).");
    }
} 
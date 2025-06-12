package com.talenttracker.controller;

import com.talenttracker.dao.DashboardDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.Album;
import com.talenttracker.model.DailyStats;
import com.talenttracker.model.Popularity;
import com.talenttracker.util.DatabaseUtil;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(ApplicationExtension.class)
public class DashboardControllerTest {

    private DashboardDAO dashboardDAO;
    private UserDAO userDAO;
    private DashboardController controller;

    @Start
    private void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HeaderView.fxml"));
        Parent root = loader.load();
        HeaderController headerController = loader.getController();

        // The HeaderController loads the DashboardView automatically. We need to wait for it.
        // Then we can get the DashboardController instance from the HeaderController.
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> {
            Object contentController = headerController.getCurrentContentController();
            if (contentController instanceof DashboardController) {
                this.controller = (DashboardController) contentController;
                return true; // Stop waiting
            }
            return false; // Keep waiting
        });

        assertNotNull(controller, "DashboardController should have been loaded and set.");

        stage.setScene(new Scene(root));
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp() throws SQLException {
        dashboardDAO = new DashboardDAO();
        userDAO = new UserDAO();

        // Clean up database
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Sales");
            stmt.executeUpdate("DELETE FROM AlbumSold");
            stmt.executeUpdate("DELETE FROM Visitors");
            stmt.executeUpdate("DELETE FROM TopAlbum");
            stmt.executeUpdate("DELETE FROM Popularity");
            stmt.executeUpdate("DELETE FROM `user` WHERE email = 'testartist.search@talent.com' OR idUser = 997");
        }

        // Add a dummy artist to satisfy foreign key constraints
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO `user` (idUser, fullName, email, password, role) VALUES (997, 'Dummy Artist', 'dummy@talent.com', 'password', 'Artist')");
        }

        // Create test data
        dashboardDAO.addTestDataForDate(LocalDate.now(), 15000.0, 100, 50);
        dashboardDAO.addTestDataForDate(LocalDate.now().minusDays(1), 12000.0, 80, 40);

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO TopAlbum (albumName, sold, date, idArtis) VALUES ('Test Album 1', 50, '" + LocalDate.now() + "', 997)");
            stmt.executeUpdate("INSERT INTO TopAlbum (albumName, sold, date, idArtis) VALUES ('Test Album 2', 30, '" + LocalDate.now() + "', 997)");
            stmt.executeUpdate("INSERT INTO Popularity (socialMedia, todayFollowers, date, idArtis) VALUES ('Instagram', 1000, '" + LocalDate.now().minusDays(1) + "', 997)");
            stmt.executeUpdate("INSERT INTO Popularity (socialMedia, todayFollowers, date, idArtis) VALUES ('TikTok', 1200, '" + LocalDate.now() + "', 997)");
        }

        userDAO.addUser("Searchable Artist", "testartist.search@talent.com", "password", "Artist");
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Clean up database
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Sales");
            stmt.executeUpdate("DELETE FROM AlbumSold");
            stmt.executeUpdate("DELETE FROM Visitors");
            stmt.executeUpdate("DELETE FROM TopAlbum");
            stmt.executeUpdate("DELETE FROM Popularity");
            stmt.executeUpdate("DELETE FROM `user` WHERE email = 'testartist.search@talent.com' OR idUser = 997");
        }
    }

    @Test
    void testInitialDataLoadsCorrectly(FxRobot robot) throws TimeoutException {
        // Use robot.interact to ensure the refresh happens on the FX thread and the test waits for it.
        robot.interact(() -> controller.refreshAllData());

        // Use a more robust wait: poll the UI until a specific condition is met.
        // This makes the test resilient to variations in loading/rendering time.
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> {
            Label label = robot.lookup("#totalSalesLabel").queryAs(Label.class);
            return "Rp15.000,00".equals(label.getText());
        });

        // Now that we've waited for the first element, the others should also be loaded.
        // We can now safely run the assertions.
        assertEquals("Rp15.000,00", robot.lookup("#totalSalesLabel").queryAs(Label.class).getText());
        assertEquals("+25%", robot.lookup("#totalSalesChangeLabel").queryAs(Label.class).getText());
        assertEquals("100", robot.lookup("#albumsSoldLabel").queryAs(Label.class).getText());
        assertEquals("+25%", robot.lookup("#albumsSoldChangeLabel").queryAs(Label.class).getText());
        assertEquals("50", robot.lookup("#newVisitorsLabel").queryAs(Label.class).getText());
        assertEquals("+25%", robot.lookup("#newVisitorsChangeLabel").queryAs(Label.class).getText());

        // Check charts
        javafx.scene.chart.BarChart<String, Number> barChart = robot.lookup("#topAlbumChart").queryAs(javafx.scene.chart.BarChart.class);
        assertEquals(2, barChart.getData().get(0).getData().size(), "Bar chart should have 2 data points.");

        javafx.scene.chart.LineChart<String, Number> lineChart = robot.lookup("#socialInsightsChart").queryAs(javafx.scene.chart.LineChart.class);
        assertEquals(2, lineChart.getData().get(0).getData().size(), "Line chart should have 2 data points.");
    }

    @Test
    void testSearchFunctionality_Success(FxRobot robot) throws TimeoutException {
        // Refresh data and wait until UI is ready before interaction
        robot.interact(() -> controller.refreshAllData());
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !robot.lookup("#totalSalesLabel").queryAs(Label.class).getText().isEmpty());

        robot.clickOn("#searchTextField").write("Searchable Artist");
        robot.clickOn("#searchButton");
        
        WaitForAsyncUtils.waitForFxEvents();

        // Verify that the LaporanKinerjaView is loaded by checking for a known element within it.
        assertNotNull(robot.lookup("#artistNameLabel").query(), "LaporanKinerjaView should be loaded after successful search.");
    }

    @Test
    void testSearchFunctionality_NotFound(FxRobot robot) throws TimeoutException {
        // Refresh data and wait until UI is ready
        robot.interact(() -> controller.refreshAllData());
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> !robot.lookup("#totalSalesLabel").queryAs(Label.class).getText().isEmpty());
        
        BorderPane mainContainer = (BorderPane) robot.lookup("#searchTextField").query().getScene().getRoot();
        javafx.scene.Node initialCenter = mainContainer.getCenter();

        robot.clickOn("#searchTextField").write("Unknown Artist");
        robot.clickOn("#searchButton");

        WaitForAsyncUtils.waitForFxEvents();

        // Verify that the view has not changed by checking if the center node is the same
        assertEquals(initialCenter, mainContainer.getCenter(), "The view should not change for an unknown artist search.");
    }
}

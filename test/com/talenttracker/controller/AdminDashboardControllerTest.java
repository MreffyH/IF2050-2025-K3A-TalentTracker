package com.talenttracker.controller;

import com.talenttracker.dao.AttendanceDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.util.DatabaseUtil;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(ApplicationExtension.class)
public class AdminDashboardControllerTest {

    private UserDAO userDAO;
    private AttendanceDAO attendanceDAO;
    private AdminDashboardController controller; // Store controller instance

    @Start
    private void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AttendanceDashboardCEO.fxml"));
        Parent root = loader.load();
        // Get the controller instance to be used later
        controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp() throws SQLException {
        userDAO = new UserDAO();
        attendanceDAO = new AttendanceDAO();

        // Clean up tables to ensure a clean slate
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Attendance");
            stmt.executeUpdate("DELETE FROM User WHERE role = 'Staff'");
        }

        // Create test staff
        userDAO.addUser("Alpha Staff", "alpha@talent.com", "password", "Staff");
        userDAO.addUser("Bravo Staff", "bravo@talent.com", "password", "Staff");
        userDAO.addUser("Charlie Staff", "charlie@talent.com", "password", "Staff");
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Clean up tables
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Attendance");
            stmt.executeUpdate("DELETE FROM User WHERE role = 'Staff'");
        }
    }

    @Test
    void testInitialDisplay(FxRobot robot) throws TimeoutException {
        // Wait for a key element to be present before proceeding
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> robot.lookup("#searchField").tryQuery().isPresent());
        
        // Manually trigger the data loading now that the UI is ready
        Platform.runLater(() -> controller.refreshStaffList());
        WaitForAsyncUtils.waitForFxEvents(); // Wait for the UI to update

        VBox staffTableContent = robot.lookup("#staffTableContent").queryAs(VBox.class);
        assertNotNull(staffTableContent);

        // Wait until the 3 staff members are visible
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> staffTableContent.getChildren().size() == 3);
        
        assertEquals(3, staffTableContent.getChildren().size(), "Should display all 3 staff members after refresh.");

        // Check details of the first staff member
        GridPane firstRow = (GridPane) staffTableContent.getChildren().get(0);
        Label nameLabel = (Label) firstRow.getChildren().get(0);
        assertEquals("Alpha Staff", nameLabel.getText());
    }

    @Test
    void testSearchFunctionality(FxRobot robot) throws TimeoutException {
        // Wait for a key element to be present before proceeding
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> robot.lookup("#searchField").tryQuery().isPresent());

        // Manually trigger the data loading for this test as well
        Platform.runLater(() -> controller.refreshStaffList());
        WaitForAsyncUtils.waitForFxEvents();

        VBox staffTableContent = robot.lookup("#staffTableContent").queryAs(VBox.class);
        TextField searchField = robot.lookup("#searchField").queryAs(TextField.class);

        // 1. Initially, all 3 staff should be present
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> staffTableContent.getChildren().size() == 3);
        assertEquals(3, staffTableContent.getChildren().size());

        // 2. Search for "Alpha"
        robot.clickOn(searchField).write("Alpha");
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> staffTableContent.getChildren().size() == 1);
        assertEquals(1, staffTableContent.getChildren().size());
        Label nameLabel = (Label) ((GridPane) staffTableContent.getChildren().get(0)).getChildren().get(0);
        assertEquals("Alpha Staff", nameLabel.getText());

        // 3. Clear search, all 3 should be back
        robot.clickOn(searchField).eraseText("Alpha".length());
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> staffTableContent.getChildren().size() == 3);
        assertEquals(3, staffTableContent.getChildren().size());
    }
}

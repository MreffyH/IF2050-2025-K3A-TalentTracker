package com.talenttracker.controller;

import com.talenttracker.dao.AttendanceDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.Attendance;
import com.talenttracker.model.User;
import com.talenttracker.util.DatabaseUtil;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class AttendanceDashboardControllerTest {

    private UserDAO userDAO;
    private AttendanceDAO attendanceDAO;
    private AttendanceDashboardController controller;
    private User testStaff;

    @Start
    private void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AttendanceDashboardStaff.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.show();
        stage.toFront();
    }

    @BeforeEach
    void setUp() throws SQLException {
        userDAO = new UserDAO();
        attendanceDAO = new AttendanceDAO();

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Attendance");
            stmt.executeUpdate("DELETE FROM `user` WHERE email = 'teststaff@talent.com'");
        }

        userDAO.addUser("Test Staff", "teststaff@talent.com", "password", "Staff");
        testStaff = userDAO.getUserByEmail("teststaff@talent.com");

        // Set the user on the controller right after creating it, for all tests
        Platform.runLater(() -> controller.setUser(testStaff));
        WaitForAsyncUtils.waitForFxEvents();
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM Attendance");
            stmt.executeUpdate("DELETE FROM `user` WHERE email = 'teststaff@talent.com'");
        }
    }

    @Test
    void testCheckInAndCheckOut(FxRobot robot) throws SQLException, TimeoutException, InterruptedException {
        // 1. Find the single button for check-in/out
        Button checkButton = robot.lookup("#checkButton").queryButton();
        assertEquals("Check In", checkButton.getText(), "Button should initially say 'Check In'.");
        assertFalse(checkButton.isDisabled(), "Check-in button should be enabled initially if no attendance exists.");

        // 2. Click to Check-In
        robot.interact(checkButton::fire);

        // 3. Verify UI state and database after Check-In
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> "Check Out".equals(checkButton.getText()));
        assertEquals("Check Out", checkButton.getText(), "Button text should change to 'Check Out'.");

        List<Attendance> attendanceList = attendanceDAO.getAttendanceByUserId(testStaff.getId());
        Attendance todayAttendance = attendanceList.stream()
            .filter(a -> a.getDate().toLocalDate().equals(LocalDate.now()))
            .findFirst()
            .orElse(null);

        assertNotNull(todayAttendance, "Attendance record should exist after check-in.");
        assertNotNull(todayAttendance.getTime(), "Check-in time should be recorded.");
        assertEquals(0, todayAttendance.getWorkingHours(), "Working hours should be 0 after check-in.");

        // 4. Click to Check-Out
        // We must pause here to simulate time passing for the workingHoursTimeline to update elapsedSeconds
        Thread.sleep(1100); // Wait for more than 1 second for the timer to tick.
        robot.interact(checkButton::fire);

        // 5. Verify UI state and database after Check-Out
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, checkButton::isDisabled);
        assertTrue(checkButton.isDisabled(), "Button should be disabled after checking out.");

        List<Attendance> updatedAttendanceList = attendanceDAO.getAttendanceByUserId(testStaff.getId());
        Attendance updatedAttendance = updatedAttendanceList.stream()
            .filter(a -> a.getDate().toLocalDate().equals(LocalDate.now()))
            .findFirst()
            .orElse(null);
        assertNotNull(updatedAttendance, "Attendance record should still exist after check-out.");
        assertTrue(updatedAttendance.getWorkingHours() > 0, "Working hours should be updated after checkout.");
    }

    @Test
    void testViewSalaryButton(FxRobot robot) throws Exception {
        // 1. Find the necessary UI components
        Label salaryAmountLabel = robot.lookup("#salaryAmountLabel").queryAs(Label.class);

        // 2. Verify initial state (salary hidden)
        assertEquals("IDR ********", salaryAmountLabel.getText(), "Salary should be hidden initially.");

        // 3. Call handler to reveal the salary
        Platform.runLater(() -> controller.handleViewSalary());
        WaitForAsyncUtils.waitForFxEvents();

        // 4. Verify the salary is now visible
        String expectedSalary = "IDR " + String.format("%,d", testStaff.getSalary());
        assertEquals(expectedSalary, salaryAmountLabel.getText(), "Salary should be visible after the first click.");

        // 5. Call handler again to hide the salary
        Platform.runLater(() -> controller.handleViewSalary());
        WaitForAsyncUtils.waitForFxEvents();

        // 6. Verify the salary is hidden again
        assertEquals("IDR ********", salaryAmountLabel.getText(), "Salary should be hidden again after the second click.");
    }
}

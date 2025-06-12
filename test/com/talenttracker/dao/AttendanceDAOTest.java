package com.talenttracker.dao;

import com.talenttracker.dao.AttendanceDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.Attendance;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;

public class AttendanceDAOTest {

    private AttendanceDAO attendanceDAO;
    private UserDAO userDAO;
    private int testUserId = 998; // Use a high number to avoid conflicts

    @BeforeEach
    public void setUp() throws SQLException {
        attendanceDAO = new AttendanceDAO();
        userDAO = new UserDAO();

        // Clean up any old test data, child records first
        attendanceDAO.removeAttendanceByUser(testUserId); 
        userDAO.removeUser(testUserId); 

        // Add a test user
        userDAO.addUser(testUserId, "Test Staff", "staff@test.com", "password", "Staff");
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Clean up child records first
        attendanceDAO.removeAttendanceByUser(testUserId);
        // Then clean up the parent record
        userDAO.removeUser(testUserId);
    }

    @Test
    public void testAddAndGetAttendance() {
        try {
            // 1. Add a new attendance record
            Attendance newAttendance = new Attendance();
            newAttendance.setIdStaff(testUserId);
            newAttendance.setDate(new java.sql.Date(System.currentTimeMillis()));
            newAttendance.setTime(new java.sql.Time(System.currentTimeMillis()));
            newAttendance.setOnTime(true);
            newAttendance.setWorkingHours(8 * 3600); // 8 hours in seconds
            
            attendanceDAO.addAttendance(newAttendance);

            // 2. Retrieve the attendance list for the user
            List<Attendance> attendanceList = attendanceDAO.getAttendanceByUserId(testUserId);

            // 3. Verify the results
            assertNotNull(attendanceList, "Attendance list should not be null");
            assertFalse(attendanceList.isEmpty(), "Attendance list should not be empty after adding a record");
            assertEquals(1, attendanceList.size(), "Attendance list should contain exactly one record");
            
            Attendance retrievedAttendance = attendanceList.get(0);
            assertEquals(testUserId, retrievedAttendance.getIdStaff(), "Staff ID should match");
            assertTrue(retrievedAttendance.isOnTime(), "Attendance status should be on-time");

        } catch (SQLException e) {
            fail("SQLException was thrown: " + e.getMessage());
        }
    }
} 
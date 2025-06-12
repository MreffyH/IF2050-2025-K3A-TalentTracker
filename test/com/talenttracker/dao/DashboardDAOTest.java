package com.talenttracker.dao;

import com.talenttracker.dao.DashboardDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.DailyStats;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class DashboardDAOTest {

    private DashboardDAO dashboardDAO;
    private UserDAO userDAO;
    private final LocalDate testDate = LocalDate.now();
    private final int testArtistId = 997;

    @BeforeEach
    public void setUp() throws SQLException {
        dashboardDAO = new DashboardDAO();
        userDAO = new UserDAO();

        // Clean up any old test data
        dashboardDAO.removeTestDataForDate(testDate);
        userDAO.removeUser(testArtistId);

        // Add fresh test data
        userDAO.addUser(testArtistId, "Test Artist for Dashboard", "dashboard@test.com", "password", "Artist");
        dashboardDAO.addTestDataForDate(testDate, 100.0, 10.0, 50.0);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Clean up the test data
        dashboardDAO.removeTestDataForDate(testDate);
        userDAO.removeUser(testArtistId);
    }

    @Test
    public void testGetDailyStatsForDate() {
        try {
            DailyStats stats = dashboardDAO.getDailyStatsForDate(testDate);

            assertNotNull(stats, "DailyStats should not be null");
            assertEquals(100.0, stats.getTotalSales(), 0.01, "Sales should match the test data");
            assertEquals(10.0, stats.getAlbumsSold(), 0.01, "Albums sold should match the test data");
            assertEquals(50.0, stats.getNewVisitors(), 0.01, "Visitors should match the test data");

        } catch (SQLException e) {
            fail("SQLException was thrown: " + e.getMessage());
        }
    }
} 
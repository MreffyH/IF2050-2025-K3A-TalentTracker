package com.talenttracker.dao;

import com.talenttracker.dao.StatsDAO;
import com.talenttracker.dao.UserDAO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StatsDAOTest {

    private StatsDAO statsDAO;
    private UserDAO userDAO;
    private final int testArtistId = 1004;
    private final LocalDate testDate = LocalDate.now().minusDays(5); // Use a fixed past date

    @BeforeEach
    public void setUp() throws SQLException {
        statsDAO = new StatsDAO();
        userDAO = new UserDAO();

        // Clean up before test
        statsDAO.removeStatsForArtist(testArtistId);
        userDAO.removeUser(testArtistId);

        // Add test artist
        userDAO.addUser(testArtistId, "Stats Star", "stats@test.com", "password", "Artist");
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Clean up after test
        statsDAO.removeStatsForArtist(testArtistId);
        userDAO.removeUser(testArtistId);
    }

    @Test
    public void testAddAndGetStatsForDate() throws SQLException {
        // 1. Add stats data for a specific date
        statsDAO.addVisitors(100, testArtistId, testDate);
        statsDAO.addSales(5000, testArtistId, testDate);
        statsDAO.addAlbumsSold(50, testArtistId, testDate);

        // 2. Retrieve the stats for that date
        double visitors = statsDAO.getVisitorsForDate(testDate, testArtistId);
        double sales = statsDAO.getSalesForDate(testDate, testArtistId);
        double albumsSold = statsDAO.getAlbumsSoldForDate(testDate, testArtistId);

        // 3. Verify the data
        assertEquals(100, visitors, "Visitor count should match.");
        assertEquals(5000, sales, "Sales amount should match.");
        assertEquals(50, albumsSold, "Albums sold count should match.");
    }
} 
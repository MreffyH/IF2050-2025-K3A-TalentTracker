package com.talenttracker.dao;

import com.talenttracker.dao.FansDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.FanResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FansDAOTest {

    private FansDAO fansDAO;
    private UserDAO userDAO;
    private final int testArtistId = 996;

    @BeforeEach
    public void setUp() throws SQLException {
        fansDAO = new FansDAO();
        userDAO = new UserDAO();

        // Clean up before test
        fansDAO.removeFanResponsesByArtist(testArtistId);
        userDAO.removeUser(testArtistId);

        // Add test artist
        userDAO.addUser(testArtistId, "Test Artist for Fans", "fans@test.com", "password", "Artist");

        // Add test data
        Timestamp recentTimestamp = Timestamp.valueOf(LocalDateTime.now().minusDays(5));
        Timestamp oldTimestamp = Timestamp.valueOf(LocalDateTime.now().minusMonths(2));
        
        fansDAO.addFanResponse(1, testArtistId, "X", "Great new song!", "Positive", recentTimestamp);
        fansDAO.addFanResponse(2, testArtistId, "Instagram", "Loved the concert.", "Positive", recentTimestamp);
        fansDAO.addFanResponse(3, testArtistId, "TikTok", "Old album was better.", "Negative", oldTimestamp);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Clean up after test
        fansDAO.removeFanResponsesByArtist(testArtistId);
        userDAO.removeUser(testArtistId);
    }

    @Test
    public void testGetFanResponsesWithMonthFilter() throws SQLException {
        List<FanResponse> responses = fansDAO.getFanResponses(testArtistId, true);
        assertEquals(2, responses.size(), "Should only return responses from the last month.");
    }

    @Test
    public void testGetFanResponsesWithoutFilter() throws SQLException {
        List<FanResponse> responses = fansDAO.getFanResponses(testArtistId, false);
        assertEquals(3, responses.size(), "Should return all responses for the artist.");
    }
} 
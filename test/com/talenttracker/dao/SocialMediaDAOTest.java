package com.talenttracker.dao;

import com.talenttracker.dao.SocialMediaDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.Popularity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SocialMediaDAOTest {

    private SocialMediaDAO socialMediaDAO;
    private UserDAO userDAO;
    private final int testArtistId = 1003;

    @BeforeEach
    public void setUp() throws SQLException {
        socialMediaDAO = new SocialMediaDAO();
        userDAO = new UserDAO();

        // Clean up before test
        socialMediaDAO.removeSocialMediaDataByArtist(testArtistId);
        userDAO.removeUser(testArtistId);

        // Add test artist
        userDAO.addUser(testArtistId, "Social Media Star", "sms@test.com", "password", "Artist");
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Clean up after test
        socialMediaDAO.removeSocialMediaDataByArtist(testArtistId);
        userDAO.removeUser(testArtistId);
    }

    @Test
    public void testAddAndGetSocialMediaData() throws SQLException {
        // 1. Add social media data for different dates
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        socialMediaDAO.addSocialMediaData("X", 10000, testArtistId, today);
        socialMediaDAO.addSocialMediaData("Instagram", 25000, testArtistId, yesterday);

        // 2. Get social media data
        List<Popularity> socialMediaData = socialMediaDAO.getSocialMediaData(testArtistId);

        // 3. Verify the data
        assertNotNull(socialMediaData);
        assertEquals(2, socialMediaData.size(), "Should retrieve two social media entries.");

        boolean twitterFound = socialMediaData.stream()
                .anyMatch(p -> p.getPlatform().equals("X") && p.getFollowers() == 10000);
        boolean instagramFound = socialMediaData.stream()
                .anyMatch(p -> p.getPlatform().equals("Instagram") && p.getFollowers() == 25000);

        assertTrue(twitterFound, "X data should be correct.");
        assertTrue(instagramFound, "Instagram data should be correct.");
    }
} 
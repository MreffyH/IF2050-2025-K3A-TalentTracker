package com.talenttracker.dao;

import com.talenttracker.dao.AlbumDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.Album;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;

public class AlbumDAOTest {

    private AlbumDAO albumDAO;
    private UserDAO userDAO;
    private int testArtistId = 999;
    private String testArtistName = "Test Artist";
    private String testAlbumName = "Test Album";

    @BeforeEach
    public void setUp() throws SQLException {
        albumDAO = new AlbumDAO();
        userDAO = new UserDAO();
        
        // Clean up any old test data before running
        albumDAO.removeAlbum(testAlbumName, testArtistId);
        userDAO.removeUser(testArtistId); // Ensure user is removed first

        // Add a fresh user and album for testing
        userDAO.addUser(testArtistId, testArtistName, "test@test.com", "password", "Artist");
        albumDAO.addAlbum(testAlbumName, 100, testArtistId);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Clean up the test data after the test runs
        albumDAO.removeAlbum(testAlbumName, testArtistId);
        userDAO.removeUser(testArtistId);
    }

    @Test
    public void testGetArtistAlbums() {
        try {
            List<Album> albums = albumDAO.getArtistAlbums(testArtistId, false);
            assertNotNull(albums, "Album list should not be null");
            assertFalse(albums.isEmpty(), "Album list should not be empty after adding one");
            assertEquals(1, albums.size(), "Should be exactly one album for the test artist");
            assertEquals(testAlbumName, albums.get(0).getName(), "Album name should match the test album");
        } catch (SQLException e) {
            fail("SQLException was thrown: " + e.getMessage());
        }
    }
}

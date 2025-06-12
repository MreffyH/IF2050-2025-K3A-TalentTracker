package com.talenttracker.dao;

import com.talenttracker.dao.ProjectArtistDAO;
import com.talenttracker.dao.ProjectDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.Project;
import com.talenttracker.model.User;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectArtistDAOTest {

    private ProjectArtistDAO projectArtistDAO;
    private ProjectDAO projectDAO;
    private UserDAO userDAO;

    private int testProjectId = 995;
    private int testArtistId1 = 994;
    private int testArtistId2 = 993;
    private int testCeoId = 992;
    private int testStaffId = 991;

    @BeforeEach
    public void setUp() throws SQLException {
        projectArtistDAO = new ProjectArtistDAO();
        projectDAO = new ProjectDAO();
        userDAO = new UserDAO();

        // Clean up before test
        projectArtistDAO.removeArtistsFromProject(testProjectId);
        projectDAO.deleteProject(testProjectId);
        userDAO.removeUser(testArtistId1);
        userDAO.removeUser(testArtistId2);
        userDAO.removeUser(testCeoId);
        userDAO.removeUser(testStaffId);

        // Add test data
        userDAO.addUser(testCeoId, "Test CEO", "ceo@test.com", "pw", "CEO");
        userDAO.addUser(testStaffId, "Test Staff", "staff@test.com", "pw", "Staff");
        Project testProject = new Project(testProjectId, "Test Project", "Concert", LocalDate.now().atStartOfDay(), LocalDate.now().plusMonths(1).atStartOfDay(), "Test Description", testCeoId, testStaffId);
        projectDAO.addProjectForTest(testProject);

        userDAO.addUser(testArtistId1, "Artist One", "a1@test.com", "pw", "Artist");
        userDAO.addUser(testArtistId2, "Artist Two", "a2@test.com", "pw", "Artist");
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Clean up after test
        projectArtistDAO.removeArtistsFromProject(testProjectId);
        projectDAO.deleteProject(testProjectId);
        userDAO.removeUser(testArtistId1);
        userDAO.removeUser(testArtistId2);
        userDAO.removeUser(testCeoId);
        userDAO.removeUser(testStaffId);
    }

    @Test
    public void testAddAndGetArtistsForProject() throws SQLException {
        // 1. Add artists to project
        List<Integer> artistIds = Arrays.asList(testArtistId1, testArtistId2);
        projectArtistDAO.addArtistsToProject(testProjectId, artistIds);

        // 2. Get artists for project
        List<User> artists = projectArtistDAO.getArtistsForProject(testProjectId);

        // 3. Verify
        assertNotNull(artists);
        assertEquals(2, artists.size());
        
        List<String> retrievedNames = artists.stream().map(User::getFullName).toList();
        assertTrue(retrievedNames.contains("Artist One"));
        assertTrue(retrievedNames.contains("Artist Two"));
    }
} 
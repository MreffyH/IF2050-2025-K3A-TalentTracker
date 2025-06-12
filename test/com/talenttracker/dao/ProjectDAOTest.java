package com.talenttracker.dao;

import com.talenttracker.dao.ProjectDAO;
import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.Project;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectDAOTest {

    private ProjectDAO projectDAO;
    private UserDAO userDAO;

    private int testProjectId = 1000;
    private int testCeoId = 1001;
    private int testStaffId = 1002;
    private Project testProject;

    @BeforeEach
    public void setUp() throws SQLException {
        projectDAO = new ProjectDAO();
        userDAO = new UserDAO();

        // Clean up any old test data
        projectDAO.deleteProject(testProjectId);
        userDAO.removeUser(testCeoId);
        userDAO.removeUser(testStaffId);

        // Add dummy users for foreign key constraints
        userDAO.addUser(testCeoId, "Test CEO", "ceo@test.com", "password", "CEO");
        userDAO.addUser(testStaffId, "Test Staff", "staff@test.com", "password", "Staff");
        
        testProject = new Project(
            testProjectId,
            "Music Video Production",
            "Video",
            LocalDateTime.now(),
            LocalDateTime.now().plusMonths(3),
            "A new music video for the latest hit single.",
            testCeoId,
            testStaffId
        );
    }

    @AfterEach
    public void tearDown() throws SQLException {
        // Clean up test data
        projectDAO.deleteProject(testProjectId);
        projectDAO.deleteProject(testProjectId + 1); // Clean up project from getNextProjectId test
        userDAO.removeUser(testCeoId);
        userDAO.removeUser(testStaffId);
    }

    @Test
    public void testAddAndGetAllProjects() throws SQLException {
        // 1. Add a project
        projectDAO.addProjectForTest(testProject);

        // 2. Get all projects
        List<Project> projects = projectDAO.getAllProjects();

        // 3. Verify the new project is in the list
        assertNotNull(projects);
        assertFalse(projects.isEmpty());
        boolean found = projects.stream().anyMatch(p -> p.getIdProject() == testProjectId);
        assertTrue(found, "The newly added project should be in the list of all projects.");
    }

    @Test
    public void testGetNextProjectId() throws SQLException {
        // 1. Get the next project ID from a clean state
        int nextIdBefore = projectDAO.getNextProjectId();

        // 2. Add a project with that ID
        Project nextProject = new Project(
            nextIdBefore,
            "Concert Planning",
            "Live Event",
            LocalDateTime.now(),
            LocalDateTime.now().plusWeeks(10),
            "Planning for the summer tour.",
            testCeoId,
            testStaffId
        );
        projectDAO.addProjectForTest(nextProject);
        testProjectId = nextIdBefore; // Set for cleanup

        // 3. Get the next project ID again
        int nextIdAfter = projectDAO.getNextProjectId();

        // 4. Verify it has incremented
        assertEquals(nextIdBefore + 1, nextIdAfter, "The next project ID should increment after adding a new project.");
    }
} 
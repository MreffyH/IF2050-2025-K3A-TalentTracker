package com.talenttracker.dao;

import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.User;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;

public class UserDAOTest {

    @Test
    public void testGetUserById() {
        UserDAO userDAO = new UserDAO();
        try {
            // Assuming a user with ID 1 exists in the test database
            User user = userDAO.getUserById(1);
            assertNotNull(user, "User should not be null");
            assertEquals(1, user.getId(), "User ID should be 1");
        } catch (SQLException e) {
            fail("SQLException was thrown: " + e.getMessage());
        }
    }
} 
package com.example.app.dao;

import com.example.app.model.User;
import java.util.List;

public interface UserDAO {
    List<User> getUsersByRole(String role);
} 
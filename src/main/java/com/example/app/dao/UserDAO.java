package com.example.app.dao;

import java.util.List;

import com.example.app.model.User;

public interface UserDAO {
    List<User> getUsersByRole(String role);
    User getUserById(int id);
} 
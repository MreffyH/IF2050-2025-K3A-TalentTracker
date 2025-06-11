package com.example.app.dao;

import java.util.List;

import com.example.app.model.User;

public interface ProjectArtistDAO {
    void addArtistsToProject(int projectId, List<Integer> artistIds);
    List<User> getArtistsForProject(int projectId);
} 
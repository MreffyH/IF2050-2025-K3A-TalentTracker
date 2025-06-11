package com.example.app.dao;

import java.util.List;

import com.example.app.model.Project;

public interface ProjectDAO {
    boolean addProject(Project project);
    // updateProject and deleteProject can be re-implemented later if needed
    // void updateProject(Project project);
    // void deleteProject(int projectId);
    Project getProjectById(int projectId);
    List<Project> getAllProjects();
    int getNextProjectId();
} 
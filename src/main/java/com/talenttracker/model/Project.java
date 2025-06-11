package com.talenttracker.model;

import java.time.LocalDateTime;

public class Project {
    private int idProject;
    private String projectName;
    private String type;
    private String description;
    private int idCEO;
    private int idStaff;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // Constructors
    public Project() {
    }

    // Getters and Setters
    public int getIdProject() {
        return idProject;
    }

    public void setIdProject(int idProject) {
        this.idProject = idProject;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getIdCEO() {
        return idCEO;
    }

    public void setIdCEO(int idCEO) {
        this.idCEO = idCEO;
    }

    public int getIdStaff() {
        return idStaff;
    }

    public void setIdStaff(int idStaff) {
        this.idStaff = idStaff;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
} 
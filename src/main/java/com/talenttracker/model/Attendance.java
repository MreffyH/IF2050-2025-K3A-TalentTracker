package com.talenttracker.model;

import java.sql.Date;
import java.sql.Time;

public class Attendance {
    private int id;
    private int idStaff;
    private Date date;
    private Time time;
    private boolean onTime; // Maps to tinyint(1)
    private int workingHours; // in seconds

    // Constructors
    public Attendance() {
    }

    public Attendance(int id, int idStaff, Date date, Time time, boolean onTime, int workingHours) {
        this.id = id;
        this.idStaff = idStaff;
        this.date = date;
        this.time = time;
        this.onTime = onTime;
        this.workingHours = workingHours;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdStaff() {
        return idStaff;
    }

    public void setIdStaff(int idStaff) {
        this.idStaff = idStaff;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public boolean isOnTime() {
        return onTime;
    }

    public void setOnTime(boolean onTime) {
        this.onTime = onTime;
    }

    public int getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(int workingHours) {
        this.workingHours = workingHours;
    }
} 
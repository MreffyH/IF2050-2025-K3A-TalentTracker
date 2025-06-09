package com.example.controller;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class AttendanceDashboardController implements Initializable {

    @FXML
    private VBox attendanceRecordsVBox;

    @FXML
    private Canvas analogClockCanvas;

    @FXML
    private Canvas onTimeChartCanvas;

    @FXML
    private Canvas lateChartCanvas;

    @FXML
    private Label digitalTimeLabel;

    @FXML
    private Label workingTimeLabel;

    @FXML
    private Label salaryAmountLabel;

    @FXML
    private Button checkButton;
    
    @FXML
    private Button viewSalaryButton;

    @FXML
    private Circle profileCircle;

    private Timeline clockTimeline;
    private Timeline workingTimeline;
    private LocalDateTime checkInTime;
    private boolean isCheckedIn = false;
    private boolean salaryVisible = false;

    private List<AttendanceRecord> attendanceRecords = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize profile circle
        profileCircle.setFill(Color.WHITE);
        
        // Initialize attendance records
        loadAttendanceRecords();
        displayAttendanceRecords();

        // Initialize charts
        drawOnTimeChart();
        drawLateChart();

        // Initialize clock
        initializeClock();
    }

    private void loadAttendanceRecords() {
        // Sample data - in a real app, this would come from a database
        attendanceRecords.add(new AttendanceRecord("25-01-2025", "09:46 AM", true));
        attendanceRecords.add(new AttendanceRecord("25-01-2025", "10:00 AM", false));
        attendanceRecords.add(new AttendanceRecord("25-01-2025", "09:46 AM", true));
        attendanceRecords.add(new AttendanceRecord("25-01-2025", "09:46 AM", true));
        attendanceRecords.add(new AttendanceRecord("25-01-2023", "09:46 AM", true));
        attendanceRecords.add(new AttendanceRecord("25-01-2023", "09:46 AM", true));
        attendanceRecords.add(new AttendanceRecord("25-01-2023", "10:00 AM", false));
        attendanceRecords.add(new AttendanceRecord("25-01-2023", "09:46 AM", true));
        attendanceRecords.add(new AttendanceRecord("25-01-2023", "09:46 AM", true));
        attendanceRecords.add(new AttendanceRecord("25-01-2023", "09:46 AM", true));
        attendanceRecords.add(new AttendanceRecord("25-01-2023", "09:46 AM", true));
    }

    private void displayAttendanceRecords() {
        attendanceRecordsVBox.getChildren().clear();
        
        for (AttendanceRecord record : attendanceRecords) {
            HBox recordRow = new HBox();
            recordRow.getStyleClass().add("record-row");
            recordRow.setPadding(new Insets(12, 0, 12, 0));
            recordRow.setSpacing(20);
            
            // Date on left
            Label dateLabel = new Label(record.getDate());
            dateLabel.getStyleClass().add("record-date");
            
            // Spacer
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            // Time with status dot on right
            HBox timeBox = new HBox();
            timeBox.setSpacing(15);
            timeBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            
            Circle statusDot = new Circle(5);
            statusDot.getStyleClass().add(record.isOnTime() ? "status-dot-on-time" : "status-dot-absent");
            
            Label timeLabel = new Label(record.getTime());
            timeLabel.getStyleClass().add("time-text");
            
            timeBox.getChildren().addAll(statusDot, timeLabel);
            
            recordRow.getChildren().addAll(dateLabel, spacer, timeBox);
            
            attendanceRecordsVBox.getChildren().add(recordRow);
        }
    }

    private void initializeClock() {
        // Initialize the clock timeline
        clockTimeline = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> {
                updateClock();
            })
        );
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
        
        // Initial update
        updateClock();
    }

    private void updateClock() {
        LocalDateTime now = LocalDateTime.now();
        
        // Update digital clock with uppercase AM/PM
        int hour = now.getHour();
        int minute = now.getMinute();
        String amPm = hour >= 12 ? "PM" : "AM";
        int displayHour = hour % 12;
        if (displayHour == 0) displayHour = 12;
        
        digitalTimeLabel.setText(String.format("%d : %02d %s", displayHour, minute, amPm));
        
        // Draw analog clock
        drawAnalogClock(now);
    }

    private void drawAnalogClock(LocalDateTime time) {
        double centerX = analogClockCanvas.getWidth() / 2;
        double centerY = analogClockCanvas.getHeight() / 2;
        double radius = Math.min(centerX, centerY) - 15;
        
        GraphicsContext gc = analogClockCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, analogClockCanvas.getWidth(), analogClockCanvas.getHeight());
        
        // Draw clock face background
        gc.setFill(Color.web("#dbeafe"));
        gc.setGlobalAlpha(0.4);
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        gc.setGlobalAlpha(1.0);
        
        // Draw clock face
        gc.setFill(Color.WHITE);
        gc.fillOval(centerX - radius * 0.85, centerY - radius * 0.85, radius * 1.7, radius * 1.7);
        gc.setStroke(Color.web("#e5e7eb"));
        gc.setLineWidth(2);
        gc.strokeOval(centerX - radius * 0.85, centerY - radius * 0.85, radius * 1.7, radius * 1.7);
        
        // Draw hour markers
        gc.setStroke(Color.web("#9ca3af"));
        gc.setLineWidth(3);
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians(i * 30);
            double startX = centerX + Math.sin(angle) * (radius * 0.8);
            double startY = centerY - Math.cos(angle) * (radius * 0.8);
            double endX = centerX + Math.sin(angle) * radius;
            double endY = centerY - Math.cos(angle) * radius;
            gc.strokeLine(startX, startY, endX, endY);
        }
        
        // Calculate hand angles
        int hour = time.getHour() % 12;
        int minute = time.getMinute();
        int second = time.getSecond();
        
        double hourAngle = Math.toRadians((hour * 30) + (minute * 0.5));
        double minuteAngle = Math.toRadians(minute * 6);
        double secondAngle = Math.toRadians(second * 6);
        
        // Draw hour hand
        drawClockHand(gc, centerX, centerY, hourAngle, radius * 0.5, 5, Color.web("#1f2937"));
        
        // Draw minute hand
        drawClockHand(gc, centerX, centerY, minuteAngle, radius * 0.7, 4, Color.web("#4b5563"));
        
        // Draw second hand
        drawClockHand(gc, centerX, centerY, secondAngle, radius * 0.8, 2, Color.web("#ef4444"));
        
        // Draw center dot
        gc.setFill(Color.web("#1f2937"));
        gc.fillOval(centerX - 6, centerY - 6, 12, 12);
    }

    private void drawClockHand(GraphicsContext gc, double centerX, double centerY, 
                              double angle, double length, double width, Color color) {
        double endX = centerX + Math.sin(angle) * length;
        double endY = centerY - Math.cos(angle) * length;
        
        gc.setStroke(color);
        gc.setLineWidth(width);
        gc.strokeLine(centerX, centerY, endX, endY);
    }

    private void drawOnTimeChart() {
        GraphicsContext gc = onTimeChartCanvas.getGraphicsContext2D();
        drawChart(gc, Color.web("#22c55e"), 65);
    }

    private void drawLateChart() {
        GraphicsContext gc = lateChartCanvas.getGraphicsContext2D();
        drawChart(gc, Color.web("#ef4444"), 35);
    }

    private void drawChart(GraphicsContext gc, Color color, int percentage) {
        double width = gc.getCanvas().getWidth();
        double height = gc.getCanvas().getHeight();
        
        gc.clearRect(0, 0, width, height);
        
        // Create gradient
        LinearGradient gradient = new LinearGradient(
            0, 0, 0, height,
            false, CycleMethod.NO_CYCLE,
            new Stop(0, color.deriveColor(0, 1, 1, 0.3)),
            new Stop(1, color.deriveColor(0, 1, 1, 0.1))
        );
        
        // Generate points for the wave
        double[] xPoints = new double[120];
        double[] yPoints = new double[120];
        
        for (int i = 0; i < 120; i++) {
            xPoints[i] = width * i / 119;
            
            // Create a wave pattern
            double wave = Math.sin(i * 0.08) * 10;
            
            // Adjust wave height based on percentage
            double baseHeight = height * 0.7;
            double percentageEffect = (100 - percentage) / 100.0 * height * 0.4;
            
            yPoints[i] = baseHeight - wave - percentageEffect;
        }
        
        // Draw filled area
        gc.setFill(gradient);
        gc.beginPath();
        gc.moveTo(0, yPoints[0]);
        
        for (int i = 1; i < 120; i++) {
            gc.lineTo(xPoints[i], yPoints[i]);
        }
        
        // Complete the path to create a closed shape
        gc.lineTo(width, height);
        gc.lineTo(0, height);
        gc.closePath();
        gc.fill();
        
        // Draw the line on top
        gc.setStroke(color);
        gc.setLineWidth(3);
        gc.beginPath();
        gc.moveTo(0, yPoints[0]);
        
        for (int i = 1; i < 120; i++) {
            gc.lineTo(xPoints[i], yPoints[i]);
        }
        
        gc.stroke();
    }

    @FXML
    private void toggleCheckIn() {
        if (!isCheckedIn) {
            // Check in
            isCheckedIn = true;
            checkInTime = LocalDateTime.now();
            checkButton.setText("Check Out");
            checkButton.getStyleClass().remove("check-button-in");
            checkButton.getStyleClass().add("check-button-out");
            
            // Start working time counter
            if (workingTimeline != null) {
                workingTimeline.stop();
            }
            
            workingTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    updateWorkingTime();
                })
            );
            workingTimeline.setCycleCount(Animation.INDEFINITE);
            workingTimeline.play();
            
            // Add new attendance record with current time in uppercase AM/PM format
            LocalDate today = LocalDate.now();
            String formattedDate = today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            
            // Format time with uppercase AM/PM
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
            String formattedTime = LocalDateTime.now().format(timeFormatter).toUpperCase();
            
            // Determine if on time (assuming work starts at 9:00 AM)
            boolean onTime = LocalDateTime.now().getHour() < 9 || 
                           (LocalDateTime.now().getHour() == 9 && LocalDateTime.now().getMinute() <= 0);
            
            AttendanceRecord newRecord = new AttendanceRecord(formattedDate, formattedTime, onTime);
            attendanceRecords.add(0, newRecord); // Add to the beginning of the list
            displayAttendanceRecords();
            
        } else {
            // Check out
            isCheckedIn = false;
            checkInTime = null;
            checkButton.setText("Check In");
            checkButton.getStyleClass().remove("check-button-out");
            checkButton.getStyleClass().add("check-button-in");
            
            // Stop working time counter
            if (workingTimeline != null) {
                workingTimeline.stop();
            }
            
            workingTimeLabel.setText("0 Hr 00 Mins 00 Secs");
        }
    }

    private void updateWorkingTime() {
        if (checkInTime != null) {
            LocalDateTime now = LocalDateTime.now();
            long secondsDiff = java.time.Duration.between(checkInTime, now).getSeconds();
            
            long hours = secondsDiff / 3600;
            long minutes = (secondsDiff % 3600) / 60;
            long seconds = secondsDiff % 60;
            
            workingTimeLabel.setText(String.format("%d Hr %02d Mins %02d Secs", hours, minutes, seconds));
        }
    }

    @FXML
    private void handleViewSalary() {
        // Toggle salary visibility instead of showing popup
        if (!salaryVisible) {
            salaryAmountLabel.setText("IDR 5,000,000");
            viewSalaryButton.setText("Hide");
            salaryVisible = true;
        } else {
            salaryAmountLabel.setText("IDR ********");
            viewSalaryButton.setText("View");
            salaryVisible = false;
        }
    }

    // Helper class for attendance records
    private static class AttendanceRecord {
        private String date;
        private String time;
        private boolean onTime;

        public AttendanceRecord(String date, String time, boolean onTime) {
            this.date = date;
            this.time = time;
            this.onTime = onTime;
        }

        public String getDate() {
            return date;
        }

        public String getTime() {
            return time;
        }

        public boolean isOnTime() {
            return onTime;
        }
    }
}
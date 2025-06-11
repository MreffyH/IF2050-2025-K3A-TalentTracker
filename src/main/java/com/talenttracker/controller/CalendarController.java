package com.talenttracker.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.input.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class CalendarController {
    @FXML private GridPane calendarGrid;
    @FXML private GridPane weekDaysGrid;
    @FXML private Label monthLabel;
    @FXML private Label yearLabel;
    @FXML private Button prevMonth;
    @FXML private Button nextMonth;

    private YearMonth currentYearMonth;
    private LocalDate today;
    private LocalDate selectedDate;
    private Label selectedLabel;
    private final Locale locale = new Locale("en", "US");

    @FXML
    public void initialize() {
        today = LocalDate.now();
        currentYearMonth = YearMonth.from(today);
        selectedDate = today;
        
        prevMonth.setOnAction(e -> handlePrevMonth());
        nextMonth.setOnAction(e -> handleNextMonth());
        
        setupWeekDays();
        updateCalendar();
    }

    private void setupWeekDays() {
        for (int i = 0; i < 7; i++) {
            DayOfWeek day = DayOfWeek.of(((i + 1) % 7) + 1);
            Label dayLabel = new Label(day.getDisplayName(TextStyle.SHORT, locale).substring(0, 2));
            dayLabel.getStyleClass().add("calendar-header");
            weekDaysGrid.add(dayLabel, i, 0);
        }
    }

    private void updateCalendar() {
        monthLabel.setText(currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, locale));
        yearLabel.setText(String.valueOf(currentYearMonth.getYear()));

        calendarGrid.getChildren().clear();

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int daysInMonth = currentYearMonth.lengthOfMonth();
        
        int dayOfWeek = (firstOfMonth.getDayOfWeek().getValue() - 1) % 7;

        int day = 1;
        int row = 0;

        YearMonth prevMonth = currentYearMonth.minusMonths(1);
        int prevMonthDays = prevMonth.lengthOfMonth();
        for (int i = 0; i < dayOfWeek; i++) {
            Label dateLabel = new Label(String.valueOf(prevMonthDays - dayOfWeek + i + 1));
            dateLabel.getStyleClass().addAll("calendar-date", "inactive-date");
            calendarGrid.add(dateLabel, i, row);
        }
        
        while (day <= daysInMonth) {
            for (int col = dayOfWeek; col < 7 && day <= daysInMonth; col++) {
                Label dateLabel = new Label(String.valueOf(day));
                dateLabel.getStyleClass().add("calendar-date");
                
                final int currentDay = day;
                dateLabel.setUserData(currentYearMonth.atDay(currentDay));
                
                dateLabel.setOnMouseClicked(e -> handleDateClick(dateLabel));
                
                if (today.equals(currentYearMonth.atDay(day))) {
                    dateLabel.getStyleClass().add("today");
                    dateLabel.setStyle("-fx-background-color: #223055; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 0; -fx-padding: 5;");
                }
                
                if (selectedDate != null && selectedDate.equals(currentYearMonth.atDay(day))) {
                    dateLabel.getStyleClass().add("selected");
                    selectedLabel = dateLabel;
                }
                
                calendarGrid.add(dateLabel, col, row);
                day++;
            }
            row++;
            dayOfWeek = 0;
        }

        int nextMonthDay = 1;
        while (row < 6) {
            for (int col = dayOfWeek; col < 7; col++) {
                Label dateLabel = new Label(String.valueOf(nextMonthDay));
                dateLabel.getStyleClass().addAll("calendar-date", "inactive-date");
                calendarGrid.add(dateLabel, col, row);
                nextMonthDay++;
            }
            row++;
            dayOfWeek = 0;
        }
    }

    private void handleDateClick(Label dateLabel) {
        if (selectedLabel != null) {
            selectedLabel.getStyleClass().remove("selected");
        }
        
        dateLabel.getStyleClass().add("selected");
        selectedLabel = dateLabel;
        
        selectedDate = (LocalDate) dateLabel.getUserData();
    }

    @FXML
    private void handlePrevMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        updateCalendar();
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        updateCalendar();
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }
} 
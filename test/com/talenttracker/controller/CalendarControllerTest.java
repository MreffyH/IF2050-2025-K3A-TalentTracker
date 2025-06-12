package com.talenttracker.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ApplicationExtension.class)
public class CalendarControllerTest {

    private CalendarController controller;

    @Start
    private void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CalendarView.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    void testInitialDisplay(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        Label monthLabel = robot.lookup("#monthLabel").queryAs(Label.class);
        Label yearLabel = robot.lookup("#yearLabel").queryAs(Label.class);

        LocalDate today = LocalDate.now();
        String expectedMonth = today.getMonth().getDisplayName(TextStyle.FULL, new Locale("en", "US"));
        String expectedYear = String.valueOf(today.getYear());

        assertEquals(expectedMonth, monthLabel.getText());
        assertEquals(expectedYear, yearLabel.getText());
    }

    @Test
    void testMonthNavigation(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();
        LocalDate today = LocalDate.now();

        // Go to the next month
        robot.clickOn("#nextMonth");
        WaitForAsyncUtils.waitForFxEvents();

        YearMonth nextMonth = YearMonth.from(today).plusMonths(1);
        String expectedNextMonth = nextMonth.getMonth().getDisplayName(TextStyle.FULL, new Locale("en", "US"));
        String expectedNextYear = String.valueOf(nextMonth.getYear());

        assertEquals(expectedNextMonth, robot.lookup("#monthLabel").queryAs(Label.class).getText());
        assertEquals(expectedNextYear, robot.lookup("#yearLabel").queryAs(Label.class).getText());

        // Go back to the current month
        robot.clickOn("#prevMonth");
        WaitForAsyncUtils.waitForFxEvents();

        String expectedCurrentMonth = today.getMonth().getDisplayName(TextStyle.FULL, new Locale("en", "US"));
        String expectedCurrentYear = String.valueOf(today.getYear());

        assertEquals(expectedCurrentMonth, robot.lookup("#monthLabel").queryAs(Label.class).getText());
        assertEquals(expectedCurrentYear, robot.lookup("#yearLabel").queryAs(Label.class).getText());
    }

    @Test
    void testDateSelection(FxRobot robot) {
        WaitForAsyncUtils.waitForFxEvents();

        // Click on the 15th day of the current month
        robot.clickOn((Label l) -> "15".equals(l.getText()) && l.getStyleClass().contains("calendar-date") && !l.getStyleClass().contains("inactive-date"));
        WaitForAsyncUtils.waitForFxEvents();

        LocalDate expectedDate = YearMonth.now().atDay(15);
        assertEquals(expectedDate, controller.getSelectedDate());
    }
} 
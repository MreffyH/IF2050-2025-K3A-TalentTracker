/*
 *  Copyright (C) 2024 Your Organization. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.calendarfx.app;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Calendar.Style;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;

public class CalendarApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Load Gabarito fonts
        Font.loadFont(getClass().getResourceAsStream("/fonts/Gabarito-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Gabarito-Medium.ttf"), 14);

        // Create main layout
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #FFE4E9;");
        root.setPadding(new Insets(10));

        // Create calendar view
        CalendarView calendarView = new CalendarView();
        calendarView.getStylesheets().add(getClass().getResource("/styles/calendar-custom.css").toExternalForm());

        // Create calendar sources
        CalendarSource artistSource = new CalendarSource("Artists");

        // Create different calendars for different artist types
        Calendar soloArtists = new Calendar("Solo Artists");
        soloArtists.setStyle(Style.STYLE1);

        Calendar groups = new Calendar("Groups");
        groups.setStyle(Style.STYLE2);

        Calendar trainees = new Calendar("Trainees");
        trainees.setStyle(Style.STYLE3);

        // Add calendars to source
        artistSource.getCalendars().addAll(soloArtists, groups, trainees);
        calendarView.getCalendarSources().add(artistSource);

        // Add sample entries
        LocalDate today = LocalDate.now();
        
        Entry<String> soloEntry = new Entry<>("IU Concert");
        soloEntry.setInterval(today.atTime(18, 0), today.atTime(20, 0));
        soloArtists.addEntry(soloEntry);

        Entry<String> groupEntry = new Entry<>("BTS Practice");
        groupEntry.setInterval(today.atTime(14, 0), today.atTime(17, 0));
        groups.addEntry(groupEntry);

        Entry<String> traineeEntry = new Entry<>("New Group Training");
        traineeEntry.setInterval(today.atTime(9, 0), today.atTime(12, 0));
        trainees.addEntry(traineeEntry);

        // Add calendar view to root
        root.getChildren().add(calendarView);

        // Create scene
        Scene scene = new Scene(root);
        primaryStage.setTitle("SN Entertainment Artist Schedule");
        primaryStage.setScene(scene);
        primaryStage.setWidth(1920);
        primaryStage.setHeight(1080);
        primaryStage.centerOnScreen();
        primaryStage.show();

        // Update time
        Thread updateTimeThread = new Thread("Calendar: Update Time Thread") {
            @Override
            public void run() {
                while (true) {
                    Platform.runLater(() -> {
                        calendarView.setToday(LocalDate.now());
                        calendarView.setTime(LocalTime.now());
                    });

                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        updateTimeThread.setPriority(Thread.MIN_PRIORITY);
        updateTimeThread.setDaemon(true);
        updateTimeThread.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

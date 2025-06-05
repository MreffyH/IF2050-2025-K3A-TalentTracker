# Talent Tracker Calendar

A Java desktop application for managing schedules and events. The application provides three different calendar views:
- Daily view with hourly timeline
- Weekly view with day-by-day events
- Monthly view with a traditional calendar layout

## Features
- Modern and clean UI design
- Easy navigation between days, weeks, and months
- Event display with time and location
- Different calendar views (Day, Week, Month)

## Requirements
- Java Development Kit (JDK) 11 or higher
- Java Swing (included in JDK)

## Running the Application
1. Compile the Java files:
```bash
javac -d out src/main/java/com/talenttracker/**/*.java
```

2. Run the application:
```bash
java -cp out com.talenttracker.Main
```

## Project Structure
```
src/main/java/com/talenttracker/
├── Main.java                 # Application entry point
├── ui/
│   ├── MainFrame.java       # Main application window
│   ├── BaseCalendarPanel.java # Base class for calendar views
│   ├── DayViewPanel.java    # Daily calendar view
│   ├── WeekViewPanel.java   # Weekly calendar view
│   └── MonthViewPanel.java  # Monthly calendar view
```
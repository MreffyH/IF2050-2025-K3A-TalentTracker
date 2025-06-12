# TalentTracker

## Description

TalentTracker is an Artist and Staff Management System developed for SN Entertainment. This software is designed to streamline the management of artist schedules, performance evaluations, and general staff administration.

The system provides a centralized platform for managing artist schedules, conducting objective performance evaluations, and ensuring efficient staff management. The workflow begins with user login, after which staff can manage schedules, evaluate performance, and generate analyses and reports. TalentTracker also integrates with external systems for functionalities like digital calendars, assessment matrices, and payroll modules.

This system build by Group A K03 :
- Muhammad Reffy Haykal (18222103)
- Moh Afnan Fawaz (18222111)
- Aqila Ataa (18222120)
- Gymnastiar Anwar (18222121)
- Fadian Alif Mahardika (18222124)

## Key Features

- **Centralized Schedule Management**: View and manage schedules for all artists in one place.
- **Objective Performance Evaluation**: Track and evaluate artist performance using defined metrics.
- **Efficient Staff Management**: Handle staff-related administrative tasks.
- **Reporting and Analytics**: Generate performance reports, apply filters, and analyze data.

## User Roles

The system is designed for the following user roles:
- **CEO**: Has a complete overview of all schedules, performances, and reports.
- **Artist**: Can view their own schedule and performance reports.
- **Staff**: Manages artist schedules, inputs performance data, and handles administrative tasks.

## Environment Setup

Before running the application, you need to set up the database.

1.  **Configure Database Credentials**:
    Create a file named `.env` inside the `src/main/resources/` directory. Add the following properties to this file, replacing the placeholder values with your actual MySQL database credentials:

    ```
    db.url=jdbc:mysql://localhost:3306/your_database_name
    db.user=your_username
    db.password=your_password
    ```

2.  **Create Database Schema**:
    You need to create the database and its tables using the provided SQL script.
    - Create a new database in MySQL with the same name you specified in the `.env` file.
    - Execute the `src/main/resources/database/schema.sql` script in your MySQL client to create the necessary tables.

## Prerequisites

- **Java Development Kit (JDK)**: Version 17 or higher must be installed on your system.

## How to Run the Application

You can run the application directly from the executable JAR file.

1.  **Build the project**: If you have made changes to the source code, you need to build the project first. Open a terminal in the project's root directory and run:
    ```bash
    mvn clean package
    ```
    This will create the executable JAR file in the `target/` directory.

2.  **Run the JAR file**: Open a terminal or command prompt, navigate to the project's root directory, and execute the following command:
    ```bash
    java -jar talent-tracker-1.0-SNAPSHOT.jar
    ```

The application window should now open.
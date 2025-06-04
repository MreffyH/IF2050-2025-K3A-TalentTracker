import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class viewAbsensi extends Application {
    @Override
    public void start(Stage primaryStage) {
        // Top Navigation Bar
        HBox topBar = new HBox(30);
        topBar.setPadding(new Insets(20, 40, 20, 40));
        topBar.setStyle("-fx-background-color: #23305A;");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label logo = new Label("[Logo]"); // Placeholder for logo
        logo.setTextFill(Color.WHITE);
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        Label dashboard = navLabel("Dashboard");
        Label project = navLabel("Project");
        Label attendance = navLabel("Attendance");
        Label schedule = navLabel("Schedule");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        VBox userBox = new VBox(2);
        Label userName = new Label("Reffy Mahardika");
        userName.setTextFill(Color.WHITE);
        userName.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        Label userRole = new Label("Staff");
        userRole.setTextFill(Color.WHITE);
        userRole.setFont(Font.font("Arial", 14));
        // Placeholder for user icon
        Circle userIcon = new Circle(20, Color.TRANSPARENT);
        userIcon.setStroke(Color.WHITE);
        userIcon.setStrokeWidth(2);
        StackPane userIconPane = new StackPane(userIcon, new Label("👤"));
        userBox.getChildren().addAll(userName, userRole);
        HBox userInfo = new HBox(10, userBox, userIconPane);
        userInfo.setAlignment(Pos.CENTER);
        topBar.getChildren().addAll(logo, dashboard, project, attendance, schedule, spacer, userInfo);

        // Main Background
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setStyle("-fx-background-color: #FFC1D3;");

        // Center Layout
        HBox centerBox = new HBox(30);
        centerBox.setPadding(new Insets(40, 40, 40, 40));

        // Attendance Table (Left)
        VBox tableBox = new VBox(10);
        tableBox.setPadding(new Insets(0, 0, 0, 0));
        tableBox.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, #e0e0e0, 10, 0, 0, 2);");
        tableBox.setPrefWidth(350);
        tableBox.setAlignment(Pos.TOP_CENTER);
        Label tableTitle = new Label("");
        TableView<String[]> table = new TableView<>();
        table.setPrefHeight(400);
        TableColumn<String[], String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[0]));
        TableColumn<String[], String> timeCol = new TableColumn<>("Time In");
        timeCol.setCellValueFactory(param -> new javafx.beans.property.SimpleStringProperty(param.getValue()[1]));
        table.getColumns().addAll(dateCol, timeCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // Sample data
        String[][] data = {
            {"25-01-2025", "09:46 AM"},
            {"25-01-2025", "10:00 AM"},
            {"25-01-2025", "09:46 AM"},
            {"25-01-2025", "09:46 AM"},
            {"25-01-2023", "09:46 AM"},
            {"25-01-2023", "09:46 AM"},
            {"25-01-2023", "10:00 AM"},
            {"25-01-2023", "09:46 AM"},
            {"25-01-2023", "09:46 AM"},
            {"25-01-2023", "09:46 AM"}
        };
        for (String[] row : data) table.getItems().add(row);
        HBox legend = new HBox(10);
        legend.setAlignment(Pos.CENTER_LEFT);
        Circle onTime = new Circle(6, Color.web("#4CAF50"));
        Label onTimeLbl = new Label("On Time");
        Circle absent = new Circle(6, Color.web("#FF6F61"));
        Label absentLbl = new Label("Absent");
        legend.getChildren().addAll(onTime, onTimeLbl, absent, absentLbl);
        tableBox.getChildren().addAll(tableTitle, table, legend);

        // Right Side (Stats + Clock)
        VBox rightBox = new VBox(20);
        rightBox.setAlignment(Pos.TOP_CENTER);

        // Stats Cards
        HBox statsCards = new HBox(20);
        statsCards.setAlignment(Pos.CENTER);
        statsCards.getChildren().addAll(
            statCard("On Time Percentage", "February", "65%", Color.web("#4CAF50")),
            statCard("Late Percentage", "February", "35%", Color.web("#FF6F61")),
            salaryCard()
        );

        // Clock and Check-in
        VBox clockBox = new VBox(20);
        clockBox.setAlignment(Pos.CENTER);
        clockBox.setPadding(new Insets(20));
        clockBox.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, #e0e0e0, 10, 0, 0, 2);");
        // Clock placeholder
        StackPane clockPane = new StackPane();
        Circle clockCircle = new Circle(80, Color.web("#F2F6FA"));
        clockCircle.setStroke(Color.LIGHTGRAY);
        clockCircle.setStrokeWidth(2);
        // Simple clock hands (static)
        javafx.scene.shape.Line hourHand = new javafx.scene.shape.Line(0, 0, 0, -40);
        hourHand.setStrokeWidth(4);
        hourHand.setStroke(Color.web("#23305A"));
        javafx.scene.shape.Line minHand = new javafx.scene.shape.Line(0, 0, 40, 0);
        minHand.setStrokeWidth(2);
        minHand.setStroke(Color.web("#FF6F61"));
        clockPane.getChildren().addAll(clockCircle, hourHand, minHand);
        clockPane.setPrefSize(160, 160);
        clockPane.setMaxSize(160, 160);
        // Time label
        Label timeLabel = new Label("12 : 16 PM");
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        timeLabel.setTextFill(Color.web("#23305A"));
        // Working hours
        VBox workingBox = new VBox(5);
        Label workingTitle = new Label("Working Hours");
        workingTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        workingTitle.setTextFill(Color.web("#23305A"));
        Label workingTime = new Label("0 Hr 00 Mins 00 Secs");
        workingTime.setFont(Font.font("Arial", 16));
        workingTime.setTextFill(Color.web("#23305A"));
        workingBox.getChildren().addAll(workingTitle, workingTime);
        workingBox.setStyle("-fx-background-color: #F8FBFF; -fx-background-radius: 10; -fx-padding: 10 20 10 20; -fx-border-color: #E0E0E0; -fx-border-radius: 10;");
        // Check-in button
        Button checkIn = new Button("Check In");
        checkIn.setStyle("-fx-background-color: linear-gradient(to right, #4CAF50, #8BC34A); -fx-text-fill: white; -fx-font-size: 18; -fx-background-radius: 8;");
        checkIn.setPrefWidth(300);
        checkIn.setPrefHeight(40);
        clockBox.getChildren().addAll(clockPane, timeLabel, workingBox, checkIn);

        rightBox.getChildren().addAll(statsCards, clockBox);

        centerBox.getChildren().addAll(tableBox, rightBox);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 1365, 768);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Attendance Dashboard");
        primaryStage.show();
    }

    private Label navLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 22));
        return label;
    }

    private VBox statCard(String title, String subtitle, String value, Color color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.setPrefSize(180, 90);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, #e0e0e0, 10, 0, 0, 2);");
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.web("#23305A"));
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.setFont(Font.font("Arial", 12));
        subtitleLabel.setTextFill(Color.web("#23305A"));
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        valueLabel.setTextFill(color);
        // Placeholder for chart/graph
        Region chart = new Region();
        chart.setPrefSize(60, 20);
        card.getChildren().addAll(titleLabel, subtitleLabel, valueLabel, chart);
        return card;
    }

    private VBox salaryCard() {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15));
        card.setPrefSize(220, 90);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, #e0e0e0, 10, 0, 0, 2);");
        Label info = new Label("Based by your Attendance\nHere is your monthly salary");
        info.setFont(Font.font("Arial", 12));
        info.setTextFill(Color.web("#23305A"));
        Label salary = new Label("IDR ********");
        salary.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        salary.setTextFill(Color.web("#23305A"));
        Button viewBtn = new Button("View");
        viewBtn.setStyle("-fx-background-color: #23305A; -fx-text-fill: white; -fx-background-radius: 8;");
        card.getChildren().addAll(info, salary, viewBtn);
        return card;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

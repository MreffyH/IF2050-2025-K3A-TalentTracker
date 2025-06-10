package com.talenttracker.controller;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.stage.FileChooser;

import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

import com.talenttracker.DatabaseManager;
import com.talenttracker.Main;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class LaporanKinerjaController {

    // --- Injected Header Controller ---
    @FXML private HeaderController headerComponentController;

    // --- UI Elements ---
    @FXML private Label successMessageLabel;
    @FXML private ImageView artistAvatarView;
    @FXML private Label artistNameLabel;
    @FXML private Button reportButton;
    @FXML private ImageView downloadIcon;

    // Charts
    @FXML private BarChart<String, Number> topAlbumChart;
    @FXML private CategoryAxis topAlbumXAxis;
    @FXML private NumberAxis topAlbumYAxis;
    @FXML private LineChart<String, Number> socialMediaChart;
    @FXML private CategoryAxis socialMediaXAxis;
    @FXML private NumberAxis socialMediaYAxis;

    // Album Form
    @FXML private TextField albumNameField;
    @FXML private TextField albumSoldField;
    @FXML private Button addAlbumButton;
    @FXML private Button addAlbumSoldButton;
    @FXML private TextField addAlbumSoldAmountField;

    // Social Media Form
    @FXML private ComboBox<String> socialMediaComboBox;
    @FXML private TextField followersField;
    @FXML private Button addSocialButton;

    // Visitors Elements
    @FXML private Label visitorsCountLabel;
    @FXML private TextField visitorsField;
    @FXML private Button addVisitorsButton;
    @FXML private ImageView visitorsIconView;

    // Sales Elements
    @FXML private Label salesAmountLabel;
    @FXML private TextField salesTodayField;
    @FXML private Button addSalesButton;
    @FXML private ImageView salesIconView;

    // Album Sold Elements
    @FXML private Label albumsSoldCountLabel;
    @FXML private ImageView albumsSoldIconView;

    // Social Popularity Elements
    @FXML private Label socialPopularityChangeLabel;
    @FXML private Label socialTotalFollowersLabel;
    @FXML private Label visitorsChangeLabel;
    @FXML private Label salesChangeLabel;
    @FXML private Label albumsSoldChangeLabel;

    // --- Data Series for Charts ---
    private XYChart.Series<String, Number> topAlbumSeries;
    private XYChart.Series<String, Number> socialMediaSeries;

    private final Random random = new Random();
    private final List<String> barColors = Arrays.asList(
        "#FFB74D", "#FFA726", "#EC407A", "#42A5F5",
        "#AB47BC", "#81C784", "#4DB6AC", "#64B5F6", "#90A4AE"
    );

    private int artistId;
    private String artistFullName;

    public void setArtist(int artistId, String artistName) {
        this.artistId = artistId;
        this.artistFullName = artistName;
        this.artistNameLabel.setText(artistName);
        
        // Update profile image
        try {
            String profileImageName = artistName.replaceAll("\\s+", "") + "Profile.png";
            Image profileImage = new Image("file:img/" + profileImageName);
            if (!profileImage.isError()) {
                artistAvatarView.setImage(profileImage);
                downloadIcon.setImage(new Image("file:img/DownloadIcon.png"));
            } else {
                 System.err.println("Could not load profile image: " + profileImageName);
            }
        } catch (Exception e) {
            System.err.println("Error creating image for: " + artistName);
        }

        // Reload all data for the newly set artist
        loadAllDataFromDatabase();
    }
    
    @FXML
    public void initialize() {
        // Initialize chart series first
        topAlbumSeries = new XYChart.Series<>();
        topAlbumChart.getData().add(topAlbumSeries);
        socialMediaChart.getData().clear();
        socialMediaChart.setAnimated(true);

        // Setup button actions
        reportButton.setOnAction(event -> handleReportButton());
        addAlbumButton.setOnAction(e -> handleAddAlbum());
        addAlbumSoldButton.setOnAction(e -> handleAddAlbumSold());
        addSocialButton.setOnAction(e -> handleAddSocial());
        addVisitorsButton.setOnAction(e -> handleAddVisitors());
        addSalesButton.setOnAction(e -> handleAddSales());
        
        setupComboBox();
        initializeImageViews();

        // If the view is loaded for an Artist, automatically set them.
        if ("Artist".equalsIgnoreCase(Main.getLoggedInUserRole())) {
            setArtist(Main.getLoggedInUserId(), Main.getLoggedInUserFullName());
        }
    }

    private void initializeImageViews() {
        try {
            visitorsIconView.setImage(new Image("file:img/VisitorIcon.png"));
            salesIconView.setImage(new Image("file:img/MoneyIcon.png"));
            albumsSoldIconView.setImage(new Image("file:img/BagIcon.png"));
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
        }
    }

    private void setupComboBox() {
        socialMediaComboBox.setItems(FXCollections.observableArrayList(
            "Instagram", "X", "TikTok"
        ));
    }

    private void setupButtonHandlers() {
        addAlbumButton.setOnAction(e -> handleAddAlbum());
        addSocialButton.setOnAction(e -> handleAddSocial());
        addVisitorsButton.setOnAction(e -> handleAddVisitors());
        addSalesButton.setOnAction(e -> handleAddSales());
    }

    private void loadAllDataFromDatabase() {
        loadStats();
        loadTopAlbumChart();
        loadSocialMediaChart();
        loadSocialPopularityStats();
    }

    private void loadStats() {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        // Load and display today's stats directly
        double todayVisitors = getTotalForDate("Visitors", "visitorsToday", today, this.artistId);
        visitorsCountLabel.setText(String.format("%,.0f", todayVisitors));
        double todaySales = getTotalForDate("Sales", "salesToday", today, this.artistId);
        salesAmountLabel.setText(currencyFormat.format(todaySales));
        double todayAlbums = getTotalForDate("AlbumSold", "albumSoldToday", today, this.artistId);
        albumsSoldCountLabel.setText(String.format("%,.0f", todayAlbums));

        // Calculate and display percentage change from yesterday
        double yesterdayVisitors = getTotalForDate("Visitors", "visitorsToday", yesterday, this.artistId);
        updateChangeLabel(visitorsChangeLabel, todayVisitors, yesterdayVisitors, false);
        
        double yesterdaySales = getTotalForDate("Sales", "salesToday", yesterday, this.artistId);
        updateChangeLabel(salesChangeLabel, todaySales, yesterdaySales, true);
        
        double yesterdayAlbums = getTotalForDate("AlbumSold", "albumSoldToday", yesterday, this.artistId);
        updateChangeLabel(albumsSoldChangeLabel, todayAlbums, yesterdayAlbums, false);
    }

    private void loadTopAlbumChart() {
        topAlbumSeries.getData().clear();
        String sql = "SELECT albumName, sold FROM TopAlbum WHERE idArtis = ? ORDER BY sold DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    topAlbumSeries.getData().add(new XYChart.Data<>(rs.getString("albumName"), rs.getInt("sold")));
                }
            }
            styleBarChartNodes();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSocialMediaChart() {
        socialMediaChart.getData().clear();
        socialMediaChart.layout();

        String sql = "SELECT socialMedia, todayFollowers, date FROM Popularity WHERE idArtis = ? ORDER BY date ASC";

        Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();
        List<String> platforms = Arrays.asList("Instagram", "X", "TikTok");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for(String platform : platforms) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(platform);
            seriesMap.put(platform, series);
        }

         try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while(rs.next()){
                    String platform = rs.getString("socialMedia");
                    int followers = rs.getInt("todayFollowers");
                    LocalDate date = rs.getDate("date").toLocalDate();
                    
                    XYChart.Series<String, Number> series = seriesMap.get(platform);
                    if (series != null) {
                        series.getData().add(new XYChart.Data<>(date.format(formatter), followers));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        for (String platform : platforms) {
            socialMediaChart.getData().add(seriesMap.get(platform));
        }
    }

    private void loadSocialPopularityStats() {
        double currentFollowers = getFollowerTotalForDate(LocalDate.now());
        double lastMonthFollowers = getFollowerTotalForDate(LocalDate.now().minusMonths(1));

        socialTotalFollowersLabel.setText(String.format("%,.0f Followers", currentFollowers));
        updateChangeLabel(socialPopularityChangeLabel, currentFollowers, lastMonthFollowers, false);
    }

    private double getFollowerTotalForDate(LocalDate date) {
        double totalFollowers = 0;
        String sql = "SELECT SUM(todayFollowers) as total FROM Popularity WHERE idArtis = ? AND date <= ? " + 
                     "AND (socialMedia, date) IN " +
                     "(SELECT socialMedia, MAX(date) FROM Popularity WHERE idArtis = ? AND date <= ? GROUP BY socialMedia)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            pstmt.setInt(3, artistId);
            pstmt.setDate(4, java.sql.Date.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    totalFollowers = rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalFollowers;
    }

    private double getTotalForDate(String tableName, String columnName, LocalDate date, int artistId) {
        String sql = String.format("SELECT SUM(%s) AS total FROM %s WHERE date = ? AND idArtis = ?", columnName, tableName);
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(date));
            pstmt.setInt(2, artistId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    private void updateChangeLabel(Label label, double current, double previous, boolean isCurrency) {
        if (previous == 0) {
            if (current > 0) label.setText("+100%"); // Infinite growth
            else label.setText("N/A");
            return;
        }
        double change = ((current - previous) / previous) * 100;
        String prefix = change >= 0 ? "+" : "";
        label.setText(String.format("%s%.0f%%", prefix, change));
    }

    private List<String> getAllAlbumNamesForArtist(int artistId) {
        List<String> albumNames = new ArrayList<>();
        String sql = "SELECT DISTINCT albumName FROM TopAlbum WHERE idArtis = ? ORDER BY albumName";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, artistId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                albumNames.add(rs.getString("albumName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return albumNames;
    }

    private void handleAddAlbum() {
        try {
            String name = albumNameField.getText();
            String soldText = albumSoldField.getText();

            if (name == null || name.trim().isEmpty()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText("Album Name is required");
                alert.setContentText("Please enter a name for the new album.");
                alert.showAndWait();
                return;
            }
            if (soldText == null || soldText.trim().isEmpty()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText("Album Sold is required");
                alert.setContentText("Please enter the number of albums sold.");
                alert.showAndWait();
                return;
            }

            int sold = Integer.parseInt(soldText);

            try (Connection conn = DatabaseManager.getConnection()) {
                String selectSql = "SELECT albumName FROM TopAlbum WHERE albumName = ? AND idArtis = ?";
                try (PreparedStatement selectPstmt = conn.prepareStatement(selectSql)) {
                    selectPstmt.setString(1, name);
                    selectPstmt.setInt(2, artistId);
                    try (ResultSet rs = selectPstmt.executeQuery()) {
                        if (rs.next()) {
                            Alert alert = new Alert(AlertType.ERROR);
                            alert.setTitle("Duplicate Album");
                            alert.setHeaderText("Album already exists.");
                            alert.setContentText("The album '" + name + "' already exists. To add more sales, use the 'Add Album Sold' function.");
                            alert.showAndWait();
                            return;
                        }
                    }
                }

                String insertSql = "INSERT INTO TopAlbum (albumName, sold, date, idArtis) VALUES (?, ?, ?, ?)";
                try (PreparedStatement insertPstmt = conn.prepareStatement(insertSql)) {
                    insertPstmt.setString(1, name);
                    insertPstmt.setInt(2, sold);
                    insertPstmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                    insertPstmt.setInt(4, artistId);
                    insertPstmt.executeUpdate();
                }
                
                String albumSoldSql = "INSERT INTO AlbumSold (idArtis, date, albumSoldToday) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE albumSoldToday = albumSoldToday + VALUES(albumSoldToday)";
                try (PreparedStatement pstmt = conn.prepareStatement(albumSoldSql)) {
                     pstmt.setInt(1, artistId);
                     pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                     pstmt.setInt(3, sold);
                     pstmt.executeUpdate();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("Could not save new album.");
                alert.setContentText("An unexpected database error occurred.");
                alert.showAndWait();
            }

            albumNameField.clear();
            albumSoldField.clear();
            showSuccessMessage();
            loadAllDataFromDatabase();

        } catch (NumberFormatException ex) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Input Error");
            alert.setHeaderText("Invalid number format");
            alert.setContentText("Please enter a valid number for albums sold.");
            alert.showAndWait();
        }
    }

    private void handleAddAlbumSold() {
        try {
            if (addAlbumSoldAmountField.getText() == null || addAlbumSoldAmountField.getText().trim().isEmpty()) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Input Error");
                alert.setHeaderText("Album Sold field is empty");
                alert.setContentText("Please enter the number of albums sold to add.");
                alert.showAndWait();
                return;
            }
            int sold = Integer.parseInt(addAlbumSoldAmountField.getText());

            List<String> albumNames = getAllAlbumNamesForArtist(artistId);
            if (albumNames.isEmpty()) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("No Albums Found");
                alert.setHeaderText("There are no albums to add sales to.");
                alert.setContentText("Please create a new album first using the 'Add New Album' form.");
                alert.showAndWait();
                return;
            }

            ChoiceDialog<String> dialog = new ChoiceDialog<>(albumNames.get(0), albumNames);
            dialog.setTitle("Select Album");
            dialog.setHeaderText("Select an album to add sales to.");
            dialog.setContentText("Choose album:");
            
            Optional<String> result = dialog.showAndWait();
            String nameToUpdate;
            if (result.isPresent()){
                nameToUpdate = result.get();
            } else {
                return; // User cancelled
            }

            try (Connection conn = DatabaseManager.getConnection()) {
                String updateSql = "UPDATE TopAlbum SET sold = sold + ?, date = ? WHERE albumName = ? AND idArtis = ?";
                try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                    updatePstmt.setInt(1, sold);
                    updatePstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                    updatePstmt.setString(3, nameToUpdate);
                    updatePstmt.setInt(4, artistId);
                    updatePstmt.executeUpdate();
                }

                String albumSoldSql = "INSERT INTO AlbumSold (idArtis, date, albumSoldToday) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE albumSoldToday = albumSoldToday + VALUES(albumSoldToday)";
                try (PreparedStatement pstmt = conn.prepareStatement(albumSoldSql)) {
                     pstmt.setInt(1, artistId);
                     pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                     pstmt.setInt(3, sold);
                     pstmt.executeUpdate();
                }

            } catch (SQLException e) {
                e.printStackTrace();
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("Could not update album sales.");
                alert.setContentText("An unexpected database error occurred.");
                alert.showAndWait();
            }

            addAlbumSoldAmountField.clear();
            showSuccessMessage();
            loadAllDataFromDatabase();

        } catch (NumberFormatException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Input Error");
            alert.setHeaderText("Invalid number format");
            alert.setContentText("Please enter a valid number for albums sold.");
            alert.showAndWait();
        }
    }

    private void handleAddSocial() {
        try {
            int followers = Integer.parseInt(followersField.getText());
            String platform = socialMediaComboBox.getValue();
            if (platform == null) return;

            String sql = "INSERT INTO Popularity (idArtis, date, socialMedia, todayFollowers) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE todayFollowers = todayFollowers + VALUES(todayFollowers)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, artistId);
                pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setString(3, platform);
                pstmt.setInt(4, followers);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            followersField.clear();
            socialMediaComboBox.getSelectionModel().clearSelection();
            showSuccessMessage();
            loadAllDataFromDatabase(); // Refresh UI
        } catch (NumberFormatException ex) {
            System.err.println("Invalid number format for followers.");
        }
    }

    private void handleAddVisitors() {
        try {
            int newVisitors = Integer.parseInt(visitorsField.getText());

            String sql = "INSERT INTO Visitors (idArtis, date, visitorsToday) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE visitorsToday = visitorsToday + VALUES(visitorsToday)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, artistId);
                pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setInt(3, newVisitors);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            visitorsField.clear();
            showSuccessMessage();
            loadAllDataFromDatabase(); // Refresh UI
        } catch (NumberFormatException ex) {
            System.err.println("Invalid number format for visitors.");
        }
    }

    private void handleAddSales() {
        try {
            double salesToday = Double.parseDouble(salesTodayField.getText());

            String sql = "INSERT INTO Sales (idArtis, date, salesToday) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE salesToday = salesToday + VALUES(salesToday)";
            try (Connection conn = DatabaseManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, artistId);
                pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
                pstmt.setDouble(3, salesToday);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            salesTodayField.clear();
            showSuccessMessage();
            loadAllDataFromDatabase(); // Refresh UI
        } catch (NumberFormatException ex) {
            System.err.println("Invalid number format for sales.");
        }
    }

    private void styleBarChartNodes() {
        // Must be called after data is added
        for (int i = 0; i < topAlbumSeries.getData().size(); i++) {
            XYChart.Data<String, Number> data = topAlbumSeries.getData().get(i);
            if (data.getNode() != null) {
                 data.getNode().setStyle("-fx-bar-fill: " + barColors.get(i % barColors.size()) + ";");
            } else {
                // If the node is not yet created, we can't style it directly.
                // We rely on the fact that this is called after new data is added and rendered.
            }
        }
    }

    private void showSuccessMessage() {
        successMessageLabel.setVisible(true);
        successMessageLabel.setManaged(true);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), successMessageLabel);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), successMessageLabel);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> {
            successMessageLabel.setVisible(false);
            successMessageLabel.setManaged(false);
        });

        fadeIn.setOnFinished(event -> pause.play());
        pause.setOnFinished(event -> fadeOut.play());
        fadeIn.play();
    }

    @FXML
    private void handleReportButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.setInitialFileName(artistFullName.replaceAll("\\s+", "_") + "_Performance_Report.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(reportButton.getScene().getWindow());

        if (file != null) {
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                    contentStream.newLineAtOffset(50, 750);
                    contentStream.showText("Performance Report for " + artistFullName);
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(50, 700);

                    // Fetch and write Top Album data
                    contentStream.showText("Top Albums:");
                    contentStream.newLineAtOffset(0, -15);
                    String topAlbumSql = "SELECT albumName, sold FROM TopAlbum WHERE idArtis = ?";
                    try (Connection conn = DatabaseManager.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(topAlbumSql)) {
                        pstmt.setInt(1, artistId);
                        ResultSet rs = pstmt.executeQuery();
                        while (rs.next()) {
                            String line = "  - " + rs.getString("albumName") + " (" + rs.getInt("sold") + " sold)";
                            contentStream.showText(line);
                            contentStream.newLineAtOffset(0, -15);
                        }
                    }
                    contentStream.newLineAtOffset(0, -15);

                    // Fetch and write Popularity data
                    contentStream.showText("Social Media Popularity:");
                    contentStream.newLineAtOffset(0, -15);
                    String popularitySql = "SELECT socialMedia, todayFollowers, date FROM Popularity WHERE idArtis = ?";
                    try (Connection conn = DatabaseManager.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(popularitySql)) {
                        pstmt.setInt(1, artistId);
                        ResultSet rs = pstmt.executeQuery();
                        while (rs.next()) {
                            String line = "  - " + rs.getString("socialMedia") + ": " + rs.getInt("todayFollowers") + " followers on " + rs.getDate("date").toLocalDate();
                            contentStream.showText(line);
                            contentStream.newLineAtOffset(0, -15);
                        }
                    }
                    contentStream.newLineAtOffset(0, -15);

                    // Fetch and write Sales data
                    contentStream.showText("Sales:");
                    contentStream.newLineAtOffset(0, -15);
                    String salesSql = "SELECT salesToday, date FROM Sales WHERE idArtis = ?";
                    try (Connection conn = DatabaseManager.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(salesSql)) {
                        pstmt.setInt(1, artistId);
                        ResultSet rs = pstmt.executeQuery();
                        while (rs.next()) {
                            String line = "  - " + rs.getDouble("salesToday") + " on " + rs.getDate("date").toLocalDate();
                            contentStream.showText(line);
                            contentStream.newLineAtOffset(0, -15);
                        }
                    }
                    contentStream.newLineAtOffset(0, -15);

                    // Fetch and write Album Sold data
                    contentStream.showText("Albums Sold:");
                    contentStream.newLineAtOffset(0, -15);
                    String albumSoldSql = "SELECT albumSoldToday, date FROM AlbumSold WHERE idArtis = ?";
                    try (Connection conn = DatabaseManager.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(albumSoldSql)) {
                        pstmt.setInt(1, artistId);
                        ResultSet rs = pstmt.executeQuery();
                        while (rs.next()) {
                            String line = "  - " + rs.getInt("albumSoldToday") + " on " + rs.getDate("date").toLocalDate();
                            contentStream.showText(line);
                            contentStream.newLineAtOffset(0, -15);
                        }
                    }
                    contentStream.newLineAtOffset(0, -15);

                    // Fetch and write Visitors data
                    contentStream.showText("Visitors:");
                    contentStream.newLineAtOffset(0, -15);
                    String visitorsSql = "SELECT visitorsToday, date FROM Visitors WHERE idArtis = ?";
                    try (Connection conn = DatabaseManager.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(visitorsSql)) {
                        pstmt.setInt(1, artistId);
                        ResultSet rs = pstmt.executeQuery();
                        while (rs.next()) {
                            String line = "  - " + rs.getInt("visitorsToday") + " on " + rs.getDate("date").toLocalDate();
                            contentStream.showText(line);
                            contentStream.newLineAtOffset(0, -15);
                        }
                    }
                    contentStream.newLineAtOffset(0, -15);

                    // Fetch and write Fans Response data
                    contentStream.showText("Fans Response:");
                    contentStream.newLineAtOffset(0, -15);
                    String fansResponseSql = "SELECT source, comment, category, timestamp FROM FansResponse WHERE idArtis = ?";
                    try (Connection conn = DatabaseManager.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(fansResponseSql)) {
                        pstmt.setInt(1, artistId);
                        ResultSet rs = pstmt.executeQuery();
                        while (rs.next()) {
                            String line = "  - " + rs.getString("source") + " (" + rs.getString("category") + "): " + rs.getString("comment") + " on " + rs.getTimestamp("timestamp").toLocalDateTime().toLocalDate();
                            contentStream.showText(line);
                            contentStream.newLineAtOffset(0, -15);
                        }
                    }

                    contentStream.endText();
                }

                document.save(file);
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
}


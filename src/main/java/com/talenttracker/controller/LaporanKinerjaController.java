package com.talenttracker.controller;

import com.talenttracker.Main;
import com.talenttracker.dao.AlbumDAO;
import com.talenttracker.dao.SocialMediaDAO;
import com.talenttracker.dao.StatsDAO;
import com.talenttracker.model.Album;
import com.talenttracker.model.Popularity;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class LaporanKinerjaController {

    @FXML private Label successMessageLabel;
    @FXML private ImageView artistAvatarView;
    @FXML private Label artistNameLabel;
    @FXML private Button reportButton;
    @FXML private ImageView downloadIcon;
    @FXML private BarChart<String, Number> topAlbumChart;
    @FXML private LineChart<String, Number> socialMediaChart;
    @FXML private TextField albumNameField;
    @FXML private TextField albumSoldField;
    @FXML private Button addAlbumButton;
    @FXML private Button addAlbumSoldButton;
    @FXML private TextField addAlbumSoldAmountField;
    @FXML private ComboBox<String> socialMediaComboBox;
    @FXML private TextField followersField;
    @FXML private Button addSocialButton;
    @FXML private Label visitorsCountLabel;
    @FXML private TextField visitorsField;
    @FXML private Button addVisitorsButton;
    @FXML private ImageView visitorsIconView;
    @FXML private Label salesAmountLabel;
    @FXML private TextField salesTodayField;
    @FXML private Button addSalesButton;
    @FXML private ImageView salesIconView;
    @FXML private Label albumsSoldCountLabel;
    @FXML private ImageView albumsSoldIconView;
    @FXML private Label socialPopularityChangeLabel;
    @FXML private Label socialTotalFollowersLabel;
    @FXML private Label visitorsChangeLabel;
    @FXML private Label salesChangeLabel;
    @FXML private Label albumsSoldChangeLabel;

    private final AlbumDAO albumDAO = new AlbumDAO();
    private final SocialMediaDAO socialMediaDAO = new SocialMediaDAO();
    private final StatsDAO statsDAO = new StatsDAO();
    private final Random random = new Random();
    private final List<String> barColors = Arrays.asList(
            "#FEB95A", "#E89B2F", "#223055", "#FFB7CA", "#28AEF3",
            "#D7598B", "#A9DFD8", "#538EA5"
    );

    private int artistId;
    private String artistFullName;

    public void setArtist(int artistId, String artistName) {
        this.artistId = artistId;
        this.artistFullName = artistName;
        this.artistNameLabel.setText(artistName);
        
        try {
            String profileImageName = artistName.replaceAll("\\s+", "") + "Profile.png";
            Image profileImage = new Image("file:img/" + profileImageName);
                artistAvatarView.setImage(profileImage);
            if (profileImage.isError()) {
                artistAvatarView.setImage(new Image("file:img/DefaultArtist.png"));
            }
            downloadIcon.setImage(new Image("file:img/DownloadIcon.png"));
        } catch (Exception e) {
            System.err.println("Error loading image for " + artistName + ", using default.");
            artistAvatarView.setImage(new Image("file:img/DefaultArtist.png"));
        }

        loadAllDataFromDatabase();
    }
    
    @FXML
    public void initialize() {
        socialMediaChart.setAnimated(true);
        reportButton.setOnAction(event -> handleReportButton());
        addAlbumButton.setOnAction(e -> handleAddAlbum());
        addAlbumSoldButton.setOnAction(e -> handleAddAlbumSold());
        addSocialButton.setOnAction(e -> handleAddSocial());
        addVisitorsButton.setOnAction(e -> handleAddVisitors());
        addSalesButton.setOnAction(e -> handleAddSales());
        
        setupComboBox();
        initializeImageViews();

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
            System.err.println("Error loading icon images: " + e.getMessage());
        }
    }

    private void setupComboBox() {
        socialMediaComboBox.setItems(FXCollections.observableArrayList("Instagram", "X", "TikTok"));
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

        try {
            double todayVisitors = statsDAO.getVisitorsForDate(today, this.artistId);
        visitorsCountLabel.setText(String.format("%,.0f", todayVisitors));
            double todaySales = statsDAO.getSalesForDate(today, this.artistId);
        salesAmountLabel.setText(currencyFormat.format(todaySales));
            double todayAlbums = statsDAO.getAlbumsSoldForDate(today, this.artistId);
        albumsSoldCountLabel.setText(String.format("%,.0f", todayAlbums));

            double yesterdayVisitors = statsDAO.getVisitorsForDate(yesterday, this.artistId);
        updateChangeLabel(visitorsChangeLabel, todayVisitors, yesterdayVisitors, false);
            double yesterdaySales = statsDAO.getSalesForDate(yesterday, this.artistId);
        updateChangeLabel(salesChangeLabel, todaySales, yesterdaySales, true);
            double yesterdayAlbums = statsDAO.getAlbumsSoldForDate(yesterday, this.artistId);
        updateChangeLabel(albumsSoldChangeLabel, todayAlbums, yesterdayAlbums, false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadTopAlbumChart() {
        topAlbumChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        try {
            List<Album> albums = albumDAO.getArtistAlbums(artistId, false);
            for (Album album : albums) {
                series.getData().add(new XYChart.Data<>(album.getName(), album.getSales()));
            }
            topAlbumChart.getData().add(series);
            topAlbumChart.applyCss();
            styleBarChart(series);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void styleBarChart(XYChart.Series<String, Number> series) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                String color = barColors.get(random.nextInt(barColors.size()));
                data.getNode().setStyle("-fx-bar-fill: " + color + ";");
            }
        }
    }

    private void loadSocialMediaChart() {
        socialMediaChart.getData().clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        try {
            List<Popularity> popularityData = socialMediaDAO.getSocialMediaData(artistId);
            Map<String, XYChart.Series<String, Number>> seriesMap = new HashMap<>();

            for (Popularity p : popularityData) {
                seriesMap.computeIfAbsent(p.getPlatform(), k -> {
                    XYChart.Series<String, Number> newSeries = new XYChart.Series<>();
                    newSeries.setName(k);
                    socialMediaChart.getData().add(newSeries);
                    return newSeries;
                }).getData().add(new XYChart.Data<>(p.getDate().format(formatter), p.getFollowers()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadSocialPopularityStats() {
        try {
            Map<String, Double> followerStats = socialMediaDAO.getMostRecentFollowerStats(artistId);
            double currentFollowers = followerStats.get("current");
            double previousFollowers = followerStats.get("previous");

            socialTotalFollowersLabel.setText(String.format("%,.0f", currentFollowers));
            updateChangeLabel(socialPopularityChangeLabel, currentFollowers, previousFollowers, false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void updateChangeLabel(Label label, double current, double previous, boolean isCurrency) {
        if (previous == 0) {
            label.setText(current > 0 ? "+100%" : "N/A");
            return;
        }
        double change = ((current - previous) / previous) * 100;
        label.setText(String.format("%s%.0f%%", change >= 0 ? "+" : "", change));
    }

    private void handleAddAlbum() {
        String albumName = albumNameField.getText();
        String soldText = albumSoldField.getText();
        if (albumName.isEmpty() || soldText.isEmpty()) {
            showAlert("Error", "Album name and initial sold count cannot be empty.");
            return;
        }

        try {
            int sold = Integer.parseInt(soldText);
            albumDAO.addAlbum(albumName, sold, artistId);
            statsDAO.addAlbumsSold(sold, artistId);
            
            loadAllDataFromDatabase();

            albumNameField.clear();
            albumSoldField.clear();

        } catch (NumberFormatException e) {
            showAlert("Error", "Initial sold must be a number.");
        } catch (SQLException e) {
            showAlert("Database Error", "Could not add album: " + e.getMessage());
        }
    }

    private void handleAddAlbumSold() {
        String amountText = addAlbumSoldAmountField.getText();
        if (amountText.isEmpty()) {
            showAlert("Error", "Amount cannot be empty.");
            return;
        }

        try {
            List<String> albumNames = albumDAO.getAllAlbumNamesForArtist(artistId);
            if (albumNames.isEmpty()) {
                showAlert("No Albums", "This artist has no albums to update.");
                return;
            }

            ChoiceDialog<String> dialog = new ChoiceDialog<>(albumNames.get(0), albumNames);
            dialog.setTitle("Add Album Sales");
            dialog.setHeaderText("Select an album to update its sales.");
            dialog.setContentText("Album:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(selectedAlbum -> {
                try {
                    int amount = Integer.parseInt(amountText);
                    albumDAO.addAlbumSold(selectedAlbum, amount, artistId);
                    statsDAO.addAlbumsSold(amount, artistId);
            showSuccessMessage();
                    loadAllDataFromDatabase();
                    addAlbumSoldAmountField.clear();
                } catch (NumberFormatException e) {
                    showAlert("Error", "Amount must be a number.");
                } catch (SQLException e) {
                    showAlert("Database Error", "Could not update album sales: " + e.getMessage());
                }
            });
        } catch (SQLException e) {
            showAlert("Database Error", "Could not retrieve album list: " + e.getMessage());
        }
    }

    private void handleAddSocial() {
        String platform = socialMediaComboBox.getValue();
        String followersText = followersField.getText();
        if (platform == null || followersText.isEmpty()) {
            showAlert("Error", "Social media platform and followers count cannot be empty.");
            return;
        }
        try {
            int followers = Integer.parseInt(followersText);
            socialMediaDAO.addSocialMediaData(platform, followers, artistId);
            showSuccessMessage();
            loadSocialMediaChart();
            loadSocialPopularityStats();
            followersField.clear();
        } catch (NumberFormatException e) {
            showAlert("Error", "Followers must be a number.");
        } catch (SQLException e) {
            showAlert("Database Error", "Could not add social media data: " + e.getMessage());
        }
    }

    private void handleAddVisitors() {
        String visitorsText = visitorsField.getText();
        if (visitorsText.isEmpty()) {
            showAlert("Error", "Visitors count cannot be empty.");
            return;
        }
        try {
            int count = Integer.parseInt(visitorsText);
            statsDAO.addVisitors(count, artistId);
            showSuccessMessage();
            loadStats();
            visitorsField.clear();
        } catch (NumberFormatException e) {
            showAlert("Error", "Visitors must be a number.");
        } catch (SQLException e) {
            showAlert("Database Error", "Could not add visitors: " + e.getMessage());
        }
    }

    private void handleAddSales() {
        String salesText = salesTodayField.getText();
        if (salesText.isEmpty()) {
            showAlert("Error", "Sales amount cannot be empty.");
            return;
        }
        try {
            int amount = Integer.parseInt(salesText);
            statsDAO.addSales(amount, artistId);
            showSuccessMessage();
            loadStats();
            salesTodayField.clear();
        } catch (NumberFormatException e) {
            showAlert("Error", "Sales must be a number.");
        } catch (SQLException e) {
            showAlert("Database Error", "Could not add sales: " + e.getMessage());
        }
    }

    public void showSuccessMessage() {
        successMessageLabel.setText("Data added successfully!");
        successMessageLabel.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(200), successMessageLabel);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
        PauseTransition pt = new PauseTransition(Duration.seconds(2));
        pt.setOnFinished(e -> successMessageLabel.setVisible(false));
        pt.play();
    }

    @FXML
    private void handleReportButton() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("PDF", "PDF", "CSV");
        dialog.setTitle("Export Report");
        dialog.setHeaderText("Choose the format for your report.");
        dialog.setContentText("Format:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(format -> {
            if ("PDF".equals(format)) {
                exportAsPdf();
            } else if ("CSV".equals(format)) {
                exportAsCsv();
            }
        });
    }

    private void exportAsPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
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

                    addPdfContent(contentStream);
                }
                document.save(file);
                showAlert("Success", "PDF report saved successfully.");
            } catch (IOException | SQLException e) {
                showAlert("Error", "Failed to save PDF report: " + e.getMessage());
            }
        }
    }

    private void addPdfContent(PDPageContentStream contentStream) throws IOException, SQLException {
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        float y = 700;

        contentStream.beginText();
        contentStream.newLineAtOffset(50, y);
        contentStream.showText("Total Visitors: " + visitorsCountLabel.getText());
        contentStream.newLineAtOffset(0, -20);
        contentStream.showText("Total Sales: " + salesAmountLabel.getText());
        contentStream.newLineAtOffset(0, -20);
        contentStream.showText("Albums Sold: " + albumsSoldCountLabel.getText());
        contentStream.newLineAtOffset(0, -20);
        contentStream.showText("Total Followers: " + socialTotalFollowersLabel.getText());
        contentStream.endText();
        y -= 100;

        contentStream.beginText();
        contentStream.newLineAtOffset(50, y);
        contentStream.showText("Top Albums:");
        contentStream.endText();
        y -= 20;
        List<Album> albums = albumDAO.getArtistAlbums(artistId, false);
        for (Album album : albums) {
            contentStream.beginText();
            contentStream.newLineAtOffset(60, y);
            contentStream.showText("- " + album.getName() + ": " + album.getSales() + " sold");
            contentStream.endText();
            y -= 20;
        }
    }

    private void exportAsCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV Report");
        fileChooser.setInitialFileName(artistFullName.replaceAll("\\s+", "_") + "_Performance_Report.csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(reportButton.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.append("Metric,Value\n");
                writer.append("Total Visitors,").append(visitorsCountLabel.getText()).append("\n");
                writer.append("Total Sales,").append(salesAmountLabel.getText()).append("\n");
                writer.append("Albums Sold,").append(albumsSoldCountLabel.getText()).append("\n");
                writer.append("Total Followers,").append(socialTotalFollowersLabel.getText()).append("\n\n");

                writer.append("Album,Sales\n");
                List<Album> albums = albumDAO.getArtistAlbums(artistId, false);
                for (Album album : albums) {
                    writer.append(album.getName()).append(",").append(String.valueOf(album.getSales())).append("\n");
                    }
                writer.append("\n");

                writer.append("Platform,Date,Followers\n");
                List<Popularity> socialData = socialMediaDAO.getSocialMediaData(artistId);
                for (Popularity p : socialData) {
                    writer.append(p.getPlatform()).append(",").append(p.getDate().toString()).append(",").append(String.valueOf(p.getFollowers())).append("\n");
                }

                showAlert("Success", "CSV report saved successfully.");
            } catch (IOException | SQLException e) {
                showAlert("Error", "Failed to save CSV report: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}


package com.talenttracker.controller;

import com.talenttracker.dao.UserDAO;
import com.talenttracker.model.User;
import com.talenttracker.util.DatabaseUtil;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;
import org.testfx.util.WaitForAsyncUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class LaporanKinerjaControllerTest {

    private User testArtist;
    private LaporanKinerjaController controller;

    @Start
    private void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LaporanKinerjaView.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    void setUp() throws SQLException {
        UserDAO userDAO = new UserDAO();
        // Clean up previous test data
        try (Connection conn = DatabaseUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM TopAlbum WHERE idArtis IN (SELECT idUser FROM `user` WHERE email = 'test.artist.laporan@talent.com')");
            stmt.executeUpdate("DELETE FROM Visitors WHERE idArtis IN (SELECT idUser FROM `user` WHERE email = 'test.artist.laporan@talent.com')");
            stmt.executeUpdate("DELETE FROM Sales WHERE idArtis IN (SELECT idUser FROM `user` WHERE email = 'test.artist.laporan@talent.com')");
            stmt.executeUpdate("DELETE FROM AlbumSold WHERE idArtis IN (SELECT idUser FROM `user` WHERE email = 'test.artist.laporan@talent.com')");
            stmt.executeUpdate("DELETE FROM Popularity WHERE idArtis IN (SELECT idUser FROM `user` WHERE email = 'test.artist.laporan@talent.com')");
            stmt.executeUpdate("DELETE FROM `user` WHERE email = 'test.artist.laporan@talent.com'");
        }

        // Create a test user
        userDAO.addUser("Test Artist Laporan", "test.artist.laporan@talent.com", "password", "Artist");
        testArtist = userDAO.getUserByEmail("test.artist.laporan@talent.com");
        assertNotNull(testArtist, "Test artist could not be created.");

        // Add test data
        try (Connection conn = DatabaseUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO Visitors (idArtis, `date`, visitorsToday) VALUES (" + testArtist.getId() + ", '" + LocalDate.now() + "', 2000)");
            stmt.executeUpdate("INSERT INTO Sales (idArtis, `date`, salesToday) VALUES (" + testArtist.getId() + ", '" + LocalDate.now() + "', 5000000)");
            stmt.executeUpdate("INSERT INTO AlbumSold (idArtis, `date`, albumSoldToday) VALUES (" + testArtist.getId() + ", '" + LocalDate.now() + "', 100)");
            stmt.executeUpdate("INSERT INTO Visitors (idArtis, `date`, visitorsToday) VALUES (" + testArtist.getId() + ", '" + LocalDate.now().minusDays(1) + "', 1000)");
            stmt.executeUpdate("INSERT INTO Sales (idArtis, `date`, salesToday) VALUES (" + testArtist.getId() + ", '" + LocalDate.now().minusDays(1) + "', 2500000)");
            stmt.executeUpdate("INSERT INTO AlbumSold (idArtis, `date`, albumSoldToday) VALUES (" + testArtist.getId() + ", '" + LocalDate.now().minusDays(1) + "', 50)");
            stmt.executeUpdate("INSERT INTO TopAlbum (idArtis, `albumName`, sold) VALUES (" + testArtist.getId() + ", 'Album A', 1500)");
            stmt.executeUpdate("INSERT INTO TopAlbum (idArtis, `albumName`, sold) VALUES (" + testArtist.getId() + ", 'Album B', 2500)");
            stmt.executeUpdate("INSERT INTO Popularity (idArtis, socialMedia, `date`, todayFollowers) VALUES (" + testArtist.getId() + ", 'Instagram', '" + LocalDate.now().minusDays(1) + "', 9000)");
            stmt.executeUpdate("INSERT INTO Popularity (idArtis, socialMedia, `date`, todayFollowers) VALUES (" + testArtist.getId() + ", 'Instagram', '" + LocalDate.now() + "', 10000)");
            stmt.executeUpdate("INSERT INTO Popularity (idArtis, socialMedia, `date`, todayFollowers) VALUES (" + testArtist.getId() + ", 'TikTok', '" + LocalDate.now() + "', 5000)");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Final cleanup
        try (Connection conn = DatabaseUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM TopAlbum WHERE idArtis = " + testArtist.getId());
            stmt.executeUpdate("DELETE FROM Visitors WHERE idArtis = " + testArtist.getId());
            stmt.executeUpdate("DELETE FROM Sales WHERE idArtis = " + testArtist.getId());
            stmt.executeUpdate("DELETE FROM AlbumSold WHERE idArtis = " + testArtist.getId());
            stmt.executeUpdate("DELETE FROM Popularity WHERE idArtis = " + testArtist.getId());
            stmt.executeUpdate("DELETE FROM `user` WHERE idUser = " + testArtist.getId());
        }
    }

    private void loadDataForTest(FxRobot robot) {
        // Use robot.interact to ensure the action is on the FX thread and the test waits for it
        robot.interact(() -> controller.setArtist(testArtist.getId(), testArtist.getFullName()));
        
        // After telling the controller to load data, wait until a key piece of data appears
        try {
            WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> {
                Label label = robot.lookup("#visitorsCountLabel").queryAs(Label.class);
                return !label.getText().isEmpty() && !"0".equals(label.getText()); // Wait for non-empty/non-zero
            });
        } catch (TimeoutException e) {
            fail("Timed out waiting for initial data to load.", e);
        }
    }

    @Test
    void testInitialDataIsDisplayedCorrectly(FxRobot robot) throws TimeoutException {
        loadDataForTest(robot);

        assertEquals("Test Artist Laporan", robot.lookup("#artistNameLabel").queryAs(Label.class).getText());
        assertEquals("2.000", robot.lookup("#visitorsCountLabel").queryAs(Label.class).getText());
        assertTrue(robot.lookup("#salesAmountLabel").queryAs(Label.class).getText().replace("\u00A0", " ").matches("Rp.*5.000.000,00"));
        assertEquals("100", robot.lookup("#albumsSoldCountLabel").queryAs(Label.class).getText());
    }

    @Test
    void testChartsArePopulated(FxRobot robot) throws TimeoutException {
        loadDataForTest(robot);
        
        BarChart<?, ?> topAlbumChart = robot.lookup("#topAlbumChart").queryAs(BarChart.class);
        assertEquals(1, topAlbumChart.getData().size(), "Top album chart should have one series.");
        assertEquals(2, topAlbumChart.getData().get(0).getData().size(), "Album series should have two data points.");

        LineChart<?, ?> socialMediaChart = robot.lookup("#socialMediaChart").queryAs(LineChart.class);
        assertEquals(2, socialMediaChart.getData().size(), "Social media chart should have two series (Instagram and TikTok).");
    }

    @Test
    void testAddNewAlbum(FxRobot robot) throws TimeoutException {
        loadDataForTest(robot);

        // ACT: Simulate user input and button click
        robot.interact(() -> {
            robot.lookup("#albumNameField").queryAs(TextField.class).setText("New Test Album");
            robot.lookup("#albumSoldField").queryAs(TextField.class).setText("500");
            robot.lookup("#addAlbumButton").queryButton().fire();
        });

        // WAIT & ASSERT: Wait for the result of the action.
        // The most reliable result is the chart's data updating.
        BarChart<?, ?> topAlbumChart = robot.lookup("#topAlbumChart").queryAs(BarChart.class);
        WaitForAsyncUtils.waitFor(5, TimeUnit.SECONDS, () -> {
            if (!topAlbumChart.getData().isEmpty()) {
                XYChart.Series<?, ?> series = topAlbumChart.getData().get(0);
                // The condition to wait for is the data size becoming 3.
                return series.getData().size() == 3;
            }
            return false;
        });
        
        // Final assertion to confirm the test passes for the right reason.
        // This runs only after the waitFor condition is met.
        XYChart.Series<?, ?> albumSeries = topAlbumChart.getData().get(0);
        assertEquals(3, albumSeries.getData().size(), "Album chart should be updated with the new album.");
    }
}

package pyroman.jigsawsockets.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import pyroman.jigsawsockets.game.Game;
import pyroman.jigsawsockets.view.Stopwatch;
import pyroman.jigsawsockets.view.Tile;
import pyroman.jigsawsockets.view.figure.FigureSpawner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class BoardViewController implements Initializable {

    public static final int BOARD_TILES_WIDTH = 9;
    public static final int BOARD_TILES_HEIGHT = 9;

    @FXML
    private StackPane root;

    @FXML
    private Button buttonNewGame;

    @FXML
    private Button buttonEndGame;

    @FXML
    private FigureSpawner figureSpawningField;

    @FXML
    private Label stopwatchLabel;

    @FXML
    private Label labelFigureCounter;

    private Stopwatch stopwatch;
    private MediaPlayer mediaPlayer;

    private final SimpleIntegerProperty figuresCountProperty = new SimpleIntegerProperty(0);
    private final SimpleStringProperty stopwatchProperty = new SimpleStringProperty("00:00:00");

    @FXML
    private GridPane gridPaneBoard;

    private Game game;

    @FXML
    private void onButtonExitClick() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        launchSplashScreen();
    }

    private void launchSplashScreen() {
        try {
            Stage stage = (Stage) root.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/splashscreen-view.fxml"));
            root.getChildren().setAll((Node) fxmlLoader.load());
            stage.setUserData("mainMenu");
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @FXML
    private void onButtonNewGameClick() {
        buttonNewGame.setDisable(true);
        buttonEndGame.setDisable(false);

        game = new Game();

        stopwatch = new Stopwatch(stopwatchProperty);

        stopwatch.start();
        playMainTheme();

        figuresCountProperty.set(0);

        figureSpawningField.spawnFigure(game.getFigureDataByIndex(0));
    }

    @FXML
    private void onButtonEndGameClick() {
        buttonEndGame.setDisable(true);
        buttonNewGame.setDisable(false);

        stopwatch.stop();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        refreshGridPane();
        figureSpawningField.getChildren().clear();
    }

    private void refreshGridPane() {
        gridPaneBoard.getChildren().clear();

        for (int row = 0; row < BOARD_TILES_WIDTH; ++row) {
            for (int column = 0; column < BOARD_TILES_HEIGHT; ++column) {
                gridPaneBoard.add(new Tile(row, column, figuresCountProperty), column, row);
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initGridPane();

        stopwatchLabel.setText("00:00:00");
        labelFigureCounter.setText("Поставлено фигур: 0");

        figuresCountProperty.addListener((observableValue, oldValue, newValue) -> {
            labelFigureCounter.setText("Поставлено фигур: " + newValue);
            figureSpawningField.spawnFigure(game.getFigureDataByIndex((Integer) newValue));
        });

        stopwatchProperty.addListener((observableValue, oldValue, newValue) ->
                stopwatchLabel.setText(newValue));
    }

    private void playMainTheme() {
        Media music = new Media((Objects.requireNonNull(getClass().getResource("/sounds/sobyanin.mp3")).toString()));
        mediaPlayer = new MediaPlayer(music);
        mediaPlayer.setVolume(0.25);
        mediaPlayer.play();
        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
    }

    private void initGridPane() {
        refreshGridPane();
    }
}

package pyroman.jigsawsockets.controller;

import javafx.application.Platform;
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
import pyroman.jigsawsockets.connection.client.Client;
import pyroman.jigsawsockets.connection.server.PlayerData;
import pyroman.jigsawsockets.utils.ParsingUtils;
import pyroman.jigsawsockets.view.MultiplayerInfoContainer;
import pyroman.jigsawsockets.view.Stopwatch;
import pyroman.jigsawsockets.view.Tile;
import pyroman.jigsawsockets.view.figure.FigureSpawner;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

public class MultiplayerBoardViewController implements Initializable {

    public static final int BOARD_TILES_WIDTH = 9;
    public static final int BOARD_TILES_HEIGHT = 9;

    @FXML
    private StackPane root;

    @FXML
    private Label labelStatus;

    @FXML
    private Label labelOpponentName;

    @FXML
    private Label labelPlayingTime;

    @FXML
    private Button buttonEndGame;

    @FXML
    private FigureSpawner figureSpawningField;

    @FXML
    private Label stopwatchLabel;

    @FXML
    private Label labelFigureCounter;

    private final SimpleIntegerProperty figuresCountProperty = new SimpleIntegerProperty(0);
    private final SimpleStringProperty stopwatchProperty = new SimpleStringProperty("00:00:00");

    @FXML
    private GridPane gridPaneMain;

    @FXML
    private GridPane gridPaneBoard;

    private Client client;
    private Stopwatch stopwatch;
    private MediaPlayer mediaPlayer;

    private String playerNickname;
    private PlayerData opponentData;

    private void assignClient() {
        MultiplayerInfoContainer multiplayerInfoContainer = (MultiplayerInfoContainer) gridPaneMain.getScene().getWindow().getUserData();

        client = multiplayerInfoContainer.client();

        labelPlayingTime.setText("Время на игру: " + multiplayerInfoContainer.playingTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        playerNickname = multiplayerInfoContainer.nickname();
        labelOpponentName.setText("Оппонент: " + multiplayerInfoContainer.partnerNickname());

        stopwatchProperty.addListener((observableValue, oldValue, newValue) -> {
            if (newValue.equals(multiplayerInfoContainer.playingTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")))) {
                stopGame();
            }
        });

        client.addNewServerCallbackHandler("FIGURE", callback -> Platform.runLater(() ->
                figureSpawningField.spawnFigure(callback)));

        client.addNewServerCallbackHandler("FINISHED", callback -> Platform.runLater(() -> {
            opponentData = ParsingUtils.getPlayerDataFromString(callback);
            labelStatus.setText("Оппонент завершил игру");
        }));

        client.addNewServerCallbackHandler("STOP", callback -> Platform.runLater(() -> {
            client.close();
            showWinner();
        }));

        client.addNewServerCallbackHandler("SERVER_CLOSED", callback -> {
            client.close();
            Platform.runLater(this::returnToMainMenu);
        });

    }

    private void showWinner() {
        if (opponentData == null) {
            labelStatus.setText("Оппонент потерял соединение.");
        } else {
            if (opponentData.numberOfPlacedFigures() > figuresCountProperty.getValue()) {
                labelStatus.setText("Оппонент " + opponentData.nickname() + " победил!");
            } else if (opponentData.numberOfPlacedFigures() <= figuresCountProperty.getValue()) {
                labelStatus.setText("Вы победили!");
            } else if (opponentData.playingTime().compareTo(LocalTime.parse(stopwatchProperty.get(),
                    DateTimeFormatter.ofPattern("HH:mm:ss"))) > 0) {
                labelStatus.setText("Оппонент " + opponentData.nickname() + " победил!");
            } else if (opponentData.playingTime().compareTo(LocalTime.parse(stopwatchProperty.get(),
                    DateTimeFormatter.ofPattern("HH:mm:ss"))) < 0) {
                labelStatus.setText("Вы победили!");
            } else {
                labelStatus.setText("Ничья!");
            }
        }
    }

    private void returnToMainMenu() {
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
    private void onButtonExitClick() {
        client.close();
        Stage stage = (Stage) gridPaneMain.getScene().getWindow();
        stage.close();
    }

    private void stopGame() {
        buttonEndGame.setDisable(true);
        stopwatch.stop();
        mediaPlayer.stop();
        client.sendRequestToServer("RESULT " + playerNickname + " " +
                figuresCountProperty.get() + " " + stopwatchProperty.get());

        refreshGridPane();
        figureSpawningField.getChildren().clear();
    }

    @FXML
    private void onButtonEndGameClick() {
        stopGame();
    }

    private void refreshGridPane() {
        gridPaneBoard.getChildren().clear();

        for (int row = 0; row < BOARD_TILES_WIDTH; ++row) {
            for (int column = 0; column < BOARD_TILES_HEIGHT; ++column) {
                gridPaneBoard.add(new Tile(row, column, figuresCountProperty), column, row);
            }
        }
    }

    private void playMainTheme() {
        Media music = new Media((Objects.requireNonNull(getClass().getResource("/sounds/sobyanin.mp3")).toString()));
        mediaPlayer = new MediaPlayer(music);
        mediaPlayer.setVolume(0.25);
        mediaPlayer.play();
        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initGridPane();

        stopwatchLabel.setText("00:00:00");
        labelFigureCounter.setText("Поставлено фигур: 0");

        figuresCountProperty.addListener((observableValue, oldValue, newValue) -> {
            labelFigureCounter.setText("Поставлено фигур: " + newValue);
            client.sendRequestToServer("FIGURE " + newValue);
        });

        stopwatchProperty.addListener((observableValue, oldValue, newValue) -> stopwatchLabel.setText(newValue));
    }

    public void onWindowShowing() {
        assignClient();

        client.sendRequestToServer("FIGURE 0");

        stopwatch = new Stopwatch(stopwatchProperty);

        stopwatch.start();
        playMainTheme();

        figuresCountProperty.set(0);
    }

    private void initGridPane() {
        refreshGridPane();
    }
}

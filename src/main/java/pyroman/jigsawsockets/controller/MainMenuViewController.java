package pyroman.jigsawsockets.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;

public class MainMenuViewController {

    @FXML
    private StackPane root;

    @FXML
    private GridPane gridPaneMainMenu;

    private MediaPlayer mediaPlayer;

    @FXML
    private void onButtonSoloGameClick() {
        mediaPlayer.stop();
        launchSplashScreenWithData("singlePlayerBoard");
    }

    @FXML
    private void onButtonLocalGameClick() {
        mediaPlayer.stop();
        launchSplashScreenWithData("multiPlayerLobby");
    }

    private void launchSplashScreenWithData(String data) {
        try {
            Stage stage = (Stage) gridPaneMainMenu.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/splashscreen-view.fxml"));
            root.getChildren().setAll((Node) fxmlLoader.load());
            stage.setUserData(data);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @FXML
    private void onButtonExitClick() {
        Stage stage = (Stage) gridPaneMainMenu.getScene().getWindow();
        stage.close();
    }

    private void playMainTheme() {
        Media music = new Media((Objects.requireNonNull(getClass().getResource("/sounds/main_menu_theme.mp3")).toString()));
        mediaPlayer = new MediaPlayer(music);
        mediaPlayer.setVolume(0.25);
        mediaPlayer.play();
        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
    }

    public void onWindowShowing() {
        playMainTheme();
    }
}

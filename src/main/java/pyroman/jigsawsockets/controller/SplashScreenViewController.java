package pyroman.jigsawsockets.controller;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;

import static pyroman.jigsawsockets.JigsawApplication.SCREEN_HEIGHT;

public class SplashScreenViewController implements Initializable {

    private static final Duration ANIMATION_SECONDS_DURATION = Duration.seconds(2);

    @FXML
    private StackPane root;

    @FXML
    private GridPane gridPaneMain;

    @FXML
    private ImageView imageViewTop;

    @FXML
    private ImageView imageViewBottom;

    private TranslateTransition translateTransitionTop;
    private TranslateTransition translateTransitionBottom;
    private FadeTransition fadeInTransition;

    private void initAnimations() {
        initTranslateTransitionTop();
        initTranslateTransitionBottom();
        initFadeInTransition();
    }

    private void initTranslateTransitionTop() {
        translateTransitionTop = new TranslateTransition();
        translateTransitionTop.setNode(imageViewTop);

        translateTransitionTop.setFromY(SCREEN_HEIGHT * (-0.3));
        translateTransitionTop.setToY(imageViewTop.getY());
        translateTransitionTop.setCycleCount(1);

        translateTransitionTop.setDuration(ANIMATION_SECONDS_DURATION);
    }

    private void initTranslateTransitionBottom() {
        translateTransitionBottom = new TranslateTransition();

        translateTransitionBottom.setNode(imageViewBottom);

        translateTransitionBottom.setFromY(SCREEN_HEIGHT * (0.3));
        translateTransitionBottom.setToY(imageViewBottom.getY());
        translateTransitionBottom.setCycleCount(1);

        translateTransitionBottom.setDuration(ANIMATION_SECONDS_DURATION);
    }

    private void initFadeInTransition() {
        fadeInTransition = new FadeTransition(ANIMATION_SECONDS_DURATION, gridPaneMain);

        fadeInTransition.setFromValue(0);
        fadeInTransition.setToValue(1);
        fadeInTransition.setCycleCount(1);

        fadeInTransition.setOnFinished(this::loadBoardView);
    }

    private void loadBoardView(ActionEvent event) {
        Stage stage = ((Stage) (gridPaneMain).getScene().getWindow());

        String nextSceneInfo = stage.getUserData().toString();

        switch (nextSceneInfo) {
            case "singlePlayerBoard" -> loadSinglePlayerBoard();
            case "multiPlayerLobby" -> loadMultiplayerLobby();
            case "lobbyClient" -> loadMultiplayerBoard();
            case "mainMenu" -> loadMainMenu();
        }

    }

    private void loadSplashScreen() {
        translateTransitionTop.play();
        translateTransitionBottom.play();
        fadeInTransition.play();
    }

    private void loadSinglePlayerBoard() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/singleplayer/board-view.fxml"));
            root.getChildren().setAll((Node) fxmlLoader.load());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private void loadMultiplayerBoard() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/multiplayer/multiplayer-board-view.fxml"));
            root.getChildren().setAll((Node) fxmlLoader.load());

            MultiplayerBoardViewController multiplayerBoardViewController = fxmlLoader.getController();
            multiplayerBoardViewController.onWindowShowing();

        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private void loadMultiplayerLobby() {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/multiplayer/lobby-view.fxml"));
            Node node = fxmlLoader.load();
            root.getChildren().setAll(node);

        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private void loadMainMenu() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/mainmenu-view.fxml"));
            root.getChildren().setAll((Node) fxmlLoader.load());
            MainMenuViewController mainMenuViewController = fxmlLoader.getController();
            mainMenuViewController.onWindowShowing();

        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initAnimations();
        loadSplashScreen();
    }
}

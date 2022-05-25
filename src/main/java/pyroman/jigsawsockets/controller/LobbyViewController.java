package pyroman.jigsawsockets.controller;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import javafx.util.Duration;
import pyroman.jigsawsockets.connection.server.GameServer;
import pyroman.jigsawsockets.connection.client.Client;
import pyroman.jigsawsockets.utils.ParsingUtils;
import pyroman.jigsawsockets.view.MultiplayerInfoContainer;
import pyroman.jigsawsockets.view.PlayerLobbyCard;
import pyroman.jigsawsockets.view.Radio;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;

import static pyroman.jigsawsockets.JigsawApplication.SCREEN_HEIGHT;

public class LobbyViewController implements Initializable {

    private static final int MAX_NUMBER_OF_PLAYERS = 2;

    @FXML
    private GridPane gridPaneBackgroundCover;

    @FXML
    private StackPane stackPaneConnectionInfoNest;

    @FXML
    private Radio radio;

    @FXML
    private Button buttonDisconnect;

    @FXML
    private Label labelErrorMessage;

    @FXML
    private GridPane gridPaneLobbyPanel;

    @FXML
    private StackPane root;

    @FXML
    private GridPane gridPaneMain;

    @FXML
    private GridPane gridPanePlayerCards;

    private ServerInfoViewController serverInfoViewController;
    private ClientInfoViewController clientInfoViewController;

    private GameServer server;
    private Client client;

    private TranslateTransition translateTransitionOpen;
    private TranslateTransition hideLobbyPanel;
    private FadeTransition fadeOutTransition;
    private static final Duration ANIMATION_SECONDS_DURATION = Duration.seconds(2);

    private LocalTime playingTime;
    private String nickname;
    private String partnerName;

    private void initClientInfoView() {
        StackPane stackPaneClientInfo;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/multiplayer/client-info-view.fxml"));
            stackPaneClientInfo = fxmlLoader.load();
            stackPaneClientInfo.setVisible(false);
            clientInfoViewController = fxmlLoader.getController();
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }

        clientInfoViewController.textFieldsValidated.addListener((observableValue, oldValue, validated) -> {
            if (validated) {
                try {
                    joinServer(false);
                    playOpenAnimation();
                    radio.turnOn();
                } catch (ConnectException exception) {
                    showErrorMessageWithData("Ошибка при соединении");
                } catch (IOException exception) {
                    showErrorMessageWithData("Неизвестная ошибка, повторите попытку позже.");
                }
            }
        });

        stackPaneConnectionInfoNest.getChildren().add(stackPaneClientInfo);

    }

    private void initServerInfoView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/multiplayer/server-info-view.fxml"));
            StackPane stackPaneServerInfo = fxmlLoader.load();
            stackPaneServerInfo.setVisible(false);
            serverInfoViewController = fxmlLoader.getController();

            serverInfoViewController.textFieldsValidated.addListener((observableValue, oldValue, validated) -> {
                if (validated) {
                    try {
                        launchServer();
                        joinServer(true);
                        playOpenAnimation();
                        radio.turnOn();
                    } catch (IOException exception) {
                        showErrorMessageWithData("Ошибка при запуске сервера");
                    }
                }
            });

            stackPaneConnectionInfoNest.getChildren().add(stackPaneServerInfo);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private void initGridPanePlayerCards() {
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setPercentHeight(100);
        gridPanePlayerCards.getRowConstraints().add(rowConstraints);

        for (int index = 0; index < MAX_NUMBER_OF_PLAYERS; ++index) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth((double) 100 / MAX_NUMBER_OF_PLAYERS);
            gridPanePlayerCards.getColumnConstraints().add(columnConstraints);
        }
    }

    public void fillPlayersInfo(List<String> playerNicknames) {
        gridPanePlayerCards.getChildren().clear();

        for (int index = 0; index < playerNicknames.size(); ++index) {
            GridPane gridPanePlayerCard = new PlayerLobbyCard(playerNicknames.get(index));
            gridPanePlayerCards.add(gridPanePlayerCard, index, 0);
        }

        if (playerNicknames.size() < MAX_NUMBER_OF_PLAYERS) {
            for (int index = playerNicknames.size(); index < MAX_NUMBER_OF_PLAYERS; ++index) {
                GridPane gridPaneUnknownCard = new PlayerLobbyCard("");
                gridPanePlayerCards.add(gridPaneUnknownCard, index, 0);
            }
        }
    }

    @FXML
    private void onButtonCreateLobbyClick() {
        serverInfoViewController.showView();
    }

    private void showErrorMessageWithData(String data) {
        labelErrorMessage.setText(data);
        playFadeOutTransition();
    }

    private void initOpenAnimation(TranslateTransition translateTransition, Node node) {
        translateTransition.setNode(node);

        translateTransition.setFromY(node.getLayoutY());
        translateTransition.setToY(-1.5 * SCREEN_HEIGHT);
        translateTransition.setCycleCount(1);

        translateTransition.setDuration(ANIMATION_SECONDS_DURATION);

        translateTransition.setOnFinished(event -> root.getChildren().remove(node));
    }

    private void initFadeOutTransition() {
        fadeOutTransition = new FadeTransition(ANIMATION_SECONDS_DURATION, labelErrorMessage);

        fadeOutTransition.setFromValue(1);
        fadeOutTransition.setToValue(0);
        fadeOutTransition.setCycleCount(1);
    }

    private void playOpenAnimation() {
        playOpenSound();
        translateTransitionOpen.play();
        hideLobbyPanel.play();
    }

    private void playOpenSound() {
        Media music = new Media((Objects.requireNonNull(getClass().getResource("/sounds/garage_door.mp3")).toString()));
        MediaPlayer mediaPlayer = new MediaPlayer(music);
        mediaPlayer.play();
    }

    private void playFadeOutTransition() {
        fadeOutTransition.play();
    }

    @FXML
    private void onButtonJoinLobbyClick() {
        clientInfoViewController.showView();
    }

    private void launchServer() throws IOException {
        ServerSocket serverSocket = new ServerSocket(serverInfoViewController.getPort());
        server = new GameServer(serverSocket, 2);

        root.getScene().getWindow().setOnCloseRequest(windowEvent -> {
            if (server != null) {
                server.close();
            }
            if (client != null) {
                client.close();
            }
            windowEvent.consume();
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (server != null) {
                server.close();
            }
        }));

        playingTime = serverInfoViewController.getPlayingTime();

        server.start();
    }

    private void joinServer(boolean isHost) throws IOException {
        Map<String, Consumer<String>> serverCallBackHandlers = new HashMap<>();

        serverCallBackHandlers.put("UPDATE", callback -> {
            List<String> allPlayerNicknames = ParsingUtils.getNicknamesListFromString(callback);
            Platform.runLater(() -> fillPlayersInfo(allPlayerNicknames));
        });

        serverCallBackHandlers.put("START", callback -> {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            playingTime = LocalTime.parse(callback, dateTimeFormatter);

            Platform.runLater(this::startGame);
        });

        serverCallBackHandlers.put("OPPONENT", callback -> partnerName = callback);

        if (isHost) {
            nickname = serverInfoViewController.getNickname();
            client = new Client(new Socket("localhost", serverInfoViewController.getPort()),
                    nickname,
                    true,
                    serverCallBackHandlers,
                    List.of("FIGURE", "RESULT", "TIME"));

            client.sendRequestToServer("TIME " + playingTime.format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        } else {
            nickname = clientInfoViewController.getNickname();
            client = new Client(new Socket(clientInfoViewController.getServerIP(),
                    clientInfoViewController.getPort()), nickname,
                    false,
                    serverCallBackHandlers,
                    List.of("FIGURE", "RESULT"));
        }

        root.getScene().getWindow().setOnCloseRequest(windowEvent -> {
            if (server != null) {
                server.close();
            }
            if (client != null) {
                client.close();
            }
            windowEvent.consume();
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (client != null) {
                client.close();
            }
        }));

        client.lobby();
    }

    private void startGame() {
        try {
            gridPaneMain.getChildren().remove(buttonDisconnect);
            radio.turnOff();
            Stage stage = (Stage) gridPaneMain.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/splashscreen-view.fxml"));
            root.getChildren().setAll((Node) fxmlLoader.load());
            stage.setUserData(new MultiplayerInfoContainer(client, playingTime, nickname, partnerName));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @FXML
    private void onButtonExitClick() {
        launchSplashScreenWithData("mainMenu");
    }

    private void launchSplashScreenWithData(String data) {
        try {
            Stage stage = (Stage) root.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/splashscreen-view.fxml"));
            root.getChildren().setAll((Node) fxmlLoader.load());
            stage.setUserData(data);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    @FXML
    private void onButtonExitLobbyClick() {
        if (server != null) {
            server.close();
            server = null;
        }

        if (client != null) {
            client.close();
            client = null;
        }

        radio.turnOff();
        launchSplashScreenWithData("multiPlayerLobby");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initGridPanePlayerCards();
        translateTransitionOpen = new TranslateTransition();
        initOpenAnimation(translateTransitionOpen, gridPaneBackgroundCover);
        hideLobbyPanel = new TranslateTransition();
        initOpenAnimation(hideLobbyPanel, gridPaneLobbyPanel);
        initFadeOutTransition();
        initServerInfoView();
        initClientInfoView();
    }
}

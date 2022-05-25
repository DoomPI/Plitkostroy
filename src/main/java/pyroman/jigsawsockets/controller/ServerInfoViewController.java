package pyroman.jigsawsockets.controller;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class ServerInfoViewController implements Initializable {

    private static final Duration ANIMATION_SECONDS_DURATION = Duration.seconds(2);

    public SimpleBooleanProperty textFieldsValidated;

    @FXML
    private StackPane root;

    @FXML
    private TextField textFieldPort;

    @FXML
    private TextField textFieldPlayingTime;

    @FXML
    private TextField textFieldNickname;

    @FXML
    private Label labelErrorMessage;

    private FadeTransition fadeOutTransition;

    public int getPort() {
        return Integer.parseInt(textFieldPort.getText().trim());
    }

    public LocalTime getPlayingTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        return LocalTime.parse(textFieldPlayingTime.getText().trim(), dateTimeFormatter);
    }

    public String getNickname() {
        return textFieldNickname.getText().trim();
    }

    @FXML
    private void onButtonSubmitClick() {
        String errorInfo = validateTextFields();
        if (errorInfo.isEmpty()) {
            textFieldsValidated.set(true);
            hideView();
        } else {
            showErrorMessageWithData(errorInfo);
        }
    }

    private String validateTextFields() {
        StringBuilder errorInfo = new StringBuilder();

        String port = textFieldPort.getText();

        try {
            if (port == null || port.trim().isEmpty()) {
                errorInfo.append("Некорректный порт!\n");
            } else {
                int intValue = Integer.parseInt(port.trim());
                if (intValue < 0 || intValue > 65535) {
                    errorInfo.append("Некорректный порт!\n");
                }
            }
        } catch (NumberFormatException exception) {
            errorInfo.append("Некорректный порт!\n");
        }

        String playingTime = textFieldPlayingTime.getText();

        try {
            if (playingTime == null || playingTime.trim().isEmpty()) {
                errorInfo.append("Некорректное время!\n");
            } else {
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalTime time = LocalTime.parse(playingTime.trim(), dateTimeFormatter);

                if (time.compareTo(LocalTime.parse("00:00:45", dateTimeFormatter)) < 0) {
                    errorInfo.append("На игру должно отводиться минимум 45 секунд!");
                }
            }
        } catch (DateTimeParseException exception) {
            errorInfo.append("Некорректное время!\n");
        }

        String nickname = textFieldNickname.getText();

        if (nickname == null || nickname.trim().isEmpty()) {
            errorInfo.append("Некорректный никнейм!");
        }

        return errorInfo.toString();
    }

    @FXML
    private void onButtonCancelClick() {
        hideView();
    }

    private void showErrorMessageWithData(String data) {
        labelErrorMessage.setText(data);
        playFadeOutTransition();
    }

    private void playFadeOutTransition() {
        fadeOutTransition.play();
    }

    private void initFadeOutTransition() {
        fadeOutTransition = new FadeTransition(ANIMATION_SECONDS_DURATION, labelErrorMessage);

        fadeOutTransition.setFromValue(1);
        fadeOutTransition.setToValue(0);
        fadeOutTransition.setCycleCount(1);
    }

    public void showView() {
        root.setVisible(true);
    }

    public void hideView() {
        root.setVisible(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initFadeOutTransition();
        textFieldsValidated = new SimpleBooleanProperty(false);
    }
}

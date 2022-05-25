package pyroman.jigsawsockets.view;

import javafx.scene.control.ToggleButton;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.Objects;

public class Radio extends ToggleButton {

    private MediaPlayer mediaPlayer;

    public Radio() {
        super();

        setOnAction(event -> {
            if (isSelected()) {
                turnOff();
            } else {
                turnOn();
            }
        });
    }


    public void turnOn() {
        Media music = new Media((Objects.requireNonNull(getClass().getResource("/sounds/radio/radio.mp3")).toString()));
        mediaPlayer = new MediaPlayer(music);
        mediaPlayer.setVolume(0.25);
        mediaPlayer.play();
        mediaPlayer.setOnEndOfMedia(() -> mediaPlayer.seek(Duration.ZERO));
    }

    public void turnOff() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }
}

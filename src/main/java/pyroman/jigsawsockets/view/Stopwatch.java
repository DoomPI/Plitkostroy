package pyroman.jigsawsockets.view;

import javafx.animation.AnimationTimer;
import javafx.beans.property.SimpleStringProperty;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Stopwatch extends AnimationTimer {

    private LocalDateTime startTimestamp;

    private final SimpleStringProperty stopWatchProperty;

    private final DateTimeFormatter dateTimeFormatter;

    public Stopwatch(SimpleStringProperty stopWatchProperty) {
        this.stopWatchProperty = stopWatchProperty;
        dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    }

    @Override
    public void start() {
        startTimestamp = LocalDateTime.now();
        super.start();
    }

    @Override
    public void handle(long l) {
        LocalDateTime currentTime = LocalDateTime.now();
        long secondsDuration = Duration.between(startTimestamp, currentTime).toSeconds();
        stopWatchProperty.set(dateTimeFormatter.format(LocalTime.of(0, 0, 0).plusSeconds(secondsDuration)));
    }
}

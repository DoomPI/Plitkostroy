package pyroman.jigsawsockets.connection.server;


import java.time.LocalTime;

public record PlayerData(String nickname, int numberOfPlacedFigures, LocalTime playingTime) {
}

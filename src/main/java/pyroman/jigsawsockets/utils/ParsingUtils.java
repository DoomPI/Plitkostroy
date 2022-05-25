package pyroman.jigsawsockets.utils;

import javafx.geometry.Point2D;
import pyroman.jigsawsockets.connection.server.PlayerData;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParsingUtils {

    public static List<Point2D> getCoordinatesFromString(String input) {
        List<Point2D> output = new ArrayList<>();
        String[] pairs = input.split(";");

        for (String pair : pairs) {
            output.add(new Point2D(Integer.parseInt(pair.split(",")[0]),
                    Integer.parseInt(pair.split(",")[1])));
        }

        return output;
    }

    public static List<String> getNicknamesListFromString(String input) {
        String[] nicknames = input.split(" ");
        return new ArrayList<>(Arrays.asList(nicknames));
    }

    public static PlayerData getPlayerDataFromString(String input) {
        String[] data = input.split(" ");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        return new PlayerData(
                data[0],
                Integer.parseInt(data[1]),
                LocalTime.parse(data[2], dateTimeFormatter));
    }
}

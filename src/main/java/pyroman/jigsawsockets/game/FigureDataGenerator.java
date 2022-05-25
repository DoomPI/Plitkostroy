package pyroman.jigsawsockets.game;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class FigureDataGenerator {
    private final List<String> allFiguresData;

    public FigureDataGenerator() {
        File path = new File(Objects.requireNonNull(getClass().getResource("/figures.txt")).getFile());
        try {
            allFiguresData = Files.readAllLines(path.toPath());
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    public String generateRandomFigureData() {
        return allFiguresData.get(ThreadLocalRandom.current().nextInt(0, allFiguresData.size() - 1));
    }
}

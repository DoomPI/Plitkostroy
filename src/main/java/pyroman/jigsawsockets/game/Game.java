package pyroman.jigsawsockets.game;

import pyroman.jigsawsockets.controller.BoardViewController;

import java.time.LocalTime;

public class Game {

    private final String[] figuresData;
    private LocalTime playingTime;

    public void setPlayingTime(LocalTime playingTime) {
        this.playingTime = playingTime;
    }

    public LocalTime getPlayingTime() {
        return playingTime;
    }

    public Game() {
        FigureDataGenerator figureDataGenerator = new FigureDataGenerator();

        figuresData = new String[BoardViewController.BOARD_TILES_WIDTH * BoardViewController.BOARD_TILES_HEIGHT];
        for (int index = 0; index < figuresData.length; ++index) {
            figuresData[index] = figureDataGenerator.generateRandomFigureData();
        }
    }

    public String getFigureDataByIndex(int index) {
        return figuresData[index];
    }
}

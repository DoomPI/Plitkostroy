package pyroman.jigsawsockets.view;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import pyroman.jigsawsockets.utils.ParsingUtils;
import pyroman.jigsawsockets.view.figure.FigurePiece;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class Tile extends Pane {

    public static final int TILE_SIZE = 80;

    private final int row;
    private final int column;
    private boolean isFilled;
    private final SimpleIntegerProperty figuresCountProperty;

    public Tile(int row, int column, SimpleIntegerProperty figuresCountProperty) {
        this.row = row;
        this.column = column;
        isFilled = false;
        this.figuresCountProperty = figuresCountProperty;

        setScale();
        initEventHandlers();
    }

    private void setScale() {
        setPrefWidth(TILE_SIZE);
        setPrefHeight(TILE_SIZE);
    }

    private void initEventHandlers() {
        setOnDragOver(this::handleDragOver);
        setOnDragExited(this::handleMouseExit);
        setOnDragDropped(this::handleDragDrop);
    }

    private void handleDragOver(DragEvent event) {
        if (event.getGestureSource().getClass() == FigurePiece.class
                && event.getDragboard().hasString()) {

            List<Point2D> coordinates = ParsingUtils.getCoordinatesFromString(event.getDragboard().getString());

            int originRow = (int) coordinates.get(0).getX();
            int originColumn = (int) coordinates.get(0).getY();

            GridPane parent = (GridPane) getParent();

            for (Point2D coordinate : coordinates) {
                int gridRow = row + ((int) coordinate.getX() - originRow);
                int gridColumn = column + ((int) coordinate.getY() - originColumn);

                if (gridRow < 0 || gridRow >= parent.getRowCount()
                        || gridColumn < 0 || gridColumn >= parent.getColumnCount()) {
                    markTilesRed(coordinates);
                    return;
                }

                for (Node node : parent.getChildren()) {
                    if (node instanceof Tile gridTile) {
                        if (GridPane.getRowIndex(gridTile) == gridRow
                                && GridPane.getColumnIndex(gridTile) == gridColumn) {
                            if (!gridTile.isFilled) {
                                gridTile.getStyleClass().clear();
                                gridTile.getStyleClass().add("tile-border-green");
                            } else {
                                markTilesRed(coordinates);
                                return;
                            }
                        }
                    }
                }
            }

            event.acceptTransferModes(TransferMode.MOVE);
        }
    }

    private void handleMouseExit(DragEvent event) {
        GridPane parent = (GridPane) getParent();
        for (Node node : parent.getChildren()) {
            if (node instanceof Tile) {
                node.getStyleClass().clear();
            }
        }
    }

    private void handleDragDrop(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard.hasString()) {
            List<Point2D> coordinates = ParsingUtils.getCoordinatesFromString(event.getDragboard().getString());

            int originRow = (int) coordinates.get(0).getX();
            int originColumn = (int) coordinates.get(0).getY();

            GridPane parent = (GridPane) getParent();

            for (Point2D coordinate : coordinates) {
                int gridRow = row + ((int) coordinate.getX() - originRow);
                int gridColumn = column + ((int) coordinate.getY() - originColumn);

                for (Node node : parent.getChildren()) {
                    if (node instanceof Tile gridTile) {
                        if (GridPane.getRowIndex(gridTile) == gridRow
                                && GridPane.getColumnIndex(gridTile) == gridColumn) {
                            ((Tile) node).setRandomTexture();
                            ((Tile) node).isFilled = true;
                        }
                    }
                }
            }

            figuresCountProperty.set(figuresCountProperty.getValue() + 1);

            playRandomPlacementSound();
        }
    }


    private void setRandomTexture() {
        int numberOfFigures = Objects.requireNonNull(
                new File(Objects.requireNonNull(
                        getClass().getResource("/textures/figure")).getFile()).listFiles()).length;

        int randomNumber = Math.abs(ThreadLocalRandom.current().nextInt()) % numberOfFigures + 1;
        setBackground(new Background(new BackgroundImage(new Image(Objects.requireNonNull(getClass().getResource(
                "/textures/figure/stone_tile_" + randomNumber + ".jpg")).toString()),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT
        )));
    }

    private void markTilesRed(List<Point2D> coordinates) {
        GridPane parent = (GridPane) getParent();

        for (Point2D coord : coordinates) {
            int originRow = (int) coordinates.get(0).getX();
            int originColumn = (int) coordinates.get(0).getY();

            int gridRow = this.row + ((int) coord.getX() - originRow);
            int gridColumn = this.column + ((int) coord.getY() - originColumn);

            for (Node node : parent.getChildren()) {
                if (node instanceof Tile gridTile) {
                    if (GridPane.getRowIndex(gridTile) == gridRow
                            && GridPane.getColumnIndex(gridTile) == gridColumn) {
                        gridTile.getStyleClass().clear();
                        gridTile.getStyleClass().add("tile-border-red");
                    }
                }
            }
        }
    }

    private void playRandomPlacementSound() {
        int randomNumber = Math.abs(ThreadLocalRandom.current().nextInt()) % 3 + 1;
        new AudioClip(Objects.requireNonNull(getClass().getResource("/sounds/tile/tile_sound_" + randomNumber + ".mp3"))
                .toString()).play();
    }
}

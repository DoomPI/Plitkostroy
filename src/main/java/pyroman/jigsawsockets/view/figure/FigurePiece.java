package pyroman.jigsawsockets.view.figure;

import javafx.scene.image.Image;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

public class FigurePiece extends Pane {

    public FigurePiece(String pieceCoordinatesData) {
        setRandomTexture();
        setPrefWidth(80);
        setPrefHeight(80);


        setOnDragDetected(event -> {
            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(pieceCoordinatesData);
            dragboard.setContent(content);
            event.consume();
        });
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


}

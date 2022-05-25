package pyroman.jigsawsockets.view.figure;

import javafx.geometry.Point2D;
import javafx.scene.layout.GridPane;
import pyroman.jigsawsockets.utils.ParsingUtils;

import java.util.List;

public class FigureSpawner extends GridPane {

    public void spawnFigure(String figureData) {
        getChildren().clear();
        fillWithPieces(figureData);
    }

    private void fillWithPieces(String piecesCoordinatesData) {

        List<Point2D> piecesCoordinates = ParsingUtils.getCoordinatesFromString(piecesCoordinatesData);

        for (Point2D coordinate : piecesCoordinates) {
            add(new FigurePiece(piecesCoordinatesData), (int) coordinate.getY(), (int) coordinate.getX());
        }
    }
}

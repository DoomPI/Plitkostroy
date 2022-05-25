package pyroman.jigsawsockets.view;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

public class PlayerLobbyCard extends GridPane {

    public PlayerLobbyCard(String playerNickname) {
        super();

        initConstraints();

        if (!playerNickname.isEmpty()) {
            initGridPaneInner(false);
            initLabelNickname(playerNickname);
        } else {
            initGridPaneInner(true);
            initLabelNickname("Ожидание...");
        }
    }

    private void initConstraints() {
        ColumnConstraints columnConstraintsFirst = new ColumnConstraints();
        columnConstraintsFirst.setPercentWidth(20);
        ColumnConstraints columnConstraintsSecond = new ColumnConstraints();
        columnConstraintsSecond.setPercentWidth(60);
        ColumnConstraints columnConstraintsThird = new ColumnConstraints();
        columnConstraintsThird.setPercentWidth(20);

        getColumnConstraints().addAll(
                columnConstraintsFirst,
                columnConstraintsSecond,
                columnConstraintsThird);

        RowConstraints rowConstraintsFirst = new RowConstraints();
        rowConstraintsFirst.setPercentHeight(20);
        RowConstraints rowConstraintsSecond = new RowConstraints();
        rowConstraintsSecond.setPercentHeight(50);
        RowConstraints rowConstraintsThird = new RowConstraints();
        rowConstraintsThird.setPercentHeight(30);

        getRowConstraints().addAll(
                rowConstraintsFirst,
                rowConstraintsSecond,
                rowConstraintsThird);
    }

    private void initGridPaneInner(boolean isUnknown) {
        GridPane gridPaneInner = new GridPane();
        gridPaneInner.getStyleClass().add(isUnknown ? "grid-pane-unknown-card" : "grid-pane-player-card");
        add(gridPaneInner, 1, 1);
    }

    private void initLabelNickname(String playerNickname) {
        Label labelNickname = new Label(playerNickname);
        labelNickname.getStyleClass().add("label-nickname");
        GridPane.setHalignment(labelNickname, HPos.CENTER);
        GridPane.setValignment(labelNickname, VPos.TOP);
        add(labelNickname, 1, 2);
    }
}

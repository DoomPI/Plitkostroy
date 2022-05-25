package pyroman.jigsawsockets;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;
import pyroman.jigsawsockets.controller.MainMenuViewController;

import java.io.IOException;
import java.util.Objects;

public class JigsawApplication extends Application {
    public static final double SCREEN_HEIGHT = Screen.getPrimary().getVisualBounds().getHeight();

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/mainmenu-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Плиткострой");
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setScene(scene);
        MainMenuViewController mainMenuViewController = fxmlLoader.getController();
        mainMenuViewController.onWindowShowing();

        stage.show();
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResource("/textures/icon.jpg")).toString()));
    }

    public static void main(String[] args) {
        launch();
    }
}
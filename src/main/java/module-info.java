module pyroman.jigsawsockets {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;


    opens pyroman.jigsawsockets to javafx.fxml;
    exports pyroman.jigsawsockets;

    exports pyroman.jigsawsockets.view;
    exports pyroman.jigsawsockets.controller;
    exports pyroman.jigsawsockets.view.figure;
    opens pyroman.jigsawsockets.view.figure to javafx.fxml;
    opens pyroman.jigsawsockets.view to javafx.fxml;
    opens pyroman.jigsawsockets.controller to javafx.fxml;
    exports pyroman.jigsawsockets.game;
    opens pyroman.jigsawsockets.game to javafx.fxml;
    exports pyroman.jigsawsockets.connection.client;
    opens pyroman.jigsawsockets.connection.client to javafx.fxml;
    exports pyroman.jigsawsockets.connection.server;
    opens pyroman.jigsawsockets.connection.server to javafx.fxml;
}
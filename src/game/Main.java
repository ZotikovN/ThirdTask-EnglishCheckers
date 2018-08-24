package game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Main extends Application {
    private static final double boardSize = 650.0;

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        BackgroundImage boardImg = new BackgroundImage(
                new Image("img/board.png", boardSize, boardSize,false,true),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
        root.setBackground(new Background(boardImg));
        root.getChildren().add(new Field());
        Scene scene = new Scene(root, boardSize, boardSize);
        scene.setOnKeyPressed((KeyEvent kek) -> {
            if(kek.getCode() == KeyCode.F5) {
                root.getChildren().clear();
                root.getChildren().add(new Field());
            }
        });
        primaryStage.setTitle("English Checkers");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

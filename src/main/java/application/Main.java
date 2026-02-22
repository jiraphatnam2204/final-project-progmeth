package application;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    public static SceneManager sceneManager;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Tanjiro: The Swordsmith");
        stage.setResizable(false);
        stage.getIcons().add(new javafx.scene.image.Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png"))
        ));

        sceneManager = new SceneManager(stage);

        sceneManager.showMainMenu();

        stage.show();
    }
}

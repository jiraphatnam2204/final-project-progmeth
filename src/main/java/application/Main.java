package application;

import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Application entry point for "Tanjiro: The Swordsmith".
 * Initialises the primary JavaFX {@link Stage} and hands control to {@link SceneManager}.
 */
public class Main extends Application {

    /** The global scene manager used to switch between all game screens. */
    public static SceneManager sceneManager;

    /**
     * Standard Java entry point; delegates to {@link Application#launch}.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Called by JavaFX after the toolkit is initialised.
     * Sets up the window title, icon, and shows the main menu.
     *
     * @param stage the primary stage provided by the JavaFX runtime
     */
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

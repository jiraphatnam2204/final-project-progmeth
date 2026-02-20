package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import logic.creatures.Player;
import logic.pickaxe.Pickaxe;

/**
 * Main entry point — JavaFX calls start() and gives us the window (Stage).
 * We use a static SceneManager so any class can switch scenes globally.
 */
public class Main extends Application {

    // SceneManager is the "TV remote" — it controls which screen is shown
    public static SceneManager sceneManager;

    @Override
    public void start(Stage stage) {
        stage.setTitle("⚔  Mining Adventure RPG");
        stage.setResizable(false);

        // Create the scene manager, wiring it to the stage
        sceneManager = new SceneManager(stage);

        // Start at the main menu
        sceneManager.showMainMenu();

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

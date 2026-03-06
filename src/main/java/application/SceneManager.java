package application;

import audio.AudioManager;
import javafx.stage.Stage;
import logic.creatures.Player;
import logic.pickaxe.Pickaxe;
import scenes.boss.BossController;
import scenes.boss.BossView;
import scenes.game.GameController;
import scenes.game.GameView;
import scenes.gameover.GameOverController;
import scenes.gameover.GameOverView;
import scenes.mainmenu.MainMenuController;
import scenes.mainmenu.MainMenuView;

/**
 * Central scene coordinator for the game.
 * Owns the primary {@link Stage} and is responsible for transitioning between
 * all screens: main menu, game world, boss room, and game-over screen.
 */
public class SceneManager {

    /** The fixed window width in pixels. */
    public static final int W = 960;

    /** The fixed window height in pixels. */
    public static final int H = 755;

    /** The primary JavaFX stage managed by this coordinator. */
    private final Stage stage;

    /**
     * Creates a new SceneManager and configures the window dimensions.
     *
     * @param stage the primary JavaFX stage to manage
     */
    public SceneManager(Stage stage) {
        this.stage = stage;
        stage.setWidth(W);
        stage.setHeight(H);
    }

    /**
     * Transitions to the main menu screen and starts the menu BGM.
     */
    public void showMainMenu() {
        AudioManager.playBGM("/sounds/menu.mp3", 0.1);

        MainMenuController controller = new MainMenuController();
        MainMenuView view = new MainMenuView(controller);
        stage.setScene(view.build());
    }

    /**
     * Transitions to the main game world screen.
     *
     * @param player  the player character to use in the game
     * @param pickaxe the starting pickaxe for the player
     */
    public void showGame(Player player, Pickaxe pickaxe) {
        AudioManager.playBGM("/sounds/bgm.mp3", 0.1);

        GameController controller = new GameController(player, pickaxe);
        GameView view = new GameView(controller);
        stage.setScene(view.buildScene());
    }

    /**
     * Transitions to the boss battle room and starts the boss BGM.
     *
     * @param player        the player entering the boss room
     * @param pickaxeHolder a single-element array holding the current pickaxe,
     *                      passed by reference so the boss scene can read upgrades
     */
    public void showBossRoom(Player player, Pickaxe[] pickaxeHolder) {
        AudioManager.playBGM("/sounds/boss.mp3", 0.02);

        BossController controller = new BossController(player);
        BossView view = new BossView(controller, pickaxeHolder);
        stage.setScene(view.build());
    }

    /**
     * Transitions to the game-over / victory screen.
     *
     * @param won    {@code true} if all bosses were defeated, {@code false} if the player died
     * @param player the player whose final stats are displayed
     */
    public void showGameOver(boolean won, Player player) {
        AudioManager.playBGM("/sounds/menu.mp3", 0.02);

        GameOverController controller = new GameOverController(won, player);
        GameOverView view = new GameOverView(controller);
        stage.setScene(view.build());
    }
}
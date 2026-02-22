package application;

import javafx.stage.Stage;
import logic.creatures.Player;
import logic.pickaxe.Pickaxe;

/**
 * SceneManager is the "screen router" of the game.
 * Instead of juggling Scenes everywhere, any class just calls:
 *   Main.sceneManager.showGame(player, pickaxe);
 * and the right screen appears.
 *
 * Think of it like a TV remote — you tell it which channel to show.
 */
public class SceneManager {

    private final Stage stage;

    // Window dimensions — used by all scenes
    public static final int W = 960;
    public static final int H = 755;

    public SceneManager(Stage stage) {
        this.stage = stage;
        stage.setWidth(W);
        stage.setHeight(H);
    }

    /** Show the main menu */
    public void showMainMenu() {
        AudioManager.playBGM("/sounds/menu.mp3",  0.1);
        MainMenuScene menu = new MainMenuScene();
        stage.setScene(menu.build());
    }

    /** Show the overworld (mining + monsters) */
    public void showGame(Player player, Pickaxe pickaxe) {
        AudioManager.playBGM("/sounds/bgm.mp3",   0.1);
        GameScene game = new GameScene(player, pickaxe);
        stage.setScene(game.buildScene());
    }



    /** Show the boss room */
    public void showBossRoom(Player player, Pickaxe[] pickaxeHolder) {
        AudioManager.playBGM("/sounds/boss.mp3", 0.02);
        BossScene boss = new BossScene(player, pickaxeHolder);
        stage.setScene(boss.build());
    }

    /** Show the game-over / victory screen */
    public void showGameOver(boolean won, Player player) {
        AudioManager.playBGM("/sounds/boss.mp3", 0.02);
        GameOverScene over = new GameOverScene(won, player);
        stage.setScene(over.build());
    }
}

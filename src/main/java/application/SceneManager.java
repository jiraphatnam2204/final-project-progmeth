package application;

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

public class SceneManager {

    public static final int W = 960;
    public static final int H = 755;

    private final Stage stage;

    public SceneManager(Stage stage) {
        this.stage = stage;
        stage.setWidth(W);
        stage.setHeight(H);
    }

    // ── Each method now creates a Controller (logic) + View (drawing) pair ────

    public void showMainMenu() {
        // AudioManager.playBGM("/sounds/menu.mp3", 0.1);

        // Controller holds the star animation state
        // View builds the Scene and drives the AnimationTimer
        MainMenuController controller = new MainMenuController();
        MainMenuView view = new MainMenuView(controller);
        stage.setScene(view.build());
    }

    public void showGame(Player player, Pickaxe pickaxe) {
        // AudioManager.playBGM("/sounds/bgm.mp3", 0.1);

        // Controller holds the world grid, player position, monsters, etc.
        // View builds the Scene, draws tiles/HUD, and manages overlays
        GameController controller = new GameController(player, pickaxe);
        GameView view = new GameView(controller);
        stage.setScene(view.buildScene());
    }

    public void showBossRoom(Player player, Pickaxe[] pickaxeHolder) {
        // AudioManager.playBGM("/sounds/boss.mp3", 0.02);

        // Controller manages combat state (whose turn, HP, log, etc.)
        // View draws sprites, HP bars, buttons, and runs the battle loop
        BossController controller = new BossController(player);
        BossView view = new BossView(controller, pickaxeHolder);
        stage.setScene(view.build());
    }

    public void showGameOver(boolean won, Player player) {
        // AudioManager.playBGM("/sounds/menu.mp3", 0.02);

        // Controller holds win/lose flag and particle positions
        // View draws the animated background and final stats
        GameOverController controller = new GameOverController(won, player);
        GameOverView view = new GameOverView(controller);
        stage.setScene(view.build());
    }
}
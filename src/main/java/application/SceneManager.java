package application;

import javafx.stage.Stage;
import logic.creatures.Player;
import logic.pickaxe.Pickaxe;

public class SceneManager {

    public static final int W = 960;
    public static final int H = 755;
    private final Stage stage;

    public SceneManager(Stage stage) {
        this.stage = stage;
        stage.setWidth(W);
        stage.setHeight(H);
    }

    public void showMainMenu() {
        AudioManager.playBGM("/sounds/menu.mp3", 0.1);
        MainMenuScene menu = new MainMenuScene();
        stage.setScene(menu.build());
    }

    public void showGame(Player player, Pickaxe pickaxe) {
        AudioManager.playBGM("/sounds/bgm.mp3", 0.1);
        GameScene game = new GameScene(player, pickaxe);
        stage.setScene(game.buildScene());
    }


    public void showBossRoom(Player player, Pickaxe[] pickaxeHolder) {
        AudioManager.playBGM("/sounds/boss.mp3", 0.02);
        BossScene boss = new BossScene(player, pickaxeHolder);
        stage.setScene(boss.build());
    }

    public void showGameOver(boolean won, Player player) {
        AudioManager.playBGM("/sounds/menu.mp3", 0.02);
        GameOverScene over = new GameOverScene(won, player);
        stage.setScene(over.build());
    }
}

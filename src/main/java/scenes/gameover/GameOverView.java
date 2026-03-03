package scenes.gameover;

import application.Main;
import application.SceneManager;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import logic.util.ItemCounter;

/**
 * JavaFX view for the game-over / victory scene.
 * Renders an animated particle effect (confetti on victory, falling ash on defeat),
 * the result headline, final player stats, and navigation buttons.
 * Delegates particle animation logic to {@link GameOverController}.
 */
public class GameOverView {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;

    private final GameOverController controller;

    /**
     * Creates a new GameOverView.
     *
     * @param controller the game-over controller providing particle and player data
     */
    public GameOverView(GameOverController controller) {
        this.controller = controller;
    }

    /**
     * Builds and returns the game-over {@link Scene}, including the animated canvas,
     * result headline, stats panel, and "Main Menu" / "Quit" buttons.
     * Starts an {@link javafx.animation.AnimationTimer} that drives the animation loop.
     *
     * @return the fully constructed game-over scene
     */
    public Scene build() {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);

        Button menuBtn = makeBtn("Main Menu", "#1565c0", "#1976d2");
        Button quitBtn = makeBtn("Quit", "#c62828", "#ef5350");

        menuBtn.setLayoutX(W / 2.0 - 160);
        menuBtn.setLayoutY(H * 0.78);
        quitBtn.setLayoutX(W / 2.0 + 20);
        quitBtn.setLayoutY(H * 0.78);

        menuBtn.setOnAction(e -> Main.sceneManager.showMainMenu());
        quitBtn.setOnAction(e -> System.exit(0));

        root.getChildren().addAll(menuBtn, quitBtn);

        // The AnimationTimer acts like a movie projector running at 60 frames per second.
        // Every frame, it updates the math (particle positions) and tells the GraphicsContext
        // to erase the old frame and paint the new one.
        new AnimationTimer() {
            double t = 0;

            @Override
            public void handle(long now) {
                t += 1.0 / 60;
                controller.updateParticles();
                draw(gc, t);
            }
        }.start();

        return new Scene(root, W, H);
    }

    // The traffic controller for drawing. It checks the game state to decide whether
    // to paint the Victory screen or the Defeat screen, and always slaps the stats box on top.
    private void draw(GraphicsContext gc, double t) {
        if (controller.isWon()) {
            drawVictory(gc, t);
        } else {
            drawDefeat(gc, t);
        }
        drawStatsBox(gc);
    }

    // Draws the victory screen: a shiny gradient background, confetti particles, and glowing text.
    private void drawVictory(GraphicsContext gc, double t) {
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0d0800")),
                new Stop(0.5, Color.web("#2a1800")),
                new Stop(1, Color.web("#0d0800")));
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);

        double[] px = controller.getPx();
        double[] py = controller.getPy();
        double[] pr = controller.getPr();
        for (int i = 0; i < controller.getParticleCount(); i++) {
            Color c = i % 3 == 0 ? Color.web("#ffd700")
                    : i % 3 == 1 ? Color.web("#ff8f00")
                    : Color.WHITE;
            gc.setFill(c);
            gc.fillOval(px[i], py[i], pr[i], pr[i]);
        }

        // Math.sin() naturally loops between -1 and 1. Multiplying it scales that wave up,
        // which makes the text "bob" up and down smoothly over time, like a boat on water.
        double bob = Math.sin(t * 2) * 8;

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 80));

        // This loop draws the same text 10 times, getting slightly more transparent and offset
        // each time. This creates a cheap but effective "glowing shadow" behind the real text.
        for (int d = 10; d >= 1; d--) {
            gc.setFill(Color.rgb(255, 200, 0, (5.0 * d) / 255.0));
            gc.fillText("🏆 VICTORY! 🏆", W / 2.0 + d, H * 0.28 + bob + d);
        }
        gc.setFill(Color.web("#ffd700"));
        gc.fillText("🏆 VICTORY! 🏆", W / 2.0, H * 0.28 + bob);

        gc.setFont(Font.font("Georgia", 24));
        gc.setFill(Color.web("#fff9c4"));
        gc.fillText("All bosses defeated! The kingdom is saved!", W / 2.0, H * 0.42 + bob * 0.5);
    }

    // Draws the defeat screen: a dark red gradient, falling ash particles, and red text.
    private void drawDefeat(GraphicsContext gc, double t) {
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0000")),
                new Stop(1, Color.web("#1a0000")));
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);

        double[] px = controller.getPx();
        double[] py = controller.getPy();
        double[] pr = controller.getPr();
        for (int i = 0; i < controller.getParticleCount(); i++) {
            double alpha = (80.0 + pr[i] * 15.0) / 255.0;
            gc.setFill(Color.rgb(180, 60, 60, alpha));
            gc.fillOval(px[i], py[i], pr[i] * 0.7, pr[i] * 0.7);
        }

        double bob = Math.sin(t * 1.5) * 5;

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 88));

        for (int d = 8; d >= 1; d--) {
            gc.setFill(Color.rgb(200, 0, 0, (6.0 * d) / 255.0));
            gc.fillText("💀 GAME OVER 💀", W / 2.0 + d, H * 0.28 + bob + d);
        }
        gc.setFill(Color.web("#ef5350"));
        gc.fillText("💀 GAME OVER 💀", W / 2.0, H * 0.28 + bob);

        gc.setFont(Font.font("Georgia", 22));
        gc.setFill(Color.web("#ef9a9a"));
        gc.fillText("You were vanquished...", W / 2.0, H * 0.42);
    }

    // Renders a semi-transparent dark panel holding the player's final game data.
    private void drawStatsBox(GraphicsContext gc) {
        var player = controller.getPlayer();

        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRoundRect(W / 2.0 - 250, H * 0.50, 500, 150, 14, 14);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#b0bec5"));
        gc.fillText("─── Final Stats ───", W / 2.0, H * 0.50 + 24);

        gc.setFont(Font.font("Arial", 13));
        gc.setFill(Color.WHITE);
        gc.fillText(
                "Gold: " + player.getGold()
                        + "  |  HP: " + player.getHealth() + "/" + player.getMaxHealth()
                        + "  |  ATK: " + player.getAttack()
                        + "  |  DEF: " + player.getDefense(),
                W / 2.0, H * 0.50 + 50);

        gc.setFont(Font.font("Arial", 12));
        gc.setFill(Color.web("#90a4ae"));
        gc.fillText("Items collected:", W / 2.0, H * 0.50 + 74);

        StringBuilder inv = new StringBuilder();
        for (ItemCounter ic : player.getInventory()) {
            if (!inv.isEmpty()) inv.append("  ");
            inv.append(ic.getItem().getName()).append("×").append(ic.getCount());
        }
        gc.fillText(inv.isEmpty() ? "(none)" : inv.toString(), W / 2.0, H * 0.50 + 96);

        // Reset text alignment back to default so other drawing operations aren't messed up
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // Helper method to create UI buttons and apply CSS styling (like hover effects) directly via code.
    private Button makeBtn(String text, String bg, String hover) {
        Button b = new Button(text);
        b.setPrefWidth(130);
        b.setPrefHeight(40);
        String s = "-fx-background-color:" + bg + ";-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-font-size:14px;"
                + "-fx-background-radius:8;-fx-cursor:hand;";
        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(s.replace(bg, hover)));
        b.setOnMouseExited(e -> b.setStyle(s));
        return b;
    }
}
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
 * GameOverView — the "face" of the game-over / victory screen.
 * <p>
 * Responsibility: ONLY visual output.
 * - Draws the animated background (gradient + particles)
 * - Draws the big title ("VICTORY!" or "GAME OVER")
 * - Draws the final stats box
 * - Runs the AnimationTimer (which calls controller.updateParticles())
 * - Creates the Main Menu / Quit buttons
 */
public class GameOverView {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;

    private final GameOverController controller;

    public GameOverView(GameOverController controller) {
        this.controller = controller;
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    /**
     * Builds and returns the complete game-over Scene.
     */
    public Scene build() {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);

        // ── Buttons ─────────────────────────────────────────────────────────
        Button menuBtn = makeBtn("Main Menu", "#1565c0", "#1976d2");
        Button quitBtn = makeBtn("Quit", "#c62828", "#ef5350");

        menuBtn.setLayoutX(W / 2.0 - 160);
        menuBtn.setLayoutY(H * 0.78);
        quitBtn.setLayoutX(W / 2.0 + 20);
        quitBtn.setLayoutY(H * 0.78);

        menuBtn.setOnAction(e -> Main.sceneManager.showMainMenu());
        quitBtn.setOnAction(e -> System.exit(0));

        root.getChildren().addAll(menuBtn, quitBtn);

        // ── Animation loop ───────────────────────────────────────────────────
        new AnimationTimer() {
            double t = 0; // time accumulator for bobbing/pulsing effects

            @Override
            public void handle(long now) {
                t += 1.0 / 60; // advance by one frame (~16ms at 60fps)
                controller.updateParticles(); // logic handles position math
                draw(gc, t);                  // view handles all drawing
            }
        }.start();

        return new Scene(root, W, H);
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    private void draw(GraphicsContext gc, double t) {
        if (controller.isWon()) {
            drawVictory(gc, t);
        } else {
            drawDefeat(gc, t);
        }
        drawStatsBox(gc);
    }

    private void drawVictory(GraphicsContext gc, double t) {
        // Dark golden background
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0d0800")),
                new Stop(0.5, Color.web("#2a1800")),
                new Stop(1, Color.web("#0d0800")));
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);

        // Confetti particles — 3 alternating colours
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

        // Bobbing title offset (Math.sin creates a smooth up-down wave)
        double bob = Math.sin(t * 2) * 8;

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 80));

        // Glow shadow layers (drawn behind the main text)
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

    private void drawDefeat(GraphicsContext gc, double t) {
        // Deep red background
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0000")),
                new Stop(1, Color.web("#1a0000")));
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);

        // Falling ash particles
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

        // Red glow shadow layers
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

    /**
     * Draws the dark panel showing final HP, ATK, DEF, gold, and inventory.
     */
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

        gc.setTextAlign(TextAlignment.LEFT);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

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

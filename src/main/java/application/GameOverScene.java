package application;

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
import logic.creatures.Player;
import logic.util.ItemCounter;

import java.util.Random;

public class GameOverScene {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;

    private final boolean won;
    private final Player player;

    private final double[] px  = new double[80];
    private final double[] py  = new double[80];
    private final double[] pvx = new double[80];
    private final double[] pvy = new double[80];
    private final double[] pr  = new double[80];

    public GameOverScene(boolean won, Player player) {
        this.won = won;
        this.player = player;

        Random rng = new Random();
        for (int i = 0; i < 80; i++) {
            px[i]  = rng.nextDouble() * W;
            py[i]  = rng.nextDouble() * H;
            pvx[i] = (rng.nextDouble() - 0.5) * 2;
            pvy[i] = won ? -(0.5 + rng.nextDouble() * 2)
                    :  (0.5 + rng.nextDouble() * 2);
            pr[i]  = 2 + rng.nextDouble() * 5;
        }
    }

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

        new AnimationTimer() {
            double t = 0;

            @Override
            public void handle(long now) {
                t += 1.0 / 60;
                updateParticles();
                draw(gc, t);
            }
        }.start();

        return new Scene(root, W, H);
    }

    private void updateParticles() {
        for (int i = 0; i < 80; i++) {
            px[i] += pvx[i];
            py[i] += pvy[i];

            if (won && py[i] < -10) {
                py[i] = H + 5;
                px[i] = Math.random() * W;
            }

            if (!won && py[i] > H + 10) {
                py[i] = -5;
                px[i] = Math.random() * W;
            }
        }
    }

    private void draw(GraphicsContext gc, double t) {

        if (won) {

            LinearGradient bg = new LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#0d0800")),
                    new Stop(0.5, Color.web("#2a1800")),
                    new Stop(1, Color.web("#0d0800"))
            );

            gc.setFill(bg);
            gc.fillRect(0, 0, W, H);

            for (int i = 0; i < 80; i++) {
                Color c = i % 3 == 0
                        ? Color.web("#ffd700")
                        : i % 3 == 1
                        ? Color.web("#ff8f00")
                        : Color.WHITE;

                gc.setFill(c);
                gc.fillOval(px[i], py[i], pr[i], pr[i]);
            }

            double bob = Math.sin(t * 2) * 8;

            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font("Georgia", FontWeight.BOLD, 80));

            // FIXED opacity
            for (int d = 10; d >= 1; d--) {
                double alpha = (5.0 * d) / 255.0;
                gc.setFill(Color.rgb(255, 200, 0, alpha));
                gc.fillText("🏆 VICTORY! 🏆", W / 2.0 + d, H * 0.28 + bob + d);
            }

            gc.setFill(Color.web("#ffd700"));
            gc.fillText("🏆 VICTORY! 🏆", W / 2.0, H * 0.28 + bob);

            gc.setFont(Font.font("Georgia", 24));
            gc.setFill(Color.web("#fff9c4"));
            gc.fillText("All bosses defeated! The kingdom is saved!",
                    W / 2.0, H * 0.42 + bob * 0.5);

        } else {

            LinearGradient bg = new LinearGradient(
                    0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#0a0000")),
                    new Stop(1, Color.web("#1a0000"))
            );

            gc.setFill(bg);
            gc.fillRect(0, 0, W, H);

            for (int i = 0; i < 80; i++) {
                double alpha = (80.0 + pr[i] * 15.0) / 255.0;
                gc.setFill(Color.rgb(180, 60, 60, alpha));
                gc.fillOval(px[i], py[i], pr[i] * 0.7, pr[i] * 0.7);
            }

            double bob = Math.sin(t * 1.5) * 5;

            gc.setTextAlign(TextAlignment.CENTER);
            gc.setFont(Font.font("Georgia", FontWeight.BOLD, 88));

            // FIXED opacity
            for (int d = 8; d >= 1; d--) {
                double alpha = (6.0 * d) / 255.0;
                gc.setFill(Color.rgb(200, 0, 0, alpha));
                gc.fillText("💀 GAME OVER 💀",
                        W / 2.0 + d, H * 0.28 + bob + d);
            }

            gc.setFill(Color.web("#ef5350"));
            gc.fillText("💀 GAME OVER 💀",
                    W / 2.0, H * 0.28 + bob);

            gc.setFont(Font.font("Georgia", 22));
            gc.setFill(Color.web("#ef9a9a"));
            gc.fillText("You were vanquished...",
                    W / 2.0, H * 0.42);
        }

        // Stats Box
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRoundRect(W / 2.0 - 250, H * 0.50, 500, 150, 14, 14);

        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(Color.web("#b0bec5"));
        gc.fillText("─── Final Stats ───",
                W / 2.0, H * 0.50 + 24);

        gc.setFont(Font.font("Arial", 13));
        gc.setFill(Color.WHITE);

        gc.fillText("Gold: " + player.getGold()
                        + "  |  HP: " + player.getHealth()
                        + "/" + player.getMaxHealth()
                        + "  |  ATK: " + player.getAttack()
                        + "  |  DEF: " + player.getDefense(),
                W / 2.0, H * 0.50 + 50);

        gc.setFont(Font.font("Arial", 12));
        gc.setFill(Color.web("#90a4ae"));
        gc.fillText("Items collected:",
                W / 2.0, H * 0.50 + 74);

        StringBuilder inv = new StringBuilder();
        for (ItemCounter ic : player.getInventory()) {
            if (inv.length() > 0) inv.append("  ");
            inv.append(ic.getItem().getName())
                    .append("×")
                    .append(ic.getCount());
        }

        gc.fillText(inv.isEmpty() ? "(none)" : inv.toString(),
                W / 2.0, H * 0.50 + 96);

        gc.setTextAlign(TextAlignment.LEFT);
    }

    private Button makeBtn(String text, String bg, String hover) {
        Button b = new Button(text);
        b.setPrefWidth(130);
        b.setPrefHeight(40);

        String s = "-fx-background-color:" + bg + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-weight:bold;" +
                "-fx-font-size:14px;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;";

        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(s.replace(bg, hover)));
        b.setOnMouseExited(e -> b.setStyle(s));

        return b;
    }
}
package application;

import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import logic.creatures.Player;
import logic.pickaxe.Pickaxe;

import java.util.Random;

public class MainMenuScene {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;

    private final double[] starX = new double[120];
    private final double[] starY = new double[120];
    private final double[] starSpd = new double[120];  // falling speed
    private final double[] starR = new double[120];  // radius

    private final Image logo;

    public MainMenuScene() {
        Random rng = new Random();
        for (int i = 0; i < 120; i++) {
            starX[i] = rng.nextDouble() * W;
            starY[i] = rng.nextDouble() * H;
            starSpd[i] = 0.3 + rng.nextDouble() * 0.8;
            starR[i] = 1 + rng.nextDouble() * 2;
        }

        Image loaded = null;
        try {
            var stream = getClass().getResourceAsStream("/images/logo.png");
            if (stream != null) loaded = new Image(stream);
        } catch (Exception e) {
            System.out.println("Logo not found — skipping logo draw.");
        }
        this.logo = loaded;
    }

    public Scene build() {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Button startBtn = makeButton("PLAY GAME", "#43a047", "#66bb6a");
        Button quitBtn = makeButton("QUIT", "#c62828", "#ef5350");

        startBtn.setOnAction(e -> {
            Player player = new Player(100, 20, 10);
            player.setGold(999999);

            player.getInventory().add(new logic.util.ItemCounter(new logic.stone.NormalStone(), 30));
            player.getInventory().add(new logic.util.ItemCounter(new logic.stone.HardStone(), 20));
            player.getInventory().add(new logic.util.ItemCounter(new logic.stone.Iron(), 20));
            player.getInventory().add(new logic.util.ItemCounter(new logic.stone.Platinum(), 15));
            player.getInventory().add(new logic.util.ItemCounter(new logic.stone.Mithril(), 15));

            Pickaxe[] pickaxeHolder = {Pickaxe.createIronPickaxe()};
            Main.sceneManager.showGame(player, pickaxeHolder[0]);
        });
        quitBtn.setOnAction(e -> System.exit(0));

        VBox buttons = new VBox(16, startBtn, quitBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setLayoutX(W / 2.0 - 120);
        buttons.setLayoutY(H * 0.62);

        Pane root = new Pane(canvas, buttons);
        Scene scene = new Scene(root, W, H);

        new AnimationTimer() {
            long lastTime = 0;
            double titleBob = 0;

            @Override
            public void handle(long now) {
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                titleBob += dt * 1.8;

                for (int i = 0; i < 120; i++) {
                    starY[i] += starSpd[i];
                    if (starY[i] > H) {
                        starY[i] = 0;
                        starX[i] = Math.random() * W;
                    }
                }

                drawBackground(gc, titleBob);
            }
        }.start();

        return scene;
    }

    private void drawBackground(GraphicsContext gc, double titleBob) {
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0a1a")),
                new Stop(1, Color.web("#1a0a2e")));
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);

        for (int i = 0; i < 120; i++) {
            double alpha = 0.4 + starR[i] * 0.2;
            gc.setFill(Color.rgb(255, 255, 255, Math.min(1.0, alpha)));
            gc.fillOval(starX[i], starY[i], starR[i], starR[i]);
        }

        gc.setFill(Color.web("#0d1b2a"));
        double[] mx = {0, 80, 180, 280, 380, 450, 560, 650, 760, 860, 960, 960, 0};
        double[] my = {H, 520, 460, 510, 430, 480, 390, 450, 410, 470, 440, H, H};
        gc.fillPolygon(mx, my, mx.length);

        gc.setFill(Color.web("#050d14"));
        gc.fillRect(0, H - 120, W, 120);

        drawTorch(gc, W * 0.25, H - 115, titleBob);
        drawTorch(gc, W * 0.75, H - 115, titleBob + 1.3);

        double bobY = Math.sin(titleBob) * 6;

        drawLogo(gc, bobY);

        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 52));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int d = 8; d >= 1; d--) {
            gc.setFill(Color.rgb(255, 160, 0, 0.04 * d));
            gc.fillText("Tanjiro: The Swordsmith", W / 2.0 + d, H * 0.38 + bobY + d);
        }

        gc.setFill(Color.web("#3a1a00"));
        gc.fillText("Tanjiro: The Swordsmith", W / 2.0 + 4, H * 0.38 + bobY + 4);

        LinearGradient titleGrad = new LinearGradient(0, 0.2, 0, 0.8, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ffd700")),
                new Stop(0.5, Color.web("#ff8c00")),
                new Stop(1, Color.web("#ffd700")));
        gc.setFill(titleGrad);
        gc.fillText("Tanjiro: The Swordsmith", W / 2.0, H * 0.38 + bobY);

        gc.setFont(Font.font("Georgia", 22));
        gc.setFill(Color.rgb(180, 210, 255, 0.85));
        gc.fillText("Mine, Craft, Fight, Survive", W / 2.0, H * 0.47 + bobY * 0.5);

        gc.setFont(Font.font("Arial", 13));
        gc.setFill(Color.rgb(140, 160, 180, 0.7));
        gc.fillText("WASD: Move   |   E: Inventory   |   LMB: Attack   |   RMB: Mine   ", W / 2.0, H * 0.90);
    }

    private void drawLogo(GraphicsContext gc, double bobY) {
        if (logo == null || logo.isError()) return;

        double logoSize = 175;
        double cx = W / 2.0;
        double cy = H * 0.15 + bobY;

        gc.setFill(Color.rgb(255, 200, 50, 0.18));
        gc.fillOval(cx - logoSize / 2.0 - 12, cy - logoSize / 2.0 - 12,
                logoSize + 24, logoSize + 24);

        gc.save();
        gc.beginPath();
        gc.arc(cx, cy, logoSize / 2.0, logoSize / 2.0, 0, 360);
        gc.closePath();
        gc.clip();

        gc.drawImage(logo, cx - logoSize / 2.0, cy - logoSize / 2.0, logoSize, logoSize);

        gc.restore();

        gc.setStroke(Color.web("#ffd700"));
        gc.setLineWidth(3);
        gc.strokeOval(cx - logoSize / 2.0, cy - logoSize / 2.0, logoSize, logoSize);
        gc.setLineWidth(1);
    }

    private void drawTorch(GraphicsContext gc, double x, double y, double t) {
        gc.setFill(Color.web("#5d3a1a"));
        gc.fillRoundRect(x - 4, y, 8, 30, 4, 4);

        double flicker = Math.sin(t * 6) * 3;
        gc.setFill(Color.rgb(255, 80, 0, 0.9));
        gc.fillOval(x - 7 + flicker * 0.3, y - 18, 14, 18);
        gc.setFill(Color.rgb(255, 200, 50, 0.85));
        gc.fillOval(x - 5 + flicker * 0.5, y - 14, 10, 13);
        gc.setFill(Color.rgb(255, 255, 200, 0.7));
        gc.fillOval(x - 3, y - 10, 6, 8);

        gc.setFill(Color.rgb(255, 140, 0, 0.08 + Math.abs(Math.sin(t * 3)) * 0.06));
        gc.fillOval(x - 40, y - 40, 80, 80);
    }

    private Button makeButton(String text, String colorHex, String hoverHex) {
        Button btn = new Button(text);
        btn.setPrefWidth(240);
        btn.setPrefHeight(48);
        String base = "-fx-background-color:" + colorHex + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-size:15px;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;";
        String hover = "-fx-background-color:" + hoverHex + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-size:15px;" +
                "-fx-font-weight:bold;" +
                "-fx-background-radius:8;" +
                "-fx-cursor:hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }
}
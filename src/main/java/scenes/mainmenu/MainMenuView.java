package scenes.mainmenu;

import application.Main;
import application.SceneManager;
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

/**
 * MainMenuView — the "face" of the main menu.
 * <p>
 * Responsibility: ONLY drawing and layout.
 * - Draws the animated star/mountain background
 * - Draws torches, logo, title text, and subtitle
 * - Creates Play / Quit buttons and navigates on click
 * - Runs the AnimationTimer (calls controller.update() each frame)
 */
public class MainMenuView {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;

    private final MainMenuController controller;

    // Logo image loaded once; may be null if file is missing
    private final Image logo;

    public MainMenuView(MainMenuController controller) {
        this.controller = controller;

        // Try to load logo — gracefully skip if missing
        Image loaded = null;
        try {
            var stream = getClass().getResourceAsStream("/images/logo.png");
            if (stream != null) loaded = new Image(stream);
        } catch (Exception e) {
            System.out.println("Logo not found — skipping logo draw.");
        }
        this.logo = loaded;
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    /**
     * Builds and returns the complete main-menu Scene.
     */
    public Scene build() {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // ── Buttons ─────────────────────────────────────────────────────────
        Button startBtn = makeButton("PLAY GAME", "#43a047", "#66bb6a");
        Button quitBtn = makeButton("QUIT", "#c62828", "#ef5350");

        startBtn.setOnAction(e -> startGame());
        quitBtn.setOnAction(e -> System.exit(0));

        VBox buttons = new VBox(16, startBtn, quitBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setLayoutX(W / 2.0 - 120);
        buttons.setLayoutY(H * 0.65);

        Pane root = new Pane(canvas, buttons);
        Scene scene = new Scene(root, W, H);

        // ── Animation loop ───────────────────────────────────────────────────
        new AnimationTimer() {
            long lastTime = 0;
            double titleBob = 0; // time value fed to Math.sin for bobbing

            @Override
            public void handle(long now) {
                // dt: time since last frame in seconds
                double dt = lastTime == 0 ? 0 : (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                titleBob += dt * 1.8;

                controller.update(dt);    // controller advances star positions
                drawBackground(gc, titleBob); // view draws everything
            }
        }.start();

        return scene;
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    /**
     * Creates a fresh player with starter inventory and starts the game.
     */
    private void startGame() {
        Player player = new Player(100, 20, 10);
        player.setGold(200);

        Pickaxe startPickaxe = Pickaxe.createWoodenPickaxe();
        Main.sceneManager.showGame(player, startPickaxe);
    }

    // ── Drawing ───────────────────────────────────────────────────────────────

    /**
     * Draws the entire background each frame.
     */
    private void drawBackground(GraphicsContext gc, double titleBob) {
        // Sky gradient
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0a1a")),
                new Stop(1, Color.web("#1a0a2e")));
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);

        // Falling stars — positions come from controller
        double[] sx = controller.getStarX();
        double[] sy = controller.getStarY();
        double[] sr = controller.getStarR();
        for (int i = 0; i < controller.getStarCount(); i++) {
            double alpha = Math.min(1.0, 0.4 + sr[i] * 0.2);
            gc.setFill(Color.rgb(255, 255, 255, alpha));
            gc.fillOval(sx[i], sy[i], sr[i], sr[i]);
        }

        // Mountain silhouette
        gc.setFill(Color.web("#0d1b2a"));
        double[] mx = {0, 80, 180, 280, 380, 450, 560, 650, 760, 860, 960, 960, 0};
        double[] my = {H, 520, 460, 510, 430, 480, 390, 450, 410, 470, 440, H, H};
        gc.fillPolygon(mx, my, mx.length);

        // Ground strip at bottom
        gc.setFill(Color.web("#050d14"));
        gc.fillRect(0, H - 120, W, 120);

        // Torches (animated flicker)
        drawTorch(gc, W * 0.25, H - 115, titleBob);
        drawTorch(gc, W * 0.75, H - 115, titleBob + 1.3); // offset so they don't sync

        // Logo
        double bobY = Math.sin(titleBob) * 6;
        drawLogo(gc, bobY);

        // Title text with glow shadow
        double baseX = W / 2.0;
        double baseY = H * 0.38 + bobY;

        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 52));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int d = 8; d >= 1; d--) {
            gc.setFill(Color.rgb(255, 160, 0, 0.04 * d));
            gc.fillText("Tanjiro: The Swordsmith", baseX + d, baseY + bobY + d);
        }
        // Dark shadow first, then golden gradient on top
        gc.setFill(Color.web("#3a1a00"));
        gc.fillText("Tanjiro: The Swordsmith", baseX + 4, baseY + bobY + 4);
        LinearGradient titleGrad = new LinearGradient(0, 0.2, 0, 0.8, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ffd700")),
                new Stop(0.5, Color.web("#ff8c00")),
                new Stop(1, Color.web("#ffd700")));
        gc.setFill(titleGrad);
        gc.fillText("Tanjiro: The Swordsmith", baseX, baseY + bobY);

        // Subtitle
        gc.setFont(Font.font("Georgia", 22));
        gc.setFill(Color.rgb(180, 210, 255, 0.85));
        double subtitleY = H * 0.46 + bobY * 0.5;
        gc.fillText("Mine, Craft, Fight, Survive", baseX, subtitleY);

        double startY = (H * 0.46 + bobY * 0.5) + 40; // Starts 40 pixels below the subtitle
        int lineGap = 25; // Vertical distance between each name

        // Draw 4 separate lines of text
        gc.setFont(Font.font("Georgia", 16)); // Slightly smaller font for credits
        gc.setFill(Color.LIGHTGRAY);          // A subtle color so it doesn't distract

        gc.fillText("6833022221", baseX, startY);
        gc.fillText("6833103421", baseX, startY + lineGap);
        gc.fillText("6833009121", baseX, startY + (lineGap * 2));
        gc.fillText("6833029721 Jiraphat Namvong", baseX, startY + (lineGap * 3));

        // Controls hint at the bottom
        gc.setFont(Font.font("Arial", 13));
        gc.setFill(Color.rgb(140, 160, 180, 0.7));
        gc.fillText("WASD: Move   |   E: Inventory   |   LMB: Attack   |   RMB: Mine",
                W / 2.0, H * 0.90);
    }

    /**
     * Draws the circular logo with golden border and glow.
     */
    private void drawLogo(GraphicsContext gc, double bobY) {
        if (logo == null || logo.isError()) return;

        double logoSize = 250;
        double cx = W / 2.0;
        double cy = H * 0.17 + bobY;

        gc.save();
        gc.beginPath();
        gc.arc(cx, cy, logoSize / 2.0, logoSize / 2.0, 0, 360);
        gc.closePath();
        gc.clip();
        gc.drawImage(logo, cx - logoSize / 2.0, cy - logoSize / 2.0, logoSize, logoSize);
        gc.restore();
    }

    /**
     * Draws a flickering torch at (x, y). `t` drives the flicker animation.
     */
    private void drawTorch(GraphicsContext gc, double x, double y, double t) {
        // Wooden stick
        gc.setFill(Color.web("#5d3a1a"));
        gc.fillRoundRect(x - 4, y, 8, 30, 4, 4);

        // Flame layers (outer → inner, darker → brighter)
        double flicker = Math.sin(t * 6) * 3;
        gc.setFill(Color.rgb(255, 80, 0, 0.9));
        gc.fillOval(x - 7 + flicker * 0.3, y - 18, 14, 18);
        gc.setFill(Color.rgb(255, 200, 50, 0.85));
        gc.fillOval(x - 5 + flicker * 0.5, y - 14, 10, 13);
        gc.setFill(Color.rgb(255, 255, 200, 0.7));
        gc.fillOval(x - 3, y - 10, 6, 8);

        // Ambient glow circle (pulsing)
        gc.setFill(Color.rgb(255, 140, 0, 0.08 + Math.abs(Math.sin(t * 3)) * 0.06));
        gc.fillOval(x - 40, y - 40, 80, 80);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Button makeButton(String text, String colorHex, String hoverHex) {
        Button btn = new Button(text);
        btn.setPrefWidth(240);
        btn.setPrefHeight(48);
        String base = "-fx-background-color:" + colorHex + ";-fx-text-fill:white;"
                + "-fx-font-size:15px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-cursor:hand;";
        String hover = "-fx-background-color:" + hoverHex + ";-fx-text-fill:white;"
                + "-fx-font-size:15px;-fx-font-weight:bold;"
                + "-fx-background-radius:8;-fx-cursor:hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }
}

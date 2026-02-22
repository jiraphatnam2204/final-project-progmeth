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
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import logic.creatures.Player;
import logic.pickaxe.Pickaxe;

import java.util.Random;

/**
 * MainMenuScene: The first thing the player sees.
 * Has an animated starfield background, the game title, and a Start button.
 */
public class MainMenuScene {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;

    // Star data for the animated background
    private final double[] starX   = new double[120];
    private final double[] starY   = new double[120];
    private final double[] starSpd = new double[120];  // falling speed
    private final double[] starR   = new double[120];  // radius

    // ── Logo image ───────────────────────────────────────────────────
    // We load it ONCE here, not inside the animation loop.
    // Think of it like loading a photo into RAM once, then just displaying it every frame.
    private final Image logo;

    public MainMenuScene() {
        Random rng = new Random();
        for (int i = 0; i < 120; i++) {
            starX[i]   = rng.nextDouble() * W;
            starY[i]   = rng.nextDouble() * H;
            starSpd[i] = 0.3 + rng.nextDouble() * 0.8;
            starR[i]   = 1 + rng.nextDouble() * 2;
        }

        // Load the logo — falls back to null safely if the file is missing
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

        // --- Buttons overlay (JavaFX nodes on top of canvas) ---
        Button startBtn  = makeButton("PLAY GAME",    "#43a047", "#66bb6a");
        Button quitBtn   = makeButton("QUIT",        "#c62828", "#ef5350");

        startBtn.setOnAction(e -> {
            // ── TEST MODE: generous starting resources ──
            Player player = new Player(99999, 20, 9999);
            player.setGold(999999);

            player.getInventory().add(new logic.util.ItemCounter(new logic.stone.NormalStone(), 30));
            player.getInventory().add(new logic.util.ItemCounter(new logic.stone.HardStone(),   20));
            player.getInventory().add(new logic.util.ItemCounter(new logic.stone.Iron(),        20));
            player.getInventory().add(new logic.util.ItemCounter(new logic.stone.Platinum(),    15));
            player.getInventory().add(new logic.util.ItemCounter(new logic.stone.Mithril(),     15));

            Pickaxe[] pickaxeHolder = { Pickaxe.createIronPickaxe() };
            Main.sceneManager.showGame(player, pickaxeHolder[0]);
        });
        quitBtn.setOnAction(e -> System.exit(0));

        VBox buttons = new VBox(16, startBtn, quitBtn);
        buttons.setAlignment(Pos.CENTER);
        buttons.setLayoutX(W / 2.0 - 120);
        buttons.setLayoutY(H * 0.62);

        Pane root = new Pane(canvas, buttons);
        Scene scene = new Scene(root, W, H);

        // --- Animation loop ---
        new AnimationTimer() {
            long lastTime = 0;
            double titleBob = 0;

            @Override
            public void handle(long now) {
                double dt = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;
                titleBob += dt * 1.8;

                // Move stars downward
                for (int i = 0; i < 120; i++) {
                    starY[i] += starSpd[i];
                    if (starY[i] > H) { starY[i] = 0; starX[i] = Math.random() * W; }
                }

                drawBackground(gc, titleBob);
            }
        }.start();

        return scene;
    }

    private void drawBackground(GraphicsContext gc, double titleBob) {
        // Deep space gradient background
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0a1a")),
                new Stop(1, Color.web("#1a0a2e")));
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);

        // Stars
        for (int i = 0; i < 120; i++) {
            double alpha = 0.4 + starR[i] * 0.2;
            gc.setFill(Color.rgb(255, 255, 255, Math.min(1.0, alpha)));
            gc.fillOval(starX[i], starY[i], starR[i], starR[i]);
        }

        // Ground silhouette (mountains)
        gc.setFill(Color.web("#0d1b2a"));
        double[] mx = {0,  80, 180, 280, 380, 450, 560, 650, 760, 860, 960, 960, 0};
        double[] my = {H, 520, 460, 510, 430, 480, 390, 450, 410, 470, 440,   H,  H};
        gc.fillPolygon(mx, my, mx.length);

        // Foreground dark strip
        gc.setFill(Color.web("#050d14"));
        gc.fillRect(0, H - 120, W, 120);

        // Torches (flickering orange glow)
        drawTorch(gc, W * 0.25, H - 115, titleBob);
        drawTorch(gc, W * 0.75, H - 115, titleBob + 1.3);

        // Title text with bob animation
        double bobY = Math.sin(titleBob) * 6;

        // ── Draw the logo above the title ────────────────────────────
        // The logo bobs up and down at the same rhythm as the title text.
        drawLogo(gc, bobY);

        // Outer glow
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 52));
        gc.setTextAlign(TextAlignment.CENTER);
        for (int d = 8; d >= 1; d--) {
            gc.setFill(Color.rgb(255, 160, 0, 0.04 * d));
            gc.fillText("Tanjiro: The Swordsmith", W / 2.0 + d, H * 0.38 + bobY + d);
        }

        // Title shadow
        gc.setFill(Color.web("#3a1a00"));
        gc.fillText("Tanjiro: The Swordsmith", W / 2.0 + 4, H * 0.38 + bobY + 4);

        // Title main
        LinearGradient titleGrad = new LinearGradient(0, 0.2, 0, 0.8, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#ffd700")),
                new Stop(0.5, Color.web("#ff8c00")),
                new Stop(1, Color.web("#ffd700")));
        gc.setFill(titleGrad);
        gc.fillText("Tanjiro: The Swordsmith", W / 2.0, H * 0.38 + bobY);

        // Subtitle
        gc.setFont(Font.font("Georgia", 22));
        gc.setFill(Color.rgb(180, 210, 255, 0.85));
        gc.fillText("Mine, Craft, Fight, Survive", W / 2.0, H * 0.47 + bobY * 0.5);

        // Instructions
        gc.setFont(Font.font("Arial", 13));
        gc.setFill(Color.rgb(140, 160, 180, 0.7));
        gc.fillText("WASD: Move   |   E: Inventory   |   LMB: Attack   |   RMB: Mine   ", W / 2.0, H * 0.90);
    }

    /**
     * Draws the circular logo centered above the title.
     *
     * How the circular clip works:
     *   1. gc.save()            → save the current drawing state (like saving your game)
     *   2. beginPath() + arc()  → trace an invisible circle path
     *   3. clip()               → anything drawn after this is ONLY visible inside that circle
     *   4. drawImage()          → draw the logo; only the circular region shows
     *   5. gc.restore()         → undo the clip so future drawing isn't affected
     *
     * After restoring, we draw the golden ring on top as decoration.
     */
    private void drawLogo(GraphicsContext gc, double bobY) {
        if (logo == null || logo.isError()) return; // skip if image failed to load

        double logoSize = 175;                        // diameter of the circle
        double cx = W / 2.0;                         // center X
        double cy = H * 0.15 + bobY;                 // center Y — sits above the title

        // ── Outer golden glow ring (drawn first so it appears behind the logo) ──
        gc.setFill(Color.rgb(255, 200, 50, 0.18));
        gc.fillOval(cx - logoSize / 2.0 - 12, cy - logoSize / 2.0 - 12,
                logoSize + 24, logoSize + 24);

        // ── Clip to a circle, then draw the image inside it ──────────
        gc.save();                            // remember current drawing state
        gc.beginPath();
        gc.arc(cx, cy, logoSize / 2.0, logoSize / 2.0, 0, 360); // trace the circle
        gc.closePath();
        gc.clip();                            // restrict future drawing to this circle

        // Draw the logo image centred on (cx, cy)
        gc.drawImage(logo,
                cx - logoSize / 2.0,          // top-left X
                cy - logoSize / 2.0,          // top-left Y
                logoSize, logoSize);           // width, height

        gc.restore();                         // remove the clip — back to normal drawing

        // ── Golden decorative ring drawn on top of the logo ──────────
        gc.setStroke(Color.web("#ffd700"));
        gc.setLineWidth(3);
        gc.strokeOval(cx - logoSize / 2.0, cy - logoSize / 2.0, logoSize, logoSize);
        gc.setLineWidth(1); // reset line width so other shapes aren't affected
    }

    private void drawTorch(GraphicsContext gc, double x, double y, double t) {
        // Handle
        gc.setFill(Color.web("#5d3a1a"));
        gc.fillRoundRect(x - 4, y, 8, 30, 4, 4);

        // Flame (flickers with sine wave)
        double flicker = Math.sin(t * 6) * 3;
        gc.setFill(Color.rgb(255, 80, 0, 0.9));
        gc.fillOval(x - 7 + flicker * 0.3, y - 18, 14, 18);
        gc.setFill(Color.rgb(255, 200, 50, 0.85));
        gc.fillOval(x - 5 + flicker * 0.5, y - 14, 10, 13);
        gc.setFill(Color.rgb(255, 255, 200, 0.7));
        gc.fillOval(x - 3, y - 10, 6, 8);

        // Glow
        gc.setFill(Color.rgb(255, 140, 0, 0.08 + Math.abs(Math.sin(t * 3)) * 0.06));
        gc.fillOval(x - 40, y - 40, 80, 80);
    }

    private Button makeButton(String text, String colorHex, String hoverHex) {
        Button btn = new Button(text);
        btn.setPrefWidth(240);
        btn.setPrefHeight(48);
        String base  = "-fx-background-color:" + colorHex + ";" +
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
        btn.setOnMouseExited(e  -> btn.setStyle(base));
        return btn;
    }
}
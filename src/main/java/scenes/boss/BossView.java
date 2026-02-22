package scenes.boss;

import application.Main;
import application.SceneManager;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import logic.creatures.Player;
import logic.pickaxe.Pickaxe;

import java.util.Objects;

/**
 * BossView — the "face" of the boss battle screen.
 * <p>
 * Responsibility: ALL JavaFX drawing and button management.
 * - Loads and draws boss/player sprite images
 * - Manages visual animation state (shake offsets, attack flash, animTime)
 * - Runs the AnimationTimer; calls BossController for logic at the right times
 * - Updates button visibility/enabled state based on controller results
 */
public class BossView {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;
    private static final long ANIM_DURATION = 600; // ms for attack flash

    private final BossController controller;
    private final Pickaxe[] pickaxeHolder;

    // ── Sprites ───────────────────────────────────────────────────────────────
    private Image imgBoss1, imgBoss2, imgBoss3;
    private Image imgPlayerIdle, imgPlayerAttack;

    // ── Visual-only animation state (no game logic here) ─────────────────────
    private double animTime = 0;    // continuously increases each frame
    private boolean showAttackAnim = false;
    private long attackAnimEndMs = 0;
    private double bossShakeX = 0;
    private long lastShakeTime = 0;
    private double playerShakeX = 0;
    private long lastPlayerShake = 0;

    // ── Buttons ───────────────────────────────────────────────────────────────
    private Button attackBtn, healBtn, fleeBtn, nextBtn;

    public BossView(BossController controller, Pickaxe[] pickaxeHolder) {
        this.controller = controller;
        this.pickaxeHolder = pickaxeHolder;
        loadImages();
    }

    // ── Image loading ─────────────────────────────────────────────────────────

    private void loadImages() {
        imgBoss1 = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/images/Akaza.png")));
        imgBoss2 = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/images/Kokushibo.png")));
        imgBoss3 = new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/images/Muzan.png")));
        imgPlayerIdle = loadImage("/images/player_idle.png");
        imgPlayerAttack = loadImage("/images/player_slash_right.png");
    }

    private Image loadImage(String path) {
        try {
            var s = getClass().getResourceAsStream(path);
            return s == null ? null : new Image(s);
        } catch (Exception e) {
            System.out.println("Could not load image: " + path);
            return null;
        }
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    /**
     * Builds and returns the complete boss-battle Scene.
     */
    public Scene build() {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);

        // ── Buttons ─────────────────────────────────────────────────────────
        attackBtn = makeBtn("⚔  Attack", "#c62828", "#ef5350");
        healBtn = makeBtn("💊  Heal", "#1b5e20", "#388e3c");
        fleeBtn = makeBtn("🏃  Flee", "#4a148c", "#7b1fa2");
        nextBtn = makeBtn("➜  Next Boss", "#e65100", "#f4511e");

        attackBtn.setLayoutX(W / 2.0 - 230);
        attackBtn.setLayoutY(H - 90);
        healBtn.setLayoutX(W / 2.0 - 70);
        healBtn.setLayoutY(H - 90);
        fleeBtn.setLayoutX(W / 2.0 + 90);
        fleeBtn.setLayoutY(H - 90);
        nextBtn.setLayoutX(W / 2.0 - 70);
        nextBtn.setLayoutY(H - 90);
        nextBtn.setVisible(false);

        // Button actions delegate to controller; View then reacts to ActionResult
        attackBtn.setOnAction(e -> handleAttack());
        healBtn.setOnAction(e -> handleHeal());
        fleeBtn.setOnAction(e -> Main.sceneManager.showGame(
                controller.getPlayer(), pickaxeHolder[0]));
        nextBtn.setOnAction(e -> handleNextBoss());

        root.getChildren().addAll(attackBtn, healBtn, fleeBtn, nextBtn);

        // ── Animation loop ───────────────────────────────────────────────────
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                animTime += 1.0 / 60;
                updateVisualState();

                // Trigger enemy turn after 900 ms delay (gives player time to read log)
                if (controller.getState() == BossController.BattleState.ENEMY_TURN
                        && System.currentTimeMillis() - controller.getLastEnemyActionMs() > 900) {
                    handleEnemyTurn();
                }

                render(gc);
            }
        }.start();

        return new Scene(root, W, H);
    }

    // ── Button handlers ───────────────────────────────────────────────────────

    private void handleAttack() {
        BossController.ActionResult result = controller.doPlayerAttack();

        // Consume controller's animation flags
        if (controller.isPendingAttackAnim()) {
            showAttackAnim = true;
            attackAnimEndMs = System.currentTimeMillis() + ANIM_DURATION;
            controller.clearAttackAnimFlag();
        }
        if (controller.isPendingBossShake()) {
            bossShakeX = 8;
            lastShakeTime = System.currentTimeMillis();
            controller.clearBossShakeFlag();
        }

        applyResult(result);
    }

    private void handleHeal() {
        BossController.ActionResult result = controller.doPlayerHeal();
        applyResult(result);
    }

    private void handleEnemyTurn() {
        BossController.ActionResult result = controller.doEnemyTurn();

        if (controller.isPendingPlayerShake()) {
            playerShakeX = 10;
            lastPlayerShake = System.currentTimeMillis();
            controller.clearPlayerShakeFlag();
        }

        applyResult(result);
    }

    private void handleNextBoss() {
        if (controller.hasNextBoss()) {
            controller.advanceToNextBoss();
            // Reset buttons to normal combat state
            nextBtn.setVisible(false);
            attackBtn.setVisible(true);
            healBtn.setVisible(true);
            fleeBtn.setVisible(true);
            setButtonsEnabled(true);
        } else {
            Main.sceneManager.showGameOver(true, controller.getPlayer());
        }
    }

    /**
     * Translates a controller ActionResult into button state changes.
     * This is the key split: controller says WHAT happened, view decides HOW to show it.
     */
    private void applyResult(BossController.ActionResult result) {
        switch (result) {
            case ENEMY_TURN -> setButtonsEnabled(false);
            case PLAYER_TURN -> setButtonsEnabled(true);
            case BOSS_DEFEATED -> showVictoryButtons();
            case ALL_CLEAR -> {
                showVictoryButtons();
                nextBtn.setText("🏆 Victory!");
            }
            case PLAYER_DEFEATED -> {
                setButtonsEnabled(false);
                fleeBtn.setText("☠  Game Over");
                fleeBtn.setVisible(true);
                fleeBtn.setOnAction(e ->
                        Main.sceneManager.showGameOver(false, controller.getPlayer()));
            }
            default -> {
            } // NONE — do nothing
        }
    }

    // ── Visual state updates ──────────────────────────────────────────────────

    private void updateVisualState() {
        // Clear shakes after 80 ms
        if (System.currentTimeMillis() - lastShakeTime > 80) bossShakeX = 0;
        if (System.currentTimeMillis() - lastPlayerShake > 80) playerShakeX = 0;

        // Clear attack animation after its duration
        if (showAttackAnim && System.currentTimeMillis() > attackAnimEndMs)
            showAttackAnim = false;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void render(GraphicsContext gc) {
        Color bossColor = controller.getBossColor();

        // Animated background gradient — centre colour tinted by boss colour
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0015")),
                new Stop(0.5, bossColor.deriveColor(0, 0.3, 0.2, 1)),
                new Stop(1, Color.web("#0a0015")));
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);

        // Pulsing aura behind boss
        gc.setFill(bossColor.deriveColor(0, 0.5, 0.3, 0.06));
        double pulse = 1 + Math.sin(animTime * 1.5) * 0.04;
        gc.fillOval(W * 0.6 - 150 * pulse, H * 0.25 - 150 * pulse, 300 * pulse, 300 * pulse);

        // Arena floor
        gc.setFill(Color.web("#1a0a0a", 0.6));
        gc.fillRect(0, H * 0.55, W, H * 0.45);
        gc.setStroke(Color.web("#3a1a1a", 0.5));
        gc.setLineWidth(1);
        for (int y = 0; y < H; y += 60) gc.strokeLine(0, y, W, y);

        drawBoss(gc);
        drawPlayerChar(gc);
        drawBossHPBar(gc);
        drawPlayerHPBar(gc);
        drawLog(gc);
        drawTurnIndicator(gc);
    }

    private void drawBoss(GraphicsContext gc) {
        Color bossColor = controller.getBossColor();
        double bx = W * 0.62 + bossShakeX;
        double by = H * 0.12;
        double scale = 1 + Math.sin(animTime * 1.2) * 0.015;

        gc.save();
        gc.translate(bx + 100, by + 140);
        gc.scale(scale, scale);
        gc.translate(-100, -140);

        gc.setFill(bossColor.deriveColor(0, 1, 1, 0.15));
        gc.fillOval(-30, -30, 390, 390);

        Image img = switch (controller.getBossIndex()) {
            case 0 -> imgBoss1;
            case 1 -> imgBoss2;
            default -> imgBoss3;
        };

        double alpha = controller.getCurrentBoss().isAlive() ? 1.0 : 0.3;
        if (img != null && !img.isError()) {
            gc.setGlobalAlpha(alpha);
            gc.drawImage(img, 0, 0, 320, 320);
            gc.setGlobalAlpha(1.0);
        }
        gc.restore();

        // Name plate
        double npX = W * 0.62 + 160 - 120;
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRoundRect(npX, H * 0.08, 240, 30, 8, 8);
        gc.setFill(bossColor);
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(controller.getBossName(), npX + 120, H * 0.08 + 21);
    }

    private void drawPlayerChar(GraphicsContext gc) {
        double spriteW = 240, spriteH = 320;
        double px = W * 0.06 + playerShakeX;
        double py = H * 0.12;

        // Highlight glow when it's player's turn
        if (controller.getState() == BossController.BattleState.PLAYER_TURN) {
            gc.setFill(Color.rgb(100, 150, 255, 0.12));
            gc.fillOval(px - 30, py - 20, spriteW + 60, spriteH + 40);
        }

        // Drop shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.25));
        gc.fillOval(px + spriteW * 0.15, py + spriteH - 10, spriteW * 0.7, 20);

        Image sprite = (showAttackAnim && imgPlayerAttack != null && !imgPlayerAttack.isError())
                ? imgPlayerAttack : imgPlayerIdle;

        if (sprite != null && !sprite.isError()) {
            gc.drawImage(sprite, px, py, spriteW, spriteH);

            // "YOU" name plate above player
            double plateW = 80, plateH = 30;
            double plateX = px + spriteW / 2.0 - plateW / 2.0;
            double plateY = py - 38;
            gc.setFill(Color.rgb(0, 0, 0, 0.7));
            gc.fillRoundRect(plateX, plateY, plateW, plateH, 8, 8);
            gc.setFill(Color.web("#80cbc4"));
            gc.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("YOU", px + spriteW / 2.0, plateY + 21);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    private void drawBossHPBar(GraphicsContext gc) {
        var boss = controller.getCurrentBoss();
        double bx = W * 0.55;
        double pct = boss.isAlive()
                ? (double) boss.getHealthPoint() / boss.getMaxHealthPoint() : 0;

        gc.setFill(Color.rgb(0, 0, 0, 0.65));
        gc.fillRoundRect(bx, H * 0.60, 350, 22, 6, 6);
        gc.setFill(pct > 0.5 ? Color.web("#ef5350")
                : pct > 0.25 ? Color.ORANGE
                : Color.web("#b71c1c"));
        gc.fillRoundRect(bx, H * 0.60, 350 * pct, 22, 6, 6);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(controller.getBossName() + ": "
                        + boss.getHealthPoint() + "/" + boss.getMaxHealthPoint(),
                bx + 175, H * 0.60 + 15);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawPlayerHPBar(GraphicsContext gc) {
        Player player = controller.getPlayer();
        double pct = (double) player.getHealth() / player.getMaxHealth();

        gc.setFill(Color.rgb(0, 0, 0, 0.65));
        gc.fillRoundRect(30, H * 0.60, 280, 22, 6, 6);
        gc.setFill(pct > 0.5 ? Color.web("#43a047")
                : pct > 0.25 ? Color.ORANGE
                : Color.web("#c62828"));
        gc.fillRoundRect(30, H * 0.60, 280 * pct, 22, 6, 6);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("HP: " + player.getHealth() + "/" + player.getMaxHealth()
                        + " | ATK:" + player.getAttack() + " DEF:" + player.getDefense(),
                170, H * 0.60 + 15);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawLog(GraphicsContext gc) {
        var log = controller.getLog();
        double lx = 20, ly = H * 0.67, lw = W - 40, lh = H * 0.21;

        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRoundRect(lx, ly, lw, lh, 10, 10);
        gc.setStroke(Color.web("#333"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(lx, ly, lw, lh, 10, 10);

        for (int i = 0; i < log.size(); i++) {
            // Older lines fade out; the latest line is bright and bold
            double a = 0.4 + 0.6 * ((i + 1.0) / log.size());
            boolean isLatest = (i == log.size() - 1);
            Color c = isLatest
                    ? Color.web("#fff9c4")
                    : Color.rgb(180, 200, 220, (int) (a * 255) / 255.0);
            gc.setFill(c);
            gc.setFont(Font.font("Arial",
                    isLatest ? FontWeight.BOLD : FontWeight.NORMAL, 12));
            gc.fillText(log.get(i), lx + 12, ly + 18 + i * 17);
        }
    }

    private void drawTurnIndicator(GraphicsContext gc) {
        String indicator = switch (controller.getState()) {
            case PLAYER_TURN -> "⚡ YOUR TURN";
            case ENEMY_TURN -> "⏳ Enemy acting...";
            case VICTORY -> "🏆 BOSS DEFEATED!";
            case DEFEAT -> "💀 DEFEATED";
            case ALL_CLEAR -> "🎉 ALL BOSSES CLEARED!";
        };
        Color col = switch (controller.getState()) {
            case PLAYER_TURN -> Color.web("#fff176");
            case ENEMY_TURN, DEFEAT -> Color.web("#ef9a9a");
            case VICTORY, ALL_CLEAR -> Color.web("#a5d6a7");
        };

        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRoundRect(W / 2.0 - 100, H * 0.62, 200, 22, 8, 8);
        gc.setFill(col);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(indicator, W / 2.0, H * 0.62 + 15);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // ── Button helpers ────────────────────────────────────────────────────────

    private void setButtonsEnabled(boolean on) {
        attackBtn.setDisable(!on);
        healBtn.setDisable(!on);
    }

    private void showVictoryButtons() {
        attackBtn.setVisible(false);
        healBtn.setVisible(false);
        fleeBtn.setVisible(false);
        nextBtn.setVisible(true);
    }

    private Button makeBtn(String text, String bg, String hover) {
        Button b = new Button(text);
        b.setPrefWidth(140);
        b.setPrefHeight(36);
        String s = "-fx-background-color:" + bg + ";-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-font-size:13px;"
                + "-fx-background-radius:7;-fx-cursor:hand;";
        String h = s.replace(bg, hover);
        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(h));
        b.setOnMouseExited(e -> b.setStyle(s));
        return b;
    }
}

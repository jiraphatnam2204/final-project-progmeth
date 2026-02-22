package application;

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
import logic.creatures.*;
import logic.pickaxe.Pickaxe;
import logic.util.ItemCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BossScene {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;
    private static final long ANIM_DURATION = 600; // ms
    private final Player player;
    private final Pickaxe[] pickaxeHolder;
    private final BossInfo[] bosses = {
            new BossInfo("Akaza", new EasyBoss(400,40,10,100), Color.web("#64B5F6")),
            new BossInfo("Kokushibo", new MediumBoss(400,40,10,10), Color.web("#ce93d8")),
            new BossInfo("Muzan", new HardBoss(400,40,10,10), Color.web("#ef5350")),
    };
    private final List<String> log = new ArrayList<>();
    private int bossIndex = 0;
    private Monster currentBoss;
    private String bossName;
    private Color bossColor;
    private String bossEmoji;
    private Image imgBoss1, imgBoss2, imgBoss3;
    private Image imgPlayerIdle;
    private Image imgPlayerAttack;
    private boolean showAttackAnim = false;
    private long attackAnimEndMs = 0;
    private BattleState state = BattleState.PLAYER_TURN;
    private long lastActionTime = 0;
    private double bossShakeX = 0;
    private long lastShakeTime = 0;
    private double playerShakeX = 0;
    private long lastPlayerShake = 0;
    private double animTime = 0;
    // ── Buttons ──────────────────────────────────────────────────────
    private Button attackBtn, healBtn, fleeBtn, nextBtn;

    public BossScene(Player player, Pickaxe[] pickaxeHolder) {
        this.player = player;
        this.pickaxeHolder = pickaxeHolder;
        imgBoss1 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Akaza.png")));
        imgBoss2 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Kokushibo.png")));
        imgBoss3 = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Muzan.png")));

        imgPlayerIdle = loadImage("/images/player_idle.png");
        imgPlayerAttack = loadImage("/images/player_slash_right.png");
        loadBoss(0);
    }

    private Image loadImage(String path) {
        try {
            var stream = getClass().getResourceAsStream(path);
            if (stream == null) return null;
            return new Image(stream);
        } catch (Exception e) {
            System.out.println("Could not load image: " + path);
            return null;
        }
    }

    private void loadBoss(int index) {
        bossIndex = index;
        BossInfo bi = bosses[index];
        currentBoss = bi.monster();
        bossName = bi.name();
        bossColor = bi.color();
        log.clear();
        log.add("⚔ A wild " + bossName + " appears!");
        log.add("Your HP: " + player.getHealth() + "/" + player.getMaxHealth()
                + "  ATK: " + player.getAttack() + "  DEF: " + player.getDefense());
    }

    public Scene build() {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);

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

        attackBtn.setOnAction(e -> doPlayerAttack());
        healBtn.setOnAction(e -> doPlayerHeal());
        fleeBtn.setOnAction(e -> Main.sceneManager.showGame(player, pickaxeHolder[0]));
        nextBtn.setOnAction(e -> {
            if (bossIndex + 1 < bosses.length) {
                loadBoss(bossIndex + 1);
                state = BattleState.PLAYER_TURN;
                nextBtn.setVisible(false);
                attackBtn.setVisible(true);
                healBtn.setVisible(true);
                fleeBtn.setVisible(true);
            } else {
                Main.sceneManager.showGameOver(true, player);
            }
        });

        root.getChildren().addAll(attackBtn, healBtn, fleeBtn, nextBtn);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                animTime += 1.0 / 60;

                if (System.currentTimeMillis() - lastShakeTime > 80) bossShakeX = 0;
                if (System.currentTimeMillis() - lastPlayerShake > 80) playerShakeX = 0;

                if (showAttackAnim && System.currentTimeMillis() > attackAnimEndMs) {
                    showAttackAnim = false;
                }

                if (state == BattleState.ENEMY_TURN
                        && System.currentTimeMillis() - lastActionTime > 900) {
                    doEnemyTurn();
                }

                render(gc);
            }
        }.start();

        return new Scene(root, W, H);
    }

    private void doPlayerAttack() {
        if (state != BattleState.PLAYER_TURN) return;

        player.attack(currentBoss);
        int dmg = Math.max(1, player.getAttack() - currentBoss.getDefense());
        log.add("You hit " + bossName + " for " + dmg + " dmg!");

        showAttackAnim = true;
        attackAnimEndMs = System.currentTimeMillis() + ANIM_DURATION;

        bossShakeX = 8;
        lastShakeTime = System.currentTimeMillis();

        if (!currentBoss.isAlive()) {
            int gold = currentBoss.dropMoney() * 3;
            player.setGold(player.getGold() + gold);
            log.add("★ " + bossName + " defeated! +" + gold + "g");
            if (bossIndex + 1 < bosses.length) {
                state = BattleState.VICTORY;
                log.add("Press 'Next Boss' to continue!");
                setButtonsForVictory();
            } else {
                state = BattleState.ALL_CLEAR;
                log.add("🎉 ALL BOSSES DEFEATED! YOU WIN!");
                setButtonsForVictory();
                nextBtn.setText("🏆 Victory!");
            }
        } else {
            log.add(bossName + " HP: " + currentBoss.getHealthPoint() + "/" + currentBoss.getMaxHealthPoint());
            state = BattleState.ENEMY_TURN;
            lastActionTime = System.currentTimeMillis();
            setButtonsEnabled(false);
        }
        trimLog();
    }

    private void doPlayerHeal() {
        if (state != BattleState.PLAYER_TURN) return;
        for (ItemCounter ic : player.getInventory()) {
            if (ic.getItem() instanceof logic.base.BasePotion pot) {
                pot.consume(player);
                ic.setCount(ic.getCount() - 1);
                if (ic.getCount() <= 0) player.getInventory().remove(ic);
                log.add("You used " + ic.getItem().getName() + "! HP: " + player.getHealth() + "/" + player.getMaxHealth());
                state = BattleState.ENEMY_TURN;
                lastActionTime = System.currentTimeMillis();
                setButtonsEnabled(false);
                trimLog();
                return;
            }
        }
        int heal = Math.max(5, player.getMaxHealth() / 10);
        player.heal(heal);
        log.add("You rest briefly... +" + heal + " HP  (" + player.getHealth() + "/" + player.getMaxHealth() + ")");
        state = BattleState.ENEMY_TURN;
        lastActionTime = System.currentTimeMillis();
        setButtonsEnabled(false);
        trimLog();
    }

    private void doEnemyTurn() {
        if (state != BattleState.ENEMY_TURN) return;

        boolean crit = Math.random() < 0.20;
        int baseDmg = currentBoss.getAttack();
        if (crit) baseDmg = (int) (baseDmg * 1.6);

        currentBoss.attack(player);
        int actualDmg = Math.max(1, baseDmg - player.getDefense());
        String suffix = crit ? " 💥CRIT!" : "";
        log.add(bossName + " attacks you for " + actualDmg + " dmg!" + suffix);
        log.add("Your HP: " + player.getHealth() + "/" + player.getMaxHealth());

        playerShakeX = 10;
        lastPlayerShake = System.currentTimeMillis();

        if (!player.isAlive()) {
            state = BattleState.DEFEAT;
            log.add("💀 You have been defeated...");
            setButtonsEnabled(false);
            fleeBtn.setText("☠  Game Over");
            fleeBtn.setVisible(true);
            fleeBtn.setOnAction(e -> Main.sceneManager.showGameOver(false, player));
        } else {
            state = BattleState.PLAYER_TURN;
            setButtonsEnabled(true);
        }
        trimLog();
    }

    private void setButtonsEnabled(boolean on) {
        attackBtn.setDisable(!on);
        healBtn.setDisable(!on);
    }

    private void setButtonsForVictory() {
        attackBtn.setVisible(false);
        healBtn.setVisible(false);
        fleeBtn.setVisible(false);
        nextBtn.setVisible(true);
    }

    private void trimLog() {
        while (log.size() > 7) log.remove(0);
    }

    private void render(GraphicsContext gc) {
        LinearGradient bg = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0015")),
                new Stop(0.5, bossColor.deriveColor(0, 0.3, 0.2, 1)),
                new Stop(1, Color.web("#0a0015")));
        gc.setFill(bg);
        gc.fillRect(0, 0, W, H);

        gc.setFill(bossColor.deriveColor(0, 0.5, 0.3, 0.06));
        double pulse = 1 + Math.sin(animTime * 1.5) * 0.04;
        gc.fillOval(W * 0.6 - 150 * pulse, H * 0.25 - 150 * pulse, 300 * pulse, 300 * pulse);

        gc.setFill(Color.web("#1a0a0a", 0.6));
        gc.fillRect(0, H * 0.55, W, H * 0.45);
        gc.setStroke(Color.web("#3a1a1a", 0.5));
        gc.setLineWidth(1);
        for (int y = 0; y < H; y += 60) {
            gc.strokeLine(0, y, W, y);
        }

        drawBoss(gc);
        drawPlayerChar(gc);
        drawBossHPBar(gc);
        drawPlayerHPBar(gc);
        drawLog(gc);
        drawTurnIndicator(gc);
    }

    private void drawBoss(GraphicsContext gc) {
        double bx = W * 0.62 + bossShakeX;
        double by = H * 0.12;
        double scale = 1 + Math.sin(animTime * 1.2) * 0.015; // gentle breathing

        gc.save();
        gc.translate(bx + 100, by + 140);
        gc.scale(scale, scale);
        gc.translate(-100, -140);

        gc.setFill(bossColor.deriveColor(0, 1, 1, 0.15));
        gc.fillOval(-30, -30, 390, 390);

        double alpha = currentBoss.isAlive() ? 1.0 : 0.3;

        Image img = switch (bossIndex) {
            case 0 -> imgBoss1;
            case 1 -> imgBoss2;
            default -> imgBoss3;
        };
        if (img != null && !img.isError()) {
            gc.setGlobalAlpha(alpha);
            gc.drawImage(img, 0, 0, 320, 320);
            gc.setGlobalAlpha(1.0);
        }
        gc.restore();

        double namePlateX = W * 0.62 + 160 - 120;
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRoundRect(namePlateX, H * 0.08, 240, 30, 8, 8);
        gc.setFill(bossColor);
        gc.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(bossName, namePlateX + 120, H * 0.08 + 21);
    }

    private void drawPlayerChar(GraphicsContext gc) {
        double spriteW = 240;
        double spriteH = 320;

        double px = W * 0.06 + playerShakeX;
        double py = H * 0.12;

        if (state == BattleState.PLAYER_TURN) {
            gc.setFill(Color.rgb(100, 150, 255, 0.12));
            gc.fillOval(px - 30, py - 20, spriteW + 60, spriteH + 40);
        }

        gc.setFill(Color.rgb(0, 0, 0, 0.25));
        gc.fillOval(px + spriteW * 0.15, py + spriteH - 10, spriteW * 0.7, 20);

        Image sprite = (showAttackAnim && imgPlayerAttack != null && !imgPlayerAttack.isError())
                ? imgPlayerAttack
                : imgPlayerIdle;

        if (sprite != null && !sprite.isError()) {
            gc.drawImage(sprite, px, py, spriteW, spriteH);

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
            return;
        }
    }

    private void drawBossHPBar(GraphicsContext gc) {
        double bx = W * 0.55;
        double pct = currentBoss.isAlive()
                ? (double) currentBoss.getHealthPoint() / currentBoss.getMaxHealthPoint() : 0;
        gc.setFill(Color.rgb(0, 0, 0, 0.65));
        gc.fillRoundRect(bx, H * 0.60, 350, 22, 6, 6);
        gc.setFill(pct > 0.5 ? Color.web("#ef5350") : pct > 0.25 ? Color.ORANGE : Color.web("#b71c1c"));
        gc.fillRoundRect(bx, H * 0.60, 350 * pct, 22, 6, 6);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(bossName + ": " + currentBoss.getHealthPoint() + "/" + currentBoss.getMaxHealthPoint(),
                bx + 175, H * 0.60 + 15);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawPlayerHPBar(GraphicsContext gc) {
        double pct = (double) player.getHealth() / player.getMaxHealth();
        gc.setFill(Color.rgb(0, 0, 0, 0.65));
        gc.fillRoundRect(30, H * 0.60, 280, 22, 6, 6);
        gc.setFill(pct > 0.5 ? Color.web("#43a047") : pct > 0.25 ? Color.ORANGE : Color.web("#c62828"));
        gc.fillRoundRect(30, H * 0.60, 280 * pct, 22, 6, 6);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("HP: " + player.getHealth() + "/" + player.getMaxHealth()
                + " | ATK:" + player.getAttack() + " DEF:" + player.getDefense(), 170, H * 0.60 + 15);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawLog(GraphicsContext gc) {
        double lx = 20, ly = H * 0.67, lw = W - 40, lh = H * 0.21;
        gc.setFill(Color.rgb(0, 0, 0, 0.75));
        gc.fillRoundRect(lx, ly, lw, lh, 10, 10);
        gc.setStroke(Color.web("#333"));
        gc.setLineWidth(1);
        gc.strokeRoundRect(lx, ly, lw, lh, 10, 10);

        for (int i = 0; i < log.size(); i++) {
            double a = 0.4 + 0.6 * ((i + 1.0) / log.size()); // older lines fade
            Color c = i == log.size() - 1 ? Color.web("#fff9c4") : Color.rgb(180, 200, 220, (int) (a * 255) / 255.0);
            gc.setFill(c);
            gc.setFont(Font.font("Arial", i == log.size() - 1 ? FontWeight.BOLD : FontWeight.NORMAL, 12));
            gc.fillText(log.get(i), lx + 12, ly + 18 + i * 17);
        }
    }

    private void drawTurnIndicator(GraphicsContext gc) {
        String indicator = switch (state) {
            case PLAYER_TURN -> "⚡ YOUR TURN";
            case ENEMY_TURN -> "⏳ Enemy acting...";
            case VICTORY -> "🏆 BOSS DEFEATED!";
            case DEFEAT -> "💀 DEFEATED";
            case ALL_CLEAR -> "🎉 ALL BOSSES CLEARED!";
        };
        Color col = switch (state) {
            case PLAYER_TURN -> Color.web("#fff176");
            case ENEMY_TURN -> Color.web("#ef9a9a");
            case VICTORY, ALL_CLEAR -> Color.web("#a5d6a7");
            case DEFEAT -> Color.web("#ef9a9a");
        };
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRoundRect(W / 2.0 - 100, H * 0.62, 200, 22, 8, 8);
        gc.setFill(col);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(indicator, W / 2.0, H * 0.62 + 15);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private Button makeBtn(String text, String bg, String hover) {
        Button b = new Button(text);
        b.setPrefWidth(140);
        b.setPrefHeight(36);
        String s = "-fx-background-color:" + bg + ";-fx-text-fill:white;-fx-font-weight:bold;" +
                "-fx-font-size:13px;-fx-background-radius:7;-fx-cursor:hand;";
        String h = s.replace(bg, hover);
        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(h));
        b.setOnMouseExited(e -> b.setStyle(s));
        return b;
    }

    private enum BattleState {PLAYER_TURN, ENEMY_TURN, VICTORY, DEFEAT, ALL_CLEAR}

    private record BossInfo(String name, Monster monster, Color color) {
    }
}
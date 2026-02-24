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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import logic.creatures.Player;
import logic.pickaxe.Pickaxe;
import scenes.game.GameController;

import java.util.List;
import java.util.Objects;

public class BossView {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;
    private static final long ANIM_DURATION = 600;
    private BossController.ActionResult r;
    private final BossController controller;
    private final Pickaxe[] pickaxeHolder;

    private Image imgBoss1, imgBoss2, imgBoss3;
    private Image imgPlayerIdle, imgPlayerAttack;

    private double animTime = 0;
    private boolean showAttackAnim = false;
    private long attackAnimEndMs   = 0;
    private double bossShakeX = 0, lastShakeTime = 0;
    private double playerShakeX = 0, lastPlayerShake = 0;
    private AnimationTimer GameLoop;
    private Button attackBtn, skillBtn, bagBtn, fleeBtn, nextBtn, defenseBtn;
    private SkillMenuView skillMenuView;
    private HealMenuView  healMenuView;
    private Pane skillPane, healPane;

    public BossView(BossController controller, Pickaxe[] pickaxeHolder) {
        this.controller    = controller;
        this.pickaxeHolder = pickaxeHolder;
        loadImages();
    }

    private void loadImages() {
        imgBoss1        = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Akaza.png")));
        imgBoss2        = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Kokushibo.png")));
        imgBoss3        = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Muzan.png")));
        imgPlayerIdle   = loadImg("/images/player_idle.png");
        imgPlayerAttack = loadImg("/images/player_slash_right.png");
    }

    private Image loadImg(String p) {
        try { var s = getClass().getResourceAsStream(p); return s == null ? null : new Image(s); }
        catch (Exception e) { return null; }
    }

    public Scene build() {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        BattleMenuController menuCtrl = controller.getMenuCtrl();

        skillMenuView = new SkillMenuView(menuCtrl,
                idx -> {
                    BossController.ActionResult r = controller.doSkill(idx);
                    if (controller.isPendingAttackAnim()) {
                        showAttackAnim  = true;
                        attackAnimEndMs = System.currentTimeMillis() + ANIM_DURATION;
                        controller.clearAttackAnimFlag();
                    }
                    if (controller.isPendingBossShake()) {
                        bossShakeX = 8; lastShakeTime = System.currentTimeMillis();
                        controller.clearBossShakeFlag();
                    }
                    applyResult(r);
                    skillPane.setVisible(false);
                },
                () -> skillPane.setVisible(false));

        healMenuView = new HealMenuView(menuCtrl,
                entry -> {
                    BossController.ActionResult r = controller.usePotion(entry);
                    applyResult(r);
                    healPane.setVisible(false);
                },
                () -> {
                    BossController.ActionResult r = controller.doRestHeal();
                    applyResult(r);
                    healPane.setVisible(false);
                },
                () -> healPane.setVisible(false));

        skillPane = skillMenuView.build();
        skillPane.setVisible(false);
        healPane  = healMenuView.build();
        healPane.setVisible(false);
        defenseBtn = makeBtn("DEFENSE", "#1565c0", "#1976d2");
        attackBtn = makeBtn("ATTACK",   "#c62828", "#ef5350");
        skillBtn  = makeBtn("SKILL",    "#4a148c", "#7b1fa2");
        bagBtn    = makeBtn("BAG",      "#1b5e20", "#388e3c");
        fleeBtn   = makeBtn("FLEE",     "#37474f", "#546e7a");
        nextBtn   = makeBtn("Next Boss","#e65100", "#f4511e");

        double btnY = H - 80, bw = 110, gap = 40;
        double tx   = W / 3 - (bw * 4 + gap * 3) / 2.0;
        attackBtn.setLayoutX(tx);              attackBtn.setLayoutY(btnY);
        skillBtn.setLayoutX(tx + bw + gap);    skillBtn.setLayoutY(btnY);
        defenseBtn.setLayoutX(tx + (bw+gap)*2); defenseBtn.setLayoutY(btnY);
        bagBtn.setLayoutX(tx + (bw+gap)*3);    bagBtn.setLayoutY(btnY);
        fleeBtn.setLayoutX(tx + (bw+gap)*4);   fleeBtn.setLayoutY(btnY);
        nextBtn.setLayoutX(tx + (bw+gap)*4);
        nextBtn.setLayoutY(btnY);  // btnY เดิม
        nextBtn.setVisible(false);
        attackBtn.setOnAction(e -> handleAttack());
        skillBtn.setOnAction(e  -> openSkillMenu());
        bagBtn.setOnAction(e    -> openHealMenu());
        fleeBtn.setOnAction(e   -> Main.sceneManager.showGame(controller.getPlayer(), pickaxeHolder[0]));
        nextBtn.setOnAction(e   -> handleNextBoss());
        defenseBtn.setOnAction(e -> handleDefense());

        StackPane root = new StackPane();
        Pane base = new Pane(canvas, attackBtn, skillBtn,defenseBtn, bagBtn, fleeBtn, nextBtn);
        root.getChildren().addAll(base, skillPane, healPane);

        GameLoop = new AnimationTimer() {
            @Override public void handle(long now) {
                animTime += 1.0 / 60;
                updateVisualState();

                if (controller.getState() == BossController.BattleState.ENEMY_TURN
                        && System.currentTimeMillis() - controller.getLastEnemyActionMs() > 900) {
                    handleEnemyTurn();
                }
                render(gc);
                if (skillPane.isVisible()) skillMenuView.update();
                if (healPane.isVisible()) {
                    healMenuView.update(menuCtrl.getPotions(controller.getPlayer()));
                }
            }
        };
        GameLoop.start();

        return new Scene(root, W, H);
    }
    private void handleDefense() {
        BossController.ActionResult r = controller.doDefend();
        applyResult(r);
    }
    private void handleAttack() {
        if (controller.getState() != BossController.BattleState.PLAYER_TURN) return;
        skillPane.setVisible(false);
        healPane.setVisible(false);
        r = controller.doPlayerAttack();
        if (controller.isPendingAttackAnim()) {
            showAttackAnim  = true;
            attackAnimEndMs = System.currentTimeMillis() + ANIM_DURATION;
            controller.clearAttackAnimFlag();
        }
        if (controller.isPendingBossShake()) {
            bossShakeX = 8; lastShakeTime = System.currentTimeMillis();
            controller.clearBossShakeFlag();
        }
        applyResult(r);
    }

    private void openSkillMenu() {
        if (controller.getState() != BossController.BattleState.PLAYER_TURN) return;
        healPane.setVisible(false);
        skillPane.setVisible(true);
    }

    private void openHealMenu() {
        if (controller.getState() != BossController.BattleState.PLAYER_TURN) return;
        skillPane.setVisible(false);
        healMenuView.refresh(controller.getMenuCtrl().getPotions(controller.getPlayer()));
        healPane.setVisible(true);
    }

    private void handleEnemyTurn() {
        r = controller.doEnemyTurn();
        if (controller.isPendingPlayerShake()) {
            playerShakeX = 10; lastPlayerShake = System.currentTimeMillis();
            controller.clearPlayerShakeFlag();
        }
        applyResult(r);
    }

    private void handleNextBoss() {
        if (controller.hasNextBoss()) {
            controller.advanceToNextBoss();
            nextBtn.setVisible(false);
            attackBtn.setVisible(true); skillBtn.setVisible(true); bagBtn.setVisible(true); fleeBtn.setVisible(true);
            defenseBtn.setVisible(true);
            setButtonsEnabled(true);
        } else {
            Main.sceneManager.showGameOver(true, controller.getPlayer());
        }
    }

    private void applyResult(BossController.ActionResult r) {
        switch (r) {
            case ENEMY_TURN    -> setButtonsEnabled(false);
            case PLAYER_TURN   -> setButtonsEnabled(true);
            case BOSS_DEFEATED -> showVictoryButtons();
            case ALL_CLEAR     -> { showVictoryButtons(); nextBtn.setText("Victory!"); }
            case PLAYER_DEFEATED -> {
                GameLoop.stop();
                fleeBtn.setText("☠  Game Over");
                fleeBtn.setVisible(true);
                imgPlayerIdle = loadImg("/images/player-dead.png");
                fleeBtn.setOnAction(e ->
                        Main.sceneManager.showGameOver(false, controller.getPlayer()));
            }
            default -> {}
        }
    }

    private void updateVisualState() {
        if (System.currentTimeMillis() - lastShakeTime   > 80) bossShakeX   = 0;
        if (System.currentTimeMillis() - lastPlayerShake > 80) playerShakeX = 0;
        if (showAttackAnim && System.currentTimeMillis() > attackAnimEndMs) showAttackAnim = false;
    }

    private void render(GraphicsContext gc) {

        Color bossColor = controller.getBossColor();
        LinearGradient bg = new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#0a0015")),
                new Stop(0.5, bossColor.deriveColor(0,0.3,0.2,1)),
                new Stop(1, Color.web("#0a0015")));
        gc.setFill(bg); gc.fillRect(0,0,W,H);

        gc.setFill(bossColor.deriveColor(0,0.5,0.3,0.06));
        double pulse = 1 + Math.sin(animTime*1.5)*0.04;
        gc.fillOval(W*0.6-150*pulse, H*0.25-150*pulse, 300*pulse, 300*pulse);

        gc.setFill(Color.web("#1a0a0a",0.6));
        gc.fillRect(0, H*0.55, W, H*0.45);
        gc.setStroke(Color.web("#3a1a1a",0.5)); gc.setLineWidth(1);
        for (int y = 0; y < H; y += 60) gc.strokeLine(0,y,W,y);

        drawBoss(gc);
        drawPlayerChar(gc);
        drawBossHPBar(gc);
        drawPlayerHPBar(gc);
        drawLog(gc);
        drawTurnIndicator(gc);
    }

    private void drawBoss(GraphicsContext gc) {
        Color bossColor = controller.getBossColor();
        double bx = W*0.62 + bossShakeX, by = H*0.12;
        double scale = 1 + Math.sin(animTime*1.2)*0.015;
        gc.save();
        gc.translate(bx+100, by+140); gc.scale(scale,scale); gc.translate(-100,-140);
        gc.setFill(bossColor.deriveColor(0,1,1,0.15));
        gc.fillOval(-30,-30,390,390);
        Image img = switch (controller.getBossIndex()) {
            case 0 -> imgBoss1; case 1 -> imgBoss2; default -> imgBoss3;
        };
        double alpha = controller.getCurrentBoss().isAlive() ? 1.0 : 0.3;
        if (img != null && !img.isError()) {
            gc.setGlobalAlpha(alpha);
            gc.drawImage(img, 0, 0, 320, 320);
            gc.setGlobalAlpha(1.0);
        }
        gc.restore();
        double npX = W*0.62+160-120;
        gc.setFill(Color.rgb(0,0,0,0.7)); gc.fillRoundRect(npX, H*0.08, 240, 30, 8, 8);
        gc.setFill(bossColor); gc.setFont(Font.font("Georgia", FontWeight.BOLD,16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(controller.getBossName(), npX+120, H*0.08+21);
    }

    private void drawPlayerChar(GraphicsContext gc) {
        double sw=240, sh=320;
        double px = W*0.06 + playerShakeX, py = (r == BossController.ActionResult.PLAYER_DEFEATED) ? H*0.3 : H*0.12;

        // Status effect glow
        BattleMenuController m = controller.getMenuCtrl();
        if (m.isShieldWallActive()) {
            double pulse = 0.10 + Math.abs(Math.sin(animTime*3))*0.08;
            gc.setFill(Color.rgb(100,160,255,(int)(pulse*255)/255.0));
            gc.fillOval(px-20, py-15, sw+40, sh+30);
        }
        if (controller.getState() == BossController.BattleState.PLAYER_TURN) {
            gc.setFill(Color.rgb(100,150,255,0.12));
            gc.fillOval(px-30, py-20, sw+60, sh+40);
        }
        gc.setFill(Color.rgb(0,0,0,0.25));
        gc.fillOval(px+sw*0.15, py+sh-10, sw*0.7, 20);

        Image sprite = (showAttackAnim && imgPlayerAttack!=null && !imgPlayerAttack.isError())
                ? imgPlayerAttack : imgPlayerIdle;
        if (sprite!=null && !sprite.isError()) {
            gc.drawImage(sprite, px, py, sw, sh);
            double plateW=80, plateH=30;
            double plateX = px+sw/2.0-plateW/2.0, plateY = py-38;
            gc.setFill(Color.rgb(0,0,0,0.7)); gc.fillRoundRect(plateX,plateY,plateW,plateH,8,8);
            gc.setFill(Color.web("#80cbc4")); gc.setFont(Font.font("Georgia",FontWeight.BOLD,14));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("YOU", px+sw/2.0, plateY+21);
            gc.setTextAlign(TextAlignment.LEFT);
        }

        // Status badges
        double bx2 = W*0.06;
        if (m.isShieldWallActive()) {
            gc.setFill(Color.rgb(30,100,200,0.85));
            gc.fillRoundRect(bx2, H*0.52, 120, 20, 6,6);
            gc.setFill(Color.WHITE); gc.setFont(Font.font("Arial",FontWeight.BOLD,10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("SHIELD WALL", bx2+60, H*0.52+14);
            gc.setTextAlign(TextAlignment.LEFT);
        }
        if (m.isBerserkDebuffActive()) {
            gc.setFill(Color.rgb(180,20,20,0.85));
            gc.fillRoundRect(bx2, H*0.54, 130, 20, 6,6);
            gc.setFill(Color.WHITE); gc.setFont(Font.font("Arial",FontWeight.BOLD,10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("DEF -50% NEXT", bx2+65, H*0.54+14);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    private void drawBossHPBar(GraphicsContext gc) {
        var boss = controller.getCurrentBoss();
        double bx = W*0.55;
        double pct = boss.isAlive() ? (double)boss.getHealthPoint()/boss.getMaxHealthPoint() : 0;
        gc.setFill(Color.rgb(0,0,0,0.65)); gc.fillRoundRect(bx, H*0.60, 350, 22, 6,6);
        gc.setFill(pct>0.5?Color.web("#ef5350"):pct>0.25?Color.ORANGE:Color.web("#b71c1c"));
        gc.fillRoundRect(bx, H*0.60, 350*pct, 22, 6,6);
        gc.setFill(Color.WHITE); gc.setFont(Font.font("Arial",FontWeight.BOLD,12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(controller.getBossName()+": "+boss.getHealthPoint()+"/"+boss.getMaxHealthPoint(), bx+175, H*0.60+15);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawPlayerHPBar(GraphicsContext gc) {
        Player player = controller.getPlayer();
        double pct = (double)player.getHealth()/player.getMaxHealth();
        gc.setFill(Color.rgb(0,0,0,0.65)); gc.fillRoundRect(30, H*0.60, 280, 22, 6,6);
        gc.setFill(pct>0.5?Color.web("#43a047"):pct>0.25?Color.ORANGE:Color.web("#c62828"));
        gc.fillRoundRect(30, H*0.60, 280*pct, 22, 6,6);
        gc.setFill(Color.WHITE); gc.setFont(Font.font("Arial",FontWeight.BOLD,12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("HP: "+player.getHealth()+"/"+player.getMaxHealth()
                +" | ATK:"+player.getAttack()+" DEF:"+player.getDefense(), 170, H*0.60+15);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawLog(GraphicsContext gc) {
        var log = controller.getLog();
        double lx=20, ly=H*0.67, lw=W*0.52, lh=H*0.21;
        gc.setFill(Color.rgb(0,0,0,0.75)); gc.fillRoundRect(lx,ly,lw,lh,10,10);
        gc.setStroke(Color.web("#333")); gc.setLineWidth(1);
        gc.strokeRoundRect(lx,ly,lw,lh,10,10);
        for (int i=0; i<log.size(); i++) {
            boolean latest = (i==log.size()-1);
            double a = 0.4+0.6*((i+1.0)/log.size());
            Color c = latest ? Color.web("#fff9c4") : Color.rgb(180,200,220,(int)(a*255)/255.0);
            gc.setFill(c);
            gc.setFont(Font.font("Arial", latest?FontWeight.BOLD:FontWeight.NORMAL, 12));
            gc.fillText(log.get(i), lx+12, ly+18+i*17);
        }
    }

    private void drawTurnIndicator(GraphicsContext gc) {
        String ind = switch (controller.getState()) {
            case PLAYER_TURN -> "YOUR TURN";
            case ENEMY_TURN  -> "Enemy acting...";
            case VICTORY     -> "BOSS DEFEATED!";
            case DEFEAT      -> "DEFEATED";
            case ALL_CLEAR   -> "ALL BOSSES CLEARED!";
        };
        Color col = switch (controller.getState()) {
            case PLAYER_TURN -> Color.web("#fff176");
            case ENEMY_TURN, DEFEAT -> Color.web("#ef9a9a");
            case VICTORY, ALL_CLEAR -> Color.web("#a5d6a7");
        };
        gc.setFill(Color.rgb(0,0,0,0.6)); gc.fillRoundRect(W/2.0-100, H*0.62, 200, 22, 8,8);
        gc.setFill(col); gc.setFont(Font.font("Arial",FontWeight.BOLD,12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(ind, W/2.0, H*0.62+15);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawBottomBar(GraphicsContext gc) {
        // Pokemon-style bottom panel description
        double bx = 20, by = H - 60, bw = W/2.0 - 30, bh = 50;
        gc.setFill(Color.rgb(0,0,0,0.70));
        gc.fillRoundRect(bx, by, bw, bh, 10, 10);
        gc.setFill(Color.web("#e0e0e0"));
        gc.setFont(Font.font("Arial", 11));
        boolean skillOpen = controller.getMenuCtrl().getMenuState() == BattleMenuController.MenuState.SKILLS;
        String hint = switch(controller.getState()) {
            case PLAYER_TURN -> "FIGHT: open skill menu  |  BAG: use items  |  FLEE: escape battle";
            case ENEMY_TURN  -> "Enemy is preparing their next move...";
            case VICTORY     -> "Boss defeated! Proceed to the next challenge.";
            case DEFEAT      -> "You have fallen in battle...";
            case ALL_CLEAR   -> "All bosses vanquished! Victory is yours!";
        };
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(hint, bx + bw/2.0, by + 30);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void setButtonsEnabled(boolean on) {
        attackBtn.setDisable(!on);
        skillBtn.setDisable(!on);
        defenseBtn.setDisable(!on);
        bagBtn.setDisable(!on);
    }

    private void showVictoryButtons() {
        attackBtn.setVisible(false); skillBtn.setVisible(false);
        defenseBtn.setVisible(false); bagBtn.setVisible(false); fleeBtn.setVisible(false);
        nextBtn.setVisible(true);
    }

    private Button makeBtn(String text, String bg, String hover) {
        Button b = new Button(text);
        b.setPrefWidth(140); b.setPrefHeight(36);
        String s = "-fx-background-color:"+bg+";-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-font-size:13px;"
                + "-fx-background-radius:7;-fx-cursor:hand;";
        String h = s.replace(bg, hover);
        b.setStyle(s);
        b.setOnMouseEntered(e -> b.setStyle(h));
        b.setOnMouseExited(e  -> b.setStyle(s));
        return b;
    }
}
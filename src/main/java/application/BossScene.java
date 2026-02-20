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

import logic.creatures.EasyMonster;
import logic.creatures.HardMonster;
import logic.creatures.MediumMonster;
import logic.creatures.Monster;
import logic.creatures.Player;
import logic.item.potion.HealPotion;
import logic.item.potion.SmallHealthPotion;
import logic.pickaxe.Pickaxe;
import logic.util.ItemCounter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * BossScene: A stylised RPG-style battle screen.
 *
 * Three boss tiers: Goblin King → Orc Warlord → Dragon
 * The player fights each boss in order, then wins the game.
 *
 * "Action log" at the bottom shows what happened each turn — just like classic JRPGs.
 * Uses your Monster, Player, attack() and takeDamage() methods directly.
 */
public class BossScene {

    private static final int W = SceneManager.W;
    private static final int H = SceneManager.H;

    private final Player    player;
    private final Pickaxe[] pickaxeHolder;

    // ── Boss definitions ─────────────────────────────────────────────
    private record BossInfo(String name, Monster monster, Color color, String emoji) {}
    private final BossInfo[] bosses = {
        new BossInfo("Goblin King",   new EasyMonster(),  Color.web("#66bb6a"), "👺"),
        new BossInfo("Orc Warlord",   new MediumMonster(),Color.web("#ce93d8"), "👹"),
        new BossInfo("The Dragon",    new HardMonster(),  Color.web("#ef5350"), "🐉"),
    };

    private int    bossIndex   = 0;     // which boss we're fighting
    private Monster currentBoss;
    private String  bossName;
    private Color   bossColor;
    private String  bossEmoji;

    // ── Battle state ─────────────────────────────────────────────────
    private enum BattleState { PLAYER_TURN, ENEMY_TURN, VICTORY, DEFEAT, ALL_CLEAR }
    private BattleState state = BattleState.PLAYER_TURN;

    private final List<String> log = new ArrayList<>();  // action log lines
    private long   lastActionTime  = 0;                  // enemy delays 1s
    private double bossShakeX      = 0;                  // hit animation
    private long   lastShakeTime   = 0;
    private double playerShakeX    = 0;
    private long   lastPlayerShake = 0;
    private double animTime        = 0;                  // for idle animations

    // ── Buttons ──────────────────────────────────────────────────────
    private Button attackBtn, healBtn, fleeBtn, nextBtn;

    public BossScene(Player player, Pickaxe[] pickaxeHolder) {
        this.player        = player;
        this.pickaxeHolder = pickaxeHolder;
        loadBoss(0);
    }

    private void loadBoss(int index) {
        bossIndex   = index;
        BossInfo bi = bosses[index];
        currentBoss = bi.monster();
        bossName    = bi.name();
        bossColor   = bi.color();
        bossEmoji   = bi.emoji();
        log.clear();
        log.add("⚔ A wild " + bossName + " appears!");
        log.add("Your HP: " + player.getHealth() + "/" + player.getMaxHealth()
                + "  ATK: " + player.getAttack() + "  DEF: " + player.getDefense());
    }

    // ════════════════════════════════════════════════════════════════
    public Scene build() {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Pane root = new Pane(canvas);

        // ── Action buttons ──────────────────────────────────────────
        attackBtn = makeBtn("⚔  Attack",  "#c62828", "#ef5350");
        healBtn   = makeBtn("💊  Heal",   "#1b5e20", "#388e3c");
        fleeBtn   = makeBtn("🏃  Flee",   "#4a148c", "#7b1fa2");
        nextBtn   = makeBtn("➜  Next Boss","#e65100","#f4511e");

        attackBtn.setLayoutX(W/2.0 - 230); attackBtn.setLayoutY(H - 90);
        healBtn  .setLayoutX(W/2.0 - 70);  healBtn  .setLayoutY(H - 90);
        fleeBtn  .setLayoutX(W/2.0 + 90);  fleeBtn  .setLayoutY(H - 90);
        nextBtn  .setLayoutX(W/2.0 - 70);  nextBtn  .setLayoutY(H - 90);
        nextBtn  .setVisible(false);

        attackBtn.setOnAction(e -> doPlayerAttack());
        healBtn  .setOnAction(e -> doPlayerHeal());
        fleeBtn  .setOnAction(e -> Main.sceneManager.showGame(player, pickaxeHolder[0]));
        nextBtn  .setOnAction(e -> {
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

        // ── Game loop ─────────────────────────────────────────────────
        new AnimationTimer() {
            @Override public void handle(long now) {
                animTime += 1.0 / 60;

                // Boss shake decay
                if (System.currentTimeMillis() - lastShakeTime > 80) bossShakeX = 0;
                if (System.currentTimeMillis() - lastPlayerShake > 80) playerShakeX = 0;

                // Enemy turn: wait 1 second, then act
                if (state == BattleState.ENEMY_TURN
                        && System.currentTimeMillis() - lastActionTime > 900) {
                    doEnemyTurn();
                }

                render(gc);
            }
        }.start();

        return new Scene(root, W, H);
    }

    // ── Player actions ───────────────────────────────────────────────
    private void doPlayerAttack() {
        if (state != BattleState.PLAYER_TURN) return;

        // Uses Player.attack() → calls Monster.takeDamage(player.attack)
        player.attack(currentBoss);
        int dmg = Math.max(1, player.getAttack() - currentBoss.getDefense());
        log.add("You hit " + bossName + " for " + dmg + " dmg!");

        // Boss recoil shake
        bossShakeX = 8; lastShakeTime = System.currentTimeMillis();

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
        // Check if player has a potion in inventory
        for (ItemCounter ic : player.getInventory()) {
            if (ic.getItem() instanceof logic.base.BasePotion pot) {
                pot.consume(player);
                ic.setCount(ic.getCount() - 1);
                if (ic.getCount() <= 0) player.getInventory().remove(ic);
                log.add("You used " + ic.getItem().getName() + "! HP: "+player.getHealth()+"/"+player.getMaxHealth());
                state = BattleState.ENEMY_TURN;
                lastActionTime = System.currentTimeMillis();
                setButtonsEnabled(false);
                trimLog();
                return;
            }
        }
        // No potion — small free heal
        int heal = Math.max(5, player.getMaxHealth() / 10);
        player.heal(heal);
        log.add("You rest briefly... +" + heal + " HP  ("+player.getHealth()+"/"+player.getMaxHealth()+")");
        state = BattleState.ENEMY_TURN;
        lastActionTime = System.currentTimeMillis();
        setButtonsEnabled(false);
        trimLog();
    }

    private void doEnemyTurn() {
        if (state != BattleState.ENEMY_TURN) return;

        // Critical hit chance 20%
        boolean crit = Math.random() < 0.20;
        int baseDmg = currentBoss.getAttack();
        if (crit) baseDmg = (int)(baseDmg * 1.6);

        currentBoss.attack(player);  // calls Player.takeDamage()
        int actualDmg = Math.max(1, baseDmg - player.getDefense());
        String suffix = crit ? " 💥CRIT!" : "";
        log.add(bossName + " attacks you for " + actualDmg + " dmg!" + suffix);
        log.add("Your HP: " + player.getHealth() + "/" + player.getMaxHealth());

        // Player shake
        playerShakeX = 10; lastPlayerShake = System.currentTimeMillis();

        if (!player.isAlive()) {
            state = BattleState.DEFEAT;
            log.add("💀 You have been defeated...");
            setButtonsEnabled(false);
            fleeBtn.setText("☠  Game Over"); fleeBtn.setVisible(true);
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

    private void trimLog() { while (log.size() > 7) log.remove(0); }

    // ── Render ────────────────────────────────────────────────────────
    private void render(GraphicsContext gc) {
        // Background — dramatic dark gradient
        LinearGradient bg = new LinearGradient(0,0,0,1,true,CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#0a0015")),
            new Stop(0.5, bossColor.deriveColor(0,0.3,0.2,1)),
            new Stop(1, Color.web("#0a0015")));
        gc.setFill(bg); gc.fillRect(0,0,W,H);

        // Atmospheric circles
        gc.setFill(bossColor.deriveColor(0,0.5,0.3,0.06));
        double pulse = 1 + Math.sin(animTime*1.5)*0.04;
        gc.fillOval(W*0.6-150*pulse, H*0.25-150*pulse, 300*pulse, 300*pulse);

        // Arena floor
        gc.setFill(Color.web("#1a0a0a",0.6)); gc.fillRect(0, H*0.55, W, H*0.45);
        gc.setStroke(Color.web("#3a1a1a",0.5)); gc.setLineWidth(1);
        for (int y=0;y<H;y+=60) { gc.strokeLine(0,y,W,y); }

        drawBoss(gc);
        drawPlayerChar(gc);
        drawBossHPBar(gc);
        drawPlayerHPBar(gc);
        drawLog(gc);
        drawTurnIndicator(gc);
    }

    /** Draw the boss on the right side of the screen — big, intimidating */
    private void drawBoss(GraphicsContext gc) {
        double bx = W * 0.62 + bossShakeX;
        double by = H * 0.12;
        double scale = 1 + Math.sin(animTime * 1.2) * 0.015; // gentle breathing

        gc.save();
        gc.translate(bx + 100, by + 140);
        gc.scale(scale, scale);
        gc.translate(-100, -140);

        // Boss glow aura
        gc.setFill(bossColor.deriveColor(0,1,1,0.15));
        gc.fillOval(-30, -20, 260, 320);

        double alpha = currentBoss.isAlive() ? 1.0 : 0.3;

        switch(bossIndex) {
            case 0 -> drawGoblinKing(gc, bossColor, alpha);
            case 1 -> drawOrcWarlord(gc, bossColor, alpha);
            case 2 -> drawDragon(gc, bossColor, alpha);
        }
        gc.restore();

        // Boss name plate
        gc.setFill(Color.rgb(0,0,0,0.7)); gc.fillRoundRect(W*0.58,H*0.08,240,30,8,8);
        gc.setFill(bossColor); gc.setFont(Font.font("Georgia",FontWeight.BOLD,16));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(bossEmoji+" "+bossName, W*0.58+120, H*0.08+21);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawGoblinKing(GraphicsContext gc, Color c, double a) {
        // Body
        gc.setFill(Color.rgb((int)(c.getRed()*255),(int)(c.getGreen()*255),(int)(c.getBlue()*255),(int)(a)));
        gc.fillOval(50,80,100,110);
        // Head
        gc.setFill(Color.web("#66bb6a",(int)(a*255)/255.0)); gc.fillOval(60,20,80,70);
        // Crown
        gc.setFill(Color.web("#ffd700",a)); gc.fillPolygon(new double[]{60,80,100,120,140},new double[]{25,5,20,5,25},5);
        // Eyes
        gc.setFill(Color.RED); gc.fillOval(75,42,14,14); gc.fillOval(111,42,14,14);
        // Teeth
        gc.setFill(Color.WHITE);
        for (int t=0;t<4;t++) gc.fillRect(72+t*16,80,8,12);
        // Arms
        gc.setFill(Color.web("#66bb6a",a)); gc.fillOval(20,100,40,30); gc.fillOval(140,100,40,30);
        // Legs
        gc.fillRect(60,185,30,50); gc.fillRect(110,185,30,50);
        // Club
        gc.setFill(Color.web("#5d4037",a)); gc.fillRoundRect(155,85,14,60,5,5);
        gc.setFill(Color.web("#8d6e63",a)); gc.fillRoundRect(148,65,28,30,8,8);
    }

    private void drawOrcWarlord(GraphicsContext gc, Color c, double a) {
        // Big body
        gc.setFill(c.deriveColor(0,1,0.7,a)); gc.fillOval(30,80,140,130);
        // Head
        gc.setFill(c.deriveColor(0,1,0.8,a)); gc.fillOval(55,15,90,75);
        // Helmet
        gc.setFill(Color.web("#546e7a",a)); gc.fillArc(50,10,100,55,0,180,javafx.scene.shape.ArcType.CHORD);
        // Eyes
        gc.setFill(Color.web("#ff1744")); gc.fillOval(72,45,16,16); gc.fillOval(112,45,16,16);
        // Tusks
        gc.setFill(Color.web("#fffde7",a)); gc.fillPolygon(new double[]{78,84,80},new double[]{88,88,105},3);
        gc.fillPolygon(new double[]{116,122,118},new double[]{88,88,105},3);
        // Arms (huge)
        gc.setFill(c.deriveColor(0,1,0.6,a)); gc.fillOval(0,90,45,35); gc.fillOval(155,90,45,35);
        // Axe
        gc.setFill(Color.web("#b0bec5",a)); gc.fillPolygon(new double[]{170,195,195,170},new double[]{65,50,100,85},4);
        gc.setFill(Color.web("#4e342e",a)); gc.fillRect(185,48,12,90);
        // Legs
        gc.setFill(c.deriveColor(0,1,0.6,a)); gc.fillRect(55,205,40,55); gc.fillRect(105,205,40,55);
    }

    private void drawDragon(GraphicsContext gc, Color c, double a) {
        // Wings (background)
        gc.setFill(c.deriveColor(0,0.7,0.5,a*0.7));
        gc.fillPolygon(new double[]{100,-30,-60,20}, new double[]{100,20,140,180},4);
        gc.fillPolygon(new double[]{100,230,260,180},new double[]{100,20,140,180},4);

        // Body
        gc.setFill(c.deriveColor(0,1,0.6,a)); gc.fillOval(40,100,120,130);
        // Neck
        gc.setFill(c.deriveColor(0,1,0.7,a)); gc.fillOval(65,50,70,70);
        // Head
        gc.fillOval(55,10,90,60);
        // Horns
        gc.setFill(Color.web("#1a0000",a));
        gc.fillPolygon(new double[]{70,80,65},new double[]{12,12,-20},3);
        gc.fillPolygon(new double[]{120,130,135},new double[]{12,12,-20},3);
        // Eyes (glowing)
        gc.setFill(Color.rgb(255,200,0,(int)(a))); gc.fillOval(70,28,18,18); gc.fillOval(112,28,18,18);
        gc.setFill(Color.BLACK); gc.fillOval(75,32,8,8); gc.fillOval(117,32,8,8);
        // Fire breath (when enemy turn)
        if (state == BattleState.ENEMY_TURN) {
            double flick = Math.sin(animTime*15)*8;
            gc.setFill(Color.rgb(255,100,0,0.8)); gc.fillOval(-20+flick, 28, 80, 25);
            gc.setFill(Color.rgb(255,220,0,0.6)); gc.fillOval(-10+flick, 32, 60, 18);
        }
        // Tail
        gc.setFill(c.deriveColor(0,1,0.5,a));
        gc.fillOval(30,210,30,25); gc.fillOval(10,225,25,20); gc.fillOval(-5,232,20,14);
        // Legs
        gc.setFill(c.deriveColor(0,1,0.5,a));
        gc.fillRect(50,225,30,40); gc.fillRect(120,225,30,40);
        gc.setFill(Color.web("#212121",a));
        gc.fillRect(50,260,30,10); gc.fillRect(120,260,30,10);
    }

    /** Draw the player on the left side */
    private void drawPlayerChar(GraphicsContext gc) {
        double px = W * 0.10 + playerShakeX;
        double py = H * 0.22;

        // Glow when it's player's turn
        if (state == BattleState.PLAYER_TURN) {
            gc.setFill(Color.rgb(100,150,255,0.12));
            gc.fillOval(px-20, py-10, 160, 220);
        }

        // Shadow
        gc.setFill(Color.rgb(0,0,0,0.25)); gc.fillOval(px+20,py+155,80,20);

        // Player body (larger battle sprite)
        gc.setFill(Color.web("#880e4f")); gc.fillRoundRect(px+30,py+70,60,55,10,10); // cape
        gc.setFill(Color.web("#1565c0")); gc.fillRoundRect(px+35,py+75,50,45,8,8);  // armor
        gc.setFill(Color.web("#4e342e")); gc.fillRect(px+35,py+118,20,30); gc.fillRect(px+65,py+118,20,30); // legs

        // Head
        gc.setFill(Color.web("#ffcc80")); gc.fillOval(px+40,py+30,50,50);
        gc.setFill(Color.web("#4e342e")); gc.fillRect(px+40,py+30,50,18); // hair

        // Eyes
        gc.setFill(Color.web("#1a237e"));
        gc.fillOval(px+48,py+52,8,8); gc.fillOval(px+68,py+52,8,8);

        // Sword (drawn big for battle)
        gc.save();
        gc.translate(px+112, py+80);
        gc.rotate(state == BattleState.PLAYER_TURN ? -20 : 10);
        gc.setFill(Color.web("#795548")); gc.fillRoundRect(-5,-5,10,35,4,4); // handle
        gc.setFill(Color.web("#ffd700")); gc.fillRect(-10,30,20,8); // guard
        gc.setFill(Color.web("#b0bec5")); gc.fillPolygon(new double[]{-6,6,0},new double[]{38,38,-18},3);
        gc.setFill(Color.web("#eceff1")); gc.fillPolygon(new double[]{-3,3,0},new double[]{36,36,-14},3);
        gc.restore();

        // "YOU" label
        gc.setFill(Color.web("#80cbc4")); gc.setFont(Font.font("Arial",FontWeight.BOLD,12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("YOU", px+65, py+22);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawBossHPBar(GraphicsContext gc) {
        double bx = W * 0.55;
        double pct = currentBoss.isAlive()
            ? (double)currentBoss.getHealthPoint()/currentBoss.getMaxHealthPoint() : 0;
        gc.setFill(Color.rgb(0,0,0,0.65)); gc.fillRoundRect(bx,H*0.60,350,22,6,6);
        gc.setFill(pct>0.5?Color.web("#ef5350"):pct>0.25?Color.ORANGE:Color.web("#b71c1c"));
        gc.fillRoundRect(bx,H*0.60,350*pct,22,6,6);
        gc.setFill(Color.WHITE); gc.setFont(Font.font("Arial",FontWeight.BOLD,12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(bossName+": "+currentBoss.getHealthPoint()+"/"+currentBoss.getMaxHealthPoint(),
                    bx+175, H*0.60+15);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawPlayerHPBar(GraphicsContext gc) {
        double pct = (double)player.getHealth()/player.getMaxHealth();
        gc.setFill(Color.rgb(0,0,0,0.65)); gc.fillRoundRect(30,H*0.60,280,22,6,6);
        gc.setFill(pct>0.5?Color.web("#43a047"):pct>0.25?Color.ORANGE:Color.web("#c62828"));
        gc.fillRoundRect(30,H*0.60,280*pct,22,6,6);
        gc.setFill(Color.WHITE); gc.setFont(Font.font("Arial",FontWeight.BOLD,12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("HP: "+player.getHealth()+"/"+player.getMaxHealth()
                    +" | ATK:"+player.getAttack()+" DEF:"+player.getDefense(), 170, H*0.60+15);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawLog(GraphicsContext gc) {
        double lx=20, ly=H*0.67, lw=W-40, lh=H*0.21;
        gc.setFill(Color.rgb(0,0,0,0.75)); gc.fillRoundRect(lx,ly,lw,lh,10,10);
        gc.setStroke(Color.web("#333")); gc.setLineWidth(1); gc.strokeRoundRect(lx,ly,lw,lh,10,10);

        for (int i=0; i<log.size(); i++) {
            double a = 0.4 + 0.6*((i+1.0)/log.size()); // older lines fade
            Color c = i == log.size()-1 ? Color.web("#fff9c4") : Color.rgb(180,200,220,(int)(a*255)/255.0);
            gc.setFill(c); gc.setFont(Font.font("Arial", i==log.size()-1?FontWeight.BOLD:FontWeight.NORMAL, 12));
            gc.fillText(log.get(i), lx+12, ly+18+i*17);
        }
    }

    private void drawTurnIndicator(GraphicsContext gc) {
        String indicator = switch(state) {
            case PLAYER_TURN -> "⚡ YOUR TURN";
            case ENEMY_TURN  -> "⏳ Enemy acting...";
            case VICTORY     -> "🏆 BOSS DEFEATED!";
            case DEFEAT      -> "💀 DEFEATED";
            case ALL_CLEAR   -> "🎉 ALL BOSSES CLEARED!";
        };
        Color col = switch(state) {
            case PLAYER_TURN -> Color.web("#fff176");
            case ENEMY_TURN  -> Color.web("#ef9a9a");
            case VICTORY, ALL_CLEAR -> Color.web("#a5d6a7");
            case DEFEAT      -> Color.web("#ef9a9a");
        };
        gc.setFill(Color.rgb(0,0,0,0.6)); gc.fillRoundRect(W/2.0-100,H*0.62,200,22,8,8);
        gc.setFill(col); gc.setFont(Font.font("Arial",FontWeight.BOLD,12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(indicator, W/2.0, H*0.62+15);
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private Button makeBtn(String text, String bg, String hover) {
        Button b = new Button(text);
        b.setPrefWidth(140); b.setPrefHeight(36);
        String s = "-fx-background-color:"+bg+";-fx-text-fill:white;-fx-font-weight:bold;" +
                   "-fx-font-size:13px;-fx-background-radius:7;-fx-cursor:hand;";
        String h = s.replace(bg,hover);
        b.setStyle(s); b.setOnMouseEntered(e->b.setStyle(h)); b.setOnMouseExited(e->b.setStyle(s));
        return b;
    }
}

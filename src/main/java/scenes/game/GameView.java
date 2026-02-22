package scenes.game;

import application.Main;
import application.SceneManager;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import logic.pickaxe.Pickaxe;
import logic.util.ItemCounter;
import scenes.crafting.CraftingController;
import scenes.crafting.CraftingView;
import scenes.inventory.InventoryController;
import scenes.inventory.InventoryView;
import scenes.shop.ShopController;
import scenes.shop.ShopView;

import java.util.List;
import java.util.Objects;

/**
 * GameView — the "face" of the main game world.
 *
 * Responsibility: ALL JavaFX scene construction and rendering. No game logic.
 *   - Builds the Scene with canvas and overlay layers
 *   - Sets up keyboard and mouse input (passes raw input to GameController)
 *   - Runs the AnimationTimer; calls controller.update() then renders
 *   - Draws the world tiles, monsters, player, floating texts, and HUD
 *   - Manages shop/crafting/inventory overlay visibility
 */
public class GameView {

    private static final int W = GameController.W;
    private static final int H = GameController.H;

    private final GameController controller;

    // ── Sub-scene controllers and views ──────────────────────────────────────
    private ShopController      shopController;
    private ShopView            shopView;
    private CraftingController  craftController;
    private CraftingView        craftView;
    private InventoryController invController;
    private InventoryView       invView;

    // ── Overlay layers (shown on top of the game world) ───────────────────────
    private Pane shopLayer, craftLayer, invLayer;

    // ── Root layout ───────────────────────────────────────────────────────────
    private StackPane root;

    // ── Sprite images ─────────────────────────────────────────────────────────
    private final Image[] playerWalkImgs  = new Image[4];
    private final Image[] playerSlashImgs = new Image[4];
    private Image imgEasyMonster, imgMediumMonster, imgHardMonster;

    // ── Animation timer (kept as field so we can stop it) ────────────────────
    private AnimationTimer gameLoop;

    public GameView(GameController controller) {
        this.controller = controller;
        loadImages();
        buildSubScenes();
    }

    // ── Image loading ─────────────────────────────────────────────────────────

    private void loadImages() {
        imgEasyMonster   = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Rui.png")));
        imgMediumMonster = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Enmu.png")));
        imgHardMonster   = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Daki.png")));

        String[] dirs = {"up", "left", "down", "right"};
        for (int i = 0; i < 4; i++) {
            playerWalkImgs[i]  = loadImage("/images/player_walk_"  + dirs[i] + ".png");
            playerSlashImgs[i] = loadImage("/images/player_slash_" + dirs[i] + ".png");
        }
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

    // ── Sub-scene construction ────────────────────────────────────────────────

    private void buildSubScenes() {
        // Each overlay has its own Controller + View pair
        shopController = new ShopController(controller.getPlayer(), controller.getPickaxeHolder());
        shopView       = new ShopView(shopController, this::closeShop);
        shopLayer      = shopView.build();
        shopLayer.setVisible(false);

        craftController = new CraftingController(controller.getPlayer());
        craftView       = new CraftingView(craftController, this::closeCraft);
        craftLayer      = craftView.build();
        craftLayer.setVisible(false);

        invController = new InventoryController(controller.getPlayer());
        invView       = new InventoryView(invController, this::closeInventory);
        invLayer      = invView.build();
        invLayer.setVisible(false);
    }

    // ── Build ─────────────────────────────────────────────────────────────────

    /** Builds and returns the complete game Scene. Call once. */
    public Scene buildScene() {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        root = new StackPane();
        root.getChildren().addAll(canvas, shopLayer, craftLayer, invLayer);

        Scene scene = new Scene(root, W, H);

        // ── Input wiring ─────────────────────────────────────────────────────
        scene.setOnKeyPressed(e -> {
            controller.keyPressed(e.getCode());
            if (e.getCode() == KeyCode.SPACE) handleBuildingEntry();
            if (e.getCode() == KeyCode.E)     toggleInventory();
        });
        scene.setOnKeyReleased(e -> controller.keyReleased(e.getCode()));

        scene.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY)   controller.setLeftMouse(true);
            if (e.getButton() == MouseButton.SECONDARY) controller.setRightMouse(true);
        });
        scene.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY)   controller.setLeftMouse(false);
            if (e.getButton() == MouseButton.SECONDARY) controller.setRightMouse(false);
        });

        // ── Main game loop ───────────────────────────────────────────────────
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Only update game logic if no overlay is open
                boolean overlayOpen = shopLayer.isVisible()
                        || craftLayer.isVisible()
                        || invLayer.isVisible();

                if (!overlayOpen) {
                    controller.update(now, this); // controller handles logic
                }

                // Always render (overlays need updating too)
                render(gc, now);

                // Refresh overlay sub-scenes each frame
                if (shopLayer.isVisible())  shopView .update();
                if (craftLayer.isVisible()) craftView.update();
                if (invLayer.isVisible())   invView  .update();
            }
        };
        gameLoop.start();

        return scene;
    }

    // ── Building entry ────────────────────────────────────────────────────────

    private void handleBuildingEntry() {
        GameController.BuildingType type = controller.checkBuildingEntry();
        switch (type) {
            case SHOP  -> toggleShop();
            case CRAFT -> toggleCraft();
            case BOSS  -> {
                gameLoop.stop();
                Main.sceneManager.showBossRoom(controller.getPlayer(),
                        controller.getPickaxeHolder());
            }
            default -> {} // NONE — notification already shown by controller
        }
    }

    private void toggleShop()      { shopLayer.setVisible(!shopLayer.isVisible()); }
    private void toggleCraft()     { craftLayer.setVisible(!craftLayer.isVisible()); }
    private void toggleInventory() {
        boolean opening = !invLayer.isVisible();
        invLayer.setVisible(opening);
        if (opening) invView.refresh();
    }

    private void closeShop()      { shopLayer.setVisible(false); }
    private void closeCraft()     { craftLayer.setVisible(false); }
    private void closeInventory() { invLayer.setVisible(false); }

    // ── Render ────────────────────────────────────────────────────────────────

    private void render(GraphicsContext gc, long nowNanos) {
        drawWorld(gc);
        drawMonsters(gc);
        drawPlayer(gc);
        drawFloatingTexts(gc);
        drawHUD(gc);
    }

    // ── Tile rendering ────────────────────────────────────────────────────────

    private void drawWorld(GraphicsContext gc) {
        int[][] world = controller.getWorld();

        for (int r = 0; r < GameController.ROWS; r++) {
            for (int c = 0; c < GameController.COLS; c++) {
                double x = c * GameController.TILE_SIZE;
                double y = r * GameController.TILE_SIZE;

                switch (world[r][c]) {
                    case GameController.T_GROUND -> {
                        gc.setFill(Color.web("#4a7c38"));
                        gc.fillRect(x, y, GameController.TILE_SIZE, GameController.TILE_SIZE);
                    }
                    case GameController.T_GRASS -> {
                        gc.setFill(Color.web("#3d6b2d"));
                        gc.fillRect(x, y, GameController.TILE_SIZE, GameController.TILE_SIZE);
                        gc.setFill(Color.web("#2d5220"));
                        gc.fillRect(x + 8, y + 10, 3, 8);
                        gc.fillRect(x + 20, y + 6, 3, 10);
                        gc.fillRect(x + 32, y + 12, 3, 7);
                    }
                    case GameController.T_PATH -> {
                        gc.setFill(Color.web("#8d7b6a"));
                        gc.fillRect(x, y, GameController.TILE_SIZE, GameController.TILE_SIZE);
                        gc.setStroke(Color.web("#7a6a5a", 0.4));
                        gc.strokeRect(x + 1, y + 1, GameController.TILE_SIZE - 2, GameController.TILE_SIZE - 2);
                    }
                    case GameController.T_NORMAL_ROCK ->
                        drawRock(gc, x, y, controller.getStoneObjects()[r][c],
                                Color.web("#9e9e9e"), Color.web("#757575"), "N");
                    case GameController.T_HARD_ROCK ->
                        drawRock(gc, x, y, controller.getStoneObjects()[r][c],
                                Color.web("#78909c"), Color.web("#455a64"), "H");
                    case GameController.T_IRON_ROCK ->
                        drawRock(gc, x, y, controller.getStoneObjects()[r][c],
                                Color.web("#bf8f5b"), Color.web("#8d6030"), "Fe");
                    case GameController.T_PLATINUM ->
                        drawRock(gc, x, y, controller.getStoneObjects()[r][c],
                                Color.web("#90caf9"), Color.web("#1976d2"), "Pt");
                    case GameController.T_MITHRIL ->
                        drawRock(gc, x, y, controller.getStoneObjects()[r][c],
                                Color.web("#ce93d8"), Color.web("#7b1fa2"), "Mi");
                    case GameController.T_VIBRANIUM ->
                        drawRock(gc, x, y, controller.getStoneObjects()[r][c],
                                Color.web("#80cbc4"), Color.web("#00695c"), "Vb");
                    case GameController.T_SHOP -> {
                        gc.setFill(Color.web("#5d4037")); gc.fillRect(x, y, GameController.TILE_SIZE, GameController.TILE_SIZE);
                        gc.setFill(Color.web("#795548")); gc.fillRect(x + 4, y + 4, GameController.TILE_SIZE - 8, GameController.TILE_SIZE - 8);
                        gc.setFill(Color.web("#ffd54f"));
                        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                        gc.setTextAlign(TextAlignment.CENTER);
                        gc.fillText("SHOP", x + GameController.TILE_SIZE / 2.0, y + 28);
                        gc.fillText("🛒",   x + GameController.TILE_SIZE / 2.0, y + 18);
                    }
                    case GameController.T_CRAFT -> {
                        gc.setFill(Color.web("#1a237e")); gc.fillRect(x, y, GameController.TILE_SIZE, GameController.TILE_SIZE);
                        gc.setFill(Color.web("#283593")); gc.fillRect(x + 4, y + 4, GameController.TILE_SIZE - 8, GameController.TILE_SIZE - 8);
                        gc.setFill(Color.web("#80cbc4"));
                        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
                        gc.setTextAlign(TextAlignment.CENTER);
                        gc.fillText("CRAFT", x + GameController.TILE_SIZE / 2.0, y + 28);
                        gc.fillText("⚒",    x + GameController.TILE_SIZE / 2.0, y + 18);
                    }
                    case GameController.T_BOSS_DOOR -> {
                        gc.setFill(Color.web("#b71c1c")); gc.fillRect(x, y, GameController.TILE_SIZE, GameController.TILE_SIZE);
                        gc.setFill(Color.web("#c62828")); gc.fillRoundRect(x + 4, y + 4, GameController.TILE_SIZE - 8, GameController.TILE_SIZE - 8, 6, 6);
                        gc.setFill(Color.web("#ff5252"));
                        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
                        gc.setTextAlign(TextAlignment.CENTER);
                        gc.fillText("BOSS", x + GameController.TILE_SIZE / 2.0, y + 28);
                        gc.fillText("💀",   x + GameController.TILE_SIZE / 2.0, y + 18);
                    }
                }
                gc.setTextAlign(TextAlignment.LEFT);
            }
        }

        // Highlight facing tile (mining target)
        int[] ft = controller.facingTile();
        int[][] w = controller.getWorld();
        if (controller.inBounds(ft[0], ft[1])) {
            int t = w[ft[0]][ft[1]];
            boolean isOre = (t >= GameController.T_NORMAL_ROCK && t <= GameController.T_PLATINUM)
                    || t == GameController.T_MITHRIL || t == GameController.T_VIBRANIUM;
            if (isOre) {
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(3);
                gc.strokeRect(ft[1] * GameController.TILE_SIZE + 2, ft[0] * GameController.TILE_SIZE + 2,
                        GameController.TILE_SIZE - 4, GameController.TILE_SIZE - 4);
                gc.setLineWidth(1);
            }
        }

        // Highlight nearby buildings (SPACE prompt)
        int pc = (int)((controller.getPlayerX() + GameController.TILE_SIZE / 2.0) / GameController.TILE_SIZE);
        int pr = (int)((controller.getPlayerY() + GameController.TILE_SIZE / 2.0) / GameController.TILE_SIZE);
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++) {
                int r = pr + dr, c = pc + dc;
                if (!controller.inBounds(r, c)) continue;
                int t = w[r][c];
                if (t == GameController.T_SHOP || t == GameController.T_CRAFT || t == GameController.T_BOSS_DOOR) {
                    gc.setStroke(Color.CYAN);
                    gc.setLineWidth(2.5);
                    gc.strokeRect(c * GameController.TILE_SIZE + 2, r * GameController.TILE_SIZE + 2,
                            GameController.TILE_SIZE - 4, GameController.TILE_SIZE - 4);
                    gc.setFill(Color.CYAN);
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                    gc.setTextAlign(TextAlignment.CENTER);
                    gc.fillText("[SPACE]", c * GameController.TILE_SIZE + GameController.TILE_SIZE / 2.0,
                            r * GameController.TILE_SIZE - 4);
                    gc.setTextAlign(TextAlignment.LEFT);
                    gc.setLineWidth(1);
                }
            }
    }

    private void drawRock(GraphicsContext gc, double x, double y,
                          interfaces.Mineable stone, Color light, Color dark, String label) {
        gc.setFill(dark);
        gc.fillRect(x, y, GameController.TILE_SIZE, GameController.TILE_SIZE);
        gc.setFill(light);
        gc.fillRoundRect(x + 3, y + 3, GameController.TILE_SIZE - 6, GameController.TILE_SIZE - 6, 8, 8);
        gc.setStroke(dark.darker());
        gc.setLineWidth(1.5);
        gc.strokeLine(x + 12, y + 12, x + 20, y + 20);
        gc.strokeLine(x + 26, y + 14, x + 32, y + 24);
        gc.setLineWidth(1);
        gc.setFill(dark.darker());
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(label, x + GameController.TILE_SIZE / 2.0, y + 30);
        gc.setTextAlign(TextAlignment.LEFT);
        if (stone != null) {
            double pct = (double) stone.getDurability() / stone.getMaxDurability();
            gc.setFill(Color.rgb(0, 0, 0, 0.5));
            gc.fillRect(x + 4, y + GameController.TILE_SIZE - 8, GameController.TILE_SIZE - 8, 5);
            gc.setFill(pct > 0.5 ? Color.LIMEGREEN : pct > 0.25 ? Color.ORANGE : Color.RED);
            gc.fillRect(x + 4, y + GameController.TILE_SIZE - 8, (GameController.TILE_SIZE - 8) * pct, 5);
        }
    }

    // ── Monster rendering ─────────────────────────────────────────────────────

    private void drawMonsters(GraphicsContext gc) {
        for (GameController.MonsterEntity me : controller.getMonsters()) {
            if (!me.monster.isAlive()) continue;
            double x = me.x, y = me.y;

            gc.setFill(Color.rgb(0, 0, 0, 0.2));
            gc.fillOval(x + 6, y + 38, 36, 10);

            Image img = switch (me.type) {
                case 0  -> imgEasyMonster;
                case 1  -> imgMediumMonster;
                default -> imgHardMonster;
            };

            if (img != null && !img.isError()) {
                gc.drawImage(img, x, y, GameController.TILE_SIZE, GameController.TILE_SIZE);
            } else {
                gc.setFill(Color.RED);
                gc.fillRect(x, y, GameController.TILE_SIZE, GameController.TILE_SIZE);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("?", x + GameController.TILE_SIZE / 2.0, y + GameController.TILE_SIZE / 2.0);
                gc.setTextAlign(TextAlignment.LEFT);
            }

            // Aggro indicator
            if (me.aggro) {
                gc.setFill(Color.rgb(255, 50, 50, 0.4));
                gc.fillOval(x - 4, y - 4, GameController.TILE_SIZE + 8, GameController.TILE_SIZE + 8);
                gc.setFill(Color.RED);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("!", x + GameController.TILE_SIZE / 2.0, y);
                gc.setTextAlign(TextAlignment.LEFT);
            }

            // HP bar
            int hp = me.monster.getHealthPoint(), mhp = me.monster.getMaxHealthPoint();
            double pct = (double) hp / mhp;
            gc.setFill(Color.web("#1a0000", 0.6));
            gc.fillRect(x + 2, y + GameController.TILE_SIZE - 8, GameController.TILE_SIZE - 4, 5);
            gc.setFill(pct > 0.5 ? Color.LIMEGREEN : pct > 0.25 ? Color.ORANGE : Color.RED);
            gc.fillRect(x + 2, y + GameController.TILE_SIZE - 8, (GameController.TILE_SIZE - 4) * pct, 5);
            gc.setFont(Font.font("Arial", 9));
            String name = me.type == 0 ? "Rui" : me.type == 1 ? "Enmu" : "Daki";
            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(name + " " + hp + "/" + mhp,
                    x + GameController.TILE_SIZE / 2.0, y + GameController.TILE_SIZE + 10);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    // ── Player rendering ──────────────────────────────────────────────────────

    private void drawPlayer(GraphicsContext gc) {
        // Flicker when invincible (blink every other frame)
        if (controller.getInvincibleFrames() > 0 && controller.getAnimFrame() % 2 == 0) return;

        Image[] sprites = controller.isAttackAnim() ? playerSlashImgs : playerWalkImgs;
        Image sprite    = sprites[controller.getFacing()];

        if (sprite != null && !sprite.isError()) {
            gc.setFill(Color.rgb(0, 0, 0, 0.25));
            gc.fillOval(controller.getPlayerX() + 8, controller.getPlayerY() + 38, 32, 10);
            gc.drawImage(sprite, controller.getPlayerX(), controller.getPlayerY(),
                    GameController.TILE_SIZE, GameController.TILE_SIZE);
        }
    }

    // ── Floating texts ────────────────────────────────────────────────────────

    private void drawFloatingTexts(GraphicsContext gc) {
        long now = System.currentTimeMillis();
        gc.setTextAlign(TextAlignment.CENTER);
        for (GameController.FloatingText ft : controller.getFloatingTexts()) {
            double age   = (now - ft.born) / (double) ft.life;
            double alpha = Math.max(0, 1.0 - age);
            Color c = ft.color;
            gc.setFill(Color.color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.fillText(ft.text, ft.x + GameController.TILE_SIZE / 2.0, ft.y);
        }
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    private void drawHUD(GraphicsContext gc) {
        var player  = controller.getPlayer();
        var pickaxe = controller.getPickaxeHolder()[0];

        // Top bar
        gc.setFill(Color.rgb(0, 0, 0, 0.72));
        gc.fillRect(0, 0, W, 56);

        // HP bar
        double hpPct = (double) player.getHealth() / player.getMaxHealth();
        gc.setFill(Color.web("#7f0000"));
        gc.fillRoundRect(10, 8, 170, 16, 5, 5);
        gc.setFill(hpPct > 0.5 ? Color.web("#e53935") : hpPct > 0.25 ? Color.ORANGE : Color.RED);
        gc.fillRoundRect(10, 8, 170 * hpPct, 16, 5, 5);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.fillText("HP: " + player.getHealth() + " / " + player.getMaxHealth(), 14, 21);
        gc.setFill(Color.web("#ff8a65")); gc.fillText("ATK: " + player.getAttack(), 14, 42);
        gc.setFill(Color.web("#90caf9")); gc.fillText("DEF: " + player.getDefense(), 80, 42);

        // Gold & pickaxe
        gc.setFill(Color.web("#ffd700"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("GOLD: " + player.getGold() + "g", 200, 23);
        gc.setFill(Color.web("#b0bec5"));
        gc.setFont(Font.font("Arial", 11));
        gc.fillText("⛏ " + pickaxe.getName() + " (Pwr:" + pickaxe.getPower() + ")", 200, 42);

        // Equipped gear (centre top)
        String weaponStr = player.getEquippedWeapon() != null
                ? "⚔ " + player.getEquippedWeapon().getName() : "⚔ None";
        String armorStr  = player.getEquippedArmor()  != null
                ? "🛡 " + player.getEquippedArmor().getName()  : "🛡 None";
        gc.setFill(Color.rgb(0, 0, 0, 0.55));
        gc.fillRoundRect(W / 2.0 - 130, 4, 260, 48, 8, 8);
        gc.setFill(player.getEquippedWeapon() != null ? Color.web("#ffcc80") : Color.web("#757575"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(weaponStr, W / 2.0, 22);
        gc.setFill(player.getEquippedArmor() != null ? Color.web("#80cbc4") : Color.web("#757575"));
        gc.fillText(armorStr, W / 2.0, 42);
        gc.setTextAlign(TextAlignment.LEFT);

        // Monster count (top-right)
        long alive = controller.getMonsters().stream().filter(me -> me.monster.isAlive()).count();
        gc.setFill(alive == 0 ? Color.LIMEGREEN : Color.web("#ff8a80"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.fillText("Monsters: " + alive, W - 150, 23);
        if (alive == 0) gc.fillText("✓ Area clear!", W - 150, 42);

        // Inventory mini-panel (bottom-left)
        gc.setFill(Color.rgb(0, 0, 0, 0.70));
        gc.fillRoundRect(6, H - 106, 220, 100, 8, 8);
        gc.setFill(Color.web("#ffd54f"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.fillText("INVENTORY", 14, H - 92);
        List<ItemCounter> inv = player.getInventory();
        if (inv.isEmpty()) {
            gc.setFill(Color.LIGHTGRAY);
            gc.setFont(Font.font("Arial", 10));
            gc.fillText("(mine rocks to fill inventory)", 14, H - 78);
        } else {
            int shown = Math.min(inv.size(), 5);
            for (int i = 0; i < shown; i++) {
                ItemCounter ic = inv.get(i);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", 10));
                gc.fillText("• " + ic.getItem().getName() + ": " + ic.getCount(), 14, H - 78 + i * 14);
            }
            if (inv.size() > 5) {
                gc.setFill(Color.LIGHTGRAY);
                gc.fillText("...+" + (inv.size() - 5) + " more", 14, H - 78 + 5 * 14);
            }
        }

        // Controls panel (bottom-right)
        gc.setFill(Color.rgb(0, 0, 0, 0.70));
        gc.fillRoundRect(W - 190, H - 116, 184, 110, 8, 8);
        gc.setFill(Color.web("#80cbc4"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.fillText("CONTROLS", W - 180, H - 100);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 10));
        String[] lines = {"WASD-Move", "E-Inventory", "LMB-Attack", "RMB-Mine",
                "(yellow border = mine target)", "(cyan border = enter building)"};
        for (int i = 0; i < lines.length; i++)
            gc.fillText(lines[i], W - 180, H - 86 + i * 14);

        // Notification banner (fades out after NOTIF_DURATION)
        long age = System.currentTimeMillis() - controller.getNotifTime();
        if (age < GameController.NOTIF_DURATION && !controller.getNotifMsg().isEmpty()) {
            double a = age < 1800 ? 1.0 : 1.0 - (age - 1800) / 400.0;
            gc.setFill(Color.rgb(0, 0, 0, 0.75 * a));
            gc.fillRoundRect(W / 2.0 - 180, H - 138, 360, 28, 10, 10);
            gc.setFill(Color.rgb(255, 235, 59, a));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(controller.getNotifMsg(), W / 2.0, H - 119);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }
}

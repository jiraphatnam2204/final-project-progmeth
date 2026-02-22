package application;

import interfaces.Mineable;
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
import logic.base.BaseItem;
import logic.creatures.EasyMonster;
import logic.creatures.HardMonster;
import logic.creatures.MediumMonster;
import logic.creatures.Player;
import logic.pickaxe.Pickaxe;
import logic.stone.HardStone;
import logic.stone.Iron;
import logic.stone.Mithril;
import logic.stone.NormalStone;
import logic.stone.Platinum;
import logic.stone.Vibranium;
import logic.util.ItemCounter;

import java.util.*;

public class GameScene {
    static final int TILE_SIZE = 48;
    static final int COLS = 20;
    static final int ROWS = 15;
    static final int W = TILE_SIZE * COLS;
    static final int H = TILE_SIZE * ROWS;
    static final double PLAYER_SPEED = 3.2;

    static final int T_GROUND = 0;
    static final int T_GRASS = 1;
    static final int T_NORMAL_ROCK = 2;
    static final int T_HARD_ROCK = 3;
    static final int T_IRON_ROCK = 4;
    static final int T_PLATINUM  = 5;
    static final int T_MITHRIL   = 10;  // ใช้เลข 10 เพื่อไม่ชนกับค่าเดิม
    static final int T_VIBRANIUM = 11;
    static final int T_SHOP = 6; // shop building tile
    static final int T_CRAFT = 7; // crafting station tile
    static final int T_BOSS_DOOR = 8; // boss room door tile
    static final int T_PATH = 9; // stone path

    private static final long ATTACK_COOLDOWN = 600;
    private static final long MINE_COOLDOWN = 280;
    private static final long NOTIF_DURATION = 2200;
    private final int[][] world = new int[ROWS][COLS];
    private final Mineable[][] stoneObjects = new Mineable[ROWS][COLS];
    private final Player player;
    private final Pickaxe[] pickaxeHolder;

    private final Image[] playerWalkImgs = new Image[4];
    private final Image[] playerSlashImgs = new Image[4];
    private final List<MonsterEntity> monsters = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    private final Set<KeyCode> keys = new HashSet<>();
    private StackPane root = new StackPane();
    private Pane shopLayer;
    private ShopScene shopScene;
    private Pane craftingLayer;
    private CraftingScene craftingScene;
    private Pane inventoryLayer;
    private InventoryScene inventoryScene;
    private Pane bossLayer;
    private BossScene bossScene;
    private double playerX, playerY;
    private int facing = 2;
    private int animFrame = 0;
    private long lastAnimTime = 0;
    private int playerInvincibleFrames = 0;
    private boolean gameEnded = false;

    private AnimationTimer gameLoop;
    private Image imgEasyMonster;
    private Image imgMediumMonster;
    private Image imgHardMonster;

    private boolean isAttackAnim = false;
    private long attackAnimEndMs = 0;

    private long lastAttackTime = 0;
    private long lastMineTime = 0;
    private String notifMsg = "";
    private long notifTime = 0;

    private boolean leftMouseDown = false;
    private boolean rightMouseDown = false;

    // ── Respawn Queues ─────────────────────────────────────────────────────────
    // [row, col, tileType, respawnAtMs]
    private final List<long[]> oreRespawnQueue = new ArrayList<>();
    // [monsterType, respawnAtMs]
    private final List<long[]> monsterRespawnQueue = new ArrayList<>();
    private final Random spawnRng = new Random();

    private static final long ORE_RESPAWN_MIN_MS  = 1_000; // 10 วินาที
    private static final long ORE_RESPAWN_MAX_MS  = 2_000; // 20 วินาที
    private static final long MON_RESPAWN_MIN_MS  = 1_000; // 15 วินาที
    private static final long MON_RESPAWN_MAX_MS  = 3_000; // 30 วินาที

    public GameScene(Player player, Pickaxe pickaxe) {
        this.player = player;
        this.pickaxeHolder = new Pickaxe[]{pickaxe};
        shopScene = new ShopScene(player, pickaxeHolder, this::closeShop);
        shopLayer = shopScene.build();
        craftingScene = new CraftingScene(player, pickaxeHolder, this::closeCraft);
        craftingLayer = craftingScene.build();
        inventoryScene = new InventoryScene(player, this::closeInventory);
        inventoryLayer = inventoryScene.build();
        imgEasyMonster = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Rui.png")));
        imgMediumMonster = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Enmu.png")));
        imgHardMonster = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/Daki.png")));

        String[] dirs = {"up", "left", "down", "right"};
        for (int i = 0; i < 4; i++) {
            playerWalkImgs[i] = loadImage("/images/player_walk_" + dirs[i] + ".png");
            playerSlashImgs[i] = loadImage("/images/player_slash_" + dirs[i] + ".png");
        }
        generateWorld();
        spawnMonsters();
        this.playerX = 9 * TILE_SIZE;
        this.playerY = 7 * TILE_SIZE;
    }

    private Image loadImage(String resourcePath) {
        try {
            var stream = getClass().getResourceAsStream(resourcePath);
            if (stream == null) return null;
            return new Image(stream);
        } catch (Exception e) {
            System.out.println("Could not load image: " + resourcePath);
            return null;
        }
    }

    private void generateWorld() {
        Random rng = new Random(77);

        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                world[r][c] = rng.nextInt(10) < 4 ? T_GRASS : T_GROUND;

        for (int r = 0; r < ROWS; r++) {
            placeRock(r, 0, T_NORMAL_ROCK);
            placeRock(r, COLS - 1, T_NORMAL_ROCK);
        }
        for (int c = 0; c < COLS; c++) {
            placeRock(0, c, T_NORMAL_ROCK);
            placeRock(ROWS - 1, c, T_NORMAL_ROCK);
        }

        for (int c = 1; c < COLS - 1; c++) world[ROWS / 2][c] = T_PATH;
        for (int r = 1; r < ROWS - 1; r++) world[r][COLS / 2] = T_PATH;

        placeBuilding(2, 2, T_SHOP);
        placeBuilding(2, COLS - 4, T_CRAFT);
        placeBuilding(ROWS - 3, COLS / 2 - 1, T_BOSS_DOOR);

        int[][] rockSpecs = {
                {T_NORMAL_ROCK, 18},
                {T_HARD_ROCK,   12},
                {T_IRON_ROCK,    8},
                {T_PLATINUM,     4},
                {T_MITHRIL,      3},   // หายากกว่า
                {T_VIBRANIUM,    2},   // หายากที่สุด
        };
        for (int[] spec : rockSpecs)
            for (int i = 0; i < spec[1]; i++) {
                int r = 1 + rng.nextInt(ROWS - 2);
                int c = 1 + rng.nextInt(COLS - 2);
                if (world[r][c] == T_GROUND || world[r][c] == T_GRASS)
                    if (!isProtectedArea(r, c))
                        placeRock(r, c, spec[0]);
            }
    }

    private void placeBuilding(int r, int c, int type) {
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = 0; dc <= 1; dc++)
                if (inBounds(r + dr, c + dc)) world[r + dr][c + dc] = T_PATH;
        world[r][c] = type;
    }

    private boolean isProtectedArea(int r, int c) {
        if (Math.abs(r - ROWS / 2) < 3 && Math.abs(c - COLS / 2) < 3) return true;
        if (r <= 4 && c <= 5) return true;
        if (r <= 4 && c >= COLS - 6) return true;
        if (r >= ROWS - 5 && Math.abs(c - COLS / 2) < 4) return true;
        return false;
    }

    private void placeRock(int r, int c, int type) {
        world[r][c] = type;
        stoneObjects[r][c] = switch (type) {
            case T_NORMAL_ROCK -> new NormalStone();
            case T_HARD_ROCK   -> new HardStone();
            case T_IRON_ROCK   -> new Iron();
            case T_PLATINUM    -> new Platinum();
            case T_MITHRIL     -> new Mithril();
            case T_VIBRANIUM   -> new Vibranium();
            default -> null;
        };
    }

    private boolean inBounds(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    private void spawnMonsters() {
        Random rng = new Random(55);
        int[][] specs = {{0, 4}, {1, 2}, {2, 1}};  // {type, count}
        for (int[] spec : specs) {
            for (int i = 0; i < spec[1]; i++) {
                double mx, my;
                int tries = 0;
                do {
                    mx = (2 + rng.nextInt(COLS - 4)) * TILE_SIZE;
                    my = (2 + rng.nextInt(ROWS - 4)) * TILE_SIZE;
                    tries++;
                } while (isSolid(mx, my) || isNearPlayer(mx, my, 200) && tries < 30);

                logic.creatures.Monster m = switch (spec[0]) {
                    case 0 -> new EasyMonster();
                    case 1 -> new MediumMonster();
                    default -> new HardMonster();
                };
                monsters.add(new MonsterEntity(m, mx, my, spec[0]));
            }
        }
    }

    private boolean isNearPlayer(double x, double y, double dist) {
        return Math.hypot(x - playerX, y - playerY) < dist;
    }

    public Scene buildScene() {
        Canvas canvas = new Canvas(W, H);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);
        Scene scene = new Scene(root, W, H);
        shopLayer.setVisible(false);
        root.getChildren().add(shopLayer);
        craftingLayer.setVisible(false);
        root.getChildren().add(craftingLayer);
        inventoryLayer.setVisible(false);
        root.getChildren().add(inventoryLayer);
        scene.setOnKeyPressed(e -> {
            keys.add(e.getCode());

            if (e.getCode() == KeyCode.SPACE) {
                checkBuildingEntry();
            }
            if (e.getCode() == KeyCode.E) {
                toggleInventory();
            }
        });


        scene.setOnKeyReleased(e -> keys.remove(e.getCode()));

        scene.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) leftMouseDown = true;
            if (e.getButton() == MouseButton.SECONDARY) rightMouseDown = true;
        });

        scene.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) leftMouseDown = false;
            if (e.getButton() == MouseButton.SECONDARY) rightMouseDown = false;
        });

        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update(now);
                render(gc, now);
                if (shopLayer.isVisible()) {
                    shopScene.update();
                }
                if (craftingLayer.isVisible()) {
                    craftingScene.update();
                }
                if (inventoryLayer.isVisible()) {
                    inventoryScene.update();
                }
            }
        };
        gameLoop.start();

        return scene;
    }

    private void update(long nowNanos) {
        if (gameEnded) return;

        if (!player.isAlive()) {
            gameEnded = true;
            gameLoop.stop();
            Main.sceneManager.showGameOver(false, player);
            return;
        }
        if (!shopLayer.isVisible() && !inventoryLayer.isVisible() && !craftingLayer.isVisible()) {
            handleMovement();
            if (leftMouseDown) handleAttack(nowNanos);
            if (rightMouseDown) handleMining(nowNanos);
            updateMonsters();
            updateFloatingTexts();
            processRespawns(System.currentTimeMillis()); // ← เพิ่มบรรทัดนี้
        }


        if (playerInvincibleFrames > 0) playerInvincibleFrames--;

        if (isAttackAnim && System.currentTimeMillis() > attackAnimEndMs) {
            isAttackAnim = false;
        }
    }

    private void handleMovement() {
        double dx = 0, dy = 0;
        if (keys.contains(KeyCode.W) || keys.contains(KeyCode.UP)) {
            dy -= PLAYER_SPEED;
            facing = 0;
        }
        if (keys.contains(KeyCode.S) || keys.contains(KeyCode.DOWN)) {
            dy += PLAYER_SPEED;
            facing = 2;
        }
        if (keys.contains(KeyCode.A) || keys.contains(KeyCode.LEFT)) {
            dx -= PLAYER_SPEED;
            facing = 1;
        }
        if (keys.contains(KeyCode.D) || keys.contains(KeyCode.RIGHT)) {
            dx += PLAYER_SPEED;
            facing = 3;
        }
        if (dx != 0 && dy != 0) {
            dx *= 0.707;
            dy *= 0.707;
        }
        if (dx != 0 && canMoveTo(playerX + dx, playerY)) playerX += dx;
        if (dy != 0 && canMoveTo(playerX, playerY + dy)) playerY += dy;

        playerX = Math.max(0, Math.min(W - TILE_SIZE, playerX));
        playerY = Math.max(0, Math.min(H - TILE_SIZE, playerY));
    }

    private boolean canMoveTo(double nx, double ny) {
        int m = 5;
        return !isSolid(nx + m, ny + m) && !isSolid(nx + TILE_SIZE - m, ny + m)
                && !isSolid(nx + m, ny + TILE_SIZE - m) && !isSolid(nx + TILE_SIZE - m, ny + TILE_SIZE - m);
    }

    private boolean isSolid(double px, double py) {
        int c = (int) (px / TILE_SIZE), r = (int) (py / TILE_SIZE);
        if (!inBounds(r, c)) return true;
        int t = world[r][c];
        return (t >= T_NORMAL_ROCK && t <= T_PLATINUM) || t == T_MITHRIL || t == T_VIBRANIUM;
    }

    private void handleMining(long nowNanos) {
        long nowMs = nowNanos / 1_000_000;
        if (nowMs - lastMineTime < MINE_COOLDOWN) return;
        lastMineTime = nowMs;

        int[] ft = facingTile();
        int tr = ft[0], tc = ft[1];
        if (!inBounds(tr, tc)) return;
        int tile = world[tr][tc];
        boolean isOre = (tile >= T_NORMAL_ROCK && tile <= T_PLATINUM)
                || tile == T_MITHRIL || tile == T_VIBRANIUM;
        if (!isOre) return;
        Mineable stone = stoneObjects[tr][tc];
        if (stone == null || stone.isBroken()) return;

        List<BaseItem> drops = pickaxeHolder[0].use(stone, player);
        floatingTexts.add(new FloatingText(tc * TILE_SIZE + 12, tr * TILE_SIZE, "⛏", Color.WHITE, 700));

        if (stone.isBroken()) {
            int brokenTileType = tile; // บันทึก type ก่อน clear
            world[tr][tc] = T_GROUND;
            stoneObjects[tr][tc] = null;

            // Queue respawn แร่ที่ตำแหน่งเดิม
            long oreDelay = ORE_RESPAWN_MIN_MS + (long)(spawnRng.nextDouble() * (ORE_RESPAWN_MAX_MS - ORE_RESPAWN_MIN_MS));
            oreRespawnQueue.add(new long[]{tr, tc, brokenTileType, System.currentTimeMillis() + oreDelay});

            if (!drops.isEmpty()) {
                for (BaseItem item : drops) addToInventory(item);
                String name = drops.get(0).getName();
                showNotif("+ " + drops.size() + "x " + name);
                floatingTexts.add(new FloatingText(tc * TILE_SIZE, tr * TILE_SIZE,
                        "+" + drops.size() + " " + name, Color.YELLOW, 1500));
            }
        } else {
            showNotif("Mining... [" + stone.getDurability() + "/" + stone.getMaxDurability() + "]");
        }
    }

    private void addToInventory(BaseItem item) {
        for (ItemCounter ic : player.getInventory()) {
            if (ic.getItem().getName().equals(item.getName())) {
                ic.addCount(1);
                return;
            }
        }
        player.getInventory().add(new ItemCounter(item, 1));
    }

    private void handleAttack(long nowNanos) {
        long nowMs = nowNanos / 1_000_000;
        if (nowMs - lastAttackTime < ATTACK_COOLDOWN) return;
        lastAttackTime = nowMs;

        isAttackAnim = true;
        attackAnimEndMs = System.currentTimeMillis() + ATTACK_COOLDOWN;

        double range = TILE_SIZE * 1.6;
        boolean hit = false;
        Iterator<MonsterEntity> it = monsters.iterator();
        while (it.hasNext()) {
            MonsterEntity me = it.next();
            if (!me.monster.isAlive()) {
                it.remove();
                continue;
            }
            double dist = Math.hypot(playerX - me.x, playerY - me.y);
            if (dist < range) {
                player.attack(me.monster);
                int dmg = Math.max(1, player.getAttack() - me.monster.getDefense());
                floatingTexts.add(new FloatingText(me.x + 8, me.y - 8,
                        "-" + dmg, Color.web("#ff5252"), 1000));
                hit = true;
                if (!me.monster.isAlive()) {
                    int gold = me.monster.dropMoney();
                    player.setGold(player.getGold() + gold);
                    floatingTexts.add(new FloatingText(me.x, me.y - 20,
                            "+" + gold + "g !", Color.GOLD, 1800));
                    showNotif("Monster defeated! +" + gold + " gold");

                    // Queue respawn มอนสเตอร์ชนิดเดิม
                    long monDelay = MON_RESPAWN_MIN_MS + (long)(spawnRng.nextDouble() * (MON_RESPAWN_MAX_MS - MON_RESPAWN_MIN_MS));
                    monsterRespawnQueue.add(new long[]{me.type, System.currentTimeMillis() + monDelay});

                    it.remove();
                }
            }
        }
        if (!hit) showNotif("No monsters in range! (get closer)");
    }

    private void updateMonsters() {
        double aggroRange = TILE_SIZE * 5.0;
        double attackRange = TILE_SIZE * 1.2;
        double monSpd = 0.5;

        for (MonsterEntity me : monsters) {
            if (!me.monster.isAlive()) continue;
            double dist = Math.hypot(playerX - me.x, playerY - me.y);

            me.aggro = dist < aggroRange;

            if (me.aggro) {
                double nx = playerX - me.x;
                double ny = playerY - me.y;
                double len = Math.max(1, Math.hypot(nx, ny));
                double spd = monSpd * (1 + me.type * 0.3);
                me.x += (nx / len) * spd;
                me.y += (ny / len) * spd;

                if (dist < attackRange && playerInvincibleFrames <= 0) {
                    me.monster.attack(player);  // uses Monster.attack() from your code
                    playerInvincibleFrames = 50;
                    int dmg = Math.max(1, me.monster.getAttack() - player.getDefense());
                    floatingTexts.add(new FloatingText(playerX, playerY - 10,
                            "-" + dmg + " HP", Color.web("#ff1744"), 1200));
                }
            } else {
                me.moveTimer -= 1.0 / 60;
                if (me.moveTimer <= 0) {
                    me.moveTimer = 1.5 + Math.random() * 2;
                    double angle = Math.random() * Math.PI * 2;
                    me.dx = Math.cos(angle) * 0.6;
                    me.dy = Math.sin(angle) * 0.6;
                }
                double nx = me.x + me.dx;
                double ny = me.y + me.dy;
                if (!isSolid(nx + 5, ny + 5) && !isSolid(nx + TILE_SIZE - 5, ny + 5)
                        && !isSolid(nx + 5, ny + TILE_SIZE - 5) && !isSolid(nx + TILE_SIZE - 5, ny + TILE_SIZE - 5)) {
                    me.x = nx;
                    me.y = ny;
                }
            }
        }
    }

    private void processRespawns(long nowMs) {
        // ── Ore Respawns ─────────────────────────────────────────────────────
        Iterator<long[]> oreIt = oreRespawnQueue.iterator();
        while (oreIt.hasNext()) {
            long[] entry = oreIt.next();
            if (nowMs >= entry[3]) {
                int r = (int) entry[0];
                int c = (int) entry[1];
                int tileType = (int) entry[2];
                // respawn เฉพาะเมื่อช่องนั้นว่างอยู่
                if ((world[r][c] == T_GROUND || world[r][c] == T_GRASS)
                        && !isNearPlayer(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE)) {
                    // สุ่มชนิดแร่ใหม่ โดยน้ำหนักตามความหายาก
                    int[] orePool = {
                            T_NORMAL_ROCK, T_NORMAL_ROCK, T_NORMAL_ROCK, T_NORMAL_ROCK,  // 4/10 = 40%
                            T_HARD_ROCK,   T_HARD_ROCK,   T_HARD_ROCK,                    // 3/10 = 30%
                            T_IRON_ROCK,   T_IRON_ROCK,                                    // 2/10 = 20%
                            T_PLATINUM                                                     // 1/10 = 10%
                    };
                    // ถ้ามี Mithril และ Vibranium ให้ใช้ pool นี้แทน (ครอบคลุมทุกชนิด)
                    int[] fullOrePool = {
                            T_NORMAL_ROCK, T_NORMAL_ROCK, T_NORMAL_ROCK, T_NORMAL_ROCK,  // 4/12 ≈ 33%
                            T_HARD_ROCK,   T_HARD_ROCK,   T_HARD_ROCK,                    // 3/12 = 25%
                            T_IRON_ROCK,   T_IRON_ROCK,                                    // 2/12 ≈ 17%
                            T_PLATINUM,                                                    // 1/12 ≈  8%
                            T_MITHRIL,                                                     // 1/12 ≈  8%
                            T_VIBRANIUM                                                    // 1/12 ≈  8%
                    };
                    int randomTile = fullOrePool[spawnRng.nextInt(fullOrePool.length)];
                    placeRock(r, c, randomTile);

                    String oreName = switch (randomTile) {
                        case T_NORMAL_ROCK -> "Normal Stone";
                        case T_HARD_ROCK   -> "Hard Stone";
                        case T_IRON_ROCK   -> "Iron";
                        case T_PLATINUM    -> "Platinum";
                        case T_MITHRIL     -> "Mithril";
                        case T_VIBRANIUM   -> "Vibranium";
                        default            -> "Ore";
                    };
                    floatingTexts.add(new FloatingText(
                            c * TILE_SIZE, r * TILE_SIZE - 10,
                            "✨ " + oreName + " appeared!", Color.CYAN, 2000));
                }
                oreIt.remove();
            }
        }

        // ── Monster Respawns ──────────────────────────────────────────────────
        Iterator<long[]> monIt = monsterRespawnQueue.iterator();
        while (monIt.hasNext()) {
            long[] entry = monIt.next();
            if (nowMs >= entry[1]) {
                int type = (int) entry[0];
                double mx, my;
                int tries = 0;
                do {
                    mx = (2 + spawnRng.nextInt(COLS - 4)) * TILE_SIZE;
                    my = (2 + spawnRng.nextInt(ROWS - 4)) * TILE_SIZE;
                    tries++;
                } while ((isSolid(mx + 5, my + 5) || isNearPlayer(mx, my, 200)) && tries < 30);

                logic.creatures.Monster m = switch (type) {
                    case 0 -> new EasyMonster();
                    case 1 -> new MediumMonster();
                    default -> new HardMonster();
                };
                monsters.add(new MonsterEntity(m, mx, my, type));
                floatingTexts.add(new FloatingText(mx, my - 14,
                        "👹 Monster appeared!", Color.web("#ff5252"), 2200));
                monIt.remove();
            }
        }
    }

    private void toggleInventory() {
        boolean opening = !inventoryLayer.isVisible();
        inventoryLayer.setVisible(opening);
        if (opening) {
            inventoryScene.refresh();
        }

    }

    private void closeInventory() {
        inventoryLayer.setVisible(false);
    }

    private void toggleShopVisible() {
        if (shopLayer.isVisible()) shopLayer.setVisible(false);
        else shopLayer.setVisible(true);
    }

    private void closeCraft() {
        craftingLayer.setVisible(false);
    }

    private void closeShop() {
        shopLayer.setVisible(false);
    }

    private void toggleCraftVisisble() {
        if (craftingLayer.isVisible()) craftingLayer.setVisible(false);
        else craftingLayer.setVisible(true);
    }

    private void checkBuildingEntry() {
        if (!keys.contains(KeyCode.SPACE)) return;
        int pc = (int) ((playerX + TILE_SIZE / 2.0) / TILE_SIZE);
        int pr = (int) ((playerY + TILE_SIZE / 2.0) / TILE_SIZE);

        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++) {
                int r = pr + dr, c = pc + dc;
                if (!inBounds(r, c)) continue;
                switch (world[r][c]) {
                    case T_SHOP -> {
                        toggleShopVisible();
                    }
                    case T_CRAFT -> {
                        toggleCraftVisisble();
                        return;
                    }
                    case T_BOSS_DOOR -> {
                        gameLoop.stop();
                        Main.sceneManager.showBossRoom(player, pickaxeHolder);
                        return;
                    }
                }
            }
        showNotif("Nothing to enter nearby.");
    }

    private void updateFloatingTexts() {
        floatingTexts.removeIf(ft -> ft.isDead());
        for (FloatingText ft : floatingTexts) ft.y += ft.vy;
    }

    private int[] facingTile() {
        int pc = (int) ((playerX + TILE_SIZE / 2.0) / TILE_SIZE);
        int pr = (int) ((playerY + TILE_SIZE / 2.0) / TILE_SIZE);
        return switch (facing) {
            case 0 -> new int[]{pr - 1, pc};
            case 1 -> new int[]{pr, pc - 1};
            case 3 -> new int[]{pr, pc + 1};
            default -> new int[]{pr + 1, pc};
        };
    }

    private void showNotif(String msg) {
        notifMsg = msg;
        notifTime = System.currentTimeMillis();
    }

    private void render(GraphicsContext gc, long nowNanos) {
        if (nowNanos - lastAnimTime > 140_000_000L) {
            animFrame++;
            lastAnimTime = nowNanos;
        }
        drawWorld(gc);
        drawMonsters(gc);
        drawPlayer(gc);
        drawFloatingTexts(gc);
        drawHUD(gc);
    }

    private void drawWorld(GraphicsContext gc) {
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++) {
                double x = c * TILE_SIZE, y = r * TILE_SIZE;
                switch (world[r][c]) {
                    case T_GROUND -> {
                        gc.setFill(Color.web("#4a7c38"));
                        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                    }
                    case T_GRASS -> {
                        gc.setFill(Color.web("#3d6b2d"));
                        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                        gc.setFill(Color.web("#2d5220"));
                        gc.fillRect(x + 8, y + 10, 3, 8);
                        gc.fillRect(x + 20, y + 6, 3, 10);
                        gc.fillRect(x + 32, y + 12, 3, 7);
                    }
                    case T_PATH -> {
                        gc.setFill(Color.web("#8d7b6a"));
                        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                        gc.setStroke(Color.web("#7a6a5a", 0.4));
                        gc.strokeRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2);
                    }
                    case T_NORMAL_ROCK ->
                            drawRock(gc, x, y, stoneObjects[r][c], Color.web("#9e9e9e"), Color.web("#757575"), "N");
                    case T_HARD_ROCK ->
                            drawRock(gc, x, y, stoneObjects[r][c], Color.web("#78909c"), Color.web("#455a64"), "H");
                    case T_IRON_ROCK ->
                            drawRock(gc, x, y, stoneObjects[r][c], Color.web("#bf8f5b"), Color.web("#8d6030"), "Fe");
                    case T_PLATINUM ->
                            drawRock(gc, x, y, stoneObjects[r][c], Color.web("#90caf9"), Color.web("#1976d2"), "Pt");
                    case T_MITHRIL ->
                            drawRock(gc, x, y, stoneObjects[r][c], Color.web("#ce93d8"), Color.web("#7b1fa2"), "Mi");
                    case T_VIBRANIUM ->
                            drawRock(gc, x, y, stoneObjects[r][c], Color.web("#80cbc4"), Color.web("#00695c"), "Vb");
                    case T_SHOP -> {
                        gc.setFill(Color.web("#5d4037"));
                        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                        gc.setFill(Color.web("#795548"));
                        gc.fillRect(x + 4, y + 4, TILE_SIZE - 8, TILE_SIZE - 8);
                        gc.setFill(Color.web("#ffd54f"));
                        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                        gc.setTextAlign(TextAlignment.CENTER);
                        gc.fillText("SHOP", x + TILE_SIZE / 2.0, y + 28);
                        gc.setFill(Color.web("#ffd54f"));
                        gc.fillText("🛒", x + TILE_SIZE / 2.0, y + 18);
                    }
                    case T_CRAFT -> {
                        gc.setFill(Color.web("#1a237e"));
                        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                        gc.setFill(Color.web("#283593"));
                        gc.fillRect(x + 4, y + 4, TILE_SIZE - 8, TILE_SIZE - 8);
                        gc.setFill(Color.web("#80cbc4"));
                        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
                        gc.setTextAlign(TextAlignment.CENTER);
                        gc.fillText("CRAFT", x + TILE_SIZE / 2.0, y + 28);
                        gc.fillText("⚒", x + TILE_SIZE / 2.0, y + 18);
                    }
                    case T_BOSS_DOOR -> {
                        gc.setFill(Color.web("#b71c1c"));
                        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                        gc.setFill(Color.web("#c62828"));
                        gc.fillRoundRect(x + 4, y + 4, TILE_SIZE - 8, TILE_SIZE - 8, 6, 6);
                        gc.setFill(Color.web("#ff5252"));
                        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
                        gc.setTextAlign(TextAlignment.CENTER);
                        gc.fillText("BOSS", x + TILE_SIZE / 2.0, y + 28);
                        gc.fillText("💀", x + TILE_SIZE / 2.0, y + 18);
                    }
                }
                gc.setTextAlign(TextAlignment.LEFT);
            }

        int[] ft = facingTile();
        if (inBounds(ft[0], ft[1]) && world[ft[0]][ft[1]] >= T_NORMAL_ROCK && world[ft[0]][ft[1]] <= T_PLATINUM) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(3);
            gc.strokeRect(ft[1] * TILE_SIZE + 2, ft[0] * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
            gc.setLineWidth(1);
        }

        int pc = (int) ((playerX + TILE_SIZE / 2.0) / TILE_SIZE);
        int pr = (int) ((playerY + TILE_SIZE / 2.0) / TILE_SIZE);
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++) {
                int r = pr + dr, c = pc + dc;
                if (!inBounds(r, c)) continue;
                int t = world[r][c];
                if (t == T_SHOP || t == T_CRAFT || t == T_BOSS_DOOR) {
                    gc.setStroke(Color.CYAN);
                    gc.setLineWidth(2.5);
                    gc.strokeRect(c * TILE_SIZE + 2, r * TILE_SIZE + 2, TILE_SIZE - 4, TILE_SIZE - 4);
                    gc.setFill(Color.CYAN);
                    gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                    gc.setTextAlign(TextAlignment.CENTER);
                    gc.fillText("[SPACE]", c * TILE_SIZE + TILE_SIZE / 2.0, r * TILE_SIZE - 4);
                    gc.setTextAlign(TextAlignment.LEFT);
                    gc.setLineWidth(1);
                }
            }
    }

    private void drawRock(GraphicsContext gc, double x, double y,
                          Mineable stone, Color light, Color dark, String label) {
        gc.setFill(dark);
        gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        gc.setFill(light);
        gc.fillRoundRect(x + 3, y + 3, TILE_SIZE - 6, TILE_SIZE - 6, 8, 8);
        gc.setStroke(dark.darker());
        gc.setLineWidth(1.5);
        gc.strokeLine(x + 12, y + 12, x + 20, y + 20);
        gc.strokeLine(x + 26, y + 14, x + 32, y + 24);
        gc.setLineWidth(1);
        gc.setFill(dark.darker());
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(label, x + TILE_SIZE / 2.0, y + 30);
        gc.setTextAlign(TextAlignment.LEFT);
        if (stone != null) {
            double pct = (double) stone.getDurability() / stone.getMaxDurability();
            gc.setFill(Color.rgb(0, 0, 0, 0.5));
            gc.fillRect(x + 4, y + TILE_SIZE - 8, TILE_SIZE - 8, 5);
            gc.setFill(pct > 0.5 ? Color.LIMEGREEN : pct > 0.25 ? Color.ORANGE : Color.RED);
            gc.fillRect(x + 4, y + TILE_SIZE - 8, (TILE_SIZE - 8) * pct, 5);
        }
    }

    private void drawMonsters(GraphicsContext gc) {
        for (MonsterEntity me : monsters) {
            if (!me.monster.isAlive()) continue;
            double x = me.x, y = me.y;

            gc.setFill(Color.rgb(0, 0, 0, 0.2));
            gc.fillOval(x + 6, y + 38, 36, 10);

            Image img = switch (me.type) {
                case 0 -> imgEasyMonster;
                case 1 -> imgMediumMonster;
                default -> imgHardMonster;
            };

            if (img != null && !img.isError()) {
                gc.drawImage(img, x, y, TILE_SIZE, TILE_SIZE);
            } else {
                gc.setFill(Color.RED);
                gc.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("?", x + TILE_SIZE / 2.0, y + TILE_SIZE / 2.0);
                gc.setTextAlign(TextAlignment.LEFT);
            }

            if (me.aggro) {
                gc.setFill(Color.rgb(255, 50, 50, 0.4));
                gc.fillOval(x - 4, y - 4, TILE_SIZE + 8, TILE_SIZE + 8);
                gc.setFill(Color.RED);
                gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("!", x + TILE_SIZE / 2.0, y);
                gc.setTextAlign(TextAlignment.LEFT);
            }

            int hp = me.monster.getHealthPoint(), mhp = me.monster.getMaxHealthPoint();
            double pct = (double) hp / mhp;
            gc.setFill(Color.web("#1a0000", 0.6));
            gc.fillRect(x + 2, y + TILE_SIZE - 8, TILE_SIZE - 4, 5);
            gc.setFill(pct > 0.5 ? Color.LIMEGREEN : pct > 0.25 ? Color.ORANGE : Color.RED);
            gc.fillRect(x + 2, y + TILE_SIZE - 8, (TILE_SIZE - 4) * pct, 5);

            gc.setFont(Font.font("Arial", 9));
            String name = me.type == 0 ? "Rui" : me.type == 1 ? "Enmu" : "Daki";
            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(name + " " + hp + "/" + mhp, x + TILE_SIZE / 2.0, y + TILE_SIZE + 10);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    private void drawPlayer(GraphicsContext gc) {
        double px = playerX, py = playerY;

        if (playerInvincibleFrames > 0 && animFrame % 2 == 0) return;

        Image[] spriteSet = isAttackAnim ? playerSlashImgs : playerWalkImgs;
        Image sprite = spriteSet[facing];

        if (sprite != null && !sprite.isError()) {
            gc.setFill(Color.rgb(0, 0, 0, 0.25));
            gc.fillOval(px + 8, py + 38, 32, 10);
            gc.drawImage(sprite, px, py, TILE_SIZE, TILE_SIZE);
            return;
        }
    }

    private void drawFloatingTexts(GraphicsContext gc) {
        long now = System.currentTimeMillis();
        gc.setTextAlign(TextAlignment.CENTER);
        for (FloatingText ft : floatingTexts) {
            double age = (now - ft.born) / (double) ft.life;
            double alpha = Math.max(0, 1.0 - age);
            Color c = ft.color;
            gc.setFill(Color.color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.fillText(ft.text, ft.x + TILE_SIZE / 2.0, ft.y);
        }
        gc.setTextAlign(TextAlignment.LEFT);
    }

    private void drawHUD(GraphicsContext gc) {
        gc.setFill(Color.rgb(0, 0, 0, 0.72));
        gc.fillRect(0, 0, W, 56);

        double hpPct = (double) player.getHealth() / player.getMaxHealth();
        gc.setFill(Color.web("#7f0000"));
        gc.fillRoundRect(10, 8, 170, 16, 5, 5);
        gc.setFill(hpPct > 0.5 ? Color.web("#e53935") : hpPct > 0.25 ? Color.ORANGE : Color.RED);
        gc.fillRoundRect(10, 8, 170 * hpPct, 16, 5, 5);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.fillText("HP: " + player.getHealth() + " / " + player.getMaxHealth(), 14, 21);

        gc.setFill(Color.web("#ff8a65"));
        gc.fillText("ATK: " + player.getAttack(), 14, 42);

        gc.setFill(Color.web("#90caf9"));
        gc.fillText("DEF: " + player.getDefense(), 80, 42);

        gc.setFill(Color.web("#ffd700"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        gc.fillText("GOLD: " + player.getGold() + "g", 200, 23);

        gc.setFill(Color.web("#b0bec5"));
        gc.setFont(Font.font("Arial", 11));
        gc.fillText("⛏ " + pickaxeHolder[0].getName() + " (Pwr:" + pickaxeHolder[0].getPower() + ")", 200, 42);

        long alive = monsters.stream().filter(me -> me.monster.isAlive()).count();
        gc.setFill(alive == 0 ? Color.LIMEGREEN : Color.web("#ff8a80"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.fillText("Monsters: " + alive, W - 150, 23);
        if (alive == 0) gc.fillText("✓ Area clear!", W - 150, 42);

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

        gc.setFill(Color.rgb(0, 0, 0, 0.70));
        gc.fillRoundRect(W - 190, H - 116, 184, 110, 8, 8);
        gc.setFill(Color.web("#80cbc4"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.fillText("CONTROLS", W - 180, H - 100);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 10));
        String[] lines = {"WASD-Move", "E-Enter building", "LMB-Attack", "RMB-Mine", "(yellow border = mine target)", "(cyan border = enter building)"};
        for (int i = 0; i < lines.length; i++) gc.fillText(lines[i], W - 180, H - 86 + i * 14);

        long age = System.currentTimeMillis() - notifTime;
        if (age < NOTIF_DURATION && !notifMsg.isEmpty()) {
            double a = age < 1800 ? 1.0 : 1.0 - (age - 1800) / 400.0;
            gc.setFill(Color.rgb(0, 0, 0, 0.75 * a));
            gc.fillRoundRect(W / 2.0 - 180, H - 138, 360, 28, 10, 10);
            gc.setFill(Color.rgb(255, 235, 59, a));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(notifMsg, W / 2.0, H - 119);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    private static class MonsterEntity {
        logic.creatures.Monster monster;
        double x, y;
        int type;
        double moveTimer;
        double dx, dy;
        boolean aggro;

        MonsterEntity(logic.creatures.Monster m, double x, double y, int type) {
            this.monster = m;
            this.x = x;
            this.y = y;
            this.type = type;
            this.moveTimer = 1 + Math.random() * 2;
        }
    }

    private static class FloatingText {
        double x, y, vy;
        String text;
        Color color;
        long born;
        long life;

        FloatingText(double x, double y, String text, Color c, long lifeMs) {
            this.x = x;
            this.y = y;
            this.vy = -1.2;
            this.text = text;
            this.color = c;
            this.born = System.currentTimeMillis();
            this.life = lifeMs;
        }

        boolean isDead() {
            return System.currentTimeMillis() - born > life;
        }
    }
}
package scenes.game;

import application.Main;
import interfaces.Mineable;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import logic.base.BaseItem;
import logic.creatures.*;
import logic.pickaxe.Pickaxe;
import logic.stone.*;
import logic.util.ItemCounter;

import java.util.*;

/**
 * GameController — the "brain" of the main game world.
 * <p>
 * Responsibility: ALL game logic. Zero JavaFX drawing.
 * - Holds the world tile grid and stone objects
 * - Manages player position and movement
 * - Handles attacking and mining
 * - Runs monster AI
 * - Manages respawn queues for ores and monsters
 * - Tracks input state (held keys and mouse buttons)
 * - Notifies View about visual events via getters/flags
 */
public class GameController {

    // ── World constants ────────────────────────────────────────────────────────
    public static final int TILE_SIZE = 48;
    public static final int COLS = 20;
    public static final int ROWS = 15;
    public static final int W = TILE_SIZE * COLS;
    public static final int H = TILE_SIZE * ROWS;
    public static final double PLAYER_SPEED = 3.2;

    // Tile type constants
    public static final int T_GROUND = 0;
    public static final int T_GRASS = 1;
    public static final int T_NORMAL_ROCK = 2;
    public static final int T_HARD_ROCK = 3;
    public static final int T_IRON_ROCK = 4;
    public static final int T_PLATINUM = 5;
    public static final int T_MITHRIL = 10;
    public static final int T_VIBRANIUM = 11;
    public static final int T_SHOP = 6;
    public static final int T_CRAFT = 7;
    public static final int T_BOSS_DOOR = 8;
    public static final int T_PATH = 9;
    public static final long NOTIF_DURATION = 2200;
    // Cooldowns / durations (milliseconds)
    private static final long ATTACK_COOLDOWN = 600;
    private static final long MINE_COOLDOWN = 280;
    private static final long ORE_RESPAWN_MIN = 1_000;
    private static final long ORE_RESPAWN_MAX = 2_000;
    private static final long MON_RESPAWN_MIN = 1_000;
    private static final long MON_RESPAWN_MAX = 3_000;

    // ── World data ─────────────────────────────────────────────────────────────
    private final int[][] world = new int[ROWS][COLS];
    private final Mineable[][] stoneObjects = new Mineable[ROWS][COLS];

    // ── Entities ───────────────────────────────────────────────────────────────
    private final Player player;
    private final Pickaxe[] pickaxeHolder;
    private final List<MonsterEntity> monsters = new ArrayList<>();

    // ── Floating damage/pickup texts (read by View for rendering) ─────────────
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    // ── Input state ────────────────────────────────────────────────────────────
    private final Set<KeyCode> keys = new HashSet<>();
    // ── Respawn queues ─────────────────────────────────────────────────────────
    // Ore queue: [row, col, tileType, respawnAtMs]
    private final List<long[]> oreRespawnQueue = new ArrayList<>();
    // Monster queue: [monsterType, respawnAtMs]
    private final List<long[]> monsterRespawnQueue = new ArrayList<>();
    private final Random spawnRng = new Random();
    private boolean leftMouseDown = false;
    private boolean rightMouseDown = false;
    // ── Player movement/animation state ───────────────────────────────────────
    private double playerX, playerY;
    private int facing = 2; // 0=up 1=left 2=down 3=right
    private int animFrame = 0;
    private long lastAnimTime = 0;
    private int playerInvincibleFrames = 0;
    // ── Attack animation state ─────────────────────────────────────────────────
    private boolean isAttackAnim = false;
    private long attackAnimEndMs = 0;
    private long lastAttackTime = 0;
    // ── Mining ────────────────────────────────────────────────────────────────
    private long lastMineTime = 0;
    // ── Notification banner ────────────────────────────────────────────────────
    private String notifMsg = "";
    private long notifTime = 0;
    // ── Game-ended flag ────────────────────────────────────────────────────────
    private boolean gameEnded = false;

    public GameController(Player player, Pickaxe pickaxe) {
        this.player = player;
        this.pickaxeHolder = new Pickaxe[]{pickaxe};
        generateWorld();
        spawnMonsters();
        this.playerX = 9 * TILE_SIZE;
        this.playerY = 7 * TILE_SIZE;
    }

    // ── World generation ──────────────────────────────────────────────────────

    private void generateWorld() {
        Random rng = new Random(77);
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                world[r][c] = rng.nextInt(10) < 4 ? T_GRASS : T_GROUND;

        // Border walls
        for (int r = 0; r < ROWS; r++) {
            placeRock(r, 0, T_NORMAL_ROCK);
            placeRock(r, COLS - 1, T_NORMAL_ROCK);
        }
        for (int c = 0; c < COLS; c++) {
            placeRock(0, c, T_NORMAL_ROCK);
            placeRock(ROWS - 1, c, T_NORMAL_ROCK);
        }

        // Cross-shaped stone paths
        for (int c = 1; c < COLS - 1; c++) world[ROWS / 2][c] = T_PATH;
        for (int r = 1; r < ROWS - 1; r++) world[r][COLS / 2] = T_PATH;

        // Buildings
        placeBuilding(2, 2, T_SHOP);
        placeBuilding(2, COLS - 4, T_CRAFT);
        placeBuilding(ROWS - 3, COLS / 2 - 1, T_BOSS_DOOR);

        // Ore deposits — each spec is {tileType, count}
        int[][] rockSpecs = {
                {T_NORMAL_ROCK, 18}, {T_HARD_ROCK, 12}, {T_IRON_ROCK, 8},
                {T_PLATINUM, 4}, {T_MITHRIL, 3}, {T_VIBRANIUM, 2}
        };
        for (int[] spec : rockSpecs)
            for (int i = 0; i < spec[1]; i++) {
                int r = 1 + rng.nextInt(ROWS - 2);
                int c = 1 + rng.nextInt(COLS - 2);
                if ((world[r][c] == T_GROUND || world[r][c] == T_GRASS) && !isProtectedArea(r, c))
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

    public void placeRock(int r, int c, int type) {
        world[r][c] = type;
        stoneObjects[r][c] = switch (type) {
            case T_NORMAL_ROCK -> new NormalStone();
            case T_HARD_ROCK -> new HardStone();
            case T_IRON_ROCK -> new Iron();
            case T_PLATINUM -> new Platinum();
            case T_MITHRIL -> new Mithril();
            case T_VIBRANIUM -> new Vibranium();
            default -> null;
        };
    }

    // ── Monster spawning ─────────────────────────────────────────────────────

    private void spawnMonsters() {
        Random rng = new Random(55);
        int[][] specs = {{0, 4}, {1, 2}, {2, 1}}; // {type, count}
        for (int[] spec : specs)
            for (int i = 0; i < spec[1]; i++) {
                double mx, my;
                int tries = 0;
                do {
                    mx = (2 + rng.nextInt(COLS - 4)) * TILE_SIZE;
                    my = (2 + rng.nextInt(ROWS - 4)) * TILE_SIZE;
                    tries++;
                } while (isSolid(mx, my) || isNearPlayer(mx, my, 200) && tries < 30);

                Monster m = switch (spec[0]) {
                    case 0 -> new EasyMonster();
                    case 1 -> new MediumMonster();
                    default -> new HardMonster();
                };
                monsters.add(new MonsterEntity(m, mx, my, spec[0]));
            }
    }

    // ── Main update (called every frame) ─────────────────────────────────────

    /**
     * Main game-logic update.
     * Call this every frame BEFORE rendering.
     * Returns false if the game should end (player died).
     */
    public boolean update(long nowNanos, AnimationTimer gameLoop) {
        if (gameEnded) return false;

        // Update animation frame counter
        if (nowNanos - lastAnimTime > 140_000_000L) {
            animFrame++;
            lastAnimTime = nowNanos;
        }

        // Player death check
        if (!player.isAlive()) {
            gameEnded = true;
            gameLoop.stop();
            Main.sceneManager.showGameOver(false, player);
            return false;
        }

        handleMovement();
        if (leftMouseDown) handleAttack(nowNanos);
        if (rightMouseDown) handleMining(nowNanos);
        updateMonsters();
        updateFloatingTexts();
        processRespawns(System.currentTimeMillis());

        if (playerInvincibleFrames > 0) playerInvincibleFrames--;
        if (isAttackAnim && System.currentTimeMillis() > attackAnimEndMs) isAttackAnim = false;

        return true;
    }

    // ── Input handlers ────────────────────────────────────────────────────────

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

        // Diagonal normalisation — prevents faster diagonal movement
        if (dx != 0 && dy != 0) {
            dx *= 0.707;
            dy *= 0.707;
        }

        if (dx != 0 && canMoveTo(playerX + dx, playerY)) playerX += dx;
        if (dy != 0 && canMoveTo(playerX, playerY + dy)) playerY += dy;
        playerX = Math.max(0, Math.min(W - TILE_SIZE, playerX));
        playerY = Math.max(0, Math.min(H - TILE_SIZE, playerY));
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
                floatingTexts.add(new FloatingText(me.x + 8, me.y - 8, "-" + dmg, floatingRed(), 1000));
                hit = true;
                if (!me.monster.isAlive()) {
                    int gold = me.monster.dropMoney();
                    player.setGold(player.getGold() + gold);
                    floatingTexts.add(new FloatingText(me.x, me.y - 20, "+" + gold + "g!", java.awt.Color.YELLOW == null ? javafx.scene.paint.Color.GOLD : javafx.scene.paint.Color.GOLD, 1800));
                    showNotif("Monster defeated! +" + gold + " gold");

                    long delay = MON_RESPAWN_MIN + (long) (spawnRng.nextDouble() * (MON_RESPAWN_MAX - MON_RESPAWN_MIN));
                    monsterRespawnQueue.add(new long[]{me.type, System.currentTimeMillis() + delay});
                    it.remove();
                }
            }
        }
        if (!hit) showNotif("No monsters in range! (get closer)");
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
        floatingTexts.add(new FloatingText(tc * TILE_SIZE + 12, tr * TILE_SIZE, "⛏", javafx.scene.paint.Color.WHITE, 700));

        if (stone.isBroken()) {
            int brokenType = tile;
            world[tr][tc] = T_GROUND;
            stoneObjects[tr][tc] = null;

            long delay = ORE_RESPAWN_MIN + (long) (spawnRng.nextDouble() * (ORE_RESPAWN_MAX - ORE_RESPAWN_MIN));
            oreRespawnQueue.add(new long[]{tr, tc, brokenType, System.currentTimeMillis() + delay});

            if (!drops.isEmpty()) {
                for (BaseItem item : drops) addToInventory(item);
                String name = drops.get(0).getName();
                showNotif("+ " + drops.size() + "x " + name);
                floatingTexts.add(new FloatingText(tc * TILE_SIZE, tr * TILE_SIZE,
                        "+" + drops.size() + " " + name, javafx.scene.paint.Color.YELLOW, 1500));
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

    // ── Monster AI ────────────────────────────────────────────────────────────

    private void updateMonsters() {
        double aggroRange = TILE_SIZE * 5.0;
        double attackRange = TILE_SIZE * 1.2;

        for (MonsterEntity me : monsters) {
            if (!me.monster.isAlive()) continue;
            double dist = Math.hypot(playerX - me.x, playerY - me.y);
            me.aggro = dist < aggroRange;

            if (me.aggro) {
                // Chase player
                double nx = playerX - me.x, ny = playerY - me.y;
                double len = Math.max(1, Math.hypot(nx, ny));
                double spd = 0.5 * (1 + me.type * 0.3);
                me.x += (nx / len) * spd;
                me.y += (ny / len) * spd;

                if (dist < attackRange && playerInvincibleFrames <= 0) {
                    me.monster.attack(player);
                    playerInvincibleFrames = 50;
                    int dmg = Math.max(1, me.monster.getAttack() - player.getDefense());
                    floatingTexts.add(new FloatingText(playerX, playerY - 10, "-" + dmg + " HP",
                            javafx.scene.paint.Color.web("#ff1744"), 1200));
                }
            } else {
                // Wander randomly
                me.moveTimer -= 1.0 / 60;
                if (me.moveTimer <= 0) {
                    me.moveTimer = 1.5 + Math.random() * 2;
                    double angle = Math.random() * Math.PI * 2;
                    me.dx = Math.cos(angle) * 0.6;
                    me.dy = Math.sin(angle) * 0.6;
                }
                double nx = me.x + me.dx, ny = me.y + me.dy;
                if (!isSolid(nx + 5, ny + 5) && !isSolid(nx + TILE_SIZE - 5, ny + 5)
                        && !isSolid(nx + 5, ny + TILE_SIZE - 5) && !isSolid(nx + TILE_SIZE - 5, ny + TILE_SIZE - 5)) {
                    me.x = nx;
                    me.y = ny;
                }
            }
        }
    }

    // ── Respawn system ────────────────────────────────────────────────────────

    private void processRespawns(long nowMs) {
        int[] fullOrePool = {
                T_NORMAL_ROCK, T_NORMAL_ROCK, T_NORMAL_ROCK, T_NORMAL_ROCK,
                T_HARD_ROCK, T_HARD_ROCK, T_HARD_ROCK,
                T_IRON_ROCK, T_IRON_ROCK,
                T_PLATINUM, T_MITHRIL, T_VIBRANIUM
        };

        // Ore respawns
        oreRespawnQueue.removeIf(entry -> {
            if (nowMs < entry[3]) return false;
            int r = (int) entry[0], c = (int) entry[1];
            if ((world[r][c] == T_GROUND || world[r][c] == T_GRASS)
                    && !isNearPlayer(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE)) {
                int randomTile = fullOrePool[spawnRng.nextInt(fullOrePool.length)];
                placeRock(r, c, randomTile);
                String oreName = switch (randomTile) {
                    case T_NORMAL_ROCK -> "Normal Stone";
                    case T_HARD_ROCK -> "Hard Stone";
                    case T_IRON_ROCK -> "Iron";
                    case T_PLATINUM -> "Platinum";
                    case T_MITHRIL -> "Mithril";
                    case T_VIBRANIUM -> "Vibranium";
                    default -> "Ore";
                };
                floatingTexts.add(new FloatingText(c * TILE_SIZE, r * TILE_SIZE - 10,
                        "✨ " + oreName + " appeared!", javafx.scene.paint.Color.CYAN, 2000));
            }
            return true;
        });

        // Monster respawns
        monsterRespawnQueue.removeIf(entry -> {
            if (nowMs < entry[1]) return false;
            int type = (int) entry[0];
            double mx, my;
            int tries = 0;
            do {
                mx = (2 + spawnRng.nextInt(COLS - 4)) * TILE_SIZE;
                my = (2 + spawnRng.nextInt(ROWS - 4)) * TILE_SIZE;
                tries++;
            } while ((isSolid(mx + 5, my + 5) || isNearPlayer(mx, my, 200)) && tries < 30);

            Monster m = switch (type) {
                case 0 -> new EasyMonster();
                case 1 -> new MediumMonster();
                default -> new HardMonster();
            };
            monsters.add(new MonsterEntity(m, mx, my, type));
            floatingTexts.add(new FloatingText(mx, my - 14, "👹 Monster appeared!",
                    javafx.scene.paint.Color.web("#ff5252"), 2200));
            return true;
        });
    }

    // ── Building interaction ──────────────────────────────────────────────────

    /**
     * Checks what building (if any) the player is standing next to.
     * Returns a BuildingType enum so the caller (View/GameView) can open the right overlay.
     */
    public BuildingType checkBuildingEntry() {
        int pc = (int) ((playerX + TILE_SIZE / 2.0) / TILE_SIZE);
        int pr = (int) ((playerY + TILE_SIZE / 2.0) / TILE_SIZE);
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++) {
                int r = pr + dr, c = pc + dc;
                if (!inBounds(r, c)) continue;
                switch (world[r][c]) {
                    case T_SHOP:
                        return BuildingType.SHOP;
                    case T_CRAFT:
                        return BuildingType.CRAFT;
                    case T_BOSS_DOOR:
                        return BuildingType.BOSS;
                }
            }
        showNotif("Nothing to enter nearby.");
        return BuildingType.NONE;
    }

    private void updateFloatingTexts() {
        floatingTexts.removeIf(FloatingText::isDead);
        for (FloatingText ft : floatingTexts) ft.y += ft.vy;
    }

    // ── Floating text cleanup ─────────────────────────────────────────────────

    private boolean canMoveTo(double nx, double ny) {
        int m = 5;
        return !isSolid(nx + m, ny + m) && !isSolid(nx + TILE_SIZE - m, ny + m)
                && !isSolid(nx + m, ny + TILE_SIZE - m) && !isSolid(nx + TILE_SIZE - m, ny + TILE_SIZE - m);
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    public boolean isSolid(double px, double py) {
        int c = (int) (px / TILE_SIZE), r = (int) (py / TILE_SIZE);
        if (!inBounds(r, c)) return true;
        int t = world[r][c];
        return (t >= T_NORMAL_ROCK && t <= T_PLATINUM) || t == T_MITHRIL || t == T_VIBRANIUM;
    }

    public boolean inBounds(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    private boolean isNearPlayer(double x, double y, double dist) {
        return Math.hypot(x - playerX, y - playerY) < dist;
    }

    /**
     * Returns [row, col] of the tile the player is currently facing.
     */
    public int[] facingTile() {
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

    private javafx.scene.paint.Color floatingRed() {
        return javafx.scene.paint.Color.web("#ff5252");
    }

    public void keyPressed(KeyCode key) {
        keys.add(key);
    }

    // ── Input setters (called from View's event handlers) ─────────────────────

    public void keyReleased(KeyCode key) {
        keys.remove(key);
    }

    public void setLeftMouse(boolean on) {
        leftMouseDown = on;
    }

    public void setRightMouse(boolean on) {
        rightMouseDown = on;
    }

    public Player getPlayer() {
        return player;
    }

    // ── Getters for View ──────────────────────────────────────────────────────

    public Pickaxe[] getPickaxeHolder() {
        return pickaxeHolder;
    }

    public int[][] getWorld() {
        return world;
    }

    public Mineable[][] getStoneObjects() {
        return stoneObjects;
    }

    public List<MonsterEntity> getMonsters() {
        return monsters;
    }

    public List<FloatingText> getFloatingTexts() {
        return floatingTexts;
    }

    public double getPlayerX() {
        return playerX;
    }

    public double getPlayerY() {
        return playerY;
    }

    public int getFacing() {
        return facing;
    }

    public int getAnimFrame() {
        return animFrame;
    }

    public boolean isAttackAnim() {
        return isAttackAnim;
    }

    public int getInvincibleFrames() {
        return playerInvincibleFrames;
    }

    public String getNotifMsg() {
        return notifMsg;
    }

    public long getNotifTime() {
        return notifTime;
    }

    public enum BuildingType {NONE, SHOP, CRAFT, BOSS}

    // ── Inner classes ─────────────────────────────────────────────────────────

    /**
     * Holds a monster instance along with its world position and AI state.
     */
    public static class MonsterEntity {
        public Monster monster;
        public double x, y, moveTimer, dx, dy;
        public int type;
        public boolean aggro;

        public MonsterEntity(Monster m, double x, double y, int type) {
            this.monster = m;
            this.x = x;
            this.y = y;
            this.type = type;
            this.moveTimer = 1 + Math.random() * 2;
        }
    }

    /**
     * A piece of text that floats upward and fades out over time.
     */
    public static class FloatingText {
        public double x, y, vy;
        public String text;
        public javafx.scene.paint.Color color;
        public long born, life;

        public FloatingText(double x, double y, String text,
                            javafx.scene.paint.Color c, long lifeMs) {
            this.x = x;
            this.y = y;
            this.vy = -1.2;
            this.text = text;
            this.color = c;
            this.born = System.currentTimeMillis();
            this.life = lifeMs;
        }

        public boolean isDead() {
            return System.currentTimeMillis() - born > life;
        }
    }
}

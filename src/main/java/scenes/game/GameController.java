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
 * Controller for the main game world scene.
 * Manages the tile-based world map, player movement, combat, mining,
 * monster AI, ore/monster respawning, and floating text notifications.
 */
public class GameController {

    /** Width in pixels of a single world tile. */
    public static final int TILE_SIZE = 48;

    /** Number of tile columns in the world map. */
    public static final int COLS = 20;

    /** Number of tile rows in the world map. */
    public static final int ROWS = 15;

    /** Total world width in pixels ({@code TILE_SIZE * COLS}). */
    public static final int W = TILE_SIZE * COLS;

    /** Total world height in pixels ({@code TILE_SIZE * ROWS}). */
    public static final int H = TILE_SIZE * ROWS;

    /** Player movement speed in pixels per frame. */
    public static final double PLAYER_SPEED = 7.5;

    /** Tile type: plain ground. */
    public static final int T_GROUND = 0;

    /** Tile type: grass (decorative ground variant). */
    public static final int T_GRASS = 1;

    /** Tile type: normal stone rock (solid, mineable). */
    public static final int T_NORMAL_ROCK = 2;

    /** Tile type: hard stone rock (solid, mineable). */
    public static final int T_HARD_ROCK = 3;

    /** Tile type: iron ore rock (solid, mineable). */
    public static final int T_IRON_ROCK = 4;

    /** Tile type: platinum ore rock (solid, mineable). */
    public static final int T_PLATINUM = 5;

    /** Tile type: mithril ore rock (solid, mineable). */
    public static final int T_MITHRIL = 10;

    /** Tile type: vibranium ore rock (solid, mineable). */
    public static final int T_VIBRANIUM = 11;

    /** Tile type: the shop building entrance. */
    public static final int T_SHOP = 6;

    /** Tile type: the crafting station entrance. */
    public static final int T_CRAFT = 7;

    /** Tile type: the boss door entrance. */
    public static final int T_BOSS_DOOR = 8;

    /** Tile type: decorative path tile. */
    public static final int T_PATH = 9;

    /** Duration in milliseconds that a notification message is displayed. */
    public static final long NOTIF_DURATION = 2200;

    /** Milliseconds between successive player attacks. */
    private static final long ATTACK_COOLDOWN = 600;

    /** Milliseconds between successive mining hits. */
    private static final long MINE_COOLDOWN = 280;

    /** Minimum milliseconds before a mined ore respawns. */
    private static final long ORE_RESPAWN_MIN = 1_000;

    /** Maximum milliseconds before a mined ore respawns. */
    private static final long ORE_RESPAWN_MAX = 2_000;

    /** Minimum milliseconds before a defeated monster respawns. */
    private static final long MON_RESPAWN_MIN = 1_000;

    /** Maximum milliseconds before a defeated monster respawns. */
    private static final long MON_RESPAWN_MAX = 3_000;

    /** The 2-D tile-type grid representing the world map. */
    private final int[][] world = new int[ROWS][COLS];

    /** Mineable stone/ore objects associated with each tile position. */
    private final Mineable[][] stoneObjects = new Mineable[ROWS][COLS];

    /** The player character. */
    private final Player player;

    /** Single-element array holding the player's active pickaxe (shared by reference). */
    private final Pickaxe[] pickaxeHolder;

    /** Live monster entities currently present on the world map. */
    private final List<MonsterEntity> monsters = new ArrayList<>();

    /** Active floating-text pop-ups (damage numbers, notifications). */
    private final List<FloatingText> floatingTexts = new ArrayList<>();

    /** Set of keyboard keys currently held down. */
    private final Set<KeyCode> keys = new HashSet<>();

    /** Pending ore respawn entries: each is {@code {row, col, tileType, respawnTimeMs}}. */
    private final List<long[]> oreRespawnQueue = new ArrayList<>();

    /** Pending monster respawn entries: each is {@code {type, respawnTimeMs}}. */
    private final List<long[]> monsterRespawnQueue = new ArrayList<>();

    /** Random number generator used for respawn position calculations. */
    private final Random spawnRng = new Random();

    /** {@code true} while the left mouse button is held (attack action). */
    private boolean leftMouseDown = false;

    /** {@code true} while the right mouse button is held (mine action). */
    private boolean rightMouseDown = false;

    /** Player's current X position in pixels. */
    private double playerX;

    /** Player's current Y position in pixels. */
    private double playerY;

    /** Direction the player is facing: 0=up, 1=left, 2=down, 3=right. */
    private int facing = 2;

    /** Current animation frame index, incremented over time. */
    private int animFrame = 0;

    /** System time (nanoseconds) of the last animation frame increment. */
    private long lastAnimTime = 0;

    /** Remaining invincibility frames after the player takes damage. */
    private int playerInvincibleFrames = 0;

    /** {@code true} while the attack animation should be shown. */
    private boolean isAttackAnim = false;

    /** System time (ms) at which the current attack animation should end. */
    private long attackAnimEndMs = 0;

    /** System time (ms) of the player's last successful attack. */
    private long lastAttackTime = 0;

    /** System time (ms) of the player's last mining hit. */
    private long lastMineTime = 0;

    /** The most recently triggered notification message to display on-screen. */
    private String notifMsg = "";

    /** System time (ms) when the current notification message was set. */
    private long notifTime = 0;

    /** {@code true} once the game has ended (player died or transitioned away). */
    private boolean gameEnded = false;

    /**
     * Creates a new GameController, generates the world, spawns monsters,
     * and positions the player at the centre of the map.
     *
     * @param player  the player character
     * @param pickaxe the player's starting pickaxe
     */
    public GameController(Player player, Pickaxe pickaxe) {
        this.player = player;
        this.pickaxeHolder = new Pickaxe[]{pickaxe};
        generateWorld();
        spawnMonsters();
        this.playerX = 9 * TILE_SIZE;
        this.playerY = 7 * TILE_SIZE;
    }

    /**
     * Procedurally generates the world map: ground/grass tiles, border walls, paths,
     * buildings, and initial ore deposits.
     */
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

    /**
     * Places a building tile and surrounds it with path tiles.
     *
     * @param r    the row of the main building entrance tile
     * @param c    the column of the main building entrance tile
     * @param type the tile type constant for the building entrance
     */
    private void placeBuilding(int r, int c, int type) {
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = 0; dc <= 1; dc++)
                if (inBounds(r + dr, c + dc)) world[r + dr][c + dc] = T_PATH;
        world[r][c] = type;
    }

    /**
     * Returns {@code true} if the given tile position is a protected area where
     * ore deposits should not be placed (e.g. near the player start or buildings).
     *
     * @param r the row index
     * @param c the column index
     * @return {@code true} if the position is protected
     */
    private boolean isProtectedArea(int r, int c) {
        if (Math.abs(r - ROWS / 2) < 3 && Math.abs(c - COLS / 2) < 3) return true;
        if (r <= 4 && c <= 5) return true;
        if (r <= 4 && c >= COLS - 6) return true;
        if (r >= ROWS - 5 && Math.abs(c - COLS / 2) < 4) return true;
        return false;
    }

    /**
     * Places a rock/ore tile at the given grid position and creates the matching stone object.
     *
     * @param r    the row index
     * @param c    the column index
     * @param type the tile type constant (e.g. {@link #T_NORMAL_ROCK})
     */
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

    /**
     * Spawns the initial set of monsters on the world map with randomised positions.
     */
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

    /**
     * Advances the game state by one frame.
     *
     * @param nowNanos the current time in nanoseconds (from {@link AnimationTimer#handle})
     * @param gameLoop the animation timer, stopped automatically on player death
     * @return {@code false} if the game has ended, {@code true} otherwise
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

    /**
     * Reads keyboard input and moves the player character, applying diagonal normalisation
     * and collision detection against solid tiles.
     */
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

    /**
     * Attempts a player attack if the attack cooldown has expired.
     * Hits all monsters within melee range, deals damage, and awards gold for kills.
     *
     * @param nowNanos the current time in nanoseconds
     */
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

    /**
     * Attempts a mining hit on the tile the player is facing if the mine cooldown has expired.
     * Reduces the ore's durability and awards items if it breaks.
     *
     * @param nowNanos the current time in nanoseconds
     */
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

    /**
     * Adds one unit of the given item to the player's inventory, stacking if possible.
     *
     * @param item the item to add
     */
    private void addToInventory(BaseItem item) {
        for (ItemCounter ic : player.getInventory()) {
            if (ic.getItem().getName().equals(item.getName())) {
                ic.addCount(1);
                return;
            }
        }
        player.getInventory().add(new ItemCounter(item, 1));
    }

    /**
     * Updates monster AI for each frame: aggro detection, chasing, wandering,
     * and attacking the player on contact.
     */
    private void updateMonsters() {
        double aggroRange = TILE_SIZE * 5.0;
        double attackRange = TILE_SIZE * 1.2;

        for (MonsterEntity me : monsters) {
            if (!me.monster.isAlive()) continue;
            double dist = Math.hypot(playerX - me.x, playerY - me.y);
            me.aggro = dist < aggroRange;

            if (me.aggro) {
                double nx = playerX - me.x, ny = playerY - me.y;
                double len = Math.max(1, Math.hypot(nx, ny));
                double spd = 0.5 * (1 + me.type * 0.3);
                me.x += (nx / len) * spd;
                me.y += (ny / len) * spd;

                if (dist < attackRange && playerInvincibleFrames <= 0) {
                    me.monster.attack(player);
                    playerInvincibleFrames = 150;
                    int dmg = Math.max(0, me.monster.getAttack() - player.getDefense());
                    floatingTexts.add(new FloatingText(playerX, playerY - 10, "-" + dmg + " HP",
                            javafx.scene.paint.Color.web("#ff1744"), 1200));
                }
            } else {
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

    /**
     * Processes the ore and monster respawn queues, spawning entities whose timer has elapsed.
     *
     * @param nowMs the current system time in milliseconds
     */
    private void processRespawns(long nowMs) {
        int[] fullOrePool = {
                T_NORMAL_ROCK, T_NORMAL_ROCK, T_NORMAL_ROCK, T_NORMAL_ROCK,
                T_HARD_ROCK, T_HARD_ROCK, T_HARD_ROCK,
                T_IRON_ROCK, T_IRON_ROCK,
                T_PLATINUM, T_MITHRIL, T_VIBRANIUM
        };

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

    /**
     * Checks whether the player is standing adjacent to a building and returns its type.
     * Displays a "nothing nearby" notification if no building is found.
     *
     * @return the {@link BuildingType} of the nearby building, or {@link BuildingType#NONE}
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

    /**
     * Removes expired floating texts and advances each active text upward by its velocity.
     */
    private void updateFloatingTexts() {
        floatingTexts.removeIf(FloatingText::isDead);
        for (FloatingText ft : floatingTexts) ft.y += ft.vy;
    }

    /**
     * Returns {@code true} if the player's bounding box at position {@code (nx, ny)}
     * does not overlap any solid tile.
     *
     * @param nx the candidate X position
     * @param ny the candidate Y position
     * @return {@code true} if the position is passable
     */
    private boolean canMoveTo(double nx, double ny) {
        int m = 5;
        return !isSolid(nx + m, ny + m) && !isSolid(nx + TILE_SIZE - m, ny + m)
                && !isSolid(nx + m, ny + TILE_SIZE - m) && !isSolid(nx + TILE_SIZE - m, ny + TILE_SIZE - m);
    }

    /**
     * Returns whether the world position {@code (px, py)} is inside a solid tile (rock/ore).
     *
     * @param px the x pixel coordinate
     * @param py the y pixel coordinate
     * @return {@code true} if the tile is solid or out-of-bounds
     */
    public boolean isSolid(double px, double py) {
        int c = (int) (px / TILE_SIZE), r = (int) (py / TILE_SIZE);
        if (!inBounds(r, c)) return true;
        int t = world[r][c];
        return (t >= T_NORMAL_ROCK && t <= T_PLATINUM) || t == T_MITHRIL || t == T_VIBRANIUM;
    }

    /**
     * Returns whether the grid position {@code (r, c)} is within the world bounds.
     *
     * @param r the row index
     * @param c the column index
     * @return {@code true} if the position is valid
     */
    public boolean inBounds(int r, int c) {
        return r >= 0 && r < ROWS && c >= 0 && c < COLS;
    }

    /**
     * Returns {@code true} if the world position {@code (x, y)} is within {@code dist} pixels
     * of the player's current position.
     *
     * @param x    the X pixel coordinate to test
     * @param y    the Y pixel coordinate to test
     * @param dist the maximum allowed distance in pixels
     * @return {@code true} if within range
     */
    private boolean isNearPlayer(double x, double y, double dist) {
        return Math.hypot(x - playerX, y - playerY) < dist;
    }

    /**
     * Returns the grid coordinates {@code [row, col]} of the tile the player is currently facing.
     *
     * @return a two-element array {@code {row, col}}
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

    /**
     * Displays a notification message on the HUD for {@link #NOTIF_DURATION} milliseconds.
     *
     * @param msg the text to show in the notification area
     */
    private void showNotif(String msg) {
        notifMsg = msg;
        notifTime = System.currentTimeMillis();
    }

    /**
     * Returns the standard red colour used for damage floating-text labels.
     *
     * @return the colour {@code #ff5252}
     */
    private javafx.scene.paint.Color floatingRed() {
        return javafx.scene.paint.Color.web("#ff5252");
    }

    /**
     * Registers that the given key has been pressed.
     *
     * @param key the pressed key
     */
    public void keyPressed(KeyCode key) {
        keys.add(key);
    }

    /**
     * Registers that the given key has been released.
     *
     * @param key the released key
     */
    public void keyReleased(KeyCode key) {
        keys.remove(key);
    }

    /**
     * Sets whether the left mouse button (attack) is held down.
     *
     * @param on {@code true} if pressed
     */
    public void setLeftMouse(boolean on) {
        leftMouseDown = on;
    }

    /**
     * Sets whether the right mouse button (mine) is held down.
     *
     * @param on {@code true} if pressed
     */
    public void setRightMouse(boolean on) {
        rightMouseDown = on;
    }

    /**
     * Returns the player character.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the single-element array holding the player's active pickaxe.
     *
     * @return the pickaxe holder array
     */
    public Pickaxe[] getPickaxeHolder() {
        return pickaxeHolder;
    }

    /**
     * Returns the 2-D tile-type grid representing the world map.
     *
     * @return the world grid
     */
    public int[][] getWorld() {
        return world;
    }

    /**
     * Returns the grid of mineable stone/ore objects corresponding to rock tiles.
     *
     * @return the stone object grid
     */
    public Mineable[][] getStoneObjects() {
        return stoneObjects;
    }

    /**
     * Returns the list of live monster entities on the world map.
     *
     * @return the monster entity list
     */
    public List<MonsterEntity> getMonsters() {
        return monsters;
    }

    /**
     * Returns the list of active floating-text pop-ups.
     *
     * @return the floating-text list
     */
    public List<FloatingText> getFloatingTexts() {
        return floatingTexts;
    }

    /**
     * Returns the player's current X position in pixels.
     *
     * @return player X position
     */
    public double getPlayerX() {
        return playerX;
    }

    /**
     * Returns the player's current Y position in pixels.
     *
     * @return player Y position
     */
    public double getPlayerY() {
        return playerY;
    }

    /**
     * Returns the direction the player is currently facing.
     * Values: 0=up, 1=left, 2=down, 3=right.
     *
     * @return facing direction index
     */
    public int getFacing() {
        return facing;
    }

    /**
     * Returns the current animation frame index.
     *
     * @return animation frame
     */
    public int getAnimFrame() {
        return animFrame;
    }

    /**
     * Returns whether an attack animation is currently playing.
     *
     * @return {@code true} if the attack animation is active
     */
    public boolean isAttackAnim() {
        return isAttackAnim;
    }

    /**
     * Returns the number of remaining player invincibility frames after taking damage.
     *
     * @return invincibility frames remaining
     */
    public int getInvincibleFrames() {
        return playerInvincibleFrames;
    }

    /**
     * Returns the most recently triggered notification message.
     *
     * @return the notification message string
     */
    public String getNotifMsg() {
        return notifMsg;
    }

    /**
     * Returns the system time (ms) when the current notification was set.
     *
     * @return notification timestamp in milliseconds
     */
    public long getNotifTime() {
        return notifTime;
    }

    /** The type of building the player is adjacent to on the game world map. */
    public enum BuildingType {NONE, SHOP, CRAFT, BOSS}

    /**
     * Represents a live monster entity on the world map, including its position,
     * AI state, and movement variables.
     */
    public static class MonsterEntity {
        /** The underlying monster logic object (stats, health, etc.). */
        public Monster monster;

        /** Current X pixel position on the world map. */
        public double x;

        /** Current Y pixel position on the world map. */
        public double y;

        /** Timer (seconds) until the monster picks a new random wander direction. */
        public double moveTimer;

        /** Current X component of the wander movement velocity. */
        public double dx;

        /** Current Y component of the wander movement velocity. */
        public double dy;

        /** Difficulty tier: 0=easy, 1=medium, 2=hard. */
        public int type;

        /** {@code true} when the monster is chasing the player. */
        public boolean aggro;

        /**
         * Creates a new MonsterEntity.
         *
         * @param m    the underlying monster logic object
         * @param x    the initial x pixel position
         * @param y    the initial y pixel position
         * @param type the difficulty tier (0=easy, 1=medium, 2=hard)
         */
        public MonsterEntity(Monster m, double x, double y, int type) {
            this.monster = m;
            this.x = x;
            this.y = y;
            this.type = type;
            this.moveTimer = 1 + Math.random() * 2;
        }
    }

    /**
     * A short piece of text that floats upward from a world position and fades out over time.
     * Used for damage numbers, item pickups, and event notifications.
     */
    public static class FloatingText {
        /** Current X pixel position. */
        public double x;

        /** Current Y pixel position. */
        public double y;

        /** Vertical velocity in pixels per frame (negative = upward). */
        public double vy;

        /** The string to display. */
        public String text;

        /** The colour of the text. */
        public javafx.scene.paint.Color color;

        /** System time (ms) when this text was created. */
        public long born;

        /** How long (ms) this text should remain visible. */
        public long life;

        /**
         * Creates a new FloatingText at the given world position.
         *
         * @param x      the initial x pixel position
         * @param y      the initial y pixel position
         * @param text   the string to display
         * @param c      the text colour
         * @param lifeMs how long (in milliseconds) the text should remain visible
         */
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

        /**
         * Returns whether this floating text has exceeded its lifetime.
         *
         * @return {@code true} if the text should be removed
         */
        public boolean isDead() {
            return System.currentTimeMillis() - born > life;
        }
    }
}

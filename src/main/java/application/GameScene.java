package application;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import logic.base.BaseItem;
import logic.creatures.Player;
import logic.creatures.EasyMonster;
import logic.creatures.MediumMonster;
import logic.creatures.HardMonster;
import logic.item.potion.SmallHealthPotion;
import logic.item.potion.HealPotion;
import logic.pickaxe.Pickaxe;
import logic.stone.*;
import logic.util.ItemCounter;
import interfaces.Mineable;

import java.util.*;

/**
 * GameScene: The overworld — a top-down tile map where the player:
 *   - Walks with WASD / Arrow keys
 *   - Mines rocks with E or Space
 *   - Fights monsters with F
 *   - Enters buildings (Shop, Crafting, Boss) by walking to them and pressing Enter
 *
 * The world is a 2D grid. Each cell is TILE_SIZE x TILE_SIZE pixels.
 * Think of it like graph paper — each square holds one thing.
 */
public class GameScene {

    // ── Constants ──────────────────────────────────────────────────────────────
    static final int TILE_SIZE = 48;
    static final int COLS      = 20;
    static final int ROWS      = 15;
    static final int W         = TILE_SIZE * COLS;   // 960px
    static final int H         = TILE_SIZE * ROWS;   // 720px
    static final double PLAYER_SPEED = 3.2;
    // ── Tile IDs ────────────────────────────────────────────────────────────────
    static final int T_GROUND      = 0;
    static final int T_GRASS       = 1;
    static final int T_NORMAL_ROCK = 2;
    static final int T_HARD_ROCK   = 3;
    static final int T_IRON_ROCK   = 4;
    static final int T_PLATINUM    = 5;
    static final int T_SHOP        = 6;   // shop building tile
    static final int T_CRAFT       = 7;   // crafting station tile
    static final int T_BOSS_DOOR   = 8;   // boss room door tile
    static final int T_PATH        = 9;   // stone path

    // ── World data ──────────────────────────────────────────────────────────────
    private final int[][]      world        = new int[ROWS][COLS];
    private final Mineable[][] stoneObjects = new Mineable[ROWS][COLS];
    private StackPane root = new StackPane();
    private Pane shopLayer;
    private ShopScene shopScene;
    private Pane craftingLayer;
    private CraftingScene craftingScene;
    private Pane inventoryLayer;
    private InventoryScene inventoryScene;
    // ── Player ──────────────────────────────────────────────────────────────────
    private final Player  player;
    private final Pickaxe[] pickaxeHolder;   // array so sub-scenes can update it
    private double playerX, playerY;         // pixel position (top-left of sprite)
    private int    facing = 2;               // 0=up 1=left 2=down 3=right
    private int    animFrame    = 0;
    private long   lastAnimTime = 0;
    private int    playerInvincibleFrames = 0; // brief i-frames after being hit
    private boolean gameEnded = false;
    private AnimationTimer gameLoop;
    // ── Monsters ────────────────────────────────────────────────────────────────
    /** Holds a live monster instance plus its pixel position on the map */
    private static class MonsterEntity {
        logic.creatures.Monster monster;
        double x, y;
        int type;          // 0=easy 1=medium 2=hard
        double moveTimer;  // countdown until next random move
        double dx, dy;     // current movement direction
        boolean aggro;     // true = chasing the player

        MonsterEntity(logic.creatures.Monster m, double x, double y, int type) {
            this.monster = m; this.x = x; this.y = y; this.type = type;
            this.moveTimer = 1 + Math.random() * 2;
        }
    }
    private final List<MonsterEntity> monsters = new ArrayList<>();
    //EFFECT
    // ── Combat ──────────────────────────────────────────────────────────────────
    private long lastAttackTime = 0;
    private static final long ATTACK_COOLDOWN = 600;    // ms
    private long lastMineTime   = 0;
    private static final long MINE_COOLDOWN   = 280;    // ms

    // ── UI / Notifications ───────────────────────────────────────────────────────
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private String notifMsg  = "";
    private long   notifTime = 0;
    private static final long NOTIF_DURATION = 2200;

    /** A piece of text that floats upward and fades (damage numbers, loot text) */
    private static class FloatingText {
        double x, y, vy;
        String text;
        Color color;
        long born;
        long life;
        FloatingText(double x, double y, String text, Color c, long lifeMs) {
            this.x=x; this.y=y; this.vy=-1.2; this.text=text; this.color=c;
            this.born=System.currentTimeMillis(); this.life=lifeMs;
        }
        boolean isDead() { return System.currentTimeMillis()-born > life; }
    }

    // ── Input ────────────────────────────────────────────────────────────────────
    private final Set<KeyCode> keys = new HashSet<>();

    // ── Constructor ──────────────────────────────────────────────────────────────
    public GameScene(Player player, Pickaxe pickaxe) {
        this.player        = player;
        this.pickaxeHolder = new Pickaxe[]{ pickaxe };
        shopScene = new ShopScene(player, pickaxeHolder, this::closeShop);
        shopLayer = shopScene.build();
        craftingScene = new CraftingScene(player,pickaxeHolder,this::closeCraft);
        craftingLayer = craftingScene.build();
        inventoryScene = new InventoryScene(player, this::closeInventory);
        inventoryLayer = inventoryScene.build();
        generateWorld();
        spawnMonsters();
        // Start player near centre, on the path
        this.playerX = 9 * TILE_SIZE;
        this.playerY = 7 * TILE_SIZE;
    }

    // ══════════════════════════════════════════════════════════════════
    //  WORLD GENERATION
    // ══════════════════════════════════════════════════════════════════
    private void generateWorld() {
        Random rng = new Random(77);

        // Fill with grass / ground
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                world[r][c] = rng.nextInt(10) < 4 ? T_GRASS : T_GROUND;

        // Stone border walls
        for (int r = 0; r < ROWS; r++) { placeRock(r,0,T_NORMAL_ROCK); placeRock(r,COLS-1,T_NORMAL_ROCK); }
        for (int c = 0; c < COLS; c++) { placeRock(0,c,T_NORMAL_ROCK); placeRock(ROWS-1,c,T_NORMAL_ROCK); }

        // Central horizontal + vertical paths
        for (int c = 1; c < COLS-1; c++) world[ROWS/2][c] = T_PATH;
        for (int r = 1; r < ROWS-1; r++) world[r][COLS/2] = T_PATH;

        // Buildings: Shop top-left area, Crafting top-right, Boss at bottom-centre
        placeBuilding(2, 2, T_SHOP);
        placeBuilding(2, COLS-4, T_CRAFT);
        placeBuilding(ROWS-3, COLS/2 - 1, T_BOSS_DOOR);

        // Scatter rocks (not on paths/buildings/player start)
        int[][] rockSpecs = {{T_NORMAL_ROCK,18},{T_HARD_ROCK,12},{T_IRON_ROCK,8},{T_PLATINUM,4}};
        for (int[] spec : rockSpecs)
            for (int i = 0; i < spec[1]; i++) {
                int r = 1 + rng.nextInt(ROWS-2);
                int c = 1 + rng.nextInt(COLS-2);
                if (world[r][c] == T_GROUND || world[r][c] == T_GRASS)
                    if (!isProtectedArea(r, c))
                        placeRock(r, c, spec[0]);
            }
    }

    private void placeBuilding(int r, int c, int type) {
        // Buildings occupy a 2x3 footprint; entrance tile at given position
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = 0; dc <= 1; dc++)
                if (inBounds(r+dr,c+dc)) world[r+dr][c+dc] = T_PATH;
        world[r][c] = type;
    }

    private boolean isProtectedArea(int r, int c) {
        // Keep area around player spawn clear, and around buildings
        if (Math.abs(r-ROWS/2)<3 && Math.abs(c-COLS/2)<3) return true;
        if (r<=4 && c<=5) return true;                       // shop area
        if (r<=4 && c>=COLS-6) return true;                  // craft area
        if (r>=ROWS-5 && Math.abs(c-COLS/2)<4) return true;  // boss area
        return false;
    }

    private void placeRock(int r, int c, int type) {
        world[r][c] = type;
        stoneObjects[r][c] = switch (type) {
            case T_NORMAL_ROCK -> new NormalStone();
            case T_HARD_ROCK   -> new HardStone();
            case T_IRON_ROCK   -> new Iron();
            case T_PLATINUM    -> new Platinum();
            default            -> null;
        };
    }

    private boolean inBounds(int r, int c) {
        return r>=0 && r<ROWS && c>=0 && c<COLS;
    }

    // ══════════════════════════════════════════════════════════════════
    //  MONSTER SPAWNING
    // ══════════════════════════════════════════════════════════════════
    private void spawnMonsters() {
        Random rng = new Random(55);
        // Spread monsters around the map, avoiding player start area
        int[][] specs = {{0,5},{1,4},{2,2}};  // {type, count}
        for (int[] spec : specs) {
            for (int i = 0; i < spec[1]; i++) {
                double mx, my;
                int tries = 0;
                do {
                    mx = (2 + rng.nextInt(COLS-4)) * TILE_SIZE;
                    my = (2 + rng.nextInt(ROWS-4)) * TILE_SIZE;
                    tries++;
                } while (isSolid(mx, my) || isNearPlayer(mx,my,200) && tries<30);

                logic.creatures.Monster m = switch(spec[0]) {
                    case 0  -> new EasyMonster();
                    case 1  -> new MediumMonster();
                    default -> new HardMonster();
                };
                monsters.add(new MonsterEntity(m, mx, my, spec[0]));
            }
        }
    }

    private boolean isNearPlayer(double x, double y, double dist) {
        return Math.hypot(x-playerX, y-playerY) < dist;
    }

    // ══════════════════════════════════════════════════════════════════
    //  SCENE BUILDER
    // ══════════════════════════════════════════════════════════════════
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

            if (e.getCode() == KeyCode.ENTER) {
                checkBuildingEntry();
            }
            if(e.getCode() == KeyCode.I){
                toggleInventory();
            }
        });


        scene.setOnKeyReleased(e -> keys.remove(e.getCode()));

        gameLoop = new AnimationTimer() {
            @Override public void handle(long now) {
                update(now);
                render(gc, now);
                if(shopLayer.isVisible()){
                    shopScene.update();
                }
                if(craftingLayer.isVisible()){
                    craftingScene.update();
                }
                if(inventoryLayer.isVisible()){
                    inventoryScene.update();
                }
            }
        };
        gameLoop.start();

        return scene;
    }

    // ══════════════════════════════════════════════════════════════════
    //  UPDATE  (called ~60x/sec)
    // ══════════════════════════════════════════════════════════════════
    private void update(long nowNanos) {
        if (gameEnded) return;

        if (!player.isAlive()) {
            gameEnded = true;
            gameLoop.stop();
            Main.sceneManager.showGameOver(false, player);
            return;
        }
        if(!shopLayer.isVisible()&&!inventoryLayer.isVisible()&&!craftingLayer.isVisible()) {
            handleMovement();
            handleMining(nowNanos);
            handleAttack(nowNanos);
            updateMonsters();
            updateFloatingTexts();
        }

        if (playerInvincibleFrames > 0) playerInvincibleFrames--;
    }

    private void handleMovement() {
        double dx=0, dy=0;
        if (keys.contains(KeyCode.W)||keys.contains(KeyCode.UP))    { dy-=PLAYER_SPEED; facing=0; }
        if (keys.contains(KeyCode.S)||keys.contains(KeyCode.DOWN))  { dy+=PLAYER_SPEED; facing=2; }
        if (keys.contains(KeyCode.A)||keys.contains(KeyCode.LEFT))  { dx-=PLAYER_SPEED; facing=1; }
        if (keys.contains(KeyCode.D)||keys.contains(KeyCode.RIGHT)) { dx+=PLAYER_SPEED; facing=3; }
        if (dx!=0 && dy!=0) { dx*=0.707; dy*=0.707; }
        if (dx!=0 && canMoveTo(playerX+dx, playerY))      playerX+=dx;
        if (dy!=0 && canMoveTo(playerX,    playerY+dy))   playerY+=dy;
        // Clamp to map
        playerX = Math.max(0, Math.min(W-TILE_SIZE, playerX));
        playerY = Math.max(0, Math.min(H-TILE_SIZE, playerY));
    }

    private boolean canMoveTo(double nx, double ny) {
        int m = 5;
        return !isSolid(nx+m,ny+m) && !isSolid(nx+TILE_SIZE-m,ny+m)
            && !isSolid(nx+m,ny+TILE_SIZE-m) && !isSolid(nx+TILE_SIZE-m,ny+TILE_SIZE-m);
    }

    private boolean isSolid(double px, double py) {
        int c=(int)(px/TILE_SIZE), r=(int)(py/TILE_SIZE);
        if (!inBounds(r,c)) return true;
        int t = world[r][c];
        return t>=T_NORMAL_ROCK && t<=T_PLATINUM;  // only rock tiles block
    }

    // ── Mining ───────────────────────────────────────────────────────
    private void handleMining(long nowNanos) {
        if (!keys.contains(KeyCode.E) && !keys.contains(KeyCode.SPACE)) return;
        long nowMs = nowNanos/1_000_000;
        if (nowMs - lastMineTime < MINE_COOLDOWN) return;
        lastMineTime = nowMs;

        int[] ft = facingTile();
        int tr=ft[0], tc=ft[1];
        if (!inBounds(tr,tc)) return;
        int tile = world[tr][tc];
        if (tile < T_NORMAL_ROCK || tile > T_PLATINUM) return;
        Mineable stone = stoneObjects[tr][tc];
        if (stone==null||stone.isBroken()) return;

        List<BaseItem> drops = pickaxeHolder[0].use(stone, player);
        floatingTexts.add(new FloatingText(tc*TILE_SIZE+12, tr*TILE_SIZE, "⛏", Color.WHITE, 700));

        if (stone.isBroken()) {
            world[tr][tc] = T_GROUND;
            stoneObjects[tr][tc] = null;
            if (!drops.isEmpty()) {
                for (BaseItem item : drops) addToInventory(item);
                String name = drops.get(0).getName();
                showNotif("+ " + drops.size() + "x " + name);
                floatingTexts.add(new FloatingText(tc*TILE_SIZE, tr*TILE_SIZE,
                    "+"+drops.size()+" "+name, Color.YELLOW, 1500));
            }
        } else {
            showNotif("Mining... ["+stone.getDurability()+"/"+stone.getMaxDurability()+"]");
        }
    }

    private void addToInventory(BaseItem item) {
        for (ItemCounter ic : player.getInventory()) {
            if (ic.getItem().getName().equals(item.getName())) { ic.addCount(1); return; }
        }
        player.getInventory().add(new ItemCounter(item, 1));
    }

    // ── Attack ───────────────────────────────────────────────────────
    private void handleAttack(long nowNanos) {
        if (!keys.contains(KeyCode.F) && !keys.contains(KeyCode.Z)) return;
        long nowMs = nowNanos/1_000_000;
        if (nowMs - lastAttackTime < ATTACK_COOLDOWN) return;
        lastAttackTime = nowMs;

        // Attack all monsters within melee range
        double range = TILE_SIZE * 1.6;
        boolean hit = false;
        Iterator<MonsterEntity> it = monsters.iterator();
        while (it.hasNext()) {
            MonsterEntity me = it.next();
            if (!me.monster.isAlive()) { it.remove(); continue; }
            double dist = Math.hypot(playerX-me.x, playerY-me.y);
            if (dist < range) {
                player.attack(me.monster);  // uses Player.attack() from your code
                int dmg = Math.max(1, player.getAttack() - me.monster.getDefense());
                floatingTexts.add(new FloatingText(me.x+8, me.y-8,
                    "-"+dmg, Color.web("#ff5252"), 1000));
                hit = true;
                if (!me.monster.isAlive()) {
                    int gold = me.monster.dropMoney();
                    player.setGold(player.getGold()+gold);
                    floatingTexts.add(new FloatingText(me.x, me.y-20,
                        "+"+gold+"g !", Color.GOLD, 1800));
                    showNotif("Monster defeated! +" + gold + " gold");
                    it.remove();
                }
            }
        }
        if (!hit) showNotif("No monsters in range! (get closer)");
    }

    // ── Monster AI ───────────────────────────────────────────────────
    private void updateMonsters() {
        double aggroRange  = TILE_SIZE * 5.0;
        double attackRange = TILE_SIZE * 1.2;
        double monSpd = 0.5;

        for (MonsterEntity me : monsters) {
            if (!me.monster.isAlive()) continue;
            double dist = Math.hypot(playerX-me.x, playerY-me.y);

            // Aggro: chase player if close enough
            me.aggro = dist < aggroRange;

            if (me.aggro) {
                // Move toward player
                double nx = playerX - me.x;
                double ny = playerY - me.y;
                double len = Math.max(1, Math.hypot(nx,ny));
                double spd = monSpd * (1 + me.type * 0.3);
                me.x += (nx/len)*spd;
                me.y += (ny/len)*spd;

                // Attack player if in range
                if (dist < attackRange && playerInvincibleFrames <= 0) {
                    me.monster.attack(player);  // uses Monster.attack() from your code
                    playerInvincibleFrames = 50;
                    int dmg = Math.max(1, me.monster.getAttack() - player.getDefense());
                    floatingTexts.add(new FloatingText(playerX, playerY-10,
                        "-"+dmg+" HP", Color.web("#ff1744"), 1200));
                }
            } else {
                // Wander randomly
                me.moveTimer -= 1.0/60;
                if (me.moveTimer <= 0) {
                    me.moveTimer = 1.5 + Math.random()*2;
                    double angle = Math.random()*Math.PI*2;
                    me.dx = Math.cos(angle)*0.6;
                    me.dy = Math.sin(angle)*0.6;
                }
                double nx = me.x + me.dx;
                double ny = me.y + me.dy;
                // Don't walk into walls
                if (!isSolid(nx+5,ny+5) && !isSolid(nx+TILE_SIZE-5,ny+5)
                    && !isSolid(nx+5,ny+TILE_SIZE-5) && !isSolid(nx+TILE_SIZE-5,ny+TILE_SIZE-5)) {
                    me.x = nx; me.y = ny;
                }
            }
        }
    }

    // ── Building Entry ────────────────────────────────────────────────
    private void toggleInventory(){
        boolean opening = !inventoryLayer.isVisible();
        inventoryLayer.setVisible(opening);
        if (opening) {
            inventoryScene.refresh(); // build ตอนเปิดเท่านั้น
        }

    }

    private void closeInventory(){
        inventoryLayer.setVisible(false);
    }
    private void toggleShopVisible(){
        if(shopLayer.isVisible()) shopLayer.setVisible(false);
        else shopLayer.setVisible(true);
    }
    private void closeCraft(){
        craftingLayer.setVisible(false);
    }
    private void closeShop(){
        shopLayer.setVisible(false);
    }
    private void toggleCraftVisisble(){
        if(craftingLayer.isVisible()) craftingLayer.setVisible(false);
        else craftingLayer.setVisible(true);
    }
    private void checkBuildingEntry() {
        if (!keys.contains(KeyCode.ENTER)) return;
        int pc=(int)((playerX+TILE_SIZE/2.0)/TILE_SIZE);
        int pr=(int)((playerY+TILE_SIZE/2.0)/TILE_SIZE);

        // Check neighbouring tiles for a building entrance
        for (int dr=-1; dr<=1; dr++) for (int dc=-1; dc<=1; dc++) {
            int r=pr+dr, c=pc+dc;
            if (!inBounds(r,c)) continue;
            switch (world[r][c]) {
                case T_SHOP ->      {
                    toggleShopVisible();
                }
                case T_CRAFT ->     { toggleCraftVisisble(); return; }
                case T_BOSS_DOOR -> { Main.sceneManager.showBossRoom(player, pickaxeHolder); return; }
            }
        }
        showNotif("Nothing to enter nearby.");
    }

    private void updateFloatingTexts() {
        floatingTexts.removeIf(ft -> ft.isDead());
        for (FloatingText ft : floatingTexts) ft.y += ft.vy;
    }

    private int[] facingTile() {
        int pc=(int)((playerX+TILE_SIZE/2.0)/TILE_SIZE);
        int pr=(int)((playerY+TILE_SIZE/2.0)/TILE_SIZE);
        return switch(facing) {
            case 0 -> new int[]{pr-1, pc};
            case 1 -> new int[]{pr,   pc-1};
            case 3 -> new int[]{pr,   pc+1};
            default-> new int[]{pr+1, pc};
        };
    }

    private void showNotif(String msg) { notifMsg=msg; notifTime=System.currentTimeMillis(); }

    // ══════════════════════════════════════════════════════════════════
    //  RENDER
    // ══════════════════════════════════════════════════════════════════
    private void render(GraphicsContext gc, long nowNanos) {
        if (nowNanos-lastAnimTime > 140_000_000L) { animFrame++; lastAnimTime=nowNanos; }
        drawWorld(gc);
        drawMonsters(gc);
        drawPlayer(gc);
        drawFloatingTexts(gc);
        drawHUD(gc);
    }

    // ── World ─────────────────────────────────────────────────────────
    private void drawWorld(GraphicsContext gc) {
        for (int r=0; r<ROWS; r++) for (int c=0; c<COLS; c++) {
            double x=c*TILE_SIZE, y=r*TILE_SIZE;
            switch (world[r][c]) {
                case T_GROUND -> { gc.setFill(Color.web("#4a7c38")); gc.fillRect(x,y,TILE_SIZE,TILE_SIZE); }
                case T_GRASS  -> {
                    gc.setFill(Color.web("#3d6b2d")); gc.fillRect(x,y,TILE_SIZE,TILE_SIZE);
                    gc.setFill(Color.web("#2d5220"));
                    gc.fillRect(x+8,y+10,3,8); gc.fillRect(x+20,y+6,3,10); gc.fillRect(x+32,y+12,3,7);
                }
                case T_PATH -> {
                    gc.setFill(Color.web("#8d7b6a")); gc.fillRect(x,y,TILE_SIZE,TILE_SIZE);
                    gc.setStroke(Color.web("#7a6a5a",0.4)); gc.strokeRect(x+1,y+1,TILE_SIZE-2,TILE_SIZE-2);
                }
                case T_NORMAL_ROCK -> drawRock(gc,x,y,stoneObjects[r][c],Color.web("#9e9e9e"),Color.web("#757575"),"N");
                case T_HARD_ROCK   -> drawRock(gc,x,y,stoneObjects[r][c],Color.web("#78909c"),Color.web("#455a64"),"H");
                case T_IRON_ROCK   -> drawRock(gc,x,y,stoneObjects[r][c],Color.web("#bf8f5b"),Color.web("#8d6030"),"Fe");
                case T_PLATINUM    -> drawRock(gc,x,y,stoneObjects[r][c],Color.web("#90caf9"),Color.web("#1976d2"),"Pt");
                case T_SHOP -> {
                    gc.setFill(Color.web("#5d4037")); gc.fillRect(x,y,TILE_SIZE,TILE_SIZE);
                    gc.setFill(Color.web("#795548")); gc.fillRect(x+4,y+4,TILE_SIZE-8,TILE_SIZE-8);
                    gc.setFill(Color.web("#ffd54f")); gc.setFont(Font.font("Arial",FontWeight.BOLD,10));
                    gc.setTextAlign(TextAlignment.CENTER); gc.fillText("SHOP",x+TILE_SIZE/2.0,y+28);
                    gc.setFill(Color.web("#ffd54f")); gc.fillText("🛒",x+TILE_SIZE/2.0,y+18);
                }
                case T_CRAFT -> {
                    gc.setFill(Color.web("#1a237e")); gc.fillRect(x,y,TILE_SIZE,TILE_SIZE);
                    gc.setFill(Color.web("#283593")); gc.fillRect(x+4,y+4,TILE_SIZE-8,TILE_SIZE-8);
                    gc.setFill(Color.web("#80cbc4")); gc.setFont(Font.font("Arial",FontWeight.BOLD,9));
                    gc.setTextAlign(TextAlignment.CENTER); gc.fillText("CRAFT",x+TILE_SIZE/2.0,y+28);
                    gc.fillText("⚒",x+TILE_SIZE/2.0,y+18);
                }
                case T_BOSS_DOOR -> {
                    gc.setFill(Color.web("#b71c1c")); gc.fillRect(x,y,TILE_SIZE,TILE_SIZE);
                    gc.setFill(Color.web("#c62828")); gc.fillRoundRect(x+4,y+4,TILE_SIZE-8,TILE_SIZE-8,6,6);
                    gc.setFill(Color.web("#ff5252")); gc.setFont(Font.font("Arial",FontWeight.BOLD,9));
                    gc.setTextAlign(TextAlignment.CENTER); gc.fillText("BOSS",x+TILE_SIZE/2.0,y+28);
                    gc.fillText("💀",x+TILE_SIZE/2.0,y+18);
                }
            }
            gc.setTextAlign(TextAlignment.LEFT);
        }

        // Highlight facing tile (if it's a rock)
        int[] ft = facingTile();
        if (inBounds(ft[0],ft[1]) && world[ft[0]][ft[1]]>=T_NORMAL_ROCK && world[ft[0]][ft[1]]<=T_PLATINUM) {
            gc.setStroke(Color.YELLOW); gc.setLineWidth(3);
            gc.strokeRect(ft[1]*TILE_SIZE+2, ft[0]*TILE_SIZE+2, TILE_SIZE-4, TILE_SIZE-4);
            gc.setLineWidth(1);
        }
        // Highlight adjacent buildings
        int pc=(int)((playerX+TILE_SIZE/2.0)/TILE_SIZE);
        int pr=(int)((playerY+TILE_SIZE/2.0)/TILE_SIZE);
        for (int dr=-1;dr<=1;dr++) for (int dc=-1;dc<=1;dc++) {
            int r=pr+dr,c=pc+dc;
            if (!inBounds(r,c)) continue;
            int t=world[r][c];
            if (t==T_SHOP||t==T_CRAFT||t==T_BOSS_DOOR) {
                gc.setStroke(Color.CYAN); gc.setLineWidth(2.5);
                gc.strokeRect(c*TILE_SIZE+2, r*TILE_SIZE+2, TILE_SIZE-4, TILE_SIZE-4);
                gc.setFill(Color.CYAN); gc.setFont(Font.font("Arial",FontWeight.BOLD,10));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.fillText("[ENTER]", c*TILE_SIZE+TILE_SIZE/2.0, r*TILE_SIZE-4);
                gc.setTextAlign(TextAlignment.LEFT);
                gc.setLineWidth(1);
            }
        }
    }

    private void drawRock(GraphicsContext gc, double x, double y,
                          Mineable stone, Color light, Color dark, String label) {
        gc.setFill(dark);  gc.fillRect(x,y,TILE_SIZE,TILE_SIZE);
        gc.setFill(light); gc.fillRoundRect(x+3,y+3,TILE_SIZE-6,TILE_SIZE-6,8,8);
        gc.setStroke(dark.darker()); gc.setLineWidth(1.5);
        gc.strokeLine(x+12,y+12,x+20,y+20); gc.strokeLine(x+26,y+14,x+32,y+24); gc.setLineWidth(1);
        gc.setFill(dark.darker()); gc.setFont(Font.font("Arial",FontWeight.BOLD,10));
        gc.setTextAlign(TextAlignment.CENTER); gc.fillText(label,x+TILE_SIZE/2.0,y+30); gc.setTextAlign(TextAlignment.LEFT);
        if (stone!=null) {
            double pct=(double)stone.getDurability()/stone.getMaxDurability();
            gc.setFill(Color.rgb(0,0,0,0.5)); gc.fillRect(x+4,y+TILE_SIZE-8,TILE_SIZE-8,5);
            gc.setFill(pct>0.5?Color.LIMEGREEN:pct>0.25?Color.ORANGE:Color.RED);
            gc.fillRect(x+4,y+TILE_SIZE-8,(TILE_SIZE-8)*pct,5);
        }
    }

    // ── Monsters ─────────────────────────────────────────────────────
    private void drawMonsters(GraphicsContext gc) {
        for (MonsterEntity me : monsters) {
            if (!me.monster.isAlive()) continue;
            double x=me.x, y=me.y;

            // Shadow
            gc.setFill(Color.rgb(0,0,0,0.2)); gc.fillOval(x+6,y+38,36,10);

            Color bodyColor = switch(me.type) {
                case 0  -> Color.web("#ef9a9a"); // easy  = light red
                case 1  -> Color.web("#ce93d8"); // medium= purple
                default -> Color.web("#f44336"); // hard  = bright red
            };
            Color darkBody = bodyColor.darker();

            // Body
            gc.setFill(bodyColor); gc.fillOval(x+8,y+14,32,28);
            // Arms
            gc.setFill(darkBody);
            gc.fillOval(x+2,y+18,12,10); gc.fillOval(x+34,y+18,12,10);
            // Head
            gc.setFill(bodyColor.brighter()); gc.fillOval(x+12,y+4,24,20);
            // Eyes (scary)
            gc.setFill(Color.RED);
            gc.fillOval(x+16,y+9,5,5); gc.fillOval(x+27,y+9,5,5);
            gc.setFill(Color.web("#1a0000")); gc.fillOval(x+17,y+10,3,3); gc.fillOval(x+28,y+10,3,3);
            // Horns (harder monsters)
            if (me.type>=1) {
                gc.setFill(Color.web("#4a0000"));
                gc.fillPolygon(new double[]{x+17,x+21,x+13},new double[]{y+6,y+1,y+1},3);
                gc.fillPolygon(new double[]{x+31,x+35,x+27},new double[]{y+6,y+1,y+1},3);
            }

            // Aggro indicator
            if (me.aggro) {
                gc.setFill(Color.rgb(255,50,50,0.4));
                gc.fillOval(x-4,y-4,TILE_SIZE+8,TILE_SIZE+8);
                gc.setFill(Color.RED); gc.setFont(Font.font("Arial",FontWeight.BOLD,11));
                gc.setTextAlign(TextAlignment.CENTER); gc.fillText("!",x+TILE_SIZE/2.0,y); gc.setTextAlign(TextAlignment.LEFT);
            }

            // HP bar
            int hp=me.monster.getHealthPoint(), mhp=me.monster.getMaxHealthPoint();
            double pct=(double)hp/mhp;
            gc.setFill(Color.web("#1a0000",0.6)); gc.fillRect(x+2,y+TILE_SIZE-8,TILE_SIZE-4,5);
            gc.setFill(pct>0.5?Color.LIMEGREEN:pct>0.25?Color.ORANGE:Color.RED);
            gc.fillRect(x+2,y+TILE_SIZE-8,(TILE_SIZE-4)*pct,5);

            // Monster name label
            gc.setFont(Font.font("Arial",9));
            String name = me.type==0?"Goblin":me.type==1?"Orc":"Troll";
            gc.setFill(Color.WHITE); gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(name+" "+hp+"/"+mhp, x+TILE_SIZE/2.0, y+TILE_SIZE+10);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }

    // ── Player ────────────────────────────────────────────────────────
    private void drawPlayer(GraphicsContext gc) {
        double px=playerX, py=playerY;
        boolean moving = keys.stream().anyMatch(k->
            k==KeyCode.W||k==KeyCode.A||k==KeyCode.S||k==KeyCode.D||
            k==KeyCode.UP||k==KeyCode.DOWN||k==KeyCode.LEFT||k==KeyCode.RIGHT);
        int legBob = (moving && animFrame%2==0) ? 2 : 0;

        // Invincibility flash
        if (playerInvincibleFrames > 0 && animFrame%2==0) return;

        // Shadow
        gc.setFill(Color.rgb(0,0,0,0.25)); gc.fillOval(px+8,py+38,32,10);

        // Legs
        gc.setFill(Color.web("#4e342e"));
        gc.fillRoundRect(px+12,py+34+legBob, 10,10,4,4);
        gc.fillRoundRect(px+26,py+34-legBob, 10,10,4,4);

        // Cape / cloak
        gc.setFill(Color.web("#880e4f"));
        gc.fillRoundRect(px+10,py+18,28,22,5,5);

        // Body (armour)
        gc.setFill(Color.web("#1565c0"));
        gc.fillRoundRect(px+12,py+20,24,18,5,5);

        // Head
        gc.setFill(Color.web("#ffcc80"));
        gc.fillOval(px+14,py+6,20,20);

        // Hair
        gc.setFill(Color.web("#4e342e"));
        gc.fillRect(px+14,py+6,20,8);

        // Eyes
        gc.setFill(Color.web("#1a237e"));
        switch(facing) {
            case 2 -> { gc.fillOval(px+18,py+14,4,4); gc.fillOval(px+26,py+14,4,4); }
            case 0 -> { gc.setFill(Color.web("#c49a5a")); gc.fillRect(px+14,py+6,20,12); }
            case 1 -> gc.fillOval(px+15,py+14,4,4);
            case 3 -> gc.fillOval(px+29,py+14,4,4);
        }

        // Sword (equipped weapon glow)
        boolean attacking = keys.contains(KeyCode.F)||keys.contains(KeyCode.Z);
        drawSword(gc, px, py, attacking);
    }

    private void drawSword(GraphicsContext gc, double px, double py, boolean swinging) {
        gc.save();
        double hx = facing==1 ? px+4 : px+44;
        gc.translate(hx, py+24);
        gc.rotate(swinging ? (facing==1?30:-30) : 0);
        // Hilt
        gc.setFill(Color.web("#795548")); gc.fillRoundRect(-3,-2,6,14,3,3);
        // Guard
        gc.setFill(Color.web("#ffd700")); gc.fillRect(-7,12,14,4);
        // Blade (glows when attacking)
        if (swinging) {
            gc.setFill(Color.rgb(255,255,200,0.3)); gc.fillOval(-8,-18,16,22);
        }
        gc.setFill(Color.web("#b0bec5")); gc.fillPolygon(
            new double[]{-3,3,0}, new double[]{16,16,-8}, 3);
        gc.setFill(Color.web("#eceff1")); gc.fillPolygon(
            new double[]{-1,1,0}, new double[]{14,14,-6}, 3);
        gc.restore();
    }

    // ── Floating Texts ────────────────────────────────────────────────
    private void drawFloatingTexts(GraphicsContext gc) {
        long now = System.currentTimeMillis();
        gc.setTextAlign(TextAlignment.CENTER);
        for (FloatingText ft : floatingTexts) {
            double age = (now - ft.born) / (double) ft.life;
            double alpha = Math.max(0, 1.0 - age);
            Color c = ft.color;
            gc.setFill(Color.color(c.getRed(),c.getGreen(),c.getBlue(),alpha));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.fillText(ft.text, ft.x+TILE_SIZE/2.0, ft.y);
        }
        gc.setTextAlign(TextAlignment.LEFT);
    }

    // ── HUD ───────────────────────────────────────────────────────────
    private void drawHUD(GraphicsContext gc) {
        // Top bar background
        gc.setFill(Color.rgb(0,0,0,0.72)); gc.fillRect(0,0,W,56);

        // HP bar
        double hpPct = (double)player.getHealth()/player.getMaxHealth();
        gc.setFill(Color.web("#7f0000")); gc.fillRoundRect(10,8,170,16,5,5);
        gc.setFill(hpPct>0.5?Color.web("#e53935"):hpPct>0.25?Color.ORANGE:Color.RED);
        gc.fillRoundRect(10,8,170*hpPct,16,5,5);
        gc.setFill(Color.WHITE); gc.setFont(Font.font("Arial",FontWeight.BOLD,11));
        gc.fillText("❤  "+player.getHealth()+" / "+player.getMaxHealth(), 14, 21);

        // Attack stat
        gc.setFill(Color.web("#ff8a65"));
        gc.fillText("⚔ ATK: "+player.getAttack(), 14, 42);

        // Defense stat
        gc.setFill(Color.web("#90caf9"));
        gc.fillText("🛡 DEF: "+player.getDefense(), 80, 42);

        // Gold
        gc.setFill(Color.web("#ffd700"));
        gc.setFont(Font.font("Arial",FontWeight.BOLD,13));
        gc.fillText("💰 "+player.getGold()+"g", 200, 23);

        // Pickaxe
        gc.setFill(Color.web("#b0bec5")); gc.setFont(Font.font("Arial",11));
        gc.fillText("⛏ "+pickaxeHolder[0].getName()+" (Pwr:"+pickaxeHolder[0].getPower()+")", 200, 42);

        // Monster count
        long alive = monsters.stream().filter(me->me.monster.isAlive()).count();
        gc.setFill(alive==0?Color.LIMEGREEN:Color.web("#ff8a80"));
        gc.setFont(Font.font("Arial",FontWeight.BOLD,11));
        gc.fillText("👾 Monsters: "+alive, W-150, 23);
        if (alive == 0) gc.fillText("✓ Area clear!", W-150, 42);

        // Inventory panel (bottom-left)
        gc.setFill(Color.rgb(0,0,0,0.70)); gc.fillRoundRect(6,H-106,220,100,8,8);
        gc.setFill(Color.web("#ffd54f")); gc.setFont(Font.font("Arial",FontWeight.BOLD,11));
        gc.fillText("INVENTORY", 14, H-92);
        List<ItemCounter> inv = player.getInventory();
        if (inv.isEmpty()) {
            gc.setFill(Color.LIGHTGRAY); gc.setFont(Font.font("Arial",10));
            gc.fillText("(mine rocks to fill inventory)", 14, H-78);
        } else {
            int shown=Math.min(inv.size(),5);
            for (int i=0;i<shown;i++) {
                ItemCounter ic=inv.get(i);
                gc.setFill(Color.WHITE); gc.setFont(Font.font("Arial",10));
                gc.fillText("• "+ic.getItem().getName()+": "+ic.getCount(), 14, H-78+i*14);
            }
            if (inv.size()>5) { gc.setFill(Color.LIGHTGRAY); gc.fillText("...+"+(inv.size()-5)+" more",14,H-78+5*14); }
        }

        // Controls guide (bottom-right)
        gc.setFill(Color.rgb(0,0,0,0.70)); gc.fillRoundRect(W-190,H-116,184,110,8,8);
        gc.setFill(Color.web("#80cbc4")); gc.setFont(Font.font("Arial",FontWeight.BOLD,11));
        gc.fillText("CONTROLS", W-180, H-100);
        gc.setFill(Color.WHITE); gc.setFont(Font.font("Arial",10));
        String[] lines={"WASD/↑↓←→  Move","E/Space     Mine rock","F/Z         Attack","ENTER       Enter building","(yellow border = mine target)","(cyan border = enter building)"};
        for (int i=0;i<lines.length;i++) gc.fillText(lines[i], W-180, H-86+i*14);

        // Notification bar
        long age = System.currentTimeMillis()-notifTime;
        if (age < NOTIF_DURATION && !notifMsg.isEmpty()) {
            double a = age < 1800 ? 1.0 : 1.0-(age-1800)/400.0;
            gc.setFill(Color.rgb(0,0,0,0.75*a)); gc.fillRoundRect(W/2.0-180,H-138,360,28,10,10);
            gc.setFill(Color.rgb(255,235,59,a)); gc.setFont(Font.font("Arial",FontWeight.BOLD,13));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(notifMsg, W/2.0, H-119);
            gc.setTextAlign(TextAlignment.LEFT);
        }
    }
}

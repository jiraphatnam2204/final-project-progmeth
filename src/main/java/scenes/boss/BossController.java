package scenes.boss;

import javafx.scene.paint.Color;
import logic.base.BasePotion;
import logic.creatures.*;
import logic.util.ItemCounter;

import java.util.ArrayList;
import java.util.List;

public class BossController {

    private final Player player;
    private final BossInfo[] bosses;
    private final List<String> log = new ArrayList<>();

    private int bossIndex = 0;
    private Monster currentBoss;
    private String bossName;
    private Color bossColor;
    private BattleState state = BattleState.PLAYER_TURN;

    private boolean pendingAttackAnim  = false;
    private boolean pendingBossShake   = false;
    private boolean pendingPlayerShake = false;
    private long lastEnemyActionMs     = 0;
    private int pendingDefenseReset = 0;

    // Sub-menu + skill cooldown controller
    private final BattleMenuController menuCtrl = new BattleMenuController();

    public BossController(Player player) {
        this.player = player;
        bosses = new BossInfo[]{
                new BossInfo("Akaza",     new EasyBoss(500,   60, 15, 300),  Color.web("#64B5F6")),
                new BossInfo("Kokushibo", new MediumBoss(900,  80, 22, 700),  Color.web("#ce93d8")),
                new BossInfo("Muzan",     new HardBoss(1600, 100, 38, 1500), Color.web("#ef5350")),
        };
        loadBoss(0);
    }
    public ActionResult doDefend() {
        if (state != BattleState.PLAYER_TURN) return ActionResult.NONE;

        int bonus = (int)(player.getDefense()); // เพิ่มอีก 1.5x → รวมเป็น 2.5x
        player.addBonus(0, bonus, 0, 0);
        pendingDefenseReset = bonus;
        log.add("🛡 Defending! DEF x2 this turn!");

        state = BattleState.ENEMY_TURN;
        lastEnemyActionMs = System.currentTimeMillis();
        trimLog();
        return ActionResult.ENEMY_TURN;
    }
    public void loadBoss(int index) {
        bossIndex   = index;
        BossInfo bi = bosses[index];
        currentBoss = bi.monster();
        bossName    = bi.name();
        bossColor   = bi.color();
        log.clear();
        log.add("A wild " + bossName + " appears!");
        log.add("HP: " + player.getHealth() + "/" + player.getMaxHealth()
                + "  ATK:" + player.getAttack() + "  DEF:" + player.getDefense());
    }

    // ── Normal Attack ─────────────────────────────────────────────────────────
    public ActionResult doPlayerAttack() {
        if (state != BattleState.PLAYER_TURN) return ActionResult.NONE;
        menuCtrl.close();

        int dmg = Math.max(1, player.getAttack() - currentBoss.getDefense());
        currentBoss.takeDamage(player.getAttack());
        log.add("You hit " + bossName + " for " + dmg + " dmg!");
        pendingAttackAnim = true;
        pendingBossShake  = true;

        return advanceAfterPlayerAction();
    }

    // ── Skills ────────────────────────────────────────────────────────────────
    public ActionResult doSkill(int idx) {
        if (state != BattleState.PLAYER_TURN) return ActionResult.NONE;
        if (!menuCtrl.isReady(idx)) return ActionResult.NONE;
        menuCtrl.close();

        pendingAttackAnim = true;
        pendingBossShake  = true;

        switch (idx) {
            case 0 -> { // Power Strike: 2x damage
                int base = Math.max(1, player.getAttack() - currentBoss.getDefense());
                int dmg  = base * 2;
                currentBoss.takeDamage(player.getAttack() * 2);
                log.add("Power Strike! Hit " + bossName + " for " + dmg + " dmg!");
                menuCtrl.setCooldown(0, BattleMenuController.SKILL_MAX_CD[0]);
            }
            case 1 -> { // Shield Wall
                menuCtrl.setShieldWall(true);
                log.add("Shield Wall raised! Incoming damage halved this turn.");
                pendingAttackAnim = false;
                pendingBossShake  = false;
                menuCtrl.setCooldown(1, BattleMenuController.SKILL_MAX_CD[1]);
            }
            case 2 -> { // Berserk: 3 hits, debuff self
                int base  = Math.max(1, player.getAttack() - currentBoss.getDefense());
                int total = base * 3;
                for (int h = 0; h < 3; h++) currentBoss.takeDamage(player.getAttack());
                menuCtrl.setBerserkDebuff(true);
                log.add("Berserk! 3 rapid hits for " + total + " total dmg! DEF -50% next turn.");
                menuCtrl.setCooldown(2, BattleMenuController.SKILL_MAX_CD[2]);
            }
            case 3 -> { // Soul Drain
                int base = Math.max(1, player.getAttack() - currentBoss.getDefense());
                currentBoss.takeDamage(player.getAttack());
                int heal = Math.max(1, (int)(base * 0.30));
                player.heal(heal);
                log.add("Soul Drain: dealt " + base + " dmg, healed " + heal + " HP!");
                menuCtrl.setCooldown(3, BattleMenuController.SKILL_MAX_CD[3]);
            }
        }

        return advanceAfterPlayerAction();
    }

    // ── Use Potion ────────────────────────────────────────────────────────────
    public ActionResult usePotion(BattleMenuController.PotionEntry entry) {
        if (state != BattleState.PLAYER_TURN) return ActionResult.NONE;
        menuCtrl.close();

        ItemCounter ic = entry.counter();
        if (ic.getItem() instanceof BasePotion pot) {
            pot.consume(player);
            ic.setCount(ic.getCount() - 1);
            if (ic.getCount() <= 0) player.getInventory().remove(ic);
            log.add("Used "
                    + entry.name() + "! HP: " + player.getHealth() + "/" + player.getMaxHealth());
        }
        state = BattleState.ENEMY_TURN;
        lastEnemyActionMs = System.currentTimeMillis();
        trimLog();
        return ActionResult.ENEMY_TURN;
    }

    // ── Rest Heal (no potions fallback) ──────────────────────────────────────
    public ActionResult doRestHeal() {
        if (state != BattleState.PLAYER_TURN) return ActionResult.NONE;
        menuCtrl.close();
        int heal = Math.max(5, player.getMaxHealth() / 10);
        player.heal(heal);
        log.add("You rest briefly... +" + heal + " HP  ("
                + player.getHealth() + "/" + player.getMaxHealth() + ")");
        state = BattleState.ENEMY_TURN;
        lastEnemyActionMs = System.currentTimeMillis();
        trimLog();
        return ActionResult.ENEMY_TURN;
    }

    // ── Enemy Turn ────────────────────────────────────────────────────────────
    public ActionResult doEnemyTurn() {
        if (state != BattleState.ENEMY_TURN) return ActionResult.NONE;

        menuCtrl.tickCooldowns();

        boolean crit   = Math.random() < 0.20;
        int baseDmg    = currentBoss.getAttack();
        if (crit) baseDmg = (int)(baseDmg * 1.6);

        // Apply berserk debuff (player DEF halved)
        int savedDef = player.getDefense();
        if (menuCtrl.isBerserkDebuffActive()) {
            player.setDefense(savedDef / 2);
            menuCtrl.setBerserkDebuff(false);
        }

        // Apply shield wall
        if (menuCtrl.isShieldWallActive()) {
            baseDmg = baseDmg / 2;
            menuCtrl.setShieldWall(false);
            log.add("Shield Wall absorbed half the damage!");
        }

        int actualDmg = Math.max(0, baseDmg - player.getDefense());
        // Restore defense after calc
        player.takeDamage(baseDmg);

        player.setDefense(savedDef);
        if (pendingDefenseReset > 0) {
            player.removeBonus(0, pendingDefenseReset, 0, 0);
            pendingDefenseReset = 0;
        }
        String suffix = crit ? " CRIT!" : "";
        log.add(bossName + " attacks for " + actualDmg + " dmg!" + suffix);
        log.add("Your HP: " + player.getHealth() + "/" + player.getMaxHealth());

        pendingPlayerShake = true;

        if (!player.isAlive()) {
            state = BattleState.DEFEAT;
            log.add("You have been defeated...");
            trimLog();
            return ActionResult.PLAYER_DEFEATED;
        }

        state = BattleState.PLAYER_TURN;
        trimLog();
        return ActionResult.PLAYER_TURN;
    }

    // ── Internal helpers ──────────────────────────────────────────────────────
    private ActionResult advanceAfterPlayerAction() {
        if (!currentBoss.isAlive()) {
            int gold = currentBoss.dropMoney() * 3;
            player.setGold(player.getGold() + gold);
            log.add(bossName + " defeated! +" + gold + "g");
            if (bossIndex + 1 < bosses.length) {
                state = BattleState.VICTORY;
                log.add("Press Next Boss to continue!");
                trimLog();
                return ActionResult.BOSS_DEFEATED;
            } else {
                state = BattleState.ALL_CLEAR;
                log.add("ALL BOSSES DEFEATED! YOU WIN!");
                trimLog();
                return ActionResult.ALL_CLEAR;
            }
        }
        log.add(bossName + " HP: " + currentBoss.getHealthPoint() + "/" + currentBoss.getMaxHealthPoint());
        state = BattleState.ENEMY_TURN;
        lastEnemyActionMs = System.currentTimeMillis();
        trimLog();
        return ActionResult.ENEMY_TURN;
    }

    public void advanceToNextBoss() {
        if (bossIndex + 1 < bosses.length) {
            loadBoss(bossIndex + 1);
            state = BattleState.PLAYER_TURN;
        }
    }

    private void trimLog() { while (log.size() > 8) log.remove(0); }

    // ── Getters ───────────────────────────────────────────────────────────────
    public Player              getPlayer()          { return player; }
    public Monster             getCurrentBoss()     { return currentBoss; }
    public String              getBossName()        { return bossName; }
    public Color               getBossColor()       { return bossColor; }
    public int                 getBossIndex()       { return bossIndex; }
    public BattleState         getState()           { return state; }
    public List<String>        getLog()             { return log; }
    public long                getLastEnemyActionMs(){ return lastEnemyActionMs; }
    public boolean             hasNextBoss()        { return bossIndex + 1 < bosses.length; }
    public BattleMenuController getMenuCtrl()       { return menuCtrl; }

    public boolean isPendingAttackAnim()  { return pendingAttackAnim; }
    public boolean isPendingBossShake()   { return pendingBossShake; }
    public boolean isPendingPlayerShake() { return pendingPlayerShake; }
    public void clearAttackAnimFlag()     { pendingAttackAnim  = false; }
    public void clearBossShakeFlag()      { pendingBossShake   = false; }
    public void clearPlayerShakeFlag()    { pendingPlayerShake = false; }

    // ── Enums ─────────────────────────────────────────────────────────────────
    public enum BattleState {
        PLAYER_TURN, ENEMY_TURN, VICTORY, DEFEAT, ALL_CLEAR
    }

    public enum ActionResult {
        NONE, ENEMY_TURN, PLAYER_TURN, BOSS_DEFEATED, ALL_CLEAR, PLAYER_DEFEATED
    }

    public record BossInfo(String name, Monster monster, Color color) {}
}
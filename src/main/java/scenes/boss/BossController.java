package scenes.boss;

import javafx.scene.paint.Color;
import logic.base.BasePotion;
import logic.creatures.*;
import logic.util.ItemCounter;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the sequential boss battle system.
 * Manages turn state, player and enemy actions, skill cooldowns,
 * and progression through the three boss encounters (Akaza → Kokushibo → Muzan).
 */
public class BossController {

    /** The player participating in the boss battle. */
    private final Player player;

    /** Array of all three boss entries (Akaza, Kokushibo, Muzan). */
    private final BossInfo[] bosses;

    /** Rolling battle log displayed in the UI (capped at 8 entries). */
    private final List<String> log = new ArrayList<>();

    /** Sub-menu and skill cooldown controller for the battle. */
    private final BattleMenuController menuCtrl = new BattleMenuController();

    /** Index of the currently active boss in the {@link #bosses} array. */
    private int bossIndex = 0;

    /** The monster instance for the currently active boss. */
    private Monster currentBoss;

    /** Display name of the current boss. */
    private String bossName;

    /** Theme colour of the current boss, used for UI accents. */
    private Color bossColor;

    /** Current phase of the turn-based battle. */
    private BattleState state = BattleState.PLAYER_TURN;

    /** {@code true} when an attack animation should play this frame. */
    private boolean pendingAttackAnim = false;

    /** {@code true} when the boss shake animation should trigger. */
    private boolean pendingBossShake = false;

    /** {@code true} when the player shake animation should trigger. */
    private boolean pendingPlayerShake = false;

    /** System time (ms) when the enemy last took an action. */
    private long lastEnemyActionMs = 0;

    /** Defense bonus amount to remove after the enemy's turn (from the Defend action). */
    private int pendingDefenseReset = 0;

    /**
     * Creates a new BossController for the given player and initialises the first boss.
     *
     * @param player the player entering the boss room
     */
    public BossController(Player player) {
        this.player = player;
        bosses = new BossInfo[]{
                new BossInfo("Akaza", new EasyBoss(), Color.web("#64B5F6")),
                new BossInfo("Kokushibo", new MediumBoss(), Color.web("#ce93d8")),
                new BossInfo("Muzan", new HardBoss(), Color.web("#ef5350")),
        };
        loadBoss(0);
    }

    /**
     * Performs the player's Defend action, doubling DEF for the enemy's next turn.
     *
     * @return {@link ActionResult#ENEMY_TURN} if successful, {@link ActionResult#NONE} if not the player's turn
     */
    public ActionResult doDefend() {
        if (state != BattleState.PLAYER_TURN) return ActionResult.NONE;

        int bonus = (int) (player.getDefense()); // เพิ่มอีก 1.5x → รวมเป็น 2.5x
        player.addBonus(0, bonus, 0, 0);
        pendingDefenseReset = bonus;
        log.add("🛡 Defending! DEF x2 this turn!");

        state = BattleState.ENEMY_TURN;
        lastEnemyActionMs = System.currentTimeMillis();
        trimLog();
        return ActionResult.ENEMY_TURN;
    }

    /**
     * Loads the boss at the given index and resets the battle log.
     *
     * @param index the boss index (0 = Akaza, 1 = Kokushibo, 2 = Muzan)
     */
    public void loadBoss(int index) {
        bossIndex = index;
        BossInfo bi = bosses[index];
        currentBoss = bi.monster();
        bossName = bi.name();
        bossColor = bi.color();
        log.clear();
        log.add("A wild " + bossName + " appears!");
        log.add("HP: " + player.getHealth() + "/" + player.getMaxHealth()
                + "  ATK:" + player.getAttack() + "  DEF:" + player.getDefense());
    }

    /**
     * Performs the player's normal attack against the current boss.
     *
     * @return the resulting {@link ActionResult} after the attack
     */
    public ActionResult doPlayerAttack() {
        if (state != BattleState.PLAYER_TURN) return ActionResult.NONE;
        menuCtrl.close();

        int dmg = Math.max(1, player.getAttack() - currentBoss.getDefense());
        currentBoss.takeDamage(player.getAttack());
        log.add("You hit " + bossName + " for " + dmg + " dmg!");
        pendingAttackAnim = true;
        pendingBossShake = true;

        return advanceAfterPlayerAction();
    }

    /**
     * Activates the player skill at the given index if it is ready and it is the player's turn.
     *
     * @param idx the skill index (0=Kagura Dance, 1=Dead Calm, 2=Constant Flux, 3=Water Wheel)
     * @return the resulting {@link ActionResult}, or {@link ActionResult#NONE} if the skill is on cooldown
     */
    public ActionResult doSkill(int idx) {
        if (state != BattleState.PLAYER_TURN) return ActionResult.NONE;
        if (!menuCtrl.isReady(idx)) return ActionResult.NONE;
        menuCtrl.close();

        pendingAttackAnim = true;
        pendingBossShake = true;

        switch (idx) {
            case 0 -> {
                int base = Math.max(0, player.getAttack() - currentBoss.getDefense());
                int dmg = base * 2;
                currentBoss.takeDamage(player.getAttack() * 2);
                log.add("Kagura Dance! Hit " + bossName + " for " + dmg + " dmg!");
                menuCtrl.setCooldown(0, BattleMenuController.SKILL_MAX_CD[0]);
            }
            case 1 -> {
                menuCtrl.setShieldWall(true);
                log.add("Dead Calm! Incoming damage halved this turn.");
                pendingAttackAnim = false;
                pendingBossShake = false;
                menuCtrl.setCooldown(1, BattleMenuController.SKILL_MAX_CD[1]);
            }
            case 2 -> {
                int base = Math.max(0, player.getAttack() - currentBoss.getDefense());
                int total = base * 3;
                for (int h = 0; h < 3; h++) currentBoss.takeDamage(player.getAttack());
                menuCtrl.setBerserkDebuff(true);
                log.add("Constant Flux! 3 rapid hits for " + total + " total dmg! DEF -50% next turn.");
                menuCtrl.setCooldown(2, BattleMenuController.SKILL_MAX_CD[2]);
            }
            case 3 -> {
                int base = Math.max(0, player.getAttack() - currentBoss.getDefense());
                currentBoss.takeDamage(player.getAttack());
                int heal = Math.max(1, (int) (base * 0.30));
                player.heal(heal);
                log.add("Water Wheel: dealt " + base + " dmg, healed " + heal + " HP!");
                menuCtrl.setCooldown(3, BattleMenuController.SKILL_MAX_CD[3]);
            }
        }

        return advanceAfterPlayerAction();
    }

    // Use Potion from Heal Menu

    /**
     * Consumes the selected potion from the player's inventory.
     *
     * @param entry the potion entry selected from the heal menu
     * @return {@link ActionResult#ENEMY_TURN} after consuming, or {@link ActionResult#NONE} if not the player's turn
     */
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

    // Rest Heal

    /**
     * Performs the Rest action, recovering 10% of the player's max HP without using an item.
     *
     * @return {@link ActionResult#ENEMY_TURN} after resting, or {@link ActionResult#NONE} if not the player's turn
     */
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

    // Enemy Turn

    /**
     * Executes the enemy's turn: applies attack (with possible crit), berserk debuff, and shield wall.
     *
     * @return the resulting {@link ActionResult} after the enemy acts
     */
    public ActionResult doEnemyTurn() {
        if (state != BattleState.ENEMY_TURN) return ActionResult.NONE;

        menuCtrl.tickCooldowns();

        boolean crit = Math.random() < 0.20;
        int baseDmg = currentBoss.getAttack();
        if (crit) baseDmg = (int) (baseDmg * 1.6);

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

    // Victory Check

    /**
     * Checks whether the current boss was defeated and advances the battle accordingly.
     * Awards gold, loads the next boss on intermediate victories, or signals all-clear.
     *
     * @return {@link ActionResult#BOSS_DEFEATED} if this boss was beaten but more remain,
     *         {@link ActionResult#ALL_CLEAR} if all bosses are defeated, or
     *         {@link ActionResult#ENEMY_TURN} to continue the current fight
     */
    private ActionResult advanceAfterPlayerAction() {
        if (!currentBoss.isAlive()) {
            int gold = currentBoss.dropMoney();
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

    /**
     * Advances the battle to the next boss if one exists.
     * Resets the battle state to {@link BattleState#PLAYER_TURN}.
     */
    public void advanceToNextBoss() {
        if (bossIndex + 1 < bosses.length) {
            loadBoss(bossIndex + 1);
            state = BattleState.PLAYER_TURN;
        }
    }

    /**
     * Trims the battle log to a maximum of 8 entries by removing the oldest entries.
     */
    private void trimLog() {
        while (log.size() > 8) log.remove(0);
    }

    // Getters

    /**
     * @return the player participating in the battle
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return the currently active boss monster
     */
    public Monster getCurrentBoss() {
        return currentBoss;
    }

    /**
     * @return the display name of the current boss
     */
    public String getBossName() {
        return bossName;
    }

    /**
     * @return the theme colour of the current boss
     */
    public Color getBossColor() {
        return bossColor;
    }

    /**
     * @return the index of the current boss (0–2)
     */
    public int getBossIndex() {
        return bossIndex;
    }

    /**
     * @return the current {@link BattleState}
     */
    public BattleState getState() {
        return state;
    }

    /**
     * @return the live battle log (up to 8 most-recent lines)
     */
    public List<String> getLog() {
        return log;
    }

    /**
     * @return the system-time (ms) when the enemy last acted
     */
    public long getLastEnemyActionMs() {
        return lastEnemyActionMs;
    }

    /**
     * @return {@code true} if there is another boss after the current one
     */
    public boolean hasNextBoss() {
        return bossIndex + 1 < bosses.length;
    }

    /**
     * @return the {@link BattleMenuController} managing menus and cooldowns
     */
    public BattleMenuController getMenuCtrl() {
        return menuCtrl;
    }

    /**
     * @return {@code true} if an attack animation should play this frame
     */
    public boolean isPendingAttackAnim() {
        return pendingAttackAnim;
    }

    /**
     * @return {@code true} if the boss shake animation should trigger
     */
    public boolean isPendingBossShake() {
        return pendingBossShake;
    }

    /**
     * @return {@code true} if the player shake animation should trigger
     */
    public boolean isPendingPlayerShake() {
        return pendingPlayerShake;
    }

    /**
     * Clears the pending attack animation flag after the view has consumed it.
     */
    public void clearAttackAnimFlag() {
        pendingAttackAnim = false;
    }

    /**
     * Clears the pending boss shake flag after the view has consumed it.
     */
    public void clearBossShakeFlag() {
        pendingBossShake = false;
    }

    /**
     * Clears the pending player shake flag after the view has consumed it.
     */
    public void clearPlayerShakeFlag() {
        pendingPlayerShake = false;
    }

    // Enums

    /**
     * Represents the current phase of the turn-based battle.
     */
    public enum BattleState {
        /** It is the player's turn to choose an action. */
        PLAYER_TURN,
        /** The enemy is taking its turn. */
        ENEMY_TURN,
        /** The current boss has been defeated; waiting to advance to the next. */
        VICTORY,
        /** The player has been defeated. */
        DEFEAT,
        /** All three bosses have been defeated. */
        ALL_CLEAR
    }

    /**
     * Represents the outcome of a player or enemy action for the view to react to.
     */
    public enum ActionResult {
        /** No meaningful state change occurred. */
        NONE,
        /** Control passes to the enemy turn. */
        ENEMY_TURN,
        /** Control passes back to the player turn. */
        PLAYER_TURN,
        /** The current boss was defeated and another boss remains. */
        BOSS_DEFEATED,
        /** All bosses were defeated. */
        ALL_CLEAR,
        /** The player was defeated. */
        PLAYER_DEFEATED
    }

    /**
     * Immutable data holder for a boss entry (name, monster instance, and theme colour).
     *
     * @param name    the display name of the boss
     * @param monster the monster instance
     * @param color   the theme colour used in the UI
     */
    public record BossInfo(String name, Monster monster, Color color) {
    }
}
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
    private boolean pendingAttackAnim = false;
    private boolean pendingBossShake = false;
    private boolean pendingPlayerShake = false;
    private long lastEnemyActionMs = 0;

    public BossController(Player player) {
        this.player = player;
        bosses = new BossInfo[]{
                new BossInfo("Akaza", new EasyBoss(500, 35, 15, 300), Color.web("#64B5F6")),
                new BossInfo("Kokushibo", new MediumBoss(900, 55, 22, 700), Color.web("#ce93d8")),
                new BossInfo("Muzan", new HardBoss(1600, 85, 38, 1500), Color.web("#ef5350")),
        };
        loadBoss(0);
    }

    public void loadBoss(int index) {
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

    public ActionResult doPlayerAttack() {
        if (state != BattleState.PLAYER_TURN) return ActionResult.NONE;

        player.attack(currentBoss);
        int dmg = Math.max(1, player.getAttack() - currentBoss.getDefense());
        log.add("You hit " + bossName + " for " + dmg + " dmg!");

        // Signal View to play slash animation and shake the boss
        pendingAttackAnim = true;
        pendingBossShake = true;

        if (!currentBoss.isAlive()) {
            int gold = currentBoss.dropMoney() * 3;
            player.setGold(player.getGold() + gold);
            log.add("★ " + bossName + " defeated! +" + gold + "g");

            if (bossIndex + 1 < bosses.length) {
                state = BattleState.VICTORY;
                log.add("Press 'Next Boss' to continue!");
                trimLog();
                return ActionResult.BOSS_DEFEATED;
            } else {
                state = BattleState.ALL_CLEAR;
                log.add("🎉 ALL BOSSES DEFEATED! YOU WIN!");
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

    public ActionResult doPlayerHeal() {
        if (state != BattleState.PLAYER_TURN) return ActionResult.NONE;

        for (ItemCounter ic : player.getInventory()) {
            if (ic.getItem() instanceof BasePotion pot) {
                pot.consume(player);
                ic.setCount(ic.getCount() - 1);
                if (ic.getCount() <= 0) player.getInventory().remove(ic);
                log.add("You used " + ic.getItem().getName()
                        + "! HP: " + player.getHealth() + "/" + player.getMaxHealth());
                state = BattleState.ENEMY_TURN;
                lastEnemyActionMs = System.currentTimeMillis();
                trimLog();
                return ActionResult.ENEMY_TURN;
            }
        }

        int heal = Math.max(5, player.getMaxHealth() / 10);
        player.heal(heal);
        log.add("You rest briefly... +" + heal + " HP  ("
                + player.getHealth() + "/" + player.getMaxHealth() + ")");
        state = BattleState.ENEMY_TURN;
        lastEnemyActionMs = System.currentTimeMillis();
        trimLog();
        return ActionResult.ENEMY_TURN;
    }

    public ActionResult doEnemyTurn() {
        if (state != BattleState.ENEMY_TURN) return ActionResult.NONE;

        boolean crit = Math.random() < 0.20;
        int baseDmg = currentBoss.getAttack();
        if (crit) baseDmg = (int) (baseDmg * 1.6);

        currentBoss.attack(player);
        int actualDmg = Math.max(0, baseDmg - player.getDefense());
        String suffix = crit ? " 💥CRIT!" : "";
        log.add(bossName + " attacks you for " + actualDmg + " dmg!" + suffix);
        log.add("Your HP: " + player.getHealth() + "/" + player.getMaxHealth());

        pendingPlayerShake = true;

        if (!player.isAlive()) {
            state = BattleState.DEFEAT;
            log.add("💀 You have been defeated...");
            trimLog();
            return ActionResult.PLAYER_DEFEATED;
        }

        state = BattleState.PLAYER_TURN;
        trimLog();
        return ActionResult.PLAYER_TURN;
    }

    public void advanceToNextBoss() {
        if (bossIndex + 1 < bosses.length) {
            loadBoss(bossIndex + 1);
            state = BattleState.PLAYER_TURN;
        }
    }

    public Player getPlayer() {
        return player;
    }

    public Monster getCurrentBoss() {
        return currentBoss;
    }

    public String getBossName() {
        return bossName;
    }

    public Color getBossColor() {
        return bossColor;
    }

    public int getBossIndex() {
        return bossIndex;
    }

    public int getTotalBosses() {
        return bosses.length;
    }

    public BattleState getState() {
        return state;
    }

    public List<String> getLog() {
        return log;
    }

    public long getLastEnemyActionMs() {
        return lastEnemyActionMs;
    }

    public boolean hasNextBoss() {
        return bossIndex + 1 < bosses.length;
    }

    public boolean isPendingAttackAnim() {
        return pendingAttackAnim;
    }

    public boolean isPendingBossShake() {
        return pendingBossShake;
    }

    public boolean isPendingPlayerShake() {
        return pendingPlayerShake;
    }

    public void clearAttackAnimFlag() {
        pendingAttackAnim = false;
    }

    public void clearBossShakeFlag() {
        pendingBossShake = false;
    }

    public void clearPlayerShakeFlag() {
        pendingPlayerShake = false;
    }

    private void trimLog() {
        while (log.size() > 7) log.remove(0);
    }

    public enum BattleState {
        PLAYER_TURN,
        ENEMY_TURN,
        VICTORY,     // current boss defeated; more bosses remain
        DEFEAT,      // player died
        ALL_CLEAR    // all bosses defeated
    }

    public enum ActionResult {
        NONE,           // action was ignored (wrong turn)
        ENEMY_TURN,     // player acted; it's now the enemy's turn
        PLAYER_TURN,    // enemy acted; it's now the player's turn
        BOSS_DEFEATED,  // current boss died; next boss is available
        ALL_CLEAR,      // all bosses defeated — trigger victory screen
        PLAYER_DEFEATED // player died — trigger game over screen
    }

    public record BossInfo(String name, Monster monster, Color color) {
    }
}

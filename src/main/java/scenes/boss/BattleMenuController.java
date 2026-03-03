package scenes.boss;

import logic.base.BasePotion;
import logic.creatures.Player;
import logic.util.ItemCounter;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the in-battle sub-menu state and skill cooldowns for the boss fight.
 * Tracks which menu is open (skills, heal, or none), cooldown turns for each skill,
 * and active status effects (shield wall, berserk debuff).
 */
public class BattleMenuController {

    /** The number of player skills available in battle. */
    public static final int SKILL_COUNT = 4;
    public static final String[] SKILL_NAMES = {
            "Kagura Dance",
            "Dead Calm",
            "Constant Flux",
            "Water Wheel"
    };
    public static final String[] SKILL_ICONS = {"\u26A1", "\uD83D\uDEE1", "\uD83D\uDCA2", "\uD83E\uDE78"};
    public static final String[] SKILL_DESCS = {
            "Deal 2x damage to the boss",
            "Reduce next incoming damage by 50%",
            "3 hits but lose 50% DEF next turn",
            "Damage boss + heal 30% of damage"
    };
    public static final int[] SKILL_MAX_CD = {3, 2, 4, 5};
    private final int[] cooldowns = new int[SKILL_COUNT];
    private MenuState menuState = MenuState.NONE;
    private boolean shieldWallActive = false;
    private boolean berserkDebuffActive = false;

    /**
     * Returns the currently open menu state.
     *
     * @return the current {@link MenuState}
     */
    public MenuState getMenuState() {
        return menuState;
    }

    /** Opens the skill selection menu. */
    public void openSkills() {
        menuState = MenuState.SKILLS;
    }

    /** Opens the heal/bag menu. */
    public void openHeal() {
        menuState = MenuState.HEAL;
    }

    /** Closes any open sub-menu. */
    public void close() {
        menuState = MenuState.NONE;
    }

    /**
     * Returns the remaining cooldown turns for the skill at the given index.
     *
     * @param i the skill index (0–3)
     * @return remaining cooldown turns
     */
    public int getCooldown(int i) {
        return cooldowns[i];
    }

    /**
     * Returns whether the skill at the given index is ready to use (cooldown is 0).
     *
     * @param i the skill index (0–3)
     * @return {@code true} if the skill can be used
     */
    public boolean isReady(int i) {
        return cooldowns[i] <= 0;
    }

    /**
     * Sets the cooldown for the skill at the given index.
     *
     * @param i     the skill index (0–3)
     * @param turns the number of turns before the skill can be used again
     */
    public void setCooldown(int i, int turns) {
        cooldowns[i] = turns;
    }

    /** Decrements all skill cooldowns by 1 turn (called at the end of each enemy turn). */
    public void tickCooldowns() {
        for (int i = 0; i < SKILL_COUNT; i++) {
            if (cooldowns[i] > 0) cooldowns[i]--;
        }
    }

    /**
     * Returns whether the Shield Wall (Dead Calm) effect is active.
     *
     * @return {@code true} if incoming damage is halved this turn
     */
    public boolean isShieldWallActive() {
        return shieldWallActive;
    }

    /**
     * Returns whether the Berserk debuff (from Constant Flux) is active.
     *
     * @return {@code true} if the player's DEF is halved on the next enemy turn
     */
    public boolean isBerserkDebuffActive() {
        return berserkDebuffActive;
    }

    /**
     * Sets the shield wall status effect.
     *
     * @param v {@code true} to activate, {@code false} to deactivate
     */
    public void setShieldWall(boolean v) {
        shieldWallActive = v;
    }

    /**
     * Sets the berserk debuff status effect.
     *
     * @param v {@code true} to activate, {@code false} to deactivate
     */
    public void setBerserkDebuff(boolean v) {
        berserkDebuffActive = v;
    }

    /**
     * Returns a list of all potions currently in the player's inventory.
     *
     * @param player the player whose inventory is searched
     * @return list of {@link PotionEntry} objects representing available potions
     */
    public List<PotionEntry> getPotions(Player player) {
        List<PotionEntry> list = new ArrayList<>();
        for (ItemCounter ic : player.getInventory()) {
            if (ic.getItem() instanceof BasePotion) {
                list.add(new PotionEntry(ic));
            }
        }
        return list;
    }

    /** Represents which sub-menu is currently visible. */
    public enum MenuState {NONE, SKILLS, HEAL}

    /**
     * A lightweight view-model wrapping an {@link ItemCounter} for potions in the heal menu.
     *
     * @param counter the backing inventory slot
     */
    public record PotionEntry(ItemCounter counter) {
        public String name() {
            return counter.getItem().getName();
        }

        public int count() {
            return counter.getCount();
        }

        public boolean hasStock() {
            return counter.getCount() > 0;
        }
    }
}
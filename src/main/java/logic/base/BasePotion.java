package logic.base;

import interfaces.Consumable;
import interfaces.Stackable;
import logic.creatures.Player;

/**
 * Abstract base class for all potions.
 * Potions are stackable consumable items that restore the player's health.
 */
public abstract class BasePotion extends BaseItem implements Consumable {

    private int stat;

    /**
     * Creates a new potion with the given name and heal amount.
     * Potions stack up to 30 per slot.
     *
     * @param name the name of the potion
     * @param stat the amount of health restored when consumed
     */
    public BasePotion(String name, int stat) {
        super(name, true, 30); setStat(stat);
    }

    /**
     * Consumes this potion, restoring the player's health by {@code stat} points.
     *
     * @param p the player who consumes this potion
     */
    @Override
    public void consume(Player p) { p.heal(stat); }

    /**
     * Returns the heal amount provided by this potion.
     *
     * @return heal amount
     */
    public int getStat() { return stat; }

    /**
     * Sets the heal amount of this potion. Value cannot go below zero.
     *
     * @param stat the new heal amount
     */
    public void setStat(int stat) { this.stat = Math.max(0, stat); }
}

package logic.base;

import interfaces.Craftable;
import interfaces.Equipable;
import logic.creatures.Player;
import logic.util.ItemCounter;
import java.util.ArrayList;

/**
 * Abstract base class for all weapons.
 * Weapons are non-stackable, craftable, and equipable items that increase the player's attack damage.
 */
public abstract class BaseWeapon extends BaseItem implements Craftable, Equipable {

    private int dmg, craftingPrice;
    private double cd;

    /**
     * Creates a new weapon with the given stats.
     *
     * @param name          the name of the weapon
     * @param dmg           the damage bonus granted when equipped
     * @param cd            the cooldown in seconds (must be &ge; 0)
     * @param craftingPrice the gold cost to craft this weapon
     */
    public BaseWeapon(String name, int dmg, double cd, int craftingPrice) {
        super(name, false, 1); setCd(cd); setDmg(dmg); setCraftingPrice(craftingPrice);
    }

    /**
     * Checks whether the player has enough gold and materials to craft this weapon.
     *
     * @param p the player attempting to craft
     * @return {@code true} if all requirements are met
     */
    @Override
    public boolean canCraft(Player p) {
        if (p.getGold() < craftingPrice) return false;
        ArrayList<ItemCounter> recipe = getRecipe();
        if (recipe == null) return false;
        for (ItemCounter it : recipe) {
            int cnt = 0;
            for (ItemCounter i : p.getInventory()) if (it.equals(i)) cnt += i.getCount();
            if (it.getCount() > cnt) return false;
        }
        return true;
    }

    /**
     * Applies this weapon's damage bonus to the player's attack stat.
     *
     * @param p the player equipping this weapon
     */
    @Override
    public void equip(Player p) {
        p.addBonus(dmg, 0, 0, 0);
    }

    /**
     * Removes this weapon's damage bonus from the player's attack stat.
     *
     * @param p the player unequipping this weapon
     */
    @Override
    public void unequip(Player p) {
        p.removeBonus(dmg, 0, 0, 0);
    }

    /**
     * Crafts this weapon by consuming the required materials and gold from the player.
     * Does nothing if {@link #canCraft(Player)} returns {@code false}.
     *
     * @param p the player crafting this weapon
     */
    @Override
    public void craft(Player p) {
        if (!canCraft(p)) return;
        ArrayList<ItemCounter> recipe = getRecipe();
        for (ItemCounter it : recipe) {
            int remaining = it.getCount();
            for (ItemCounter i : p.getInventory()) {
                int start = i.getCount();
                if (it.equals(i)) i.setCount(i.getCount() - it.getCount());
                remaining -= (start - i.getCount());
                if (i.getCount() == 0) { p.getInventory().remove(i); break; }
                if (remaining == 0) break;
            }
        }
        p.setGold(p.getGold() - getCraftingPrice());
    }

    /**
     * Returns the gold cost to craft this weapon.
     *
     * @return crafting price in gold
     */
    @Override
    public int getCraftingPrice() { return craftingPrice; }

    /**
     * Sets the crafting price.
     *
     * @param v the new crafting price
     */
    public void setCraftingPrice(int v) { craftingPrice = v; }

    /**
     * Returns the damage bonus granted by this weapon.
     *
     * @return damage value
     */
    public int getDmg() { return dmg; }

    /**
     * Sets the damage value. Minimum value is 1.
     *
     * @param v the new damage value
     */
    public void setDmg(int v) { dmg = Math.max(1, v); }

    /**
     * Returns the cooldown of this weapon in seconds.
     *
     * @return cooldown in seconds
     */
    public double getCd() { return cd; }

    /**
     * Sets the cooldown. Minimum value is 0.
     *
     * @param v the new cooldown
     */
    public void setCd(double v) { cd = Math.max(0, v); }
}

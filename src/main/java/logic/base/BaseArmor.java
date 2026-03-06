package logic.base;

import interfaces.Craftable;
import interfaces.Equipable;
import logic.creatures.Player;
import logic.util.ItemCounter;
import java.util.ArrayList;

/**
 * Abstract base class for all armor items.
 * Armor is non-stackable, craftable, and equipable, granting stat bonuses to the player.
 */
public abstract class BaseArmor extends BaseItem implements Equipable, Craftable {

    /** Defense bonus granted to the player when this armor is equipped. */
    protected int def = 0;

    /** Attack bonus granted to the player when this armor is equipped. */
    protected int atk = 0;

    /** Speed bonus granted to the player when this armor is equipped. */
    protected int spd = 0;

    /** Max HP bonus granted to the player when this armor is equipped. */
    protected int hp = 0;

    /** Gold cost required to craft this armor. */
    protected int craftingPrice = 0;

    /**
     * Creates a new armor with the given stat bonuses and crafting cost.
     *
     * @param name          the name of the armor
     * @param atk           the attack bonus granted when equipped
     * @param def           the defense bonus granted when equipped
     * @param hp            the max HP bonus granted when equipped
     * @param spd           the speed bonus granted when equipped
     * @param craftingPrice the gold cost to craft this armor
     */
    public BaseArmor(String name, int atk, int def, int hp, int spd, int craftingPrice) {
        super(name, false, 1);
        setAtk(atk); setDef(def); setHp(hp); setSpd(spd); setCraftingPrice(craftingPrice);
    }

    /**
     * Sets the crafting price. Minimum value is 0.
     *
     * @param v the new crafting price
     */
    public void setCraftingPrice(int v) { craftingPrice = Math.max(0, v); }

    /**
     * Returns the gold cost to craft this armor.
     *
     * @return crafting price in gold
     */
    @Override
    public int getCraftingPrice() { return craftingPrice; }

    /**
     * Applies this armor's stat bonuses to the player.
     *
     * @param p the player equipping this armor
     */
    @Override
    public void equip(Player p) { p.addBonus(atk, def, hp, spd); }

    /**
     * Removes this armor's stat bonuses from the player.
     *
     * @param p the player unequipping this armor
     */
    @Override
    public void unequip(Player p) { p.removeBonus(atk, def, hp, spd); }

    /**
     * Checks whether the player has enough gold and materials to craft this armor.
     *
     * @param p the player attempting to craft
     * @return {@code true} if all requirements are met
     */
    @Override
    public boolean canCraft(Player p) {
        if (p.getGold() < craftingPrice) return false;
        for (ItemCounter it : getRecipe()) {
            int cnt = 0;
            for (ItemCounter i : p.getInventory()) if (it.equals(i)) cnt += i.getCount();
            if (it.getCount() > cnt) return false;
        }
        return true;
    }

    /**
     * Crafts this armor by consuming the required materials and gold from the player.
     * Does nothing if {@link #canCraft(Player)} returns {@code false}.
     *
     * @param p the player crafting this armor
     */
    @Override
    public void craft(Player p) {
        if (!canCraft(p)) return;
        for (ItemCounter it : getRecipe()) {
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
     * Returns the defense bonus.
     *
     * @return defense bonus
     */
    public int getDef() { return def; }

    /**
     * Sets the defense bonus.
     *
     * @param v the new defense value
     */
    public void setDef(int v) { def = v; }

    /**
     * Returns the attack bonus.
     *
     * @return attack bonus
     */
    public int getAtk() { return atk; }

    /**
     * Sets the attack bonus.
     *
     * @param v the new attack value
     */
    public void setAtk(int v) { atk = v; }

    /**
     * Returns the max HP bonus.
     *
     * @return HP bonus
     */
    public int getHp() { return hp; }

    /**
     * Sets the max HP bonus.
     *
     * @param v the new HP bonus
     */
    public void setHp(int v) { hp = v; }

    /**
     * Returns the speed bonus.
     *
     * @return speed bonus
     */
    public int getSpd() { return spd; }

    /**
     * Sets the speed bonus.
     *
     * @param v the new speed value
     */
    public void setSpd(int v) { spd = v; }
}

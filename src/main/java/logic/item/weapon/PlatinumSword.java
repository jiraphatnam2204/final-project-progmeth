package logic.item.weapon;

import logic.base.BaseWeapon;
import logic.stone.Iron;
import logic.stone.Platinum;
import logic.util.ItemCounter;

import java.util.ArrayList;

/**
 * Tier-4 craftable sword made from Iron and Platinum.
 * Grants +45 ATK. Recipe: 8 Iron + 10 Platinum + 160 gold.
 */
public class PlatinumSword extends BaseWeapon {

    /**
     * Creates a new PlatinumSword with preset stats.
     */
    public PlatinumSword() {
        super("Platinum Sword", 45, 0.7, 160);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new Iron(), 8));
        r.add(new ItemCounter(new Platinum(), 10));
        return r;
    }
}

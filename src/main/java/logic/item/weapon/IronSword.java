package logic.item.weapon;

import logic.base.BaseWeapon;
import logic.stone.Iron;
import logic.stone.NormalStone;
import logic.util.ItemCounter;

import java.util.ArrayList;

/**
 * Tier-3 craftable sword made from Normal Stone and Iron.
 * Grants +30 ATK. Recipe: 5 Normal Stone + 8 Iron + 100 gold.
 */
public class IronSword extends BaseWeapon {

    /**
     * Creates a new IronSword with preset stats.
     */
    public IronSword() {
        super("Iron Sword", 30, 0.8, 100);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new NormalStone(), 5));
        r.add(new ItemCounter(new Iron(), 8));
        return r;
    }
}

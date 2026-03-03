package logic.item.weapon;

import logic.base.BaseWeapon;
import logic.stone.NormalStone;
import logic.util.ItemCounter;

import java.util.ArrayList;

/**
 * Tier-1 craftable sword made from Normal Stone.
 * Grants +15 ATK. Recipe: 10 Normal Stone + 10 gold.
 */
public class StoneSword extends BaseWeapon {

    /**
     * Creates a new StoneSword with preset stats.
     */
    public StoneSword() {
        super("Stone Sword", 15, 1, 10);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new NormalStone(), 10));
        return r;
    }
}

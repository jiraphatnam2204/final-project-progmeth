package logic.item.armor;

import logic.base.BaseArmor;
import logic.stone.Iron;
import logic.stone.NormalStone;
import logic.util.ItemCounter;

import java.util.ArrayList;

/**
 * Tier-3 craftable armor made from Normal Stone and Iron.
 * Grants +15 DEF, +40 HP. Recipe: 5 Normal Stone + 8 Iron + 100 gold.
 */
public class IronArmor extends BaseArmor {

    /**
     * Creates a new IronArmor with preset stats.
     */
    public IronArmor() {
        super("Iron Armor", 0, 15, 40, 0, 100);
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

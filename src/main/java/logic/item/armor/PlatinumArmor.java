package logic.item.armor;

import logic.base.BaseArmor;
import logic.stone.Iron;
import logic.stone.Platinum;
import logic.util.ItemCounter;

import java.util.ArrayList;

/**
 * Tier-4 craftable armor made from Iron and Platinum.
 * Grants +25 DEF, +60 HP. Recipe: 8 Iron + 10 Platinum + 160 gold.
 */
public class PlatinumArmor extends BaseArmor {

    /**
     * Creates a new PlatinumArmor with preset stats.
     */
    public PlatinumArmor() {
        super("Platinum Armor", 0, 25, 60, 0, 160);
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

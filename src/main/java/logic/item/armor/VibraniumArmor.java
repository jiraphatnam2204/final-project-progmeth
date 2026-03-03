package logic.item.armor;

import logic.base.BaseArmor;
import logic.stone.Mithril;
import logic.stone.Vibranium;
import logic.util.ItemCounter;

import java.util.ArrayList;

/**
 * Tier-6 (top-tier) craftable armor made from Mithril and Vibranium.
 * Grants +55 DEF, +150 HP. Recipe: 10 Mithril + 15 Vibranium + 310 gold.
 */
public class VibraniumArmor extends BaseArmor {

    /**
     * Creates a new VibraniumArmor with preset stats.
     */
    public VibraniumArmor() {
        super("Vibranium Armor", 0, 55, 150, 0, 310);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new Mithril(), 10));
        r.add(new ItemCounter(new Vibranium(), 15));
        return r;
    }
}

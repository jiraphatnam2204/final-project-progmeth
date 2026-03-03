package logic.item.armor;

import logic.base.BaseArmor;
import logic.stone.Mithril;
import logic.stone.Platinum;
import logic.util.ItemCounter;

import java.util.ArrayList;

/**
 * Tier-5 craftable armor made from Platinum and Mithril.
 * Grants +40 DEF, +100 HP. Recipe: 5 Platinum + 15 Mithril + 230 gold.
 */
public class MithrilArmor extends BaseArmor {

    /**
     * Creates a new MithrilArmor with preset stats.
     */
    public MithrilArmor() {
        super("Mithril Armor", 0, 40, 100, 0, 230);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new Platinum(), 5));
        r.add(new ItemCounter(new Mithril(), 15));
        return r;
    }
}

package logic.item.weapon;

import logic.base.BaseWeapon;
import logic.stone.Mithril;
import logic.stone.Platinum;
import logic.util.ItemCounter;

import java.util.ArrayList;

/**
 * Tier-5 craftable sword made from Platinum and Mithril.
 * Grants +70 ATK. Recipe: 5 Platinum + 15 Mithril + 230 gold.
 */
public class MithrilSword extends BaseWeapon {

    /**
     * Creates a new MithrilSword with preset stats.
     */
    public MithrilSword() {
        super("Mithril Sword", 70, 0.6, 230);
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

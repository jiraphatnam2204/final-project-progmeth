package logic.item.weapon;

import logic.base.BaseWeapon;
import logic.stone.Mithril;
import logic.stone.Vibranium;
import logic.util.ItemCounter;

import java.util.ArrayList;

/**
 * Tier-6 (top-tier) craftable sword made from Mithril and Vibranium.
 * Grants +100 ATK. Recipe: 10 Mithril + 15 Vibranium + 310 gold.
 */
public class VibraniumSword extends BaseWeapon {

    /**
     * Creates a new VibraniumSword with preset stats.
     */
    public VibraniumSword() {
        super("Vibranium Sword", 100, 0.5, 310);
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

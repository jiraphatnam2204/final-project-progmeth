package logic.item.armor;

import logic.base.BaseArmor;
import logic.stone.Iron;
import logic.stone.Platinum;
import logic.util.ItemCounter;

import java.util.ArrayList;

public class PlatinumArmor extends BaseArmor {
    public PlatinumArmor() {
        super("Platinum Armor", 0, 25, 60, 0, 500);
    }

    @Override
    public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new Iron(), 10));
        r.add(new ItemCounter(new Platinum(), 10));
        return r;
    }
}

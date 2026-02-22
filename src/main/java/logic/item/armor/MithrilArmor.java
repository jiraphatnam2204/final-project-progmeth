package logic.item.armor;

import logic.base.BaseArmor;
import logic.stone.Mithril;
import logic.stone.Platinum;
import logic.util.ItemCounter;

import java.util.ArrayList;

public class MithrilArmor extends BaseArmor {
    public MithrilArmor() {
        super("Mithril Armor", 0, 40, 100, 0, 1100);
    }

    @Override
    public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new Platinum(), 5));
        r.add(new ItemCounter(new Mithril(), 20));
        return r;
    }
}

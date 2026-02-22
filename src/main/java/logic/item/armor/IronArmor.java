package logic.item.armor;

import logic.base.BaseArmor;
import logic.stone.Iron;
import logic.stone.NormalStone;
import logic.util.ItemCounter;

import java.util.ArrayList;

public class IronArmor extends BaseArmor {
    public IronArmor() {
        super("Iron Armor", 0, 15, 40, 0, 200);
    }

    @Override
    public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new NormalStone(), 5));
        r.add(new ItemCounter(new Iron(), 8));
        return r;
    }
}

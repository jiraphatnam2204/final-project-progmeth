package logic.item.armor;

import logic.base.BaseArmor;
import logic.stone.Mithril;
import logic.stone.Vibranium;
import logic.util.ItemCounter;

import java.util.ArrayList;

public class VibraniumArmor extends BaseArmor {
    public VibraniumArmor() {
        super("Vibranium Armor", 0, 55, 150, 0, 1500);
    }

    @Override
    public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new Mithril(), 30));
        r.add(new ItemCounter(new Vibranium(), 30));
        return r;
    }
}

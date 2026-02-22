package logic.item.weapon;

import logic.base.BaseWeapon;
import logic.stone.Iron;
import logic.stone.NormalStone;
import logic.util.ItemCounter;

import java.util.ArrayList;

public class IronSword extends BaseWeapon {
    public IronSword() {
        super("Iron Sword", 25, 0.8, 80);
    }

    @Override
    public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new NormalStone(), 5));
        r.add(new ItemCounter(new Iron(), 8));
        return r;
    }
}

package logic.item.weapon;

import logic.base.BaseWeapon;
import logic.stone.Iron;
import logic.stone.NormalStone;
import logic.util.ItemCounter;

import java.util.ArrayList;

public class IronSword extends BaseWeapon {
    public IronSword(){
        super("Iron Sword",40,0.7,100);
    }
    @Override
    public ArrayList<ItemCounter> getRecipe(){
        ArrayList<ItemCounter> recipe = new ArrayList<ItemCounter>();
        recipe.add(new ItemCounter(new NormalStone(),10));
        recipe.add(new ItemCounter(new Iron(),10));
        return recipe;
    }
}

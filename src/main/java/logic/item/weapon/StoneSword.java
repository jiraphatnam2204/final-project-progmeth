package logic.item.weapon;

import logic.base.BaseWeapon;
import logic.util.ItemCounter;

import java.util.ArrayList;

public class StoneSword extends BaseWeapon {
    public StoneSword(){
        super("Stone Sword",15,1,20);
    }
    @Override
    public ArrayList<ItemCounter> getRecipe(){
        ItemCounter stone = new ItemCounter(new Stone(),10);
        ArrayList<ItemCounter> recipe = new ArrayList<ItemCounter>();
        recipe.add(stone);
        return recipe;
    }
}

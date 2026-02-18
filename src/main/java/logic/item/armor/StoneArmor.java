package logic.item.armor;

import logic.base.BaseArmor;
import logic.util.ItemCounter;

import java.util.ArrayList;

public class StoneArmor extends BaseArmor {
    public StoneArmor(){
        super("Stone Armor",0,5,10,0,10);
    }
    @Override
    public ArrayList<ItemCounter> getRecipe(){
        ItemCounter stone = new ItemCounter(new Stone(),10);
        ArrayList<ItemCounter> recipe = new ArrayList<ItemCounter>();
        recipe.add(stone);
        return recipe;
    }
}

package logic.item.armor;

import logic.base.BaseArmor;
import logic.util.ItemCounter;

import java.util.ArrayList;

public class PlatinumArmor extends BaseArmor {
    public PlatinumArmor(){
        super("Platinum Armor",0,30,50,0,1000);

    }
    @Override
    public ArrayList<ItemCounter> getRecipe(){
        ArrayList<ItemCounter> recipe = new ArrayList<ItemCounter>();
        recipe.add(new ItemCounter(new Iron(),10));
        recipe.add(new ItemCounter(new Platinum(),10));
        return recipe;
    }
}

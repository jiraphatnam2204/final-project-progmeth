package logic.item.armor;

import logic.base.BaseArmor;
import logic.util.ItemCounter;

import java.util.ArrayList;

public class VibraniumArmor extends BaseArmor {
    public VibraniumArmor(){
        super("Vibranium Armor",0,80,200,0,1500);

    }
    @Override
    public ArrayList<ItemCounter> getRecipe(){
        ArrayList<ItemCounter> recipe = new ArrayList<ItemCounter>();
        recipe.add(new ItemCounter(new Mithril(),30));
        recipe.add(new ItemCounter(new Vibranium(),30));
        return recipe;
    }
}

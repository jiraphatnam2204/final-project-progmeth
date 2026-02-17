package logic.item.weapon;

import logic.base.BaseWeapon;
import logic.util.ItemCounter;

import java.util.ArrayList;

public class VibraniumSword extends BaseWeapon {
    public VibraniumSword(){
        super("Vibranium Sword",300,0.5,1500);

    }
    @Override
    public ArrayList<ItemCounter> getRecipe(){
        ArrayList<ItemCounter> recipe = new ArrayList<ItemCounter>();
        recipe.add(new ItemCounter(new Mithril(),30));
        recipe.add(new ItemCounter(new Vibranium(),30));
        return recipe;
    }
}

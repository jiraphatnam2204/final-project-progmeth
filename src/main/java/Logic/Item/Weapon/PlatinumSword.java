package Logic.Item.Weapon;

import Logic.Base.BaseArmor;
import Logic.Base.BaseWeapon;
import Logic.Util.ItemCounter;

import java.util.ArrayList;

public class PlatinumSword extends BaseWeapon {
    public PlatinumSword(){
        super("Platinum Sword",100,0.7,400);

    }
    @Override
    public ArrayList<ItemCounter> getRecipe(){
        ArrayList<ItemCounter> recipe = new ArrayList<ItemCounter>();
        recipe.add(new ItemCounter(new Iron(),10));
        recipe.add(new ItemCounter(new Platinum(),10));
        return recipe;
    }
}

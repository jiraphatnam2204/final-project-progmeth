package Logic.Item.Weapon;

import Logic.Base.BaseArmor;
import Logic.Base.BaseWeapon;
import Logic.Util.ItemCounter;

import java.util.ArrayList;

public class MithrilSword extends BaseWeapon {
    public MithrilSword(){
        super("Mithril Sword",150,0.4,1000);

    }
    @Override
    public ArrayList<ItemCounter> getRecipe(){
        ArrayList<ItemCounter> recipe = new ArrayList<ItemCounter>();
        recipe.add(new ItemCounter(new Platinum(),5));
        recipe.add(new ItemCounter(new Mithril(),20));
        return recipe;
    }
}

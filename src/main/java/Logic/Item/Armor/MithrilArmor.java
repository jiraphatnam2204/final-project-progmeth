package Logic.Item.Armor;

import Logic.Base.BaseArmor;
import Logic.Util.ItemCounter;

import java.util.ArrayList;

public class MithrilArmor extends BaseArmor {
    public MithrilArmor(){
        super("Mithril Armor",0,60,150,0,1000);

    }
    @Override
    public ArrayList<ItemCounter> getRecipe(){
        ArrayList<ItemCounter> recipe = new ArrayList<ItemCounter>();
        recipe.add(new ItemCounter(new Platinum(),5));
        recipe.add(new ItemCounter(new Mithril(),20));
        return recipe;
    }
}

package Logic.Item.Armor;

import Logic.Base.BaseArmor;
import Logic.Util.ItemCounter;

import java.util.ArrayList;

public class HardstoneArmor extends BaseArmor {
    public HardstoneArmor(){
        super("Hardstone Armor",0,10,15,0);
    }
    @Override
    public ArrayList<ItemCounter> getRecipe(){
        ArrayList<ItemCounter> recipe = new ArrayList<ItemCounter>();
        recipe.add(new ItemCounter(new Stone(),5));
        recipe.add(new ItemCounter(new Iron(),5));
    }
}

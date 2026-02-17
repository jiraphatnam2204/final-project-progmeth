package Logic.Item.Weapon;

import Logic.Base.BaseArmor;
import Logic.Base.BaseWeapon;
import Logic.Util.ItemCounter;

import java.util.ArrayList;

public class HardstoneSword extends BaseWeapon {
    public HardstoneSword(){
        super("Hardstone Sword",20,1,50);
    }
    @Override
    public ArrayList<ItemCounter> getRecipe(){
        ArrayList<ItemCounter> recipe = new ArrayList<ItemCounter>();
        recipe.add(new ItemCounter(new Stone(),5));
        recipe.add(new ItemCounter(new HardStone(),10));
        return recipe;
    }

}

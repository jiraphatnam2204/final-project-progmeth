package logic.item.armor;

import logic.base.BaseArmor;
import logic.stone.HardStone;
import logic.stone.NormalStone;
import logic.util.ItemCounter;

import java.util.ArrayList;

public class HardstoneArmor extends BaseArmor {
    public HardstoneArmor(){
        super("Hardstone Armor",0,10,15,-1,50);
    }
    @Override
    public ArrayList<ItemCounter> getRecipe(){
        ArrayList<ItemCounter> recipe = new ArrayList<ItemCounter>();
        recipe.add(new ItemCounter(new NormalStone(),5));
        recipe.add(new ItemCounter(new HardStone(),10));
        return recipe;
    }

}

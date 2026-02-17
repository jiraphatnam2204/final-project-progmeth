package Logic.Item.Armor;

import Logic.Base.BaseArmor;
import Logic.Util.ItemCounter;

import java.util.ArrayList;

public class StoneArmor extends BaseArmor {
    public StoneArmor(){
        super("Stone Armor",0,5,10,0);
    }
    @Override
    public ArrayList<ItemCounter> getRecipe(){
        ItemCounter stone = new ItemCounter(new Stone(),10);
        ArrayList<ItemCounter> recipe = new ArrayList<ItemCounter>();
        recipe.add(stone);
        return recipe;
    }
    @Override
    public boolean canCraft(Player p){
        ArrayList<ItemCounter> recipe = getRecipe();
        int findCount = 0;
        int itemRequire = recipe.size();
        for(ItemCounter it : recipe){
            boolean find = false;
            int cnt = 0;
            for(ItemCounter i : p.getInventory()) {
                if(it.equals(i)) cnt += i.getCount() ;
            }
            if(it.getCount()>cnt) return false;
        }
        return true;
    }
}

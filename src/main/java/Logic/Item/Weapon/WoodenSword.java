package Logic.Item.Weapon;

import Logic.Base.BaseWeapon;
import Logic.Util.ItemCounter;

import java.util.ArrayList;

public class WoodenSword extends BaseWeapon {
    public WoodenSword(){
        super("Wooden Sword",5,0.3,10);
    }

    @Override
    public ArrayList<ItemCounter> getRecipe() {
        return null;
    }
}

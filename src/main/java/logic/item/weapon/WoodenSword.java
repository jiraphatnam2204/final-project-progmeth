package logic.item.weapon;

import logic.base.BaseWeapon;
import logic.util.ItemCounter;

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

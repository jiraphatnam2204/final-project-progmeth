package logic.item.weapon;
import logic.base.BaseWeapon; import logic.stone.NormalStone; import logic.util.ItemCounter;
import java.util.ArrayList;
public class StoneSword extends BaseWeapon {
    public StoneSword() { super("Stone Sword", 15, 1, 20); }
    @Override public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>(); r.add(new ItemCounter(new NormalStone(), 10)); return r;
    }
}

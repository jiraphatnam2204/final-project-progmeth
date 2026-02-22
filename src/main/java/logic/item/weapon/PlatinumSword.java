package logic.item.weapon;
import logic.base.BaseWeapon; import logic.stone.Iron; import logic.stone.Platinum; import logic.util.ItemCounter;
import java.util.ArrayList;
public class PlatinumSword extends BaseWeapon {
    public PlatinumSword() { super("Platinum Sword", 100, 0.7, 400); }
    @Override public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new Iron(), 10)); r.add(new ItemCounter(new Platinum(), 10)); return r;
    }
}

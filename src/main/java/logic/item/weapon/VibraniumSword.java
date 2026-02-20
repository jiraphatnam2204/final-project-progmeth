package logic.item.weapon;
import logic.base.BaseWeapon; import logic.stone.Mithril; import logic.stone.Vibranium; import logic.util.ItemCounter;
import java.util.ArrayList;
public class VibraniumSword extends BaseWeapon {
    public VibraniumSword() { super("Vibranium Sword", 300, 0.5, 1500); }
    @Override public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new Mithril(), 30)); r.add(new ItemCounter(new Vibranium(), 30)); return r;
    }
}

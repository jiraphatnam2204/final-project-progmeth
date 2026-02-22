package logic.item.armor;
import logic.base.BaseArmor; import logic.stone.NormalStone; import logic.util.ItemCounter;
import java.util.ArrayList;
public class StoneArmor extends BaseArmor {
    public StoneArmor() { super("Stone Armor", 0, 5, 10, 0, 10); }
    @Override public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>(); r.add(new ItemCounter(new NormalStone(), 10)); return r;
    }
}

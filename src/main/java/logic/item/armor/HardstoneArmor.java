package logic.item.armor;
import logic.base.BaseArmor; import logic.stone.HardStone; import logic.stone.NormalStone; import logic.util.ItemCounter;
import java.util.ArrayList;
public class HardstoneArmor extends BaseArmor {
    public HardstoneArmor() { super("Hardstone Armor", 0, 10, 15, 0, 50); }
    @Override public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new NormalStone(), 5)); r.add(new ItemCounter(new HardStone(), 10)); return r;
    }
}

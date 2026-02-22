package logic.item.armor;
import logic.base.BaseArmor; import logic.stone.Iron; import logic.stone.NormalStone; import logic.util.ItemCounter;
import java.util.ArrayList;
public class IronArmor extends BaseArmor {
    public IronArmor() { super("Iron Armor", 0, 20, 30, 0, 300); }
    @Override public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new NormalStone(), 10)); r.add(new ItemCounter(new Iron(), 10)); return r;
    }
}

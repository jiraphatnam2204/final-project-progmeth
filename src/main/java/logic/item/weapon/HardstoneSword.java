package logic.item.weapon;
import logic.base.BaseWeapon; import logic.stone.HardStone; import logic.stone.NormalStone; import logic.util.ItemCounter;
import java.util.ArrayList;
public class HardstoneSword extends BaseWeapon {
    public HardstoneSword() { super("Hardstone Sword", 20, 1, 50); }
    @Override public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new NormalStone(), 5)); r.add(new ItemCounter(new HardStone(), 10)); return r;
    }
}

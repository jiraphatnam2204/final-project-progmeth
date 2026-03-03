package logic.item.armor;
import logic.base.BaseArmor; import logic.stone.HardStone; import logic.stone.NormalStone; import logic.util.ItemCounter;
import java.util.ArrayList;
/**
 * Tier-2 craftable armor made from Normal Stone and Hard Stone.
 * Grants +10 DEF, +15 HP. Recipe: 5 Normal Stone + 10 Hard Stone + 50 gold.
 */
public class HardstoneArmor extends BaseArmor {

    /**
     * Creates a new HardstoneArmor with preset stats.
     */
    public HardstoneArmor() { super("Hardstone Armor", 0, 10, 15, 0, 50); }

    /**
     * {@inheritDoc}
     */
    @Override public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new NormalStone(), 5)); r.add(new ItemCounter(new HardStone(), 10)); return r;
    }
}

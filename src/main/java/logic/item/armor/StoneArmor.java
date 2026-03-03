package logic.item.armor;
import logic.base.BaseArmor; import logic.stone.NormalStone; import logic.util.ItemCounter;
import java.util.ArrayList;
/**
 * Tier-1 craftable armor made from Normal Stone.
 * Grants +5 DEF, +10 HP. Recipe: 10 Normal Stone + 10 gold.
 */
public class StoneArmor extends BaseArmor {

    /**
     * Creates a new StoneArmor with preset stats.
     */
    public StoneArmor() { super("Stone Armor", 0, 5, 10, 0, 10); }

    /**
     * {@inheritDoc}
     */
    @Override public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>(); r.add(new ItemCounter(new NormalStone(), 10)); return r;
    }
}

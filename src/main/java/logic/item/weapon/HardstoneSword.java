package logic.item.weapon;
import logic.base.BaseWeapon; import logic.stone.HardStone; import logic.stone.NormalStone; import logic.util.ItemCounter;
import java.util.ArrayList;
/**
 * Tier-2 craftable sword made from Normal Stone and Hard Stone.
 * Grants +20 ATK. Recipe: 5 Normal Stone + 10 Hard Stone + 50 gold.
 */
public class HardstoneSword extends BaseWeapon {

    /**
     * Creates a new HardstoneSword with preset stats.
     */
    public HardstoneSword() { super("Hardstone Sword", 20, 1, 50); }

    /**
     * {@inheritDoc}
     */
    @Override public ArrayList<ItemCounter> getRecipe() {
        ArrayList<ItemCounter> r = new ArrayList<>();
        r.add(new ItemCounter(new NormalStone(), 5)); r.add(new ItemCounter(new HardStone(), 10)); return r;
    }
}

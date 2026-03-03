package logic.item.weapon;
import logic.base.BaseWeapon; import logic.util.ItemCounter;
import java.util.ArrayList;
/**
 * The starter weapon given to the player at the beginning of the game.
 * Cannot be crafted (returns {@code null} recipe). Grants +5 ATK.
 */
public class WoodenSword extends BaseWeapon {

    /**
     * Creates a new WoodenSword with preset stats.
     */
    public WoodenSword() { super("Wooden Sword", 5, 0.3, 10); }

    /**
     * Returns {@code null} because the Wooden Sword cannot be crafted.
     *
     * @return {@code null}
     */
    @Override public ArrayList<ItemCounter> getRecipe() { return null; }
}

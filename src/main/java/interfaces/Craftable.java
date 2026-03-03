package interfaces;

import logic.creatures.Player;
import logic.util.ItemCounter;
import java.util.ArrayList;

/**
 * Represents an item that can be crafted using materials and gold.
 */
public interface Craftable {

    /**
     * Returns the gold cost required to craft this item.
     *
     * @return the crafting price in gold
     */
    int getCraftingPrice();

    /**
     * Returns the list of materials and quantities required to craft this item.
     *
     * @return the recipe as a list of {@link ItemCounter}
     */
    ArrayList<ItemCounter> getRecipe();

    /**
     * Checks whether the player has enough materials and gold to craft this item.
     *
     * @param p the player attempting to craft
     * @return {@code true} if the player can craft this item, {@code false} otherwise
     */
    boolean canCraft(Player p);

    /**
     * Crafts this item, consuming the required materials and gold from the player's inventory.
     * Does nothing if {@link #canCraft(Player)} returns {@code false}.
     *
     * @param p the player who is crafting
     */
    void craft(Player p);
}

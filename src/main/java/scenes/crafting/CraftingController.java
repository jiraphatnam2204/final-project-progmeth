package scenes.crafting;

import interfaces.Craftable;
import logic.base.BaseItem;
import logic.creatures.Player;
import logic.item.armor.*;
import logic.item.weapon.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the crafting station scene.
 * Holds the full list of craftable recipes (all weapons and armors)
 * and delegates craft operations to the underlying {@link Craftable} items.
 */
public class CraftingController {
    /** The player who will be crafting items. */
    private final Player player;

    /** The full list of craftable items available at this station (weapons and armors). */
    private final List<Craftable> recipes = new ArrayList<>();

    /**
     * Creates a new CraftingController and builds the recipe catalogue.
     *
     * @param player the player who will be crafting items
     */
    public CraftingController(Player player) {
        this.player = player;
        buildRecipeList();
    }

    /**
     * Populates the recipe list with all craftable weapons and armors in tier order.
     */
    private void buildRecipeList() {
        recipes.add(new StoneSword());
        recipes.add(new HardstoneSword());
        recipes.add(new IronSword());
        recipes.add(new PlatinumSword());
        recipes.add(new MithrilSword());
        recipes.add(new VibraniumSword());

        recipes.add(new StoneArmor());
        recipes.add(new HardstoneArmor());
        recipes.add(new IronArmor());
        recipes.add(new PlatinumArmor());
        recipes.add(new MithrilArmor());
        recipes.add(new VibraniumArmor());
    }


    /**
     * Returns the full list of craftable recipes available at this station.
     *
     * @return unmodifiable view of the recipe list
     */
    public List<Craftable> getRecipes() {
        return recipes;
    }

    /**
     * Returns the player associated with this crafting session.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Attempts to craft the recipe at the given index.
     * On success, consumes required materials/gold and adds the item to the player's inventory.
     *
     * @param index the index into the recipe list
     * @return a {@link CraftResult} indicating success or failure with a message
     */
    public CraftResult craft(int index) {
        Craftable recipe = recipes.get(index);

        if (recipe.canCraft(player)) {
            recipe.craft(player);
            player.addItem((BaseItem) recipe, 1);

            String itemName = ((BaseItem) recipe).getName();
            return new CraftResult(true, "✓ Crafted " + itemName);
        }

        return new CraftResult(false, "✗ Missing materials or gold!");
    }

    /**
     * Immutable result of a crafting attempt.
     *
     * @param success {@code true} if the craft succeeded
     * @param message a user-facing status message
     */
    public record CraftResult(boolean success, String message) {
    }
}

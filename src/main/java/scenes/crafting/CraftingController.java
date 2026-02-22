package scenes.crafting;

import interfaces.Craftable;
import logic.base.BaseItem;
import logic.creatures.Player;
import logic.item.armor.*;
import logic.item.weapon.*;

import java.util.ArrayList;
import java.util.List;

/**
 * CraftingController — the "brain" of the crafting screen.
 *
 * Responsibility: ONLY game logic. No JavaFX, no colors, no buttons.
 *   - Holds the list of craftable recipes
 *   - Decides if a craft is allowed
 *   - Modifies player state when a craft succeeds
 *   - Returns a result object so the View knows what to display
 */
public class CraftingController {

    // The player whose inventory/gold will be changed on crafting
    private final Player player;

    // All craftable recipes shown in the UI
    private final List<Craftable> recipes = new ArrayList<>();

    public CraftingController(Player player) {
        this.player = player;
        buildRecipeList();
    }

    // ── Recipe list ───────────────────────────────────────────────────────────

    private void buildRecipeList() {
        // Weapons (order matters — determines grid position in the View)
        recipes.add(new StoneSword());
        recipes.add(new HardstoneSword());
        recipes.add(new IronSword());
        recipes.add(new PlatinumSword());
        recipes.add(new MithrilSword());
        recipes.add(new VibraniumSword());

        // Armors
        recipes.add(new StoneArmor());
        recipes.add(new HardstoneArmor());
        recipes.add(new IronArmor());
        recipes.add(new PlatinumArmor());
        recipes.add(new MithrilArmor());
        recipes.add(new VibraniumArmor());
    }

    // ── Public API used by CraftingView ───────────────────────────────────────

    /** Returns the full recipe list so the View can draw the cards. */
    public List<Craftable> getRecipes() {
        return recipes;
    }

    /** Returns the player so the View can display stats (gold, hp, etc.). */
    public Player getPlayer() {
        return player;
    }

    /**
     * Attempts to craft the recipe at position [index].
     *
     * Think of this like pressing a button on a vending machine — the machine
     * (controller) checks if you have enough coins (materials), takes them,
     * and either dispenses the item or says "insufficient funds".
     *
     * @param index position in the recipes list
     * @return a CraftResult telling the View whether it worked and what to show
     */
    public CraftResult craft(int index) {
        Craftable recipe = recipes.get(index);

        if (recipe.canCraft(player)) {
            // canCraft() passes — deduct materials/gold and give item
            recipe.craft(player);
            player.addItem((BaseItem) recipe, 1);

            String itemName = ((BaseItem) recipe).getName();
            return new CraftResult(true, "✓ Crafted " + itemName);
        }

        return new CraftResult(false, "✗ Missing materials or gold!");
    }

    // ── Inner result record ───────────────────────────────────────────────────

    /**
     * A simple "envelope" carrying the result of a craft attempt back to the View.
     *
     * Using a Java record here (Java 16+) is like a tiny immutable data class —
     * perfect for passing structured results without extra boilerplate.
     */
    public record CraftResult(boolean success, String message) {}
}

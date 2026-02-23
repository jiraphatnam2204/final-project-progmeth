package scenes.crafting;

import interfaces.Craftable;
import logic.base.BaseItem;
import logic.creatures.Player;
import logic.item.armor.*;
import logic.item.weapon.*;

import java.util.ArrayList;
import java.util.List;

public class CraftingController {
    private final Player player;

    private final List<Craftable> recipes = new ArrayList<>();

    public CraftingController(Player player) {
        this.player = player;
        buildRecipeList();
    }

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


    public List<Craftable> getRecipes() {
        return recipes;
    }

    public Player getPlayer() {
        return player;
    }

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

    public record CraftResult(boolean success, String message) {
    }
}

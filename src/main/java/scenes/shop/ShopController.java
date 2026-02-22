package scenes.shop;

import logic.creatures.Player;
import logic.item.potion.BigHealthPotion;
import logic.item.potion.MediumHealthPotion;
import logic.item.potion.SmallHealthPotion;
import logic.pickaxe.Pickaxe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * ShopController — sells ONLY potions and pickaxes.
 * <p>
 * Rule: weapons and armor must be obtained through crafting only.
 * This enforces the intended game loop: mine ores → craft gear → fight bosses.
 * The shop is purely a support/upgrade store.
 */
public class ShopController {

    private final Player player;
    private final Pickaxe[] pickaxeHolder;
    private final List<ShopItem> items = new ArrayList<>();

    public ShopController(Player player, Pickaxe[] pickaxeHolder) {
        this.player = player;
        this.pickaxeHolder = pickaxeHolder;
        buildCatalogue();
    }

    private void buildCatalogue() {
        // ── Potions ───────────────────────────────────────────────────────────────
        items.add(new ShopItem("Small Potion", "Heals 40 HP", 25,
                p -> p.addItem(new SmallHealthPotion(), 1)));
        items.add(new ShopItem("Medium Potion", "Heals 100 HP", 70,
                p -> p.addItem(new MediumHealthPotion(), 1)));
        items.add(new ShopItem("Big Potion", "Heals 200 HP", 130,
                p -> p.addItem(new BigHealthPotion(), 1)));

        // ── All Pickaxes (every tier) ─────────────────────────────────────────────
        // Normal Stone Pickaxe is the starting pickaxe — sold cheap as a replacement
        items.add(new ShopItem("Wooden Pick", "Power: 1", 5,
                p -> pickaxeHolder[0] = Pickaxe.createWoodenPickaxe()));
        items.add(new ShopItem("Normal Pick", "Power: 2", 10,
                p -> pickaxeHolder[0] = Pickaxe.createNormalStonePickaxe()));
        items.add(new ShopItem("Hardstone Pick", "Power: 5", 60,
                p -> pickaxeHolder[0] = Pickaxe.createHardStonePickaxe()));
        items.add(new ShopItem("Iron Pickaxe", "Power: 12", 150,
                p -> pickaxeHolder[0] = Pickaxe.createIronPickaxe()));
        items.add(new ShopItem("Platinum Pick", "Power: 27", 400,
                p -> pickaxeHolder[0] = Pickaxe.createPlatinumPickaxe()));
        items.add(new ShopItem("Mithril Pick", "Power: 45", 1000,
                p -> pickaxeHolder[0] = Pickaxe.createMithrilPickaxe()));
        items.add(new ShopItem("Vibranium Pick", "Power: 100", 2500,
                p -> pickaxeHolder[0] = Pickaxe.createVibraniumPickaxe()));
    }

    public List<ShopItem> getItems() {
        return items;
    }

    public Player getPlayer() {
        return player;
    }

    public Pickaxe[] getPickaxeHolder() {
        return pickaxeHolder;
    }

    public BuyResult buy(ShopItem item) {
        if (player.getGold() >= item.price()) {
            player.setGold(player.getGold() - item.price());
            item.onBuy().accept(player);
            return new BuyResult(true, "✓ Bought " + item.name() + "!");
        }
        return new BuyResult(false, "✗ Not enough gold! (need " + item.price() + "g)");
    }

    public record ShopItem(String name, String description, int price, Consumer<Player> onBuy) {
    }

    public record BuyResult(boolean success, String message) {
    }
}
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
 * Controller for the in-game shop scene.
 * Sells potions and pickaxes only — weapons and armor must be crafted.
 * This enforces the intended game loop: mine ores → craft gear → fight bosses.
 */
public class ShopController {

    /** The player who is shopping. */
    private final Player player;

    /** Single-element array holding the player's active pickaxe; replaced in-place on purchase. */
    private final Pickaxe[] pickaxeHolder;

    /** The list of items available for purchase in this shop session. */
    private final List<ShopItem> items = new ArrayList<>();

    /**
     * Creates a new ShopController and builds the item catalogue.
     *
     * @param player        the player who is shopping
     * @param pickaxeHolder a single-element array holding the player's active pickaxe;
     *                      replaced in-place when a new pickaxe is purchased
     */
    public ShopController(Player player, Pickaxe[] pickaxeHolder) {
        this.player = player;
        this.pickaxeHolder = pickaxeHolder;
        buildCatalogue();
    }

    /**
     * Populates the shop with all available items: potions and pickaxes of every tier.
     */
    private void buildCatalogue() {
        // ── Potions ───────────────────────────────────────────────────────────────
        items.add(new ShopItem("Small Potion", "Heals 40 HP", 50,
                p -> p.addItem(new SmallHealthPotion(), 1)));
        items.add(new ShopItem("Medium Potion", "Heals 100 HP", 100,
                p -> p.addItem(new MediumHealthPotion(), 1)));
        items.add(new ShopItem("Big Potion", "Heals 200 HP", 200,
                p -> p.addItem(new BigHealthPotion(), 1)));

        // ── All Pickaxes (every tier) ─────────────────────────────────────────────
        // Normal Stone Pickaxe is the starting pickaxe — sold cheap as a replacement
        items.add(new ShopItem("Wooden Pick", "Power: 1", 5,
                p -> pickaxeHolder[0] = Pickaxe.createWoodenPickaxe()));
        items.add(new ShopItem("Normal Pick", "Power: 2", 10,
                p -> pickaxeHolder[0] = Pickaxe.createNormalStonePickaxe()));
        items.add(new ShopItem("Hardstone Pick", "Power: 5", 50,
                p -> pickaxeHolder[0] = Pickaxe.createHardStonePickaxe()));
        items.add(new ShopItem("Iron Pickaxe", "Power: 12", 100,
                p -> pickaxeHolder[0] = Pickaxe.createIronPickaxe()));
        items.add(new ShopItem("Platinum Pick", "Power: 27", 160,
                p -> pickaxeHolder[0] = Pickaxe.createPlatinumPickaxe()));
        items.add(new ShopItem("Mithril Pick", "Power: 45", 230,
                p -> pickaxeHolder[0] = Pickaxe.createMithrilPickaxe()));
        items.add(new ShopItem("Vibranium Pick", "Power: 100", 310,
                p -> pickaxeHolder[0] = Pickaxe.createVibraniumPickaxe()));
    }

    /**
     * Returns the list of items available for purchase in the shop.
     *
     * @return the shop catalogue
     */
    public List<ShopItem> getItems() {
        return items;
    }

    /**
     * Returns the player associated with this shop session.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the pickaxe holder array (a reference shared with the game world).
     *
     * @return the single-element pickaxe holder
     */
    public Pickaxe[] getPickaxeHolder() {
        return pickaxeHolder;
    }

    /**
     * Attempts to purchase the given item.
     * Deducts the price from the player's gold and applies the item's effect on success.
     *
     * @param item the item the player wants to buy
     * @return a {@link BuyResult} indicating success or failure with a message
     */
    public BuyResult buy(ShopItem item) {
        if (player.getGold() >= item.price()) {
            player.setGold(player.getGold() - item.price());
            item.onBuy().accept(player);
            return new BuyResult(true, "✓ Bought " + item.name() + "!");
        }
        return new BuyResult(false, "✗ Not enough gold! (need " + item.price() + "g)");
    }

    /**
     * Immutable data holder for a shop catalogue entry.
     *
     * @param name        the display name of the item
     * @param description a short description shown on the item card
     * @param price       the gold cost
     * @param onBuy       the action applied to the player when purchased
     */
    public record ShopItem(String name, String description, int price, Consumer<Player> onBuy) {
    }

    /**
     * Immutable result of a purchase attempt.
     *
     * @param success {@code true} if the purchase succeeded
     * @param message a user-facing status message
     */
    public record BuyResult(boolean success, String message) {
    }
}
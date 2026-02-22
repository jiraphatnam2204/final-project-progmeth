package scenes.inventory;

import logic.base.BaseArmor;
import logic.base.BaseItem;
import logic.base.BasePotion;
import logic.base.BaseWeapon;
import logic.creatures.Player;
import logic.util.ItemCounter;

import java.util.List;

/**
 * InventoryController — the "brain" of the inventory screen.
 * <p>
 * Responsibility: ONLY inventory management logic.
 * - Tracks which page of items is shown (pagination)
 * - Handles equip/unequip of weapons and armors
 * - Handles using potions
 * - Returns results so the View knows what happened
 */
public class InventoryController {

    // How many items fit on one page of the inventory list
    public static final int ITEMS_PER_PAGE = 8;

    private final Player player;

    // Which page we're on (0-indexed, so page 0 = items 0–7, page 1 = items 8–15, etc.)
    private int currentPage = 0;

    public InventoryController(Player player) {
        this.player = player;
    }

    // ── Pagination ────────────────────────────────────────────────────────────

    public int getCurrentPage() {
        return currentPage;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * Total number of pages, always at least 1.
     */
    public int getTotalPages() {
        int size = player.getInventory().size();
        if (size == 0) return 1;
        return (int) Math.ceil((double) size / ITEMS_PER_PAGE);
    }

    /**
     * Move to the next page (if there is one).
     */
    public void nextPage() {
        if (currentPage < getTotalPages() - 1) currentPage++;
    }

    /**
     * Move to the previous page (if there is one).
     */
    public void prevPage() {
        if (currentPage > 0) currentPage--;
    }

    /**
     * Returns the slice of items visible on the current page.
     * Think of it like slicing a loaf of bread — you only see one slice at a time.
     */
    public List<ItemCounter> getPageItems() {
        // Clamp page in case inventory shrank (e.g. after using last potion)
        clampPage();
        List<ItemCounter> inv = player.getInventory();
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, inv.size());
        return inv.subList(start, end);
    }

    /**
     * Makes sure currentPage stays within valid range.
     */
    public void clampPage() {
        int total = getTotalPages();
        if (currentPage >= total) currentPage = total - 1;
        if (currentPage < 0) currentPage = 0;
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Equips the given weapon on the player.
     */
    public void equipWeapon(BaseWeapon weapon) {
        player.equipWeapon(weapon);
    }

    /**
     * Equips the given armor on the player.
     */
    public void equipArmor(BaseArmor armor) {
        player.equipArmor(armor);
    }

    /**
     * Unequips the player's current weapon (if any).
     */
    public void unequipWeapon() {
        if (player.getEquippedWeapon() != null) player.unequipWeapon();
    }

    /**
     * Unequips the player's current armor (if any).
     */
    public void unequipArmor() {
        if (player.getEquippedArmor() != null) player.unequipArmor();
    }

    /**
     * Uses a potion from the given ItemCounter slot.
     * Decrements count and removes the slot when empty.
     */
    public void usePotion(ItemCounter counter) {
        if (counter.getItem() instanceof BasePotion potion) {
            potion.consume(player);
            counter.addCount(-1);
            if (counter.getCount() <= 0) {
                player.getInventory().remove(counter);
            }
        }
    }

    // ── Helpers for View to build display strings ─────────────────────────────

    /**
     * Returns the stat suffix shown next to an item name.
     * e.g. "(+atk 15)" or "✅" if already equipped.
     */
    public String buildStatSuffix(BaseItem item) {
        if (item instanceof BaseWeapon w) {
            boolean equipped = player.getEquippedWeapon() != null
                    && player.getEquippedWeapon().getName().equals(item.getName());
            return equipped ? "  ✅" : "  (+atk " + w.getDmg() + ")";
        }
        if (item instanceof BaseArmor a) {
            boolean equipped = player.getEquippedArmor() != null
                    && player.getEquippedArmor().getName().equals(item.getName());
            return equipped ? "  ✅" : "  (+def " + a.getDef() + ")";
        }
        return "";
    }

    /**
     * Returns true if the given weapon is currently equipped.
     */
    public boolean isWeaponEquipped(BaseWeapon weapon) {
        return player.getEquippedWeapon() != null
                && player.getEquippedWeapon().getName().equals(weapon.getName());
    }

    /**
     * Returns true if the given armor is currently equipped.
     */
    public boolean isArmorEquipped(BaseArmor armor) {
        return player.getEquippedArmor() != null
                && player.getEquippedArmor().getName().equals(armor.getName());
    }
}

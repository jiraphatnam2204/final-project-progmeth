package scenes.inventory;

import logic.base.BaseArmor;
import logic.base.BaseItem;
import logic.base.BasePotion;
import logic.base.BaseWeapon;
import logic.creatures.Player;
import logic.util.ItemCounter;

import java.util.List;

/**
 * Controller for the inventory overlay scene.
 * Manages pagination of the player's item list and delegates equip/use actions
 * to the underlying {@link Player}.
 */
public class InventoryController {

    /** Number of items shown on a single inventory page. */
    public static final int ITEMS_PER_PAGE = 8;
    private final Player player;
    private int currentPage = 0;

    /**
     * Creates a new InventoryController for the given player.
     *
     * @param player the player whose inventory will be managed
     */
    public InventoryController(Player player) {
        this.player = player;
    }

    /**
     * Returns the zero-based index of the currently displayed page.
     *
     * @return the current page index
     */
    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Returns the player associated with this inventory session.
     *
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Returns the total number of pages needed to display the player's inventory.
     * Always returns at least {@code 1} even when the inventory is empty.
     *
     * @return the total page count
     */
    public int getTotalPages() {
        int size = player.getInventory().size();
        if (size == 0) return 1;
        return (int) Math.ceil((double) size / ITEMS_PER_PAGE);
    }

    /**
     * Advances to the next page if one exists.
     */
    public void nextPage() {
        if (currentPage < getTotalPages() - 1) currentPage++;
    }

    /**
     * Goes back to the previous page if one exists.
     */
    public void prevPage() {
        if (currentPage > 0) currentPage--;
    }

    /**
     * Returns the slice of the inventory items that belong to the current page.
     *
     * @return a sub-list of {@link ItemCounter}s for the current page
     */
    public List<ItemCounter> getPageItems() {
        clampPage();
        List<ItemCounter> inv = player.getInventory();
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, inv.size());
        return inv.subList(start, end);
    }

    /**
     * Ensures {@code currentPage} stays within the valid range {@code [0, totalPages-1]}.
     */
    public void clampPage() {
        int total = getTotalPages();
        if (currentPage >= total) currentPage = total - 1;
        if (currentPage < 0) currentPage = 0;
    }

    /**
     * Equips the given weapon on the player.
     *
     * @param weapon the weapon to equip
     */
    public void equipWeapon(BaseWeapon weapon) {
        player.equipWeapon(weapon);
    }

    /**
     * Equips the given armor on the player.
     *
     * @param armor the armor to equip
     */
    public void equipArmor(BaseArmor armor) {
        player.equipArmor(armor);
    }

    /**
     * Unequips the player's currently equipped weapon, if any.
     */
    public void unequipWeapon() {
        if (player.getEquippedWeapon() != null) player.unequipWeapon();
    }

    /**
     * Unequips the player's currently equipped armor, if any.
     */
    public void unequipArmor() {
        if (player.getEquippedArmor() != null) player.unequipArmor();
    }

    /**
     * Consumes the potion in the given counter, applying its effect to the player.
     * Decrements the stack count and removes the entry from the inventory when the
     * count reaches zero.
     *
     * @param counter the inventory entry holding the potion to use
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

    /**
     * Builds a short display suffix showing the item's stat bonus or equipped status.
     * Returns {@code "  ✅"} if the item is currently equipped, or {@code "  (+atk N)"}
     * / {@code "  (+def N)"} for unequipped weapons/armors. Returns an empty string
     * for all other item types.
     *
     * @param item the item to build a suffix for
     * @return the stat suffix string
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
     * Returns whether the given weapon is currently equipped by the player.
     *
     * @param weapon the weapon to check
     * @return {@code true} if the weapon is equipped
     */
    public boolean isWeaponEquipped(BaseWeapon weapon) {
        return player.getEquippedWeapon() != null
                && player.getEquippedWeapon().getName().equals(weapon.getName());
    }

    /**
     * Returns whether the given armor is currently equipped by the player.
     *
     * @param armor the armor to check
     * @return {@code true} if the armor is equipped
     */
    public boolean isArmorEquipped(BaseArmor armor) {
        return player.getEquippedArmor() != null
                && player.getEquippedArmor().getName().equals(armor.getName());
    }
}
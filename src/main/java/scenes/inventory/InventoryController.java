package scenes.inventory;

import logic.base.BaseArmor;
import logic.base.BaseItem;
import logic.base.BasePotion;
import logic.base.BaseWeapon;
import logic.creatures.Player;
import logic.util.ItemCounter;

import java.util.List;

public class InventoryController {

    public static final int ITEMS_PER_PAGE = 8;
    private final Player player;
    private int currentPage = 0;

    public InventoryController(Player player) {
        this.player = player;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public Player getPlayer() {
        return player;
    }

    public int getTotalPages() {
        int size = player.getInventory().size();
        if (size == 0) return 1;
        return (int) Math.ceil((double) size / ITEMS_PER_PAGE);
    }

    public void nextPage() {
        if (currentPage < getTotalPages() - 1) currentPage++;
    }

    public void prevPage() {
        if (currentPage > 0) currentPage--;
    }

    // Retrieves only the specific items meant to be shown on the current screen.
    // Think of the total inventory like a long book. This method grabs the text for just
    // one specific page (based on currentPage) so the UI doesn't have to load everything at once.
    public List<ItemCounter> getPageItems() {
        clampPage();
        List<ItemCounter> inv = player.getInventory();
        int start = currentPage * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, inv.size());
        return inv.subList(start, end);
    }

    public void clampPage() {
        int total = getTotalPages();
        if (currentPage >= total) currentPage = total - 1;
        if (currentPage < 0) currentPage = 0;
    }

    public void equipWeapon(BaseWeapon weapon) {
        player.equipWeapon(weapon);
    }

    public void equipArmor(BaseArmor armor) {
        player.equipArmor(armor);
    }

    public void unequipWeapon() {
        if (player.getEquippedWeapon() != null) player.unequipWeapon();
    }

    public void unequipArmor() {
        if (player.getEquippedArmor() != null) player.unequipArmor();
    }

    // Applies the potion's effect to the player and reduces the item stack count.
    // This is like taking a bottle of water out of a 6-pack. If you drink the very last one
    // (count <= 0), the method completely throws away the empty box (removes it from the list).
    public void usePotion(ItemCounter counter) {
        if (counter.getItem() instanceof BasePotion potion) {
            potion.consume(player);
            counter.addCount(-1);
            if (counter.getCount() <= 0) {
                player.getInventory().remove(counter);
            }
        }
    }

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

    public boolean isWeaponEquipped(BaseWeapon weapon) {
        return player.getEquippedWeapon() != null
                && player.getEquippedWeapon().getName().equals(weapon.getName());
    }

    public boolean isArmorEquipped(BaseArmor armor) {
        return player.getEquippedArmor() != null
                && player.getEquippedArmor().getName().equals(armor.getName());
    }
}
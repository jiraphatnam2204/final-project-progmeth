package interfaces;

import logic.creatures.Player;

/**
 * Represents an item that can be equipped and unequipped by the player,
 * such as a weapon or armor.
 */
public interface Equipable {

    /**
     * Applies this item's stat bonuses to the given player.
     *
     * @param p the player who equips this item
     */
    void equip(Player p);

    /**
     * Removes this item's stat bonuses from the given player.
     *
     * @param p the player who unequips this item
     */
    void unequip(Player p);
}

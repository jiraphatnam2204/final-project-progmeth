package interfaces;

import logic.creatures.Player;

/**
 * Represents an item that can be consumed by the player, such as a potion.
 */
public interface Consumable {

    /**
     * Applies this item's effect to the given player.
     *
     * @param p the player who consumes this item
     */
    void consume(Player p);
}

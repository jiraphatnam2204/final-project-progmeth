package interfaces;

import logic.base.BaseItem;
import logic.creatures.Player;
import java.util.List;

/**
 * Represents a stone or resource node that can be mined by the player.
 */
public interface Mineable {

    /**
     * Mines this node with the given power level and grants items to the player.
     *
     * @param minePower the mining power of the player's pickaxe
     * @param player    the player performing the mining action
     * @return the list of items obtained from mining
     */
    List<BaseItem> mine(int minePower, Player player);

    /**
     * Returns whether this node is fully depleted and can no longer be mined.
     *
     * @return {@code true} if broken, {@code false} otherwise
     */
    boolean isBroken();

    /**
     * Returns the current durability of this node.
     *
     * @return current durability
     */
    int getDurability();

    /**
     * Returns the maximum durability of this node.
     *
     * @return maximum durability
     */
    int getMaxDurability();
}

package logic.stone;
import interfaces.Mineable; import logic.base.BaseItem; import logic.creatures.Player;
import java.util.ArrayList; import java.util.List;
/**
 * Abstract base class for all mineable ore and stone nodes in the game world.
 * Each node has a fixed durability that is reduced each time it is mined.
 * When durability reaches zero the node is broken and drops items to the player.
 */
public abstract class baseStone extends BaseItem implements Mineable {
    protected int durability; protected final int maxDurability; protected final int dropAmount;

    /**
     * Creates a new stone node with the given name, durability, and drop amount.
     *
     * @param name          the display name of this ore type
     * @param maxDurability the number of mining hits required to break this node
     * @param dropAmount    the number of item drops produced when the node breaks
     */
    protected baseStone(String name, int maxDurability, int dropAmount) {
        super(name); this.maxDurability = maxDurability; durability = maxDurability; this.dropAmount = dropAmount;
    }
    /**
     * {@inheritDoc}
     * Reduces this node's durability by {@code minePower} (minimum 1).
     * Returns the list of dropped items when the node breaks; otherwise returns an empty list.
     */
    @Override public List<BaseItem> mine(int minePower, Player player) {
        if (isBroken()) return List.of();
        durability -= Math.max(1, minePower);
        if (durability <= 0) {
            List<BaseItem> drops = dropItems();
            for (BaseItem item : drops) player.addItem(item, 1);
            return drops;
        }
        return List.of();
    }
    /**
     * Generates the list of items dropped when this node is fully mined.
     *
     * @return a list containing {@code dropAmount} items created by {@link #createItem()}
     */
    protected List<BaseItem> dropItems() {
        List<BaseItem> drops = new ArrayList<>();
        for (int i = 0; i < dropAmount; i++) drops.add(createItem());
        return drops;
    }
    /**
     * Factory method that creates one instance of the ore item dropped by this node.
     *
     * @return a new {@link BaseItem} representing the ore
     */
    protected abstract BaseItem createItem();
    /** {@inheritDoc} */
    @Override public boolean isBroken() { return durability <= 0; }

    /** {@inheritDoc} */
    @Override public int getDurability() { return durability; }

    /** {@inheritDoc} */
    @Override public int getMaxDurability() { return maxDurability; }
}

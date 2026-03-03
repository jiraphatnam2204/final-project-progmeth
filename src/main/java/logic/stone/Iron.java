package logic.stone;

import logic.base.BaseItem;

/**
 * An iron ore node found in the game world (uncommon rarity).
 * Durability: 36, drops 3 Iron items when broken.
 */
public class Iron extends baseStone {

    /**
     * Creates a new Iron ore node.
     */
    public Iron() {
        super("Iron", 36, 3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BaseItem createItem() {
        return new BaseItem("Iron");
    }
}

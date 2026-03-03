package logic.stone;

import logic.base.BaseItem;

/**
 * A mithril ore node found in the game world (rare rarity).
 * Durability: 120, drops 3 Mithril items when broken.
 */
public class Mithril extends baseStone {

    /**
     * Creates a new Mithril ore node.
     */
    public Mithril() {
        super("Mithril", 120, 3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BaseItem createItem() {
        return new BaseItem("Mithril");
    }
}

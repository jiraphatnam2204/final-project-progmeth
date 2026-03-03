package logic.stone;

import logic.base.BaseItem;

/**
 * A platinum ore node found in the game world (rare rarity).
 * Durability: 80, drops 3 Platinum items when broken.
 */
public class Platinum extends baseStone {

    /**
     * Creates a new Platinum ore node.
     */
    public Platinum() {
        super("Platinum", 80, 3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BaseItem createItem() {
        return new BaseItem("Platinum");
    }
}

package logic.stone;

import logic.base.BaseItem;

/**
 * A common stone ore node found throughout the game world.
 * Durability: 5, drops 1 Normal Stone item when broken.
 */
public class NormalStone extends baseStone {

    /**
     * Creates a new NormalStone node.
     */
    public NormalStone() {
        super("Normal Stone", 5, 1);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BaseItem createItem() {
        return new BaseItem("Normal Stone");
    }
}

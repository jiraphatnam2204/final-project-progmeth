package logic.stone;

import logic.base.BaseItem;

/**
 * A vibranium ore node found in the game world (very rare rarity).
 * Durability: 210, drops 3 Vibranium items when broken.
 */
public class Vibranium extends baseStone {

    /**
     * Creates a new Vibranium ore node.
     */
    public Vibranium() {
        super("Vibranium", 210, 3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BaseItem createItem() {
        return new BaseItem("Vibranium");
    }
}

package logic.stone;
import logic.base.BaseItem;
/**
 * A tougher stone ore node found in the game world.
 * Durability: 15, drops 2 Hard Stone items when broken.
 */
public class HardStone extends baseStone {

    /**
     * Creates a new HardStone node.
     */
    public HardStone() { super("Hard Stone", 15, 2); }

    /**
     * {@inheritDoc}
     */
    @Override protected BaseItem createItem() { return new BaseItem("Hard Stone"); }
}

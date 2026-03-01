package logic.stone;
import logic.base.BaseItem;
public class HardStone extends baseStone {
    public HardStone() { super("HardStone", 15, 2); }
    @Override protected BaseItem createItem() { return new BaseItem("HardStone"); }
}

package logic.stone;
import logic.base.BaseItem;
public class Platinum extends baseStone {
    public Platinum() { super("Platinum", 80, 1); }
    @Override protected BaseItem createItem() { return new BaseItem("Platinum"); }
}

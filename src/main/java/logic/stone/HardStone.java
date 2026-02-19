package logic.stone;

import logic.base.BaseItem;

public class HardStone extends baseStone {

    public HardStone() {
        super("Hard Stone", 15, 2);
    }

    @Override
    protected BaseItem createItem() {
        return new BaseItem("HardStone");
    }
}
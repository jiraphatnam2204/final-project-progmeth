package logic.stone;

import logic.item.BaseItem;

public class HardStone extends baseStone {

    public HardStone() {
        super(15, 1);
    }

    @Override
    protected BaseItem createItem() {
        return new BaseItem("HardStone");
    }
}
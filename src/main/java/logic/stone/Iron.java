package logic.stone;

import logic.base.BaseItem;

public class Iron extends baseStone {

    public Iron() {
        super("Iron", 35, 3);
    }

    @Override
    protected BaseItem createItem() {
        return new BaseItem("Iron");
    }
}
package logic.stone;

import logic.item.BaseItem;

public class Iron extends baseStone {

    public Iron() {
        super(35, 1);
    }

    @Override
    protected BaseItem createItem() {
        return new BaseItem("Iron");
    }
}
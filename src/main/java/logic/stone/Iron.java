package logic.stone;

import logic.item.BaseItem;

public class Iron extends Stone {

    public Iron() {
        super(35, 1);
    }

    @Override
    protected BaseItem createItem() {
        return new BaseItem("Iron");
    }
}
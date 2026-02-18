package logic.stone;

import logic.item.BaseItem;

public class NormalStone extends baseStone {

    public NormalStone() {
        super(5, 1);
    }

    @Override
    protected BaseItem createItem() {
        return new BaseItem("Stone");
    }
}
package logic.stone;

import logic.base.BaseItem;

public class NormalStone extends baseStone {

    public NormalStone() {
        super("Normal Stone", 5, 1);
    }

    @Override
    protected BaseItem createItem() {
        return new BaseItem("Stone");
    }
}
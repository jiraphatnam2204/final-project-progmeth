package logic.stone;

import logic.base.BaseItem;

public class Vibranium extends baseStone {
    public Vibranium() {
        super("Vibranium", 180, 1);
    }

    @Override
    protected BaseItem createItem() {
        return new BaseItem("Vibranium");
    }
}
